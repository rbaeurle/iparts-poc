/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEPCLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test-Importer für EPC am Beispiel BM_DICTIONARY.csv
 */
public class EPCTestImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String BM_DICT_DESCIDX = "DESCIDX";
    final static String BM_DICT_LANG = "LANG";
    final static String BM_DICT_TEXT = "TEXT";

    private String[] headerNames = new String[]{
            BM_DICT_DESCIDX,
            BM_DICT_LANG,
            BM_DICT_TEXT
    };

    private String tableName = "table";
    private Map<String, String> mapping;

    public EPCTestImporter(EtkProject project) {
        super(project, "EPC BM-Dictionary",
              new FilesImporterFileListType("table", "!!EPC BM-Dictionary", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_GZ,
                                                          MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        mapping = new HashMap<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
//        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
//        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessage(getMessageLog(), getLogLanguage(),
//                                                                             EnumSet.of(DictTextKindTypes.ADD_TEXT), null, null)) {
//            return false;
//        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        BMDictImportHelper importHelper = new BMDictImportHelper(getProject(), mapping, tableName);
        iPartsEPCLanguageDefs langDef = iPartsEPCLanguageDefs.getType(importRec.get(BM_DICT_LANG));
        if ((langDef == iPartsEPCLanguageDefs.EPC_UNKNOWN) || (langDef == iPartsEPCLanguageDefs.EPC_NEUTRAL)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Sprache \"%2\" übersprungen", String.valueOf(recordNo),
                                                        importRec.get(BM_DICT_LANG)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        String descIdx = importHelper.handleValueOfSpecialField(BM_DICT_DESCIDX, importRec);
        String text = importHelper.handleValueOfSpecialField(BM_DICT_TEXT, importRec);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, '¬', true, headerNames, '"'));
        } else if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_CSV)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, '¬', true, headerNames));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, '¬', false, null));
        }
    }

    private class BMDictImportHelper extends EPCImportHelper {

        public BMDictImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }

            return value;
        }
    }
}
