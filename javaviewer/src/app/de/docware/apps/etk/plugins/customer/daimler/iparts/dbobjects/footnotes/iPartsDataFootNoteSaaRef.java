/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_FN_SAA_REF.
 */
public class iPartsDataFootNoteSaaRef extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DFNS_SAA, FIELD_DFNS_FNID };

    private iPartsDataFootNoteContentList footnoteContents;

    public iPartsDataFootNoteSaaRef(EtkProject project, iPartsFootNoteSaaRefId id) {
        super(KEYS);
        tableName = TABLE_DA_FN_SAA_REF;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    /**
     * Erzeugt einen Datensatz für die übergebene {@link iPartsFootNoteSaaRefId} und Fußnoten laufende Nummer.
     *
     * @param project
     * @param id
     * @param footNoteSeqNr
     */
    public iPartsDataFootNoteSaaRef(EtkProject project, iPartsFootNoteSaaRefId id, int footNoteSeqNr) {
        this(project, id);
        initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DFNS_FN_SEQNO, EtkDbsHelper.formatLfdNr(footNoteSeqNr), DBActionOrigin.FROM_EDIT);
    }

    @Override
    public iPartsFootNoteSaaRefId createId(String... idValues) {
        return new iPartsFootNoteSaaRefId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsFootNoteSaaRefId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsFootNoteSaaRefId)id;
    }

    public void clear() {
        footnoteContents = null;
    }

    public iPartsDataFootNoteContentList getFootNoteList() {
        if (footnoteContents == null) {
            footnoteContents = iPartsDataFootNoteContentList.loadFootNote(getEtkProject(), getAsId().getFootNoteId());
        }
        return footnoteContents;
    }
}
