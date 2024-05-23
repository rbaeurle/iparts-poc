/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWWPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSAlternativePartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSReplacementHelper;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Part Data Transfer Object für die iParts Webservices
 */
public class iPartsWSPart extends iPartsWSPartBase {

    private String calloutId;
    private List<iPartsWSNote> notes;
    private List<iPartsWSFootNote> footNotes;
    private String quantity;
    private boolean colorInfoAvailable;
    private List<iPartsWSPartBase> optionalParts;
    private List<String> damageCodes;
    private String codeValidity;
    private List<String> saaValidity;
    private List<String> modelValidity;
    private List<iPartsWSReplacementPart> replacedBy;
    private List<iPartsWSPart> subParts;
    private String level;
    private boolean accessory;
    private String steering;
    private String transmission;
    private String shelflife;
    private boolean alternativePartsAvailable;
    private List<iPartsWSAlternativePart> alternativeParts;
    private List<String> alternativePartsTypes;
    private boolean plantInformationAvailable;
    private boolean colored;
    private List<String> countryValidity;
    private List<String> specValidity;
    // Für GetParts und PartsList Webservice
    private String description;
    // NUR für PartsList Webservice
    private String additionalDesc;
    private String sequenceId;
    private String materialDesc;
    private List<String> additionalDescRefs;
    private String materialDescRef;
    private String bomKey;
    private boolean wiringHarnessKitAvailable;
    private List<iPartsWSWiringHarness> wiringHarnessKit;
    private List<iPartsWSEinPAS> einPAS;
    private boolean einPASNodeAvailable;
    private iPartsWSEinPAS latestEinPASNode;
    private String genericLocation;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPart() {
    }

    /**
     * Setzt RMI-kontextrelevante Informationen im Part-Objekt.
     *
     * @param project                  Zum Nachladen aus der DB
     * @param partListEntry            Der Stücklisteneintrag aus dem das Part DTO erstellt werden soll
     * @param includePartContext
     * @param language                 Sprachinformation
     * @param withExtendedDescriptions Gibt an, ob zusätzliche, beschreibende Textinformationen hinzugefügt werden sollen
     * @param reducedInformation       Sollen die Informationen auf ein Minimum reduziert werden?
     */
    public void assignRMIValues(EtkProject project, iPartsDataPartListEntry partListEntry, boolean includePartContext, String language,
                                boolean withExtendedDescriptions, boolean reducedInformation) {

        super.assignRMIValues(project, partListEntry, includePartContext, reducedInformation);

        if (!reducedInformation) {
            // Fußnoten
            Collection<iPartsFootNote> footNotes = partListEntry.getFootNotesForRetail();
            if ((footNotes != null) && !footNotes.isEmpty()) {
                this.footNotes = new DwList<>(footNotes.size());
                for (iPartsFootNote footNote : footNotes) {
                    this.footNotes.add(new iPartsWSFootNote(project, footNote));
                }
            }

            // DAIMLER-7186: Info, ob Farbteil ausgeben auch wenn keine Farbtabelle gültig ist
            // Im Zusammenspiel mit dem Part-Attribut "colorInfoAvailable" sind dann folgende Varianten möglich:
            //
            // Farbteil = "isColoredPart" an Stücklistenposition ist true
            // Farbe (un-)gültig = "colorTableForRetail" an der Stücklistenposition (nicht) vorhanden
            //
            // 1. Kein Farbteil, ungültig f. Fzg => colorInfoAvailable + colored nicht vorhanden
            // 2. Farbteil, (Farbe) ungültig f. Fzg. => colored = true, colorInfoAvailable nicht vorhanden
            // 3. Farbteil, (Farbe) gültig f. Fzg. => colored = true, colorInfoAvailable = true
            iPartsColorTable colorTables = partListEntry.getColorTableForRetail();
            this.colorInfoAvailable = colorTables != null;

            // codeValidity soll nicht ausgegeben werden wenn die Code Bedingung ";" ist
            String codes = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_WITH_EVENTS);
            if (DaimlerCodes.isEmptyCodeString(codes)) {
                codes = "";
            }
            this.codeValidity = setIfNotNull(codes);

            // SAA-Gültigkeiten
            EtkDataArray tempArray = partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_SA_VALIDITY);
            if ((tempArray != null) && !tempArray.isEmpty()) {
                this.saaValidity = new DwList<>(tempArray.getAttributes().size());
                String dbLanguage = project.getDBLanguage();
                for (DBDataObjectAttribute arrayValue : tempArray.getAttributes()) {
                    // Formatierung der SAAs wie Materialnummern (K_SA_VALIDITY kann für die Formatierung nicht verwendet
                    // werden, weil das VisObject in diesem Fall aktuell die Daten aus der DB nachladen würde für das Array-Feld)
                    this.saaValidity.add(iPartsNumberHelper.formatPartNo(project, arrayValue.getAsString(), dbLanguage));
                }
            }

            // Baumuster-Gültigkeiten
            tempArray = partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_MODEL_VALIDITY);
            if ((tempArray != null) && !tempArray.isEmpty()) {
                this.modelValidity = tempArray.getArrayAsStringList();
            }

            this.subParts = null; //vorerst keine Business Logik laut Confluence

            this.steering = setIfNotNull(partListEntry.getFieldValue(iPartsConst.FIELD_K_STEERING));
            this.transmission = setIfNotNull(partListEntry.getFieldValue(iPartsConst.FIELD_K_GEARBOX_TYPE));

            this.plantInformationAvailable = partListEntry.getFactoryDataValidity() == iPartsFactoryData.ValidityType.VALID;

            // Ergänzungstext
            if (withExtendedDescriptions) {
                // Anfrage für erweiterte Ausgabe der Texte
                EtkMultiSprache neutralPartDesc = partListEntry.getPart().getFieldValueAsMultiLanguage(iPartsConst.FIELD_M_ADDTEXT);
                if (neutralPartDesc != null) {
                    // Setze den sprachneutralen Text vom Material
                    this.materialDesc = setIfNotNull(neutralPartDesc.getText(language));
                    if (this.materialDesc != null) {
                        // Wenn ein sprachneutraler Text existiert, dann die Text-Id ausgeben
                        if (!neutralPartDesc.getTextId().isEmpty()) {
                            materialDescRef = neutralPartDesc.getTextId();
                        }
                    }
                }
                // Check, ob am Stücklisteneintrag Ergänzungstexte hängen
                List<EtkMultiSprache> combTextToken = partListEntry.getCombinedMultiTextList();
                if ((combTextToken != null) && !combTextToken.isEmpty()) {
                    additionalDescRefs = new DwList<>(combTextToken.size());
                    StringBuilder builder = new StringBuilder();
                    // Durchlaufe alle Teile der Ergänzungstexte und baue den Ergänzungstext zusammen. Zusätzlich werden
                    // die Text-Ids aller Einzelteile gesammelt.
                    for (EtkMultiSprache token : combTextToken) {
                        if (!builder.toString().isEmpty()) {
                            builder.append(" ");
                        }
                        builder.append(token.getTextByNearestLanguage(project.getDBLanguage(), project.getDataBaseFallbackLanguages()));
                        additionalDescRefs.add(token.getTextId());
                    }
                    this.additionalDesc = builder.toString();
                }

                // DAIMLER-9767, sequenceId in partsList Response aufnehmen
                this.sequenceId = setIfNotNull(partListEntry.getAsId().getKLfdnr());
            } else {
                this.description = setIfNotNull(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, project.getDBLanguage(), true));
            }
        } else {
            // Sonderfall sequenceId: Diese soll nur angezeigt werden, wenn extendedDescriptions oder reducedInformation gesetzt sind
            // deshalb leider das Code-Duplikat
            this.sequenceId = setIfNotNull(partListEntry.getAsId().getKLfdnr());
        }
    }

    /**
     * Füllt die Nicht-RMI-Informationen für dieses {@link iPartsWSPart}-Objekt.
     *
     * @param project                  Zum Nachladen aus der DB
     * @param partListEntry            Der Stücklisteneintrag aus dem das Part DTO erstellt werden soll
     * @param hotspotFieldName         Der Hotspot zum Stücklisteneintrag
     * @param includePartContext       Soll der {@link iPartsWSPartContext} zu dem Teil hinzugefügt werden?
     * @param includeNotes             Sollen Notizen zu dem Teil hinzugefügt werden?
     * @param language
     * @param withExtendedDescriptions Sollen erweiterte Beschreibungen wie z.B. Text-IDs ausgegeben werden?
     * @param includeReplacementChain
     * @param modelNumber
     * @param countryCode
     * @param primusReplacementsCache
     * @return (Gemappte Gleichteile -)Teilenummer
     */
    public String assignNonRMIValues(EtkProject project, iPartsDataPartListEntry partListEntry, String hotspotFieldName,
                                     boolean includePartContext, boolean includeNotes, String language, boolean withExtendedDescriptions,
                                     boolean includeReplacementChain, boolean includeAlternativeParts, String modelNumber, String countryCode, iPartsPRIMUSReplacementsCache primusReplacementsCache) {
        String matNr = super.assignNonRMIValues(project, partListEntry, withExtendedDescriptions);

        if (StrUtils.isValid(hotspotFieldName)) {
            this.calloutId = setIfNotNull(partListEntry.getFieldValue(hotspotFieldName));
        }

        this.quantity = partListEntry.getFieldValue(EtkDbConst.FIELD_K_MENGE);

        this.colored = partListEntry.isColoredPart();

        // Gefilterte Wahlweise-Teile bestimmen
        if (partListEntry.getOwnerAssembly() != null) {
            // Alle Wahlweise-Teile ( inkl. Extra-Wahlweise-Teile) bestimmen
            Collection<EtkDataPartListEntry> wwParts = iPartsWWPartsHelper.getWWParts(partListEntry, true);
            if (!wwParts.isEmpty()) {
                this.optionalParts = new DwList<>(wwParts.size());
                for (EtkDataPartListEntry wwPart : wwParts) {
                    iPartsWSPartBase partBase = new iPartsWSPartBase();
                    partBase.assignRMIValues(project, (iPartsDataPartListEntry)wwPart, includePartContext, false);
                    // Da die opionalParts nur mit deaktivierten RMI-Modus ausgegeben werden, enthalten sie selbst immer alle Werte.
                    partBase.assignNonRMIValues(project, (iPartsDataPartListEntry)wwPart, withExtendedDescriptions);
                    this.optionalParts.add(partBase);
                }
            }
        }

        // Die Fehlerortliste (ist an dieser Stelle schon berechnet).
        String failLocListString = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION);
        if (!failLocListString.isEmpty()) {
            this.damageCodes = StrUtils.toStringList(failLocListString, ",", false, true);
        }

        // Ersetzungen
        Collection<iPartsReplacement> successors = partListEntry.getSuccessors(true);
        if ((successors != null) && !successors.isEmpty()) {
            this.replacedBy = new DwList<>(successors.size());
            for (iPartsReplacement successor : successors) {
                iPartsWSReplacementPart replacementPart = new iPartsWSReplacementPart(project, successor, includePartContext, withExtendedDescriptions,
                                                                                      false, true, true);
                // Ersatzkette auf Hinweis C74 prüfen
                replacementPart.setPrimusCode74AvailableDependingOnPrimusRepCacheData(primusReplacementsCache);
                this.replacedBy.add(replacementPart);
            }
        }

        if (includeReplacementChain) {
            iPartsWSReplacementHelper replacementHelper = new iPartsWSReplacementHelper(
                    project, includePartContext, withExtendedDescriptions, modelNumber, countryCode, partListEntry.getAsId());

            if (successors != null) {
                List<iPartsWSReplacementPart> successorChain = replacementHelper.buildSuccessorChain(successors, true,
                                                                                                     primusReplacementsCache);
                if (Utils.isValid(successorChain)) {
                    this.addReplacementsToChain(successorChain, true);
                }
            }

            List<iPartsReplacement> predecessors = partListEntry.getPredecessors(true);
            if (predecessors != null) {
                List<iPartsWSReplacementPart> predecessorChain = replacementHelper.buildSuccessorChain(predecessors, false,
                                                                                                       primusReplacementsCache);
                if (Utils.isValid(predecessorChain)) {
                    this.addReplacementsToChain(predecessorChain, false);
                }
            }
        }


        // Level zweistellig ausgeben mit führender 0 falls notwendig
        String hierarchy = partListEntry.getFieldValue(iPartsConst.FIELD_K_HIERARCHY);
        if (!hierarchy.isEmpty()) {
            this.level = StrUtils.prefixStringWithCharsUpToLength(hierarchy, '0', 2);
        }

        this.accessory = partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_ACC_CODE);

        this.shelflife = setIfNotNull(partListEntry.getPart().getFieldValue(iPartsConst.FIELD_M_SHELF_LIFE));

        if (partListEntry.getOwnerAssembly().getDocumentationType().isPKWDocumentationType()) {
            this.genericLocation = setIfNotNull(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO));
        }

        // Alternativteile
        List<iPartsWSAlternativePart> alternativeParts = null;
        Set<EtkDataPart> alternativeMaterials = partListEntry.getAlternativePartsFilteredByReplacements(modelNumber, countryCode);
        if ((alternativeMaterials != null) && !alternativeMaterials.isEmpty()) {
            this.alternativePartsAvailable = true;
            iPartsES1 es1Cache = iPartsES1.getInstance(project);
            this.alternativePartsTypes = alternativeMaterials
                    .stream()
                    .filter(ap -> ap.getFieldValue(iPartsConst.FIELD_M_AS_ES_1) != null)
                    .map(ap -> es1Cache.getType(ap.getFieldValue(iPartsConst.FIELD_M_AS_ES_1)))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            if (includeAlternativeParts) {
                alternativeParts = iPartsWSAlternativePartsHelper.fillAlternativeParts(alternativeMaterials, project);
                if (Utils.isValid(alternativeParts)) {
                    this.alternativeParts = alternativeParts;
                }
            }

        }


        if (includeNotes) {
            List<iPartsNote> notes = iPartsNote.getNotes(partListEntry, project);
            this.notes = iPartsWSNote.convertToWSNotes(notes, language, project.getDataBaseFallbackLanguages());
        }

        Set<String> countryValidities = partListEntry.getCountryValidities();
        if (!countryValidities.isEmpty()) {
            this.countryValidity = new ArrayList<>(countryValidities);
        }
        Set<String> specValidities = partListEntry.getSpecValidities();
        if (!specValidities.isEmpty()) {
            this.specValidity = new ArrayList<>(specValidities);
        }

        setPrimusCode74AvailableDependingOnPrimusRepCacheData(primusReplacementsCache);
        return matNr;
    }

    /**
     * Befüllt die Werte, die für den Validate WS benötigt werden:
     * ES1/ES2, PartContext, alternativePartsAvailable, colorInfoAvailable und Hotspot (calloutId)
     *
     * @param partListEntry
     * @param hotspotField
     * @param modelId
     * @param country
     */
    public void assignValuesForValidate(iPartsDataPartListEntry partListEntry, String hotspotField, String modelId, String country) {
        // ES1 / ES2
        String uniqueColorNr = getUniqueColorId(partListEntry);
        if (uniqueColorNr != null) {
            this.setEs2Key(uniqueColorNr);
        }
        setES1ES2Keys(partListEntry.getPart());

        // PartContext
        setPartContext(new iPartsWSPartContext(partListEntry));

        // alternativePartsAvailable
        Set<EtkDataPart> alternativeMaterials = partListEntry.getAlternativePartsFilteredByReplacements(modelId, country, true);
        if ((alternativeMaterials != null) && !alternativeMaterials.isEmpty()) {
            setAlternativePartsAvailable(true);
        }

        // colorInfoAvailable
        iPartsColorTable colorTables = partListEntry.getColorTableForRetail();
        setColorInfoAvailable(colorTables != null);

        // Hotspot / CalloutId
        setCalloutId(setIfNotNull(partListEntry.getFieldValue(hotspotField)));
    }

    private String setIfNotNull(String value) {
        if (!value.isEmpty()) {
            return value;
        }
        return null;
    }

    // Aktuell ist iPartsWSPart kein Input Parameter, aber vielleicht wird er es ja noch
//    @Override
//    public void checkIfValid(String path) throws RESTfulWebApplicationException {
//        // alles außer quantity darf leer sein (partNo und partNoFormatted sind in PartBase)
//        super.checkIfValid(path);
//        checkAttribValid(path, "quantity", quantity);
//    }

    // Getter und Setter
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCalloutId() {
        return calloutId;
    }

    public void setCalloutId(String calloutId) {
        this.calloutId = calloutId;
    }

    public List<iPartsWSNote> getNotes() {
        return notes;
    }

    public void setNotes(List<iPartsWSNote> notes) {
        this.notes = notes;
    }

    public List<iPartsWSFootNote> getFootNotes() {
        return footNotes;
    }

    public void setFootNotes(List<iPartsWSFootNote> footNotes) {
        this.footNotes = footNotes;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public boolean isColorInfoAvailable() {
        return colorInfoAvailable;
    }

    public void setColorInfoAvailable(boolean colorInfoAvailable) {
        this.colorInfoAvailable = colorInfoAvailable;
    }

    public List<iPartsWSPartBase> getOptionalParts() {
        return optionalParts;
    }

    public void setOptionalParts(List<iPartsWSPartBase> optionalParts) {
        this.optionalParts = optionalParts;
    }

    public List<String> getDamageCodes() {
        return damageCodes;
    }

    public void setDamageCodes(List<String> damageCodes) {
        this.damageCodes = damageCodes;
    }

    public String getCodeValidity() {
        return codeValidity;
    }

    public void setCodeValidity(String codeValidity) {
        this.codeValidity = codeValidity;
    }

    public List<String> getSaaValidity() {
        return saaValidity;
    }

    public void setSaaValidity(List<String> saaValidity) {
        this.saaValidity = saaValidity;
    }

    public List<String> getModelValidity() {
        return modelValidity;
    }

    public void setModelValidity(List<String> modelValidity) {
        this.modelValidity = modelValidity;
    }

    public List<iPartsWSReplacementPart> getReplacedBy() {
        return replacedBy;
    }

    public void setReplacedBy(List<iPartsWSReplacementPart> replacedBy) {
        this.replacedBy = replacedBy;
    }

    public List<iPartsWSPart> getSubParts() {
        return subParts;
    }

    public void setSubParts(List<iPartsWSPart> subParts) {
        this.subParts = subParts;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean isAccessory() {
        return accessory;
    }

    public void setAccessory(boolean accessory) {
        this.accessory = accessory;
    }

    public String getSteering() {
        return steering;
    }

    public void setSteering(String steering) {
        this.steering = steering;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getShelflife() {
        return shelflife;
    }

    public void setShelflife(String shelflife) {
        this.shelflife = shelflife;
    }

    public boolean isAlternativePartsAvailable() {
        return alternativePartsAvailable;
    }

    public void setAlternativePartsAvailable(boolean alternativePartsAvailable) {
        this.alternativePartsAvailable = alternativePartsAvailable;
    }

    public List<iPartsWSAlternativePart> getAlternativeParts() {
        return alternativeParts;
    }

    public void setAlternativeParts(List<iPartsWSAlternativePart> alternativeParts) {
        this.alternativeParts = alternativeParts;
    }

    public List<String> getAlternativePartsTypes() {
        return alternativePartsTypes;
    }

    public void setAlternativePartsTypes(List<String> alternativePartsTypes) {
        this.alternativePartsTypes = alternativePartsTypes;
    }

    public boolean isPlantInformationAvailable() {
        return plantInformationAvailable;
    }

    public void setPlantInformationAvailable(boolean plantInformationAvailable) {
        this.plantInformationAvailable = plantInformationAvailable;
    }

    public boolean isColored() {
        return colored;
    }

    public void setColored(boolean colored) {
        this.colored = colored;
    }

    public String getAdditionalDesc() {
        return additionalDesc;
    }

    public void setAdditionalDesc(String additionalDesc) {
        this.additionalDesc = additionalDesc;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getMaterialDesc() {
        return materialDesc;
    }

    public void setMaterialDesc(String materialDesc) {
        this.materialDesc = materialDesc;
    }

    public List<String> getAdditionalDescRefs() {
        return additionalDescRefs;
    }

    public void setAdditionalDescRefs(List<String> additionalDescRefs) {
        this.additionalDescRefs = additionalDescRefs;
    }

    public String getMaterialDescRef() {
        return materialDescRef;
    }

    public void setMaterialDescRef(String materialDescRef) {
        this.materialDescRef = materialDescRef;
    }

    public String getBomKey() {
        return bomKey;
    }

    public void setBomKey(String bomKey) {
        this.bomKey = bomKey;
    }

    public boolean isWiringHarnessKitAvailable() {
        return wiringHarnessKitAvailable;
    }

    public void setWiringHarnessKitAvailable(boolean wiringHarnessKitAvailable) {
        this.wiringHarnessKitAvailable = wiringHarnessKitAvailable;
    }

    public List<iPartsWSWiringHarness> getWiringHarnessKit() {
        return wiringHarnessKit;
    }

    public void setWiringHarnessKit(List<iPartsWSWiringHarness> wiringHarnessKit) {
        this.wiringHarnessKit = wiringHarnessKit;
    }

    public List<iPartsWSEinPAS> getEinPAS() {
        return einPAS;
    }

    public void setEinPAS(List<iPartsWSEinPAS> einPAS) {
        this.einPAS = einPAS;
    }

    public iPartsWSEinPAS getLatestEinPASNode() {
        return latestEinPASNode;
    }

    public void setLatestEinPASNode(iPartsWSEinPAS latestEinPASNode) {
        this.latestEinPASNode = latestEinPASNode;
    }

    public boolean isEinPASNodeAvailable() {
        return einPASNodeAvailable;
    }

    public void setEinPASNodeAvailable(boolean einPASNodeAvailable) {
        this.einPASNodeAvailable = einPASNodeAvailable;
    }

    public String getGenericLocation() {
        return genericLocation;
    }

    public void setGenericLocation(String genericLocation) {
        this.genericLocation = genericLocation;
    }

    public List<String> getCountryValidity() {
        return countryValidity;
    }

    public void setCountryValidity(List<String> countryValidity) {
        this.countryValidity = countryValidity;
    }

    public List<String> getSpecValidity() {
        return specValidity;
    }

    public void setSpecValidity(List<String> specValidity) {
        this.specValidity = specValidity;
    }
}