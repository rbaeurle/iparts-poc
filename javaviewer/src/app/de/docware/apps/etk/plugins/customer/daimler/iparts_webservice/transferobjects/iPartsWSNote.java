/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsNote;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Note Data Transfer Object für die iParts Webservices
 */
public class iPartsWSNote implements RESTfulTransferObjectInterface {

    // Platzhalter für ein komplexeres Note DTO
    String text;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSNote() {
    }

    public iPartsWSNote(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Konvertiert die übergebenen Notizen basierend auf der gewünschten Sprache und Rückfallsprachen in eine Liste von
     * {@link iPartsWSNote}-Objekten.
     *
     * @param notes
     * @param requestedLanguage
     * @param fallbackLanguages
     * @return {@code null} falls die übergebenen Notizen {@code null} sind oder keine Notizen basierend auf den übergebenen
     * Sprachen gefunden wurden
     */
    @JsonIgnore
    public static List<iPartsWSNote> convertToWSNotes(List<iPartsNote> notes, String requestedLanguage, List<String> fallbackLanguages) {
        if (notes == null) {
            return null;
        }

        List<iPartsWSNote> resultNotes = new DwList<iPartsWSNote>(notes.size());
        for (iPartsNote note : notes) {
            String noteText = note.getMultiLangText().getTextByNearestLanguage(requestedLanguage, fallbackLanguages);
            if (!noteText.isEmpty()) {
                resultNotes.add(new iPartsWSNote(noteText));
            }
        }
        if (!resultNotes.isEmpty()) {
            return resultNotes;
        } else {
            return null;
        }
    }
}
