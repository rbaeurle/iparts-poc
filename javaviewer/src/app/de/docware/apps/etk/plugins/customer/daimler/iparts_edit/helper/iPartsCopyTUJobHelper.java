/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.events.OnValidateAttributesEvent;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.DataImageId;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImageList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.session.SessionData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReportConstNodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuListItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAssemblyListForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.RelocateEntriesConfig;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.AuthorOrderHistoryFormatter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.EditTransferToASHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.TransferToASElement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.framework.modules.gui.session.SessionType;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.enums.EnumUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.os.OsUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper um aus aus einer Liste von TU's neue zu kopieren
 */
public class iPartsCopyTUJobHelper {

    private static final String TITLE = "!!TUs-Kopieren in anderes Produkt";
    private static final String TITLE_PSK = "!!TUs-Kopieren in anderes PSK-Produkt";
    private static final String EXECUTE_IN_BACKGROUND_TEXT = "!!Im Hintergrund ausführen";
    private static final EnumSet<iPartsDocuRelevant> ALLOWED_FOR_TRANSFER_TO_AS = EnumSet.of(iPartsDocuRelevant.DOCU_DOCUMENTED,
                                                                                             iPartsDocuRelevant.DOCU_RELEVANT_YES,
                                                                                             iPartsDocuRelevant.DOCU_DOCUMENTED_IN_AUTHOR_ORDER);

    private final AbstractJavaViewerFormIConnector connector;
    private final EtkProject project;
    private final Session sessionForGUI;
    private final iPartsProductId sourceProductId;
    private final Set<iPartsAssemblyId> targetAssemblyIds = new TreeSet<>();
    private final boolean targetIsPSKProduct;
    private final String kgForMessageLog;
    private final boolean isLastKG;
    private final boolean showMessageBeforeTransfer = true; // vor der Übernahme nach AS Meldung anzeigen
    private final VarParam<Boolean> isCancelled = new VarParam<>(false);
    private final VarParam<Boolean> finished = new VarParam<>(false);
    private EtkProject projectForGUI;
    private Thread threadForGUI;
    private Session sessionForExecution;
    private iPartsProductId targetProductId;
    private boolean copyPSKProductToPSKProduct;
    private boolean filterPSKVariantIds;
    private boolean updatePSKModules;
    private EtkMessageLogForm messageLogForm;
    private EventListener executeInBackgroundEventListener;
    private EtkMessageLog messageLogForExecution;
    private DWFile logFile;
    private iPartsDataAuthorOrder updatePSKDataAuthorOrder;
    private EtkMessageLogForm.CancelListener cancelListener;
    private EtkMessageLogForm.FinishedListener finishedListener;
    private boolean finishedWithErrors;
    private boolean executedInBackground;
    private boolean executeForAll;
    private boolean stopForFollowing;

    public static iPartsCopyTUJobHelper copyTUsWithJob(CopyOrUpdatePSKContainer copyOrUpdatePSKContainer,
                                                       Collection<iPartsCopyTUJobHelper.TUCopyContainer> copyList,
                                                       boolean targetIsPSKProduct,
                                                       String kgForMessageLog, boolean isLastKG) {
        iPartsCopyTUJobHelper helper = new iPartsCopyTUJobHelper(copyOrUpdatePSKContainer, targetIsPSKProduct, kgForMessageLog,
                                                                 isLastKG);
        helper.addAssembliesToCopyJob(copyList);
        return helper;
    }

    public static iPartsCopyTUJobHelper updatePSKTUsWithJob(CopyOrUpdatePSKContainer copyOrUpdatePSKContainer,
                                                            Collection<iPartsCopyTUJobHelper.TUCopyContainer> updateList,
                                                            String kgForMessageLog, boolean isLastKG) {
        iPartsCopyTUJobHelper helper = new iPartsCopyTUJobHelper(copyOrUpdatePSKContainer, true, kgForMessageLog, isLastKG);
        helper.addAssembliesToUpdatePSKJob(updateList);
        return helper;
    }

    public iPartsCopyTUJobHelper(CopyOrUpdatePSKContainer copyOrUpdatePSKContainer, boolean targetIsPSKProduct, String kgForMessageLog,
                                 boolean isLastKG) {
        this.connector = copyOrUpdatePSKContainer.connectorForCopyUpdate;
        this.project = connector.getProject();
        this.sourceProductId = copyOrUpdatePSKContainer.sourceProductId;
        this.targetProductId = copyOrUpdatePSKContainer.targetProductId;
        this.sessionForGUI = copyOrUpdatePSKContainer.sessionForGUI;
        this.sessionForExecution = copyOrUpdatePSKContainer.sessionForExecution;
        this.messageLogForm = copyOrUpdatePSKContainer.messageLogFormForExecution;
        this.messageLogForExecution = copyOrUpdatePSKContainer.messageLogForExecution;
        this.logFile = copyOrUpdatePSKContainer.logFileForExecution;
        this.targetIsPSKProduct = targetIsPSKProduct;
        this.filterPSKVariantIds = false;
        this.kgForMessageLog = kgForMessageLog;
        this.isLastKG = isLastKG;
        this.executedInBackground = ((messageLogForm != null) && messageLogForm.getModalResult() == ModalResult.CONTINUE);
        this.executeForAll = copyOrUpdatePSKContainer.executeForAll;
        this.stopForFollowing = copyOrUpdatePSKContainer.stoppedByUser;
        if (sessionForGUI != null) {
            this.projectForGUI = (EtkProject)sessionForGUI.getAttribute(JavaViewerApplication.SESSION_KEY_PROJECT);
            this.threadForGUI = Thread.currentThread();
        }
        this.updatePSKDataAuthorOrder = copyOrUpdatePSKContainer.updatePSKDataAuthorOrder;
    }

    public EtkProject getProject() {
        return project;
    }

    public EtkRevisionsHelper getRevisionsHelper() {
        return project.getRevisionsHelper();
    }

    public int addAssembliesToCopyJob(Collection<TUCopyContainer> copyList) {
        if (!copyList.isEmpty()) {
            // Step 1: bestimme Ziel-Produkt
            iPartsProduct targetProduct;
            if (targetProductId != null) {
                targetProduct = iPartsProduct.getInstance(project, targetProductId);
            } else {
                targetProduct = askForTargetProduct();
            }
            if (targetProduct == null) {
                markAsFinished(true);
                return 0;
            }

            String title = targetIsPSKProduct ? TITLE_PSK : TITLE;
            String subTitle = targetIsPSKProduct ? EditModuleForm.IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_PSK_MULTI
                                                 : EditModuleForm.IPARTS_MENU_ITEM_COPY_RETAIL_MODULE_TEXT_MULTI;
            return addAssembliesToJob(targetProduct, copyList, title, subTitle);
        } else {
            markAsFinished(true);
            return 0;
        }
    }

    public int addAssembliesToUpdatePSKJob(Collection<TUCopyContainer> updateList) {
        if (!updateList.isEmpty()) {
            EtkEbenenDaten partsListTypeWithCopyVari = new EtkEbenenDaten();
            partsListTypeWithCopyVari.addFeld(new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_COPY_VARI, false, false));

            updatePSKModules = true;
            List<iPartsCopyTUJobHelper.TUCopyContainer> updateListWithSourceAssemblies = new ArrayList<>(updateList.size());
            for (TUCopyContainer updateContainer : updateList) {
                AssemblyId targetAssemblyId = updateContainer.getSourceAssemblyId(); // Ziel-PSK-Modul ist noch in assemblyId enthalten
                AssemblyId sourceAssemblyId = calcSourceAssemblyIdForPSK(targetAssemblyId, partsListTypeWithCopyVari);
                updateListWithSourceAssemblies.add(new TUCopyContainer(sourceAssemblyId, targetAssemblyId, updateContainer.getKgTuId()));
            }

            String title = (updateListWithSourceAssemblies.size() > 1) ? EditModuleForm.IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT_MULTI :
                           EditModuleForm.IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT;
            return addAssembliesToJob(iPartsProduct.getInstance(project, targetProductId), updateListWithSourceAssemblies,
                                      title, "");
        } else {
            markAsFinished(true);
            return 0;
        }
    }

    protected AssemblyId calcSourceAssemblyIdForPSK(AssemblyId targetAssemblyId, EtkEbenenDaten partsListTypeWithCopyVari) {
        // Quell-Stückliste ermitteln über die Stücklisteneinträge mit den meisten Einträgen für das Quell-Modul in K_COPY_VARI
        EtkDataAssembly targetAssembly = EtkDataObjectFactory.createDataAssembly(project, targetAssemblyId);
        Map<AssemblyId, Integer> assemblyIdToRefCountMap = new TreeMap<>();
        for (EtkDataPartListEntry partListEntry : targetAssembly.getPartListUnfiltered(partsListTypeWithCopyVari)) {
            String copyVari = partListEntry.getFieldValue(iPartsConst.FIELD_K_COPY_VARI);
            if (!copyVari.isEmpty()) {
                AssemblyId sourceAssemblyId = new AssemblyId(copyVari, "");
                Integer refCount = assemblyIdToRefCountMap.computeIfAbsent(sourceAssemblyId, assemblyId -> 0);
                refCount++;
                assemblyIdToRefCountMap.put(sourceAssemblyId, refCount); // Integer selbst kann nicht verändert werden
            }
        }

        // Quell-Modul mit den meisten Referenzen suchen
        AssemblyId sourceAssemblyId = null;
        int maxRefCount = 0;
        for (Map.Entry<AssemblyId, Integer> assemblyIdRefCountEntry : assemblyIdToRefCountMap.entrySet()) {
            int refCount = assemblyIdRefCountEntry.getValue();
            if (refCount > maxRefCount) {
                sourceAssemblyId = assemblyIdRefCountEntry.getKey();
                maxRefCount = refCount;
            }
        }
        return sourceAssemblyId;
    }

    private EtkMessageLogForm.FinishedListener createFinishedListener(EtkMessageLogForm messageLogForm, boolean isLastKG) {
        return workerThread -> {
            if (!isLastKG) {
                EtkMessageLogForm messageLogFormLocal = messageLogForm;
                if (messageLogFormLocal != null) {
                    // Der Abbrechen-Button wird vom EtkMessageLogForm nach dem Beenden vom Arbeits-Thread ausgeblendet
                    // und stattdessen der Schließen-Button angezeigt -> darf aber erst bei der letzten KG gemacht werden
                    messageLogFormLocal.getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE).setVisible(false);
                    messageLogFormLocal.getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setVisible(true);
                }
            }
        };
    }

    private EtkMessageLogForm createMessageForm(String title, String subTitle) {
        // Log-Fenster für die Logmeldungen
        EtkMessageLogForm messageLogForm = new EtkMessageLogForm(title, subTitle, null);

        // Speichern in dem Member finishedListener ist wichtig, damit der Listener nicht von der GC aufgeräumt wird, da
        // die Listener per WeakReference verwaltet werden
        finishedListener = createFinishedListener(messageLogForm, isLastKG);
        messageLogForm.addFinishedListener(finishedListener);

        messageLogForm.getGui().setSize(1000, 800);

        // Button für die Ausführung im Hintergrund
        messageLogForm.getButtonPanel().addCustomButton(EXECUTE_IN_BACKGROUND_TEXT);

        return messageLogForm;
    }

    private void refreshButtonBackgroundExecution() {
        GuiButtonOnPanel buttonBackgroundExecution = messageLogForm.getButtonPanel().getCustomButtonOnPanel(EXECUTE_IN_BACKGROUND_TEXT);
        if (buttonBackgroundExecution != null) {
            buttonBackgroundExecution.removeEventListeners(Event.ACTION_PERFORMED_EVENT); // EventListener von der vorherigen KG entfernen
            executeInBackgroundEventListener = new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    executedInBackground = true;
                    if (messageLogForm != null) {
                        messageLogForm.closeWindow(ModalResult.CONTINUE, false);
                    }

                    // Der ursprüngliche GUI-Thread müssen bei Ausführung im Hintergrund der neuen Hintergrund-Session
                    // zugewiesen werden
                    if ((sessionForGUI != null) && (threadForGUI != null)) {
                        SessionManager.getInstance().registerThreadForSession(sessionForExecution, threadForGUI, false);
                    }
                }
            };

            // Sicherstellen, dass die Abarbeitung threadSafe in einem Thread der GUI-Session stattfindet
            if (sessionForGUI != null) {
                sessionForGUI.invokeThreadSafeInSessionThread(() -> buttonBackgroundExecution.addEventListener(executeInBackgroundEventListener));
            } else {
                buttonBackgroundExecution.addEventListener(executeInBackgroundEventListener);
            }
        }
    }

    private FrameworkRunnable createJobRunnable(final iPartsProduct destinationProduct, final Collection<TUCopyContainer> copyList,
                                                final String title, final VarParam<Integer> result) {
        FrameworkRunnable runnable = new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                if (sessionForExecution == null) {
                    // Arbeits-Thread aus der aktuellen GUI-Session entfernen, da diese ansonsten nicht beendet werden
                    // kann bevor nicht der Arbeits-Thread beendet wurde
                    Session session = Session.get();
                    if ((session != null) && (messageLogForm != null)) {
                        if (thread != null) {
                            SessionManager sessionManager = SessionManager.getInstance();
                            Thread executionThread = thread.getRealThread();
                            executionThread.setName("Execution thread for " + TranslationHandler.translateForLanguage(title, Language.EN.getCode())
                                                    + " started by session " + session.getId());

                            try {
                                // Den aktuellen Thread explizit NICHT in der neuen Session registrieren, weil diese
                                // nur mit dem executionThread laufen soll
                                Session sessionForExecutionLocal = EtkEndpointHelper.createSession(SessionType.BACKGROUND_GUI, false);
                                if (sessionForExecutionLocal != null) {
                                    // sessionForExecutionLocal initialisieren für die Ausführung im Hintergrund
                                    sessionManager.registerThreadForSession(sessionForExecutionLocal, executionThread, false);

                                    // Attribute der Original-Session übernehmen außer TranslationHandler
                                    sessionForExecutionLocal.assignAttributes(session, Constants.SESSION_KEY_DEFAULT_TRANSLATION_HANDLER);

                                    // EtkProject, SessionData und TranslationHandler explizit setzen
                                    sessionForExecutionLocal.setAttribute(JavaViewerApplication.SESSION_KEY_PROJECT, project);
                                    SessionData sessionData = (SessionData)session.getAttribute(SessionData.SESSION_KEY_CONFIG);
                                    SessionData sessionDataForBackgroundExecution = new SessionData(session.getStartParameter(), project);
                                    if (sessionData != null) {
                                        sessionDataForBackgroundExecution.setMainWindow(sessionData.getMainWindow());
                                    }
                                    sessionForExecutionLocal.setAttribute(SessionData.SESSION_KEY_CONFIG, sessionDataForBackgroundExecution);

                                    TranslationHandler translationHandler = (TranslationHandler)sessionForExecutionLocal.getAttribute(Constants.SESSION_KEY_DEFAULT_TRANSLATION_HANDLER);
                                    translationHandler.setCurrentLanguage(project.getViewerLanguage());

                                    sessionForExecution = sessionForExecutionLocal;
                                }
                            } finally {
                                // Der aktuelle Thread muss bei Ausführung im Hintergrund der neuen Hintergrund-Session
                                // zugewiesen werden
                                SessionManager.getInstance().registerThreadForSession(sessionForExecution, Thread.currentThread(), false);

                                iPartsEditPlugin.stopEditing(session); // Das Editieren in der GUI-Session wieder erlauben
                            }
                        }
                    }
                }

                boolean hasErrors = false;
                try {
                    String msg = getKGProductString();
                    showMsg(TranslationHandler.translate("!!Starte Verarbeitung%1", msg));
                    addMsgToUpdatePSKDataAuthorOrder(TranslationHandler.translate("!!PSK-Abgleich%1", msg));
                    // Step 2: Relevante TUs zusammensuchen inkl. Anzeige/Bestätigung bei bereits vorhandenen TUs
                    if (!checkUsage(copyList, destinationProduct)) {
                        if (updatePSKDataAuthorOrder != null) {
                            // Für den gesamten KG-Knoten gibt es keine relevanten TUs -> Beschreibung vom PSK-Ablgeich-Autoren-Auftrag
                            // leeren, weil dieser nun für die nächste KG verwendet wird
                            updatePSKDataAuthorOrder.setDescription("", DBActionOrigin.FROM_EDIT);
                        }
                        return;
                    }
                    if (isCancelled.getValue()) {
                        showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                        return;
                    }

                    // Step 3: Bilde Map aus den HM/M/SM-IDs (Dummy-ID bei Nicht-DIALOG-Ziel-Produkt) zu Retail-Stücklisteneinträgen
                    Map<HmMSmId, List<TURetailElem>> bcteUsageMap = new HashMap<>();
                    showMsg(TranslationHandler.translate("!!Analyse der Retail-TUs."));
                    if (!analyzeSourceAssemblies(destinationProduct, copyList, bcteUsageMap)) {
                        return;
                    }

                    // Step 4: Prüfe, ob die ermittelten „BCTE“-Schlüssel im Status "dokumentiert" oder „offen“ sind
                    // (nur für DIALOG-Ziel-Produkt)
                    Map<HmMSmId, EtkDataAssembly> constructionAssemblyMap = new HashMap<>();
                    if (!checkConstructionAssemblies(destinationProduct, bcteUsageMap, constructionAssemblyMap)) {
                        return;
                    }

                    // Step 5: alles für die Übernahme nach AS vorbereiten
                    Map<HmMSmId, ModuleTransferElem> constToRetailMap = new HashMap<>();
                    if (!prepareForTransfer(destinationProduct, bcteUsageMap, constructionAssemblyMap,
                                            constToRetailMap)) {
                        return;
                    }
                    if (isCancelled.getValue()) {
                        showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                        return;
                    }

                    // Step 6: verbliebene Stücklisteneinträge nach AS transferrieren
                    result.setValue(transferToAS(destinationProduct, constToRetailMap, constructionAssemblyMap));
                    if (result.getValue() < 0) {
                        hasErrors = true;
                        result.setValue(0);
                    }
                } catch (Exception e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    hasErrors = true;
                } finally {
                    finishedWithErrors = hasErrors;
                    showEndMsg();
                }
            }

            private void showEndMsg() {
                String kgProductString = getKGProductString();
                String msg;
                if (updatePSKModules) {
                    if (finishedWithErrors) {
                        msg = TranslationHandler.translate("!!Abgleich%1 mit Fehlern beendet.", kgProductString);
                    } else if (isCancelled.getValue()) {
                        msg = TranslationHandler.translate("!!Abgleich abgebrochen.");
                    } else {
                        msg = TranslationHandler.translate("!!Abgleich%1 beendet.", kgProductString);
                    }
                } else {
                    if (finishedWithErrors) {
                        msg = TranslationHandler.translate("!!Übernahme%1 mit Fehlern beendet.", kgProductString);
                    } else if (isCancelled.getValue()) {
                        msg = TranslationHandler.translate("!!Übernahme abgebrochen.");
                    } else {
                        msg = TranslationHandler.translate("!!Übernahme%1 beendet. %2 %3 erstellt.",
                                                           kgProductString, String.valueOf(result.getValue()),
                                                           (result.getValue() == 1) ? "TU" : "TUs");
                    }
                }

                EtkMessageLog messageLog = getMessageLogForExecution();
                if (messageLog != null) {
                    messageLog.fireMessageWithSeparators(msg, MessageLogOption.TIME_STAMP);
                }

                if (isLastKG) {
                    if ((messageLogForm != null) && (sessionForGUI != null)) {
                        // threadSafe in einem neuen Thread der GUI-Session aufrufen
                        sessionForGUI.invokeThreadSafeWithThread(() -> {
                            // Nach Beenden vom Kopieren/Abgleich von der letzten KG ist keine Ausführung im Hintergrund mehr möglich
                            GuiButtonOnPanel buttonBackgroundExecutionLocal = messageLogForm.getButtonPanel().getCustomButtonOnPanel(EXECUTE_IN_BACKGROUND_TEXT);
                            if (buttonBackgroundExecutionLocal != null) {
                                buttonBackgroundExecutionLocal.setVisible(false);
                            }

                            // Der Abbrechen-Button muss nach dem Beenden vom Kopieren/Abgleich von der letzten KG ausgeblendet
                            // und stattdessen der Schließen-Button angezeigt werden
                            messageLogForm.getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setVisible(false);
                            messageLogForm.getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE).setVisible(true);
                        });
                    }
                }

                markAsFinished(isCancelled.getValue());
            }
        };
        return runnable;
    }

    public int addAssembliesToJob(final iPartsProduct destinationProduct, final Collection<TUCopyContainer> copyList, String title,
                                  String subTitle) {
        if (!copyList.isEmpty()) {
            boolean useExistingMessageLogForm = messageLogForm != null;
            if (messageLogForExecution == null) {
                messageLogForm = createMessageForm(title, subTitle);
                messageLogForExecution = messageLogForm.getMessageLog();
            }

            // Beim Abbruch muss isCancelled gesetzt werden und es muss auf den Button "Im Hintergrund ausführen" reagiert
            // werden
            if (messageLogForm != null) {
                cancelListener = () -> isCancelled.setValue(true);
                messageLogForm.addCancelListener(cancelListener);

                refreshButtonBackgroundExecution();
            }

            final VarParam<Integer> result = new VarParam<>(0);
            if (logFile == null) {
                logFile = iPartsJobsManager.getInstance().addDefaultLogFileToMessageLog(getMessageLogForExecution(),
                                                                                        TranslationHandler.translateForLanguage(title, iPartsConst.LOG_FILES_LANGUAGE),
                                                                                        iPartsPlugin.LOG_CHANNEL_DEBUG);
            }

            FrameworkRunnable runnable = createJobRunnable(destinationProduct, copyList, title, result);

            if (useExistingMessageLogForm || executedInBackground || (sessionForGUI == null)) {
                if (sessionForExecution != null) {
                    sessionForExecution.runInSession(() -> runnable.run(null));
                } else if (sessionForGUI != null) {
                    sessionForGUI.runInSession(() -> runnable.run(null));
                } else {
                    runnable.run(null); // Kann eigentlich nicht passieren
                }
            } else {
                // MessageLogForm in einem neuen GUI-Session-Thread anzeigen, weil die Verarbeitung ansonsten nach der ersten
                // KG stoppen würde und auf das Schließen vom modalen Fenster gewartet werden würde
                sessionForGUI.invokeThreadSafeWithThread(() -> {
                    if (messageLogForm != null) {
                        messageLogForm.showModal(runnable);
                    }
                });
            }

            return result.getValue();
        } else {
            markAsFinished(true);
            return 0;
        }
    }

    private String getKGProductString() {
        String kgProductString = (StrUtils.isValid(kgForMessageLog) ? " " + TranslationHandler.translate("!!für KG %1",
                                                                                                         kgForMessageLog) : "")
                                 + " " + ((targetProductId != null) ? TranslationHandler.translate("!!im Produkt %1",
                                                                                                   targetProductId.getProductNumber()) : "");
        return kgProductString;
    }

    private void markAsFinished(boolean cancelled) {
        isCancelled.setValue(cancelled);
        synchronized (finished) {
            finished.setValue(true);
            finished.notifyAll();
        }
    }

    /**
     * Step 2: Relevante TUs zusammensuchen inkl. Anzeige/Bestätigung bei bereits vorhandenen TUs
     *
     * @param copyList
     * @param destinationProduct
     * @return
     */
    private boolean checkUsage(Collection<TUCopyContainer> copyList, iPartsProduct destinationProduct) {
        if (updatePSKModules) {
            // Pro ZielModul prüfen, ob ein Quell-Modul gefunden wurde
            boolean validPSKModuleFound = false;
            Iterator<TUCopyContainer> copyContainerIterator = copyList.iterator();
            while (copyContainerIterator.hasNext()) {
                TUCopyContainer copyContainer = copyContainerIterator.next();
                if (copyContainer.getSourceAssemblyId() != null) {
                    // Überprüfen, dass das PSK-Modul keine PSK-Doku-Methode hat (dann wurde das PSK-Modul nicht aus einem
                    // Serien-Modul erzeugt)
                    EtkDataAssembly pskAssembly = EtkDataObjectFactory.createDataAssembly(project, copyContainer.getDestinationAssemblyId());
                    if (pskAssembly instanceof iPartsDataAssembly) {
                        iPartsDocumentationType documentationType = ((iPartsDataAssembly)pskAssembly).getDocumentationType();
                        if (documentationType.isPSKDocumentationType()) {
                            String msg = TranslationHandler.translate("!!PSK-TU \"%1\" wird übersprungen, da er die Dokumentationsmethode \"%2\" hat.",
                                                                      copyContainer.getDestinationAssemblyId().getKVari(),
                                                                      documentationType.getExportValue());
                            showWarning(msg);
                            addMsgToUpdatePSKDataAuthorOrder(msg);
                            copyContainerIterator.remove();
                            continue;
                        }
                    }

                    // Überprüfen, ob das PSK-Modul gerade in Bearbeitung ist
                    iPartsDataAuthorOrderList authorOrderList = EditModuleHelper.getActiveAuthorOrderListForModule(copyContainer.getDestinationAssemblyId(),
                                                                                                                   getProject());
                    if (!authorOrderList.isEmpty()) {
                        String msg = "!!PSK-TU \"%1\" wird übersprungen, da er gerade in einem nicht freigegebenen Autoren-Auftrag (%2) in Bearbeitung ist.";
                        if (authorOrderList.size() > 1) {
                            msg = "!!PSK-TU \"%1\" wird übersprungen, da er gerade in nicht freigegebenen Autoren-Aufträgen (%2, ...) in Bearbeitung ist.";
                        }
                        msg = TranslationHandler.translate(msg, copyContainer.getDestinationAssemblyId().getKVari(), authorOrderList.get(0).getAuthorOrderName());
                        showWarning(msg);
                        addMsgToUpdatePSKDataAuthorOrder(msg);
                        copyContainerIterator.remove();
                        continue;
                    }

                    // PSK-Modul ist gültig
                    validPSKModuleFound = true;
                    String msg = TranslationHandler.translate("!!PSK-TU \"%1\" wird mit dem Serien-TU \"%2\" abgeglichen.",
                                                              copyContainer.getDestinationAssemblyId().getKVari(),
                                                              copyContainer.getSourceAssemblyId().getKVari());
                    showMsg(msg);
                    addMsgToUpdatePSKDataAuthorOrder(msg);
                } else {
                    String msg = TranslationHandler.translate("!!Kein Serien-TU gefunden für den Abgleich mit dem PSK-TU \"%1\".",
                                                              copyContainer.getDestinationAssemblyId().getKVari());
                    showWarning(msg);
                    addMsgToUpdatePSKDataAuthorOrder(msg);
                    copyContainerIterator.remove();
                }
            }

            return validPSKModuleFound;
        }

        List<TUCopyContainer> destinationCopyContainerList = buildDestinationCopyContainer(destinationProduct, copyList);
        List<AssemblyId> existingDestinations = new DwList<>();
        List<String> existingReserved = new DwList<>();
        List<KgTuId> noKgTuNodeList = new DwList<>();
        Iterator<TUCopyContainer> iterator = copyList.iterator();
        // alle bereits im Ziel vorhandenen oder in einem ChangeSet angelegten TU's aus der copyList entfernen
        while (iterator.hasNext()) {
            TUCopyContainer sourceCopyContainer = iterator.next();
            if (containsKgTu(sourceCopyContainer, destinationCopyContainerList)) {
                existingDestinations.add(sourceCopyContainer.getSourceAssemblyId());
                iterator.remove();
            } else {
                List<String> messages = new DwList<>();
                if (EditModuleHelper.isStandardModuleInReservedPK(getProject(), destinationProduct.getAsId(), sourceCopyContainer.kgTuId, false, messages)) {
                    existingReserved.add(messages.get(0));
                    iterator.remove();
                }
            }
        }
        if (copyList.isEmpty()) {
            showMsg(TranslationHandler.translate("!!Im Ziel-Produkt \"%1\" sind bereits alle TU-Knoten belegt.",
                                                 destinationProduct.getAsId().getProductNumber()));
            return false;
        }

        // Falls das Ziel-Produkt ein PSK-Produkt ist, soll nicht gegen das Template geprüft werden
        if (!targetIsPSKProduct) {
            Map<String, KgTuListItem> kgtuCacheEntry = KgTuHelper.getKGTUStructure(getProject(), destinationProduct.getAsId());
            EditTransferToASHelper transferHelper = new EditTransferToASHelper(getProject(), null, null);
            iterator = copyList.iterator();
            while (iterator.hasNext()) {
                TUCopyContainer sourceCopyContainer = iterator.next();
                KgTuId kgTuId = sourceCopyContainer.getKgTuId();
                KgTuListItem kgTuListItem = transferHelper.getKgTuListItem(kgTuId, kgtuCacheEntry);
                if (kgTuListItem == null) {
                    noKgTuNodeList.add(sourceCopyContainer.getKgTuId());
                    iterator.remove();
                }
            }
            if (copyList.isEmpty()) {
                if (!noKgTuNodeList.isEmpty()) {
                    StringBuilder str = new StringBuilder();
                    appendNoKgTuList(destinationProduct, noKgTuNodeList, str);
                } else {
                    showMsg(TranslationHandler.translate("!!Im Ziel-Produkt \"%1\" können die geforderten TU-Knoten nicht angelegt werden.",
                                                         destinationProduct.getAsId().getProductNumber()));
                }
                return false;
            }
        }

        // Ausgabe vorbereiten
        StringBuilder str = new StringBuilder();
        if (!existingDestinations.isEmpty()) {
            String msg = TranslationHandler.translate("!!Folgende TUs sind im Produkt \"%1\" bereits vorhanden:", destinationProduct.getAsId().getProductNumber());
            showMsg(msg);
            str.append(msg);
            str.append(OsUtils.NEWLINE);
            str.append(buildAssemblyList(existingDestinations));
            str.append(OsUtils.NEWLINE);
        }
        if (!existingReserved.isEmpty()) {
            String msg = TranslationHandler.translate("!!Folgende TUs sind im Produkt \"%1\" bereits belegt:", destinationProduct.getAsId().getProductNumber());
            showMsg(msg);
            str.append(msg);
            str.append(OsUtils.NEWLINE);
            for (String text : existingReserved) {
                str.append(text);
                showMsg("  " + text);
                str.append(OsUtils.NEWLINE);
            }
        }
        appendNoKgTuList(destinationProduct, noKgTuNodeList, str);
        if (str.length() > 0) {
            List<AssemblyId> assemblyIdList = new DwList<>();
            for (TUCopyContainer copyContainer : copyList) {
                assemblyIdList.add(copyContainer.getSourceAssemblyId());
            }
            if (!showContinueDialog(str, assemblyIdList,
                                    destinationProduct.getAsId().getProductNumber(),
                                    null, "!!Es wird nur der folgende TU in das Produkt \"%1\" kopiert:",
                                    "!!Es werden nur die folgenden TUs in das Produkt \"%1\" kopiert:",
                                    "!!Trotzdem fortsetzen?")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verschiedene Status-Meldungen als Beschreibung in den Autoren-Auftrag aufnehmen
     *
     * @param msg
     */
    private void addMsgToUpdatePSKDataAuthorOrder(String msg) {
        if (updatePSKDataAuthorOrder != null) {
            String text = updatePSKDataAuthorOrder.getDescription();
            if (!text.isEmpty()) {
                text += "\n";
            }
            updatePSKDataAuthorOrder.setDescription(text + msg, DBActionOrigin.FROM_EDIT);
        }
    }

    private String buildPartListEntryList(List<PartListEntryId> partListEntryIdList, boolean onlyLog) {
        Set<String> idList = new TreeSet<>();
        for (PartListEntryId partListEntryId : partListEntryIdList) {
            idList.add(partListEntryId.getKLfdnr());
        }
        return buildNumberList(idList, onlyLog);
    }

    /**
     * Assembly-Id-Liste für die Ausgabe umformatieren
     *
     * @param assemblyIdList
     * @return
     */
    private String buildAssemblyList(List<AssemblyId> assemblyIdList) {
        Set<String> idList = new TreeSet<>();
        for (AssemblyId assemblyId : assemblyIdList) {
            idList.add(assemblyId.getKVari());
        }
        return buildNumberList(idList);
    }

    private String buildKgTuList(List<KgTuId> kgTuIdList) {
        Set<String> idList = new TreeSet<>();
        for (KgTuId kgTuId : kgTuIdList) {
            idList.add(kgTuId.toString("/"));
        }
        return buildNumberList(idList);
    }

    private String buildNumberList(Collection<String> idList) {
        return buildNumberList(idList, false);
    }

    private String buildNumberList(Collection<String> idList, boolean onlyLog) {
        StringBuilder str = new StringBuilder();
        int lfdNr = 0;
        StringBuilder strId = new StringBuilder();
        for (String idString : idList) {
            if ((lfdNr % 4) == 0) {
                if (strId.length() > 0) {
                    str.append(strId);
                    if (onlyLog) {
                        logMsg("  " + strId);
                    } else {
                        showMsg("  " + strId);
                    }
                    strId = new StringBuilder();
                    str.append(OsUtils.NEWLINE);
                }
            }
            if (strId.length() > 0) {
                strId.append(", ");
            }
            strId.append(idString);
            lfdNr++;
        }
        if (strId.length() > 0) {
            str.append(strId);
            if (onlyLog) {
                logMsg("  " + strId);
            } else {
                showMsg("  " + strId);
            }
            str.append(OsUtils.NEWLINE);
        }
        return str.toString();
    }

    /**
     * copyList für die Ausgabe umformatieren
     *
     * @param copyList
     * @return
     */
    private String buildExtraAssemblyList(List<TUCopyContainer> copyList) {
        List<AssemblyId> assemblyIdList = new DwList<>();
        for (TUCopyContainer copyContainer : copyList) {
            assemblyIdList.add(copyContainer.getSourceAssemblyId());
        }
        return buildAssemblyList(assemblyIdList);
    }

    /**
     * sucht, ob es in der Destination TU-Liste den Source-TU gibt
     *
     * @param sourceCopyContainer
     * @param destinationCopyContainerList
     * @return
     */
    private boolean containsKgTu(TUCopyContainer sourceCopyContainer, List<TUCopyContainer> destinationCopyContainerList) {
        for (TUCopyContainer tuCopyContainer : destinationCopyContainerList) {
            if (tuCopyContainer.isKgTuEqual(sourceCopyContainer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Im Ziel-Produkt die vorhandenen TUs bei den angeforderten KG/TUs bestimmen
     *
     * @param destinationProduct
     * @param sourceCopyContainer
     * @return
     */
    private List<TUCopyContainer> buildDestinationCopyContainer(iPartsProduct destinationProduct, Collection<TUCopyContainer> sourceCopyContainer) {
        List<TUCopyContainer> result = new DwList<>();
        if (sourceCopyContainer.isEmpty()) {
            return result;
        }

        KgTuId searchKgTuId;
        TUCopyContainer firstSourceCopyContainer = sourceCopyContainer.iterator().next();
        if (sourceCopyContainer.size() > 1) {
            searchKgTuId = new KgTuId(firstSourceCopyContainer.getKgTuId().getKg(), "");
        } else {
            searchKgTuId = firstSourceCopyContainer.getKgTuId();
        }
        iPartsDataModuleEinPASList dataModuleEinPASList = iPartsDataModuleEinPASList.loadForKgTu(getProject(), destinationProduct.getAsId(), searchKgTuId);
        for (iPartsDataModuleEinPAS dataModuleEinPAS : dataModuleEinPASList) {
            TUCopyContainer copyContainer = new TUCopyContainer(dataModuleEinPAS);
            result.add(copyContainer);
        }
        return result;
    }

    private iPartsDataModuleEinPAS findTUinModuleEinPasList(iPartsDataModuleEinPASList dataModuleEinPASList, AssemblyId assemblyId) {
        for (iPartsDataModuleEinPAS dataModuleEinPAS : dataModuleEinPASList) {
            if (dataModuleEinPAS.getAsId().getModuleNumber().equals(assemblyId.getKVari())) {
                return dataModuleEinPAS;
            }
        }
        return null;
    }

    /**
     * Step 1: bestimme Ziel-Product
     *
     * @return
     */
    private iPartsProduct askForTargetProduct() {
        iPartsProduct sourceProduct = iPartsProduct.getInstance(getProject(), sourceProductId);
        iPartsSeriesId referencedSeriesId = sourceProduct.getReferencedSeries();
        boolean seriesIsValid = (referencedSeriesId != null) && referencedSeriesId.isValidId();
        List<iPartsProduct> productList = new DwList<>();
        if (sourceProduct.getDocumentationType().isPKWDocumentationType()) {
            if (seriesIsValid) {
                productList = iPartsProduct.getAllProductsForReferencedSeries(getProject(), referencedSeriesId);
            }
        } else {
            // Bei Trucks alle Produkte auflisten, die keine PKW-Dokumethode haben
            List<iPartsProduct> allProductsList = iPartsProduct.getAllProducts(getProject());
            for (iPartsProduct product : allProductsList) {
                // Filterung nach Dokumethode
                if (!product.getDocumentationType().isPKWDocumentationType()) {
                    productList.add(product);
                }
            }
        }

        copyPSKProductToPSKProduct = sourceProduct.isPSK() && targetIsPSKProduct;
        List<iPartsProduct> products = new DwList<>();
        for (iPartsProduct product : productList) {
            // Quell-Produkt entfernen und Filterung nach PSK-Flag
            if (product.getAsId().equals(sourceProductId) || (product.isPSK() != targetIsPSKProduct)) {
                continue;
            }

            if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
                if (copyPSKProductToPSKProduct) {
                    // Bei dem Kopieren von PSK auf PSK sollen als Ziel-Produkt die Produkte mit derselben Doku-Methode
                    // angeboten werden.
                    if (product.getDocumentationType() == sourceProduct.getDocumentationType()) {
                        products.add(product);
                    }
                } else {
                    // Dokumethoden aus Quell- und Ziel-Produkt müssen kompatibel sein
                    // Wenn das Produkt keinen Dokumentationstyp hat, dann wird der Modultyp später anahnd der Herkunft bestimmt
                    if (product.getDocumentationType().isSameOverallDocType(sourceProduct.getDocumentationType()) || (product.getDocumentationType() == iPartsDocumentationType.UNKNOWN)) {
                        products.add(product);
                    }
                }
            }
        }

        String title = TranslationHandler.translate(targetIsPSKProduct ? "!!PSK-Produkt auswählen" : "!!Produkt auswählen");
        iPartsProduct targetProduct = null;
        if (products.isEmpty()) {
            if (seriesIsValid) {
                // Für diese Baureihe gibt es kein Produkt -> Da muss erst ein Produkt vom Adminredakteur angelegt werden
                MessageDialog.show(TranslationHandler.translate("!!Für die Baureihe \"%1\" existiert kein weiteres passendes Produkt. " +
                                                                "Bitte erst ein passendes Produkt anlegen.",
                                                                referencedSeriesId.getSeriesNumber()), title);
            } else {
                MessageDialog.show(TranslationHandler.translate("!!Es existiert kein weiteres passendes Produkt. Bitte erst ein passendes Produkt anlegen."),
                                   title);
            }
        } else if (products.size() == 1) {
            targetProductId = products.get(0).getAsId();
            targetProduct = iPartsProduct.getInstance(getProject(), targetProductId);
            String productTitle = targetProduct.getProductTitle(getProject()).getTextByNearestLanguage(getProject().getDBLanguage(), getProject().getDataBaseFallbackLanguages());
            if (MessageDialog.showYesNo(TranslationHandler.translate("!!Es wurde nur das passende Produkt \"%1\" gefunden. Dorthin kopieren?",
                                                                     productTitle), title) != ModalResult.YES) {
                targetProductId = null;
                return null;
            }
        } else { // kein oder mehrere EinPAS-/KG/TU-Produkte gefunden -> Produktauswahldialog für EinPAS-KG/TU-Produkte
            // Titel zusammenbauen
            EnumSet<iPartsConst.PRODUCT_STRUCTURING_TYPE> structuringTypes = EnumSet.noneOf(iPartsConst.PRODUCT_STRUCTURING_TYPE.class);
            for (iPartsProduct product : products) {
                structuringTypes = EnumUtils.plus(structuringTypes, product.getProductStructuringType());
            }
            if (seriesIsValid) {
                title += OsUtils.NEWLINE;
                title += TranslationHandler.translate("!!Einschränkung auf referenzierte Baureihe \"%1\"", referencedSeriesId.getSeriesNumber());
            }

            final List<iPartsProductId> productIdList = new DwList<>();
            for (iPartsProduct product : products) {
                productIdList.add(product.getAsId());
            }
            // nur gültige Produkte passend zur Baureihe zulassen
            OnValidateAttributesEvent onValidateAttributesEvent = attributes -> {
                // nur gültige Produkte passend zur Baureihe zulassen
                String productNumber = attributes.getField(iPartsConst.FIELD_DP_PRODUCT_NO).getAsString();
                if (productIdList.contains(new iPartsProductId(productNumber))) {
                    return true;
                }
                return false;
            };
            SelectSearchGridProduct selectSearchGridProduct = new SelectSearchGridProduct(connector.getActiveForm());
            selectSearchGridProduct.setTitle(title);
            selectSearchGridProduct.setOnValidateAttributesEvent(onValidateAttributesEvent);
            int maxResults = SelectSearchGridProduct.getMaxSelectResultSize(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE, false);
            selectSearchGridProduct.setMaxResults(maxResults);
            if (seriesIsValid) {
                selectSearchGridProduct.setFilterFieldNames(new String[]{ iPartsConst.FIELD_DP_STRUCTURING_TYPE, iPartsConst.FIELD_DP_SERIES_REF });
                selectSearchGridProduct.setFilterValues(new String[]{ iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU.name(), referencedSeriesId.getSeriesNumber() });
            } else {
                selectSearchGridProduct.setFilterFieldNames(new String[]{ iPartsConst.FIELD_DP_STRUCTURING_TYPE });
                selectSearchGridProduct.setFilterValues(new String[]{ iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU.name() });
            }
            String productNo = selectSearchGridProduct.showGridSelectionDialog("*");
            if (!productNo.isEmpty()) {
                targetProduct = iPartsProduct.getInstance(getProject(), new iPartsProductId(productNo));
                targetProductId = targetProduct.getAsId();
                if (sourceProduct.getDocumentationType().isPKWDocumentationType()) {
                    Set<String> aaSet = targetProduct.getAAsFromModels(getProject());
                    String aaString = StrUtils.stringListToString(targetProduct.getAAsFromModels(getProject()), ",");
                    String message = "!!Ausgewähltes Ziel-Produkt: \"%1\" mit den Ausführungsarten (%2)";
                    if (aaSet.size() == 1) {
                        message = "!!Ausgewähltes Ziel-Produkt: \"%1\" mit der Ausführungsart (%2)";
                    }
                    showMsg(TranslationHandler.translate(message, targetProductId.getProductNumber(), aaString));
                } else {
                    showMsg(TranslationHandler.translate("!!Ausgewähltes Ziel-Produkt: \"%1\"", targetProductId.getProductNumber()));
                }
            } else {
                showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
            }
        }

        if (targetProduct != null) {
            if (copyPSKProductToPSKProduct) {
                Set<String> sourceVariants = sourceProduct.getProductVariantsIds(project);
                Set<String> targetVariants = targetProduct.getProductVariantsIds(project);
                if (!targetVariants.containsAll(sourceVariants)) {
                    sourceVariants = new TreeSet<>(sourceVariants);
                    sourceVariants.removeAll(targetVariants);
                    String msg = TranslationHandler.translate("!!Im Ziel-Produkt \"%1\" fehlen die folgenden Varianten des Quell-Produkts \"%2\":",
                                                              targetProduct.getAsId().getProductNumber(), sourceProduct.getAsId().getProductNumber()) +
                                 "\n" + StrUtils.stringListToString(sourceVariants, ", ") +
                                 "\n\n" + TranslationHandler.translate("!!Es werden ggf. nicht alle Variantengültigkeiten übernommen.") +
                                 "\n" + TranslationHandler.translate("!!Trotzdem kopieren?");
                    filterPSKVariantIds = true;
                    if (MessageDialog.showYesNo(msg, TITLE_PSK) != ModalResult.YES) {
                        targetProductId = null;
                        return null;
                    }
                }
            }
        }
        return targetProduct;
    }

    /**
     * Step 3: Bilde Map aus den Retail-Stücklisteneinträgen zu Konstruktions-Modulen bzw. Dummy für Nicht-DIALOG-Ziel-Produkte
     *
     * @param destinationProduct
     * @param copyList
     * @param bcteUsageMap
     * @return
     */
    private boolean analyzeSourceAssemblies(iPartsProduct destinationProduct, Collection<TUCopyContainer> copyList, Map<HmMSmId,
            List<TURetailElem>> bcteUsageMap) {
        int maxPos = copyList.size();
        boolean showProgress = maxPos > 2;
        int pos = 0;

        if (showProgress) {
            fireProgress(pos, maxPos);
        }
        ASUsageHelper asUsageHelper = new ASUsageHelper(getProject());
        for (TUCopyContainer copyContainer : copyList) {
            if (isCancelled.getValue()) {
                showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                return false;
            }
            showMsg(TranslationHandler.translate("!!Analysiere Retail-TU \"%1\"", copyContainer.getSourceAssemblyId().getKVari()));
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), copyContainer.getSourceAssemblyId());
            DBDataObjectList<EtkDataPartListEntry> partListEntryList = assembly.getPartListUnfiltered(assembly.getEbene());
            boolean isPKWSourceProduct = false; // PKW statt DIALOG, da Quelle auch ein PSK-PKW-Produkt sein könnte
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                isPKWSourceProduct = iPartsAssembly.getDocumentationType().isPKWDocumentationType();

                // Für bessere Performance beim Kopieren, wobei clearAllDataCombTextListsForPartList() am Ende nicht aufgerufen
                // werden muss, da assembly sowieso nur temporär existiert
                iPartsAssembly.loadAllDataCombTextListsForPartList();
            }
            boolean isDIALOGDestinationProduct = destinationProduct.getDocumentationType().isDIALOGDocumentationType();
            Map<HmMSmId, List<TURetailElem>> bcteUsageModuleMap = analyzeOneSourceAssembly(asUsageHelper,
                                                                                           partListEntryList,
                                                                                           copyContainer,
                                                                                           isPKWSourceProduct,
                                                                                           isDIALOGDestinationProduct);
            if (!bcteUsageModuleMap.isEmpty()) {
                for (Map.Entry<HmMSmId, List<TURetailElem>> entry : bcteUsageModuleMap.entrySet()) {
                    HmMSmId hmMSmId;
                    if (isDIALOGDestinationProduct) {
                        hmMSmId = entry.getKey();
                    } else {
                        hmMSmId = new HmMSmId(); // Dummy HM/M/SM-Id für Nicht-DIALOG-Ziel-Produkte
                    }
                    List<TURetailElem> retailElemList = bcteUsageMap.computeIfAbsent(hmMSmId, id -> new DwList<>());
                    for (TURetailElem retailElem : entry.getValue()) {
                        if (retailElem.isInit()) {
                            retailElemList.add(retailElem);
                        }
                    }
                }
            }
            pos++;
            if (showProgress) {
                fireProgress(pos, maxPos);
            }
        }
        if (showProgress) {
            hideProgress();
        }
        if (bcteUsageMap.isEmpty()) {
            showMsg(TranslationHandler.translate("!!Keine Stücklisteneinträge zum Kopieren gefunden"));
        }
        return !bcteUsageMap.isEmpty();
    }

    private Map<HmMSmId, List<TURetailElem>> analyzeOneSourceAssembly(ASUsageHelper asUsageHelper,
                                                                      DBDataObjectList<EtkDataPartListEntry> partListEntryList,
                                                                      TUCopyContainer copyContainer, boolean isPKWSourceProduct,
                                                                      boolean isDIALOGDestinationProduct) {
        AssemblyId sourceAssemblyId = copyContainer.getSourceAssemblyId();
        Map<HmMSmId, List<TURetailElem>> bcteUsageMap = new HashMap<>();
        if (partListEntryList.isEmpty()) {
            showMsg(TranslationHandler.translate("!!Der Retail-TU \"%1\" besitzt keine Stückliste.", sourceAssemblyId.getKVari()));
            return bcteUsageMap;
        }
        Map<String, List<TURetailElem>> checkAAMap = new HashMap<>();
        List<PartListEntryId> invalidBcteKeyList = new DwList<>();
        List<PartListEntryId> invalidHotSpotList = new DwList<>();
        boolean hasDuplicateKeys = false;
        for (EtkDataPartListEntry partListEntry : partListEntryList) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                TURetailElem retailElem = new TURetailElem((iPartsDataPartListEntry)partListEntry, copyContainer.getKgTuId(),
                                                           isPKWSourceProduct);
                if (retailElem.isInit()) {
                    if (updatePSKModules) {
                        retailElem.setDestinationAssemblyId(copyContainer.getDestinationAssemblyId());
                    }
                    List<TURetailElem> retailElemList = bcteUsageMap.get(retailElem.getSourceHmMSmId());
                    if (retailElemList == null) {
                        retailElemList = new DwList<>();
                        bcteUsageMap.put(retailElem.getSourceHmMSmId(), retailElemList);
                    }
                    retailElemList.add(retailElem);

                    // Überprüfung hotSpot
                    String hotspot = retailElem.getSourceHotSpot();
                    if (!StrUtils.isValid(hotspot)) {
                        invalidHotSpotList.add(partListEntry.getAsId());
                    }

                    if (isDIALOGDestinationProduct) {
                        // Aufsammeln der bcteKey ohne AA für die Auswertung
                        String aaNeutralBCTEGUID = retailElem.getBcteGUIDWithoutAAandHotSpot();
                        List<TURetailElem> idList = checkAAMap.get(aaNeutralBCTEGUID);
                        if (idList == null) {
                            idList = new DwList<>();
                            checkAAMap.put(aaNeutralBCTEGUID, idList);
                        }
                        idList.add(retailElem);
                        if (idList.size() > 1) {
                            hasDuplicateKeys = true;
                        }
                    }

                    // DAIMLER-9337: Übernahme der restlichen Daten Fußnoten-Vorbereitung
                    if (isDIALOGDestinationProduct && (retailElem.partListEntry.getFootNotes() != null)) {
                        List<PartListEntryId> usedInASList = asUsageHelper.getPartListEntryIdsUsedInAS(retailElem.bcteKey);
                        if (usedInASList.size() <= 1) {
                            retailElem.copyFootNotes = true;
                        } else {
                            showWarning(TranslationHandler.translate("!!Der Stücklisteneintrag \"%1\" wird bereits mehrfach in AS benutzt. Deswegen werden die Fußnoten nicht kopiert.",
                                                                     retailElem.partListEntry.getAsId().toStringForLogMessages()));
                        }
                    }
                } else {
                    if (retailElem.bcteKey == null) {
                        invalidBcteKeyList.add(partListEntry.getAsId());
                    }
                }
            }
        }
        if (hasDuplicateKeys) {
            // hier Fehlerauswertung bzgl identischer BCTE-Keys ohne AA
            logWarning(TranslationHandler.translate("!!Im Retail-TU \"%1\" werden im gleichen Hotspot gleiche DIALOG-Schlüssel benutzt!",
                                                    sourceAssemblyId.getKVari()));
            for (Map.Entry<String, List<TURetailElem>> entry : checkAAMap.entrySet()) {
                if (entry.getValue().size() <= 1) {
                    continue;
                }
                composeMsgForSameBcteKey(entry.getValue());
                // alle transferElems außer dem ersten ungültig setzen
                for (int lfdNr = 1; lfdNr < entry.getValue().size(); lfdNr++) {
                    entry.getValue().get(lfdNr).partListEntry = null;
                }
                logWarning(TranslationHandler.translate("!!Es wird nur der erste Stücklisteneintrag %1 Hotspot (%2) übernommen.",
                                                        entry.getValue().get(0).partListEntry.getAsId().getKLfdnr(),
                                                        entry.getValue().get(0).getSourceHotSpot()));
            }
        }

        if (!invalidBcteKeyList.isEmpty()) {
            showWarning(TranslationHandler.translate("!!Im Retail-TU \"%1\" besitzen die folgenden Stücklisteneinträge keinen oder einen ungültigen BCTE-Schlüssel. Diese werden ignoriert!",
                                                     sourceAssemblyId.getKVari()));
            // show kLfdNrn
            buildPartListEntryList(invalidBcteKeyList, false);
        }

        if (!invalidHotSpotList.isEmpty()) {
            showWarning(TranslationHandler.translate("!!Im Retail-TU \"%1\" ist bei folgenden Stücklisteneinträgen kein Hotspot gesetzt. Dieser wird automatisch vergeben!",
                                                     sourceAssemblyId.getKVari()));
            // show kLfdNrn
            buildPartListEntryList(invalidHotSpotList, false);
        }
        return bcteUsageMap;
    }

    private void composeMsgForSameBcteKey(List<TURetailElem> retailList) {
        for (TURetailElem retailElem : retailList) {
            logWarning("  " + TranslationHandler.translate("!!Der Stücklisteneintrag %1 Hotspot (%2) besitzt den DIALOG-Schlüssel %3",
                                                           retailElem.partListEntry.getAsId().getKLfdnr(),
                                                           retailElem.getSourceHotSpot(),
                                                           retailElem.bcteKey.createDialogGUID()));
        }
    }

    private EtkDataPartListEntry getConstPartListEntry(Map<HmMSmId, EtkDataAssembly> constructionAssemblyMap,
                                                       iPartsDialogBCTEPrimaryKey bcteKey) {
        EtkDataAssembly constAssembly = constructionAssemblyMap.get(bcteKey.getHmMSmId());
        if (constAssembly == null) {
            showError(TranslationHandler.translate("!!Konstruktion-Stückliste nicht gefunden %1!", bcteKey.getHmMSmId().toStringForLogMessages()));
            return null;
        }
        return constAssembly.getPartListEntryFromKLfdNr(bcteKey.createDialogGUID());
    }

    private int transferToAS(iPartsProduct destinationProduct,
                             Map<HmMSmId, ModuleTransferElem> constToRetailMap,
                             Map<HmMSmId, EtkDataAssembly> constructionAssemblyMap) {
        List<HmMSmId> constModules = new DwList<>(constToRetailMap.keySet());
        VarParam<Boolean> result = new VarParam(true);
        int newTUModuleCount = 0;
        if (getRevisionsHelper() == null) {
            showError(TranslationHandler.translate("!!RevisionHelper nicht vorhanden!"));
            return -1;
        }
        iPartsRevisionChangeSet changeSet = iPartsRevisionChangeSet.createTempChangeSet(getProject(), updatePSKModules ? iPartsChangeSetSource.UPDATE_PSK_TU
                                                                                                                       : iPartsChangeSetSource.COPY_TU);
        int maxPos = constModules.size();
        boolean showProgress = false; // im Runnable werden keine Progress und Messages ausgegeben
        int pos = 0;
        if (showProgress) {
            fireProgress(pos, maxPos);
        }
        Collection<AssemblyId> newModuleIds = new DwList<>();
        Set<AssemblyId> modifiedAssemblies = new HashSet<>();
        try {
            // Beim Abgleich von PSK-Modulen die SerializedDataObjects komprimieren nach Merges (speziell erst löschen,
            // dann neu erzeugen mit identischen Daten), damit das ChangeSet möglichst klein und übersichtlich wird
            if (updatePSKModules) {
                changeSet.setCompressSerializedDataObjectsAfterMerge(true);
            }

            boolean isDIALOGDestinationProduct = destinationProduct.getDocumentationType().isDIALOGDocumentationType();
            if (!isDIALOGDestinationProduct) {
                showMsg(updatePSKModules ? TranslationHandler.translate("!!Stücklisten werden mit Serien-TUs abgeglichen...")
                                         : TranslationHandler.translate("!!Stücklisten werden kopiert..."));

            }

            // Variablen nur setzen falls PSK Varianten gefiltert werden sollen
            // Es werden nur die im Ziel-Produkt vorhandenen Variantengültigkeit übernommen
            Set<String> pskVariantIdsIntersection = new HashSet<>();
            if (filterPSKVariantIds) {
                iPartsProduct sourceProduct = iPartsProduct.getInstance(project, sourceProductId);
                Set<String> sourceProductVariantIds = sourceProduct.getProductVariantsIds(project);
                Set<String> targetProductProductVariantsIds = destinationProduct.getProductVariantsIds(project);
                pskVariantIdsIntersection.addAll(sourceProductVariantIds);
                pskVariantIdsIntersection.retainAll(targetProductProductVariantsIds);
            }

            // über alle Konstruktions-Module
            for (HmMSmId hmMSmId : constModules) {
                if (isCancelled.getValue()) {
                    showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                    return -1;
                }
                if (isDIALOGDestinationProduct) {
                    showMsg(TranslationHandler.translate("Übernahme nach AS aus Konstruktions-Stückliste %1 (%2 von %3)",
                                                         hmMSmId.toStringForLogMessages(), String.valueOf(pos + 1), String.valueOf(maxPos)));
                }

                // die beiden Maps holen
                ModuleTransferElem currentTransferElem = constToRetailMap.remove(hmMSmId);
                // das Konstruktions Modul holen
                EtkDataAssembly sourceConstAssembly = constructionAssemblyMap.get(hmMSmId);
                // das Runnable für ein Konstruktions-Modul erzeugen (darin wird das ChangeSet auch aktiviert)
                Runnable transferToASRunnable = createRunnable(changeSet, currentTransferElem, sourceConstAssembly, hmMSmId,
                                                               isDIALOGDestinationProduct, pskVariantIdsIntersection, result);
                // ohne extern evtl. aktives ChangeSet das Runnable laufen lassen
                getRevisionsHelper().executeWithoutActiveChangeSets(transferToASRunnable, false, getProject());


                if (result.getValue()) {
                    // kein Fehler aufgetreten
                    if (!currentTransferElem.notExistingModuleMap.isEmpty()) {
                        // die vorher nicht vorhandenen TUs (wurden im Runnable erzeugt) auf die anderen Elemente der restlichen Map verteilen
                        for (Map.Entry<String, TransferToASElement> entry : currentTransferElem.notExistingModuleMap.entrySet()) {
                            AssemblyId assemblyId = entry.getValue().getAssemblyId();
                            newTUModuleCount++;
                            for (Map.Entry<HmMSmId, ModuleTransferElem> constEntry : constToRetailMap.entrySet()) {
                                constEntry.getValue().removeAssemblyId(assemblyId);
                            }
                            newModuleIds.add(assemblyId);
                        }
                    }
                    for (Map.Entry<String, List<TransferToASElement>> entry : currentTransferElem.moduleMap.entrySet()) {
                        if (!entry.getValue().isEmpty()) {
                            AssemblyId assemblyId = entry.getValue().get(0).getAssemblyId();
                            if (assemblyId != null) {
                                modifiedAssemblies.add(assemblyId);
                            }
                        }
                    }
                } else {
                    if (!isCancelled.getValue()) {
                        // Fehler im Runnable aufgetreten => Abbruch
                        newTUModuleCount = -1;
                    }
                    break;
                }
                pos++;
                if (showProgress) {
                    fireProgress(pos, maxPos);
                }
            }
        } catch (Exception e) {
            newTUModuleCount = -1;
            Logger.getLogger().throwRuntimeException(e);
        } finally {
            if (showProgress) {
                hideProgress();
            }

            if (newTUModuleCount >= 0) {
                if (!isCancelled.getValue()) {
                    showMsg(TranslationHandler.translate("!!Übernahme beendet"));
                }
            } else {
                showMsg(TranslationHandler.translate("!!Übernahme mit Fehlern beendet"));
            }

            // im Erfolgsfall das ChangeSet speichern und die Einträge in die DB speichern
            enableMessageLogButtons(false);
            if ((newTUModuleCount >= 0) && !isCancelled.getValue()) {
                showMsg(TranslationHandler.translate("!!Speichern..."));

                // Beim Abgleich von PSK-TUs sind unveränderte SerializedDataObjects untinteressant und sollen aus dem
                // ChangeSet für bessere Übersichtlichkeit entfernt werden
                if (updatePSKModules) {
                    changeSet.removeUnmodifiedSerializedDataObjects();
                }

                BeforeSaveChangeSetInterface beforeSaveChangeSet = createBeforeSaveChangeSet();
                Runnable afterSaveChangeSetRunnable = () -> EditModuleHelper.updateAllModuleSAAValiditiesForFilter(getProject(),
                                                                                                                   changeSet);

                if (changeSet.commit(true, false, false, beforeSaveChangeSet, afterSaveChangeSetRunnable)) { // Die Caches werden vom Aufrufer nach dem letzten KG gelöscht
                    boolean saveDataObjectsToDB = !iPartsChangeSetSource.isSpecialChangeSetSource(iPartsChangeSetSource.COPY_TU);
                    if (saveDataObjectsToDB) {
                        handleCachesAndNotifications(destinationProduct.getAsId(), newModuleIds, modifiedAssemblies);
                    }
                    iPartsChangeSetSource source = updatePSKModules ? iPartsChangeSetSource.UPDATE_PSK_TU
                                                                    : iPartsChangeSetSource.COPY_TU;
                    showMsg(TranslationHandler.translate("!!Alle Änderungen wurden in ein technisches Änderungsset \"%1\" geschrieben.",
                                                         AuthorOrderHistoryFormatter.getTechChangeSetName(project, source)));
                    showMsg(TranslationHandler.translate("!!Änderungsset-ID: %1", changeSet.getChangeSetId().getGUID()));
                    if (updatePSKDataAuthorOrder != null) {
                        showMsg(TranslationHandler.translate("!!Diese Änderungen sind im freigegebenen Autoren-Auftrag \"%1\" gespeichert.",
                                                             updatePSKDataAuthorOrder.getAuthorOrderName()));

                    }
                } else {
                    showError(TranslationHandler.translate("!!Fehler beim Speichern!"));
                    newTUModuleCount = -1;
                }
            } else {
                // Es ist ein Fehler aufgetreten => Reste löschen, da ChangeSet nicht gespeichert wird
                // Alle Berechnungen für die Auswertung von Teilepositionen löschen für das freigegebene ChangeSet
                iPartsDataReportConstNodeList.deleteAllDataForChangesetGuid(getProject(), changeSet.getChangeSetId().getGUID());

                // Alle Primärschlüssel-Reservierungen löschen für das freigegebene ChangeSet
                iPartsDataReservedPKList.deletePrimaryKeysForChangeSet(getProject(), changeSet.getChangeSetId());
            }
            enableMessageLogButtons(true);
        }
        return newTUModuleCount;
    }

    private void enableMessageLogButtons(boolean enabled) {
        if ((messageLogForm != null) && (sessionForGUI != null) && sessionForGUI.isActive()) {
            sessionForGUI.invokeThreadSafeInSessionThread(() -> messageLogForm.setButtonsEnabled(enabled));
        }
    }

    private BeforeSaveChangeSetInterface createBeforeSaveChangeSet() {
        if ((updatePSKDataAuthorOrder == null) || !updatePSKModules) {
            updatePSKDataAuthorOrder = null;
            return null;
        }

        return new BeforeSaveChangeSetInterface() {
            @Override
            public void beforeSaveChangeSet(EtkProject project, iPartsDataChangeSet dataChangeSet) {
                // Aktuellen Autoren-Auftrag mit ChangeSet verbinden, EndStatus setzen und speichern
                updatePSKDataAuthorOrder.setChangeSetId(dataChangeSet.getAsId());
                updatePSKDataAuthorOrder.changeStatus(iPartsAuthorOrderStatus.getEndState());
                String commitDate = dataChangeSet.getFieldValue(iPartsConst.FIELD_DCS_COMMIT_DATE);
                updatePSKDataAuthorOrder.setCommitDateForHistory(commitDate);

                // "PSK-Abgleich + Produkt + KG bzw. TU"
                String kgOrTuNumber;
                if (StrUtils.isValid(kgForMessageLog)) {
                    kgOrTuNumber = " (KG " + kgForMessageLog + ")";
                } else if (!targetAssemblyIds.isEmpty()) {
                    String tuModules = targetAssemblyIds.stream()
                            .map(targetAssemblyId -> targetAssemblyId.getKVari())
                            .collect(Collectors.joining(", "));
                    kgOrTuNumber = " (TU " + tuModules + ")";
                } else {
                    kgOrTuNumber = "";
                }
                String name = "PSK-Abgleich für PSK-Produkt \"" + getTargetProductId().getProductNumber() + "\"" + kgOrTuNumber;
                updatePSKDataAuthorOrder.setAuthorOrderName(name);

                // ChangeSet anpassen
                dataChangeSet.setSource(iPartsChangeSetSource.AUTHOR_ORDER, DBActionOrigin.FROM_EDIT);
                updatePSKDataAuthorOrder.saveToDB();

                // Einen neuen Autoren-Auftrag für den nächsten KG-Durchlauf erstellen
                iPartsAuthorOrderId aoId = new iPartsAuthorOrderId(StrUtils.makeGUID());
                iPartsDataAuthorOrder aoData = new iPartsDataAuthorOrder(project, aoId);
                aoData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                aoData.setStatus(iPartsAuthorOrderStatus.CREATED, DBActionOrigin.FROM_EDIT);
                aoData.setCurrentCreationDate(DBActionOrigin.FROM_EDIT);
                aoData.setCreationUser(updatePSKDataAuthorOrder.getCreationUser(), DBActionOrigin.FROM_EDIT);
                aoData.setCreationUserGroupId(updatePSKDataAuthorOrder.getCreationUserGroupId(), DBActionOrigin.FROM_EDIT);
                aoData.setBstId(updatePSKDataAuthorOrder.getFieldValue(iPartsConst.FIELD_DAO_BST_ID), DBActionOrigin.FROM_EDIT);

                // Damit die Logausgabe (kommt erst nach dem Aufruf von diesem beforeSaveChangeSet()-Aufruf) funktioniert
                aoData.setAuthorOrderName(updatePSKDataAuthorOrder.getAuthorOrderName());
                updatePSKDataAuthorOrder = aoData;
            }
        };
    }

    private void handleCachesAndNotifications(iPartsProductId productId,
                                              Collection<AssemblyId> newModuleIds,
                                              Set<AssemblyId> modifiedAssemblies) {
        iPartsProduct.removeProductFromCache(getProject(), productId);
        KgTuForProduct.removeKgTuForProductFromCache(getProject(), productId);
        boolean projectForGUIIsActive = (sessionForGUI != null) && sessionForGUI.isActive() && (projectForGUI != null);
        if (projectForGUIIsActive) {
            sessionForGUI.invokeThreadSafeInSessionThread(() -> {
                projectForGUI.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                  productId, false), true);

                projectForGUI.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                  iPartsDataChangedEventByEdit.Action.NEW,
                                                                                  newModuleIds, false), true);
            });
        }
        // Veränderte Module
        for (AssemblyId assemblyId : modifiedAssemblies) {
            if (!newModuleIds.contains(assemblyId)) {
                EtkDataAssembly.removeDataAssemblyFromCache(getProject(), assemblyId);
            }
        }
        if (projectForGUIIsActive) {
            sessionForGUI.invokeThreadSafeInSessionThread(() -> {
                if (!modifiedAssemblies.isEmpty()) {
                    projectForGUI.fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                      iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                      modifiedAssemblies, false));
                }
                projectForGUI.fireProjectEvent(new DataChangedEvent(null), true);
            });
        }

        // Cluster-Event verschicken
        boolean clearProductsCache = iPartsProduct.getInstance(project, productId).isAggregateProduct(project);
        iPartsDataChangedEventByEdit.Action action = iPartsDataChangedEventByEdit.Action.MODIFIED;
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                                  action,
                                                                                                  productId,
                                                                                                  clearProductsCache));
    }

    private Runnable createRunnable(final iPartsRevisionChangeSet changeSet,
                                    final ModuleTransferElem currentTransferElem,
                                    final EtkDataAssembly sourceConstAssembly, final HmMSmId hmMSmId,
                                    final boolean isDIALOGDestinationProduct,
                                    Set<String> pskVariantIdsIntersection,
                                    final VarParam<Boolean> result) {
        Runnable transferToASRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // DataChangedEvent ist hier nicht notwendig
                    List<AbstractRevisionChangeSet> changeSets = new DwList<>(1);
                    changeSets.add(changeSet);
                    getRevisionsHelper().setActiveRevisionChangeSets(changeSets, changeSet, false, getProject());

                    EditTransferToASHelper transferToASHelper = new EditTransferToASHelper(getProject(), sourceConstAssembly, hmMSmId);
                    transferToASHelper.setOnModifyCreatedRetailPartListEntryEvent(createModifyEvent(currentTransferElem));
                    transferToASHelper.setShowOnlyProgress(true);
                    transferToASHelper.setIsCancelledVarParam(isCancelled);

                    Map<PartListEntryId, List<PartListEntryId>> sourcePLEIdToTargetPLEIdMap = null;
                    Map<PartListEntryId, EtkDataArray> sourcePLEIdToVariantValidityMap = null;
                    Set<PartListEntryId> sourcePLEIdToOmitFlagSet = null;
                    Set<PartListEntryId> sourcePLEIdToLockEntryFlagSet = null;
                    if (updatePSKModules) {
                        // Beim Abgleich von PSK-Modulen mit den Serien-Modulen alle Stücklisteneinträge der Serien-Module
                        // zuerst löschen aus den PSK-Modulen, damit diese danach neu übernommen werden können
                        sourcePLEIdToTargetPLEIdMap = new HashMap<>(); // Map, um dieselben laufenden Nummern im PSK-Modul wieder zu verwenden
                        sourcePLEIdToVariantValidityMap = new HashMap<>(); // Map zum Merken der PSK-Varianten-Gültigkeiten
                        sourcePLEIdToOmitFlagSet = new HashSet<>(); // Set zum Merken des Unterdrückt-Flags
                        sourcePLEIdToLockEntryFlagSet = new HashSet<>(); // Set zum Merken des zum Editieren gesperrt-Flags
                        EtkEbenenDaten partsListTypeWithCopyVari = new EtkEbenenDaten();
                        partsListTypeWithCopyVari.addFeld(new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_COPY_VARI, false, false));
                        partsListTypeWithCopyVari.addFeld(new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_COPY_LFDNR, false, false));
                        partsListTypeWithCopyVari.addFeld(new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY, false, true));

                        EtkDataObjectList dataObjectListToBeSaved = new GenericEtkDataObjectList(); // für die WW-Sets und Zeichnungen
                        for (Map.Entry<String, List<TransferToASElement>> entry : currentTransferElem.moduleMap.entrySet()) {
                            if (isCancelled.getValue()) {
                                showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                                result.setValue(false);
                                return;
                            }

                            if (entry.getValue().isEmpty()) {
                                continue;
                            }

                            // Alle Quell-Stücklisteneinträge kommen aus demselben Serien-Modul -> AssemblyId vom ersten verwenden
                            AssemblyId sourceAssemblyId = entry.getValue().get(0).getSelectedPartlistEntry().getOwnerAssemblyId();

                            AssemblyId targetAssemblyId = new AssemblyId(entry.getKey(), "");
                            EtkDataAssembly targetAssembly = EtkDataObjectFactory.createDataAssembly(project, targetAssemblyId);
                            if (!targetAssembly.existsInDB()) { // Kann eigentlich nicht passieren, existsInDB() lädt aber auch die Attribute der targetAssembly
                                continue;
                            }

                            updatePSKImages(sourceAssemblyId, targetAssembly, entry, dataObjectListToBeSaved);

                            // Zu löschende Stücklisteneinträge im PSK-Modul aus dem Serien-Modul ermitteln
                            List<EtkDataPartListEntry> deletePLEList = new DwList<>();
                            for (EtkDataPartListEntry targetPartListEntry : targetAssembly.getPartListUnfiltered(partsListTypeWithCopyVari)) {
                                if (isCancelled.getValue()) {
                                    showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                                    result.setValue(false);
                                    return;
                                }

                                PartListEntryId copyPLEId = new PartListEntryId(targetPartListEntry.getFieldValue(iPartsConst.FIELD_K_COPY_VARI),
                                                                                "", targetPartListEntry.getFieldValue(iPartsConst.FIELD_K_COPY_LFDNR));
                                if (copyPLEId.isValidId() && copyPLEId.getOwnerAssemblyId().equals(sourceAssemblyId)) {
                                    // Es handelt sich um einen aus dem abzugleichenden Serien-Modul übernommenen Stücklisteneintrag
                                    // -> PSK-Varianten-Gültigkeit, Unterdrückt-Flag sowie bisherige laufende Nummer merken
                                    // und Stücklisteneintrag aus dem PSK-Modul zum Löschen markieren
                                    EtkDataArray variantValidity = targetPartListEntry.getFieldValueAsArray(iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY);
                                    if ((variantValidity != null) && !variantValidity.isEmpty()) {
                                        sourcePLEIdToVariantValidityMap.put(copyPLEId, variantValidity);
                                    }
                                    // Unterdrückt-Flag merken
                                    if (targetPartListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT)) {
                                        sourcePLEIdToOmitFlagSet.add(copyPLEId);
                                    }
                                    // "Für Edit gesperrt"-Flag merken
                                    if (iPartsLockEntryHelper.isLockedWithDBCheck(targetPartListEntry)) {
                                        sourcePLEIdToLockEntryFlagSet.add(copyPLEId);
                                    }

                                    List<PartListEntryId> destPLEIds = sourcePLEIdToTargetPLEIdMap.computeIfAbsent(copyPLEId,
                                                                                                                   pleId -> new ArrayList<>());
                                    destPLEIds.add(targetPartListEntry.getAsId());

                                    deletePLEList.add(targetPartListEntry);
                                }
                            }

                            int count = deletePLEList.size();
                            String msg = "!!Lösche %1 Datensätze aus \"%2\"...";
                            if (count == 1) {
                                msg = "!!Lösche %1 Datensatz aus \"%2\"...";
                            }
                            showMsg(TranslationHandler.translate(msg, String.valueOf(count), targetAssemblyId.getKVari()));

                            // WW-Sets korrigieren
                            dataObjectListToBeSaved.addAll(EditAssemblyListForm.deleteWWSets(deletePLEList, targetAssembly,
                                                                                             project), DBActionOrigin.FROM_EDIT);

                            if (targetAssembly instanceof iPartsDataAssembly) {
                                // Für bessere Performance beim Löschen, wobei clearAllDataCombTextListsForPartList() am
                                // Ende nicht aufgerufen werden muss, da targetAssembly sowieso nur temporär existiert
                                ((iPartsDataAssembly)targetAssembly).loadAllDataCombTextListsForPartList();
                            }

                            // Stücklisteneinträge aus dem PSK-Modul inkl. Referenzen löschen analog zu EditAssemblyListForm.deletePartListEntries()
                            for (EtkDataPartListEntry partListEntry : deletePLEList) {
                                if (isCancelled.getValue()) {
                                    showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                                    result.setValue(false);
                                    return;
                                }

                                // Lädt auch alle Ersetzungen und "PEM ab/bis auswerten"-Flags an allen Vorgängern und Nachfolgern neu
                                EtkDataObjectList deletedReferencedData = partListEntry.deleteReferencedData(deletePLEList);
                                if (deletedReferencedData == null) {
                                    deletedReferencedData = new GenericEtkDataObjectList();
                                }
                                deletedReferencedData.delete(partListEntry, true, DBActionOrigin.FROM_EDIT);

                                // Jetzt schon im ChangeSet speichern, damit beim Löschen von mehreren Stücklisteneinträgen
                                // dies von den anderen Stücklisteneinträgen z.B. bei Ersetzungen bereits berücksichtigt werden
                                // kann. Theoretisch sollte dies zwar weiter unten in der Transaktion stattfinden, aber dies
                                // ist leider nicht möglich, da die Änderungen ansonsten eben nicht berücksichtigt werden
                                // könnten beim Löschen von mehreren Stücklisteneinträgen. Die Transaktion schon vor dieser
                                // Schleife zu starten funktioniert leider ebenfalls nicht, weil im Worst Case Pseudo-Transaktionen
                                // benötigt werden könnten beim Laden von Daten für das Löschen der Stücklisteneinträge.
                                changeSet.addDataObjectList(deletedReferencedData);
                            }

                            // PSK-Modul aus dem Cache entfernen, damit die gelöschten Stücklisteneinträge berücksichtigt
                            // werden beim späteren Hinzufügen der neuen übernommenen Stücklisteneinträge
                            EtkDataAssembly.removeDataAssemblyFromCache(project, targetAssemblyId);
                        }
                        changeSet.addDataObjectList(dataObjectListToBeSaved);
                    } else { // Beim Abgleich von PSK-Modulen gibt es keine neuen Module
                        // Modul-Meta-Daten der Quell-Module laden und in moduleMetaDataMap ablegen
                        Map<String, iPartsDataModule> moduleMetaDataMap = new HashMap<>();
                        for (Map.Entry<String, TransferToASElement> noExistingModuleEntry : currentTransferElem.notExistingModuleMap.entrySet()) {
                            TransferToASElement transferElem = noExistingModuleEntry.getValue();
                            AssemblyId sourceAssemblyId = null;
                            if (!isDIALOGDestinationProduct) {
                                EtkDataPartListEntry sourcePartListEntry = transferElem.getSelectedPartlistEntry();
                                if (sourcePartListEntry != null) {
                                    sourceAssemblyId = sourcePartListEntry.getAsId().getOwnerAssemblyId();
                                }
                            } else {
                                if (transferElem.hasUserObject() && (transferElem.getUserObject() instanceof TURetailElem)) {
                                    sourceAssemblyId = ((TURetailElem)transferElem.getUserObject()).getSourceAssemblyId();
                                }
                            }
                            if (sourceAssemblyId != null) {
                                iPartsDataModule sourceDataModule = new iPartsDataModule(project, new iPartsModuleId(sourceAssemblyId.getKVari()));
                                if (sourceDataModule.existsInDB()) {
                                    // DAIMLER-15089
                                    sourceDataModule.setSourceModule(sourceAssemblyId, DBActionOrigin.FROM_EDIT);
                                    moduleMetaDataMap.put(noExistingModuleEntry.getKey(), sourceDataModule);
                                }
                            }
                        }

                        // Neue Module anlegen
                        transferToASHelper.createNewModules(currentTransferElem.notExistingModuleMap, moduleMetaDataMap);
                        if (isCancelled.getValue()) {
                            showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                            result.setValue(false);
                            return;
                        }
                        for (Map.Entry<String, TransferToASElement> entry : currentTransferElem.notExistingModuleMap.entrySet()) {
                            AssemblyId assemblyId = entry.getValue().getAssemblyId();
                            showMsg(TranslationHandler.translate("!!Neuer Retail-TU \"%1\" wurde angelegt", assemblyId.getKVari()));
                        }
                    }

                    // Stücklisteneinträge übernehmen
                    List<String> logMessages = new ArrayList<>();
                    List<String> messages = new DwList<>();
                    for (Map.Entry<String, List<TransferToASElement>> entry : currentTransferElem.moduleMap.entrySet()) {
                        if (isCancelled.getValue()) {
                            showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                            result.setValue(false);
                            return;
                        }
                        int count = entry.getValue().size();
                        String msg = "!!Übernehme %1 Datensätze nach \"%2\"...";
                        if (count == 1) {
                            msg = "!!Übernehme %1 Datensatz nach \"%2\"...";
                        }
                        messages.add(TranslationHandler.translate(msg, String.valueOf(count), entry.getKey()));
                    }
                    showMsg(messages);

                    if (isDIALOGDestinationProduct) { // Übernahme aus der Konstruktions-Stückliste
                        transferToASHelper.createAndTransferDIALOGPartListEntriesForCopyModule(currentTransferElem.moduleMap,
                                                                                               currentTransferElem.notExistingModuleMap,
                                                                                               logMessages, getMessageLogForExecution());
                    } else { // Normales Kopieren der Quell-Stücklisteneinträge
                        EtkDataObjectList dataObjectListToBeSaved = new GenericEtkDataObjectList();
                        for (Map.Entry<String, List<TransferToASElement>> moduleEntries : currentTransferElem.moduleMap.entrySet()) {
                            if (isCancelled.getValue()) {
                                showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                                result.setValue(false);
                                return;
                            }

                            String targetModuleId = moduleEntries.getKey();
                            if (updatePSKModules) {
                                showMsg(TranslationHandler.translate("!!Stücklisteneinträge vom PSK-TU \"%1\" werden abgeglichen...",
                                                                     targetModuleId));
                            } else {
                                showMsg(TranslationHandler.translate("!!Kopiere Stücklisteneinträge in den Retail-TU \"%1\"...",
                                                                     targetModuleId));
                            }
                            AssemblyId targetAssemblyId = new AssemblyId(targetModuleId, "");
                            EtkDataAssembly targetAssembly = EtkDataObjectFactory.createDataAssembly(project, targetAssemblyId);
                            if (filterPSKVariantIds) {
                                sourcePLEIdToVariantValidityMap = new HashMap<>();
                            }
                            List<EtkDataPartListEntry> sourcePartListEntries = new ArrayList<>(moduleEntries.getValue().size());
                            for (TransferToASElement transferToASElement : moduleEntries.getValue()) {
                                EtkDataPartListEntry sourcePartListEntry = transferToASElement.getSelectedPartlistEntry();
                                if (filterPSKVariantIds) {
                                    EtkDataArray filteredVariantValidities = new EtkDataArray();
                                    if (!pskVariantIdsIntersection.isEmpty()) {
                                        //Es werden nur die im Ziel-Produkt vorhandenen Variantengültigkeit übernommen
                                        EtkDataArray sourceVariantValidities = sourcePartListEntry.getFieldValueAsArray(iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY);
                                        if (sourcePartListEntries != null) {
                                            List<String> sourceVariantValiditiesList = sourceVariantValidities.getArrayAsStringList();
                                            for (String sourceVariantValidity : sourceVariantValiditiesList) {
                                                if (pskVariantIdsIntersection.contains(sourceVariantValidity)) {
                                                    filteredVariantValidities.add(sourceVariantValidity);
                                                }
                                            }
                                        }
                                    }
                                    sourcePLEIdToVariantValidityMap.put(sourcePartListEntry.getAsId(), filteredVariantValidities);
                                }
                                sourcePartListEntries.add(sourcePartListEntry);
                            }
                            boolean assemblyIsNew = currentTransferElem.notExistingModuleMap.containsKey(targetModuleId);
                            if (targetAssembly instanceof iPartsDataAssembly) {
                                // Hier Prüfung auf PKW und nicht nur DIALOG für das Kopieren der Stücklisteneinträge,
                                // da auch für PSK-PKW DIALOG-sepzifische Aktionen in relocateEntries() stattfinden müssen
                                boolean isPKWAssembly = ((iPartsDataAssembly)targetAssembly).getDocumentationType().isPKWDocumentationType();
                                // Konfig für das Verlagern erstellen
                                RelocateEntriesConfig relocateEntriesConfig = RelocateEntriesConfig.createConfig(assemblyIsNew, isPKWAssembly)
                                        .setSourceReferenceAndTimeStamp(!copyPSKProductToPSKProduct)
                                        .setSourcePLEIdToTargetVariantValidityMap(sourcePLEIdToVariantValidityMap)
                                        .setSourcePLEIdToTargetOmitFlagSet(sourcePLEIdToOmitFlagSet)
                                        .setSourcePLEIdToTargetEditLockFlagSet(sourcePLEIdToLockEntryFlagSet)
                                        .setSourcePLEIdToTargetPLEIdMap(sourcePLEIdToTargetPLEIdMap);
                                EditAssemblyListForm.relocateEntries((iPartsDataAssembly)targetAssembly, sourcePartListEntries,
                                                                     dataObjectListToBeSaved, relocateEntriesConfig,
                                                                     EditAssemblyListForm.RELOCATE_ACTION.COPY, project);
                            }
                            // Bildreferenzen kopieren und wenn kein PSK-Produkt Modul auf verborgen setzen
                            EditTransferToASHelper.OnModifyCreatedRetailPartListEntryEvent onModifyCreatedRetailPartListEntryEvent = transferToASHelper.getOnModifyCreatedRetailPartListEntryEvent();
                            if (onModifyCreatedRetailPartListEntryEvent != null) {
                                onModifyCreatedRetailPartListEntryEvent.onModifyCreatedAssembly(project, dataObjectListToBeSaved, assemblyIsNew, targetAssembly);
                            }
                        }
                        changeSet.addDataObjectList(dataObjectListToBeSaved);
                    }

                    if (isCancelled.getValue()) {
                        showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                        result.setValue(false);
                        hideProgress();
                        return;
                    }
                    if (!logMessages.isEmpty()) {
                        MessageDialog.show(logMessages);
                    }
                    hideProgress();
                } catch (EditTransferToASHelper.EditTransferPartListEntriesException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    if (isExecutedInBackground()) {
                        if (sessionForGUI.isActive()) {
                            sessionForGUI.invokeThreadSafeInSessionThread(() -> {
                                MessageDialog.showError(e.getMessage());
                            });
                        }
                    }
                    showError(e.getMessage());
                    result.setValue(false);
                    return;
                } catch (Exception e) {
                    showError(Utils.exceptionToString(e));
                    result.setValue(false);
                    Logger.getLogger().throwRuntimeException(e);
                } finally {
                    getRevisionsHelper().clearActiveRevisionChangeSets(getProject(), false);
                }
            }

            private void updatePSKImages(AssemblyId sourceAssemblyId, EtkDataAssembly targetAssembly, Map.Entry<String, List<TransferToASElement>> transferEntry,
                                         EtkDataObjectList dataObjectListToBeSaved) {
                // Zeichnungen abgleichen
                Map<String, EtkDataImage> sourceImageIdToImage = collectImages(sourceAssemblyId);

                // In den Zeichnungen vom PSK-Modul nach den Zeichnungen aus dem Serien-Modul suchen
                int modifiedImagesCount = 0;
                DBDataObjectList<EtkDataImage> targetImages = targetAssembly.getUnfilteredImages();
                for (EtkDataImage targetImage : targetImages) {
                    String targetImageKey = getImageKey(targetImage);
                    EtkDataImage sourceImage = sourceImageIdToImage.remove(targetImageKey);
                    if (sourceImage != null) {
                        // Zeichnung im PSK-Modul aktualisieren
                        DataImageId targetImageId = targetImage.getAsId();

                        // Attribute der Zeichnung vom Serien-Modul mit dem Primärschlüssel der Zeichnung vom PSK-Modul
                        // korrigieren, das Feld T_STAMP ebenfalls auf den Wert vom PSK-Modul setzen sowie die PSK-Varianten-Gültigkeit
                        // vom PSK-Modul beibehalten
                        DBDataObjectAttributes sourceAttributes = sourceImage.getAttributes();
                        sourceAttributes = sourceAttributes.cloneMe(DBActionOrigin.FROM_DB);
                        targetAssembly.convertImageAttributes(sourceAttributes, targetImageId.getIBlatt(), targetImage, DBActionOrigin.FROM_DB);
                        sourceAttributes.addField(iPartsConst.FIELD_STAMP, targetImage.getFieldValue(iPartsConst.FIELD_STAMP),
                                                  DBActionOrigin.FROM_DB);
                        sourceAttributes.addField(targetImage.getAttribute(iPartsConst.FIELD_I_PSK_VARIANT_VALIDITY),
                                                  DBActionOrigin.FROM_DB);
                        targetImage.assignAttributesValues(project, sourceAttributes, true, DBActionOrigin.FROM_EDIT);

                        if (targetImage.isModifiedWithChildren()) {
                            // Wert vom Feld T_STAMP auf den Wert vom Serien-TU setzen, da auch andere Attribute
                            // verändert worden sind
                            targetImage.setFieldValue(iPartsConst.FIELD_STAMP, sourceAttributes.getFieldValue(iPartsConst.FIELD_STAMP),
                                                      DBActionOrigin.FROM_EDIT);

                            dataObjectListToBeSaved.add(targetImage, DBActionOrigin.FROM_EDIT);
                            modifiedImagesCount++;
                        }
                    }
                }
                if (modifiedImagesCount > 0) {
                    String msg = "!!Aktualisiere %1 Zeichnungen in \"%2\"...";
                    if (modifiedImagesCount == 1) {
                        msg = "!!Aktualisiere %1 Zeichnung in \"%2\"...";
                    }
                    showMsg(TranslationHandler.translate(msg, String.valueOf(modifiedImagesCount), transferEntry.getKey()));
                }

                // Alle Zeichnungen aus dem Serien-Modul, die jetzt noch in sourceImageIdToImage vorhanden sind,
                // sind neu und müssen übernommen werden
                if (!sourceImageIdToImage.isEmpty()) {
                    int count = sourceImageIdToImage.size();
                    String msg = "!!Übernehme %1 neue Zeichnungen nach \"%2\"...";
                    if (count == 1) {
                        msg = "!!Übernehme %1 neue Zeichnung nach \"%2\"...";
                    }
                    showMsg(TranslationHandler.translate(msg, String.valueOf(count), transferEntry.getKey()));
                    for (EtkDataImage newSourceImage : sourceImageIdToImage.values()) {
                        EtkDataImage newTargetImage = targetAssembly.addImage(newSourceImage.getAttributes(), DBActionOrigin.FROM_EDIT);
                        dataObjectListToBeSaved.add(newTargetImage, DBActionOrigin.FROM_EDIT);
                    }
                }
            }

            private Map<String, EtkDataImage> collectImages(AssemblyId assemblyId) {
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
                DBDataObjectList<EtkDataImage> images = assembly.getUnfilteredImages();
                Map<String, EtkDataImage> imageIdToImage = new LinkedHashMap<>();
                for (EtkDataImage image : images) {
                    String sourceImageKey = getImageKey(image);
                    imageIdToImage.put(sourceImageKey, image);
                }
                return imageIdToImage;
            }

            private String getImageKey(EtkDataImage image) {
                return image.getImagePoolNo() + IdWithType.DB_ID_DELIMITER + image.getImagePoolVer();
            }
        };

        return transferToASRunnable;
    }

    private EditTransferToASHelper.OnModifyCreatedRetailPartListEntryEvent createModifyEvent(final ModuleTransferElem currentTransferElem) {
        return new EditTransferToASHelper.OnModifyCreatedRetailPartListEntryEvent() {

            @Override
            public void onModifyCreatedPartListEntry(EtkProject project, EtkDataPartListEntry createdRetailPartListEntry,
                                                     TransferToASElement transferElem,
                                                     iPartsDataCombTextList combinedTextList,
                                                     iPartsDataFootNoteCatalogueRefList fnCatalogueRefList) {
                TURetailElem retailElem = getUserObject(transferElem);
                if (retailElem != null) {
                    if (retailElem.copyFootNotes) {
                        // kopiere Fußnoten vom sourceRetail- zum destRetail-Partlistentry
                        copyFootNotes(project, retailElem.partListEntry, createdRetailPartListEntry,
                                      fnCatalogueRefList);
                    }
                    // Omitted Flag übernehmen
                    createdRetailPartListEntry.setFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT, retailElem.getOmmitted(), DBActionOrigin.FROM_EDIT);
                }
                logMsg(TranslationHandler.translate("!!Konstruktionsposition %1 nach %2 übernommen",
                                                    transferElem.getSelectedPartlistEntry().getAsId().getKLfdnr(),
                                                    createdRetailPartListEntry.getAsId().toStringForLogMessages()));
            }

            @Override
            public void onModifyCreatedPartListEntryEDS(EtkProject project, EtkDataPartListEntry createdRetailPartListEntry, TransferToASElement transferElem, iPartsDataCombTextList combinedTextList, iPartsDataFootNoteCatalogueRefList fnCatalogueRefList) {
                TURetailElem retailElem = getUserObject(transferElem);
                if (retailElem != null) {
                    if (retailElem.copyFootNotes) {
                        // kopiere Fußnoten vom sourceRetail- zum destRetail-Partlistentry
                        copyFootNotes(project, retailElem.partListEntry, createdRetailPartListEntry,
                                      fnCatalogueRefList);
                    }
                }
                logMsg(TranslationHandler.translate("!!Konstruktionsposition %1 nach %2 übernommen",
                                                    transferElem.getSelectedPartlistEntry().getAsId().getKLfdnr(),
                                                    createdRetailPartListEntry.getAsId().toStringForLogMessages()));
            }

            @Override
            public void onModifyCreatedAssembly(EtkProject project, EtkDataObjectList dataObjectListToBeSaved, boolean assemblyIsNew, EtkDataAssembly destAssembly) {
                if (assemblyIsNew && (destAssembly instanceof iPartsDataAssembly)) {
                    List<TransferToASElement> transferList = currentTransferElem.moduleMap.get(destAssembly.getAsId().getKVari());
                    if (!transferList.isEmpty()) {
                        TransferToASElement transferElem = transferList.get(0);
                        // Modul bei PSK Produkt nicht auf verborgen setzen
                        if (!targetIsPSKProduct) {
                            // Hier nur den "Visible" Wert in der DB betrachten sonst ist das Produkt immer sichtbar
                            if (transferElem.getProduct().isRetailRelevantFromDB()) {
                                showMsg(TranslationHandler.translate("Der neue Retail-TU \"%1\" wird als zu verbergen gekennzeichnet.",
                                                                     destAssembly.getAsId().getKVari()));
                                // Metadaten zum Modul anpassen und speichern
                                iPartsDataModule moduleMetaData = ((iPartsDataAssembly)destAssembly).getModuleMetaData();
                                moduleMetaData.setFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN, true, DBActionOrigin.FROM_EDIT);
                                dataObjectListToBeSaved.add(moduleMetaData, DBActionOrigin.FROM_EDIT);
                            }
                        }
                        TURetailElem retailElem = getUserObject(transferElem);
                        if (retailElem != null) {
                            // Bildreferenzen ohne Gültigkeiten kopieren
                            EtkDataImageList imageList = copyPicReferences(project, retailElem.getSourceAssemblyId(), destAssembly.getAsId());
                            if (!imageList.isEmpty()) {
                                String key;
                                if (imageList.size() == 1) {
                                    key = "!!Kopiere %1 Bildreferenz nach \"%2\"";
                                } else {
                                    key = "!!Kopiere %1 Bildreferenzen nach \"%2\"";
                                }
                                showMsg(TranslationHandler.translate(key, String.valueOf(imageList.size()), destAssembly.getAsId().getKVari()));
                                dataObjectListToBeSaved.addAll(imageList, DBActionOrigin.FROM_EDIT);
                            }
                        }
                    }
                }
            }

            @Override
            public void onModifyCreatedAssemblyEDS(EtkProject project, EtkDataObjectList dataObjectListToBeSaved, boolean assemblyIsNew, EtkDataAssembly destAssembly) {
                onModifyCreatedAssembly(project, dataObjectListToBeSaved, assemblyIsNew, destAssembly);
            }

            private TURetailElem getUserObject(TransferToASElement transferElem) {
                if ((transferElem != null) && transferElem.hasUserObject()) {
                    Object userObject = transferElem.getUserObject();
                    if (userObject instanceof TURetailElem) {
                        return (TURetailElem)userObject;
                    }
                }
                return null;
            }

            private void copyFootNotes(EtkProject project, iPartsDataPartListEntry sourcePartListEntry, EtkDataPartListEntry createdRetailPartListEntry,
                                       iPartsDataFootNoteCatalogueRefList fnCatalogueRefList) {
                if (sourcePartListEntry == null) {
                    return;
                }
                Collection<iPartsFootNote> sourceFootNoteList = sourcePartListEntry.getFootNotes();
                if ((sourceFootNoteList != null) && !sourceFootNoteList.isEmpty()) {
                    iPartsDataFootNoteCatalogueRefList sourceCatalogueRefList = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntry(project,
                                                                                                                                                 sourcePartListEntry.getAsId());
                    EditEqualizeFootNoteHelper footNoteHelper = new EditEqualizeFootNoteHelper(project);
                    iPartsDataFootNoteCatalogueRefList destRefList = getDestCatalogueRefList(createdRetailPartListEntry.getAsId(), fnCatalogueRefList);
                    iPartsDataFootNoteCatalogueRefList inheritedFootnotes = footNoteHelper.inheritFootNotes(sourceCatalogueRefList, createdRetailPartListEntry, destRefList, true);
                    if (!inheritedFootnotes.isEmpty()) {
                        List<String> fnIds = new DwList<>();
                        for (iPartsDataFootNoteCatalogueRef fnCatalogueRef : inheritedFootnotes) {
                            fnIds.add(fnCatalogueRef.getAsId().getFootNoteId());
                        }
                        String key = "!!Beim Stücklisteneintrag \"%1\" wurden folgende Fußnoten vom Retail-TU \"%2\" übernommen:";
                        if (fnIds.size() == 1) {
                            key = "!!Beim Stücklisteneintrag \"%1\" wurde folgende Fußnote vom Retail-TU \"%2\" übernommen:";
                        }
                        showMsg(TranslationHandler.translate(key, createdRetailPartListEntry.getAsId().getKVari(),
                                                             sourcePartListEntry.getAsId().getKVari()));
                        buildNumberList(fnIds);
                    }
                    if (!destRefList.isEmpty() && !inheritedFootnotes.isEmpty()) {
                        List<iPartsDataFootNoteCatalogueRef> addedElems = fnCatalogueRefList.getAsList();
                        List<iPartsDataFootNoteCatalogueRef> deletedElems = fnCatalogueRefList.getDeletedList();
                        fnCatalogueRefList.clear(DBActionOrigin.FROM_DB);
                        for (iPartsDataFootNoteCatalogueRef catRef : addedElems) {
                            if (!catRef.getAsId().getPartListEntryId().equals(createdRetailPartListEntry.getAsId())) {
                                fnCatalogueRefList.add(catRef, DBActionOrigin.FROM_EDIT);
                            }
                        }
                        if (!deletedElems.isEmpty()) {
                            for (iPartsDataFootNoteCatalogueRef catRef : deletedElems) {
                                if (!catRef.getAsId().getPartListEntryId().equals(createdRetailPartListEntry.getAsId())) {
                                    fnCatalogueRefList.delete(catRef, true, DBActionOrigin.FROM_EDIT);
                                }
                            }
                        }
                    }
                    // erst jetzt zur DB-Liste hinzufügen
                    fnCatalogueRefList.addAll(inheritedFootnotes, DBActionOrigin.FROM_EDIT);
                }
            }

            /**
             * Kopiert die Bildreferenzen der Quellstückliste zur Zielstückliste. Hierbei werden die Gültigkeiten
             * nicht kopiert, weil sie an der Zielstückliste keinen Sinn ergeben (BM mit anderen AA).
             *
             * @param project
             * @param sourceAssemblyId
             * @param destAssemblyId
             * @return
             */
            private EtkDataImageList copyPicReferences(EtkProject project, AssemblyId sourceAssemblyId, AssemblyId destAssemblyId) {
                EtkDataImageList imageList = EtkDataObjectFactory.createDataImageList();
                DBDataObjectAttributesList imageAttributesList = imageList.getUnfilteredImageAttributesList(project, sourceAssemblyId);
                if (!imageAttributesList.isEmpty()) {
                    for (DBDataObjectAttributes imageAttributes : imageAttributesList) {
                        String img = imageAttributes.getField(EtkDbConst.FIELD_I_IMAGES).getAsString();
                        String imgVer = imageAttributes.getField(EtkDbConst.FIELD_I_PVER).getAsString();
                        String blatt = imageAttributes.getField(EtkDbConst.FIELD_I_BLATT).getAsString();

                        EtkDataImage image = EtkDataObjectFactory.createDataImage(project, destAssemblyId, blatt, img, imgVer);
                        image.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        image.setFieldValue(iPartsConst.FIELD_I_IMAGEDATE, imageAttributes.getField(iPartsConst.FIELD_I_IMAGEDATE).getAsString(), DBActionOrigin.FROM_EDIT);
                        // die I_PVER setzen, da kein PK von EtkImage
                        image.setFieldValue(iPartsConst.FIELD_I_PVER, imgVer, DBActionOrigin.FROM_EDIT);
                        image.setFieldValue(iPartsConst.FIELD_I_IMAGES, img, DBActionOrigin.FROM_EDIT);
                        imageList.add(image, DBActionOrigin.FROM_EDIT);
                    }
                }
                return imageList;
            }
        };
    }

    private iPartsDataFootNoteCatalogueRefList getDestCatalogueRefList(PartListEntryId createdRetailPartListEntryId,
                                                                       iPartsDataFootNoteCatalogueRefList fnCatalogueRefList) {
        iPartsDataFootNoteCatalogueRefList result = new iPartsDataFootNoteCatalogueRefList();
        for (iPartsDataFootNoteCatalogueRef catalogueRef : fnCatalogueRefList) {
            if (catalogueRef.getAsId().getPartListEntryId().equals(createdRetailPartListEntryId)) {
                result.add(catalogueRef, DBActionOrigin.FROM_DB);
            }
        }
        return result;
    }

    private List<String> getInheritedFootNoteIds(PartListEntryId createdRetailPartListEntryId, iPartsDataFootNoteCatalogueRefList fnCatalogueRefList) {
        List<String> result = new DwList<>();
        for (iPartsDataFootNoteCatalogueRef catalogueRef : fnCatalogueRefList) {
            if (catalogueRef.getAsId().getPartListEntryId().equals(createdRetailPartListEntryId)) {
                result.add(catalogueRef.getAsId().getFootNoteId());
            }
        }
        return result;
    }

    /**
     * Step 5: alles für die Übernahme nach AS vorbereiten
     *
     * @param destinationProduct
     * @param bcteUsageMap
     * @param constructionAssemblyMap
     * @param constToRetailMap
     * @return
     */
    private boolean prepareForTransfer(iPartsProduct destinationProduct,
                                       Map<HmMSmId, List<TURetailElem>> bcteUsageMap,
                                       Map<HmMSmId, EtkDataAssembly> constructionAssemblyMap,
                                       Map<HmMSmId, ModuleTransferElem> constToRetailMap) {
        boolean isDIALOGDestinationProduct = destinationProduct.getDocumentationType().isDIALOGDocumentationType();
        Set<AssemblyId> copyTUSet = new HashSet<>();
        Set<String> kgNoSet = new HashSet<>();
        for (Map.Entry<HmMSmId, List<TURetailElem>> entry : bcteUsageMap.entrySet()) {
            HmMSmId hmMSmId = entry.getKey();
            List<TURetailElem> retailList = entry.getValue();
            if (retailList.isEmpty()) {
                continue;
            }
            ModuleTransferElem moduleTransferElem = new ModuleTransferElem();
            for (TURetailElem retailElem : retailList) {
                if (retailElem.transferBcteKeyList.isEmpty()) {
                    continue;
                }
                if (isDIALOGDestinationProduct && (constructionAssemblyMap.get(retailElem.getSourceHmMSmId()) == null)) {
                    continue;
                }
                if (!updatePSKModules) { // Beim Abgleich von PSK-Modulen ist das PSK-Modul bereits bekannt
                    retailElem.setDestinationAssemblyId(destinationProduct);
                }

                AssemblyId destinationAssemblyId = retailElem.destinationAssemblyId;
                targetAssemblyIds.add(new iPartsAssemblyId(destinationAssemblyId.getKVari(), destinationAssemblyId.getKVer()));

                String destAssemblyName = destinationAssemblyId.getKVari();
                for (iPartsDialogBCTEPrimaryKey bcteKey : retailElem.transferBcteKeyList) {
                    EtkDataPartListEntry sourcePartListEntry;
                    if (isDIALOGDestinationProduct) {
                        sourcePartListEntry = getConstPartListEntry(constructionAssemblyMap, bcteKey);
                    } else {
                        sourcePartListEntry = retailElem.partListEntry;
                    }
                    if (sourcePartListEntry != null) {
                        String hotspot = retailElem.getSourceHotSpot();
                        KgTuListItem kgTuListItem = null;
                        if (!updatePSKModules) {
                            Map<String, KgTuListItem> kgTuStructureCache = KgTuHelper.getKGTUStructure(project, sourceProductId);
                            kgTuListItem = kgTuStructureCache.get(retailElem.getKgTuId().getKg());
                        }
                        EtkMultiSprache kgTitle = null;
                        Map<KgTuId, EtkMultiSprache> tuTitlesMap = new HashMap<>();
                        if (kgTuListItem != null) {
                            kgTitle = kgTuListItem.getKgTuNode().getTitle();
                            for (KgTuListItem child : kgTuListItem.getChildren()) {
                                tuTitlesMap.put(child.getKgTuId(), child.getKgTuNode().getTitle());
                            }
                        }
                        TransferToASElement transferElem = new TransferToASElement(null,
                                                                                   retailElem.getKgTuId(),
                                                                                   hotspot, destinationProduct,
                                                                                   null, kgTitle, tuTitlesMap, bcteKey,
                                                                                   sourcePartListEntry);
                        transferElem.setUserObject(retailElem);
                        transferElem.setAutoTransfer(true);
                        moduleTransferElem.add(destAssemblyName, transferElem);
                        copyTUSet.add(retailElem.getSourceAssemblyId());
                        kgNoSet.add(retailElem.kgTuId.getKg());
                    }
                }

            }
            if (!moduleTransferElem.isEmpty()) {
                constToRetailMap.put(hmMSmId, moduleTransferElem);
            }
        }
        if (constToRetailMap.isEmpty()) {
            if (isDIALOGDestinationProduct) {
                showMsg(TranslationHandler.translate("!!Keine Konstruktions-Stücklisteneinträge zum Kopieren gefunden."));
            } else {
                showMsg(TranslationHandler.translate("!!Keine relevanten Stücklisteneinträge zum Kopieren gefunden."));
            }
        } else {
            if (showMessageBeforeTransfer && !updatePSKModules) {
                List<AssemblyId> assemblyIdList = new DwList<>(copyTUSet);
                if (!showContinueDialog(null, assemblyIdList,
                                        destinationProduct.getAsId().getProductNumber(),
                                        kgNoSet, "!!Es wird der folgende TU in das Produkt \"%1\" kopiert:",
                                        "!!Es werden die folgenden TUs in das Produkt \"%1\" kopiert:",
                                        "!!Fortsetzen?")) {
                    return false;
                }
            }
        }
        return constToRetailMap.size() > 0;
    }

    private void appendNoKgTuList(iPartsProduct destinationProduct, List<KgTuId> noKgTuNodeList, StringBuilder str) {
        if (!noKgTuNodeList.isEmpty()) {
            String msg = TranslationHandler.translate("!!Folgende KG/TU-Knoten können im Produkt \"%1\" nicht angelegt werden:",
                                                      destinationProduct.getAsId().getProductNumber());
            showMsg(msg);
            if (str.length() > 0) {
                str.append(OsUtils.NEWLINE);
            }
            str.append(msg);
            str.append(OsUtils.NEWLINE);
            str.append(buildKgTuList(noKgTuNodeList));
            str.append(OsUtils.NEWLINE);
        }
    }

    private boolean showContinueDialog(StringBuilder str, List<AssemblyId> assemblyIdList,
                                       String destProductNumber,
                                       Set<String> kgNoSet, String singleKey, String multiKey, String continueMsg) {
        // Bei Ausführen im Hintergrund keine Frage-Dialoge anzeigen
        if (executedInBackground || executeForAll) {
            return true;
        }

        String key = multiKey;
        if (assemblyIdList.size() == 1) {
            key = singleKey;
        }
        String msg = TranslationHandler.translate(key, destProductNumber);
        showMsg(msg);
        if (str == null) {
            str = new StringBuilder();
        } else {
            str.append(OsUtils.NEWLINE);
        }
        str.append(msg);
        str.append(OsUtils.NEWLINE);
        if ((kgNoSet != null) && !kgNoSet.isEmpty()) {
            str.append(" ");
            str.append(TranslationHandler.translate("!!Aus KG-Knoten %1", kgNoSet.iterator().next()));
            str.append(OsUtils.NEWLINE);
        }
        str.append(buildAssemblyList(assemblyIdList));
        str.append(OsUtils.NEWLINE);
        str.append(OsUtils.NEWLINE);
        str.append(TranslationHandler.translate(continueMsg));

        // Bei Ausführen im Hintergrund keine Frage-Dialoge anzeigen (hier erneut prüfen, um Race-Conditions zu vermeiden)
        if (executedInBackground || executeForAll) {
            return true;
        }

        // Benutzer-Abfrage, ob weitergemach werden soll
        final String message = str.toString();
        VarParam<Boolean> result = new VarParam<>(true);
        sessionForGUI.invokeThreadSafeInSessionThread(() -> {
            String executeForAllText = "!!Ja, für alle Folgenden";
            String executeForNoneText = "!!Nein, für alle Folgenden";
            String res = MessageDialog.show(message, MessageDialogIcon.CONFIRMATION, executeForAllText, executeForNoneText,
                                            MessageDialogButtons.YES.getButtonText(), MessageDialogButtons.NO.getButtonText());
            if (res.equals(MessageDialogButtons.NO.getButtonText()) || res.equals(executeForNoneText)) {
                result.setValue(false);
                String kgNumber = "";
                if ((kgNoSet != null) && !kgNoSet.isEmpty()) {
                    kgNumber = " " + kgNoSet.iterator().next();
                }
                if (res.equals(executeForNoneText)) {
                    stopForFollowing = true;
                    showMsg(TranslationHandler.translate("!!Alle restlichen KGs wurden durch Benutzer ignoriert"));
                    GuiButtonOnPanel buttonBackgroundExecution = messageLogForm.getButtonPanel().getCustomButtonOnPanel(EXECUTE_IN_BACKGROUND_TEXT);
                    if (buttonBackgroundExecution != null) {
                        buttonBackgroundExecution.setVisible(false);
                    }
                } else {
                    showMsg(TranslationHandler.translate("!!KG %1 wurde durch Benutzer ignoriert", kgNumber));
                }
            } else {
                executeForAll = res.equals(executeForAllText);
                if (executeForAll) {
                    showMsg(TranslationHandler.translate("!!Alle weiteren KG/TUs werden ohne Abfrage kopiert"));
                }
            }
        });
        return result.getValue();
    }

    /**
     * Step 4: Prüfe ob die ermittelten „BCTE“-Schlüssel im Status „offen“ sind
     * Lädt alle angeforderten Konstruktions-Stücklisten und überprüft für alle Ausführungsarten des Ziel-Produkts,
     * ob die angesprochenen Stücklisteneinträge den Status Offen besitzen
     *
     * @param destinationProduct
     * @param bcteUsageMap
     * @param constructionAssemblyMap
     * @return
     */
    private boolean checkConstructionAssemblies(iPartsProduct destinationProduct, Map<HmMSmId, List<TURetailElem>> bcteUsageMap,
                                                Map<HmMSmId, EtkDataAssembly> constructionAssemblyMap) {
        int maxPos = bcteUsageMap.size();
        boolean showProgress = maxPos > 2;

        Set<String> aaSetForDestinationProduct = destinationProduct.getAAsFromModels(getProject());
        Set<String> aaSet;
        boolean isDIALOGDestinationProduct = destinationProduct.getDocumentationType().isDIALOGDocumentationType();
        if (isDIALOGDestinationProduct) {
            aaSet = aaSetForDestinationProduct;
        } else {
            aaSet = new HashSet<>();
            aaSet.add(""); // Dummy-AA für nicht-DIALOG-Ziel-Produkte
        }
        int count = 0;
        int pos = 0;
        if (showProgress) {
            fireProgress(pos, maxPos);
        }
        for (Map.Entry<HmMSmId, List<TURetailElem>> entry : bcteUsageMap.entrySet()) {
            if (isCancelled.getValue()) {
                showMsg(TranslationHandler.translate("!!Abbruch durch Benutzer"));
                return false;
            }
            HmMSmId hmMSmId = entry.getKey();
            List<TURetailElem> retailList = entry.getValue();

            EtkDataAssembly constAssembly = null;
            if (isDIALOGDestinationProduct) {
                showMsg(TranslationHandler.translate("!!Überprüfe Konstruktions-Stückliste %1", hmMSmId.toStringForLogMessages()));
                constAssembly = createConstructionAssembly(hmMSmId);
            }
            int constructionElemCount = 0;
            if ((constAssembly != null) || !isDIALOGDestinationProduct) {
                for (TURetailElem retailElem : retailList) {
                    if (checkTransferAllowed(retailElem, constAssembly, aaSet, aaSetForDestinationProduct, isDIALOGDestinationProduct)) {
                        // es wurde minsestens ein offener Konstruktions-Stücklisteneintrag gefunden => Assembly merken
                        if (constructionAssemblyMap.get(hmMSmId) == null) {
                            constructionAssemblyMap.put(hmMSmId, constAssembly);
                        }
                    }
                    constructionElemCount += retailElem.transferBcteKeyList.size();
                }
                if ((constructionElemCount == 0) && isDIALOGDestinationProduct) {
                    showMsg(TranslationHandler.translate("!!In der Konstruktions-Stückliste %1 wurden keine zu übernehmenden Teilepositionen gefunden.",
                                                         hmMSmId.toStringForLogMessages()));
                }
            }
            count += constructionElemCount;
            pos++;
            if (showProgress) {
                fireProgress(pos, maxPos);
            }
        }
        if (showProgress) {
            hideProgress();
        }
        if (count == 0) {
            if (isDIALOGDestinationProduct) {
                showMsg(TranslationHandler.translate("!!Keine Konstruktions-Stücklisteneinträge mit Status \"offen\" gefunden."));
            } else {
                showMsg(TranslationHandler.translate("!!Es wurden keine zu übernehmenden Teilepositionen gefunden."));
            }
        }
        return count > 0;
    }

    /**
     * Überprüft für alle Ausführungsarten des Ziel-Produkts, ob der Stücklisteneintrag in der Konstruktion
     * vorhanden ist und den Status dokumentiert oder offen besitzt für DIALOG-Ziel-Produkte bzw. ob die Ausführungsart
     * des Stücklisteneintrags zum Ziel-Produkt passt sofern es sich bei der Quell-Stückliste überhaupt um eine DIALOG-Stückliste
     * handelt.
     *
     * @param retailElem
     * @param constAssembly
     * @param aaSet
     * @param aaSetForDestinationProduct
     * @param isDIALOGDestinationProduct
     * @return
     */
    private boolean checkTransferAllowed(TURetailElem retailElem, EtkDataAssembly constAssembly, Set<String> aaSet, Set<String> aaSetForDestinationProduct,
                                         boolean isDIALOGDestinationProduct) {
        boolean result = false;
        for (String aa : aaSet) {
            if (!isDIALOGDestinationProduct) {
                String sourceAA = retailElem.getBcteKey().getAA();
                if (StrUtils.isValid(sourceAA) && !aaSetForDestinationProduct.contains(sourceAA)) {
                    return false;
                }
            }

            iPartsDialogBCTEPrimaryKey searchBcteKey = retailElem.getSearchBcteKey(aa);
            if (!isDIALOGDestinationProduct || checkTransferStatus(searchBcteKey, constAssembly, retailElem.partListEntry.getAsId())) {
                retailElem.addToTransferList(searchBcteKey);
                result = true;
            }
        }
        return result;
    }

    /**
     * Suchen den Konstruktions-Stücklisteneintrags zum DIALOG-Schlüssel und Überprüfung des berechneten Status bzgl DOCU_RELEVANT_YES
     *
     * @param searchBcteKey
     * @param constAssembly
     * @return
     */
    private boolean checkTransferStatus(iPartsDialogBCTEPrimaryKey searchBcteKey, EtkDataAssembly constAssembly,
                                        PartListEntryId sourcePartListEntryId) {
        EtkDataPartListEntry constPartListEntry = constAssembly.getPartListEntryFromKLfdNr(searchBcteKey.createDialogGUID());
        if (constPartListEntry != null) {
            String calculatedDocuRelevantString = "";
            List<String> calculatedDocuRelevantSet = constPartListEntry.getFieldValueAsSetOfEnum(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT);
            if (!calculatedDocuRelevantSet.isEmpty()) {
                // SetOfEnum wird nur für die Filterung benötigt. Im Feld steht immer nur genau ein Wert drin.
                calculatedDocuRelevantString = calculatedDocuRelevantSet.get(0);
            }
            if (ALLOWED_FOR_TRANSFER_TO_AS.contains(iPartsDocuRelevant.getFromDBValue(calculatedDocuRelevantString))) {
                return true;
            } else {
                logMsg("  " + TranslationHandler.translate("!!Der Stücklisteneintrag \"%1\" besitzt nicht den Status \"offen\" (%2). Betroffene Quell-TU-Teileposition: %3",
                                                           searchBcteKey.createDialogGUID(), calculatedDocuRelevantString,
                                                           sourcePartListEntryId.toStringForLogMessages()));
            }
        } else {
            logMsg("  " + TranslationHandler.translate("!!Stücklisteneintrag zu \"%1\" nicht gefunden. Betroffene Quell-TU-Teileposition: %2",
                                                       searchBcteKey.createDialogGUID(),
                                                       sourcePartListEntryId.toStringForLogMessages()));
        }
        return false;
    }

    /**
     * Erzeugt aus dem HmMSmId-Schlüssel ein EtkDataAssembly und überprüft, ob es existiert
     *
     * @param hmMSmId
     * @return
     */
    private EtkDataAssembly createConstructionAssembly(HmMSmId hmMSmId) {
        List<iPartsVirtualNode> nodes = new ArrayList<>();

        nodes.add(new iPartsVirtualNode(iPartsNodeType.DIALOG_HMMSM, new iPartsSeriesId(hmMSmId.getSeries())));
        nodes.add(new iPartsVirtualNode(iPartsNodeType.HMMSM, hmMSmId));
        AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
        if ((assembly == null) || !assembly.existsInDB()) {
            showMsg(TranslationHandler.translate("!!Konstruktions-Stückliste %1 existiert nicht.", hmMSmId.toString("|")));
            assembly = null;
        }
        return assembly;
    }

    private void showMsg(List<String> msgs) {
        for (String msg : msgs) {
            showMsg(msg);
        }
    }

    private void showMsg(String msg) {
        showLogFormMessage(msg, MessageLogType.tmlMessage);
    }

    private void showError(String msg) {
        showLogFormMessage(msg, MessageLogType.tmlError);
    }

    private void showWarning(String msg) {
        showLogFormMessage(msg, MessageLogType.tmlWarning);
    }

    private void showLogFormMessage(String msg, MessageLogType logType) {
        EtkMessageLog messageLog = getMessageLogForExecution();
        if (messageLog != null) {
            messageLog.fireMessage(msg, logType, MessageLogOption.TIME_STAMP);
        }
    }

    private void logMsg(List<String> msgs) {
        for (String msg : msgs) {
            logMsg(msg);
        }
    }

    private void logMsg(String msg) {
        EtkMessageLog messageLog = getMessageLogForExecution();
        if (messageLog != null) {
            messageLog.fireMessage(msg, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }
    }

    private void logWarning(String msg) {
        EtkMessageLog messageLog = getMessageLogForExecution();
        if (messageLog != null) {
            messageLog.fireMessage(msg, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }
    }

    private void fireProgress(int pos, int maxPos) {
        EtkMessageLog messageLog = getMessageLogForExecution();
        if (messageLog != null) {
            messageLog.fireProgress(pos, maxPos, "", true, true);
        }
    }

    private void hideProgress() {
        EtkMessageLog messageLog = getMessageLogForExecution();
        if (messageLog != null) {
            messageLog.hideProgress();
        }
    }

    /**
     * {@link iPartsProductId} für das Ziel-Produkt
     *
     * @return
     */
    public iPartsProductId getTargetProductId() {
        return targetProductId;
    }

    /**
     * Alle Ziel-{@link AssemblyId}s
     *
     * @return
     */
    public Set<iPartsAssemblyId> getTargetAssemblyIds() {
        return Collections.unmodifiableSet(targetAssemblyIds);
    }

    /**
     * {@link EtkMessageLogForm} für das Ausführen vom Kopieren/Abgleich
     *
     * @return {@code null} falls die Verarbeitung im Hintergrund stattfindet
     */
    public EtkMessageLogForm getMessageLogFormForExecution() {
        return messageLogForm;
    }

    /**
     * {@link EtkMessageLog} für das Ausführen vom Kopieren/Abgleich
     *
     * @return
     */
    public EtkMessageLog getMessageLogForExecution() {
        return messageLogForExecution;
    }

    /**
     * Log-Datei für das Ausführen vom Kopieren/Abgleich
     *
     * @return
     */
    public DWFile getLogFileForExecution() {
        return logFile;
    }

    /**
     * {@link Session} für das Ausführen vom Kopieren/Abgleich
     *
     * @return
     */
    public Session getSessionForExecution() {
        return sessionForExecution;
    }

    /**
     * Wartet bis das Kopieren/Abgleich beendet ist
     *
     * @return
     */
    public boolean waitUntilFinished() {
        synchronized (finished) {
            if (!finished.getValue()) {
                try {
                    finished.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return !isCancelled.getValue();
    }

    /**
     * Wurde die Verarbeitung mit Fehlern beendet?
     *
     * @return
     */
    public boolean isFinishedWithErrors() {
        return finishedWithErrors;
    }

    public boolean isStoppedForFollowing() {
        return stopForFollowing;
    }

    /**
     * Verschiebt die Log-Datei abhängig vom Übernahmeergebnis.
     *
     * @param hasErrors
     * @param isCancelled
     */
    public void finishLogFile(boolean hasErrors, boolean isCancelled) {
        if (logFile != null) {
            if (hasErrors) {
                iPartsJobsManager.getInstance().jobError(logFile);
            } else if (isCancelled) {
                iPartsJobsManager.getInstance().jobCancelled(logFile, true);
            } else {
                iPartsJobsManager.getInstance().jobProcessed(logFile);
            }
        }
    }

    /**
     * Wurde die Verarbeitung im Hintergrund ausgeführt?
     *
     * @return
     */
    public boolean isExecutedInBackground() {
        return executedInBackground;
    }

    public static class TUCopyContainer {

        private AssemblyId sourceAssemblyId;
        private AssemblyId destinationAssemblyId;
        private KgTuId kgTuId;

        public TUCopyContainer(AssemblyId sourceAssemblyId, KgTuId kgTuId) {
            this.sourceAssemblyId = sourceAssemblyId;
            this.kgTuId = kgTuId;
        }

        public TUCopyContainer(AssemblyId sourceAssemblyId, AssemblyId destinationAssemblyId, KgTuId kgTuId) {
            this.sourceAssemblyId = sourceAssemblyId;
            this.destinationAssemblyId = destinationAssemblyId;
            this.kgTuId = kgTuId;
        }

        public TUCopyContainer(iPartsDataModuleEinPAS moduleEinPAS) {
            this(new AssemblyId(moduleEinPAS.getAsId().getModuleNumber(), ""),
                 new KgTuId(moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG),
                            moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU)));
        }

        public AssemblyId getSourceAssemblyId() {
            return sourceAssemblyId;
        }

        public AssemblyId getDestinationAssemblyId() {
            return destinationAssemblyId;
        }

        public KgTuId getKgTuId() {
            return kgTuId;
        }

        public boolean isKgTuEqual(TUCopyContainer copyContainer) {
            return kgTuId.equals(copyContainer.getKgTuId());
        }
    }


    private class TURetailElem {

        private boolean isPKWSourceProduct;
        private iPartsDialogBCTEPrimaryKey bcteKey;
        private iPartsDataPartListEntry partListEntry;
        private KgTuId kgTuId;
        private List<iPartsDialogBCTEPrimaryKey> transferBcteKeyList;
        private AssemblyId destinationAssemblyId;
        private boolean copyFootNotes;

        public TURetailElem(iPartsDataPartListEntry partListEntry, KgTuId kgTuId, boolean isPKWSourceProduct) {
            this.partListEntry = partListEntry;
            this.isPKWSourceProduct = isPKWSourceProduct;
            if (isPKWSourceProduct) {
                this.bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
            } else {
                // Dummy-BCTE-Schlüssel für Nicht-DIALOG-Quell-Produkte
                this.bcteKey = new iPartsDialogBCTEPrimaryKey("", "", "", "", "", "", "", "", "", "");
            }
            this.kgTuId = kgTuId;
            this.transferBcteKeyList = new DwList<>();
            this.destinationAssemblyId = null;
            this.copyFootNotes = false;
        }

        public boolean isInit() {
            return (partListEntry != null) && (bcteKey != null);
        }

        public HmMSmId getSourceHmMSmId() {
            if (isInit()) {
                return bcteKey.getHmMSmId();
            }
            return null;
        }

        public String getSourceHotSpot() {
            if (isInit()) {
                return partListEntry.getFieldValue(iPartsConst.FIELD_K_POS);
            }
            return null;
        }

        public boolean getOmmitted() {
            if (isInit()) {
                return partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT);
            }
            return false;
        }

        public iPartsDialogBCTEPrimaryKey getBcteKey() {
            return bcteKey;
        }

        public iPartsDialogBCTEPrimaryKey getSearchBcteKey(String aa) {
            if (isPKWSourceProduct) {
                iPartsDialogBCTEPrimaryKey clone = bcteKey.cloneMe();
                clone.aa = aa;
                return clone;
            }
            return bcteKey;
        }

        public iPartsDialogBCTEPrimaryKey getBcteKeyWithoutAA() {
            return getSearchBcteKey("");
        }

        public String getBcteGUIDWithoutAAandHotSpot() {
            return getBcteKeyWithoutAA().createDialogGUID() + iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER + getSourceHotSpot();
        }

        public AssemblyId getSourceAssemblyId() {
            if (isInit()) {
                return partListEntry.getAsId().getOwnerAssemblyId();
            }
            return null;
        }

        public KgTuId getKgTuId() {
            return kgTuId;
        }

        public void addToTransferList(iPartsDialogBCTEPrimaryKey bcteKey) {
            transferBcteKeyList.add(bcteKey);
        }

        public void setDestinationAssemblyId(AssemblyId destinationAssemblyId) {
            this.destinationAssemblyId = destinationAssemblyId;
        }

        public void setDestinationAssemblyId(iPartsProduct destinationProduct) {
            String assemblyName = EditModuleHelper.createStandardModuleName(destinationProduct.getAsId(), kgTuId);
            setDestinationAssemblyId(new AssemblyId(assemblyName, ""));
        }
    }


    private class ModuleTransferElem {

        private Map<String, List<TransferToASElement>> moduleMap;
        private Map<String, TransferToASElement> notExistingModuleMap;

        public ModuleTransferElem() {
            moduleMap = new LinkedHashMap<>();
            notExistingModuleMap = new LinkedHashMap<>();
        }

        public boolean isEmpty() {
            return moduleMap.isEmpty();
        }

        public void add(String destAssemblyName, TransferToASElement transferElem) {
            List<TransferToASElement> transferList = moduleMap.get(destAssemblyName);
            if (transferList == null) {
                transferList = new DwList<>();
                moduleMap.put(destAssemblyName, transferList);
                if (!updatePSKModules) { // Beim Abgleich von PSK-Modulen gibt es keine neuen Module
                    TransferToASElement notExistingTransferElem = notExistingModuleMap.get(destAssemblyName);
                    if (notExistingTransferElem == null) {
                        notExistingModuleMap.put(destAssemblyName, transferElem);
                    }
                }
            }
            transferList.add(transferElem);
        }

        public void removeAssemblyId(AssemblyId assemblyId) {
            List<TransferToASElement> transferList = moduleMap.get(assemblyId.getKVari());
            if (transferList != null) {
                for (TransferToASElement transferElem : transferList) {
                    transferElem.setAssemblyId(assemblyId);
                }
            }
            notExistingModuleMap.remove(assemblyId.getKVari());
        }
    }


    /**
     * Container für alle relevanten Daten zum Kopieren/Abgleichen von Modulen
     */
    public static class CopyOrUpdatePSKContainer {

        private Session sessionForGUI;
        private EditModuleFormConnector connectorForCopyUpdate;
        private boolean isMultiKGs;
        private Map<String, Set<iPartsCopyTUJobHelper.TUCopyContainer>> copyMap;
        private String viewerLanguage;
        private iPartsProductId sourceProductId;
        private iPartsProductId targetProductId;
        private EtkMessageLogForm messageLogFormForExecution;
        private EtkMessageLog messageLogForExecution;
        private DWFile logFileForExecution;
        private Session sessionForExecution;
        private int kgCounter;
        private boolean isCancelled;
        private boolean hasErrors;
        private boolean stoppedByUser;
        private boolean executeForAll;

        private iPartsDataAuthorOrder updatePSKDataAuthorOrder;

        public CopyOrUpdatePSKContainer(Session sessionForGUI, EditModuleFormConnector connectorForCopyUpdate, iPartsProductId sourceProductId,
                                        iPartsProductId targetProductId, Map<String, Set<iPartsCopyTUJobHelper.TUCopyContainer>> copyMap,
                                        String viewerLanguage) {
            this.sessionForGUI = sessionForGUI;
            this.connectorForCopyUpdate = connectorForCopyUpdate;
            this.sourceProductId = sourceProductId;
            this.targetProductId = targetProductId;
            this.copyMap = copyMap;
            this.viewerLanguage = viewerLanguage;
            this.isMultiKGs = copyMap.size() > 1;
            this.executeForAll = false;
        }

        public boolean isStoppedByUser() {
            return stoppedByUser;
        }

        public void setStoppedByUser(boolean value) {
            stoppedByUser = value;
        }

        public boolean hasErrors() {
            return hasErrors;
        }

        public void setHasErrors() {
            hasErrors = true;
        }

        public boolean isCancelled() {
            return isCancelled;
        }

        public int getKgCounter() {
            return kgCounter;
        }

        public void incKgCounter() {
            kgCounter += 1;
        }

        public void setIsCancelled() {
            isCancelled = true;
        }

        public boolean isMessageLogForExecutionValid() {
            return messageLogForExecution != null;
        }

        public void fireEndMessage(boolean isLastKG) {
            if (isLastKG && isMultiKGs() && isMessageLogForExecutionValid()) {
                String msg;
                if (hasErrors()) {
                    msg = "!!Gesamte Verarbeitung mit Fehlern abgeschlossen";
                } else if (isCancelled()) {
                    msg = "!!Verarbeitung teilweise abgeschlossen mit Abbruch durch Benutzer";
                } else {
                    msg = "!!Gesamte Verarbeitung abgeschlossen";
                }
                messageLogForExecution.fireMessage(TranslationHandler.translateForLanguage(msg, viewerLanguage),
                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
        }

        public void fireBackgroundEndMessage(String title, boolean finished) {
            if (sessionForGUI.isActive()) {
                sessionForGUI.invokeThreadSafeInSessionThread(() -> {
                    if (finished) {
                        MessageDialog.show("!!Ausführung im Hintergrund abgeschlossen.", title);
                    } else {
                        MessageDialog.showWarning("!!Ausführung im Hintergrund wurde abgebrochen.", title);
                    }
                });
            }
        }

        public boolean isTargetProductIdValid() {
            return targetProductId != null;
        }

        public boolean isMultiKGs() {
            return isMultiKGs;
        }

        public Map<String, Set<iPartsCopyTUJobHelper.TUCopyContainer>> getCopyMap() {
            return copyMap;
        }

        public boolean isLastKg() {
            return getKgCounter() == getCopyMap().size();
        }

        public void setUpdatePSKDataAuthorOrder(iPartsDataAuthorOrder updatePSKdataAuthorOrder) {
            this.updatePSKDataAuthorOrder = updatePSKdataAuthorOrder;
        }

        public void disposeConnectorAndProjectForCopyUpdate() {
            connectorForCopyUpdate.getProject().setDBActive(false, false);
            connectorForCopyUpdate.dispose();
        }

        public Session getSessionForExecution() {
            return sessionForExecution;
        }

        protected boolean isSessionForExecutionValid() {
            return getSessionForExecution() != null;
        }

        public Session getActualSession() {
            return isSessionForExecutionValid() ? getSessionForExecution() : getSessionForGUI();
        }

        public Session getSessionForGUI() {
            return sessionForGUI;
        }

        public void copyHelperResultsForNextKg(iPartsCopyTUJobHelper helper) {
            // Session für die Ausführung merken für die nächste KG
            sessionForExecution = helper.getSessionForExecution();

            // EtkMessageLogForm für die Ausführung merken für die nächste KG (kann zwischendrin
            // auch wieder null werden, wenn das EtkMessageLogForm durch den Start der
            // Ausführung im Hintergrund geschlossen wird)
            messageLogFormForExecution = helper.getMessageLogFormForExecution();

            // EtkMessageLog für die Ausführung merken für die nächste KG
            messageLogForExecution = helper.getMessageLogForExecution();

            // Log-Datei für die Ausführung merken für die nächste KG
            logFileForExecution = helper.getLogFileForExecution();

            // Ziel-Produkt merken für die nächste KG
            targetProductId = helper.getTargetProductId();
            updatePSKDataAuthorOrder = helper.updatePSKDataAuthorOrder;
            executeForAll = helper.executeForAll;
        }
    }
}
