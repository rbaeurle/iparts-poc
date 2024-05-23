/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditCombinedTextForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiCombTextCompleteEditControl;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helferklasse mit Methoden, um kombinierte Texte im Edit verarbeiten zu können
 */
public class iPartsEditCombTextHelper {

    /**
     * Liefert die Aufteilung der übergebenen Textbausteine nach den übergebenen Textarten
     *
     * @param project
     * @param combTextList
     * @param textKinds
     * @return
     */
    public static Map<DictTextKindTypes, iPartsDataCombTextList> getTextKindToCombTextMap(EtkProject project,
                                                                                          iPartsDataCombTextList combTextList,
                                                                                          Map<String, EditCombinedTextForm.TextKindType> textKinds) {
        Map<DictTextKindTypes, iPartsDataCombTextList> textKindFoCombTextMap = new HashMap<>();
        // Erst die zu löschenden Elemente durchgehen und verteilen
        fillCombTextMap(project, textKindFoCombTextMap, combTextList.getDeletedList(), textKinds);
        // Tatsächliches verschieben der gelöschten Elemente in die "Delete" Liste
        for (iPartsDataCombTextList deleteCombTextList : textKindFoCombTextMap.values()) {
            deleteCombTextList.deleteAll(DBActionOrigin.FROM_EDIT);
        }
        fillCombTextMap(project, textKindFoCombTextMap, combTextList.getAsList(), textKinds);
        return textKindFoCombTextMap;
    }

    private static void fillCombTextMap(EtkProject project, Map<DictTextKindTypes, iPartsDataCombTextList> textKindFoCombTextMap,
                                        List<iPartsDataCombText> combTextList, Map<String, EditCombinedTextForm.TextKindType> textKinds) {
        for (iPartsDataCombText dataCombText : combTextList) {
            EtkMultiSprache multiSprache = dataCombText.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT);
            if (multiSprache != null) {
                // TextKindType bestimmen in Abhängigkeit von der TextId
                iPartsDataDictMetaList dataDictMetaList = iPartsDataDictMetaList.loadMetaFromTextIdList(project, multiSprache.getTextId());
                if (!dataDictMetaList.isEmpty()) {
                    EditCombinedTextForm.TextKindType txtKindType = null;
                    for (iPartsDataDictMeta dataDictMeta : dataDictMetaList) {
                        EditCombinedTextForm.TextKindType testTxtKindType = textKinds.get(dataDictMeta.getAsId().getTextKindId());
                        if (testTxtKindType != null) {
                            txtKindType = testTxtKindType;
                            break;
                        }
                    }
                    if (txtKindType != null) {
                        iPartsDataCombTextList combTexts = textKindFoCombTextMap.get(txtKindType.type);
                        if (combTexts == null) {
                            combTexts = new iPartsDataCombTextList();
                            textKindFoCombTextMap.put(txtKindType.type, combTexts);
                        }
                        combTexts.add(dataCombText, DBActionOrigin.FROM_DB);
                    }
                }
            }
        }
    }

    /**
     * Speichert die veränderten kombinierten Texte innerhalb des übergebenen {@link iPartsGuiCombTextCompleteEditControl}
     *
     * @param project
     * @param combTextControl
     * @param currentPartListEntry
     * @return Alle durch den Inplace-Editor zusätzlich veränderten Assemblies; kann auch {@code null} sein
     */
    public static Set<AssemblyId> storeDataCombList(EtkProject project, iPartsGuiCombTextCompleteEditControl combTextControl, EtkDataPartListEntry currentPartListEntry) {
        Set<AssemblyId> equalizedAssemblyIds = null;

        // Im Multi-Edit nichts speichern
        if ((combTextControl != null) && !combTextControl.isMultiEdit()) {
            iPartsDataCombTextList dataCombTextList = combTextControl.getAllCombTexts(true);
            if ((dataCombTextList != null) && dataCombTextList.isModifiedWithChildren()) {
                // Flags für "Quelle GenVO" müssen zurückgesetzt werden, wenn es mehr als einen sprachabhängigen Ergänzungstext gibt
                boolean forceResetSourceGenVO = false;
                if (dataCombTextList.size() > 1) {
                    Map<String, EditCombinedTextForm.TextKindType> textKindsForCombTexts = EditCombinedTextForm.loadTextKinds(project);
                    Map<DictTextKindTypes, iPartsDataCombTextList> textkindToCombTexts = getTextKindToCombTextMap(project,
                                                                                                                  dataCombTextList,
                                                                                                                  textKindsForCombTexts);
                    iPartsDataCombTextList dataCombTextListAddText = textkindToCombTexts.get(DictTextKindTypes.ADD_TEXT);
                    int addTextCount = (dataCombTextListAddText != null) ? dataCombTextListAddText.size() : 0;
                    forceResetSourceGenVO = addTextCount != 1;
                }

                // Flags für "Quelle GenVO" müssen zurückgesetzt werden
                for (iPartsDataCombText dataCombText : dataCombTextList) {
                    if (forceResetSourceGenVO || dataCombText.isModified()) {
                        dataCombText.setFieldValueAsBoolean(iPartsConst.FIELD_DCT_SOURCE_GENVO, false, DBActionOrigin.FROM_EDIT);
                    }
                }

                // Kennzeichen für InplaceEditor
                if (currentPartListEntry != null) {
                    GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();
                    equalizedAssemblyIds = EditEqualizeFieldsHelper.doEqualizeCombTextInplaceEditor(project, currentPartListEntry,
                                                                                                    dataCombTextList, modifiedDataObjects);
                    if (!modifiedDataObjects.isEmpty()) {
                        EtkRevisionsHelper revisionsHelper = project.getEtkDbs().getRevisionsHelper();
                        if (revisionsHelper != null) {
                            revisionsHelper.addDataObjectListToActiveChangeSetForEdit(modifiedDataObjects);
                        }
                    }
                }

                // hier speichern in ChangeSet oder DB
                if (!handleDataObjectListViaChangeSet(dataCombTextList, project)) {
                    project.getDbLayer().startTransaction();
                    try {
                        dataCombTextList.saveToDB(project, true);
                        project.getDbLayer().commit();
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));
                    } catch (Exception e) {
                        project.getDbLayer().rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }
                }
            }
        }

        return equalizedAssemblyIds;
    }

    /**
     * Speichern einer {@link EtkDataObjectList} via aktivem {@link iPartsRevisionChangeSet}.
     *
     * @param dataObjectList
     * @param project
     * @return
     */
    private static boolean handleDataObjectListViaChangeSet(iPartsDataCombTextList dataObjectList, EtkProject project) {
        EtkRevisionsHelper revisionsHelper = project.getEtkDbs().getRevisionsHelper();
        if (revisionsHelper != null) {
            revisionsHelper.addDataObjectListToActiveChangeSetForEdit(dataObjectList);
            return true;
        }
        return false;
    }
}
