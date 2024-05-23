/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.DictTextKindTransitTypes;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Cache für die Beziehung zwischen {@link DictTextKindTypes} und der {@link iPartsDictTextKindId}
 */
public class DictTxtKindIdByMADId {

    public static final String TRANSLATION_TEXTKINDS_DELIMITER = ",";

    private static DictTxtKindIdByMADId instance = null;

    private static Map<String, iPartsDataDictTextKind> tagged_DictTextKindIds;
    private static Map<String, iPartsDataDictTextKind> tagged_DictRSKTextKindIds;
    private static Map<String, iPartsDataDictTextKind> tagged_DictPRIMUSTextKindIds;
    private static Map<String, iPartsDataDictTextKind> tagged_DictEPCTextKindIds;
    private static Map<DictTextKindTransitTypes, List<iPartsDictTextKindId>> tagged_DictiPartsTextKindIds;
    private static Map<String, List<DictTextKindTransitTypes>> iPartsTextKindsToTransitTextkinds;
    private static Map<iPartsDictTextKindId, iPartsDataDictTextKind> iPartsTextKindIdToTextKind;
    private static List<iPartsDataDictTextKind> untagged_DictTextKinds;

    public static synchronized DictTxtKindIdByMADId getInstance(EtkProject project) {
        if (instance == null) {
            instance = new DictTxtKindIdByMADId(project);
        }
        return instance;
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instance = null;
    }

    private DictTxtKindIdByMADId(EtkProject project) {
        load(project);
    }

    private void load(EtkProject project) {
        tagged_DictTextKindIds = new HashMap<>();
        tagged_DictRSKTextKindIds = new HashMap<>();
        tagged_DictPRIMUSTextKindIds = new HashMap<>();
        tagged_DictEPCTextKindIds = new HashMap<>();
        tagged_DictiPartsTextKindIds = new HashMap<>();
        untagged_DictTextKinds = new DwList<>();
        iPartsTextKindsToTransitTextkinds = new HashMap<>();
        iPartsTextKindIdToTextKind = new HashMap<>();
        iPartsDataDictTextKindList txtKindList = iPartsDataDictTextKindList.loadAllTextKindList(project, true);
        for (iPartsDataDictTextKind dataTxtKind : txtKindList) {
            iPartsTextKindIdToTextKind.put(dataTxtKind.getAsId(), dataTxtKind);
            String foreignTextKind = dataTxtKind.getFieldValue(iPartsConst.FIELD_DA_DICT_TK_FOREIGN_TKIND);
            if (!foreignTextKind.isEmpty()) {
                if (isMADDictTextKindType(foreignTextKind)) {
                    tagged_DictTextKindIds.put(foreignTextKind, dataTxtKind);
                    handleTransitTextkinds(dataTxtKind);
                } else if (isRSKDictTextKindType(foreignTextKind)) {
                    tagged_DictRSKTextKindIds.put(foreignTextKind, dataTxtKind);
                } else if (isPRIMUSDictTextKindType(foreignTextKind)) {
                    tagged_DictPRIMUSTextKindIds.put(foreignTextKind, dataTxtKind);
                } else if (isEPCDictTextKindType(foreignTextKind)) {
                    tagged_DictEPCTextKindIds.put(foreignTextKind, dataTxtKind);
                } else {
                    //als Rückfallposition
                    untagged_DictTextKinds.add(dataTxtKind);
                }
            } else {
                untagged_DictTextKinds.add(dataTxtKind);
            }
            //laden der Usages
            dataTxtKind.getUsages();
        }
    }

    /**
     * Erzeugt die Übersetzungstextarten anhand der Verknüpfung aus dem benutzerdefinierten Import
     *
     * @param dataTxtKind
     */
    private void handleTransitTextkinds(iPartsDataDictTextKind dataTxtKind) {
        String transitForeignTextKinds = dataTxtKind.getFieldValue(iPartsConst.FIELD_DA_DICT_TK_TRANSIT_TKIND);
        if (!transitForeignTextKinds.isEmpty()) {
            List<String> textKinds = StrUtils.toStringList(transitForeignTextKinds, TRANSLATION_TEXTKINDS_DELIMITER, false, true);
            if (!textKinds.isEmpty()) {
                for (String singleTransitTextkind : textKinds) {
                    DictTextKindTransitTypes transitType = DictTextKindTransitTypes.getType(singleTransitTextkind);
                    List<DictTextKindTransitTypes> transitTypesForTextKind = iPartsTextKindsToTransitTextkinds.computeIfAbsent(dataTxtKind.getAsId().getTextKindId(), k -> new ArrayList<>());
                    transitTypesForTextKind.add(transitType);
                    List<iPartsDictTextKindId> textkindsForTransitTextKind = tagged_DictiPartsTextKindIds.computeIfAbsent(transitType, k -> new ArrayList<>());
                    textkindsForTransitTextKind.add(dataTxtKind.getAsId());
                }
            }
        }
    }

    private boolean isEPCDictTextKindType(String foreignTextKind) {
        DictTextKindEPCTypes txtEPCKindType = DictTextKindEPCTypes.getType(foreignTextKind);
        return txtEPCKindType != DictTextKindEPCTypes.UNKNOWN;
    }

    private boolean isPRIMUSDictTextKindType(String foreignTextKind) {
        DictTextKindPRIMUSTypes txtPRIMUSKindType = DictTextKindPRIMUSTypes.getType(foreignTextKind);
        return txtPRIMUSKindType != DictTextKindPRIMUSTypes.UNKNOWN;
    }

    private boolean isRSKDictTextKindType(String foreignTextKind) {
        DictTextKindRSKTypes txtRSKKindType = DictTextKindRSKTypes.getType(foreignTextKind);
        return txtRSKKindType != DictTextKindRSKTypes.UNKNOWN;

    }

    private boolean isMADDictTextKindType(String foreignTextKind) {
        DictTextKindTypes txtKindType = DictTextKindTypes.getType(foreignTextKind);
        return txtKindType != DictTextKindTypes.UNKNOWN;
    }

    public iPartsDictTextKindId getTxtKindId(DictTextKindTypes textKindType) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictTextKindIds.get(textKindType.getMadId());
        if (dataTxtKind != null) {
            return dataTxtKind.getAsId();
        }
        return null;
    }

    public iPartsDictTextKindId getTxtKindId(DictTextKindTypes textKindType, String tableDotFieldName) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictTextKindIds.get(textKindType.getMadId());
        if (dataTxtKind != null) {
            for (iPartsDataDictTextKindUsage dataUsage : dataTxtKind.getUsages()) {
                if (dataUsage.getAsId().getTableDotFieldName().equals(tableDotFieldName)) {
                    return new iPartsDictTextKindId(dataUsage.getAsId().getTextKindId());
                }
            }
        }
        return null;
    }

    public boolean isTextKindIdPartOfMAD(iPartsDictTextKindId textKindId) {
        for (Map.Entry<String, iPartsDataDictTextKind> entry : tagged_DictTextKindIds.entrySet()) {
            if (entry.getValue().getAsId().equals(textKindId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDictMetaIdOfMADTypeOf(iPartsDictMetaId dictMetaId, DictTextKindTypes... textKindTypes) {
        for (Map.Entry<String, iPartsDataDictTextKind> entry : tagged_DictTextKindIds.entrySet()) {
            if (entry.getValue().getAsId().getTextKindId().equals(dictMetaId.getTextKindId())) {
                DictTextKindTypes searchTextKindType = DictTextKindTypes.getType(entry.getKey());
                for (DictTextKindTypes textKindType : textKindTypes) {
                    if (textKindType == searchTextKindType) {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    /**
     * Nur {@link iPartsDataDictTextKind} zurückliefern, wenn eine gültige Tertart für TRANSIT eingetragen ist
     *
     * @param textKindId
     * @param project
     * @return
     */
    public iPartsDataDictTextKind findDictTextKindByTextKindIdAndValidTransit(iPartsDictTextKindId textKindId, EtkProject project) {
        if (iPartsTextKindsToTransitTextkinds.containsKey(textKindId.getTextKindId())) {
            return findDictTextKindByTextKindId(textKindId, project);
        }
        return null;
    }

    /**
     * Liefert für die übergebene iParts Textart die dazugehörige Transit-Textarten
     *
     * @param textKindId
     * @return
     */
    public List<DictTextKindTransitTypes> getTransitTextKindsForiPartsTextKind(iPartsDictTextKindId textKindId) {
        return iPartsTextKindsToTransitTextkinds.get(textKindId.getTextKindId());
    }

    /**
     * Liefert für die übergebene iParts Textart die erste gefundene dazugehörige Transit-Textarten
     *
     * @param textKindId
     * @return
     */
    public DictTextKindTransitTypes getFirstTransitTextKindsForiPartsTextKind(iPartsDictTextKindId textKindId) {
        List<DictTextKindTransitTypes> transitTextKinds = getTransitTextKindsForiPartsTextKind(textKindId);
        if (transitTextKinds != null) {
            return transitTextKinds.get(0);
        }
        return null;
    }

    public iPartsDataDictTextKind findDictTextKindByTextKindId(iPartsDictTextKindId textKindId, EtkProject project) {
        if (iPartsTextKindIdToTextKind.containsKey(textKindId)) {
            return cloneDataDictTextKind(iPartsTextKindIdToTextKind.get(textKindId), project);
        }

        for (Map.Entry<String, iPartsDataDictTextKind> entry : tagged_DictTextKindIds.entrySet()) {
            if (entry.getValue().getAsId().equals(textKindId)) {
                return cloneDataDictTextKind(entry.getValue(), project);
            }
        }
        for (Map.Entry<String, iPartsDataDictTextKind> entry : tagged_DictRSKTextKindIds.entrySet()) {
            if (entry.getValue().getAsId().equals(textKindId)) {
                return cloneDataDictTextKind(entry.getValue(), project);
            }
        }
        for (Map.Entry<String, iPartsDataDictTextKind> entry : tagged_DictPRIMUSTextKindIds.entrySet()) {
            if (entry.getValue().getAsId().equals(textKindId)) {
                return cloneDataDictTextKind(entry.getValue(), project);
            }
        }
        for (iPartsDataDictTextKind dataDictTextKind : untagged_DictTextKinds) {
            if (dataDictTextKind.getAsId().equals(textKindId)) {
                return cloneDataDictTextKind(dataDictTextKind, project);
            }
        }
        return null;
    }

    private iPartsDataDictTextKind cloneDataDictTextKind(iPartsDataDictTextKind dataDictTextKind, EtkProject project) {
        if (dataDictTextKind != null) {
            return dataDictTextKind.cloneMe(project);
        } else {
            return null;
        }
    }

    public String findTableAndFieldNameByTextKindId(iPartsDictTextKindId textKindId, EtkProject project) {
        iPartsDataDictTextKindUsageList usages = getUsagesByTextKindId(textKindId, project);
        if (usages != null) {
            return usages.get(0).getFeld();
        }
        return null;
    }

    public iPartsDataDictTextKindUsageList getUsagesByTextKindId(iPartsDictTextKindId textKindId, EtkProject project) {
        iPartsDataDictTextKind dataDictTextKind = findDictTextKindByTextKindId(textKindId, project);
        if (dataDictTextKind != null) {
            if ((dataDictTextKind.getUsages() != null) && !dataDictTextKind.getUsages().isEmpty()) {
                return dataDictTextKind.getUsages();
            }
        }
        return null;
    }

    /**
     * Liefert alle Verwendungen für die übergebene Textart zurück.
     * Achtung! Hier werden keine Klone der {@link iPartsDataDictTextKind}-Objekte erzeugt sondern die Originale zurückgegeben!
     *
     * @param textKindType
     * @return
     */
    public iPartsDataDictTextKindUsageList getTxtKindUsages(DictTextKindTypes textKindType) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictTextKindIds.get(textKindType.getMadId());
        if (dataTxtKind != null) {
            return dataTxtKind.getUsages();
        }
        return null;
    }

    public boolean dictionaryExists(DictTextKindTypes txtKindType) {
        iPartsDictTextKindId textKindId = getTxtKindId(txtKindType);
        return ((textKindId != null) && (textKindId.isValidId()));
    }

    public iPartsDictTextKindId getEPCTxtKindId(DictTextKindEPCTypes textKindType) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictEPCTextKindIds.get(textKindType.getEpcId());
        if (dataTxtKind != null) {
            return dataTxtKind.getAsId();
        }
        return null;
    }

    public List<iPartsDictTextKindId> getiPartsTxtKinds(DictTextKindTransitTypes textKindType) {
        return tagged_DictiPartsTextKindIds.get(textKindType);
    }

    public boolean dictionaryExists(DictTextKindEPCTypes txtKindType) {
        iPartsDictTextKindId textKindId = getEPCTxtKindId(txtKindType);
        return ((textKindId != null) && (textKindId.isValidId()));
    }

    public boolean dictionaryExists(DictTextKindTransitTypes txtKindType) {
        List<iPartsDictTextKindId> textKindIds = getiPartsTxtKinds(txtKindType);
        if (textKindIds != null) {
            for (iPartsDictTextKindId textKindId : textKindIds) {
                if (textKindId.isValidId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getDictionaryName(DictTextKindTypes txtKindType, String language) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictTextKindIds.get(txtKindType.getMadId());
        if (dataTxtKind != null) {
            return dataTxtKind.getName(language);
        }
        return "";
    }

    public iPartsDictTextKindId getRSKTxtKindId(DictTextKindRSKTypes txtRSKKindType) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictRSKTextKindIds.get(txtRSKKindType.getRSKId());
        if (dataTxtKind != null) {
            return dataTxtKind.getAsId();
        }
        return null;
    }

    public iPartsDictTextKindId getRSKTxtKindId(DictTextKindRSKTypes txtRSKKindType, String tableDotFieldName) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictRSKTextKindIds.get(txtRSKKindType.getRSKId());
        if (dataTxtKind != null) {
            for (iPartsDataDictTextKindUsage dataUsage : dataTxtKind.getUsages()) {
                if (dataUsage.getAsId().getTableDotFieldName().equals(tableDotFieldName)) {
                    return new iPartsDictTextKindId(dataUsage.getAsId().getTextKindId());
                }
            }
        }
        return null;
    }

    public boolean dictionaryExists(DictTextKindRSKTypes txtRSKKindType) {
        iPartsDictTextKindId textKindId = getRSKTxtKindId(txtRSKKindType);
        return ((textKindId != null) && (textKindId.isValidId()));
    }

    public String getDictionaryName(DictTextKindRSKTypes txtRSKKindType, String language) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictRSKTextKindIds.get(txtRSKKindType.getRSKId());
        if (dataTxtKind != null) {
            return dataTxtKind.getName(language);
        }
        return "";
    }

    public iPartsDictTextKindId getPRIMUSTxtKindId(DictTextKindPRIMUSTypes txtPRIMUSKindType) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictPRIMUSTextKindIds.get(txtPRIMUSKindType.getPRIMUSId());
        if (dataTxtKind != null) {
            return dataTxtKind.getAsId();
        }
        return null;
    }

    public iPartsDictTextKindId getPRIMUSTxtKindId(DictTextKindPRIMUSTypes txtPRIMUSKindType, String tableDotFieldName) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictPRIMUSTextKindIds.get(txtPRIMUSKindType.getPRIMUSId());
        if (dataTxtKind != null) {
            for (iPartsDataDictTextKindUsage dataUsage : dataTxtKind.getUsages()) {
                if (dataUsage.getAsId().getTableDotFieldName().equals(tableDotFieldName)) {
                    return new iPartsDictTextKindId(dataUsage.getAsId().getTextKindId());
                }
            }
        }
        return null;
    }

    public boolean dictionaryExists(DictTextKindPRIMUSTypes txtPRIMUSKindType) {
        iPartsDictTextKindId textKindId = getPRIMUSTxtKindId(txtPRIMUSKindType);
        return ((textKindId != null) && (textKindId.isValidId()));
    }

    public String getDictionaryName(DictTextKindPRIMUSTypes txtPRIMUSKindType, String language) {
        iPartsDataDictTextKind dataTxtKind = tagged_DictPRIMUSTextKindIds.get(txtPRIMUSKindType.getPRIMUSId());
        if (dataTxtKind != null) {
            return dataTxtKind.getName(language);
        }
        return "";
    }

    public String checkDictionariesExists(String language, Set<DictTextKindTypes> txtKindTypes, Set<DictTextKindRSKTypes> txtRSKKindTypes,
                                          Set<DictTextKindPRIMUSTypes> txtPRIMUSKindTypes, Set<DictTextKindEPCTypes> txtEPCKindTypes,
                                          Set<DictTextKindTransitTypes> txtTransitKindTypes) {
        StringBuilder errorMsg = new StringBuilder();
        if (txtKindTypes != null) {
            for (DictTextKindTypes dictKindType : txtKindTypes) {
                if (!dictionaryExists(dictKindType)) {
                    if (errorMsg.length() > 0) {
                        errorMsg.append(", ");
                    }
                    errorMsg.append(dictKindType.getMadId());
                    errorMsg.append(" (");
                    errorMsg.append(TranslationHandler.translateForLanguage(dictKindType.getTextKindName(), language));
                    errorMsg.append(")");
                }
            }
        }
        if (txtRSKKindTypes != null) {
            for (DictTextKindRSKTypes dictRSKKindType : txtRSKKindTypes) {
                if (!dictionaryExists(dictRSKKindType)) {
                    if (errorMsg.length() > 0) {
                        errorMsg.append(", ");
                    }
                    errorMsg.append(dictRSKKindType.getRSKId());
                    errorMsg.append(" (");
                    errorMsg.append(TranslationHandler.translateForLanguage(dictRSKKindType.getTextKindName(), language));
                    errorMsg.append(")");
                }
            }
        }
        if (txtPRIMUSKindTypes != null) {
            for (DictTextKindPRIMUSTypes dictPRIMUSKindType : txtPRIMUSKindTypes) {
                if (!dictionaryExists(dictPRIMUSKindType)) {
                    if (errorMsg.length() > 0) {
                        errorMsg.append(", ");
                    }
                    errorMsg.append(dictPRIMUSKindType.getPRIMUSId());
                    errorMsg.append(" (");
                    errorMsg.append(TranslationHandler.translateForLanguage(dictPRIMUSKindType.getTextKindName(), language));
                    errorMsg.append(")");
                }
            }
        }

        if (txtEPCKindTypes != null) {
            for (DictTextKindEPCTypes dictEPCKindType : txtEPCKindTypes) {
                if (!dictionaryExists(dictEPCKindType)) {
                    if (errorMsg.length() > 0) {
                        errorMsg.append(", ");
                    }
                    errorMsg.append(dictEPCKindType.getEpcId());
                    errorMsg.append(" (");
                    errorMsg.append(TranslationHandler.translateForLanguage(dictEPCKindType.getTextKindName(), language));
                    errorMsg.append(")");
                }
            }
        }

        if (txtTransitKindTypes != null) {
            for (DictTextKindTransitTypes dictTransitKindType : txtTransitKindTypes) {
                if (!dictionaryExists(dictTransitKindType)) {
                    if (errorMsg.length() > 0) {
                        errorMsg.append(", ");
                    }
                    errorMsg.append(dictTransitKindType.getDbValue());
                    errorMsg.append(" (");
                    errorMsg.append(TranslationHandler.translateForLanguage(dictTransitKindType.getTextKindName(), language));
                    errorMsg.append(")");
                }
            }
        }
        return errorMsg.toString();
    }

    public boolean checkDictionariesExistsWithErrorLogMessage(EtkMessageLog messageLog, String language,
                                                              Set<DictTextKindTypes> txtKindTypes,
                                                              Set<DictTextKindRSKTypes> txtRSKKindTypes,
                                                              Set<DictTextKindPRIMUSTypes> txtPRIMUSKindTypes,
                                                              Set<DictTextKindEPCTypes> txtEPCKindTypes,
                                                              Set<DictTextKindTransitTypes> txtTransitKindTypes) {
        String errorMessage = checkDictionariesExists(language, txtKindTypes, txtRSKKindTypes, txtPRIMUSKindTypes, txtEPCKindTypes, txtTransitKindTypes);
        if (!errorMessage.isEmpty()) {
            messageLog.fireMessage(TranslationHandler.translateForLanguage("!!Fehlende Fremd-Textart im Lexikon: %1", language, errorMessage),
                                   MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkDictionariesExistsWithErrorLogMessageForMAD(EtkMessageLog messageLog, String logLanguage, DictTextKindTypes... txtKindTypes) {
        if (txtKindTypes == null) {
            return false;
        }
        Set<DictTextKindTypes> txtKindTypesSet = new HashSet<>(txtKindTypes.length);
        Collections.addAll(txtKindTypesSet, txtKindTypes);
        return checkDictionariesExistsWithErrorLogMessageForMAD(messageLog, logLanguage, txtKindTypesSet);
    }

    public boolean checkDictionariesExistsWithErrorLogMessageForMAD(EtkMessageLog messageLog, String logLanguage, Set<DictTextKindTypes> txtKindTypes) {
        return checkDictionariesExistsWithErrorLogMessage(messageLog, logLanguage, txtKindTypes, null, null, null, null);
    }

    public boolean checkDictionariesExistsWithErrorLogMessageForRSK(EtkMessageLog messageLog, String logLanguage, DictTextKindRSKTypes... txtRSKKindTypes) {
        if (txtRSKKindTypes == null) {
            return false;
        }
        Set<DictTextKindRSKTypes> txtKindTypesSet = new HashSet<>(txtRSKKindTypes.length);
        Collections.addAll(txtKindTypesSet, txtRSKKindTypes);
        return checkDictionariesExistsWithErrorLogMessage(messageLog, logLanguage, null, txtKindTypesSet, null, null, null);
    }

    public boolean checkDictionariesExistsWithErrorLogMessageForPRIMUS(EtkMessageLog messageLog, String logLanguage, DictTextKindPRIMUSTypes... txtPRIMUSKindTypes) {
        if (txtPRIMUSKindTypes == null) {
            return false;
        }
        Set<DictTextKindPRIMUSTypes> txtKindTypesSet = new HashSet<>(txtPRIMUSKindTypes.length);
        Collections.addAll(txtKindTypesSet, txtPRIMUSKindTypes);
        return checkDictionariesExistsWithErrorLogMessage(messageLog, logLanguage, null, null, txtKindTypesSet, null, null);
    }

    public boolean checkDictionariesExistsWithErrorLogMessageForEPC(EtkMessageLog messageLog, String logLanguage, DictTextKindEPCTypes... txtEPCKindTypes) {
        if (txtEPCKindTypes == null) {
            return false;
        }
        Set<DictTextKindEPCTypes> txtKindTypesSet = new HashSet<>(txtEPCKindTypes.length);
        Collections.addAll(txtKindTypesSet, txtEPCKindTypes);
        return checkDictionariesExistsWithErrorLogMessage(messageLog, logLanguage, null, null, null, txtKindTypesSet, null);
    }

    public boolean checkDictionariesExistsWithErrorLogMessageForiParts(EtkMessageLog messageLog, String logLanguage, DictTextKindTransitTypes... txtTransitKindTypes) {
        if (txtTransitKindTypes == null) {
            return false;
        }
        Set<DictTextKindTransitTypes> txtKindTypesSet = new HashSet<>(txtTransitKindTypes.length);
        Collections.addAll(txtKindTypesSet, txtTransitKindTypes);
        return checkDictionariesExistsWithErrorLogMessage(messageLog, logLanguage, null, null, null, null, txtKindTypesSet);
    }
}
