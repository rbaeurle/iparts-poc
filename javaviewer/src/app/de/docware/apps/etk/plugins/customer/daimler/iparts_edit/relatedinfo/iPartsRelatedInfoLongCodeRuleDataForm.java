/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.dia4u.iPartsDia4UServiceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.dia4u.partUsages.iPartsDia4UPartUsagesGetLongCodeRuleResponse;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Formular für Anzeige der Langen Code Regeln eins Datensatzes in der Konstruktion
 */
public class iPartsRelatedInfoLongCodeRuleDataForm extends AbstractRelatedInfoPartlistDataForm {

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.Dialog_SM_Construction);

    public enum HeaderDataRows {
        LK("!!LK"),
        KEMA("!!KEM-AB"),
        SDATA("!!Datum ab"),
        KEMB("!!KEM-Bis"),
        SDATB("!!Datum bis"),
        LCR("!!Lange Coderegel");

        private final String description;

        HeaderDataRows(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String getFieldName() {
            return name();
        }
    }

    private final String DUMMY_TABLE_NAME = "dummy";
    private DataObjectGrid longCodeRuleResultGrid;

    public iPartsRelatedInfoLongCodeRuleDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        return null;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    protected void postCreateGui() {
        longCodeRuleResultGrid = new DataObjectGrid(getConnector(), this);
        longCodeRuleResultGrid.setDisplayFields(createEtkDisplayFields());
        mainWindow.longCodeTablePanel.addChildBorderCenter(longCodeRuleResultGrid.getGui());
        longCodeRuleResultGrid.setDefaultNoEntriesText();
        longCodeRuleResultGrid.showToolbar(false);

        // Lange Coderegeln im Grid
        EtkDataPartListEntry partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        List<iPartsDia4UPartUsagesGetLongCodeRuleResponse> foundLongCodeRules = getLongCodeRuleObjects(partListEntry);
        fillGrid(foundLongCodeRules);

        // Kurze Coderegel in Textarea
        String shortCodeRule = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES);
        mainWindow.codeTextArea.setText(shortCodeRule);
    }

    public EtkDisplayField getEtkDisplayFieldWithDummyTable(HeaderDataRows header) {
        EtkDisplayField displayField = new EtkDisplayField(DUMMY_TABLE_NAME, header.getFieldName(), false, false);
        displayField.loadStandards(getConfig());
        displayField.setDefaultText(false);
        displayField.setText(new EtkMultiSprache(header.getDescription(), getConfig().getViewerLanguages()));
        return displayField;
    }

    public EtkDisplayFields createEtkDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        for (HeaderDataRows rowHeader : HeaderDataRows.values()) {
            EtkDisplayField displayField = getEtkDisplayFieldWithDummyTable(rowHeader);
            displayFields.addFeld(displayField);
        }
        return displayFields;
    }

    /**
     * Alle langen Code Regeln vom DIA4U Webservice für die selektierte Teileposition abfragen. Eingeschränkt auf
     * Baureihe, Matnr und Ausführungsart.
     * Erhaltene Datenobjekte werden weiter eingeschränkt: BR, Raster, PosE, Pv, AA, Etz, WW müssen mit der selektierten
     * Teilepos übereinstimmen. Außerdem muss Sesi = E und Posp = "" sein (die Werte vom Webservice).
     * Wird nach diesen Kriterien ein Datensatz gefunden, muss dieser noch in die Zeitscheibe passen.
     * Gefundene Datensätze werden nach SDATA und Lenkung sortiert.
     *
     * @param partListEntry selektierte Teilepos
     * @return
     */
    private List<iPartsDia4UPartUsagesGetLongCodeRuleResponse> getLongCodeRuleObjects(EtkDataPartListEntry partListEntry) {
        if (!partListEntry.existsInDB()) {
            return null;
        }
        VarParam<List<iPartsDia4UPartUsagesGetLongCodeRuleResponse>> resultSetParam = new VarParam<>();
        EtkMessageLogForm logForm = new EtkMessageLogForm("!!Lange Coderegel",
                                                          TranslationHandler.translate("!!Frage LCR Webservice an..."),
                                                          null, true);
        logForm.disableButtons(true);
        logForm.showModal(thread -> {
            try {
                String partNo = partListEntry.getFieldValue(iPartsConst.FIELD_K_MATNR);
                String series = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO);
                String aa = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA);
                Set<iPartsDia4UPartUsagesGetLongCodeRuleResponse> resultSetUnfiltered =
                        iPartsDia4UServiceUtils.getLongCodeRuleObjectFromDia4UService(partNo, series, aa,
                                                                                      getConnector().getProject().getDBLanguage());
                if (resultSetUnfiltered != null) {
                    List<iPartsDia4UPartUsagesGetLongCodeRuleResponse> validEntries = new ArrayList<>();
                    iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
                    if (bcteKey != null) {
                        // BCTE Schlüssel der Stücklistenposition ohne SDATA
                        iPartsDialogBCTEPrimaryKey bcteFromPartListEntryWithoutSData = bcteKey.getPositionBCTEPrimaryKeyWithoutSDA();
                        for (iPartsDia4UPartUsagesGetLongCodeRuleResponse longCodeRuleObject : resultSetUnfiltered) {
                            HmMSmId longCodeRuleObjectHmMSmId = HmMSmId.getIdFromRaster(longCodeRuleObject.getBrteBr(),
                                                                                        longCodeRuleObject.getBrteRas());
                            if (longCodeRuleObjectHmMSmId != null) {
                                // BCTE Schlüssel für die Werte aus der Response
                                iPartsDialogBCTEPrimaryKey bctePrimaryKeyFromWS = new iPartsDialogBCTEPrimaryKey(longCodeRuleObjectHmMSmId,
                                                                                                                 longCodeRuleObject.getBrtePose(),
                                                                                                                 longCodeRuleObject.getBrtePv(),
                                                                                                                 longCodeRuleObject.getBrteWw(),
                                                                                                                 longCodeRuleObject.getBrteEtz(),
                                                                                                                 longCodeRuleObject.getBrteAa(),
                                                                                                                 "");
                                // BCTE Werte (bis auf SDATA) müssen übereinstimmen und SESI = "E" und POSP muss leer sein
                                // und die Zeitscheiben müssen überlappen
                                if (bcteFromPartListEntryWithoutSData.equals(bctePrimaryKeyFromWS)
                                    && longCodeRuleObject.getBrteSesi().equals("E")
                                    && longCodeRuleObject.getBrtePosp().equals("")
                                    && isValidTimeSlice(partListEntry, longCodeRuleObject)) {
                                    validEntries.add(longCodeRuleObject);
                                }
                            }
                        }
                    }
                    validEntries.sort(Comparator.comparing((iPartsDia4UPartUsagesGetLongCodeRuleResponse::getBrteSdata))
                                              .thenComparing(iPartsDia4UPartUsagesGetLongCodeRuleResponse::getBrteL));
                    resultSetParam.setValue(validEntries);
                }
            } catch (CallWebserviceException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DIA_4_U, LogType.ERROR, e);
                MessageDialog.showError("!!Fehler beim Aufruf des LCR Webservices. Lange Coderegel konnte nicht abgefragt werden!");
            }
        });
        return resultSetParam.getValue();
    }

    private boolean isValidTimeSlice(EtkDataPartListEntry partListEntry,
                                     iPartsDia4UPartUsagesGetLongCodeRuleResponse longCodeRuleObject) {
        // Zeitscheibenprüfung
        long ddSdata = getLongValue(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA));
        long ddSdatb = getLongValue(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB));
        if (ddSdatb == 0) {
            ddSdatb = Long.MAX_VALUE;
        }
        long brteSdata = getLongValue(longCodeRuleObject.getBrteSdata());
        long brteSdatb = getLongValue(longCodeRuleObject.getBrteSdatb());
        if (brteSdatb == 0) {
            brteSdatb = Long.MAX_VALUE;
        }
        return ((brteSdata < ddSdata) && (brteSdatb > ddSdata))
               || ((brteSdata < ddSdatb) && (brteSdatb >= ddSdatb))
               || ((brteSdata >= ddSdata) && (brteSdata < ddSdatb))
               || ((brteSdatb > ddSdata) && (brteSdatb <= ddSdatb));
    }

    private String convertDate(String dateTimeValue) {
        String dateTime = StrUtils.getEmptyOrValidString(dateTimeValue);
        if (StrUtils.isValid(dateTime)) {
            dateTime = dateTime.startsWith("9999") ? "" : dateTime;
        }
        return dateTime;

    }

    private long getLongValue(String dateTimeValue) {
        return StrUtils.strToLongDef(convertDate(dateTimeValue), 0);
    }

    /**
     * Aus dem Datensatz der Dia4UResponse muss ein Objekt erstellt werden. Dieses wird ans Grid gehängt.
     *
     * @param longCodeRuleResponses Dia4U Response
     */
    private void fillGrid(List<iPartsDia4UPartUsagesGetLongCodeRuleResponse> longCodeRuleResponses) {
        if (longCodeRuleResponses == null) {
            longCodeRuleResultGrid.setNoResultsLabelText(DataObjectGrid.NO_ENTRIES_IN_GRID_TEXT);
            longCodeRuleResultGrid.showNoResultsLabel(true);
            return;
        } else if (longCodeRuleResponses.isEmpty()) {
            longCodeRuleResultGrid.setNoResultsLabelText("!!Keine gültigen Einträge");
            longCodeRuleResultGrid.showNoResultsLabel(true);
            return;

        }
        for (iPartsDia4UPartUsagesGetLongCodeRuleResponse longCodeRule : longCodeRuleResponses) {
            EtkDataObject longCodeRuleObject = createDataLongCodeRuleObject(longCodeRule);
            longCodeRuleResultGrid.addObjectToGrid(longCodeRuleObject);
        }
    }

    /**
     * Erstellen eines {@link EtkDataObject} Objekts aus der Dia4U Response um die Informationen im Grid anzuzeigen.
     *
     * @param longCodeRuleData
     * @return
     */
    private EtkDataObject createDataLongCodeRuleObject(iPartsDia4UPartUsagesGetLongCodeRuleResponse longCodeRuleData) {
        DBDataObjectAttributes longCodeRuleObjecAttributes = new DBDataObjectAttributes();
        for (HeaderDataRows rowHeader : HeaderDataRows.values()) {
            DBDataObjectAttribute attrib = createAttribute(rowHeader, longCodeRuleData);
            longCodeRuleObjecAttributes.addField(attrib, DBActionOrigin.FROM_DB);
        }

        // Ein künstliches EtkDataObject für die DBDataObjectAttributes erstellen, damit dieses im DataObjectGrid
        // verwendet werden kann
        return new EtkDataObject(new String[]{ HeaderDataRows.LK.name() }) {
            {
                tableName = DUMMY_TABLE_NAME;
                init(getProject());
                setAttributes(longCodeRuleObjecAttributes, true, false, DBActionOrigin.FROM_DB);
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
     * Attribute erstellen für das Costum-Objekt aus der DIA4U Response
     *
     * @param headerDataRow
     * @param longCodeRuleResponse
     * @return
     */
    private DBDataObjectAttribute createAttribute(HeaderDataRows headerDataRow, iPartsDia4UPartUsagesGetLongCodeRuleResponse longCodeRuleResponse) {
        DBDataObjectAttribute attrib = new DBDataObjectAttribute(headerDataRow.getFieldName(), DBDataObjectAttribute.TYPE.STRING, false);
        String attributeValue = "";
        if (longCodeRuleResponse != null) {
            DateConfig dateConfig = DateConfig.getInstance(getProject().getConfig());
            switch (headerDataRow) {
                case LK:
                    attributeValue = StrUtils.getEmptyOrValidString(longCodeRuleResponse.getBrteL());
                    break;
                case SDATA:
                    attributeValue = getValidDateAttributeValue(longCodeRuleResponse.getBrteSdata(), dateConfig);
                    break;
                case KEMA:
                    attributeValue = StrUtils.getEmptyOrValidString(longCodeRuleResponse.getBrteKema());
                    break;
                case SDATB:
                    attributeValue = getValidDateAttributeValue(longCodeRuleResponse.getBrteSdatb(), dateConfig);
                    break;
                case KEMB:
                    attributeValue = StrUtils.getEmptyOrValidString(longCodeRuleResponse.getBrteKemb());
                    break;
                case LCR:
                    attributeValue = StrUtils.getEmptyOrValidString(longCodeRuleResponse.getBrteLcr());
                    break;
                default:
                    break;
            }
        }
        attrib.setValueAsString(attributeValue, DBActionOrigin.FROM_DB);
        return attrib;
    }

    private String getValidDateAttributeValue(String dateTimeValue, DateConfig dateConfig) {
        String value = convertDate(dateTimeValue);
        if (StrUtils.isValid(value)) {
            value = StrUtils.getEmptyOrValidString(dateConfig.formatDate(getProject().getDBLanguage(), value));
        }
        return value;
    }

    public static boolean relatedInfoIsVisible(EtkDataAssembly assembly) {
        return relatedInfoIsVisible(assembly, VALID_MODULE_TYPES);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.mainPanel;
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(120);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel codeTextAreaPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel codeTextAreaLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane codeTextAreaScrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea codeTextArea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel longCodeTablePanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel longCodeLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            mainPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanel.setName("mainPanel");
            mainPanel.__internal_setGenerationDpi(120);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            mainPanel.setMinimumWidth(10);
            mainPanel.setMinimumHeight(10);
            mainPanel.setPaddingTop(4);
            mainPanel.setPaddingLeft(8);
            mainPanel.setPaddingRight(8);
            mainPanel.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanel.setLayout(mainPanelLayout);
            codeTextAreaPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            codeTextAreaPanel.setName("codeTextAreaPanel");
            codeTextAreaPanel.__internal_setGenerationDpi(120);
            codeTextAreaPanel.registerTranslationHandler(translationHandler);
            codeTextAreaPanel.setScaleForResolution(true);
            codeTextAreaPanel.setMinimumWidth(0);
            codeTextAreaPanel.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder codeTextAreaPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            codeTextAreaPanel.setLayout(codeTextAreaPanelLayout);
            codeTextAreaLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            codeTextAreaLabel.setName("codeTextAreaLabel");
            codeTextAreaLabel.__internal_setGenerationDpi(120);
            codeTextAreaLabel.registerTranslationHandler(translationHandler);
            codeTextAreaLabel.setScaleForResolution(true);
            codeTextAreaLabel.setMinimumWidth(10);
            codeTextAreaLabel.setMinimumHeight(10);
            codeTextAreaLabel.setPaddingBottom(4);
            codeTextAreaLabel.setText("!!Coderegel:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder codeTextAreaLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            codeTextAreaLabelConstraints.setPosition("north");
            codeTextAreaLabel.setConstraints(codeTextAreaLabelConstraints);
            codeTextAreaPanel.addChild(codeTextAreaLabel);
            codeTextAreaScrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            codeTextAreaScrollPane.setName("codeTextAreaScrollPane");
            codeTextAreaScrollPane.__internal_setGenerationDpi(120);
            codeTextAreaScrollPane.registerTranslationHandler(translationHandler);
            codeTextAreaScrollPane.setScaleForResolution(true);
            codeTextAreaScrollPane.setMinimumWidth(0);
            codeTextAreaScrollPane.setMinimumHeight(0);
            codeTextAreaScrollPane.setBorderWidth(1);
            codeTextAreaScrollPane.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            codeTextArea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            codeTextArea.setName("codeTextArea");
            codeTextArea.__internal_setGenerationDpi(120);
            codeTextArea.registerTranslationHandler(translationHandler);
            codeTextArea.setScaleForResolution(true);
            codeTextArea.setMinimumWidth(0);
            codeTextArea.setMinimumHeight(0);
            codeTextArea.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder codeTextAreaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            codeTextArea.setConstraints(codeTextAreaConstraints);
            codeTextAreaScrollPane.addChild(codeTextArea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder codeTextAreaScrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            codeTextAreaScrollPane.setConstraints(codeTextAreaScrollPaneConstraints);
            codeTextAreaPanel.addChild(codeTextAreaScrollPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder codeTextAreaPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            codeTextAreaPanelConstraints.setPosition("north");
            codeTextAreaPanel.setConstraints(codeTextAreaPanelConstraints);
            mainPanel.addChild(codeTextAreaPanel);
            longCodeTablePanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            longCodeTablePanel.setName("longCodeTablePanel");
            longCodeTablePanel.__internal_setGenerationDpi(120);
            longCodeTablePanel.registerTranslationHandler(translationHandler);
            longCodeTablePanel.setScaleForResolution(true);
            longCodeTablePanel.setMinimumWidth(0);
            longCodeTablePanel.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder longCodeTablePanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            longCodeTablePanel.setLayout(longCodeTablePanelLayout);
            longCodeLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            longCodeLabel.setName("longCodeLabel");
            longCodeLabel.__internal_setGenerationDpi(120);
            longCodeLabel.registerTranslationHandler(translationHandler);
            longCodeLabel.setScaleForResolution(true);
            longCodeLabel.setMinimumWidth(10);
            longCodeLabel.setMinimumHeight(10);
            longCodeLabel.setPaddingTop(8);
            longCodeLabel.setPaddingBottom(4);
            longCodeLabel.setText("!!Lange Coderegel:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder longCodeLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            longCodeLabelConstraints.setPosition("north");
            longCodeLabel.setConstraints(longCodeLabelConstraints);
            longCodeTablePanel.addChild(longCodeLabel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder longCodeTablePanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            longCodeTablePanel.setConstraints(longCodeTablePanelConstraints);
            mainPanel.addChild(longCodeTablePanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            this.addChild(mainPanel);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(120);
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
