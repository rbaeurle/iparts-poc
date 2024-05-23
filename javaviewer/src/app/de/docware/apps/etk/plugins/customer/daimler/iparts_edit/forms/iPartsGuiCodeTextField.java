/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSeriesCodesDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiTextFieldBackgroundToggle;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EditCharCase;
import de.docware.util.StrUtils;

import java.util.Set;

/**
 *
 */
public class iPartsGuiCodeTextField extends iPartsGuiTextFieldBackgroundToggle {

    public static final String TYPE = "ipartscodetextfield";

    public enum CODE_TEST_TYPE {
        SERIES_CODE, COLOR_CODE, X4E_CODE, PRODUCTGRP_ONLY
    }

    // Defaultwerte
    private int MAX_ERROR_MESSAGES = 5;

    // Spezifische Eigenschaften der Komponente
    // Die Instanz die die onChangeEvents Aktionen abarbeitet
    protected EventListeners eventOnChangeListeners;

    private EtkProject project;
    private iPartsDocumentationType docuType;

    private String seriesNo;
    private String productGrp;
    private String dateFrom;
    private String ausfuehrungsArt;
    private CODE_TEST_TYPE codeTestType;
    private Set<String> x4eCodeList;
    private String errorMessage;
    private String warningMessage;
    private boolean x4eWarningWasShown;
    private boolean beautified;

    /**
     * Liefert bei leerer AS-Coderegel die Konstruktions-Coderegel aus dem Feld {@code constructionCodeFieldName} zurück,
     * ansonsten den übergebenen {@code codeString}. Jeweils inkl. Trim und Strichpunkt bei leerer Coderegel.
     *
     * @param codeString
     * @param dataObject
     * @param constructionCodeFieldName
     * @return
     */
    public static String getConstCodesForEmptyASCodes(String codeString, EtkDataObject dataObject, String constructionCodeFieldName) {
        codeString = codeString.trim();

        // DAIMLER-10539: Bei leerer AS-Coderegel die Konstruktions-Coderegel setzen
        if (codeString.isEmpty()) {
            boolean oldLogLoadFieldIfNeeded = dataObject.isLogLoadFieldIfNeeded();
            try {
                dataObject.setLogLoadFieldIfNeeded(false);
                codeString = dataObject.getFieldValue(constructionCodeFieldName);
            } finally {
                dataObject.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
        }
        return DaimlerCodes.beautifyCodeString(codeString);
    }

    public iPartsGuiCodeTextField(String initialValue) {
        super();
        setType(TYPE);
        setCaseMode(CaseMode.UPPERCASE);
        this.eventOnChangeListeners = new EventListeners();
        this.beautified = true;
        this.x4eWarningWasShown = false;
        setInitialValue(initialValue);
        prepareToolTip();
    }

    public iPartsGuiCodeTextField() {
        this("");
    }

    /**
     * Setzt den Initialwert ohne die Listener zu triggern.
     *
     * @param initialValue
     */
    private void setInitialValue(String initialValue) {
        if (StrUtils.isValid(initialValue)) {
            switchOffEventListeners();
            setText(initialValue);
            switchOnEventListeners();
        }
    }

    /**
     * Überschreiben, damit auf jeden Fall nach setzen des Textes die Größe neu bestimmt wird. Sonst kann es passieren
     * dass das Textfeld zum Bearbeiten deutlich zu klein ist
     *
     * @param text
     */
    @Override
    public void setText(String text) {
        super.setText(text);
        __internal_setDimensionDirty();
    }

    /**
     * Initialisierung der Baureihe, Produktgruppe und Datum ab Werte, für eine spätere Prüfung ob es zu der
     * eingegebenen Coderegel einen gültigen Codestamm gibt. Sollten die Werte nicht initialisiert werden, dann ist die
     * Gültigkeitsprüfung immer positiv. {@link #checkInput()}
     *
     * @param project           Für Datenbankabfragen
     * @param documentationType Die Code Validierung findet aktuell nur bei DIALOG statt
     * @param seriesNumber      Baureihe; kann auch leer sein
     * @param productGroup      Produktgruppe; sollte nicht leer sein
     * @param dateFrom          Datum ab; kann auch leer sein
     * @param ausfuehrungsArt   Ausführungsart; kann auch leer sein
     * @param codeTestType      Validierungstyp; Wenn <i>null</i> wird der default verwendet (PRODUCTGRP_ONLY)
     */
    public void init(EtkProject project, iPartsDocumentationType documentationType, String seriesNumber, String productGroup, String dateFrom,
                     String ausfuehrungsArt, CODE_TEST_TYPE codeTestType) {
        boolean isLoaded = false;
        if (this.project != null) {
            isLoaded = (this.seriesNo != null) && (this.seriesNo.equals(seriesNumber));
            if (isLoaded) {
                isLoaded = this.codeTestType == CODE_TEST_TYPE.X4E_CODE;
                if (isLoaded) {
                    if (codeTestType != null) {
                        isLoaded = this.codeTestType == codeTestType;
                    }
                }
            }
            if (isLoaded) {
                if (StrUtils.isValid(ausfuehrungsArt)) {
                    if (StrUtils.isValid(this.ausfuehrungsArt)) {
                        isLoaded = this.ausfuehrungsArt.equals(ausfuehrungsArt);
                    }
                } else {
                    isLoaded = !StrUtils.isValid(this.ausfuehrungsArt);
                }
            }
        }
        if (!isLoaded) {
            this.project = project;
            this.seriesNo = seriesNumber;
            this.productGrp = productGroup;
            this.dateFrom = dateFrom;
            this.ausfuehrungsArt = ausfuehrungsArt;
            this.x4eWarningWasShown = false;
            this.docuType = documentationType;
            if (codeTestType == null) {
                this.codeTestType = CODE_TEST_TYPE.PRODUCTGRP_ONLY;
            } else {
                this.codeTestType = codeTestType;
            }
            x4eCodeList = null;

            if (codeTestType == CODE_TEST_TYPE.X4E_CODE) {
                x4eCodeList = iPartsSeriesCodesDataList.loadAllSeriesCodesForSeries(project, seriesNo, ausfuehrungsArt);
            }
        }
        prepareToolTip();
    }

    protected void prepareToolTip() {
        String msg = "";
        if ((codeTestType != null) && (codeTestType == CODE_TEST_TYPE.X4E_CODE)) {
            if ((x4eCodeList == null) || x4eCodeList.isEmpty()) {
                if (StrUtils.isValid(this.seriesNo)) {
                    msg = TranslationHandler.translate("!!Keine X4E-Stammdaten zu \"%1\" vorhanden.", this.seriesNo);
                } else {
                    msg = TranslationHandler.translate("!!Keine X4E-Stammdaten vorhanden.");
                }
            }
        }
        setTooltip(msg);
    }

    public EtkProject getProject() {
        return project;
    }

    public iPartsDocumentationType getDocumentationType() {
        return docuType;
    }

    public EditCharCase getEditCaseMode() {
        switch (getCaseMode()) {
            case UPPERCASE:
                return EditCharCase.eecUpperCase;
            case LOWERCASE:
                return EditCharCase.eecLowerCase;
        }
        return EditCharCase.eecNormal;
    }

    public void setEditCaseMode(EditCharCase editCaseMode) {
        CaseMode caseMode = CaseMode.NO_CASE_MODE;
        switch (editCaseMode) {
            case eecLowerCase:
                caseMode = CaseMode.LOWERCASE;
                break;
            case eecUpperCase:
                caseMode = CaseMode.UPPERCASE;
                break;
        }
        setCaseMode(caseMode);
    }

    @Override
    protected String controlText(String actText) {
        /* + - / ( ) ; */
        if ((actText != null) && (actText.length() > 0)) {
            StringBuilder str = new StringBuilder();
            for (int lfdNr = 0; lfdNr < actText.length(); lfdNr++) {
                char ch = actText.charAt(lfdNr);
                if (Character.isLetter(ch) || Character.isDigit(ch)) {
                    // .isLetter(ch) lässt Umlaute etc durch, deswegen nochmal gezielt abgefragt
                    if ((ch >= '0') && (ch <= '9')) {
                        str.append(ch);
                    } else if ((ch >= 'A') && (ch <= 'Z')) {
                        str.append(ch);
                    } else if ((ch >= 'a') && (ch <= 'z')) {
                        str.append(ch);
                    }
                } else {
                    // restliche erlaubte Zeichen
                    switch (ch) {
                        case '+':
                        case '-':
                        case '/':
                        case '(':
                        case ')':
                        case ';':
                            str.append(ch);
                            break;
                    }
                }
            }
            return str.toString();
        }
        return actText;
    }

    private void clearErrorsAndWarnings() {
        errorMessage = null;
        warningMessage = null;
    }

    public String getTextCaseMode() {
        String text = getText();
        if (getCaseMode() == CaseMode.LOWERCASE) {
            return text.toLowerCase();
        } else if (getCaseMode() == CaseMode.UPPERCASE) {
            return text.toUpperCase();
        }
        return text;
    }

    /**
     * Prüft ob es zu jedem einzelnen Code aus der eingegebenen Code-Bedingung Code Stammdaten gibt
     * dazu werden die Baureihe, Produktgruppe und Datum ab benötigt. Diese Werte werden in der
     * {@link #init(EtkProject, iPartsDocumentationType, String, String, String, String, CODE_TEST_TYPE)}
     * Routine initialisiert. Wurde die Initilisierung nicht durchgeführt, dann ist die Prüfung immer positiv.
     * Bei negativem Ergebnis kann die Fehlermeldung über {@link #getErrorMessage()} abgerufen werden
     *
     * @return {@code null} bei positiver Prüfung, sonst eine entsprechende Fehlermeldung
     */
    public boolean checkInput() {
        clearErrorsAndWarnings();
        String text = getText().toUpperCase();
        if (!StrUtils.isValid(text)) {
            return true;
        }

        String productGroupText = productGrp;
        if (project != null) {
            EnumValue enumValue = project.getEtkDbs().getEnumValue(iPartsConst.ENUM_KEY_PRODUCT_GROUP);
            if (enumValue != null) {
                EnumEntry enumEntry = enumValue.get(productGrp);
                if (enumEntry != null) {
                    productGroupText = enumEntry.getEnumText().getText(project.getViewerLanguage());
                }
            }
            String dateFromText = project.getVisObject().asText(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_DATEFROM, dateFrom, project.getViewerLanguage());
            StringBuilder warnings = new StringBuilder();
            StringBuilder errors = new StringBuilder();
            if (!DaimlerCodes.syntaxIsOK(text)) {
                errorMessage = TranslationHandler.translate("!!Coderegel ist syntaktisch fehlerhaft:") + "\n" + text;
                return false;
            } else {
                if (docuType.isPKWDocumentationType() && (codeTestType != null)) {
                    Set<String> codes = DaimlerCodes.getCodeSet(text); // Codestring zerlegen
                    int countWarnings = 0;
                    int countErrors = 0;
                    int checkedCodes = 0;
                    boolean x4eDataExists = true;
                    boolean codeDataExists = true;
                    boolean useError = ((codeTestType == CODE_TEST_TYPE.X4E_CODE) && (x4eCodeList != null) && !x4eCodeList.isEmpty())
                                       || (codeTestType == CODE_TEST_TYPE.PRODUCTGRP_ONLY);
                    for (String code : codes) {
                        StringBuilder tempWarningMessages = new StringBuilder();
                        boolean codeIsValid = false;
                        // laut DAIMLER wird mit einer anderen Tabelle überprüft
                        switch (codeTestType) {
                            case SERIES_CODE:
                            case COLOR_CODE:
                                if (checkedCodes == 0) {
                                    if ((project != null) && StrUtils.isValid(seriesNo, productGrp)) {
                                        // Beispiel BR-Code und Farb-Code
                                        codeIsValid = checkSeriesColorCodes(code, productGroupText, dateFromText, tempWarningMessages);
                                    } else {
                                        if (!x4eWarningWasShown) {
                                            tempWarningMessages.append(TranslationHandler.translate("!!Die Code können nicht überprüft werden."));
                                            x4eWarningWasShown = true;
                                        }
                                        codeIsValid = true;
                                        codeDataExists = false;
                                    }
                                } else {
                                    if (codeDataExists) {
                                        codeIsValid = checkSeriesColorCodes(code, productGroupText, dateFromText, tempWarningMessages);
                                    } else {
                                        codeIsValid = true;
                                    }
                                }
                                break;
                            case PRODUCTGRP_ONLY:
                                if (checkedCodes == 0) {
                                    if ((project != null) && StrUtils.isValid(productGrp)) {
                                        codeIsValid = checkProductGroupCodes(code, productGroupText, errors);
                                    } else {
                                        if (!x4eWarningWasShown) {
                                            tempWarningMessages.append(TranslationHandler.translate("!!Die Code können nicht überprüft werden."));
                                            x4eWarningWasShown = true;
                                        }
                                        codeIsValid = true;
                                        codeDataExists = false;
                                    }
                                } else {
                                    if (codeDataExists) {
                                        codeIsValid = checkProductGroupCodes(code, productGroupText, errors);
                                    } else {
                                        codeIsValid = true;
                                    }
                                }
                                break;
                            case X4E_CODE:
                                if ((checkedCodes == 0) && !useError) {
                                    if (!x4eWarningWasShown) {
                                        // diese Meldung wird unterdrückt!!
                                        if (StrUtils.isValid(seriesNo)) {
                                            tempWarningMessages.append(TranslationHandler.translate("!!Zur Baureihe: \"%1\" existieren keine X4E-Stammdaten.",
                                                                                                    seriesNo));
                                        } else {
                                            tempWarningMessages.append(TranslationHandler.translate("!!Keine Baureihe angegeben."));
                                        }
                                        tempWarningMessages.append("\n");
                                        tempWarningMessages.append(TranslationHandler.translate("!!Die Code können nicht überprüft werden."));
                                        x4eWarningWasShown = true;
                                    }
                                    x4eDataExists = false;
                                    codeIsValid = true;
                                } else {
                                    if (x4eDataExists) {
                                        codeIsValid = checkSeriesX4ECodes(code, tempWarningMessages);
                                    } else {
                                        codeIsValid = true;
                                    }
                                }
                                break;
                        }

                        checkedCodes++;
                        // Code-Id wurde überprüft, Warnungen bzw. Fehler ausgeben, falls sie ungültig war:
                        if (!codeIsValid || !tempWarningMessages.toString().isEmpty()) {
                            if (!useError) {
                                countWarnings++;
                                if (countWarnings > 0) {
                                    warnings.append("\n");
                                }
                                warnings.append(tempWarningMessages.toString());
                                if (countWarnings >= MAX_ERROR_MESSAGES) {
                                    if (checkedCodes < codes.size()) {
                                        warnings.append("\n\n");
                                        warnings.append(TranslationHandler.translate("!!(Weitere Warnungen sind möglich)"));
                                    }
                                    warningMessage = warnings.toString();
                                    return false;
                                }
                            } else {
                                countErrors++;
                                if (countErrors > 0) {
                                    errors.append("\n");
                                }
                                errors.append(tempWarningMessages.toString());
                                if (countErrors >= MAX_ERROR_MESSAGES) {
                                    if (checkedCodes < codes.size()) {
                                        errors.append("\n\n");
                                        errors.append(TranslationHandler.translate("!!(Weitere Fehler sind möglich)"));
                                    }
                                    errorMessage = errors.toString();
                                    return false;
                                }
                            }
                        }
                    }
                    if ((countWarnings != 0) || (countErrors != 0)) {
                        if (countWarnings != 0) {
                            warningMessage = warnings.toString();
                        }
                        if (countErrors != 0) {
                            errorMessage = errors.toString();
                        }
                        return false;
                    } else {
                        clearErrorsAndWarnings();
                        return true;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkSeriesX4ECodes(String code, StringBuilder tempWarningMessages) {
        boolean codeIsValid = false;
        if (x4eCodeList != null) {
            if (x4eCodeList.contains(code)) {
                codeIsValid = true;
            } else {
                if (StrUtils.isValid(ausfuehrungsArt)) {
                    tempWarningMessages.append(TranslationHandler.translate("!!Zu Code \"%1\" (Baureihe: %2, Ausführungsart: \"%3\") existieren keine X4E-Stammdaten.",
                                                                            code, seriesNo, ausfuehrungsArt));
                } else {
                    tempWarningMessages.append(TranslationHandler.translate("!!Zu Code \"%1\" (Baureihe: %2) existieren keine X4E-Stammdaten.",
                                                                            code, seriesNo));
                }
            }
        }
        return codeIsValid;
    }

    private boolean checkProductGroupCodes(String code, String productGroupText, StringBuilder messages) {
        iPartsDataCodeList dataCodeList =
                iPartsDataCodeList.loadCodeDataSortedWithoutJoinForCheckProductGroupCode(project,
                                                                                         new iPartsCodeDataId(code, "", productGrp, "", iPartsImportDataOrigin.UNKNOWN));
        if (dataCodeList.isEmpty()) {
            messages.append(TranslationHandler.translate("!!Zu Code \"%1\" (Produktgruppe: \"%2\") existieren keine Stammdaten.", code, productGroupText));
            return false;
        } else {
            return true;
        }
    }

    private boolean checkSeriesColorCodes(String code, String productGroupText, String dateFromText, StringBuilder
            tempWarningMessages) {
        iPartsDataCode foundCodeData = null;
        iPartsCodeDataId codeId = new iPartsCodeDataId(code, seriesNo, productGrp, "", iPartsImportDataOrigin.UNKNOWN);
        iPartsDataCodeList dataCodeList = iPartsDataCodeList.loadCodeDataSortedWithoutJoin(project, codeId);
        if (dataCodeList.isEmpty()) {
            tempWarningMessages.append(TranslationHandler.translate("!!Zu Code \"%1\" (Baureihe: %2, Produktgruppe: \"%3\") existieren keine Stammdaten.",
                                                                    code, seriesNo, productGroupText));
        } else {
            if (StrUtils.isValid(dateFrom)) {
                // Wenn das Datum befüllt ist zuerst mit allen verfügbaren Daten prüfen (BR, Produktgruppe, Datum ab)
                foundCodeData = iPartsDataCodeList.calculateFittingDateTimeCode(dataCodeList, dateFrom, codeId.getProductGroup(), false);
                if (foundCodeData == null) {
                    tempWarningMessages.append(TranslationHandler.translate("!!Zu Code \"%1\" (Baureihe: %2, Produktgruppe: \"%3\", Datum ab: %4) existieren keine Stammdaten.",
                                                                            code, seriesNo, productGroupText, dateFromText));
                    // Die Baureihe kann bei den Codes auch manchmal fehlen, da der DIALOG Code-Importer die Baureihe nicht
                    // kennt -> nochmal ohne Baureihe nach dem Code suchen (siehe iPartsCodeMatrixDialog.showCodeMasterData)
                    codeId = new iPartsCodeDataId(code, "", productGrp, "", iPartsImportDataOrigin.UNKNOWN);
                    dataCodeList = iPartsDataCodeList.loadCodeDataSortedWithoutJoin(project, codeId);
                    foundCodeData = iPartsDataCodeList.calculateFittingDateTimeCode(dataCodeList, dateFrom, codeId.getProductGroup(), false);
                    if (foundCodeData == null) {
                        tempWarningMessages.append(TranslationHandler.translate("!!Zu Code \"%1\" (Produktgruppe: \"%3\", Datum ab: %4) existieren keine Stammdaten.",
                                                                                code, seriesNo, productGroupText, dateFromText));
                    }
                }
            }
            if (foundCodeData == null) {
                for (iPartsDataCode dataCode : dataCodeList) {
                    if (dataCode.getAsId().getCodeId().equals(code)) {
                        foundCodeData = dataCode;
                        break;
                    }
                }
                if (foundCodeData == null) {
                    tempWarningMessages.append(TranslationHandler.translate("!!Zu Code \"%1\" (Baureihe: %2, Produktgruppe: \"%3\") existieren keine Stammdaten.",
                                                                            code, seriesNo, productGroupText));
                }
            }
        }
        return (foundCodeData != null);
    }

    /**
     * Abfrage für Fehlermeldung bei vorangegangener Gültigkeitsprüfung
     * {@link #checkInput()}
     *
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setX4eWarningWasShown(boolean x4eWarningWasShown) {
        this.x4eWarningWasShown = x4eWarningWasShown;
    }

    /**
     * Überprüft, das übergeben {@link iPartsGuiCodeTextField} Code-Feld und liefert eine Fehlermeldung, wenn der
     * Code nicht valide ist.
     *
     * @return
     */
    public boolean checkInputWithErrorMessage() {
        boolean result = true;
        if (!checkInput()) {
            // Falls es Fehler gibt, ist das Ergebnis auf jedenfall falsch
            // Bei Warnungen ist es richtig
            if (hasErrorMessage()) {
                result = false;
            }
            showErrorOrWarningMessage();
        }
        return result;
    }

    public boolean hasErrorMessage() {
        String errorMessage = getErrorMessage();
        return StrUtils.isValid(errorMessage);
    }

    public void showErrorOrWarningMessage() {
        String errorMessage = getErrorMessage();
        String warningMessage = getWarningMessage();
        if (StrUtils.isValid(errorMessage)) {
            MessageDialog.showError(errorMessage, "!!Code-Prüfung");
        } else {
            if (StrUtils.isValid(warningMessage)) {
                // Warnung anzeigen, jedoch kein Fehler
                MessageDialog.showWarning(warningMessage, "!!Code-Prüfung");
            }
        }
    }

    /**
     * Abfrage für Warnungen bei vorangegangener Gültigkeitsprüfung
     * {@link #checkInput()}
     *
     * @return
     */
    public String getWarningMessage() {
        return warningMessage;
    }

    @Override
    public String getText() {
        String textInput = super.getText();
        if (isBeautified() && StrUtils.isValid(textInput) && !StrUtils.stringEndsWith(textInput, ';', false)) {
            return textInput + ";";
        }
        return textInput;
    }

    public boolean isBeautified() {
        return beautified;
    }

    public void setBeautified(boolean beautified) {
        this.beautified = beautified;
    }
}
