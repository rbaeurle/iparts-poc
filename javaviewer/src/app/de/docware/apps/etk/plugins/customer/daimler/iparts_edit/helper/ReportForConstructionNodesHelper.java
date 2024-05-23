/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.mechanic.AssemblyFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReportConstNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsReportConstNodeId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyDialogBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;

import java.util.EnumSet;
import java.util.List;

/**
 * Hilfsklasse für das Erzeugen von Auswertungen bzgl. der offenen und geänderten Teilepositionen in der Konstruktions-Sicht
 * von iParts.
 */
public class ReportForConstructionNodesHelper {

    public static final String AUTO_EXPORTED_SERIES_LANGUAGE = Language.DE.getCode(); // Deutsch
    public static final String IPARTS_MENU_ITEM_CALC_REPORT_FOR_CONST_NODE = "iPartsMenuItemCalcReportForConstNode";
    public static final String MENU_ITEM_TEXT_CALC_REPORT_FOR_CONST_NODE = "!!Auswertung für Teilepositionen berechnen";
    public static final String MENU_ITEM_TEXT_CALC_REPORT_FOR_CONST_NODE_RUNNING = "!!Auswertung für Teilepositionen läuft";
    public static final String IPARTS_MENU_ITEM_EXPORT_CSV_FOR_CONST_NODE = "iPartsMenuItemExportCSVForConstNode";
    public static final String MENU_ITEM_TEXT_EXPORT_CSV_FOR_CONST_NODE = "!!Stücklisten exportieren";
    public static final String IPARTS_MENU_ITEM_DOWNLOAD_EXPORTED_SERIES = "iPartsMenuItemDownloadExportedSeries";
    public static final String MENU_ITEM_TEXT_DOWNLOAD_EXPORTED_SERIES = "!!Exportierte Stücklisten der Baureihe herunterladen";
    public static final String MENU_ITEM_TEXT_EXPORT_CSV_FOR_CONST_NODE_RUNNING = "!!Export Stücklisten läuft";
    public static final boolean changeMenuTextForCalcReport = false;  // Menu-Text bei laufendem Calc-Report ändern

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.CONSTRUCTION_SERIES,
                                                                                   iPartsModuleTypes.Dialog_HM_Construction,
                                                                                   iPartsModuleTypes.Dialog_M_Construction,
                                                                                   iPartsModuleTypes.Dialog_SM_Construction);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_CALC_REPORT_FOR_CONST_NODE,
                                                                                           MENU_ITEM_TEXT_CALC_REPORT_FOR_CONST_NODE, DefaultImages.module.getImage(),
                                                                                           null);
        addCalcReportForConstNodeEventListener(menuItem, connector, true, false);
        menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_EXPORT_CSV_FOR_CONST_NODE,
                                                                               MENU_ITEM_TEXT_EXPORT_CSV_FOR_CONST_NODE, EditDefaultImages.save.getImage(),
                                                                               null);
        addCalcReportForConstNodeEventListener(menuItem, connector, true, true);

        menuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_DOWNLOAD_EXPORTED_SERIES,
                                                                               MENU_ITEM_TEXT_DOWNLOAD_EXPORTED_SERIES, EditDefaultImages.save.getImage(),
                                                                               null);
        addDownloadSeriesListener(menuItem, connector, true);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        EtkDataAssembly destAssembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector(connector);

        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_CALC_REPORT_FOR_CONST_NODE,
                                                                                           AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly,
                                                                                                                                                    VALID_MODULE_TYPES));
        // Ab Daimler-9386 gibt es ein neues Recht zur Anzeige der konstruktiven Stückliste. Deswegen explizit auf EDIT_PARTS_DATA abfragen
        boolean isVisible = iPartsRight.EDIT_PARTS_DATA.checkRightInSession() && handleReportMenu(menuItem, false, connector.getProject(), destAssembly);
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_CALC_REPORT_FOR_CONST_NODE, isVisible);

        menuItem = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_EXPORT_CSV_FOR_CONST_NODE,
                                                                               AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(destAssembly,
                                                                                                                                        VALID_MODULE_TYPES));
        // Ab Daimler-9386 gibt es ein neues Recht zur Anzeige der konstruktiven Stückliste. Deswegen explizit auf EDIT_PARTS_DATA abfragen
        isVisible = iPartsRight.EDIT_PARTS_DATA.checkRightInSession() && handleReportMenu(menuItem, true, connector.getProject(), destAssembly);
        AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_EXPORT_CSV_FOR_CONST_NODE, isVisible);

        checkSeriesDownloadVisibility(connector.getProject(), destAssembly, popupMenu);

    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractAssemblyTreeForm formWithTree) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_CALC_REPORT_FOR_CONST_NODE,
                                                                                       MENU_ITEM_TEXT_CALC_REPORT_FOR_CONST_NODE, null);
        addCalcReportForConstNodeEventListener(menuItem, formWithTree.getConnector(), false, false);
        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_EXPORT_CSV_FOR_CONST_NODE,
                                                                           MENU_ITEM_TEXT_EXPORT_CSV_FOR_CONST_NODE, null);
        menuItem.setIcon(EditDefaultImages.save.getImage());
        addCalcReportForConstNodeEventListener(menuItem, formWithTree.getConnector(), false, true);
        menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_DOWNLOAD_EXPORTED_SERIES,
                                                                           MENU_ITEM_TEXT_DOWNLOAD_EXPORTED_SERIES, null);
        menuItem.setIcon(EditDefaultImages.save.getImage());
        addDownloadSeriesListener(menuItem, formWithTree.getConnector(), false);
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_CALC_REPORT_FOR_CONST_NODE,
                                                                                       VALID_MODULE_TYPES);
        // Ab Daimler-9386 gibt es ein neues Recht zur Anzeige der konstruktiven Stückliste. Deswegen explizit auf EDIT_PARTS_DATA abfragen
        boolean isVisible = iPartsRight.EDIT_PARTS_DATA.checkRightInSession() && handleReportMenu(menuItem, false, connector.getProject(), connector.getCurrentAssembly());
        AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_CALC_REPORT_FOR_CONST_NODE, isVisible);
        menuItem = AbstractRelatedInfoPartlistDataForm.updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_EXPORT_CSV_FOR_CONST_NODE,
                                                                           VALID_MODULE_TYPES);
        // Ab Daimler-9386 gibt es ein neues Recht zur Anzeige der konstruktiven Stückliste. Deswegen explizit auf EDIT_PARTS_DATA abfragen
        isVisible = iPartsRight.EDIT_PARTS_DATA.checkRightInSession() && handleReportMenu(menuItem, true, connector.getProject(), connector.getCurrentAssembly());
        AbstractRelatedInfoPartlistDataForm.setVisibilityForItem(popupMenu, IPARTS_MENU_ITEM_EXPORT_CSV_FOR_CONST_NODE, isVisible);
        checkSeriesDownloadVisibility(connector.getProject(), connector.getCurrentAssembly(), popupMenu);
    }

    private static boolean handleReportMenu(GuiMenuItem menuItem, boolean isCSVExport, EtkProject project, EtkDataAssembly assembly) {
        if (menuItem != null) {
            if (changeMenuTextForCalcReport) {
                if (menuItem.isVisible()) {
                    menuItem.setEnabled(true);
                    if (isCSVExport) {
                        menuItem.setText(MENU_ITEM_TEXT_EXPORT_CSV_FOR_CONST_NODE);
                    } else {
                        menuItem.setText(MENU_ITEM_TEXT_CALC_REPORT_FOR_CONST_NODE);
                    }
                    String seriesNo = getSeriesNoFromSeriesOrHmMSmNode(assembly);
                    if (StrUtils.isValid(seriesNo)) {
                        iPartsReportConstNodeId lockReportConstNodeId = new iPartsReportConstNodeId(seriesNo, "", "");
                        iPartsDataReportConstNode lockDataReportConstNode = new iPartsDataReportConstNode(project, lockReportConstNodeId);
                        if (lockDataReportConstNode.existsInDB()) {
                            String calculationDate = lockDataReportConstNode.getCalculationDate();
                            if (iPartsVirtualAssemblyDialogBase.isHmMSmNodeCalculationValid(calculationDate)) {
                                menuItem.setEnabled(false);
                                // text ändern
                                if (isCSVExport) {
                                    menuItem.setText(MENU_ITEM_TEXT_EXPORT_CSV_FOR_CONST_NODE_RUNNING);
                                } else {
                                    menuItem.setText(MENU_ITEM_TEXT_CALC_REPORT_FOR_CONST_NODE_RUNNING);
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                if (isSeriesOrHmMSmNode(assembly)) {
                    HmMSmNode hmMSmNode = iPartsVirtualNode.getHmMSmNodeForAssemblyId(assembly.getAsId(), project);
                    if (hmMSmNode != null) { // HM/M/SM-Knoten
                        if (isCSVExport) {
                            return !hmMSmNode.isHiddenRecursively();
                        } else {
                            return !hmMSmNode.isNoCalcRecursively();
                        }
                    } else {
                        return true; // Baureihe
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSeriesOrHmMSmNode(EtkDataAssembly assembly) {
        return StrUtils.isValid(getSeriesNoFromSeriesOrHmMSmNode(assembly));
    }

    private static String getSeriesNoFromSeriesOrHmMSmNode(EtkDataAssembly assembly) {
        iPartsSeriesId seriesId = null;
        HmMSmNode hmMSmNode = null;
        if (assembly != null) {
            iPartsVirtualNode virtualRootNode = iPartsVirtualNode.getVirtualRootNodeFromAssemblyId(assembly.getAsId());
            if ((virtualRootNode != null) && (virtualRootNode.getType() == iPartsNodeType.DIALOG_HMMSM)) {
                seriesId = iPartsVirtualNode.getSeriesIdForAssemblyId(assembly.getAsId());
                if (seriesId == null) {
                    hmMSmNode = iPartsVirtualNode.getHmMSmNodeForAssemblyId(assembly.getAsId(), assembly.getEtkProject());
                }
            }
        }

        String seriesNo = null;
        if (seriesId != null) {
            seriesNo = seriesId.getSeriesNumber();
        } else if (hmMSmNode != null) {
            HmMSmId hmMSmId = hmMSmNode.getId();
            seriesNo = hmMSmId.getSeries();
        }
        return seriesNo;
    }

    private static void addCalcReportForConstNodeEventListener(final GuiMenuItem menuItem, final AssemblyFormIConnector connector,
                                                               final boolean isPartListPopupMenu, final boolean isCSVExport) {
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                // Check, ob es schon einen Export gibt
                if (isCSVExport && checkDownloadInsteadOfExport(connector, isPartListPopupMenu)) {
                    return;
                }
                doReporting(connector, isPartListPopupMenu, isCSVExport);
            }
        });
    }

    /**
     * Ermöglicht dem Benutzer einen bestehenden Export herunterzuladen anstelle einer kompletten Neuberechnung
     *
     * @param connector
     * @param isPartListPopupMenu
     * @return
     */
    private static boolean checkDownloadInsteadOfExport(AssemblyFormIConnector connector, boolean isPartListPopupMenu) {
        iPartsDataAssembly assembly = getAssemblyFromConnector(connector, isPartListPopupMenu);
        // Check, ob es sich um eine Baureihen Assembly handelt
        if ((assembly != null) && assembly.isVirtual() && assembly.isConstructionSeriesAssembly()) {
            DWFile existingExport = getDownloadFileForCurrentSeries(assembly);
            // Wenn eine Exportdatei existiert, muss geprüft werden, ob sie bezüglich dem eingestellten Zeitintervall
            // gültig ist
            if ((existingExport != null) && checkExportFileValidityWithDelete(existingExport, assembly)) {
                iPartsSeriesId seriesId = getSeriesIdFromAssembly(assembly);
                if (seriesId != null) {
                    String dateInFilename = getDateFromFilename(seriesId, existingExport);
                    String message;
                    if (DateUtils.isValidDateTime_yyyyMMddHHmmss(dateInFilename)) {
                        DateConfig dateConfig = DateConfig.getInstance(connector.getConfig());
                        dateInFilename = dateConfig.formatDateTime(connector.getProject().getViewerLanguage(), dateInFilename);
                        message = TranslationHandler.translate("!!Für die Baureihe \"%1\" existiert schon ein Export der Stücklisten mit dem Zeitstempel %2.",
                                                               seriesId.getSeriesNumber(), dateInFilename);
                    } else {
                        message = TranslationHandler.translate("!!Für die Baureihe \"%1\" existiert schon ein Export der Stücklisten.",
                                                               seriesId.getSeriesNumber());
                    }
                    if (MessageDialog.showYesNo(message + "\n\n" + TranslationHandler.translate("!!Soll statt einer Neuberechnung der existierende Export heruntergeladen werden?"),
                                                MENU_ITEM_TEXT_EXPORT_CSV_FOR_CONST_NODE) == ModalResult.YES) {
                        existingExport.downloadFile();
                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.DEBUG, "User \"" + iPartsUserAdminDb.getLoginUserName()
                                                                                                       + "\" chose download instead of a recalculation");
                        return true;
                    } else {
                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.DEBUG, "User \"" + iPartsUserAdminDb.getLoginUserName()
                                                                                                       + "\" chose recalculation instead of download");
                    }
                }
            }
        }
        return false;
    }

    /**
     * Liefert das Exportdatum aus dem Dateinamen der Exportdatei
     *
     * @param seriesId
     * @param existingExport
     * @return
     */
    private static String getDateFromFilename(iPartsSeriesId seriesId, DWFile existingExport) {
        String prefix = ReportForConstructionCSVExport.getSeriesPrefixForFilename(seriesId);
        String filename = existingExport.extractFileName(false);
        return StrUtils.stringAfterCharacter(filename, prefix);
    }

    private static iPartsDataAssembly getAssemblyFromConnector(AssemblyFormIConnector connector, boolean isPartListPopupMenu) {
        EtkDataAssembly assembly = null;
        if (isPartListPopupMenu) {
            if (connector instanceof AssemblyListFormIConnector) {
                assembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector((AssemblyListFormIConnector)connector);
            }
        } else {
            assembly = connector.getCurrentAssembly();
        }
        if (assembly instanceof iPartsDataAssembly) {
            return (iPartsDataAssembly)assembly;
        }
        return null;
    }

    private static void addDownloadSeriesListener(final GuiMenuItem menuItem, final AssemblyFormIConnector connector,
                                                  final boolean isPartListPopupMenu) {
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                downloadSeriesExport(connector, isPartListPopupMenu);
            }
        });
    }

    /**
     * Ermöglicht das Herunterladen von exportierten Dateien zur ausgewählten Baureihe.
     *
     * @param connector
     * @param isPartListPopupMenu
     */
    private static void downloadSeriesExport(AssemblyFormIConnector connector, boolean isPartListPopupMenu) {
        EtkDataAssembly assembly = getAssemblyFromConnector(connector, isPartListPopupMenu);
        DWFile downloadFile = getDownloadFileForCurrentSeries(assembly);
        if (downloadFile != null) {
            downloadFile.downloadFile();
        }
    }

    /**
     * Liefert die aktuelle Exportdatei zur aktuellen Baureihe
     *
     * @param assembly
     * @return
     */
    private static DWFile getDownloadFileForCurrentSeries(EtkDataAssembly assembly) {
        if (assembly != null) {
            DWFile autoCalcAndExportDir = iPartsEditPlugin.getDirForAutoCalcAndExport();
            // Check, ob das eingestellte Verzeichnis existiert
            if ((autoCalcAndExportDir != null) && autoCalcAndExportDir.exists(1000) && !autoCalcAndExportDir.isEmpty()) {
                iPartsSeriesId seriesId = getSeriesIdFromAssembly(assembly);
                if (seriesId != null) {
                    String seriesPrefix = ReportForConstructionCSVExport.getSeriesPrefixForFilename(seriesId);
                    if (StrUtils.isValid(seriesPrefix)) {
                        List<DWFile> filesForSeries = ReportForConstructionCSVExport.getExportFilesForSeries(seriesId);
                        if (!filesForSeries.isEmpty()) {
                            return filesForSeries.get(0);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static iPartsSeriesId getSeriesIdFromAssembly(EtkDataAssembly destAssembly) {
        if (destAssembly == null) {
            return null;
        }
        return iPartsVirtualNode.getSeriesIdForAssemblyId(destAssembly.getAsId());
    }

    private static void checkSeriesDownloadVisibility(EtkProject project, EtkDataAssembly assembly, GuiContextMenu popupMenu) {
        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_DOWNLOAD_EXPORTED_SERIES,
                                                                                           AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(assembly,
                                                                                                                                                    EnumSet.of(iPartsModuleTypes.CONSTRUCTION_SERIES)));
        if ((menuItem != null) && menuItem.isVisible()) {
            boolean isVisible = false;
            // Check, ob im Moment eine Berechnung läuft
            if (!isSeriesExportRunning(project, assembly)) {
                // Falls nein -> Check, ob die Exportdatei noch gültig ist
                DWFile exportFile = getDownloadFileForCurrentSeries(assembly);
                if (exportFile != null) {
                    isVisible = checkExportFileValidityWithDelete(exportFile, assembly);
                }
            }
            AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_DOWNLOAD_EXPORTED_SERIES,
                                                                        isVisible);
        }
    }

    /**
     * Überprüft, ob die Exportdatei bezüglich dem eingestellten Zeitintervall (Adminoption) noch gültig ist. Falls nicht,
     * wird die Datei gelöscht.
     *
     * @param exportFile
     * @param assembly
     * @return
     */
    private static boolean checkExportFileValidityWithDelete(DWFile exportFile, EtkDataAssembly assembly) {
        boolean result = false;
        if (exportFile != null) {
            iPartsSeriesId seriesId = getSeriesIdFromAssembly(assembly);
            if (seriesId != null) {
                String dateFromFilename = getDateFromFilename(seriesId, exportFile);
                result = iPartsVirtualAssemblyDialogBase.isHmMSmNodeCalculationValid(dateFromFilename);
            }
            if (!result) {
                exportFile.deleteRecursivelyWithRepeat(5);
            }
        }
        return result;
    }

    /**
     * Überprüft, ob für die ausgewählte Baureihe eine Berechnung läuft
     *
     * @param project
     * @param assembly
     * @return
     */
    private static boolean isSeriesExportRunning(EtkProject project, EtkDataAssembly assembly) {
        iPartsSeriesId seriesId = getSeriesIdFromAssembly(assembly);
        if (seriesId == null) {
            return false;
        }
        iPartsReportConstNodeId lockReportConstNodeId = new iPartsReportConstNodeId(seriesId.getSeriesNumber(), "", "");
        iPartsDataReportConstNode lockDataReportConstNode = new iPartsDataReportConstNode(project, lockReportConstNodeId);
        if (lockDataReportConstNode.existsInDB()) {
            String calculationDate = lockDataReportConstNode.getCalculationDate();
            if (iPartsVirtualAssemblyDialogBase.isHmMSmNodeCalculationValid(calculationDate)) {
                return true;
            }
        }
        return false;
    }

    private static void doReporting(AssemblyFormIConnector connector, boolean isPartListPopupMenu, final boolean isCSVExport) {
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        AssemblyId parentAssemblyId = null;

        // DestinationAssembly in der Stückliste
        if (isPartListPopupMenu) {
            if (connector instanceof AssemblyListFormIConnector) {
                parentAssemblyId = assembly.getAsId();
                assembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector((AssemblyListFormIConnector)connector);
                if (assembly == null) {
                    return;
                }
            }
        } else if (assembly instanceof iPartsDataAssembly) {
            parentAssemblyId = ((iPartsDataAssembly)assembly).getFirstParentAssemblyIdFromParentEntries();
        }
        doReporting(connector.getProject(), assembly, parentAssemblyId, isCSVExport, false);
    }

    public static FrameworkThread doReporting(final EtkProject projectForGUI, EtkDataAssembly assembly, AssemblyId parentAssemblyId,
                                              final boolean isCSVExport, final boolean isAutoCalcAndExport) {


        final AssemblyId parentAssemblyIdFinal = parentAssemblyId;
        final String assemblyHeading = assembly.getHeading1(-1, null);

        // ChangeSet bei der Berechnung berücksichtigen oder nicht?
        AbstractRevisionChangeSet activeRevisionChangeSet = null;
        if (iPartsConst.USE_CHANGE_SET_FOR_REPORT_FOR_CONSTRUCTION_NODES_CALCULATIONS && !isCSVExport) {
            EtkRevisionsHelper revisionsHelper = projectForGUI.getRevisionsHelper();
            if (revisionsHelper != null) {
                activeRevisionChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
            }
        }

        // Für Berechnungen mit aktivem Autoren-Auftrag die aktuelle Session verwenden, weil die Berechnung beim
        // Deaktivieren vom Autoren-Auftrag sofort beendet wird; für globale Berechnungen ohne aktiven Autoren-Auftrag
        // jedoch die zentrale Session vom iPartsPlugin verwenden, da es ansonsten auch zu Problemen kommt, wenn
        // während der Berechnung plötzlich ein Autoren-Auftrag aktiviert wird, da dies dann Auswirkungen auf die
        // Berechnungen haben würde (und sogar zu Exceptions bzgl. Pseudo-Transaktionen kommen kann).
        final Session sessionForCalculation;
        final boolean usingTempProject;
        final EtkProject projectForCalculation;
        final Session sessionForGUI = Session.get();
        if (activeRevisionChangeSet != null) {
            usingTempProject = false;
            sessionForCalculation = sessionForGUI;
            projectForCalculation = projectForGUI;
        } else {
            sessionForCalculation = iPartsPlugin.getMqSession();

            // Eigenes temporäres EtkProject erzeugen, damit für die Berechnung der offenen Stände und v.a. für den CSV-Export
            // die Stücklisten in der REPORT_LANGUAGE geladen werden
            usingTempProject = true;

            // EtkEndpointHelper.createProject(Session) kann nicht verwendet werden, weil das temporäre EtkProject nicht
            // als EtkProject in der globalen sessionForCalculation gesetzt werden soll
            DWFile dwkFile = (DWFile)sessionForCalculation.getAttribute(JavaViewerApplication.SESSION_KEY_DWK_FILE);
            projectForCalculation = EtkEndpointHelper.createProject(dwkFile, true);
            if (projectForCalculation == null) {
                if (!isAutoCalcAndExport) {
                    MessageDialog.showError("!!Fehler bei der Initialisierung für die Berechnung im Hintergrund.");
                }
                Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.ERROR, "Error while initializing series calculation");
                return null;
            }

            if (isAutoCalcAndExport) {
                // Bei automatischer Berechnung eine hardcodierte Sprache wählen
                projectForCalculation.getConfig().setCurrentViewerLanguage(AUTO_EXPORTED_SERIES_LANGUAGE);
                projectForCalculation.getConfig().setCurrentDatabaseLanguage(AUTO_EXPORTED_SERIES_LANGUAGE);
            } else {
                projectForCalculation.getConfig().setCurrentViewerLanguage(projectForGUI.getViewerLanguage());
                projectForCalculation.getConfig().setCurrentDatabaseLanguage(projectForGUI.getDBLanguage());
            }

            // Assembly mit dem projectForCalculation neu laden, damit alle weiteren Aktionen sauber damit durchgeführt werden
            assembly = EtkDataObjectFactory.createDataAssembly(projectForCalculation, assembly.getAsId());
        }
        if (sessionForCalculation == null) {
            if (!isAutoCalcAndExport) {
                MessageDialog.showError("!!Keine Session für die Berechnung im Hintergrund vorhanden.");
            }
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.ERROR, "Session for series calculation does not exist");
            return null;
        }

        final EtkDataAssembly assemblyFinal = assembly;

        // Baureihe bzw. HM/M/SM-Knoten bestimmen
        final HmMSmId hmMSmId;
        final iPartsSeriesId seriesId = iPartsVirtualNode.getSeriesIdForAssemblyId(assemblyFinal.getAsId());
        if (seriesId == null) {
            HmMSmNode hmMSmNode = iPartsVirtualNode.getHmMSmNodeForAssemblyId(assemblyFinal.getAsId(), projectForCalculation);
            if (hmMSmNode != null) {
                hmMSmId = hmMSmNode.getId();
            } else {
                hmMSmId = null;
            }
            if (hmMSmId == null) {
                if (!isAutoCalcAndExport) {
                    MessageDialog.showError("!!Keine Baureihe anhand der aktuellen Selektion für die Berechnung im Hintergrund gefunden.");
                }
                Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.ERROR, "Series could not be found for series calculation: "
                                                                                               + assemblyFinal.getAsId().toStringForLogMessages());
                return null;
            }
        } else {
            hmMSmId = null;
        }

        final AbstractReportForConstructionNodesHelper reporter;
        if (isCSVExport) {
            reporter = new ReportForConstructionCSVExport(projectForCalculation, projectForGUI,
                                                          isAutoCalcAndExport ? AUTO_EXPORTED_SERIES_LANGUAGE : projectForGUI.getViewerLanguage(),
                                                          isAutoCalcAndExport ? AUTO_EXPORTED_SERIES_LANGUAGE : projectForGUI.getDBLanguage());
        } else {
            reporter = new ReportForConstructionNodes(projectForCalculation, projectForGUI);
        }
        // Spezial-Datensatz zum Sperren der gesamten Baureihe gegen mehrfache Berechnungen in die DB schreiben
        // mit leerer Knoten-ID und leerem ChangeSet (das Sperren soll global unabhängig von einem ChangeSet stattinden)
        final String seriesNumber = (seriesId != null) ? seriesId.getSeriesNumber() : hmMSmId.getSeries();
        String errMsg = "!!Für die Baureihe \"%1\" findet seit %2 bereits eine Berechnung bzw. Export im Hintergrund statt.";
        if (!reporter.makeAndCheckStartNode(seriesNumber, errMsg)) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.DEBUG, "A calculation has already been started for series \""
                                                                                           + seriesNumber + "\"");
            return null;
        }

        Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.DEBUG,
                   isAutoCalcAndExport ? "Scheduled series calculation for \"" + seriesNumber + "\" started in a background thread"
                                       : "Series calculation for \"" + assemblyHeading + "\" started in a background thread by "
                                         + iPartsUserAdminDb.getUserNameForLogging(projectForGUI));

        // Berechnungs-Thread erzeugen
        final VarParam<EventListener> sessionDisposingEventListener = new VarParam<>();
        final FrameworkThread calculationThread = sessionForCalculation.startChildThread(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                long startTime = System.currentTimeMillis();
                boolean hasException = false;
                boolean wasCancelled = false;
                try {
                    // Rekursive Berechnung durchführen
                    VarParam<Integer> openEntries = new VarParam<>(0);
                    VarParam<Integer> changedEntries = new VarParam<>(0);
                    if ((seriesId != null) || hmMSmId.isHmNode() || hmMSmId.isMNode()) {
                        if (!reporter.calculateSeriesHmMNode(assemblyFinal, parentAssemblyIdFinal, hmMSmId, openEntries, changedEntries)) {
                            wasCancelled = true;
                            return;
                        }
                    } else if (hmMSmId.isSmNode()) {
                        if (!reporter.calculateSmNode(assemblyFinal, parentAssemblyIdFinal, hmMSmId, openEntries, changedEntries)) {
                            wasCancelled = true;
                            return;
                        }
                    } else { // Kann eigentlich nicht passieren
                        return;
                    }
                } catch (Exception e) {
                    hasException = true;
                    Logger.logExceptionWithoutThrowing(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.ERROR, e);
                } finally {
                    // Sperre für die Baureihe wieder freigeben falls der Berechnungszeitpunkt noch übereinstimmst
                    // (falls nicht, dann hat die Berechnung zu lange gedauert und wurde zwischenzeitlich als
                    // ungültig angesehen)
                    reporter.removeLockForReport(wasCancelled, hasException);

                    if (sessionDisposingEventListener.getValue() != null) {
                        sessionForCalculation.removeEventListener(sessionDisposingEventListener.getValue());
                    }

                    // Temporäres EtkProject wieder aufräumen
                    if ((usingTempProject) && (projectForCalculation != null)) {
                        projectForCalculation.setDBActive(false, false);
                    }

                    // Abbruch mit aktiver GUI-Session, in der auch die Berechnung gelaufen ist?
                    if (!isAutoCalcAndExport && wasCancelled && (sessionForGUI != null) && sessionForGUI.isActive() && (sessionForGUI == sessionForCalculation)) {
                        // Die GUI aktualisieren wegen dem Abbruch
                        projectForGUI.fireProjectEvent(new DataChangedEvent());
                        MessageDialog.show(TranslationHandler.translate("!!Die Berechnung für \"%1\" wurde z.B. aufgrund von Edit-Aktionen abgebrochen.",
                                                                        assemblyHeading),
                                           MENU_ITEM_TEXT_CALC_REPORT_FOR_CONST_NODE);
                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.DEBUG,
                                   "Series calculation for \"" + assemblyHeading + "\" was cancelled due to edit activity by "
                                   + iPartsUserAdminDb.getUserNameForLogging(projectForGUI));
                    }
                }

                final String timeDurationSring = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime,
                                                                                    true, false, Language.EN.getCode());

                // Ist die GUI-Session überhaupt noch aktiv?
                if (!isAutoCalcAndExport && (sessionForGUI != null) && sessionForGUI.isActive()) {
                    final boolean hasExceptionFinal = hasException;
                    final Runnable calculationFinishedRunnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (hasExceptionFinal) {
                                    Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.ERROR,
                                               "Series calculation for \"" + assemblyHeading + "\" started by "
                                               + iPartsUserAdminDb.getUserNameForLogging(projectForGUI) + " finished with errors in "
                                               + timeDurationSring);
                                } else {
                                    Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.DEBUG,
                                               "Series calculation for \"" + assemblyHeading + "\" started by "
                                               + iPartsUserAdminDb.getUserNameForLogging(projectForGUI) + " finished successfully in "
                                               + timeDurationSring);
                                }

                                // Nach allen Berechnungen die GUI aktualisieren
                                projectForGUI.fireProjectEvent(new DataChangedEvent());

                                String messageText;
                                if (isCSVExport) {
                                    if (hasExceptionFinal) {
                                        messageText = "!!Der Export ist mit Fehlern abgeschlossen für \"%1\". Siehe Logdatei für Details.";
                                    } else {
                                        messageText = "!!Der Export ist abgeschlossen für \"%1\".";
                                    }
                                } else {
                                    if (hasExceptionFinal) {
                                        messageText = "!!Die Berechnung ist mit Fehlern abgeschlossen für \"%1\". Siehe Logdatei für Details.";
                                    } else {
                                        messageText = "!!Die Berechnung ist abgeschlossen für \"%1\".";
                                    }
                                }
                                MessageDialog.show(TranslationHandler.translate(messageText, assemblyHeading),
                                                   MENU_ITEM_TEXT_CALC_REPORT_FOR_CONST_NODE);
                                reporter.doPostCalcOperations(!hasExceptionFinal);
                                reporter.clearAfterCalcFinished();
                            } catch (Exception e) {
                                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                            }
                        }
                    };

                    // Die Ausführung in einem neuen Thread ist dann notwendig, wenn wir uns im Kontext von einer
                    // anderen Session befinden
                    if (sessionForGUI != sessionForCalculation) {
                        sessionForGUI.startChildThread(new FrameworkRunnable() {
                            @Override
                            public void run(FrameworkThread thread) {
                                sessionForGUI.invokeThreadSafe(calculationFinishedRunnable);
                            }
                        });
                    } else {
                        sessionForGUI.invokeThreadSafe(calculationFinishedRunnable);
                    }
                } else {
                    if (isAutoCalcAndExport) {
                        // Nach allen Berechnungen die GUI aktualisieren
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.DEBUG,
                                   "Scheduled series calculation and export for \"" + seriesNumber + "\" finished successfully in "
                                   + timeDurationSring);
                    }
                    reporter.doPostCalcOperations(false);
                    reporter.clearAfterCalcFinished();
                }
            }
        });

        // Beim Beenden der Session den Thread abbrechen
        sessionDisposingEventListener.setValue(new EventListener(Event.DISPOSING_EVENT) {
            @Override
            public void fire(Event event) {
                calculationThread.cancel(false);
            }
        });
        sessionForCalculation.addEventListener(sessionDisposingEventListener.getValue());

        // Beim Deaktivieren vom Autoren-Auftrag den Thread abbrechen
        if (activeRevisionChangeSet != null) {
            activeRevisionChangeSet.addChangeSetThread(calculationThread);
        }

        if (!isAutoCalcAndExport) {
            projectForGUI.fireProjectEvent(new DataChangedEvent()); // Aktualisierung der gerade angezeigten Stückliste
            String msg;
            if (isCSVExport) {
                msg = "!!Der Export für \"%1\" wurde im Hintergrund gestartet...";
            } else {
                msg = "!!Die Berechnung für \"%1\" wurde im Hintergrund gestartet...";
            }
            MessageDialog.show(TranslationHandler.translate(msg, assemblyHeading), MENU_ITEM_TEXT_CALC_REPORT_FOR_CONST_NODE);
        }
        return calculationThread;
    }
}