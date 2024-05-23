/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.toolbar.EtkToolbarManager;
import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.mainview.connector.MechanicFormConnector;
import de.docware.apps.etk.base.mechanic.mainview.forms.AbstractMechanicForm;
import de.docware.apps.etk.base.mechanic.mainview.forms.MechanicFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.misc.MenuManager;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.events.*;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.search.connector.SearchFormConnector;
import de.docware.apps.etk.base.search.forms.AbstractSearchForm;
import de.docware.apps.etk.base.search.model.SearchEvents;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.base.viewermain.forms.responsive.ResponsiveMechanicForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAAModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAAModelsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyEdsBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuBar;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbarManager;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.misc.observer.ObserverCall;
import de.docware.util.misc.observer.ObserverCallback;

import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.docware.framework.modules.gui.controls.GuiWindow.NonModalStyle.OPEN_IN_NEW_WINDOW;

/**
 * Fenster mit einem {@link AbstractMechanicForm}, welches in iParts nicht-modal angezeigt werden kann.
 */
public class iPartsMechanicFormWindow extends AbstractJavaViewerForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_OPEN_DIALOG_CONSTRUCTION_WINDOW = "iPartsMenuItemOpenDIALOGConstructionWindow";
    public static final String IPARTS_MENU_ITEM_OPEN_EDS_CONSTRUCTION_WINDOW = "iPartsMenuItemOpenEDSConstructionWindow";
    public static final String IPARTS_MENU_ITEM_OPEN_NON_MODAL_MECHANIC_WINDOW = "iPartsMenuItemOpenNonModalMechanicWindow"; // Zur Wiedererkennung beim Pop-Up Menü im Baum
    private static final String IPARTS_MENU_TEXT_OPEN_CONSTRUCTION_WINDOW = "!!Gehe zur Konstruktions-Stückliste";

    private GuiWindow window;
    private GuiMenuBar menuBar;
    private GuiToolbar toolbar;
    private EtkToolbarManager toolbarManager;
    private GuiToolButton buttonRefreshWindow;
    private AbstractMechanicForm mechanicForm;

    /**
     * Fügt dem Kontextmenü den neuen Eintrag hinzu
     *
     * @param menu
     * @param formWithTree
     */
    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {

        GuiMenuItem menuItem = AbstractRelatedInfoPartlistDataForm.modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_OPEN_NON_MODAL_MECHANIC_WINDOW,
                                                                                       "!!In neuem Fenster öffnen...", null);
        if (menuItem != null) {
            menuItem.setIcon(EditDefaultImages.edit_openNonModalMechanicWindow.getImage());
            EventListener listener = new EventListener(Event.MENU_ITEM_EVENT, EventListenerOptions.buildEnumSet(false, false)) {
                @Override
                public void fire(Event event) {
                    AssemblyId rootAssemblyId;
                    EtkDataAssembly currentAssembly;
                    NavigationPath path;
                    MechanicFormIConnector mechanicFormConnector = new MechanicFormConnector(JavaViewerApplication.getInstance().getMainConnector());
                    if (formWithTree instanceof AbstractAssemblyTreeForm) {
                        AbstractAssemblyTreeForm treeForm = (AbstractAssemblyTreeForm)formWithTree;
                        rootAssemblyId = treeForm.getRootAssemblyId();
                        currentAssembly = EtkDataObjectFactory.createDataAssembly(mechanicFormConnector.getProject(),
                                                                                  treeForm.getCurrentAssembly().getAsId());
                        path = new NavigationPath(treeForm.getConnector().getCurrentNavigationPath());
                    } else {
                        rootAssemblyId = mechanicFormConnector.getProject().getRootNodes().getRootAssemblyId();
                        currentAssembly = EtkDataObjectFactory.createDataAssembly(mechanicFormConnector.getProject(), rootAssemblyId);
                        path = new NavigationPath();
                        path.add(new PartListEntryId(rootAssemblyId));
                    }
                    mechanicFormConnector.setRootAssemblyId(rootAssemblyId);
                    mechanicFormConnector.setCurrentAssembly(currentAssembly);
                    mechanicFormConnector.setCurrentNavigationPath(path);

                    iPartsMechanicFormWindow mechanicFormWindow = new iPartsMechanicFormWindow(mechanicFormConnector, null,
                                                                                               formWithTree.getRootParentForm());
                    mechanicFormWindow.addOwnConnector(mechanicFormConnector);
                    mechanicFormWindow.showNonModal();
                }
            };
            menuItem.addEventListener(listener);

            menuItem.setVisible(true); // Der Popup-Menüeintrag immer sichtbar
        }
    }

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        // DAIMLER-16114: "Gehe zur Konstruktion" nur wenn iPartsRight.VIEW_CONSTRUCTION_PARTS_DATA vorhanden
        if (!iPartsRight.VIEW_CONSTRUCTION_PARTS_DATA.checkRightInSession()) {
            return;
        }

        GuiMenuItem goToDialogMenuItem = AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector,
                                                                                                     IPARTS_MENU_ITEM_OPEN_DIALOG_CONSTRUCTION_WINDOW,
                                                                                                     IPARTS_MENU_TEXT_OPEN_CONSTRUCTION_WINDOW,
                                                                                                     EditDefaultImages.edit_openNonModalMechanicWindow.getImage(),
                                                                                                     null);
        EventListener listener = new EventListener(Event.MENU_ITEM_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                java.util.List<EtkDataPartListEntry> selectedList = connector.getSelectedPartListEntries();
                if ((selectedList != null) && (selectedList.size() == 1)) {
                    EtkDataPartListEntry selectedPartListEntry = selectedList.get(0);
                    if (isGotoDIALOGConstructionVisible(selectedPartListEntry)) {
                        iPartsGotoHelper.gotoDialogConstructionPartList(selectedPartListEntry, connector);
                    }
                }
            }
        };
        goToDialogMenuItem.addEventListener(listener);

        AbstractRelatedInfoPartlistDataForm.modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_OPEN_EDS_CONSTRUCTION_WINDOW,
                                                                    IPARTS_MENU_TEXT_OPEN_CONSTRUCTION_WINDOW,
                                                                    EditDefaultImages.edit_openNonModalMechanicWindow.getImage(),
                                                                    null);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        // DAIMLER-16114: "Gehe zur Konstruktion" nur wenn iPartsRight.VIEW_CONSTRUCTION_PARTS_DATA vorhanden
        if (!iPartsRight.VIEW_CONSTRUCTION_PARTS_DATA.checkRightInSession()) {
            return;
        }

        List<EtkDataPartListEntry> selectedList = connector.getSelectedPartListEntries();
        if ((selectedList != null) && (selectedList.size() == 1)) {
            EtkDataPartListEntry selectedPartListEntry = selectedList.get(0);
            AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_OPEN_DIALOG_CONSTRUCTION_WINDOW,
                                                                        isGotoDIALOGConstructionVisible(selectedPartListEntry));

            boolean isGotoEDSConstructionVisible = isGotoEDSConstructionVisible(selectedPartListEntry);
            GuiMenuItem gotoEDSConstructionMenuItem = AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu,
                                                                                                                  IPARTS_MENU_ITEM_OPEN_EDS_CONSTRUCTION_WINDOW,
                                                                                                                  isGotoEDSConstructionVisible);

            if ((gotoEDSConstructionMenuItem != null) && isGotoEDSConstructionVisible) {
                addGotoSaaMenuEntries(connector, gotoEDSConstructionMenuItem, selectedPartListEntry);
            }
        } else {
            AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_OPEN_DIALOG_CONSTRUCTION_WINDOW,
                                                                        false);
            AbstractRelatedInfoPartlistDataForm.updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_OPEN_EDS_CONSTRUCTION_WINDOW,
                                                                        false);
        }
    }

    private static boolean isGotoDIALOGConstructionVisible(EtkDataPartListEntry partListEntry) {
        EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
        if (ownerAssembly instanceof iPartsDataAssembly) {
            iPartsDocumentationType documentationType = ((iPartsDataAssembly)ownerAssembly).getDocumentationType();
            if (documentationType.isPKWDocumentationType()) {
                String sourceGuid = partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                if (StrUtils.isValid(sourceGuid)) {
                    iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGuid);
                    return bcteKey != null;
                }
            }
        }

        return false;
    }

    private static boolean isGotoEDSConstructionVisible(EtkDataPartListEntry partListEntry) {
        EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
        if (ownerAssembly instanceof iPartsDataAssembly) {
            iPartsDocumentationType documentationType = ((iPartsDataAssembly)ownerAssembly).getDocumentationType();
            if (documentationType.isTruckDocumentationType()) {
                EtkDataArray saaValidity = partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_SA_VALIDITY);
                return (saaValidity != null) && !saaValidity.isEmpty();
            }
        }

        return false;
    }

    private static GuiMenuItem getGuiMenuItem(GuiContextMenu popupMenu, String name) {
        for (AbstractGuiControl child : popupMenu.getChildren()) {
            if (Utils.objectEquals(child.getUserObject(), name)) {
                return (GuiMenuItem)child;
            }
        }

        return null;
    }

    /**
     * Fügt für jede SAA aus den SAA-Gültigkeiten einen neuen Untermenüeintrag für den Sprung zu dieser hinzu.
     * Vorhandene Einträge werden vorher entfernt, da sich die SAA-Gültigkeiten durch Edit-Aktionen ändern können.
     *
     * @param connector
     * @param gotoConstructionMenuItem
     * @param partListEntry
     */
    private static void addGotoSaaMenuEntries(AssemblyListFormIConnector connector, GuiMenuItem gotoConstructionMenuItem,
                                              EtkDataPartListEntry partListEntry) {
        gotoConstructionMenuItem.removeAllChildren();
        EditToolbarButtonMenuHelper toolbarHelper = new EditToolbarButtonMenuHelper(connector, null);
        EtkDataArray saaValidity = partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_SA_VALIDITY);

        // Die Anzahl SAAs im Pop-up Menü beschränken, damit die Liste nicht mehr oben und unten über den Bildschirmrand hinausragt.
        List<String> saaStrList = saaValidity.getArrayAsStringList();
        if (saaStrList.size() <= iPartsMainImportHelper.MAX_ELEMS_FOR_SHOW) {
            for (String saa : saaStrList) {
                String formattedSAA = iPartsNumberHelper.formatPartNo(connector.getProject(), saa);
                GuiMenuItem menuItem = toolbarHelper.createMenuEntry(saa, formattedSAA, null, new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                                EventListenerOptions.SYNCHRON_EVENT) {
                    @Override
                    public void fire(Event event) {
                        gotoSaaConstructionPartsList(partListEntry, saa, connector);
                    }

                }, null);
                gotoConstructionMenuItem.addChild(menuItem);
            }
        } else {
            // Hier den neuen Richard-Dialog [iPartsShowDataObjectsDialog] verwenden, wenn die Anzahl Elemente zu groß wird.
            GuiMenuItem menuItem = toolbarHelper.createMenuEntry("gotoSaaMenu", "!!SAA-Auswahl...", null, new EventListener(Event.MENU_ITEM_EVENT,
                                                                                                                            EventListenerOptions.SYNCHRON_EVENT) {
                @Override
                public void fire(Event event) {
                    iPartsSaaId selectedSaaId = iPartsShowDataObjectsDialog.showGotoSaaElements(connector, connector.getActiveForm(),
                                                                                                saaStrList, true);
                    if (selectedSaaId != null) {
                        gotoSaaConstructionPartsList(partListEntry, selectedSaaId.getSaaNumber(), connector);
                    }
                }
            }, null);
            gotoConstructionMenuItem.addChild(menuItem);
        }
    }

    private static void gotoSaaConstructionPartsList(EtkDataPartListEntry partListEntry, String selectedSaa, AssemblyListFormIConnector connector) {
        EtkProject project = connector.getProject();
        boolean isSaAssembly = false;
        // Bestimmung, ob es sich um einen SA-TU handelt
        if (partListEntry.getOwnerAssembly() instanceof iPartsDataAssembly) {
            isSaAssembly = ((iPartsDataAssembly)partListEntry.getOwnerAssembly()).isSAAssembly();
        }
        // DAIMLER-10319: BM-Gültigkeiten des Stücklisteneintrags bevorzugt verwenden.
        EtkDataArray validModelsOfPartListEntry = partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_MODEL_VALIDITY);
        List<String> validModels = validModelsOfPartListEntry.getArrayAsStringList();
        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
        if (validModels.isEmpty()) {
            if (!isSaAssembly) {
                // Keine BM-Gültigkeit vorhanden. Es werden die BM-Gültigkeiten des Produkts herangezogen.
                iPartsProductId productId = ((iPartsDataPartListEntry)partListEntry).getOwnerAssembly().getProductIdFromModuleUsage();
                if (productId != null) {
                    validModels.addAll(iPartsProduct.getInstance(project, productId).getModelNumbers(project));
                }
            } else {
                // bei SA-TU: Suche Vorkommen von selectedSaa und sammle Baumuster
                DBDataObjectAttributesList modelsGroupsAndScopesForSaa = iPartsVirtualAssemblyEdsBase.loadModelStructureForSaa(project,
                                                                                                                               selectedSaa,
                                                                                                                               structureHelper);
                if (!modelsGroupsAndScopesForSaa.isEmpty()) {
                    for (DBDataObjectAttributes modelGroupAndScopeForSaa : modelsGroupsAndScopesForSaa) {
                        String modelNo = modelGroupAndScopeForSaa.getFieldValue(iPartsConst.FIELD_EDS_MODEL_MODELNO);
                        if (StrUtils.isValid(modelNo)) {
                            validModels.add(modelNo);
                        }
                    }
                }
                // Falls bei EDS keine Baumuster gefunden wurden, check, ob es bei CTT welche gibt
                if (validModels.isEmpty()) {
                    iPartsDataSAAModelsList modelsList = iPartsDataSAAModelsList.loadDataSAAModelsListForSAA(project, new iPartsSaaId(selectedSaa));
                    modelsList.forEach(modelSaaData -> {
                        String model = modelSaaData.getAsId().getModelNumber();
                        if (StrUtils.isValid(model)) {
                            validModels.add(model);
                        }
                    });
                }
            }
        }

        if (validModels.isEmpty()) {
            // Abfrage ob SA-TU, Sprung nach MBS (EDS wird 'normal' behandelt)
            if (isSaAssembly) {
                // Bestimmung Sprungziel für SA-TU in MBS
                PartListEntryId pId = iPartsMBSHelper.getPathToSaaWithoutValidModelMBS(project, selectedSaa, partListEntry.getPart().getAsId());
                if (pId != null) {
                    // Sprungziel in MBS für SA-TU vorhanden
                    iPartsGotoHelper.gotoMBSConstruction(connector, connector.getActiveForm(), true, pId);
                    return;
                }
                // Fehlermeldung ausgeben
                String key = "!!Es wurde kein Eintrag in der EDS/MBS Konstruktion gefunden, der die SAA \"%1\" enhält.";
                iPartsNumberHelper helper = new iPartsNumberHelper();
                if (!helper.isValidSaa(selectedSaa)) {
                    key = "!!Es wurde kein Eintrag in der EDS/MBS Konstruktion gefunden, der den Baukasten \"%1\" enthält.";
                }

                MessageDialog.showError(TranslationHandler.translate(key,
                                                                     iPartsNumberHelper.formatPartNo(project, selectedSaa)),
                                        IPARTS_MENU_TEXT_OPEN_CONSTRUCTION_WINDOW);
                return;
            }

            MessageDialog.showError(TranslationHandler.translate("!!Keine Baumustergültigkeiten gefunden. " +
                                                                 "Ein Sprung in die Konstruktions-Stückliste ist dadurch nicht möglich."),
                                    IPARTS_MENU_TEXT_OPEN_CONSTRUCTION_WINDOW);
            return;
        }
        if (goToEDSConstPartsList(project, connector, selectedSaa, validModels, partListEntry, structureHelper)) {
            return;
        }
        if (goToMBSConstPartsList(project, connector, selectedSaa, validModels, partListEntry)) {
            return;
        }
        if (goToCTTConstPartsList(project, connector, selectedSaa, validModels, partListEntry)) {
            return;
        }
        // Fehlermeldung ausgeben
        String key = "!!Es wurde kein Baumuster in EDS/MBS/CTT gefunden, das die SAA" +
                     " \"%1\" enthält und Teil der" +
                     " Baumustergültigkeiten ist.%2Gültige Baumuster: %3";
        iPartsNumberHelper helper = new iPartsNumberHelper();
        if (!helper.isValidSaa(selectedSaa)) {
            key = "!!Es wurde kein Baumuster in EDS/MBS/CTT gefunden, das" +
                  " den Baukasten \"%1\" enthält und Teil der" +
                  " Baumustergültigkeiten ist.%2Gültige Baumuster: %3";
        }

        MessageDialog.showError(TranslationHandler.translate(key, iPartsNumberHelper.formatPartNo(project, selectedSaa),
                                                             "\n", StrUtils.stringListToString(validModels, ", ")),
                                IPARTS_MENU_TEXT_OPEN_CONSTRUCTION_WINDOW);
    }

    private static boolean goToMBSConstPartsList(EtkProject project, AssemblyListFormIConnector connector,
                                                 String selectedSaa, List<String> validModels,
                                                 EtkDataPartListEntry partListEntry) {
        // keine Einträge in EDS gefunden => Suche in MBS
        PartListEntryId pId = iPartsMBSHelper.getPathToSaaWithValidModelMBS(project, selectedSaa, validModels, partListEntry.getPart().getAsId());
        if (pId != null) {
            // Sprungziel in MBS vorhanden
            iPartsGotoHelper.gotoMBSConstruction(connector, connector.getActiveForm(), true, pId);
            return true;
        }
        return false;
    }

    private static boolean goToEDSConstPartsList(EtkProject project, AssemblyListFormIConnector connector, String selectedSaa,
                                                 List<String> validModels, EtkDataPartListEntry partListEntry,
                                                 iPartsEdsStructureHelper structureHelper) {
        EtkDataObject pathWithValidModel = getPathToEDSBCSSaaWithValidModel(project, selectedSaa, validModels, structureHelper);
        if (pathWithValidModel == null) {
            return false;
        }
        String modelNo = pathWithValidModel.getFieldValue(structureHelper.getModelNumberField());
        String subElement = pathWithValidModel.getFieldValue(structureHelper.getSubElementField());
        HierarchicalIDWithType structureId = structureHelper.createStructureIdFromDataObject(pathWithValidModel);

        // Bestimme PartListEntry in der EDS-Konstruktion
        PartListEntryId destPartListEntryId = findEDSConstructionPartListEntry(project, modelNo, structureId, subElement,
                                                                               partListEntry.getPart().getAsId());

        // Springe zu EDS
        iPartsGotoHelper.gotoEDSConstruction(connector, connector.getActiveForm(), true, modelNo, structureId,
                                             subElement, destPartListEntryId);
        return true;
    }

    private static boolean goToCTTConstPartsList(EtkProject project, AssemblyListFormIConnector connector, String selectedSaa,
                                                 List<String> validModels, EtkDataPartListEntry partListEntry) {
        Map<String, Set<String>> cttModelMap = SessionKeyHelper.getSelectedCTTModelMap();
        Set<String> sortedModels = new LinkedHashSet<>();
        for (String modelNo : validModels) {
            if (isModelInConstModelMap(cttModelMap, modelNo)) {
                sortedModels.add(modelNo);
            }
        }
        sortedModels.addAll(validModels);

        iPartsDataSAAModelsList modelsList = iPartsDataSAAModelsList.loadDataSAAModelsListForSAA(project, new iPartsSaaId(selectedSaa));
        String firstModel = null;
        for (String validModel : sortedModels) {
            for (iPartsDataSAAModels modelForSaa : modelsList) {
                String modelNo = modelForSaa.getAsId().getModelNumber();
                if (validModel.equals(modelNo)) {
                    firstModel = modelNo;
                    break;
                }
            }
            if (firstModel != null) {
                break;
            }
        }
        if (firstModel == null) {
            return false;
        }

        // Bestimme PartListEntry in der CTT-Konstruktion
        PartListEntryId destPartListEntryId = findCTTConstructionPartListEntry(project, firstModel, selectedSaa,
                                                                               partListEntry.getPart().getAsId());
        // Springe zu CTT
        iPartsGotoHelper.gotoCTTConstruction(connector, connector.getActiveForm(), true, firstModel,
                                             selectedSaa, destPartListEntryId);
        return true;
    }

    /**
     * Sucht die übergebene Teilenummer in der übergebenen EDS-Stückliste.
     *
     * @param project
     * @param modelNo
     * @param structureId
     * @param saaBkNo
     * @param searchPartId
     * @return Die Id des Stücklisteneintrags, falls er gefunden wurde, ansonsten {@code null}.
     */
    private static PartListEntryId findEDSConstructionPartListEntry(EtkProject project, String modelNo, HierarchicalIDWithType structureId,
                                                                    String saaBkNo, PartId searchPartId) {
        List<iPartsVirtualNode> virtualNodePath = iPartsGotoHelper.createVirtualNodePathForEDSConstruction(modelNo,
                                                                                                           structureId,
                                                                                                           saaBkNo);
        String virtualIdString = iPartsVirtualNode.getVirtualIdString(virtualNodePath.toArray(new iPartsVirtualNode[virtualNodePath.size()]));

        iPartsAssemblyId assemblyId = new iPartsAssemblyId(virtualIdString, "");
        EtkDataAssembly destAssembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

        for (EtkDataPartListEntry edsPartListEntry : destAssembly.getPartListUnfiltered(null)) {
            if (edsPartListEntry.getPart().getAsId().equals(searchPartId)) {
                return edsPartListEntry.getAsId();
            }
        }

        return null;
    }

    /**
     * Sucht die übergebene Teilenummer in der übergebenen CTT-Stückliste.
     *
     * @param project
     * @param modelNo
     * @param saaBkNo
     * @param searchPartId
     * @return Die Id des Stücklisteneintrags, falls er gefunden wurde, ansonsten {@code null}.
     */
    private static PartListEntryId findCTTConstructionPartListEntry(EtkProject project, String modelNo, String saaBkNo,
                                                                    PartId searchPartId) {
        List<iPartsVirtualNode> virtualNodePath = iPartsGotoHelper.createVirtualNodePathForCTTConstruction(modelNo,
                                                                                                           saaBkNo);
        String virtualIdString = iPartsVirtualNode.getVirtualIdString(virtualNodePath.toArray(new iPartsVirtualNode[virtualNodePath.size()]));

        iPartsAssemblyId assemblyId = new iPartsAssemblyId(virtualIdString, "");
        EtkDataAssembly destAssembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

        for (EtkDataPartListEntry edsPartListEntry : destAssembly.getPartListUnfiltered(null)) {
            if (edsPartListEntry.getPart().getAsId().equals(searchPartId)) {
                return edsPartListEntry.getAsId();
            }
        }

        return null;
    }

    /**
     * DAIMLER-10319: Suche nach einem Verwendungspfad der übergebenen SAA in einer SAA Konstruktionsstückliste. Es wird
     * der Pfad zurückgeliefert, dessen Baumuster als Erstes in der übergebenen BM-Gültigkeit enthalten ist.
     *
     * @param project
     * @param saa
     * @param validModels
     * @param edsStructureHelper
     * @return {@code null}, falls kein Pfad gefunden wurde, der zu der BM-Gültigkeit passt
     */
    private static EtkDataObject getPathToEDSBCSSaaWithValidModel(EtkProject project, String saa, List<String> validModels, iPartsEdsStructureHelper edsStructureHelper) {
        iPartsEdsStructureHelper structureHelper = (edsStructureHelper == null) ? iPartsEdsStructureHelper.getInstance() : edsStructureHelper;
        DBDataObjectAttributesList modelsStructureForSaa = iPartsVirtualAssemblyEdsBase.loadModelStructureForSaa(project, saa, structureHelper);
        if (!modelsStructureForSaa.isEmpty()) {
            // Übergebene Baumuster so umsortieren, dass zuerst die bereits in EDS konfigurierten BM untersucht werden
            // => beim Sprung wird die Laufzeit verkürzt
            Map<String, Set<String>> edsModelMap = SessionKeyHelper.getSelectedEDSModelMap();
            Set<String> sortedModels = new LinkedHashSet<>();
            for (String modelNo : validModels) {
                if (isModelInConstModelMap(edsModelMap, modelNo)) {
                    sortedModels.add(modelNo);
                }
            }
            sortedModels.addAll(validModels);

            String modelNumberField = structureHelper.getModelNumberField();
            String upperStructureValueField = structureHelper.getUpperStructureValueField();
            String lowerStructureValueField = structureHelper.getLowerStructureValueField();
            for (String validModel : sortedModels) {
                for (DBDataObjectAttributes modelStructureForSaa : modelsStructureForSaa) {
                    String modelNo = modelStructureForSaa.getFieldValue(modelNumberField);
                    if (validModel.equals(modelNo)) {
                        String module = modelStructureForSaa.getFieldValue(upperStructureValueField);
                        String subModule = modelStructureForSaa.getFieldValue(lowerStructureValueField);
                        EtkDataObject pathToSaaObject = structureHelper.createPathToSaaWithValidModelObject(project, modelNo,
                                                                                                            module, subModule);
                        pathToSaaObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                        pathToSaaObject.setFieldValue(structureHelper.getSubElementField(), saa, DBActionOrigin.FROM_DB);
                        return pathToSaaObject;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isModelInConstModelMap(Map<String, Set<String>> edsModelMap, String modelNo) {
        if ((edsModelMap != null) && StrUtils.isValid(modelNo)) {
            Set<String> models = null;
            if (iPartsModel.isAggregateModel(modelNo)) {
                models = edsModelMap.get(MODEL_NUMBER_PREFIX_AGGREGATE);
            }
            if (iPartsModel.isVehicleModel(modelNo)) {
                models = edsModelMap.get(MODEL_NUMBER_PREFIX_CAR);
            }
            if (models != null) {
                return models.contains(modelNo);
            }
        }
        return false;
    }


    /**
     * Erzeugt ein neues iParts-sepzifisches {@link AbstractMechanicForm}.
     * RootParentWindow von callerForm bestimmt die Größe des Fensters mit
     *
     * @param dataConnector
     * @param parentForm
     * @param callerForm
     */
    public iPartsMechanicFormWindow(final MechanicFormIConnector dataConnector, final AbstractJavaViewerForm parentForm, final AbstractJavaViewerForm callerForm) {
        super(dataConnector, parentForm);
        GuiPanel toolbarPanel = new GuiPanel(new LayoutBorder());

        // Die menuBar wird effektiv gar nicht angezeigt, ist aber notwendig, damit der MenuManager und dessen Verwendungen
        // korrekt funktionieren
        menuBar = new GuiMenuBar();
        JavaViewerMainWindow mainWindow = dataConnector.getMainWindow();
        dataConnector.setMenuManager(new MenuManager(mainWindow, menuBar));

        // SecondToolbar
        toolbar = new GuiToolbar();
        toolbar.setName("secondToolbar");
        toolbar.setBackgroundImage(DesignImage.toolbar2Background.getImage());
        toolbar.setButtonLayout(de.docware.framework.modules.gui.controls.toolbar.ToolButtonLayout.IMAGE_WEST);
        toolbar.setButtonStyle(de.docware.framework.modules.gui.controls.toolbar.ToolButtonStyle.SMALL2);
        toolbarManager = new EtkToolbarManager(dataConnector, toolbar);
        dataConnector.setToolbarManager(toolbarManager);

        toolbar.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        toolbarPanel.addChild(toolbar);

        // MechanicForm
        mechanicForm = dataConnector.getProject().getFormFactory().createMechanicForm(dataConnector, this);

        // Stücklisten verzögert aktualisieren mit Unterdrücken des automatischen Updates bei Sichtbarwerden, da das Aktualisieren
        // erst durch den Toolbar-Button ausgelöst werden soll
        mechanicForm.getAssemblyListForm().setDelayedUpdates(true);
        mechanicForm.getAssemblyListForm().setSuppressAutomaticDelayedUpdates(true);

        dataConnector.setActiveForm(mechanicForm); // Das MechanicForm ist IMMER aktiv
        dataConnector.updateAllViews(null, true, null);

        // Panel enthält Schnellsuche, einen Separator und einen Button
        GuiPanel toolbarEastPanel = new GuiPanel(new LayoutBorder());
        toolbarEastPanel.setName("toolbarEastPanel");
        toolbarEastPanel.setBackgroundImage(DesignImage.toolbar2Background.getImage());

        SearchFormConnector searchFormConnector = new SearchFormConnector(dataConnector);
        final AbstractSearchForm searchForm = dataConnector.getProject().getFormFactory().createSearchForm(searchFormConnector, mechanicForm);
        AbstractGuiControl fastSearchGui = searchForm.getFastSearchGui();
        if (fastSearchGui != null) {
            //Schnellsuche ist konfiguriert. Einbauen in die Oberfläche des Javaviewers
            fastSearchGui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_WEST));
            toolbarEastPanel.addChild(fastSearchGui);
        }
        searchForm.addSearchEventHandler(new SearchEvents() {
            @Override
            public void OnClear() {
                mechanicForm.onClearSearchResults();
            }
        });

        // Toolbarbutton "Fenster aktualisieren" hinzufügen
        buttonRefreshWindow = iPartsToolbarButtonAlias.REFRESH_WINDOW.createToolButton(
                new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        buttonRefreshWindow.setVisible(false);
                        buttonRefreshWindow.setHover(false);
                        mechanicForm.getAssemblyListForm().updateAssemblyWhileShowingLabel();
                    }
                }
        );
        buttonRefreshWindow.setName("buttonRefreshWindow");
        buttonRefreshWindow.setVisible(false);

        // Toolbarbutton "Übernahme der aktuellen Teileselektion ins Ursprungsfenster und schließen des aktuellen Fensters" hinzufügen
        GuiToolButton buttonMoveToParentWindowAndClose = iPartsToolbarButtonAlias.MOVE_TO_PARENTWINDOW.createToolButton(
                new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        NavigationPath path = new NavigationPath(mechanicForm.getAssemblyTreeForm().getConnector().getCurrentNavigationPath());
                        List<EtkDataPartListEntry> selectedEntries = mechanicForm.getAssemblyListForm().getConnector().getSelectedPartListEntries();

                        String lfdNr = "";
                        if (mechanicForm.getAssemblyListForm().getConnector().getSelectedPartListEntries().size() > 0) {
                            lfdNr = selectedEntries.get(0).getAsId().getKLfdnr();
                        }

                        GotoPartWithPartialPathEvent partWithPartialPathEvent = new GotoPartWithPartialPathEvent(
                                path, mechanicForm.getConnector().getCurrentAssembly().getAsId(),
                                lfdNr, false, false, callerForm);

                        dataConnector.getProject().fireProjectEvent(partWithPartialPathEvent);

                        getGui().setVisible(false);
                    }
                }
        );
        buttonMoveToParentWindowAndClose.setName("buttonMoveToParentWindowAndClose");

        GuiToolButton searchSeparator = GuiToolbarManager.createSeparator();
        // Toolbar setzen ist notwending, damit der Button angezeigt wird, da sonst in GuiToolButton.reinitComponents() die toolbar null ist
        buttonRefreshWindow.setGuiToolbar(toolbar);
        buttonMoveToParentWindowAndClose.setGuiToolbar(toolbar);
        searchSeparator.setGuiToolbar(toolbar);

        searchSeparator.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        searchSeparator.setPadding(2);
        toolbarEastPanel.addChild(searchSeparator);

        GuiPanel buttonsEastPanel = new GuiPanel();
        buttonsEastPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_EAST));
        buttonsEastPanel.setBackgroundColor(Colors.clTransparent.getColor());
        buttonRefreshWindow.setPadding(2);
        buttonsEastPanel.addChild(buttonRefreshWindow);
        buttonMoveToParentWindowAndClose.setPadding(2);
        buttonsEastPanel.addChild(buttonMoveToParentWindowAndClose);
        toolbarEastPanel.addChild(buttonsEastPanel);

        toolbarEastPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_EAST));
        toolbarPanel.addChild(toolbarEastPanel);

        // EventListener für die Schnellsuche
        getProject().addAppEventListener(new ObserverCallback(callbackBinder, SearchEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus meinem eigenen SearchForm verarbeiten
                SearchEvent event = (SearchEvent)call;
                if (isValidEventFromSender(event)) {
                    searchForm.startSearchFromEvent(event);
                }
            }
        });

        getProject().addAppEventListener(new ObserverCallback(callbackBinder, SearchCancelEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus demselben RootParentForm verarbeiten
                SearchCancelEvent event = (SearchCancelEvent)call;
                if (isValidEventFromSender(event)) {
                    searchForm.cancelSearch();
                }
            }
        });

        // EventListener für Validierungen und Sprünge
        getProject().addAppEventListener(new ObserverCallback(callbackBinder, AssemblyValidEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus demselben RootParentForm verarbeiten
                AssemblyValidEvent event = (AssemblyValidEvent)call;
                if (isValidEventFromSender(event) && mechanicForm.canShowAssembly(event.getId(), event.isPreferTopDown())) {
                    event.setValid(true);
                }
            }
        });

        getProject().addAppEventListener(new ObserverCallback(callbackBinder, NavigationPathValidEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus demselben RootParentForm verarbeiten
                NavigationPathValidEvent event = (NavigationPathValidEvent)call;
                if (isValidEventFromSender(event) && mechanicForm.canShowNavigationPath(event)) {
                    event.setValid(true);
                }
            }
        });

        getProject().addAppEventListener(new ObserverCallback(callbackBinder, GotoPartWithPartialPathEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus demselben RootParentForm verarbeiten
                GotoPartWithPartialPathEvent event = (GotoPartWithPartialPathEvent)call;
                if (isValidEventFromSender(event) && mechanicForm.canShowAssembly(event.getAssemblyId(), event.isPreferTopDown())) {
                    boolean selectionDone = mechanicForm.gotoPartWithPartialPath(event.getPath(), event.getAssemblyId(),
                                                                                 event.getkLfdNr(), event.isMarkEntryAsSearchResult(),
                                                                                 event.isNavigateToSubAssembly(),
                                                                                 event.getImageIndex(), event.isForceThumbnails(),
                                                                                 event.isTryWithoutNavigationPath(),
                                                                                 event.isPreferTopDown());
                    event.setFound(selectionDone);
                }
            }
        });

        getProject().addAppEventListener(new ObserverCallback(callbackBinder, MaterialValidEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus demselben RootParentForm verarbeiten
                MaterialValidEvent event = (MaterialValidEvent)call;
                if (isValidEventFromSender(event) && mechanicForm.canShowMaterial(event.getId())) {
                    event.setValid(true);
                }
            }
        });

        getProject().addAppEventListener(new ObserverCallback(callbackBinder, GotoMaterialEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus demselben RootParentForm verarbeiten
                GotoMaterialEvent event = (GotoMaterialEvent)call;
                if (isValidEventFromSender(event) && mechanicForm.canShowMaterial(event.getPartId())) {
                    mechanicForm.gotoMaterial(event.getPartId());
                }
            }
        });

        getProject().addAppEventListener(new ObserverCallback(callbackBinder, ImageValidEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus demselben RootParentForm verarbeiten
                ImageValidEvent event = (ImageValidEvent)call;
                if (isValidEventFromSender(event) && mechanicForm.canShowImage(event.getpoolEntryId(), event.getAssemblyId())) {
                    event.setValid(true);
                }
            }
        });

        getProject().addAppEventListener(new ObserverCallback(callbackBinder, GotoImageEvent.class) {
            @Override
            public void callback(ObserverCall call) {
                // Nur Events aus demselben RootParentForm verarbeiten
                GotoImageEvent event = (GotoImageEvent)call;
                if (isValidEventFromSender(event) && mechanicForm.canShowImage(event.getImageId(), null)) {
                    mechanicForm.gotoImage(event.getImageId(), null);
                }
            }
        });

        // Baugruppenbaum initial ausblenden
        if (mechanicForm instanceof ResponsiveMechanicForm) {
            ((ResponsiveMechanicForm)mechanicForm).setNavigationMenuCollapsed(true);
        } else {
            GuiToolButton buttonShowTree = (GuiToolButton)toolbarManager.getButton(ToolbarButtonAlias.SHOW_TREE.getAlias());
            if ((buttonShowTree != null) && buttonShowTree.isPressed()) {
                buttonShowTree.doClick();
            }
        }

        // GUI zusammenbasteln
        // Initiale Größe 50 Pixel kleiner als das öffnende Fenster
        GuiWindow rootWindow = null;
        if (callerForm != null) {
            rootWindow = callerForm.getRootParentWindow();
        }
        Dimension screenSize = FrameworkUtils.getScreenSize(rootWindow);
        window = new GuiWindow(createTitle(), screenSize.width - 50, screenSize.height - 50);

        window.setLayout(new LayoutBorder());
        toolbarPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_NORTH));
        window.addChild(toolbarPanel);
        mechanicForm.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        window.addChild(mechanicForm.getGui());

        // Dieses Form freigeben, wenn das Fenster geschlossen wird
        addCloseNonModalWindowListener(window);
    }

    @Override
    public AbstractGuiControl getGui() {
        return window;
    }

    @Override
    public MechanicFormIConnector getConnector() {
        return (MechanicFormIConnector)super.getConnector();
    }

    /**
     * Zeigt das Fenster nicht-modal an.
     */
    public void showNonModal() {
        window.showNonModal(OPEN_IN_NEW_WINDOW);
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (window != null) {
            window.setTitle(createTitle());
        }

        if (buttonRefreshWindow != null) {
            // Analog zu AssemblyListForm.updateData() ermitteln, ob die Stückliste aktualisiert werden müsste (neu laden
            // bzw. verzögert neu aufbauen), wobei bei forceUpdateAll bzw. Ändern der aktuellen AssemblyId die Stückliste
            // sofort neu geladen wird
            if (forceUpdateAll || getConnector().isFlagCurrentAssemblyIdChanged()) {
                buttonRefreshWindow.setVisible(false);
            } else if (!buttonRefreshWindow.isVisible() && (mechanicForm.getAssemblyListForm().isReloadAssemblyNeeded(sender)
                                                            || (mechanicForm.getAssemblyListForm().isDelayedUpdateAssemblyNeeded()
                                                                && !mechanicForm.getAssemblyListForm().isImmediateUpdateAssemblyNeeded()))) {
                buttonRefreshWindow.setVisible(true);
            }
        }
    }

    private String createTitle() {
        return TranslationHandler.translate("!!iParts Autorensystem - %1", mechanicForm.getConnector().getCurrentAssemblyText());
    }
}