/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_CTABLE_PART.
 */
public class iPartsDataColorTableToPart extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCTP_TABLE_ID, FIELD_DCTP_POS, FIELD_DCTP_SDATA };

    public iPartsDataColorTableToPart(EtkProject project, iPartsColorTableToPartId id) {
        super(KEYS);
        tableName = TABLE_DA_COLORTABLE_PART;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataColorTableToPart cloneMe(EtkProject project) {
        iPartsDataColorTableToPart clone = new iPartsDataColorTableToPart(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsColorTableToPartId createId(String... idValues) {
        return new iPartsColorTableToPartId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsColorTableToPartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsColorTableToPartId)id;
    }

    @Override
    public boolean loadVirtualField(String attributeName) {
        if (iPartsDataVirtualFieldsDefinition.loadVirtualFieldForFilterReason(attributeName, attributes)) {
            return false;
        }

        return super.loadVirtualField(attributeName);
    }

    /**
     * Handelt es sich um historische Daten?
     *
     * @return
     */
    public boolean isHistoryRecord() {
        String sdatb = getFieldValue(iPartsConst.FIELD_DCTP_SDATB);
        iPartsDialogDateTimeHandler dateTimeHandler = new iPartsDialogDateTimeHandler(sdatb);
        return !dateTimeHandler.isFinalStateDateTime();
    }

    public iPartsImportDataOrigin getDataOrigin() {
        return iPartsImportDataOrigin.getTypeFromCode(getFieldValue(FIELD_DCTP_SOURCE));
    }

    public String getET_KZ() {
        return getFieldValue(FIELD_DCTP_ETKZ);
    }

    public String getPartNumber() {
        return getFieldValue(FIELD_DCTP_PART);
    }
}
