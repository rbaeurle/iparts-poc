/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.translations;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TransitImporterTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TranslationLogObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.List;
import java.util.Map;

public class TranslationsLogXMLImporter extends AbstractDataImporter implements iPartsConst {

    protected String tableName;
    private boolean importToDB = true;

    public TranslationsLogXMLImporter(EtkProject project) {
        super(project, "!!Prüfquittungen für Übersetzungspakete",
              new FilesImporterFileListType(TABLE_DA_DICT_TRANS_JOB, "!!Prüfquittungen für Übersetzungspakete", true,
                                            false, false,
                                            TranslationLogObject.getAllTranslationLogAliases()));
        tableName = TABLE_DA_DICT_TRANS_JOB;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
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
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            TransitImporterTypes importType = TransitImporterTypes.getImportType(importFile, true);
            if (importType == TransitImporterTypes.TRANSLATION_LOG_ZIP) {
                getMessageLog().fireMessage(translateForLog("!!Lösche nicht benötigtes Archiv %1", importFile.getName()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                return importFile.delete();
            } else {
                return importXMLLogFile(importFile);
            }
        }
        return false;
    }

    private boolean importXMLLogFile(DWFile importFile) {
        if (importFile == null) {
            return false;
        }
        TranslationLogObject logObject = TranslationLogObject.createFromXmlContent(importFile);
        if (logObject == null) {
            getMessageLog().fireMessage(translateForLog("!!Importer abgebrochen weil der Status nicht eindeutig bestimmt werden konnte"),
                                        MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            return false;
        }
        return saveTranslationLogObjectToDB(logObject);
    }


    public boolean saveTranslationLogObjectToDB(TranslationLogObject logObject) {
        String bundleName = logObject.getBundleName();
        if (!StrUtils.isValid(bundleName)) {
            getMessageLog().fireMessage(translateForLog("!!Importer abgebrochen weil der Bundle Name nicht bestimmt werden konnte"),
                                        MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            return false;
        }
        iPartsDataDictTransJobList jobsByBundleName = iPartsDataDictTransJobList.getJobsByBundleName(getProject(), bundleName);
        if (jobsByBundleName.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Importer abgebrochen weil es zu \"%1\" keinen Eintrag in der DB gibt", bundleName),
                                        MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            return false;
        }

        for (iPartsDataDictTransJob transJob : jobsByBundleName) {
            if (logObject.isOKState()) {
                transJob.updateTranslationJob(iPartsDictTransJobStates.TRANSLATING);
            } else {
                iPartsDictTransJobId transJobId = transJob.getAsId();
                String textId = transJobId.getTextId();
                transJob.updateTranslationJob(iPartsDictTransJobStates.TRANS_ERROR_CLM, logObject.getMessage());
                // Status an allen verfügbaren Sprachen zurücksetzen damit dieser Text nochmal an CBSL geschickt werden kann
                iPartsDataDictMetaList metaList = iPartsDataDictMetaList.loadMetaFromTextIdList(getProject(), textId);
                int langCount = 0;
                for (iPartsDataDictMeta dictMeta : metaList) {
                    langCount += dictMeta.getLanguages().size();
                    dictMeta.setTranslationStateForAllExistingLanguages(iPartsDictSpracheTransState.RELEVANT_FOR_TRANSLATION, DBActionOrigin.FROM_EDIT);
                }
                getMessageLog().fireMessage(translateForLog("!!Für die Text-Id \"%1\" werden alle Einträge " +
                                                            "in DA_DICT_SPRACHE aufgrund eines Fehler beim Übersetzer " +
                                                            "zurückgesetzt. TransJobId: %2, Anzahl DictMeta für Text-ID: %3, " +
                                                            "Anzahl Spracheinträge insgesamt: %4",
                                                            textId, transJobId.toStringForLogMessages(),
                                                            String.valueOf(metaList.size()), String.valueOf(langCount)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                metaList.saveToDB(getProject());
            }

            if (importToDB) {
                transJob.saveToDB();
            }
        }
        return true;
    }
}
