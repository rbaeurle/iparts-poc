/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataDictTransJobList extends EtkDataObjectList<iPartsDataDictTransJob> implements iPartsConst {

    public iPartsDataDictTransJobList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataDictTransJobList getJobsByState(EtkProject project, iPartsDictTransJobStates state) {
        iPartsDataDictTransJobList list = new iPartsDataDictTransJobList();
        list.getJobsByStateFromDB(project, state, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataDictTransJobList getJobsByBundleName(EtkProject project, String bundleName) {
        iPartsDataDictTransJobList list = new iPartsDataDictTransJobList();
        list.getJobsByBundleName(project, bundleName, DBActionOrigin.FROM_DB);
        return list;
    }

    private void getJobsByStateFromDB(EtkProject project, iPartsDictTransJobStates state, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DTJ_TRANSLATION_STATE };
        String[] whereValues = new String[]{ state.getDbValue() };

        searchAndFill(project, TABLE_DA_DICT_TRANS_JOB, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    private void getJobsByBundleName(EtkProject project, String bundleName, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DTJ_BUNDLE_NAME };
        String[] whereValues = new String[]{ bundleName };

        searchAndFill(project, TABLE_DA_DICT_TRANS_JOB, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    public static iPartsDataDictTransJobList getJobsByTextAndJobId(EtkProject project, String textId, String jobId) {
        iPartsDataDictTransJobList list = new iPartsDataDictTransJobList();
        list.getJobsByTextId(project, textId, jobId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void getJobsByTextId(EtkProject project, String textId, String jobId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DTJ_TEXTID, FIELD_DTJ_JOBID };
        String[] whereValues = new String[]{ textId, jobId };

        searchAndFill(project, TABLE_DA_DICT_TRANS_JOB, whereFields, whereValues, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataDictTransJob getNewDataObject(EtkProject project) {
        return new iPartsDataDictTransJob(project, null);
    }
}
