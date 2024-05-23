/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_FN_KATALOG_REF.
 */
public class iPartsDataFootNoteCatalogueRef extends AbstractFootnoteRef implements EtkDbConst, iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DFNK_MODULE, FIELD_DFNK_MODVER, FIELD_DFNK_SEQNO, FIELD_DFNK_FNID };

    public iPartsDataFootNoteCatalogueRef(EtkProject project, iPartsFootNoteCatalogueRefId id) {
        super(KEYS);
        tableName = TABLE_DA_FN_KATALOG_REF;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    /**
     * Erzeugt einen Datensatz für die übergebene {@link iPartsFootNoteCatalogueRefId} und Fußnoten laufende Nummer.
     *
     * @param project
     * @param id
     * @param footNoteSeqNr
     */
    public iPartsDataFootNoteCatalogueRef(EtkProject project, iPartsFootNoteCatalogueRefId id, int footNoteSeqNr) {
        this(project, id);
        initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DFNK_FN_SEQNO, EtkDbsHelper.formatLfdNr(footNoteSeqNr), DBActionOrigin.FROM_EDIT);
    }

    @Override
    public iPartsFootNoteCatalogueRefId createId(String... idValues) {
        return new iPartsFootNoteCatalogueRefId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsFootNoteCatalogueRefId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsFootNoteCatalogueRefId)id;
    }

    @Override
    public iPartsDataFootNoteCatalogueRef cloneMe(EtkProject project) {
        iPartsDataFootNoteCatalogueRef clone = new iPartsDataFootNoteCatalogueRef(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    protected String getFootnoteId() {
        return getAsId().getFootNoteId();
    }

    // Convienience methods
    public PartListEntryId getPartListId() {
        return new PartListEntryId(getAsId().getModuleId(), getAsId().getModuleVer(), getAsId().getModuleSeqNo());
    }

    public String getSequenceNumber() {
        return getFieldValue(FIELD_DFNK_FN_SEQNO);
    }

    public void setSequenceNumber(int sequenceNumber) {
        setFieldValue(FIELD_DFNK_FN_SEQNO, EtkDbsHelper.formatLfdNr(sequenceNumber), DBActionOrigin.FROM_EDIT);
    }

    public String getCacheKey() {
        String key = getAsId().toString("|");
        if (attributeExists(FIELD_DFNK_FN_SEQNO)) {
            key += "|" + getFieldValue(FIELD_DFNK_FN_SEQNO);
        }
        return key;
    }
}
