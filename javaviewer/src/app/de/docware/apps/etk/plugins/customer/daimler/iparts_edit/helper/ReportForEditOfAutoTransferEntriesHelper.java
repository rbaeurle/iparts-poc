/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.mechanic.AssemblyFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.EnumSet;

/**
 * Hilfsklasse für das Erzeugen von Auswertemöglichkeiten zur Identifikation von Autorenänderungen an automatisch erzeugten
 * Teilepositionen.
 */
public class ReportForEditOfAutoTransferEntriesHelper {

    public static final String IPARTS_MENU_ITEM_CALC_REPORT_FOR_EDIT_OF_AUTO_ENTRIES = "iPartsMenuItemCalcReportForEditOfAutoEntries";
    public static final String MENU_ITEM_TEXT_CALC_REPORT_FOR_EDIT_OF_AUTO_ENTRIES = "!!Auswertung von Änderungen an autom. erzeugten Teilepos.";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.PRODUCT_KGTU,
                                                                                   iPartsModuleTypes.KG,
                                                                                   iPartsModuleTypes.TU,
                                                                                   iPartsModuleTypes.DialogRetail);

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractAssemblyTreeForm formWithTree) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_CALC_REPORT_FOR_EDIT_OF_AUTO_ENTRIES,
                                                                                       MENU_ITEM_TEXT_CALC_REPORT_FOR_EDIT_OF_AUTO_ENTRIES, null);
        menuItem.setIcon(EditDefaultImages.save.getImage());
        addCalcReportListener(menuItem, formWithTree.getConnector());
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        if (!iPartsRight.REPORT_EDIT_OF_AUTO_TRANSFER_ENTRIES.checkRightInSession()) {
            AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_CALC_REPORT_FOR_EDIT_OF_AUTO_ENTRIES, false);
        } else {
            GuiMenuItem menu = AbstractRelatedInfoPartlistDataForm.updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_CALC_REPORT_FOR_EDIT_OF_AUTO_ENTRIES,
                                                                                       VALID_MODULE_TYPES);
            if ((menu != null) && menu.isVisible()) {
                AssemblyId assemblyId = connector.getCurrentAssembly().getAsId();
                if (assemblyId.isVirtual()) {
                    String productNo = iPartsVirtualNode.getProductNumberFromAssemblyId(assemblyId);
                    if (StrUtils.isValid(productNo)) {
                        iPartsProduct product = iPartsProduct.getInstance(connector.getProject(), new iPartsProductId(productNo));
                        if (!product.getDocumentationType().isPKWDocumentationType()) {
                            menu.setVisible(false);
                        }
                    }
                }
            }
        }
    }

    private static void addCalcReportListener(final GuiMenuItem menuItem, final AssemblyFormIConnector connector) {
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
                    MessageDialog.showError("!!Fehler bei der Initialisierung für die Berechnung im Hintergrund.");
                    return;
                }

                projectForReport.getConfig().setCurrentViewerLanguage(connector.getProject().getViewerLanguage());
                projectForReport.getConfig().setCurrentDatabaseLanguage(connector.getProject().getDBLanguage());

                sessionForGUI.startChildThread(thread -> {
                    try {
                        ReportForEditOfAutoTransferEntriesWorker worker = new ReportForEditOfAutoTransferEntriesWorker();
                        worker.startCalculation(projectForReport, connector.getCurrentAssembly().getAsId());
                        if (sessionForGUI.isActive() && !Thread.currentThread().isInterrupted()) {
                            sessionForGUI.invokeThreadSafe(new Runnable() {
                                @Override
                                public void run() {
                                    String msg = "!!Download komplett.";
                                    if (!worker.downloadFile()) {
                                        msg = "!!Keine Download-Datei vorhanden!";
                                        String extraNote = worker.getExtraNotes();
                                        if (StrUtils.isValid(extraNote)) {
                                            msg = extraNote + ".";
                                        }
                                    }
                                    MessageDialog.show(msg);
                                }
                            });
                        }
                    } finally {
                        projectForReport.setDBActive(false, false);
                    }
                });
                MessageDialog.show("!!Die Berechnung wurde im Hintergrund gestartet.", MENU_ITEM_TEXT_CALC_REPORT_FOR_EDIT_OF_AUTO_ENTRIES);
            }
        });
    }
}