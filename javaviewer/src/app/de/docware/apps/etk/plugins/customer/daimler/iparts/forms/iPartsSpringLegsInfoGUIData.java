/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSpringMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsSpringMapping;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiSplitPane;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.color.FrameworkConstantColor;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Klasse mit allen Daten für die Darstellung der Federbeine samt verknüpfter Federn
 */
public class iPartsSpringLegsInfoGUIData {

    private static final boolean USE_DOUBLE_GRID = true;

    private GuiSplitPane springLegsInfoSplitpane;
    private GuiPanel springLegsInfoTop;
    private GuiPanel springLegsInfoBottom;
    private DataObjectFilterGrid springMappingGridTop;
    private DataObjectFilterGrid springMappingGridBottom;
    private InitDataForSpringGUI initDataForSpringGUI;

    public iPartsSpringLegsInfoGUIData(InitDataForSpringGUI initDataForSpringGUI) {
        if (initDataForSpringGUI != null) {
            this.initDataForSpringGUI = initDataForSpringGUI;
            initGUIData();
        }
    }

    /**
     * Initialisiert die GUI Daten für die Feder GUI (Grids und Textfeld)
     */
    private void initGUIData() {
        if (USE_DOUBLE_GRID) {
            this.springLegsInfoSplitpane = createInfoSplitPane();
            this.springLegsInfoTop = createInfoGridPanel("Top");
            // DataGrid erzeugen
            this.springMappingGridTop = createInfoGrid(springLegsInfoTop);
            this.springLegsInfoBottom = createInfoGridPanel("Bottom");
            // DataGrid erzeugen
            this.springMappingGridBottom = createInfoGrid(springLegsInfoBottom);
        } else {
            // DataGrid erzeugen
            this.springMappingGridTop = new DataObjectFilterGrid(getConnector(), getParentForm());
            initSingleGridView(springMappingGridTop, initDataForSpringGUI.gridParentPanel, initDataForSpringGUI.displayFields);
        }
        initSpringLegProperties(initDataForSpringGUI.textInputParentPanel, initDataForSpringGUI.activeSpringLegPartNumbers);
    }

    /**
     * Funktion, die zum übergebenen Federbein die Mapping-Daten ermittelt und im Grid darstellt.
     *
     * @param springLeg
     * @param dataGrid
     * @param panel
     */
    private void lookUpMappingData(String springLeg, boolean isValid, DataObjectGrid dataGrid, GuiPanel panel) {

        if (dataGrid == null) {
            dataGrid = springMappingGridTop;
        }
        if (panel == null) {
            panel = initDataForSpringGUI.gridParentPanel;
        }

        boolean mappingDataFound = false;
        dataGrid.clearGrid();
        if (!StrUtils.isEmpty(springLeg) && isValid) {
            // Zugriff auf den Cache
            iPartsSpringMapping springMappingCache = iPartsSpringMapping.getInstance(getProject());
            // Liste aus dem Cache holen
            iPartsDataSpringMapping springMapping = springMappingCache.getSpringMappingForSpringLeg(springLeg);

            // Mapping im Grid darstellen
            if (springMapping != null) {

                // Holen u.a. des mehrsprachigen Ergänzungstextes zur Feder.
                // DA_SPRING_MAPPING.DSM_SPRING == MAT.M_MATNR und davon dann M_TEXTNR
                String matNo = springMapping.getAttribute(iPartsConst.FIELD_DSM_SPRING).getAsString();
                PartId partID = new PartId(matNo, "");
                EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getProject(), partID);

                // Die ermittelten Daten aus DA_SPRING_MAPPING und MAT an die Zeile des Grids hängen
                dataGrid.addObjectToGrid(springMapping, dataPart);
                mappingDataFound = true;
            }
        }

        dataGrid.showNoResultsLabel(!mappingDataFound);
        String tempTitle;
        if (mappingDataFound) {
            springLeg = iPartsNumberHelper.formatPartNo(getProject(), springLeg);
            tempTitle = TranslationHandler.translate(initDataForSpringGUI.title) + TranslationHandler.translate("!!: %1", springLeg);
        } else {
            tempTitle = initDataForSpringGUI.title;
        }
        panel.setTitle(tempTitle);
    }

    /**
     * Ermittelt die Mapping Daten für die Darstellung im oberen Doppel-Grid
     */
    private void lookUpMappingDataForTopGrid(DataObjectFilterGrid springMappingGridTop, GuiPanel springLegsInfoTop) {
        if (!USE_DOUBLE_GRID) {
            lookUpMappingDataForSingleTopGrid();
        } else {
            lookUpMappingData(getSpringLegProperties().getTopTextFieldText(), getSpringLegProperties().isTopTextFieldValid(),
                              springMappingGridTop, springLegsInfoTop);
        }

    }

    /**
     * Ermittelt die Mapping Daten für die Darstellung im oberen Einzel-Grid
     */
    private void lookUpMappingDataForSingleTopGrid() {
        lookUpMappingData(getSpringLegProperties().getTopTextFieldText(), getSpringLegProperties().isTopTextFieldValid(), null, null);
    }

    /**
     * Ermittelt die Mapping Daten für die Darstellung im unteren Doppel-Grid
     */
    private void lookUpMappingDataForBottomGrid(DataObjectFilterGrid springMappingGridBottom, GuiPanel springLegsInfoBottom) {
        if (!USE_DOUBLE_GRID) {
            lookUpMappingDataForSingleBottomGrid();
        } else {
            lookUpMappingData(getSpringLegProperties().getBottomTextFieldText(), getSpringLegProperties().isBottomTextFieldValid(),
                              springMappingGridBottom, springLegsInfoBottom);
        }
    }

    /**
     * Ermittelt die Mapping Daten für die Darstellung im unteren Einzel-Grid
     */
    private void lookUpMappingDataForSingleBottomGrid() {
        lookUpMappingData(getSpringLegProperties().getBottomTextFieldText(), getSpringLegProperties().isBottomTextFieldValid(), null, null);
    }

    /**
     * Initialisiert die Darstellung der Feder-Textfelder
     *
     * @param parentPanel
     * @param springLegPartNumbers
     */
    public void initSpringLegProperties(GuiPanel parentPanel, List<String> springLegPartNumbers) {
        if (getSpringLegProperties() == null) {
            return;
        }
        parentPanel.addChild(getSpringLegProperties().getGui());
        getSpringLegProperties().setValues(springLegPartNumbers);

        // -------------------------------------------------------------------------------------------------------------
        // Event-Listener für das OBERE Eingabefeld
        // -------------------------------------------------------------------------------------------------------------
        getSpringLegProperties().addOnChangeListenerTopTextField(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                lookUpMappingDataForTopGrid(springMappingGridTop, springLegsInfoTop);
            }
        });
        getSpringLegProperties().addOnChangeListenerTopTextField(new EventListener(Event.ON_FOCUS_GAINED_EVENT) {
            @Override
            public void fire(Event event) {
                if (!USE_DOUBLE_GRID) {
                    lookUpMappingDataForSingleTopGrid();
                }
            }
        });

        // -------------------------------------------------------------------------------------------------------------
        // Event-Listener für das UNTERE Eingabefeld:
        // -------------------------------------------------------------------------------------------------------------
        getSpringLegProperties().addChangeListenerBottomTextField(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                lookUpMappingDataForBottomGrid(springMappingGridBottom, springLegsInfoBottom);
            }
        });
        getSpringLegProperties().addChangeListenerBottomTextField(new EventListener(Event.ON_FOCUS_GAINED_EVENT) {
            @Override
            public void fire(Event event) {
                if (!USE_DOUBLE_GRID) {
                    lookUpMappingDataForSingleBottomGrid();
                }
            }
        });
    }


    /**
     * Initialisiert die Daten für die Darstellung im Einzel-Grid
     *
     * @param springMappingGridTop
     * @param parentPanel
     * @param displayFields
     */
    private void initSingleGridView(DataObjectFilterGrid springMappingGridTop, GuiPanel parentPanel, EtkDisplayFields displayFields) {
        springMappingGridTop.setDisplayFields(displayFields);

        // DataGrid auf die rechte Hälfte der oberen SplitPane setzen.
        springMappingGridTop.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        parentPanel.addChild(springMappingGridTop.getGui());
    }

    /**
     * Erzeugt ein {@link DataObjectFilterGrid} und hängt es als Kind an das übergebene {@link GuiPanel}
     *
     * @param springLegsFrontInfo
     * @return
     */
    private DataObjectFilterGrid createInfoGrid(GuiPanel springLegsFrontInfo) {
        DataObjectFilterGrid infoGrid = new DataObjectFilterGrid(getConnector(), getParentForm());
        infoGrid.setDisplayFields(initDataForSpringGUI.displayFields);

        // DataGrid auf die rechte Hälfte der oberen SplitPane setzen.
        infoGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        springLegsFrontInfo.addChild(infoGrid.getGui());
        return infoGrid;
    }

    /**
     * Erzeugt das Panel für die Grids
     *
     * @param nameSuffix
     * @return
     */
    private GuiPanel createInfoGridPanel(String nameSuffix) {
        GuiPanel infoGridPanel = new GuiPanel();
        infoGridPanel.setName("splitpaneGridSpringLegs" + initDataForSpringGUI.namePrefix + nameSuffix);
        infoGridPanel.__internal_setGenerationDpi(96);
        infoGridPanel.registerTranslationHandler(TranslationHandler.getUiTranslationHandler());
        infoGridPanel.setScaleForResolution(true);
        infoGridPanel.setMinimumWidth(0);
        infoGridPanel.setBorderWidth(0);
        infoGridPanel.setBorderColor(new FrameworkConstantColor("clBtnShadow"));
        infoGridPanel.setTitle(initDataForSpringGUI.title);
        LayoutBorder splitpaneFrontChildFrontGridLayout = new LayoutBorder();
        infoGridPanel.setLayout(splitpaneFrontChildFrontGridLayout);
        springLegsInfoSplitpane.addChild(infoGridPanel);
        return infoGridPanel;
    }

    /**
     * Erzeugt das Splitpane in das die Textfelder und die Grids kommen
     *
     * @return
     */
    private GuiSplitPane createInfoSplitPane() {
        GuiSplitPane infoSplitPane = new GuiSplitPane();
        infoSplitPane.setName("splitpaneGridSpringLegsFront");
        infoSplitPane.__internal_setGenerationDpi(96);
        infoSplitPane.registerTranslationHandler(TranslationHandler.getUiTranslationHandler());
        infoSplitPane.setScaleForResolution(true);
        infoSplitPane.setMinimumWidth(10);
        infoSplitPane.setMinimumHeight(10);
        infoSplitPane.setDividerPosition(200);
        infoSplitPane.setResizeWeight(0.5);
        infoSplitPane.setHorizontal(false);
        ConstraintsBorder border = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        infoSplitPane.setConstraints(border);
        infoSplitPane.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, infoSplitPane) {
            @Override
            public void fireOnce(Event event) {
                int value = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                infoSplitPane.setDividerPosition(value / 2);
            }
        });
        initDataForSpringGUI.gridParentPanel.addChild(infoSplitPane);
        initDataForSpringGUI.gridParentPanel.setTitle("");
        return infoSplitPane;
    }

    /**
     * Initialisiert alle GUI Daten beim Öffnen des Dialogs
     */
    public void lookUpInitialData() {
        if (!USE_DOUBLE_GRID) {
            lookUpMappingDataForSingleTopGrid();
        } else {
            lookUpMappingDataForTopGrid(springMappingGridTop, springLegsInfoTop);
            lookUpMappingDataForBottomGrid(springMappingGridBottom, springLegsInfoBottom);
        }
    }

    private EtkProject getProject() {
        return getConnector().getProject();
    }

    private AbstractJavaViewerFormIConnector getConnector() {
        return initDataForSpringGUI.springProperties.getConnector();
    }

    private AbstractJavaViewerForm getParentForm() {
        return initDataForSpringGUI.springProperties.getParentForm();
    }

    public iPartsSpringProperties getSpringLegProperties() {
        return initDataForSpringGUI.springProperties;
    }

    /**
     * Hilfsobjekt mit den notwendigen Informationen zur Erstellung der GUI
     */
    public static class InitDataForSpringGUI {

        public iPartsSpringProperties springProperties;
        public GuiPanel gridParentPanel;
        public GuiPanel textInputParentPanel;
        public List<String> activeSpringLegPartNumbers;
        public EtkDisplayFields displayFields;
        public String title;
        public String namePrefix;
    }
}
