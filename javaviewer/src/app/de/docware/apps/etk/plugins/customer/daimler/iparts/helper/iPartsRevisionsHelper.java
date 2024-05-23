/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.ChangeSetId;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsChangeSetInfoDefinitions;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.config.db.DBProject;
import de.docware.util.sql.TableAndFieldName;

import java.util.Set;
import java.util.TreeSet;

/**
 * iParts-spezifische Hilfsklasse für die Verwaltung von Änderungssets (siehe {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}
 * inkl. Pseudo-Transaktionen, die bei Edit-Aktionen für die Simulation von Änderungsständen verwendet werden.
 */
public class iPartsRevisionsHelper extends EtkRevisionsHelper implements iPartsConst {

    /**
     * Sucht nach aktiven (also noch nicht freigegebenen) {@link iPartsRevisionChangeSet}s,
     * die {@link de.docware.framework.modules.db.serialization.SerializedDBDataObject}s mit der übergebenen Verweis Konstruktion
     * Datensatz GUID enthalten und liefert eine {@link iPartsDataChangeSetEntryList} mit den entsprechenden ChangeSet-Einträgen
     * zurück.
     *
     * @param dataObjectType
     * @param sourceGUID     Partielle GUID von der Quelle (bei DIALOG der BCTE-Schlüssel)
     * @param project
     * @return
     */
    public static iPartsDataChangeSetEntryList getActiveChangeSetEntriesContainingSourceGUID(String dataObjectType, String sourceGUID,
                                                                                             EtkProject project) {
        iPartsDataChangeSetEntryList dataChangeSetEntries = new iPartsDataChangeSetEntryList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_GUID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_SOURCE_GUID, false, false));
        // Like Abfrage, weil die GUID auch partiell durchsucht werden kann. Die GUID kann eben auch ein Teil eines BCTE-Schlüssel sein
        dataChangeSetEntries.searchSortAndFillWithJoin(project, null, selectFields,
                                                       new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                                                     TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_SOURCE_GUID) },
                                                       new String[]{ dataObjectType, sourceGUID },
                                                       new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET, FIELD_DCS_STATUS) },
                                                       new String[]{ iPartsChangeSetStatus.COMMITTED.name() },
                                                       false, null, false, true, null,
                                                       new EtkDataObjectList.JoinData(TABLE_DA_CHANGE_SET,
                                                                                      new String[]{ FIELD_DCE_GUID },
                                                                                      new String[]{ FIELD_DCS_GUID },
                                                                                      false, false));
        return dataChangeSetEntries;
    }

    /**
     * Sucht nach aktiven (also noch nicht freigegebenen) {@link iPartsRevisionChangeSet}s,
     * die {@link de.docware.framework.modules.db.serialization.SerializedDBDataObject}s die die Materialnummer enthalten
     * und liefert eine {@link iPartsDataChangeSetEntryList} mit den entsprechenden ChangeSet-Einträgen
     * zurück.
     *
     * @param dataObjectType
     * @param matNr
     * @param project
     * @return
     */
    public static iPartsDataChangeSetEntryList getActiveChangeSetEntriesContainingMatNo(String dataObjectType, String matNr,
                                                                                        EtkProject project) {
        iPartsDataChangeSetEntryList dataChangeSetEntries = new iPartsDataChangeSetEntryList();
        EtkDisplayFields selectFields = new EtkDisplayFields();

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_GUID, false, false));
        // Like Abfrage, weil die GUID auch partiell durchsucht werden kann. Die GUID kann eben auch ein Teil eines BCTE-Schlüssel sein
        dataChangeSetEntries.searchSortAndFillWithJoin(project, null, selectFields,
                                                       new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                                                     TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_MATNR) },
                                                       new String[]{ dataObjectType, matNr },
                                                       new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET, FIELD_DCS_STATUS) },
                                                       new String[]{ iPartsChangeSetStatus.COMMITTED.name() },
                                                       false, null, false, false, null,
                                                       new EtkDataObjectList.JoinData(TABLE_DA_CHANGE_SET,
                                                                                      new String[]{ FIELD_DCE_GUID },
                                                                                      new String[]{ FIELD_DCS_GUID },
                                                                                      false, false));
        return dataChangeSetEntries;
    }

    /**
     * Sucht nach aktiven (also noch nicht freigegebenen) {@link iPartsRevisionChangeSet}s,
     * die {@link de.docware.framework.modules.db.serialization.SerializedDBDataObject}s mit der übergebenen Verweis Konstruktion
     * Datensatz GUID enthalten und liefert eine {@link iPartsDataChangeSetList} mit den entsprechenden ChangeSets zurück.
     *
     * @param dataObjectType
     * @param sourceGUID     Partielle GUID von der Quelle (bei DIALOG der BCTE-Schlüssel)
     * @param project
     * @return
     */
    public static iPartsDataChangeSetList getActiveChangeSetsContainingSourceGUID(String dataObjectType, String sourceGUID,
                                                                                  EtkProject project) {
        iPartsDataChangeSetList dataChangeSets = new iPartsDataChangeSetList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET, FIELD_DCS_GUID, false, false));

        // Like Abfrage, weil die GUID auch partiell durchsucht werden kann. Die GUID kann eben auch ein Teil eines BCTE-Schlüssel sein
        dataChangeSets.searchSortAndFillWithJoin(project, null, selectFields,
                                                 new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                                               TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_SOURCE_GUID) },
                                                 new String[]{ dataObjectType, sourceGUID },
                                                 new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET, FIELD_DCS_STATUS) },
                                                 new String[]{ iPartsChangeSetStatus.COMMITTED.name() },
                                                 false, null, false, true, null,
                                                 new EtkDataObjectList.JoinData(TABLE_DA_CHANGE_SET_ENTRY,
                                                                                new String[]{ FIELD_DCS_GUID },
                                                                                new String[]{ FIELD_DCE_GUID },
                                                                                false, false));
        return dataChangeSets;
    }

    /**
     * Sucht nach aktiven (also noch nicht freigegebenen) {@link iPartsRevisionChangeSet}s,
     * die {@link de.docware.framework.modules.db.serialization.SerializedDBDataObject}s mit der übergebenen Verweis Konstruktion
     * Datensatz GUID enthalten und liefert ein Set mit den entsprechenden {@link iPartsChangeSetId}s zurück.
     *
     * @param dataObjectType
     * @param sourceGUID
     * @param project
     * @return
     */
    public static Set<iPartsChangeSetId> searchActiveChangeSetsContainingSourceGUID(String dataObjectType, String sourceGUID,
                                                                                    EtkProject project) {
        iPartsDataChangeSetList dataChangeSetList = getActiveChangeSetsContainingSourceGUID(dataObjectType, sourceGUID, project);
        Set<iPartsChangeSetId> changeSetIds = new TreeSet<>();
        for (iPartsDataChangeSet dataChangeSet : dataChangeSetList) {
            changeSetIds.add(dataChangeSet.getAsId());
        }
        return changeSetIds;
    }

    public iPartsRevisionsHelper() {
        super(iPartsPlugin.LOG_CHANNEL_CHANGE_SETS);
    }

    @Override
    public AbstractRevisionChangeSet createChangeSet(ChangeSetId changeSetId, EtkProject project) {
        if (changeSetId instanceof iPartsChangeSetId) {
            return new iPartsRevisionChangeSet((iPartsChangeSetId)changeSetId, project);
        } else {
            throw new IllegalArgumentException("ChangeSetId is no iPartsChangeSetId");
        }
    }

    @Override
    public Set<String> getExtraAttributeNamesForSerializedDBDataObject(String objectType, DBProject project) {
        if (project instanceof EtkProject) {
            Set<String> extraAttributeNames = iPartsChangeSetInfoDefinitions.getInstance((EtkProject)project).getExtraAttributeNamesForSerializedDBDataObject(objectType);
            if (extraAttributeNames != null) {
                return extraAttributeNames;
            }
        }

        return super.getExtraAttributeNamesForSerializedDBDataObject(objectType, project);
    }
}