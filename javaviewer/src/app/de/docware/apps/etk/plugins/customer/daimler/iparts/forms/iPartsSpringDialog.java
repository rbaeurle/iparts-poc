/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiSplitPane;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.collections.dwlist.DwList;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog für die Anzeige der Federbeine, Federn und Beilagen zu einer Datenkarte
 */
public class iPartsSpringDialog extends AbstractJavaViewerForm {

    public static final String CONFIG_KEY_SPRING_LEG_MAPPING_DATA = "Plugin/iPartsEdit/FilterSpringLegMapping";

    // Mode ("viewing"/"edit") = (true/false)
    // Steuerung um die Datenfelder editierbar zu setzen und dann Änderungen zurückzugeben.
    private final boolean isInViewingMode;

    // Die "Dialoge" mit den beiden Textfeldern für die einzelnen Listeninhalte:
    private iPartsSpringProperties springShimRearProperties;
    private iPartsSpringLegsInfoGUIData frontGUIData;
    private iPartsSpringLegsInfoGUIData rearGUIData;

    /**
     * Funktion, die diesen Dialog anzeigt.
     *
     * @param dataConnector
     * @param parentForm
     */
    public static boolean showSpringDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           String titleAddition, iPartsAdminFilterValuesPanel.SpringFields springFields, Boolean isViewing) {
        iPartsSpringDialog dlg = new iPartsSpringDialog(dataConnector, parentForm, titleAddition, springFields, isViewing);


        if (dlg.showModal() == ModalResult.OK) {
            // Im EDIT-Mode die geänderten Daten wieder aus dem Dialog fischen.
            springFields.setActiveSpringLegFront(dlg.getFrontGUIData().getSpringLegProperties().getValues());
            springFields.setActiveSpringRear(dlg.getRearGUIData().getSpringLegProperties().getValues());
            springFields.setActiveSpringShimRear(dlg.springShimRearProperties.getValues());
            return true;
        }
        return false;
    }

    /**
     * Erzeugt eine Instanz von iPartsSpringDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsSpringDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                              String titleAddition, iPartsAdminFilterValuesPanel.SpringFields springFields, Boolean isViewing) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);

        isInViewingMode = isViewing;
        postCreateGui(springFields);
        String msg = TranslationHandler.translate("!!Anzeige Federbeine, Federn und Beilagen zur Datenkarte %1", titleAddition);
        mainWindow.title.setTitle(msg);

    }

    /**
     * Hier kann eigener Code stehen der ausgeführt wird wenn die Instanz erzeugt wurde.
     */
    private void postCreateGui(iPartsAdminFilterValuesPanel.SpringFields springFields) {
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
        mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.OK, !isInViewingMode);
        if (isInViewingMode) {
            // Hier wird auf dem "Cancel"-Button das "OK" angezeigt.
            mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setText("!!OK");
        } else {
            mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setText("!!Abbrechen");
        }
        // Vordere Federbeine
        // VPD: 00064: ZB-Federbein vorn
        initFrontGUI(springFields);
        // Hintere Federbeine
        // VPD: 00065: Feder hinten
        initRearGUI(springFields);
        // Die Divider Positionen anpassen und verknüpfen
        initDividerPositions();
        // Zusatzmaterialien zum hinteren Federbein
        // VPD: 00066: Federbeilage hinten
        initShimRear(springFields);
        // Größe des Dialogs anpassen
        setFormSize();
    }

    /**
     * Setzt die Größe für den Federfilter Dialog
     */
    private void setFormSize() {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        int newHeight = (int)(screenSize.height * 0.8);
        mainWindow.setSize((int)(screenSize.width * 0.8), newHeight);
        mainWindow.splitpaneAll.setDividerPosition((int)(newHeight * 0.7));
    }

    /**
     * Initialisiert die GUI für die Zusatzmaterialien zum hinteren Federbein
     *
     * @param springFields
     */
    private void initShimRear(iPartsAdminFilterValuesPanel.SpringFields springFields) {
        springShimRearProperties = new iPartsSpringProperties(getConnector(), this, isInViewingMode,
                                                              iPartsSpringProperties.TYPE.springShimRear);
        mainWindow.splitpaneRearChildSpringShimRear.addChild(springShimRearProperties.getGui());
        springShimRearProperties.setValues(springFields.activeSpringShimRear);
    }

    /**
     * Initialisiert die GUI für die vorderen Federbein
     *
     * @param springFields
     */
    private void initFrontGUI(iPartsAdminFilterValuesPanel.SpringFields springFields) {
        iPartsSpringProperties springLegFrontProperties = new iPartsSpringProperties(getConnector(), this, isInViewingMode,
                                                                                     iPartsSpringProperties.TYPE.springLegFront);
        frontGUIData = new iPartsSpringLegsInfoGUIData(createInitGUIDataForFrontLegs(springLegFrontProperties, springFields.activeSpringLegFront));

    }

    /**
     * Erstellt ein {@link de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsSpringLegsInfoGUIData.InitDataForSpringGUI}
     * Objekt mit allen Informationen für die Erstellung der GUI für die vorderen Federbeine (Grid und Textfeld)
     *
     * @param springLegProperties
     * @param activeSpringLegFront
     * @return
     */
    private iPartsSpringLegsInfoGUIData.InitDataForSpringGUI createInitGUIDataForFrontLegs(iPartsSpringProperties springLegProperties,
                                                                                           List<String> activeSpringLegFront) {
        iPartsSpringLegsInfoGUIData.InitDataForSpringGUI initData = new iPartsSpringLegsInfoGUIData.InitDataForSpringGUI();
        initData.springProperties = springLegProperties;
        initData.gridParentPanel = mainWindow.springLegFrontGrid;
        initData.textInputParentPanel = mainWindow.springLegFrontTextInput;
        initData.activeSpringLegPartNumbers = activeSpringLegFront;
        initData.displayFields = getDiplayFields(CONFIG_KEY_SPRING_LEG_MAPPING_DATA);
        initData.title = "!!Federn zum vorderen Federbein";
        initData.namePrefix = "Front";
        return initData;
    }

    /**
     * Initialisiert die GUI für die hinteren Federbein
     *
     * @param springFields
     */
    private void initRearGUI(iPartsAdminFilterValuesPanel.SpringFields springFields) {
        iPartsSpringProperties springLegRearProperties = new iPartsSpringProperties(getConnector(), this, isInViewingMode,
                                                                                    iPartsSpringProperties.TYPE.springLegRear);
        rearGUIData = new iPartsSpringLegsInfoGUIData(createInitGUIDataForRearLegs(springLegRearProperties, springFields.activeSpringRear));
    }

    /**
     * Erstellt ein {@link de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsSpringLegsInfoGUIData.InitDataForSpringGUI}
     * Objekt mit allen Informationen für die Erstellung der GUI für die hinteren Federbeine (Grid und Textfeld)
     *
     * @param springLegProperties
     * @param activeSpringLegFront
     * @return
     */
    private iPartsSpringLegsInfoGUIData.InitDataForSpringGUI createInitGUIDataForRearLegs(iPartsSpringProperties springLegProperties,
                                                                                          List<String> activeSpringLegFront) {
        iPartsSpringLegsInfoGUIData.InitDataForSpringGUI initData = new iPartsSpringLegsInfoGUIData.InitDataForSpringGUI();
        initData.springProperties = springLegProperties;
        initData.gridParentPanel = mainWindow.springLegRearGrid;
        initData.textInputParentPanel = mainWindow.springLegRearTextInput;
        initData.activeSpringLegPartNumbers = activeSpringLegFront;
        initData.displayFields = getDiplayFields(CONFIG_KEY_SPRING_LEG_MAPPING_DATA);
        initData.title = "!!Federn zum hinteren Federbein";
        initData.namePrefix = "Rear";
        return initData;
    }

    /**
     * Initialisiert die Positionen der Divider
     */
    private void initDividerPositions() {
        // Die waagerechte SplitPane mittig platzieren
        mainWindow.splitpaneMain.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.splitpaneMain) {
            @Override
            public void fireOnce(Event event) {
                int value = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                mainWindow.splitpaneMain.setDividerPosition(value / 2);
            }
        });
        linkDividerForInnerSplitPane(mainWindow.splitpaneFront, mainWindow.splitpaneRear);
        linkDividerForInnerSplitPane(mainWindow.splitpaneRear, mainWindow.splitpaneFront);
    }


    /**
     * Verknüpft die Divider der übergebenen Splitpanes
     *
     * @param splitpaneForListeners
     * @param splitpaneForChangedDivider
     */
    private void linkDividerForInnerSplitPane(GuiSplitPane splitpaneForListeners, GuiSplitPane splitpaneForChangedDivider) {

        // Die senkrechte SplitPane im oberen Fenster mittig platzieren.
        splitpaneForListeners.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, splitpaneForListeners) {
            @Override
            public void fireOnce(Event event) {
                int value = event.getIntParameter(Event.EVENT_PARAMETER_NEWWIDTH);
                splitpaneForListeners.setDividerPosition(value / 4);
            }
        });
        // Die senkrechte SplitPane im oberen Fenster mittig platzieren.
        splitpaneForListeners.addEventListener(new EventListener(Event.SPLITPANE_DIVIDER_MOVING_EVENT) {
            @Override
            public void fire(Event event) {
                splitpaneForChangedDivider.setDividerPosition(event.getIntParameter(Event.SPLITPANE_LEFT_SIZE));
            }
        });
    }

    /**
     * Funktion, die die anzuzeigenden Spalten 1. aus der Konfiguration liest.
     * Ist nichts eingestellt, werden 2. die Default Anzeigespalten zurückgegeben.
     *
     * @param configKey
     * @return
     */
    protected EtkDisplayFields getDiplayFields(String configKey) {
        // 1. Versuch, die anzuzeigenden Spalten aus der Konfiguration zu ermitteln.
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.load(getConfig(), configKey);
        // 2. Nichts konfiguriert ==> die Default-Spalten zurückliefern.
        if (displayResultFields.size() == 0) {
            List<EtkDisplayField> defaultDisplayFields = createDefaultDisplayFields();
            if (defaultDisplayFields != null) {
                for (EtkDisplayField defaultDisplayField : defaultDisplayFields) {
                    displayResultFields.addFeld(defaultDisplayField);
                }
            }
            displayResultFields.loadStandards(getConfig());
        }
        return displayResultFields;
    }

    /**
     * Funktion, die den Default für die anzuzeigenden Spalten festlegt,
     * falls keine Spalten über die Workbench definiert wurden.
     *
     * @return
     */
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();

        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_SPRING_MAPPING, iPartsConst.FIELD_DSM_SPRING, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_SPRING_MAPPING, iPartsConst.FIELD_DSM_EDAT, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_SPRING_MAPPING, iPartsConst.FIELD_DSM_ADAT, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    // HOCHKOPIEREN aus dem automatisch erzeugten Code, da getGui sonst immer wieder überschrieben wird.
    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public iPartsSpringLegsInfoGUIData getFrontGUIData() {
        return frontGUIData;
    }

    public iPartsSpringLegsInfoGUIData getRearGUIData() {
        return rearGUIData;
    }

    public ModalResult showModal() {
        frontGUIData.lookUpInitialData();
        rearGUIData.lookUpInitialData();
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    private void addWarning(List<String> warnings, String msg, String springNo) {
        warnings.add(TranslationHandler.translate(msg, iPartsNumberHelper.formatPartNo(getProject(), springNo)));
    }

    /**
     * Funktion, die die Werte aus den Eingabefeldern auf Gültigkeit überprüft und ggf. eine detailierte Fehlermeldung
     * zusammenbaut und am Ende ausgibt.
     *
     * @param warnings
     * @return
     */
    private boolean checkData(List<String> warnings) {
        warnings.clear();
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        if (!frontGUIData.getSpringLegProperties().isTopTextFieldValid()) {
            String springNo = frontGUIData.getSpringLegProperties().getTopTextFieldText();
            checkSpringNumber(numberHelper, springNo, warnings);
        }
        if (!rearGUIData.getSpringLegProperties().isBottomTextFieldValid()) {
            String springNo = rearGUIData.getSpringLegProperties().getBottomTextFieldText();
            checkSpringNumber(numberHelper, springNo, warnings);
        }
        if (!springShimRearProperties.isTopTextFieldValid()) {
            addWarning(warnings, "!!Ungültige Federbeilage Nummer \"%1\"", springShimRearProperties.getTopTextFieldText());
        }
        if (!springShimRearProperties.isBottomTextFieldValid()) {
            addWarning(warnings, "!!Ungültige Federbeilage Nummer \"%1\"", springShimRearProperties.getBottomTextFieldText());
        }
        return warnings.isEmpty();
    }

    private void checkSpringNumber(iPartsNumberHelper numberHelper, String springNo, List<String> warnings) {
        if (numberHelper.isValidASachNo(getProject(), springNo)) {
            addWarning(warnings, "!!Federbein Nummer \"%1\" ist nicht definiert", springNo);
        } else {
            addWarning(warnings, "!!Ungültige Federbein Nummer \"%1\"", springNo);
        }
    }

    /**
     * Prüfung ob beim Click auf den OK-Button das Fenster geschlossen werden darf oder nicht.
     * Es werden die Daten aus den Eingabefeldern auf Gültigkeit überprüft.
     *
     * @param event
     */
    private void onButtonOkEvent(Event event) {
        List<String> warnings = new DwList<String>();
        if (checkData(warnings)) {
            mainWindow.setModalResult(ModalResult.OK);
            close();
        } else {
            MessageDialog.showWarning(warnings);
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
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneAll;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneAll_firstChild_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneMainChildFront;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneFront;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel springLegFrontTextInput;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel springLegFrontGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneMainChildRear;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneRear;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel springLegRearTextInput;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel springLegRearGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneAll_secondChild_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneRearChildSpringShimRear;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitpaneAll = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneAll.setName("splitpaneAll");
            splitpaneAll.__internal_setGenerationDpi(96);
            splitpaneAll.registerTranslationHandler(translationHandler);
            splitpaneAll.setScaleForResolution(true);
            splitpaneAll.setMinimumWidth(10);
            splitpaneAll.setMinimumHeight(10);
            splitpaneAll.setHorizontal(false);
            splitpaneAll.setDividerPosition(0);
            splitpaneAll_firstChild_1 = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneAll_firstChild_1.setName("splitpaneAll_firstChild_1");
            splitpaneAll_firstChild_1.__internal_setGenerationDpi(96);
            splitpaneAll_firstChild_1.registerTranslationHandler(translationHandler);
            splitpaneAll_firstChild_1.setScaleForResolution(true);
            splitpaneAll_firstChild_1.setMinimumWidth(0);
            splitpaneAll_firstChild_1.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneAll_firstChild_1Layout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneAll_firstChild_1.setLayout(splitpaneAll_firstChild_1Layout);
            splitpaneMain = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneMain.setName("splitpaneMain");
            splitpaneMain.__internal_setGenerationDpi(96);
            splitpaneMain.registerTranslationHandler(translationHandler);
            splitpaneMain.setScaleForResolution(true);
            splitpaneMain.setMinimumWidth(10);
            splitpaneMain.setMinimumHeight(10);
            splitpaneMain.setHorizontal(false);
            splitpaneMain.setDividerPosition(20);
            splitpaneMain.setResizeWeight(0.5);
            splitpaneMainChildFront = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneMainChildFront.setName("splitpaneMainChildFront");
            splitpaneMainChildFront.__internal_setGenerationDpi(96);
            splitpaneMainChildFront.registerTranslationHandler(translationHandler);
            splitpaneMainChildFront.setScaleForResolution(true);
            splitpaneMainChildFront.setMinimumWidth(0);
            splitpaneMainChildFront.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutGridBag splitpaneMainChildFrontLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            splitpaneMainChildFront.setLayout(splitpaneMainChildFrontLayout);
            splitpaneFront = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneFront.setName("splitpaneFront");
            splitpaneFront.__internal_setGenerationDpi(96);
            splitpaneFront.registerTranslationHandler(translationHandler);
            splitpaneFront.setScaleForResolution(true);
            splitpaneFront.setMinimumWidth(10);
            splitpaneFront.setMinimumHeight(10);
            splitpaneFront.setDividerPosition(9);
            splitpaneFront.setResizeWeight(0.5);
            springLegFrontTextInput = new de.docware.framework.modules.gui.controls.GuiPanel();
            springLegFrontTextInput.setName("springLegFrontTextInput");
            springLegFrontTextInput.__internal_setGenerationDpi(96);
            springLegFrontTextInput.registerTranslationHandler(translationHandler);
            springLegFrontTextInput.setScaleForResolution(true);
            springLegFrontTextInput.setMinimumWidth(0);
            springLegFrontTextInput.setBorderWidth(0);
            springLegFrontTextInput.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clBtnShadow"));
            springLegFrontTextInput.setTitle("!!Vordere Federbeine (VPD 64)");
            de.docware.framework.modules.gui.layout.LayoutBorder springLegFrontTextInputLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            springLegFrontTextInput.setLayout(springLegFrontTextInputLayout);
            splitpaneFront.addChild(springLegFrontTextInput);
            springLegFrontGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            springLegFrontGrid.setName("springLegFrontGrid");
            springLegFrontGrid.__internal_setGenerationDpi(96);
            springLegFrontGrid.registerTranslationHandler(translationHandler);
            springLegFrontGrid.setScaleForResolution(true);
            springLegFrontGrid.setMinimumWidth(0);
            springLegFrontGrid.setBorderWidth(0);
            springLegFrontGrid.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clBtnShadow"));
            springLegFrontGrid.setTitle("!!Federn zum vorderen Federbein");
            de.docware.framework.modules.gui.layout.LayoutBorder springLegFrontGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            springLegFrontGrid.setLayout(springLegFrontGridLayout);
            splitpaneFront.addChild(springLegFrontGrid);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag splitpaneFrontConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            splitpaneFront.setConstraints(splitpaneFrontConstraints);
            splitpaneMainChildFront.addChild(splitpaneFront);
            splitpaneMain.addChild(splitpaneMainChildFront);
            splitpaneMainChildRear = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneMainChildRear.setName("splitpaneMainChildRear");
            splitpaneMainChildRear.__internal_setGenerationDpi(96);
            splitpaneMainChildRear.registerTranslationHandler(translationHandler);
            splitpaneMainChildRear.setScaleForResolution(true);
            splitpaneMainChildRear.setMinimumWidth(0);
            splitpaneMainChildRear.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutGridBag splitpaneMainChildRearLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            splitpaneMainChildRear.setLayout(splitpaneMainChildRearLayout);
            splitpaneRear = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneRear.setName("splitpaneRear");
            splitpaneRear.__internal_setGenerationDpi(96);
            splitpaneRear.registerTranslationHandler(translationHandler);
            splitpaneRear.setScaleForResolution(true);
            splitpaneRear.setMinimumWidth(10);
            splitpaneRear.setMinimumHeight(10);
            splitpaneRear.setDividerPosition(3);
            splitpaneRear.setResizeWeight(0.5);
            springLegRearTextInput = new de.docware.framework.modules.gui.controls.GuiPanel();
            springLegRearTextInput.setName("springLegRearTextInput");
            springLegRearTextInput.__internal_setGenerationDpi(96);
            springLegRearTextInput.registerTranslationHandler(translationHandler);
            springLegRearTextInput.setScaleForResolution(true);
            springLegRearTextInput.setMinimumWidth(0);
            springLegRearTextInput.setBorderWidth(0);
            springLegRearTextInput.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clBtnShadow"));
            springLegRearTextInput.setTitle("!!Hintere Federbeine (VPD 65)");
            de.docware.framework.modules.gui.layout.LayoutBorder springLegRearTextInputLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            springLegRearTextInput.setLayout(springLegRearTextInputLayout);
            splitpaneRear.addChild(springLegRearTextInput);
            springLegRearGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            springLegRearGrid.setName("springLegRearGrid");
            springLegRearGrid.__internal_setGenerationDpi(96);
            springLegRearGrid.registerTranslationHandler(translationHandler);
            springLegRearGrid.setScaleForResolution(true);
            springLegRearGrid.setMinimumWidth(0);
            springLegRearGrid.setBorderWidth(0);
            springLegRearGrid.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clBtnShadow"));
            de.docware.framework.modules.gui.layout.LayoutBorder springLegRearGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            springLegRearGrid.setLayout(springLegRearGridLayout);
            splitpaneRear.addChild(springLegRearGrid);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag splitpaneRearConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            splitpaneRear.setConstraints(splitpaneRearConstraints);
            splitpaneMainChildRear.addChild(splitpaneRear);
            splitpaneMain.addChild(splitpaneMainChildRear);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneMain.setConstraints(splitpaneMainConstraints);
            splitpaneAll_firstChild_1.addChild(splitpaneMain);
            splitpaneAll.addChild(splitpaneAll_firstChild_1);
            splitpaneAll_secondChild_1 = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneAll_secondChild_1.setName("splitpaneAll_secondChild_1");
            splitpaneAll_secondChild_1.__internal_setGenerationDpi(96);
            splitpaneAll_secondChild_1.registerTranslationHandler(translationHandler);
            splitpaneAll_secondChild_1.setScaleForResolution(true);
            splitpaneAll_secondChild_1.setMinimumWidth(0);
            splitpaneAll_secondChild_1.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneAll_secondChild_1Layout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneAll_secondChild_1.setLayout(splitpaneAll_secondChild_1Layout);
            splitpaneRearChildSpringShimRear = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneRearChildSpringShimRear.setName("splitpaneRearChildSpringShimRear");
            splitpaneRearChildSpringShimRear.__internal_setGenerationDpi(96);
            splitpaneRearChildSpringShimRear.registerTranslationHandler(translationHandler);
            splitpaneRearChildSpringShimRear.setScaleForResolution(true);
            splitpaneRearChildSpringShimRear.setMinimumWidth(10);
            splitpaneRearChildSpringShimRear.setMinimumHeight(10);
            splitpaneRearChildSpringShimRear.setTitle("!!Zusatzmaterialien zum hinteren Federbein (VPD 66)");
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneRearChildSpringShimRearLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneRearChildSpringShimRear.setLayout(splitpaneRearChildSpringShimRearLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneRearChildSpringShimRearConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneRearChildSpringShimRear.setConstraints(splitpaneRearChildSpringShimRearConstraints);
            splitpaneAll_secondChild_1.addChild(splitpaneRearChildSpringShimRear);
            splitpaneAll.addChild(splitpaneAll_secondChild_1);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneAllConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneAll.setConstraints(splitpaneAllConstraints);
            panelMain.addChild(splitpaneAll);
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
                    onButtonOkEvent(event);
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