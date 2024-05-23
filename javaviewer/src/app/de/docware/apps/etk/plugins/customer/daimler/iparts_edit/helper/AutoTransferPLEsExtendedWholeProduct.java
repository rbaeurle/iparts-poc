/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.framework.modules.gui.session.SessionType;
import de.docware.util.StrUtils;

/**
 * Automatische Submodul-Verarbeitung übers ganze Produkt
 */
public class AutoTransferPLEsExtendedWholeProduct {

    public static final String IPARTS_MENU_ITEM_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT =
            "iPartsMenuItemAutoTransferPartListEntriesExtendedWholeProduct";
    public static final String IPARTS_MENU_ITEM_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT_TEXT =
            "!!Automatische Submodul-Verarbeitung übers ganze Produkt";
    public static final String IPARTS_ASK_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT_TEXT =
            "!!Automatische Submodul-Verarbeitung übers ganze Produkt starten?";

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        GuiMenuItem menuItem =
                AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT,
                                                                            IPARTS_MENU_ITEM_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT_TEXT, DefaultImages.module.getImage(),
                                                                            null);
        menuItem.addEventListener(new EventListener<>(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                EtkDataAssembly assembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector(connector);
                doAutoTransferPLESForProduct(connector, assembly);
            }
        });
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        EtkDataAssembly destAssembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector(connector);
        boolean isVisible = isMenuAutoTranferPLESVisible(connector.getProject(), destAssembly);
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT, isVisible);
    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractAssemblyTreeForm formWithTree) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT,
                                                                                       IPARTS_MENU_ITEM_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT_TEXT, null);
        menuItem.addEventListener(new EventListener<>(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                AssemblyTreeFormIConnector connector = formWithTree.getConnector();
                doAutoTransferPLESForProduct(connector, connector.getCurrentAssembly());
            }
        });
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        boolean isVisible = isMenuAutoTranferPLESVisible(connector.getProject(), connector.getCurrentAssembly());
        AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT, isVisible);
    }

    private static boolean isMenuAutoTranferPLESVisible(EtkProject project, EtkDataAssembly currentAssembly) {
        return iPartsRight.AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT.checkRightInSession()
               && isProductValidForAutoTransfer(project, currentAssembly)
               && !project.isRevisionChangeSetActiveForEdit();
    }

    private static boolean isProductValidForAutoTransfer(EtkProject project, EtkDataAssembly currentAssembly) {
        String productNo = getProductNoFromVirtutalId(currentAssembly);
        if (StrUtils.isValid(productNo)) {
            iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNo));
            return !product.isRetailRelevantFromDB() && product.getDocumentationType().isDIALOGDocumentationType() && (product.getReferencedSeries() != null);
        }
        return false;
    }

    private static String getProductNoFromVirtutalId(EtkDataAssembly currentAssembly) {
        if (currentAssembly != null) {
            AssemblyId assemblyId = currentAssembly.getAsId();
            if (assemblyId.isVirtual()) {
                return iPartsVirtualNode.getProductNumberFromAssemblyId(assemblyId);
            }
        }
        return "";
    }

    private static void doAutoTransferPLESForProduct(AbstractJavaViewerFormIConnector connector, EtkDataAssembly assembly) {
        String productNo = getProductNoFromVirtutalId(assembly);
        if (StrUtils.isEmpty(productNo)) {
            return;
        }
        if (MessageDialog.showYesNo(IPARTS_ASK_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT_TEXT) != ModalResult.YES) {
            return;
        }

        ImportExportLogHelper logHelper = ImportExportLogHelper.createLogHelperWithRunningJob("AutoTransferSubmodulesForWholeProduct");
        logHelper.addLogMsgWithTranslation("!!Starte automatische Submodul-Verarbeitung über das ganze Produkt \"%1\".", productNo);
        final Session sessionForGUI = Session.get();
        final Session sessionForTransfer = EtkEndpointHelper.createSession(SessionType.ENDPOINT, false); // Typ ENDPOINT, weil diese Hintergrund-Session keine GUI hat

        // Attribute der Original-Session übernehmen außer TranslationHandler und MainConnector (z.B. für die Benutzer-ID)
        sessionForTransfer.assignAttributes(sessionForGUI, Constants.SESSION_KEY_DEFAULT_TRANSLATION_HANDLER, JavaViewerApplication.SESSION_KEY_MAIN_CONNECTOR);

        final EtkProject projectForTransfer = EtkEndpointHelper.createProjectAndStoreItInEndpointSession(sessionForTransfer);
        if (projectForTransfer == null) {
            SessionManager.getInstance().destroySession(sessionForTransfer);
            String errorText = "!!Fehler bei der Initialisierung für die automatische Submodul-Verarbeitung im Hintergrund.";
            MessageDialog.showError(errorText);
            logHelper.addLogErrorWithTranslation(errorText);
            iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
            return;
        }

        projectForTransfer.getConfig().setCurrentViewerLanguage(connector.getProject().getViewerLanguage());
        projectForTransfer.getConfig().setCurrentDatabaseLanguage(connector.getProject().getDBLanguage());

        sessionForTransfer.startChildThread(thread -> {
            boolean noErrors = true;
            try {
                TranslationHandler.getUiTranslationHandler().setCurrentLanguage(logHelper.getLogLanguage());
                AutoTransferPLEsExtendedWholeProductHelper helper = new AutoTransferPLEsExtendedWholeProductHelper(sessionForTransfer, projectForTransfer, logHelper);
                if (sessionForTransfer.isActive() && !Thread.currentThread().isInterrupted()) {
                    projectForTransfer.getDbLayer().startTransaction();
                    try {
                        iPartsProduct product = iPartsProduct.getInstance(projectForTransfer, new iPartsProductId(productNo));
                        noErrors = helper.transferPLEsExtendedWholeProduct(product);
                        if (AutoTransferPLEsExtendedWholeProductHelper.TEST_SINGLE_SUBMODULE_WITH_ROLLBACK) {
                            projectForTransfer.getDbLayer().rollback();
                        } else {
                            if (noErrors) {
                                projectForTransfer.getDbLayer().commit();
                                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
                            } else {
                                projectForTransfer.getDbLayer().rollback();
                            }
                        }
                    } catch (Exception e) {
                        logHelper.addLogError(e.getMessage());
                        projectForTransfer.getDbLayer().rollback();
                        noErrors = false;
                    }

                    String msg = noErrors ? "!!Automatische Submodul-Verarbeitung beendet." : "!!Automatische Submodul-Verarbeitung mit Fehlern beendet.";
                    logHelper.addLogMsgWithTranslation(msg);
                    if (sessionForGUI.isActive()) {
                        sessionForGUI.invokeThreadSafeInSessionThread(() -> MessageDialog.show(msg));
                    }
                }
            } finally {
                // LogHelper schließen
                if (noErrors) {
                    iPartsJobsManager.getInstance().jobProcessed(logHelper.getLogFile());
                } else {
                    iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
                }
                projectForTransfer.setDBActive(false, false);
                SessionManager sessionManager = SessionManager.getInstance();
                sessionManager.deregisterThreadForSession(sessionForTransfer, Thread.currentThread()); // Damit die Session sauber entfernt werden kann
                sessionManager.destroySession(sessionForTransfer);
            }
        });
        MessageDialog.show("!!Automatische Submodul-Verarbeitung wurde im Hintergrund gestartet.", IPARTS_MENU_ITEM_AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT_TEXT);
    }

}
