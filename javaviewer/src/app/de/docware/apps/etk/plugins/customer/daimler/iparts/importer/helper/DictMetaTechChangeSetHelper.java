/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.importer.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataTextEntry;
import de.docware.apps.etk.base.project.mechanic.ids.TextEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

/**
 * Hilfsklasse für die Verwaltung und Speicherung von neu erzeugten Lexikon-Einträgen
 * Damit die Technischen ChangeSets nicht zu groß werden, wird automatisch nach MAX_ENTRIES_FOR_TECH_CHANGE_SET Einträgen
 * ein Technisches ChangeSet angelegt und die Daten darin und der DB gespeichert
 */
public class DictMetaTechChangeSetHelper {

    private final EtkProject project;
    private final Language searchLang;
    private EtkDataObjectList dataObjectList;
    private String tableDotFieldName;
    private int totalDictMetaSavedCounter;
    private int totalChangeSetCounter;

    protected DictMetaTechChangeSetHelper(EtkProject project, Language searchLang) {
        this.project = project;
        this.searchLang = searchLang;
        clear();
    }

    public DictMetaTechChangeSetHelper(EtkProject project, iPartsDictTextKindId textKindId, Language searchLang) {
        this(project, searchLang);
        this.tableDotFieldName = calcTableDotFieldName(textKindId);
    }

    public DictMetaTechChangeSetHelper(EtkProject project, DictTextKindTypes textKindType, Language searchLang) {
        this(project, searchLang);
        this.tableDotFieldName = calcTableDotFieldName(textKindType);
    }

    public void clear() {
        totalDictMetaSavedCounter = 0;
        totalChangeSetCounter = 0;
        dataObjectList = new GenericEtkDataObjectList();
    }

    public int getTotalDictMetaSavedCounter() {
        return totalDictMetaSavedCounter;
    }

    public int getTotalChangeSetCounter() {
        return totalChangeSetCounter;
    }

    public void saveCreatedDataDictMetaList(boolean saveToDB) {
        updateCreatedDataDictMetaList(null, 0, saveToDB);
    }

    public void updateCreatedDataDictMetaListWithoutSave(iPartsDataDictMeta dataDictMeta, DictTextKindTypes textKindType) {
        storeDictMeta(dataDictMeta, calcTableDotFieldName(textKindType));
    }

    public void updateCreatedDataDictMetaListWithoutSave(iPartsDataDictMeta dataDictMeta, String currentTableDotFieldName) {
        storeDictMeta(dataDictMeta, currentTableDotFieldName);
    }

    public void updateCreatedDataDictMetaListWithoutSave(iPartsDataDictMeta dataDictMeta, String currentTableDotFieldName,
                                                         boolean allLanguages) {
        storeDictMeta(dataDictMeta, currentTableDotFieldName, allLanguages);
    }

    public void updateCreatedDataDictMetaListWithoutSave(iPartsDataDictMeta dataDictMeta) {
        storeDictMeta(dataDictMeta, tableDotFieldName);
    }

    public void updateCreatedDataDictMetaList(iPartsDataDictMeta dataDictMeta, DictTextKindTypes textKindType, int maxCount, boolean saveToDB) {
        storeDictMeta(dataDictMeta, calcTableDotFieldName(textKindType));
        updateCreatedDataDictMetaList(null, maxCount, saveToDB);
    }

    public void updateCreatedDataDictMetaList(iPartsDataDictMeta dataDictMeta, String currentTableDotFieldName, int maxCount, boolean saveToDB) {
        storeDictMeta(dataDictMeta, currentTableDotFieldName);
        updateCreatedDataDictMetaList(null, maxCount, saveToDB);
    }

    public void updateCreatedDataDictMetaList(iPartsDataDictMeta dataDictMeta, int maxCount, boolean saveToDB) {
        storeDictMeta(dataDictMeta, tableDotFieldName);
        if (dataObjectList.size() >= maxCount) {
            if (saveToDB) {
                if (!dataObjectList.isEmpty()) {
                    project.getDbLayer().startTransaction();
                    project.getDbLayer().startBatchStatement();
                    try {
                        // Techn ChangeSet anlegen + Speichern
                        if (iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(project, dataObjectList, iPartsChangeSetSource.DICTIONARY)) {
                            iPartsDataDictMetaList dictMetasForSave = new iPartsDataDictMetaList();
                            dataObjectList.forEach(dataObject -> {
                                if (dataObject instanceof iPartsDataDictMeta) {
                                    dictMetasForSave.add((iPartsDataDictMeta)dataObject, DBActionOrigin.FROM_EDIT);
                                }
                            });
                            dictMetasForSave.saveToDB(project);
                            totalChangeSetCounter++;
                        }
                        // todo ggf Fehlermeldung

                        project.getDbLayer().endBatchStatement();
                        project.getDbLayer().commit();
                    } catch (Exception e) {
                        project.getDbLayer().cancelBatchStatement();
                        project.getDbLayer().rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }
                }
            }
            dataObjectList.clear(DBActionOrigin.FROM_DB);
        }
    }

    protected void storeDictMeta(iPartsDataDictMeta dataDictMeta, String currentTableDotFieldName) {
        storeDictMeta(dataDictMeta, currentTableDotFieldName, false);
    }

    protected void storeDictMeta(iPartsDataDictMeta dataDictMeta, String currentTableDotFieldName, boolean allLanguages) {
        if (dataDictMeta != null) {
            dataObjectList.add(dataDictMeta, DBActionOrigin.FROM_EDIT);
            totalDictMetaSavedCounter++;
            addTextEntries(dataDictMeta.getMultiLang(), currentTableDotFieldName, allLanguages);
        }
    }

    protected void addTextEntries(EtkMultiSprache multi, String currentTableDotFieldName, boolean allLanguages) {
        if (StrUtils.isEmpty(currentTableDotFieldName)) {
            // Rückfallposition
            currentTableDotFieldName = tableDotFieldName;
        }
        if (allLanguages) {
            for (Language lang : multi.getLanguages()) {
                addTextEntry(currentTableDotFieldName, multi, lang);
            }
        } else {
            addTextEntry(currentTableDotFieldName, multi, searchLang);
        }
    }

    private void addTextEntry(String currentTableDotFieldName, EtkMultiSprache multi, Language lang/*, String text*/) {
        String text = multi.getText(lang.getCode());
        if (StrUtils.isValid(text)) {
            String textId = multi.getTextId();
            TextEntryId textEntryId = new TextEntryId(currentTableDotFieldName, textId, lang.getCode());
            EtkDataTextEntry textEntry = EtkDataObjectFactory.createDataTextEntry(project, textEntryId);
            if (!textEntry.existsInDB()) {
                textEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                textEntry.setFieldValue(iPartsConst.FIELD_S_TEXTID, textId, DBActionOrigin.FROM_EDIT);
            }
            textEntry.setFieldValue(iPartsConst.FIELD_S_BENENN, text, DBActionOrigin.FROM_EDIT);
            dataObjectList.add(textEntry, DBActionOrigin.FROM_EDIT);
        }
    }

    protected String calcTableDotFieldName(DictTextKindTypes textKindType) {
        return calcTableDotFieldName(DictTxtKindIdByMADId.getInstance(project).getTxtKindId(textKindType));
    }

    protected String calcTableDotFieldName(iPartsDictTextKindId textKindId) {
        if (textKindId == null) {
            // Rückfallposition (das Lexikon existiert nicht)
            return TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR);
        }
        String currentTableDotFieldName =
                DictTxtKindIdByMADId.getInstance(project).findTableAndFieldNameByTextKindId(textKindId, project);
        if (StrUtils.isEmpty(currentTableDotFieldName)) {
            currentTableDotFieldName = TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR);
        }
        return currentTableDotFieldName;
    }
}
