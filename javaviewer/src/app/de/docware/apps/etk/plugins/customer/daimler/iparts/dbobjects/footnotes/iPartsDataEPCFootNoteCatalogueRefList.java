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
 * Liste von {@link iPartsDataEPCFootNoteCatalogueRef}s.
 */
public class iPartsDataEPCFootNoteCatalogueRefList extends EtkDataObjectList<iPartsDataEPCFootNoteCatalogueRef> implements iPartsConst {

    public static iPartsDataEPCFootNoteCatalogueRefList loadAllRefsForKGWithPlaceholderSigns(EtkProject project,
                                                                                             String productNo, String kgNumber) {
        iPartsDataEPCFootNoteCatalogueRefList list = new iPartsDataEPCFootNoteCatalogueRefList();
        list.loadAllRefDataForKgNumber(project, productNo, kgNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllRefDataForKgNumber(EtkProject project, String productNo, String kgNumber, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_PRODUCT_NO),
                                             TableAndFieldName.make(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_KG),
                                             TableAndFieldName.make(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_TYPE) };
        String[] whereValues = new String[]{ productNo, kgNumber, EPCFootnoteType.MODEL.getDBValue() };
        String[] sortFields = new String[]{ FIELD_DEFR_TEXT_ID };

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_PRODUCT_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_KG, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_FN_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_TEXT_ID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_KATALOG_REF, FIELD_DEFR_GROUP, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_TYPE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_ABBR, false, false));
        searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues, false, sortFields,
                                  false, null, new JoinData(TABLE_DA_EPC_FN_CONTENT,
                                                            new String[]{ FIELD_DEFR_TEXT_ID },
                                                            new String[]{ FIELD_DEFC_TEXT_ID },
                                                            false, false));
    }

    @Override
    protected iPartsDataEPCFootNoteCatalogueRef getNewDataObject(EtkProject project) {
        return new iPartsDataEPCFootNoteCatalogueRef(project, null);
    }
}
