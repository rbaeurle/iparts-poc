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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_EPC_FN_KATALOG_REF.
 */
public class iPartsDataEPCFootNoteCatalogueRef extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DEFR_PRODUCT_NO, FIELD_DEFR_KG, FIELD_DEFR_FN_NO, FIELD_DEFR_TEXT_ID };

    public static final String CHILDREN_NAME_FOOTNOTES = "iPartsDataEPCFootNoteCatalogueRef.footnotes";

    public iPartsDataEPCFootNoteCatalogueRef(EtkProject project, iPartsEPCFootNoteCatalogueRefId id) {
        super(KEYS);
        tableName = TABLE_DA_EPC_FN_KATALOG_REF;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsEPCFootNoteCatalogueRefId createId(String... idValues) {
        return new iPartsEPCFootNoteCatalogueRefId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsEPCFootNoteCatalogueRefId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsEPCFootNoteCatalogueRefId)id;
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
            footnotesList = iPartsDataEPCFootNoteContentList.loadFootNote(getEtkProject(), EPCFootnoteType.MODEL, getAsId().getEPCTextId());
            setChildren(CHILDREN_NAME_FOOTNOTES, footnotesList);
        }
        return footnotesList;
    }

    public void setFootNoteList(DBDataObjectList<iPartsDataEPCFootNoteContent> footNoteContents) {
        setChildren(CHILDREN_NAME_FOOTNOTES, footNoteContents);
    }
}
