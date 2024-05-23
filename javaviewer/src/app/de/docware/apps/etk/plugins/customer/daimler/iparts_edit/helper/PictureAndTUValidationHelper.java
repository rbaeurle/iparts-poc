/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataConfirmChanges;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataConfirmChangesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderEditHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.ChangeSetShowTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerUtils;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sort.SortUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hilfsklasse zur Überprüfung eines TU's nach DAIMLER-8699
 * 1. offener Bildauftrag zum Autorenauftrag => Fehler
 * 2. Bild hat Hotspot ohne Teilepos => Hinweis
 * 3. Teilepos hat Hotspot ohne Bild => Hinweis
 * 4. leerer TU (kein Bild, keine Stückliste) => Fehler
 * 5. nur Bild im TU (Bild aber keine Stückliste => Hinweis
 * Hinzukommt in DAIMLER-16242
 * 6. TU ist ausgeblendet => Hinweis
 */
public class PictureAndTUValidationHelper {

    private final EditModuleFormIConnector editConnector;
    private final EtkMessageLogFormHelper messageLogHelper;
    private AssemblyId assemblyId;
    private final PictureAndTUValidationEntryList validationEntries;
    private final boolean simplifiedQualityCheck;

    public PictureAndTUValidationHelper(EditModuleFormIConnector editConnector, EtkMessageLogFormHelper messageLogHelper,
                                        boolean simplifiedQualityCheck) {
        this.editConnector = editConnector;
        this.messageLogHelper = messageLogHelper;
        this.validationEntries = new PictureAndTUValidationEntryList();
        this.simplifiedQualityCheck = simplifiedQualityCheck;
    }

    public EditModuleFormIConnector getConnector() {
        return editConnector;
    }

    public EtkProject getProject() {
        return getConnector().getProject();
    }

    public AssemblyId getAssemblyId() {
        if (assemblyId == null) {
            assemblyId = getConnector().getCurrentAssembly().getAsId();
        }
        return assemblyId;
    }

    protected void fireMessage(String message) {
        if (messageLogHelper != null) {
            messageLogHelper.fireMessage(message);
        }
    }

    protected void fireProgress() {
        if (messageLogHelper != null) {
            messageLogHelper.fireProgress();
        }
    }

    public int getNumberOfChecks() {
        // Es werden 7 Prüfungen (fireProgress()) durchgeführt
        return 7;
    }

    /**
     * Start der Überprüfung
     *
     * @return {@link PictureAndTUValidationEntryList} mit der Liste der Ergebnisse
     */
    public PictureAndTUValidationEntryList startPictureAndTUValidation() {
        assemblyId = editConnector.getCurrentAssembly().getAsId();
        validationEntries.clear();

        // Prüfung vom TU
        fireMessage("!!Prüfung des Technischen Umfangs");
        boolean isPartListEmpty = isPartListEmpty();
        boolean arePicturesInTU = arePicturesInTU();
        if (isPartListEmpty) {
            if (!arePicturesInTU) {
                validationEntries.addErrorT(getAssemblyId(), "!!Leerer TU (kein Bild, keine Stückliste)");
            } else {
                validationEntries.addWarningT(getAssemblyId(), "!!TU hat Bilder, aber keine Stückliste");
            }
        } else {
            if (!arePicturesInTU) {
                validationEntries.addErrorT(getAssemblyId(), "!!TU hat kein Bild");
            }
        }
        boolean isModuleHidden = isModuleHidden();
        if (isModuleHidden) {
            validationEntries.addWarningT(getAssemblyId(), "!!TU ist ausgeblendet");
        }
        fireProgress();

        // Offene Bestätigungen
        if (!simplifiedQualityCheck) {
            executeOpenConfirmationsForChangeSetCheck();
        }
        fireProgress();

        // Offene Bildaufträge
        Map<IdWithType, String> openPicOrders = isAnOpenPicOrderInAuthorOrder();
        for (IdWithType key : openPicOrders.keySet()) {
            validationEntries.addError(key, openPicOrders.get(key), TranslationHandler.translate("!!Offener Bildauftrag zum Autorenauftrag"));
        }
        fireProgress();

        // Mehrere Bildtafeln zu einer MC Nummer
        Map<String, Set<String>> picsWithSameMCNumber = getPicturesWithSameMCNumber();
        if ((picsWithSameMCNumber != null) && !picsWithSameMCNumber.isEmpty()) {
            picsWithSameMCNumber.forEach((mcNumber, pics) ->
                                                 validationEntries.addError(assemblyId, mcNumber, TranslationHandler.translate("!!Mehrere Bildtafeln zum gleichen Mediencontainer vorhanden"),
                                                                            String.join("\n", pics)));
        }
        fireProgress();

        // Bilder mit Hotspots ohne Teileposition
        EditHotSpotHelper editHotSpotHelper = new EditHotSpotHelper(getConnector());
        Map<IdWithType, List<String>> hotspotWithoutPosresult = isAHotspotWithoutPos(editHotSpotHelper);
        for (IdWithType key : hotspotWithoutPosresult.keySet()) {
            for (String hotSpot : hotspotWithoutPosresult.get(key)) {
                validationEntries.addErrorT(key, "!!Bild hat Hotspot ohne Teileposition (%1)", hotSpot);
            }
        }
        fireProgress();

        doPartListEntryChecks(editHotSpotHelper, validationEntries);
        fireProgress();

        if (arePicturesInTU) {
            // DAIMLER-15289: Qualitätsprüfung um Code-Durchgängigkeit an Bildtafel erweitern
            doCheckPatency();
        }
        fireProgress();
        return validationEntries;
    }

    /**
     * Liefert die Bildtafeln aus der Stückliste, die zur gleichen MC Nummer gehören (MediaContainer aus AS-PLM).
     *
     * @return
     */
    private Map<String, Set<String>> getPicturesWithSameMCNumber() {
        // Bildtafeln an der Stückliste
        DBDataObjectList<EtkDataImage> imagesFromCurrentAssembly = getConnector().getCurrentAssembly().getUnfilteredImages();
        if ((imagesFromCurrentAssembly != null) && !imagesFromCurrentAssembly.isEmpty()) {
            fireMessage("!!Prüfung der Bildtafeln zum gleichen Mediencontainer");
            // MC Nummer für alle Bilder bestimmen
            iPartsPicOrderEditHelper.addMCNumbersToPictures(getProject(), imagesFromCurrentAssembly);
            Map<String, Set<String>> mcNumberAndPicNumbers = new HashMap<>();
            // Alle Bilder durchlaufen und sie nach ihren MC Nummern gruppieren
            imagesFromCurrentAssembly.forEach(image -> {
                String mcNumber = iPartsPicOrderEditHelper.getMediaContainerFromImage(image);
                String picNumber = image.getImagePoolNo();
                String picNumberVer = image.getImagePoolVer();
                if (StrUtils.isEmpty(picNumberVer)) {
                    picNumberVer = " - ";
                }
                if (StrUtils.isValid(mcNumber, picNumber)) {
                    Set<String> picNumbers = mcNumberAndPicNumbers.computeIfAbsent(mcNumber, k -> new TreeSet<>());
                    picNumbers.add(TranslationHandler.translate("!!Bildtafel: %1, Version: %2", picNumber, picNumberVer));
                }
            });
            // Nur die Einträge zurückliefern, bei denen es zu einer MC Nummer mehrere Bildtafeln gibt
            return mcNumberAndPicNumbers.entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return null;
    }

    /**
     * Führt die Prüfungen für die Teilepositionen in der Stückliste durch
     *
     * @param editHotSpotHelper
     * @param validationEntries
     */
    protected void doPartListEntryChecks(EditHotSpotHelper editHotSpotHelper, PictureAndTUValidationEntryList validationEntries) {
        RunTimeLogger runTimeLogger = new RunTimeLogger(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK);
        runTimeLogger.setStartTime();

        // Prüfung der Teilepositionen
        Map<String, Set<String>> posHierachiesMap = new HashMap<>();
        Map<String, List<PartListEntryId>> posPartListEntryIdMap = new HashMap<>();
        fillPosToPartListEntryIdsAndHierachiesMaps(posPartListEntryIdMap, posHierachiesMap);
        Map<EditHotSpotHelper.KEY_FAULTY_POS_MAP, List<String>> faultyPos = isAFaultyPos(editHotSpotHelper);
        fireMessage("!!Prüfung der Teilepositionen der Stückliste");
        for (EditHotSpotHelper.KEY_FAULTY_POS_MAP key : faultyPos.keySet()) {
            for (String pos : faultyPos.get(key)) {
                List<PartListEntryId> partList = posPartListEntryIdMap.get(pos);
                if (!partList.isEmpty()) {
                    for (PartListEntryId partListEntryId : partList) {
                        // DAIMLER-15137
                        if (key == EditHotSpotHelper.KEY_FAULTY_POS_MAP.POSNO_WITHOUT_HOTSPOTS) {
                            validationEntries.addErrorT(partListEntryId, key.getErrorMsg(), pos);
                        } else {
                            validationEntries.addWarningT(partListEntryId, key.getErrorMsg(), pos);
                        }
                    }
                } else {
                    validationEntries.addWarningT(getAssemblyId(), key.getErrorMsg(), pos);
                }
            }
        }

        EtkDataAssembly currentAssembly = editConnector.getCurrentAssembly();
        if (!simplifiedQualityCheck) {
            // Prüfung auf Hotspots von Teilepositionen mit unterschiedlichen Strukturstufen
            StringBuilder hierachiesForWarning = new StringBuilder();
            List<String> keyList = new DwList<>(posHierachiesMap.keySet());
            if (!keyList.isEmpty()) {
                SortUtils.sortList(keyList, false, true, true);
                for (String hotSpot : keyList) {
                    if (posHierachiesMap.get(hotSpot).size() > 1) {
                        if (hierachiesForWarning.length() != 0) {
                            hierachiesForWarning.append(",");
                        }
                        hierachiesForWarning.append(hotSpot);
                    }
                }
            }

            if (hierachiesForWarning.length() != 0) {
                String hotSpotString = "!!Hotspot";
                if (hierachiesForWarning.length() > 1) {
                    hotSpotString = "!!Hotspots";
                }
                validationEntries.addWarningT(getAssemblyId(), "!!Strukturfehler - %1 (%2) mit unterschiedlichen Strukturstufen",
                                              TranslationHandler.translate(hotSpotString), hierachiesForWarning.toString());
            }

            if ((currentAssembly instanceof iPartsDataAssembly) && (((iPartsDataAssembly)currentAssembly).getDocumentationType() == iPartsDocumentationType.DIALOG_IPARTS)) {
                doSuppressedPLEsCheck(validationEntries);
            }

            doInvalidFactoryDataCheck(validationEntries);
            checkCombTextForHotSpots(posPartListEntryIdMap, validationEntries);
        }
        runTimeLogger.stopTimeAndStore();
        runTimeLogger.logRunTime(currentAssembly.getAsId().getKVari() + ": Parts list entries checks (picture/TU) in");
    }

    private void doSuppressedPLEsCheck(PictureAndTUValidationEntryList validationEntries) {
        // DAIMLER-15823: Qualitätsprüfung: Unterdrückte Teilepos mit ET-KZ = E als Fehler ausgeben
        for (EtkDataPartListEntry partListEntry : editConnector.getCurrentPartListEntries()) {
            if (partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT) && partListEntry.getPart().getFieldValue(iPartsConst.FIELD_M_ETKZ).equals("E")) {
                String kEtkz = partListEntry.getFieldValue(iPartsConst.FIELD_K_ETKZ);
                if (!kEtkz.equals("K") && !kEtkz.equals("KB")) {
                    validationEntries.addError(partListEntry.getAsId(), TranslationHandler.translate("!!Teileposition ist unterdrückt, obwohl das Teil Ersatzteil ist"));
                }
            }
        }
    }

    private void doInvalidFactoryDataCheck(PictureAndTUValidationEntryList validationEntries) {
        for (EtkDataPartListEntry partListEntry : editConnector.getCurrentPartListEntries()) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsFactoryData factoryData = ((iPartsDataPartListEntry)partListEntry).getFactoryDataForRetailUnfiltered();
                if (factoryData != null) {
                    boolean validFactoryDataFound;
                    if (factoryData.hasValidFactories()) {
                        validFactoryDataFound = false;
                        factoryDataLoop:
                        for (List<iPartsFactoryData.DataForFactory> factoryDataList : factoryData.getFactoryDataMap().values()) {
                            for (iPartsFactoryData.DataForFactory dataForFactory : factoryDataList) {
                                // Wenn PEM-Datum-ab/bis ungültig sind oder sich unterscheiden, dann sind die Werksdaten
                                // für diese Prüfung gültig
                                if (!dataForFactory.hasValidDateFrom() || !dataForFactory.hasValidDateTo() || (dataForFactory.dateFrom != dataForFactory.dateTo)) {
                                    validFactoryDataFound = true;
                                    break factoryDataLoop;
                                }
                            }
                        }
                    } else {
                        // Ohne echte Werksdaten ist die Prüfung gültig, wenn es keine Rohdaten mit PEM-Datum-ab/bis unendlich gibt
                        validFactoryDataFound = !factoryData.hasFactoryDataWithInfiniteDates();
                    }

                    if (!validFactoryDataFound) {
                        validationEntries.addWarning(partListEntry.getAsId(), TranslationHandler.translate("!!Teileposition enthält nur ungültige Werksdaten"));
                    }
                }
            }
        }
    }

    /**
     * DAIMLER-15434: Qualitätsprüfung um gleiche Ergänzungstexte mit unterschiedlicher ID erweitern
     *
     * @param posPartListEntryIdMap
     * @param validationEntries
     */
    protected void checkCombTextForHotSpots(Map<String, List<PartListEntryId>> posPartListEntryIdMap, PictureAndTUValidationEntryList validationEntries) {
        // alle kombinierten Texte zum TU laden
        Map<PartListEntryId, iPartsDataCombText> combTextMap = getCombTextMap();
        if (combTextMap.isEmpty()) {
            // es werden keine kombinierten Texte verwendet => keine Prüfung nötig
            return;
        }

        StringBuilder str = new StringBuilder();
        // über alle nach Hotspot sortierten TeilePos
        for (Map.Entry<String, List<PartListEntryId>> entry : posPartListEntryIdMap.entrySet()) {
            String hotspot = entry.getKey();
            List<PartListEntryId> partListEntryIdList = entry.getValue();
            // gibt es zu diesem Hotspot mehr als eine TeilePos
            if (Utils.isValid(partListEntryIdList) && (partListEntryIdList.size() > 1)) {
                // Map pleId zu textId aufbauen
                Map<PartListEntryId, String> partListEntryCombTextMap = buildPartListEntryCombTextMap(partListEntryIdList, combTextMap);
                // gibt es mehr als eine verwendete TextId?
                if (partListEntryCombTextMap.size() > 1) {
                    // Erster Test: Anzahl der unterschiedlichen TextIds bestimmen
                    Set<String> firstCheckSet = new TreeSet<>(partListEntryCombTextMap.values());
                    // wird mehr als eine unterschiedliche TextId verwendet?
                    if (firstCheckSet.size() > 1) {
                        // weitere Prüfung
                        // Vorbereitung auf Prüfung über mehrere Sprachen
                        List<String> langToCheck = new DwList<>();
                        langToCheck.add(getProject().getDBLanguage());
                        for (String lang : langToCheck) {
                            Map<String, Set<String>> secondCheckMap = new HashMap<>();
                            Map<String, List<PartListEntryId>> secondHelpMap = new HashMap<>();
                            // Map Text zu pleId und Map Text zu TextId aufbauen
                            // dabei wird automatisch überprüft, ob es zum gleichen Text mehr als eine TextId gibt (somethingFound == true)
                            boolean somethingFound = makeSecondCombTextForHotSpotsCheck(partListEntryIdList, lang, combTextMap, secondCheckMap, secondHelpMap);
                            if (somethingFound) {
                                // Warnung zusammenbauen
                                buildInfoForCombTextForHotSpotsCheck(str, hotspot, lang, combTextMap, secondCheckMap, secondHelpMap);
                            }
                        }
                    }
                }
            }
        }
        if (str.toString().length() > 0) {
            // Warnung ausgeben
            validationEntries.addWarning(getAssemblyId(), "", TranslationHandler.translate("!!Unterschiedliche Lexikon-Einträge bei Kombinierten Texten verwendet"), str.toString());
        }
    }

    /**
     * Map <PartListEntryId, TextId> zu den übergebenen partListEntryIds aufbauen
     *
     * @param partListEntryIdList
     * @param combTextMap
     * @return
     */
    private Map<PartListEntryId, String> buildPartListEntryCombTextMap(List<PartListEntryId> partListEntryIdList, Map<PartListEntryId, iPartsDataCombText> combTextMap) {
        Map<PartListEntryId, String> partListEntryCombTextMap = new HashMap<>();
        // Map pleId zu textId aufbauen
        for (PartListEntryId partListEntryId : partListEntryIdList) {
            iPartsDataCombText dataCombText = combTextMap.get(partListEntryId);
            if (dataCombText != null) {
                String combTextId = dataCombText.getAttribute(iPartsConst.FIELD_DCT_DICT_TEXT).getMultiLanguageTextId();
                if (StrUtils.isValid(combTextId)) {
                    partListEntryCombTextMap.put(partListEntryId, combTextId);
                }
            }
        }
        return partListEntryCombTextMap;
    }

    /**
     * Map Text zu pleId und Map Text zu TextId aufbauen
     *
     * @param partListEntryIdList
     * @param lang
     * @param combTextMap
     * @param secondCheckMap
     * @param secondHelpMap
     * @return
     */
    private boolean makeSecondCombTextForHotSpotsCheck(List<PartListEntryId> partListEntryIdList, String lang,
                                                       Map<PartListEntryId, iPartsDataCombText> combTextMap,
                                                       Map<String, Set<String>> secondCheckMap, Map<String, List<PartListEntryId>> secondHelpMap) {
        // Map Text zu pleId und Map Text zu TextId aufbauen
        // dabei wird automatisch überprüft, ob es zum gleichen Text mehr als eine TextId gibt (somethingFound == true)
        boolean somethingFound = false;
        for (PartListEntryId partListEntryId : partListEntryIdList) {
            iPartsDataCombText dataCombText = combTextMap.get(partListEntryId);
            // besitzt die TeilePos einen kombinierten Text
            if (dataCombText != null) {
                String text = dataCombText.getTextValue(iPartsConst.FIELD_DCT_DICT_TEXT, lang);
                // Map Text zu pleId aufbauen
                List<PartListEntryId> partListIds = secondHelpMap.computeIfAbsent(text, s -> new DwList<>());
                partListIds.add(partListEntryId);
                // Map Text zu TextId aufbauen
                Set<String> textIdList = secondCheckMap.computeIfAbsent(text, s -> new TreeSet<>());
                textIdList.add(dataCombText.getAttribute(iPartsConst.FIELD_DCT_DICT_TEXT).getMultiLanguageTextId());
                somethingFound |= textIdList.size() > 1;
            }
        }
        return somethingFound;
    }

    private void buildInfoForCombTextForHotSpotsCheck(StringBuilder str, String hotspot, String lang,
                                                      Map<PartListEntryId, iPartsDataCombText> combTextMap,
                                                      Map<String, Set<String>> secondCheckMap, Map<String, List<PartListEntryId>> secondHelpMap) {
        // Warnung zusammenbauen
        for (Map.Entry<String, Set<String>> checkEntry : secondCheckMap.entrySet()) {
            String text = checkEntry.getKey();
            List<String> textIds = new DwList<>(checkEntry.getValue());
            // wenn es nur eine TextId gibt => weitermachen
            if (textIds.size() <= 1) {
                continue;
            }
            // betroffene TeilePos holen
            List<PartListEntryId> checkedPartListEntryIds = secondHelpMap.get(text);
            // nach dem ersten und zweiten Vorkommen einer TextId suchen
            PartListEntryId firstPleId = null;
            PartListEntryId secondPleId = null;
            if (checkedPartListEntryIds != null) {
                for (PartListEntryId pleId : checkedPartListEntryIds) {
                    iPartsDataCombText dataCombText = combTextMap.get(pleId);
                    if (dataCombText != null) {
                        String textId = dataCombText.getAttribute(iPartsConst.FIELD_DCT_DICT_TEXT).getMultiLanguageTextId();
                        if (firstPleId == null) {
                            if (textIds.contains(textId)) {
                                firstPleId = pleId;
                                textIds.remove(textId);
                                continue;
                            }
                        }
                        if (secondPleId == null) {
                            if (textIds.contains(textId)) {
                                secondPleId = pleId;
                                textIds.remove(textId);
                            }
                        }
                    }
                    // eine TeilePos mit der einen TextId und eine TeilePos mit der zweiten TextId gefunden?
                    if ((firstPleId != null) && (secondPleId != null)) {
                        addToInfo(str, hotspot, lang, firstPleId, secondPleId);
                        secondPleId = null;
                        if (textIds.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void addToInfo(StringBuilder str, String hotspot, String lang, PartListEntryId firstPleId, PartListEntryId secondPleId) {
        // Meldung zusammenbauen
        if (str.toString().length() > 0) {
            str.append("\n");
        }
        str.append(TranslationHandler.translate("!!Hotspot")).append(": ");
        str.append(hotspot);
        str.append(" ");
        str.append(TranslationHandler.translate("!!Teileposition (%1) und Teileposition (%2) benutzen unterschiedliche Lexikoneinträge in der Sprache %3",
                                                firstPleId.getKLfdnr(), secondPleId.getKLfdnr(), lang));
    }

    /**
     * Alle kombinierten Texte zum TU laden und als Map den TeilePos zuordnen
     *
     * @return
     */
    protected Map<PartListEntryId, iPartsDataCombText> getCombTextMap() {
        Map<PartListEntryId, iPartsDataCombText> combTextMap = new HashMap<>();
        iPartsDataCombTextList dataCombTextList = iPartsDataCombTextList.loadForModuleAndAllLanguages(getProject(), getAssemblyId());
        for (iPartsDataCombText dataCombText : dataCombTextList) {
            combTextMap.put(dataCombText.getAsId().getPartListEntryId(), dataCombText);
        }
        return combTextMap;
    }

    /**
     * DAIMLER-15289: Qualitätsprüfung um Code-Durchgängigkeit an Bildtafel erweitern
     */
    protected void doCheckPatency() {
        EditModuleFormIConnector connector = getConnector();
        if (connector.getImageCount() > 1) {
            String noCode = "!!Bildtafel \"%1\" besitzt keine Coderegel";
            String withCode = "!!Bildtafel \"%1\" besitzt Coderegel";
            String startCode = connector.getImage(0).getFieldValue(iPartsConst.FIELD_I_CODES);
            boolean codesMustBeEmpty = StrUtils.isEmpty(startCode);
            boolean hasWarnings = false;
            StringBuilder information = new StringBuilder();
            information.append(TranslationHandler.translate("!!Belegung der Coderegeln:")).append("\n");
            for (EtkDataImage image : connector.getCurrentAssembly().getImages()) {
                String code = image.getFieldValue(iPartsConst.FIELD_I_CODES);
                boolean isValid;
                if (codesMustBeEmpty) {
                    isValid = StrUtils.isEmpty(code);
                } else {
                    isValid = StrUtils.isValid(code);
                }
                String msg = withCode;
                if (StrUtils.isEmpty(code)) {
                    msg = noCode;
                }
                information.append(TranslationHandler.translate(msg, image.getFieldValue(iPartsConst.FIELD_I_IMAGES))).append("\n");
                if (!isValid) {
                    hasWarnings = true;
                }
            }
            if (hasWarnings) {
                validationEntries.addWarning(getAssemblyId(), null, TranslationHandler.translate("!!Bildtafeln besitzen nicht durchgängig eine Coderegel"),
                                             information.toString());

            }
        }
    }

    /**
     * Check, ob noch offene Bestätigungen existieren
     */
    public void executeOpenConfirmationsForChangeSetCheck() {
        if (getProject().isRevisionChangeSetActiveForEdit()) {
            AbstractRevisionChangeSet changeSetForEdit = getProject().getRevisionsHelper().getActiveRevisionChangeSetForEdit();
            if (changeSetForEdit != null) {
                iPartsDataConfirmChangesList confirmChangesList = iPartsDataConfirmChangesList.loadConfirmChangesForChangeSetAndAssemblyId(getProject(),
                                                                                                                                           changeSetForEdit.getChangeSetId(),
                                                                                                                                           assemblyId,
                                                                                                                                           true);
                for (iPartsDataConfirmChanges dataConfirmChanges : confirmChangesList) {
                    IdWithType id = dataConfirmChanges.getAsId().getAsPartListEntryId();
                    if (id == null) {
                        id = assemblyId;
                    }

                    String dataObjectType = ChangeSetShowTypes.getTranslatedDescriptionFromObjectType(dataConfirmChanges.getAsId().getDataObjectType());
                    validationEntries.addErrorT(id, "!!Offene Bestätigung für die Änderung von %1", dataObjectType);
                }
            }
        }
    }

    /**
     * Füllt eine Map mit Listen von partListEntryIds pro Hotspot
     * und die andere mit Sets von Strukturstufen pro Hotspot
     *
     * @param posPartListEntryIdMap
     * @param posHierachiesMap
     */
    protected void fillPosToPartListEntryIdsAndHierachiesMaps(Map<String, List<PartListEntryId>> posPartListEntryIdMap, Map<String, Set<String>> posHierachiesMap) {
        String fieldName = editConnector.getPosFieldName();
        for (EtkDataPartListEntry partListEntry : editConnector.getCurrentPartListEntries()) {
            if (!isOmittedPartListEntry(partListEntry)) {
                String kPos = partListEntry.getFieldValue(fieldName);
                if (kPos.isEmpty()) {
                    posPartListEntryIdMap.putIfAbsent(kPos, new ArrayList<>());
                    posPartListEntryIdMap.get(kPos).add(partListEntry.getAsId());
                    posHierachiesMap.putIfAbsent(kPos, new HashSet<>());
                    posHierachiesMap.get(kPos).add(partListEntry.getFieldValue(iPartsConst.FIELD_K_HIERARCHY));
                } else {
                    List<String> posNoList = GuiViewerUtils.splitPosNumber(kPos);
                    for (String pos : posNoList) {
                        posPartListEntryIdMap.putIfAbsent(pos, new ArrayList<>());
                        posPartListEntryIdMap.get(pos).add(partListEntry.getAsId());
                        posHierachiesMap.putIfAbsent(pos, new HashSet<>());
                        posHierachiesMap.get(pos).add(partListEntry.getFieldValue(iPartsConst.FIELD_K_HIERARCHY));
                    }
                }
            }
        }
    }

    protected List<PartListEntryId> getPartListEntryIdsFromPos(String pos) {
        List<PartListEntryId> result = new DwList<>();
        String fieldName = editConnector.getPosFieldName();
        for (EtkDataPartListEntry partListEntry : editConnector.getCurrentPartListEntries()) {
            if (!isOmittedPartListEntry(partListEntry)) {
                String kPos = partListEntry.getFieldValue(fieldName);
                if (kPos.isEmpty()) {
                    if (pos.isEmpty()) {
                        result.add(partListEntry.getAsId());
                    }
//                    posNoList.add("");
                } else {
                    List<String> posNoList = GuiViewerUtils.splitPosNumber(kPos);
                    if (posNoList.contains(pos)) {
                        result.add(partListEntry.getAsId());
                    }
                }
            }
        }
        return result;
    }

    protected boolean isOmittedPartListEntry(EtkDataPartListEntry partListEntry) {
        return partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT);
    }

    /**
     * Testen, ob die Stückliste leer ist
     *
     * @return true, falls sie leer ist
     */
    protected boolean isPartListEmpty() {
        int partListSize = getConnector().getUnfilteredPartListEntries().size();
        return partListSize == 0;
    }

    /**
     * Testen, ob das Modul ausgeblendet ist
     *
     * @return Ist Modul ausgeblendet
     */
    protected boolean isModuleHidden() {
        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
        iPartsDataModule moduleMetaData = ((iPartsDataAssembly)currentAssembly).getModuleMetaData();
        return moduleMetaData.getFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN);
    }

    /**
     * Gibt es Bilder im TU
     *
     * @return true, falls es keine Bilder gibt
     */
    protected boolean arePicturesInTU() {
        int imageCount = getConnector().getImageCount();
        return imageCount != 0;
    }

    /**
     * Gibt es noch einen offenen Bildauftrag im Autorenauftrag
     * Nur im Edit möglich
     *
     * @return true, falls ein Bildauftrag bei aktivierten Autorenauftrag noch offen ist
     */
    protected Map<IdWithType, String> isAnOpenPicOrderInAuthorOrder() {
        EditModuleFormIConnector editConnector = getConnector();
        Map<IdWithType, String> result = new HashMap<>();
        boolean isChangeSetActive = editConnector.getCurrentAssembly().isRevisionChangeSetActive();

        // Nur mit mindestens einem aktivierten ChangeSet testen
        if (isChangeSetActive) {
            fireMessage("!!Prüfung der Bildaufträge zum Autoren-Auftrag");
            // Aktivierte Changesets laden (ein vorhandenes Edit-ChangeSet hat Vorrang vor readOnly-ChangeSets)
            Collection<AbstractRevisionChangeSet> activeChangsets;
            AbstractRevisionChangeSet changeSetForEdit = getProject().getRevisionsHelper().getActiveRevisionChangeSetForEdit();
            if (changeSetForEdit != null) {
                activeChangsets = new DwList<>();
                activeChangsets.add(changeSetForEdit);
            } else {
                activeChangsets = getProject().getEtkDbs().getActiveRevisionChangeSets();
            }

            for (AbstractRevisionChangeSet changeSet : activeChangsets) {
                Map<IdWithType, String> picOrders = getPicOrderFromChangeSetWithUnfinishedState(changeSet, assemblyId, getProject());
                if (!picOrders.isEmpty()) {
                    result.putAll(picOrders);
                }
            }
        }
        return result;
    }

    public static Map<IdWithType, String> getPicOrderFromChangeSetWithUnfinishedState(AbstractRevisionChangeSet changeSet,
                                                                                      AssemblyId assemblyId, EtkProject project) {
        Map<IdWithType, String> result = new HashMap<>();
        // Einträge zu diesem Changeset in der Tabelle Picorder Modules
        Collection<SerializedDBDataObject> serializedList = changeSet.getSerializedObjectsByTable(iPartsConst.TABLE_DA_PICORDER_MODULES);
        if (serializedList != null) {
            for (SerializedDBDataObject serializedPicOrderModule : serializedList) {
                iPartsPicOrderModulesId id = new iPartsPicOrderModulesId(serializedPicOrderModule.getPkValues());
                boolean searchPicOrder;
                if (assemblyId == null) {
                    // Suche nach offenen Bildaufträgen nicht auf Modul beschränkt
                    searchPicOrder = true;
                } else {
                    // Suche ist auf Modul beschränkt → Kontrollieren, ob Bildaufträge aus dem Changeset
                    // in diesem Modul vorhanden sind
                    searchPicOrder = id.getModuleNo().equals(assemblyId.getKVari());
                }
                if (searchPicOrder) {
                    // Für jedes Bild Status prüfen
                    iPartsPicOrderId picOrderId = new iPartsPicOrderId(id.getOrderGuid());
                    iPartsDataPicOrder picOrder = new iPartsDataPicOrder(project, picOrderId);
                    if (picOrder.loadFromDB(picOrderId) && picOrder.isValid() && !picOrder.isCancelled()) {
                        iPartsTransferStates picOrderStatus = picOrder.getStatus();
                        // Wenn Ende Status nicht erreicht ist, dann ist Bildauftrag noch offen
                        if (!iPartsTransferStates.isReleasedState(picOrderStatus)) {
                            result.put(picOrder.getAsId(), picOrder.getOrderIdExtern());
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Prüft, ob es Hotspots gibt, die nicht einer Teileposition in der Stückliste zugeordnet sind
     *
     * @return die Liste mit den fehlerhaften Hotspots
     */
    protected Map<IdWithType, List<String>> isAHotspotWithoutPos(EditHotSpotHelper editHotSpotHelper) {
        EditModuleFormIConnector editConnector = getConnector();
        Map<IdWithType, List<String>> result = new HashMap<>();
        if (editConnector != null) {
            String msg = "!!Prüfung der Positionsnummern auf der Bildtafel";
            if (editConnector.getImageCount() > 1) {
                msg = "!!Prüfung der Positionsnummern auf den Bildtafeln";
            }
            fireMessage(msg);
            // Fehlerhafte Hotspots zur Bildtafelnummer zuordnen
            result = editHotSpotHelper.getFaultyHotspots();
        }

        return result;
    }

    /**
     * Prüft, ob es Teilepositionen gibt, deren POS keinem Hotspot auf dem Bild zugeordnet ist
     * oder ob es Teilepositionen mit leerem POS gibt
     * oder ob es ungültige POS gibt
     *
     * @return die Liste mit den fehlerhaften POS nummern
     */
    protected Map<EditHotSpotHelper.KEY_FAULTY_POS_MAP, List<String>> isAFaultyPos(EditHotSpotHelper editHotSpotHelper) {
        EditModuleFormIConnector editConnector = getConnector();
        Map<EditHotSpotHelper.KEY_FAULTY_POS_MAP, List<String>> result;
        if (editConnector != null) {
            result = editHotSpotHelper.getFaultyPOS();
        } else {
            result = new HashMap<>();
        }
        return result;
    }

    public boolean isSimplifiedQualityCheck() {
        return simplifiedQualityCheck;
    }
}
