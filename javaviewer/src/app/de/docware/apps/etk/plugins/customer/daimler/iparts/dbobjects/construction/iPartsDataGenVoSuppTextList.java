package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataGenVoSuppText}.
 */
public class iPartsDataGenVoSuppTextList extends EtkDataObjectList<iPartsDataGenVoSuppText> {

    public iPartsDataGenVoSuppTextList() {
        setSearchWithoutActiveChangeSets(true);
    }

    @Override
    protected iPartsDataGenVoSuppText getNewDataObject(EtkProject project) {
        return new iPartsDataGenVoSuppText(project, null);
    }

    public static iPartsDataGenVoSuppTextList loadDataForGenVoNo(EtkProject project, String genVoNo) {
        iPartsDataGenVoSuppTextList list = new iPartsDataGenVoSuppTextList();
        list.loadDataForGenVoNo(project, genVoNo, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadDataForGenVoNo(EtkProject project, String genVoNo, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ iPartsConst.FIELD_DA_GENVO_NO };
        String[] whereValues = new String[]{ genVoNo };
        searchAndFill(project, iPartsConst.TABLE_DA_GENVO_SUPP_TEXT, whereFields, whereValues, LoadType.COMPLETE, origin);
    }
}
