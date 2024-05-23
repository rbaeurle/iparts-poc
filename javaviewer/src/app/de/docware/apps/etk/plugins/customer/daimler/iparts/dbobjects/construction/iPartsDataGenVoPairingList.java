/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataGenVoPairingList extends EtkDataObjectList<iPartsDataGenVoPairing> implements iPartsConst {


    public iPartsDataGenVoPairingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt eine Liste mit {@link iPartsDataGenVoPairing}s, und füllt sie mit den Werten aus der DB.
     *
     * @param project
     * @return
     */
    public static iPartsDataGenVoPairingList loadAllGenVoPairings(EtkProject project) {
        iPartsDataGenVoPairingList list = new iPartsDataGenVoPairingList();
        list.loadAllGenVoPairings(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataGenVoPairing} Datensätze aus der DB.
     *
     * @param project
     * @param origin
     */
    private void loadAllGenVoPairings(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_GENVO_PAIRING, null, null, LoadType.ONLY_IDS, origin);
    }

    @Override
    protected iPartsDataGenVoPairing getNewDataObject(EtkProject project) {
        return new iPartsDataGenVoPairing(project, null);
    }
}
