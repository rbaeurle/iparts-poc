/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem;

import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsShowDataObjectsDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.GetEcoContentResult;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.table.GuiTableHeader;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Formular zur Anzeige vom BOM-DB KEM-Datenblatt
 */
public class iPartsEcoContentServiceForm extends AbstractJavaViewerForm {

    public enum KEM_HEADER_DATA_ROWS {
        ECO_NUMBER("!!Sachnummer"),
        RELEASE_DATE("!!Änderungstand/Datum"),
        VERSION("!!Änderungsstand/Version"),
        PROJECT("!!Projektnummer"),
        PROJECT_DESCRIPTION("!!Projektbezeichnung"),
        ENGINEERING_SCOPE("!!Betroffene Umfänge"),
        REASON("!!Ursache/Grund"),
        ENGINEERING_CHANGE_OVER("!!Einsatzvorgaben"),
        SIMULTANEOUS_ECOS("!!Zusammen mit"),
        REMARK("!!Bemerkung"),
        PLANT_SUPPLIES_EDS("!!Betroffene Werke EDS"),
        PLANT_SUPPLIES_BCS("!!Betroffene Werke BCS");

        private String description;

        KEM_HEADER_DATA_ROWS(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String getFieldName() {
            return name();
        }
    }

    private static final String DUMMY_TABLE_NAME = "BOM_DB_EcoContent";

    private iPartsEcoContentServiceGrid grid;
    private EtkDataObject dataEcoContent;

    /**
     * Zeigt das BOM-DB KEM-Datenblatt für das übergebene {@link GetEcoContentResult} an.
     *
     * @param dataConnector
     * @param parentForm
     * @param kemNumber
     * @param ecoContent
     */
    public static void showEcoContentServiceForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 String kemNumber, GetEcoContentResult ecoContent) {
        iPartsEcoContentServiceForm ecoContentServiceForm = new iPartsEcoContentServiceForm(dataConnector, parentForm, ecoContent);
        Dimension screenSize = FrameworkUtils.getScreenSize();
        final GuiWindow window = new GuiWindow(TranslationHandler.translate("!!BOM-DB KEM-Datenblatt für KEM \"%1\"", kemNumber),
                                               screenSize.width - 20, screenSize.height - 20);
        window.setResizable(true);
        window.setLayout(new LayoutBorder());
        window.addChildBorderCenter(ecoContentServiceForm.getGui());

        GuiButtonPanel buttonPanel = new GuiButtonPanel();
        buttonPanel.setDialogStyle(GuiButtonPanel.DialogStyle.CLOSE);
        window.addChildBorderSouth(buttonPanel);
        GuiButtonOnPanel closeButton = buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE);
        closeButton.requestFocus();
        closeButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                window.setVisible(false);
                ecoContentServiceForm.dispose();
            }
        });

        window.addEventListener(new EventListener(Event.CLOSING_EVENT) {
            @Override
            public void fire(Event event) {
                window.setVisible(false);
                ecoContentServiceForm.dispose();
            }
        });

        window.showNonModal(GuiWindow.NonModalStyle.OPEN_IN_NEW_WINDOW);
    }

    /**
     * Erzeugt eine Instanz von iPartsEcoContentServiceForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsEcoContentServiceForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, GetEcoContentResult ecoContent) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        dataEcoContent = createDataEcoContent(ecoContent);
        postCreateGui();
        fillKEMDataTable(ecoContent);
    }

    private iPartsEcoContentServiceGrid createGrid() {
        List<EtkDataObject> dataObjects = new ArrayList(1);
        dataObjects.add(dataEcoContent);
        grid = new iPartsEcoContentServiceGrid(getConnector(), this, getDisplayFields(), dataObjects);
        return grid;
    }

    private void postCreateGui() {
        grid = createGrid();
        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        grid.getGui().setConstraints(constraints);
        mainWindow.tabbedPaneEntryKEMHeaderContentPanel.addChildBorderCenter(grid.getGui());

        // KEM-Inhalt / KEM-Daten als Tabelle
        GuiTableHeader header = new GuiTableHeader();
        header.addChild(TranslationHandler.translate("!!KEM-Pos."));
        header.addChild(TranslationHandler.translate("!!Sachnummer"));
        header.addChild(TranslationHandler.translate("!!Änderungskennzeichen"));
        header.addChild(TranslationHandler.translate("!!AS"));
        header.addChild(TranslationHandler.translate("!!Benennung"));
        header.addChild(TranslationHandler.translate("!!Rohteil-KZ"));
        header.addChild(TranslationHandler.translate("!!Fertigteil-KZ"));
        header.addChild(TranslationHandler.translate("!!Montiert-KZ"));
        header.addChild(TranslationHandler.translate("!!ET-KZ"));
        header.addChild(TranslationHandler.translate("!!Austausch-KZ"));
        header.addChild(TranslationHandler.translate("!!Werkzeug-KZ"));
        header.addChild(TranslationHandler.translate("!!Änderungstext"));
        mainWindow.tabbedPaneEntryKEMDataTable.setHeader(header);
    }

    @Override
    public void dispose() {
        super.dispose();
        grid.dispose();
    }

    private EtkDisplayFields getDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        for (KEM_HEADER_DATA_ROWS kemHeaderDataRow : KEM_HEADER_DATA_ROWS.values()) {
            displayFields.addFeld(DUMMY_TABLE_NAME, kemHeaderDataRow.getFieldName(), false, false, kemHeaderDataRow.getDescription(),
                                  getProject());
        }
        return displayFields;
    }


    private EtkDataObject createDataEcoContent(GetEcoContentResult ecoContent) {
        DBDataObjectAttributes ecoContentAttributes = new DBDataObjectAttributes();
        for (KEM_HEADER_DATA_ROWS kemHeaderDataRow : KEM_HEADER_DATA_ROWS.values()) {
            DBDataObjectAttribute attrib = createAttribute(kemHeaderDataRow, ecoContent);
            ecoContentAttributes.addField(attrib, DBActionOrigin.FROM_DB);
        }

        // Ein künstliches EtkDataObject für die DBDataObjectAttributes erstellen, damit dieses im iPartsEcoContentServiceGrid
        // verwendet werden kann
        return new EtkDataObject(new String[]{ KEM_HEADER_DATA_ROWS.ECO_NUMBER.name() }) {
            {
                tableName = DUMMY_TABLE_NAME;
                init(getProject());
                setAttributes(ecoContentAttributes, true, false, DBActionOrigin.FROM_DB);
            }

            @Override
            public IdWithType createId(String... idValues) {
                return new IdWithType(DUMMY_TABLE_NAME, idValues);
            }

            @Override
            public IdWithType getAsId() {
                if (id == null) {
                    setEmptyId(DBActionOrigin.FROM_DB);
                }

                return id;
            }
        };
    }

    /**
     * Erzeugt pro KEM-Inhaltsdatensatz eine Zeile in der zugehörigen Tabelle.
     *
     * @param ecoContent Der EcoContent aus dem BOM-DB SOAP Service
     */
    private void fillKEMDataTable(GetEcoContentResult ecoContent) {
        if ((ecoContent.getEco() != null) && (ecoContent.getEco().getK3S() != null)) {
            EtkProject project = getProject();
            ecoContent.getEco().getK3S().getK3().stream()
                    .filter(Objects::nonNull)
                    .forEach((k3) -> {
                        List<String> kemDataContentCells = new ArrayList<>();

                        /* KEM-Pos. */
                        kemDataContentCells.add(StrUtils.getEmptyOrValidString(k3.getPosition()));

                        /* Sachnummer */
                        String itemNumber = StrUtils.getEmptyOrValidString(k3.getItem());
                        itemNumber = iPartsNumberHelper.formatPartNo(project, itemNumber);
                        kemDataContentCells.add(itemNumber);

                        /* Änderungskennzeichen */
                        kemDataContentCells.add(StrUtils.getEmptyOrValidString(k3.getModificationFlag()));

                        // Leere Zellen hinzufügen falls Daten fehlen
                        String as = "";
                        String description = "";
                        if ((k3.getItemContainer() != null) && (k3.getItemContainer().getPartMasterData() != null)) {
                            /* AS */
                            as = StrUtils.getEmptyOrValidString(k3.getItemContainer().getPartMasterData().getVersionFrom().toString());
                            /* Benennung */
                            description = StrUtils.getEmptyOrValidString(k3.getItemContainer().getPartMasterData().getDescription());
                        }
                        kemDataContentCells.add(as);
                        kemDataContentCells.add(description);

                        VarParam<Boolean> rowAdded = new VarParam<>(false);
                        if (k3.getChangeNotices() != null) {
                            k3.getChangeNotices().getChangeNotice().stream()
                                    .filter(Objects::nonNull)
                                    .forEach((cn) -> {
                                        rowAdded.setValue(true);
                                        GuiTableRow row = makeRowFromList(kemDataContentCells);

                                        if (cn.getOrderSupplementData() != null) {
                                            /* Rohteil-KZ */
                                            row.addChild(StrUtils.getEmptyOrValidString(cn.getOrderSupplementData().getRawMaterial()));
                                            /* Fertigteil-KZ */
                                            row.addChild(StrUtils.getEmptyOrValidString(cn.getOrderSupplementData().getFinishedPart()));
                                            /* Montiert-KZ */
                                            row.addChild(StrUtils.getEmptyOrValidString(cn.getOrderSupplementData().getMounting()));
                                            /* ET-KZ */
                                            row.addChild(StrUtils.getEmptyOrValidString(cn.getOrderSupplementData().getSparePart()));
                                            /* Austausch-KZ */
                                            row.addChild(StrUtils.getEmptyOrValidString(cn.getOrderSupplementData().getExchangeability()));
                                            /* Werkzeug-KZ */
                                            row.addChild(StrUtils.getEmptyOrValidString(cn.getOrderSupplementData().getTool()));
                                        } else { // Leere Zellen hinzufügen falls Daten fehlen
                                            fillRowWithEmptyValues(row, 6);
                                        }

                                        /* Änderungstext */
                                        StringBuilder changeNotices = new StringBuilder();
                                        if (cn.getChangeNoticeTexts() != null) {
                                            cn.getChangeNoticeTexts().getChangeNoticeText().stream()
                                                    .filter(Objects::nonNull)
                                                    .forEach((text) -> {
                                                        addChangeNoticeRow(changeNotices, text.getChangeNoticeTextRow1());
                                                        addChangeNoticeRow(changeNotices, text.getChangeNoticeTextRow2());
                                                        addChangeNoticeRow(changeNotices, text.getChangeNoticeTextRow3());
                                                    });
                                        }
                                        row.addChild(changeNotices.toString());

                                        mainWindow.tabbedPaneEntryKEMDataTable.addRow(row);
                                    });
                        }

                        if (!rowAdded.getValue()) { // Es wurde bisher keine Zeile hinzugefügt, weil es keine ChangeNotices gibt
                            GuiTableRow row = makeRowFromList(kemDataContentCells);
                            // Leere Zellen hinzufügen (OrderSupplementData + ChangeNoticeTexts)
                            fillRowWithEmptyValues(row, 7);
                            mainWindow.tabbedPaneEntryKEMDataTable.addRow(row);
                        }
                    });
        }
    }

    private void fillRowWithEmptyValues(GuiTableRow row, int amountEmptyValues) {
        for (int i = 0; i < amountEmptyValues; i++) {
            row.addChild("");
        }
    }

    private GuiTableRow makeRowFromList(List<String> listWithRowValues) {
        GuiTableRow row = new GuiTableRow();
        for (String rowValue : listWithRowValues) {
            row.addChild(rowValue);
        }
        return row;
    }

    private void addChangeNoticeRow(StringBuilder changeNotices, String changeNoticeRow) {
        String rowText = StrUtils.getEmptyOrValidString(changeNoticeRow).trim();
        if (!rowText.isEmpty()) {
            if (changeNotices.length() > 0) {
                changeNotices.append('\n');
            }
            changeNotices.append(rowText.trim());
        }
    }

    private DBDataObjectAttribute createAttribute(KEM_HEADER_DATA_ROWS kemHeaderDataRow, GetEcoContentResult ecoContent) {
        DBDataObjectAttribute attrib = new DBDataObjectAttribute(kemHeaderDataRow.getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
        String attributeValue = "";
        if (ecoContent != null) {
            if ((ecoContent.getEco() != null) && (ecoContent.getQueryHead() != null) && (ecoContent.getEco().getEcoMasterData() != null)) {
                switch (kemHeaderDataRow) {
                    case ECO_NUMBER:
                        attributeValue = StrUtils.getEmptyOrValidString(ecoContent.getEco().getEco());
                        break;
                    case RELEASE_DATE:
                        DateConfig dateConfig = DateConfig.getInstance(getProject().getConfig());
                        String date = DateUtils.toyyyyMMdd_Calendar(ecoContent.getQueryHead().getQueryTimeStamp().toGregorianCalendar());
                        attributeValue = StrUtils.getEmptyOrValidString(dateConfig.formatDateTime(getProject().getDBLanguage(), date));
                        break;
                    case VERSION:
                        attributeValue = StrUtils.getEmptyOrValidString(ecoContent.getQueryHead().getServiceVersion());
                        break;
                    case PROJECT:
                        attributeValue = StrUtils.getEmptyOrValidString(ecoContent.getEco().getEcoMasterData().getProject());
                        break;
                    case PROJECT_DESCRIPTION:
                        attributeValue = StrUtils.getEmptyOrValidString(ecoContent.getEco().getEcoMasterData().getProjectDescription());
                        break;
                    case ENGINEERING_SCOPE:
                        attributeValue = StrUtils.getEmptyOrValidString(ecoContent.getEco().getEcoMasterData().getEngineeringScope());
                        break;
                    case REASON:
                        attributeValue = StrUtils.getEmptyOrValidString(ecoContent.getEco().getEcoMasterData().getReason());
                        break;
                    case ENGINEERING_CHANGE_OVER:
                        attributeValue = StrUtils.getEmptyOrValidString(ecoContent.getEco().getEcoMasterData().getEngineeringChangeOver());
                        break;
                    case SIMULTANEOUS_ECOS:
                        if (ecoContent.getEco().getEcoMasterData().getSimultaneousEcos() != null) {
                            attributeValue = StrUtils.getEmptyOrValidString(StrUtils.getEmptyOrValidStringsFromList(ecoContent.getEco().getEcoMasterData().getSimultaneousEcos().getSimultaneousEco(), ", "));
                        }
                        break;
                    case REMARK:
                        attributeValue = StrUtils.getEmptyOrValidString(ecoContent.getEco().getEcoMasterData().getRemark());
                        break;
                    case PLANT_SUPPLIES_EDS:
                        if (ecoContent.getEco().getEcoMasterData().getPlantSuppliesEds() != null) {
                            attributeValue = StrUtils.getEmptyOrValidString(StrUtils.getEmptyOrValidStringsFromList(ecoContent.getEco().getEcoMasterData().getPlantSuppliesEds().getPlantSupply(), ", "));
                        }
                        break;
                    case PLANT_SUPPLIES_BCS:
                        if (ecoContent.getEco().getEcoMasterData().getPlantSuppliesBcs() != null) {
                            attributeValue = StrUtils.getEmptyOrValidString(StrUtils.getEmptyOrValidStringsFromList(ecoContent.getEco().getEcoMasterData().getPlantSuppliesBcs().getPlantSupply(), ", "));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        attrib.setValueAsString(attributeValue, DBActionOrigin.FROM_DB);
        return attrib;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.mainPanel;
    }

    @Override
    public boolean isSecondToolbarVisible() {
        return false;
    }


    private class iPartsEcoContentServiceGrid extends iPartsShowDataObjectsDialog {

        /**
         * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
         * Den $$internalCreateGui$$() Aufruf nicht ändern!
         *
         * @param dataConnector
         * @param parentForm
         * @param displayFields
         * @param dataObjects
         */
        public iPartsEcoContentServiceGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           EtkDisplayFields displayFields, List<EtkDataObject> dataObjects) {
            super(dataConnector, parentForm, displayFields, dataObjects, false, true, true, 100);
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
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPane tabbedpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedPaneEntryKEMHeader;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedPaneEntryKEMHeaderContent;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedPaneEntryKEMHeaderContentPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedPaneEntryKEMData;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedPaneEntryKEMDataContent;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedPaneEntryKEMDataContentPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable tabbedPaneEntryKEMDataTable;

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
            title.setTitle("!!BOM-DB KEM-Datenblatt");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            mainPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanel.setName("mainPanel");
            mainPanel.__internal_setGenerationDpi(96);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            mainPanel.setMinimumWidth(10);
            mainPanel.setMinimumHeight(10);
            mainPanel.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanel.setLayout(mainPanelLayout);
            tabbedpane = new de.docware.framework.modules.gui.controls.GuiTabbedPane();
            tabbedpane.setName("tabbedpane");
            tabbedpane.__internal_setGenerationDpi(96);
            tabbedpane.registerTranslationHandler(translationHandler);
            tabbedpane.setScaleForResolution(true);
            tabbedpane.setMinimumWidth(10);
            tabbedpane.setMinimumHeight(10);
            tabbedPaneEntryKEMHeader = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedPaneEntryKEMHeader.setName("tabbedPaneEntryKEMHeader");
            tabbedPaneEntryKEMHeader.__internal_setGenerationDpi(96);
            tabbedPaneEntryKEMHeader.registerTranslationHandler(translationHandler);
            tabbedPaneEntryKEMHeader.setScaleForResolution(true);
            tabbedPaneEntryKEMHeader.setMinimumWidth(10);
            tabbedPaneEntryKEMHeader.setMinimumHeight(10);
            tabbedPaneEntryKEMHeader.setTitle("!!KEM-Stammdaten");
            tabbedPaneEntryKEMHeaderContent = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedPaneEntryKEMHeaderContent.setName("tabbedPaneEntryKEMHeaderContent");
            tabbedPaneEntryKEMHeaderContent.__internal_setGenerationDpi(96);
            tabbedPaneEntryKEMHeaderContent.registerTranslationHandler(translationHandler);
            tabbedPaneEntryKEMHeaderContent.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedPaneEntryKEMHeaderContentLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedPaneEntryKEMHeaderContent.setLayout(tabbedPaneEntryKEMHeaderContentLayout);
            tabbedPaneEntryKEMHeaderContentPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedPaneEntryKEMHeaderContentPanel.setName("tabbedPaneEntryKEMHeaderContentPanel");
            tabbedPaneEntryKEMHeaderContentPanel.__internal_setGenerationDpi(96);
            tabbedPaneEntryKEMHeaderContentPanel.registerTranslationHandler(translationHandler);
            tabbedPaneEntryKEMHeaderContentPanel.setScaleForResolution(true);
            tabbedPaneEntryKEMHeaderContentPanel.setMinimumWidth(10);
            tabbedPaneEntryKEMHeaderContentPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedPaneEntryKEMHeaderContentPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedPaneEntryKEMHeaderContentPanel.setLayout(tabbedPaneEntryKEMHeaderContentPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tabbedPaneEntryKEMHeaderContentPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tabbedPaneEntryKEMHeaderContentPanel.setConstraints(tabbedPaneEntryKEMHeaderContentPanelConstraints);
            tabbedPaneEntryKEMHeaderContent.addChild(tabbedPaneEntryKEMHeaderContentPanel);
            tabbedPaneEntryKEMHeader.addChild(tabbedPaneEntryKEMHeaderContent);
            tabbedpane.addChild(tabbedPaneEntryKEMHeader);
            tabbedPaneEntryKEMData = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedPaneEntryKEMData.setName("tabbedPaneEntryKEMData");
            tabbedPaneEntryKEMData.__internal_setGenerationDpi(96);
            tabbedPaneEntryKEMData.registerTranslationHandler(translationHandler);
            tabbedPaneEntryKEMData.setScaleForResolution(true);
            tabbedPaneEntryKEMData.setMinimumWidth(10);
            tabbedPaneEntryKEMData.setMinimumHeight(10);
            tabbedPaneEntryKEMData.setTitle("!!KEM-Inhalt");
            tabbedPaneEntryKEMDataContent = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedPaneEntryKEMDataContent.setName("tabbedPaneEntryKEMDataContent");
            tabbedPaneEntryKEMDataContent.__internal_setGenerationDpi(96);
            tabbedPaneEntryKEMDataContent.registerTranslationHandler(translationHandler);
            tabbedPaneEntryKEMDataContent.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedPaneEntryKEMDataContentLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedPaneEntryKEMDataContent.setLayout(tabbedPaneEntryKEMDataContentLayout);
            tabbedPaneEntryKEMDataContentPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedPaneEntryKEMDataContentPanel.setName("tabbedPaneEntryKEMDataContentPanel");
            tabbedPaneEntryKEMDataContentPanel.__internal_setGenerationDpi(96);
            tabbedPaneEntryKEMDataContentPanel.registerTranslationHandler(translationHandler);
            tabbedPaneEntryKEMDataContentPanel.setScaleForResolution(true);
            tabbedPaneEntryKEMDataContentPanel.setMinimumWidth(10);
            tabbedPaneEntryKEMDataContentPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedPaneEntryKEMDataContentPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedPaneEntryKEMDataContentPanel.setLayout(tabbedPaneEntryKEMDataContentPanelLayout);
            tabbedPaneEntryKEMDataTable = new de.docware.framework.modules.gui.controls.table.GuiTable();
            tabbedPaneEntryKEMDataTable.setName("tabbedPaneEntryKEMDataTable");
            tabbedPaneEntryKEMDataTable.__internal_setGenerationDpi(96);
            tabbedPaneEntryKEMDataTable.registerTranslationHandler(translationHandler);
            tabbedPaneEntryKEMDataTable.setScaleForResolution(true);
            tabbedPaneEntryKEMDataTable.setMinimumWidth(10);
            tabbedPaneEntryKEMDataTable.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tabbedPaneEntryKEMDataTableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tabbedPaneEntryKEMDataTable.setConstraints(tabbedPaneEntryKEMDataTableConstraints);
            tabbedPaneEntryKEMDataContentPanel.addChild(tabbedPaneEntryKEMDataTable);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tabbedPaneEntryKEMDataContentPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tabbedPaneEntryKEMDataContentPanel.setConstraints(tabbedPaneEntryKEMDataContentPanelConstraints);
            tabbedPaneEntryKEMDataContent.addChild(tabbedPaneEntryKEMDataContentPanel);
            tabbedPaneEntryKEMData.addChild(tabbedPaneEntryKEMDataContent);
            tabbedpane.addChild(tabbedPaneEntryKEMData);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tabbedpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tabbedpane.setConstraints(tabbedpaneConstraints);
            mainPanel.addChild(tabbedpane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            this.addChild(mainPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}