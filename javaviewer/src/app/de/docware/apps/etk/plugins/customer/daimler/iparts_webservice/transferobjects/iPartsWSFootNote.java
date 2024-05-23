/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * FootNote Data Transfer Object für die iParts Webservices
 */
public class iPartsWSFootNote implements RESTfulTransferObjectInterface {

    private enum TYPE {
        legacyColor,
        text
    }

    // Platzhalter für ein komplexeres FootNote DTO
    private String id;
    private String text;
    private String type;
    private boolean mark;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSFootNote() {
    }

    public iPartsWSFootNote(EtkProject project, iPartsFootNote footNote) {
        String footnoteName = footNote.getFootNoteName(project);
        if (StrUtils.isValid(footnoteName)) {
            this.id = footnoteName;
        } else {
            this.id = " ";
        }
        List<String> texts = footNote.getFootNoteTexts(project);
        // Text content of the footnote. All content (including table-formatted footnotes) is returned as one single
        // text-body separated by '\n' (Carriage-Return) delimiters.
        this.text = StrUtils.stringListToString(texts, "\n");
        if (footNote.isColorTablefootnote()) {
            this.type = TYPE.legacyColor.name();
        } else {
            this.type = TYPE.text.name();
        }
        this.mark = footNote.isMarked();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMark() {
        return mark;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }
}
