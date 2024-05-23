/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_EPC_FN_SA_REF.
 */

public class iPartsDataEPCFootNoteSaRef extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DEFS_SA_NO, FIELD_DEFS_FN_NO };

    public static final String CHILDREN_NAME_FOOTNOTES = "iPartsDataEPCFootNoteSaRef.footnotes";

    public iPartsDataEPCFootNoteSaRef(EtkProject project, iPartsEPCFootNoteSaRefId id) {
        super(KEYS);
        tableName = TABLE_DA_EPC_FN_SA_REF;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsEPCFootNoteSaRefId createId(String... idValues) {
        return new iPartsEPCFootNoteSaRefId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsEPCFootNoteSaRefId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsEPCFootNoteSaRefId)id;
    }

    public void clear() {
        setChildren(CHILDREN_NAME_FOOTNOTES, null);
    }

    @Override
    public void deleteFromDB(boolean forceDelete) {
        if (forceDelete || !isNew()) { // ein neuer Datensatz muss keine Kindelemente aus der DB laden
            loadChildren();
        }
        super.deleteFromDB(forceDelete);
    }

    public void loadChildren() {
        getEPCFootNoteList();
    }

    public DBDataObjectList<iPartsDataEPCFootNoteContent> getEPCFootNoteList() {
        DBDataObjectList<iPartsDataEPCFootNoteContent> footnotesList = (DBDataObjectList<iPartsDataEPCFootNoteContent>)getChildren(CHILDREN_NAME_FOOTNOTES);
        if (footnotesList == null) {
            footnotesList = iPartsDataEPCFootNoteContentList.loadFootNote(getEtkProject(), EPCFootnoteType.SA, getFieldValue(FIELD_DEFS_TEXT_ID));
            setChildren(CHILDREN_NAME_FOOTNOTES, footnotesList);
        }
        return footnotesList;
    }

    public void setFootNoteList(DBDataObjectList<iPartsDataEPCFootNoteContent> footNoteContents) {
        setChildren(CHILDREN_NAME_FOOTNOTES, footNoteContents);
    }
}
