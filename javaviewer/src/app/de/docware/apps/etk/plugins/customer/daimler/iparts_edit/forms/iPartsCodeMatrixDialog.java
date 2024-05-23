/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.CodeHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getcode.iPartsTruckBOMFoundationWSGetCodeHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getcode.iPartsTruckBOMFoundationWSGetCodeRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWebserviceException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWebserviceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getcode.TruckBOMCodeData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getcode.TruckBOMCodeVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getcode.TruckBOMSingleCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.code.EcoCodeDataService;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.helper.BomDBServiceHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.CodeMasterData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.GetCodeMasterDataResult;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiTextArea;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableHeader;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.booleanfunctionparser.BooleanFunction;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.booleanfunctionparser.model.AbstractTerm;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;
import de.docware.util.sort.SortUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Formular zur Anzeige von Code, Code-DNF, Code-Matrix und Teil-Konjunktion aus der Code-Matrix
 */
public class iPartsCodeMatrixDialog extends AbstractJavaViewerForm implements iPartsConst {

    public static void showCodeMatrix(AbstractJavaViewerForm parentForm, String codes) {
        iPartsCodeMatrixDialog dlg = new iPartsCodeMatrixDialog(parentForm.getConnector(), parentForm, null);
        dlg.setTitle("!!Code-Matrix");
        dlg.showCode(true);
        dlg.showCodeDNF(false);
        dlg.showCodeMasterDataDefault(codes);
        dlg.showModal();
    }

    public static final String IPARTS_MENU_ITEM_SHOW_CODE_MASTER_DATA = "iPartsMenuItemShowCodeMasterData";
    public static final String CONFIG_KEY_CODE_MASTER_DATA = "Plugin/iPartsEdit/CodeMasterData";

    private String codes;
    private DwList<String> codeList;
    private Disjunction dnfCode;
    private boolean withFontColor = true;
    private DataObjectFilterGrid codeMasterDataGrid;

    /**
     * Erzeugt eine Instanz von iPartsCodeMatrixDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsCodeMatrixDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                  EtkDisplayFields displayFieldsCodeExplanation) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui(displayFieldsCodeExplanation);
    }

    public iPartsCodeMatrixDialog(iPartsCodeMatrixDialog.CodeMasterDataQuery codeMasterDataQuery, AbstractJavaViewerFormIConnector dataConnector,
                                  AbstractJavaViewerForm parentForm) {
        this(dataConnector, parentForm, null);
        showCodeMasterDataDefault(codeMasterDataQuery.getCodeString(), codeMasterDataQuery.getSeriesId().getSeriesNumber(),
                                  codeMasterDataQuery.getProductGroup(), codeMasterDataQuery.getCompareDate());
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(EtkDisplayFields displayFieldsCodeExplanation) {
        // DataGrid erzeugen
        codeMasterDataGrid = new DataObjectFilterGrid(getConnector(), this);
        if (displayFieldsCodeExplanation == null) {
            codeMasterDataGrid.setDisplayFields(getDisplayFields(CONFIG_KEY_CODE_MASTER_DATA));
        } else {
            codeMasterDataGrid.setDisplayFields(displayFieldsCodeExplanation);
        }

        // DataGrid auf untere Hälfte der Splitpane setzen
        codeMasterDataGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelCodeExplanation.addChild(codeMasterDataGrid.getGui());

        clear();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.splitpaneMain;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public boolean isWithFontColor() {
        return withFontColor;
    }

    /**
     * da schwarze Schrift auf grünem Untergrund schlecht lesbar ist, kann hier
     * die Font-Farbe auf hellgrau gesetzt werden (withFontColor = true
     *
     * @param withFontColor
     */
    public void setWithFontColor(boolean withFontColor) {
        this.withFontColor = withFontColor;
    }

    /**
     * Code Anzeige Fenster anzeigen/verstecken
     *
     * @param visible
     */
    public void showCode(boolean visible) {
        mainWindow.splitpaneCodeTop.setVisible(visible);
        int dividerSize = 0;
        if (visible && isCodeDNFVisible()) {
            dividerSize = 5;
        }
        if (!visible) {
            mainWindow.splitpaneCode.setDividerPosition(0);
        }
        mainWindow.splitpaneCode.setDividerSize(dividerSize);
        mainWindow.splitpaneMainTop.setVisible(isCodeVisible() || isCodeDNFVisible());
        dividerSize = 0;
        if (mainWindow.splitpaneMainTop.isVisible()) {
            dividerSize = 5;
        }
        mainWindow.splitpaneMain.setDividerSize(dividerSize);
    }

    /**
     * Code-DNF Anzeige Fenster anzeigen/verstecken
     *
     * @param visible
     */
    public void showCodeDNF(boolean visible) {
        mainWindow.splitpaneCodeBottom.setVisible(visible);
        mainWindow.splitpaneCodeBottom.setMinimumHeight(0);
        int dividerSize = 0;
        if (visible && isCodeVisible()) {
            dividerSize = 5;
        }
        if (!visible) {
            mainWindow.splitPane2.setDividerPosition(mainWindow.splitpaneCodeTop.getPreferredHeight());
        }
        mainWindow.splitpaneCode.setDividerSize(dividerSize);
        mainWindow.splitpaneMainTop.setVisible(isCodeVisible() || isCodeDNFVisible());
        dividerSize = 0;
        if (mainWindow.splitpaneMainTop.isVisible()) {
            dividerSize = 5;
        }
        mainWindow.splitpaneMain.setDividerSize(dividerSize);
    }

    public boolean isCodeDNFVisible() {
        return mainWindow.splitpaneCodeBottom.isVisible();
    }

    public boolean isCodeVisible() {
        return mainWindow.splitpaneCodeTop.isVisible();
    }

    public void clear() {
        getTable().removeRows();
        getConjunctionArea().clear();
        getCodeArea().clear();
        getDNFArea().clear();
        this.codes = "";
    }

    public void setCode(String codes) {
        clear();
        this.codes = codes;
        if (!this.codes.isEmpty()) {
            getCodeArea().setText(this.codes);
            if (DaimlerCodes.syntaxIsOK(this.codes)) {
                setCodeMatrixTableHeader();
                if (buildDNFCodes()) {
                    getDNFArea().setText(getDNFRepresentation());
                    fillCodeMatrix();
                } else {
                    getCodeExplanation().setText("!!Code-Regel ist fehlerhaft!");
                    getCodeExplanation().setFontStyle(DWFontStyle.BOLD);
                }
            } else {
                // Fehler in der Codebedingung -> Controls der unteren Splitpane anpassen
                getCodeExplanation().setText("!!Code-Regel ist fehlerhaft!");
                getCodeExplanation().setFontStyle(DWFontStyle.BOLD);
            }
        }
    }

    private void setCodeMatrixTableHeader() {
        codeList = new DwList<>(DaimlerCodes.getCodeSet(codes)); // Codestring zerlegen
        SortUtils.sortList(codeList, false, false, true);
        GuiTableHeader tableHeader = new GuiTableHeader();
        if (codeList.size() > 0) {
            for (String code : codeList) {
                GuiLabel label = new GuiLabel(code);
                tableHeader.addChild(label);
            }
            GuiLabel label = new GuiLabel();
            tableHeader.addChild(label);
        } else {
            GuiLabel label = new GuiLabel("!!keine Matrix anzeigbar");
            tableHeader.addChild(label);
        }
        getTable().setHeader(tableHeader);
    }

    private void fillCodeMatrix() {
        if (!dnfCode.isEmpty()) {
            for (Conjunction conjunction : dnfCode) {
                GuiTableRow row = new GuiTableRow();
                for (String code : codeList) {
                    GuiLabel label = new GuiLabel();
                    int cIndex = findConjunctionIndex(conjunction, code);
                    if (cIndex != -1) {
                        AbstractTerm term = conjunction.get(cIndex);
                        if (term.isNot()) {
                            label.setBackgroundColor(iPartsPlugin.clPlugin_iParts_CodeMatrixBackground_negative);
                            if (withFontColor) {
                                label.setForegroundColor(iPartsPlugin.clPlugin_iParts_CodeMatrixFontColor_negative);
                            }
                        } else {
                            label.setBackgroundColor(iPartsPlugin.clPlugin_iParts_CodeMatrixBackground_positive);
                            if (withFontColor) {
                                label.setForegroundColor(iPartsPlugin.clPlugin_iParts_CodeMatrixFontColor_positive);
                            }
                        }
                        label.setText(term.getStringRepresentation());
                    } else {
                        label.setBackgroundColor(AbstractGuiControl.DEFAULT_BACKGROUND_COLOR);
                    }
                    row.addChild(label);
                }
                getTable().addRow(row);
            }
        }
    }

    private int findConjunctionIndex(Conjunction conjunction, String varName) {
        for (int index = 0; index < conjunction.size(); index++) {
            AbstractTerm term = conjunction.get(index);
            if (term.getVarName().equals(varName)) {
                return index;
            }
        }
        return -1;
    }

    private String getDNFRepresentation() {
        StringBuilder str = new StringBuilder();
        java.util.List<String> childElems = new ArrayList<>();
        for (Conjunction conjunction : dnfCode) {
            String helper = DaimlerCodes.fromFunctionParser(new BooleanFunction(conjunction));
            childElems.add(StrUtils.removeAllLastCharacterIfCharacterIs(helper, ";"));
        }

        SortUtils.sortList(childElems, false, false, true);
        for (int lfdNr = 0; lfdNr < childElems.size(); lfdNr++) {
            str.append("(" + childElems.get(lfdNr) + ")");
            if (lfdNr < (childElems.size() - 1)) {
                str.append(" /\n");
            }
        }
        return str.toString();
    }

    private boolean buildDNFCodes() {
        try {
            dnfCode = DaimlerCodes.getDnfCodeOriginal(codes); // dnfCode wird nicht verändert -> kein Klon der DNF notwendig
            return true;
        } catch (BooleanFunctionSyntaxException e) {
            //e.printStackTrace();
            return false;
        }
    }

    private GuiTable getTable() {
        return mainWindow.tableCodeMatrix;
    }

    private GuiTextArea getConjunctionArea() {
        return mainWindow.textareaConjunction;
    }

    private GuiTextArea getCodeArea() {
        return mainWindow.textareaCode;
    }

    private GuiTextArea getDNFArea() {
        return mainWindow.textareaCodeDNF;
    }

    private GuiLabel getCodeExplanation() {
        return mainWindow.labelGridExplanation;
    }

    private void onTableSelectionChanged(Event event) {
        int rowIndex = getTable().getSelectedRowIndex();
        if (rowIndex >= 0) {
            String helper = DaimlerCodes.fromFunctionParser(new BooleanFunction(dnfCode.get(rowIndex)));
            getConjunctionArea().setText(helper);
        } else {
            getConjunctionArea().setText("");
        }
    }

    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<>();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_CODE_ID, false, false);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_DESC, false, false);
        defaultDisplayFields.add(displayField);
        return defaultDisplayFields;
    }

    protected EtkDisplayFields getDisplayFields(String configKey) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.load(getConfig(), configKey);

        if (displayResultFields.size() == 0) {
            List<EtkDisplayField> defaultDisplayFields = createDefaultDisplayFields();
            if (defaultDisplayFields != null) {
                for (EtkDisplayField defaultDisplayField : defaultDisplayFields) {
                    displayResultFields.addFeld(defaultDisplayField);
                }
            }
            displayResultFields.loadStandards(getConfig());
        }
        return displayResultFields;
    }

    public void showCodeMasterData(String codeString, SpecificCodeDataHandler handler) {
        setCodeAndClear(codeString);
        if (DaimlerCodes.syntaxIsOK(codeString)) {
            Set<String> codes = DaimlerCodes.getCodeSet(codeString);
            if (!codes.isEmpty()) {
                handler.handleCodeData(codes);
            }
            sortCodes();
        } else {
            showInvalidCodeRuleMessage();
        }
    }

    public void showCodeMasterDataDefault(String codeString) {
        showCodeMasterDataDefault(codeString, "", "", "");
    }

    /**
     * Zeigt die Code-Stammdaten für den übergebenen Code-String, Baureihe, Produktgruppe und Vergleichsdatum an.
     *
     * @param codeString
     * @param seriesNumber
     * @param productGroup
     * @param compareDate
     */
    public void showCodeMasterDataDefault(String codeString, String seriesNumber, String productGroup, String compareDate) {
        enableNonDefaultDisplayFields(FIELD_DC_SERIES_NO, FIELD_DC_PGRP);
        showCodeMasterData(codeString, codes -> handleDefaultCode(codes, seriesNumber, productGroup, compareDate));
    }

    /**
     * Zeigt die EDS/BCS Code-Stammdaten für den übergebenen Code-String und Vergleichsdatum an.
     *
     * @param codeString
     * @param compareDate
     */
    public void showCodeMasterDataEDS(String codeString, String productGroup, String compareDate) {
        disableNonDefaultDisplayFields(FIELD_DC_SERIES_NO);
        showCodeMasterData(codeString, codes -> handleEDSCode(codes, productGroup, compareDate));
    }

    /**
     * Zeigt die MBS Code-Stammdaten für den übergebenen Code-String und Vergleichsdatum an.
     *
     * @param codeString
     * @param compareDate
     */
    public void showCodeMasterDataMBS(String codeString, String compareDate) {
        disableNonDefaultDisplayFields(FIELD_DC_SERIES_NO, FIELD_DC_PGRP);
        showCodeMasterData(codeString, codes -> handleMBSCode(codes, compareDate));
    }

    private void setDisplayFieldVisibility(String fieldname, boolean isVisible) {
        EtkDisplayField displayField = codeMasterDataGrid.getDisplayFields().getFeldByName(TABLE_DA_CODE, fieldname);
        if (displayField != null) {
            displayField.setVisible(isVisible);
        }
    }

    private void setVisibilityForNonDefaultDiaplayFields(boolean visible, String... fieldNames) {
        if (fieldNames.length > 0) {
            for (String fieldName : fieldNames) {
                setDisplayFieldVisibility(fieldName, visible);
            }
            codeMasterDataGrid.updateGridHeader(true);
        }
    }

    /**
     * Entfernt Spalten, die nicht zu den default Code gehören
     */
    private void disableNonDefaultDisplayFields(String... fieldNames) {
        setVisibilityForNonDefaultDiaplayFields(false, fieldNames);
    }

    /**
     * Fügt Spalten hinzu, die zu den default Code gehören
     */
    private void enableNonDefaultDisplayFields(String... fieldNames) {
        setVisibilityForNonDefaultDiaplayFields(true, fieldNames);
    }

    /**
     * Verarbeitet EDS/BCS Code und erzeugt pro übergebenen Code ein {@link iPartsDataCode} Objekt samt Stammdaten
     *
     * @param codes
     * @param compareDate
     */
    private void handleEDSCode(Set<String> codes, String productGroup, String compareDate) {
        final String serviceName = iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_USE_AS_CODE_SOURCE)
                                   ? TranslationHandler.translate("!!TruckBOM.foundation Code-Beschreibungen")
                                   : TranslationHandler.translate("!!BOM-DB Code-Beschreibungen");
        if (codes.size() < 5) {
            retrieveEDSCodesFromWebservice(null, codes, productGroup, compareDate, getProject().getDBLanguage(), serviceName);
        } else {
            final EtkMessageLogForm messageLogForm = new EtkMessageLogForm(serviceName, "!!Suche Code-Beschreibungen in BOM-DB", null);
            messageLogForm.getGui().setSize(600, 250);
            messageLogForm.showMarquee();
            messageLogForm.showModal(new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    retrieveEDSCodesFromWebservice(messageLogForm, codes, productGroup, compareDate, getProject().getDBLanguage(), serviceName);
                }
            });
        }
    }

    /**
     * Verarbeitet Standard Code (nicht MBS und nicht EDS/BCS) und erzeugt pro übergebenen Code ein {@link iPartsDataCode}
     * Objekt samt Stammdaten
     *
     * @param codes
     * @param seriesNumber
     * @param productGroup
     * @param compareDate
     */
    private void handleDefaultCode(Set<String> codes, String seriesNumber, String productGroup, String compareDate) {
        // Codestring zerlegen
        String dbLanguage = getProject().getDBLanguage();
        for (String code : codes) {
            iPartsDataCode codeData = null;
            if (!productGroup.isEmpty()) { // Produktgruppe muss gesetzt sein
                codeData = iPartsDataCodeList.getFittingDateTimeCodeWithAddSearch(getProject(), dbLanguage, code, seriesNumber,
                                                                                  productGroup, compareDate);
            }
            if (codeData == null) {
                // wir erzeugen eine Dummy DataId mit dem Error-Text als Benennung
                codeData = createEmptyDataCode(code, seriesNumber, productGroup);
            }
            codeMasterDataGrid.addObjectToGrid(codeData);
        }
    }

    /**
     * Verarbeitet MBS Code und erzeugt pro übergebenen Code ein {@link iPartsDataCode} Objekt samt Stammdaten
     *
     * @param codes
     * @param compareDate
     */
    private void handleMBSCode(Set<String> codes, String compareDate) {
        // Codestring zerlegen
        String dbLanguage = getProject().getDBLanguage();
        for (String code : codes) {
            iPartsDataCode codeData = iPartsDataCodeList.getFittingDateTimeCodeMBS(getProject(), dbLanguage, code, compareDate);
            if (codeData == null) {
                // wir erzeugen eine Dummy DataId mit dem Error-Text als Benennung
                codeData = createEmptyDataCode(code);
            }
            codeMasterDataGrid.addObjectToGrid(codeData);
        }
    }

    /**
     * Zeigt eine Meldung, wenn die Code-Regel nicht gültig ist
     */
    private void showInvalidCodeRuleMessage() {
        // Fehler in der Codebedingung -> Controls der unteren Splitpane anpassen
        mainWindow.labelCodeExplanation.setText("!!Code-Regel ist fehlerhaft!");
        mainWindow.labelCodeExplanation.setFontStyle(DWFontStyle.BOLD);
        codeMasterDataGrid.getGui().setVisible(false);
        mainWindow.splitpaneMainBottom.setVisible(false);
        mainWindow.splitpaneMain.setDividerPosition(5000); // damit der Divider verschwindet
    }

    /**
     * Sortiert die angezeigten Code nach ihrer Bezeichnung
     */
    private void sortCodes() {
        // Sortieren nach der Code ID
        int codeIDFieldIndex = codeMasterDataGrid.getDisplayFields().getIndexOfFeld(TABLE_DA_CODE, FIELD_DC_CODE_ID, false);
        if (codeIDFieldIndex >= 0) {
            codeMasterDataGrid.getTable().sortRowsAccordingToColumn(codeIDFieldIndex, true);
        }
    }

    /**
     * Setzt die Code zentral und setzt die Ansicht zurück
     *
     * @param codeString
     */
    private void setCodeAndClear(String codeString) {
        setCode(codeString);
        // Controls der unteren Splitpane auf Defaults zurücksetzen
        codeMasterDataGrid.clearGrid();
        mainWindow.labelCodeExplanation.setText("!!Code-Erklärung:");
        mainWindow.labelCodeExplanation.setFontStyle(DWFontStyle.PLAIN);
        codeMasterDataGrid.getGui().setVisible(true);
    }

    /**
     * Fragt die übergebenen EDS Code vom gesetzten Webservice ab
     *
     * @param messageLogForm
     * @param codes
     * @param productGroup
     * @param compareDate
     * @param dbLanguage
     * @param serviceName
     */
    private void retrieveEDSCodesFromWebservice(EtkMessageLogForm messageLogForm, Set<String> codes, String productGroup,
                                                String compareDate, String dbLanguage, String serviceName) {
        // Check, ob die Code via TruckBOM.foundation Webservice abgefragt werden sollen
        if (iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_USE_AS_CODE_SOURCE)) {
            // TruckBOM.foundation Webservice
            getCodeFromTBFoundationWebservice(messageLogForm, codes, productGroup, serviceName);
        } else {
            // BOM-DB Webservice
            getCodeFromBOMDBWebservice(messageLogForm, codes, productGroup, compareDate, dbLanguage, serviceName);
        }
    }

    /**
     * Liefert die Stammdaten zu den übergebenen Code aus der TruckBOM.foundation via Webservice
     *
     * @param messageLogForm
     * @param codes
     * @param productGroup
     * @param serviceName
     */
    private void getCodeFromTBFoundationWebservice(EtkMessageLogForm messageLogForm, Set<String> codes, String productGroup,
                                                   String serviceName) {
        iPartsDataCodeList codeList = new iPartsDataCodeList();
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade %1...", serviceName));
        }
        try {
            // Alle Code durchlaufen und aufsammeln
            Set<String> edsCodes = new HashSet<>();
            codes.forEach(code -> {
                if (StrUtils.isValid(code)) {
                    if (code.startsWith(CodeHelper.CODE_PREFIX)) {
                        // Wenn der Code mit "I" anfängt feuern wir zunächst den Original-Code ab (und weiter unten noch den zusammengebauten Code)
                        edsCodes.add(code);
                    }

                    // Code bestehend aus "I" + Produktgruppe aufbauen und immer abfragen
                    edsCodes.add(CodeHelper.buildCodeWithProductGroupPrefix(code, productGroup));
                }
            });

            // Hier der WebService-Aufruf. Es werden nun alle Code inkl. Präfix abgefragt. Bei Code, die mit "I" beginnen
            // wird der Original-Code sowie der zusammengebaute Code abgefragt. Somit haben wir alle möglichen Variationen
            // der Code. Danach können wir uns die Code-Informationen einfach herausholen
            iPartsTruckBOMFoundationWSGetCodeRequest request = iPartsTruckBOMFoundationWSGetCodeRequest.createDefaultRequest();
            // Default Request mit allen Code befüllen
            request.addCodes(edsCodes);
            Genson genson = JSONUtils.createGenson(true);
            // Request als String
            String requestBody = genson.serialize(request);
            TruckBOMCodeData truckBOMCodeData = null;
            try {
                // Webservice anfragen und die Antwort verarbeiten
                String response = iPartsTruckBOMFoundationWebserviceUtils.getJsonFromWebservice(iPartsTruckBOMFoundationWSGetCodeHelper.WEBSERVICE_NAME,
                                                                                                requestBody, getProject());
                // Antwort via GENSON in DTO umwandeln
                truckBOMCodeData = genson.deserialize(response, TruckBOMCodeData.class);
            } catch (iPartsTruckBOMFoundationWebserviceException e) {
                MessageDialog.showError(TranslationHandler.translate("!!Fehler beim Aufruf des Webservices: %1 - %2", String.valueOf(e.getHttpResponseCode()),
                                                                     e.getMessage()));
                Logger.log(iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION, LogType.ERROR, "Error while performing TruckBOM.foundation webservice \""
                                                                                         + iPartsTruckBOMFoundationWSGetCodeHelper.WEBSERVICE_NAME + "\": "
                                                                                         + e.getHttpResponseCode()
                                                                                         + " - " + e.getMessage());
            }

            // Check, ob die Antwort gültig ist
            if ((truckBOMCodeData == null) || (truckBOMCodeData.getCode() == null) || truckBOMCodeData.getCode().isEmpty()) {
                // Falls der WS keine gültige Antwort geschickt hat, Original-Code ohne Daten anzeigen
                handleWSSendNoData(codeList, codes, messageLogForm, serviceName);
            } else {
                // Falls der WS eine gültige Antwort geschickt hat, Code samt gelieferten Daten anzeigen
                List<EDSCodeDataFromWS> codeData = truckBOMCodeData.getCode().stream()
                        .map(EDSCodeDataFromWS::new)
                        .collect(Collectors.toList());
                handleWSCodeData(codeData, productGroup, codes, codeList, serviceName, messageLogForm);
            }
        } catch (Exception e) {
            showExceptionMessage(messageLogForm, e, serviceName, codes, codeList);
        }
        showReceivedCodes(messageLogForm, codeList);
    }

    /**
     * Verarbeitet die gültigen Daten des Webservice
     *
     * @param codeData
     * @param productGroup
     * @param codes
     * @param codeList
     * @param serviceName
     * @param messageLogForm
     */
    private void handleWSCodeData(List<EDSCodeDataFromWS> codeData, String productGroup, Set<String> codes,
                                  iPartsDataCodeList codeList, String serviceName, EtkMessageLogForm messageLogForm) {
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Code vom Webservice \"%1\"" +
                                                                                    " vollständig geladen.",
                                                                                    serviceName));
            messageLogForm.closeWindow(ModalResult.OK);
        }
        // Hier die Code Informationen aufbereiten in Bezug auf die Code, wie sie in der Code-Regel stehen
        prepareDataCodeOutput(codeData, codes, codeList, productGroup);
    }

    /**
     * Verarbeitet einen nicht gültigen WS Aufruf
     *
     * @param codeList
     * @param codes
     * @param messageLogForm
     * @param serviceName
     */
    private void handleWSSendNoData(iPartsDataCodeList codeList, Set<String> codes,
                                    EtkMessageLogForm messageLogForm, String serviceName) {
        if (messageLogForm != null) {
            Session.invokeThreadSafeInSession(() -> {
                messageLogForm.disableMarquee();
                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Fehlender Inhalt beim " +
                                                                                        "Aufruf vom Webservice \"%1\".",
                                                                                        serviceName));
            });
        }
        for (String code : codes) {
            // wir erzeugen eine Dummy DataId mit dem Error-Text als Benennung
            iPartsDataCode codeData = createEmptyDataCode(code);
            codeList.add(codeData, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Liefert die Stammdaten zu den übergebenen Code aus der BOM-DB via Webservice
     *
     * @param messageLogForm
     * @param codes
     * @param compareDate
     * @param dbLanguage
     * @param serviceName
     */
    private void getCodeFromBOMDBWebservice(EtkMessageLogForm messageLogForm, Set<String> codes, String productGroup,
                                            String compareDate, String dbLanguage, String serviceName) {
        EcoCodeDataService ecoCodeDataService = new EcoCodeDataService();
        iPartsDataCodeList codeList = new iPartsDataCodeList();
        GetCodeMasterDataResult ecoCodeData;  //new GetEcoContentResult();
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade %1...", serviceName));
        }
        try {
            // Code, wie sie in der Stückliste aufsammeln (Falls da welche dabei sind, die dem "I" Präfix noch besitzen
            Set<String> edsCodes = new HashSet<>(codes);
            // Und nun die die Ausgangscode samt Präfix hinzufügen
            edsCodes.addAll(codes.stream().map(entry -> CodeHelper.buildCodeWithProductGroupPrefix(entry, productGroup)).collect(Collectors.toSet()));
            // Hier der WebService Aufruf. es werden nun alle Code, wie sie in der DB stehen und inkl. Präfix auf einmal
            // abgefragt. Somit haben wir alle möglichen Variationen der Code. Danach können wir uns die Code Informationen
            // einfach rausholen
            ecoCodeData = ecoCodeDataService.getEcoCodeData(edsCodes, compareDate, dbLanguage);
            if ((ecoCodeData == null) || (ecoCodeData.getCodeMasterDatas() == null)) {
                handleWSSendNoData(codeList, codes, messageLogForm, serviceName);
            } else {
                List<EDSCodeDataFromWS> codeData = ecoCodeData.getCodeMasterDatas().getCodeMasterData().stream()
                        .map(EDSCodeDataFromWS::new)
                        .collect(Collectors.toList());
                handleWSCodeData(codeData, productGroup, codes, codeList, serviceName, messageLogForm);
            }
        } catch (Exception e) {
            showExceptionMessage(messageLogForm, e, serviceName, codes, codeList);
        }
        showReceivedCodes(messageLogForm, codeList);
    }

    /**
     * Zeigt die übergebenen {@link iPartsDataCode} an
     *
     * @param messageLogForm
     * @param codeList
     */
    private void showReceivedCodes(EtkMessageLogForm messageLogForm, iPartsDataCodeList codeList) {
        Runnable showReceivedCodesRunnable = () -> {
            for (iPartsDataCode codeData : codeList) {
                codeMasterDataGrid.addObjectToGrid(codeData);
            }
        };
        if (messageLogForm != null) {
            Session.get().invokeThreadSafeWithThread(showReceivedCodesRunnable);
        } else {
            showReceivedCodesRunnable.run();
        }
    }

    private void showExceptionMessage(EtkMessageLogForm messageLogForm, Exception e, String serviceName,
                                      Set<String> codes, iPartsDataCodeList codeList) {
        Logger.logExceptionWithoutThrowing(iPartsEditPlugin.LOG_CHANNEL_BOM_DB_SOAP_WEBSERVICE, LogType.ERROR, e);
        if (messageLogForm != null) {
            messageLogForm.disableMarquee();
            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Fehler beim Aufruf vom Webservice \"%1\":%2",
                                                                                    serviceName,
                                                                                    "\n" + e.getMessage()));
        } else {
            final EtkMessageLogForm helperMessageLogForm = new EtkMessageLogForm(serviceName, "!!Suche Code-Beschreibungen in BOM-DB", null);
            helperMessageLogForm.getGui().setSize(600, 250);
            helperMessageLogForm.showModal(new FrameworkRunnable() {

                @Override
                public void run(FrameworkThread thread) {
                    helperMessageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade %1...", serviceName));
                    helperMessageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Fehler beim Aufruf vom Webservice \"%1\":%2",
                                                                                                  serviceName,
                                                                                                  "\n" + e.getMessage()));
                }
            });
            for (String code : codes) {
                // wir erzeugen eine Dummy DataId mit dem Error-Text als Benennung
                iPartsDataCode codeData = createEmptyDataCode(code);
                codeList.add(codeData, DBActionOrigin.FROM_DB);
            }
        }
    }

    private void prepareDataCodeOutput(List<EDSCodeDataFromWS> codeMasterData, Set<String> initialCodes,
                                       iPartsDataCodeList codeList, String productGroup) {
        for (EDSCodeDataFromWS codeDataFromWS : codeMasterData) {
            // Wir wissen, dass die Code vom Webservice immer mit dem "I" + Produktgruppe - Präfix ausgeliefert werden
            String wsCode = codeDataFromWS.getCode();
            // Code von Webservice zerlegen
            CodeHelper.CodeWithPrefix codeKey = CodeHelper.createCodeInfoFromOriginalCode(wsCode);
            // Check, ob Produktgruppe und Code extrahiert werden konnten und ob die Produktgruppe gleich der Vorgabe ist.
            // Falls nicht, weiter mit dem nächsten Code
            if (!codeKey.isCodeWithValidPrefix() || !productGroup.equals(codeKey.getProductGroup())) {
                continue;
            }
            // Check, ob der Code überhaupt angefragt wurde
            boolean existsWithPrefix = initialCodes.contains(wsCode); // Code steht mit Prefix in der Code-Regel
            String code = codeKey.getCode();
            boolean existsWithoutPrefix = initialCodes.contains(code); // Code steht ohne Prefix in der Code-Regel
            // Er wurde nur angefragt, wenn er mit oder ohne Präfix in dem Set der Ausgangscode existiert
            if (!existsWithPrefix && !existsWithoutPrefix) {
                continue;
            }

            // Hier jetzt die Form bestimmen, in der der Code auch wirklich in der Code-Regel auftaucht
            String actualCode = existsWithPrefix ? wsCode : code;
            iPartsCodeDataId codeDataId = new iPartsCodeDataId(actualCode, "", productGroup, codeDataFromWS.getDateFrom(), iPartsImportDataOrigin.EDS);
            iPartsDataCode codeData = new iPartsDataCode(getProject(), codeDataId);
            codeData.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);

            codeData.setFieldValue(FIELD_DC_SDATB, codeDataFromWS.getDateTo(), DBActionOrigin.FROM_DB);
            codeData.setFieldValueAsMultiLanguage(FIELD_DC_DESC, codeDataFromWS.getDescription(), DBActionOrigin.FROM_DB);
            codeList.add(codeData, DBActionOrigin.FROM_DB);
            initialCodes.remove(actualCode);
        }
        if (!initialCodes.isEmpty()) {
            for (String edsCode : initialCodes) {
                String code = CodeHelper.convertCodeWithPrefixToiPartsCode(edsCode);
                iPartsDataCode codeData = createEmptyDataCode(code, productGroup);
                codeList.add(codeData, DBActionOrigin.FROM_DB);
            }
        }
    }

    private iPartsDataCode createEmptyDataCode(String code) {
        return createEmptyDataCode(code, "", "");
    }

    private iPartsDataCode createEmptyDataCode(String code, String productGroup) {
        return createEmptyDataCode(code, "", productGroup);
    }

    private iPartsDataCode createEmptyDataCode(String code, String seriesNumber, String productGroup) {
        // wir erzeugen eine Dummy DataId mit dem Error-Text als Benennung
        iPartsCodeDataId codeDataId = new iPartsCodeDataId(code, seriesNumber, productGroup, "", iPartsImportDataOrigin.UNKNOWN);
        iPartsDataCode codeData = new iPartsDataCode(getProject(), codeDataId);
        codeData.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        codeData.setMultLangValue(getProject().getDBLanguage(), FIELD_DC_DESC,
                                  TranslationHandler.translate("!!Code nicht gefunden"),
                                  null, null, false, DBActionOrigin.FROM_DB);
        return codeData;
    }

    public static GuiMenuItem createMenuItem(final CodeMasterDataCallback callback, final AbstractJavaViewerForm parentForm) {
        return createMenuItem(callback, parentForm, RELATED_INFO_CODE_MASTER_DATA_TEXT);
    }

    public static GuiMenuItem createMenuItem(final CodeMasterDataCallback callback, final AbstractJavaViewerForm parentForm, String menuText) {
        GuiMenuItem menuItem = new GuiMenuItem();
        menuItem.setUserObject(IPARTS_MENU_ITEM_SHOW_CODE_MASTER_DATA);
        menuItem.setName(IPARTS_MENU_ITEM_SHOW_CODE_MASTER_DATA);
        menuItem.setText(menuText);
        menuItem.setIcon(EditDefaultImages.edit_code.getImage());
        menuItem.addEventListener(new EventListener(de.docware.framework.modules.gui.event.Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                CodeMasterDataQuery codeMasterDataQuery = callback.getCodeMasterDataQuery();
                if (codeMasterDataQuery == null) {
                    codeMasterDataQuery = new CodeMasterDataQuery();
                }
                iPartsCodeMatrixDialog codeMasterDataForm = new iPartsCodeMatrixDialog(codeMasterDataQuery,
                                                                                       parentForm.getConnector(),
                                                                                       parentForm);
                codeMasterDataForm.showCodeDNF(false);
                codeMasterDataForm.showModal();
            }
        });

        return menuItem;
    }

    /**
     * Callback zur Bestimmung der Daten für die Anzeige der Code-Stammdaten.
     */
    public interface CodeMasterDataCallback {

        CodeMasterDataQuery getCodeMasterDataQuery();
    }

    /**
     * Interface zum Verarbeiten von spezifischen Code (MBS, EDS/BCS und Standard)
     */
    interface SpecificCodeDataHandler {

        void handleCodeData(Set<String> codes);
    }


    /**
     * Abfrageinformationen zur Bestimmung der Daten für die Anzeige der Code-Stammdaten.
     */
    public static class CodeMasterDataQuery {

        private String codeString;
        private iPartsSeriesId seriesId;
        private String productGroup;
        private String compareDate;

        public CodeMasterDataQuery(String codeString, iPartsSeriesId seriesId, String productGroup, String compareDate) {
            this.codeString = codeString;
            this.seriesId = seriesId;
            this.productGroup = productGroup;
            this.compareDate = compareDate;
        }

        public CodeMasterDataQuery() {
            this("", new iPartsSeriesId(), "", "");
        }

        public String getCodeString() {
            return codeString;
        }

        public void setCodeString(String codeString) {
            this.codeString = codeString;
        }

        public iPartsSeriesId getSeriesId() {
            return seriesId;
        }

        public void setSeriesId(iPartsSeriesId seriesId) {
            this.seriesId = seriesId;
        }

        public String getProductGroup() {
            return productGroup;
        }

        public void setProductGroup(String productGroup) {
            this.productGroup = productGroup;
        }

        public String getCompareDate() {
            return compareDate;
        }

        public void setCompareDate(String compareDate) {
            this.compareDate = compareDate;
        }
    }

    /**
     * EDS Code Informationen, die von einem Webservice geliefert werden
     */
    private class EDSCodeDataFromWS {

        private final String code;
        private final String dateFrom;
        private final String dateTo;
        private EtkMultiSprache description = new EtkMultiSprache();

        /**
         * Konstruktor für Daten vom BOM-DB Webservice
         *
         * @param edsCodeData
         */
        public EDSCodeDataFromWS(CodeMasterData edsCodeData) {
            code = edsCodeData.getCode();
            dateFrom = BomDBServiceHelper.convertXMLCalendarToDbString(edsCodeData.getReleaseDateFrom());
            dateTo = BomDBServiceHelper.convertXMLCalendarToDbString(edsCodeData.getReleaseDateTo());
            description.setText(getProject().getDBLanguage(), edsCodeData.getDescription());
        }

        /**
         * Konstruktor für Daten vom TruckBOM.foundation Webservice
         *
         * @param tbfCodeData
         */
        public EDSCodeDataFromWS(TruckBOMSingleCode tbfCodeData) {
            code = tbfCodeData.getIdentifier();
            dateFrom = "";
            dateTo = "";
            Optional<TruckBOMCodeVersion> newestVersion = tbfCodeData.getNewestCodeVersion();
            newestVersion.ifPresent(codeVersion -> {
                if ((codeVersion.getNomenclature() != null) && !codeVersion.getNomenclature().isEmpty()) {
                    description = codeVersion.getNomenclatureAsMultiLangObject();
                }
            });
        }

        public String getCode() {
            return code;
        }

        public String getDateFrom() {
            return dateFrom;
        }

        public String getDateTo() {
            return dateTo;
        }

        public EtkMultiSprache getDescription() {
            return description;
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneMainTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitPane2;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitPane2Top;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneCode;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneCodeTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCode;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneCode;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaCode;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneCodeBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCodeDNF;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneCodeDNF;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaCodeDNF;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitPane2Bottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCodeExplanation;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelCodeExplanation;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneMainBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable tableCodeMatrix;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelGridExplanation;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelConjunction;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelConjunctionString;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneConjunction;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaConjunction;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setName("Code-Stammdaten");
            this.setHeight(800);
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
            title.setTitle("Code-Stammdaten");
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
            panelMain.setPaddingLeft(8);
            panelMain.setPaddingRight(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitpaneMain = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneMain.setName("splitpaneMain");
            splitpaneMain.__internal_setGenerationDpi(96);
            splitpaneMain.registerTranslationHandler(translationHandler);
            splitpaneMain.setScaleForResolution(true);
            splitpaneMain.setMinimumWidth(10);
            splitpaneMain.setMinimumHeight(10);
            splitpaneMain.setHorizontal(false);
            splitpaneMain.setDividerPosition(279);
            splitpaneMainTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneMainTop.setName("splitpaneMainTop");
            splitpaneMainTop.__internal_setGenerationDpi(96);
            splitpaneMainTop.registerTranslationHandler(translationHandler);
            splitpaneMainTop.setScaleForResolution(true);
            splitpaneMainTop.setMinimumWidth(0);
            splitpaneMainTop.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneMainTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneMainTop.setLayout(splitpaneMainTopLayout);
            splitPane2 = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitPane2.setName("splitPane2");
            splitPane2.__internal_setGenerationDpi(96);
            splitPane2.registerTranslationHandler(translationHandler);
            splitPane2.setScaleForResolution(true);
            splitPane2.setMinimumWidth(10);
            splitPane2.setMinimumHeight(10);
            splitPane2.setHorizontal(false);
            splitPane2.setDividerPosition(137);
            splitPane2Top = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitPane2Top.setName("splitPane2Top");
            splitPane2Top.__internal_setGenerationDpi(96);
            splitPane2Top.registerTranslationHandler(translationHandler);
            splitPane2Top.setScaleForResolution(true);
            splitPane2Top.setMinimumWidth(0);
            splitPane2Top.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitPane2TopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitPane2Top.setLayout(splitPane2TopLayout);
            splitpaneCode = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneCode.setName("splitpaneCode");
            splitpaneCode.__internal_setGenerationDpi(96);
            splitpaneCode.registerTranslationHandler(translationHandler);
            splitpaneCode.setScaleForResolution(true);
            splitpaneCode.setMinimumWidth(10);
            splitpaneCode.setMinimumHeight(10);
            splitpaneCode.setHorizontal(false);
            splitpaneCode.setDividerPosition(67);
            splitpaneCodeTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneCodeTop.setName("splitpaneCodeTop");
            splitpaneCodeTop.__internal_setGenerationDpi(96);
            splitpaneCodeTop.registerTranslationHandler(translationHandler);
            splitpaneCodeTop.setScaleForResolution(true);
            splitpaneCodeTop.setMinimumWidth(0);
            splitpaneCodeTop.setMinimumHeight(0);
            splitpaneCodeTop.setPaddingTop(4);
            splitpaneCodeTop.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneCodeTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneCodeTop.setLayout(splitpaneCodeTopLayout);
            labelCode = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCode.setName("labelCode");
            labelCode.__internal_setGenerationDpi(96);
            labelCode.registerTranslationHandler(translationHandler);
            labelCode.setScaleForResolution(true);
            labelCode.setMinimumWidth(10);
            labelCode.setMinimumHeight(10);
            labelCode.setPaddingBottom(4);
            labelCode.setText("!!Code-Regel:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelCodeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelCodeConstraints.setPosition("north");
            labelCode.setConstraints(labelCodeConstraints);
            splitpaneCodeTop.addChild(labelCode);
            scrollpaneCode = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneCode.setName("scrollpaneCode");
            scrollpaneCode.__internal_setGenerationDpi(96);
            scrollpaneCode.registerTranslationHandler(translationHandler);
            scrollpaneCode.setScaleForResolution(true);
            scrollpaneCode.setMinimumWidth(10);
            scrollpaneCode.setMinimumHeight(10);
            scrollpaneCode.setBorderWidth(1);
            scrollpaneCode.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textareaCode = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaCode.setName("textareaCode");
            textareaCode.__internal_setGenerationDpi(96);
            textareaCode.registerTranslationHandler(translationHandler);
            textareaCode.setScaleForResolution(true);
            textareaCode.setMinimumWidth(200);
            textareaCode.setMinimumHeight(50);
            textareaCode.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaCodeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaCode.setConstraints(textareaCodeConstraints);
            scrollpaneCode.addChild(textareaCode);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneCodeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpaneCode.setConstraints(scrollpaneCodeConstraints);
            splitpaneCodeTop.addChild(scrollpaneCode);
            splitpaneCode.addChild(splitpaneCodeTop);
            splitpaneCodeBottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneCodeBottom.setName("splitpaneCodeBottom");
            splitpaneCodeBottom.__internal_setGenerationDpi(96);
            splitpaneCodeBottom.registerTranslationHandler(translationHandler);
            splitpaneCodeBottom.setScaleForResolution(true);
            splitpaneCodeBottom.setMinimumWidth(0);
            splitpaneCodeBottom.setMinimumHeight(0);
            splitpaneCodeBottom.setPaddingTop(4);
            splitpaneCodeBottom.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneCodeBottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneCodeBottom.setLayout(splitpaneCodeBottomLayout);
            labelCodeDNF = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCodeDNF.setName("labelCodeDNF");
            labelCodeDNF.__internal_setGenerationDpi(96);
            labelCodeDNF.registerTranslationHandler(translationHandler);
            labelCodeDNF.setScaleForResolution(true);
            labelCodeDNF.setMinimumWidth(10);
            labelCodeDNF.setMinimumHeight(10);
            labelCodeDNF.setPaddingBottom(4);
            labelCodeDNF.setText("!!Code-DNF:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelCodeDNFConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelCodeDNFConstraints.setPosition("north");
            labelCodeDNF.setConstraints(labelCodeDNFConstraints);
            splitpaneCodeBottom.addChild(labelCodeDNF);
            scrollpaneCodeDNF = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneCodeDNF.setName("scrollpaneCodeDNF");
            scrollpaneCodeDNF.__internal_setGenerationDpi(96);
            scrollpaneCodeDNF.registerTranslationHandler(translationHandler);
            scrollpaneCodeDNF.setScaleForResolution(true);
            scrollpaneCodeDNF.setMinimumWidth(10);
            scrollpaneCodeDNF.setMinimumHeight(10);
            scrollpaneCodeDNF.setBorderWidth(1);
            scrollpaneCodeDNF.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textareaCodeDNF = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaCodeDNF.setName("textareaCodeDNF");
            textareaCodeDNF.__internal_setGenerationDpi(96);
            textareaCodeDNF.registerTranslationHandler(translationHandler);
            textareaCodeDNF.setScaleForResolution(true);
            textareaCodeDNF.setMinimumWidth(200);
            textareaCodeDNF.setMinimumHeight(50);
            textareaCodeDNF.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaCodeDNFConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaCodeDNF.setConstraints(textareaCodeDNFConstraints);
            scrollpaneCodeDNF.addChild(textareaCodeDNF);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneCodeDNFConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpaneCodeDNF.setConstraints(scrollpaneCodeDNFConstraints);
            splitpaneCodeBottom.addChild(scrollpaneCodeDNF);
            splitpaneCode.addChild(splitpaneCodeBottom);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneCodeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneCode.setConstraints(splitpaneCodeConstraints);
            splitPane2Top.addChild(splitpaneCode);
            splitPane2.addChild(splitPane2Top);
            splitPane2Bottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitPane2Bottom.setName("splitPane2Bottom");
            splitPane2Bottom.__internal_setGenerationDpi(96);
            splitPane2Bottom.registerTranslationHandler(translationHandler);
            splitPane2Bottom.setScaleForResolution(true);
            splitPane2Bottom.setMinimumWidth(0);
            splitPane2Bottom.setMinimumHeight(0);
            splitPane2Bottom.setPaddingTop(8);
            splitPane2Bottom.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutBorder splitPane2BottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitPane2Bottom.setLayout(splitPane2BottomLayout);
            labelCodeExplanation = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCodeExplanation.setName("labelCodeExplanation");
            labelCodeExplanation.__internal_setGenerationDpi(96);
            labelCodeExplanation.registerTranslationHandler(translationHandler);
            labelCodeExplanation.setScaleForResolution(true);
            labelCodeExplanation.setMinimumWidth(10);
            labelCodeExplanation.setMinimumHeight(10);
            labelCodeExplanation.setPaddingBottom(4);
            labelCodeExplanation.setText("!!Code-Erklärung:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelCodeExplanationConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelCodeExplanationConstraints.setPosition("north");
            labelCodeExplanation.setConstraints(labelCodeExplanationConstraints);
            splitPane2Bottom.addChild(labelCodeExplanation);
            panelCodeExplanation = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelCodeExplanation.setName("panelCodeExplanation");
            panelCodeExplanation.__internal_setGenerationDpi(96);
            panelCodeExplanation.registerTranslationHandler(translationHandler);
            panelCodeExplanation.setScaleForResolution(true);
            panelCodeExplanation.setMinimumWidth(10);
            panelCodeExplanation.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelCodeExplanationLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelCodeExplanation.setLayout(panelCodeExplanationLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelCodeExplanationConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelCodeExplanation.setConstraints(panelCodeExplanationConstraints);
            splitPane2Bottom.addChild(panelCodeExplanation);
            splitPane2.addChild(splitPane2Bottom);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitPane2Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitPane2.setConstraints(splitPane2Constraints);
            splitpaneMainTop.addChild(splitPane2);
            splitpaneMain.addChild(splitpaneMainTop);
            splitpaneMainBottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneMainBottom.setName("splitpaneMainBottom");
            splitpaneMainBottom.__internal_setGenerationDpi(96);
            splitpaneMainBottom.registerTranslationHandler(translationHandler);
            splitpaneMainBottom.setScaleForResolution(true);
            splitpaneMainBottom.setMinimumWidth(0);
            splitpaneMainBottom.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneMainBottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneMainBottom.setLayout(splitpaneMainBottomLayout);
            panelGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGrid.setName("panelGrid");
            panelGrid.__internal_setGenerationDpi(96);
            panelGrid.registerTranslationHandler(translationHandler);
            panelGrid.setScaleForResolution(true);
            panelGrid.setMinimumWidth(10);
            panelGrid.setMinimumHeight(10);
            panelGrid.setPaddingTop(4);
            panelGrid.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelGrid.setLayout(panelGridLayout);
            scrollpaneGrid = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneGrid.setName("scrollpaneGrid");
            scrollpaneGrid.__internal_setGenerationDpi(96);
            scrollpaneGrid.registerTranslationHandler(translationHandler);
            scrollpaneGrid.setScaleForResolution(true);
            scrollpaneGrid.setMinimumWidth(10);
            scrollpaneGrid.setMinimumHeight(10);
            tableCodeMatrix = new de.docware.framework.modules.gui.controls.table.GuiTable();
            tableCodeMatrix.setName("tableCodeMatrix");
            tableCodeMatrix.__internal_setGenerationDpi(96);
            tableCodeMatrix.registerTranslationHandler(translationHandler);
            tableCodeMatrix.setScaleForResolution(true);
            tableCodeMatrix.setMinimumWidth(10);
            tableCodeMatrix.setMinimumHeight(10);
            tableCodeMatrix.setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);
            tableCodeMatrix.addEventListener(new de.docware.framework.modules.gui.event.EventListener("tableSelectionEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onTableSelectionChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tableCodeMatrixConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tableCodeMatrix.setConstraints(tableCodeMatrixConstraints);
            scrollpaneGrid.addChild(tableCodeMatrix);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpaneGrid.setConstraints(scrollpaneGridConstraints);
            panelGrid.addChild(scrollpaneGrid);
            labelGridExplanation = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelGridExplanation.setName("labelGridExplanation");
            labelGridExplanation.__internal_setGenerationDpi(96);
            labelGridExplanation.registerTranslationHandler(translationHandler);
            labelGridExplanation.setScaleForResolution(true);
            labelGridExplanation.setMinimumWidth(10);
            labelGridExplanation.setMinimumHeight(10);
            labelGridExplanation.setPaddingBottom(4);
            labelGridExplanation.setText("!!Code-Matrix:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelGridExplanationConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelGridExplanationConstraints.setPosition("north");
            labelGridExplanation.setConstraints(labelGridExplanationConstraints);
            panelGrid.addChild(labelGridExplanation);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelGrid.setConstraints(panelGridConstraints);
            splitpaneMainBottom.addChild(panelGrid);
            panelConjunction = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelConjunction.setName("panelConjunction");
            panelConjunction.__internal_setGenerationDpi(96);
            panelConjunction.registerTranslationHandler(translationHandler);
            panelConjunction.setScaleForResolution(true);
            panelConjunction.setMinimumWidth(10);
            panelConjunction.setMinimumHeight(10);
            panelConjunction.setPaddingTop(4);
            panelConjunction.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelConjunctionLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelConjunction.setLayout(panelConjunctionLayout);
            labelConjunctionString = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelConjunctionString.setName("labelConjunctionString");
            labelConjunctionString.__internal_setGenerationDpi(96);
            labelConjunctionString.registerTranslationHandler(translationHandler);
            labelConjunctionString.setScaleForResolution(true);
            labelConjunctionString.setMinimumWidth(10);
            labelConjunctionString.setMinimumHeight(10);
            labelConjunctionString.setPaddingBottom(4);
            labelConjunctionString.setText("!!Teil-Konjunktion:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelConjunctionStringConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelConjunctionStringConstraints.setPosition("north");
            labelConjunctionString.setConstraints(labelConjunctionStringConstraints);
            panelConjunction.addChild(labelConjunctionString);
            scrollpaneConjunction = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneConjunction.setName("scrollpaneConjunction");
            scrollpaneConjunction.__internal_setGenerationDpi(96);
            scrollpaneConjunction.registerTranslationHandler(translationHandler);
            scrollpaneConjunction.setScaleForResolution(true);
            scrollpaneConjunction.setMinimumWidth(10);
            scrollpaneConjunction.setMinimumHeight(10);
            scrollpaneConjunction.setBorderWidth(1);
            scrollpaneConjunction.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textareaConjunction = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaConjunction.setName("textareaConjunction");
            textareaConjunction.__internal_setGenerationDpi(96);
            textareaConjunction.registerTranslationHandler(translationHandler);
            textareaConjunction.setScaleForResolution(true);
            textareaConjunction.setMinimumWidth(200);
            textareaConjunction.setEditable(false);
            textareaConjunction.setLineWrap(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaConjunctionConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaConjunction.setConstraints(textareaConjunctionConstraints);
            scrollpaneConjunction.addChild(textareaConjunction);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneConjunctionConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpaneConjunction.setConstraints(scrollpaneConjunctionConstraints);
            panelConjunction.addChild(scrollpaneConjunction);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelConjunctionConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelConjunctionConstraints.setPosition("south");
            panelConjunction.setConstraints(panelConjunctionConstraints);
            splitpaneMainBottom.addChild(panelConjunction);
            splitpaneMain.addChild(splitpaneMainBottom);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneMain.setConstraints(splitpaneMainConstraints);
            panelMain.addChild(splitpaneMain);
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