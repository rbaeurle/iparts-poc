/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.DictTextKindTransitTypes;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.sql.TableAndFieldName;

/**
 * Hilfsroutinen für das Dictionary
 */
public class DictHelper implements iPartsDictConst {

    public static String buildHashtagForeignId(String id) {
        return HASHTAG_FOREIGN_ID_PREFIX + id;
    }

    //MAD Dictionary
    public static String buildDictTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    public static String buildTextId(String prefix, String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(prefix, textIdForDict);
        }
        return textIdForDict;
    }

    public static boolean isLongTextId(String textId) {
        return textId.endsWith(iPartsDictPrefixAndSuffix.DICT_LONGTEXT_SUFFIX.getPrefixValue());
    }

    public static boolean isDictTextId(String textId) {
        return getDictPrefix(textId) == iPartsDictPrefixAndSuffix.DICT_PREFIX;
    }

    public static String getIdFromDictTextId(String textId) {
        if (isDictTextId(textId)) {
            return getDictId(textId);
        }
        return textId;
    }

    public static boolean isDictTextIdEmpty(String textId) {
        return getIdFromDictTextId(textId).trim().isEmpty();
    }

    public static boolean hasNoDictPrefix(String textId) {
        iPartsDictPrefixAndSuffix prefix = getDictPrefix(textId);
        if (prefix == iPartsDictPrefixAndSuffix.DICT_EMPTY) {
            return true;
        } else if (!iPartsDictPrefixAndSuffix.isValidPrefix(prefix)) {
            return true;
        }
        return false;
    }

    public static boolean isGuidTextId(String textId) {
        if (!hasNoDictPrefix(textId)) {
            String dictId = getDictId(textId);
            if (dictId.length() < 32) {
                return false;
            } else {
                String value = dictId;
                if (dictId.contains(".")) {
                    String[] splittedDictId = dictId.split(".");
                    if (splittedDictId.length >= 1) {
                        value = splittedDictId[0];
                    }
                } else if (dictId.contains("_")) {
                    String[] splittedDictId = dictId.split("_");
                    if (splittedDictId.length >= 1) {
                        value = splittedDictId[0];
                    }
                }
                if (value.length() != 32) {
                    return false;
                }
                for (int i = 0; i < value.length(); i++) {
                    char ch = value.charAt(i);
                    if ((ch < '0') && (ch > 9) || ((ch < 'A') && (ch > 'F'))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    //PRIMUS Dictionary
    public static String buildDictPRIMUSTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_PRIMUS_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    // Produktbemerkungen über TAL47S Dictionary
    public static String buildDictProductRemarksTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_AUTO_PRODUCT_SELECT_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    /**
     * Baut anhanhd des {@link DictTextKindEPCTypes} die dazugehörige Id zusammen.
     *
     * @param importType
     * @param termId
     * @return
     */
    public static String buildEPCTextId(DictTextKindEPCTypes importType, String termId) {
        switch (importType) {
            case MODEL_DICTIONARY:
                return DictHelper.buildEPCModelDescTextId(termId);
            case SA_DICTIONARY:
                return DictHelper.buildEPCSaKgDescTextId(termId);
            case PART_DESCRIPTION:
                return DictHelper.buildEPCPartDescTextId(termId);
            case ADD_TEXT:
                return DictHelper.buildEPCAddTextTextId(termId);
            case SA_FOOTNOTE:
                return DictHelper.buildEPCSAFootnoteTextId(termId);
            case MODEL_FOOTNOTE:
                return DictHelper.buildEPCModelFootnoteTextId(termId);
        }
        return null;
    }

    public static boolean isDictEPCTextId(String textId) {
        return DictTextKindEPCTypes.getType(getDictPrefix(textId).getPrefixValue()) != DictTextKindEPCTypes.UNKNOWN;
    }

    // SA-Fußnoten aus EPC
    private static String buildEPCSAFootnoteTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_EPC_SA_FN_DICT_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    // Baumuster-Fußnoten aus EPC
    private static String buildEPCModelFootnoteTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_EPC_MODEL_FN_DICT_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    // Baumusterbenennung aus EPC
    public static String buildEPCModelDescTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_EPC_MODEL_DICT_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    // SA-KG-Benennung aus EPC
    public static String buildEPCSaKgDescTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_EPC_SA_DICT_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    // Ergänzungstexte aus EPC
    public static String buildEPCAddTextTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_EPC_ADD_TEXT_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    //  Teilebenennung ET aus EPC
    public static String buildEPCPartDescTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_EPC_PART_DESC_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    // Unklare Texte (keine IDs und der deutsche Text wird nicht gefunden)
    public static String buildDictIndistinctTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_INDISTINCT_TEXT_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    public static boolean isPRIMUSDictTextId(String textId) {
        return getDictPrefix(textId) == iPartsDictPrefixAndSuffix.DICT_PRIMUS_PREFIX;
    }

    public static String getIdFromPRIMUSDictTextId(String textId) {
        if (isPRIMUSDictTextId(textId)) {
            return getDictId(textId);
        }
        return textId;
    }

    public static boolean isPRIMUSDictTextIdEmpty(String textId) {
        return getIdFromPRIMUSDictTextId(textId).trim().isEmpty();
    }

    //RSK Dictionary für Aftersales
    public static String buildDictRSKTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_RSK_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    public static boolean isDictRSKTextId(String textId) {
        return getDictPrefix(textId) == iPartsDictPrefixAndSuffix.DICT_RSK_PREFIX;
    }

    public static String getIdFromDictRSKTextId(String textId) {
        if (isDictRSKTextId(textId)) {
            return getDictId(textId);
        }
        return textId;
    }

    public static boolean isDictRSKTextIdEmpty(String textId) {
        return getIdFromDictRSKTextId(textId).trim().isEmpty();
    }

    //RSK Dictionary für Konstruktion
    public static String buildDictRSKCDTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_RSKCD_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    public static boolean isDictRSKCDTextId(String textId) {
        return getDictPrefix(textId) == iPartsDictPrefixAndSuffix.DICT_RSKCD_PREFIX;
    }

    public static String getIdFromDictRSKCDTextId(String textId) {
        if (isDictRSKCDTextId(textId)) {
            return getDictId(textId);
        }
        return textId;
    }

    public static boolean isDictRSKCDTextIdEmpty(String textId) {
        return getIdFromDictRSKCDTextId(textId).trim().isEmpty();
    }

    //RSK Sprachneutrale Texte Dictionary
    public static String buildDictRSKFixedTextId(String textIdForDict) {
        if (hasNoDictPrefix(textIdForDict)) {
            return makeDictId(iPartsDictPrefixAndSuffix.DICT_RSKFIXED_PREFIX, textIdForDict);
        }
        return textIdForDict;
    }

    public static boolean isDictRSKFixedTextId(String textId) {
        return getDictPrefix(textId) == iPartsDictPrefixAndSuffix.DICT_RSKFIXED_PREFIX;
    }

    public static String getIdFromDictRSKFixedTextId(String textId) {
        if (isDictRSKFixedTextId(textId)) {
            return getDictId(textId);
        }
        return textId;
    }

    public static boolean isDictRSKFixedTextIdEmpty(String textId) {
        return getIdFromDictRSKFixedTextId(textId).trim().isEmpty();
    }

    //RSK allgemein
    public static String getRSKTextPrefix(DictTextKindRSKTypes importType) {
        switch (importType) {
            case MAT_AFTER_SALES:
                return iPartsDictPrefixAndSuffix.DICT_RSK_PREFIX.getPrefixValue();
            case MAT_CONSTRUCTION:
                return iPartsDictPrefixAndSuffix.DICT_RSKCD_PREFIX.getPrefixValue();
            case MAT_NEUTRAL:
                return iPartsDictPrefixAndSuffix.DICT_RSKFIXED_PREFIX.getPrefixValue();
        }
        return null;
    }

    public static String getRSKTextId(DictTextKindRSKTypes importType, String textIdForDict) {
        switch (importType) {
            case MAT_AFTER_SALES:
                return DictHelper.buildDictRSKTextId(textIdForDict);
            case MAT_CONSTRUCTION:
                return DictHelper.buildDictRSKCDTextId(textIdForDict);
            case MAT_NEUTRAL:
                return DictHelper.buildDictRSKFixedTextId(textIdForDict);
        }
        return null;
    }

    public static String getTermIdFromRSKTextId(DictTextKindRSKTypes importType, String textIdForDict) {
        switch (importType) {
            case MAT_AFTER_SALES:
                return DictHelper.getIdFromDictRSKTextId(textIdForDict);
            case MAT_CONSTRUCTION:
                return DictHelper.getIdFromDictRSKCDTextId(textIdForDict);
            case MAT_NEUTRAL:
                return DictHelper.getIdFromDictRSKFixedTextId(textIdForDict);
        }
        return null;
    }

    // Konsolidierte Texte (iParts)
    public static boolean isIPARTSDictTextId(String textId) {
        return getDictPrefix(textId) == iPartsDictPrefixAndSuffix.DICT_IPARTS_PREFIX;
    }

    public static String buildIPARTSDictTextId(String textIdForDict) {
        return makeDictId(iPartsDictPrefixAndSuffix.DICT_IPARTS_PREFIX, textIdForDict);
    }

    public static String buildIPARTSDictTextId() {
        return buildIPARTSDictTextId(FrameworkUtils.createUniqueId(true));
    }

    public static String getIdFromIPARTSDictTextId(String textId) {
        if (isIPARTSDictTextId(textId)) {
            return getDictId(textId);
        }
        return textId;
    }

    public static boolean isIPARTSDictTextIdEmpty(String textId) {
        return getIdFromIPARTSDictTextId(textId).trim().isEmpty();
    }

    public static String makeIPARTSDictId(DictTextKindTransitTypes transitType, String textIdForDict) {
        return TableAndFieldName.make(iPartsDictPrefixAndSuffix.DICT_IPARTS_PREFIX.getPrefixValue() + "_"
                                      + transitType.getPrefixForTextId(), textIdForDict);
    }

    //Hilfsroutinen
    public static String makeDictId(String prefix, String textIdForDict) {
        return TableAndFieldName.make(prefix, textIdForDict);
    }

    public static String makeDictId(iPartsDictPrefixAndSuffix prefix, String textIdForDict) {
        return makeDictId(prefix.getPrefixValue(), textIdForDict);
    }

    public static iPartsDictPrefixAndSuffix getDictPrefix(String textId) {
        return iPartsDictPrefixAndSuffix.getType(TableAndFieldName.getTableName(textId));
    }

    public static String getDictId(String textId) {
        return TableAndFieldName.getFieldName(textId);
    }

    public static String makeFNUnformattedDictId(String textIdForDict) {
        return buildDictTextId(textIdForDict) + FOOTNOTE_UNFORMATTED_TEXT_SIGN;
    }

    public static String makeFNUnformattedDictIdFromDictId(String dictId) {
        if (isDictTextId(dictId)) {
            return dictId + FOOTNOTE_UNFORMATTED_TEXT_SIGN;
        } else {
            return makeFNUnformattedDictId(dictId);
        }
    }

    public static boolean isFNUnformattedDictId(String dictId) {
        return dictId.endsWith(FOOTNOTE_UNFORMATTED_TEXT_SIGN);
    }

    //Weitere Routinen
    public static String getMADUserId() {
        return "mad01";
    }

    public static String getRSKUserId() {
        return "rsk01";
    }

    public static String getEPCUserId() {
        return "epc01";
    }

    public static String getPRIMUSUserId() {
        return "prim01";
    }

    public static String getPROVALUserId() {
        return "proval";
    }

    public static String getSAPMBSUserId() {
        return "sap_mbs";
    }

    public static String getMADDictStatus() {
        return iPartsDictConst.DICT_STATUS_RELEASED;
    }

    public static String getMADDictEndStatus() {
        return iPartsDictConst.DICT_STATUS_LOCKED;
    }

    public static String getMADForeignSource() {
        return iPartsImportDataOrigin.MAD.getOrigin();
    }

    public static String getRSKForeignSource() {
        return "RSK";
    }

    public static String getPRIMUSForeignSource() {
        return "PRIM";
    }

    public static String getAutoProductRemarksSource() {
        return "PROD_REM";
    }

    public static String getKgTuTemplateImportForeignSource() {
        return "KgTuTemplate";
    }

    public static String getHashtagForeignSource() {
        return "Hashtag";
    }

    public static String getEPCForeignSource() {
        return iPartsImportDataOrigin.EPC.getOrigin();
    }

    public static String getPROVALForeignSource() {
        return iPartsImportDataOrigin.PROVAL.getOrigin();
    }

    public static String getIPartsSource() {
        return iPartsImportDataOrigin.IPARTS.getOrigin();
    }

    /**
     * Liefert die passende iParts-Quelle abhängig von den Eigenschaften des eingeloggten Benutzers der aktuellen Session
     * zurück.
     *
     * @return
     */
    public static String getIPartsSourceForCurrentSession() {
        return iPartsImportDataOrigin.getIPartsSourceForCurrentSession();
    }

    public static String getSAPMBSForeignSource() {
        return iPartsImportDataOrigin.SAP_MBS.getOrigin();
    }

    public static String getPSKForeignSource() {
        return iPartsImportDataOrigin.PSK.getOrigin();
    }

    public static String getPSKUserId() {
        return "psk";
    }

    public static String getConnectForeignSource() {
        return iPartsImportDataOrigin.CONNECT.getOrigin();
    }

    public static String getConnectUserId() {
        return "connect";
    }

}
