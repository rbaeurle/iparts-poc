/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste von {@link iPartsDataEPCFootNoteSaRef}s.
 */
public class iPartsDataEPCFootNoteSaRefList extends EtkDataObjectList<iPartsDataEPCFootNoteSaRef> implements iPartsConst {

    public static iPartsDataEPCFootNoteSaRefList loadAllRefsForSaWithPlaceholderSigns(EtkProject project, String saNumber) {
        iPartsDataEPCFootNoteSaRefList list = new iPartsDataEPCFootNoteSaRefList();
        list.loadAllRefDataForSaNumber(project, saNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllRefDataForSaNumber(EtkProject project, String saNumber, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_EPC_FN_SA_REF, FIELD_DEFS_SA_NO),
                                             TableAndFieldName.make(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_TYPE) };
        String[] whereValues = new String[]{ saNumber, EPCFootnoteType.SA.getDBValue() };
        String[] sortFields = new String[]{ FIELD_DEFS_TEXT_ID };

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_SA_REF, FIELD_DEFS_SA_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_SA_REF, FIELD_DEFS_FN_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_SA_REF, FIELD_DEFS_TEXT_ID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_SA_REF, FIELD_DEFS_GROUP, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_TYPE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_ABBR, false, false));
        searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues, false, sortFields,
                                  false, null, new JoinData(TABLE_DA_EPC_FN_CONTENT,
                                                            new String[]{ FIELD_DEFS_TEXT_ID },
                                                            new String[]{ FIELD_DEFC_TEXT_ID },
                                                            false, false));
    }

    @Override
    protected iPartsDataEPCFootNoteSaRef getNewDataObject(EtkProject project) {
        return new iPartsDataEPCFootNoteSaRef(project, null);
    }
}