/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.misc.EtkTableHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWWPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.SteeringIdentKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.AuthorOrderChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditCombinedTextForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsAuthorOrderPreReleaseCheckResults;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditCombTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditValidationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTabbedPane;
import de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;

import java.util.*;

import static de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoModelEvaluationForm.CONFIG_KEY_MODEL_EVALUATION_DIALOG;
import static de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoModelEvaluationForm.CONFIG_KEY_MODEL_EVALUATION_EDS;

/**
 * Formular zur Validierung von Modulen bzgl. überlappender Teilkonjunktionen von Stücklisteneinträgen.
 */
public class iPartsEditAssemblyListValidationOverlappingEntriesForm extends iPartsEditAssemblyListValidationForm implements iPartsConst {

    private static final String TAB_ERROR_SUFFIX = " (X)";
    private static final String TAB_WARNING_SUFFIX = " (!)";
    private static final String PARTLIST_AND_COLOR_TAB_TITLE = "!!Stückliste/Farben";
    private static final String PICTURE_AND_TU_TAB_TITLE = "!!Bild/TU";
    private static final String FIN_VALIDATION_TITLE = "!!FIN Prüfung";
    private static final String GENERAL_REASON = "GENERAL_REASON";
    private static final String ADD_FACTORIES_ENUM_KEY = "TUValidationAddFactories";

    private Map<String, ValidationEntry> validationEntries = new HashMap<>(); // Map<"klfdNr | model" -> ValidationEntry>
    private iPartsDialogSeries series;
    private Comparator<EtkDataPartListEntry> partListEntryCompareLfdNr;
    private boolean useMessageLog = true;
    private Map<String, ValidationResult> colorTableValidationResult = new HashMap<>();
    private int firstIndexWithErrorOrWarning;
    // Der Autorenauftrag, auf den sich diese Form bezieht. Dieser ändert sich nie. Also auch nicht,
    // wenn Hauptfenster einer aktiviert wird. Ist null, falls die Berechnung ohne Autorenauftrag ausgeführt wird.
    private iPartsDataAuthorOrder authorOrder;
    private ValidationTimers runTimeLoggers;

    private iPartsValidationPictureAndTUForm validationPictureAndTUForm;
    private iPartsFINValidationForm finValidationForm;
    private GuiTabbedPane tabbedPane;
    private GuiTabbedPaneEntry partlistAndColor;
    private GuiTabbedPaneEntry pictureAndTU;
    private GuiTabbedPaneEntry finValidation;
    private volatile FrameworkThread pictureAndTUThread;
    private volatile FrameworkThread finValidationThread;

    /**
     * Führt die Qualitätsprüfung auf überlappende Teilkonjunktionen im Hintergrund für eine Assembly durch
     *
     * @param connector
     * @param assembly
     * @param messageLog
     * @return das Gesamtergebnis der Prüfungen
     */
    public static iPartsEditAssemblyListValidationOverlappingEntriesForm validateAssemblySilent(AbstractJavaViewerFormIConnector connector,
                                                                                                final EtkDataAssembly assembly, EtkMessageLog messageLog) {
        EditModuleFormConnector editConnectorForValidation = new EditModuleFormConnector(connector);
        editConnectorForValidation.setCurrentAssembly(assembly);

        iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm = new iPartsEditAssemblyListValidationOverlappingEntriesForm(editConnectorForValidation, null);
        validationForm.addOwnConnector(editConnectorForValidation);
        validationForm.setUseMessageLog(false);
        validationForm.setExternalMessageLog(messageLog);

        // Validierung durchführen ohne Aufbau der GUI
        validationForm.doValidation();

        return validationForm;
    }

    public iPartsEditAssemblyListValidationOverlappingEntriesForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, false, false);
        doResizeWindow(iPartsEditAssemblyListValidationForm.SCREEN_SIZES.MAXIMIZE);
        makeWindowTabbed();
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    /**
     * wird für "Überprüfung aller TUs in einem Produkt" benötigt
     *
     * @return
     */
    public iPartsValidationPictureAndTUForm getValidationPictureAndTUForm() {
        return validationPictureAndTUForm;
    }

    /**
     * wird für "Überprüfung aller TUs in einem Produkt" benötigt
     *
     * @return
     */
    public iPartsFINValidationForm getFinValidationForm() {
        return finValidationForm;
    }

    /**
     * wird für "Überprüfung aller TUs in einem Produkt" benötigt
     *
     * @return
     */
    public Map<String, ValidationResult> getColorTableValidationResult() {
        return colorTableValidationResult;
    }

    /**
     * wird für "Überprüfung aller TUs in einem Produkt" benötigt
     *
     * @return
     */
    public List<EtkDisplayField> getValidationDisplayFields() {
        if ((validationContent instanceof IPartsRelatedInfoAssemblyListForm)) {
            IPartsRelatedInfoAssemblyListForm validationContentForm = (IPartsRelatedInfoAssemblyListForm)validationContent;
            if (validationContentForm != null) {
                validationContentForm.getConnector().setCurrentAssembly(getConnector().getCurrentAssembly());
                EtkTableHelper helper = validationContentForm.createTableHelper();
                return helper.getDesktopDisplayList();
            }
        }
        return null;
    }

    /**
     * wird für "Überprüfung aller TUs in einem Produkt" benötigt
     *
     * @return
     */
    public List<EtkDataPartListEntry> getCurrentPartListEntries() {
        if ((validationContent instanceof IPartsRelatedInfoAssemblyListForm)) {
            IPartsRelatedInfoAssemblyListForm validationContentForm = (IPartsRelatedInfoAssemblyListForm)validationContent;
            if (validationContentForm != null) {
                return validationContentForm.getConnector().getCurrentPartListEntries();
            }
        }
        return null;
    }

    /**
     * Ab Daimler-8699 muss dieser Dialog in eine Tabbed Pane verschoben werden, da die Qualitätsprüfung auf Bilder/ TU erweitert wird
     */
    private void makeWindowTabbed() {
        tabbedPane = new GuiTabbedPane();
        tabbedPane.setConstraints(new ConstraintsBorder());

        partlistAndColor = new GuiTabbedPaneEntry(PARTLIST_AND_COLOR_TAB_TITLE);
        partlistAndColor.setConstraints(new ConstraintsBorder());
        GuiPanel partlistAndColorPanel = new GuiPanel();
        partlistAndColorPanel.setLayout(new LayoutBorder());
        partlistAndColor.addChild(partlistAndColorPanel);

        AbstractGuiControl oldDialogGUI = getGui();
        // Alter Dialog in ein Tab verschieben
        if (oldDialogGUI != null) {
            partlistAndColorPanel.addChildBorderCenter(oldDialogGUI);
        }

        finValidation = new GuiTabbedPaneEntry(FIN_VALIDATION_TITLE);
        finValidation.setConstraints(new ConstraintsBorder());
        GuiPanel finValidationPanel = new GuiPanel();
        finValidationPanel.setLayout(new LayoutBorder());
        finValidation.addChild(finValidationPanel);

        finValidationForm = new iPartsFINValidationForm(getConnector(), this, false, false);
        finValidationPanel.addChildBorderCenter(finValidationForm.getGui());

        pictureAndTU = new GuiTabbedPaneEntry(PICTURE_AND_TU_TAB_TITLE);
        pictureAndTU.setConstraints(new ConstraintsBorder());
        GuiPanel pictureAndTUPanel = new GuiPanel();
        pictureAndTUPanel.setLayout(new LayoutBorder());
        pictureAndTU.addChild(pictureAndTUPanel);

        // leere iPartsEditAssemblyListValidationForm für die Gui, in der die Ergebnistabelle der Qualitätsprüfung Bild/TU sein soll
        validationPictureAndTUForm = new iPartsValidationPictureAndTUForm(getConnector(), this, false, false);

        // Gui mit Tabelle an TabbedPaneEntry hängen
        pictureAndTUPanel.addChildBorderCenter(validationPictureAndTUForm.getGui());

        if (!isSimplifiedQualityCheck()) {
            tabbedPane.addChild(partlistAndColor);
            tabbedPane.addChild(finValidation);
        }
        tabbedPane.addChild(pictureAndTU);

        if (!isSimplifiedQualityCheck()) {
            tabbedPane.selectTab(partlistAndColor);
        } else {
            tabbedPane.selectTab(pictureAndTU);
        }

        // Alten Dialog-GUI entfernen, da diese jetzt in eine TabPaneEntry lebt
        // und TabbedPane hinzufügen
        getMainWindow().removeChild(oldDialogGUI);
        getMainWindow().addChild(tabbedPane);
    }

    public void setParentForm(AbstractJavaViewerForm parentForm) {
        this.parentForm = parentForm;
    }

    @Override
    protected void postCreateGui(boolean removeAdditionalInfoPanel, boolean minimizeAdditionalInfoPanel) {
        EtkDataAssembly assembly = EditModuleHelper.getAssemblyFromConnector(getConnector());
        if (assembly instanceof iPartsDataAssembly) {
            setSubTitle(assembly.getHeading1(-1, null));
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
            iPartsProductId productId = (iPartsAssembly).getProductIdFromModuleUsage();
            if ((productId != null) && productId.isValidId()) {
                Set<String> productModels = iPartsProduct.getInstance(getProject(), productId).getModelNumbers(getProject());
                // Produkt nicht angeben, damit die BM nicht in der Session gespeichert werden
                setSelectedModels(productModels, null, false);
                iPartsSeriesId seriesId = iPartsProduct.getInstance(getProject(), productId).getReferencedSeries();
                if ((seriesId != null) && seriesId.isValidId()) {
                    series = iPartsDialogSeries.getInstance(getProject(), seriesId);
                }
            }
        }

        authorOrder = iPartsDataAuthorOrderList.getAuthorOrderByActiveChangeSet(getProject());

        String title = translate("!!Qualitätsprüfungen");
        if ((authorOrder != null)) {
            title += " " + translate("!!für Autoren-Auftrag \"%1\"", authorOrder.getFieldValue(FIELD_DAO_NAME));
        }
        setTitle(title);

        partListEntryCompareLfdNr = new Comparator<EtkDataPartListEntry>() {
            @Override
            public int compare(EtkDataPartListEntry o1, EtkDataPartListEntry o2) {
                return o1.getAsId().getKLfdnr().compareTo(o2.getAsId().getKLfdnr());
            }
        };

        final GuiButtonOnPanel openInNewWindowButton = getButtonPanel().addCustomButton("!!In neuem Fenster öffnen");
        openInNewWindowButton.setIcon(EditDefaultImages.edit_openNonModalMechanicWindow.getImage());

        final GuiButtonOnPanel refreshButton = getButtonPanel().addCustomButton("!!Aktualisieren");
        refreshButton.setIcon(EditDefaultImages.edit_btn_refresh.getImage());
        refreshButton.setVisible(false);
        iPartsDataAuthorOrder initialyActiveAuthorOrder = getConnector().getCurrentDataAuthorOrder();
        iPartsAuthorOrderId connectorAuthorOrderId = null;
        if (initialyActiveAuthorOrder != null) {
            connectorAuthorOrderId = initialyActiveAuthorOrder.getAsId();
        }
        refreshButton.setEnabled(isRefreshEnabled(connectorAuthorOrderId));

        EventListener openInNewWindowButtonListener = new EventListener(Event.MOUSE_CLICKED_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                openInNewWindowButton.setVisible(false);
                showNonModal();
                refreshButton.setVisible(true);
            }
        };
        openInNewWindowButton.addEventListener(openInNewWindowButtonListener);

        final iPartsAuthorOrderId formAuthorOrderId = (authorOrder != null) ? authorOrder.getAsId() : null;
        getProject().addAppEventListener(new ObserverCallback(callbackBinder, AuthorOrderChangedEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                AuthorOrderChangedEvent authorOrderChangedEvent = (AuthorOrderChangedEvent)call;
                // Fenster immer schließen sobald sich der Autorenauftrag ändert oder deaktiviert wird.
                // Spezialfall: Die Form wurde für einen Autorenauftrag aus der EditChangeSetViewerForm heraus erzeugt,
                // der nicht aktiviert war. Dann bleibt das Fenster nach Aktivieren des entsprechenden Auftrags offen.
                iPartsAuthorOrderId newAuthorOrderId = authorOrderChangedEvent.getAuthorOrderId();
                boolean isSameAuthorOrder = ((newAuthorOrderId == null) && (formAuthorOrderId == null))
                                            || ((newAuthorOrderId != null) && newAuthorOrderId.equals(formAuthorOrderId));
                if (!isSameAuthorOrder) {
                    close();
                    return;
                }
                refreshButton.setEnabled(isRefreshEnabled(authorOrderChangedEvent.getAuthorOrderId()));
            }
        });

        final Runnable updateViewRunnable = () -> {
            // die Assembly neu laden damit alle parallel gemachten Änderungen berücksichtigt werden
            AssemblyId assemblyId = getConnector().getCurrentAssembly().getAsId();
            EtkDataAssembly updatedAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
            getConnector().setCurrentAssembly(updatedAssembly);
            Session.get().invokeThreadSafeForced(() -> {
                resetMarkFirstErrorOrWarning(); // Selektion zurücksetzen damit wieder zum ersten Fehler gesprungen wird.
                updateView();
            });
        };

        EventListener refreshButtonListener = new EventListener(Event.MOUSE_CLICKED_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                // Marquee im Hauptfenster (NICHT das aktuelle Browserfenster, da der Aktualisieren Button nur im separaten Fenster angeboten
                // wird und Sinn macht) anzeigen. Damit ist dort die Bearbeitung gesperrt, und man umgeht denn Fall, dass der Autor
                // Änderungen vornimmt, während die Berechnung noch läuft.
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Qualitätsprüfung läuft",
                                                                               "!!Bitte warten...", null, true);
                messageLogForm.disableButtons(true);
                messageLogForm.showModal(Session.get().getMasterRootWindow(), new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        // Aktualisieren im eigenen Fenster als Edit-Aktion behandeln. Damit kann z.B. der Deadlock nicht auftreten, dass während der
                        // Berechnung im invokeThreadSafe() eine Pseudotransaktion gestartet werden soll, aber der Hauptthread gerade in einer Edit-Aktion
                        // auch eine Pseudotransaktion ausführt und darin wiederum wartet bis invokeThreadSafe() freigegeben wird.
                        if (!iPartsEditPlugin.startEditing()) {
                            return;
                        }
                        try {
                            // Für denn Fall, dass der Dialog die Ergebnisse für einen bestimmten Autorenauftrag anzeigt, muss dieser vor der Aktualisierung
                            // wieder aktiviert werden, damit die neuen Ergebnisse für genau diesen Auftrag berechnet werden.
                            final EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
                            if ((revisionsHelper != null)) {
                                boolean isAuthorOrderActivatable = false;
                                iPartsChangeSetId changeSetId = null;
                                if (authorOrder != null) {
                                    isAuthorOrderActivatable = authorOrder.isActivatable();
                                    changeSetId = authorOrder.getChangeSetId();
                                }
                                revisionsHelper.runInChangeSet(getProject(), updateViewRunnable, isAuthorOrderActivatable, changeSetId);
                            }
                        } finally {
                            iPartsEditPlugin.stopEditing();
                        }
                    }
                });
            }
        };
        refreshButton.addEventListener(refreshButtonListener);

        super.postCreateGui(removeAdditionalInfoPanel, minimizeAdditionalInfoPanel);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (validationPictureAndTUForm != null) {
            validationPictureAndTUForm.dispose();
        }
        if (finValidationForm != null) {
            finValidationForm.dispose();
        }
    }

    @Override
    public void showNonModal() {
        if (parentForm instanceof iPartsAuthorOrderPreReleaseCheckResults) {
            setRelatedFormForNonModalShow(parentForm);
            if (validationPictureAndTUForm != null) {
                validationPictureAndTUForm.setRelatedFormForNonModalShow(parentForm);
            }
            if (finValidationForm != null) {
                finValidationForm.setRelatedFormForNonModalShow(parentForm);
            }
        }
        super.showNonModal();
    }

    /**
     * Ist der Aktualisieren-Button aktiv? Das ist der Fall, wenn der Autorenauftrag auf den sich die Form bezieht
     * gleich dem gerade aktiven ist, da nur dann tatsächlich Änderungen in dem Autorenauftrag stattfinden können.
     *
     * @param activeAuthorOrderId der gerade aktive Autorenauftrag.
     * @return
     */
    private boolean isRefreshEnabled(iPartsAuthorOrderId activeAuthorOrderId) {
        return (authorOrder != null) && authorOrder.getAsId().equals(activeAuthorOrderId);
    }

    public void setUseMessageLog(boolean useMessageLog) {
        this.useMessageLog = useMessageLog;
    }

    @Override
    protected void doCancelCalculation() {
        super.doCancelCalculation();
        validationEntries.clear();
    }

    @Override
    public void updateValidationContent(boolean getSelectedModelsFromSession, boolean forceReloadAssembly, boolean updateValidationGUI) {
        if (validationEntries == null) { // Kann offenbar während der Initialisierung durch eine Race-Condition passieren
            return;
        }

        validationEntries.clear();
        if (useMessageLog) {
            initMessageLog("!!Qualitätsprüfungen", "!!TU wird überprüft");
        }
        super.updateValidationContent(getSelectedModelsFromSession, forceReloadAssembly, updateValidationGUI);
        if (isCancelled()) {
            validationEntries.clear();
        }
    }

    private void cancelPictureAndTUThread() {
        FrameworkThread pictureAndTUThreadLocal = pictureAndTUThread;
        if (pictureAndTUThreadLocal != null) {
            pictureAndTUThreadLocal.cancel();
            pictureAndTUThread = null;
        }
    }

    private void startPictureAndTUThread() {
        pictureAndTUThread = Session.get().startChildThread(thread -> validationPictureAndTUForm.startPictureAndTUValidation());
    }

    private void waitForPictureAndTUThreadFinished() {
        FrameworkThread pictureAndTUThreadLocal = pictureAndTUThread;
        if ((pictureAndTUThreadLocal != null) && !pictureAndTUThreadLocal.wasCanceled()) {
            pictureAndTUThreadLocal.waitFinished();
        }
    }

    private void cancelFINValidationThread() {
        FrameworkThread finValidationThreadLocal = finValidationThread;
        if (finValidationThreadLocal != null) {
            finValidationThreadLocal.cancel();
            finValidationThread = null;
        }
    }

    private void startFINValidationThread() {
        finValidationThread = Session.get().startChildThread(thread -> finValidationForm.startFINValidation());
    }

    private void waitForFINValidationThreadFinished() {
        FrameworkThread finValidationThreadLocal = finValidationThread;
        if ((finValidationThreadLocal != null) && !finValidationThreadLocal.wasCanceled()) {
            finValidationThreadLocal.waitFinished();
        }
    }

    @Override
    protected void beforeValidatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly) {
        runTimeLoggers = new ValidationTimers(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK);
        runTimeLoggers.setkVari(assembly.getAsId().getKVari());
        validationEntries.clear();
        colorTableValidationResult.clear();
        firstIndexWithErrorOrWarning = -1;

        // Erst die erweiterte Qualitätsprüfung Daimler-8773 als eigenen Thread starten DAIMLER-15015
        cancelPictureAndTUThread();
        startPictureAndTUThread();

        cancelFINValidationThread();
        if (finValidationForm.FINsExistForProduct()) {
            startFINValidationThread();
        } else {
            // Falls am Produkt keine FINs gespeichert sind, den Tab für diese Auswertung nicht anzeigen
            tabbedPane.removeChild(finValidation);
        }

        if (isValidationForColorsPossible(assembly)) {
            fireMessage("!!Qualitätsprüfung für Farben");
            // Qualitätsprüfung Farbvarianten
            EtkDataAssembly currentAssembly = getCurrentAssembly();
            if (currentAssembly instanceof iPartsDataAssembly) {
                runTimeLoggers.setStartTime(TIMER_TYPES.TIMER_COLOR_CHECK);
                Set<String> modelsForQualityCheck = iPartsEditValidationHelper.getAllModelsForAssembly((iPartsDataAssembly)currentAssembly);
                Map<String, iPartsEditValidationHelper.ColortableQualityCheckResult> colortableQualityChecksCache = new HashMap<>();
                int currentIndex = 0;
                int firstErrorIndex = -1;
                int firstWarningIndex = -1;
                for (EtkDataPartListEntry partListEntry : partlist) {
                    if (isCancelled()) {
                        return;
                    }
                    fireProgress();
                    if (partListEntry instanceof iPartsDataPartListEntry) {
                        ValidationResult currentResult = iPartsEditValidationHelper.executeColortableQualityChecks((iPartsDataPartListEntry)partListEntry,
                                                                                                                   modelsForQualityCheck, colortableQualityChecksCache);
                        colorTableValidationResult.put(partListEntry.getAsId().getKLfdnr(), currentResult);
                        if ((firstErrorIndex == -1) && (currentResult == ValidationResult.ERROR)) {
                            firstErrorIndex = currentIndex;
                        }
                        if ((firstWarningIndex == -1) && (currentResult == ValidationResult.WARNING)) {
                            firstWarningIndex = currentIndex;
                        }
                    }
                    currentIndex++;
                }

                if (firstErrorIndex != -1) {
                    firstIndexWithErrorOrWarning = firstErrorIndex;
                } else {
                    firstIndexWithErrorOrWarning = firstWarningIndex;
                }
                runTimeLoggers.stopTimeAndStore(TIMER_TYPES.TIMER_COLOR_CHECK);
            }
        }

        super.beforeValidatePartList(partlist, assembly);
    }

    /**
     * Für TRUCK nur die normale Baumusterauswertung durchführen
     * Für DIALOG folgen nach der Baumusterauswertung noch weitere Prüfungen
     *
     * @param partlist
     * @param assembly
     * @param selectedModel
     */
    @Override
    protected void validatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly, String selectedModel) {
        if (!isValidationForPartListPossible(assembly)) {
            return;
        }

        fireProgress();
        Map<String, Set<iPartsDataPartListEntry>> evaluationGroups = new HashMap<>();
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (isCancelled()) {
                return;
            }
            boolean partListEntryValidForModel = filterForModelEvaluation.checkFilter(partListEntry);
            if (partListEntryValidForModel) {

                // mit DAIMLER-9042 wieder deaktiviert
                // Unechte Wahlweise sollen nicht zum Vergleich hergezogen werden
                // Und können hier auf Validation Result OK gesetzt werden
//                if (isFakeWW(partListEntry)) {
//                    setResult(partListEntry, selectedModel, ValidationResult.OK);
//                    getValidationEntry(partListEntry, selectedModel).filterReason = getInvisiblePartOrZZPartMessage(partListEntry);
//                    continue;
//                }

                if (assembly.getDocumentationType().isPKWDocumentationType()) {
                    String key = createEvalKey(selectedModel, partListEntry);
                    String steering = partListEntry.getFieldValue(FIELD_K_STEERING);
                    if (steering.isEmpty()) {
                        addToEvalGroup(evaluationGroups, partListEntry, key, SteeringIdentKeys.STEERING_LEFT);
                        addToEvalGroup(evaluationGroups, partListEntry, key, SteeringIdentKeys.STEERING_RIGHT);
                    } else {
                        addToEvalGroup(evaluationGroups, partListEntry, key, steering);
                    }
                    setResult(partListEntry, selectedModel, ValidationResult.UNCHECKED);
                } else {
                    setResult(partListEntry, selectedModel, ValidationResult.OK);
                }
            } else {
                // DAIMLER-8054: Die folgenden Stücklisteneinträge sind rausgeflogen weil sie Entfallteile sind
                // oder weil das Kennzeichen "nur Baumuster-Filter" oder "unterdrücken" gesetzt ist. Für solche
                // Stücklisteneinträge soll die Filterung nochmal durchgeführt werden, ohne die entsprechenden Filter.
                if (isPartListEntryInvisibleAndValid(partListEntry)) {
                    setResult(partListEntry, selectedModel, ValidationResult.INVISIBLE_PART_VALID);
                    getValidationEntry(partListEntry, selectedModel).filterReason = getInvisiblePartOrZZPartMessage(partListEntry);
                } else {
                    setResult(partListEntry, selectedModel, ValidationResult.MODEL_INVALID);
                }
            }
        }

        // weitere Prüfungen für DIALOG
        if (assembly.getDocumentationType().isPKWDocumentationType()) {
            Set<String> alreadyCheckedEntries = new HashSet<>();
            for (iPartsDataPartListEntry currentEntry : getEntriesForResult(partlist, selectedModel, ValidationResult.UNCHECKED)) {
                List<iPartsDataPartListEntry> compareGroup = getCompareGroup(evaluationGroups, currentEntry, selectedModel);

                // Nur im DEVELOPMENT-Modus die laufenden Nummern von allen Stücklisteneinträgen der Vergleichs-Gruppe ausgeben
                if (Constants.DEVELOPMENT) {
                    DwList<String> compareGroupText = new DwList<>();
                    for (iPartsDataPartListEntry compareEntry : compareGroup) {
                        compareGroupText.add(compareEntry.getAsId().getKLfdnr());
                    }
                    getValidationEntry(currentEntry, selectedModel).compareGroupOutput = StrUtils.stringListToString(compareGroupText,
                                                                                                                     ", ");
                }

                if (compareGroup.isEmpty()) {
                    // Es gibt keinen Eintrag zum vergleichen also OK setzen
                    setResult(currentEntry, selectedModel, ValidationResult.OK);
                }

                for (iPartsDataPartListEntry compareEntry : compareGroup) {
                    // Nur solche Stücklisteneinträge prüfen, die nicht bereits vollständig geprüft wurden
                    if (!alreadyCheckedEntries.contains(compareEntry.getAsId().getKLfdnr())) {
                        if (checkPartlistEntries(currentEntry, compareEntry)) {
                            setResult(currentEntry, selectedModel, ValidationResult.OK);
                            setResult(compareEntry, selectedModel, ValidationResult.OK);
                        } else {
                            ValidationResult validationResult = ValidationResult.OK;
                            // gleiche Teilkonjunktion und gleicher Text ergibt Fehler
                            if (isSameCombinedText(currentEntry, compareEntry)) {
                                validationResult = ValidationResult.ERROR;
                            }
                            setResult(currentEntry, compareEntry, selectedModel, validationResult);
                            setResult(compareEntry, currentEntry, selectedModel, validationResult);
                        }
                    }
                }

                // Der Stücklisteneintrag currentEntry ist nun vollständig geprüft
                alreadyCheckedEntries.add(currentEntry.getAsId().getKLfdnr());
            }

            // Jetzt alle Fehlerhaften Einträge durchgehen und den genauen Fehlergrund bestimmen
            List<iPartsDataPartListEntry> errorEntries = getEntriesWithErrorOrWarning(partlist, selectedModel);
            for (iPartsDataPartListEntry errorEntry : errorEntries) {
                ValidationEntry validationEntry = getValidationEntry(errorEntry, selectedModel);
                Set<String> otherErrorEntries = validationEntry.otherEntries;
                if (otherErrorEntries != null) {
                    List<iPartsDataPartListEntry> compareGroup = getCompareGroup(evaluationGroups, errorEntry, selectedModel);
                    for (iPartsDataPartListEntry compareEntry : compareGroup) {
                        // Nochmal prüfen, ob wirklich diese beiden Einträge die fehlerhaften sind
                        if (otherErrorEntries.contains(compareEntry.getAsId().getKLfdnr())) {
                            // Nochmal die Teilkonjunktionen prüfen und einen genauen Fehlergrund ermitteln
                            String overlappingConjunction = iPartsEditValidationHelper.isPartialConjunctionOverlapWithErrorMessage(
                                    errorEntry.getFieldValue(FIELD_K_CODES_REDUCED), compareEntry.getFieldValue(FIELD_K_CODES_REDUCED));
                            if (!overlappingConjunction.isEmpty()) {
                                validationEntry.filterReason = getErrorLogMessage(errorEntry, compareEntry, overlappingConjunction,
                                                                                  selectedModel, validationEntry.filterReason);
                            }
                        }
                    }
                }
            }
        }

        for (EtkDataPartListEntry partListEntry : partlist) {
            String fieldName = createVirtualFieldNameForModelOrFINEvaluation(selectedModel);
            partListEntry.setFieldValue(fieldName, getResult(partListEntry, selectedModel).getDbValue(), DBActionOrigin.FROM_DB);
        }
    }

    @Override
    protected void afterValidatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly) {
        super.afterValidatePartList(partlist, assembly);

        boolean isValidationForPartListPossible = isValidationForPartListPossible(assembly);
        boolean isValidationForColorsPossible = isValidationForColorsPossible(assembly);
        if (isValidationForPartListPossible) {
            Map<String, ValidationEntry> combTextsValidationEntriesMap = null;
            Map<String, ValidationEntry> addFactoriesValidationEntriesMap = null;
            if (assembly.getDocumentationType().isDIALOGDocumentationType()) {
                runTimeLoggers.setStartTime(TIMER_TYPES.TIMER_COMBINED_TEXTS_CHECK);
                combTextsValidationEntriesMap = executeCombTextsChecks(partlist, assembly);
                runTimeLoggers.stopTimeAndStore(TIMER_TYPES.TIMER_COMBINED_TEXTS_CHECK);

                // Prüfung auf Werksdaten aus Zusatzwerken nur für Fahrzeug-Produkte
                EtkProject project = getProject();
                iPartsProductId productId = assembly.getProductIdFromModuleUsage();
                if ((productId != null) && !iPartsProduct.getInstance(project, productId).isAggregateProduct(project)) {
                    runTimeLoggers.setStartTime(TIMER_TYPES.TIMER_ADD_FACTORIES_CHECK);
                    addFactoriesValidationEntriesMap = executeAddFactoriesCheck(partlist);
                    runTimeLoggers.stopTimeAndStore(TIMER_TYPES.TIMER_ADD_FACTORIES_CHECK);
                }
            }

            int currentIndex = 0;
            int firstErrorIndex = -1;
            int firstWarningIndex = -1;
            for (EtkDataPartListEntry partListEntry : partlist) {
                ValidationResult totalResult = ValidationResult.OK;

                if (isValidationForColorsPossible) {
                    // Ergebnis der Qualitätsprüfung für Farben dazumischen
                    ValidationResult colorTableValidationResult = this.colorTableValidationResult.get(partListEntry.getAsId().getKLfdnr());
                    if (colorTableValidationResult != null) {
                        totalResult = mergeTotalValidationResult(totalResult, colorTableValidationResult);
                    }
                }

                // Ergebnis der Qualitätsprüfung für Stücklisteneinträge dazumischen
                boolean partlistEntryInvalidForAllModels = true;
                for (String model : getModelsForEvaluation()) {
                    // Danach die Qualitätsprüfung für Teilkonjunktionen pro Baumuster checken
                    ValidationEntry validationEntry = getValidationEntry(partListEntry, model);
                    totalResult = mergeTotalValidationResult(totalResult, validationEntry.validationResult);

                    if (validationEntry.validationResult != ValidationResult.MODEL_INVALID) {
                        partlistEntryInvalidForAllModels = false;
                    }
                }

                // Allgemeine Prüfungen für DIALOG und TRUCK
                if (assembly.getDocumentationType().isPKWDocumentationType()) {
                    // Prüfen, ob dieser Stücklisteneintrag einen Hotspot hat, aber für kein Baumuster gültig ist
                    executePLEWithValidHotspotIsNotValidForModelsCheck(partListEntry, partlistEntryInvalidForAllModels);
                    // Werkseinsatzdaten prüfen
                    runTimeLoggers.setStartTime(TIMER_TYPES.TIMER_FACTORY_DATA_CHECK);
                    executePLEFactoryDataExistsCheck(partListEntry);
                    runTimeLoggers.stopTimeAndStore(TIMER_TYPES.TIMER_FACTORY_DATA_CHECK);

                    if (combTextsValidationEntriesMap != null) {
                        // Ergebnis der Qualitätsprüfungen für Ergänzungstexte
                        ValidationEntry validationEntry = combTextsValidationEntriesMap.get(partListEntry.getAsId().getKLfdnr());
                        if (validationEntry != null) {
                            addGeneralValidationResult(partListEntry, validationEntry.validationResult, validationEntry.filterReason);
                        }
                    }

                    if (addFactoriesValidationEntriesMap != null) {
                        // Ergebnis der Qualitätsprüfungen für Zusatzwerke
                        ValidationEntry validationEntry = addFactoriesValidationEntriesMap.get(partListEntry.getAsId().getKLfdnr());
                        if (validationEntry != null) {
                            addGeneralValidationResult(partListEntry, validationEntry.validationResult, validationEntry.filterReason);
                        }
                    }
                } else if (assembly.getDocumentationType().isTruckDocumentationType()) {
                    // Prüfungen nicht bei freien SA durchführen
                    if (!assembly.isSAAssembly()) {
                        executePLEModelValidityCheck(partListEntry);
                        executePLEWithValidHotspotIsNotValidForModelsCheck(partListEntry, partlistEntryInvalidForAllModels);
                    }
                    executeWWPartOmittedCheck(partListEntry);
                }

                // Generisches Validierungsergebnis berücksichtigen
                if (totalResult != ValidationResult.ERROR) {
                    ValidationResult generalValidationResult = getGeneralValidationEntry(partListEntry).validationResult;
                    if ((generalValidationResult == ValidationResult.ERROR) || (generalValidationResult == ValidationResult.WARNING)) {
                        totalResult = generalValidationResult;
                    }
                }

                if ((firstErrorIndex == -1) && (totalResult == ValidationResult.ERROR)) {
                    firstErrorIndex = currentIndex;
                }
                if ((firstWarningIndex == -1) && (totalResult == ValidationResult.WARNING)) {
                    firstWarningIndex = currentIndex;
                }

                // Gesamtergebnis der Qualitätsprüfung setzen
                partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_QUALITY_CHECK_ERROR,
                                                       convertFromValidationResult(totalResult).name(), true, DBActionOrigin.FROM_DB);
                currentIndex++;
                fireProgress();
            }

            if (firstIndexWithErrorOrWarning == -1) {
                if (firstErrorIndex != -1) {
                    firstIndexWithErrorOrWarning = firstErrorIndex;
                } else {
                    firstIndexWithErrorOrWarning = firstWarningIndex;
                }
            } else {
                if (firstErrorIndex == -1) {
                    firstErrorIndex = firstWarningIndex;
                }
                if (firstErrorIndex != -1) {
                    firstIndexWithErrorOrWarning = Math.min(firstErrorIndex, firstIndexWithErrorOrWarning);
                }
            }
        }

        ValidationResult colorAndPartlistTableValidationResult = getColorAndPartlistTableValidationResult();
        runTimeLoggers.logRunTimes();
        // Warten auf pictureAndTUForm
        waitForPictureAndTUThreadFinished();
        waitForFINValidationThreadFinished();

        ValidationResult finValidationResult = finValidationForm.getTotalValidationResult();
        ValidationResult pictureAndTUTableValidationResult = validationPictureAndTUForm.getTotalValidationResult();
        Session.invokeThreadSafeInSession(() -> {
            setTabTitle(partlistAndColor, colorAndPartlistTableValidationResult);
            setTabTitle(finValidation, finValidationResult);
            setTabTitle(pictureAndTU, pictureAndTUTableValidationResult);

            // Falls es keine Validierung für die Stückliste/Farben gibt oder es nur im PictureAndTU-Tab Fehler gibt, dann diesen
            // als erstes zeigen
            // Bei TRUCK gib es keine Farb-Prüfung. Deswegen nur nach Stücklistenprüfung fragen. Die gibt es bei DIALOG und TRUCK
            if (!isValidationForPartListPossible || ((colorAndPartlistTableValidationResult == ValidationResult.OK)
                                                     && (pictureAndTUTableValidationResult != ValidationResult.OK))) {
                tabbedPane.selectTab(pictureAndTU);
            }
        });
    }

    /**
     * Prüfung je Teileposition, ob Baumuster aus BM-Gültigkeit zu den BM aus dem Produkt passen
     *
     * @param partListEntry
     */
    private void executePLEModelValidityCheck(EtkDataPartListEntry partListEntry) {
        EtkDataArray modelValiditiesArray = partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_MODEL_VALIDITY);
        if (modelValiditiesArray == null) {
            return;
        }

        runTimeLoggers.setStartTime(TIMER_TYPES.TIMER_MODEL_VALIDITY_CHECK);
        List<String> modelValidities = modelValiditiesArray.getArrayAsStringList();
        Set<String> modelsNotInProductSet = new TreeSet<>();
        for (String modelValidity : modelValidities) {
            if (!getSelectedModels().contains(modelValidity)) {
                modelsNotInProductSet.add(modelValidity);
            }
        }
        if (!modelsNotInProductSet.isEmpty()) {
            String productName = "?";
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsProductId productId = ((iPartsDataPartListEntry)partListEntry).getOwnerAssembly().getProductIdFromModuleUsage();
                if (productId != null) {
                    EtkProject project = getProject();
                    productName = iPartsProduct.getInstance(project, productId).getProductTitle(project).getTextByNearestLanguage(project.getDBLanguage(),
                                                                                                                                  project.getDataBaseFallbackLanguages());
                }
            }
            addGeneralValidationResult(partListEntry, ValidationResult.WARNING, translate("!!Baumuster nicht im Produkt \"%2\" enthalten: %1",
                                                                                          StrUtils.stringListToString(modelsNotInProductSet, ", "),
                                                                                          productName));
        }
        runTimeLoggers.stopTimeAndStore(TIMER_TYPES.TIMER_MODEL_VALIDITY_CHECK);
    }

    /**
     * Teileposition mit gültigem Hotspot ist für kein Baumuster gültig
     *
     * @param partListEntry
     */
    private void executePLEWithValidHotspotIsNotValidForModelsCheck(EtkDataPartListEntry partListEntry, boolean partlistEntryInvalidForAllModels) {
        String hotspot = partListEntry.getFieldValue(FIELD_K_POS);
        if (!hotspot.isEmpty() && partlistEntryInvalidForAllModels) {
            addGeneralValidationResult(partListEntry, ValidationResult.ERROR, "!!Teileposition hat einen gültigen Hotspot, ist aber für kein Baumuster gültig.");
        }
    }

    /**
     * Teileposition (im Wahlweiseset) ist unterdrückt
     *
     * @param partListEntry
     */
    private void executeWWPartOmittedCheck(EtkDataPartListEntry partListEntry) {
        if (partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT)) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                if (iPartsWWPartsHelper.hasWWParts(iPartsPartListEntry, false)) {
                    addGeneralValidationResult(partListEntry, ValidationResult.ERROR, "!!Teileposition ist unterdrückt, befindet sich aber in einem Wahlweiseset");
                } else {
                    addGeneralValidationResult(partListEntry, ValidationResult.WARNING, "!!Teileposition ist unterdrückt");
                }
            }
        }
    }

    /**
     * Prüft, ob bei gesetztem/berechneten Flag "PEM-ab/bis auswerten" es ein gültiges Datum PEM-ab/bis bei den Werkseinsatzdaten
     * gibt.
     *
     * @param partListEntry
     */
    private void executePLEFactoryDataExistsCheck(EtkDataPartListEntry partListEntry) {
        if (partListEntry instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;

            // "PEM ab auswerten"-Flag gesetzt?
            if (iPartsPartListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED)) {
                boolean validFactoryDataFound = false;
                iPartsFactoryData factoryData = iPartsPartListEntry.getFactoryDataForRetail();
                if ((factoryData != null) && factoryData.hasValidFactories()) {
                    factoryDataLoop:
                    for (List<iPartsFactoryData.DataForFactory> factoryDataList : factoryData.getFactoryDataMap().values()) {
                        for (iPartsFactoryData.DataForFactory dataForFactory : factoryDataList) {
                            if (dataForFactory.hasValidDateFrom() && (dataForFactory.dateFrom != 0)) { // Echtes PEM-ab Datum vorhanden?
                                validFactoryDataFound = true;
                                break factoryDataLoop;
                            }
                        }
                    }
                }

                if (!validFactoryDataFound) {
                    addGeneralValidationResult(partListEntry, ValidationResult.ERROR, "!!Keine Werkseinsatzdaten mit gültiger PEM-ab vorhanden trotz \"Auswertung PEM-ab\"");
                }
            }

            // "PEM bis auswerten"-Flag gesetzt?
            if (iPartsPartListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED)) {
                boolean validFactoryDataFound = false;
                iPartsFactoryData factoryData = iPartsPartListEntry.getFactoryDataForRetail();
                if ((factoryData != null) && factoryData.hasValidFactories()) {
                    factoryDataLoop:
                    for (List<iPartsFactoryData.DataForFactory> factoryDataList : factoryData.getFactoryDataMap().values()) {
                        for (iPartsFactoryData.DataForFactory dataForFactory : factoryDataList) {
                            if (dataForFactory.hasValidDateTo() && (dataForFactory.dateTo != 0)) { // Echtes PEM-bis Datum vorhanden?
                                validFactoryDataFound = true;
                                break factoryDataLoop;
                            }
                        }
                    }
                }

                if (!validFactoryDataFound) {
                    addGeneralValidationResult(partListEntry, ValidationResult.ERROR, "!!Keine Werkseinsatzdaten mit gültiger PEM-bis vorhanden trotz \"Auswertung PEM-bis\"");
                }
            }
        }
    }

    /**
     * Prüfung der Durchgängigkeit vom Ergänzungstext an allen Teilepositionen eines Hotspots sowie nur ein Ergänzungstext
     * je Teileposition
     *
     * @param partlist
     * @param assembly
     * @return
     */
    private Map<String, ValidationEntry> executeCombTextsChecks(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly) {
        iPartsOmittedParts omittedParts = iPartsOmittedParts.getInstance(getProject());
        // Map von Hotspot auf Stücklisteneinträge aufbauen
        Map<String, List<iPartsDataPartListEntry>> posToPartListEntriesMap = new TreeMap<>();
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                String hotspot = partListEntry.getFieldValue(FIELD_K_POS);
                boolean omitFlag = partListEntry.getFieldValueAsBoolean(FIELD_K_OMIT);
                // DAIMLER-14781: keine Prüfung bei leerem Hotspot, für Unterdrückte Teilepos und für Wegfall-SNR
                if (!hotspot.isEmpty() && !omitFlag && !omittedParts.isOmittedPart(partListEntry)) {
                    List<iPartsDataPartListEntry> partListEntriesForPos = posToPartListEntriesMap.computeIfAbsent(
                            hotspot, pos -> new ArrayList<>());
                    partListEntriesForPos.add((iPartsDataPartListEntry)partListEntry);
                }
            }
        }

        // Falls wir mehr als eine Position haben, lade die kombinierten Texte für das gesamte Modul und setze die Texte
        // an den jeweiligen Positionen. Ansonsten würden wir für jede Position in partListEntryForPos.getDataCombTextList()
        // in die DB gehen.
        if ((partlist.size() > 1) && (assembly != null)) {
            assembly.loadAllDataCombTextListsForPartList(partlist, false);
        }

        // DAIMLER-14585 1. Durchgängigkeit Ergänzungstext an allen Teilepositionen eines Hotspots
        Map<String, ValidationEntry> combTextsValidationEntriesMap = new HashMap<>();
        EtkProject project = getProject();
        Map<String, EditCombinedTextForm.TextKindType> textKindsForCombTexts = EditCombinedTextForm.loadTextKinds(project);
        for (Map.Entry<String, List<iPartsDataPartListEntry>> posToPartListEntriesMapEntry : posToPartListEntriesMap.entrySet()) {
            List<iPartsDataPartListEntry> noCombTextFound = new DwList<>();
            boolean combTextFound = false;
            for (iPartsDataPartListEntry partListEntryForPos : posToPartListEntriesMapEntry.getValue()) {
                // Zuerst über das virtuelle Feld prüfen, ob es überhaupt einen kombinierten Ergänzungstext gibt
                if (!partListEntryForPos.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT).isEmpty()) {
                    // Falls ja, dann die einzelnen Textbausteine laden
                    iPartsDataCombTextList dataCombTextList = partListEntryForPos.getDataCombTextList();
                    Map<DictTextKindTypes, iPartsDataCombTextList> textKindToCombTextMap = iPartsEditCombTextHelper.getTextKindToCombTextMap(project,
                                                                                                                                             dataCombTextList,
                                                                                                                                             textKindsForCombTexts);
                    iPartsDataCombTextList addTextList = textKindToCombTextMap.get(DictTextKindTypes.ADD_TEXT);
                    if (Utils.isValid(addTextList)) { // Ergänzungstext vorhanden?
                        combTextFound = true;

                        // DAIMLER-14585 2. Nur ein Ergänzungstext je Teileposition
                        if (addTextList.size() > 1) {
                            StringBuilder addTexts = new StringBuilder();
                            for (iPartsDataCombText addText : addTextList) {
                                if (addTexts.length() > 0) {
                                    addTexts.append(", ");
                                }
                                addTexts.append("\"").append(addText.getFieldValue(FIELD_DCT_DICT_TEXT, project.getDBLanguage(), true)).append("\"");
                            }
                            ValidationEntry validationEntry = new ValidationEntry(ValidationResult.ERROR, translate("!!Mehr als ein Ergänzungstext: %1",
                                                                                                                    addTexts.toString()));
                            combTextsValidationEntriesMap.put(partListEntryForPos.getAsId().getKLfdnr(),
                                                              validationEntry);
                        }

                        continue;
                    }
                }

                // Kein echter nicht-sprachneutraler Ergänzungstext vorhanden
                noCombTextFound.add(partListEntryForPos);
            }

            if (combTextFound) {
                for (iPartsDataPartListEntry partListEntryWithoutCombText : noCombTextFound) {
                    ValidationEntry validationEntry = new ValidationEntry(ValidationResult.WARNING, translate("!!Fehlender Ergänzungstext für Hotspot %1",
                                                                                                              posToPartListEntriesMapEntry.getKey()));
                    combTextsValidationEntriesMap.put(partListEntryWithoutCombText.getAsId().getKLfdnr(),
                                                      validationEntry);
                }
            }
        }

        if (assembly != null) {
            // Daten der kombinierten Texte wieder entfernen, da diese auch nur für die aktuelle DB-Sprache geladen wurden
            assembly.clearAllDataCombTextListsForPartList();
        }

        return combTextsValidationEntriesMap;
    }

    /**
     * Prüfung filterrelevante Teilepositionen von C-Stücklisten mit Werksdaten aus Zusatzwerken
     *
     * @param partlist
     * @return
     */
    private Map<String, ValidationEntry> executeAddFactoriesCheck(List<EtkDataPartListEntry> partlist) {
        // DAIMLER-14585 3. Filterrelevante Teilepositionen von C-Stücklisten mit Werksdaten aus Zusatzwerken
        EnumValue addFactoriesEnumTokens = getProject().getEtkDbs().getEnumValue(ADD_FACTORIES_ENUM_KEY);
        Set<String> addFactories = addFactoriesEnumTokens.keySet();

        Map<String, ValidationEntry> addFactoriesValidationEntriesMap = new HashMap<>();
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPLE = (iPartsDataPartListEntry)partListEntry;

                // "PEM ab auswerten"-Flag oder "PEM bis auswerten"-Flag gesetzt?
                // Die Prüfung wird nur für Teilepositionen durchgeführt, bei denen entweder "PEM-AB-auswerten" oder "PEM-BIS-auswerten" gesetzt ist.
                if (iPartsPLE.getFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM) || iPartsPLE.getFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO)) {
                    iPartsFactoryData factoryDataForRetail = iPartsPLE.getFactoryDataForRetailUnfiltered();
                    if ((factoryDataForRetail != null) && factoryDataForRetail.hasValidFactories()) {

                        // Echtes PEM-AB und echtes PEM-BIS vorhanden?
                        // Die Prüfung wird nur für Werksdaten durchgeführt, bei denen PEM-AB ungleich PEM-BIS ist.
                        factoryDataForRetail.getFactoryDataMap().entrySet().forEach((factoryData) -> {
                            List<iPartsFactoryData.DataForFactory> factoryDataList = factoryData.getValue();

                            boolean validFactoryDataFound = false;
                            for (iPartsFactoryData.DataForFactory dataForFactory : factoryDataList) {
                                if (dataForFactory.dateFrom != dataForFactory.dateTo) { // PEM-AB und PEM-BIS müssen ungleich sein
                                    validFactoryDataFound = true;
                                    break;
                                }
                            }

                            if (validFactoryDataFound) {
                                String factoryNumber = factoryData.getKey();
                                if (addFactories.contains(factoryNumber)) { // Werkseinsatzdaten für Zusatzwerk gefunden
                                    ValidationEntry validationEntry = new ValidationEntry(ValidationResult.ERROR, translate("!!Unzulässiges Zusatzwerk in Fahrzeug-Baureihe: %1",
                                                                                                                            factoryNumber));
                                    addFactoriesValidationEntriesMap.put(partListEntry.getAsId().getKLfdnr(), validationEntry);
                                }
                            }
                        });
                    }
                }
            }
        }

        return addFactoriesValidationEntriesMap;
    }

    @Override
    protected String getDisplayFieldConfigKey(String partListType) {
        iPartsModuleTypes moduleType = iPartsModuleTypes.getFromDBValue(partListType);
        if (moduleType.getDefaultDocumentationType().isPKWDocumentationType()) {
            return CONFIG_KEY_MODEL_EVALUATION_DIALOG;
        } else if (moduleType.getDefaultDocumentationType().isTruckDocumentationType()) {
            return CONFIG_KEY_MODEL_EVALUATION_EDS;
        }

        return null;
    }

    private String getInvisiblePartOrZZPartMessage(EtkDataPartListEntry partListEntry) {
        DwList<String> reasons = new DwList<>();
        if ((omittedParts != null) && omittedParts.isOmittedPart(partListEntry)) {
            reasons.add(translate("!!eine Wegfallsachnummer"));
        }
        if (partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT)) {
            reasons.add(translate("!!eine Entfallposition"));
        }
        if (partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_ONLY_MODEL_FILTER)) {
            reasons.add(translate("!!nur für den Baumuster-Filter relevant"));
        }
//        if (Utils.objectEquals(partlistEntriesWithZZ.get(partListEntry.getAsId().getKLfdnr()), Boolean.TRUE)) {
//            reasons.add(translate("!!ein unechtes Wahlweise. Die Teilkonjunktionsprüfung wurde nicht durchgeführt."));
//        }
        if (reasons.isEmpty()) {
            return "";
        }
        StringBuilder message = new StringBuilder();
        message.append(translate("!!Stücklisteneintrag %1, %2 (laufende Nummer %3) ist",
                                 partListEntry.getPart().getDisplayValue(iPartsConst.FIELD_M_BESTNR,
                                                                         getProject().getDBLanguage()),
                                 partListEntry.getPart().getDisplayValue(iPartsConst.FIELD_M_TEXTNR,
                                                                         getProject().getDBLanguage()),
                                 partListEntry.getAsId().getKLfdnr()));
        message.append(" ");
        message.append(StrUtils.stringListToString(reasons, ", "));
        message.append(".");
        return message.toString();
    }

    private String getErrorLogMessage(iPartsDataPartListEntry errorEntry, iPartsDataPartListEntry compareEntry,
                                      String overlappingConjunction, String modelNumber, String existingText) {
        StringBuilder message = new StringBuilder();
        if (existingText != null) {
            message.append(existingText);
            message.append("\n\n");
        }
        message.append(translate("!!Doppelte Teilkonjunktion \"%1\" für Baumuster \"%2\" mit folgendem " +
                                 "Stücklisteneintrag gefunden: %3, %4 (laufende Nummer %5)",
                                 overlappingConjunction,
                                 modelNumber,
                                 compareEntry.getPart().getDisplayValue(iPartsConst.FIELD_M_BESTNR,
                                                                        getProject().getDBLanguage()),
                                 compareEntry.getPart().getDisplayValue(iPartsConst.FIELD_M_TEXTNR,
                                                                        getProject().getDBLanguage()),
                                 compareEntry.getAsId().getKLfdnr()));
        message.append("\n");
        message.append(translate("!!- Reduzierte Code vom ausgewählten Stücklisteneintrag: %1",
                                 errorEntry.getFieldValue(FIELD_K_CODES_REDUCED)));
        message.append("\n");
        message.append(translate("!!- Reduzierte Code vom Vergleichs-Stücklisteneintrag: %1",
                                 compareEntry.getFieldValue(FIELD_K_CODES_REDUCED)));

        message.append("\n");
        if (isSameCombinedText(errorEntry, compareEntry)) {
            message.append(translate("!!Ergänzungstext \"%1\" ist identisch", getCombinedText(errorEntry)));
        } else {
            message.append(translate("!!Ergänzungstexte unterscheiden sich:"));
            message.append("\n");
            message.append(translate("!!- Ergänzungstext vom ausgewählten Stücklisteneintrag: \"%1\"", getCombinedText(errorEntry)));
            message.append("\n");
            message.append(translate("!!- Ergänzungstext vom Vergleichs-Stücklisteneintrag: \"%1\"", getCombinedText(compareEntry)));
        }

        return message.toString();
    }

    private List<iPartsDataPartListEntry> getEntriesForResults(List<EtkDataPartListEntry> partlist, String selectedModel,
                                                               Set<ValidationResult> results) {
        List<iPartsDataPartListEntry> resultList = new DwList<>();
        boolean doCounting = results.contains(ValidationResult.ERROR) && results.contains(ValidationResult.WARNING);
        int currentIndex = 0;
        for (EtkDataPartListEntry partListEntry : partlist) {
            ValidationResult validationResult = getValidationEntry(partListEntry, selectedModel).validationResult;
            if ((validationResult != null) && results.contains(validationResult) && (partListEntry instanceof iPartsDataPartListEntry)) {
                resultList.add((iPartsDataPartListEntry)partListEntry);
                if (doCounting) {
                    if (firstIndexWithErrorOrWarning == -1) {
                        firstIndexWithErrorOrWarning = currentIndex;
                    } else {
                        firstIndexWithErrorOrWarning = Math.min(firstIndexWithErrorOrWarning, currentIndex);
                    }
                }
            }
            currentIndex++;
        }
        return resultList;
    }

    private List<iPartsDataPartListEntry> getEntriesForResult(List<EtkDataPartListEntry> partlist, String selectedModel,
                                                              ValidationResult result) {
        Set<ValidationResult> errorOrWarning = new HashSet<>();
        errorOrWarning.add(result);
        return getEntriesForResults(partlist, selectedModel, errorOrWarning);
    }

    private List<iPartsDataPartListEntry> getEntriesWithErrorOrWarning(List<EtkDataPartListEntry> partlist, String selectedModel) {
        Set<ValidationResult> errorOrWarning = new HashSet<>();
        errorOrWarning.add(ValidationResult.ERROR);
        errorOrWarning.add(ValidationResult.WARNING);
        return getEntriesForResults(partlist, selectedModel, errorOrWarning);
    }

    private List<iPartsDataPartListEntry> getCompareGroup(Map<String, Set<iPartsDataPartListEntry>> evaluationGroups,
                                                          EtkDataPartListEntry partListEntry, String model) {
        String key = createEvalKey(model, partListEntry);
        String steering = partListEntry.getFieldValue(FIELD_K_STEERING);
        Set<iPartsDataPartListEntry> compareGroup = new TreeSet<>(partListEntryCompareLfdNr);
        if (steering.isEmpty()) {
            compareGroup.addAll(evaluationGroups.get(key + SteeringIdentKeys.STEERING_LEFT));
            compareGroup.addAll(evaluationGroups.get(key + SteeringIdentKeys.STEERING_RIGHT));
        } else {
            compareGroup.addAll(evaluationGroups.get(key + steering));
        }

        Set<iPartsDataPartListEntry> minCompareGroup = new TreeSet<>(partListEntryCompareLfdNr);
        // Gleiche Stücklisteneinträge und Einträge mit gleichem Material aus der Vergleichsliste entfernen
        for (iPartsDataPartListEntry compareEntry : compareGroup) {
            if (!compareEntry.getAsId().getKLfdnr().equals(partListEntry.getAsId().getKLfdnr()) && !iPartsEditValidationHelper.isSameMatNr(partListEntry,
                                                                                                                                           compareEntry)) {
                minCompareGroup.add(compareEntry);
            }
        }

        return new DwList<>(minCompareGroup);
    }

    private void addToEvalGroup(Map<String, Set<iPartsDataPartListEntry>> evaluationGroups, EtkDataPartListEntry partListEntry,
                                String key, String steering) {
        String subKey = key + steering;
        Set<iPartsDataPartListEntry> group = evaluationGroups.get(subKey);
        if (group == null) {
            group = new TreeSet<>(partListEntryCompareLfdNr);
            evaluationGroups.put(subKey, group);
        }
        group.add((iPartsDataPartListEntry)partListEntry);
    }

    private String createEvalKey(String selectedModel, EtkDataPartListEntry partListEntry) {
        StringBuilder sb = new StringBuilder();
        sb.append(selectedModel);
        sb.append(K_SOURCE_CONTEXT_DELIMITER);
        sb.append(partListEntry.getFieldValue(FIELD_K_POS));
        return sb.toString();
    }

    private String createResultKey(EtkDataPartListEntry partListEntry, String model) {
        StringBuilder sb = new StringBuilder();
        sb.append(partListEntry.getAsId().getKLfdnr());
        sb.append(K_SOURCE_CONTEXT_DELIMITER);
        sb.append(model);
        return sb.toString();
    }

    private ValidationEntry setResult(EtkDataPartListEntry partListEntry, EtkDataPartListEntry otherPartListEntry, String model, ValidationResult validationResult) {
        ValidationEntry validationEntry = getValidationEntry(partListEntry, model);
        if (validationEntry.setValidationResult(validationResult)) {
            if ((validationResult == ValidationResult.ERROR) || (validationResult == ValidationResult.WARNING)) {
                if (otherPartListEntry != null) {
                    validationEntry.addOtherEntry(otherPartListEntry);
                }
            }
        }
        return validationEntry;
    }

    private ValidationEntry setResult(EtkDataPartListEntry partListEntry, String model, ValidationResult validationResult) {
        return setResult(partListEntry, null, model, validationResult);
    }

    private ValidationResult getResult(EtkDataPartListEntry partListEntry, String model) {
        ValidationResult validationResult = getValidationEntry(partListEntry, model).validationResult;
        if (validationResult != null) {
            return validationResult;
        }
        return ValidationResult.UNCHECKED;
    }

    private void addGeneralValidationResult(EtkDataPartListEntry partListEntry, ValidationResult validationResult, String validationReason) {
        ValidationEntry validationEntry = setResult(partListEntry, GENERAL_REASON, validationResult);
        if (StrUtils.isValid(validationReason)) {
            validationReason = translate(validationReason);
            if (StrUtils.isValid(validationEntry.filterReason)) {
                validationEntry.filterReason += '\n' + validationReason;
            } else {
                validationEntry.filterReason = validationReason;
            }
        }
    }

    public ValidationEntry getGeneralValidationEntry(EtkDataPartListEntry partListEntry) {
        return getValidationEntry(partListEntry, GENERAL_REASON);
    }

    private String getCombinedText(iPartsDataPartListEntry partListEntry) {
        return partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, Language.DE.getCode(), false);
    }

    private boolean isSameCombinedText(iPartsDataPartListEntry currentEntry, iPartsDataPartListEntry compareEntry) {
        String currentEntryText = getCombinedText(currentEntry);
        String compareEntryText = getCombinedText(compareEntry);
        return Utils.objectEquals(currentEntryText, compareEntryText);
    }

    private boolean checkPartlistEntries(iPartsDataPartListEntry partListEntry1, iPartsDataPartListEntry partListEntry2) {
        // Wahlweise
        if (iPartsEditValidationHelper.isWWPart(partListEntry1, partListEntry2)) {
            return true;
        }

        // Ereignisketten (schneller zu berechnen als Ersetzungsketten -> deswegen zuerst überprüfen)
        if ((series != null) && series.isEventTriggered()) { // nur bei Ereignis-gesteuerten Baureihen
            if (!iPartsEditValidationHelper.isEventChainOverlap(series, partListEntry1, partListEntry2)) {
                return true;
            }
        }

        // Ersetzungsketten
        if (iPartsFilterHelper.replacementChainContainsPartListEntry(partListEntry1, partListEntry2)) {
            return true;
        }

        // Werkseinsatzdaten
        if (!iPartsEditValidationHelper.isFactoryDataOverlap(partListEntry1, partListEntry2)) {
            return true;
        }

        // Teilkonjunktionensüberlappung der reduzierten Code-Regeln prüfen
        runTimeLoggers.setStartTime(TIMER_TYPES.TIMER_PARTIAL_CONJUNCTION_OVERLAP_CHECK);
        boolean result = !iPartsEditValidationHelper.isPartialConjunctionOverlap(partListEntry1.getFieldValue(FIELD_K_CODES_REDUCED),
                                                                                 partListEntry2.getFieldValue(FIELD_K_CODES_REDUCED));
        runTimeLoggers.stopTimeAndStore(TIMER_TYPES.TIMER_PARTIAL_CONJUNCTION_OVERLAP_CHECK);
        if (result) {
            return true;
        }

        return false;
    }

//    /**
//     * Unechte Wahlweise-Teile sollen nicht zur Qualitätsprüfung verwendet werden.
//     * Unecht bedeutet Teilepos hat kein WW-Set, aber einen WW-Kenner im BCTE Schlüssel und in {@code DA_DIALOG.DD_GES}
//     * steht ein {@code ZZ} an dritter und vierter Stelle.
//     *
//     * @param entry
//     * @return
//     */
//    private boolean isFakeWW(EtkDataPartListEntry entry) {
//        if (entry != null) {
//            iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(entry);
//            // Nur DA_DIALOG.DD_GES nachladen, falls ein WW-Kenner im BCTE Schlüssel vorhanden ist, aber kein WW SET (K_WW ist leer)
//            if (bcteKey != null) {
//                if (entry.getFieldValue(iPartsConst.FIELD_K_WW).isEmpty() && !bcteKey.ww.isEmpty()) {
//                    String kLfdnr = entry.getAsId().getKLfdnr();
//                    // Und es auch noch nicht geladen wurde
//                    Boolean isFakeWW = partlistEntriesWithZZ.get(kLfdnr);
//                    if (isFakeWW == null) {
//                        iPartsDialogId dialogId = new iPartsDialogId(entry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID));
//                        iPartsDataDialogData constrPartListEntry = new iPartsDataDialogData(getProject(), dialogId);
//                        if (constrPartListEntry.existsInDB()) {
//                            String ddGes = constrPartListEntry.getFieldValue(iPartsConst.FIELD_DD_GES);
//                            if (iPartsWWPartsHelper.isZZ(ddGes)) {
//                                partlistEntriesWithZZ.put(kLfdnr, true);
//                                return true;
//                            } else {
//                                partlistEntriesWithZZ.put(kLfdnr, false);
//                                return false;
//                            }
//                        }
//                    } else {
//                        return isFakeWW;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    @Override
    public String createVirtualFieldNameForModelOrFINEvaluation(String modelNumber) {
        return VirtualFieldsUtils.addVirtualFieldMask(iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION
                                                      + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER
                                                      + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_CHECK_OVERLAP
                                                      + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_SPACER
                                                      + modelNumber);
    }

    @Override
    protected String getAdditionalInfoText(EtkDataPartListEntry partListEntry, String model) {
        ValidationEntry validationEntry = getValidationEntry(partListEntry, model);
        String text = validationEntry.filterReason;
        if (StrUtils.isValid(text)) {
            if (Constants.DEVELOPMENT) {
                text += "\nDEV: Vergleichsgruppe für diesen Stücklisteneintrag: [" + validationEntry.compareGroupOutput + "]";
            }
        } else {
            text = "";
        }

        // Baumuster-unabhängigen Validierungstext hinzufügen unter den Baumuster-abhängigen Text
        validationEntry = getGeneralValidationEntry(partListEntry);
        if (validationEntry != null) {
            String generalReason = validationEntry.filterReason;
            if (StrUtils.isValid(generalReason)) {
                if (!text.isEmpty()) {
                    text += "\n\n";
                }
                text += generalReason;
            }
        }

        return text;
    }

    private ValidationEntry getValidationEntry(EtkDataPartListEntry partListEntry, String model) {
        String key = createResultKey(partListEntry, model);
        ValidationEntry validationEntry = validationEntries.get(key);
        if (validationEntry == null) {
            validationEntry = new ValidationEntry();
            validationEntries.put(key, validationEntry);
        }
        return validationEntry;
    }

    /**
     * Liefert das Gesamtergebnis aller Qualitätsprüfungen zurück, wobei {@link ValidationResult#ERROR} eine {@link ValidationResult#WARNING}
     * übertrumpft.
     *
     * @return
     */
    public ValidationResult getTotalValidationResult() {
        ValidationResult pictureAndTuValidationResult = validationPictureAndTUForm.getTotalValidationResult();
        ValidationResult colorAndPartListValidationResult = getColorAndPartlistTableValidationResult();
        ValidationResult finValidationResult = finValidationForm.getTotalValidationResult();
        if ((colorAndPartListValidationResult == ValidationResult.ERROR) || (pictureAndTuValidationResult == ValidationResult.ERROR)
            || (finValidationResult == ValidationResult.ERROR)) {
            return ValidationResult.ERROR;
        }

        if ((colorAndPartListValidationResult == ValidationResult.WARNING) || (pictureAndTuValidationResult == ValidationResult.WARNING)
            || (finValidationResult == ValidationResult.WARNING)) {
            return ValidationResult.WARNING;
        }

        return ValidationResult.OK;
    }

    /**
     * Liefert das Ergebnis der Qualitätsprüfungen für Farbvarianten (falls erlaubt) und Stücklisteneinträge zurück, wobei {@link ValidationResult#ERROR}
     * eine {@link ValidationResult#WARNING} übertrumpft.
     *
     * @return
     */
    public ValidationResult getColorAndPartlistTableValidationResult() {
        ValidationResult result = ValidationResult.OK;
        iPartsDataAssembly assembly = (iPartsDataAssembly)getCurrentAssembly();
        if (isValidationForColorsPossible(assembly)) {
            for (ValidationResult currentColorTableResult : this.colorTableValidationResult.values()) {
                result = mergeTotalValidationResult(result, currentColorTableResult);
            }


            if (result == ValidationResult.ERROR) {
                return result;
            }
        }

        for (ValidationEntry validationEntry : validationEntries.values()) {
            result = mergeTotalValidationResult(result, validationEntry.validationResult);
        }

        return result;
    }

    public void resetMarkFirstErrorOrWarning() {
        setSelectedIndex(-1, false);
        setAdditionalInfoText("");
        validationPictureAndTUForm.resetMarkFirstErrorOrWarning();
    }

    public void markFirstErrorOrWarning() {
        if (getSelectedIndex() == -1) {
            // nur wenn der Benutzer noch keine eigene Selektion hat, den ersten Fehler markieren
            if (firstIndexWithErrorOrWarning != -1) {
                setSelectedIndex(firstIndexWithErrorOrWarning, true);

                // Zusatztext aktualisieren
                if (validationContent instanceof IPartsRelatedInfoAssemblyListForm) {
                    IPartsRelatedInfoAssemblyListForm relatedInfoAssemblyListForm = (IPartsRelatedInfoAssemblyListForm)validationContent;
                    if (relatedInfoAssemblyListForm.getPartListTable() != null) {
                        GuiTableRow row = relatedInfoAssemblyListForm.getPartListTable().getRow(firstIndexWithErrorOrWarning);
                        if (row != null) {
                            handleCellClick(row, 0);
                        }
                    }
                }
            }
        }
        validationPictureAndTUForm.markFirstErrorOrWarning();
    }

    @Override
    public void afterUpdateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.afterUpdateData(sender, forceUpdateAll);
        markFirstErrorOrWarning();
    }

    public void setTabTitle(GuiTabbedPaneEntry tabbedPaneEntry, ValidationResult resultToShow) {
        // In den Tabs soll zu sehen sein, ob es mindestens einen Fehler gibt und wenn dem nicht so ist, ob es mindestens einen Hinweis gibt
        String title = "";
        if (tabbedPaneEntry == partlistAndColor) {
            title = PARTLIST_AND_COLOR_TAB_TITLE;
        } else if (tabbedPaneEntry == pictureAndTU) {
            title = PICTURE_AND_TU_TAB_TITLE;
        } else if (tabbedPaneEntry == finValidation) {
            title = FIN_VALIDATION_TITLE;
        }

        if (resultToShow == ValidationResult.ERROR) {
            title = translate(title) + TAB_ERROR_SUFFIX;
        } else if (resultToShow == ValidationResult.WARNING) {
            title = translate(title) + TAB_WARNING_SUFFIX;
        }
        tabbedPaneEntry.setTitle(title);
    }

    private enum TotalQualityCheckResult {
        OK,
        WARNING,
        ERROR
    }

    private TotalQualityCheckResult convertFromValidationResult(ValidationResult input) {
        if (input == ValidationResult.ERROR) {
            return TotalQualityCheckResult.ERROR;
        } else if (input == ValidationResult.WARNING) {
            return TotalQualityCheckResult.WARNING;
        } else {
            return TotalQualityCheckResult.OK;
        }
    }

    private ValidationResult mergeTotalValidationResult(ValidationResult existingResult, ValidationResult input) {
        if (input == null) { // Kann z.B. beim generischen Baumuster-unabhängigen Ergebnis passieren
            return existingResult;
        }

        if (existingResult == ValidationResult.WARNING) {
            if (input == ValidationResult.ERROR) {
                return ValidationResult.ERROR;
            }
        } else if (existingResult == ValidationResult.OK) {
            if ((input != ValidationResult.MODEL_INVALID) && (input != ValidationResult.INVISIBLE_PART_VALID)) {
                return input;
            }
        }
        return existingResult;
    }

    @Override
    public void setExternalMessageLog(EtkMessageLog messageLog) {
        super.setExternalMessageLog(messageLog);
        if (finValidationForm != null) {
            finValidationForm.setExternalMessageLog(messageLog);
        }
    }

    /**
     * Daten-Klasse für alle Validierungs-Werte pro Stücklisteneintrag
     */
    public static class ValidationEntry {

        String compareGroupOutput = "";

        public ValidationResult getValidationResult() {
            return validationResult;
        }

        public String getFilterReason() {
            return filterReason;
        }

        ValidationResult validationResult;
        String filterReason;
        Set<String> otherEntries;

        public ValidationEntry() {
        }

        public ValidationEntry(ValidationResult validationResult, String filterReason) {
            this.validationResult = validationResult;
            this.filterReason = filterReason;
        }

        boolean setValidationResult(ValidationResult result) {
            if (result == ValidationResult.OK) {
                // Ein OK darf weder Fehler noch Warnung überschreiben
                if ((validationResult == ValidationResult.ERROR) ||
                    (validationResult == ValidationResult.WARNING)) {
                    return false;
                }
            } else if (result == ValidationResult.WARNING) {
                // Warnung darf OK überschreiben, aber Fehler nicht
                if (validationResult == ValidationResult.ERROR) {
                    return false;
                }
            }
            this.validationResult = result;
            return true;
        }

        void addOtherEntry(EtkDataPartListEntry otherPartListEntry) {
            if (otherEntries == null) {
                otherEntries = new HashSet<>();
            }
            otherEntries.add(otherPartListEntry.getAsId().getKLfdnr());
        }
    }
}
