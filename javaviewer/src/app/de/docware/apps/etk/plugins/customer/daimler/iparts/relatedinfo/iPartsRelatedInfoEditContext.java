/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo;

import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * iParts-spezifischer {@link RelatedInfoEditContext}.
 */
public class iPartsRelatedInfoEditContext extends RelatedInfoEditContext {

    private EditFormIConnector editFormConnector;
    private DBDataObjectAttributes oldEditPartListEntryAttributes;
    private AbstractRevisionChangeSet changeSetForEdit;
    private AbstractRevisionChangeSet authorOrderChangeSetForEdit;
    private List<Runnable> saveEditRunnables = new DwList<>();
    private List<Runnable> cancelEditRunnables = new DwList<>();
    private Set<AssemblyId> modifiedAssemblyIds = new HashSet<>();

    // Diverse Update-Flags (Zurücksetzen in der Methode clear() nicht vergessen!)
    private boolean fireDataChangedEvent;
    private boolean updateModuleMasterData;
    private boolean updateEditAssemblyData;
    private boolean updateEditAssemblyPosNumber;
    private boolean updateRetailColortableData;
    private boolean updateRetailFactoryData;
    private boolean updateResponseData;
    private boolean updateResponseSpikes;
    private boolean updateFootNotes;
    private boolean updateReplacements;
    private boolean updateWWParts;
    private PartId updatePartId;
    private boolean updateMatFootNotes;

    /**
     * Erzeugt einen {@link iPartsRelatedInfoEditContext} basierend auf den übergebenen Parametern.
     *
     * @param dataConnector
     * @param isEditAllowed Bei {@code false} wird {@code null} zurückgegeben
     * @return
     */
    public static iPartsRelatedInfoEditContext createEditContext(AbstractJavaViewerFormIConnector dataConnector, boolean isEditAllowed) {
        if (!isEditAllowed) {
            return null;
        }

        return new iPartsRelatedInfoEditContext(getEditModuleFormConnector(dataConnector));
    }

    /**
     * Erzeugt einen {@link iPartsRelatedInfoEditContext} basierend auf dem übergebenen {@link AssemblyListFormIConnector}.
     *
     * @param assemblyListFormConnector
     * @return
     */
    public static iPartsRelatedInfoEditContext createEditContext(AssemblyListFormIConnector assemblyListFormConnector) {
        return new iPartsRelatedInfoEditContext(new AssemblyListFormEditConnector(assemblyListFormConnector));
    }


    /**
     * Liefert den {@link EditFormIConnector} zurück, der sich in der Hierarchie vom übergebenen {@link AbstractJavaViewerFormIConnector}
     * befindet (oder mit diesem identisch ist).
     *
     * @param dataConnector
     * @return {@code null} falls keine Edit-Aktion vorliegt
     */
    public static EditFormIConnector getEditModuleFormConnector(AbstractJavaViewerFormIConnector dataConnector) {
        // EditFormIConnector über getOwnerConnector() suchen
        AbstractJavaViewerFormIConnector ownerConnector = dataConnector;
        while (ownerConnector != null) {
            if (ownerConnector instanceof EditFormIConnector) {
                return (EditFormIConnector)ownerConnector;
            }
            ownerConnector = ownerConnector.getOwnerConnector();
        }

        return null;
    }

    /**
     * Erzeugt einen iParts-spezifischen Edit-Kontext ohne {@link EditFormIConnector}.
     */
    public iPartsRelatedInfoEditContext() {
        this(null);
    }

    /**
     * Erzeugt einen iParts-spezifischen Edit-Kontext.
     *
     * @param editFormConnector {@link EditFormIConnector} für die Edit-Aktion
     */
    public iPartsRelatedInfoEditContext(EditFormIConnector editFormConnector) {
        this.editFormConnector = editFormConnector;
    }

    /**
     * Setzt alle Flags und anderen Daten bis auf den {@link #editFormConnector} zurück.
     */
    public void clear() {
        // editFormConnector darf nicht zurückgesetzt werden, da dieser nur im Konstruktor gesetzt wird und sich durch eine
        // konkrete Edit-Aktion auch nicht ändert
        oldEditPartListEntryAttributes = null;
        changeSetForEdit = null;
        authorOrderChangeSetForEdit = null;
        saveEditRunnables.clear();
        cancelEditRunnables.clear();
        // messageDigestDIALOGChanges muss nicht zurückgesetzt werden, weil dieser unabhängig ist und nur als Cache dient

        // Diverse Update-Flags
        fireDataChangedEvent = false;
        updateModuleMasterData = false;
        updateEditAssemblyData = false;
        updateEditAssemblyPosNumber = false;
        updateRetailColortableData = false;
        updateRetailFactoryData = false;
        updateResponseData = false;
        updateResponseSpikes = false;
        updateFootNotes = false;
        updateReplacements = false;
        updateWWParts = false;
        updatePartId = null;
        updateMatFootNotes = false;
    }

    /**
     * Liefert den {@link EditFormIConnector} für die Edit-Aktion zurück.
     *
     * @return
     */
    public EditFormIConnector getEditFormConnector() {
        return editFormConnector;
    }

    /**
     * Liefert die bisherigen/alten {@link DBDataObjectAttributes} vom bearbeiteten Stücklisteneintrag vor dem Editieren zurück.
     *
     * @return
     */
    public DBDataObjectAttributes getOldEditPartListEntryAttributes() {
        return oldEditPartListEntryAttributes;
    }

    /**
     * Setzt die bisherigen/alten {@link DBDataObjectAttributes} vom bearbeiteten Stücklisteneintrag vor dem Editieren.
     *
     * @param oldEditPartListEntryAttributes
     */
    public void setOldEditPartListEntryAttributes(DBDataObjectAttributes oldEditPartListEntryAttributes) {
        this.oldEditPartListEntryAttributes = oldEditPartListEntryAttributes;
    }

    /**
     * Liefert das {@link AbstractRevisionChangeSet} zurück, das für die Related Info innerhalb von diesem Edit-Kontext
     * verwendet werden soll.
     *
     * @return
     */
    public AbstractRevisionChangeSet getChangeSetForEdit() {
        return changeSetForEdit;
    }

    /**
     * Setzt das {@link AbstractRevisionChangeSet}, das für die Related Info innerhalb von diesem Edit-Kontext verwendet
     * werden soll.
     *
     * @param changeSetForEdit
     */
    public void setChangeSetForEdit(AbstractRevisionChangeSet changeSetForEdit) {
        this.changeSetForEdit = changeSetForEdit;
    }

    /**
     * Liefert das {@link AbstractRevisionChangeSet} zurück, das vom aktiven Autoren-Auftrag für Edit verwendet wird.
     *
     * @return
     */
    public AbstractRevisionChangeSet getAuthorOrderChangeSetForEdit() {
        return authorOrderChangeSetForEdit;
    }

    /**
     * Setzt das {@link AbstractRevisionChangeSet}, das vom aktiven Autoren-Auftrag für Edit verwendet wird.
     *
     * @param authorOrderChangeSetForEdit
     */
    public void setAuthorOrderChangeSetForEdit(AbstractRevisionChangeSet authorOrderChangeSetForEdit) {
        this.authorOrderChangeSetForEdit = authorOrderChangeSetForEdit;
    }

    public boolean isFireDataChangedEvent() {
        return fireDataChangedEvent;
    }

    public void setFireDataChangedEvent(boolean fireDataChangedEvent) {
        this.fireDataChangedEvent = fireDataChangedEvent;
    }

    public boolean isUpdateModuleMasterData() {
        return updateModuleMasterData;
    }

    public void setUpdateModuleMasterData(boolean updateModuleMasterData) {
        this.updateModuleMasterData = updateModuleMasterData;
    }

    public boolean isUpdateEditAssemblyData() {
        return updateEditAssemblyData;
    }

    public void setUpdateEditAssemblyData(boolean updateEditAssemblyData) {
        this.updateEditAssemblyData = updateEditAssemblyData;
    }

    public boolean isUpdateEditAssemblyPosNumber() {
        return updateEditAssemblyPosNumber;
    }

    public void setUpdateEditAssemblyPosNumber(boolean updateEditAssemblyPosNumber) {
        this.updateEditAssemblyPosNumber = updateEditAssemblyPosNumber;
    }

    public boolean isUpdateRetailColortableData() {
        return updateRetailColortableData;
    }

    public void setUpdateRetailColortableData(boolean updateRetailColortableData) {
        this.updateRetailColortableData = updateRetailColortableData;
    }

    public boolean isUpdateRetailFactoryData() {
        return updateRetailFactoryData;
    }

    public void setUpdateRetailFactoryData(boolean updateRetailFactoryData) {
        this.updateRetailFactoryData = updateRetailFactoryData;
    }

    public boolean isUpdateResponseData() {
        return updateResponseData;
    }

    public void setUpdateResponseData(boolean updateResponseData) {
        this.updateResponseData = updateResponseData;
    }

    public boolean isUpdateResponseSpikes() {
        return updateResponseSpikes;
    }

    public void setUpdateResponseSpikes(boolean updateResponseSpikes) {
        this.updateResponseSpikes = updateResponseSpikes;
    }

    public boolean isUpdateFootNotes() {
        return updateFootNotes;
    }

    public void setUpdateFootNotes(boolean updateFootNotes) {
        this.updateFootNotes = updateFootNotes;
    }

    public boolean isUpdateMatFootNotes() {
        return updateMatFootNotes;
    }

    public void setUpdateMatFootNotes(boolean updateMatFootNotes) {
        this.updateMatFootNotes = updateMatFootNotes;
    }

    public PartId getUpdatePartId() {
        return updatePartId;
    }

    public void setUpdatePartId(PartId updatePartId) {
        this.updatePartId = updatePartId;
    }

    public boolean isUpdateReplacements() {
        return updateReplacements;
    }

    public void setUpdateReplacements(boolean updateReplacements) {
        this.updateReplacements = updateReplacements;
    }

    public boolean isUpdateWWParts() {
        return updateWWParts;
    }

    public void setUpdateWWParts(boolean updateWWParts) {
        this.updateWWParts = updateWWParts;
    }

    /**
     * Ein {@link Runnable} hinzufügen, welches beim Speichern der RelatedEdit direkt am Anfang ausgeführt werden soll.
     *
     * @param runnable
     */
    public void addSaveEditRunnable(Runnable runnable) {
        saveEditRunnables.add(runnable);
    }

    /**
     * Nicht veränderbare Liste aller {@link Runnable}s, die beim Speichern der RelatedEdit direkt am Anfang ausgeführt
     * werden sollen.
     *
     * @return
     */
    public List<Runnable> getSaveEditRunnables() {
        return Collections.unmodifiableList(saveEditRunnables);
    }

    /**
     * Ein {@link Runnable} hinzufügen, welches beim Abbrechen der RelatedEdit direkt am Anfang ausgeführt werden soll.
     *
     * @param runnable
     */
    public void addCancelEditRunnable(Runnable runnable) {
        cancelEditRunnables.add(runnable);
    }

    /**
     * Nicht veränderbare Liste aller {@link Runnable}s, die beim Abbrechen der RelatedEdit direkt am Anfang ausgeführt
     * werden sollen.
     *
     * @return
     */
    public List<Runnable> getCancelEditRunnables() {
        return Collections.unmodifiableList(cancelEditRunnables);
    }

    /**
     * Fügt die übergebenen veränderten {@link AssemblyId}s zum Set aller veränderten {@link AssemblyId}s hinzu.
     *
     * @param modifiedAssemblyIds
     */
    public void addModifiedAssemblyIds(Collection<AssemblyId> modifiedAssemblyIds) {
        if (modifiedAssemblyIds != null) {
            this.modifiedAssemblyIds.addAll(modifiedAssemblyIds);
        }
    }

    /**
     * Liefert das Set aller veränderten {@link AssemblyId}s zurück, wobei die {@link AssemblyId} der im Edit befindlichen
     * {@link EtkDataAssembly} in der Regel nur dann enthalten ist, wenn auch ein anderer Stücklisteneintrag als der gerade
     * editierte verändert wurde.
     *
     * @return
     */
    public Set<AssemblyId> getModifiedAssemblyIds() {
        return Collections.unmodifiableSet(modifiedAssemblyIds);
    }


    /**
     * Simpler {@link EditFormIConnector} basierend auf einem {@link AssemblyListFormIConnector}
     */
    private static class AssemblyListFormEditConnector extends AbstractJavaViewerFormConnector implements EditFormIConnector {

        private AssemblyListFormIConnector assemblyListFormConnector;

        public AssemblyListFormEditConnector(AssemblyListFormIConnector owner) {
            super(owner);
            assemblyListFormConnector = owner;
        }

        @Override
        public EtkDataAssembly getCurrentAssembly() {
            return assemblyListFormConnector.getCurrentAssembly();
        }

        @Override
        public void setCurrentAssembly(EtkDataAssembly value) {
            assemblyListFormConnector.setCurrentAssembly(value);
        }

        @Override
        public boolean isAuthorOrderValid() {
            // Wenn ein ChangeSet für Edit aktiv ist, dann ist auch ein Autoren-Auftrag aktiv
            return assemblyListFormConnector.getProject().isRevisionChangeSetActiveForEdit() && iPartsRight.EDIT_PARTS_DATA.checkRightInSession();
        }

        public void clearFilteredEditPartListEntries() {
        }

    }
}