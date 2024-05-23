/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHashtagTextsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsFootnoteType;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Datenklasse für eine Fußnote in iParts inkl. {@link iPartsFootNoteId}, Fußnotenname und den Fußnotentexten in der aktuellen DB-Sprache.
 */
public class iPartsFootNote implements RESTfulTransferObjectInterface {

    @JsonProperty
    private iPartsFootNoteId footNoteId;
    @JsonProperty
    private String footNoteName;
    @JsonProperty
    private List<String> footNoteTexts;
    @JsonProperty
    private boolean isStandardFootNote;
    @JsonProperty
    private iPartsFootnoteType footnoteType;
    @JsonProperty
    private boolean isMarked;

    public iPartsFootNote() {
    }

    public iPartsFootNote(iPartsFootNoteId footNoteId) {
        this(footNoteId, null, null, false, iPartsFootnoteType.NO_TYPE);
    }

    public iPartsFootNote(iPartsFootNoteId footNoteId, String footNoteName, List<String> footNoteTexts,
                          boolean isStandardFootNote, iPartsFootnoteType footnoteType) {
        this.footNoteId = footNoteId;
        this.footNoteName = footNoteName;
        this.footNoteTexts = footNoteTexts;
        this.isStandardFootNote = isStandardFootNote;
        this.footnoteType = footnoteType;
    }

    public iPartsFootNote cloneMe() {
        iPartsFootNote clonedFootNote = new iPartsFootNote(footNoteId, footNoteName, null, isStandardFootNote, footnoteType);
        if (footNoteTexts != null) {
            clonedFootNote.footNoteTexts = new ArrayList<>(footNoteTexts);
        }
        clonedFootNote.isMarked = isMarked;
        return clonedFootNote;
    }

    /**
     * Liefert die Fußnoten-ID zurück.
     *
     * @return
     */
    public iPartsFootNoteId getFootNoteId() {
        return footNoteId;
    }

    /**
     * Liefert den Fußnotennamen zurück und lädt diesen vorher bei Bedarf aus der Datenbank.
     *
     * @param project
     * @return
     */
    public String getFootNoteName(EtkProject project) {
        if (footNoteName == null) { // Fußnotenname nachladen
            iPartsDataFootNote footNote = new iPartsDataFootNote(project, footNoteId);
            footNoteName = footNote.getFieldValue(iPartsConst.FIELD_DFN_NAME);
        }

        return footNoteName;
    }

    /**
     * Liefert die Fußnotentexte zurück und lädt diese vorher bei Bedarf aus der Datenbank.
     *
     * @param project
     * @return
     */
    public List<String> getFootNoteTexts(EtkProject project) {
        if (footNoteTexts == null) { // Fußnotentexte nachladen
            footNoteTexts = new ArrayList<String>();
            String dbLanguage = project.getDBLanguage();
            List<String> fallbackLanguages = project.getDataBaseFallbackLanguages();
            iPartsDataFootNoteContentList contentList = iPartsDataFootNoteContentList.loadFootNote(project, footNoteId);
            for (iPartsDataFootNoteContent content : contentList) {
                footNoteTexts.add(content.getText(dbLanguage, fallbackLanguages));
            }
        }

        return footNoteTexts;
    }

    public boolean isStandardFootNote() {
        return isStandardFootNote;
    }

    public void setFootnoteType(iPartsFootnoteType footnoteType) {
        this.footnoteType = footnoteType;
    }

    public iPartsFootnoteType getFootnoteType() {
        return footnoteType;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setIsMarked(boolean isMarked) {
        this.isMarked = isMarked;
    }

    public boolean isColorTablefootnote() {
        return footnoteType == iPartsFootnoteType.COLOR_TABLEFOOTNOTE;
    }

    public boolean isPartFootnote() {
        return footnoteType == iPartsFootnoteType.PART;
    }

    public boolean isDIALOGFootnote() {
        return footnoteType == iPartsFootnoteType.CONSTRUCTION_FOOTNOTE;
    }

    public boolean isRealFootnote() {
        return footnoteType.isRealFootnote();
    }

    public static String convertHashtagText(EtkProject project, String text, String language, List<String> fallbackLanguages) {
        // #-Platzhalter fangen immer mit # an und sind genau 3 Zeichen lang
        int hashtagIndex = text.indexOf('#');
        int hashtagEndIndex = hashtagIndex + 3;
        while ((hashtagIndex >= 0) && (text.length() >= hashtagEndIndex)) {
            int hashtagTextLength = 1;

            // #-Platzhalter muss am Zeilenanfang stehen oder es muss ein Leerzeichen davor sein
            if ((hashtagIndex == 0) || (text.charAt(hashtagIndex - 1) == ' ') || (text.charAt(hashtagIndex - 1) == '\n')) {
                String hashtagKey = text.substring(hashtagIndex, hashtagEndIndex);
                String hashtagText = DictHashtagTextsCache.getInstance(project).getText(hashtagKey, language, fallbackLanguages);
                if (StrUtils.isValid(hashtagText)) {
                    hashtagTextLength = hashtagText.length();
                    if (text.length() > hashtagEndIndex) { // Es gibt noch Text nach dem Hashtag
                        // Sicherstellen, dass nach dem ersetzten Text auch ein Leerzeichen kommt
                        if (text.charAt(hashtagEndIndex) != ' ') {
                            text = text.substring(0, hashtagIndex) + hashtagText + " " + text.substring(hashtagEndIndex);
                        } else {
                            text = text.substring(0, hashtagIndex) + hashtagText + text.substring(hashtagEndIndex);
                        }
                    } else { // Textende erreicht
                        text = text.substring(0, hashtagIndex) + hashtagText;
                    }
                }
            }

            hashtagIndex = text.indexOf('#', hashtagIndex + hashtagTextLength);
            hashtagEndIndex = hashtagIndex + 3;
        }

        return text;
    }
}