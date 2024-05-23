/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.mechanic.AssemblyFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.timeslice.iPartsFilterTimeSliceHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWWPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsColorFactoryDataForRetail;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsColorTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsEditBaseValidationForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.iPartsEditAssemblyListValidationOverlappingEntriesForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.misc.booleanfunctionparser.BooleanFunction;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class iPartsEditValidationHelper implements iPartsConst {

    public enum ColortableQualityCheck {
        CHECK0, // Teil ist farbig aber keine Farbtabelle zur Baureihe der Teileposition vorhanden
        CHECK1, // Farbtabelle ist zeitlich zu keinem Baumuster gültig
        CHECK2,  // Alle Farbvarianten einer Farbtabelle sind zu keinem Baumuster gültig
        CHECK3, // Überlappung bei gleichem ES2
        CHECK4, // Überlappung bei unterschiedlichem ES2
        CHECK_PEM_FLAGS, // Variantentabellen sowohl mit "Auswertung PEM-ab/bis" als auch ohne
        CHECK_PEM_FROM, // Werkseinsatzdaten mit gültiger PEM-ab vorhanden bei gesetztem Flag "PEM-ab auswerten"?
        CHECK_PEM_TO // Werkseinsatzdaten mit gültiger PEM-bis vorhanden bei gesetztem Flag "PEM-bis auswerten"?
    }

    public static class ColortableQualityCheckResult {

        private Set<ColortableQualityCheck> failedQualityChecks;
        private iPartsEditBaseValidationForm.ValidationResult totalResult;

        public ColortableQualityCheckResult() {
            this.failedQualityChecks = new LinkedHashSet<>();
            this.totalResult = iPartsEditBaseValidationForm.ValidationResult.OK;
        }

        /**
         * Fügt die übergebene Prüfung zur Liste der fehlgeschlagenen Prüfungen hinzu und speichert sich das maximale Ergebnis.
         * D.h. wenn ein ERROR übergeben wird, ist das Gesamtergebnis ERROR, wenn ein WARNING übergeben wird, und zuvor
         * bereits ein ERROR gespeichert war, bleibt es ein ERROR. Ein gespeichertes OK wird von WARNING und ERROR überschrieben.
         * MODEL_INVALID und INVISIBLE_PART_VALID werden dabei wie OK behandelt
         *
         * @param failedCheck
         * @param checkResult
         */
        public void addFailedCheck(ColortableQualityCheck failedCheck, iPartsEditBaseValidationForm.ValidationResult checkResult) {
            failedQualityChecks.add(failedCheck);
            if (totalResult == iPartsEditBaseValidationForm.ValidationResult.WARNING) {
                if (checkResult == iPartsEditBaseValidationForm.ValidationResult.ERROR) {
                    totalResult = iPartsEditBaseValidationForm.ValidationResult.ERROR;
                }
            } else if (totalResult == iPartsEditBaseValidationForm.ValidationResult.OK) {
                if ((checkResult != iPartsEditBaseValidationForm.ValidationResult.MODEL_INVALID) && (checkResult != iPartsEditBaseValidationForm.ValidationResult.INVISIBLE_PART_VALID)) {
                    totalResult = checkResult;
                }
            }
        }

        public Set<String> getFailedQualityChecksAsStrings() {
            Set<String> result = new LinkedHashSet<>();
            for (ColortableQualityCheck failedQualityCheck : failedQualityChecks) {
                result.add(failedQualityCheck.name());
            }
            return result;
        }
    }

    private static ObjectInstanceLRUList<String, Boolean> partialConjunctionOverlapCache =
            new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_CODES, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private static final String CACHE_KEY_DELIMITER = "||";

    public static final String IPARTS_MENU_ITEM_VALIDATE_ASSEMBLY = "iPartsMenuItemValidateAssembly";
    public static final String IPARTS_MENU_ITEM_VALIDATE_PRODUCT = "iPartsMenuItemValidateProduct";
    public static final String IPARTS_MENU_ITEM_VALIDATE_PRODUCT_TEXT = "!!TUs im Produkt überprüfen";
    private static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES_FOR_VALIDATE_ASSEMBLY = iPartsModuleTypes.getEditableModuleTypes();
    private static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES_FOR_VALIDATE_PRODUCT = EnumSet.of(iPartsModuleTypes.PRODUCT_KGTU);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_VALIDATE_ASSEMBLY,
                                                                                           EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getTooltip(),
                                                                                           EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getImage(),
                                                                                           null);
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                validateAssembly(connector, connector.getActiveForm());
            }
        });
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        EtkDataAssembly destAssembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector(connector);
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_VALIDATE_ASSEMBLY,
                                                                    AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly,
                                                                                                                             VALID_MODULE_TYPES_FOR_VALIDATE_ASSEMBLY));
    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractAssemblyTreeForm formWithTree) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_VALIDATE_ASSEMBLY,
                                                                                       EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getTooltip(),
                                                                                       null);
        if (menuItem != null) {
            menuItem.setIcon(EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getImage());
            menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    validateAssembly(formWithTree.getConnector(), formWithTree);
                }
            });
        }

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_VALIDATE_PRODUCT,
                                                                           IPARTS_MENU_ITEM_VALIDATE_PRODUCT_TEXT,
                                                                           null);
        if (menuItem != null) {
            menuItem.setIcon(EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getImage());
            addValidateProductListener(menuItem, formWithTree.getConnector());
        }
    }

    private static void addValidateProductListener(final GuiMenuItem menuItem, final AssemblyFormIConnector connector) {
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                final Session sessionForGUI = Session.get();
                final EtkProject projectForReport;

                // EtkEndpointHelper.createProject(Session) kann nicht verwendet werden, weil das temporäre EtkProject nicht
                // als EtkProject in der sessionForGUI gesetzt werden soll
                DWFile dwkFile = (DWFile)sessionForGUI.getAttribute(JavaViewerApplication.SESSION_KEY_DWK_FILE);
                projectForReport = EtkEndpointHelper.createProject(dwkFile, true);
                if (projectForReport == null) {
                    MessageDialog.showError("!!Fehler bei der Initialisierung für die Überprüfung im Hintergrund.");
                    return;
                }

                projectForReport.getConfig().setCurrentViewerLanguage(connector.getProject().getViewerLanguage());
                projectForReport.getConfig().setCurrentDatabaseLanguage(connector.getProject().getDBLanguage());

                sessionForGUI.startChildThread(thread -> {
                    try {
                        // DAIMLER-14758: Businesslogik erstellen
                        EditValidationProductEntriesWorker worker = new EditValidationProductEntriesWorker();
                        worker.startValidation(connector, projectForReport, connector.getCurrentAssembly().getAsId());
                        if (sessionForGUI.isActive() && !Thread.currentThread().isInterrupted()) {
                            sessionForGUI.invokeThreadSafe(new Runnable() {
                                @Override
                                public void run() {
                                    String msg = "!!Download komplett.";
                                    if (!worker.downloadFile()) {
                                        msg = "!!Keine Download-Datei vorhanden!";
                                        String extraNote = worker.getExtraNotes();
                                        if (StrUtils.isValid(extraNote)) {
                                            msg = extraNote;
                                        }
                                        MessageDialog.showError(msg, IPARTS_MENU_ITEM_VALIDATE_PRODUCT_TEXT);
                                    } else {
                                        String extraNote = worker.getExtraNotes();
                                        if (StrUtils.isValid(extraNote)) {
                                            msg += "\n" + extraNote;
                                        }
                                        MessageDialog.show(msg, IPARTS_MENU_ITEM_VALIDATE_PRODUCT_TEXT);
                                    }
                                }
                            });
                        }
                    } finally {
                        projectForReport.setDBActive(false, false);
                    }
                });
                MessageDialog.show("!!Die Überprüfung wurde im Hintergrund gestartet.", IPARTS_MENU_ITEM_VALIDATE_PRODUCT_TEXT);
            }
        });
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_VALIDATE_ASSEMBLY,
                                                                 AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(connector.getCurrentAssembly(),
                                                                                                                          VALID_MODULE_TYPES_FOR_VALIDATE_ASSEMBLY));
        if (!iPartsRight.REPORT_TU_VALIDATION_FOR_PRODUKT.checkRightInSession()) {
            AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_VALIDATE_PRODUCT, false);
        } else {
            AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_VALIDATE_PRODUCT,
                                                                     AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(connector.getCurrentAssembly(),
                                                                                                                              VALID_MODULE_TYPES_FOR_VALIDATE_PRODUCT));
        }
    }

    /**
     * Überprüft das im übergebenen {@link AbstractJavaViewerFormIConnector} aktuell ausgewählte Modul z.B. bzgl. überlappenden
     * Teilkonjunktionen zwischen Stücklisteneinträgen.
     *
     * @param connector
     * @param parentForm
     */
    public static void validateAssembly(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm) {
        EtkDataAssembly assembly = EditModuleHelper.getAssemblyFromConnector(connector);
        if (assembly != null) {
            assembly = assembly.getLastHiddenSingleSubAssemblyOrThis(assembly.getEbene());
            if (assembly instanceof iPartsDataAssembly) {
                boolean isEdit = false;
                if (parentForm.isRevisionChangeSetActiveForEdit()) {
                    if (!iPartsEditPlugin.startEditing()) {
                        return;
                    }
                    isEdit = true;
                }
                try {
                    EditModuleFormIConnector editConnectorForValidation;
                    if (connector instanceof EditModuleFormIConnector) {
                        editConnectorForValidation = ((EditModuleFormIConnector)connector).cloneMe(false);
                    } else {
                        editConnectorForValidation = new EditModuleFormConnector(connector);
                        editConnectorForValidation.setCurrentAssembly(assembly);
                    }
                    iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm = new iPartsEditAssemblyListValidationOverlappingEntriesForm(editConnectorForValidation,
                                                                                                                                                       parentForm);
                    validationForm.addOwnConnector(editConnectorForValidation);
                    validationForm.showModal();
                    return;
                } finally {
                    if (isEdit) {
                        iPartsEditPlugin.stopEditing();
                    }
                }
            }
        }

        MessageDialog.show("!!Es ist kein technischer Umfang für die Überprüfung ausgewählt.", EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY.getText());
    }

    /**
     * Führt die Freigabevorprüfung bzw. die Freigabeprüfung für einen Autorenauftrag mit Multi-Threading durch.
     * Dazu werden pro geändertem Modul die Qualitätsprüfung für dieses Modul inkl. der Qualitätsprüfung für Farben aufgerufen
     * und die Ergebnisse zusammengetragen.
     * Pro Modul werden 2 Threads gestartet (einer für die Prüfungen für Stückliste/Farben und einer für Bild/TU).
     *
     * @param selectedAuthorOrder
     * @param connector
     * @param isFinalReleaseCheck
     * @return Eine Map, die pro Modul das Gesamtergebnis der Qualitätsprüfungen enthält; {@code null} bei Abbruch
     */
    public static Map<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> executePreReleaseCheckMulti(iPartsDataAuthorOrder selectedAuthorOrder,
                                                                                                                      AbstractJavaViewerFormIConnector connector,
                                                                                                                      boolean isFinalReleaseCheck) {
        String title = isFinalReleaseCheck ? "!!Freigabeprüfung" : "!!Freigabevorprüfung";
        MessageLogFormForCheck messageLogForm = new MessageLogFormForCheck("!!Qualitätsprüfungen", title, connector);
        ModalResult modalResult = messageLogForm.doShowModal(selectedAuthorOrder);
        if (modalResult == ModalResult.OK) {
            return messageLogForm.getAssemblyValidationResult();
        } else {
            // zuvor erzeugte Forms wieder aufräumen
            for (iPartsEditAssemblyListValidationOverlappingEntriesForm value : messageLogForm.getAssemblyValidationResult().values()) {
                value.dispose();
            }

            if (modalResult == ModalResult.ABORT) {
                MessageDialog.show(TranslationHandler.translate("!!Der Autoren-Auftrag \"%1\" ist leer.", selectedAuthorOrder.getAuthorOrderName()),
                                   title);
            }

            return null;
        }

    }

    /**
     * Private Klasse, in der die Überprüfung der TUs eines Autorenauftrags vorgenommen wird.
     * Die Überprüfung kann dabei sequentiell oder parallel (2 Threads pro Modul) ausgeführt werden.
     */
    private static class MessageLogFormForCheck extends EtkMessageLogForm {

        public AbstractJavaViewerFormIConnector connector;
        private Map<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> assemblyValidationResult;
        private VarParam<FrameworkThread> preReleaseThread;
        private VarParam<Boolean> isCancelled;
        private boolean cancelStarted;
        private int maxProgress;
        private int currentProgress;
        private boolean isQualityCheckParallelActive;
        private ExecutorService executorService;
        private int threadCount;

        public MessageLogFormForCheck(String windowTitle, String title, AbstractJavaViewerFormIConnector connector) {
            super(windowTitle, title, null);
            setAutoClose(false);
            this.connector = connector;
            this.isCancelled = new VarParam<>(false);
            this.preReleaseThread = new VarParam<>();
            this.assemblyValidationResult = new TreeMap<>(new Comparator<AssemblyId>() {
                @Override
                public int compare(AssemblyId o1, AssemblyId o2) {
                    return o1.getKVari().compareTo(o2.getKVari());
                }
            });
        }

        @Override
        protected void cancel(Event event) {
            isCancelled.setValue(true);
            if (checkIsCanceled()) {
                FrameworkThread preReleaseThreadLocal = preReleaseThread.getValue();
                if (preReleaseThreadLocal != null) {
                    preReleaseThreadLocal.cancel(true);
                }
            }
        }

        public EtkProject getProject() {
            return connector.getProject();
        }

        /**
         * Getter für die Ergebnisse
         *
         * @return
         */
        public Map<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> getAssemblyValidationResult() {
            return assemblyValidationResult;
        }

        /**
         * "Überlagert" showModal(), da vorher noch einiges getan werden muss
         *
         * @param selectedAuthorOrder // der zu prüfende Autoren-Auftrag
         * @return
         */
        public ModalResult doShowModal(iPartsDataAuthorOrder selectedAuthorOrder) {
            isQualityCheckParallelActive = iPartsEditPlugin.isQualityCheckParallelActive();
            FrameworkRunnable runnable = createMainRunnableForCheck(selectedAuthorOrder);
            return showModal(runnable);
        }

        /**
         * Hauptroutine für die Prüfung
         *
         * @param selectedAuthorOrder
         * @return
         */
        private FrameworkRunnable createMainRunnableForCheck(iPartsDataAuthorOrder selectedAuthorOrder) {
            return new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    // alle geänderten Module in diesem Autoren-Auftrag bestimmen
                    if (selectedAuthorOrder != null) {
                        preReleaseThread.setValue(thread);
                        iPartsRevisionChangeSet changeSet = new iPartsRevisionChangeSet(selectedAuthorOrder.getChangeSetId(),
                                                                                        getProject());

                        // ChangeSet darf nicht leer sein
                        if (changeSet.isEmpty()) {
                            fireMsg("!!Der Autoren-Auftrag \"%1\" ist leer.", selectedAuthorOrder.getAuthorOrderName());
                            closeWindow(ModalResult.ABORT);
                            return;
                        }

                        // ggf Multi-Threading vorbereiten
                        handleMultiThreading();
                        // alle relevanten Module bestimmen
                        Set<AssemblyId> changedModuleIds = changeSet.getModuleIdsWithStateAnyOf(SerializedDBDataObjectState.NEW,
                                                                                                SerializedDBDataObjectState.REPLACED,
                                                                                                SerializedDBDataObjectState.MODIFIED);
                        // Progress vorbereiten
                        setProgress(changedModuleIds.size());
                        fireMsg("!!Starte Prüfung von %1 TUs", String.valueOf(maxProgress));
                        RunTimeLogger runTimeLogger = new RunTimeLogger(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK, true);
                        getProject().startPseudoTransactionForActiveChangeSet(true);
                        try {
                            // Überprüfung vornehmen
                            if (startModulesCheck(changedModuleIds)) {
                                // ggf Warten auf gestartete Threads
                                waitForThreadsEnded();
                                addFinishLogMsg(runTimeLogger);
                            }
                        } finally {
                            getProject().stopPseudoTransactionForActiveChangeSet();
                        }
                        if (!isCancelled.getValue()) {
                            closeWindow(ModalResult.OK);
                        }
                    } else {
                        closeWindow(ModalResult.ABORT);
                    }
                }
            };
        }

        /**
         * Einstellungen aus der Konfig abfragen und ggf Multi-Threading vorbereiten
         */
        private void handleMultiThreading() {
            threadCount = 1;
            if (isQualityCheckParallelActive) {
                threadCount = iPartsEditPlugin.getQualityCheckThreadCount();
                if (threadCount == 1) {
                    isQualityCheckParallelActive = false;
                }
            }
            if (isQualityCheckParallelActive) {
                fireMsg("!!Bearbeitung mit %1 Threads", String.valueOf(threadCount));
                executorService = Executors.newFixedThreadPool(threadCount);
            } else {
                fireMsg("!!Sequenzielle Bearbeitung");
            }
        }

        /**
         * Überprüfung für alle Module vornehmen
         *
         * @param changedModuleIds
         */
        private boolean startModulesCheck(Set<AssemblyId> changedModuleIds) {
            Session session = Session.get();
            for (AssemblyId changedModuleId : changedModuleIds) {
                if (checkIsCanceled()) {
                    return false;
                }
                // Assembly aus der Id bestimmen
                iPartsDataAssembly iPartsAssembly = getAssemblyFromId(changedModuleId);
                if (iPartsAssembly != null) {
                    // das Runnable für einen TU
                    Runnable calculationRunnable = createRunnableForQualityCheckAssembly(iPartsAssembly);
                    if (isQualityCheckParallelActive) {
                        // mit Multi-Threading
                        executorService.execute(() -> {
                            if ((session != null) && session.isActive()) {
                                session.runInSession(calculationRunnable);
                            }
                        });
                    } else {
                        // ohne Multi-Threading
                        calculationRunnable.run();
                    }
                }
            }
            return true;
        }

        /**
         * Ggf Warten, bis alle Threads beendet sind
         * Ende-Meldung ausgeben
         */
        private void waitForThreadsEnded() {
            if (isCancelled.getValue()) {
                return;
            }
            if (isQualityCheckParallelActive) {
                ExecutorService executorServiceLocal = executorService;
                if (executorServiceLocal != null) {
                    // Alle gewünschten Überprüfungen wurden zum ExecutorService hinzugefügt -> Shutdown aufrufen und warten bis alle
                    // Tasks abgearbeitet wurden
                    executorServiceLocal.shutdown();
                    try {
                        executorServiceLocal.awaitTermination(1, TimeUnit.HOURS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        if (!isCancelled.getValue()) {
                            addLogMsg("Waiting for quality check threads to finish interrupted");
                        }
                    }
                    executorService = null;
                }
            }
        }

        /**
         * Runnable für die Überprüfung eines TUs anlegen
         *
         * @param iPartsAssembly
         * @return
         */
        private Runnable createRunnableForQualityCheckAssembly(iPartsDataAssembly iPartsAssembly) {
            return () -> {
                if (checkIsCanceled()) {
                    return;
                }
                String moduleNumber = iPartsAssembly.getAsId().getKVari();
                fireMsg("!!Starte Prüfung von TU \"%1\"...", moduleNumber);
                checkAssemblyAndAdd(iPartsAssembly, getMessageLog());
                fireMsg("!!Prüfung von TU \"%1\" beendet", moduleNumber);
                checkIsCanceled();
            };
        }

        /**
         * Der eigentliche Überprüfungsaufruf und Aufsammeln des Ergebnisses
         *
         * @param iPartsAssembly
         * @param messageLog
         */
        private void checkAssemblyAndAdd(iPartsDataAssembly iPartsAssembly, EtkMessageLog messageLog) {
            if (checkIsCanceled()) {
                return;
            }
            iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm =
                    iPartsEditAssemblyListValidationOverlappingEntriesForm.validateAssemblySilent(connector, iPartsAssembly, messageLog);
            addForm(iPartsAssembly, validationForm);
        }

        private iPartsDataAssembly getAssemblyFromId(AssemblyId changedModuleId) {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), changedModuleId);
            assembly = assembly.getLastHiddenSingleSubAssemblyOrThis(assembly.getEbene());
            if (assembly instanceof iPartsDataAssembly) {
                return (iPartsDataAssembly)assembly;
            }
            return null;
        }

        private boolean checkIsCanceled() {
            if (isCancelled.getValue()) {
                synchronized (isCancelled) {
                    if (cancelStarted) {
                        return true;
                    } else {
                        cancelStarted = true;
                    }
                }
                fireMsg("!!Prüfung abgebrochen");
                addLogMsg("Quality check cancelled by user");
                if (isQualityCheckParallelActive) {
                    ExecutorService executorServiceLocal = executorService;
                    if (executorServiceLocal != null) {
                        executorServiceLocal.shutdownNow();
                        Thread.currentThread().interrupt();
                        try {
                            executorServiceLocal.awaitTermination(1, TimeUnit.HOURS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        executorService = null;
                    }
                }
                closeWindow(ModalResult.CANCEL, true);
                return true;
            }
            return false;
        }

        /**
         * Ergebnis einer Prüfung aufsammeln und den Fortschritt weiterschalten
         *
         * @param iPartsAssembly
         * @param validationForm
         */
        private void addForm(iPartsDataAssembly iPartsAssembly, iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm) {
            synchronized (this) {
                assemblyValidationResult.put(iPartsAssembly.getAsId(), validationForm);
            }
            incProgress();
        }

        private void setProgress(int maxProgress) {
            this.maxProgress = maxProgress;
            this.currentProgress = 0;
            getMessageLog().fireProgress(0, maxProgress, "", false, false);
            addStartLogMsg();
        }

        private void incProgress() {
            currentProgress++;
            getMessageLog().fireProgress(currentProgress, maxProgress, "", false, false);
        }

        private void fireMsg(String key, String... placeHolderTexts) {
            getMessageLog().fireMessage(TranslationHandler.translate(key, placeHolderTexts), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }

        private void addStartLogMsg() {
            if (isQualityCheckParallelActive) {
                addLogMsg("Running " + threadCount + " parallel threads for quality check of " + maxProgress + " TUs");
            } else {
                addLogMsg("Running single thread for quality check of " + maxProgress + " TUs");
            }
        }

        private void addFinishLogMsg(RunTimeLogger runTimeLogger) {
            addLogMsg("Finished check for all " + maxProgress + " TUs in " + runTimeLogger.getDurationString());
        }

        private void addLogMsg(String msg) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK, LogType.DEBUG, msg);
        }
    }

    /**
     * Ermittelt das Gesamtergebnis für alle übergebenen {@link iPartsEditAssemblyListValidationOverlappingEntriesForm}s.
     * Sobald eines der Forms einen Fehler meldet, ist das Ergebnis Fehler. Falls es keinen Fehler aber Warnungen gibt,
     * ist das Ergebnis Warnung, und sonst OK.
     *
     * @param validationForms
     * @return {@link iPartsEditBaseValidationForm.ValidationResult#ERROR} oder {@link iPartsEditBaseValidationForm.ValidationResult#WARNING}
     * oder {@link iPartsEditBaseValidationForm.ValidationResult#OK}
     */
    public static iPartsEditBaseValidationForm.ValidationResult evaluatePreReleaseCheck(Map<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> validationForms) {
        iPartsEditBaseValidationForm.ValidationResult result = iPartsEditBaseValidationForm.ValidationResult.OK;
        for (iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm : validationForms.values()) {
            iPartsEditBaseValidationForm.ValidationResult validationResult = validationForm.getTotalValidationResult();
            if (validationResult == iPartsEditBaseValidationForm.ValidationResult.ERROR) {
                return iPartsEditBaseValidationForm.ValidationResult.ERROR;
            }
            if (validationResult == iPartsEditBaseValidationForm.ValidationResult.WARNING) {
                result = validationResult;
            }
        }
        return result;
    }

    /**
     * Überprüft, ob die beiden Stücklisteneinträge die gleiche Materialnummer haben.
     *
     * @param first
     * @param second
     * @return <code>true</code> wenn <code>first</code> und <code>second</code> die gleiche Materialnummer haben
     */
    public static boolean isSameMatNr(EtkDataPartListEntry first, EtkDataPartListEntry second) {
        if ((first != null) && (second != null)) {
            return first.getPart().getAsId().getMatNr().equals(second.getPart().getAsId().getMatNr());
        }
        return false;
    }

    /**
     * Überprüft, ob die beiden Stücklisteneinträge wahlweise zueinander sind.
     * Dazu werden nur die echten Wahlweise-Teile, die sich in der Stückliste befinden, berücksichtigt.
     *
     * @param first
     * @param second
     * @return <code>true</code> wenn <code>first</code> und <code>second</code> wahlweise zueinander sind
     */
    public static boolean isWWPart(iPartsDataPartListEntry first, iPartsDataPartListEntry second) {
        Collection<EtkDataPartListEntry> wwPartsFirst = iPartsWWPartsHelper.getRealWWParts(first, false);
        Collection<EtkDataPartListEntry> wwPartsSecond = iPartsWWPartsHelper.getRealWWParts(second, false);
        for (EtkDataPartListEntry wwFirst : wwPartsFirst) {
            if (wwFirst.getAsId().equals(second.getAsId())) {
                return true;
            }
        }
        for (EtkDataPartListEntry wwSecond : wwPartsSecond) {
            if (wwSecond.getAsId().equals(first.getAsId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Überprüft, ob die beiden übergebenen Stücklisteneinträge eine Überlappung in ihren Ereignisketten haben
     *
     * @param series Die DIALOG-Baureihe, um die Reihenfolge der Ereignisse zu bestimmen
     * @param first
     * @param second
     * @return <code>true</code> wenn <code>first</code> und <code>second</code> eine Überlappung in ihren
     * Ereignisketten haben
     */
    public static boolean isEventChainOverlap(iPartsDialogSeries series, iPartsDataPartListEntry first, iPartsDataPartListEntry second) {
        int event1From = series.getEventOrdinal(first.getFieldValue(iPartsConst.FIELD_K_EVENT_FROM));
        int event1To = series.getEventOrdinal(first.getFieldValue(iPartsConst.FIELD_K_EVENT_TO));
        int event2From = series.getEventOrdinal(second.getFieldValue(iPartsConst.FIELD_K_EVENT_FROM));
        int event2To = series.getEventOrdinal(second.getFieldValue(iPartsConst.FIELD_K_EVENT_TO));

        return isEventChainOverlap(event1From, event1To, event2From, event2To);
    }

    /**
     * Überprüft, ob die beiden übergebenen Farbvarianten eine Überlappung in ihren Ereignisketten haben
     *
     * @param series Die DIALOG-Baureihe, um die Reihenfolge der Ereignisse zu bestimmen
     * @param first
     * @param second
     * @return <code>true</code> wenn <code>first</code> und <code>second</code> eine Überlappung in ihren
     * Ereignisketten haben
     */
    public static boolean isEventChainOverlap(iPartsDialogSeries series, iPartsColorTable.ColorTableContent first,
                                              iPartsColorTable.ColorTableContent second) {
        int event1From = series.getEventOrdinal(first.getFilterEventFromId());
        int event1To = series.getEventOrdinal(first.getFilterEventToId());
        int event2From = series.getEventOrdinal(second.getFilterEventFromId());
        int event2To = series.getEventOrdinal(second.getFilterEventToId());

        return isEventChainOverlap(event1From, event1To, event2From, event2To);
    }

    /**
     * Überprüft, ob die beiden übergebenen Ereignisintervalle eine Überlappung in ihren Ereignisketten haben
     *
     * @param event1From
     * @param event1To
     * @param event2From
     * @param event2To
     * @return
     */
    private static boolean isEventChainOverlap(int event1From, int event1To, int event2From, int event2To) {
        // leere Ereignis-bis Werte sollen als +unendlich behandelt werden. Leere Ereignis-ab Werte sind bereits -1 = -unendlich
        if (event1To < 0) {
            event1To = Integer.MAX_VALUE;
        }
        if (event2To < 0) {
            event2To = Integer.MAX_VALUE;
        }

        return valueOverlap(event1From, event1To, event2From, event2To);
    }

    private static boolean valueOverlap(long entry1Start, long entry1End, long entry2Start, long entry2End) {
        if (((entry1Start >= entry2Start) && (entry1Start < entry2End)) ||
            (((entry1End <= entry2End) && (entry1End > entry2Start)))) {
            return true;
        }
        return false;
    }

    private static long getMinFactoryDate(List<? extends iPartsFactoryData.AbstractDataForFactory> dataForFactoryList,
                                          boolean evalPemFrom) {
        if (!evalPemFrom) {
            return 0; // -unendlich
        }

        if (dataForFactoryList == null) {
            // Überlappung ausschließen, weil Werk nicht vorhanden und aufgrund von isValidForEndNumberFilter dadurch
            // Ausfiltern im Endnummernfilter
            return iPartsFactoryData.INVALID_DATE;
        } else {
            boolean validFactoryDataFound = false;
            long minDate = Long.MAX_VALUE;
            for (iPartsFactoryData.AbstractDataForFactory dataForFactory : dataForFactoryList) {
                if (dataForFactory.dateFrom < minDate) {
                    validFactoryDataFound = true;
                    minDate = dataForFactory.dateFrom;
                }
            }
            if (validFactoryDataFound) {
                return minDate;
            } else {
                // Überlappung ausschließen, weil keine PEM ab für das Werk vorhanden und aufgrund von isValidForEndNumberFilter
                // dadurch Ausfiltern im Endnummernfilter
                return iPartsFactoryData.INVALID_DATE;
            }
        }
    }

    private static long getMaxFactoryDate(List<? extends iPartsFactoryData.AbstractDataForFactory> dataForFactoryList,
                                          boolean evalPemTo) {
        if (!evalPemTo) {
            return Long.MAX_VALUE; // unendlich
        }

        if (dataForFactoryList == null) {
            // Überlappung ausschließen, weil Werk nicht vorhanden und aufgrund von isValidForEndNumberFilter dadurch
            // Ausfiltern im Endnummernfilter
            return iPartsFactoryData.INVALID_DATE;
        } else {
            boolean validFactoryDataFound = false;
            long maxDate = 0;
            for (iPartsFactoryData.AbstractDataForFactory dataForFactory : dataForFactoryList) {
                long dateTo = dataForFactory.getDateToWithInfinity();
                if (dateTo > maxDate) {
                    validFactoryDataFound = true;
                    maxDate = dateTo;
                }
            }
            if (validFactoryDataFound) {
                return maxDate;
            } else {
                // Überlappung ausschließen, weil keine PEM bis für das Werk vorhanden und aufgrund von isValidForEndNumberFilter
                // dadurch Ausfiltern im Endnummernfilter
                return iPartsFactoryData.INVALID_DATE;
            }
        }
    }

    /**
     * Überprüft, ob die die Werkseinsatzdaten pro Werk der Stücklisteneinträge überlappen.
     * Dazu werden die Retail-Werkseinsatzdaten der Stücklisteneinträge herangezogen (wie sie auch zur Filterung
     * verwendet werden).
     *
     * @param first
     * @param second
     * @return <code>true</code> wenn <code>first</code> und <code>second</code> eine Überlappung in ihren Werkseinsatzdaten
     * haben
     */
    public static boolean isFactoryDataOverlap(iPartsDataPartListEntry first, iPartsDataPartListEntry second) {
        // Wenn einer der beiden Stücklisteneinträge keine relevanten Werkseinsatzdaten für den Endnummernfilter hat,
        // dann gibt es automatisch eine Überlappung
        if (!first.isValidFactoryDataRelevantForEndNumberFilter() || !second.isValidFactoryDataRelevantForEndNumberFilter()) {
            return true;
        }

        // Alle Werke für diese beiden Stücklisteneinträge bestimmen (beide Stücklisteneinträge müssen ab dieser Stelle
        // aufgrund obiger Prüfung relevant sein für den Endnummernfilter)
        Set<String> factoryNumbers = new HashSet<>();
        iPartsFactoryData firstFactoryDataForRetail = first.getFactoryDataForRetail();
        Map<String, List<iPartsFactoryData.DataForFactory>> factoryDataMap = firstFactoryDataForRetail.getFactoryDataMap();
        if (factoryDataMap != null) {
            factoryNumbers.addAll(factoryDataMap.keySet());
        }

        iPartsFactoryData secondFactoryDataForRetail = second.getFactoryDataForRetail();
        factoryDataMap = secondFactoryDataForRetail.getFactoryDataMap();
        if (factoryDataMap != null) {
            factoryNumbers.addAll(factoryDataMap.keySet());
        }

        for (String factoryNumber : factoryNumbers) {
            List<iPartsFactoryData.DataForFactory> firstDataForFactory = firstFactoryDataForRetail.getDataForFactory(factoryNumber);
            long startDateFirst = getMinFactoryDate(firstDataForFactory, firstFactoryDataForRetail.isEvalPemFrom());
            long endDateFirst = getMaxFactoryDate(firstDataForFactory, firstFactoryDataForRetail.isEvalPemTo());
            List<iPartsFactoryData.DataForFactory> secondDataForFactory = secondFactoryDataForRetail.getDataForFactory(factoryNumber);
            long startDateSecond = getMinFactoryDate(secondDataForFactory, secondFactoryDataForRetail.isEvalPemFrom());
            long endDateSecond = getMaxFactoryDate(secondDataForFactory, secondFactoryDataForRetail.isEvalPemTo());
            if (factoryDateOverlap(startDateFirst, endDateFirst, startDateSecond, endDateSecond)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Überprüft, ob die die Werkseinsatzdaten pro Werk der Farbvarianten überlappen.
     * Dazu werden die Retail-Werkseinsatzdaten der Farbvarianten herangezogen (wie sie auch zur Filterung verwendet werden).
     *
     * @param first
     * @param second
     * @return <code>true</code> wenn <code>first</code> und <code>second</code> eine Überlappung in ihren Werkseinsatzdaten
     * haben
     */
    public static boolean isFactoryDataOverlap(iPartsColorTable.ColorTableContent first, iPartsColorTable.ColorTableContent second) {
        Set<String> factoryNumbers = new HashSet<>();
        iPartsColorFactoryDataForRetail firstFactoryDataForRetail = first.getFactoryData();
        Map<String, List<iPartsColorFactoryDataForRetail.DataForFactory>> factoryDataMap = firstFactoryDataForRetail.getFactoryDataMap();
        if (factoryDataMap != null) {
            factoryNumbers.addAll(factoryDataMap.keySet());
        }

        iPartsColorFactoryDataForRetail secondFactoryDataForRetail = second.getFactoryData();
        factoryDataMap = secondFactoryDataForRetail.getFactoryDataMap();
        if (factoryDataMap != null) {
            factoryNumbers.addAll(factoryDataMap.keySet());
        }

        for (String factoryNumber : factoryNumbers) {
            List<iPartsColorFactoryDataForRetail.DataForFactory> firstDataForFactory = firstFactoryDataForRetail.getDataForFactory(factoryNumber);
            long startDateFirst = getMinFactoryDate(firstDataForFactory, first.isEvalPemFrom());
            long endDateFirst = getMaxFactoryDate(firstDataForFactory, first.isEvalPemTo());
            List<iPartsColorFactoryDataForRetail.DataForFactory> secondDataForFactory = secondFactoryDataForRetail.getDataForFactory(factoryNumber);
            long startDateSecond = getMinFactoryDate(secondDataForFactory, second.isEvalPemFrom());
            long endDateSecond = getMaxFactoryDate(secondDataForFactory, second.isEvalPemTo());
            if (factoryDateOverlap(startDateFirst, endDateFirst, startDateSecond, endDateSecond)) {
                return true;
            }
        }

        return false;
    }

    private static boolean factoryDateOverlap(long startDateFirst, long endDateFirst, long startDateSecond, long endDateSecond) {
        if ((startDateFirst != iPartsFactoryData.INVALID_DATE) && (endDateFirst != iPartsFactoryData.INVALID_DATE)
            && (startDateSecond != iPartsFactoryData.INVALID_DATE) && (endDateSecond != iPartsFactoryData.INVALID_DATE)) {
            return iPartsFilterTimeSliceHelper.isInTimeSlice(startDateFirst, endDateFirst, startDateSecond, endDateSecond);
        }
        return false;
    }

    /**
     * Überprüft, ob die Coderegeln in ihren Teilkonjunktionen überlappen.
     * Dazu werden die Coderegeln in ihre Teilkonjunktionen zerlegt und geprüft, ob die Coderegeln mindestens eine gleiche
     * Teilkonjunktion besitzen.
     * Leere Teilkonjunktionen überlappen grundsätzlich nicht mit nicht-leeren Teilkonjunktionen
     *
     * @param codeFirst
     * @param codeSecond
     * @return <code>true</code> wenn <code>first</code> und <code>second</code> mindestens eine gleiche
     * Teilkonjunktion haben
     */
    public static boolean isPartialConjunctionOverlap(String codeFirst, String codeSecond) {
        return isPartialConjunctionOverlap(codeFirst, codeSecond, null);
    }

    private static boolean isPartialConjunctionOverlap(String codeFirst, String codeSecond, VarParam<Conjunction> error) {
        // Zuerst im Cache nachsehen, um doppelte Berechnungen zu vermeiden
        String partialConjunctionOverlapKey;
        if (codeFirst.compareTo(codeSecond) <= 0) {
            partialConjunctionOverlapKey = codeFirst + CACHE_KEY_DELIMITER + codeSecond + CACHE_KEY_DELIMITER;
        } else {
            partialConjunctionOverlapKey = codeSecond + CACHE_KEY_DELIMITER + codeFirst + CACHE_KEY_DELIMITER;
        }

        // Cache nur dann verwenden, wenn die Fehler-Konjunktion nicht benötigt wird
        if (error == null) {
            Boolean cacheResult = partialConjunctionOverlapCache.get(partialConjunctionOverlapKey);
            if (cacheResult != null) {
                return cacheResult;
            }
        }

        try {
            // dnfFirst und dnfSecond werden nicht verändert -> kein Klon der DNF notwendig
            Disjunction dnfFirst = DaimlerCodes.getDnfCodeOriginal(codeFirst);
            Disjunction dnfSecond = DaimlerCodes.getDnfCodeOriginal(codeSecond);

            // Wenn beide Code leer sind (;), dann überlappen sie
            if (dnfFirst.isEmpty() && dnfSecond.isEmpty()) {
                if (error != null) {
                    error.setValue(new Conjunction());
                }
                partialConjunctionOverlapCache.put(partialConjunctionOverlapKey, true);
                return true;
            }

            // Wenn nur einer der beiden Code leer ist (;), dann überlappen sie auch nicht
            if (dnfFirst.isEmpty() || dnfSecond.isEmpty()) {
                partialConjunctionOverlapCache.put(partialConjunctionOverlapKey, false);
                return false;
            }

            for (Conjunction conjunction1 : dnfFirst) {
                for (Conjunction conjunction2 : dnfSecond) {
                    if (conjunction1.isTheSame(conjunction2)) {
                        if (error != null) {
                            error.setValue(conjunction1);
                        }
                        partialConjunctionOverlapCache.put(partialConjunctionOverlapKey, true);
                        return true;
                    }
                }
            }
        } catch (BooleanFunctionSyntaxException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, e);
            // Exceptions beim Parsen können nur bedeuten, dass die Teilkonjunktionen nicht überlappen -> return false am Ende
        }

        partialConjunctionOverlapCache.put(partialConjunctionOverlapKey, false);
        return false;
    }

    /**
     * Prüfung auf "Vorgänger hat alle Teilkonjunktionen der neuen Teilepos und der Vorgänger hat weitere Teilkonjunktionen."
     *
     * @param codesPredecessorStr                 = Vorgänger
     * @param codesSuccessorStr                   = Nachfolger
     * @param checkPredecessorHasMoreConjunctions Flag, ob geprüft werden soll, ob der Vorgänger mehr Teilkonjunktionen
     *                                            hat als der Nachfolger
     * @return <code>true</code> wenn alle <code>codesSuccessorStr</code> in <code>codesPredecessorStr</code> enthalten sind.
     */
    public static boolean isAllPartialConjunctionsIncluded(String codesPredecessorStr, String codesSuccessorStr, boolean checkPredecessorHasMoreConjunctions) {
        try {
            // codesPredecessorStr und codesSuccessorStr werden nicht verändert -> kein Klon der DNF notwendig
            Disjunction codesPredecessor = DaimlerCodes.getDnfCodeOriginal(codesPredecessorStr);
            Disjunction codesSuccessor = DaimlerCodes.getDnfCodeOriginal(codesSuccessorStr);

            // Prüfung, ob der Vorgänger weitere Teilkonjunktionen besitzt.
            // Wenn die Anzahl der Teilkonjunktionen des Vorgängers <= der Anzahl der Teilkonjunktionen des Nachfolgers
            // ist, kann abgebrochen werden.
            if (checkPredecessorHasMoreConjunctions) {
                if (codesPredecessor.size() <= codesSuccessor.size()) {
                    return false;
                }
            }

            return codesPredecessor.isAllPartialConjunctionsIncluded(codesSuccessor);
        } catch (BooleanFunctionSyntaxException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, e);
            // Exceptions beim Parsen können nur bedeuten, dass die Teilkonjunktionen nicht überlappen -> return false
            return false;
        }
    }

    /**
     * Prüfung auf Lenkungsgültigkeit.
     * D.h.:
     * + Leer und "L" ist in Ordnung.
     * + Leer und "R" ist in Ordnung.
     * + Leer und leer ist in Ordnung.
     * + "L" und "R" führen zu Fehler!
     *
     * @param steeringPredecessorStr
     * @param steeringSuccessorStr
     * @return
     */
    public static boolean isSteeringValid(String steeringPredecessorStr, String steeringSuccessorStr) {
        // Wenn mindestens einer der beiden Strings leer ist, passt der Vergleich.
        if (StrUtils.isEmpty(steeringPredecessorStr) || StrUtils.isEmpty(steeringSuccessorStr)) {
            return true;
            // Ansonsten müssen beide gleich sein.
        } else if (steeringPredecessorStr.equals(steeringSuccessorStr)) {
            return true;
        }
        return false;
    }

    public static boolean isAllPartialConjunctionsIncludedForColumnFilter(String filterValue, String codeString) {
        try {
            // codesPredecessorStr und codesSuccessorStr werden nicht verändert -> kein Klon der DNF notwendig
            Disjunction codesFilterValue = DaimlerCodes.getDnfCodeOriginal(filterValue);
            Disjunction codesCodeString = DaimlerCodes.getDnfCodeOriginal(codeString);

            return codesFilterValue.isAllPartialConjunctionsIncludedForColumnFilter(codesCodeString);
        } catch (BooleanFunctionSyntaxException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, e);
            // Exceptions beim Parsen können nur bedeuten, dass die Teilkonjunktionen nicht überlappen -> return false
            return false;
        }
    }

    /**
     * Prüfung, ob die übergebenen Coderegeln bzgl. ihrer DNF identisch sind.
     *
     * @param codes
     * @param otherCodes
     * @return <code>true</code> wenn die Teilkonjunktionen der beiden Coderegeln identisch sind.
     */
    public static boolean isAllPartialConjunctionsEqual(String codes, String otherCodes) {
        try {
            // codes und otherCodes werden nicht verändert -> kein Klon der DNF notwendig
            Disjunction codesDNF = DaimlerCodes.getDnfCodeOriginal(codes);
            Disjunction otherCodesDNF = DaimlerCodes.getDnfCodeOriginal(otherCodes);

            // Prüfung, ob die Anzahl der Teilkonjunktionen übereinstimmt
            if (codesDNF.size() != otherCodesDNF.size()) {
                return false;
            }

            return codesDNF.isAllPartialConjunctionsIncluded(otherCodesDNF);
            // Andere Richtung (otherCodesDNF -> codesDNF) muss nicht geprüft werden, weil vorher ja schon auf gleiche Anzahl
            // Teilkonjunktionen geprüft wurde
        } catch (BooleanFunctionSyntaxException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, e);
            // Exceptions beim Parsen können nur bedeuten, dass die Teilkonjunktionen nicht identisch sind -> return false
            return false;
        }
    }

    /**
     * Überprüft, ob die Coderegeln in ihren Teilkonjunktionen überlappen und liefert die überlappende Teilkonjunktion zurück.
     * Dazu werden die Coderegeln in ihre Teilkonjunktionen zerlegt und geprüft, ob die Coderegeln mindestens eine gleiche
     * Teilkonjunktion besitzen.
     * Ob leere Teilkonjunktionen mit nicht leeren Teilkonjunktionen überlappen, wird über <code>isEmptyOverlap</code>
     * geregelt.
     *
     * @param codeFirst
     * @param codeSecond
     * @return Die überlappende Teilkonjunktion bzw. leer bei keiner Überlappung
     */
    public static String isPartialConjunctionOverlapWithErrorMessage(String codeFirst, String codeSecond) {
        VarParam<Conjunction> error = new VarParam<>();
        if (isPartialConjunctionOverlap(codeFirst, codeSecond, error)) {
            if (error.getValue() != null) {
                return DaimlerCodes.fromFunctionParser(new BooleanFunction(error.getValue()));
            }
        }
        return "";
    }

    /**
     * Liefert alle Filter-relevanten Baumuster für die übergebene Stückliste zurück.
     *
     * @return {@code null} falls keine Baumuster bestimmt werden konnten
     */
    public static Set<String> getAllModelsForAssembly(iPartsDataAssembly assembly) {
        iPartsProductId productId = assembly.getProductIdFromModuleUsage();
        if ((productId != null) && productId.isValidId()) {
            return iPartsProduct.getInstance(assembly.getEtkProject(), productId).getModelNumbers(assembly.getEtkProject());
        }

        return null;
    }

    /**
     * Führt alle Qualitätsprüfungen für Farbvarianten aus für den übergebenen Stücklisteneintrag und Baumuster.
     *
     * @param partListEntry
     * @param models                       Alle zu prüfenden Baumuster bzgl. der Farbvarianten
     * @param colortableQualityChecksCache Cache für die Ergebnisse aller Qualitätsprüfungen mit der Materialnummer (an der
     *                                     alle Farbvariantentabellen hängen) und Baureihe als Schlüssel
     */
    public static iPartsEditBaseValidationForm.ValidationResult executeColortableQualityChecks(iPartsDataPartListEntry partListEntry, Set<String> models,
                                                                                               Map<String, ColortableQualityCheckResult> colortableQualityChecksCache) {
        // Sicherstellen, dass das virtuelle Feld existiert
        partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_COLORTABLE_QUALITY_CHECK, "", true,
                                               DBActionOrigin.FROM_DB);
        iPartsSeriesId seriesId = partListEntry.getSeriesId();
        String cacheKey = partListEntry.getPart().getAsId().getMatNr() + CACHE_KEY_DELIMITER + ((seriesId != null) ? seriesId.getSeriesNumber() : "");

        // Zuerst im Cache nachsehen, um doppelte Berechnungen zu vermeiden
        ColortableQualityCheckResult result = colortableQualityChecksCache.get(cacheKey);
        if (result == null) { // Keinen Cache-Eintrag gefunden
            result = new ColortableQualityCheckResult();

            // Qualitätsprüfung 0
            Set<iPartsColorTable.ColorTable> colorTables = executeColortableQualityCheck0NoVariantTable(partListEntry);
            if (colorTables == null) {
                // Qualitätsprüfung 0 fehlgeschlagen
                result.addFailedCheck(ColortableQualityCheck.CHECK0, iPartsEditBaseValidationForm.ValidationResult.WARNING);
            } else if (!colorTables.isEmpty()) { // Handelt es sich überhaupt um ein farbiges Teil? Falls nein, dann ist colorTables leer
                // Qualitätsprüfung 1
                colorTables = executeColortableQualityCheck1VariantTableInvalid(partListEntry, colorTables);
                if (colorTables == null) {
                    // Qualitätsprüfung 1 fehlgeschlagen
                    result.addFailedCheck(ColortableQualityCheck.CHECK1, iPartsEditBaseValidationForm.ValidationResult.ERROR);
                } else {
                    // Qualitätsprüfung für Variantentabellen zur Baureihe sowohl mit "Auswertung PEM-ab/bis" als auch ohne
                    VarParam<Boolean> hasColorTableWithPemFlags = new VarParam<>(false);
                    if (!executeColortablePemFlagsChecks(colorTables, hasColorTableWithPemFlags)) {
                        result.addFailedCheck(ColortableQualityCheck.CHECK_PEM_FLAGS, iPartsEditBaseValidationForm.ValidationResult.WARNING);
                    }

                    // Qualitätsprüfung 2, 3, 4 zusammen da alle 3 den Baumusterfilter brauchen
                    executeColortableQualityChecksPerModel(partListEntry, colorTables, models, hasColorTableWithPemFlags.getValue(), result);
                }
            }
            colortableQualityChecksCache.put(cacheKey, result);
        }

        // Prüfergebnis als SetOfEnum ins virtuelle Feld schreiben
        partListEntry.setFieldValueAsSetOfEnum(iPartsDataVirtualFieldsDefinition.DA_COLORTABLE_QUALITY_CHECK, result.getFailedQualityChecksAsStrings(),
                                               DBActionOrigin.FROM_DB);

        return result.totalResult;
    }


    /**
     * Führt die Qualitätsprüfung 0 für Farbvarianten aus (Teil ist farbig aber keine Farbtabelle zur Baureihe der Teileposition
     * vorhanden) für den übergebenen Stücklisteneintrag.
     *
     * @param partListEntry
     * @return Set mit allen Farbvariantentabellen, die zur Baureihe der Teileposition passen; bei {@code null} gilt
     * die Prüfung als nicht bestanden; handelt es sich hingegen gar nicht um ein farbiges Teil, dann wird ein leeres
     * Set zurückgegeben
     */
    public static Set<iPartsColorTable.ColorTable> executeColortableQualityCheck0NoVariantTable(iPartsDataPartListEntry partListEntry) {
        boolean oldLogLoadFieldIfNeeded = partListEntry.isLogLoadFieldIfNeeded();
        partListEntry.setLogLoadFieldIfNeeded(false); // M_VARIANT_SIGN ist normalerweise nicht in den geladenen Feldern vorhanden
        String variantSign;
        try {
            variantSign = partListEntry.getPart().getFieldValue(FIELD_M_VARIANT_SIGN);
        } finally {
            partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
        }

        Set<iPartsColorTable.ColorTable> filteredColorTables = new LinkedHashSet<>();
        if (variantSign.equals("1") || variantSign.equals("2")) {
            iPartsSeriesId seriesId = partListEntry.getSeriesId();
            if (seriesId != null) { // Ohne Baureihe keine Prüfung möglich
                iPartsColorTable colorTableData = partListEntry.getColorTableForRetailWithoutFilter();
                if (colorTableData != null) { // Sind überhaupt Farbvariantentabellen vorhanden?
                    for (Map.Entry<String, iPartsColorTable.ColorTable> colorTableEntry : colorTableData.getColorTablesMap().entrySet()) {
                        if (ColorTableHelper.extractSeriesNumberFromTableId(colorTableEntry.getKey()).equals(seriesId.getSeriesNumber())) {
                            filteredColorTables.add(colorTableEntry.getValue()); // Farbvariantentabelle passend zur Baureihe gefunden
                        }
                    }
                }

                // Falls keine zur Baureihe passenden Farbvariantentabellen gefunden wurden, muss null zurückgegeben werden
                if (filteredColorTables.isEmpty()) {
                    filteredColorTables = null;
                }
            }
        }

        return filteredColorTables;
    }

    /**
     * Führt die Qualitätsprüfung 1 für Farbvarianten aus (Keine Farbtabelle ist zeitlich zu einem Baumuster gültig) für den übergebenen
     * Stücklisteneintrag.
     *
     * @param partListEntry
     * @param colorTables   Set mit allen zur Baureihe gültigen Farbvariantentabellen
     * @return Set mit allen Farbvariantentabellen, die mindestens für ein Baumuster zeitlich gültig sind; bei {@code null} gilt
     * die Prüfung als nicht bestanden
     */
    public static Set<iPartsColorTable.ColorTable> executeColortableQualityCheck1VariantTableInvalid(iPartsDataPartListEntry partListEntry,
                                                                                                     Set<iPartsColorTable.ColorTable> colorTables) {
        if (colorTables == null) {
            return null;

        }

        // Es werden generell nur freigegebene Farbvariantentabellen an Stücklisteneinträgen geladen -> keine separate
        // Prüfung notwendig
        Set<iPartsColorTable.ColorTable> filteredColorTables = new LinkedHashSet<>();
        for (iPartsColorTable.ColorTable colorTable : colorTables) {
            for (iPartsColorTable.ColorTableToPart colorTableToPart : colorTable.colorTableToPartsMap.values()) {
                if (iPartsFilter.isColorTableValidForModelTimeSlice(partListEntry.getOwnerAssembly(), colorTableToPart, null)) {
                    filteredColorTables.add(colorTable); // Farbvariantentabelle ist für mindestens ein Baumuster zeitlich gültig
                    break;
                }
            }
        }

        // Falls keine Farbvariantentabellen für mindestens ein Baumuster zeitlich gültig ist, muss null zurückgegeben werden
        if (!filteredColorTables.isEmpty()) {
            return filteredColorTables;
        } else {
            return null;
        }
    }

    /**
     * Prüft, ob es Variantentabellen sowohl mit "Auswertung PEM-ab/bis" gibt als auch ohne
     *
     * @param colorTables
     * @param hasColorTableWithPemFlags Wird gesetzt, wenn es mindestens eine Variantentabellen mit "Auswertung PEM-ab/bis" gibt
     * @return
     */
    public static boolean executeColortablePemFlagsChecks(Set<iPartsColorTable.ColorTable> colorTables, VarParam<Boolean> hasColorTableWithPemFlags) {
        hasColorTableWithPemFlags.setValue(false);
        boolean hasColorTableWithoutPemFlags = false;
        for (iPartsColorTable.ColorTable colorTable : colorTables) {
            boolean hasPemFlags = false;

            // Alle Datensätze für eine Farbvariantentabelle gemeinsam betrachten
            for (iPartsColorTable.ColorTableToPart colorTableToPart : colorTable.colorTableToPartsMap.values()) {
                if (colorTableToPart.isEvalPemFrom() || colorTableToPart.isEvalPemTo()) {
                    hasPemFlags = true;
                    break;
                }
            }

            if (hasPemFlags) {
                hasColorTableWithPemFlags.setValue(true);
            } else {
                hasColorTableWithoutPemFlags = true;
            }

            if (hasColorTableWithoutPemFlags && hasColorTableWithPemFlags.getValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Führt die Qualitätsprüfung 2, 3 und 4 sowie Prüfung der Werkseinsatzdaten für Farbvarianten aus
     * Prüfung 2: Alle Farbvarianten einer Farbtabelle sind zu keinem Baumuster gültig
     * Prüfung 3: Bei mindestens einer Farbvariante gibt es Überlappungen in Code, Werksdaten und Ereignissen für den
     * GLEICHEN ES2-Schlüssel
     * Prüfung 4: Bei mindestens einer Farbvariante gibt es Überlappungen in Code, Werksdaten und Ereignissen für einen
     * ANDEREN ES2-Schlüssel
     * Werkseinsatzdaten: Bei gesetztem/berechneten Flag "PEM-ab/bis auswerten" muss es ein gültiges Datum PEM-ab/bis bei
     * den Werkseinsatzdaten geben
     *
     * @param partListEntry
     * @param colorTables               Set mit allen zur Baureihe gültigen Farbvariantentabellen, die zeitlich für mindestens ein
     *                                  Baumuster gütlig sind
     * @param models                    Alle zu prüfenden Baumuster bzgl. der Farbvarianten
     * @param hasColorTableWithPemFlags Flag, ob es Farbvariantentabellen mit gesetzten PEM-Flags gibt
     * @param totalResult               Bisheriges Ergebnis der Qualitätsprüfung, das um die Fehler und Warnungen dieser Prüfungen ergänzt
     *                                  werden muss
     */
    public static void executeColortableQualityChecksPerModel(iPartsDataPartListEntry partListEntry, Set<iPartsColorTable.ColorTable> colorTables,
                                                              Set<String> models, boolean hasColorTableWithPemFlags, ColortableQualityCheckResult totalResult) {
        // Es werden generell nur freigegebene Farbvarianten mit leerem ETKZ an Stücklisteneinträgen geladen -> keine separate
        // Prüfung notwendig
        iPartsColorTable colorTablesForRetailWithoutFilter = partListEntry.getColorTableForRetailWithoutFilter();
        Set<iPartsColorTable.ColorTable> filteredColorTables = new LinkedHashSet<>();

        // Nur den Baumuster-Filter aktivieren
        iPartsFilter filterForModelEvaluation = new iPartsFilter();
        filterForModelEvaluation.activateOnlyModelFilterWithFilterReason();

        boolean check3Failed = false;
        iPartsEditBaseValidationForm.ValidationResult check4ValidationResult = iPartsEditBaseValidationForm.ValidationResult.OK;
        iPartsSeriesId seriesId = partListEntry.getSeriesId();
        iPartsDialogSeries series = null;
        if (seriesId != null) {
            series = iPartsDialogSeries.getInstance(partListEntry.getEtkProject(), seriesId);
        }

        VarParam<Boolean> missingFactoryDataPEMFrom = new VarParam<>(false);
        VarParam<Boolean> missingFactoryDataPEMTo = new VarParam<>(false);

        // Für alle Baumuster aus models eine virtuelle Baumuster-Datenkarte erzeugen und damit die Farbvariantentabellen filtern
        modelsLoop:
        for (String model : models) {
            // Map von ES2-Schlüssel auf Liste von bisher gültigen {@link iPartsColorTable.ColorTableContent}s für diesen ES2
            Map<String, List<iPartsColorTable.ColorTableContent>> evalMapForCheck3and4 = new HashMap<>();

            filterForModelEvaluation.setDataCardByModel(model, partListEntry.getEtkProject());

            // Retailfilter für Farbvariantentabellen durchführen
            iPartsColorTable colorTablesForRetail = filterForModelEvaluation.getColorTableForRetailFiltered(colorTablesForRetailWithoutFilter,
                                                                                                            partListEntry);

            if (colorTablesForRetail != null) {
                // Werkseinsatzdaten prüfen
                executeColorFactoryDataExistsCheck(colorTablesForRetail, colorTablesForRetailWithoutFilter, missingFactoryDataPEMFrom,
                                                   missingFactoryDataPEMTo);

                // Für alle bisher gültigen Farbvariantentabellen prüfen, ob für das aktuelle Baumuster mindestens für eine
                // Farbvariante gültig ist (dann gibt es Farbvarianten in den colorTableContents vom passenden colorTablesForRetail-Eintrag)
                colorTablesLoop:
                for (iPartsColorTable.ColorTable colorTableOuterLoop : colorTables) {
                    // Kriterien für vorzeitigen Abbruch:
                    // Alle Farbvariantentabellen sind bereits gültig und Check 3 und 4 sind fehlgeschlagen
                    if ((filteredColorTables.size() == colorTables.size()) && check3Failed && (check4ValidationResult == iPartsEditBaseValidationForm.ValidationResult.ERROR)) {
                        if (missingFactoryDataPEMFrom.getValue() && missingFactoryDataPEMTo.getValue()) {
                            // Werkseinsatzdaten-Prüfung ist ebenfalls fehlgeschlagen -> es muss kein Baumuster mehr geprüft werden
                            break modelsLoop;
                        } else {
                            // Farbvariantentabellen müssen nicht mehr geprüft werden, aber die Werkseinsatzdaten-Prüfung
                            // ist noch nicht fehlgeschlagen -> nächstes Baumuster prüfen
                            break colorTablesLoop;
                        }
                    }

                    Set<iPartsColorTable.ColorTable> colorTablesInnerLoop = colorTables;
                    if (hasColorTableWithPemFlags) {
                        // Bei gesetzten PEM-Flags die Farbvarianten nur innerhalb einer Farbvariantentabelle prüfen
                        colorTablesInnerLoop = new HashSet<>();
                        colorTablesInnerLoop.add(colorTableOuterLoop);
                        evalMapForCheck3and4.clear();
                    }

                    for (iPartsColorTable.ColorTable colorTable : colorTablesInnerLoop) {
                        iPartsColorTable.ColorTable colorTableFiltered = colorTablesForRetail.getColorTablesMap().get(colorTable.colorTableId.getColorTableId());
                        if (colorTableFiltered != null) {
                            if (!colorTableFiltered.colorTableContents.isEmpty()) {
                                filteredColorTables.add(colorTable);
                                for (iPartsColorTable.ColorTableContent colorTableContent : colorTableFiltered.colorTableContents) {
                                    List<iPartsColorTable.ColorTableContent> evalContents = evalMapForCheck3and4.get(colorTableContent.colorNumber);
                                    if (evalContents == null) {
                                        evalContents = new DwList<>();
                                        evalMapForCheck3and4.put(colorTableContent.colorNumber, evalContents);
                                    }
                                    evalContents.add(colorTableContent);
                                }
                            }
                        }
                    }

                    // sobald Check 3 oder 4 für irgendein Baumuster fehlgeschlagen sind, kann man die Durchführung auslassen
                    if (!check3Failed) {
                        boolean check3 = executeColortableQualityCheck3(evalMapForCheck3and4, series);
                        if (!check3) {
                            check3Failed = true;
                        }
                    }
                    if (check4ValidationResult != iPartsEditBaseValidationForm.ValidationResult.ERROR) {
                        iPartsEditBaseValidationForm.ValidationResult check4 = executeColortableQualityCheck4(evalMapForCheck3and4, series);
                        if (check4 != iPartsEditBaseValidationForm.ValidationResult.OK) {
                            // check4ValidationResult kann nicht ERROR sein wegen obiger Bedingung (also keine erneute Prüfung notwendig)
                            check4ValidationResult = check4;
                        }
                    }

                    if (!hasColorTableWithPemFlags) {
                        // Ohne gesetzte PEM-Flags wurden über colorTablesInnerLoop bereits alle Farbvariantentabellen berücksichtigt
                        break colorTablesLoop;
                    }
                }
            }
        }

        // Ergebnisse sammeln und ausgeben
        // Prüfung 2
        // Geänderte Logik (DAIMLER-10084) wenn eine Variante für ein Baumuster gültig ist, dann gibt das nur eine Warnung
        // Falls aber keine Variante gültig ist, gibt es wie bisher einen Fehler
        if (filteredColorTables.size() != colorTables.size()) {
            if (filteredColorTables.size() > 0) {
                totalResult.addFailedCheck(ColortableQualityCheck.CHECK2, iPartsEditBaseValidationForm.ValidationResult.WARNING);
            } else {
                totalResult.addFailedCheck(ColortableQualityCheck.CHECK2, iPartsEditBaseValidationForm.ValidationResult.ERROR);
            }
        }

        if (check3Failed) {
            totalResult.addFailedCheck(ColortableQualityCheck.CHECK3, iPartsEditBaseValidationForm.ValidationResult.WARNING);
        }

        if (check4ValidationResult != iPartsEditBaseValidationForm.ValidationResult.OK) {
            totalResult.addFailedCheck(ColortableQualityCheck.CHECK4, check4ValidationResult);
        }

        if (missingFactoryDataPEMFrom.getValue()) {
            totalResult.addFailedCheck(ColortableQualityCheck.CHECK_PEM_FROM, iPartsEditBaseValidationForm.ValidationResult.ERROR);
        }
        if (missingFactoryDataPEMTo.getValue()) {
            totalResult.addFailedCheck(ColortableQualityCheck.CHECK_PEM_TO, iPartsEditBaseValidationForm.ValidationResult.ERROR);
        }
    }

    /**
     * Führt die Qualitätsprüfung 4 aus: Bei mindestens einer Farbvariante gibt es Überlappungen in Code, Werksdaten und Ereignissen
     * für einen ANDEREN ES2-Schlüssel
     *
     * @param evalMap Map von ES2-Schlüssel auf Liste von bisher gültigen {@link iPartsColorTable.ColorTableContent}s für
     *                diesen ES2
     * @param series
     * @return
     */
    public static iPartsEditBaseValidationForm.ValidationResult executeColortableQualityCheck4(Map<String, List<iPartsColorTable.ColorTableContent>> evalMap,
                                                                                               iPartsDialogSeries series) {
        Set<String> alreadyCheckedPairs = new HashSet<>(); // Zur Vermeidung von doppelten Überprüfungen
        for (Map.Entry<String, List<iPartsColorTable.ColorTableContent>> compareGroup : evalMap.entrySet()) {
            String colorNumber = compareGroup.getKey();
            // suche alle Einträge mit anderem ES2 Schlüssel zusammen
            List<iPartsColorTable.ColorTableContent> otherES2 = new DwList<>();
            for (Map.Entry<String, List<iPartsColorTable.ColorTableContent>> entry : evalMap.entrySet()) {
                if (!entry.getKey().equals(colorNumber)) {
                    otherES2.addAll(entry.getValue());
                }
            }

            // jeden Eintrag aus dieser Liste mit jedem aus otherES2 vergleichen
            for (iPartsColorTable.ColorTableContent validationItem : compareGroup.getValue()) {
                for (iPartsColorTable.ColorTableContent compareValidationItem : otherES2) {
                    String validationItemPairKey;
                    String validationItemString = validationItem.colorTableContentId.toDBString();
                    String compareValidationItemString = compareValidationItem.colorTableContentId.toDBString();
                    if (validationItemString.compareTo(compareValidationItemString) <= 0) {
                        validationItemPairKey = validationItemString + CACHE_KEY_DELIMITER + compareValidationItemString;
                    } else {
                        validationItemPairKey = compareValidationItemString + CACHE_KEY_DELIMITER + validationItemString;
                    }
                    if (!alreadyCheckedPairs.contains(validationItemPairKey)) {
                        if (!executeColortableQualityCheck3and4Overlap(validationItem, compareValidationItem, series)) {
                            // Bei Betriebsanleitungen ist ein fehlgeschlagener Check 4 nur eine Warnung, sonst ein Fehler
                            if (ColorTableHelper.isUserManualColorTable(validationItem.colorTableContentId.getColorTableId())) {
                                return iPartsEditBaseValidationForm.ValidationResult.WARNING;
                            } else {
                                return iPartsEditBaseValidationForm.ValidationResult.ERROR;
                            }
                        }
                        alreadyCheckedPairs.add(validationItemPairKey);
                    }
                }
            }
        }

        return iPartsEditBaseValidationForm.ValidationResult.OK;
    }

    /**
     * Führt die Qualitätsprüfung 3 aus: Bei mindestens einer Farbvariante gibt es Überlappungen in Code, Werksdaten und Ereignissen
     * für den GLEICHEN ES2-Schlüssel
     *
     * @param evalMap Map von ES2-Schlüssel auf Liste von bisher gültigen {@link iPartsColorTable.ColorTableContent}s für
     *                diesen ES2
     * @param series
     * @return
     */
    public static boolean executeColortableQualityCheck3(Map<String, List<iPartsColorTable.ColorTableContent>> evalMap,
                                                         iPartsDialogSeries series) {
        for (List<iPartsColorTable.ColorTableContent> validationItemsForES2 : evalMap.values()) {
            // Wenn es für diesen ES2-Schlüssel nur eine gültige Farbvariante gibt, muss nicht vergleichen werden
            if (validationItemsForES2.size() > 1) {
                for (int currentIndex = 0; currentIndex < validationItemsForES2.size(); currentIndex++) {
                    for (int compareIndex = currentIndex + 1; compareIndex < validationItemsForES2.size(); compareIndex++) {
                        // Nur solche Farbvarianten prüfen, die nicht bereits geprüft wurden
                        if (!executeColortableQualityCheck3and4Overlap(validationItemsForES2.get(currentIndex),
                                                                       validationItemsForES2.get(compareIndex), series)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Prüft, ob es Überlappungen in Code, Werksdaten und Ereignissen zwischen den beiden übergebenen Farbvarianten gibt.
     *
     * @param first
     * @param second
     * @param series
     * @return
     */
    public static boolean executeColortableQualityCheck3and4Overlap(iPartsColorTable.ColorTableContent first,
                                                                    iPartsColorTable.ColorTableContent second,
                                                                    iPartsDialogSeries series) {
        // Ereignisketten
        if ((series != null) && series.isEventTriggered()) { // nur bei Ereignis-gesteuerten Baureihen
            if (!isEventChainOverlap(series, first, second)) {
                return true;
            }
        }

        // Werkseinsatzdaten
        if (!isFactoryDataOverlap(first, second)) {
            return true;
        }

        // Überlappung der Teilkonjunktionen prüfen
        if (!isPartialConjunctionOverlap(first.code, second.code)) {
            return true;
        }

        return false;
    }

    /**
     * Prüft, ob bei gesetztem/berechneten Flag "PEM-ab/bis auswerten" es ein gültiges Datum PEM-ab/bis bei den Werkseinsatzdaten
     * gibt.
     *
     * @param colorTableData
     * @param colorTablesForRetailWithoutFilter
     * @param missingFactoryDataPEMFrom
     * @param missingFactoryDataPEMTo
     */
    private static void executeColorFactoryDataExistsCheck(iPartsColorTable colorTableData, iPartsColorTable colorTablesForRetailWithoutFilter,
                                                           VarParam<Boolean> missingFactoryDataPEMFrom, VarParam<Boolean> missingFactoryDataPEMTo) {
        // Frühzeitiger Abbruch
        if (missingFactoryDataPEMFrom.getValue() && missingFactoryDataPEMTo.getValue()) {
            return;
        }

        for (Map.Entry<String, iPartsColorTable.ColorTable> colorTableEntry : colorTableData.getColorTablesMap().entrySet()) {
            // DAIMLER-10583: Die eigentlichen Prüfungen bzgl. der Flags "PEM-ab/bis auswerten" sollen auf den Original-Daten
            // der Farbvarianten colorTablesForRetailWithoutFilter gemacht werden, damit berechnete Flags nicht berücksichtigt
            // werden, weil dies für die Autoren ohne zusätzliche Anzeige nur schwer nachvollziehbar ist
            iPartsColorTable.ColorTable colorTableUnfiltered = colorTablesForRetailWithoutFilter.getColorTable(colorTableEntry.getKey());
            if (colorTableUnfiltered == null) { // Kann eigentlich gar nicht passieren, weil colorTableData aus colorTablesForRetailWithoutFilter entsteht
                continue;
            }

            for (iPartsColorTable.ColorTableContent colorTableContent : colorTableEntry.getValue().colorTableContents) {
                iPartsColorTable.ColorTableContent colorTableContentUnfiltered = colorTableUnfiltered.getColorTableContent(colorTableContent.colorTableContentId);
                if (colorTableContentUnfiltered == null) { // Kann eigentlich gar nicht passieren, weil colorTableData aus colorTablesForRetailWithoutFilter entsteht
                    continue;
                }

                // "PEM ab auswerten"-Flag gesetzt?
                if (colorTableContentUnfiltered.isEvalPemFrom()) {
                    boolean validFactoryDataFound = false;
                    iPartsColorFactoryDataForRetail factoryData = colorTableContentUnfiltered.getFactoryData();
                    if (factoryData != null) {
                        factoryDataLoop:
                        for (List<iPartsColorFactoryDataForRetail.DataForFactory> factoryDataList : factoryData.getFactoryDataMap().values()) {
                            for (iPartsColorFactoryDataForRetail.DataForFactory dataForFactory : factoryDataList) {
                                if (dataForFactory.hasValidDateFrom() && (dataForFactory.dateFrom != 0)) { // Echtes PEM-ab Datum vorhanden?
                                    validFactoryDataFound = true;
                                    break factoryDataLoop;
                                }
                            }
                        }
                    }

                    if (!validFactoryDataFound) {
                        missingFactoryDataPEMFrom.setValue(true);
                    }
                }

                // Frühzeitiger Abbruch
                if (missingFactoryDataPEMFrom.getValue() && missingFactoryDataPEMTo.getValue()) {
                    return;
                }

                // "PEM bis auswerten"-Flag gesetzt?
                if (colorTableContentUnfiltered.isEvalPemTo()) {
                    boolean validFactoryDataFound = false;
                    iPartsColorFactoryDataForRetail factoryData = colorTableContentUnfiltered.getFactoryData();
                    if (factoryData != null) {
                        factoryDataLoop:
                        for (List<iPartsColorFactoryDataForRetail.DataForFactory> factoryDataList : factoryData.getFactoryDataMap().values()) {
                            for (iPartsColorFactoryDataForRetail.DataForFactory dataForFactory : factoryDataList) {
                                if (dataForFactory.hasValidDateTo() && (dataForFactory.dateTo != 0)) { // Echtes PEM-bis Datum vorhanden?
                                    validFactoryDataFound = true;
                                    break factoryDataLoop;
                                }
                            }
                        }
                    }

                    if (!validFactoryDataFound) {
                        missingFactoryDataPEMTo.setValue(true);
                    }
                }

                // Frühzeitiger Abbruch
                if (missingFactoryDataPEMFrom.getValue() && missingFactoryDataPEMTo.getValue()) {
                    return;
                }
            }
        }
    }

    /**
     * Handelt es sich um ein Spezial-Modul für die vereinfachte Qualitätsprüfung inkl. Prüfung auf das dafür notwendige
     * Recht?
     *
     * @param assembly
     * @return
     */
    public static boolean isSimplifiedQualityCheck(iPartsDataAssembly assembly) {
        return iPartsRight.SIMPLIFIED_QUALITY_CHECKS.checkRightInSession() && ((assembly.getModuleMetaData().getAggTypeForSpecialZBFilter() != DCAggregateTypes.UNKNOWN)
                                                                               || EditModuleHelper.isCarPerspectiveAssembly(assembly));
    }
}