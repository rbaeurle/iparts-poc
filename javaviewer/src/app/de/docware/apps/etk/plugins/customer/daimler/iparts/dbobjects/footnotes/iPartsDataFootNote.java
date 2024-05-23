/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_FN.
 */
public class iPartsDataFootNote extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DFN_ID };

    public static final String CHILDREN_NAME_FOOTNOTES = "iPartsDataFootNote.footnotes";

    public static final String FOOTNOTE_PREFIX_ELDAS = "ELDAS";
    public static final String FOOTNOTE_PREFIX_EPC = "EPC";
    public static final String FOOTNOTE_ID_DELIMITER = "_";

    public iPartsDataFootNote(EtkProject project, iPartsFootNoteId id) {
        super(KEYS);
        tableName = TABLE_DA_FN;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataFootNote cloneMe(EtkProject project) {
        iPartsDataFootNote clone = new iPartsDataFootNote(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsFootNoteId createId(String... idValues) {
        return new iPartsFootNoteId(idValues[0]);
    }

    @Override
    public iPartsFootNoteId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsFootNoteId)id;
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
        getFootNoteList();
    }


    public DBDataObjectList<iPartsDataFootNoteContent> getFootNoteList() {
        DBDataObjectList<iPartsDataFootNoteContent> footnotesList = (DBDataObjectList<iPartsDataFootNoteContent>)getChildren(CHILDREN_NAME_FOOTNOTES);
        if (footnotesList == null) {
            footnotesList = iPartsDataFootNoteContentList.loadFootNote(getEtkProject(), getAsId().getFootNoteId());
            setChildren(CHILDREN_NAME_FOOTNOTES, footnotesList);
        }
        return footnotesList;

    }
}
