package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 *
 */
public class iPartsDataCodeToRemove extends EtkDataObject implements iPartsConst {

    public enum iPartsDataCodeToRemoveTables {
        AS_CODES(TABLE_DA_AS_CODES, FIELD_DAS_CODE, new String[]{ FIELD_DAS_CODE }),
        ACCESSORY_CODES(TABLE_DA_ACCESSORY_CODES, FIELD_DACC_CODE, new String[]{ FIELD_DACC_CODE }),
        CONST_STATUS_CODES(TABLE_DA_CONST_STATUS_CODES, FIELD_DASC_CODE, new String[]{ FIELD_DASC_CODE });

        private String tableName;
        private String codeFieldName;
        private String[] primaryKey;

        iPartsDataCodeToRemoveTables(String tableName, String codeFieldName, String[] primaryKey) {
            this.tableName = tableName;
            this.codeFieldName = codeFieldName;
            this.primaryKey = primaryKey;
        }

        public String getTableName() {
            return tableName;
        }

        public String getCodeFieldName() {
            return codeFieldName;
        }

        public String[] getPrimaryKey() {
            return primaryKey;
        }
    }

    private iPartsDataCodeToRemove(EtkProject project, iPartsDataCodeToRemoveTables table) {
        super(table.getPrimaryKey());
        tableName = table.tableName;
        if (project != null) {
            init(project);
        }
    }

    public iPartsDataCodeToRemove(EtkProject project, iPartsDataCodeToRemoveTables table, iPartsCodeToRemoveId id) {
        this(project, table);
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsCodeToRemoveId createId(String... idValues) {
        return new iPartsCodeToRemoveId(idValues[0]);
    }

    @Override
    public iPartsCodeToRemoveId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsCodeToRemoveId)id;
    }

}



