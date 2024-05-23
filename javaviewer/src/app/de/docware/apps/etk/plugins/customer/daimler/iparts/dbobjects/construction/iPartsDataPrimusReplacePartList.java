/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste von Primus-Ersetzungs-Hinweisen {@link iPartsDataPrimusReplacePart}.
 */
public class iPartsDataPrimusReplacePartList extends EtkDataObjectList<iPartsDataPrimusReplacePart> implements iPartsConst {

    /**
     * Primus-Ersetzungs-Hinweise können nicht vom Autor geändert werden, also ChangeSets nicht berücksichtigen.
     */
    public iPartsDataPrimusReplacePartList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * lädt alle Primus-Ersetzungs-Hinweise inklusive deren Mitlieferteile.
     *
     * @param project
     * @param foundAttributesCallback Optionaler {@link FoundAttributesCallback}
     * @return
     */
    public static iPartsDataPrimusReplacePartList loadPrimusReplacePartsWithIncludeParts(EtkProject project, FoundAttributesCallback foundAttributesCallback) {
        iPartsDataPrimusReplacePartList list = new iPartsDataPrimusReplacePartList();
        list.loadPrimusReplacePartsWithIncludePartsFromDB(project, foundAttributesCallback);
        return list;
    }

    /**
     * lädt alle Primus-Ersetzungs-Hinweise mit Alternativteilen für den Nachfolger.
     *
     * @param project
     * @param foundAttributesCallback Optionaler {@link FoundAttributesCallback}
     * @return
     */
    public static iPartsDataPrimusReplacePartList loadPrimusReplacePartsWithAlternativeParts(EtkProject project, FoundAttributesCallback foundAttributesCallback) {
        iPartsDataPrimusReplacePartList list = new iPartsDataPrimusReplacePartList();
        list.loadPrimusReplacePartsWithAlternativePartsFromDB(project, foundAttributesCallback);
        return list;
    }

    private void loadPrimusReplacePartsWithIncludePartsFromDB(EtkProject project, FoundAttributesCallback foundAttributesCallback) {
        clear(DBActionOrigin.FROM_DB);

        EtkDisplayFields selectFields = project.getAllDisplayFieldsForTable(TABLE_DA_PRIMUS_REPLACE_PART);
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_PRIMUS_INCLUDE_PART));
        searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields,
                                  null, null,
                                  false, null,
                                  false, foundAttributesCallback,
                                  new JoinData(TABLE_DA_PRIMUS_INCLUDE_PART,
                                               new String[]{ FIELD_PRP_PART_NO },
                                               new String[]{ FIELD_PIP_PART_NO },
                                               true, false));
    }

    private void loadPrimusReplacePartsWithAlternativePartsFromDB(EtkProject project, FoundAttributesCallback foundAttributesCallback) {
        clear(DBActionOrigin.FROM_DB);

        EtkDisplayFields selectFields = project.getAllDisplayFieldsForTable(TABLE_DA_PRIMUS_REPLACE_PART);
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_VER, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_BASE_MATNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_1, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_2, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_STATE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_SECURITYSIGN_REPAIR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_IS_DELETED, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR_MBAG, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR_DTAG, false, false));

        searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields,
                                  null, null, new String[]{ TableAndFieldName.make(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_SUCCESSOR_PARTNO),
                                                            TableAndFieldName.make(TABLE_MAT, FIELD_M_AS_ES_1) }, new String[]{ "", "" },
                                  false, null, false, false, foundAttributesCallback,
                                  new JoinData(TABLE_MAT,
                                               new String[]{ FIELD_PRP_SUCCESSOR_PARTNO },
                                               new String[]{ FIELD_M_BASE_MATNR },
                                               false, false));
    }

    @Override
    protected iPartsDataPrimusReplacePart getNewDataObject(EtkProject project) {
        return new iPartsDataPrimusReplacePart(project, null);
    }
}
