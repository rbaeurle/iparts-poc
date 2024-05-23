/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_EPC_FN_KATALOG_REF.
 */
public class iPartsDataEPCFootNoteContent extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DEFC_TYPE, FIELD_DEFC_TEXT_ID, FIELD_DEFC_LINE_NO };

    public iPartsDataEPCFootNoteContent(EtkProject project, iPartsEPCFootNoteContentId id) {
        super(KEYS);
        tableName = TABLE_DA_EPC_FN_CONTENT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsEPCFootNoteContentId createId(String... idValues) {
        return new iPartsEPCFootNoteContentId(EPCFootnoteType.getFromDBValue(idValues[0]), idValues[1], idValues[2]);
    }

    @Override
    public iPartsEPCFootNoteContentId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsEPCFootNoteContentId)id;
    }
}
