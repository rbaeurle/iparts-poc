/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;


import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.events.GotoPartWithPartialPathEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.GotoEditPartWithPartialPathEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.DataObjectFilterGridWithStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.HTMLUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

public abstract class AbstractRelatedInfoReplacementDataForm extends AbstractRelatedInfoPartlistDataForm implements iPartsSuperEditRelatedInfoInterface {

    public static final String INCLUDE_IPARTSMENU_TEXT = "!!Mitlieferteile anzeigen...";

    protected enum REPLACEMENT_GRID_TYPE {PREDECESSOR, CURRENT, SUCCESSOR}

    // Vorgänger
    protected AbstractReplacementDataObjectGrid predecessorDataGrid;
    // Aktuelle Daten (nur eine Zeile)
    protected AbstractReplacementDataObjectGrid currentDataGrid;
    // Nachfolger
    protected AbstractReplacementDataObjectGrid successorDataGrid;

    // Aktuell geladener Stücklisteneintrag
    protected PartListEntryId startPartListEntryId;
    protected EtkDataPartListEntry partListEntry;


    protected abstract class AbstractReplacementDataObjectGrid extends DataObjectFilterGridWithStatus {

        protected GuiMenuItem gotoMenuItem;
        protected GuiMenuItem changeMenuItem;
        protected GuiMenuItem includePartsMenuItem;
        protected GuiMenuItem factoryMenuItem;

        public AbstractReplacementDataObjectGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractRelatedInfoPartlistDataForm parentForm,
                                                 String statusTableName, String statusFieldName) {
            super(dataConnector, parentForm, false, statusTableName, statusFieldName);
        }

        @Override
        public RelatedInfoBaseFormIConnector getConnector() {
            return (RelatedInfoBaseFormIConnector)super.getConnector();
        }

        protected void setGridType(REPLACEMENT_GRID_TYPE gridType) {
            GuiContextMenu contextMenu = getContextMenu();

            // Popup-Menüeintrag für "Wechsle zu"
            if ((gridType != REPLACEMENT_GRID_TYPE.CURRENT) && !isEditContext(getConnector(), true)) {
                changeMenuItem = new GuiMenuItem();
                changeMenuItem.setText((gridType == REPLACEMENT_GRID_TYPE.PREDECESSOR) ?
                                       "!!Wechsle zum Vorgänger" :
                                       "!!Wechsle zum Nachfolger");
                changeMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        changeToSelectedPartListEntry();
                    }
                });
                contextMenu.addChild(changeMenuItem);
            }

            // Popup-Menüeintrag für "Gehe zu"
            gotoMenuItem = new GuiMenuItem();
            String text = "";
            switch (gridType) {
                case PREDECESSOR:
                    text = "!!Gehe zum Vorgänger";
                    break;
                case CURRENT:
                    text = "!!Gehe zur Stückliste";
                    break;
                case SUCCESSOR:
                    text = "!!Gehe zum Nachfolger";
                    break;
            }
            gotoMenuItem.setText(text);
            gotoMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    gotoSelectedPartListEntry();
                }
            });
            contextMenu.addChild(gotoMenuItem);

            GuiSeparator separator = new GuiSeparator();
            separator.setName("menuSeparator");
            contextMenu.addChild(separator);

            // Popup-Menüeintrag für "Mitlieferteile anzeigen..."
            if (gridType != REPLACEMENT_GRID_TYPE.CURRENT) {
                includePartsMenuItem = new GuiMenuItem();
                includePartsMenuItem.setText(INCLUDE_IPARTSMENU_TEXT);
                includePartsMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        showIncludeParts();
                    }
                });
                contextMenu.addChild(includePartsMenuItem);
                includePartsMenuItem.setEnabled(false);

                separator = new GuiSeparator();
                separator.setName("menuSeparator2");
                contextMenu.addChild(separator);
            }

            // Popup-Menüeintrag für "Werkseinsatzdaten anzeigen..."
            factoryMenuItem = new GuiMenuItem();
            factoryMenuItem.setText("!!Werkseinsatzdaten anzeigen...");
            factoryMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    showFactoryData();
                }
            });
            contextMenu.addChild(factoryMenuItem);

            // Jetzt den Popup-Menüeintrag für "Kopieren" hinzufügen, damit dieser ganz unten erscheint
            addCopyContextMenuItem();
        }

        protected void changeToSelectedPartListEntry() {
            iPartsDataPartListEntry selectedPartListEntry = getSelectedPartListEntry();
            if (selectedPartListEntry != null) {
                partListEntry = selectedPartListEntry;

                dataToGrid();
            }
        }

        protected iPartsDataPartListEntry getSelectedPartListEntry() {
            List<EtkDataObject> selectionList = getSelection();
            if ((selectionList != null) && !selectionList.isEmpty()) {
                for (EtkDataObject dataObject : selectionList) {
                    if (dataObject instanceof iPartsDataPartListEntry) {
                        return (iPartsDataPartListEntry)dataObject;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onCellDblClicked(int row, int col, Event event) {
            gotoSelectedPartListEntry();
        }

        private void gotoSelectedPartListEntry() {
            EtkDataPartListEntry selectedPartListEntry = getSelectedPartListEntry();
            if (selectedPartListEntry != null) {
                NavigationPath path = this.getConnector().getRelatedInfoData().getNavigationPath();
                if (path.isEmpty()) {
                    path = new NavigationPath(); // Neuen Pfad erzeugen, damit der Pfad aus der RelatedInfoData nicht verändert wird
                    path.addAssembly(selectedPartListEntry.getOwnerAssemblyId());
                }
                if (!closeRelatedInfoFormIfNotEmbedded()) { // Erst schließen, dann springen
                    return;
                }

                if (getConnector().getActiveForm() instanceof EditModuleForm) {
                    GotoEditPartWithPartialPathEvent partWithPartialPathEvent = new GotoEditPartWithPartialPathEvent(path,
                                                                                                                     selectedPartListEntry.getOwnerAssemblyId(),
                                                                                                                     selectedPartListEntry.getAsId().getKLfdnr(),
                                                                                                                     false, false, this);
                    getProject().fireProjectEvent(partWithPartialPathEvent);
                } else {
                    GotoPartWithPartialPathEvent partWithPartialPathEvent = new GotoPartWithPartialPathEvent(path,
                                                                                                             selectedPartListEntry.getOwnerAssemblyId(),
                                                                                                             selectedPartListEntry.getAsId().getKLfdnr(),
                                                                                                             false, false, this);
                    getProject().fireProjectEvent(partWithPartialPathEvent);
                }
            }
        }

        private void showFactoryData() {
            EtkDataPartListEntry plEntry = getSelectedPartListEntry();
            if (plEntry != null) {
                // Neuen RelatedInfoFormConnector erzeugen, um den aktuellen nicht zu verändern
                RelatedInfoFormConnector connector = new RelatedInfoFormConnector(getConnector());

                // Den aktuell selektierten PartListEntry als Quelle für die Werkseinsatzdaten setzen
                connector.getRelatedInfoData().setKatInfosForPartList(plEntry, plEntry.getOwnerAssemblyId());

                iPartsRelatedInfoFactoryData factoryData = new iPartsRelatedInfoFactoryData();
                factoryData.setScaleFromParent(true);
                iPartsRelatedInfoFactoryDataForm form = (iPartsRelatedInfoFactoryDataForm)factoryData.newDisplayFormInstance(connector, this);

                connector.setActiveRelatedSubForm(form);
                form.updateView();
                String dbLanguage = getProject().getDBLanguage();
                String partName = plEntry.getPart().getFieldValue(EtkDbConst.FIELD_M_TEXTNR, dbLanguage, true);
                form.setWindowTitle(TranslationHandler.translate("!!Werkseinsatzdaten zu %1", partName),
                                    TranslationHandler.translate("!!Teilenummer: %1", plEntry.getPart().getDisplayValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR,
                                                                                                                        dbLanguage)));
                form.addOwnConnector(connector);
                form.showModal();
            }
        }

        protected abstract void showIncludeParts();

        @Override
        public <E extends EtkDataObject> List<E> getDataObjectList(Class<E> dataObjectClass) {
            // Ergebnisliste filtern, so dass die vererbten Ersetzungen nicht enthalten sind
            List<E> resultList = new DwList<>();
            for (E element : super.getDataObjectList(dataObjectClass)) {
                if (element instanceof iPartsDataReplacePart) {
                    iPartsDataReplacePart replacePart = (iPartsDataReplacePart)element;
                    if (!replacePart.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DRP_INHERITED)) {
                        resultList.add(element);
                    }
                } else {
                    resultList.add(element);
                }
            }
            return resultList;
        }
    }


    public AbstractRelatedInfoReplacementDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                  IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
    }

    protected abstract void dataToGrid();

    protected void postCreateGui() {
        addDataGrids(mainWindow.panelPredecessorData, mainWindow.panelCurrentData, mainWindow.panelSuccessorData);
    }

    /**
     * Für einen eventuell zukünftigen non-modalen Aufruf wird hier der aktuelle Stücklisteneintrag gesetzt.
     *
     * @param sender
     * @param forceUpdateAll
     */
    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        PartListEntryId currentPartListEntryId = getConnector().getRelatedInfoData().getAsPartListEntryId();
        // Überprüfung, welcher Stücklisteneintrag nun der aktuelle ist und ob die Daten aktualisiert werden müssen.
        if (forceUpdateAll || ((getConnector().getActiveRelatedSubForm() == this) && !Utils.objectEquals(currentPartListEntryId, startPartListEntryId))) {
            // Die ID des Stücklisteneintrags merken.
            startPartListEntryId = currentPartListEntryId;
            partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());

            dataToGrid();
        }
    }

    protected abstract void addDataGrids(GuiPanel panelPredecessorData, GuiPanel panelCurrentData, GuiPanel panelSuccessorData);

    protected void clearGrids() {
        predecessorDataGrid.clearGrid();
        currentDataGrid.clearGrid();
        successorDataGrid.clearGrid();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    private void closeWindow(Event event) {
        close();
        mainWindow.setVisible(false);
    }

    @Override
    public int calculateOptimalHeight() {
        int scrollBarWidth = FrameworkUtils.getSystemScrollbarWidth(false);
        int titleAndBorderHeight = HTMLUtils.getTextDimension(mainWindow.panelPredecessorData.getFont(), "Wg").getHeight() + 10;
        int optimalHeight = predecessorDataGrid.getTable().getPreferredHeight() + scrollBarWidth + titleAndBorderHeight
                            + ((predecessorDataGrid.getTable().getRowCount() == 0) ? titleAndBorderHeight : 0);
        mainWindow.splitPaneMain.setResizeWeight(0.0d);
        mainWindow.splitPaneMain.setDividerPosition(optimalHeight);
        int currentDataGridHeight = currentDataGrid.getTable().getPreferredHeight() + scrollBarWidth + titleAndBorderHeight;
        optimalHeight += currentDataGridHeight;
        mainWindow.splitPaneInner.setResizeWeight(0.0d);
        mainWindow.splitPaneInner.setDividerPosition(currentDataGridHeight);
        optimalHeight += successorDataGrid.getToolBar().getPreferredHeight() + 6 + successorDataGrid.getTable().getPreferredHeight()
                         + scrollBarWidth + titleAndBorderHeight + ((successorDataGrid.getTable().getRowCount() == 0) ? titleAndBorderHeight : 0);
        return optimalHeight;
    }


// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
//     FRAMEWORK GENERATED CODE
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------


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
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitPaneMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitPanePanelPredecessorData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelPredecessorData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel KWD109087;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPredecessorData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelInnerSplitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitPaneInner;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitPanePanelCurrentData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCurrentData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel QKB109087;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelCurrentData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitPanePanelSuccessorData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelSuccessorData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelSuccessorData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel QSJ109088;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("!!Ersetzungen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitPaneMain = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitPaneMain.setName("splitPaneMain");
            splitPaneMain.__internal_setGenerationDpi(96);
            splitPaneMain.registerTranslationHandler(translationHandler);
            splitPaneMain.setScaleForResolution(true);
            splitPaneMain.setMinimumWidth(10);
            splitPaneMain.setMinimumHeight(10);
            splitPaneMain.setHorizontal(false);
            splitPaneMain.setDividerPosition(191);
            splitPanePanelPredecessorData = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitPanePanelPredecessorData.setName("splitPanePanelPredecessorData");
            splitPanePanelPredecessorData.__internal_setGenerationDpi(96);
            splitPanePanelPredecessorData.registerTranslationHandler(translationHandler);
            splitPanePanelPredecessorData.setScaleForResolution(true);
            splitPanePanelPredecessorData.setMinimumWidth(0);
            splitPanePanelPredecessorData.setMinimumHeight(50);
            splitPanePanelPredecessorData.setTitle("!!Vorgänger");
            de.docware.framework.modules.gui.layout.LayoutBorder splitPanePanelPredecessorDataLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitPanePanelPredecessorData.setLayout(splitPanePanelPredecessorDataLayout);
            labelPredecessorData = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelPredecessorData.setName("labelPredecessorData");
            labelPredecessorData.__internal_setGenerationDpi(96);
            labelPredecessorData.registerTranslationHandler(translationHandler);
            labelPredecessorData.setScaleForResolution(true);
            labelPredecessorData.setMinimumWidth(10);
            labelPredecessorData.setMinimumHeight(10);
            labelPredecessorData.setBorderWidth(8);
            KWD109087 = new de.docware.framework.modules.gui.controls.GuiLabel();
            KWD109087.setName("KWD109087");
            KWD109087.__internal_setGenerationDpi(96);
            KWD109087.registerTranslationHandler(translationHandler);
            KWD109087.setScaleForResolution(true);
            KWD109087.setText("!!Vorgänger");
            labelPredecessorData.setTooltip(KWD109087);
            labelPredecessorData.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgPlugin_iPartsEdit_Predecessor"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelPredecessorDataConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelPredecessorDataConstraints.setPosition("west");
            labelPredecessorData.setConstraints(labelPredecessorDataConstraints);
            splitPanePanelPredecessorData.addChild(labelPredecessorData);
            panelPredecessorData = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPredecessorData.setName("panelPredecessorData");
            panelPredecessorData.__internal_setGenerationDpi(96);
            panelPredecessorData.registerTranslationHandler(translationHandler);
            panelPredecessorData.setScaleForResolution(true);
            panelPredecessorData.setMinimumWidth(10);
            panelPredecessorData.setMinimumHeight(10);
            panelPredecessorData.setPaddingTop(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelPredecessorDataLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelPredecessorData.setLayout(panelPredecessorDataLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelPredecessorDataConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelPredecessorData.setConstraints(panelPredecessorDataConstraints);
            splitPanePanelPredecessorData.addChild(panelPredecessorData);
            splitPaneMain.addChild(splitPanePanelPredecessorData);
            panelInnerSplitPane = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelInnerSplitPane.setName("panelInnerSplitPane");
            panelInnerSplitPane.__internal_setGenerationDpi(96);
            panelInnerSplitPane.registerTranslationHandler(translationHandler);
            panelInnerSplitPane.setScaleForResolution(true);
            panelInnerSplitPane.setMinimumWidth(0);
            panelInnerSplitPane.setMinimumHeight(120);
            de.docware.framework.modules.gui.layout.LayoutBorder panelInnerSplitPaneLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelInnerSplitPane.setLayout(panelInnerSplitPaneLayout);
            splitPaneInner = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitPaneInner.setName("splitPaneInner");
            splitPaneInner.__internal_setGenerationDpi(96);
            splitPaneInner.registerTranslationHandler(translationHandler);
            splitPaneInner.setScaleForResolution(true);
            splitPaneInner.setMinimumWidth(10);
            splitPaneInner.setMinimumHeight(10);
            splitPaneInner.setHorizontal(false);
            splitPaneInner.setDividerPosition(100);
            splitPanePanelCurrentData = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitPanePanelCurrentData.setName("splitPanePanelCurrentData");
            splitPanePanelCurrentData.__internal_setGenerationDpi(96);
            splitPanePanelCurrentData.registerTranslationHandler(translationHandler);
            splitPanePanelCurrentData.setScaleForResolution(true);
            splitPanePanelCurrentData.setMinimumWidth(0);
            splitPanePanelCurrentData.setMinimumHeight(70);
            splitPanePanelCurrentData.setTitle("!!Aktuelles Teil");
            de.docware.framework.modules.gui.layout.LayoutBorder splitPanePanelCurrentDataLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitPanePanelCurrentData.setLayout(splitPanePanelCurrentDataLayout);
            labelCurrentData = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCurrentData.setName("labelCurrentData");
            labelCurrentData.__internal_setGenerationDpi(96);
            labelCurrentData.registerTranslationHandler(translationHandler);
            labelCurrentData.setScaleForResolution(true);
            labelCurrentData.setMinimumWidth(10);
            labelCurrentData.setMinimumHeight(10);
            labelCurrentData.setBorderWidth(8);
            QKB109087 = new de.docware.framework.modules.gui.controls.GuiLabel();
            QKB109087.setName("QKB109087");
            QKB109087.__internal_setGenerationDpi(96);
            QKB109087.registerTranslationHandler(translationHandler);
            QKB109087.setScaleForResolution(true);
            QKB109087.setText("!!Aktuelles Teil");
            labelCurrentData.setTooltip(QKB109087);
            labelCurrentData.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgApp_javaviewer_Part"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelCurrentDataConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelCurrentDataConstraints.setPosition("west");
            labelCurrentData.setConstraints(labelCurrentDataConstraints);
            splitPanePanelCurrentData.addChild(labelCurrentData);
            panelCurrentData = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelCurrentData.setName("panelCurrentData");
            panelCurrentData.__internal_setGenerationDpi(96);
            panelCurrentData.registerTranslationHandler(translationHandler);
            panelCurrentData.setScaleForResolution(true);
            panelCurrentData.setMinimumWidth(10);
            panelCurrentData.setMinimumHeight(10);
            panelCurrentData.setPaddingTop(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelCurrentDataLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelCurrentData.setLayout(panelCurrentDataLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelCurrentDataConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelCurrentData.setConstraints(panelCurrentDataConstraints);
            splitPanePanelCurrentData.addChild(panelCurrentData);
            splitPaneInner.addChild(splitPanePanelCurrentData);
            splitPanePanelSuccessorData = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitPanePanelSuccessorData.setName("splitPanePanelSuccessorData");
            splitPanePanelSuccessorData.__internal_setGenerationDpi(96);
            splitPanePanelSuccessorData.registerTranslationHandler(translationHandler);
            splitPanePanelSuccessorData.setScaleForResolution(true);
            splitPanePanelSuccessorData.setMinimumWidth(0);
            splitPanePanelSuccessorData.setMinimumHeight(50);
            splitPanePanelSuccessorData.setTitle("!!Nachfolger");
            de.docware.framework.modules.gui.layout.LayoutBorder splitPanePanelSuccessorDataLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitPanePanelSuccessorData.setLayout(splitPanePanelSuccessorDataLayout);
            panelSuccessorData = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelSuccessorData.setName("panelSuccessorData");
            panelSuccessorData.__internal_setGenerationDpi(96);
            panelSuccessorData.registerTranslationHandler(translationHandler);
            panelSuccessorData.setScaleForResolution(true);
            panelSuccessorData.setMinimumWidth(10);
            panelSuccessorData.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelSuccessorDataLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelSuccessorData.setLayout(panelSuccessorDataLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelSuccessorDataConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelSuccessorData.setConstraints(panelSuccessorDataConstraints);
            splitPanePanelSuccessorData.addChild(panelSuccessorData);
            labelSuccessorData = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSuccessorData.setName("labelSuccessorData");
            labelSuccessorData.__internal_setGenerationDpi(96);
            labelSuccessorData.registerTranslationHandler(translationHandler);
            labelSuccessorData.setScaleForResolution(true);
            labelSuccessorData.setMinimumWidth(10);
            labelSuccessorData.setMinimumHeight(10);
            labelSuccessorData.setBorderWidth(8);
            QSJ109088 = new de.docware.framework.modules.gui.controls.GuiLabel();
            QSJ109088.setName("QSJ109088");
            QSJ109088.__internal_setGenerationDpi(96);
            QSJ109088.registerTranslationHandler(translationHandler);
            QSJ109088.setScaleForResolution(true);
            QSJ109088.setText("!!Nachfolger");
            labelSuccessorData.setTooltip(QSJ109088);
            labelSuccessorData.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgPlugin_iPartsEdit_Successor"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelSuccessorDataConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelSuccessorDataConstraints.setPosition("west");
            labelSuccessorData.setConstraints(labelSuccessorDataConstraints);
            splitPanePanelSuccessorData.addChild(labelSuccessorData);
            splitPaneInner.addChild(splitPanePanelSuccessorData);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitPaneInnerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitPaneInner.setConstraints(splitPaneInnerConstraints);
            panelInnerSplitPane.addChild(splitPaneInner);
            splitPaneMain.addChild(panelInnerSplitPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitPaneMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitPaneMain.setConstraints(splitPaneMainConstraints);
            panelMain.addChild(splitPaneMain);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
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
}
