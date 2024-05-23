/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSAModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsFootnoteType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * Repräsentation einer SA (Tabellen DA_SA_MODULES und DA_SA).
 */
public class iPartsSA implements iPartsConst {

    // Diese Caches benötigen relativ wenig Speicher -> Lebensdauer MAX_CACHE_LIFE_TIME_CORE verwenden
    private static ObjectInstanceLRUList<Object, iPartsSA> instances = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_SA,
                                                                                                   iPartsConst.MAX_CACHE_LIFE_TIME_CORE);
    private static ObjectInstanceLRUList<String, String> saSaaDescriptionCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_SA,
                                                                                                             iPartsConst.MAX_CACHE_LIFE_TIME_CORE);
    private static final String SA_SAA_LANGUAGE_DELIMITER = "|";

    protected iPartsSAId saId;
    protected AssemblyId moduleId;
    protected String titleTextNo; // Textnummer für das späteres Nachladen
    protected volatile EtkMultiSprache title;
    protected volatile String codes;
    protected volatile boolean notDocuRelevant;
    protected volatile boolean saaFootNotesLoaded;
    protected volatile Map<String, List<iPartsFootNote>> saaFootNotesMap;
    protected volatile Map<String, Set<String>> saaReferencedSAsMap;
    protected volatile Map<iPartsProductId, Set<String>> productIdsToKGsMap;
    protected volatile Set<iPartsProductId> pskProductIds;
    protected volatile boolean carAndVanSA;
    protected volatile boolean truckAndBusSA;

    public static synchronized void clearCache() {
        instances.clear();
        clearSaSaaDescriptionsCache();
    }

    public static synchronized void clearSaSaaDescriptionsCache() {
        saSaaDescriptionCache.clear();
    }

    public static synchronized void removeSAFromCache(EtkProject project, iPartsSaId saId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsSA.class, saId.getSaNumber());
        instances.removeKey(hashObject);
        removeSaSaaDescriptionFromCache(saId.getSaNumber());
    }

    public static synchronized iPartsSA addSAToCache(EtkProject project, iPartsSAId saId, AssemblyId moduleId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsSA.class, saId.getSaNumber());
        iPartsSA result = instances.get(hashObject);
        if (result == null) { // nur hinzufügen, falls SA nicht bereits im Cache
            result = new iPartsSA(saId, moduleId);
            instances.put(hashObject, result);
        }
        return result;
    }

    public static synchronized void removeSAADescriptionFromCache(iPartsSaaId saaId) {
        removeSaSaaDescriptionFromCache(saaId.getSaaNumber());
    }

    private static synchronized void removeSaSaaDescriptionFromCache(String saSaaNumber) {
        saSaaNumber = saSaaNumber + SA_SAA_LANGUAGE_DELIMITER;
        for (String saSaa : saSaaDescriptionCache.getKeys()) {
            if (saSaa.startsWith(saSaaNumber)) {
                saSaaDescriptionCache.removeKey(saSaa);
            }
        }
    }

    public static synchronized iPartsSA getInstance(EtkProject project, iPartsSAId saId) {
        return getInstance(project, saId.getSaNumber());
    }

    public static synchronized iPartsSA getInstance(EtkProject project, iPartsSAModulesId saModulesId) {
        return getInstance(project, saModulesId.getSaNumber());
    }

    private static synchronized iPartsSA getInstance(EtkProject project, String saNumber) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsSA.class, saNumber);
        iPartsSA result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsSA(project, saNumber);
            instances.put(hashObject, result);
        }

        return result;
    }

    /**
     * Liefert die Benennung der übergebenen SA/SAA für die gewünschte Sprache zurück.
     *
     * @param project
     * @param saSaaNumber
     * @param language
     * @return
     */
    public static synchronized String getSaSaaDescription(EtkProject project, String saSaaNumber, String language) {
        return getSaSaaDescription(project, saSaaNumber, language, true);
    }

    /**
     * Liefert die Benennung der übergebenen SA/SAA für die gewünschte Sprache zurück.
     *
     * @param project
     * @param saSaaNumber
     * @param language
     * @param searchForFallbackLanguagesInCache Flag, ob bei leerer SA/SAA-Benennung für die gewünschte Sprache im Cache
     *                                          auch mit den Rückfallsprachen gesucht werden soll
     * @return
     */
    private static synchronized String getSaSaaDescription(EtkProject project, String saSaaNumber, String language, boolean searchForFallbackLanguagesInCache) {
        // Cache für EtkMultiSprache wäre zu teuer, da es Hunderttausende von SA/SAAs gibt -> nur Cacheeintrag pro Sprache

        String cacheKey = saSaaNumber + SA_SAA_LANGUAGE_DELIMITER + language;
        String saSaaDescription = saSaaDescriptionCache.get(cacheKey);
        // saSaaDescription == null wenn die Sprache auch als Rückfallsprache nicht abgefragt wurde. Falls die Sprache nicht in der Datenbank ist, aber schon
        // abgefragt wurde ist "" im Cache
        if (saSaaDescription == null) {
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            EtkMultiSprache saSaaDescriptionMultiLang = null;
            String tempSaSaaNumber = numberHelper.unformatSaSaaForDB(saSaaNumber, true);
            // Beschreibung für SA/SAA aus DB nachladen
            if (numberHelper.isValidSaa(saSaaNumber)) {
                iPartsDataSaa dataSaa = new iPartsDataSaa(project, new iPartsSaaId(tempSaSaaNumber));
                saSaaDescriptionMultiLang = dataSaa.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
            } else if (numberHelper.isValidSa(saSaaNumber, true)) {
                iPartsDataSa dataSa = new iPartsDataSa(project, new iPartsSaId(tempSaSaaNumber));
                saSaaDescriptionMultiLang = dataSa.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
            }

            // Ist die SAA in der DB nicht vorhanden, wird ein leerer String zurückgegeben, z.B. SAAs von einer geladenen
            // Datenkarte, die in der iParts DB nicht existieren.
            if (saSaaDescriptionMultiLang == null) {
                saSaaDescriptionCache.put(cacheKey, "");
                return "";
            }
            saSaaDescription = saSaaDescriptionMultiLang.getText(language);
            saSaaDescriptionCache.put(cacheKey, saSaaDescription);

            // Rückfallsprachen durchsuchen falls notwendig (unabhängig von searchForFallbackLanguagesInCache, weil wir
            // das EtkMultiSprache ja sowieso schon haben und es dadurch hier am günstigsten ist, alle Rückfallsprachen durchzugehen)
            if (saSaaDescription.isEmpty()) {
                for (String fallbackLanguage : project.getDataBaseFallbackLanguages()) {
                    saSaaDescription = saSaaDescriptionMultiLang.getText(fallbackLanguage);

                    // Text für die Rückfallsprache auch gleich in den Cache legen
                    saSaaDescriptionCache.put(saSaaNumber + SA_SAA_LANGUAGE_DELIMITER + fallbackLanguage, saSaaDescription);

                    if (!saSaaDescription.isEmpty()) {
                        return saSaaDescription;
                    }
                }
            }
        }

        if (saSaaDescription.isEmpty() && searchForFallbackLanguagesInCache) {
            for (String fallbackLanguage : project.getDataBaseFallbackLanguages()) {
                saSaaDescription = getSaSaaDescription(project, saSaaNumber, fallbackLanguage, false);
                if (!saSaaDescription.isEmpty()) {
                    return saSaaDescription;
                }
            }
        }

        return saSaaDescription;
    }

    protected iPartsSA(iPartsSAId saId, AssemblyId moduleId) {
        this.saId = saId;
        this.moduleId = moduleId;
    }

    protected iPartsSA(EtkProject project, String saNumber) {
        this.saId = new iPartsSAId(saNumber);
        loadHeader(project);
    }

    public iPartsSAId getSaId() {
        return saId;
    }

    public AssemblyId getModuleId() {
        return moduleId;
    }

    public EtkMultiSprache getTitle(EtkProject project) {
        if (title == null) {
            loadSaDataIfNeeded(project);
            EtkMultiSprache newTitle = project.getEtkDbs().getMultiLanguageByTextNr(TableAndFieldName.make(TABLE_DA_SA, FIELD_DS_DESC),
                                                                                    titleTextNo);
            synchronized (this) {
                if (title == null) {
                    title = newTitle;
                }
            }
        }
        return title;
    }

    public String getCodes(EtkProject project) {
        loadSaDataIfNeeded(project);
        return codes;
    }

    public boolean isDocuRelevant(EtkProject project) {
        loadSaDataIfNeeded(project);
        return !notDocuRelevant;
    }

    /**
     * Liefert eine Map von allen SAAs dieser SA auf Listen von {@link iPartsFootNote}s zurück. Hat eine SAA keine Fußnoten,
     * ist sie in dieser Map auch nicht enthalten.
     *
     * @param project
     * @return
     */
    public Map<String, List<iPartsFootNote>> getSaaFootNotesMap(EtkProject project) {
        loadSaaFootNotesIfNeeded(project);
        if (saaFootNotesMap != null) {
            return Collections.unmodifiableMap(saaFootNotesMap);
        } else {
            return null;
        }
    }

    /**
     * Liefert eine Map von allen SAAs dieser SA auf Sets von Verbindungs-SAs zurück. Hat eine SAA keine Verbindungs-SAs,
     * ist sie in dieser Map auch nicht enthalten.
     *
     * @param project
     * @return
     */
    public Map<String, Set<String>> getSaaReferencedSAsMap(EtkProject project) {
        loadSaaReferencedSAsIfNeeded(project);
        return Collections.unmodifiableMap(saaReferencedSAsMap);
    }

    protected boolean loadHeader(EtkProject project) {
        DBDataObjectAttributes attributes = project.getDbLayer().getAttributes(TABLE_DA_SA_MODULES, new String[]{ FIELD_DSM_SA_NO },
                                                                               new String[]{ saId.getSaNumber() });
        if (attributes != null) {
            moduleId = new AssemblyId(attributes.getField(FIELD_DSM_MODULE_NO).getAsString(), "");
        } else { // SA mit Dummy-Werten befüllen, um NPEs zu vermeiden
            moduleId = new AssemblyId();
            return false;
        }

        return true;
    }

    protected void loadSaDataIfNeeded(EtkProject project) {
        if ((titleTextNo == null) || (codes == null)) {
            iPartsSaId dbSaId = new iPartsSaId(saId.getSaNumber());
            iPartsDataSa dataSa = new iPartsDataSa(project, dbSaId);
            boolean isNotDocuRel = dataSa.getFieldValueAsBoolean(FIELD_DS_NOT_DOCU_RELEVANT);
            String newTitleTextNo = dataSa.getFieldValue(FIELD_DS_DESC);

            String newCodes = dataSa.getFieldValue(FIELD_DS_CODES);

            synchronized (this) {
                if (titleTextNo == null) {
                    titleTextNo = newTitleTextNo;
                }
                if (codes == null) {
                    codes = newCodes;
                }
                notDocuRelevant = isNotDocuRel;
            }
        }
    }

    protected void loadSaaFootNotesIfNeeded(EtkProject project) {
        if (!saaFootNotesLoaded) {
            // Join von den Fußnotentexten (DA_FN_CONTENT) auf die Fußnoten-SAA-Tabelle (DA_FN_SAA_REF) sowie
            // die Fußnotenstammdaten (DA_FN)
            EtkDisplayFields selectFields = new EtkDisplayFields();
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_SAA_REF, FIELD_DFNS_SAA, false, false));

            selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_ID, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_NAME, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_STANDARD, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_TYPE, false, false));

            selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT, true, false));

            String dbLanguage = project.getDBLanguage();
            iPartsDataFootNoteContentList footNoteContentList = new iPartsDataFootNoteContentList();

            // Like-Abfrage, da auf den SA-Rumpf im  SAA Feld FIELD_DS_SAA abgefragt wird
            footNoteContentList.searchSortAndFillWithJoin(project, dbLanguage, selectFields,
                                                          new String[]{ TableAndFieldName.make(TABLE_DA_FN_SAA_REF, FIELD_DFNS_SAA) },
                                                          new String[]{ saId.getSaNumber() + "*" }, false,
                                                          new String[]{ TableAndFieldName.make(TABLE_DA_FN_SAA_REF, FIELD_DFNS_SAA),
                                                                        TableAndFieldName.make(TABLE_DA_FN_SAA_REF, FIELD_DFNS_FN_SEQNO),
                                                                        TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO) },
                                                          false, true, null,
                                                          new EtkDataObjectList.JoinData(TABLE_DA_FN_SAA_REF,
                                                                                         new String[]{ FIELD_DFNC_FNID },
                                                                                         new String[]{ FIELD_DFNS_FNID },
                                                                                         false, false),
                                                          new EtkDataObjectList.JoinData(TABLE_DA_FN,
                                                                                         new String[]{ FIELD_DFNC_FNID },

                                                                                         new String[]{ FIELD_DFN_ID },
                                                                                         false, false));

            Map<String, List<iPartsFootNote>> newSaaFootNotesMap = null;
            if (!footNoteContentList.isEmpty()) {
                newSaaFootNotesMap = new HashMap<>();
                for (iPartsDataFootNoteContent dataFootNoteContent : footNoteContentList) {
                    String saaNumber = dataFootNoteContent.getFieldValue(FIELD_DFNS_SAA);
                    List<iPartsFootNote> footNotes = newSaaFootNotesMap.get(saaNumber);
                    if (footNotes == null) {
                        footNotes = new DwList<>(); // Reihenfolge muss erhalten bleiben, da footNoteContentList bereits sortiert ist
                        newSaaFootNotesMap.put(saaNumber, footNotes);
                    }

                    // Fußnote bestimmen
                    String footNoteNumber = dataFootNoteContent.getFieldValue(FIELD_DFN_ID);
                    iPartsFootNote footNote = null;

                    // Fußnote über die Fußnotennummer suchen
                    for (iPartsFootNote existingFootNote : footNotes) {
                        if (existingFootNote.getFootNoteId().getFootNoteId().equals(footNoteNumber)) {
                            footNote = existingFootNote;
                            break;
                        }
                    }

                    if (footNote == null) {
                        String footNoteName = dataFootNoteContent.getFieldValue(FIELD_DFN_NAME);
                        boolean isStandard = dataFootNoteContent.getFieldValueAsBoolean(FIELD_DFN_STANDARD);
                        String dfnType = dataFootNoteContent.getFieldValue(FIELD_DFN_TYPE);
                        footNote = new iPartsFootNote(new iPartsFootNoteId(footNoteNumber), footNoteName, new ArrayList<String>(),
                                                      isStandard, iPartsFootnoteType.getFromDBValue(dfnType));
                        footNotes.add(footNote);
                    }

                    // Eigentlichen Fußnotentext hinzufügen
                    footNote.getFootNoteTexts(project).add(dataFootNoteContent.getFieldValue(FIELD_DFNC_TEXT, dbLanguage, true));
                }
            }

            synchronized (this) {
                if (!saaFootNotesLoaded) {
                    saaFootNotesMap = newSaaFootNotesMap;
                    saaFootNotesLoaded = true;
                }
            }
        }
    }

    protected void loadSaaReferencedSAsIfNeeded(EtkProject project) {
        if (saaReferencedSAsMap == null) {
            iPartsDataSaaList saaList = iPartsDataSaaList.loadAllSaasForSa(project, saId.getSaNumber());
            Map<String, Set<String>> newSaaReferencedSAsMap = new HashMap<>();
            if (!saaList.isEmpty()) {
                iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                for (iPartsDataSaa dataSaa : saaList) {
                    String referencesSAsString = dataSaa.getFieldValue(FIELD_DS_CONNECTED_SAS);
                    if (!referencesSAsString.isEmpty()) {
                        String saaNumber = dataSaa.getAsId().getSaaNumber();
                        Set<String> referencedSAs = new TreeSet<>();
                        newSaaReferencedSAsMap.put(saaNumber, referencedSAs);

                        // Verbindungs-SAs aufsplitten und zum Set hinzufügen
                        List<String> referencedSAsList = StrUtils.toStringList(referencesSAsString,
                                                                               EDS_CONNECTED_SAS_DELIMITER, false, true);
                        for (String referencedSA : referencedSAsList) {
                            try {
                                referencedSAs.add(numberHelper.unformatSaForDB(referencedSA)); // Versuch, die SA ins DB-Format zu bringen
                            } catch (Exception e) {
                                referencedSAs.add(referencedSA); // Datenfehler -> als Fallback einfach das Original hinzufügen
                            }
                        }
                    }
                }
            }

            synchronized (this) {
                if (saaReferencedSAsMap == null) {
                    saaReferencedSAsMap = newSaaReferencedSAsMap;
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return saId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof iPartsSA)) {
            return false;
        }

        return saId.equals(((iPartsSA)obj).saId);
    }

    /**
     * Liefert als Schlüssel der Map alle {@link iPartsProductId}s von den Produkten zurück, in denen diese SA verortet
     * ist, sowie als Werte die dazugehörigen KGs.
     *
     * @param project
     * @return
     */
    public Map<iPartsProductId, Set<String>> getProductIdsToKGsMap(EtkProject project) {
        Map<iPartsProductId, Set<String>> productIdsToKGsMapLocal = productIdsToKGsMap;
        if (productIdsToKGsMapLocal == null) {
            productIdsToKGsMapLocal = new TreeMap<>();
            iPartsDataProductSAsList dataProductSAsList = iPartsDataProductSAsList.loadDataForSA(project, saId);
            for (iPartsDataProductSAs dataProductSA : dataProductSAsList) {
                Set<String> kgs = productIdsToKGsMapLocal.computeIfAbsent(new iPartsProductId(dataProductSA.getAsId().getProductNumber()),
                                                                          productId -> new TreeSet<>());
                kgs.add(dataProductSA.getFieldValue(FIELD_DPS_KG));
            }

            synchronized (this) {
                // Sichtbarkeiten für die jeweiligen Benutzer-Eigenschaften bestimmen
                if (productIdsToKGsMap == null) {
                    Set<iPartsProductId> pskProductsLocal = new HashSet<>();
                    if (productIdsToKGsMapLocal.isEmpty()) { // Freie SAs ohne Verortung sollen wie Truck-SAs behandelt werden
                        carAndVanSA = false;
                        truckAndBusSA = true;
                    } else {
                        for (iPartsProductId productId : productIdsToKGsMapLocal.keySet()) {
                            iPartsProduct product = iPartsProduct.getInstance(project, productId);
                            if (product.isCarAndVanProduct()) {
                                carAndVanSA = true;
                            }
                            if (product.isTruckAndBusProduct()) {
                                truckAndBusSA = true;
                            }
                            if (product.isPSK()) {
                                pskProductsLocal.add(productId);
                            }
                        }
                    }
                    pskProductIds = pskProductsLocal;
                    productIdsToKGsMap = productIdsToKGsMapLocal;
                }
            }
        }

        return productIdsToKGsMapLocal;
    }

    public boolean isOnlyInPSKProducts(EtkProject project) {
        getProductIdsToKGsMap(project); // Berechnet carAndVanSA, truckAndBusSA und alle PSK Produkte
        return !pskProductIds.isEmpty() && CollectionUtils.isEqualCollection(pskProductIds, productIdsToKGsMap.keySet());
    }

    public Set<iPartsProductId> getPskProductIds(EtkProject project) {
        getProductIdsToKGsMap(project); // Berechnet carAndVanSA, truckAndBusSA und alle PSK Produkte
        return pskProductIds;
    }

    public boolean isCarAndVanSA(EtkProject project) {
        getProductIdsToKGsMap(project); // Berechnet auch carAndVanSA
        return carAndVanSA;
    }

    public boolean isTruckAndBusSA(EtkProject project) {
        getProductIdsToKGsMap(project); // Berechnet auch truckAndBusSA
        return truckAndBusSA;
    }
}