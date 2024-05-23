package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.iPartsDictConst;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.List;

/**
 * Pseudo-Id für das Handling des Lexikon-Cache bei Cluster-Events
 */
public class iPartsDictMetaCacheId extends IdWithType {


    public static String TYPE = "iPartsDictMetaCacheId";

    public static List<iPartsDictMetaCacheId> prepareDictMetaCacheIdList(iPartsDataDictMeta dataDictMeta, DictTextKindTypes dictTextKindType,
                                                                         EtkMultiSprache multiLang) {
        List<iPartsDictMetaCacheId> list = null;
        String state = dataDictMeta.getState();
        if (StrUtils.isValid(state)) {
            list = new DwList<>();
            addCacheIds(list, dataDictMeta, dictTextKindType, multiLang, !state.equals(iPartsDictConst.DICT_STATUS_RELEASED));
            if (list.isEmpty()) {
                list = null;
            }
        }
        return list;
    }

    private static void addCacheIds(List<iPartsDictMetaCacheId> list, iPartsDataDictMeta dataDictMeta, DictTextKindTypes dictTextKindType,
                                    EtkMultiSprache multiLang, boolean checkNotReleasedTexts) {
        if (DictTextCache.TEXT_KIND_TYPES_FOR_WARM_UP.contains(dictTextKindType)) {
            for (Language lang : DictTextCache.LANGUAGES_FOR_WARM_UP) {
                if (multiLang.containsLanguage(lang, true)) {
                    list.add(new iPartsDictMetaCacheId(dataDictMeta, dictTextKindType, multiLang, lang, checkNotReleasedTexts));
                }
            }
        }
    }

    protected enum INDEX {TEXTKIND_ID, TEXT_ID, TEXTKIND_TYPE, ORIGIN, TEXT, TEXT_LANGUAGE, CACHE_TYPE}

    /**
     * Der normale Konstruktor
     *
     * @param textKindId
     * @param textId
     */
    public iPartsDictMetaCacheId(String textKindId, String textId, String textKindType, String origin, String text, String textLanguage,
                                 String cacheType) {
        super(TYPE, new String[]{ textKindId, textId, textKindType, origin, text, textLanguage, cacheType });
    }

    public iPartsDictMetaCacheId(iPartsDictMetaId metaId, DictTextKindTypes dictTextKindType, iPartsImportDataOrigin origin,
                                 String text, String textLanguage, boolean checkNotReleasedTexts) {
        this(metaId.getTextKindId(), metaId.getTextId(), dictTextKindType.getTextKindPseudoDbValue(), origin.getOrigin(),
             text, textLanguage, SQLStringConvert.booleanToPPString(checkNotReleasedTexts));
    }

    public iPartsDictMetaCacheId(iPartsDictMetaId metaId) {
        this(metaId.getTextKindId(), metaId.getTextId(), DictTextKindTypes.UNKNOWN.getTextKindPseudoDbValue(), iPartsImportDataOrigin.UNKNOWN.getOrigin(),
             "", "", SQLStringConvert.booleanToPPString(false));
    }

    public iPartsDictMetaCacheId(iPartsDataDictMeta dataDictMeta, DictTextKindTypes dictTextKindType, Language lang, boolean checkNotReleasedTexts) {
        this(dataDictMeta.getAsId(), dictTextKindType, iPartsImportDataOrigin.getTypeFromCode(dataDictMeta.getSource()),
             dataDictMeta.getMultiLang().getText(lang.getCode()), lang.getCode(), checkNotReleasedTexts);
    }

    public iPartsDictMetaCacheId(iPartsDataDictMeta dataDictMeta, DictTextKindTypes dictTextKindType, EtkMultiSprache multiLang,
                                 Language lang, boolean checkNotReleasedTexts) {
        this(dataDictMeta.getAsId(), dictTextKindType, iPartsImportDataOrigin.getTypeFromCode(dataDictMeta.getSource()),
             multiLang.getText(lang.getCode()), lang.getCode(), checkNotReleasedTexts);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDictMetaCacheId() {
        this("", "", "", "", "", "", "");
    }

    public String getTextKind() {
        return id[INDEX.TEXTKIND_ID.ordinal()];
    }

    public String getTextId() {
        return id[INDEX.TEXT_ID.ordinal()];
    }

    public String getTextKindName() {
        return id[INDEX.TEXTKIND_TYPE.ordinal()];
    }

    public DictTextKindTypes getTextKindId() {
        return DictTextKindTypes.getTypeByPseudoDbValue(getTextKindName());
    }

    public String getOrigin() {
        return id[INDEX.ORIGIN.ordinal()];
    }

    public iPartsImportDataOrigin getOriginType() {
        return iPartsImportDataOrigin.getTypeFromCode(getOrigin());
    }

    public String getTextLanguage() {
        return id[INDEX.TEXT_LANGUAGE.ordinal()];
    }

    public String getText() {
        return id[INDEX.TEXT.ordinal()];
    }

    public String getCacheType() {
        return id[INDEX.CACHE_TYPE.ordinal()];
    }

    public boolean isNotReleasedTexts() {
        return SQLStringConvert.ppStringToBoolean(getCacheType());
    }
}
