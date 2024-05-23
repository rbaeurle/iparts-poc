/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SimpleDoubleListSelectForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.mainview.forms.AbstractMechanicForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMPartHistoryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaaHistoryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper.iPartsSaaBkConstPartsListHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyCTT;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyEdsBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyMBS;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ConstructionValidationDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMBSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.ctt.iPartsCTTModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditSessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutFlow;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.sort.SortStringCache;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class AssemblyListWithHistoryForm extends AbstractJavaViewerForm {

    enum FormType {
        MBS, EDS, CTT
    }

    private AssemblyListForm assemblyListForm;
    private RComboBox<String> revisionStateComboBox;
    private String currentRevisionState;
    private Map<String, Calendar> revisionStatesAndDates = new LinkedHashMap<>();
    private EdsSaaId currentEdsSaa;
    private String currentMbsConGroup;      // Darf nur gefüllt sein bei Aufruf über MBS
    private String currentPartNumber;
    private SortStringCache sortStringCache = new SortStringCache();
    private FormType formType;

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        // Separator vor Menüeintrag "Änderungsstände anzeigen..."
        GuiSeparator menuItemSeparator = new GuiSeparator();
        menuItemSeparator.setUserObject("iPartsMenuItemSeparatorShowRevisionStates");
        popupMenu.addChild(menuItemSeparator);

        // Menüeintrag "Änderungsstände anzeigen..." hinzufügen
        GuiMenuItem menuItemShowRevisionStates = new GuiMenuItem();
        menuItemShowRevisionStates.setUserObject("iPartsMenuItemShowRevisionStates");
        menuItemShowRevisionStates.setName("iPartsMenuItemShowRevisionStates");
        menuItemShowRevisionStates.setText("!!Änderungsstände des Teilebaukastens anzeigen");
        menuItemShowRevisionStates.setIcon(EditDefaultImages.edit_history.getImage());
        menuItemShowRevisionStates.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
                if (!selectedPartListEntries.isEmpty()) {
                    // Alle selektierten Teilenummern müssen identisch sein aufgrund von updatePartListPopupMenu() -> nimm die erste
                    EtkDataPartListEntry selectedPartListEntry = selectedPartListEntries.get(0);
                    String partNumber = selectedPartListEntry.getFieldValue(EtkDbConst.FIELD_K_MATNR);
                    showAssemblyListWithHistoryForm(connector, partNumber);
                }
            }
        });
        popupMenu.addChild(menuItemShowRevisionStates);


        // Menüeintrag "KEM-Markierung aufheben" hinzufügen
        GuiMenuItem menuItemShowKemMark = new GuiMenuItem();
        menuItemShowKemMark.setUserObject("iPartsMenuItemShowKemMark");
        menuItemShowKemMark.setName("iPartsMenuItemShowKemMark");
        menuItemShowKemMark.setText("!!KEM-Markierung aufheben");
        menuItemShowKemMark.setIcon(EditDefaultImages.edit_history.getImage());
        menuItemShowKemMark.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                AssemblyId assemblyId = connector.getCurrentAssembly().getAsId();
                List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)connector.getCurrentAssembly()).getVirtualNodesPath();
                if (virtualNodesPath != null) {
                    if (iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)) {
                        EditSessionKeyHelper.resetSessionKeyForMark(assemblyId);
                    } else {
                        EditSessionKeyHelper.resetSessionKeyForMarkMBS(assemblyId);
                    }
                    connector.getProject().fireProjectEvent(new DataChangedEvent(null), true);
                }

            }
        });
        popupMenu.addChild(menuItemShowKemMark);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste eine SAA Konstruktions-Stückliste
        boolean isValidAssemblyForShowRevisionStates = false;
        // Ist die aktuelle Stückliste MBS?
        boolean isValidAssemblyForShowRevisionStatesMBS = false;
        AssemblyId assemblyId = null;
        if (connector.getProject().isEditModeActive()) {
            // Bei identischen Teilen werden diese doppelt selektiert in der Stückliste -> es darf insgesamt nur genau
            // eine Teilenummer selektiert sein
            Set<String> selectedPartNumbers = new HashSet<>();
            List<EtkDataPartListEntry> selectedEntries = connector.getSelectedPartListEntries();
            for (EtkDataPartListEntry selectedPartListEntry : selectedEntries) {
                selectedPartNumbers.add(selectedPartListEntry.getFieldValue(EtkDbConst.FIELD_K_MATNR));
            }
            if (selectedPartNumbers.size() == 1) {
                EtkDataPartListEntry entry = selectedEntries.get(0);
                // Texte können kein Baukasten sein
                if (!VirtualMaterialType.isPartListTextEntry(entry)) {
                    EtkDataAssembly assembly = connector.getCurrentAssembly();
                    if (assembly instanceof iPartsDataAssembly) {
                        if (((iPartsDataAssembly)assembly).getAsId().isVirtual()) {
                            assemblyId = assembly.getAsId();
                            List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
                            if (virtualNodesPath != null) {
                                isValidAssemblyForShowRevisionStates = iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)
                                                                       || iPartsVirtualNode.isCTTSaaConstNode(virtualNodesPath);
                                isValidAssemblyForShowRevisionStatesMBS = iPartsVirtualNode.isMBSNode(virtualNodesPath);
                            }
                        }
                    }
                }
            }
        }

        // Separator und Menüeintrag "Änderungsstände anzeigen..." aktualisieren
        for (AbstractGuiControl child : popupMenu.getChildren()) {
            if (child.getUserObject() != null) {
                if (child.getUserObject().equals("iPartsMenuItemSeparatorShowRevisionStates")) {
                    child.setVisible(isValidAssemblyForShowRevisionStates);
                } else if (child.getUserObject().equals("iPartsMenuItemShowRevisionStates")) {
                    child.setVisible(isValidAssemblyForShowRevisionStates);
                } else if (child.getUserObject().equals("iPartsMenuItemShowKemMark")) {
                    boolean showMarkMenu = isValidAssemblyForShowRevisionStates || isValidAssemblyForShowRevisionStatesMBS;
                    if (showMarkMenu) {
                        showMarkMenu = (assemblyId != null);
                        if (showMarkMenu) {
                            if (isValidAssemblyForShowRevisionStates) {
                                showMarkMenu = EditSessionKeyHelper.isKemMarkSetForAssembly(assemblyId);
                            } else {
                                showMarkMenu = EditSessionKeyHelper.isKemMarkSetForAssemblyMBS(assemblyId);
                            }
                        }
                    }
                    child.setVisible(showMarkMenu);
                }
            }
        }
    }

    /**
     * Zeigt die Änderungsstände für die Stückliste an.
     *
     * @param connector
     * @param partNumber Teilenummer für den Teilebaukasten bzw. {@code null} für die komplette SAA
     */
    public static void showAssemblyListWithHistoryForm(AssemblyListFormIConnector connector, String partNumber) {
        AssemblyListWithHistoryFormConnector assemblyListWithHistoryFormConnector = new AssemblyListWithHistoryFormConnector(connector,
                                                                                                                             connector.getCurrentAssembly(),
                                                                                                                             connector.getCurrentNavigationPath(),
                                                                                                                             partNumber);
        AssemblyListWithHistoryForm assemblyListWithHistoryForm = new AssemblyListWithHistoryForm(assemblyListWithHistoryFormConnector,
                                                                                                  connector.getActiveForm());
        assemblyListWithHistoryForm.addOwnConnector(assemblyListWithHistoryFormConnector);
        assemblyListWithHistoryForm.showModal();
    }

    /**
     * Erzeugt eine Instanz von AssemblyListWithHistoryForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public AssemblyListWithHistoryForm(AssemblyListWithHistoryFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.mainPanel;
    }

    @Override
    public AssemblyListWithHistoryFormIConnector getConnector() {
        return (AssemblyListWithHistoryFormIConnector)super.getConnector();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        assemblyListForm.dispose();
        dispose();
        return modalResult;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        currentPartNumber = getConnector().getPartNumber();
        String partNumberFormatted = null;
        if (StrUtils.isValid(currentPartNumber)) { // Teilebaukasten
            mainWindow.setTitle(mainWindow.getTitle() + " " + TranslationHandler.translate("!!des Teilebaukastens"));
            partNumberFormatted = iPartsNumberHelper.formatPartNo(getProject(), currentPartNumber);
        } else { // SAA
            mainWindow.setTitle(mainWindow.getTitle() + " " + TranslationHandler.translate("!!der SAA/BK"));
        }

        setHeading(true);

        setCurrentSAAId();

        // SAA- bzw. Teilekasten-DataObjects aus DB laden
        boolean hasRevisionStates = loadAllPossibleRevisionStates();

        revisionStateComboBox = RComboBox.replaceGuiComboBox(mainWindow.revisionStateComboBox);
        revisionStateComboBox.setFilterable(false);
        revisionStateComboBox.setMaximumRowCount(10);
        revisionStateComboBox.switchOffEventListeners();
        try {
            if (formType != FormType.MBS) {
                revisionStateComboBox.addItems(sortRevisionState(revisionStatesAndDates.keySet()));
            } else {
                if (hasRevisionStates) {
                    // Bei MBS gibt es keine Änderungsstände. Im Drop-Down Menü stehen die Kem-Datumswerte
                    SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.simpleDateTimeFormatddDOTMMDOTyyyy);
                    List<String> revisionDatesAsString = new ArrayList<>();
                    revisionStatesAndDates.values().forEach(date -> {
                        revisionDatesAsString.add(sdf.format(date.getTime()));
                    });
                    revisionStateComboBox.addItems(revisionDatesAsString);
                } else {
                    revisionStateComboBox.addItem(TranslationHandler.translate("!!Nur Texte vorhanden"));
                }
            }
            revisionStateComboBox.setEnabled(hasRevisionStates);
        } finally {
            revisionStateComboBox.switchOnEventListeners();
        }
        // höchster Änderungsstand soll das aktuelle Datum erhalten
        // Nur nicht bei MBS
        if (formType != FormType.MBS) {
            changeHighestRevisionValue();
        }

        assemblyListForm = new IPartsEditAssemblyListHistoryForm();
        setCurrentRevisionState(revisionStateComboBox.getItem(0));

        if (StrUtils.isEmpty(currentPartNumber) || hasRevisionStates || !getConnector().getCurrentPartListEntries().isEmpty()) { // SAA oder Teilebaukasten
            assemblyListForm.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
            mainWindow.assemblyListPanel.addChild(assemblyListForm.getGui());
            Dimension screenSize = FrameworkUtils.getScreenSize();
            mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
        } else { // Kein Teilebaukasten
            GuiLabel noConstKitLabel = new GuiLabel(TranslationHandler.translate("!!Das Teil \"%1\" ist kein Teilebaukasten.",
                                                                                 partNumberFormatted));
            noConstKitLabel.setHorizontalAlignment(GuiLabel.HorizontalAlignment.CENTER);
            noConstKitLabel.setPaddingTop(16);
            noConstKitLabel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
            mainWindow.assemblyListPanel.addChild(noConstKitLabel);
            mainWindow.northPanel.setVisible(false);
            mainWindow.setSize(400, 140);
        }
    }

    private void setHeading(boolean isInit) {
        String heading;
        String partNumberFormatted = null;
        if (StrUtils.isValid(currentPartNumber)) { // Teilebaukasten
            partNumberFormatted = iPartsNumberHelper.formatPartNo(getProject(), currentPartNumber);
            heading = partNumberFormatted;
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), new PartId(currentPartNumber, ""));
            String partName = part.getFieldValue(EtkDbConst.FIELD_M_TEXTNR, getProject().getDBLanguage(), true);
            if (!partName.isEmpty()) {
                heading += ": " + partName;
            }
        } else { // SAA
            heading = getConnector().getCurrentAssembly().getHeading1(getConnector().getImageIndex(), getConnector().getCurrentNavigationPath());
        }

        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
        String kemNos = EditSessionKeyHelper.buildKemNoList(currentAssembly.getAsId());
        if (StrUtils.isValid(kemNos)) {
            String key = "!!Markierte KEM-Nummer: %1";
            if (kemNos.contains(",")) {
                key = "!!Markierte KEM-Nummern: %1";
            }
            String kemText = TranslationHandler.translate(key, kemNos);
            if (StrUtils.isValid(heading)) {
                heading += " - " + kemText;
            } else {
                heading = kemText;
            }
        }

        mainWindow.headingLabel.setVisible(!heading.isEmpty());
        if (!heading.isEmpty()) {
            mainWindow.headingLabel.setText(heading);
            if (isInit) {
                AbstractMechanicForm.applyHeaderDesign(getConfig(), "1", mainWindow.headingLabel);
            }
        }
    }

    /**
     * Setzt zum höchsten Änderungsstand das aktuelle Datum
     */
    private void changeHighestRevisionValue() {
        String highestRev = revisionStateComboBox.getItem(0);
        revisionStatesAndDates.put(highestRev, Calendar.getInstance());
    }

    /**
     * Sortiert die Änderungsstände
     *
     * @param strings
     * @return
     */
    private List<String> sortRevisionState(Set<String> strings) {
        List<String> revisions = new ArrayList<>(strings);
        revisions.sort((o1, o2) -> Integer.valueOf(o2).compareTo(Integer.valueOf(o1)));

        return revisions;
    }

    /**
     * Holt sich die ID zu aktuellen SAA
     */
    public void setCurrentSAAId() {
        if (getConnector().getCurrentAssembly() instanceof iPartsDataAssembly) {

            iPartsDataAssembly assembly = (iPartsDataAssembly)getConnector().getCurrentAssembly();
            List<iPartsVirtualNode> nodes = assembly.getVirtualNodesPath();
            if (nodes == null) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Assembly is no virtual EDS/MBS parts list: " + assembly.getAsId().toStringForLogMessages());
                this.closeWindow(null);
                return;
            }

            iPartsVirtualNode lastNode = nodes.get(nodes.size() - 1);
            if (lastNode.getId() instanceof EdsSaaId) {
                currentEdsSaa = (EdsSaaId)lastNode.getId();
                iPartsVirtualNode modelNode = nodes.get(0);
                // Bei einer CTT Stückliste muss die HMO Nummer zur SAA bestimmt werden, weil die Stücklistendaten
                // zur HMO Nummer importiert werden
                if ((modelNode != null) && (modelNode.getType() == iPartsNodeType.CTT_MODEL)) {
                    // Das CTT BM muss schon geladen sein, weil wir ja in der Konstruktionsstückliste sind
                    iPartsCTTModel model = iPartsCTTModel.getInstance(getProject(), (iPartsModelId)modelNode.getId());
                    String mappedHMONumber = model.getHmoForSaa(currentEdsSaa.getSaaNumber());
                    // Nur wenn eine HMO Nummer existiert, wird diese auch verwendet.
                    if (StrUtils.isValid(mappedHMONumber)) {
                        currentEdsSaa = new EdsSaaId(mappedHMONumber);
                    }
                    this.formType = FormType.CTT;
                } else {
                    this.formType = FormType.EDS;
                }
            } else if (lastNode.getId() instanceof MBSStructureId) {
                currentMbsConGroup = ((MBSStructureId)lastNode.getId()).getConGroup();
                this.formType = FormType.MBS;
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Last node must be of type EdsSaaId or MBSStructureId");
                this.closeWindow(null);
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Assembly must be of type iPartsDataAssembly.");
            this.closeWindow(null);
        }

    }

    /**
     * Holt sich zum aktuellen Änderungstand + Datum die passenden PartListEntries und übergibt sie an den Connector
     */
    private void loadCurrentPartListEntries() {
        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
        DBDataObjectList<EtkDataPartListEntry> sortedPartList;
        if (formType != FormType.MBS) {
            if (currentEdsSaa == null) { // Falls bei der Initialisierung schon was schiefgegangen ist
                getConnector().setCurrentPartListEntries(new DwList<>());
                setHeading(false);
                return;
            }

            String saaOrConstKitNo = StrUtils.isValid(currentPartNumber) ? currentPartNumber : currentEdsSaa.getSaaNumber();
            int maxLevelFromConfig = (formType == FormType.CTT) ? iPartsVirtualAssemblyCTT.getMaxLevelFromConfig()
                                                                : iPartsVirtualAssemblyEdsBase.getMaxLevelFromConfig();
            Calendar calendar = revisionStatesAndDates.get(currentRevisionState);
            // Hier kann es sich nur um CTT oder EDS handeln -> formType = FormType.EDS -> EDS ansonsten CTT
            ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getConstructionDateHelper(calendar, false);
            DBDataObjectList<EtkDataPartListEntry> partList = iPartsSaaBkConstPartsListHelper.getSaaOrConstKitEntries(getProject(),
                                                                                                                      (iPartsAssemblyId)currentAssembly.getAsId(),
                                                                                                                      currentEdsSaa.getSaaNumber(),
                                                                                                                      saaOrConstKitNo, currentRevisionState,
                                                                                                                      validationHelper, maxLevelFromConfig,
                                                                                                                      formType == FormType.EDS);
            // Stücklisteneinträge sortieren
            EtkEbenenDaten partListType = getConfig().getPartsDescription().getEbene(currentAssembly.getEbeneName());
            List<EtkDataPartListEntry> list = partList.getAsList();
            sortedPartList = EtkDataAssembly.sortPartList(list, partListType.getSortFeldName(), getProject(),
                                                          sortStringCache, true, true, currentAssembly);

        } else {
            if (currentMbsConGroup == null) { // Falls bei der Initialisierung schon was schiefgegangen ist
                getConnector().setCurrentPartListEntries(new DwList<>());
                setHeading(false);
                return;
            }

            // Mit dem KEM-Datum der Revision die MBS-Stückliste berechnen
            // Stücklisteneinträge sind schon sortiert
            iPartsVirtualAssemblyMBS mbs = new iPartsVirtualAssemblyMBS(getProject(), null, (iPartsAssemblyId)getConnector().getCurrentAssembly().getAsId());
            Calendar calendar = revisionStatesAndDates.get(currentRevisionState);
            // obwohl es sich um MBS handelt, ist calendar ein vollständiger DateTime => isMBS = false
            ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getConstructionDateHelper(calendar, false);

            sortedPartList = mbs.computeMBSPartslistDataForOneSubPart(currentMbsConGroup, currentMbsConGroup,
                                                                      calendar, 1,
                                                                      iPartsVirtualAssemblyMBS.getMaxLevelFromConfig(),
                                                                      validationHelper);
            mbs.addSeqNumbers(sortedPartList);
        }

        // Stücklisteneinträge filtern
        List<EtkDataPartListEntry> partListFiltered = sortedPartList.getAsList();
        getProject().getFilter().checkFilter(partListFiltered, getProject().getDBLanguage());

        getConnector().setCurrentPartListEntries(partListFiltered);
        setHeading(false);
    }

    /**
     * Läd für die ausgewählte SAA alle möglichen Änderungsstände + Datumsangaben und speicher sie in einer Map ab
     *
     * @return Wurden Änderungsstände gefunden?
     */
    public boolean loadAllPossibleRevisionStates() {
        revisionStatesAndDates.clear();
        boolean revisionsFound = false;
        if (formType != FormType.MBS) {
            if (StrUtils.isValid(currentPartNumber)) {
                iPartsDataBOMPartHistoryList revisionsList = iPartsDataBOMPartHistoryList.loadConstKitHistoryDataForPartNumber(getProject(),
                                                                                                                               currentPartNumber);
                revisionsFound = loadAllPossibleRevisionStates(revisionsList, iPartsConst.FIELD_DBMH_RELEASE_TO, iPartsConst.FIELD_DBMH_REV_FROM);
            } else if (formType == FormType.CTT) {
                // Bei CTT mit der HMO Nummer in der Teilehistorie suchen
                iPartsDataBOMPartHistoryList revisionsList = iPartsDataBOMPartHistoryList.loadConstKitHistoryDataForPartNumber(getProject(),
                                                                                                                               currentEdsSaa.getSaaNumber());
                revisionsFound = loadAllPossibleRevisionStates(revisionsList, iPartsConst.FIELD_DBMH_RELEASE_TO, iPartsConst.FIELD_DBMH_REV_FROM);
            } else if (currentEdsSaa != null) {
                iPartsDataSaaHistoryList revisionsList = iPartsDataSaaHistoryList.loadSaaHistoryDataForSaa(getProject(), currentEdsSaa.getSaaNumber());
                revisionsFound = loadAllPossibleRevisionStates(revisionsList, iPartsConst.FIELD_DSH_RELEASE_TO, iPartsConst.FIELD_DSH_REV_FROM);
            }
        } else {
            if (StrUtils.isValid(currentMbsConGroup)) {
                NavigableSet<Calendar> kemDates = new TreeSet<>();
                // Wir brauchen die KEM Datums von allen Strukturstufen die innerhalb der Gültgkeit von Strukturstufe 1 liegen
                getAllKemDatesForConGroupAndOptionalSubStructures(getProject(), currentMbsConGroup, kemDates);
                // Nummern der Änderungsstände faken, da es diese bei MBS nicht gibt
                int counter = 0;
                for (Calendar kemDate : kemDates.descendingSet()) {
                    String key = StrUtils.prefixStringWithCharsUpToLength(Integer.toString(++counter), '0', 3);
                    revisionStatesAndDates.put(key, kemDate);
                }

                revisionsFound = !kemDates.isEmpty();
            }
        }

        if (!revisionsFound) {
            // gar keine Revisionen vorhanden, -> zeige die aktuelle an als Änderungsstand ""
            revisionStatesAndDates.put("", Calendar.getInstance());
        }
        return revisionsFound;
    }

    /**
     * Vorbereitung der rekursiven Bestimmung
     *
     * @param project
     * @param startConGroup aktuelle Sachnummer
     * @param kemDates      Set mit den gefundenen KEM Datum Werte
     */
    private void getAllKemDatesForConGroupAndOptionalSubStructures(EtkProject project, String startConGroup,
                                                                   NavigableSet<Calendar> kemDates) {
        int maxLevel = iPartsEditPlugin.getPluginConfig().getConfigValueAsInteger(iPartsEditPlugin.CONFIG_MBS_MAX_STRUCTURE_LEVEL);
        // Aufruf der Rekursion
        getAllKemDatesForConGroupAndOptionalSubStructures(project, startConGroup, kemDates, true,
                                                          1, maxLevel, null);
    }

    /**
     * Sucht beginnend bei einer Oberen Sachnummer nach KEM Datum Werte, wenn gewollt auch in den Unterstrukturen
     * Es kommen keine doppelt vor und keine leeren
     * Pro Ebene sind sie absteigend sortiert
     *
     * @param project
     * @param currentConGroup aktuelle Sachnummer
     * @param kemDates        Set mit den gefundenen KEM Datum Werte -> Rückgabe Wert
     * @param level           aktueller Level für den Aufbau der datesPerLevel
     * @param maxLevel        Begrenzer für die Rekursion
     * @param datesPerLevel   Zwischenergebnis: Gesammelte Datum Werte pro Strukturstufe
     */
    private void getAllKemDatesForConGroupAndOptionalSubStructures(EtkProject project, String currentConGroup,
                                                                   NavigableSet<Calendar> kemDates, boolean withSubStructures,
                                                                   int level, int maxLevel, Map<Integer, NavigableSet<String>> datesPerLevel) {
        if (level > maxLevel) {
            return;
        }
        if (datesPerLevel == null) {
            datesPerLevel = new HashMap<>();
        }
        // Liste aller Unterstrukturen zur aktuellen Struktur
        Set<String> subStructures = new HashSet<>();
        // Alle Datums Werte ab und bis für diese Ebene
        NavigableSet<String> calculatedDatesForCurrentLevel = iPartsMBSHelper.getAllKemDatesForConGroup(project, currentConGroup, subStructures);

        NavigableSet<String> datesForCurrentLevel = datesPerLevel.get(level);
        if (datesForCurrentLevel == null) {
            datesPerLevel.put(level, calculatedDatesForCurrentLevel);
        } else {
            datesForCurrentLevel.addAll(calculatedDatesForCurrentLevel);
        }
        if (withSubStructures) {
            for (String snr : subStructures) {
                getAllKemDatesForConGroupAndOptionalSubStructures(project, snr, kemDates, withSubStructures, level + 1, maxLevel, datesPerLevel);
            }
        }
        if (level == 1) {
            // Rekursion ist beendet. Anreicherung der Level 1 Daten mit den darunterliegenden Stufen
            datesForCurrentLevel = datesPerLevel.get(level);
            if (datesPerLevel.size() > 1) {
                // die höheren Levels in umgekehrter Reihenfolge überprüfen, ob weitere Datumswerte relevant sind
                for (int index = maxLevel; index > 1; index--) {
                    NavigableSet<String> datesForCurrentSubLevel = datesPerLevel.get(index);
                    if (datesForCurrentSubLevel != null) {
                        String minDateTopLevel = datesForCurrentLevel.first();
//                        String maxDateTopLevel = datesForCurrentLevel.last();
                        for (String currentSubLevelDate : datesForCurrentSubLevel) {
                            if (currentSubLevelDate.compareTo(minDateTopLevel) < 0) {
                                // Datum aus Level x ist kleiner als MinDate aus Level 1 => interessiert nicht
                                continue;
                            }
//                            if (currentSubLevelDate.compareTo(maxDateTopLevel) < 0) {
//                                // Datum aus Level x ist kleiner als MaxDate aus Level 1 => interessiert nicht
//                                continue;
//                            }
                            // neues Datum, datesForCurrentLevel verhindert doppelte Einträge
                            datesForCurrentLevel.add(currentSubLevelDate);
                        }
                    }
                }
            }
            if (kemDates != null) {
                datesForCurrentLevel.forEach((dbDate) -> {
                    kemDates.add(SQLStringConvert.ppDateTimeStringToCalendar(dbDate));
                });
            }
        }
    }

    private boolean loadAllPossibleRevisionStates(EtkDataObjectList<? extends EtkDataObject> revisionsList, String fieldNameReleaseDateTo,
                                                  String fieldNameRevisionFrom) {
        if (!revisionsList.isEmpty()) {
            for (EtkDataObject masterDataObject : revisionsList.getAsList()) {
                String valueDate = masterDataObject.getFieldValue(fieldNameReleaseDateTo);
                valueDate = StrUtils.replaceSubstring(valueDate, ".", "");
                String key = masterDataObject.getFieldValue(fieldNameRevisionFrom);
                if (key.contains(".")) {
                    key = StrUtils.stringUpToCharacter(key, ".");
                }
                key = StrUtils.prefixStringWithCharsUpToLength(key, '0', 3);

                // DAIMLER-14529: 1 Sekunde abziehen für die Prüfung auf das KEM-Datum-bis als Stichtag
                Calendar releaseDateTo = SQLStringConvert.ppDateTimeStringToCalendar(valueDate);
                if (releaseDateTo != null) {
                    releaseDateTo.add(Calendar.SECOND, -1);
                }
                revisionStatesAndDates.put(key, releaseDateTo);
            }
            return true;
        }

        return false;
    }


    /**
     * Setzt den aktuell anzuzeigenden Änderungsstand.
     *
     * @param currentRevisionState
     */
    public void setCurrentRevisionState(String currentRevisionState) {
        if (Utils.objectEquals(this.currentRevisionState, currentRevisionState)) {
            return;
        }

        this.currentRevisionState = currentRevisionState;
        int revisionStateIndex;
        // Das Freigabedatum ebenfalls anzeigen, damit man weiß, welches Datum zum Filtern verwendet wurde
        if (formType != FormType.MBS) {
            Calendar revisionDate = revisionStatesAndDates.get(currentRevisionState);
            if (revisionDate != null) {
                DateConfig dateConfig = DateConfig.getInstance(getProject().getConfig());
                String dateTime = dateConfig.formatDateTime(getProject().getViewerLanguage(), DateUtils.toyyyyMMddHHmmss_Calendar(revisionDate));
                mainWindow.labelDateValue.setText(dateTime);
            } else {
                mainWindow.labelDateValue.setText("");
            }
            revisionStateComboBox.setSelectedItem(this.currentRevisionState);
            revisionStateIndex = revisionStateComboBox.getItems().indexOf(this.currentRevisionState);
        } else {
            revisionStateComboBox.setSelectedItem(this.currentRevisionState);

            revisionStateIndex = revisionStateComboBox.getItems().indexOf(this.currentRevisionState);
            this.currentRevisionState = StrUtils.prefixStringWithCharsUpToLength(Integer.toString(revisionStateIndex + 1), '0', 3);
        }
        mainWindow.nextRevisionStateButton.setEnabled(revisionStateIndex > 0);
        mainWindow.lastRevisionStateButton.setEnabled(revisionStateIndex < revisionStateComboBox.getItemCount() - 1);

        getConnector().setCurrentPartlistEntriesInvalid();
        getConnector().updateAllViews(this, false);
    }


    /**
     * Zeigt den vorherigen Änderungsstand an.
     *
     * @param event
     */
    public void showLastRevisionState(Event event) {
        int selectedIndex = revisionStateComboBox.getSelectedIndex();
        if (selectedIndex < revisionStateComboBox.getItemCount() - 1) {
            setCurrentRevisionState(revisionStateComboBox.getItems().get(selectedIndex + 1));
        }
    }

    /**
     * Zeigt den nächsten Änderungsstand an.
     *
     * @param event
     */
    public void showNextRevisionState(Event event) {
        int selectedIndex = revisionStateComboBox.getSelectedIndex();
        if (selectedIndex > 0) {
            setCurrentRevisionState(revisionStateComboBox.getItems().get(selectedIndex - 1));
        }
    }

    private void revisionStateSelected(Event event) {
        setCurrentRevisionState(revisionStateComboBox.getSelectedItem());
    }

    /**
     * Schließt das Fenster.
     *
     * @param event
     */
    public void closeWindow(Event event) {
        close();
    }


    private class IPartsEditAssemblyListHistoryForm extends AssemblyListForm {

        public IPartsEditAssemblyListHistoryForm() {
            super(AssemblyListWithHistoryForm.this.getConnector(), AssemblyListWithHistoryForm.this);
            hideEmptyPlaceHolder = true;
        }

        @Override
        protected void modifyPartListPanel(GuiPanel partListPanel) {
            super.modifyPartListPanel(partListPanel);

            // Falls vorhanden das Control mit ConstraintsBorder.POSITION_NORTH suchen und die Controls für den Änderungsstand
            // dort ganz vorne einhängen
            if (partListPanel.getChildren().size() > 1) {
                AbstractGuiControl northControl = null;
                for (AbstractGuiControl child : partListPanel.getChildren()) {
                    AbstractConstraints childConstraints = child.__internal_getOriginalConstraints();
                    if ((childConstraints instanceof ConstraintsBorder) && ((ConstraintsBorder)childConstraints).getPosition().equals(ConstraintsBorder.POSITION_NORTH)) {
                        northControl = child;
                        break;
                    }
                }

                if (northControl != null) { // northControl durch partListNorthPanel ersetzen
                    GuiPanel partListNorthPanel = new GuiPanel();
                    partListNorthPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_NORTH));
                    partListNorthPanel.setLayout(new LayoutFlow(0, 0, LayoutFlow.ORIENTATION_LEFT));

                    // Controls für den Änderungsstand
                    mainWindow.northPanel.removeFromParent();
                    mainWindow.northPanel.setConstraints(null);

                    // Bei MBS wird das Freigabedatum in der Drop-Box angezeigt. Ein Label ist nicht mehr nötig
                    if (formType == FormType.MBS) {
                        mainWindow.labelDate.removeFromParent();
                        mainWindow.labelDateValue.removeFromParent();
                    }

                    partListNorthPanel.addChild(mainWindow.northPanel);

                    // Separator
                    GuiSeparator separator = new GuiSeparator(DWOrientation.VERTICAL);
                    separator.setMinimumHeight(mainWindow.nextRevisionStateButton.getPreferredHeight());
                    partListNorthPanel.addChild(separator);

                    // bisheriges northControl
                    northControl.removeFromParent();
                    northControl.setConstraints(null);
                    partListNorthPanel.addChild(northControl);

                    partListPanel.addChild(partListNorthPanel);
                }
            }
        }

        @Override
        protected String getEbeneNameForSessionSave() {
            return TableAndFieldName.make(getCurrentAssembly().getEbeneName(), SimpleDoubleListSelectForm.PARTLIST_SOURCE_HISTORY);
        }


        @Override
        public void loadCurrentAssembly() {
            loadCurrentPartListEntries(); // Stücklisteneinträge für aktuellen Änderungsstand laden
            super.loadCurrentAssembly();
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
        private de.docware.framework.modules.gui.controls.GuiLabel headingLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel northPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel revisionStateLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton lastRevisionStateButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> revisionStateComboBox;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton nextRevisionStateButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDate;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDateValue;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel assemblyListPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(1000);
            this.setTitle("!!Stückliste mit Änderungsständen");
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            headingLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            headingLabel.setName("headingLabel");
            headingLabel.__internal_setGenerationDpi(96);
            headingLabel.registerTranslationHandler(translationHandler);
            headingLabel.setScaleForResolution(true);
            headingLabel.setMinimumWidth(10);
            headingLabel.setMinimumHeight(10);
            headingLabel.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            headingLabel.setPaddingTop(4);
            headingLabel.setPaddingLeft(4);
            headingLabel.setPaddingRight(4);
            headingLabel.setText("Heading");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder headingLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            headingLabelConstraints.setPosition("north");
            headingLabel.setConstraints(headingLabelConstraints);
            this.addChild(headingLabel);
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
            northPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            northPanel.setName("northPanel");
            northPanel.__internal_setGenerationDpi(96);
            northPanel.registerTranslationHandler(translationHandler);
            northPanel.setScaleForResolution(true);
            northPanel.setMinimumWidth(10);
            northPanel.setMinimumHeight(10);
            northPanel.setBorderWidth(4);
            northPanel.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            de.docware.framework.modules.gui.layout.LayoutGridBag northPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            northPanelLayout.setCentered(false);
            northPanel.setLayout(northPanelLayout);
            revisionStateLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            revisionStateLabel.setName("revisionStateLabel");
            revisionStateLabel.__internal_setGenerationDpi(96);
            revisionStateLabel.registerTranslationHandler(translationHandler);
            revisionStateLabel.setScaleForResolution(true);
            revisionStateLabel.setMinimumWidth(10);
            revisionStateLabel.setMinimumHeight(10);
            revisionStateLabel.setText("!!Änderungsstand:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag revisionStateLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 4);
            revisionStateLabel.setConstraints(revisionStateLabelConstraints);
            northPanel.addChild(revisionStateLabel);
            lastRevisionStateButton = new de.docware.framework.modules.gui.controls.GuiButton();
            lastRevisionStateButton.setName("lastRevisionStateButton");
            lastRevisionStateButton.__internal_setGenerationDpi(96);
            lastRevisionStateButton.registerTranslationHandler(translationHandler);
            lastRevisionStateButton.setScaleForResolution(true);
            lastRevisionStateButton.setMinimumWidth(100);
            lastRevisionStateButton.setMinimumHeight(10);
            lastRevisionStateButton.setMnemonicEnabled(true);
            lastRevisionStateButton.setText("!!Vorheriger");
            lastRevisionStateButton.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgPlugin_iParts_MoveLeft"));
            lastRevisionStateButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    showLastRevisionState(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag lastRevisionStateButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 4);
            lastRevisionStateButton.setConstraints(lastRevisionStateButtonConstraints);
            northPanel.addChild(lastRevisionStateButton);
            revisionStateComboBox = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            revisionStateComboBox.setName("revisionStateComboBox");
            revisionStateComboBox.__internal_setGenerationDpi(96);
            revisionStateComboBox.registerTranslationHandler(translationHandler);
            revisionStateComboBox.setScaleForResolution(true);
            revisionStateComboBox.setMinimumWidth(50);
            revisionStateComboBox.setMinimumHeight(10);
            revisionStateComboBox.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    revisionStateSelected(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag revisionStateComboBoxConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 4);
            revisionStateComboBox.setConstraints(revisionStateComboBoxConstraints);
            northPanel.addChild(revisionStateComboBox);
            nextRevisionStateButton = new de.docware.framework.modules.gui.controls.GuiButton();
            nextRevisionStateButton.setName("nextRevisionStateButton");
            nextRevisionStateButton.__internal_setGenerationDpi(96);
            nextRevisionStateButton.registerTranslationHandler(translationHandler);
            nextRevisionStateButton.setScaleForResolution(true);
            nextRevisionStateButton.setMinimumWidth(100);
            nextRevisionStateButton.setMinimumHeight(10);
            nextRevisionStateButton.setMnemonicEnabled(true);
            nextRevisionStateButton.setText("!!Nächster");
            nextRevisionStateButton.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgPlugin_iParts_MoveRight"));
            nextRevisionStateButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    showNextRevisionState(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag nextRevisionStateButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 4);
            nextRevisionStateButton.setConstraints(nextRevisionStateButtonConstraints);
            northPanel.addChild(nextRevisionStateButton);
            labelDate = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDate.setName("labelDate");
            labelDate.__internal_setGenerationDpi(96);
            labelDate.registerTranslationHandler(translationHandler);
            labelDate.setScaleForResolution(true);
            labelDate.setMinimumWidth(10);
            labelDate.setMinimumHeight(10);
            labelDate.setText("!!Stichtag:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelDateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(4, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 4, 0, 0);
            labelDate.setConstraints(labelDateConstraints);
            northPanel.addChild(labelDate);
            labelDateValue = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDateValue.setName("labelDateValue");
            labelDateValue.__internal_setGenerationDpi(96);
            labelDateValue.registerTranslationHandler(translationHandler);
            labelDateValue.setScaleForResolution(true);
            labelDateValue.setMinimumWidth(10);
            labelDateValue.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelDateValueConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(5, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 0);
            labelDateValue.setConstraints(labelDateValueConstraints);
            northPanel.addChild(labelDateValue);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder northPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            northPanelConstraints.setPosition("north");
            northPanel.setConstraints(northPanelConstraints);
            mainPanel.addChild(northPanel);
            assemblyListPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            assemblyListPanel.setName("assemblyListPanel");
            assemblyListPanel.__internal_setGenerationDpi(96);
            assemblyListPanel.registerTranslationHandler(translationHandler);
            assemblyListPanel.setScaleForResolution(true);
            assemblyListPanel.setMinimumWidth(10);
            assemblyListPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder assemblyListPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            assemblyListPanel.setLayout(assemblyListPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder assemblyListPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            assemblyListPanel.setConstraints(assemblyListPanelConstraints);
            mainPanel.addChild(assemblyListPanel);
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
                    closeWindow(event);
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
//</editor-fold>
}