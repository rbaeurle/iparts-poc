/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsFactoryDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsResponseDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsFootnoteType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCrossRefFootnoteHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class iPartsMigrationFootnotesHandler implements iPartsConst {

    public final static String COLOR_TABLEFOOTNOTE_START = "001";
    // Regex für Parsen Einsatzfußnotentexte
    private static Pattern patternResponseData = Pattern.compile("^([A|B][A|B|E|F|G|H|L|M|N|R|S|V]) *(([\\*,A-Z,0-9])? *([0-9]{6,8}))( *\\(WHC: *([A-Z,0-9]{3})\\))?$");
    private final static String RESPONSE_DATA_IN_TABLEFOOTNOTE_FLAG = "TF";

    private Map<iPartsFootNoteId, List<iPartsFootNoteCatalogueRefId>> currentFootNoteIdsToPLEntriesMap; // Map von Fußnoten-IDs auf eine Liste aller Stücklisteneinträge, die eine Referenz auf diese Fußnote haben, in der aktuellen KG bzw. SA
    private Map<iPartsFootNoteId, List<iPartsFootNoteId>> crossRefFootnoteNumbers;
    private Map<iPartsFootNoteCatalogueRefId, String> footNoteResponseDataForCrossRef;
    private Map<iPartsFootNoteId, iPartsDataFootNote> currentFootNoteMap;                               // Map aller gültigen Fußnoten-IDs zu den Fußnoten in der aktuellen KG bzw. SA (inkl. Gatterfußnoten und A9/E9)
    private Map<iPartsFootNoteId, iPartsDataFootNote> extraCurrentFootNoteMap;
    private Map<iPartsFootNoteId, Set<iPartsFootNoteContentId>> currentResponseFootNoteToContentMap;    // Einsatzfußnoten-IDs -> Einsatzfußnoten-Content-IDs
    private Map<String, String> footNoteIdsToResponseDataFNsInTableFootNotes;                           // Map für Tabellenfußnoten-Ids auf Fußnoten-Ids zu Einsatzfußnoten in der Tabelle
    private Set<iPartsFootNoteId> currentInvalidFootNoteIds;                                            // Set mit allen gefundenen ungültigen Fußnoten-IDs in der aktuellen KG bzw. SA
    private Map<iPartsFootNoteContentId, List<ResponseData>> currentFootnoteContentToResponseDataMap;   // Einsatzfußnoten-Content-IDs -> Rückmeldedaten
    private Set<String> standardFootNoteNumbers;                                                        // Set mit allen Standardfußnotennummern

    private EtkProject project;
    private String prefixForFootnoteIds;
    private AbstractDataImporter importer;
    private iPartsImportDataOrigin originForData;

    public iPartsMigrationFootnotesHandler(EtkProject project, AbstractDataImporter importer, String prefixForFootnoteIds,
                                           iPartsImportDataOrigin originForData) {
        this.project = project;
        this.prefixForFootnoteIds = prefixForFootnoteIds;
        this.importer = importer;
        this.originForData = originForData;
        initHandler();
    }

    private void initHandler() {
        currentFootNoteIdsToPLEntriesMap = new LinkedHashMap<>();
        crossRefFootnoteNumbers = new HashMap<>();
        footNoteResponseDataForCrossRef = new HashMap();
        currentFootNoteMap = new HashMap<>();
        extraCurrentFootNoteMap = new HashMap<>();
        currentResponseFootNoteToContentMap = new HashMap<>();
        currentInvalidFootNoteIds = new HashSet<>();
        currentFootnoteContentToResponseDataMap = new HashMap<>();
        footNoteIdsToResponseDataFNsInTableFootNotes = new HashMap<>();
    }

    public EtkProject getProject() {
        return project;
    }

    /**
     * Referenzen von Fußnoten speichern (pro KG bzw. SA)
     * - normale Fußnoten an Stücklisteneintrag
     * - Farb-Tabellenfußnoten an Stücklisteneintrag
     * - Einsatzfußnoten als
     * - iParts Fußnoten an Werkseinsatzdaten (nicht interpretierbare Gatterfußnoten und A9-Fußnoten) und/oder
     * - Rückmeldedaten (Idents; nur Gatterfußnoten)
     *
     * currentFootNoteIdsToPLEntriesMap sind die Stücklistenreferenzen aus Satzart 9/D
     */
    public void saveCurrentFootNotesForPartListEntries(Map<String, List<MadTal4XABaseImporter.ColortTablefootnote>> allCurrentColorTablefootnotes,
                                                       Map<iPartsFootNoteId, Set<List<iPartsFootNoteId>>> allFootnoteGroupsForFootnoteNumber, boolean checkIfStandardFootnote) {
        List<iPartsDataFootNoteCatalogueRef> dataFootNoteCatalogueRefList = getFnKatalogReferencesInRightSequence(allCurrentColorTablefootnotes, allFootnoteGroupsForFootnoteNumber);

        Map<String, Integer> factoryDataGuidToSequenceNoMap = new HashMap<>(); // speichert die höchste laufenden Nummer für jeden Werkseinsatzdaten-GUID
        Map<iPartsFootNoteContentId, List<ResponseData>> referencedFootNoteContentToResponseDataMap = new HashMap<>();
        Set<iPartsFootNoteId> referencedFactoryDataFootNotesSet = new HashSet<>();

        // Hier müssen die Einsatzdaten aus Tabellenfußnoten hinzugefügt werden
        dataFootNoteCatalogueRefList = addFactoryFootNotesFromTableFootNotes(dataFootNoteCatalogueRefList);

        for (iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef : dataFootNoteCatalogueRefList) {
            iPartsFootNoteId footNoteId = new iPartsFootNoteId(dataFootNoteCatalogueRef.getAsId().getFootNoteId());
            boolean isPartlistFootnote = true;       // true wenn normale Fußnoten Stückliste, also keine Einsatzfußnote
            boolean isFactoryDataFootnote = false;  // wenn true wird iParts Fußnote an Werkseinsatzdaten erstellt
            boolean isCrossRefFootNote = false;  // wenn true, dann handelt es sich um eine 'SIEHE FUSSNOTE' die Werkseinsatzdaten enthält
            if (currentResponseFootNoteToContentMap.containsKey(footNoteId)) {
                // Fußnote ist Einsatzfußnote
                isPartlistFootnote = false;
                // Wenn die Fußnote in "currentFootNoteMap" vorkommt, dann wurde sie als "normale" Fußnote verarbeitet.
                // Das bedeutet bei Einsatzfußnoten, dass die Informationen NICHT interpretiert werden konnte.
                // -> Erezeuge eine "normale" Fußnote an den Werkseisnatzdaten, also isFactoryDataFootnote = true;
                if (currentFootNoteMap.containsKey(footNoteId)) {
                    isFactoryDataFootnote = true;
                }
                isCrossRefFootNote = containsCrossRefFootNote(footNoteId);
            }

            if (isPartlistFootnote) {
                // normale Fußnote ->  Referenz wird an Stückliste gespeichert
                if ((checkIfStandardFootnote && isStandardFootNote(footNoteId.getFootNoteId())) || currentFootNoteMap.containsKey(footNoteId)) {
                    // Referenz auf normale Fußnote wird in DA_FN_KATALOG_REF gespeichert
                    importer.saveToDB(dataFootNoteCatalogueRef, false);
                } else if (!currentInvalidFootNoteIds.contains(footNoteId)) {
                    // Fußnote wurde nicht gefunden (auch nicht bei den ungültigen Fußnoten)
                    importer.getMessageLog().fireMessage(importer.translateForLog("!!Referenzierte Fußnote \"%1\" für Stücklistenposition \"%2\" nicht gefunden.",
                                                                                  footNoteId.getFootNoteId(), dataFootNoteCatalogueRef.getPartListId().toDBString()),
                                                         MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
            } else {
                // Einsatzfußnote ->  Referenz wird in Werkseinsatzdaten gespeichert (entweder als Pseudo-PEM pro Ident oder als iParts Fußnote oder beides)
                // Pro Stücklisteneintrag entstehen also i.A. mehrere Werkseinsatzdateneinträge, die über eine laufende
                // Nummer im Primärschlüssel voneinander unterschieden werden (siehe getNextFactoryDataSequenceNo()).

                // Referenz auf iParts Fußnote
                String sourceGUID = getSourceGUID(dataFootNoteCatalogueRef.getAsId().getModuleId(), dataFootNoteCatalogueRef.getAsId().getModuleSeqNo());
                if (isFactoryDataFootnote) {
                    referencedFactoryDataFootNotesSet.add(footNoteId);
                    String sequenceNo = getNextFactoryDataSequenceNo(factoryDataGuidToSequenceNoMap, sourceGUID);
                    iPartsFactoryDataId factoryDataId = new iPartsFactoryDataId(sourceGUID, sequenceNo);
                    iPartsDataFactoryData factoryData = new iPartsDataFactoryData(getProject(), factoryDataId);
                    factoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    factoryData.setAttributeValue(FIELD_DFD_SOURCE, originForData.getOrigin(), DBActionOrigin.FROM_EDIT);
                    factoryData.setAttributeValue(FIELD_DFD_FN_ID, footNoteId.getFootNoteId(), DBActionOrigin.FROM_EDIT);
                    importer.saveToDB(factoryData, false);
                }

                // jetzt die Referenzen auf Pseudo-PEMs pro Ident bzw. pro Fußnotenzeile
                Set<iPartsFootNoteContentId> footNoteContentIdList = currentResponseFootNoteToContentMap.get(footNoteId);
                for (iPartsFootNoteContentId footNoteContentId : footNoteContentIdList) {
                    if (currentFootnoteContentToResponseDataMap.containsKey(footNoteContentId)) {
                        List<ResponseData> responseDataList = currentFootnoteContentToResponseDataMap.get(footNoteContentId);
                        for (ResponseData responseData : responseDataList) {
                            String sequenceNo = getNextFactoryDataSequenceNo(factoryDataGuidToSequenceNoMap, sourceGUID);
                            iPartsFactoryDataId factoryDataId = new iPartsFactoryDataId(sourceGUID, sequenceNo);
                            iPartsDataFactoryData factoryData = new iPartsDataFactoryData(getProject(), factoryDataId);
                            factoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                            factoryData.setAttributeValue(FIELD_DFD_SOURCE, originForData.getOrigin(), DBActionOrigin.FROM_EDIT);

                            // es kann nur A? oder B? für den Typ geben. Das wurde beim Parsen der Fußnote bereits geprüft.
                            String type = responseData.getType();
                            String pemA;
                            String pemB;
                            String pseudoPEM = getPseudoPEM(footNoteContentId);
                            if (type.startsWith("A")) {
                                pemA = pseudoPEM;
                                pemB = "";
                            } else {
                                pemA = "";
                                pemB = pseudoPEM;
                            }
                            factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMA, pemA, DBActionOrigin.FROM_EDIT);
                            factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMB, pemB, DBActionOrigin.FROM_EDIT);

                            importer.saveToDB(factoryData, false);
                        }
                        referencedFootNoteContentToResponseDataMap.put(footNoteContentId, responseDataList);
                    }
                }
                if (isCrossRefFootNote) {
                    // normale Fußnote aus SIEHE FUSSNOTE ->  Referenz wird an Stückliste gespeichert
                    if (isStandardFootNote(footNoteId.getFootNoteId()) || currentFootNoteMap.containsKey(footNoteId) ||
                        extraCurrentFootNoteMap.containsKey(footNoteId)) {
                        // Referenz auf normale Fußnote wird in DA_FN_KATALOG_REF gespeichert
                        importer.saveToDB(dataFootNoteCatalogueRef, false);
                    } else if (!currentInvalidFootNoteIds.contains(footNoteId)) {
                        // Fußnote wurde nicht gefunden (auch nicht bei den ungültigen Fußnoten)
                        importer.getMessageLog().fireMessage(importer.translateForLog("!!Referenzierte Fußnote \"%1\" nicht gefunden.",
                                                                                      footNoteId.getFootNoteId()),
                                                             MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }
                }
            }
        }


        // Nur einmal alle referenzierten Rückmeldedaten der Pseudo-PEMs speichern (es könnte ja Mehrfachverwendung geben)
        for (Map.Entry<iPartsFootNoteContentId, List<ResponseData>> responseDataListEntry : referencedFootNoteContentToResponseDataMap.entrySet()) {
            // Rückmeldedaten zu Pseudo-PEM schreiben
            iPartsFootNoteContentId footNoteContentId = responseDataListEntry.getKey();
            for (ResponseData responseData : responseDataListEntry.getValue()) {
                String pseudoPEM = getPseudoPEM(footNoteContentId);
                String identStr = responseData.getIdent();

                iPartsResponseDataId responseDataId = new iPartsResponseDataId(pseudoPEM, identStr);
                iPartsDataResponseData dataResponseData = new iPartsDataResponseData(getProject(), responseDataId);
                dataResponseData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                dataResponseData.setFieldValue(iPartsConst.FIELD_DRD_WHC, responseData.getWhc(), DBActionOrigin.FROM_EDIT);
                dataResponseData.setFieldValue(iPartsConst.FIELD_DRD_TYPE, responseData.getType(), DBActionOrigin.FROM_EDIT);
                dataResponseData.setFieldValue(iPartsConst.FIELD_DRD_SOURCE, originForData.getOrigin(), DBActionOrigin.FROM_EDIT);
                dataResponseData.setFieldValue(iPartsConst.FIELD_DRD_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
                importer.saveToDB(dataResponseData, false);
            }
        }


        /**
         * Die Fußnoten haben wir bisher nur zwischengespeichert. Dies deshalb weil sich für A9/E9 Einsatzfußnoten i.A. erst aus
         * einer Folgezeile ergibt dass es sich um eine A9/E9 Einsatzfußnote handelt, die zu einer Fußnote an Werkseinsatzdaten wird.
         * Damit hätten wir in importFootNote() bei der ersten Zeile die Fußnote in der DB anlegen müssen und später nochmal lesen und verändern müssen um das "FD"
         * Kennzeichen zu setzen. Dies bedeutete einen Bruch beim "Buffered Save".
         */
        for (Map.Entry<iPartsFootNoteId, iPartsDataFootNote> footNoteEntry : currentFootNoteMap.entrySet()) {
            iPartsDataFootNote dataFootNote = footNoteEntry.getValue();

            // currentResponseFootNoteToContentMap enthält alle Einsatzfußnoten -> Typ "FD" setzen
            if (currentResponseFootNoteToContentMap.containsKey(footNoteEntry.getKey())) {
                dataFootNote.setFieldValue(FIELD_DFN_TYPE, iPartsFootnoteType.FACTORY_DATA.getValue(), DBActionOrigin.FROM_EDIT);
            } else if (isColorFootnote(footNoteEntry.getKey())) {
                dataFootNote.setFieldValue(FIELD_DFN_TYPE, iPartsFootnoteType.COLOR_TABLEFOOTNOTE.getValue(), DBActionOrigin.FROM_EDIT);
            }

            importer.saveToDB(dataFootNote, false);
        }

        currentFootNoteIdsToPLEntriesMap.clear();
        crossRefFootnoteNumbers.clear();
        footNoteResponseDataForCrossRef.clear();
        currentFootNoteMap.clear();
        extraCurrentFootNoteMap.clear();
        currentResponseFootNoteToContentMap.clear();
        currentInvalidFootNoteIds.clear();
        currentFootnoteContentToResponseDataMap.clear();
        footNoteIdsToResponseDataFNsInTableFootNotes.clear();
        allCurrentColorTablefootnotes.clear();
        if (allFootnoteGroupsForFootnoteNumber != null) {
            allFootnoteGroupsForFootnoteNumber.clear();
        }
    }

    /**
     * Fügt den aktuellen Fußnoten die Einsatz-Fußnoten aus Tabellenfußnoten hinzu
     *
     * @param dataFootNoteCatalogueRefList
     * @return
     */
    private List<iPartsDataFootNoteCatalogueRef> addFactoryFootNotesFromTableFootNotes(List<iPartsDataFootNoteCatalogueRef> dataFootNoteCatalogueRefList) {
        List<iPartsDataFootNoteCatalogueRef> result = new ArrayList<>();
        Set<PartListEntryId> handledIds = new HashSet<>();
        for (iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef : dataFootNoteCatalogueRefList) {
            String footnoteId = dataFootNoteCatalogueRef.getAsId().getFootNoteId();
            // Check, ob es zu der Tabellenfußnote eine Eisnatzfußnote (mit Pseudo-Id) gibt
            String fnIdForResponseDataInTableFootNote = footNoteIdsToResponseDataFNsInTableFootNotes.get(footnoteId);
            // Falls es eine Einsatzfußnote gibt und wir diese noch nicht an den Stücklisteneintrag gehängt haben
            // (manche Stücklosteneinträge haben verschiedene Tabellenfußnoten nit gleichen Einsatzfußnoten)
            // -> Refrenzeintrag erzeugen
            if ((fnIdForResponseDataInTableFootNote != null) && !handledIds.contains(dataFootNoteCatalogueRef.getPartListId())) {
                iPartsFootNoteId footNoteId = new iPartsFootNoteId(fnIdForResponseDataInTableFootNote);
                // Check, ob die Einsatzfußnote beim Import der Fußnoten auch als echte Einsatzfußnote interpretiert wurde
                if (currentResponseFootNoteToContentMap.containsKey(footNoteId)) {
                    iPartsFootNoteCatalogueRefId originalRefId = dataFootNoteCatalogueRef.getAsId();
                    iPartsFootNoteCatalogueRefId newRefId = new iPartsFootNoteCatalogueRefId(originalRefId.getModuleId(), originalRefId.getModuleVer(), originalRefId.getModuleSeqNo(), footNoteId.getFootNoteId());
                    iPartsDataFootNoteCatalogueRef responseRef = new iPartsDataFootNoteCatalogueRef(getProject(), newRefId);
                    responseRef.setFieldValue(FIELD_DFNK_FN_SEQNO, dataFootNoteCatalogueRef.getFieldValue(FIELD_DFNK_FN_SEQNO), DBActionOrigin.FROM_EDIT);
                    result.add(responseRef);
                    handledIds.add(newRefId.getPartListEntryId());
                }
            }
            result.add(dataFootNoteCatalogueRef);
        }
        return result;
    }

    public void saveCurrentFootNotesForPartListEntries(Map<String, List<MadTal4XABaseImporter.ColortTablefootnote>> allCurrentColorTablefootnotes, boolean checkIfStandardFootnote) {
        saveCurrentFootNotesForPartListEntries(allCurrentColorTablefootnotes, null, checkIfStandardFootnote);
    }

    private static String getNextFactoryDataSequenceNo(Map<String, Integer> factoryDataGuidToSequenceNoMap, String factoryDataGUID) {
        Integer sequenceNo = factoryDataGuidToSequenceNoMap.get(factoryDataGUID);
        if (sequenceNo != null) {
            sequenceNo++;
        } else {
            sequenceNo = 1;
        }
        factoryDataGuidToSequenceNoMap.put(factoryDataGUID, sequenceNo);
        return StrUtils.leftFill(sequenceNo.toString(), 5, '0');
    }

    private String getSourceGUID(String kVari, String kLfdnr) {
        return iPartsMigrationHelper.getSourceGUID(kVari, kLfdnr, iPartsDataFootNote.FOOTNOTE_ID_DELIMITER);
    }

    private String getPseudoPEM(iPartsFootNoteContentId footNoteContentId) {
        return footNoteContentId.getFootNoteId() + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER + footNoteContentId.getFootNoteLineNo();
    }

    /**
     * Fügt die Fußnoten-Produkt-Referenz zu der Map und Liste aller Fußnoten hinzu.
     *
     * @param isColorFootnote
     * @param footNoteId
     * @param refId
     * @param seqNo
     * @param refIdToRefMap
     * @param dataFootNoteCatalogueRefList
     */
    private void addFootnoteToCollections(boolean isColorFootnote, iPartsFootNoteId footNoteId, iPartsFootNoteCatalogueRefId refId,
                                          int seqNo, Map<iPartsFootNoteCatalogueRefId, iPartsDataFootNoteCatalogueRef> refIdToRefMap,
                                          List<iPartsDataFootNoteCatalogueRef> dataFootNoteCatalogueRefList) {
        String allTablefootnotesAsString = isColorFootnote ? extractOriginalFootnumberFromId(footNoteId.getFootNoteId()) : "";
        iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef = makeFootnoteRefDataObject(refId, isColorFootnote, allTablefootnotesAsString, seqNo);
        refIdToRefMap.put(dataFootNoteCatalogueRef.getAsId(), dataFootNoteCatalogueRef);
        dataFootNoteCatalogueRefList.add(dataFootNoteCatalogueRef);
    }

    /**
     * Extrahiert die Original-Fußnotennummer aus der FußnotenId.
     *
     * @param footnoteId
     * @return
     */
    private String extractOriginalFootnumberFromId(String footnoteId) {
        if (StrUtils.isValid(footnoteId)) {
            String[] token = StrUtils.toStringArray(footnoteId, iPartsDataFootNote.FOOTNOTE_ID_DELIMITER, false);
            if (token.length == 4) {
                if (token[0].equals(prefixForFootnoteIds)) {
                    return token[3];
                }
            }
        }
        return "";
    }

    /**
     * Erzeugt ein {@link iPartsDataFootNoteCatalogueRef} mit der übergebenen ID. Wenn es sich um eine Referenz zu einer
     * Farb-Tabellenfußnote handelt, dann werden die spezifischen Attribute ebenfalls gesetzt.
     *
     * @param catalogueRefId
     * @param isMarked
     * @param allTablefootnotesAsString
     * @param seqNo
     * @return
     */
    public iPartsDataFootNoteCatalogueRef makeFootnoteRefDataObject(iPartsFootNoteCatalogueRefId catalogueRefId,
                                                                    boolean isMarked, String allTablefootnotesAsString,
                                                                    int seqNo) {
        iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef = new iPartsDataFootNoteCatalogueRef(getProject(),
                                                                                                     catalogueRefId,
                                                                                                     seqNo);
        dataFootNoteCatalogueRef.setFieldValueAsBoolean(FIELD_DFNK_FN_MARKED, isMarked, DBActionOrigin.FROM_EDIT);
        dataFootNoteCatalogueRef.setFieldValue(FIELD_DFNK_COLORTABLEFOOTNOTE, allTablefootnotesAsString, DBActionOrigin.FROM_EDIT);
        return dataFootNoteCatalogueRef;
    }

    /**
     * Liefert alle {@link iPartsDataFootNoteCatalogueRef} für eine KG oder eine SA Sortiert nach ihrer vom Import vorgegebenen
     * Reihefolge. Bei Farb-Tabellenfußnoten wurden die zusätzlichen Fußnoten (ebenfalls in der richtigen Reihenfolge)
     * hinzugefügt.
     *
     * @return
     */
    public List<iPartsDataFootNoteCatalogueRef> getFnKatalogReferencesInRightSequence(Map<String, List<MadTal4XABaseImporter.ColortTablefootnote>> allCurrentColorTablefootnotes,
                                                                                      Map<iPartsFootNoteId, Set<List<iPartsFootNoteId>>> allFootnoteGroupsForFootnoteNumber) {
        List<iPartsDataFootNoteCatalogueRef> dataFootNoteCatalogueRefList = new ArrayList<>();
        Map<PartListEntryId, Map<iPartsFootNoteCatalogueRefId, iPartsDataFootNoteCatalogueRef>> partListEntryToRefMap = new HashMap<>();

        // Durchlaufe alle gefundenen Fußnoten
        for (Map.Entry<iPartsFootNoteId, List<iPartsFootNoteCatalogueRefId>> footNoteEntry : currentFootNoteIdsToPLEntriesMap.entrySet()) {
            iPartsFootNoteId footNoteId = footNoteEntry.getKey();
            List<iPartsFootNoteCatalogueRefId> dataFootNoteCatalogueRefIdList = footNoteEntry.getValue();
            List<MadTal4XABaseImporter.ColortTablefootnote> allColorFootnotes = null;
            List<iPartsFootNoteId> crossRefFootNotes = crossRefFootnoteNumbers.get(footNoteId);
            boolean isColorFootnote = isColorFootnote(footNoteId);
            // Wenn die FußnotenID zu einer Farb-Tabellenfußnote gehört, dann bestimme alle verknüpften Farb-Tabellenfußnoten
            if (isColorFootnote) {
                // Suche alle Farb-Tabellenfußnoten in denen diese Fußnote vorkommt
                allColorFootnotes = getColorTableFootnote(footNoteId, allCurrentColorTablefootnotes);
            }
            // Durchlaufe alle Fußnote<>Stücklisteneintrag Verknüpfungen
            for (iPartsFootNoteCatalogueRefId refId : dataFootNoteCatalogueRefIdList) {
                PartListEntryId partListEntryId = refId.getPartListEntryId();
                // Überprüfe, ob der aktuelle Stücklisteneintrag schon Referenzen enthält (wichtig für die Generierung der
                // Fußnoten-Sequenznummer)
                Map<iPartsFootNoteCatalogueRefId, iPartsDataFootNoteCatalogueRef> refIdToRefMap = partListEntryToRefMap.get(partListEntryId);
                if (refIdToRefMap == null) {
                    refIdToRefMap = new LinkedHashMap<>();
                    partListEntryToRefMap.put(partListEntryId, refIdToRefMap);
                }
                int seqNo = refIdToRefMap.size() + 1;
                // Unterscheidung Farb-Tabellenfußnoten und andere Fußnoten
                if (isColorFootnote && (allColorFootnotes != null) && !allColorFootnotes.isEmpty()) {
                    if (refIdToRefMap.containsKey(refId)) {
                        // Existiert die einzelne Fußnote innerhalb einer Farb-Tabellenfußnote, die schon an den
                        // Stücklisteneintrag gehängt wurde, dann darf die ganze Tabelle nicht nochmal erzeugt und an
                        // den Stüklisteneintrag gehängt werden. Stattdessen wird die aktuelle Fußnote in der Tabelle
                        // hervorgehoben
                        iPartsDataFootNoteCatalogueRef existingRefData = refIdToRefMap.get(refId);
                        existingRefData.setFieldValueAsBoolean(FIELD_DFNK_FN_MARKED, true, DBActionOrigin.FROM_EDIT);
                    } else {
                        // Die aktuelle Fußnote hängt noch nicht an dem Stücklisteneintrag. Erzeuge die komplette
                        // Farb-Tabellenfußnote, markiere die aktuelle Fußnote und hänge die komplette Tabelle an den
                        // Stücklisteneintrag
                        List<iPartsDataFootNoteCatalogueRef> dataFootNoteCatalogueRefsForSingleEntry = new ArrayList<>();
                        for (MadTal4XABaseImporter.ColortTablefootnote colorTablefootnote : allColorFootnotes) {
                            seqNo = colorTablefootnote.fillCatalogueReferencesList(dataFootNoteCatalogueRefsForSingleEntry, refId, seqNo);
                        }
                        fillWithAdditionalRefs(dataFootNoteCatalogueRefsForSingleEntry, refIdToRefMap, dataFootNoteCatalogueRefList);
                    }
                } else if ((allFootnoteGroupsForFootnoteNumber != null) && allFootnoteGroupsForFootnoteNumber.containsKey(footNoteId)) {
                    if (!refIdToRefMap.containsKey(refId)) {

                        Set<List<iPartsFootNoteId>> groupsForFootnoteId = allFootnoteGroupsForFootnoteNumber.get(footNoteId);
                        List<iPartsDataFootNoteCatalogueRef> dataFootNoteCatalogueRefsForSingleEntry = new ArrayList<>();
                        for (List<iPartsFootNoteId> group : groupsForFootnoteId) {
                            for (iPartsFootNoteId footnoteIdFromGroup : group) {
                                List<iPartsFootNoteId> tempCrossRefs = crossRefFootnoteNumbers.get(footnoteIdFromGroup);
                                if (tempCrossRefs != null) {
                                    if (crossRefFootNotes == null) {
                                        crossRefFootNotes = new ArrayList<>();
                                    }
                                    crossRefFootNotes.addAll(tempCrossRefs);
                                }
                                iPartsFootNoteCatalogueRefId catalogueRefId = new iPartsFootNoteCatalogueRefId(partListEntryId, footnoteIdFromGroup.getFootNoteId());
//                                boolean isMarked = catalogueRefId.equals(refId);
                                dataFootNoteCatalogueRefsForSingleEntry.add(makeFootnoteRefDataObject(catalogueRefId, false, "", seqNo));
                                seqNo++;
                            }
                        }
                        fillWithAdditionalRefs(dataFootNoteCatalogueRefsForSingleEntry, refIdToRefMap, dataFootNoteCatalogueRefList);
                    }
                } else {
                    // Bei nicht Farb-Tabellenfußnoten werden die Referenzen ganz normal samt Sequenznummer angelegt.
                    // Es kann aber vorkommen, dass wir "einzeilige" Farb-Tabellenfußnoten bekommen. Diese werden als
                    // "normale" Fußnoten angelegt, erhalten aber die FTFN spezifischen Attribute (markiert und Fußnoten
                    // aus der Tabelle)
                    if (!refIdToRefMap.containsKey(refId)) {
                        addFootnoteToCollections(isColorFootnote, footNoteId, refId, seqNo, refIdToRefMap,
                                                 dataFootNoteCatalogueRefList);
                    }
                }
                if (crossRefFootNotes != null) {
                    seqNo = refIdToRefMap.size();
                    for (iPartsFootNoteId crossRefFootNoteId : crossRefFootNotes) {
                        seqNo++;
                        boolean isCrossRefColorFootNote = isColorFootnote(crossRefFootNoteId);
                        iPartsFootNoteCatalogueRefId crossRefId = new iPartsFootNoteCatalogueRefId(refId.getPartListEntryId(),
                                                                                                   crossRefFootNoteId.getFootNoteId());
                        addFootnoteToCollections(isCrossRefColorFootNote, crossRefFootNoteId, crossRefId, seqNo, refIdToRefMap,
                                                 dataFootNoteCatalogueRefList);

                        // Bezug auf Fußnote für Werkseinsatzdaten?
                        iPartsFootNoteCatalogueRefId currentResponseFootNoteRefId = null;
                        for (iPartsFootNoteCatalogueRefId responseFootNoteRefId : footNoteResponseDataForCrossRef.keySet()) {
                            if (responseFootNoteRefId.getModuleId().equals(crossRefFootNoteId.getFootNoteId())) {
                                currentResponseFootNoteRefId = responseFootNoteRefId;
                                break;
                            }
                        }

                        if (currentResponseFootNoteRefId != null) {
                            if (!currentFootNoteMap.containsKey(crossRefFootNoteId)) {
                                // Neue Fußnote
                                iPartsDataFootNote dataFootNote = new iPartsDataFootNote(getProject(), crossRefFootNoteId);
                                dataFootNote.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                                dataFootNote.setFieldValue(FIELD_DFN_NAME, currentResponseFootNoteRefId.getFootNoteId(), DBActionOrigin.FROM_EDIT);
                                dataFootNote.setFieldValue(FIELD_DFN_TYPE, iPartsFootnoteType.FACTORY_DATA.getValue(), DBActionOrigin.FROM_EDIT);
                                importer.saveToDB(dataFootNote, false);
                                extraCurrentFootNoteMap.put(crossRefFootNoteId, dataFootNote);

                                // Fußnoteninhalt erzeugen und speichern
                                iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(getProject(), crossRefFootNoteId,
                                                                                                              currentResponseFootNoteRefId.getModuleSeqNo());
                                dataFootNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                                dataFootNoteContent.setFieldValue(FIELD_DFNC_TEXT_NEUTRAL, footNoteResponseDataForCrossRef.get(currentResponseFootNoteRefId), DBActionOrigin.FROM_EDIT);
                                importer.saveToDB(dataFootNoteContent, false);
                                footNoteResponseDataForCrossRef.remove(currentResponseFootNoteRefId);
                            }
                        }
                    }
                }
            }
        }
        return dataFootNoteCatalogueRefList;
    }

    private void fillWithAdditionalRefs(List<iPartsDataFootNoteCatalogueRef> dataFootNoteCatalogueRefsForSingleEntry,
                                        Map<iPartsFootNoteCatalogueRefId, iPartsDataFootNoteCatalogueRef> refIdToRefMap,
                                        List<iPartsDataFootNoteCatalogueRef> dataFootNoteCatalogueRefList) {
        for (iPartsDataFootNoteCatalogueRef catToFootnoteRef : dataFootNoteCatalogueRefsForSingleEntry) {
            refIdToRefMap.put(catToFootnoteRef.getAsId(), catToFootnoteRef);
            dataFootNoteCatalogueRefList.add(catToFootnoteRef);
        }
    }

    public void clear() {
        crossRefFootnoteNumbers.clear();
        footNoteResponseDataForCrossRef.clear();
        currentFootNoteMap.clear();
        extraCurrentFootNoteMap.clear();
        currentResponseFootNoteToContentMap.clear();
        currentFootnoteContentToResponseDataMap.clear();
        currentInvalidFootNoteIds.clear();
        currentFootNoteIdsToPLEntriesMap.clear();
        standardFootNoteNumbers = null;
    }

    /**
     * Erzeugt Referenz-IDs zwischen Stücklistenpositionen und Fußnoten.
     *
     * @param destPartListEntryId
     * @param footNoteIds
     */
    public void createFootNoteRefs(PartListEntryId destPartListEntryId, Set<iPartsFootNoteId> footNoteIds) {
        for (iPartsFootNoteId footNoteId : footNoteIds) {
            iPartsFootNoteCatalogueRefId footNoteCatalogueRefId = new iPartsFootNoteCatalogueRefId(destPartListEntryId, footNoteId.getFootNoteId());
            // Stücklisteneintrag für die Referenz zur KG-spezifische Fußnotennummer merken, da es sich
            // evtl. auch um Farbfußnoten o.ä. handeln könnte, für die kein Eintrag erzeugt werden darf
            List<iPartsFootNoteCatalogueRefId> dataFootNoteCatalogueRefList = currentFootNoteIdsToPLEntriesMap.get(footNoteId);
            if (dataFootNoteCatalogueRefList == null) {
                dataFootNoteCatalogueRefList = new DwList<>();
                currentFootNoteIdsToPLEntriesMap.put(footNoteId, dataFootNoteCatalogueRefList);
            }
            // Weil die Verknüpfungen vor der eigentlichen Farb-Tabellenfußnote importiert werden und Fußnoten am Ende
            // einer KG importiert werden, muss hier die Modulnummer gespeichert werden. Dadurch kann beim Speichern des
            // Moduls das Flag für die Farb-Tabellenfußnoten gesetzt werden.
            dataFootNoteCatalogueRefList.add(footNoteCatalogueRefId);
        }
    }

    public iPartsFootNoteId getFootNoteIdForProduct(String kg, String footNoteNumber, String productNo) {
        return iPartsMigrationHelper.getFootNoteIdForProduct(prefixForFootnoteIds,
                                                             iPartsDataFootNote.FOOTNOTE_ID_DELIMITER,
                                                             productNo, kg,
                                                             footNoteNumber);
    }

    public iPartsFootNoteId getFootNoteIdForSA(String footnoteNumber, String saNumber) {
        return iPartsMigrationHelper.getFootNoteIdForSA(prefixForFootnoteIds, iPartsDataFootNote.FOOTNOTE_ID_DELIMITER, footnoteNumber, saNumber);
    }


    /**
     * Verarbeitet Fußnoten, die in anderen Fußnoten mit ihrer Fußnotennummer referenziert werden.
     *
     * @param footNoteIdReal
     * @param isFootNoteForProduct
     */
    public void handleCrossRefFootnotes(iPartsFootNoteId footNoteIdReal, String productNo, String kg, String saNumber, String textDE, boolean isFootNoteForProduct) {
        Set<String> currentCrossRefFootnoteNumbers = new LinkedHashSet<>();
        if (iPartsCrossRefFootnoteHelper.analyzeFootNoteCrossreference(textDE, currentCrossRefFootnoteNumbers)) {
            if (crossRefFootnoteNumbers == null) {
                crossRefFootnoteNumbers = new HashMap<>();
            }
            for (String crossRefNumber : currentCrossRefFootnoteNumbers) {
                iPartsFootNoteId crossRefFootNoteId;
                if (isStandardFootNote(crossRefNumber)) {
                    crossRefFootNoteId = new iPartsFootNoteId(crossRefNumber);
                } else {
                    if (isFootNoteForProduct) {
                        crossRefFootNoteId = getFootNoteIdForProduct(kg, crossRefNumber, productNo);
                    } else {
                        crossRefFootNoteId = getFootNoteIdForSA(crossRefNumber, saNumber);
                    }
                }
                List<iPartsFootNoteId> crossRefList = crossRefFootnoteNumbers.get(footNoteIdReal);
                if (crossRefList == null) {
                    crossRefList = new DwList<>();
                    crossRefFootnoteNumbers.put(footNoteIdReal, crossRefList);
                }
                crossRefList.add(crossRefFootNoteId);
            }
        }
    }

    public boolean isStandardFootNote(String footNoteNumber) {
        // Standardfußnoten bei Bedarf laden
        if (standardFootNoteNumbers == null) {
            standardFootNoteNumbers = iPartsDataFootNoteList.loadStandardFootnotesAsSet(getProject());
        }
        return standardFootNoteNumbers.contains(footNoteNumber);
    }


    /**
     * Verarbeitet die Fußnoten für einen Baumusterimport (TAL40 oder EPC Model)
     *
     * @param footNoteNumbers
     * @param tuvValue
     * @param productNumber
     * @param kg
     * @param checkIfStandardFootnote
     * @return
     */
    public Set<iPartsFootNoteId> handleFootnotesForModel(List<String> footNoteNumbers, String tuvValue,
                                                         String productNumber, String kg, boolean checkIfStandardFootnote) {
        Set<iPartsFootNoteId> footNoteIds = new LinkedHashSet<>();
        // TUV Fußnotenkürzel interpretieren:
        // TUVs ´zeigen normalerweise auf Standardfußnoten. Ncht bei EPC. Dort haben die Standardfußnotennummern
        // eine spezifische Fußnote. Kann aus dem TUV Wert keine Standardfußnotennummer extrahiert werden, wird ganz
        // normal versucht eine Standardfußnote zu generieren.
        if (checkIfStandardFootnote || !handleTUVFootnoteWithoutStandardFootnotes(tuvValue, footNoteNumbers)) {
            iPartsMigrationHelper.handleTUVFootnote(tuvValue, footNoteIds);
        }
        for (String footNoteNumber : footNoteNumbers) {
            if (checkIfStandardFootnote && isStandardFootNote(footNoteNumber)) {
                footNoteIds.add(new iPartsFootNoteId(footNoteNumber));
            } else {
                footNoteIds.add(getFootNoteIdForProduct(kg, footNoteNumber, productNumber));
            }
        }
        return footNoteIds;
    }

    /**
     * Verarbeitet die Fußnoten für einen SA-Import (TAL46 oder EPC Sa)
     *
     * @param footNoteNumbers
     * @param tuvValue
     * @param saNumber
     * @param checkIfStandardFootnote
     * @return
     */
    public Set<iPartsFootNoteId> handleFootnotesForSA(List<String> footNoteNumbers, String tuvValue, String saNumber, boolean checkIfStandardFootnote) {
        Set<iPartsFootNoteId> footNoteIds = new LinkedHashSet<>();
        // TUV Fußnotenkürzel interpretieren:
        // TUVs ´zeigen normalerweise auf Standardfußnoten. Ncht bei EPC. Dort haben die Standardfußnotennummern
        // eine spezifische Fußnote. Kann aus dem TUV Wert keine Standardfußnotennummer extrahiert werden, wird ganz
        // normal versucht eine Standardfußnote zu generieren.
        if (checkIfStandardFootnote || !handleTUVFootnoteWithoutStandardFootnotes(tuvValue, footNoteNumbers)) {
            iPartsMigrationHelper.handleTUVFootnote(tuvValue, footNoteIds);
        }
        for (String footNoteNumber : footNoteNumbers) {
            if (checkIfStandardFootnote && isStandardFootNote(footNoteNumber)) {
                footNoteIds.add(new iPartsFootNoteId(footNoteNumber));
            } else {
                footNoteIds.add(getFootNoteIdForSA(footNoteNumber, saNumber));
            }
        }
        return footNoteIds;
    }

    /**
     * Holt zum TUV Wert die dazugehörige Fußnotennummer und legt diese in die übergebene Liste
     *
     * @param tuvValue
     * @param footnoteNumbers
     * @return
     */
    public boolean handleTUVFootnoteWithoutStandardFootnotes(String tuvValue, List<String> footnoteNumbers) {
        if (StrUtils.isValid(tuvValue) && (footnoteNumbers != null)) {
            String mappedFootnoteNumber = iPartsMigrationHelper.getFootnoteNumberForTUVValue(tuvValue);
            if (StrUtils.isValid(mappedFootnoteNumber)) {
                footnoteNumbers.add(mappedFootnoteNumber);
                return true;
            }
        }
        return false;
    }

    public void addResponseFootNoteContentToMap(iPartsFootNoteId footNoteId, iPartsFootNoteContentId footNoteContentId) {
        Set<iPartsFootNoteContentId> footNoteContentIdSet = currentResponseFootNoteToContentMap.get(footNoteId);
        if (footNoteContentIdSet == null) {
            footNoteContentIdSet = new LinkedHashSet<>();
            currentResponseFootNoteToContentMap.put(footNoteId, footNoteContentIdSet);
        }
        footNoteContentIdSet.add(footNoteContentId);
    }

    public void addResponseDataFootNoteFromTableFootNote(String tableFn, String responseDataFn) {
        if (StrUtils.isValid(tableFn, responseDataFn)) {
            footNoteIdsToResponseDataFNsInTableFootNotes.put(tableFn, responseDataFn);
        }
    }

    /**
     * Check, ob es sich bei der übergebenen FußnotenId um eine Fußnote innerhalb einer Farb-Tabellenfußnote handelt
     *
     * @param footNoteId
     * @return
     */
    public boolean isColorFootnote(iPartsFootNoteId footNoteId) {
        String footnoteNumber = getOriginalFootnoteNumberFromId(footNoteId);
        return isColorFootnote(footnoteNumber);
    }

    /**
     * Extrahiert die Original Fußnotennummer aus der FußnotenId
     *
     * @param footNoteId
     * @return
     */
    public String getOriginalFootnoteNumberFromId(iPartsFootNoteId footNoteId) {
        String[] token = StrUtils.toStringArray(footNoteId.getFootNoteId(), iPartsDataFootNote.FOOTNOTE_ID_DELIMITER, true);
        if (token.length > 0) {
            return token[token.length - 1];
        }
        return "";
    }

    /**
     * Check, ob es sich bei der übergebenen Fußnotennummer um eine Fußnote innerhalb einer Farb-Tabellenfußnote handelt
     *
     * @param footnoteNumber
     * @return
     */
    public boolean isColorFootnote(String footnoteNumber) {
        return (footnoteNumber.length() == 3) && footnoteNumber.startsWith("9");
    }


    /**
     * Für Einsatzfußnoten (Texte beginnen mit "#"): Texte mit Idents überlesen
     * In den Daimler-Beispielen sehen Einsatzfußnotentexte so aus:
     * - Ident-Gültigkeit:  "#AF L 107155  (WHC: WDB)"
     * - Datums-Gültigkeit: "#BF 11.01.04"
     *
     * Datums-Gültigkeiten enthalten immer einen Punkt. Ident-Gültigkeiten nicht. Auf diesem Wege schließen wir also auf
     * die Art der Gültigkeit.
     * In https://confluence.docware.de/confluence/x/mAH9 hängt eine Excel-Datei "Rückmeldedatenobjekt_Attribute_Mapping_V2.xlsx" mit
     * der Formatbeschreibung für die Einsatztexte.
     *
     * @param textDE            Text der Fußnotenzeile
     * @param footNoteContentId ID der Fußnotenzeile
     * @return true wenn als Rückmeldeobjekt behandelt und daher Zeile für Fußnote zu überspringen ist.
     */
    public boolean isFootNoteLineHandledAsResponseData(String textDE, iPartsFootNoteContentId footNoteContentId) {
        if (!textDE.startsWith("#")) {
            return false;
        }

        iPartsFootNoteId footNoteId = new iPartsFootNoteId(footNoteContentId.getFootNoteId());
        addResponseFootNoteContentToMap(footNoteId, footNoteContentId);

        if (!textDE.contains(".")) {

            /**
             * Rückmeldedaten parsen
             */

            String responseDataStr = textDE.substring(1); // "#" abschneiden
            ResponseData responseData = parseResponseDataStr(responseDataStr);
            if (responseData == null) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Einsatzfußnotenzeile \"%1\" konnte nicht als Ident-Gültigkeit geparst werden. Fußnote wird stattdessen erstellt.",
                                                                              responseDataStr),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return false;
            }
            List<ResponseData> responseDataList = currentFootnoteContentToResponseDataMap.get(footNoteContentId);
            if (responseDataList == null) {
                responseDataList = new LinkedList<>();
                currentFootnoteContentToResponseDataMap.put(footNoteContentId, responseDataList);
            }
            responseDataList.add(responseData);

            return true;
        } else {
            return false;
        }
    }

    private boolean containsCrossRefFootNote(iPartsFootNoteId footNoteId) {
        for (List<iPartsFootNoteId> crossRefList : crossRefFootnoteNumbers.values()) {
            if (crossRefList.contains(footNoteId)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Liefert die Farb-Tabellenfußnoten in denen die übergebene Fußnote vorkommt
     *
     * @param footNoteId
     * @return
     */
    private List<MadTal4XABaseImporter.ColortTablefootnote> getColorTableFootnote(iPartsFootNoteId footNoteId,
                                                                                  Map<String, List<MadTal4XABaseImporter.ColortTablefootnote>> allCurrentColorTablefootnotes) {
        List<MadTal4XABaseImporter.ColortTablefootnote> result = new ArrayList<>();
        for (List<MadTal4XABaseImporter.ColortTablefootnote> colorTablefootnotes : allCurrentColorTablefootnotes.values()) {
            for (MadTal4XABaseImporter.ColortTablefootnote colorTablefootnote : colorTablefootnotes) {
                if (colorTablefootnote.containsFootnoteId(footNoteId)) {
                    result.add(colorTablefootnote);
                }
            }
        }
        return result;
    }

    public void addToCurrentInvalidFootnoteIds(iPartsFootNoteId footNoteIdLine) {
        currentInvalidFootNoteIds.add(footNoteIdLine);
    }

    public void addToFootnoteResponseDataForCrossRef(iPartsFootNoteCatalogueRefId catRefId, String textDE) {
        footNoteResponseDataForCrossRef.put(catRefId, textDE);
    }

    public void addToCurrentFootnoteMapIfNotExists(iPartsFootNoteId footNoteIdReal, String footNoteNumber) {
        if (!currentFootNoteMap.containsKey(footNoteIdReal)) {
            // Neue Fußnote
            iPartsDataFootNote dataFootNote = new iPartsDataFootNote(getProject(), footNoteIdReal);
            dataFootNote.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataFootNote.setFieldValue(FIELD_DFN_NAME, footNoteNumber, DBActionOrigin.FROM_EDIT);
            currentFootNoteMap.put(footNoteIdReal, dataFootNote);
        }
    }

//    public void addToAllCurrentColorTablefootnotes(String firstFootnoteNumberinTable, ColortTablefootnote currentColorTablefootnote) {
//        List<iPartsMigrationFootnotesHandler.ColortTablefootnote> colorTablefootnotesWithSameStartNumber = allCurrentColorTablefootnotes.get(firstFootnoteNumberinTable);
//        if (colorTablefootnotesWithSameStartNumber == null) {
//            colorTablefootnotesWithSameStartNumber = new ArrayList<>();
//            allCurrentColorTablefootnotes.put(firstFootnoteNumberinTable, colorTablefootnotesWithSameStartNumber);
//        }
//        colorTablefootnotesWithSameStartNumber.add(currentColorTablefootnote);
//    }

    /**
     * Rückmeldedaten aus Einsatzfußnotenzeile extrahieren.
     * Beispiele: siehe Testcase
     *
     * @param text
     * @return
     */
    static ResponseData parseResponseDataStr(String text) {
        Matcher matcherText = patternResponseData.matcher(text);
        if (matcherText.matches()) {

//            for (int i = 0; i < matcherText.groupCount(); i++) {
//                System.out.println(i + "Group: " + matcherText.group(i));
//            }

            String type = matcherText.group(1);
            if (type == null) {
                type = "";
            }

            // Kombination aus WKZ und Seriennummern (WKZ kann auch fehlen)
            // evtl. Leerzeichen zwischen WKZ und Seriennummer werden entfernt
            String ident = StrUtils.removeCharsFromString(matcherText.group(2), new char[]{ ' ' });
            if (ident == null) {
                return null;
            }

            String whc = matcherText.group(6);
            if (whc == null) {
                whc = "";
            }
            return new ResponseData(type, ident, whc);
        } else {
            return null;
        }
    }

    /**
     * Liefert die seudo-Fußnoten-Id für Einsatzfußnoten, die in Tabellenfußnoten vorkommen. Normalerweise würden wir
     * hier die Id der Tabellenfußnoten nehmen und einen Suffix anhängen. Weil verschiedene Tabellenfußnoten die gleichen
     * Einsatzfußnoten haben können muss hier eine Pseudo-Fußnoten-Id erzeugt werden.
     *
     * z.B. ELDAS_PRODUKT_KG_TF_NummerDerPseudoFußnote
     *
     * @param footnoteId
     * @param size
     * @return
     */
    public iPartsFootNoteId getFNIdForResponseDataInColorTable(String footnoteId, int size) {
        if (StrUtils.isValid(footnoteId)) {
            // Fußnotennummer entfernen
            footnoteId = StrUtils.stringUpToLastCharacter(footnoteId, iPartsDataFootNote.FOOTNOTE_ID_DELIMITER);
            // Kenner setzen
            footnoteId += iPartsDataFootNote.FOOTNOTE_ID_DELIMITER + RESPONSE_DATA_IN_TABLEFOOTNOTE_FLAG +
                          iPartsDataFootNote.FOOTNOTE_ID_DELIMITER + StrUtils.prefixStringWithCharsUpToLength(String.valueOf(size), '0', 3, false);
            return new iPartsFootNoteId(footnoteId);
        }
        return null;
    }

    /**
     * Hält sich die Daten aus einer Einsatzfußnotenzeile
     */
    static class ResponseData {

        private String type;
        private String ident;  // bestehend aus Werkskennzeichen und Seriennummer wobei ersteres fehlen kann
        // die genaue Unterscheidung wird an dieser Stelle nicht benötigt und ist auch schwierig zu treffen
        private String whc;

        public ResponseData(String type, String ident, String whc) {
            this.type = type;
            this.ident = ident;
            this.whc = whc;
        }

        public String getType() {
            return type;
        }

        public String getWhc() {
            return whc;
        }

        public String getIdent() {
            return ident;
        }
    }
}
