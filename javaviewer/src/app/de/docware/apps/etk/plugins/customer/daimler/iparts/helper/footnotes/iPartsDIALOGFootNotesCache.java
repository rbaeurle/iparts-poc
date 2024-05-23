/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNotePosRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNotePosRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cache für DIALOG Fußnoten aus der Konstruktion
 */
public class iPartsDIALOGFootNotesCache implements CacheForGetCacheDataEvent<iPartsDIALOGFootNotesCache>, iPartsConst {

    private static final ObjectInstanceStrongLRUList<Object, iPartsDIALOGFootNotesCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private Map<iPartsDialogBCTEPrimaryKey, List<DIALOGFootnoteTextCacheEntry>> dialogGUIDToFootnotes;
    @JsonProperty
    private Map<String, List<DIALOGFootnoteTextCacheEntry>> dialogGUIDStringToFootnotes; // Nur für die JSON-Serialisierung

    private static String getInstanceHashObject(EtkProject project) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDIALOGFootNotesCache.class, "DIALOGFootNotesCache", false);
    }

    public static synchronized iPartsDIALOGFootNotesCache getInstance(EtkProject project) {
        Object hashObject = getInstanceHashObject(project);
        iPartsDIALOGFootNotesCache result = instances.get(hashObject);

        if (result == null) {
            result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsDIALOGFootNotesCache(), null, instances, hashObject);
            if (result != null) {
                return result;
            }

            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsDIALOGFootNotesCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }


    @Override
    public void fillCacheData(SetCacheDataEvent setCacheDataEvent) {
        // Aus BCTEKeys simple Strings machen als Map-Key
        dialogGUIDStringToFootnotes = dialogGUIDToFootnotes.entrySet().stream()
                .collect(Collectors.toMap((entry) -> entry.getKey().createDialogGUID(),
                                          (entry) -> entry.getValue()));
        CacheForGetCacheDataEvent.super.fillCacheData(setCacheDataEvent);
        dialogGUIDStringToFootnotes = null;
    }

    @Override
    public iPartsDIALOGFootNotesCache createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        iPartsDIALOGFootNotesCache dialogFootNotesCache = createInstance(setCacheDataEvent, instances, getInstanceHashObject(project));

        // Aus simplen Strings BCTEKeys machen als Map-Key
        dialogFootNotesCache.dialogGUIDToFootnotes = dialogFootNotesCache.dialogGUIDStringToFootnotes.entrySet().stream()
                .collect(Collectors.toMap((entry) -> iPartsDialogBCTEPrimaryKey.createFromDialogGuid(entry.getKey()),
                                          (entry) -> entry.getValue()));
        dialogFootNotesCache.dialogGUIDStringToFootnotes = null;
        return dialogFootNotesCache;
    }

    private void load(EtkProject project) {
        dialogGUIDToFootnotes = new HashMap<>();
        // Join zum Laden aller Teil zu Fußnote Beziehungen samt Fußnotentexte in allen Sprachen
        iPartsDataFootNotePosRefList dialogFNRefs = new iPartsDataFootNotePosRefList();
        EtkDisplayFields selectFields = new EtkDisplayFields();

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_ID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_NAME, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_STANDARD, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_TYPE, false, false));

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT, true, false));

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_POS, FIELD_DFNP_SDATB, false, false));

        // Join DA_FN_POS auf DA_FN_CONTENT und DA_FN
        dialogFNRefs.searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin(project, selectFields, TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT),
                                                                               null, null, false,
                                                                               new String[]{ TableAndFieldName.make(TABLE_DA_FN_POS, FIELD_DFNP_GUID),
                                                                                             TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_FNID),
                                                                                             TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO) },
                                                                               false, false,
                                                                               new EtkDataObjectList.JoinData(TABLE_DA_FN_CONTENT,
                                                                                                              new String[]{ FIELD_DFNP_FN_NO },
                                                                                                              new String[]{ FIELD_DFNC_FNID },
                                                                                                              false, false),
                                                                               new EtkDataObjectList.JoinData(TABLE_DA_FN,
                                                                                                              new String[]{ FIELD_DFNP_FN_NO },
                                                                                                              new String[]{ FIELD_DFN_ID },
                                                                                                              false, false));

        if (!dialogFNRefs.isEmpty()) {
            Map<iPartsFootNoteId, FootnoteWithTextForCache> tempCache = new HashMap<>();
            Set<String> existingData = new HashSet<>();
            for (iPartsDataFootNotePosRef dialogRefEntry : dialogFNRefs) {
                iPartsFootNoteId footNoteId = new iPartsFootNoteId(dialogRefEntry.getAsId().getFnNo());
                FootnoteWithTextForCache footnoteWithText = tempCache.get(footNoteId);

                // Check, ob die Fußnote schon einmal vorkam (versch. BCTE Schlüssel können ja die gleich Fußnote haben)
                if (footnoteWithText == null) {
                    footnoteWithText = new FootnoteWithTextForCache(footNoteId);
                    tempCache.put(footNoteId, footnoteWithText);
                }
                // Fußnotentext zusammenbauen
                String fnName = dialogRefEntry.getFieldValue(FIELD_DFN_NAME);
                String lineNo = dialogRefEntry.getFieldValue(FIELD_DFNC_LINE_NO);
                boolean isStandard = dialogRefEntry.getFieldValueAsBoolean(FIELD_DFN_STANDARD);
                EtkMultiSprache text = dialogRefEntry.getFieldValueAsMultiLanguage(FIELD_DFNC_TEXT);
                footnoteWithText.addText(project, fnName, iPartsFootnoteType.CONSTRUCTION_FOOTNOTE, lineNo, isStandard, text);

                // Fußnoten werden nach ihrem BCTE Schlüssel ohne SDATA gruppiert, damit man die Intervallen der Positionen
                // mit den Intervallen der Fußnoten abgleichen kann
                iPartsDialogBCTEPrimaryKey bctePrimaryKeyWithoutSDA = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogRefEntry.getAsId().getBCTEKey());
                if (bctePrimaryKeyWithoutSDA != null) {
                    String dialogFNRefSdata = bctePrimaryKeyWithoutSDA.sData;
                    String dialogFNRefSdatb = dialogRefEntry.getFieldValue(FIELD_DFNP_SDATB);
                    bctePrimaryKeyWithoutSDA.sData = "";
                    // Pro BCTE Schlüsssel ohne SDATA wird eine Liste gehalten mit DIALOGFootnoteTextCacheEntry Objekten.
                    // Ein DIALOGFootnoteTextCacheEntry Objekt enthält wiederum das SDATA, SDATB und den Text zu einer
                    // kompletten Fußnote. Daher wird hier zu den Datumsangaben, dem BCTE Schlüssel (ohne SDATA) und
                    // der Fußnoten-ID ein Key erzeugt, mit dem geprüft wird, ob diese Konstellation schon in der Liste
                    // der DIALOGFootnoteTextCacheEntry Objekte existiert. Falls ja, soll es nicht hinzugefügt werden, weil
                    // dieses Objekt dann redundant wäre.
                    String key = dialogFNRefSdata + "|" + dialogFNRefSdatb + "|" + footnoteWithText.getFootnoteId() + "|" + bctePrimaryKeyWithoutSDA;
                    if (existingData.contains(key)) {
                        continue;
                    }
                    // Fußnotenobjekt hinzufügen
                    List<DIALOGFootnoteTextCacheEntry> footnotesForBCTEWithoutSDA = dialogGUIDToFootnotes.computeIfAbsent(bctePrimaryKeyWithoutSDA, k -> new ArrayList<>());
                    DIALOGFootnoteTextCacheEntry footnoteTextCacheEntry = new DIALOGFootnoteTextCacheEntry(dialogFNRefSdata,
                                                                                                           dialogFNRefSdatb,
                                                                                                           footnoteWithText);
                    footnotesForBCTEWithoutSDA.add(footnoteTextCacheEntry);
                    existingData.add(key);

                }
            }
        }
    }

    /**
     * Liefert alle gültigen Fußnoten zur übergebenen Stücklistenposition und dem übergebenen SDATB für die gewünschte
     * Sprache
     *
     * @param project
     * @param bctePrimaryKey
     * @param sDatb
     * @param language
     * @return
     */
    public List<iPartsFootNote> getFootnotesForBCTEKey(EtkProject project, iPartsDialogBCTEPrimaryKey bctePrimaryKey,
                                                       String sDatb, String language) {
        if (bctePrimaryKey == null) {
            return null;
        }
        iPartsDialogBCTEPrimaryKey bctePrimaryKeyClone = bctePrimaryKey.cloneMe();
        bctePrimaryKeyClone.sData = "";
        List<DIALOGFootnoteTextCacheEntry> footnotes = dialogGUIDToFootnotes.get(bctePrimaryKeyClone);
        if ((footnotes != null) && !footnotes.isEmpty()) {
            List<iPartsFootNote> footnotesForBCTEKey = new DwList<>();
            for (DIALOGFootnoteTextCacheEntry footnoteTextCacheEntry : footnotes) {
                if (footnoteTextCacheEntry.isValidForEntryDates(bctePrimaryKey.sData, sDatb)) {
                    iPartsFootNote footnoteForLanguage = footnoteTextCacheEntry.getFnTextForCache().getFootnoteForLanguage(project, language);
                    if (footnoteForLanguage != null) {
                        footnotesForBCTEKey.add(footnoteForLanguage);
                    }
                }
            }
            if (!footnotesForBCTEKey.isEmpty()) {
                return footnotesForBCTEKey;
            }
        }
        return null;
    }

    /**
     * Liefert alle gültigen Fußnoten zur übergebenen Stücklistenposition
     *
     * @param project
     * @param partListEntry
     * @return
     */
    public List<iPartsFootNote> getFootnotesForBCTEKey(EtkProject project, EtkDataPartListEntry partListEntry) {
        String sdatb = iPartsFootNoteHelper.getSdatbFromEntry(partListEntry);
        return getFootnotesForBCTEKey(project, iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry),
                                      sdatb, project.getDBLanguage());
    }

    public static class DIALOGFootnoteTextCacheEntry implements RESTfulTransferObjectInterface {

        @JsonProperty
        private FootnoteWithTextForCache fnTextForCache;
        @JsonProperty
        private String sdata;
        @JsonProperty
        private String sdatb;

        public DIALOGFootnoteTextCacheEntry() {
        }

        public DIALOGFootnoteTextCacheEntry(String dialogFNRefSdata, String dialogFNRefSdatb,
                                            FootnoteWithTextForCache footnoteWithText) {
            this.sdata = dialogFNRefSdata;
            this.sdatb = dialogFNRefSdatb;
            this.fnTextForCache = footnoteWithText;
        }

        public FootnoteWithTextForCache getFnTextForCache() {
            return fnTextForCache;
        }

        public String getSdata() {
            return sdata;
        }

        public String getSdatb() {
            return sdatb;
        }

        public boolean isValidForEntryDates(String sDataFromEntry, String sDatbFromEntry) {
            return iPartsFootNoteHelper.isDIALOGFootnoteDatesValidForPartListEntryDates(sDataFromEntry, sDatbFromEntry, sdata, sdatb);
        }
    }

}
