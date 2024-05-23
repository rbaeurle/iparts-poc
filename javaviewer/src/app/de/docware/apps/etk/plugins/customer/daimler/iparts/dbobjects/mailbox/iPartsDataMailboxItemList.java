/*
 * Copyright (c) 2019 Docware GmbH
 *
 * Tabelle [DA_MESSAGE], Nachrichtenpostkorb, die Nachrichten an sich.
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

public class iPartsDataMailboxItemList extends EtkDataObjectList<iPartsDataMailboxItem> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMailboxItem}s.
     *
     * @param project
     * @param origin
     */
    public void loadAllMailboxItemsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_MESSAGE, null, null, LoadType.COMPLETE, origin);
    }


    /**
     * Lädt eine Liste aus {@Link iPartsDataMailboxItem}s zur GUID.
     * (Es kann per Primärschlüsseldefinition immer nur einen geben)
     *
     * @param project
     * @param id
     * @param origin
     */
    public void loadMailboxItem(EtkProject project, iPartsMailboxItemId id, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_GUID },
                      new String[]{ id.getMsgGuid() }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste aus {@Link iPartsDataMailboxItem} zur einer Baureihe.
     *
     * @param project
     * @param seriesNo
     * @param origin
     */
    public void loadMailboxItemForSeries(EtkProject project, String seriesNo, DBActionOrigin origin) {
        clear(origin);
        if (StrUtils.isValid(seriesNo)) {
            searchAndFill(project, TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_SERIES_NO },
                          new String[]{ seriesNo }, LoadType.COMPLETE, origin);
        }
    }

    /**
     * Lädt eine Liste aus {@Link iPartsDataMailboxItem} zu einem Nachrichtentyp.
     *
     * @param project
     * @param msgType
     * @param origin
     */
    public void loadMailboxItemForType(EtkProject project, String msgType, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_TYPE },
                      new String[]{ msgType }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste aus {@Link iPartsDataMailboxItem}, die von einem übergebenen User erstellt wurden
     * und gibt die Liste aufsteigend nach Erstellungsdatum zurück.
     *
     * @param project
     * @param creationUserGUID
     * @param origin
     */
    public void loadMailboxItemForCreationUser(EtkProject project, String creationUserGUID, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_MESSAGE,
                          new String[]{ FIELD_DMSG_CREATION_USER_ID },
                          new String[]{ creationUserGUID },
                          new String[]{ FIELD_DMSG_CREATION_USER_ID, FIELD_DMSG_CREATION_DATE },
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataMailboxItem getNewDataObject(EtkProject project) {
        return new iPartsDataMailboxItem(project, null);
    }
}