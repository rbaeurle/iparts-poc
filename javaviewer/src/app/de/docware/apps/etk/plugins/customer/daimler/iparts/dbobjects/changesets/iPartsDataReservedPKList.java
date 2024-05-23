/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Liste von {@link iPartsDataReservedPK}s.
 */
public class iPartsDataReservedPKList extends EtkDataObjectList<iPartsDataReservedPK> implements iPartsConst {

    private static int MAX_TRIES_TO_RESERVE = 10;

    public iPartsDataReservedPKList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Prüft, ob der Primärschlüssel, übergeben durch die {@code id}, noch nicht in der Datenbank reserviert ist
     * (also noch frei ist) und reserviert diesen in der Datenbank, falls ein Edit-ChangeSet aktiv ist.
     *
     * @param project
     * @param id
     * @return {@code true} falls der Primärschlüssel bisher nocht nicht reserviert und die Reservierung damit erfolgreich war
     */
    public static boolean reservePrimaryKey(EtkProject project, IdWithType id) {
        final iPartsDataReservedPK dataReservedPK = new iPartsDataReservedPK(project, new iPartsReservedPKId(id));
        if (!dataReservedPK.existsInDB()) {
            final String activeChangeSetGuidAsDbValue = project.getActiveChangeSetGuidAsDbValue();
            if (!activeChangeSetGuidAsDbValue.isEmpty()) {
                VarParam<Boolean> reserved = new VarParam<>(false);
                // Bei aktivem Edit-ChangeSet den Primärschlüssel in der DB reservieren (wobei in diesem Fall gerade eine
                // Pseudo-Transaktion aktiv sein könnte -> executeWithoutPseudoTransactions()); ohne aktives Edit-ChangeSet
                // wird das neue DataObject ja sowieso direkt in die DB geschrieben
                project.getRevisionsHelper().executeWithoutPseudoTransactions(new FrameworkRunnable() {
                    @Override
                    public void run(FrameworkThread thread) {
                        try {
                            dataReservedPK.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                            dataReservedPK.setChangeSetId(activeChangeSetGuidAsDbValue, DBActionOrigin.FROM_EDIT);
                            dataReservedPK.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
                            reserved.setValue(true);
                        } catch (Exception e) {
                            // Es gab z.B. eine Primärschlüsselverletzung -> Primärschlüssel ist nicht mehr frei
                            reserved.setValue(false);
                        }
                    }
                });
                return reserved.getValue();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Löscht den reservierten Primärschlüssel übergeben durch die {@code id}.
     *
     * @param project
     * @param id
     */
    public static void deleteReservedPrimaryKey(final EtkProject project, final IdWithType id) {
        FrameworkRunnable runnable = new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                iPartsDataReservedPK dataReservedPK = new iPartsDataReservedPK(project, new iPartsReservedPKId(id));
                dataReservedPK.deleteFromDB(true);
            }
        };

        // Bei aktivem Edit-ChangeSet könnte gerade eine Pseudo-Transaktion aktiv sein könnte -> executeWithoutPseudoTransactions()
        if (project.isRevisionChangeSetActiveForEdit()) {
            project.getRevisionsHelper().executeWithoutPseudoTransactions(runnable);
        } else {
            runnable.run(null);
        }
    }

    /**
     * Bestimme die nächste laufende Nummer für einen Stücklisteneintrag.
     * Sind keine Einträge in der ReservedPK vorhanden, liefert die Funktion einfach currentKLfdNr + 1
     * sonst wird mit den Einträgen in der ReservedPK abgeglichen.
     * Die neue Laufende Nummer wird sofort reserviert.
     * Sollte das Reservieren nicht funktionieren, wird die berechnete nächste laufende Nummer zu reservieren
     * zurückgegeben. In diesem unwahrscheinlichen Fall kann es dann doch zu Duplikaten kommen.
     *
     * @param project
     * @param assemblyId
     * @param currentKLfdNr
     * @return
     */
    public static int getAndReserveNextKLfdNr(EtkProject project, AssemblyId assemblyId, int currentKLfdNr) {
        int workKLfdNr = currentKLfdNr + 1;
        PartListEntryId id = new PartListEntryId(assemblyId.getKVari(), assemblyId.getKVer(), EtkDbsHelper.formatLfdNr(workKLfdNr));
        String pkKLfdNr = getMaxKLfdNrFromReservedPK(project, id);
        if (StrUtils.isValid(pkKLfdNr)) {
            int currentReservedKLfdNr = Integer.valueOf(pkKLfdNr);
            if (currentReservedKLfdNr >= workKLfdNr) {
                workKLfdNr = currentReservedKLfdNr + 1;
                id.setKLfdNr(EtkDbsHelper.formatLfdNr(workKLfdNr));
            }
        }
        int nextKLfdNr = tryToReserveKLfdNr(project, id);
        if (nextKLfdNr != -1) {
            return nextKLfdNr;
        }
        return workKLfdNr;
    }

    /**
     * Versuch eine {@link PartListEntryId} zu reservieren.
     *
     * @param project
     * @param id
     * @return -1: Resevierung hat nicht geklappt; sonst: neue reservierte Laufende Nummer
     */
    public static int tryToReserveKLfdNr(EtkProject project, PartListEntryId id) {
        int count = 0;
        int currentKLfdNr = Integer.valueOf(id.getKLfdnr());
        while (!reservePrimaryKey(project, id)) {
            count++;
            // irgendwann muss man aussteigen
            if (count > MAX_TRIES_TO_RESERVE) {
                id = null;
                break;
            }
            id.setKLfdNr(EtkDbsHelper.formatLfdNr(++currentKLfdNr));
        }
        if (id != null) {
            return currentKLfdNr;
        }
        return -1;
    }

    /**
     * Die zugehörige KLfdNr aus den ReservedPK holen
     *
     * @param project
     * @param id
     * @return
     */
    public static String getMaxKLfdNrFromReservedPK(EtkProject project, PartListEntryId id) {
        return getMaxKLfdNrFromReservedPK(project, id.getOwnerAssemblyId());
    }

    /**
     * Die höchste KLfdNr aus den ReservedPK holen
     *
     * @param project
     * @param id
     * @return
     */
    private static String getMaxKLfdNrFromReservedPK(EtkProject project, AssemblyId id) {
        PartListEntryId searchPartListEntryId = new PartListEntryId(id.getKVari(), id.getKVer(), "*");
        iPartsDataReservedPKList list = new iPartsDataReservedPKList();
        list.loadLikesForReservedId(project, new iPartsReservedPKId(searchPartListEntryId));
        if (!list.isEmpty()) {
            iPartsReservedPKId pkId = list.get(0).getAsId();
            IdWithType idWithType = IdWithType.fromDBString(pkId.getDataObjectType(), pkId.getDataObjectId());
            searchPartListEntryId = new PartListEntryId(idWithType.toStringArrayWithoutType());
            return searchPartListEntryId.getKLfdnr();
        }
        return null;
    }

    /**
     * Lädt alle Primärschlüssel-Reservierungen für die übergebene IdWithType.
     *
     * @param project
     * @param id
     * @return
     */
    public static iPartsDataReservedPKList loadChangeSetsForIdWithType(EtkProject project, IdWithType id) {
        iPartsDataReservedPKList list = new iPartsDataReservedPKList();
        list.loadChangeSetsForReservedId(project, new iPartsReservedPKId(id));
        return list;
    }

    /**
     * Lädt alle Primärschlüssel-Reservierungen für die übergebene ChangeSet-ID.
     *
     * @param project
     * @param changeSetId
     * @return
     */
    public static iPartsDataReservedPKList loadPrimaryKeysForChangeSet(EtkProject project, iPartsChangeSetId changeSetId) {
        iPartsDataReservedPKList list = new iPartsDataReservedPKList();
        list.loadForPrimaryKeysForChangeSet(project, changeSetId);
        return list;
    }

    /**
     * Löscht alle Primärschlüssel-Reservierungen für die übergebene ChangeSet-ID.
     *
     * @param project
     * @param changeSetId
     * @return
     */
    public static void deletePrimaryKeysForChangeSet(EtkProject project, iPartsChangeSetId changeSetId) {
        project.getDbLayer().delete(TABLE_DA_RESERVED_PK, new String[]{ FIELD_DRP_CHANGE_SET_ID }, new String[]{ changeSetId.getGUID() });
    }

    private void loadLikesForReservedId(EtkProject project, iPartsReservedPKId reservedPKId) {
        clear(DBActionOrigin.FROM_DB);
        searchSortAndFillWithLike(project, TABLE_DA_RESERVED_PK, null,
                                  new String[]{ FIELD_DRP_DO_TYPE, FIELD_DRP_DO_ID },
                                  new String[]{ reservedPKId.getDataObjectType(), reservedPKId.getDataObjectId() },
                                  new String[]{ FIELD_DRP_DO_ID }, true, LoadType.COMPLETE,
                                  false, DBActionOrigin.FROM_DB);
    }

    private void loadChangeSetsForReservedId(EtkProject project, iPartsReservedPKId reservedPKId) {
        clear(DBActionOrigin.FROM_DB);
        searchAndFill(project, TABLE_DA_RESERVED_PK, new String[]{ FIELD_DRP_DO_TYPE, FIELD_DRP_DO_ID },
                      new String[]{ reservedPKId.getDataObjectType(), reservedPKId.getDataObjectId() },
                      LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    private void loadForPrimaryKeysForChangeSet(EtkProject project, iPartsChangeSetId changeSetId) {
        clear(DBActionOrigin.FROM_DB);
        searchAndFill(project, TABLE_DA_RESERVED_PK, new String[]{ FIELD_DRP_CHANGE_SET_ID }, new String[]{ changeSetId.getGUID() },
                      LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataReservedPK getNewDataObject(EtkProject project) {
        return new iPartsDataReservedPK(project, null);
    }
}