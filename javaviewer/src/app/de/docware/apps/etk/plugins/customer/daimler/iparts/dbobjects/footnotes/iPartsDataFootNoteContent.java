/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_FN_CONTENT.
 */
public class iPartsDataFootNoteContent extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DFNC_FNID, FIELD_DFNC_LINE_NO };

    public iPartsDataFootNoteContent(EtkProject project, iPartsFootNoteContentId id) {
        super(KEYS);
        tableName = TABLE_DA_FN_CONTENT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    public iPartsDataFootNoteContent(EtkProject project, iPartsFootNoteId footNoteId, String footNoteLineString) {
        this(project, footNoteId.getFootNoteId(), footNoteLineString);
    }

    public iPartsDataFootNoteContent(EtkProject project, String footNoteId, String footNoteLineString) {
        this(project, new iPartsFootNoteContentId(footNoteId, EtkDbsHelper.formatLfdNr(StrUtils.strToIntDef(footNoteLineString, 0))));
    }

    @Override
    public iPartsDataFootNoteContent cloneMe(EtkProject project) {
        iPartsDataFootNoteContent clone = new iPartsDataFootNoteContent(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsFootNoteContentId createId(String... idValues) {
        return new iPartsFootNoteContentId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsFootNoteContentId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsFootNoteContentId)id;
    }

    /* Convenience Methods */

    /**
     * liefert das MultiSprach-Feld einer Fussnote
     *
     * @return
     */
    public EtkMultiSprache getMultiText() {
        return getFieldValueAsMultiLanguage(iPartsConst.FIELD_DFNC_TEXT);
    }

    /**
     * liefert den sprachneutralen Text einer Fussnote
     *
     * @return
     */
    public String getNeutralText() {
        return getFieldValue(iPartsConst.FIELD_DFNC_TEXT_NEUTRAL);
    }

    /**
     * Liefert entweder den mehrsprachigen Text oder den sprachneutralen Text.
     * Die #-Texte werden dabei ersetzt.
     *
     * @param language
     * @param fallbackLanguages
     * @return
     */
    public String getText(String language, List<String> fallbackLanguages) {
        return iPartsFootNote.convertHashtagText(getEtkProject(), getUnmodifiedText(language, fallbackLanguages), language, fallbackLanguages);
    }

    /**
     * Liefert entweder den mehrsprachigen Text oder den sprachneutralen Text.
     * Die #-Texte werden dabei NICHT ersetzt.
     *
     * @param language
     * @param fallbackLanguages
     * @return
     */
    public String getUnmodifiedText(String language, List<String> fallbackLanguages) {
        String neutralText = getNeutralText();
        if (!neutralText.isEmpty()) {
            return neutralText;
        }

        EtkMultiSprache multi = getMultiText();
        if (multi != null) {
            return multi.getTextByNearestLanguage(language, fallbackLanguages);
        }

        return "";
    }

    /**
     * Mehrzeilige Fußnoten aus der Migration können nicht editiert werden.
     * Für Edit-Funktionen wird die Fußnote konvertiert, dazu wird die Neue ID aus der TextID mit Präfix und der
     * Zeilennummer "00001" generiert. Der Text bleibt bestehen.
     *
     * @return Neuer Eintrag mit generierter ID und dem Text des originals
     */
    public iPartsDataFootNoteContent convertToSingleLineEditFootnote() {
        String textId = getMultiText().getTextId();
        iPartsFootNoteContentId newFootNoteId = iPartsFootNoteContentId.createConvertedFootNoteId(textId);
        iPartsDataFootNoteContent newFootNoteContent = new iPartsDataFootNoteContent(getEtkProject(), newFootNoteId);
        newFootNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        newFootNoteContent.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DFNC_TEXT, getMultiText(), DBActionOrigin.FROM_EDIT);
        return newFootNoteContent;
    }
}
