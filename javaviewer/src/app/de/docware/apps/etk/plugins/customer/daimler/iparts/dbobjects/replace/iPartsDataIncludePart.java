/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_INCLUDE_PART.
 */
public class iPartsDataIncludePart extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DIP_VARI, FIELD_DIP_VER, FIELD_DIP_LFDNR, FIELD_DIP_REPLACE_MATNR, FIELD_DIP_REPLACE_LFDNR, FIELD_DIP_SEQNO };

    public iPartsDataIncludePart(EtkProject project, iPartsIncludePartId id) {
        super(KEYS);
        tableName = TABLE_DA_INCLUDE_PART;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsIncludePartId createId(String... idValues) {
        return new iPartsIncludePartId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsIncludePartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsIncludePartId)id;
    }

    /**
     * Ist dieses Mitlieferteil gültig für die übergebene Ersetzung?
     *
     * @param dataReplacePart
     * @return
     */
    public boolean isValidForReplacement(iPartsDataReplacePart dataReplacePart) {
        if (!getAsId().getPredecessorPartListEntryId().equals(dataReplacePart.getAsId().getPredecessorPartListEntryId())) {
            return false;
        }
        if (!getFieldValue(FIELD_DIP_REPLACE_MATNR).equals(dataReplacePart.getFieldValue(FIELD_DRP_REPLACE_MATNR))) {
            return false;
        }
        if (!getFieldValue(FIELD_DIP_REPLACE_LFDNR).equals(dataReplacePart.getFieldValue(FIELD_DRP_REPLACE_LFDNR))) {
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob dieses Mitlieferteil ein Duplikat zum übergebenen Mitlieferteil ist. Das ist es, wenn es in allen Feldern
     * übereinstimmt, bis auf die Sequenznummer.
     *
     * @param otherDataIncludePart
     * @return
     */
    public boolean isDuplicateOf(iPartsDataIncludePart otherDataIncludePart) {
        for (DBDataObjectAttribute otherDataAttribute : otherDataIncludePart.getAttributes().getFields()) {
            if (otherDataAttribute.getName().equals(iPartsConst.FIELD_DIP_SEQNO)
                || otherDataAttribute.getName().equals(DBConst.FIELD_STAMP)) {
                continue;
            }
            DBDataObjectAttribute attribute = getAttribute(otherDataAttribute.getName());
            if ((attribute != null) && !otherDataAttribute.equalContent(attribute)) {
                return false;
            }
        }
        return true;
    }
}
