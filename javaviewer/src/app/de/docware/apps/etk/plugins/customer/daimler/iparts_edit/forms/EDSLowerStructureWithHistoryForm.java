/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyEdsStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Dialog zum Anzeigen der Änderungsstände der SAA auf der Position
 */
public class EDSLowerStructureWithHistoryForm extends AbstractJavaViewerForm {

    private final String SUB_TITLE_SEPERATOR = "/";
    private static final String SHOW_REVISION_STATES_MENU_SEPARATOR_NAME = "iPartsMenuItemSeparatorShowScopeRevisionStates";
    private static final String SHOW_REVISION_STATES_MENU_NAME = "iPartsMenuItemShowScopeRevisionStates";
    private static final String SHOW_REVISION_STATES_MENU_TEXT = "!!Änderungsstände anzeigen";

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        // Separator vor Menüeintrag "Änderungsstände anzeigen..."
        GuiSeparator menuItemSeparator = new GuiSeparator();
        menuItemSeparator.setUserObject(SHOW_REVISION_STATES_MENU_SEPARATOR_NAME);
        popupMenu.addChild(menuItemSeparator);

        // Menüeintrag "Änderungsstände anzeigen..." hinzufügen
        GuiMenuItem menuItemShowRevisionStates = new GuiMenuItem();
        menuItemShowRevisionStates.setUserObject(SHOW_REVISION_STATES_MENU_NAME);
        menuItemShowRevisionStates.setName(SHOW_REVISION_STATES_MENU_NAME);
        menuItemShowRevisionStates.setText(SHOW_REVISION_STATES_MENU_TEXT);
        menuItemShowRevisionStates.setIcon(EditDefaultImages.edit_history.getImage());
        menuItemShowRevisionStates.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
                if ((selectedPartListEntries != null) && !selectedPartListEntries.isEmpty() && (selectedPartListEntries.size() == 1)) {
                    // Nur eine Teilepos kann selektiert sein, siehe updatePartListPopupMenu
                    EtkDataPartListEntry selectedPartListEntry = selectedPartListEntries.get(0);
                    showEDSScopeWithHistoryForm(connector, selectedPartListEntry);
                }
            }
        });
        popupMenu.addChild(menuItemShowRevisionStates);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste EDS Struktur und der untere Strukturknoten?
        boolean isValidAssemblyForLowerStructureShowRevisionStates = false;
        List<EtkDataPartListEntry> selectedEntries = connector.getSelectedPartListEntries();
        if ((selectedEntries != null) && (selectedEntries.size() == 1)) {
            EtkDataPartListEntry entry = selectedEntries.get(0);
            // Texte können keine Änderungsstände haben
            if (!VirtualMaterialType.isPartListTextEntry(entry)) {
                EtkDataAssembly assembly = connector.getCurrentAssembly();
                if (iPartsVirtualNode.isVirtualId(assembly.getAsId()) && (assembly instanceof iPartsDataAssembly)) {
                    isValidAssemblyForLowerStructureShowRevisionStates = iPartsEdsStructureHelper.isEdsStructureLowerElementNode(((iPartsDataAssembly)assembly).getVirtualNodesPath());
                }
            }
        }

        // Separator und Menüeintrag "Änderungsstände anzeigen" aktualisieren
        for (AbstractGuiControl child : popupMenu.getChildren()) {
            if (child.getUserObject() != null) {
                if (child.getUserObject().equals(SHOW_REVISION_STATES_MENU_SEPARATOR_NAME)) {
                    child.setVisible(isValidAssemblyForLowerStructureShowRevisionStates);
                } else if (child.getUserObject().equals(SHOW_REVISION_STATES_MENU_NAME)) {
                    child.setVisible(isValidAssemblyForLowerStructureShowRevisionStates);
                }
            }
        }
    }

    public static void showEDSScopeWithHistoryForm(AbstractJavaViewerFormIConnector dataConnector, EtkDataPartListEntry selectedPartListEntry) {
        EDSLowerStructureWithHistoryForm edsLowerStructureWithHistoryForm = new EDSLowerStructureWithHistoryForm(dataConnector, dataConnector.getActiveForm(),
                                                                                                                 selectedPartListEntry);
        edsLowerStructureWithHistoryForm.showModal();
    }

    private EdsStructureContainer edsStructureContainer;
    private DataObjectFilterGrid dataObjectGrid;

    public EDSLowerStructureWithHistoryForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                            EtkDataPartListEntry selectedPartListEntry) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        edsStructureContainer = new EdsStructureContainer(selectedPartListEntry);
        postCreateGui();

        setTitle(edsStructureContainer);
        fillGrid(edsStructureContainer);
        mainWindow.setSize(1400, 600);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        //DataObjectGrid mit den Änderungsständen
        dataObjectGrid = createGrid();
        mainWindow.historyPanel.addChildBorderCenter(dataObjectGrid.getGui());
        dataObjectGrid.setNoResultsLabelText("!!Keine Änderungsstände vorhanden");
        dataObjectGrid.setDisplayFields(getDisplayFields());
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.mainPanel;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    protected void setTitle(EdsStructureContainer edsStructureContainer) {
        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
        StringBuilder str = new StringBuilder();
        if (edsStructureContainer.isInit()) {
            str.append(edsStructureContainer.getModelNo());
            str.append(SUB_TITLE_SEPERATOR);
            str.append(structureHelper.getUpperValueFromStructureId(edsStructureContainer.getStructureId()));
            str.append(SUB_TITLE_SEPERATOR);
            str.append(structureHelper.getLowerValueFromStructureId(edsStructureContainer.getStructureId()));
            str.append("; POS: ");
            str.append(edsStructureContainer.getPos());
        } else {
            str.append(TranslationHandler.translate("!!<Ungültiges Baumuster>"));
        }
        setTitle(str.toString());
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        dispose();
        return modalResult;
    }

    protected void fillGrid(EdsStructureContainer edsStructureContainer) {
        dataObjectGrid.clearGrid();
        if (edsStructureContainer.isInit()) {
            iPartsVirtualAssemblyEdsStructure virtualAssemblyEdsOps = new iPartsVirtualAssemblyEdsStructure(getProject(),
                                                                                                            edsStructureContainer.getVirtualNodesPath(),
                                                                                                            edsStructureContainer.getDataAssmebly().getAsId());


            iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
            HierarchicalIDWithType structureId = edsStructureContainer.getStructureId();
            // Alle Datensätze mit der gleichen Modulnummer, oberen Struktur, unteren Struktur und POS laden -> das sind
            // die Änderungsstände an der POS
            EtkDataObjectList<? extends EtkDataObject> list = structureHelper.loadAllStructureEntriesForPosition(getProject(),
                                                                                                                 edsStructureContainer.getModelNo(),
                                                                                                                 structureId,
                                                                                                                 edsStructureContainer.getPos());
            if (!list.isEmpty()) {
                int lfdNr = 1;
                iPartsVirtualNode rootNode = edsStructureContainer.getVirtualNodesPath().get(0);
                String virtualStructurePrefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX
                                                + structureHelper.getVirtualFieldPrefix()
                                                + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER;
                List<VirtualFieldDefinition> virtualFieldDefinitions = iPartsDataVirtualFieldsDefinition.getMapping(structureHelper.getStructureTableName(),
                                                                                                                    iPartsConst.TABLE_KATALOG);
                for (EtkDataObject dataObject : list) {
                    if (isOldRecord(dataObject, structureHelper.getKemFromField())) {
                        continue;
                    }
                    EdsSaaId childSaaId = new EdsSaaId(dataObject.getFieldValue(structureHelper.getSubElementField()));
                    EtkDataPartListEntry entry = virtualAssemblyEdsOps.createEdsSaaNode(lfdNr, rootNode, structureId, childSaaId);
                    lfdNr++;

                    // Bei loadVirtualField in iPartsDataPartListEntry wird immer nur der Datensatz pro SAA mit der höchsten Revision genommen
                    // Deswegen hier schon die virtuellen Felder mit den Daten aus den verschiedenen Änderungsständen füllen
                    for (VirtualFieldDefinition virtualFieldDefinition : virtualFieldDefinitions) {
                        if (virtualFieldDefinition.getVirtualFieldName().startsWith(virtualStructurePrefix)) {
                            String virtualFieldValue = dataObject.getFieldValue(virtualFieldDefinition.getSourceFieldName());
                            entry.getAttributes().addField(virtualFieldDefinition.getVirtualFieldName(), virtualFieldValue, true, DBActionOrigin.FROM_DB);
                        }
                    }
                    dataObjectGrid.addObjectToGrid(entry);
                }
            }
        }
        dataObjectGrid.showNoResultsLabel(dataObjectGrid.getTable().getRowCount() == 0);
    }

    private boolean isOldRecord(EtkDataObject edsModelContent, String kemFromField) {
        String kemFrom = edsModelContent.getFieldValue(kemFromField);
        return StrUtils.isEmpty(kemFrom);
//        String kemTo = edsModelContent.getFieldValue(iPartsConst.FIELD_EDS_KEMTO);
//        String releaseFrom = edsModelContent.getFieldValue(iPartsConst.FIELD_EDS_RELEASE_FROM);
//        String releasTo = edsModelContent.getFieldValue(iPartsConst.FIELD_EDS_RELEASE_TO);
//        return StrUtils.isEmpty(kemFrom, kemTo, releaseFrom, releasTo);
    }

    protected EtkDisplayFields getDisplayFields() {
        EtkDisplayFields defaultDisplayFields = createDefaultDisplayFields();
        defaultDisplayFields.loadStandards(getConfig());
        return defaultDisplayFields;
    }

    protected EtkDisplayFields createDefaultDisplayFields() {
        EtkDisplayFields defaultDisplayFields = new EtkDisplayFields();
        List<EtkDisplayField> displayFields = null;
        if (edsStructureContainer.isInit()) {
            displayFields = edsStructureContainer.getDataAssmebly().getEbene().getClonedFields();
        }

        if (displayFields != null) {
            displayFields.forEach(defaultDisplayFields::addFeld);
        } else {
            if (defaultDisplayFields.size() == 0) {
                EtkDisplayField etkDisplayField = new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_BESTNR, false, false);
                defaultDisplayFields.addFeld(etkDisplayField);
                etkDisplayField = new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR, true, false);
                defaultDisplayFields.addFeld(etkDisplayField);
                iPartsEdsStructureHelper.getInstance().addDefaultDisplayFieldsForEdsLowerStructure(defaultDisplayFields);
            }
        }

        return defaultDisplayFields;
    }

    private DataObjectFilterGrid createGrid() {
        return new DataObjectFilterGrid(getConnector(), this) {
            private GuiMenuItem codeMenuItem;

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
                    codeMenuItem = new GuiMenuItem();
                    codeMenuItem.setText("!!Code-Stammdaten anzeigen");
                    codeMenuItem.setIcon(EditDefaultImages.edit_code.getImage());
                    codeMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                        @Override
                        public void fire(Event event) {
                            List<EtkDataObject> list = getSelection();
                            if ((list != null) && !list.isEmpty() && list.size() == 1) {
                                EtkDataObject dataObject = list.get(0);
                                if (dataObject instanceof iPartsDataPartListEntry) {
                                    iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)dataObject;
                                    iPartsCodeMatrixDialog codeMatrixDialog = new iPartsCodeMatrixDialog(getConnector(),
                                                                                                         EDSLowerStructureWithHistoryForm.this, null);
                                    codeMatrixDialog.showCodeDNF(false);
                                    iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
                                    String productGroup = partListEntry.getFieldValue(structureHelper.getVirtualProductGroupSignField());
                                    String compareDate = partListEntry.getFieldValue(structureHelper.getVirtualReleaseFromField());
                                    String codeString = partListEntry.getFieldValue(structureHelper.getVirtualCodeField());
                                    // Ist kein gültiges Datum gesetzt, soll das aktuelle Datum verwendet werden
                                    if (!DateUtils.isValidDateTime_yyyyMMddHHmmss(compareDate)) {
                                        compareDate = DateUtils.toyyyyMMddHHmmss_Calendar(Calendar.getInstance());
                                    }
                                    codeMatrixDialog.showCodeMasterDataEDS(codeString, productGroup, compareDate);
                                    codeMatrixDialog.showModal();
                                }
                            }
                        }
                    });
                    contextMenu.addChild(codeMenuItem);
                }
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                if (codeMenuItem != null) {
                    codeMenuItem.setEnabled(isSingleSelected());
                }
            }
            // hier können ggf ncch weitere Mehtoden überschrieben werden
        };
    }

    private class EdsStructureContainer {

        private iPartsDataAssembly dataAssembly;
        private EtkDataPartListEntry selectedPartListEntry;
        private List<iPartsVirtualNode> virtualNodesPath;
        private String modelNo;
        private HierarchicalIDWithType structureId;
        private String pos;

        public EdsStructureContainer(EtkDataPartListEntry selectedPartListEntry) {
            this.selectedPartListEntry = selectedPartListEntry;
            if (selectedPartListEntry != null) {
                EtkDataAssembly assembly = selectedPartListEntry.getOwnerAssembly();
                if (assembly instanceof iPartsDataAssembly) {
                    dataAssembly = (iPartsDataAssembly)assembly;
                    iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
                    pos = selectedPartListEntry.getFieldValue(structureHelper.getVirtualPosField());
                    virtualNodesPath = dataAssembly.getVirtualNodesPath();
                    if (virtualNodesPath != null) {
                        modelNo = iPartsVirtualNode.getModelNumberFromAssemblyId(dataAssembly.getAsId());
                        structureId = structureHelper.createStructureIdFromOwnerAssemblyId(dataAssembly.getAsId());
                        if (structureId == null) {
                            structureId = structureHelper.createEmptyStructureId();
                        }
                    } else {
                        dataAssembly = null;
                    }
                }
            }
        }

        public boolean isInit() {
            return dataAssembly != null;
        }

        public iPartsDataAssembly getDataAssmebly() {
            return dataAssembly;
        }

        public EtkDataPartListEntry getSelectedPartListEntry() {
            return selectedPartListEntry;
        }

        public List<iPartsVirtualNode> getVirtualNodesPath() {
            return virtualNodesPath;
        }

        public String getModelNo() {
            return modelNo;
        }

        public HierarchicalIDWithType getStructureId() {
            return structureId;
        }

        public String getPos() {
            return pos;
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
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel historyPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setTitle("!!Änderungsstände der SAA auf der Position");
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
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
            historyPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            historyPanel.setName("historyPanel");
            historyPanel.__internal_setGenerationDpi(96);
            historyPanel.registerTranslationHandler(translationHandler);
            historyPanel.setScaleForResolution(true);
            historyPanel.setMinimumWidth(10);
            historyPanel.setMinimumHeight(10);
            historyPanel.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder historyPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            historyPanel.setLayout(historyPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder historyPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            historyPanel.setConstraints(historyPanelConstraints);
            mainPanel.addChild(historyPanel);
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
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}
