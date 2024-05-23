/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualDocuRelStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * Darstellung der Doku-Relevanz Berechnung
 */
public class EditShowDocuRelevantCalculationForm extends AbstractJavaViewerForm {

    private static final String TABLE_NAME = "DUMMY_TABLE_NAME";
    private static final String FIELD_TEST = "TEST";
    private static final String FIELD_INFO = "INFO";
    private static final String FIELD_RESULT = "RESULT";

    public static void showDocuRelResults(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          List<iPartsVirtualDocuRelStates.DocuRelFilterElement> docuRelList, String title) {
        EditShowDocuRelevantCalculationForm dlg = new EditShowDocuRelevantCalculationForm(dataConnector, parentForm);
        if (!StrUtils.isValid(title)) {
            title = "!!Ergebnisse";
        }
        dlg.setTitle(title);
        dlg.fillGrid(docuRelList);
        dlg.showModal();
    }

    private SimpleMasterDataSearchFilterGrid grid;
    private SimpleMasterDataSearchFilterGrid extendedGrid;
    private final String tableName;

    /**
     * Erzeugt eine Instanz von EditShowDocuRelevantCalculationForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditShowDocuRelevantCalculationForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.tableName = TABLE_NAME;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        grid = createGrid();
        grid.setDisplayResultFields(getDefaultDisplayResultFields());
        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        grid.getGui().setConstraints(constraints);
        mainWindow.splitpaneFilterResult.addChild(grid.getGui());

        Dimension screenSize = FrameworkUtils.getScreenSize();
        mainWindow.setWidth((int)(screenSize.getWidth() * 0.9));

        extendedGrid = createGrid();
        extendedGrid.setDisplayResultFields(getDefaultDisplayResultFields());
        constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        extendedGrid.getGui().setConstraints(constraints);
        mainWindow.panelFilter.addChild(extendedGrid.getGui());
        extendedGrid.getTable().setSortEnabled(false);

        if (J2EEHandler.isJ2EE()) {
            mainWindow.dockingpanel.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.dockingpanel) {
                @Override
                public boolean isFireOnceValid(Event event) {
                    return mainWindow.dockingpanel.isSplitPaneSizeValid();
                }

                @Override
                public void fireOnce(Event event) {
                    int height = getClosedHeight();
                    mainWindow.setHeight(height);
                    mainWindow.dockingpanel.setShowing(false);
                    mainWindow.dockingpanel.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                        @Override
                        public void fire(Event event) {
                            doOpenCloseDockingPanel(event, this);
                        }
                    });
                }
            });
        } else {
            mainWindow.dockingpanel.addEventListener(new EventListenerFireOnce(Event.OPENED_EVENT, mainWindow.dockingpanel) {
                @Override
                public boolean isFireOnceValid(Event event) {
                    int height = getClosedHeight();
                    return mainWindow.getHeight() != height;
                }

                @Override
                public void fireOnce(Event event) {
                    int height = getClosedHeight();
                    if (mainWindow.getHeight() != height) {
                        mainWindow.dockingpanel.getParent().addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT,
                                                                                                       mainWindow.dockingpanel.getParent()) {
                            @Override
                            public void fireOnce(Event event) {
                                mainWindow.dockingpanel.setShowing(false);
                                mainWindow.dockingpanel.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                                    @Override
                                    public void fire(Event event) {
                                        doOpenCloseDockingPanel(event, this);
                                    }
                                });
                            }
                        });
                        mainWindow.setHeight(height);
                    }
                }
            });
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelFilter;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public ModalResult showModal() {
        ModalResult result = mainWindow.showModal();
        close();
        return result;
    }

    private void doOpenCloseDockingPanel(Event event, EventListener eventListener) {
        if (mainWindow.dockingpanel.isShowing()) {
            int height = getOpenHeight();
            mainWindow.setHeight(height);
        } else {
            int height = getClosedHeight();
            mainWindow.setHeight(height);
            mainWindow.dockingpanel.removeEventListener(eventListener);
            mainWindow.dockingpanel.setShowing(true);
            mainWindow.dockingpanel.setShowing(false);
            mainWindow.dockingpanel.addEventListener(eventListener);
        }
    }

    private int getClosedHeight() {
        int factor = 5;
        int height = mainWindow.title.getPreferredHeight() + mainWindow.buttonpanel.getPreferredHeight() + 12;
        height = height + (grid.getTable().getHeader().getHeight() * factor) + 32; // 146;
        return height;
    }

    private int getOpenHeight() {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        return (int)((screenSize.getHeight()));
    }

    private void fillGrid(List<iPartsVirtualDocuRelStates.DocuRelFilterElement> docuRelList) {
        // Sortieren nach vorgegebener Reihenfolge in iPartsVirtualDocuRelStates
        docuRelList.sort(Comparator.comparingInt(o -> o.getType().getShowOrder()));
        grid.clearGrid();
        extendedGrid.clearGrid();
        Color backgroundColor = null;
        boolean isBeforeTrigger = true;
        boolean isInformationArea = false; // Flag, ob wir im PV Informationsbereich sind
        boolean valueIsFixed = false; // Flag, ob wir im PV Informationsbereich sind
        for (iPartsVirtualDocuRelStates.DocuRelFilterElement docuRelElem : docuRelList) {
            switch (docuRelElem.getType()) {
                case DOKU_REL_STATUS_CURRENT_POSV:
                    // Setze die Titelzeile für den Info-Bereich
                    addInformationAreaTitle();
                    isInformationArea = true;
                    break;
                case DOCUREL_SET_BY_AUTHOR:
                case DOCUREL_USED_IN_AS:
                case DOCUREL_USED_IN_AS_IN_OTHER_AUTHOR_ORDER:
                    // Wurde der Wert von Hand gesetzt oder ist die Position schon in AS, dann werden alle nachfolgenden
                    // Prüfungen nicht durchgeführt
                    valueIsFixed |= docuRelElem.isTrigger();
                    break;
            }
            DBDataObjectAttributes attributes = createAndFillAttributes(docuRelElem);
            extendedGrid.addAttributesToGrid(attributes);
            if (docuRelElem.isTrigger()) {
                DBDataObjectAttributes gridAttributes = attributes.cloneMe(DBActionOrigin.FROM_DB);
                gridAttributes.addField(FIELD_RESULT, getVisDocuRelValue(docuRelElem), DBActionOrigin.FROM_DB);
                grid.addAttributesToGrid(gridAttributes);
            }

            if (isBeforeTrigger) {
                if (docuRelElem.isTrigger()) {
                    backgroundColor = iPartsPlugin.clPlugin_iParts_CodeMatrixBackground_positive.getColor();
                    isBeforeTrigger = false;
                }
            } else {
                // Ab der ersten Berechnung auf Basis der Positionsvarianten werden alle Prüfungen durchgeführt und nur
                // die, die der Grund für das Ergebnis ist, wird grün dargestellt.
                if (!valueIsFixed && (docuRelElem.getType() == iPartsVirtualDocuRelStates.DOKU_REL_STATUS_CURRENT_POSV)) {
                    isBeforeTrigger = true;
                    backgroundColor = null;
                } else {
                    backgroundColor = iPartsPlugin.clPlugin_iParts_CodeMatrixBackground_negative.getColor();
                }
            }
            if (!isInformationArea && (backgroundColor != null)) {
                GuiTableRow tableRow = extendedGrid.getTable().getRow(extendedGrid.getTable().getRowCount() - 1);
                tableRow.setBackgroundColor(backgroundColor);
            }
        }

        grid.showNoResultsLabel(grid.getTable().getRowCount() == 0, false);
        extendedGrid.showNoResultsLabel(extendedGrid.getTable().getRowCount() == 0, false);
    }

    /**
     * Erzeugt aus dem übergebenen {@link de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualDocuRelStates.DocuRelFilterElement}
     * Objekt ein {@link DBDataObjectAttribute} Objekt und befüllt es mit den Werten aus dem <code>docuRelElem</code>
     * Objekt.
     *
     * @param docuRelElem
     * @return
     */
    private DBDataObjectAttributes createAndFillAttributes(iPartsVirtualDocuRelStates.DocuRelFilterElement docuRelElem) {
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        if (docuRelElem != null) {
            for (EtkDisplayField displayField : grid.getDisplayResultFields().getVisibleFields()) {
                DBDataObjectAttribute attrib;
                if (docuRelElem.getType() == iPartsVirtualDocuRelStates.DOCU_REL_OVERALL_RESULT) {
                    attrib = fillOverallAttribute(displayField, docuRelElem);
                } else {
                    attrib = fillAttribute(displayField, docuRelElem);
                }
                attributes.addField(attrib, DBActionOrigin.FROM_DB);
            }
        }
        return attributes;
    }

    /**
     * Setzt die Titelzeile für den Info-Bereich
     */
    private void addInformationAreaTitle() {
        // Leerzeile eingügen
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        for (EtkDisplayField displayField : grid.getDisplayResultFields().getVisibleFields()) {
            DBDataObjectAttribute attrib = new DBDataObjectAttribute(displayField.getKey().getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
            // Hinweis, dass die nächsten Zeile Informationen für die Doku-Relevanz basierend auf den
            // Positionsvarianten beinhalten
            if (displayField.getKey().getFieldName().equals(FIELD_TEST)) {
                attrib.setValueAsString(TranslationHandler.translate("!!Informationen zu allen PVs:"), DBActionOrigin.FROM_DB);
            }
            attributes.addField(attrib, DBActionOrigin.FROM_DB);
        }
        extendedGrid.addAttributesToGrid(attributes);
    }

    private DBDataObjectAttribute fillAttribute(EtkDisplayField displayField, iPartsVirtualDocuRelStates.DocuRelFilterElement docuRelElem) {
        DBDataObjectAttribute attrib = new DBDataObjectAttribute(displayField.getKey().getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
        switch (displayField.getKey().getFieldName()) {
            case FIELD_TEST:
                attrib.setValueAsString(TranslationHandler.translate(docuRelElem.getDescription()), DBActionOrigin.FROM_DB);
                break;
            case FIELD_INFO:
                attrib.setValueAsString(docuRelElem.getExtraInfo(), DBActionOrigin.FROM_DB);
                break;
            case FIELD_RESULT:
                if (docuRelElem.getState() == null) {
                    attrib.setValueAsString(TranslationHandler.translate(docuRelElem.getType().getDefaultResultValue()), DBActionOrigin.FROM_DB);
                } else {
                    attrib.setValueAsString(TranslationHandler.translate("!!Doku-relevanz \"%1\"", docuRelElem.getState().getDisplayValue(getProject())), DBActionOrigin.FROM_DB);
                }
                break;
            default:
//                attrib = null;
                break;
        }

        return attrib;
    }

    private DBDataObjectAttribute fillOverallAttribute(EtkDisplayField displayField, iPartsVirtualDocuRelStates.DocuRelFilterElement docuRelElem) {
        DBDataObjectAttribute attrib = new DBDataObjectAttribute(displayField.getKey().getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
        switch (displayField.getKey().getFieldName()) {
            case FIELD_TEST:
                attrib.setValueAsString(TranslationHandler.translate(docuRelElem.getDescription()), DBActionOrigin.FROM_DB);
                break;
            case FIELD_INFO:
                attrib.setValueAsString(getVisDocuRelValue(docuRelElem), DBActionOrigin.FROM_DB);
                break;
            case FIELD_RESULT:
                attrib.setValueAsString("", DBActionOrigin.FROM_DB);
                break;
            default:
//                attrib = null;
                break;
        }

        return attrib;
    }

    private String getVisDocuRelValue(iPartsVirtualDocuRelStates.DocuRelFilterElement docuRelElem) {
        return getProject().getVisObject().asHtml(iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_DOCU_RELEVANT,
                                                  docuRelElem.getState().getDbValue(), getProject().getDBLanguage()).getStringResult();
    }

    private SimpleMasterDataSearchFilterGrid createGrid() {
        SimpleMasterDataSearchFilterGrid resultGrid = new SimpleMasterDataSearchFilterGrid(getConnector(), this, tableName, null);
        resultGrid.setMaxResults(J2EEHandler.isJ2EE() ? iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
        resultGrid.showSearchFields(false);
        resultGrid.setEditAllowed(false);
        resultGrid.setModifyAllowed(false);
        resultGrid.showSelectCount(false);
        resultGrid.showToolbar(false);
        resultGrid.setLabelNotFoundText("!!Es liegen keine Ergebnisse vor.");
        OnDblClickEvent onDblClickEvent = () -> {
            // Mit Absicht ein leerer Event damit nichts gemacht wird
        };
        resultGrid.setOnDblClickEvent(onDblClickEvent);
        return resultGrid;
    }

    private EtkDisplayFields getDefaultDisplayResultFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = MasterDataProductForm.addDisplayField(tableName, FIELD_TEST, false, false, null, getProject(), displayFields);
        EtkMultiSprache multi = new EtkMultiSprache();
        multi.setText(getProject().getViewerLanguage(), TranslationHandler.translate("!!Prüfung"));
        displayField.setText(multi);
        displayField.setDefaultText(false);
//        displayField.setColumnFilterEnabled(true);

        displayField = MasterDataProductForm.addDisplayField(tableName, FIELD_INFO, false, false, null, getProject(), displayFields);
        multi = new EtkMultiSprache();
        multi.setText(getProject().getViewerLanguage(), TranslationHandler.translate("!!Zusatzinfo"));
        displayField.setText(multi);
        displayField.setDefaultText(false);
//        displayField.setColumnFilterEnabled(true);

        displayField = MasterDataProductForm.addDisplayField(tableName, FIELD_RESULT, false, false, null, getProject(), displayFields);
        multi = new EtkMultiSprache();
        multi.setText(getProject().getViewerLanguage(), TranslationHandler.translate("!!Ergebnis"));
        displayField.setText(multi);
        displayField.setDefaultText(false);
//        displayField.setColumnFilterEnabled(true);

        return displayFields;
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
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneFilterResult;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelFilter;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(1200);
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
            splitpane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpane.setName("splitpane");
            splitpane.__internal_setGenerationDpi(96);
            splitpane.registerTranslationHandler(translationHandler);
            splitpane.setScaleForResolution(true);
            splitpane.setMinimumWidth(10);
            splitpane.setMinimumHeight(10);
            splitpane.setHorizontal(false);
            splitpane.setDividerPosition(96);
            splitpaneFilterResult = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneFilterResult.setName("splitpaneFilterResult");
            splitpaneFilterResult.__internal_setGenerationDpi(96);
            splitpaneFilterResult.registerTranslationHandler(translationHandler);
            splitpaneFilterResult.setScaleForResolution(true);
            splitpaneFilterResult.setMinimumWidth(0);
            splitpaneFilterResult.setMinimumHeight(0);
            splitpaneFilterResult.setPaddingLeft(4);
            splitpaneFilterResult.setPaddingRight(4);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneFilterResultLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneFilterResult.setLayout(splitpaneFilterResultLayout);
            splitpane.addChild(splitpaneFilterResult);
            dockingpanel = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanel.setName("dockingpanel");
            dockingpanel.__internal_setGenerationDpi(96);
            dockingpanel.registerTranslationHandler(translationHandler);
            dockingpanel.setScaleForResolution(true);
            dockingpanel.setMinimumWidth(10);
            dockingpanel.setMinimumHeight(10);
            dockingpanel.setTextHide("!!Details ausblenden");
            dockingpanel.setTextShow("!!Details anzeigen");
            dockingpanel.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            dockingpanel.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            dockingpanel.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            dockingpanel.setButtonFill(true);
            panelFilter = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelFilter.setName("panelFilter");
            panelFilter.__internal_setGenerationDpi(96);
            panelFilter.registerTranslationHandler(translationHandler);
            panelFilter.setScaleForResolution(true);
            panelFilter.setMinimumWidth(10);
            panelFilter.setMinimumHeight(10);
            panelFilter.setPaddingLeft(4);
            panelFilter.setPaddingRight(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelFilterLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelFilter.setLayout(panelFilterLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelFilterConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelFilter.setConstraints(panelFilterConstraints);
            dockingpanel.addChild(panelFilter);
            splitpane.addChild(dockingpanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpane.setConstraints(splitpaneConstraints);
            panelMain.addChild(splitpane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}