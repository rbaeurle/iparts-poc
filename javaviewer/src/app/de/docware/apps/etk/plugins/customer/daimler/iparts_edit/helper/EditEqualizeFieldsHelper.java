/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ASUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserMultiChangeControlsForASPartlistEntry;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Hilfsklasse für die Vererbung bestimmter Attribut-Werte vor dem Speichern in Edit
 * Es sind Hilfsmethoden für PartList-Edit als auch für Inplace-Editoren vorhanden.
 * <p>
 * Vorgehensweise:
 * - alle festgelegten Attribute auf Änderungen überprüfen und merken
 * - bei Änderungen: Suche AS-Stücklisteneinträge mit gleichem BCTE-Key ohne AA
 * - Vererbe die geänderten Attributwerte an die gefundenen Stücklisteneinträge
 * (hierbei werden nur die ChangeSet-Einträge erzeugt und in einer Liste gesammelt)
 */
public class EditEqualizeFieldsHelper {

    private EtkProject project;
    private Map<String, EqualizeFieldElem> elems = new LinkedHashMap<>();
    private OnEqualizePartListEntryEvent onEqualizePartListEntryEvent;


    /**
     * Handelt es sich überhaupt um eine DIALOG-Stückliste?
     *
     * @param partListEntryForEdit
     * @return
     */
    public static boolean isDIALOGPartListEntry(EtkDataPartListEntry partListEntryForEdit) {
        EtkDataAssembly ownerAssembly = partListEntryForEdit.getOwnerAssembly();
        if (ownerAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)ownerAssembly;

            // Hier PSK_PKW und PSK-Produkte explizit NICHT zulassen
            return iPartsAssembly.getDocumentationType().isDIALOGDocumentationType() && !iPartsAssembly.isPSKAssembly();
        }
        return false;
    }

    /**
     * Vererbung der Änderungen für die restlichen Inplace-Editoren (außer für kombinierte Texte)
     * Vererbt wird auf alle Retail-Stücklisteneinträge mit gleichem BCTE-Schlüssel ohne Berücksichtigung von AA
     *
     * @param project
     * @param partListEntryForEdit
     * @return Alle durch Vererbung veränderte Assemblies; kann auch {@code null} sein
     */
    public static Set<AssemblyId> doEqualizeForInplaceEditor(EtkProject project, EtkDataPartListEntry partListEntryForEdit) {
        if (!isDIALOGPartListEntry(partListEntryForEdit)) {
            return null;
        }

        GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();
        Set<AssemblyId> equalizedAssemblyIds = EditEqualizeFieldsHelper.doEqualizePartListEntryEdit(project, partListEntryForEdit,
                                                                                                    null, null,
                                                                                                    modifiedDataObjects);
        if (!modifiedDataObjects.isEmpty()) {
            EtkRevisionsHelper revisionsHelper = project.getEtkDbs().getRevisionsHelper();
            if (revisionsHelper != null) {
                revisionsHelper.addDataObjectListToActiveChangeSetForEdit(modifiedDataObjects);
            }
            return equalizedAssemblyIds;
        }

        return null;
    }

    /**
     * Vererbung der Änderungen des Inplace-Editors für kombinierte Texte
     * Vererbt wird auf alle Retail-Stücklisteneinträge mit gleichem BCTE-Schlüssel ohne Berücksichtigung von AA
     *
     * @param project
     * @param partListEntryForEdit
     * @param currentCombTexts
     * @param modifiedDataObjects
     * @return Alle durch Vererbung veränderte Assemblies; kann auch {@code null} sein
     */
    public static Set<AssemblyId> doEqualizeCombTextInplaceEditor(EtkProject project, EtkDataPartListEntry partListEntryForEdit,
                                                                  final iPartsDataCombTextList currentCombTexts,
                                                                  final GenericEtkDataObjectList modifiedDataObjects) {
        if (!isDIALOGPartListEntry(partListEntryForEdit)) {
            return null;
        }

        final List<IdWithType> inChangeSetStoredIds = new DwList<>();
        EditEqualizeFieldsHelper equalizeHelper = new EditEqualizeFieldsHelper(project);

        // Callback-Routine für kombinierten Text
        OnEqualizePartListEntryEvent onEqualizePartListEntryEvent = createOnEqualizePartListEntryEvent(modifiedDataObjects,
                                                                                                       currentCombTexts,
                                                                                                       null, inChangeSetStoredIds);
        equalizeHelper.setOnEqualizePartListEntryEvent(onEqualizePartListEntryEvent);

        // nur speziell kombinierte Texte
        equalizeHelper.setModified(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);

        // Auf das Ergebnis von prepareEqualizeAttributsInASList kann man verzichten, da nur der kombinierte Text
        // behandelt wird und die Einträge in modifiedDataObjects bereits erfolgt sind
        List<EtkDataPartListEntry> asPartListEntries = equalizeHelper.equalizeAttributesInASPartListEntries(partListEntryForEdit);
        return finishEqualizePartListEntry(project, partListEntryForEdit, modifiedDataObjects, inChangeSetStoredIds, asPartListEntries);
    }

    /**
     * Vererbung der Änderungen des Fußnoten-Editors
     * Vererbt wird auf alle Retail-Stücklisteneinträge mit absolut gleichem BCTE-Schlüssel
     *
     * @param project
     * @param partListEntryForEdit
     * @param fnCatalogueRefs
     * @param modifiedDataObjects
     * @return Alle durch Vererbung veränderte Assemblies; kann auch {@code null} sein
     */
    public static Set<AssemblyId> doEqualizeFootNoteEditor(EtkProject project, EtkDataPartListEntry partListEntryForEdit,
                                                           iPartsDataFootNoteCatalogueRefList fnCatalogueRefs,
                                                           GenericEtkDataObjectList modifiedDataObjects) {
        if (!isDIALOGPartListEntry(partListEntryForEdit)) {
            return null;
        }

        // Gibt es überhaupt was zu tun?
        if ((fnCatalogueRefs == null) || (fnCatalogueRefs.isEmpty() && fnCatalogueRefs.getDeletedList().isEmpty())) {
            return null;
        }

        final List<IdWithType> inChangeSetStoredIds = new DwList<>();
        EditEqualizeFieldsHelper equalizeHelper = new EditEqualizeFieldsHelper(project);

        // Callback-Routine für das Vererben
        OnEqualizePartListEntryEvent onEqualizePartListEntryEvent = createOnEqualizePartListEntryEvent(modifiedDataObjects,
                                                                                                       null, fnCatalogueRefs,
                                                                                                       inChangeSetStoredIds);
        equalizeHelper.setOnEqualizePartListEntryEvent(onEqualizePartListEntryEvent);

        // nur speziell Fußnoten
        equalizeHelper.setModified(iPartsConst.FIELD_DFNK_FNID);

        // Auf das Ergebnis von prepareEqualizeAttributsInASList kann man verzichten, da nur die Fußnoten-Referenzen
        // behandelt werden und die Einträge in modifiedDataObjects bereits erfolgt sind
        List<EtkDataPartListEntry> asPartListEntries = equalizeHelper.equalizeAttributesInASPartListEntries(partListEntryForEdit);
        return finishEqualizePartListEntry(project, partListEntryForEdit, modifiedDataObjects, inChangeSetStoredIds, asPartListEntries);
    }

    /**
     * Vererbung der Werte beim PartListEntry-Edit
     *
     * @param project
     * @param partListEntryForEdit
     * @param currentCombTexts
     * @param footNoteCatalogueRefList
     * @param modifiedDataObjects
     * @return Alle durch Vererbung veränderte Assemblies; kann auch {@code null} sein
     */
    public static Set<AssemblyId> doEqualizePartListEntryEdit(EtkProject project, EtkDataPartListEntry partListEntryForEdit,
                                                              final iPartsDataCombTextList currentCombTexts,
                                                              iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefList,
                                                              final GenericEtkDataObjectList modifiedDataObjects) {
        if (!isDIALOGPartListEntry(partListEntryForEdit)) {
            return null;
        }

        // List der bereits zum ChangeSet hinzugefügten Id's (keine doppelten Einträge im ChangeSet)
        final List<IdWithType> inChangeSetStoredIds = new DwList<>();
        EditEqualizeFieldsHelper equalizeHelper = new EditEqualizeFieldsHelper(project);

        // Callback-Routine für kombinierten Text und Coderegeln
        OnEqualizePartListEntryEvent onEqualizePartListEntryEvent = createOnEqualizePartListEntryEvent(modifiedDataObjects,
                                                                                                       currentCombTexts,
                                                                                                       footNoteCatalogueRefList, inChangeSetStoredIds);
        equalizeHelper.setOnEqualizePartListEntryEvent(onEqualizePartListEntryEvent);

        // Suche nach Änderungen und passenden AS-Stücklisten-Einträgen sowie Vererbung durchführen
        List<EtkDataPartListEntry> asPartListEntries = equalizeHelper.checkForModificationsAndEqualizeAttributes(partListEntryForEdit);
        return finishEqualizePartListEntry(project, partListEntryForEdit, modifiedDataObjects, inChangeSetStoredIds, asPartListEntries);
    }

    /**
     * Erzeugt einen {@link OnEqualizePartListEntryEvent} zur Vererbung von komplexen Spezial-Attributen.
     *
     * @param modifiedDataObjects
     * @param currentCombTexts
     * @param fnCatalogueRefs
     * @param inChangeSetStoredIds
     * @return
     */
    public static OnEqualizePartListEntryEvent createOnEqualizePartListEntryEvent(final GenericEtkDataObjectList modifiedDataObjects,
                                                                                  final iPartsDataCombTextList currentCombTexts,
                                                                                  final iPartsDataFootNoteCatalogueRefList fnCatalogueRefs,
                                                                                  final List<IdWithType> inChangeSetStoredIds) {
        return new OnEqualizePartListEntryEvent() {
            @Override
            public boolean isModifiedSpecialAttribute(EtkProject project, String fieldName, EtkDataPartListEntry partListEntry) {
                // Überprüfung, ob sich der kombinierte Text, die Coderegel oder die Menge geändert hat
                if ((fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT) && (currentCombTexts != null))
                    || fieldName.equals(iPartsConst.FIELD_K_CODES) || fieldName.equals(iPartsConst.FIELD_K_MENGE)) {
                    DBDataObjectAttribute attribute = partListEntry.getAttribute(fieldName, false);
                    if (attribute != null) {
                        return attribute.isModified();
                    }
                } else if (fieldName.equals(iPartsConst.FIELD_DFNK_FNID) && (fnCatalogueRefs != null)) {
                    // Sind Änderungen bei den Fußnoten-Referenzen vorgenommen worden?
                    return !fnCatalogueRefs.isEmpty() || !fnCatalogueRefs.getDeletedList().isEmpty();
                }
                return false;
            }

            @Override
            public void onEqualizePartListEntry(EtkProject project, String fieldName, EtkDataPartListEntry sourcePartListEntry,
                                                EtkDataPartListEntry destPartListEntry, DBDataObjectAttributes destAttributes) {
                if (fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT)) {
                    // Vererbung des modifizierten kombinierten Textes an einen anderen Stücklisten-Eintrag
                    if (currentCombTexts != null) {
                        // Abgleich der einzelnen kombinierten Texte wie bei Vereinheitlichen
                        EtkDataObjectList objectList =
                                EditUserMultiChangeControlsForASPartlistEntry.checkCombTextsForPartlistEntry(project, destPartListEntry,
                                                                                                             currentCombTexts);
                        // Check, ob es neue, geänderte oder gelöschte Ergänzungstexte gibt
                        if (objectList.isModifiedWithChildren()) {
                            // Kombinierte Text-Liste in ChangeSet-Liste hinterlegen
                            modifiedDataObjects.addOnlyModifiedAndDeletedDataObjects(objectList, DBActionOrigin.FROM_EDIT);
                            addAssemblyAndPartListEntryToChangeSetList(project, destPartListEntry, inChangeSetStoredIds,
                                                                       modifiedDataObjects, true);
                        }
                    }
                } else if (fieldName.equals(iPartsConst.FIELD_K_CODES)) {
                    DBDataObjectAttributes sourceAttribs = sourcePartListEntry.getAttributes();
                    copyAttributeValue(fieldName, sourceAttribs, destAttributes, sourcePartListEntry);
                    copyAttributeValue(iPartsConst.FIELD_K_CODES_REDUCED, sourceAttribs, destAttributes, sourcePartListEntry);
                } else if (fieldName.equals(iPartsConst.FIELD_DFNK_FNID)) {
                    // Behandlung der Fußnoten-Referenzen
                    if (fnCatalogueRefs != null) {
                        String sourceDialogGUID = sourcePartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                        String destDialogGUID = destPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                        // für Fußnoten müssen die GUIDs übereinstimmen
                        if (sourceDialogGUID.equals(destDialogGUID)) {
                            // Fußnoten vererben
                            iPartsDataFootNoteCatalogueRefList refList = EditEqualizeFootNoteHelper.equalizeOnePartListEntryFootNote(project,
                                                                                                                                     fnCatalogueRefs,
                                                                                                                                     destPartListEntry);
                            if (!refList.isEmpty() || !refList.getDeletedList().isEmpty()) {
                                // Fußnoten-Referenzen in ChangeSet-Liste hinterlegen
                                modifiedDataObjects.addAll(refList, DBActionOrigin.FROM_EDIT);
                                // ggf. Assemblies und PartListEntries ins ChangeSet eintragen
                                addAssemblyAndPartListEntryToChangeSetList(project, destPartListEntry, inChangeSetStoredIds,
                                                                           modifiedDataObjects, true);
                            }
                        }
                    }
                } else if (fieldName.equals(iPartsConst.FIELD_K_MENGE)) {
                    // Daimler-9891: AS-Menge nur veerben, wenn die Stücklisteneinträge, die gleiche AA haben
                    String sourceDialogGUID = sourcePartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                    String destDialogGUID = destPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                    if (sourceDialogGUID.equals(destDialogGUID)) {
                        copyAttributeValue(iPartsConst.FIELD_K_MENGE, sourcePartListEntry, destPartListEntry);
                    }
                }
            }
        };
    }

    public static Set<AssemblyId> finishEqualizePartListEntry(EtkProject project, EtkDataPartListEntry partListEntryForEdit,
                                                              GenericEtkDataObjectList modifiedDataObjects, List<IdWithType> inChangeSetStoredIds,
                                                              List<EtkDataPartListEntry> asPartListEntries) {
        // alle Änderungen an Attributen in ChangeSet-Liste eintragen
        if (!asPartListEntries.isEmpty()) {
            // Zu den modifiedDataObjects hinzufügen inkl. der Assemblies
            for (EtkDataPartListEntry partListEntry : asPartListEntries) {
                if (partListEntry.isModifiedWithChildren()) {
                    // Flag "Automatisch erzeugt" muss bei Edit-Aktionen zurückgesetzt werden
                    iPartsDataPartListEntry.resetAutoCreatedFlag(partListEntry);
                    addAssemblyAndPartListEntryToChangeSetList(project, partListEntry, inChangeSetStoredIds, modifiedDataObjects, false);
                }
            }

            return getEqualizedAssemblyIds(partListEntryForEdit, inChangeSetStoredIds);
        }

        return null;
    }

    /**
     * Liefert alle {@link AssemblyId}s zurück, an die vererbt wurde.
     *
     * @param partListEntry
     * @param inChangeSetStoredIds
     */
    private static Set<AssemblyId> getEqualizedAssemblyIds(EtkDataPartListEntry partListEntry, List<IdWithType> inChangeSetStoredIds) {
        Set<AssemblyId> equalizedAssemblyIds = new HashSet<>();
        if (!inChangeSetStoredIds.isEmpty()) {
            AssemblyId masterAssemblyId = partListEntry.getOwnerAssemblyId();
            for (IdWithType id : inChangeSetStoredIds) {
                if (id.getType().equals(AssemblyId.TYPE)) {
                    if (!id.equals(masterAssemblyId)) {
                        equalizedAssemblyIds.add((AssemblyId)id);
                    }
                } else if (id.getType().equals(PartListEntryId.TYPE)) {
                    // Wurden neben dem aktiv bearbeiteten Stücklisteneintrag auch andere Stücklisteneinträge des im Edit
                    // befindlichen Moduls verändert, muss das Edit-Modul ebenfalls im Edit neu geladen werden
                    PartListEntryId partListEntryId = (PartListEntryId)id;
                    AssemblyId assemblyId = partListEntryId.getOwnerAssemblyId();
                    if (assemblyId.equals(masterAssemblyId) && !partListEntryId.equals(partListEntry.getAsId())) {
                        equalizedAssemblyIds.add(assemblyId);
                    }
                }
            }
        }

        return equalizedAssemblyIds;
    }

    private static void addAssemblyAndPartListEntryToChangeSetList(EtkProject project, EtkDataPartListEntry partListEntry,
                                                                   List<IdWithType> inChangeSetStoredIds, GenericEtkDataObjectList modifiedDataObjects,
                                                                   boolean markPartListEntryAsModified) {
        addAssemblyToChangeSetList(project, partListEntry, inChangeSetStoredIds, modifiedDataObjects);
        addPartListEntryIdToChangeSetList(partListEntry, inChangeSetStoredIds, modifiedDataObjects, markPartListEntryAsModified);
    }

    private static void addAssemblyToChangeSetList(EtkProject project, EtkDataPartListEntry partListEntry,
                                                   List<IdWithType> inChangeSetStoredIds, GenericEtkDataObjectList modifiedDataObjects) {
        AssemblyId assemblyId = partListEntry.getAsId().getOwnerAssemblyId();
        if (!inChangeSetStoredIds.contains(assemblyId)) {
            // zusätzlich Eintrag für die Assembly
            EtkDataAssembly dataAssembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
            // anstelle von dataAssembly.markAssemblyInChangeSetAsChanged();
            if (dataAssembly.existsInDB()) {
                dataAssembly.getAttributes().markAsModified();
                modifiedDataObjects.add(dataAssembly, DBActionOrigin.FROM_EDIT);
            }
            inChangeSetStoredIds.add(assemblyId);
        }
    }

    private static void addPartListEntryIdToChangeSetList(EtkDataPartListEntry partListEntry, List<IdWithType> inChangeSetStoredIds,
                                                          GenericEtkDataObjectList modifiedDataObjects, boolean markPartListEntryAsModified) {
        PartListEntryId partListEntryId = partListEntry.getAsId();
        if (!inChangeSetStoredIds.contains(partListEntryId)) {
            if (markPartListEntryAsModified) {
                // Ist z.B. bei kombinierten Texten und Fußnoten nötig, da das virtuelle Feld nicht geändert wird
                partListEntry.getAttributes().markAsModified();
            }
            modifiedDataObjects.add(partListEntry, DBActionOrigin.FROM_EDIT);
            inChangeSetStoredIds.add(partListEntryId);
        }
    }

    public EditEqualizeFieldsHelper(EtkProject project) {
        this.project = project;
        init();
    }

    /**
     * Folgende Attribute sollen vererbt werden:
     * {@code K_CODES, K_MENGE, K_HIERARCHY, K_EVENT_FROM, K_EVENT_TO}
     * und als Spezialfall {@code RETAIL_COMB_TEXT}
     */
    private void init() {
        // special wegen gleichzeitigem Setzen von FIELD_K_CODES_REDUCED
        EqualizeFieldElem elem = new EqualizeFieldElem(iPartsConst.FIELD_K_CODES, true);
        elems.put(elem.getFieldName(), elem);
        elem = new EqualizeFieldElem(iPartsConst.FIELD_K_MENGE, true);
        elems.put(elem.getFieldName(), elem);
        elem = new EqualizeFieldElem(iPartsConst.FIELD_K_HIERARCHY);
        elems.put(elem.getFieldName(), elem);
        elem = new EqualizeFieldElem(iPartsConst.FIELD_K_EVENT_FROM);
        elems.put(elem.getFieldName(), elem);
        elem = new EqualizeFieldElem(iPartsConst.FIELD_K_EVENT_TO);
        elems.put(elem.getFieldName(), elem);
        elem = new EqualizeFieldElem(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, true);
        elems.put(elem.getFieldName(), elem);
        // Als Platzhalter für die Fußnoten
        elem = new EqualizeFieldElem(iPartsConst.FIELD_DFNK_FNID, true);
        elems.put(elem.getFieldName(), elem);
    }

    /**
     * Liste der modifizierten Feldnamen bestimmen
     *
     * @return
     */
    private List<EqualizeFieldElem> getModifiedElems() {
        List<EqualizeFieldElem> result = new DwList<>();
        for (EqualizeFieldElem elem : elems.values()) {
            if (elem.isModified()) {
                result.add(elem);
            }
        }
        return result;
    }

    /**
     * Callback-Funktion für spezielle Attribute
     *
     * @return
     */
    public OnEqualizePartListEntryEvent getOnEqualizePartListEntryEvent() {
        return onEqualizePartListEntryEvent;
    }

    /**
     * Callback-Funktion für spezielle Attribute
     *
     * @param onEqualizePartListEntryEvent
     */
    public void setOnEqualizePartListEntryEvent(OnEqualizePartListEntryEvent onEqualizePartListEntryEvent) {
        this.onEqualizePartListEntryEvent = onEqualizePartListEntryEvent;
    }

    /**
     * Liste der behandelten Feldnamen
     *
     * @return
     */
    public Collection<String> getEqualizeFieldNames() {
        Collection<String> fieldNames = new LinkedHashSet<>();
        for (EqualizeFieldElem elem : elems.values()) {
            fieldNames.add(elem.getFieldName());
        }
        return fieldNames;
    }

    /**
     * Überprüfung, ob eines der Elemente modifiziert ist
     *
     * @return
     */
    public boolean isModified() {
        for (EqualizeFieldElem elem : elems.values()) {
            if (elem.isModified()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Spezielle Abfrage, ob ein Element mit {@code fieldName} modifiziert ist
     *
     * @param fieldName
     * @return
     */
    public boolean isModified(String fieldName) {
        EqualizeFieldElem elem = elems.get(fieldName);
        if (elem != null) {
            return elem.isModified();
        }
        return false;
    }

    /**
     * Ein Elemenmt als modifiziert setzen
     *
     * @param fieldName
     */
    public void setModified(String fieldName) {
        EqualizeFieldElem elem = elems.get(fieldName);
        if (elem != null) {
            elem.setModified(true);
        }
    }

    /**
     * Gemerkte Modifikationen aufheben
     */
    public void clearModified() {
        for (EqualizeFieldElem elem : elems.values()) {
            elem.setModified(false);
        }
    }

    /**
     * Alle relevanten Felder vom {@code partListEntry} auf Änderungen überprüfen
     *
     * @param partListEntry
     */
    public void fillModifiedByPartListEntry(EtkDataPartListEntry partListEntry) {
        DBDataObjectAttributes attribs = partListEntry.getAttributes();
        clearModified();
        for (EqualizeFieldElem elem : elems.values()) {
            if (!elem.isSpecialField()) {
                // normales Attribut
                DBDataObjectAttribute field = attribs.getField(elem.getFieldName(), false);
                if ((field != null) && field.isModified()) {
                    elem.setModified(true);
                }
            } else {
                // special Field
                if (onEqualizePartListEntryEvent != null) {
                    boolean modified = onEqualizePartListEntryEvent.isModifiedSpecialAttribute(project, elem.getFieldName(),
                                                                                               partListEntry);
                    if (modified) {
                        elem.setModified(true);
                    }
                }
            }
        }
    }

    /**
     * Falls es Attribut-Änderungen gibt:
     * Alle modifizierten Attributwerte vom {@code sourcePartListEntry} an die {@code destPartListEntryList} vererben.
     *
     * @param sourcePartListEntry
     * @param destPartListEntryList
     */
    public void equalizePartListEntry(EtkDataPartListEntry sourcePartListEntry, List<EtkDataPartListEntry> destPartListEntryList) {
        if (isModified()) {
            DBDataObjectAttributes sourceAttribs = sourcePartListEntry.getAttributes();
            List<EqualizeFieldElem> modifiedElems = getModifiedElems();
            for (EtkDataPartListEntry destPartListEntry : destPartListEntryList) {
                if (!destPartListEntry.getAsId().equals(sourcePartListEntry.getAsId())) {
                    DBDataObjectAttributes destAttribs = destPartListEntry.getAttributes();
                    for (EqualizeFieldElem elem : modifiedElems) {
                        if (!elem.isSpecialField()) {
                            copyAttributeValue(elem.getFieldName(), sourceAttribs, destAttribs, sourcePartListEntry);
                        } else {
                            // special Field
                            if (onEqualizePartListEntryEvent != null) {
                                onEqualizePartListEntryEvent.onEqualizePartListEntry(project, elem.getFieldName(),
                                                                                     sourcePartListEntry, destPartListEntry,
                                                                                     destAttribs);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void copyAttributeValue(String fieldName, EtkDataPartListEntry sourcePartListEntry, EtkDataPartListEntry destPartListEntry) {
        DBDataObjectAttributes sourceAttribs = sourcePartListEntry.getAttributes();
        DBDataObjectAttributes destAttribs = destPartListEntry.getAttributes();
        copyAttributeValue(fieldName, sourceAttribs, destAttribs, sourcePartListEntry);
    }

    public static void copyAttributeValue(String fieldName, DBDataObjectAttributes sourceAttribs, DBDataObjectAttributes destAttribs,
                                          EtkDataObject provider) {
        DBDataObjectAttribute sourceAttrib = sourceAttribs.getField(fieldName);
        DBDataObjectAttribute destAttrib = destAttribs.getField(fieldName);
        switch (sourceAttrib.getType()) {
            case STRING:
                destAttrib.setValueAsString(sourceAttrib.getAsString(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                break;
            case MULTI_LANGUAGE:
                EtkMultiSprache multiLanguage = sourceAttrib.getAsMultiLanguage(provider, true);
                if (multiLanguage == null) {
                    multiLanguage = new EtkMultiSprache();
                } else {
                    multiLanguage = multiLanguage.cloneMe();
                }
                String textNr = sourceAttrib.getMultiLanguageTextNr();
                String textId = sourceAttrib.getMultiLanguageTextId();
                if (StrUtils.isValid(textId)) {
                    destAttrib.setTextIdForMultiLanguage(textNr, textId, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                } else {
                    destAttrib.setTextNrForMultiLanguage(textNr, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                }
                destAttrib.setValueAsMultiLanguage(multiLanguage, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                break;
            case BLOB:
                destAttrib.setValueAsBlob(sourceAttrib.getAsBlob(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                break;
            case ARRAY:
                EtkDataArray dataArray = sourceAttrib.getAsArray(provider);
                if (dataArray == null) {
                    dataArray = new EtkDataArray();
                } else {
                    dataArray = dataArray.cloneMe();
                }
                String arrayId = sourceAttrib.getArrayId();
                destAttrib.setIdForArray(arrayId, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                destAttrib.setValueAsArray(dataArray, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                break;
        }
    }

    /**
     * Suchen nach den passenden AS-Stücklisten-Einträgen (gleicher BCTE-Key ohne AA) und Vererben der Attributwerte.
     *
     * @param sourcePartListEntry
     * @return
     */
    private List<EtkDataPartListEntry> equalizeAttributesInASPartListEntries(EtkDataPartListEntry sourcePartListEntry) {
        List<EtkDataPartListEntry> resultList = new DwList<>();
        if (isDIALOGPartListEntry(sourcePartListEntry) && isModified()) {
            ASUsageHelper helper = new ASUsageHelper(project);
            resultList = helper.getPartListEntriesUsedInAsForEqualizeWithoutPSKEntries(sourcePartListEntry, true);
            if (!resultList.isEmpty()) {
                equalizePartListEntry(sourcePartListEntry, resultList);
            }
        }
        return resultList;
    }

    public void equalizeAttributes(EtkDataPartListEntry sourcePartListEntry, EtkDataPartListEntry destPartListEntry) {
        List<EtkDataPartListEntry> destPartListEntryList = new DwList<>();
        destPartListEntryList.add(destPartListEntry);
        equalizePartListEntry(sourcePartListEntry, destPartListEntryList);
    }

    /**
     * Bestimmung der modifizierten Felder, Suche nach zugehörigen AS-Stücklisten-Einträgen und Vererbung
     *
     * @param sourcePartListEntry
     * @return
     */
    public List<EtkDataPartListEntry> checkForModificationsAndEqualizeAttributes(EtkDataPartListEntry sourcePartListEntry) {
        fillModifiedByPartListEntry(sourcePartListEntry);
        return equalizeAttributesInASPartListEntries(sourcePartListEntry);
    }


    /**
     * Hilfsklasse für den Feldnamen, ob der Attributwert modifiziert ist und ob es sich um eine Sonderbehandlung handelt.
     */
    private class EqualizeFieldElem {

        private String fieldName;
        private boolean isModified;
        private boolean isSpecialField;

        public EqualizeFieldElem(String fieldName) {
            this(fieldName, false);
        }

        public EqualizeFieldElem(String fieldName, boolean specialField) {
            this.fieldName = fieldName;
            this.isSpecialField = specialField;
        }

        public String getFieldName() {
            return fieldName;
        }

        public boolean isModified() {
            return isModified;
        }

        public void setModified(boolean modified) {
            isModified = modified;
        }

        public boolean isSpecialField() {
            return isSpecialField;
        }
    }
}
