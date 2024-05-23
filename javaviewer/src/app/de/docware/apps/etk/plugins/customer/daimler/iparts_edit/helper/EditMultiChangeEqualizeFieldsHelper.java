package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PartListEntryReferenceKeyByAA;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper für die Vererbung von Vereinheitlichen
 */
public class EditMultiChangeEqualizeFieldsHelper {

    private EtkProject project;
    private EditEqualizeFieldsHelper equalizeHelper;
    private Map<String, EqualizeContainer> workMap; // Map von GUID ohne AA auf EqualizeContainer

    public EditMultiChangeEqualizeFieldsHelper(EtkProject project) {
        this.project = project;
        this.equalizeHelper = new EditEqualizeFieldsHelper(project);
        this.workMap = new HashMap<>();
    }

    /**
     * Pro Stücklisteneintrag, der vereinheitlicht worden ist, wird überprüft, ob relevante Änderungen für das Vererben an
     * andere AS-Stücklsiteneinträge vorhanden sind. Falls ja, dann wird ein Eintrag in der {@link #workMap} gemacht.
     *
     * @param partListEntryForEdit
     * @param combTextsList
     * @param footNoteCatalogueRefList
     * @see #equalizeAttributesInASPartListEntriesFromWorkList(GenericEtkDataObjectList)
     */
    public void addPartListEntryToWorkList(EtkDataPartListEntry partListEntryForEdit, EtkDataObjectList combTextsList,
                                           iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefList) {
        if (EditEqualizeFieldsHelper.isDIALOGPartListEntry(partListEntryForEdit) && partListEntryForEdit.isModifiedWithChildren()) {
            // kombinierte Texte vorhanden?
            iPartsDataCombTextList currentCombTexts = null;
            // Check, ob es neue, geänderte oder gelöschte Ergänzungstexte gibt
            if (!combTextsList.isEmpty() || !combTextsList.getDeletedList().isEmpty()) {
                currentCombTexts = new iPartsDataCombTextList();
                currentCombTexts.addAll(combTextsList, DBActionOrigin.FROM_DB);
            }

            // für den reinen Vergleich reicht das und die Parameter können null sein
            equalizeHelper.setOnEqualizePartListEntryEvent(EditEqualizeFieldsHelper.createOnEqualizePartListEntryEvent(null,
                                                                                                                       currentCombTexts,
                                                                                                                       footNoteCatalogueRefList,
                                                                                                                       null));
            // überprüfe, ob relevante Änderungen vorhanden sind
            equalizeHelper.fillModifiedByPartListEntry(partListEntryForEdit);
            if (equalizeHelper.isModified()) {
                // ein partListEntry, der vererbt werden muss => merken
                EqualizeContainer container = new EqualizeContainer(partListEntryForEdit, currentCombTexts, footNoteCatalogueRefList);
                // überhaupt eine Konstruktions-GUID angegeben?
                if (container.getRefKey().isValid()) {
                    String guid = container.getRefKeyGuid();

                    // pro GUID ohne AA reicht ein Stücklisteneintrag
                    if (workMap.get(guid) == null) {
                        workMap.put(guid, container);
                    }
                }
            }
        }
    }

    /**
     * Alle in der {@link #workMap} gemerkten Stücklisteneinträge nach dem Vereinheitlichen vererben.
     *
     * @param modifiedDataObjects
     * @return
     * @see #addPartListEntryToWorkList(EtkDataPartListEntry, EtkDataObjectList, iPartsDataFootNoteCatalogueRefList)
     */
    public Set<AssemblyId> equalizeAttributesInASPartListEntriesFromWorkList(GenericEtkDataObjectList modifiedDataObjects) {
        Set<AssemblyId> equalizedAssemblyIds = new HashSet<>();
        if (!workMap.isEmpty()) {
            for (EqualizeContainer container : workMap.values()) {
                // pro Stücklisteneintrag das 'normale' Vererben aufrufen
                Set<AssemblyId> currentEqualizedAssemblyIds =
                        EditEqualizeFieldsHelper.doEqualizePartListEntryEdit(project,
                                                                             container.getPartListEntryForEdit(),
                                                                             container.getCurrentCombTexts(),
                                                                             container.getCurrentFootNoteCatalogueRefList(),
                                                                             modifiedDataObjects);
                if (currentEqualizedAssemblyIds != null) {
                    equalizedAssemblyIds.addAll(currentEqualizedAssemblyIds);
                }
            }
        }
        return equalizedAssemblyIds;
    }


    /**
     * Hilfs-Klasse zum Speichern eines modifizierten vereinheitlichten Stücklisteneintrags
     */
    private class EqualizeContainer {

        private PartListEntryReferenceKeyByAA refKey;
        private EtkDataPartListEntry partListEntryForEdit;
        private iPartsDataCombTextList currentCombTexts;
        private iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefList;

        public EqualizeContainer(EtkDataPartListEntry partListEntryForEdit, iPartsDataCombTextList currentCombTexts,
                                 iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefList) {
            this.partListEntryForEdit = partListEntryForEdit;
            this.refKey = new PartListEntryReferenceKeyByAA(partListEntryForEdit);
            this.currentCombTexts = currentCombTexts;
            if ((footNoteCatalogueRefList != null) && (!footNoteCatalogueRefList.isEmpty() || !footNoteCatalogueRefList.getDeletedList().isEmpty())) {
                this.footNoteCatalogueRefList = footNoteCatalogueRefList;
            }
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

        public iPartsDataCombTextList getCurrentCombTexts() {
            return currentCombTexts;
        }

        public iPartsDataFootNoteCatalogueRefList getCurrentFootNoteCatalogueRefList() {
            return footNoteCatalogueRefList;
        }
    }
}
