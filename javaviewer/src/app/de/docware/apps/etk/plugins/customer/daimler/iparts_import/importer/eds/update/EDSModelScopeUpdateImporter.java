/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataOPSScope;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsOPSScopeId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Importer für EDS Baumusterumfänge (UMF)
 */
public class EDSModelScopeUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RUMF";

    public static final String UMF_SCOPE_NUMBER = "ScopeNumber"; // Umfang
    public static final String UMF_SCOPE_LANG_DATA = "ScopeLangData";  // Sprachdaten
    public static final String UMF_LANG = "language";  // Sprache
    public static final String UMF_DESCRIPTION = "Description"; // eigentliche Benennung

    // Mapping für Texte, die über TB.f kommen
    private static final Map<String, String> EXTERNAL_TEXT_MAPPING = new HashMap<>();

    static {
        EXTERNAL_TEXT_MAPPING.put(UMF_DESCRIPTION, FIELD_DOS_DESC);
    }

    private Map<String, iPartsDataOPSScope> scopeMapping;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public EDSModelScopeUpdateImporter(EtkProject project) {
        super(project, "!!EDS-Baumusterumfang (UMF)", TABLE_DA_OPS_SCOPE, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_OPS_SCOPE, EDS_MODEL_SCOPE_NAME, false, false, true, new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        mapping.put(FIELD_DOS_DESC, UMF_DESCRIPTION);

        allXMLElements.addAll(mapping.values());
        allXMLElements.add(UMF_SCOPE_NUMBER);
        allXMLElements.add(UMF_SCOPE_LANG_DATA);
        allXMLElements.add(UMF_LANG);

        allXMLElementsToConsider.addAll(allXMLElements);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ UMF_SCOPE_NUMBER, UMF_DESCRIPTION };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        scopeMapping = new HashMap<>();
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EDSImportHelper helper = new EDSImportHelper(getProject(), getMapping(), getDestinationTable());
        helper.prepareXMLImportRec(importRec, getAllXMLElementsToConsider(), getAllXMLElements());
        iPartsOPSScopeId opsScopeId = new iPartsOPSScopeId(helper.handleValueOfSpecialField(UMF_SCOPE_NUMBER, importRec));
        if (opsScopeId.getScope().isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerem Umfang \"%2\" übersprungen",
                                                        String.valueOf(recordNo), importRec.get(UMF_SCOPE_NUMBER)),
                                        MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        iPartsEDSLanguageDefs langDef = helper.getEDSLanguageDefFromAttribute(UMF_SCOPE_LANG_DATA, UMF_LANG, importRec);

        if (langDef == iPartsEDSLanguageDefs.EDS_UNKNOWN) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wegen ungültigem Sprachkürzel \"%2\" übersprungen",
                                                        String.valueOf(recordNo), importRec.get(UMF_LANG)),
                                        MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        iPartsDataOPSScope dataOPSScope = scopeMapping.get(opsScopeId.getScope());
        if (dataOPSScope == null) {
            dataOPSScope = new iPartsDataOPSScope(getProject(), opsScopeId);
            if (!dataOPSScope.loadFromDB(opsScopeId)) {
                dataOPSScope.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            scopeMapping.put(opsScopeId.getScope(), dataOPSScope);
        } else {
            reduceRecordCount();
        }
        // Sofern vorhanden, die von außen gesetzten Texte verwenden. Außer der Beschreibung gibts keine Daten, die
        // übernommen werden sollen, daher hier die Unterscheidung und kein Zusatz zum eigentlichen Import
        if (hasExternalTexts()) {
            setExternalTexts(dataOPSScope, EXTERNAL_TEXT_MAPPING);
        } else {
            helper.fillOverrideOneLanguageTextForEDS(dataOPSScope, langDef, FIELD_DOS_DESC, helper.handleValueOfSpecialField(UMF_DESCRIPTION, importRec));
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                getMessageLog().fireProgress(0, scopeMapping.size(), "", true, false);
                int counter = 0;
                for (iPartsDataOPSScope dataOPSScope : scopeMapping.values()) {
                    saveToDB(dataOPSScope);
                    getMessageLog().fireProgress(counter++, scopeMapping.size(), "", true, true);
                }
                getMessageLog().hideProgress();
            }
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_OPS_SCOPE)) {
            getProject().getDB().delete(TABLE_DA_OPS_SCOPE);
        }
        return true;
    }
}
