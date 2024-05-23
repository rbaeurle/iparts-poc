/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuTemplate;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsKgTuTemplateId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
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
 * Importer für die KGTU templates (DA_KGTU_TEMPLATE)
 */
public class KgTuTemplateImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    private static String tableName = TABLE_DA_KGTU_TEMPLATE;

    // Wenn die Importdatei Header enthält und keine Header übergeben werden müssen die Header so heißen.
    private static final String AGGREGATE_TYPE = "Baumusterart";
    private static final String AS_PRODUCT_CLASSES = "AS Produktklassen";
    private static final String KG = "KG";
    private static final String TU = "TU";
    private static final String NAME = "Benennung";
    private static final String PICTURE = "Bild";
    private static final String TU_OPTIONS = "TU Optionen";

    private String[] primaryKeys;
    private HashMap<String, String> mapping;

    private boolean importToDB = true;

    EnumValue enumAsProductClasses = getProject().getEtkDbs().getEnumValue(ENUM_KEY_ASPRODUCT_CLASS);
    EnumValue enumAggregateTypes = getProject().getEtkDbs().getEnumValue(ENUM_KEY_AGGREGATE_TYPE);
    EnumValue enumTUOptions = getProject().getEtkDbs().getEnumValue(ENUM_KEY_CREATE_TU_OPTION);

    public KgTuTemplateImporter(EtkProject project) {
        super(project, "KG/TU Templates",
              new FilesImporterFileListType(tableName, "!!KG/TU Templates", true, true, true,
                                            new String[]{ MimeTypes.EXTENSION_EXCEL_XLSX, MimeTypes.EXTENSION_EXCEL_XLS, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }


    private void initMapping() {
        primaryKeys = new String[]{ AGGREGATE_TYPE, AS_PRODUCT_CLASSES, KG, TU };
        mapping = new HashMap<String, String>();
        mapping.put(FIELD_DA_DKT_AGGREGATE_TYPE, AGGREGATE_TYPE);
        mapping.put(FIELD_DA_DKT_AS_PRODUCT_CLASS, AS_PRODUCT_CLASSES);
        mapping.put(FIELD_DA_DKT_KG, KG);
        mapping.put(FIELD_DA_DKT_TU, TU);
        mapping.put(FIELD_DA_DKT_DESC, NAME);
        mapping.put(FIELD_DA_DKT_PICTURE, PICTURE);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeys, new String[]{ NAME }));

        // Normal würde man sagen, dass zumindest die PK-Felder gefüllt sein müssen; Bis zur Verwendung der KG/TU Templates zu
        // der Kombi aus (Baumusterart, AS Produktklasse) arbeiten wir jedenfalls mit dem Standard-Template, wo diese beiden Felder
        // leer sind. Daher keine Prüfung
//        importer.setMustHaveData(StrUtils.mergeArrays(primaryKeys, new String[]{ NAME }));
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   DictTextKindTypes.KG_TU_NAME)) {
            return false;
        }
        return true;
    }

    /**
     * Fehlende Enums haben zu nichtssagenden Null-Pointer-Exceptions geführt.
     * Hier explizit die Enums auf Existenz prüfen.
     *
     * @param messageLog
     * @return
     */
    @Override
    public boolean initImport(EtkMessageLog messageLog) {
        boolean isValid = super.initImport(messageLog);
        if (isValid) {
            // Essentiell notwendiges Enum führt im Fehlerfall zum Abbruch des Imports.
            if (enumAsProductClasses == null) {
                getMessageLog().fireMessage(translateForLog("!!Enum %1 enthält keine Werte, Import wird abgebrochen!",
                                                            ENUM_KEY_ASPRODUCT_CLASS),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                isValid = false;
            }

            // Essentiell notwendiges Enum führt im Fehlerfall zum Abbruch des Imports.
            if (enumAggregateTypes == null) {
                getMessageLog().fireMessage(translateForLog("!!Enum %1 enthält keine Werte, Import wird abgebrochen!",
                                                            ENUM_KEY_AGGREGATE_TYPE),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                isValid = false;
            }

            // Ergänzendes Enum führt im Fehlerfall nur zu einer Wanrnung und NICHT zum Abbruch des Imports.
            if (enumTUOptions == null) {
                getMessageLog().fireMessage(translateForLog("!!Enum %1 enthält keine Werte!",
                                                            ENUM_KEY_CREATE_TU_OPTION),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
        }
        return isValid;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    /**
     * Import eines Records
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        KgTuTemplateImportHelper helper = new KgTuTemplateImportHelper(getProject(), mapping, tableName);

        // Baumusterart ist ein Enum. Für die Speicherung im PK sollten wir den Werte prüfen
        // Leer lassen wir zu bis die Template-Verwendung umgestellt ist (leer = Default Template).
        String aggregateType = helper.handleValueOfSpecialField(AGGREGATE_TYPE, importRec);
        if (!(enumAggregateTypes.containsKey(aggregateType) || aggregateType.isEmpty())) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Baumusterart \"%2\" übersprungen", String.valueOf(recordNo),
                                                        aggregateType),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }


        // das PICTURE Feld ist eine Referenz auf eine Zusatzgrafik (Workbench: Import / Datentypen / Zusatzgrafiken); Aktuell gibt es diesen
        // Import noch nicht
        String pictureKey = helper.handleValueOfSpecialField(PICTURE, importRec);

        /**
         * Bezeichnung behandeln
         * Deutschen Text in Lexikon nachschlagen; wenn existent Texte übernehmen, sonst anlegen
         */

        EtkMultiSprache multiSprache = new EtkMultiSprache();
        multiSprache.setText(Language.DE, helper.handleValueOfSpecialField(NAME, importRec));

        DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
        boolean dictSuccessful = importHelper.handleDictTextId(DictTextKindTypes.KG_TU_NAME, multiSprache, "", DictHelper.getKgTuTemplateImportForeignSource(),
                                                               true, TableAndFieldName.make(tableName, FIELD_DA_DKT_DESC), null, null, true);
        if (!dictSuccessful || importHelper.hasWarnings()) {
            //Fehler beim Dictionary Eintrag
            for (String str : importHelper.getWarnings()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }

            if (!dictSuccessful) {
                // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                cancelImport();
            }
            reduceRecordCount();
            return;
        }


        // das AS Produktklassen Feld enthält eine kommasep. Liste von AS Produktklassen; wir jede wird ein Record erzeugt
        String[] asProductClasses;
        String asProductClassesString = helper.handleValueOfSpecialField(AS_PRODUCT_CLASSES, importRec);
        if (asProductClassesString.isEmpty()) {
            // das Default Template
            asProductClasses = new String[]{ "" };
        } else {
            asProductClasses = StrUtils.toStringArray(asProductClassesString, ",", false);
        }
        List<String> tuOptions = helper.getTUOptions(TU_OPTIONS, importRec, recordNo);
        for (String asProductClass : asProductClasses) {

            // AS Produktklasse ist ein Enum. Für die Speicherung im PK sollten wir den Wert prüfen
            // Leer lassen wir zu bis die Template-Verwendung umgestellt ist (leer = Default Template).
            if (!(enumAsProductClasses.containsKey(asProductClass) || asProductClassesString.isEmpty())) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger AS-Produktklasse \"%2\" übersprungen", String.valueOf(recordNo),
                                                            asProductClass),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                continue;
            }


            iPartsKgTuTemplateId kgTuTemplateId = new iPartsKgTuTemplateId(aggregateType,
                                                                           asProductClass,
                                                                           helper.handleValueOfSpecialField(KG, importRec),
                                                                           helper.handleValueOfSpecialField(TU, importRec));
            iPartsDataKgTuTemplate dataKgTuTemplate = new iPartsDataKgTuTemplate(getProject(), kgTuTemplateId);
            if (!dataKgTuTemplate.loadFromDB(kgTuTemplateId)) {
                dataKgTuTemplate.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }

            dataKgTuTemplate.setFieldValue(FIELD_DA_DKT_PICTURE, pictureKey, DBActionOrigin.FROM_EDIT);
            dataKgTuTemplate.setFieldValueAsSetOfEnum(FIELD_DA_DKT_TU_OPTIONS, tuOptions, DBActionOrigin.FROM_EDIT);

            if (importToDB) {
                saveToDB(dataKgTuTemplate);
            }

        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return removeAllExistingDataForTable(importFileType, tableName);
    }

    /**
     * Importiert die Datei für den übergebenen Import-Dateilisten-Typ.
     *
     * @param importFileType
     * @param importFile
     * @return {@code true} falls der Import erfolgreich war
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        // Excel-Import (unterscheidbar nur an der Methodensignatur)
        return importMasterData(prepareImporterKeyValue(importFile, tableName, true, StrUtils.mergeArrays(primaryKeys, new String[]{ NAME, PICTURE })));
    }


    private class KgTuTemplateImportHelper extends DIALOGImportHelper {

        public KgTuTemplateImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public List<String> getTUOptions(String sourceField, Map<String, String> importRec, int recordNo) {
            List<String> result = new DwList<>();
            if (enumTUOptions == null) {
                return result;
            }
            String value = handleValueOfSpecialField(sourceField, importRec);
            if (StrUtils.isValid(value)) {
                String wrongOptions = "";
                List<String> tuOptions = StrUtils.toStringList(value, ",", false, true);
                for (String tuOption : tuOptions) {
                    if (!enumTUOptions.containsKey(tuOption)) {
                        if (!wrongOptions.isEmpty()) {
                            wrongOptions += ", ";
                        }
                        wrongOptions += tuOption;
                    } else {
                        result.add(tuOption);
                    }
                }
                if (!wrongOptions.isEmpty()) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 mit folgenden ungültigen TU-Optionen \"%2\" werden ignoriert.",
                                                                String.valueOf(recordNo), wrongOptions),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);

                }
            }
            return result;
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {

            // Strings, die von Excel bzw. POI Lib als numerisch erkannt wurden, als String behandeln. Das kann nur die Felder KG u. TU betreffen
            // dies betrifft eine Importdatei, die früher durch den benutzerdef. Workbench Import korrekt importiert wurde.
            // dieses Problem muss durch JFRAME-1122 zentral geklärt werden
            if ((sourceField.equals(KG) || sourceField.equals(TU))
                && value.indexOf(".") != -1) {
                value = StrUtils.stringUpToCharacter(value, ".");
            }

            if (sourceField.equals(AS_PRODUCT_CLASSES)) {
                // das AS Produktklassen Feld enthält eine kommasep. Liste von AS Produktklassen; die Kommata würden by default in Punkte umgewandelt
                return value.trim();
            }
            return value;
        }
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.KG_TU_NAME));
    }
}
