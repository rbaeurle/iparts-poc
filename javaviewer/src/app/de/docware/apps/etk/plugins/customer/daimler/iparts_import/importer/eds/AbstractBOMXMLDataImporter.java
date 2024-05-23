/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractBOMXMLDataImporter extends AbstractBOMDataImporter {

    private Set<String> allXMLElementsToConsider;
    private Set<String> allXMLElements;
    private Map<String, EtkMultiSprache> textsForMultiLangFields;

    public AbstractBOMXMLDataImporter(EtkProject project, String importName, String destTable, String importTable, FilesImporterFileListType... importFileTypes) {
        super(project, importName, destTable, importTable, importFileTypes);
    }

    public AbstractBOMXMLDataImporter(EtkProject project, String importName, boolean withHeader, String destTable, String importTable, FilesImporterFileListType... importFileTypes) {
        super(project, importName, withHeader, destTable, importTable, importFileTypes);
    }

    public Set<String> getAllXMLElementsToConsider() {
        return allXMLElementsToConsider;
    }

    public Set<String> getAllXMLElements() {
        return allXMLElements;
    }

    /**
     * Check, ob bestimmte Texte von außen gesetzt wurden
     *
     * @return
     */
    protected boolean hasExternalTexts() {
        return (textsForMultiLangFields != null) && !textsForMultiLangFields.isEmpty();
    }

    protected Map<String, EtkMultiSprache> getTextsForMultiLangFields() {
        return textsForMultiLangFields;
    }

    public void setTextsForMultiLangFields(Map<String, EtkMultiSprache> textsForMultiLangFields) {
        this.textsForMultiLangFields = textsForMultiLangFields;
    }

    /**
     * Setzt für die Feldpaare (Importfeld - DB-Feld) die von außen gesetzten Texte, sofern welche vorhanden sind
     *
     * @param dataObject
     * @param mapping
     */
    protected void setExternalTexts(EtkDataObject dataObject, Map<String, String> mapping) {
        if ((mapping == null) || mapping.isEmpty()) {
            return;
        }
        Map<String, EtkMultiSprache> multiLangData = getTextsForMultiLangFields();
        if ((multiLangData != null) && !multiLangData.isEmpty()) {
            mapping.forEach((importerField, dbField) -> {
                EtkMultiSprache text = multiLangData.get(importerField);
                if (text != null) {
                    dataObject.setFieldValueAsMultiLanguage(dbField, text, DBActionOrigin.FROM_EDIT);
                }
            });
        }
    }


    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        allXMLElementsToConsider = new HashSet<>();
        allXMLElements = new HashSet<>();
        initXMLMapping(mapping, allXMLElementsToConsider, allXMLElements);
        super.initMapping(mapping);
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_EDS_IMPORT));
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();
    }

    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {

    }
}
