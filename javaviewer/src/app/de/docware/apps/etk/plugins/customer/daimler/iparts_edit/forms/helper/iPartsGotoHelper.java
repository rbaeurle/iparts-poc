/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.mechanic.mainview.connector.MechanicFormConnector;
import de.docware.apps.etk.base.mechanic.mainview.forms.MechanicFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.events.FilterChangedEvent;
import de.docware.apps.etk.base.project.events.GotoPartWithPartialPathEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAModules;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsMechanicFormWindow;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper für Goto-Sprünge
 * bisher gotoRetail und gotoEDSConstruction
 * (es fehlt noch gotoDIALOGConstruction)
 */
public class iPartsGotoHelper {

    /**
     * Goto-Retail PartListEntryId (direkt, ohne eigenes Fenster)
     * parentForm wird nicht geschlossen
     *
     * @param connector
     * @param parentForm
     * @param assemblyId
     * @return
     */
    public static boolean gotoRetailDirect(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                           AssemblyId assemblyId) {
        return gotoRetail(connector, parentForm, false, assemblyId);
    }

    /**
     * Goto-Retail AssemblyId (direkt, ohne eigenes Fenster)
     * parentForm wird nicht geschlossen
     *
     * @param connector
     * @param parentForm
     * @param partListEntryId
     * @return
     */
    public static boolean gotoRetailDirect(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                           PartListEntryId partListEntryId) {
        return gotoRetail(connector, parentForm, false, partListEntryId);
    }

    /**
     * Goto-Retail PartListEntryId
     * Ist {@param withOwnDialog} true, dann in eigenem Fenster nonModal anzeigen
     * sonst: Schließen der parentForm und in Teilekatalog springen
     * Bei withOwnDialog = false: Soll die parentForm vor dem Sprung nicht geschlossen werden, dann null übergeben
     *
     * @param connector
     * @param parentForm      wird vor dem Sprung geschlossen, wenn <> null und withOwnDialog = false
     * @param withOwnDialog   true: in eigenem nonModalen Fenster
     * @param partListEntryId Ziel des Sprunges
     */
    public static boolean gotoRetail(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                     boolean withOwnDialog, PartListEntryId partListEntryId) {
        return gotoRetail(connector, parentForm, withOwnDialog, false, partListEntryId);
    }

    /**
     * Goto-Retail AssemblyId
     * Ist {@param withOwnDialog} true, dann in eigenem Fenster nonModal anzeigen
     * sonst: Schließen der parentForm und in Teilekatalog springen
     * Bei withOwnDialog = false: Soll die parentForm vor dem Sprung nicht geschlossen werden, dann null übergeben
     *
     * @param connector
     * @param parentForm    wird vor dem Sprung geschlossen, wenn <> null und withOwnDialog = false
     * @param withOwnDialog true: in eigenem nonModalen Fenster
     * @param assemblyId    Ziel des Sprunges
     */
    public static boolean gotoRetail(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                     boolean withOwnDialog, AssemblyId assemblyId) {
        return gotoRetail(connector, parentForm, withOwnDialog, false, assemblyId);
    }

    /**
     * Goto-Retail AssemblyId
     * Ist {@param withOwnDialog} true, dann in eigenem Fenster nonModal anzeigen
     * Ist {@param closeParentform] true, dann wird die parentForm geschlossen
     *
     * @param connector
     * @param parentForm
     * @param withOwnDialog
     * @param closeParentform
     * @param assemblyId
     * @return
     */
    public static boolean gotoRetail(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                     boolean withOwnDialog, boolean closeParentform, AssemblyId assemblyId) {
        if (assemblyId != null) {
            NavigationPath navPath = new NavigationPath();
            navPath.addAssembly(assemblyId);
            return gotoPath(connector, parentForm, withOwnDialog, closeParentform, navPath, assemblyId, null);
        }
        return false;
    }

    /**
     * Goto-Retail PartListEntryId
     * Ist {@param withOwnDialog} true, dann in eigenem Fenster nonModal anzeigen
     * Ist {@param closeParentform] true, dann wird die parentForm geschlossen
     *
     * @param connector
     * @param parentForm
     * @param withOwnDialog
     * @param closeParentForm
     * @param partListEntryId
     * @return
     */
    public static boolean gotoRetail(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                     boolean withOwnDialog, boolean closeParentForm, PartListEntryId partListEntryId) {
        if (partListEntryId != null) {
            AssemblyId assemblyId = partListEntryId.getOwnerAssemblyId();
            NavigationPath navPath = new NavigationPath();
            navPath.add(partListEntryId);
            return gotoPath(connector, parentForm, withOwnDialog, closeParentForm, navPath, assemblyId, partListEntryId);
        }
        return false;
    }


    /**
     * Goto EDS-Construction
     * Ist {@param withOwnDialog} true, dann in eigenem Fenster nonModal anzeigen
     * sonst: Schließen der parentForm und in Teilekatalog springen
     * Bei withOwnDialog = false: Soll die parentForm vor dem Sprung nicht geschlossen werden, dann null übergeben
     *
     * @param connector
     * @param parentForm      wird vor dem Sprung geschlossen, wenn <> null und withOwnDialog = false
     * @param withOwnDialog   true: in eigenem nonModalen Fenster
     * @param modelNo         Adresse des EDS-Konstruktions-Moduls
     * @param structureId
     * @param saaBkNo
     * @param partListEntryId
     */
    public static boolean gotoEDSConstruction(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                              boolean withOwnDialog, String modelNo, HierarchicalIDWithType structureId, String saaBkNo,
                                              PartListEntryId partListEntryId) {
        if (iPartsPlugin.addEDSConstructionModelToFilter(modelNo)) {
            connector.getProject().fireProjectEvent(new FilterChangedEvent());
        }
        List<iPartsVirtualNode> nodes = createVirtualNodePathForEDSConstruction(modelNo, structureId, saaBkNo);
        AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
        return gotoConstruction(connector, parentForm, withOwnDialog, assemblyId, partListEntryId);
    }

    /**
     * Goto CTT-Construction
     * Ist {@param withOwnDialog} true, dann in eigenem Fenster nonModal anzeigen
     * sonst: Schließen der parentForm und in Teilekatalog springen
     * Bei withOwnDialog = false: Soll die parentForm vor dem Sprung nicht geschlossen werden, dann null übergeben
     *
     * @param connector
     * @param parentForm      wird vor dem Sprung geschlossen, wenn <> null und withOwnDialog = false
     * @param withOwnDialog   true: in eigenem nonModalen Fenster
     * @param modelNo         Adresse des EDS-Konstruktions-Moduls
     * @param saaBkNo
     * @param partListEntryId
     */
    public static boolean gotoCTTConstruction(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                              boolean withOwnDialog, String modelNo, String saaBkNo, PartListEntryId partListEntryId) {
        List<iPartsVirtualNode> nodes = createVirtualNodePathForCTTConstruction(modelNo, saaBkNo);
        iPartsVirtualNode[] nodeArray = ArrayUtil.toArray(nodes);
        if (nodeArray == null) {
            return false;
        }
        AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(nodeArray), "");
        SessionKeyHelper.getSelectedCTTModelMapWithUserSettingsCheck(connector.getProject());
        if (iPartsPlugin.addCTTConstructionModelToFilter(modelNo)) {
            final VarParam<Boolean> gotoResult = new VarParam<>(false);
            EtkMessageLogForm logForm = new EtkMessageLogForm("!!CTT Konstruktionsbaumuster", "!!Lade CTT Baumuster...", null);
            logForm.disableButtons(true);
            logForm.getGui().setSize(600, 250);
            logForm.showModal(thread -> {
                logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Baumuster \"%1\"...", modelNo));
                connector.getProject().fireProjectEvent(new FilterChangedEvent());
                gotoResult.setValue(gotoConstruction(connector, parentForm, withOwnDialog, assemblyId, partListEntryId));
            });
            connector.getProject().fireProjectEvent(new FilterChangedEvent());
            return gotoResult.getValue();
        }
        return gotoConstruction(connector, parentForm, withOwnDialog, assemblyId, partListEntryId);
    }

    private static boolean gotoConstruction(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                            boolean withOwnDialog, AssemblyId assemblyId, PartListEntryId partListEntryId) {
        NavigationPath navPath = new NavigationPath();
        navPath.addAssembly(assemblyId);
        return gotoPath(connector, parentForm, withOwnDialog, false, navPath, assemblyId, partListEntryId);
    }

    public static boolean gotoMBSConstruction(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                              boolean withOwnDialog, String modelNo, String saaBkNo, String conGroup) {
        List<iPartsVirtualNode> nodes = createVirtualNodePathForMBSConstruction(modelNo, saaBkNo, conGroup);
        // Pfad für den Sprung in die Konstruktions zusammenbauen inkl. setzen des BM für den MBS-BM-Filter
        SessionKeyHelper.getSelectedMBSModelMapWithUserSettingsCheck(connector.getProject());
        if (SessionKeyHelper.addMBSConstructionModelToFilter(modelNo)) {
            final VarParam<Boolean> gotoResult = new VarParam<>(false);
            EtkMessageLogForm logForm = new EtkMessageLogForm("!!MBS Konstruktionsbaumuster", "!!Lade MBS Baumuster...", null);
            logForm.disableButtons(true);
            logForm.getGui().setSize(600, 250);
            logForm.showModal(new FrameworkRunnable() {

                @Override
                public void run(FrameworkThread thread) {
                    logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Baumuster \"%1\"...", modelNo));
                    connector.getProject().fireProjectEvent(new FilterChangedEvent());
                    AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
                    gotoResult.setValue(gotoConstruction(connector, parentForm, withOwnDialog, assemblyId, null));
                }
            });
            return gotoResult.getValue();
        }
        AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
        return gotoConstruction(connector, parentForm, withOwnDialog, assemblyId, null);
    }

    public static boolean gotoMBSConstruction(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                              boolean withOwnDialog, PartListEntryId partListEntryId) {
        if (partListEntryId != null) {
            AssemblyId assemblyId = partListEntryId.getOwnerAssemblyId();
            if (partListEntryId.getKLfdnr().isEmpty()) {
                partListEntryId = null;
            }
            String modelNo = iPartsVirtualNode.getModelNumberFromAssemblyId(assemblyId);
            if (modelNo != null) {
                // Pfad für den Sprung in die Konstruktions zusammenbauen inkl. setzen des BM für den MBS-BM-Filter
                SessionKeyHelper.getSelectedMBSModelMapWithUserSettingsCheck(connector.getProject());
                if (SessionKeyHelper.addMBSConstructionModelToFilter(modelNo)) {
                    final PartListEntryId finalPartListEntryId = partListEntryId;
                    final VarParam<Boolean> gotoResult = new VarParam<>(false);
                    EtkMessageLogForm logForm = new EtkMessageLogForm("!!MBS Konstruktionsbaumuster", "!!Lade MBS Baumuster...", null);
                    logForm.disableButtons(true);
                    logForm.getGui().setSize(600, 250);
                    logForm.showModal(new FrameworkRunnable() {

                        @Override
                        public void run(FrameworkThread thread) {
                            logForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Baumuster \"%1\"...", modelNo));
                            connector.getProject().fireProjectEvent(new FilterChangedEvent());
                            gotoResult.setValue(gotoConstruction(connector, parentForm, withOwnDialog, assemblyId, finalPartListEntryId));
                        }
                    });
                    return gotoResult.getValue();
                }
            }
            return gotoConstruction(connector, parentForm, withOwnDialog, assemblyId, partListEntryId);
        }
        return false;
    }

    /**
     * Sprung zum Modul
     * Ist Modul eine freie SA ohne Verortung kann diese in TU bearbeiten geladen werden, falls gewünscht
     * Klappt der Sprung zur laufenden Nummer nicht, dann wird einfach in Modul gesprungen
     *
     * @param dataConnector Connector
     * @param parentForm    Dialog von dem wir kommen
     * @param project       EtkProjekt
     * @param moduleNo      Modulnummer zu dem gesprungen werden soll
     * @param lfdNr         lfdNr die markiert werden soll
     */
    public static void gotoModuleWithDifferentOptions(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                      EtkProject project, String moduleNo, String lfdNr) {
        if (StrUtils.isValid(moduleNo)) {
            AssemblyId assemblyId = new AssemblyId(moduleNo, "");
            EtkDataAssembly currentAssembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
            if (currentAssembly instanceof iPartsDataAssembly) {
                if (((iPartsDataAssembly)currentAssembly).isSAAssembly()) {
                    if (!isSaBoundToProduct(project, assemblyId)) {
                        String msg = TranslationHandler.translate("!!Die Freie SA \"%1\" ist nicht verortet.",
                                                                  iPartsNumberHelper.formatPartNo(project, moduleNo)) +
                                     "\n\n" +
                                     TranslationHandler.translate("!!Soll sie im Editor geöffnet werden?");
                        if (ModalResult.YES == MessageDialog.showYesNo(msg, "!!Abfrage")) {
                            if (StrUtils.isValid(lfdNr)) {
                                List<PartListEntryId> partListEntryIds = new DwList<>();
                                partListEntryIds.add(new PartListEntryId(assemblyId.getKVari(), assemblyId.getKVer(), lfdNr));
                                EditModuleForm.editOrViewModulesByPartListEntries(partListEntryIds, dataConnector.getMainWindow());
                            } else {
                                List<AssemblyId> assemblyIds = new DwList<>();
                                assemblyIds.add(assemblyId);
                                EditModuleForm.editOrViewModules(assemblyIds, dataConnector.getMainWindow());
                            }
                        }
                        return;
                    }
                }
            }

            // Erst versuchen direkt zum PartlistEntry zu springen ...
            if (StrUtils.isValid(lfdNr)) {
                PartListEntryId partListEntryId = new PartListEntryId(moduleNo, "", lfdNr);
                if (!gotoRetail(dataConnector, parentForm, true, partListEntryId)) {
                    MessageDialog.showWarning(TranslationHandler.translate("!!Keine Retaildaten für \"%1\", \"%2\" gefunden!",
                                                                           iPartsNumberHelper.formatPartNo(project, moduleNo), lfdNr));
                }
            } else { // ... oder wenigstens in das Modul
                if (!gotoRetail(dataConnector, parentForm, true, assemblyId)) {
                    MessageDialog.showWarning(TranslationHandler.translate("!!Keine Retaildaten für \"%1\" gefunden!",
                                                                           iPartsNumberHelper.formatPartNo(project, moduleNo)));
                }
            }
        }
    }

    protected static boolean isSaBoundToProduct(EtkProject project, AssemblyId assemblyId) {
        iPartsModuleId moduleId = new iPartsModuleId(assemblyId.getKVari());
        iPartsDataSAModulesList saModules = iPartsDataSAModulesList.loadDataForModule(project, moduleId);
        for (iPartsDataSAModules saModule : saModules) {
            iPartsSAId saId = new iPartsSAId(saModule.getAsId().getSaNumber());
            Map<iPartsProductId, Set<String>> productIdsToKGsMap = iPartsSA.getInstance(project, saId).getProductIdsToKGsMap(project);
            if (!productIdsToKGsMap.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Erzeugt den {@link iPartsVirtualNode}-Pfad als Vorbereitung zum Sprung in die EDS Konstruktion.
     *
     * @param modelNo
     * @param structureId
     * @param saaBkNo
     * @return
     */
    public static List<iPartsVirtualNode> createVirtualNodePathForEDSConstruction(String modelNo,
                                                                                  HierarchicalIDWithType structureId,
                                                                                  String saaBkNo) {
        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
        List<iPartsVirtualNode> nodes = new ArrayList<>();
        nodes.add(structureHelper.createEdsModelVirtualNode(new iPartsModelId(modelNo)));
        nodes.add(structureHelper.createEdsStructureVirtualNode(structureId));
        nodes.add(new iPartsVirtualNode(iPartsNodeType.EDS_SAA, new EdsSaaId(saaBkNo)));
        return nodes;
    }

    /**
     * Erzeugt den {@link iPartsVirtualNode}-Pfad als Vorbereitung zum Sprung in die EDS Konstruktion.
     *
     * @param modelNo
     * @param saaBkNo
     * @return
     */
    public static List<iPartsVirtualNode> createVirtualNodePathForCTTConstruction(String modelNo, String saaBkNo) {
        List<iPartsVirtualNode> nodes = new ArrayList<>();
        nodes.add(new iPartsVirtualNode(iPartsNodeType.CTT_MODEL, new iPartsModelId(modelNo)));
        nodes.add(new iPartsVirtualNode(iPartsNodeType.EDS_SAA, new EdsSaaId(saaBkNo)));
        return nodes;
    }

    /**
     * Erzeugt den {@link iPartsVirtualNode}-Pfad als Vorbereitung zum Sprung in die EDS Konstruktion.
     *
     * @param modelNo
     * @param saaNo
     * @param conGroup
     * @return
     */
    public static List<iPartsVirtualNode> createVirtualNodePathForMBSConstruction(String modelNo, String saaNo, String conGroup) {
        List<iPartsVirtualNode> nodes = new ArrayList<>();
        nodes.add(new iPartsVirtualNode(iPartsNodeType.MBS_STRUCTURE, new iPartsModelId(modelNo)));
        nodes.add(new iPartsVirtualNode(iPartsNodeType.MBS, new MBSStructureId(saaNo, conGroup)));
        return nodes;
    }

    /**
     * Goto path, assemblyId, partListEntryId
     * Ist {@param withOwnDialog} true, dann in eigenem Fenster nonModal anzeigen
     * sonst: Schließen der parentForm und in Teilekatalog springen
     * Bei withOwnDialog = false: Soll die parentForm vor dem Sprung nicht geschlossen werden, dann null übergeben
     *
     * @param connector
     * @param parentForm      wird vor dem Sprung geschlossen, wenn <> null und withOwnDialog = false
     * @param withOwnDialog   true: in eigenem nonModalen Fenster
     * @param closeParentForm
     * @param path            vorbesetzter navPath
     * @param assemblyId      Ziel Assembly
     * @param partListEntryId false <> null: Ziel TeilePosition
     */
    public static boolean gotoPath(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                   boolean withOwnDialog, boolean closeParentForm,
                                   NavigationPath path, AssemblyId assemblyId, PartListEntryId partListEntryId) {
        String kLfdNr = "";
        if (partListEntryId != null) {
            kLfdNr = partListEntryId.getKLfdnr();
        }
        if (!withOwnDialog) {
            if ((parentForm != null) && closeParentForm) {
                parentForm.close();  // Erst schließen, dann springen
            }

            GotoPartWithPartialPathEvent partWithPartialPathEvent = new GotoPartWithPartialPathEvent(path, assemblyId,
                                                                                                     kLfdNr, false, false,
                                                                                                     parentForm);
            connector.getProject().fireProjectEvent(partWithPartialPathEvent);
            return partWithPartialPathEvent.isFound();
        } else {
            return gotoPathNewWindow(connector, path, assemblyId, kLfdNr);
        }
    }

    /**
     * Springe zum übergebenen Stücklisteneintrag in einem neuen nicht-modalen Fenster inkl. optionalem Öffnen der RelatedInfo.
     *
     * @param connector
     * @param partListEntryId         ID vom Stücklisteneintrag
     * @param extendedRelatedInfoData Optionale Daten, um direkt eine RelatedInfo zu öffnen und zu diesen Daten zu springen
     *                                (falls von der RelatedInfo unterstützt)
     */
    public static boolean gotoPathNewWindowWithRelatedInfo(AbstractJavaViewerFormIConnector connector, PartListEntryId partListEntryId,
                                                           ExtendedRelatedInfoData extendedRelatedInfoData) {
        AssemblyId assemblyId = partListEntryId.getOwnerAssemblyId();
        NavigationPath navPath = new NavigationPath();
        navPath.add(partListEntryId);
        boolean result = gotoPathNewWindowWithRelatedInfo(connector, navPath, assemblyId, partListEntryId.getKLfdnr(), extendedRelatedInfoData);
        if (!result) {
            MessageDialog.showWarning(TranslationHandler.translate("!!Keine Retaildaten für \"%1\", \"%2\" gefunden!",
                                                                   iPartsNumberHelper.formatPartNo(connector.getProject(), assemblyId.getKVari()),
                                                                   partListEntryId.getKLfdnr()));
        }
        return result;
    }

    /**
     * Goto path, assemblyId, kLfdNr in einem neuen nicht-modalen Fenster
     *
     * @param connector
     * @param path       vorbesetzter navPath
     * @param assemblyId Ziel Assembly
     * @param kLfdNr     Laufende Nummer vom Stücklisteneintrag
     */
    public static boolean gotoPathNewWindow(AbstractJavaViewerFormIConnector connector, NavigationPath path, AssemblyId assemblyId,
                                            String kLfdNr) {
        return gotoPathNewWindowWithRelatedInfo(connector, path, assemblyId, kLfdNr, null);
    }

    /**
     * Goto path, assemblyId, kLfdNr in einem neuen nicht-modalen Fenster
     *
     * @param connector
     * @param path                    vorbesetzter navPath
     * @param assemblyId              Ziel Assembly
     * @param kLfdNr                  Laufende Nummer vom Stücklisteneintrag
     * @param extendedRelatedInfoData Optionale Daten, um direkt eine RelatedInfo zu öffnen und zu diesen Daten zu springen
     *                                (falls von der RelatedInfo unterstützt)
     */
    public static boolean gotoPathNewWindowWithRelatedInfo(AbstractJavaViewerFormIConnector connector, NavigationPath path,
                                                           AssemblyId assemblyId, String kLfdNr, ExtendedRelatedInfoData extendedRelatedInfoData) {
        MechanicFormIConnector mechanicFormConnector = new MechanicFormConnector(null);

        AssemblyId rootAssemblyId = mechanicFormConnector.getProject().getRootNodes().getRootAssemblyId();
        EtkDataAssembly currentAssembly = EtkDataObjectFactory.createDataAssembly(mechanicFormConnector.getProject(),
                                                                                  assemblyId);

        mechanicFormConnector.setRootAssemblyId(rootAssemblyId);
        mechanicFormConnector.setCurrentAssembly(currentAssembly);
        mechanicFormConnector.setCurrentNavigationPath(path);

        iPartsMechanicFormWindow mechanicFormWindow = new iPartsMechanicFormWindow(mechanicFormConnector, null,
                                                                                   connector.getActiveForm().getRootParentForm());
        mechanicFormWindow.addOwnConnector(mechanicFormConnector);

        // selektiere das Teil innerhalb der Assembly
        GotoPartWithPartialPathEvent partWithPartialPathEvent = new GotoPartWithPartialPathEvent(path, currentAssembly.getAsId(),
                                                                                                 kLfdNr, false, false,
                                                                                                 mechanicFormConnector.getActiveForm().getRootParentForm());

        connector.getProject().fireProjectEvent(partWithPartialPathEvent);

        mechanicFormWindow.showNonModal();
        if (partWithPartialPathEvent.isFound() && (extendedRelatedInfoData != null)) {
            showRelatedInfo(mechanicFormConnector, extendedRelatedInfoData);
        }
        return partWithPartialPathEvent.isFound();
    }

    private static void showRelatedInfo(MechanicFormIConnector mechanicFormConnector, ExtendedRelatedInfoData extendedRelatedInfoData) {
        List<EtkDataPartListEntry> partListEntries = mechanicFormConnector.getSelectedPartListEntries();
        if ((partListEntries != null) && (partListEntries.size() == 1)) {
            EtkDataPartListEntry entry = partListEntries.get(0);
            RelatedInfoFormConnector relatedInfoConnector = new RelatedInfoFormConnector(mechanicFormConnector);
            relatedInfoConnector.setEditContext(null);
            extendedRelatedInfoData.setKatInfosForPartList(entry, mechanicFormConnector.getRootAssemblyId());
            extendedRelatedInfoData.setNavigationPath(mechanicFormConnector.getCurrentNavigationPath());
            extendedRelatedInfoData.setSortedVisiblePartListEntries(mechanicFormConnector.getSortedVisiblePartListEntries());
            relatedInfoConnector.setRelatedInfoData(extendedRelatedInfoData);
            mechanicFormConnector.showRelatedInfo(relatedInfoConnector);
            relatedInfoConnector.dispose();
        }
    }

    public static void gotoDialogConstructionPartList(EtkDataPartListEntry partListEntry, final AbstractJavaViewerFormIConnector connector) {
        PartListEntryId destPlEId = EditConstructionToRetailHelper.getVirtualConstructionPartlistEntryIdFromRetailPartlistEntry(partListEntry);
        if (destPlEId != null) {
            NavigationPath path = new NavigationPath();
            path.add(destPlEId);

            gotoPathNewWindow(connector, path, destPlEId.getOwnerAssemblyId(), destPlEId.getKLfdnr());
        }
    }

    public static void loadAndGotoEditRetail(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                             PartListEntryId partListEntryId) {
        if (partListEntryId != null) {
            // Alle modalen Vater-Fenster schließen
            boolean modalWindowFound = false;
            while (parentForm != null) {
                GuiWindow parentWindow = parentForm.getGui().getParentWindow();
                if (parentWindow != null) {
                    if (parentWindow.isShownModal()) {
                        parentForm.close();  // Erst schließen, dann springen
                        modalWindowFound = true;
                    }
                }
                parentForm = parentForm.getParentForm();
            }
            if (modalWindowFound) {
                connector.updateAllViews(null, false);
            }

            List<PartListEntryId> partListEntryIds = new DwList<>(1);
            partListEntryIds.add(partListEntryId);
            EditModuleForm.editOrViewModulesByPartListEntries(partListEntryIds, connector.getMainWindow());
        }
    }
}
