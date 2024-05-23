package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.AbstractMBSDataHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handler für das Verarbeiten von SAP-MBS Code-Stammdaten
 */
public class MBSCodeDataHandler extends AbstractMBSDataHandler {

    private static final String TRIGGER_ELEMENT = "CodeMasterData";

    private static final String CODE_NUMBER = "CodeNumber";

    private Set<String> alreadyExistingCodes;
    private DictImportTextIdHelper dictHelper;
    private Map<String, EtkMultiSprache> alreadyImportedText;

    public MBSCodeDataHandler(EtkProject project, MBSDataImporter importer) {
        super(project, TRIGGER_ELEMENT, importer, "!!SAP-MBS Code-Stammdaten", TABLE_DA_CODE);
    }

    @Override
    protected void initMapping(Map<String, String> mapping) {
        // Kein Mapping benötigt, da alle Felder entweder Teil des Primärschlüssels sind,
        // oder erst nach dem Durchlaufen zusätzlicher Logik abgespeichert werden.
    }

    @Override
    protected void onStartDocument() {
        super.onStartDocument();
        alreadyExistingCodes = new HashSet<>();
        dictHelper = new DictImportTextIdHelper(getProject());
        alreadyImportedText = new HashMap<>();
    }

    /**
     * Verarbeitet einen kompletten Code-Block aus der Import XML
     */
    @Override
    protected void handleCurrentRecord() {
        String codeNumber = getCurrentRecord().get(CODE_NUMBER);
        // Check, ob es ein INSERT oder ein UPDATE ist
        if (!isValidAction(codeNumber)) {
            return;
        }
        // Die Codenummer muss mind. 3 Zeichen lang sein (Kennzeichen, Produktgruppe und der eigentliche Code)
        if (StrUtils.isValid(codeNumber) && (codeNumber.length() >= 3)) {
            CodeDataKeys codeDataKeys = new CodeDataKeys(codeNumber);
            // Check, ob es den Code samt Produktgruppe schon von einer anderen Quelle gibt. Falls ja, nicht importieren
            // Laut Story: DC_CODE_ID = 'Code' und DC_PGRP = 'Produktgruppe' und DC_SOURCE <> 'MBS'
            if (codeAlreadyExistsWithDifferentSource(codeDataKeys.getCode(), codeDataKeys.getProductGroup())) {
                return;
            }
            // Lade zum Code, Produktgruppe, Quelle "MBS" und dem Freigabedatum alle Code Datensätze
            // Laut Story: DC_CODE_ID = 'Code' und DC_PGRP = 'Produktgruppe' und DC_SOURCE = 'MBS' und DC_SDATA = 'ReleaseDateFrom'
            iPartsDataCodeList codeList = loadExistingCodes(codeDataKeys);
            if (codeList.isEmpty()) {
                // Es gibt noch keinen MBS Code -> Code samt Benennung anlegen
                handleNewCode(codeDataKeys);
            } else {
                // Es wurden MBS Datensätze gefunden. Eigentlich kann hier nur einer vorkommen, da die baureihe kein Bestandteil
                // der MBS Daten ist. Falls MBS Code existieren, Benennung und SDATB  übernehmen (sofern unterschiedlich)
                handleExistingCode(codeList);
            }
        } else {
            writeMessage(TranslationHandler.translate("!!Ungültiger Code \"%1\". Datensatz wird übersprungen!", codeNumber),
                         MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }
        clearCaches();
    }

    private void clearCaches() {
        if (alreadyImportedText.size() >= (10 * MAX_DB_OBJECTS_CACHE_SIZE)) {
            alreadyImportedText.clear();
        }
        if (alreadyExistingCodes.size() >= (10 * MAX_DB_OBJECTS_CACHE_SIZE)) {
            alreadyExistingCodes.clear();
        }
    }

    /**
     * Verarbeitet einen existierenden MBS Code. Existiert ein MBS Code schon in der Datenbank, werden Benennung und
     * Datum bis überschrieben.
     *
     * @param codeList
     */
    private void handleExistingCode(iPartsDataCodeList codeList) {
        codeList.getAsList().forEach(dataCode -> {
            // für DB formatiertes Datum bis setzen
            dataCode.setFieldValue(FIELD_DC_SDATB, getReleaseDateTo(), DBActionOrigin.FROM_EDIT);
            // Aktuellen DE Text bestimmen
            EtkMultiSprache currentDesc = dataCode.getFieldValueAsMultiLanguage(FIELD_DC_DESC);
            String currentText = currentDesc.getText(Language.DE.getCode());
            // Text aus dem Importdatensatz
            String codeDescDE = getDescription();
            if (!currentText.equals(codeDescDE)) {
                // Ist der Text unterschiedlich, wird der neue Text gesetzt
                currentDesc.setText(Language.DE, codeDescDE);
                dataCode.setFieldValueAsMultiLanguage(FIELD_DC_DESC, currentDesc, DBActionOrigin.FROM_EDIT);
                writeMessage(TranslationHandler.translate("!!Neue Benennung \"%1\" für Datensatz \"%2\" passt nicht " +
                                                          "zur Benennung in der Datenbank \"%3\". Die neue Benenung wird" +
                                                          " übernommen. Das kann Auswirkung auf die Benennungen von anderen " +
                                                          "Coden haben, die bisher den gleichen Text hatten!",
                                                          codeDescDE, dataCode.getAsId().toStringForLogMessages(),
                                                          currentText), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
            if (dataCode.isModifiedWithChildren()) {
                saveDataObject(dataCode);
            }
        });
    }

    /**
     * Verarbeitet einen neuen Code. Hierbei wird ein {@link iPartsDataCode} Objekt befüllt und gespeichert. Zusätzlich
     * wird die Benennung des Codes in die Datenbank aufgenommen.
     *
     * @param codeDataKeys
     */
    private void handleNewCode(CodeDataKeys codeDataKeys) {
        iPartsCodeDataId codeDataId = new iPartsCodeDataId(codeDataKeys.getCode(), "", codeDataKeys.getProductGroup(),
                                                           getReleaseDateFrom(), iPartsImportDataOrigin.SAP_MBS);
        iPartsDataCode dataCode = new iPartsDataCode(getProject(), codeDataId);
        dataCode.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        // für DB formatiertes Datum bis setzen
        dataCode.setFieldValue(FIELD_DC_SDATB, getReleaseDateTo(), DBActionOrigin.FROM_EDIT);
        String codeDescDE = getDescription();
        // Check, ob der Text bei einem anderen Code schon erzeugt wurde
        EtkMultiSprache multiLang = alreadyImportedText.get(codeDescDE);
        if (multiLang == null) {
            multiLang = new EtkMultiSprache();
            multiLang.setText(Language.DE, codeDescDE);
            // Text via Helper im Lexikon anlegen
            boolean dictSuccessful = dictHelper.handleSAPMBSCodeTextId(multiLang);
            handleDictionaryMessages(dictHelper);
            if (dictSuccessful) {
                alreadyImportedText.putIfAbsent(codeDescDE, multiLang);
            } else {
                writeMessage(TranslationHandler.translate("!!Fehler beim Importieren der Benennung \"%1\" für " +
                                                          "Code \"%2\" und Produktgruppe \"%3\". Benennung wird nicht importiert.",
                                                          codeDescDE, codeDataKeys.getCode(), codeDataKeys.getProductGroup()), MessageLogType.tmlMessage,
                             MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                multiLang = null;
            }
        }
        if (multiLang != null) {
            dataCode.setFieldValueAsMultiLanguage(FIELD_DC_DESC, multiLang, DBActionOrigin.FROM_EDIT);
        }
        if (dataCode.isModifiedWithChildren()) {
            saveDataObject(dataCode);
        }
    }

    /**
     * Lädt zum Code und Produktgruppe alle Datensätze bei denen die Quelle ungleich "MBS" ist.
     *
     * @param codeDataKeys
     * @return
     */
    private iPartsDataCodeList loadExistingCodes(CodeDataKeys codeDataKeys) {
        iPartsDataCodeList codeList = new iPartsDataCodeList();
        codeList.searchAndFill(getProject(), getTableName(), new String[]{ FIELD_DC_CODE_ID, FIELD_DC_PGRP, FIELD_DC_SOURCE, FIELD_DC_SDATA },
                               new String[]{ codeDataKeys.getCode(), codeDataKeys.getProductGroup(), iPartsImportDataOrigin.SAP_MBS.getOrigin(), getReleaseDateFrom() },
                               DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return codeList;
    }

    /**
     * Überprüft, ob zum Code und Produktgruppe schon ein Datensatz mit Quelle ungleich "MBS" existiert.
     *
     * @param code
     * @param productGroup
     * @return
     */
    private boolean codeAlreadyExistsWithDifferentSource(String code, String productGroup) {
        String productGroupAndCode = productGroup + code;
        if (alreadyExistingCodes.contains(productGroupAndCode)) {
            return true;
        }

        iPartsDataCodeList codeList = new iPartsDataCodeList();
        // PROVAL Code dürfen nicht berücksichtigt werden
        codeList.searchAndFill(getProject(), getTableName(), new String[]{ FIELD_DC_CODE_ID, FIELD_DC_PGRP, FIELD_DC_SOURCE, FIELD_DC_SOURCE },
                               new String[]{ code, productGroup, EtkDataObjectList.getNotWhereValue(iPartsImportDataOrigin.SAP_MBS.getOrigin()),
                                             EtkDataObjectList.getNotWhereValue(iPartsImportDataOrigin.PROVAL.getOrigin()) },
                               DBDataObjectList.LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
        if (codeList.size() > 0) {
            writeMessage(TranslationHandler.translate("!!Zum Code \"%1\" und Produktgruppe \"%2\" existieren " +
                                                      "Datensätze mit Quelle ungleich \"%3\" oder \"%4\"", code, productGroup,
                                                      iPartsImportDataOrigin.SAP_MBS.getOrigin(), iPartsImportDataOrigin.PROVAL.getOrigin()),
                         MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            alreadyExistingCodes.add(productGroupAndCode);
            return true;
        }
        return false;
    }

    /**
     * Gibt Warnungen oder Fehler aus, die während dem Lexikon-Prozess aufgetreten sind
     *
     * @param dictImportHelper
     */
    private void handleDictionaryMessages(DictImportTextIdHelper dictImportHelper) {
        if (dictImportHelper.hasWarnings()) {
            writeMessage(TranslationHandler.translate("!!Import enthielt Warnungen. Siehe Import-Log für mehr Informationen."),
                         MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            dictImportHelper.getWarnings().forEach(warning -> writeMessage(warning, MessageLogType.tmlWarning,
                                                                           MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP));
        }
        if (dictImportHelper.hasInfos()) {
            dictImportHelper.getInfos().forEach(info -> writeMessage(info, MessageLogType.tmlMessage,
                                                                     MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP));
        }
    }

    private static class CodeDataKeys {

        private String code;
        private String productGroup;

        public CodeDataKeys(String originalCodeNumber) {
            // Produktgruppe
            this.productGroup = StrUtils.copySubString(originalCodeNumber, 1, 1);
            // Code
            this.code = originalCodeNumber.substring(2);
        }

        public String getCode() {
            return code;
        }

        public String getProductGroup() {
            return productGroup;
        }
    }
}
