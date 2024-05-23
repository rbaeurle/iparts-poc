package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Liste von {@link iPartsDataES1}.
 */
public class iPartsDataES1List extends EtkDataObjectList<iPartsDataES1> implements iPartsConst {

    public iPartsDataES1List() {
        setSearchWithoutActiveChangeSets(true);
    }

    @Override
    protected iPartsDataES1 getNewDataObject(EtkProject project) {
        return new iPartsDataES1(project, null);
    }

    /**
     * Lädt alle Daten aus TABLE_DA_ES1 unsortiert
     *
     * @param project
     */
    public void load(EtkProject project) {
        super.searchAndFill(project, TABLE_DA_ES1, null, null, DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }
}
