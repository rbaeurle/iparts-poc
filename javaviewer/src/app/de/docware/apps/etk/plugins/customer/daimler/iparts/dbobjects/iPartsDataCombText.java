package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

/**
 * Repräsentiert einen Datensatz der Tabelle DA_COMB_TEXT zur Speicherung der "Kombinierten Texte"
 * siehe https://confluence.docware.de/confluence/x/DgBAAQ#KombiErgText
 */
public class iPartsDataCombText extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCT_MODULE, FIELD_DCT_MODVER, FIELD_DCT_SEQNO, FIELD_DCT_TEXT_SEQNO };

    /**
     * Erzeugt einen Datensatz für die übergebene {@link PartListEntryId}, mehrsprachigen Text und sprachneutralen Text
     * am angegebenen Index (laufende Nummer des Datensatzes für den Stücklisteneintrag).
     *
     * @param partListEntryId
     * @param textMultiLang
     * @param textNeutral
     * @param combinedTextSeqNo
     * @param project
     * @return
     */
    public iPartsDataCombText(EtkProject project, PartListEntryId partListEntryId, EtkMultiSprache textMultiLang, String textNeutral,
                              int combinedTextSeqNo) {
        this(project, new iPartsCombTextId(partListEntryId, EtkDbsHelper.formatLfdNr(combinedTextSeqNo)));
        initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        if (textMultiLang != null) {
            setFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT, textMultiLang, DBActionOrigin.FROM_EDIT);
        }
        if (!StrUtils.isEmpty(textNeutral)) {
            setFieldValue(FIELD_DCT_TEXT_NEUTRAL, textNeutral, DBActionOrigin.FROM_EDIT);
        }
    }

    public iPartsDataCombText(EtkProject project, iPartsCombTextId id) {
        super(KEYS);
        tableName = TABLE_DA_COMB_TEXT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsCombTextId createId(String... idValues) {
        return new iPartsCombTextId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsCombTextId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsCombTextId)id;
    }

    @Override
    public iPartsDataCombText cloneMe(EtkProject project) {
        iPartsDataCombText clone = new iPartsDataCombText(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }
}
