/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MadTal4XABaseImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.iPartsMigrationFootnotesHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.iPartsMigrationHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sort.SortBetweenHelper;

import java.util.*;

/**
 * EPC Abstract Importer für ModelPartList und SAPartList
 */

public abstract class AbstractEPCPartListImporter extends AbstractEPCDataImporter implements iPartsConst, EtkDbConst {

    // Zum Checken der zu importierenden Dateien/Daten
    protected enum ImportType {
        IMPORT_UNKNOWN, IMPORT_BM_PARTS, IMPORT_SA_PARTS
    }

    // Die Zieltabelle
    protected static final String DEST_TABLENAME = TABLE_KATALOG;

    protected final static String SEQNUM = "SEQNUM";
    protected final static String SEQNO = "SEQNO";
    protected final static String REPLFLG = "REPLFLG";
    protected final static String PARTNUM = "PARTNUM";
    protected final static String NOUNIDX = "NOUNIDX";
    protected final static String DESCIDX = "DESCIDX";
    protected final static String TUVSIGN = "TUVSIGN";
    protected final static String OPTFLAG = "OPTFLAG";
    protected final static String OPTPART = "OPTPART";
    protected final static String CHANGEFLAG = "CHANGEFLAG";
    protected final static String REPTYPE = "REPTYPE";
    protected final static String REPPNO = "REPPNO";
    protected final static String NEUTRAL = "NEUTRAL";
    protected final static String REPPART = "REPPART";

    // Ein kleiner Cache, damit die Produkte nicht immer wieder aus der DB geladen werden.
    private Map<iPartsProductId, Boolean> productRelevanceCache = new HashMap<>();
    private ImportType importType = ImportType.IMPORT_UNKNOWN;

    private Map<String, String> lastCompletePartListEntryImportRec;
    private List<EtkDataPartListEntry> lastCompletePartListEntry = new ArrayList<>();
    private Set<iPartsFootNoteId> currentFootNotesForPartListEntry = new HashSet<>();
    private Set<PartId> partsDone;                                                                      // Werte für diesen Teilestamm wurden schon übernommen
    private Set<String> currentPartNumbersInAssembly;                                                   // Set mit allen Teilenummern im aktuellen Modul
    private Map<String, Set<String>> currentWWPartNumbersToWWPartNumbersInAssembly;                     // Map mit Teilenummer auf Wahlweise-Teilenummern im aktuellen Modul
    private Map<String, List<EtkDataPartListEntry>> epcSortNumberToPartListEntry;
    private Map<String, List<EtkDataPartListEntry>> currentWWPartNumbersToPLEntriesInAssembly;          // Map mit Teilenummer auf Stücklisteneinträge im aktuellen Modul
    private Map<PartListEntryId, iPartsReplacement> replacementsForAssembly;
    private Map<iPartsModuleId, Boolean> moduleVariantsVisibleMap;
    private Map<String, List<MadTal4XABaseImporter.ColortTablefootnote>> allCurrentColorTablefootnotes; // Sollte es mehr als eine Farb-Fußnote geben, dann werden hier alle Farbfußnoten gehalten
    private Map<iPartsFootNoteId, Set<List<iPartsFootNoteId>>> allFootnoteGroupsForFootnoteNumber;
    private List<iPartsFootNoteId> currentFootnoteGroup;

    private DictImportTextIdHelper dictImportTextIdHelper;
    private iPartsDataAssembly currentAssembly;
    private iPartsMigrationFootnotesHandler footnotesHandler;
    private iPartsFootNoteId currentTableFootNoteId;
    private MadTal4XABaseImporter.ColortTablefootnote currentColorTablefootnote;                        // Die aktuelle Farb-Tabellenfßnote, die Zeile für Zeile aufgebaut wird

    private boolean currentFootNoteIsValid = true;
    private boolean currentFootNoteIsColorTable;                                                        // Marker, ob die aktuelle Fußnote Teil einer Farb-Tabellenfußnote ist
    private boolean currentFootNoteIsTable;
    private int currentCombinedTextSeqNo;
    private int currentRecordNo;
    private String currentFootNoteNumber;                                                               // Weitere Variablen für den Import der Fußnotendaten
    private String currentFootNoteTableGroup;                                                            // Weitere Variablen für den Import der Fußnotendaten

    public AbstractEPCPartListImporter(EtkProject project, String importName, String fileListName, ImportType importType) {
        super(project, importName, fileListName, DEST_TABLENAME, false, false);
        this.importType = importType;
        iniImporter();
    }

    private void iniImporter() {
        footnotesHandler = new iPartsMigrationFootnotesHandler(getProject(), this,
                                                               iPartsDataFootNote.FOOTNOTE_PREFIX_EPC, iPartsImportDataOrigin.EPC);
        currentPartNumbersInAssembly = new HashSet<>();
        currentWWPartNumbersToWWPartNumbersInAssembly = new LinkedHashMap<>();
        currentWWPartNumbersToPLEntriesInAssembly = new LinkedHashMap<>();
        replacementsForAssembly = new HashMap<>();
        allCurrentColorTablefootnotes = new TreeMap<>();
        allFootnoteGroupsForFootnoteNumber = new HashMap<>();
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        dictImportTextIdHelper = new DictImportTextIdHelper(getProject());
        epcSortNumberToPartListEntry = new TreeMap<>();
        moduleVariantsVisibleMap = new HashMap<>();
        currentFootnoteGroup = new ArrayList<>();
        partsDone = new HashSet<>();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EPCPartListImportHelper helper = new EPCPartListImportHelper(getProject(), DEST_TABLENAME);
        currentRecordNo = recordNo;

        if (checkIfAlreadyCreatedFromMAD(importRec)) {
            reduceRecordCount();
            return;
        }
        checkAndCreateNewAssembly(importRec);
        if (currentAssembly == null) {
            reduceRecordCount();
            return;
        }

        // Die komplette Teilenummer mit Sachkennbuchstaben
        String partNumber = getPartNumber(helper, importRec, getPartTypeFieldname(), PARTNUM, true);

        String omittedPart = getOmittedPartNumber(importRec);
        if (iPartsMigrationHelper.isOnlyOmmitedPartData(this, partNumber, omittedPart, currentRecordNo)) {
            lastCompletePartListEntryImportRec = null; // nur zur Sicherheit für etwaige Y-Teilepositionen
            return;
        }

        // Nur bei EPC kann es vorkommen, dass wir einen Datensatz ohne Teilenummer haben, der aber wie eine Position
        // mit Teilenummer behandelt werden soll. Um so einen Datensatz handelt es sich, wenn PARTNUM und PARTTYPE
        // "null" sind. So eine Position kann dementsprechend keine V oder Y Position sein (ist ja "null").
        boolean isPseudoPart = helper.isEPCNullValue(importRec, PARTNUM) && helper.isEPCNullValue(importRec, getPartTypeFieldname());
        boolean isVTextPos = !isPseudoPart && iPartsMigrationHelper.isVTextPartListEntry(helper.handleValueOfSpecialField(PARTNUM, importRec));
        boolean isYPartPos = !isPseudoPart && !isVTextPos && iPartsMigrationHelper.isYPartListEntry(helper.handleValueOfSpecialField(PARTNUM, importRec));
        boolean isXPartPos = !isPseudoPart && (StrUtils.countCharacters(partNumber.toUpperCase().trim(), 'X') == partNumber.trim().length());
        boolean isYTextPos = false;

        // Die EPC spezifischen Pseudo-Teile haben zwar keine Teilenummer, besitzen aber eine Teilebenennung. Daher
        // wird ein Pseudo-Teil erzeugt mit einer selbst erstellten ID bzw. Sachnummer:
        // Pseudo-Prefix + Modulnummer + Delimiter + TermId der Benennung
        String termIdPartDesc = helper.handleValueOfSpecialField(NOUNIDX, importRec);
        if (isPseudoPart) {
            if (StrUtils.isValid(termIdPartDesc)) {
                partNumber = helper.makeEPCPSeudoPartNumber(termIdPartDesc);
            }
        }

        String steeringAndGearboxValue = getSteeringAndGearboxValue(importRec);
        List<String> quantityValues = getQuantityValues(importRec);
        String shelfLife = getShelfLife(importRec, quantityValues);

        if (!isYPartPos || (lastCompletePartListEntryImportRec == null)) {
            if (isYPartPos) {
                isYTextPos = true;
                isYPartPos = false;
            }
            lastCompletePartListEntryImportRec = importRec;
            lastCompletePartListEntry.clear();
            currentCombinedTextSeqNo = 1;
            currentFootNotesForPartListEntry.clear();
        } else {
            String lastCompletePartListEntryPartNumber = getPartNumber(helper, lastCompletePartListEntryImportRec, getPartTypeFieldname(), PARTNUM, true);
            isVTextPos = iPartsMigrationHelper.isVTextPartListEntry(helper.handleValueOfSpecialField(PARTNUM, lastCompletePartListEntryImportRec));

            isYTextPos = iPartsMigrationHelper.isYPartListEntry(helper.handleValueOfSpecialField(PARTNUM, lastCompletePartListEntryImportRec));

            boolean isTextPos = isYTextPos || isVTextPos;
            importRec.put(getPartTypeFieldname(), "");
            importRec.put(PARTNUM, "");

            // Überprüfen, ob es sich um eine eigenständige Teileposition für die Y-Position handelt oder um weitere Attribute
            // zur vorhergehenden letzten vollständigen Teileposition
            boolean isYForNewPartPos = false;

            boolean isSteeringAndGearboxDifferent = false;

            if (!isVTextPos) { // Lenkung, Getriebe und Menge bei V-Textposition nicht importieren (bei Y-Textpositionen Lenkung und Getriebe wohl schon)
                // Ist Leknkung/Getriebe unterschiedlich?
                String steeringAndGearboxValueLastRec = getSteeringAndGearboxValue(lastCompletePartListEntryImportRec);
                isSteeringAndGearboxDifferent = iPartsMigrationHelper.isDifferentSteeringAndGearValue(steeringAndGearboxValue, steeringAndGearboxValueLastRec);
                if (isSteeringAndGearboxDifferent) {
                    isYForNewPartPos = true;
                }

                // Menge bei Y-Textposition nicht importieren
                if (!isYTextPos) {
                    // Sind echte Mengenangaben vorhanden?
                    boolean isYWithQuantity = iPartsMigrationHelper.hasRealQuantityValues(quantityValues);
                    if (isYWithQuantity) {
                        isYForNewPartPos = true;
                    }
                    // Falls der Y-Datensatz keine echten Mengenangaben hat, das gesamte Feld auf leer setzen, damit es vom lastCompletePartListEntryImportRec
                    // weiter unten übernommen wird
                    if (!isYWithQuantity) {
                        clearQuantityValue(importRec);
                    }
                }
            }

            if (isYForNewPartPos) {
                iPartsMigrationHelper.copyValuesFromPreviousDataset(importRec, lastCompletePartListEntryImportRec, isSteeringAndGearboxDifferent, getSteeringAndGearboxFieldname());
                partNumber = getPartNumber(helper, importRec, getPartTypeFieldname(), PARTNUM, true);
                steeringAndGearboxValue = getSteeringAndGearboxValue(importRec);
                quantityValues = getQuantityValues(importRec);
            } else {
                partNumber = lastCompletePartListEntryPartNumber;

                boolean hasSupportedYImportData = iPartsMigrationHelper.handleUnsupportedFields(this, isTextPos,
                                                                                                currentRecordNo,
                                                                                                getIndentValue(importRec),
                                                                                                helper.handleValueOfSpecialField(REPLFLG, importRec),
                                                                                                getPartNumber(helper, importRec, REPTYPE, REPPNO, true),
                                                                                                helper.handleValueOfSpecialField(REPPART, importRec),
                                                                                                getIndentValue(lastCompletePartListEntryImportRec),
                                                                                                getPartNumber(helper, lastCompletePartListEntryImportRec,
                                                                                                              getPartTypeFieldname(), PARTNUM, true));

                if (!isTextPos) { // SAA-Gültigkeiten nicht für Textpositionen erweitern
                    // SAA-Gültigkeiten in lastCompletePartListEntry erweitern
                    if (getImportType() == ImportType.IMPORT_BM_PARTS) {
                        iPartsMigrationHelper.handleSaaValidity(lastCompletePartListEntry, getSaaBkValidityValues(importRec, isTextPos));
                    }
                }

                // Ergänzungstext in lastCompletePartListEntry erweitern
                String addTextTermId = helper.handleValueOfSpecialField(DESCIDX, importRec);
                EtkMultiSprache additionalText = dictImportTextIdHelper.searchEPCTextWithEPCId(DictTextKindEPCTypes.ADD_TEXT,
                                                                                               addTextTermId);
                currentCombinedTextSeqNo = iPartsMigrationHelper.handleAddOrNeutralTextForYPartPosition(getProject(), this,
                                                                                                        additionalText,
                                                                                                        currentCombinedTextSeqNo,
                                                                                                        lastCompletePartListEntry);

                // Sprachneutralen Text in lastCompletePartListEntry erweitern
                String neutralTextFromData = helper.handleValueOfSpecialField(NEUTRAL, importRec);
                EtkMultiSprache neutralText = iPartsMigrationHelper.handleNeutralText(this, dictImportTextIdHelper,
                                                                                      neutralTextFromData, recordNo);
                currentCombinedTextSeqNo = iPartsMigrationHelper.handleAddOrNeutralTextForYPartPosition(getProject(), this,
                                                                                                        neutralText,
                                                                                                        currentCombinedTextSeqNo,
                                                                                                        lastCompletePartListEntry);

                // Fußnoten in lastCompletePartListEntry erweitern
                Set<iPartsFootNoteId> footNoteIds = handleFootNotes(importRec, isVTextPos);

                iPartsMigrationHelper.handleFootNotesForYPartPosition(footNoteIds, currentFootNotesForPartListEntry,
                                                                      lastCompletePartListEntry, footnotesHandler);

                // Den Code an den bereits vorhandenen Code anhängen
                iPartsMigrationHelper.handleCodeValueForYPartPosition(getCodes(importRec), lastCompletePartListEntry);

                if (!isVTextPos) { // Folgende Attribute nicht bei V-Textpositionen importieren (bei Y-Textpositionen wohl schon)
                    // Wahlweise-Teile
                    checkWWPartPosition(helper, lastCompletePartListEntryImportRec, importRec, partNumber, isVTextPos);

                    // Ersetzungen und Mitlieferteile in Y-Teileposition
                    boolean hasReplacement = helper.handleValueOfSpecialField(REPLFLG, lastCompletePartListEntryImportRec).equals("R");
                    if (hasReplacement) {
                        String replacePart = getPartNumber(helper, importRec, REPTYPE, REPPNO, true);
                        List<String> includeParts = helper.getAsArray(helper.handleValueOfSpecialField(REPPART, importRec),
                                                                      22, false, false);
                        iPartsMigrationHelper.handleReplacePartAndIncludePartsForYPartPosition(this,
                                                                                               lastCompletePartListEntry,
                                                                                               replacementsForAssembly,
                                                                                               replacePart, includeParts,
                                                                                               recordNo);
                    }
                }
                if (!hasSupportedYImportData) {
                    reduceRecordCount();
                }
                // Y-Teileposition ist durch!
                return;
            }
        }

        String hierarchyValue = getIndentValue(importRec);
        boolean isTextPos = (isVTextPos || isYTextPos) && !isPseudoPart; // Textposition = V- oder Y-Textposition (und keine Pseudo-Teil)

        // Ergänzungstext
        int localCurrentCombinedTextSeqNo = 1;
        int currentAdditionalTextSeqNo = localCurrentCombinedTextSeqNo;
        String addTextTermId = helper.handleValueOfSpecialField(DESCIDX, importRec);
        EtkMultiSprache additionalText = dictImportTextIdHelper.searchEPCTextWithEPCId(DictTextKindEPCTypes.ADD_TEXT, addTextTermId);
        if (additionalText != null) {
            localCurrentCombinedTextSeqNo++;
        }

        // Sprachneutraler Text
        int currentNeutralTextSeqNo = localCurrentCombinedTextSeqNo;
        String neutralTextFromData = helper.handleValueOfSpecialField(NEUTRAL, importRec);
        EtkMultiSprache neutralText = iPartsMigrationHelper.handleNeutralText(this, dictImportTextIdHelper, neutralTextFromData, recordNo);
        if (neutralText != null) {
            localCurrentCombinedTextSeqNo++;
        }

        // Nur bei einer echten Teileposition die Sequenznummer für die kombinierten Texte auf den lokalen Wert setzen,
        // da es bei Y-Positionen für eigenständige Stücklisteneinträge keine Ergänzungen von kombinierten Texten geben
        // kann und ansonsten die Sequenznummer für den letzten vollständigen Stücklisteneintrag fälschlicherweise wieder
        // auf 1 zurückgesetzt werden würde
        if (!isYPartPos) {
            currentCombinedTextSeqNo = localCurrentCombinedTextSeqNo;
        }


        // Fußnoten
        Set<iPartsFootNoteId> footNoteIds = handleFootNotes(importRec, isVTextPos);
        if ((footNoteIds != null) && !footNoteIds.isEmpty()) {
            currentFootNotesForPartListEntry.addAll(footNoteIds);
        }

        DBDataObjectList<EtkDataPartListEntry> destPartList = currentAssembly.getPartListUnfiltered(null, false, false);

        if (!isTextPos) { // Material nicht bei Textpositionen importieren
            partNumber = StrUtils.replaceSubstring(partNumber, " ", "");
            if (!isXPartPos) {
                helper.handlePartNumber(this, partNumber, termIdPartDesc, partsDone, shelfLife, isPseudoPart);
            }
            currentPartNumbersInAssembly.add(partNumber);
        } else {
            partNumber = ""; // Teilenummer bei Textpositionen auf leer setzen für virtuelles Material
        }

        // Wahlweise Teile
        boolean isWW = checkWWPartPosition(helper, importRec, importRec, partNumber, isVTextPos);

        iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA;
        if (!isTextPos) {
            quantityForModelOrSAA = getQuantityForModelOrSAA(importRec, quantityValues);
        } else {
            quantityForModelOrSAA = new iPartsMigrationHelper.QuantityForModelOrSAA();
            quantityForModelOrSAA.add("", "");
        }

        // Ermitteln der SAA/BK-Gültigkeit bei den Stücklisteneinträgen, nur Model-Partlist
        Set<String> saaBkValidity = getSaaBkValidityValues(importRec, isTextPos);
        if (saaBkValidity == null) {
            saaBkValidity = new LinkedHashSet<>();
        }

        String epcSeqNummer = helper.handleValueOfSpecialField(SEQNUM, importRec);
        // Für jede verschiedene Menge wird ein Datensatz geschrieben
        for (String currentQuantity : quantityForModelOrSAA.getQuantities()) {
            // bei Textpositionen gibt es nur genau eine Menge und die ist leer
            // X-Positionen sollen bei EPC explizit übernommen werden
            if (!isTextPos && currentQuantity.isEmpty() && !isXPartPos) {
                // Keine Quantity -> diese Baumuster sind nicht verbaut
                continue;
            }

            int destLfdNr = 0;
            for (EtkDataPartListEntry partListEntry : destPartList) {
                destLfdNr = Math.max(destLfdNr, Integer.valueOf(partListEntry.getAsId().getKLfdnr()));
            }
            destLfdNr++;

            PartListEntryId destPartListEntryId = new PartListEntryId(currentAssembly.getAsId().getKVari(), currentAssembly.getAsId().getKVer(),
                                                                      EtkDbsHelper.formatLfdNr(destLfdNr));

            EtkDataPartListEntry destPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), destPartListEntryId);
            destPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            List<EtkDataPartListEntry> partListEntriesForEPCSeqNum = epcSortNumberToPartListEntry.get(epcSeqNummer);
            if (partListEntriesForEPCSeqNum == null) {
                partListEntriesForEPCSeqNum = new DwList<>();
                epcSortNumberToPartListEntry.put(epcSeqNummer, partListEntriesForEPCSeqNum);
            }
            partListEntriesForEPCSeqNum.add(destPartListEntry);

            // K_SOURCE_GUID setzen
            destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_GUID,
                                            EditConstructionToRetailHelper.createNonDIALOGSourceGUID(destPartListEntryId),
                                            DBActionOrigin.FROM_EDIT);

            destPartListEntry.setFieldValue(FIELD_K_MENGE, currentQuantity, DBActionOrigin.FROM_EDIT);

            setPartlistEntryValidities(destPartListEntry, quantityForModelOrSAA, currentQuantity, isTextPos);

            checkSaaValidity(saaBkValidity, quantityForModelOrSAA, currentQuantity, isTextPos);
            boolean setFalseBestFlag = isTextPos || isXPartPos;
            iPartsMigrationHelper.setBestFlag(destPartListEntry, setFalseBestFlag, currentQuantity);
            // Positionsnummer (führende Nullen sollen entfernt werden)
            String posNr = getHotspotNumber(importRec);
            iPartsMigrationHelper.handleFieldRelatedToTextValue(destPartListEntry, saaBkValidity, partNumber, posNr,
                                                                isTextPos, isVTextPos, isXPartPos);

            if (isPseudoPart) {
                destPartListEntry.setFieldValue(FIELD_K_VIRTUAL_MAT_TYPE, VirtualMaterialType.PSEUDO_PART.getDbValue(), DBActionOrigin.FROM_EDIT);
            }

            if (!isTextPos && (getImportType() == ImportType.IMPORT_SA_PARTS)) {// SAA-Intervalle nicht bei Textpositionen importieren
                // Bei den SAA-Katalogen kann es wegen den Intervallen sein, dass die gleichen Positionen an mehreren Stellen kommen und nur wegen der Beschränkung auf
                // 10 Strichausführungen pro Intervall doppelt sind. Versuche diese Daten hier wieder zu verdichten
                if (iPartsMigrationHelper.handleSimiliarSaPartlistEntry(currentAssembly, destPartListEntry)) {
                    continue;
                }
            }

            if (!isVTextPos) {
                iPartsMigrationHelper.assignSteeringAndGearboxType(this, steeringAndGearboxValue, destPartListEntry,
                                                                   partNumber, getImportType() == ImportType.IMPORT_BM_PARTS);
                // Einrückzahl und Code zuweisen
                iPartsMigrationHelper.assignHierarchyValue(this, hierarchyValue, destPartListEntry, recordNo);
                if (getImportType() == ImportType.IMPORT_BM_PARTS) {
                    iPartsMigrationHelper.assignCodeValue(getCodes(importRec), destPartListEntry);
                }

                // Falls es sich um ein Wahlweise-Teil handelt, diesen Stücklisteneintrag entsprechend merken
                iPartsMigrationHelper.addPartListEntryIfWW(destPartListEntry, partNumber, currentWWPartNumbersToPLEntriesInAssembly, isWW);
                // Ersetungen und Mitlieferteile
                if (helper.handleValueOfSpecialField(REPLFLG, importRec).equals("R")) {
                    String replacePart = getPartNumber(helper, importRec, REPTYPE, REPPNO, true);
                    List<String> includeParts = helper.getAsArray(helper.handleValueOfSpecialField(REPPART, importRec),
                                                                  22, false, false);
                    iPartsMigrationHelper.handleReplacementAndIncludeParts(this, destPartListEntry,
                                                                           partNumber, replacePart, includeParts,
                                                                           replacementsForAssembly, recordNo);
                }
            }
            destPartList.add(destPartListEntry, DBActionOrigin.FROM_EDIT);
            iPartsMigrationHelper.finishSinglePartListEntry(this, getProject(), destPartListEntry, additionalText,
                                                            currentAdditionalTextSeqNo, neutralText, currentNeutralTextSeqNo,
                                                            isYPartPos, lastCompletePartListEntry, footNoteIds, footnotesHandler);

        }

    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            finishAssembly();
        }
        partsDone.clear();
        super.postImportTask();
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.NEUTRAL_TEXT));
    }

    protected boolean importFootNote(FootnoteObject footnote) {
        if ((((getImportType() == ImportType.IMPORT_BM_PARTS) && (footnote.getType() == EPCFootnoteType.MODEL))
             || ((getImportType() == ImportType.IMPORT_SA_PARTS) && (currentAssembly != null) && (footnote.getType() == EPCFootnoteType.SA)))) {
            String footNoteNumber = footnote.getFnNo();
            String tableFootNotenoteGroup = footnote.getGroupNo();
            String tableFootNotenoteSign = "";
            if (StrUtils.isValid(tableFootNotenoteGroup)) {
                String[] currentGroupSplittedValue = StrUtils.toStringArray(tableFootNotenoteGroup, "|", false);
                if (currentGroupSplittedValue.length == 1) {
                    tableFootNotenoteGroup = currentGroupSplittedValue[0];
                } else if (currentGroupSplittedValue.length == 2) {
                    tableFootNotenoteGroup = currentGroupSplittedValue[0];
                    tableFootNotenoteSign = currentGroupSplittedValue[1];
                }
            }
            if (!Utils.objectEquals(currentFootNoteNumber, footNoteNumber)) { // neue Fußnotennummer
                currentFootNoteNumber = footNoteNumber;
                currentFootNoteIsValid = true;
            } else if (!currentFootNoteIsValid) {
                // Falls der erste Satz nicht entsprechend aufgebaut ist, müssen der aktuelle Satz und alle weiteren Sätze zur
                // gleichen Fussnotennummer überlesen werden, wobei Tabellenfußnoten trotzdem sauber beendet werden müssen
                if (currentFootNoteIsTable) {
                    if (tableFootNotenoteSign.equals("Z")) {
                        currentFootNoteIsTable = false;
                    }
                }
                return true;
            }

            boolean isFootNoteForProduct = getImportType() == ImportType.IMPORT_BM_PARTS;
            iPartsFootNoteId footNoteIdReal;
            if (isFootNoteForProduct) {
                String kg = footnote.getKgNo();
                footNoteIdReal = footnotesHandler.getFootNoteIdForProduct(kg, footNoteNumber, footnote.productNo);
            } else {
                String saNumber = footnote.getSaNo();
                footNoteIdReal = footnotesHandler.getFootNoteIdForSA(footNoteNumber, saNumber);
            }

            // Marker, ob es eine Farb-Tabellenfußnote ist (Fußnotennummern >= 900, 3-stellig und erstes Zeichen ist 9 bis Z);
            boolean isColorTablefootnote = footnotesHandler.isColorFootnote(footNoteNumber);

            if (currentFootNoteIsTable && !currentTableFootNoteId.equals(footNoteIdReal)) {
                // Wechsel der Fußnotennummer ist nur in Farbfußnoten erlaubt. Hier ist so ein Beispiel, welches eigentlich nicht vorkommen darf
                getMessageLog().fireMessage(translateForLog("!!Unterschiedliche Tabellenfußnoten \"%1\" und \"%2\"," +
                                                            " die zur gleichen EPC Fußnotengruppe \"%3\" gehören.",
                                                            currentTableFootNoteId.getFootNoteId(),
                                                            footNoteIdReal.getFootNoteId(), tableFootNotenoteGroup),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }

            DictTextKindEPCTypes textKindForSearch = isFootNoteForProduct ? DictTextKindEPCTypes.MODEL_FOOTNOTE : DictTextKindEPCTypes.SA_FOOTNOTE;
            EtkMultiSprache text = dictImportTextIdHelper.searchEPCTextWithEPCId(textKindForSearch, footnote.getTextId());
            if (text == null) {
                text = new EtkMultiSprache();
            }
            String placeholder = footnote.getPlaceholderSign();
            boolean isNumberFootNote = placeholder.contains("#");
            // deutschen Text bei kurzer Fußnote bestimmen
            iPartsFootNoteContentId footNoteContentId = new iPartsFootNoteContentId(footNoteIdReal.getFootNoteId(), EtkDbsHelper.formatLfdNr(1));
            if (isNumberFootNote) {
                footnotesHandler.addResponseFootNoteContentToMap(footNoteIdReal, footNoteContentId);
            }

            String textDE = text.getText(Language.DE.getCode());

            footnotesHandler.handleCrossRefFootnotes(footNoteIdReal, footnote.getProductNo(), footnote.getKgNo(), footnote.getSaNo(), textDE, isFootNoteForProduct);
            // Zeile ggf. als Rückmeldedaten behandeln
            List<String> footNoteLines = StrUtils.toStringList(textDE, "\n", false, false);
            for (String line : footNoteLines) {
                footnotesHandler.isFootNoteLineHandledAsResponseData(line, footNoteContentId);
            }

            boolean currentFootNoteWasColorTable = currentFootNoteIsColorTable;
            boolean anyTableFootnote = currentFootNoteIsTable || currentFootNoteIsColorTable;
            if (anyTableFootnote) {
                // Eigentlich endet eine Tabellenfußnote mit "Z", manchmal aber auch gar nicht oder falsch mit "S"
                // Bei S wird jetzt einfach davon ausgegangen, dass eine neue Tabelle anfangen soll
                // Wenn die Tabellenfußnote gar nicht endet, dann beginnt in der Regel eine neue TU
                // Deahalb wird diese Variable auch im finishAssembly zurückgesetzt
                if (tableFootNotenoteSign.equals("Z")) {
                    if (currentFootNoteIsColorTable) {
                        // Wenn die aktuelle Farb-Tabellenfußnote zu ende ist, dann leg sie in die Map mit allen
                        // aktuellen Farb-Tabellenfußnoten ab. Sortiert wird in der Map nach der kleinsten
                        // Fußnotennummer in einer Farb-Tabellenfußnote
                        String firstFootnoteNumberinTable = currentColorTablefootnote.getFirstFootnotenumberInTable();
                        List<MadTal4XABaseImporter.ColortTablefootnote> colorTablefootnotesWithSameStartNumber = allCurrentColorTablefootnotes.get(firstFootnoteNumberinTable);
                        if (colorTablefootnotesWithSameStartNumber == null) {
                            colorTablefootnotesWithSameStartNumber = new ArrayList<>();
                            allCurrentColorTablefootnotes.put(firstFootnoteNumberinTable, colorTablefootnotesWithSameStartNumber);
                        }
                        colorTablefootnotesWithSameStartNumber.add(currentColorTablefootnote);
                    } else if (currentFootNoteIsTable) {
                        currentFootnoteGroup.add(footNoteIdReal);
                        for (iPartsFootNoteId footnoteId : currentFootnoteGroup) {
                            Set<List<iPartsFootNoteId>> allGroupsForFootnoteId = allFootnoteGroupsForFootnoteNumber.get(footnoteId);
                            if (allGroupsForFootnoteId == null) {
                                allGroupsForFootnoteId = new LinkedHashSet<>();
                                allFootnoteGroupsForFootnoteNumber.put(footnoteId, allGroupsForFootnoteId);
                            }
                            allGroupsForFootnoteId.add(currentFootnoteGroup);
                        }
                        currentFootnoteGroup = new ArrayList<>();
                    }
                    currentFootNoteIsTable = false;
                    currentFootNoteIsColorTable = false;
                }
            }

            // Es startet eine Tabellenfußnote. Die FußnotenID wird sich gemerkt und mit dieser weitergearbeitet
            if (tableFootNotenoteSign.equals("S")) {
                if (isColorTablefootnote) {
                    currentFootNoteIsColorTable = true;
                    currentFootNoteWasColorTable = true;
                    // Es startet eine neue Farb-Tabellenfußnote
                    currentColorTablefootnote = new MadTal4XABaseImporter.ColortTablefootnote(footnotesHandler);
                } else {
                    if (currentFootNoteIsTable) {
                        getMessageLog().fireMessage(translateForLog("!!Tabellenfußnote \"%1\" hat kein gültiges Ende in Record %2",
                                                                    currentTableFootNoteId.getFootNoteId(), Long.toString(currentRecordNo)),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }
                    currentFootNoteIsTable = true;
                    currentTableFootNoteId = footNoteIdReal;
                }
                currentFootNoteTableGroup = tableFootNotenoteGroup;

            }
            if (currentFootNoteIsTable) {
                currentFootnoteGroup.add(footNoteIdReal);
            }

            // Übernehme die ID Farb-Tabellenfußnote nur, wenn sie gerade anfängt.
            if (currentFootNoteWasColorTable && tableFootNotenoteGroup.equals(currentFootNoteTableGroup)) {
                currentColorTablefootnote.addSingleFootnoteId(footNoteIdReal);
            }

            if (anyTableFootnote && tableFootNotenoteSign.equals("Z")) {
                currentFootNoteTableGroup = "NO_GROUP";
            }
            footnotesHandler.addToCurrentFootnoteMapIfNotExists(footNoteIdReal, footNoteNumber);

            // Fußnoteninhalt erzeugen und speichern
            iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(getProject(), footNoteIdReal, EtkDbsHelper.formatLfdNr(1));
            dataFootNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataFootNoteContent.setFieldValueAsMultiLanguage(FIELD_DFNC_TEXT, text, DBActionOrigin.FROM_EDIT);
            saveToDB(dataFootNoteContent, false);

            return true;
        }

        return false;
    }

    private boolean checkWWPartPosition(EPCPartListImportHelper helper, Map<String, String> importRec,
                                        Map<String, String> importRecWithWWParts, String partNumber, boolean isVTextPos) {
        if (!isVTextPos) {
            String wwFlag = helper.handleValueOfSpecialField(OPTFLAG, importRec);
            String wwPartsAsString = helper.handleValueOfSpecialField(OPTPART, importRecWithWWParts);
            List<String> unformattedWWParts = helper.getAsArray(wwPartsAsString, 20, false, false);
            return iPartsMigrationHelper.handleWWSets(partNumber, wwFlag, unformattedWWParts, currentWWPartNumbersToWWPartNumbersInAssembly);
        }
        return false;
    }

    /**
     * Speichert die bisherige Assembly. Dabei werden Moduldaten, Wahlweiseinformationen und Ersetzungen angelegt
     */
    protected void finishAssembly() {
        if (currentAssembly != null) {
            // Metadaten
            iPartsMigrationHelper.handleModuleMetaData(currentAssembly, moduleVariantsVisibleMap);
            // Wahlweise
            iPartsMigrationHelper.handleWWSetsForAssembly(this, currentAssembly,
                                                          currentWWPartNumbersToWWPartNumbersInAssembly,
                                                          currentPartNumbersInAssembly, currentWWPartNumbersToPLEntriesInAssembly);

            // Nachfolger und Mitlieferteile für Ersetzungen suchen und in DB abspeichern
            iPartsMigrationHelper.handleReplacementAndIncludePartsForAssembly(getProject(), this, currentAssembly, replacementsForAssembly);

            // Die Sequenznummer wird von EPC vorgegeben, daher kann sie erst gesetzt werden, wenn die Assembly fertig ist
            String destSeqNr = SortBetweenHelper.getFirstSortValue();
            for (List<EtkDataPartListEntry> partListEntryListForSameEPCSeqNum : epcSortNumberToPartListEntry.values()) {
                for (EtkDataPartListEntry partListEntry : partListEntryListForSameEPCSeqNum) {
                    // Sequenznummer setzen
                    partListEntry.setFieldValue(EtkDbConst.FIELD_K_SEQNR, destSeqNr, DBActionOrigin.FROM_EDIT);
                    destSeqNr = SortBetweenHelper.getNextSortValue(destSeqNr);
                }
            }
            // Restliche Aufräumarbeiten
            storeFinishedAssembly();
        }

        currentPartNumbersInAssembly.clear();
        currentWWPartNumbersToWWPartNumbersInAssembly.clear();
        currentWWPartNumbersToPLEntriesInAssembly.clear();
        replacementsForAssembly.clear();
        lastCompletePartListEntryImportRec = null;
        lastCompletePartListEntry.clear();
        epcSortNumberToPartListEntry.clear();
    }

    protected String getPartNumber(EPCPartListImportHelper helper, Map<String, String> importRec, String typeField,
                                   String numberField, boolean removeBlanks) {
        String partNumberSign = helper.handleValueOfSpecialField(typeField, importRec);
        if (StrUtils.isValid(partNumberSign)) {
            String partNumber = helper.handleValueOfSpecialField(numberField, importRec);
            if (StrUtils.isValid(partNumber)) {
                if (removeBlanks) {
                    return (partNumberSign + partNumber).replace(" ", "");
                } else {
                    return (partNumberSign + partNumber).trim();
                }
            }
        }
        return "";
    }

    protected boolean deleteAssemblyIfExists(EtkDataAssembly assembly) {
        boolean oldModuleHidden = false;
        if (assembly.existsInDB()) {
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                oldModuleHidden = iPartsAssembly.getModuleMetaData().getFieldValueAsBoolean(FIELD_DM_MODULE_HIDDEN);
                getModuleVariantsVisibleMap().put(new iPartsModuleId(iPartsAssembly.getAsId().getKVari()), iPartsAssembly.getModuleMetaData().isVariantsVisible());
                iPartsAssembly.delete_iPartsAssembly(true);
            }
        }
        return oldModuleHidden;
    }

    protected void clearPartsDoneCache() {
        partsDone.clear();
    }

    protected ImportType getImportType() {
        return importType;
    }

    public Map<iPartsProductId, Boolean> getProductRelevanceCache() {
        return productRelevanceCache;
    }

    protected int getCurrentRecordNo() {
        return currentRecordNo;
    }

    protected iPartsDataAssembly getCurrentAssembly() {
        return currentAssembly;
    }

    protected void setCurrentAssembly(iPartsDataAssembly currentAssembly) {
        this.currentAssembly = currentAssembly;
    }

    protected Map<iPartsModuleId, Boolean> getModuleVariantsVisibleMap() {
        return moduleVariantsVisibleMap;
    }

    protected iPartsMigrationFootnotesHandler getFootnotesHandler() {
        return footnotesHandler;
    }

    protected Map<String, List<MadTal4XABaseImporter.ColortTablefootnote>> getAllCurrentColorTablefootnotes() {
        return allCurrentColorTablefootnotes;
    }

    protected Map<iPartsFootNoteId, Set<List<iPartsFootNoteId>>> getAllFootnoteGroupsForFootnoteNumber() {
        return allFootnoteGroupsForFootnoteNumber;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected HashMap<String, String> initMapping() {
        return new HashMap<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    protected abstract void checkSaaValidity(Set<String> saaBkValidity, iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA, String currentQuantity, boolean isTextPos);

    protected abstract String getHotspotNumber(Map<String, String> importRec);

    protected abstract void setPartlistEntryValidities(EtkDataPartListEntry destPartListEntry, iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA, String quantityValues, boolean isTextPos);

    protected abstract Set<String> getSaaBkValidityValues(Map<String, String> importRec, boolean isTextPos);

    protected abstract iPartsMigrationHelper.QuantityForModelOrSAA getQuantityForModelOrSAA(Map<String, String> importRec, List<String> quantityValues);

    protected abstract Set<iPartsFootNoteId> handleFootNotes(Map<String, String> importRec, boolean isVTextPos);

    protected abstract String getCodes(Map<String, String> importRec);

    protected abstract String getIndentValue(Map<String, String> importRec);

    protected abstract String getSteeringAndGearboxFieldname();

    protected abstract void clearQuantityValue(Map<String, String> importRec);

    protected abstract List<String> getQuantityValues(Map<String, String> importRec);

    protected abstract String getShelfLife(Map<String, String> importRec, List<String> quantityValues);

    protected abstract String getSteeringAndGearboxValue(Map<String, String> importRec);

    protected abstract boolean checkIfAlreadyCreatedFromMAD(Map<String, String> importRec);

    protected abstract void checkAndCreateNewAssembly(Map<String, String> importRec);

    protected abstract void storeFinishedAssembly();

    protected abstract String getOmittedPartNumber(Map<String, String> importRec);

    protected abstract String getPartTypeFieldname();

    protected class EPCPartListImportHelper extends EPCImportHelper {

        public EPCPartListImportHelper(EtkProject project, String tableName) {
            super(project, new HashMap<String, String>(), tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }
            // Eine sortierbare Sequenznummer aus der Vorgabe machen
            if (sourceField.equals(SEQNUM)) {
                value = Utils.toSortString(value);
            }
            return value;
        }

        public String makeEPCPSeudoPartNumber(String termIdPartDesc) {
            return VirtualMaterialType.PSEUDO_PART.getDbValue() + "_" + currentAssembly.getAsId().getKVari() + iPartsNumberHelper.PSEUDO_PART_POSITION_DELIMITER + termIdPartDesc;
        }
    }

    class FootnoteObject {

        private String productNo;
        private String kgNo;
        private String saNo;
        private String fnNo;
        private String textId;
        private String groupNo;
        private String placeholderSign;
        private EPCFootnoteType type;

        public FootnoteObject(String productNo, String kgNo, String fnNo, String textId, String groupNo, String placeholderSign) {
            this.productNo = productNo;
            this.kgNo = kgNo;
            this.fnNo = fnNo;
            this.textId = textId;
            this.groupNo = groupNo;
            this.placeholderSign = placeholderSign;
            this.type = EPCFootnoteType.MODEL;
        }

        public FootnoteObject(String saNo, String fnNo, String textId, String groupNo, String placeholderSign) {
            this.saNo = saNo;
            this.fnNo = fnNo;
            this.textId = textId;
            this.groupNo = groupNo;
            this.placeholderSign = placeholderSign;
            this.type = EPCFootnoteType.SA;
        }

        public String getProductNo() {
            return productNo;
        }

        public String getKgNo() {
            return kgNo;
        }

        public String getFnNo() {
            return fnNo;
        }

        public String getTextId() {
            return textId;
        }

        public String getGroupNo() {
            return groupNo;
        }

        public String getPlaceholderSign() {
            return placeholderSign;
        }

        public String getSaNo() {
            return saNo;
        }

        public EPCFootnoteType getType() {
            return type;
        }
    }
}
