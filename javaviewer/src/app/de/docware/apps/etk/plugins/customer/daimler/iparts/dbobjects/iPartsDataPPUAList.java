package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;

public class iPartsDataPPUAList extends EtkDataObjectList<iPartsDataPPUA> {

    public iPartsDataPPUAList() {
        setSearchWithoutActiveChangeSets(true);
    }


    @Override
    protected iPartsDataPPUA getNewDataObject(EtkProject project) {
        return new iPartsDataPPUA(project, null);
    }
}
