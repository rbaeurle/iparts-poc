/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_COLORTABLE_CONTENT.
 */
public class iPartsDataColorTableContent extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCTC_TABLE_ID, FIELD_DCTC_POS, FIELD_DCTC_SDATA };

    public iPartsDataColorTableContent(EtkProject project, iPartsColorTableContentId id) {
        super(KEYS);
        tableName = TABLE_DA_COLORTABLE_CONTENT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataColorTableContent cloneMe(EtkProject project) {
        iPartsDataColorTableContent clone = new iPartsDataColorTableContent(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsColorTableContentId createId(String... idValues) {
        return new iPartsColorTableContentId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsColorTableContentId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsColorTableContentId)id;
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
        String sdatb = getFieldValue(iPartsConst.FIELD_DCTC_SDATB);
        iPartsDialogDateTimeHandler dateTimeHandler = new iPartsDialogDateTimeHandler(sdatb);
        return !dateTimeHandler.isFinalStateDateTime();
    }

    /**
     * Gibt die Farbummer (ES2 Schlüssel) zurück.
     *
     * @return
     */
    public String getColorNumber() {
        return getFieldValue(FIELD_DCTC_COLOR_VAR);
    }

    public String getET_KZ() {
        return getFieldValue(FIELD_DCTC_ETKZ);
    }

    public iPartsDataReleaseState getStatus() {
        return iPartsDataReleaseState.getTypeByDBValue(getFieldValue(FIELD_DCTC_STATUS));
    }

    public iPartsImportDataOrigin getDataOrigin() {
        return iPartsImportDataOrigin.getTypeFromCode(getFieldValue(FIELD_DCTC_SOURCE));
    }
}
