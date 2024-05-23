/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsIdentRange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsProductRemarkLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto-Product-Select Import TAL47S (ehemals Auto-Cat-Select)
 * Importer für die Daten, die zur Produktabgrenzung bei Baumuster, die in mehreren Produkten vorkommen, herangezogen werden.
 */
public class MADTal47SImporter extends AbstractSAXPushDataImporter implements iPartsConst, EtkDbConst {

    final static String PROD_NUMBER = "<applications_list_control><item>:catalogue";
    final static String PROD_REMARK = "<applications_list_control><item><remark>";
    final static String PROD_REMARK_LANGUGE = "<applications_list_control><item><remark>:lang";
    final static String PROD_REMARK_TEXT = "<applications_list_control><item><remark>:text";
    final static String PROD_MODEL_DATA = "<applications_list_control><item><einsatz>";
    final static String PROD_MODEL_CODE = "<applications_list_control><item><einsatz>:code";
    final static String PROD_MODEL_FROM_IDENT = "<applications_list_control><item><einsatz>:from_ident";
    final static String PROD_MODEL_TO_IDENT = "<applications_list_control><item><einsatz>:upto_ident";

    private HashMap<String, String> mappingProdModelData;
    private DictTextKindTypes importType = DictTextKindTypes.PRODUCT_REMARKS;
    private Map<String, EtkMultiSprache> dictCache;
    private AutoProdData currentAutoProdData;


    private boolean importToDB = true;
    private boolean doBufferSave = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MADTal47SImporter(EtkProject project) {
        super(project, "!!MAD TAL47S", null,
              new FilesImporterFileListType(TABLE_DA_PRODUCT, "!!MAD Auto-Product-Select", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        tableName = TABLE_DA_PRODUCT;
        mappingProdModelData = new HashMap<String, String>();
        mappingProdModelData.put(FIELD_DP_APS_CODE, PROD_MODEL_CODE);
        mappingProdModelData.put(FIELD_DP_APS_FROM_IDENTS, PROD_MODEL_FROM_IDENT);
        mappingProdModelData.put(FIELD_DP_APS_TO_IDENTS, PROD_MODEL_TO_IDENT);
        dictCache = new HashMap<String, EtkMultiSprache>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   importType)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }


    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
        currentAutoProdData = new AutoProdData();
    }


    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Produkt Element
        if (importRec.containsKey(PROD_NUMBER)) {
            // Produkt Element. Da diese Information am Ende des Tags kommt, muss hier auch gleich das Produkt
            // gespeichert werden
            String productNo = importRec.get(PROD_NUMBER);
            currentAutoProdData.setProductId(new iPartsProductId(productNo));
            saveProduct();
            currentAutoProdData = new AutoProdData();
        } else if (importRec.containsKey(PROD_REMARK_LANGUGE)) {
            // Es könnten weitere Sprachen existieren, daher hier nicht aussteigen wenn ein gecachtes EtkMultiSprach
            // Objekt zugewiesen wurde
            String language = importRec.get(PROD_REMARK_LANGUGE);
            String text = importRec.get(PROD_REMARK_TEXT);
            iPartsProductRemarkLanguageDefs currentLanguage = iPartsProductRemarkLanguageDefs.getTypeByLangValue(language);
            if (currentLanguage == iPartsProductRemarkLanguageDefs.PROD_REM_DE) {
                // Schlüssel für den EtkMultiSprach Cache ist der deutsche Text
                EtkMultiSprache cachedMultiText = dictCache.get(text);
                if (cachedMultiText != null) {
                    // EtkMultiSprach Objekt mit deutschem Text gefunden
                    if (currentAutoProdData.getMultiText().isEmpty()) {
                        // Hat der aktuelle Datensatz noch ein leeres Multisprach Objekt -> Setze das aus dem Cache
                        currentAutoProdData.setMultiText(cachedMultiText);
                    } else {
                        // Hat der aktuelle Datensatz schon Daten im EtkMultiSprach (z.B. Datensatz beginnt nicht mit
                        // dem deutschen Text) -> Füge die gecachten Daten zu den aktuellen Daten hinzu
                        currentAutoProdData.getMultiText().assignData(cachedMultiText);
                    }
                }
            }
            currentAutoProdData.getMultiText().setText(currentLanguage.getDbValue(), text);
        } else if (importRec.containsKey(PROD_MODEL_DATA)) {
            // Codebedingung und Idents
            currentAutoProdData.setCodeRule(importRec.get(PROD_MODEL_CODE));
            currentAutoProdData.addIdents(importRec.get(PROD_MODEL_FROM_IDENT), importRec.get(PROD_MODEL_TO_IDENT), recordNo);
        } else {
            reduceTagCount();
        }

    }

    /**
     * Speichert das angepasste Produkt in der DB. Dies umfasst das Suche bzw. Anlegen der Bemerkungs-Texte sowie das
     * Befüllen des Produkts mit den Identsangaben
     */
    private void saveProduct() {
        iPartsProductId productId = currentAutoProdData.getProductId();
        if (productId.isValidId()) {
            iPartsDataProduct dataProduct = new iPartsDataProduct(getProject(), productId);
            if (!dataProduct.existsInDB()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen: Produkt \"%2\" existiert nicht in der Datenbank",
                                                            String.valueOf(importedRecords + 1), productId.getProductNumber()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return;
            }
            EtkMultiSprache multiText = currentAutoProdData.getMultiText();
            if (multiText != null) {
                DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
                // Dictionary Eintrag anlegen bzw aktualisieren
                boolean dictSuccessful = importHelper.handleDictTextIdForProductRemarks(importType, multiText, multiText.getTextId(),
                                                                                        DictHelper.getAutoProductRemarksSource(),
                                                                                        TableAndFieldName.make(tableName, FIELD_DP_APS_REMARK));
                if (!dictSuccessful || importHelper.hasWarnings()) {
                    //Fehler beim Dictionary Eintrag
                    for (String str : importHelper.getWarnings()) {
                        // Anzeige im LogFenster wurde ausgeschaltet, da sonst zu viele Ausgaben
                        getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(importedRecords + 1), str),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }

                    if (!dictSuccessful) {
                        // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                        cancelImport();
                    }
                    reduceTagCount();
                    return;
                }
                dictCache.put(multiText.getText(Language.DE.getCode()), multiText);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Fehler beim Anlegen der Auto-Product-Select Bemerkungen für das Produkt \"%1\".", productId.getProductNumber()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                return;
            }
            dataProduct.setFieldValueAsMultiLanguage(FIELD_DP_APS_REMARK, multiText, DBActionOrigin.FROM_EDIT);
            dataProduct.setFieldValue(FIELD_DP_APS_CODE, currentAutoProdData.getCodeRule(), DBActionOrigin.FROM_EDIT);
            fillProductWithMultipleIdents(dataProduct);
            if (importToDB) {
                saveToDB(dataProduct);
            }

        } else {
            getMessageLog().fireMessage(translateForLog("!!\"%1 (%2)\" ist kein gültiges Produkt für den Auto-Product-Select Import.", productId.getProductNumber(), productId.toString()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            return;
        }
    }

    /**
     * Befüllt das Produkt mit den Ab- und Bis-Idents. Da Produkte für die Abgrenzung mehrere Idents haben können, werden
     * sie samtTrennern als ein String in der DB gespeichert.
     *
     * @param dataProduct
     */
    private void fillProductWithMultipleIdents(iPartsDataProduct dataProduct) {
        String identFromString = "";
        String identToString = "";
        boolean hasIdentsFrom = false;
        boolean hasIdentsTo = false;
        boolean first = true;
        for (iPartsIdentRange identRange : currentAutoProdData.idents) {
            if (!first) {
                identFromString += "/";
                identToString += "/";
            } else {
                first = false;
            }
            identFromString += identRange.getFromIdent();
            hasIdentsFrom |= !identRange.getFromIdent().isEmpty();
            identToString += identRange.getToIdent();
            hasIdentsTo |= !identRange.getToIdent().isEmpty();
        }

        // Wenn es gar keine Idents von/bis gibt, dann den entsprechenden Ident-String wieder leeren
        if (!hasIdentsFrom) {
            identFromString = "";
        }
        if (!hasIdentsTo) {
            identToString = "";
        }

        dataProduct.setFieldValue(FIELD_DP_APS_FROM_IDENTS, identFromString, DBActionOrigin.FROM_EDIT);
        dataProduct.setFieldValue(FIELD_DP_APS_TO_IDENTS, identToString, DBActionOrigin.FROM_EDIT);
    }

    @Override
    protected void postImportTask() {
        dictCache = null;
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importXMLOrArchiveWithMultipleDifferentFiles(importFile);
        }
        return false;
    }

    /**
     * Klassen zum Aufsammeln der Auto-Product-Select Informationen über mehrere Tags hinaus.
     */
    private class AutoProdData {

        private List<iPartsIdentRange> idents;
        private EtkMultiSprache multiText;
        private iPartsProductId productId;
        private String codeRule;

        public AutoProdData() {
            idents = new DwList<iPartsIdentRange>(0);
        }

        public List<iPartsIdentRange> getIdents() {
            return idents;
        }

        public void setIdents(List<iPartsIdentRange> idents) {
            this.idents = idents;
        }

        public EtkMultiSprache getMultiText() {
            if (multiText == null) {
                multiText = new EtkMultiSprache();
            }
            return multiText;
        }

        public void setMultiText(EtkMultiSprache multiText) {
            this.multiText = multiText;
        }

        public iPartsProductId getProductId() {
            return productId;
        }

        public void setProductId(iPartsProductId productId) {
            this.productId = productId;
        }

        public String getCodeRule() {
            if (codeRule == null) {
                return "";
            }
            return codeRule;
        }

        public void setCodeRule(String codeRule) {
            this.codeRule = codeRule;
        }


        /**
         * Fügt pro "Einsatz" XML Element die zugehörigen Ab- und Bis-Idents hinzu. Diese werden in einer Map gespeichert
         * damit die Ab <-> Bis Beziehung bestehen bleibt.
         *
         * @param fromIdent
         * @param toIdent
         */
        public void addIdents(String fromIdent, String toIdent, int recordNo) {
            if (fromIdent == null || toIdent == null) {
                // Die Elemente müssen da sein, können aber leer sein
                getMessageLog().fireMessage(translateForLog("!!Fehler beim Importieren der Auto-Product-Select Idents für Record " +
                                                            "\"%1\". Es sind keine Ident XML Elemente vorhanden",
                                                            String.valueOf(recordNo)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                return;
            }
            if (StrUtils.isEmpty(fromIdent, toIdent)) {
                return;
            }

            if (StrUtils.isValid(fromIdent, toIdent)) {
                iPartsIdentRange currentRange = null;
                for (iPartsIdentRange range : idents) {
                    if (range.getFromIdent().equals(fromIdent)) {
                        currentRange = range;
                        break;
                    }
                }
                if ((currentRange != null) && !currentRange.getToIdent().equals(toIdent)) {
                    // Zweimal der gleiche Ab-Ident mit unterschiedlichen Bis Ident darf eigentlich nicht sein
                    getMessageLog().fireMessage(translateForLog("!!Importdatei für Auto-Product-Select Import enthält für " +
                                                                "Record \"%1\" doppelte Ab-Idents mit unterschiedlichen " +
                                                                "Bis-Idents. Ab-Ident: \"%2\"; Bis-Ident bisher: \"%2\"; Bis-Ident neu: \"%3\"",
                                                                String.valueOf(recordNo), fromIdent, currentRange.getToIdent(), toIdent),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                }
            }
            idents.add(new iPartsIdentRange(fromIdent, toIdent));
        }
    }
}
