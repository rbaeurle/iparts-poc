/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.*;

/**
 * Hilfsklasse für Fußnoten-Caches
 */
public class FootnoteWithTextForCache implements RESTfulTransferObjectInterface {

    @JsonProperty
    private iPartsFootNoteId footnoteId;
    @JsonProperty
    private final Map<String, iPartsFootNote> footnotes = new HashMap<>();
    @JsonProperty
    private final Set<String> fnLines = new HashSet<>();

    public FootnoteWithTextForCache() {
    }

    public FootnoteWithTextForCache(iPartsFootNoteId footnoteId) {
        this.footnoteId = footnoteId;
    }

    /**
     * Fügt der Fußnote einen Teiltext hinzu
     *
     * @param project
     * @param fnName
     * @param fnType
     * @param lineNo
     * @param isStandard
     * @param text
     */
    public void addText(EtkProject project, String fnName, iPartsFootnoteType fnType, String lineNo, boolean isStandard, EtkMultiSprache text) {
        if (!fnLines.contains(lineNo)) {
            fnLines.add(lineNo);
            List<String> dbFallbackLanguages = project.getDataBaseFallbackLanguages();
            for (Map.Entry<String, String> languageEntry : text.getLanguagesAndTexts().entrySet()) {
                iPartsFootNote footnoteForLanguage = footnotes.get(languageEntry.getKey());
                if (footnoteForLanguage == null) {
                    footnoteForLanguage = new iPartsFootNote(footnoteId, fnName, new ArrayList<>(), isStandard, fnType);
                    footnotes.put(languageEntry.getKey(), footnoteForLanguage);
                }
                footnoteForLanguage.getFootNoteTexts(project).add(iPartsFootNote.convertHashtagText(project, languageEntry.getValue(),
                                                                                                    languageEntry.getKey(),
                                                                                                    dbFallbackLanguages));
            }
        }
    }

    public iPartsFootNote getFootnoteForLanguage(EtkProject project, String language) {
        iPartsFootNote footNote = footnotes.get(language);
        if (footNote == null) { // Rückfallsprachen berücksichtigen
            for (String fallbackLanguage : project.getDataBaseFallbackLanguages()) {
                footNote = footnotes.get(fallbackLanguage);
                if (footNote != null) {
                    break;
                }
            }
        }

        return footNote;
    }

    public iPartsFootNoteId getFootnoteId() {
        return footnoteId;
    }
}
