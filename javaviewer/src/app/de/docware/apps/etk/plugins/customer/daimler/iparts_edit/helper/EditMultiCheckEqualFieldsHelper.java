/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ASUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PartListEntryReferenceKeyByAA;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectAttribute;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Hilfsklasse für die Überprüfung der vererbten Attribute eines TUs
 */
public class EditMultiCheckEqualFieldsHelper {

    private static final EnumSet<SerializedDBDataObjectState> VALID_STATES = EnumSet.of(SerializedDBDataObjectState.NEW,
                                                                                        SerializedDBDataObjectState.REPLACED,
                                                                                        SerializedDBDataObjectState.MODIFIED,
                                                                                        SerializedDBDataObjectState.DELETED);

    private static int MAX_FOOTNOTE_PREVIEW_LENGTH = 50;

    private EtkProject project;
    private EtkMessageLogFormHelper messageLogHelper;
    private Map<String, CheckEqualContainer> workMap; // Map von GUID (ohne AA) auf CheckEqualContainer
    private PictureAndTUValidationEntryList validationEntries;
    private ASUsageHelper usageHelper;

    public EditMultiCheckEqualFieldsHelper(EtkProject project, EtkMessageLogFormHelper messageLogHelper) {
        this.project = project;
        this.messageLogHelper = messageLogHelper;
        this.workMap = new TreeMap<>();
        this.validationEntries = new PictureAndTUValidationEntryList();
        usageHelper = new ASUsageHelper(project);
    }

    public int getNumberOfChecks() {
        return workMap.size();
    }

    /**
     * Überprüft, ob es veränderte oder neue Stücklisteneinträge, kombinierte Texte oder Fußnoten in dem Modul gibt, die
     * geprüft werden müssen, ob deren Werte identisch sind mit den vererbten Werten in anderen Modulen.
     *
     * @param assembly
     * @param partListEntryList
     * @return
     */
    public boolean checkAssemblyForAsEqualFields(iPartsDataAssembly assembly, List<EtkDataPartListEntry> partListEntryList) {
        if (partListEntryList != null) {
            AbstractRevisionChangeSet activeChangeSet = getActiveChangeSet();
            if (assembly.isRetailPartList() && (activeChangeSet != null)) {
                Map<PartListEntryId, SerializedDBDataObject> newPartListEntryMap = new HashMap<>();
                Map<PartListEntryId, SerializedDBDataObject> modifiedPartListEntryMap = new HashMap<>();
                Set<PartListEntryId> entriesWithModifiedCombTexts = new HashSet<>();
                Set<PartListEntryId> entriesWithModifiedFootNotes = new HashSet<>();

                // die relevanten Feldnamen bestimmen
                EditEqualizeFieldsHelper fieldHelper = new EditEqualizeFieldsHelper(project);
                Collection<String> fieldNames = fieldHelper.getEqualizeFieldNames();

                Map<IdWithType, SerializedDBDataObject> serializedDataObjectsMap = activeChangeSet.getSerializedDataObjectsMap();
                calculateMaps(assembly.getAsId(), serializedDataObjectsMap, newPartListEntryMap, modifiedPartListEntryMap,
                              entriesWithModifiedCombTexts, entriesWithModifiedFootNotes, fieldNames);
                for (EtkDataPartListEntry partListEntry : partListEntryList) {
                    SerializedDBDataObjectState state;
                    SerializedDBDataObject serializedDBDataObject = newPartListEntryMap.get(partListEntry.getAsId());
                    if (serializedDBDataObject != null) {
                        state = SerializedDBDataObjectState.NEW;
                    } else {
                        serializedDBDataObject = modifiedPartListEntryMap.get(partListEntry.getAsId());
                        if (serializedDBDataObject != null) {
                            state = SerializedDBDataObjectState.MODIFIED;
                        } else {
                            fireProgress();
                            continue;
                        }
                    }
                    CheckEqualContainer container = new CheckEqualContainer(state, serializedDBDataObject, partListEntry,
                                                                            entriesWithModifiedCombTexts.contains(partListEntry.getAsId()),
                                                                            entriesWithModifiedFootNotes.contains(partListEntry.getAsId()));
                    if (container.getRefKey().isValid()) {
                        String guid = container.getRefKeyGuid();
                        // pro GUID ohne AA reicht ein Stücklisteneintrag
                        if (!workMap.containsKey(guid)) {
                            workMap.put(guid, container);
                        }
                    }
                    fireProgress();
                }
                return !workMap.isEmpty();
            }
        }
        return false;
    }

    /**
     * Führt die eigentliche Prüfung auf identische Werte bei allen vererbten Attributen inkl. kombinierter Texte und
     * Fußnoten durch.
     */
    public void doExecuteCheckForEqualFields() {
        validationEntries.clear();
        if (!workMap.isEmpty()) {
            EditEqualizeFieldsHelper fieldHelper = new EditEqualizeFieldsHelper(project);
            Collection<String> fieldNames = fieldHelper.getEqualizeFieldNames();
            for (CheckEqualContainer container : workMap.values()) {
                // Aktueller PartListEntry wurde im ChangeSet modifiziert oder ist neu
                // Attribute, die verändert wurden bzw. neu sind mit anderen AS-Stücklisten abgleichen
                // Falls Abgleich stattgefunden hat => Fehler, wenn sich mindestens ein Wert unterscheidet
                findModifiedAttributes(fieldHelper, fieldNames, container);
                checkModifiedAttributes(fieldHelper, container);
                fireProgress();
            }
        }
    }

    /**
     * Vergleicht die Fußnoten zweier PartListEntries via der {@link iPartsDataFootNoteCatalogueRefList}
     *
     * @param currentFnCatalogueRefList
     * @param foreignFnCatalogueRefList
     * @return
     */
    private boolean compareFootNotes(iPartsDataFootNoteCatalogueRefList currentFnCatalogueRefList,
                                     iPartsDataFootNoteCatalogueRefList foreignFnCatalogueRefList) {
        currentFnCatalogueRefList = filterRelevantFootNotes(currentFnCatalogueRefList);
        foreignFnCatalogueRefList = filterRelevantFootNotes(foreignFnCatalogueRefList);
        if (!(foreignFnCatalogueRefList.isEmpty() && currentFnCatalogueRefList.isEmpty())) {
            if (foreignFnCatalogueRefList.size() != currentFnCatalogueRefList.size()) {
                return false;
            } else {
                int index = 0;
                for (iPartsDataFootNoteCatalogueRef fnCatalogueRef : foreignFnCatalogueRefList) {
                    if (!fnCatalogueRef.getAsId().getFootNoteId().equals(currentFnCatalogueRefList.get(index).getAsId().getFootNoteId())) {
                        return false;
                    }
                    index++;
                }
            }
        }

        return true;
    }

    /**
     * Liefert nur die relevanten Fußnoten der übergebenen Fußnoten-Liste zurück, die auch vererbt werden müssen.
     *
     * @param fnCatalogueRefList
     * @return
     */
    private iPartsDataFootNoteCatalogueRefList filterRelevantFootNotes(iPartsDataFootNoteCatalogueRefList fnCatalogueRefList) {
        iPartsDataFootNoteCatalogueRefList relevantFnRefList = new iPartsDataFootNoteCatalogueRefList();
        for (iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef : fnCatalogueRefList) {
            if (!EditEqualizeFootNoteHelper.DONT_INHERIT_FOOTNOTE_NUMBERS.contains(dataFootNoteCatalogueRef.getAsId().getFootNoteId())) {
                relevantFnRefList.add(dataFootNoteCatalogueRef, DBActionOrigin.FROM_DB);
            }
        }
        return relevantFnRefList;
    }

    /**
     * Überprüft die im ChangeSet modifizierten und neuen Attribute des aktuellen PartListEntries.
     * Sollten hier Differenzen zu den AS-Verwendungen auftreten, wird ein Fehler ausgegeben.
     *
     * @param fieldHelper
     * @param container
     */
    public void checkModifiedAttributes(EditEqualizeFieldsHelper fieldHelper, CheckEqualContainer container) {
        List<EtkDataPartListEntry> asPartListEntries = usageHelper.getPartListEntriesUsedInAsForEqualizeWithoutPSKEntries(container.getPartListEntryForEdit(), true);

        // Den zu überprüfenden Stücklisteneintrag aus den asPartListEntries entfernen, damit die Attribute nicht mit sich
        // selbst verglichen werden
        Iterator<EtkDataPartListEntry> iterator = asPartListEntries.iterator();
        while (iterator.hasNext()) {
            EtkDataPartListEntry partListEntry = iterator.next();
            if (partListEntry.getAsId().equals(container.getPartListEntryForEdit().getAsId())) {
                iterator.remove();
            }
        }

        // Wurden Attribute geändert oder sind diese neu und gibt es überhaupt andere AS-Stücklisten?
        if (!asPartListEntries.isEmpty() && fieldHelper.isModified()) {
            List<IdWithType> inChangeSetStoredIds = new DwList<>();
            GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();

            // Callback-Routine für kombinierten Text und Coderegeln
            OnEqualizePartListEntryEvent onEqualizePartListEntryEvent =
                    EditEqualizeFieldsHelper.createOnEqualizePartListEntryEvent(modifiedDataObjects,
                                                                                container.getCombTexts(),
                                                                                container.getFootNoteCatalogueRefList(),
                                                                                inChangeSetStoredIds);
            fieldHelper.setOnEqualizePartListEntryEvent(onEqualizePartListEntryEvent);

            // Nur mit Kopien der Stücklisteneinträge arbeiten, damit die Originale nicht verändert werden
            List<EtkDataPartListEntry> asPartListEntriesForTest = new DwList<>();
            for (EtkDataPartListEntry partListEntry : asPartListEntries) {
                asPartListEntriesForTest.add(partListEntry.cloneMe(project));
            }

            fieldHelper.equalizePartListEntry(container.getPartListEntryForEdit(), asPartListEntriesForTest);
            Set<AssemblyId> equalizedAssemblyIds =
                    EditEqualizeFieldsHelper.finishEqualizePartListEntry(project, container.getPartListEntryForEdit(),
                                                                         modifiedDataObjects, inChangeSetStoredIds,
                                                                         asPartListEntriesForTest);
            if ((equalizedAssemblyIds != null) && !equalizedAssemblyIds.isEmpty()) {
                // Fehler ausgeben
                addErrorForUsedModifiedAttributes(fieldHelper, container, asPartListEntries, equalizedAssemblyIds, modifiedDataObjects);
            }
        }
    }

    /**
     * Aus den Einträgen im ChangeSet werden die modifizierten bzw. neuen Attribute bestimmt
     *
     * @param fieldHelper
     * @param fieldNames
     * @param container
     */
    public void findModifiedAttributes(EditEqualizeFieldsHelper fieldHelper, Collection<String> fieldNames, CheckEqualContainer container) {
        fieldHelper.clearModified();
        SerializedDBDataObject serializedDBDataObject = container.getSerializedDBDataObject();
        if (serializedDBDataObject != null) {
            Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
            if (attributes != null) {
                for (SerializedDBDataObjectAttribute dbDataObjectAttribute : attributes) {
                    // Nicht veränderte Attribute ignorieren
                    if (!dbDataObjectAttribute.isNotModified() && fieldNames.contains(dbDataObjectAttribute.getName())) {
                        fieldHelper.setModified(dbDataObjectAttribute.getName());
                    }
                }
            }
        }
        if (container.isCombTextsModified()) {
            fieldHelper.setModified(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);
        }
        if (container.isFootNotesModified()) {
            fieldHelper.setModified(iPartsConst.FIELD_DFNK_FNID);
        }
    }

    public PictureAndTUValidationEntryList getValidationEntries() {
        return validationEntries;
    }

    /**
     * Zerlegt die Einträge im ChangeSet nach neuen und modifizierten PartListEntry-Änderungen.
     * Modifizierte PartListEntries werden nur aufgenommen, falls eines der relevanten Felder modifiziert wurde.
     * Neue PartListEntries werden immer aufgenommen.
     * Ebenso werden die Einträge für kombinierte Texte und Fußnoten aufgesammelt.
     * Aufgesammelt werden nur ChangeSet-Einträge, die sich auf die aktuelle AssemblyId beziehen.
     *
     * @param assemblyId
     * @param serializedDataObjectsMap
     * @param newPartListEntryMap
     * @param modifiedPartListEntryMap
     * @param entriesWithModifiedCombTexts
     * @param entriesWithModifiedFootNotes
     * @param fieldNames
     */
    private void calculateMaps(AssemblyId assemblyId, Map<IdWithType, SerializedDBDataObject> serializedDataObjectsMap,
                               Map<PartListEntryId, SerializedDBDataObject> newPartListEntryMap,
                               Map<PartListEntryId, SerializedDBDataObject> modifiedPartListEntryMap,
                               Set<PartListEntryId> entriesWithModifiedCombTexts,
                               Set<PartListEntryId> entriesWithModifiedFootNotes,
                               Collection<String> fieldNames) {
        newPartListEntryMap.clear();
        modifiedPartListEntryMap.clear();
        entriesWithModifiedCombTexts.clear();
        entriesWithModifiedFootNotes.clear();
        for (Map.Entry<IdWithType, SerializedDBDataObject> entry : serializedDataObjectsMap.entrySet()) {
            SerializedDBDataObject serializedObject = entry.getValue();
            if (VALID_STATES.contains(serializedObject.getState())) {
                IdWithType entryId = entry.getKey();
                if (entryId.getType().equals(PartListEntryId.TYPE) && (serializedObject.getState() != SerializedDBDataObjectState.DELETED)) {
                    // PartListEntry (gelöschte ignorieren)
                    PartListEntryId partListEntryId = new PartListEntryId(serializedObject.getPkValues());
                    AssemblyId id = partListEntryId.getOwnerAssemblyId();
                    if (id.equals(assemblyId)) {
                        if (serializedObject.getState() == SerializedDBDataObjectState.NEW) {
                            // Neuer Eintrag
                            newPartListEntryMap.put(partListEntryId, serializedObject);
                        } else {
                            // modifizierter Eintrag
                            if (checkAttributesInSerializedObject(serializedObject, fieldNames)) {
                                modifiedPartListEntryMap.put(partListEntryId, serializedObject);
                            }
                        }
                    }
                } else if (entryId.getType().equals(iPartsCombTextId.TYPE)) {
                    // CombText
                    PartListEntryId partListEntryId = new iPartsCombTextId(serializedObject.getPkValues()).getPartListEntryId();
                    AssemblyId id = partListEntryId.getOwnerAssemblyId();
                    if (id.equals(assemblyId)) {
                        entriesWithModifiedCombTexts.add(partListEntryId);
                    }
                } else if (entryId.getType().equals(iPartsFootNoteCatalogueRefId.TYPE)) {
                    // FootNote
                    PartListEntryId partListEntryId = new iPartsFootNoteCatalogueRefId(serializedObject.getPkValues()).getPartListEntryId();
                    AssemblyId id = partListEntryId.getOwnerAssemblyId();
                    if (id.equals(assemblyId)) {
                        entriesWithModifiedFootNotes.add(partListEntryId);
                    }
                }
            }
        }
    }

    private boolean checkAttributesInSerializedObject(SerializedDBDataObject serializedObject, Collection<String> fieldNames) {
        if (serializedObject != null) {
            Collection<SerializedDBDataObjectAttribute> attributes = serializedObject.getAttributes();
            if (attributes != null) {
                for (SerializedDBDataObjectAttribute dbDataObjectAttribute : attributes) {
                    // Nicht veränderte Attribute ignorieren
                    if (!dbDataObjectAttribute.isNotModified() && fieldNames.contains(dbDataObjectAttribute.getName())) {
                        return true;
                    }
                }
            } else {
                // kann bei Änderungen an kombinierten Texten und Fußnoten vorkommen
                return true;
            }
        }
        return false;
    }

    private AbstractRevisionChangeSet getActiveChangeSet() {
        EtkRevisionsHelper revisionsHelper = project.getEtkDbs().getRevisionsHelper();
        if ((revisionsHelper != null) && revisionsHelper.isRevisionChangeSetActive()) {
            AbstractRevisionChangeSet activeChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
            if (activeChangeSet == null) { // Kein Edit-ChangeSet aktiv -> es ist ein readOnly-ChangeSet aktiv
                activeChangeSet = revisionsHelper.getActiveRevisionChangeSets().iterator().next();
            }
            return activeChangeSet;
        }
        return null;
    }

    private void addErrorForUsedModifiedAttributes(EditEqualizeFieldsHelper fieldHelper, CheckEqualContainer container,
                                                   List<EtkDataPartListEntry> asPartListEntries,
                                                   Set<AssemblyId> equalizedAssemblyIds,
                                                   GenericEtkDataObjectList modifiedDataObjects) {
        StringBuilder info = new StringBuilder();
        for (AssemblyId assemblyId : equalizedAssemblyIds) {
            if (info.length() > 0) {
                info.append("\n");
            }
            EtkDataPartListEntry dataPartListEntry = findPartListEntry(modifiedDataObjects, assemblyId);
            if (dataPartListEntry != null) {
                boolean isModified = false;
                StringBuilder assemblyInfo = new StringBuilder();
                boolean appendNewLine = false;
                for (String fieldName : fieldHelper.getEqualizeFieldNames()) {
                    if ((assemblyInfo.length() > 0) && appendNewLine) {
                        assemblyInfo.append("\n");
                        appendNewLine = false;
                    }
                    if (fieldName.equals(iPartsConst.FIELD_DFNK_FNID)) {
                        // Fußnoten
                        String sourceDialogGUID = container.getPartListEntryForEdit().getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                        String destDialogGUID = dataPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                        if (sourceDialogGUID.equals(destDialogGUID)) { // AA muss auch übereinstimmen
                            iPartsDataFootNoteCatalogueRefList foreignFnCatalogueRefList = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntry(project,
                                                                                                                                                            dataPartListEntry.getAsId());

                            if (!compareFootNotes(container.getFootNoteCatalogueRefList(), foreignFnCatalogueRefList)) {
                                assemblyInfo.append(TranslationHandler.translate("!!- Fußnoten:"));
                                assemblyInfo.append(" ");
                                assemblyInfo.append(addVisFootNotesDiff(container.getFootNoteCatalogueRefList(), foreignFnCatalogueRefList));
                                appendNewLine = true;
                                isModified = true;
                            }
                        }
                    } else {
                        String value = container.getPartListEntryForEdit().getFieldValue(fieldName);
                        EtkDataPartListEntry foreignPartListEntry = findPartListEntry(asPartListEntries, dataPartListEntry.getAsId());
                        if (foreignPartListEntry != null) {
                            String foreignValue = foreignPartListEntry.getFieldValue(fieldName);
                            if (!value.equals(foreignValue)) {
                                addVisAttribute(assemblyInfo, fieldName, container.getPartListEntryForEdit(), foreignPartListEntry);
                                isModified = true;
                                appendNewLine = true;
                            }
                        }
                    }
                }
                if (isModified) {
                    info.append(TranslationHandler.translate("!!Vererbte Werte im TU \"%1\" bei %2 <> Werte in diesem TU:",
                                                             getVisAssemblyId(assemblyId), getVisPartListEntryLfdNr(dataPartListEntry)));
                    info.append("\n");
                    info.append(assemblyInfo);
                    info.append("\n");
                }
            } else { // Sollte eigentlich nicht passieren
                info.append(TranslationHandler.translate("!!Vererbte Werte im TU \"%1\" <> Werte in diesem TU:", getVisAssemblyId(assemblyId)));
                StringBuilder assemblyInfo = new StringBuilder();
                for (String fieldName : fieldHelper.getEqualizeFieldNames()) {
                    if (fieldHelper.isModified(fieldName)) {
                        if (assemblyInfo.length() > 0) {
                            assemblyInfo.append(", ");
                        }
                        if (fieldName.equals(iPartsConst.FIELD_DFNK_FNID)) {
                            // Fußnoten
                            assemblyInfo.append(TranslationHandler.translate("!!Fußnoten"));
                        } else {
                            assemblyInfo.append(getVisFieldName(fieldName));
                        }
                    }
                }
                info.append(assemblyInfo);
                info.append("\n");
            }
        }

        // Gibt es wirklich echte Unterschiede? Durch 414er Fußnoten kann es hier zu Phantom-Diffs kommen.
        if (info.length() > 0) {
            validationEntries.addError(container.getPartListEntryForEdit().getAsId(),
                                       TranslationHandler.translate("!!Vererbte Werte unterscheiden sich von denen in anderen TUs"));
            validationEntries.getLast().setAdditionalInformation(info.toString());
        }
    }

    private EtkDataPartListEntry findPartListEntry(List<EtkDataPartListEntry> asPartListEntries, PartListEntryId partListEntryId) {
        for (EtkDataPartListEntry dataPartListEntry : asPartListEntries) {
            if (dataPartListEntry.getAsId().equals(partListEntryId)) {
                return dataPartListEntry;
            }
        }
        return null;
    }

    private EtkDataPartListEntry findPartListEntry(GenericEtkDataObjectList modifiedDataObjects, AssemblyId assemblyId) {
        for (Object dataObject : modifiedDataObjects.getAsList()) {
            if (dataObject instanceof EtkDataPartListEntry) {
                EtkDataPartListEntry dataPartListEntry = (EtkDataPartListEntry)dataObject;
                if (dataPartListEntry.getAsId().getOwnerAssemblyId().equals(assemblyId)) {
                    return dataPartListEntry;
                }
            }
        }
        return null;
    }

    private String addVisFootNotesDiff(iPartsDataFootNoteCatalogueRefList currentFnCatalogueRefList,
                                       iPartsDataFootNoteCatalogueRefList foreignFnCatalogueRefList) {
        StringBuilder builder = new StringBuilder();
        addVisFootNotes(builder, foreignFnCatalogueRefList);
        builder.append(" <> ");
        addVisFootNotes(builder, currentFnCatalogueRefList);
        return builder.toString();
    }

    private void addVisFootNotes(StringBuilder builder, iPartsDataFootNoteCatalogueRefList fnCatalogueRefList) {
        List<String> footNotes = new DwList<>();
        if ((fnCatalogueRefList == null) || fnCatalogueRefList.isEmpty()) {
            footNotes.add(TranslationHandler.translate("!!Keine"));
        } else {
            String dbLanguage = project.getDBLanguage();
            List<String> dbFallbackLanguages = project.getDataBaseFallbackLanguages();
            for (iPartsDataFootNoteCatalogueRef fnCatalogueRef : fnCatalogueRefList) {
                String footNoteId = fnCatalogueRef.getAsId().getFootNoteId();
                iPartsDataFootNoteContentList dataFootNoteContents = iPartsDataFootNoteContentList.loadFootNote(project,
                                                                                                                footNoteId);
                String footNoteText;
                if (!dataFootNoteContents.isEmpty()) { // Ersten Fußnotentext gekürzt ausgeben
                    String firstFootNotText = dataFootNoteContents.get(0).getText(dbLanguage, dbFallbackLanguages).replace("\n", " ");
                    footNoteText = "\"" + StrUtils.copySubString(firstFootNotText, 0, MAX_FOOTNOTE_PREVIEW_LENGTH);

                    // Ist der Fußnotentext eigentlich länger oder hat mehr als eine Zeile?
                    if ((dataFootNoteContents.size() > 1) || (firstFootNotText.length() > MAX_FOOTNOTE_PREVIEW_LENGTH)) {
                        footNoteText += "...";
                    }

                    footNoteText += "\"";
                } else {
                    footNoteText = "(" + footNoteId + ")";
                }
                footNotes.add(footNoteText);
            }
        }
        builder.append(StrUtils.stringListToString(footNotes, ", "));
    }

    private void addVisAttribute(StringBuilder builder, String fieldName, EtkDataPartListEntry currentPartListEntry,
                                 EtkDataPartListEntry foreignPartListEntry) {
        builder.append("- ");
        builder.append(getVisFieldName(fieldName));
        builder.append(": ");
        String value = currentPartListEntry.getFieldValue(fieldName);
        String foreignValue = foreignPartListEntry.getFieldValue(fieldName);
        addVisAttributeDiff(builder, value, foreignValue);
    }

    private void addVisAttributeDiff(StringBuilder builder, String value, String foreignValue) {
        builder.append("\"");
        builder.append(foreignValue);
        builder.append("\" <> \"");
        builder.append(value);
        builder.append("\"");
    }

    private String getVisFieldName(String fieldName) {
        String visFieldName = fieldName;
        EtkDatabaseField dbField = project.getConfig().getFieldDescription(iPartsConst.TABLE_KATALOG, fieldName);
        if (dbField != null) {
            visFieldName = dbField.getDisplayText(project.getDBLanguage(), project.getDataBaseFallbackLanguages());
        }
        return visFieldName;
    }

    private String getVisAssemblyId(AssemblyId assemblyId) {
        return assemblyId.getKVari();
    }

    private String getVisPartListEntryLfdNr(EtkDataPartListEntry partListEntry) {
        return partListEntry.getAsId().getKLfdnr();
    }

    protected void fireMessage(String message) {
        if (messageLogHelper != null) {
            messageLogHelper.fireMessage(message);
        }
    }

    protected void fireProgress() {
        if (messageLogHelper != null) {
            messageLogHelper.fireProgress();
        }
    }


    /**
     * Datenklasse für alle relevanten Daten für den Vergleich der vererbeten Werte eines Stücklisteneintrags
     */
    private class CheckEqualContainer {

        private SerializedDBDataObjectState state;
        private SerializedDBDataObject serializedDBDataObject;
        private PartListEntryReferenceKeyByAA refKey;
        private EtkDataPartListEntry partListEntryForEdit;
        private boolean combTextsModified;
        private boolean footNotesModified;
        private iPartsDataCombTextList combTexts;
        private iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefList;

        public CheckEqualContainer(SerializedDBDataObjectState state, SerializedDBDataObject serializedDBDataObject,
                                   EtkDataPartListEntry partListEntryForEdit, boolean combTextsModified, boolean footNotesModified) {
            this.state = state;
            this.serializedDBDataObject = serializedDBDataObject;
            this.partListEntryForEdit = partListEntryForEdit;
            this.refKey = new PartListEntryReferenceKeyByAA(partListEntryForEdit);
            this.combTextsModified = combTextsModified;
            this.footNotesModified = footNotesModified;
        }

        public SerializedDBDataObjectState getState() {
            return state;
        }

        public SerializedDBDataObject getSerializedDBDataObject() {
            return serializedDBDataObject;
        }

        public PartListEntryReferenceKeyByAA getRefKey() {
            return refKey;
        }

        public String getRefKeyGuid() {
            return refKey.getBcteKeyWithoutAA().createDialogGUID();
        }

        public EtkDataPartListEntry getPartListEntryForEdit() {
            return partListEntryForEdit;
        }

        public boolean isCombTextsModified() {
            return combTextsModified;
        }

        public boolean isFootNotesModified() {
            return footNotesModified;
        }

        public iPartsDataCombTextList getCombTexts() {
            // Lazy-loading falls die kombinierten Texte gar nicht benötigt werden
            if (combTexts == null) {
                if (!getPartListEntryForEdit().getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT).isEmpty()) {
                    combTexts = iPartsDataCombTextList.loadForPartListEntry(getPartListEntryForEdit().getAsId(), project);
                } else {
                    combTexts = new iPartsDataCombTextList();
                }
            }

            return combTexts;
        }

        public iPartsDataFootNoteCatalogueRefList getFootNoteCatalogueRefList() {
            // Lazy-loading falls die Fußnoten gar nicht benötigt werden
            if (footNoteCatalogueRefList == null) {
                if ((getPartListEntryForEdit() instanceof iPartsDataPartListEntry) && ((iPartsDataPartListEntry)getPartListEntryForEdit()).hasFootNotes()) {
                    footNoteCatalogueRefList = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntry(project,
                                                                                                                getPartListEntryForEdit().getAsId());
                } else {
                    footNoteCatalogueRefList = new iPartsDataFootNoteCatalogueRefList();
                }
            }
            return footNoteCatalogueRefList;
        }
    }
}
