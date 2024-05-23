/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Abstrakte Klasse zum vereinheitlichen von Fußnotenreferenz-Objekten
 */
public abstract class AbstractFootnoteRef extends EtkDataObject implements iPartsConst {

    private iPartsDataFootNoteContentList footnoteContents;

    protected AbstractFootnoteRef(String[] pkKeys) {
        super(pkKeys);
    }

    public iPartsDataFootNoteContentList getFootNoteList() {
        if (footnoteContents == null) {
            footnoteContents = iPartsDataFootNoteContentList.loadFootNote(getEtkProject(), getFootnoteId());
        }
        return footnoteContents;
    }

    public void setFootNoteList(iPartsDataFootNoteContentList footNoteContents) {
        this.footnoteContents = footNoteContents;
    }

    protected abstract String getFootnoteId();

    public void clear() {
        footnoteContents = null;
    }

}
