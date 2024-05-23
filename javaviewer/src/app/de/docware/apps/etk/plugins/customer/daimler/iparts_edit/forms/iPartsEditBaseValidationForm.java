/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.iPartsEditAssemblyListValidationOverlappingEntriesForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkMessageLogFormHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.OnCancelEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.RunTimeLogger;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditValidationHelper;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextArea;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.output.j2ee.misc.BrowserInfo;
import de.docware.framework.modules.gui.output.swing.SwingHandler;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.os.OsUtils;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Basisform zur Anzeige von Validierungen (speziell für verschiedene Baumuster nur mit aktivem Baumuster-Filter).
 * Im Hauptteil wird meistens die ungefilterte Stückliste angezeigt. Diese kann um weitere virtuelle Felder erweitert
 * werden, um z.B. Kommentare zum einzelnen Stücklisteneintrag oder das Ergebnis der Validierung anzuzeigen.
 * Im unteren Bereich ist ein Dockingpanel vorbereitet, in dem die detaillierten Ergebnisse als Text angezeigt werden können.
 * Der Dialog kann aber auch ohne das Dockingpanel erzeugt werden.
 * Im oberen Bereich ist ein Panel vorbereitet, in das Buttons und zusätzliche Überschriften integriert werden können.
 * Siehe {@link #getMainPanelHeader()}
 */
public abstract class iPartsEditBaseValidationForm extends AbstractJavaViewerForm implements iPartsConst {

    public enum TIMER_TYPES {
        TIMER_MODEL_FILTER_CHECK("Model filter check"),
        TIMER_COMBINED_TEXTS_CHECK("Combined texts check"),
        TIMER_ADD_FACTORIES_CHECK("Additional factories check"),
        TIMER_COLOR_CHECK("Colors check"),
        TIMER_PARTIAL_CONJUNCTION_OVERLAP_CHECK("Partial conjunction overlap check"),
        TIMER_MODEL_VALIDITY_CHECK("Model validity check"),
        TIMER_FACTORY_DATA_CHECK("Factory data check");

        private String logMsg;

        TIMER_TYPES(String logMsg) {
            this.logMsg = logMsg;
        }

        public String getLogMsg() {
            return logMsg;
        }
    }

    private static final String SESSION_KEY_PRODUCT_TO_MODELS_MAP =
            "session_product_to_models_map_for_model_evaluation"; // Inhalt: Map<iPartsProductId, Set<String>>

    public enum SCREEN_SIZES {MAXIMIZE, SCALE_FROM_PARENT, DONT_CARE}

    protected AbstractJavaViewerForm validationContent;

    protected int firstAdditionalFieldIndex;
    protected List<EtkDisplayField> additionalDisplayFields;
    private Set<String> selectedModels;
    private boolean saveModelSelectionInSession;

    protected iPartsFilter filterForModelEvaluation;
    protected iPartsFilter filterForInvisibleEntries;

    protected String tableNameForVirtualModelFields;

    private EtkMessageLogFormHelper messageLogHelper;
    //    private EtkMessageLogForm messageLog;
//    private int maxProgress;
//    private int currentProgress;
    private EtkMessageLog externalMessageLog;
    private boolean isCancelled;
    private boolean simplifiedQualityCheck;

    public enum ValidationResult {
        OK("VALID"),
        ERROR("VALIDATION_ERROR"),
        UNCHECKED("VALIDATION_WARNING"),
        WARNING("VALIDATION_WARNING"),
        MODEL_INVALID("NOT_RELEVANT"),
        INVISIBLE_PART_VALID("INVISIBLE_PART_VALID"),
        INVALID_HOTSPOT("INVALID_HOTSPOT");

        protected String dbValue;

        public static ValidationResult getFromDbValue(String dbValue) {
            for (ValidationResult value : values()) {
                if (value.getDbValue().equals(dbValue)) {
                    return value;
                }
            }

            return UNCHECKED;
        }

        ValidationResult(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }
    }

    public iPartsEditBaseValidationForm(AbstractJavaViewerFormIConnector dataConnector,
                                        AbstractJavaViewerForm parentForm,
                                        boolean removeAdditionalInfoPanel,
                                        boolean minimizeAdditionalInfoPanel, String tableNameForVirtualModelFields) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        firstAdditionalFieldIndex = -1;
        selectedModels = new TreeSet<>();
        filterForModelEvaluation = new iPartsFilter();
        filterForInvisibleEntries = new iPartsFilter();
        this.tableNameForVirtualModelFields = tableNameForVirtualModelFields;
        if (dataConnector instanceof AssemblyListFormIConnector) {
            EtkDataAssembly assembly = ((AssemblyListFormIConnector)dataConnector).getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                simplifiedQualityCheck = iPartsEditValidationHelper.isSimplifiedQualityCheck((iPartsDataAssembly)assembly);
            }
        }
        postCreateGui(removeAdditionalInfoPanel, minimizeAdditionalInfoPanel);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     *
     * @param removeAdditionalInfoPanel
     * @param minimizeAdditionalInfoPanel
     */
    protected void postCreateGui(boolean removeAdditionalInfoPanel, boolean minimizeAdditionalInfoPanel) {
        iPartsFilterSwitchboard filterForModelEvaluationSwitchboard = filterForModelEvaluation.activateOnlyModelFilterWithFilterReason();

        // Den gleichen Filter nochmal initialisieren, aber ohne die Filter für immer in der Stückliste ausgefilterte
        // Teile, wie Wegfallsachnummern. Somit kann man die zwei Filter unabhängig voneinander benutzen, ohne
        // dass man den Cache löschen muss wenn mann Teilfilter an- und abschaltet.
        filterForInvisibleEntries.setSwitchboardState(filterForModelEvaluation.getSwitchboardState().cloneMe());

        filterForInvisibleEntries.setOmittedPartsFilterActive(false);
        filterForInvisibleEntries.setOmittedPartListEntriesFilterActive(false);
        filterForInvisibleEntries.setOnlyModelFilterActive(false);
        filterForInvisibleEntries.setModelSubFilterActive(iPartsFilter.ModelFilterTypes.OMITTED_PART, false);

        filterForInvisibleEntries.setWithFilterReason(true);

        // Anzeige der Stückliste mit zusätzlichen virtuellen Feldern
        validationContent = createValidationContent(filterForModelEvaluationSwitchboard);

        // Stückliste anstatt des Platzhalters anzeigen
        validationContent.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.evaluationContentPanel.addChild(validationContent.getGui());

        if (removeAdditionalInfoPanel) {
            // additionalInfoPanel aus der Splitpane lösen und anstatt der Splitpane als Hauptpanel einfügen
            mainWindow.splitPane.removeFromParent();
            mainWindow.evaluationContentPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
            mainWindow.mainPanel.addChild(mainWindow.evaluationContentPanel);
        } else {
            if (minimizeAdditionalInfoPanel) {
                // Alternative Implementierung, bei der allerdings unten den Rest vom Dockingplanel übrig bleibt
                mainWindow.mainPanel.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.mainPanel) {
                    @Override
                    public boolean isFireOnceValid(Event event) {
                        return mainWindow.additionalInfoPanel.isSplitPaneSizeValid();
                    }

                    @Override
                    public void fireOnce(Event event) {
                        mainWindow.additionalInfoPanel.setShowing(false);
                    }
                });
            } else {
                mainWindow.additionalInfoPanel.setVisible(true);
                mainWindow.additionalInfoPanel.setShowing(true);
                mainWindow.splitPane.setDividerPosition(5000); // Absichtlich viel zu großen Wert setzen, wird im ON_RESIZE_EVENT korrigiert
                mainWindow.mainPanel.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.mainPanel) {
                    @Override
                    public void fireOnce(Event event) {
                        int height = mainWindow.getHeight();
                        if (event.hasParameter(Event.EVENT_PARAMETER_NEWHEIGHT)) {
                            height = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                        }
                        // empirisch ermittelter Wert für ca. 4 Zeilen Text
                        mainWindow.splitPane.setDividerPosition(height - 200);
                    }
                });
            }
        }

        ThemeManager.get().render(mainWindow);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (validationContent != null) {
            validationContent.dispose();
        }
    }

    protected void doCancelCalculation() {
        isCancelled = true;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    protected void initMessageLog(String windowTitle, String title) {
        OnCancelEvent onCancelEvent = new OnCancelEvent() {

            @Override
            public void cancelCalculation() {
                doCancelCalculation();
            }
        };
        messageLogHelper = new EtkMessageLogFormHelper(windowTitle, title, onCancelEvent);
    }
//    protected void initMessageLog(String windowTitle, String title) {
//        messageLog = new EtkMessageLogForm(windowTitle, title, null) {
//            @Override
//            protected void cancel(Event event) {
//                cancelCalculation();
//                super.cancel(event);
//            }
//        };
//        isCancelled = false;
//        messageLog.getGui().setSize(600, 250);
//        resetMessageLog(0);
//    }

    protected void resetMessageLog(int maxProgress) {
        if (messageLogHelper != null) {
            messageLogHelper.resetMessageLog(maxProgress);
        }
//        this.maxProgress = maxProgress;
//        currentProgress = 0;
    }

    public void incMessageLogMaxProgess(int incValue) {
        if (messageLogHelper != null) {
            messageLogHelper.incMaxProgress(incValue);
        }
//        this.maxProgress = maxProgress;
//        currentProgress = 0;
    }

    protected EtkMessageLogForm getMessageLog() {
        if (messageLogHelper != null) {
            return messageLogHelper.getMessageLog();
        }
        return null;
//        return messageLog;
    }

    public EtkMessageLogFormHelper getMessageLogHelper() {
        return messageLogHelper;
    }

    protected void fireMessage(String message) {
        if (messageLogHelper != null) {
            messageLogHelper.fireMessage(message);
        }
//        if (messageLog != null) {
//            messageLog.getMessageLog().fireMessage(message);
//        }
    }

    protected void fireProgress() {
        if (messageLogHelper != null) {
            messageLogHelper.fireProgress();
        }
//        currentProgress++;
//        if (messageLog != null) {
//            messageLog.getMessageLog().fireProgress(currentProgress, maxProgress, "", false, true);
//        }
    }

    public void setCurrentProgress(int currentProgress) {
        if (messageLogHelper != null) {
            messageLogHelper.setCurrentProgress(currentProgress);
        }
//        this.currentProgress = currentProgress;
    }

    public int getCurrentProgress() {
        if (messageLogHelper != null) {
            return messageLogHelper.getCurrentProgress();
        }
        return -1;
//        return currentProgress;
    }

    public int getMaxProgress() {
        if (messageLogHelper != null) {
            return messageLogHelper.getMaxProgress();
        }
        return -1;
//        return maxProgress;
    }

    protected void hideMessageLog() {
        if (messageLogHelper != null) {
            messageLogHelper.hideMessageLog();
        }
//        if (messageLog != null) {
//            messageLog.getMessageLog().hideProgress();
//            messageLog.closeWindow(ModalResult.OK);
//        }
    }

    protected abstract AbstractJavaViewerForm createValidationContent(final iPartsFilterSwitchboard filterForModelEvaluationSwitchboard);

    protected abstract String getDisplayFieldConfigKey(String partListType);

    public void saveModelSelectionInSession(boolean enable) {
        this.saveModelSelectionInSession = enable;
    }

    public void doResizeWindow(SCREEN_SIZES kind) {
        switch (kind) {
            case MAXIMIZE:
                Dimension screenSize = FrameworkUtils.getScreenSize();
                mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
                break;
            case SCALE_FROM_PARENT:
                if (parentForm != null) {
                    int height = parentForm.getGui().getParentWindow().getHeight();
                    int width = parentForm.getGui().getParentWindow().getWidth();
                    mainWindow.setSize(width - iPartsConst.CASCADING_WINDOW_OFFSET_WIDTH,
                                       height - iPartsConst.CASCADING_WINDOW_OFFSET_HEIGHT);
                }
                break;
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.mainPanel;
    }

    public GuiWindow getMainWindow() {
        return mainWindow;
    }

    protected GuiPanel getEvaluationContentPanel() {
        return mainWindow.evaluationContentPanel;
    }

    public GuiTextArea getAdditionalInfoTextArea() {
        return mainWindow.additionalInfoArea;
    }

    public void setTitle(String windowTitle) {
        mainWindow.setTitle(windowTitle);
    }

    public void setSubTitle(String subTitle) {
        mainWindow.title.setTitle(subTitle);
    }

    public GuiPanel getMainPanelHeader() {
        return mainWindow.mainPanelHeader;
    }

    public GuiButtonPanel getButtonPanel() {
        return mainWindow.buttonPanel;
    }

    public ModalResult showModal() {
        return showModal(true);
    }

    public ModalResult showModal(boolean withUpdateView) {
        try {
            if (withUpdateView) {
                updateView();
            }
            if (isCancelled()) {
                close();
                return ModalResult.ABORT;
            }
            return mainWindow.showModal();
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    /**
     * Zeigt das Fenster nicht-modal an.
     */
    public void showNonModal() {
        mainWindow.setVisible(false);
        getConnector().setActiveForm(this); // Dadurch werden modale Kind-Fenster wie z.B. die RelatedInfo innerhalb des nicht-modalen Fensters geöffnet
        // das Fenster, da es jetzt nicht-modal angezeigt wird, bei der Parent-Form austragen. Wichtig, da sonst beim Schließen
        // z.B. vom Related-Edit in dieser Form alle Callbacks aufgeräumt würden, wodurch die Form keine Events mehr empfangen würde.
        if (parentForm != null) {
            parentForm.removeChildForm(this);
            parentForm = null;
        }
        addCloseNonModalWindowListener(mainWindow);
        mainWindow.showNonModal(GuiWindow.NonModalStyle.OPEN_IN_NEW_WINDOW);
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void close(Event event) {
        close();
    }

    public abstract List<EtkDisplayField> createDefaultDisplayFields();

    protected EtkDisplayField addDefaultDisplayField(String tableName, String fieldName, boolean isMultiLanguage, boolean isArray,
                                                     List<EtkDisplayField> displayFields) {
        EtkDisplayField displayField = new EtkDisplayField(tableName, fieldName, isMultiLanguage, isArray);
        displayField.setWidth(-1);
        displayField.setDefaultWidth(false);
        displayFields.add(displayField);
        return displayField;
    }

    public EtkDisplayFields getDisplayFields(String configKey) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        if (StrUtils.isValid(configKey)) {
            displayResultFields.load(getConfig(), configKey);
        }

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
     * Die zur Anzeige ausgewählten Baumuster
     *
     * @return
     */
    public Set<String> getSelectedModels() {
        return selectedModels;
    }

    /**
     * Die für die Auswertung ausgewählten Baumuster. Können sich von den zur Anzeige ausgewählten unterscheiden.
     *
     * @return
     */
    public Set<String> getModelsForEvaluation() {
        return getSelectedModels();
    }

    /**
     * Setzt die Baumuster zur Anzeige und speichert diese auch in der Session pro Produkt, falls
     * <code>productId</code> nicht <code>null</code> und {@link #saveModelSelectionInSession} gesetzt ist.
     *
     * @param models
     * @param productId  kann <code>null</code> sein, dann werden die Baumuster nicht gespeichert
     * @param withUpdate soll die Auswertung aktualisiert werden?
     */
    public void setSelectedModels(Set<String> models, iPartsProductId productId, boolean withUpdate) {
        // Baumusterauswahl für das Produkt in der Session speichern
        if ((productId != null) && saveModelSelectionInSession) {
            Object productToModelsMapFromSession = Session.get().getAttribute(SESSION_KEY_PRODUCT_TO_MODELS_MAP);
            Map<iPartsProductId, Set<String>> productToModelsMap;
            if (productToModelsMapFromSession != null) {
                productToModelsMap = (Map<iPartsProductId, Set<String>>)productToModelsMapFromSession;
            } else {
                productToModelsMap = new HashMap<>();
                Session.get().setAttribute(SESSION_KEY_PRODUCT_TO_MODELS_MAP, productToModelsMap);
            }
            productToModelsMap.put(productId, models);
        }
        selectedModels.clear();
        selectedModels.addAll(models);

        if (withUpdate) {
            updateValidationContent(false, true, true);
        }
    }

    public abstract iPartsProductId getProductIdForModelStorage();

    /**
     * Öffnet einen Dialog zur Baumuster Auswahl und setzt diese für die Anzeige
     *
     * @param withUpdate
     * @return
     */
    public Set<String> selectModels(boolean withUpdate) {
        iPartsProductId productId = getProductIdForModelStorage();

        // Baumuster-Auswahldialog für das Produkt anzeigen und danach die Stückliste neu filtern
        Collection<String> newSelectedModels = EditSelectModelsForm.showSelectionModels(this, productId, null, false, getSelectedModels());
        if (newSelectedModels != null) {
            Set<String> selectedSet = new TreeSet<>(newSelectedModels);

            setSelectedModels(selectedSet, productId, withUpdate);
            return selectedSet;
        }
        return null;
    }

    /**
     * Aktualisiert nur die DisplayFields je nachdem, welche Baumuster ausgewählt bzw. geladen wurden.
     *
     * @param getSelectedModelsFromSession
     */
    protected void updateDisplayFields(boolean getSelectedModelsFromSession) {
        iPartsProductId productId = getProductIdForModelStorage();
        if (saveModelSelectionInSession && getSelectedModelsFromSession && (productId != null)) {
            // Bisherige Baumusterauswahl für das Produkt aus der Session auslesen
            Object productToModelsMap = Session.get().getAttribute(SESSION_KEY_PRODUCT_TO_MODELS_MAP);
            if (productToModelsMap != null) {
                Set<String> selectedModelsForProduct = ((Map<iPartsProductId, Set<String>>)productToModelsMap).get(productId);

                if (selectedModelsForProduct != null) {
                    selectedModels = selectedModelsForProduct;
                } else {
                    selectedModels.clear();
                }
            }
        }

        additionalDisplayFields = getVirtualModelOrFINFields(selectedModels, false);
    }

    /**
     * Aktualisiert die Auswertung und lädt dazu ggf. die gewählten Baumuster für das relevante Produkt aus der Session
     *
     * @param getSelectedModelsFromSession falls <code>true</code> werden die relevanten Baumuster aus der Session
     *                                     geladen
     * @param forceReloadAssembly          Soll das Neuladen vom Modul (speziell die validierten Stücklisteneinträge) trotz
     *                                     gleichem Modul erzwungen werden?
     * @param updateValidationGUI          Soll die GUI aktualisiert werden?
     */
    public void updateValidationContent(boolean getSelectedModelsFromSession, boolean forceReloadAssembly, boolean updateValidationGUI) {
        updateDisplayFields(getSelectedModelsFromSession);
    }

    public abstract String createVirtualFieldNameForModelOrFINEvaluation(String modelNumber);

    /**
     * Ermittelt das Baumuster aus dem virtuellen Feldnamen
     *
     * @param fieldName
     * @return
     * @see #createVirtualFieldNameForModelOrFINEvaluation(String)
     */
    public String getModelOrFINFromVirtualFieldName(String fieldName) {
        if (StrUtils.isValid(fieldName) && VirtualFieldsUtils.isVirtualField(fieldName)) {
            String innerFieldName = VirtualFieldsUtils.removeVirtualFieldMask(fieldName);
            return StrUtils.stringAfterCharacter(innerFieldName, iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_SPACER);
        }
        return "";
    }

    public List<EtkDisplayField> getVirtualModelOrFINFields(Set<String> models, boolean isFINMode) {
        List<EtkDisplayField> virtualModelFields = new DwList<>();
        // Virtuelle Felder für die Baumusterauswertung hinzufügen
        if (!models.isEmpty()) {
            for (String selectedModel : models) {
                virtualModelFields.add(createVirtualModelOrFINEvaluationField(selectedModel, isFINMode));
            }

            // Leere letzte Spalte hinzufügen, damit die Baumuster-Spalten alle gleich breit dargestellt werden
            EtkDisplayField displayField = new EtkDisplayField(tableNameForVirtualModelFields, createVirtualFieldNameForModelOrFINEvaluation(""),
                                                               false, false);
            displayField.setDefaultWidth(false);
            displayField.setWidth(0);
            virtualModelFields.add(displayField);
        }
        return virtualModelFields;
    }

    private EtkDisplayField createVirtualModelOrFINEvaluationField(String modelNumber, boolean isFINMode) {
        EtkDisplayField displayField = new EtkDisplayField(tableNameForVirtualModelFields,
                                                           createVirtualFieldNameForModelOrFINEvaluation(modelNumber),
                                                           false, false);
        displayField.setDefaultText(false);

        // Schreibrichtung vertikal von links nach rechts per HTML CSS Style "writing-mode" mit einer passenden Anzahl
        // von Leerzeichen und Newlines für vernünftige Darstellung -> IE benötigt stattdessen HTML CSS Style "-ms-writing-mode"
        // und unter Swing geht es leider überhaupt nicht :-/
        String fieldTitle;
        int width;
        if (SwingHandler.isSwing()) {
            fieldTitle = modelNumber;
            width = 10;
        } else {
            String cssStyleWritingMode;
            BrowserInfo browserInfo = BrowserInfo.get();
            if ((browserInfo == null) || browserInfo.isIE()) {
                cssStyleWritingMode = "-ms-writing-mode:tb-lr";
            } else {
                cssStyleWritingMode = "writing-mode:vertical-lr";
            }
            fieldTitle = "<html><span style=\"" + cssStyleWritingMode + "\">    " + modelNumber + "</span></html>\n\n\n\n";

            if (isFINMode) { // damit Feldhöhe für die längeren FINs ausreicht, noch ein paar Newlines extra
                fieldTitle += "\n\n\n\n";
            }
            // Unter Unix ist die Textbreitenberechnung aufgrund der unterschiedlichen Fonts nicht korrekt und die Höhe
            // des Tabellen-Headers wäre zu gering -> ein extra Newline spendieren
            if (OsUtils.isUnix()) {
                fieldTitle += "\n";
            }

            if ((browserInfo != null) && browserInfo.isChrome()) {
                width = 6; // Warum auch immer Chrome hier mehr Platz zum Rendern braucht...
            } else {
                width = 5;
            }
        }

        displayField.setText(new EtkMultiSprache(fieldTitle, getProject().getConfig().getViewerLanguages()));
        displayField.setDefaultWidth(false);
        displayField.setWidth(width);
        displayField.setColumnFilterEnabled(true);
        return displayField;
    }

    protected String getModelInColumn(int colIndex) {
        if ((selectedModels != null) && (firstAdditionalFieldIndex > -1)) {
            int realIndex = colIndex - firstAdditionalFieldIndex;
            if ((realIndex >= 0) && (realIndex < selectedModels.size())) {
                return (String)selectedModels.toArray()[realIndex];
            }
        }
        return "";
    }

    protected void setAdditionalInfoText(String text) {
        mainWindow.additionalInfoArea.clear();
        mainWindow.additionalInfoArea.setText(text);
    }

    public boolean isSimplifiedQualityCheck() {
        return simplifiedQualityCheck;
    }

    public static class ValidationTimers {

        private Map<iPartsEditAssemblyListValidationOverlappingEntriesForm.TIMER_TYPES, RunTimeLogger> timerMap;
        private String kVari;

        public ValidationTimers(LogChannels channel) {
            timerMap = new HashMap<>();
            for (iPartsEditAssemblyListValidationOverlappingEntriesForm.TIMER_TYPES timerType : iPartsEditAssemblyListValidationOverlappingEntriesForm.TIMER_TYPES.values()) {
                timerMap.put(timerType, new RunTimeLogger(channel));
            }
        }

        public void setkVari(String kVari) {
            this.kVari = kVari;
        }

        public void setStartTime(iPartsEditAssemblyListValidationOverlappingEntriesForm.TIMER_TYPES timerType) {
            if (timerMap.containsKey(timerType)) {
                timerMap.get(timerType).setStartTime();
            }
        }

        public void stopTimeAndStore(iPartsEditAssemblyListValidationOverlappingEntriesForm.TIMER_TYPES timerType) {
            if (timerMap.containsKey(timerType)) {
                timerMap.get(timerType).stopTimeAndStore();
            }
        }

        public void logRunTime(iPartsEditAssemblyListValidationOverlappingEntriesForm.TIMER_TYPES timerType) {
            if (timerMap.containsKey(timerType)) {
                String msg = timerType.getLogMsg() + " in";
                if (StrUtils.isValid(kVari)) {
                    msg = kVari + ": " + msg;
                }
                timerMap.get(timerType).logRunTime(msg);
            }
        }

        public void logRunTimes() {
            for (iPartsEditAssemblyListValidationOverlappingEntriesForm.TIMER_TYPES timerType : iPartsEditAssemblyListValidationOverlappingEntriesForm.TIMER_TYPES.values()) {
                logRunTime(timerType);
            }
        }
    }

    public void setExternalMessageLog(EtkMessageLog messageLog) {
        externalMessageLog = messageLog;
    }

    public EtkMessageLog getExternalMessageLog() {
        return externalMessageLog;
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
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanelHeader;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel evaluationContentPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel additionalInfoPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane additionalInfoScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea additionalInfoArea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setTitle("!!Qualitätsprüfung doppelte Teilkonjunktionen");
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
            mainPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanel.setName("mainPanel");
            mainPanel.__internal_setGenerationDpi(96);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            mainPanel.setMinimumWidth(10);
            mainPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanel.setLayout(mainPanelLayout);
            mainPanelHeader = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanelHeader.setName("mainPanelHeader");
            mainPanelHeader.__internal_setGenerationDpi(96);
            mainPanelHeader.registerTranslationHandler(translationHandler);
            mainPanelHeader.setScaleForResolution(true);
            mainPanelHeader.setMinimumWidth(10);
            mainPanelHeader.setMinimumHeight(10);
            mainPanelHeader.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelHeaderLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanelHeader.setLayout(mainPanelHeaderLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelHeaderConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanelHeaderConstraints.setPosition("north");
            mainPanelHeader.setConstraints(mainPanelHeaderConstraints);
            mainPanel.addChild(mainPanelHeader);
            splitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitPane.setName("splitPane");
            splitPane.__internal_setGenerationDpi(96);
            splitPane.registerTranslationHandler(translationHandler);
            splitPane.setScaleForResolution(true);
            splitPane.setMinimumWidth(10);
            splitPane.setMinimumHeight(10);
            splitPane.setHorizontal(false);
            splitPane.setDividerPosition(810);
            evaluationContentPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            evaluationContentPanel.setName("evaluationContentPanel");
            evaluationContentPanel.__internal_setGenerationDpi(96);
            evaluationContentPanel.registerTranslationHandler(translationHandler);
            evaluationContentPanel.setScaleForResolution(true);
            evaluationContentPanel.setMinimumWidth(0);
            evaluationContentPanel.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder evaluationContentPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            evaluationContentPanel.setLayout(evaluationContentPanelLayout);
            splitPane.addChild(evaluationContentPanel);
            additionalInfoPanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            additionalInfoPanel.setName("additionalInfoPanel");
            additionalInfoPanel.__internal_setGenerationDpi(96);
            additionalInfoPanel.registerTranslationHandler(translationHandler);
            additionalInfoPanel.setScaleForResolution(true);
            additionalInfoPanel.setMinimumWidth(199);
            additionalInfoPanel.setMinimumHeight(19);
            additionalInfoPanel.setTextHide("!!Zusatzinformation ausblenden");
            additionalInfoPanel.setTextShow("!!Zusatzinformation anzeigen");
            additionalInfoPanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            additionalInfoPanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            additionalInfoPanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            additionalInfoPanel.setButtonBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonDisabledBackgroundGradient1"));
            additionalInfoPanel.setButtonForegroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignButtonBorderSelected"));
            additionalInfoPanel.setButtonFill(true);
            additionalInfoScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            additionalInfoScrollPane.setName("additionalInfoScrollPane");
            additionalInfoScrollPane.__internal_setGenerationDpi(96);
            additionalInfoScrollPane.registerTranslationHandler(translationHandler);
            additionalInfoScrollPane.setScaleForResolution(true);
            additionalInfoScrollPane.setMinimumWidth(10);
            additionalInfoScrollPane.setMinimumHeight(10);
            additionalInfoScrollPane.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextAreaDisabledBackground"));
            additionalInfoScrollPane.setBorderWidth(1);
            additionalInfoArea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            additionalInfoArea.setName("additionalInfoArea");
            additionalInfoArea.__internal_setGenerationDpi(96);
            additionalInfoArea.registerTranslationHandler(translationHandler);
            additionalInfoArea.setScaleForResolution(true);
            additionalInfoArea.setMinimumWidth(200);
            additionalInfoArea.setMinimumHeight(0);
            additionalInfoArea.setBorderWidth(4);
            additionalInfoArea.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            additionalInfoArea.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder additionalInfoAreaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            additionalInfoArea.setConstraints(additionalInfoAreaConstraints);
            additionalInfoScrollPane.addChild(additionalInfoArea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder additionalInfoScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            additionalInfoScrollPane.setConstraints(additionalInfoScrollPaneConstraints);
            additionalInfoPanel.addChild(additionalInfoScrollPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder additionalInfoPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            additionalInfoPanel.setConstraints(additionalInfoPanelConstraints);
            splitPane.addChild(additionalInfoPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitPane.setConstraints(splitPaneConstraints);
            mainPanel.addChild(splitPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            this.addChild(mainPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCloseActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    close(event);
                }
            });
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