/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_FN_MAT_REF.
 */
public class iPartsDataFootNoteMatRef extends AbstractFootnoteRef implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DFNM_MATNR, FIELD_DFNM_FNID };

    public iPartsDataFootNoteMatRef(EtkProject project, iPartsFootNoteMatRefId id) {
        super(KEYS);
        tableName = TABLE_DA_FN_MAT_REF;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataFootNoteMatRef cloneMe(EtkProject project) {
        iPartsDataFootNoteMatRef clone = new iPartsDataFootNoteMatRef(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsFootNoteMatRefId createId(String... idValues) {
        return new iPartsFootNoteMatRefId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsFootNoteMatRefId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsFootNoteMatRefId)id;
    }

    public void setSource(iPartsImportDataOrigin source, DBActionOrigin origin) {
        setFieldValue(FIELD_DFNM_SOURCE, source.getOrigin(), origin);
    }

    public iPartsImportDataOrigin getSource() {
        String source = getFieldValue(FIELD_DFNM_SOURCE);
        if (StrUtils.isValid(source)) {
            return iPartsImportDataOrigin.getTypeFromCode(source);
        }
        // Default-Wert
        return iPartsImportDataOrigin.DIALOG;
    }

    public boolean isiPartsSource() {
        return getSource() == iPartsImportDataOrigin.IPARTS;
    }

    @Override
    protected String getFootnoteId() {
        return getAsId().getFootNoteId();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof iPartsDataFootNoteMatRef)) {
            iPartsDataFootNoteMatRef footNoteMatRef = (iPartsDataFootNoteMatRef)obj;
            return footNoteMatRef.getAsId().equals(this.getAsId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getAsId().hashCode();
    }
}
