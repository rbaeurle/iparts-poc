/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.KeyValueRecordGzTarFileReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.ImporterTAL31ATypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.RFTSXImportFunction;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractMainDataImporter;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Master Importer für die MAD Stammdaten. Übernimmt das Entpacken der TAR.GZ-Datei und die Ansteuerung der einzelnen Importer
 */
public class MADMasterDataMainImporter extends AbstractMainDataImporter implements iPartsConst, EtkDbConst {

    private static String FILE_LIST_TYPE = "MADMasterDataMain";

    public MADMasterDataMainImporter(EtkProject project) {
        super(project, "MAD Stammdaten",
              new FilesImporterFileListType(FILE_LIST_TYPE, "!!MAD Stammdaten", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
    }

    private void initSubImporterList() {
        importerList = new HashMap<String, EtkFunction>();
        // TAL31A-BM_STAMM-Importer
        importerList.put(ImporterTAL31ATypes.BM_STAMM.getFileName(), new RFTSXImportFunction("iPartsTAL31A_BM_Stamm_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADModelMasterImporter(getProject());
            }
        });

        // TAL31A-EVO_BAUKASTEN-Importer
        importerList.put(ImporterTAL31ATypes.EVO_BAUKASTEN.getFileName(), null);

        // TAL31A-FEDERMAPPING-Importer
        importerList.put(ImporterTAL31ATypes.FEDERMAPPING.getFileName(), new RFTSXImportFunction("iPartsTAL31A_Federmapping_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADSpringMappingImporter(getProject());
            }
        });

        // TAL31A-TEXTART-Importer
        importerList.put(ImporterTAL31ATypes.TEXTART.getFileName(), new RFTSXImportFunction("iPartsTAL31A_Textart_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADDictionaryImporter(getProject());
            }
        });

        // TAL31A-KURZTEXTE-Importer
        importerList.put(ImporterTAL31ATypes.KURZTEXTE.getFileName(), new RFTSXImportFunction("iPartsTAL31A_Kurztexte_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return null;
            }
        });

        // TAL31A-LANG_TEXTE-Importer
        importerList.put(ImporterTAL31ATypes.LANG_TEXTE.getFileName(), new RFTSXImportFunction("iPartsTAL31A_Lang_Texte_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return null;
            }
        });

        // TAL31A-FGST_AGGR-Importer
        importerList.put(ImporterTAL31ATypes.FGST_AGGR.getFileName(), new RFTSXImportFunction("iPartsTAL31A_FGST_AGGR_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADFAGGImporter(getProject());
            }
        });

        // TAL31A-SAA_STAMM-Importer
        importerList.put(ImporterTAL31ATypes.SAA_STAMM.getFileName(), new RFTSXImportFunction("iPartsTAL31A_SAA_Stamm_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADSaSaaImporter(getProject());
            }
        });

        // TAL31A-VARIANTEN-Importer
        importerList.put(ImporterTAL31ATypes.VARIANTEN.getFileName(), new RFTSXImportFunction("iPartsTAL31A_Varianten_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADEs2Importer(getProject());
            }
        });

        // TAL31A-TEILESTAMM-Importer
        importerList.put(ImporterTAL31ATypes.TEILESTAMM.getFileName(), new RFTSXImportFunction("iPartsTAL31A_Teilestamm_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADPartMasterDataImporter(getProject());
            }
        });

        // TAL31A-E_TEXTE-Importer
        importerList.put(ImporterTAL31ATypes.E_TEXTE.getFileName(), new RFTSXImportFunction("iPartsTAL31A_E_Texte_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADETexteImporter(getProject());
            }
        });

        // TAL31A-KGTU_TEXTE-Importer
        importerList.put(ImporterTAL31ATypes.KGTU_TEXTE.getFileName(), new RFTSXImportFunction("iPartsTAL31A_KGTU_Texte_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADKgTuImporter(getProject());
            }
        });

        // TAL31A-TEXT_FUSSNOTEN-Importer
        importerList.put(ImporterTAL31ATypes.TEXT_FUSSNOTEN.getFileName(), new RFTSXImportFunction("iPartsTAL31A_Text_Fussnoten_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADFootNoteTexteImporter(getProject());
            }
        });

        // TAL31A Applikationsliste
        importerList.put(ImporterTAL31ATypes.APPLICATION_LIST.getFileName(), new RFTSXImportFunction("iPartsTAL31A_Applikations_Liste_Importer", null, getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADApplicationListImporter(getProject(), getDatasetDate());
            }
        });
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        initSubImporterList();
        setSingleCall(true);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ImporterTAL31ATypes[] importFileOrder = ImporterTAL31ATypes.getFileImportOrder();
        if ((importFileOrder == null) || (importFileOrder.length == 0)) {
            cancelImport(translateForLog("!!Import abgebrochen! Die Reihenfolge der Importdateien wurde nicht gesetzt."), MessageLogType.tmlMessage);
            return;
        }
        importList = new LinkedHashMap<String, DWFile>();
        for (int lfdNr = 0; lfdNr < importRec.size(); lfdNr++) {
            String fileName = importRec.get(KeyValueRecordGzTarFileReader.FILE_PREFIX_IDENTIFIER + lfdNr);
            DWFile importFile = DWFile.get(fileName);
            String importName = importFile.extractFileName(false);
            if (!importFile.exists()) {
                importFile = null;
            }
            importList.put(importName, importFile);
        }
        checkImporter();
        getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);

        for (int i = 0; i < importFileOrder.length; i++) {
            ImporterTAL31ATypes tal31AType = importFileOrder[i];
            if (tal31AType == null) {
                continue;
            }
            if (Thread.currentThread().isInterrupted()) {
                cancelImport("!!Import-Thread wurde frühzeitig beendet");
                return;
            }

            DWFile importFile = importList.get(tal31AType.getFileName());
            if (importFile != null) {
                EtkFunction importerFunction = importerList.get(tal31AType.getFileName());
                if (importerFunction == null) {
                    continue;
                }
                RFTSXImportFunction functionImportHelper = (RFTSXImportFunction)importerFunction;
                currentFunctionImportHelper = functionImportHelper;
                functionImportHelper.addFileForWork(importFile);
                switch (tal31AType) {
                    case TEXTART:
                        ImporterTAL31ATypes[] additionalFiles = new ImporterTAL31ATypes[]{ ImporterTAL31ATypes.KURZTEXTE, ImporterTAL31ATypes.LANG_TEXTE };
                        String missingFiles = "";
                        for (ImporterTAL31ATypes additionalFileType : additionalFiles) {

                            DWFile additionalDictionaryFile = importList.get(additionalFileType.getFileName());
                            if (additionalDictionaryFile == null) {
                                if (StrUtils.isEmpty(missingFiles)) {
                                    missingFiles += additionalFileType.getFileName();
                                } else {
                                    missingFiles += ", " + additionalFileType.getFileName();
                                }
                            } else {
                                functionImportHelper.addFileForWork(additionalDictionaryFile);
                            }

                        }
                        if (!StrUtils.isEmpty(missingFiles)) {
                            getMessageLog().fireMessage(translateForLog("!!MAD Lexikon Import übersprungen, weil folgende Dateien gefehlt haben: %1", missingFiles),
                                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            continue;
                        }

                        break;
                    case KURZTEXTE:
                    case LANG_TEXTE:
                        continue;
                }
                functionImportHelper.run(null);  //mainWindow
                addErrorCount(functionImportHelper.errorCount);
                addWarningCount(functionImportHelper.warningCount);
                getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);
                if (functionImportHelper.isCanceled || (functionImportHelper.errorCount > 0)) {
                    cancelImport();
                    return;
                }
            } else {
                getMessageLog().fireMessage(translateForLog("!!Keine Datei für Importer \"%1\" vorhanden", tal31AType.getFileName()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);
            }
        }

    }

    public void addErrorCount(int addErrors) {
        errorCount += addErrors;
    }

    public void addWarningCount(int addWarnings) {
        warningCount += addWarnings;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(FILE_LIST_TYPE)) {
            if (getDatasetDate() == null) {
                setDatasetDate(iPartsMADDateTimeHandler.extractTalXDateFromFilename(importFile.getPath(), this));
            }
            return importMasterData(prepareImporterGZTar(importFile, FILE_LIST_TYPE));
        }
        return false;
    }

    @Override
    protected boolean isWithTransaction() {
        return false; // Die Unter-Importer starten jeweils ihre eigene Transaktion
    }
}
