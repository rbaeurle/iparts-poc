/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsScoringHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSIncludePartCacheObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementCacheObject;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Datenklasse für eine Ersetzung in iParts mit Vorgänger, Nachfolger und Mitlieferteilen
 */
public class iPartsReplacement {

    /**
     * Erzeugt ein neues Ersetzungs-Hilfsobjekt für eine PRIMUS-Ersetzung und setzt dabei den Nachfolger analog zur Migration
     * auf den laut Scoring am besten passenden Stücklisteneintrag aus der Stückliste oder {@code null}, falls gar keiner passt.
     *
     * @param replacementCacheObject   Cache-Objekt, aus dem PRIMUS-Ersetzungscache
     * @param partNoToPartlistEntryMap Map mit Materialnummer auf EtkDataPartListEntry
     * @return
     */
    public static iPartsReplacement createPrimusReplacement(iPartsPRIMUSReplacementCacheObject replacementCacheObject,
                                                            iPartsDataPartListEntry predecessorPartListEntry,
                                                            Map<String, List<iPartsDataPartListEntry>> partNoToPartlistEntryMap) {
        List<iPartsDataPartListEntry> possibleSuccessors = partNoToPartlistEntryMap.get(replacementCacheObject.getSuccessorPartNo());
        EtkDataPartListEntry mostEqualPartListEntry = null;
        if (possibleSuccessors != null) {
            List<EtkDataPartListEntry> mostEqualPartListEntries = iPartsScoringHelper.getMostEqualPartListEntries(predecessorPartListEntry,
                                                                                                                  possibleSuccessors);
            if ((mostEqualPartListEntries != null) && !mostEqualPartListEntries.isEmpty()) {
                mostEqualPartListEntry = mostEqualPartListEntries.get(0);
            }
        }
        return new iPartsReplacement(replacementCacheObject, predecessorPartListEntry, mostEqualPartListEntry);
    }

    /**
     * Erzeugt ein neues Ersetzungs-Hilfsobjekt und setzt dabei den Vorgänger und Nachfolger auf die Stücklisteneinträge
     * aus der übergebenen Map.
     *
     * @param replacementDataObject
     * @param lfdNrToPartListEntryMap Map mit laufender Nummer auf EtkDataPartListEntry
     * @return {@code null}, falls sich der Vorgänger nicht in der Stückliste befindet, was nie vorkommen kann/sollte.
     */
    public static iPartsReplacement createReplacement(iPartsDataReplacePart replacementDataObject,
                                                      Map<String, EtkDataPartListEntry> lfdNrToPartListEntryMap) {
        String predecessorLfdNr = replacementDataObject.getFieldValue(iPartsConst.FIELD_DRP_LFDNR);
        EtkDataPartListEntry predecessorEntry = lfdNrToPartListEntryMap.get(predecessorLfdNr);
        if (predecessorEntry == null) {
            return null;
        }
        // Nachfolger befindet sich evtl. nicht in der Stückliste -> laufende Nummer ist dann leer
        String successorLfdNr = replacementDataObject.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_LFDNR);
        EtkDataPartListEntry successorEntry = null;
        if (!successorLfdNr.isEmpty()) {
            successorEntry = lfdNrToPartListEntryMap.get(successorLfdNr);
        }
        return new iPartsReplacement(replacementDataObject, predecessorEntry, successorEntry);
    }

    /**
     * Erzeugt einen virtuellen Klon der Ersetzung und ersetzt Vorgänger und Nachfolger durch die übergebenen,
     * falls diese nicht {@code null} sind.
     *
     * @param source
     * @param predecessorEntry
     * @param successorEntry
     * @return
     */
    public static iPartsReplacement createVirtualClone(iPartsReplacement source, EtkDataPartListEntry predecessorEntry,
                                                       EtkDataPartListEntry successorEntry) {
        iPartsReplacement clone = source.cloneMe();
        if (predecessorEntry != null) {
            clone.predecessorEntry = predecessorEntry;
        }
        if (successorEntry != null) {
            clone.successorEntry = successorEntry;
            clone.setSuccessorPartNumber(successorEntry.getPart().getAsId().getMatNr());
        }
        clone.setVirtual();
        return clone;
    }

    public enum Source {

        // Enum-Werte für das Feld DA_REPLACE_PART.DRP_SOURCE
        TS7("TS7"),
        VTNV("VTNV"),
        IPARTS("IPARTS"),
        MAD("MAD"),
        PRIMUS("PRIMUS"),
        UNKNOWN("");

        private String dbValue;

        Source(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        public static Source getFromDBValue(String dbValue) {
            for (Source source : values()) {
                if (source.getDbValue().equals(dbValue)) {
                    return source;
                }
            }
            return UNKNOWN;
        }
    }

    public EtkDataPartListEntry predecessorEntry;
    public EtkDataPartListEntry successorEntry;
    public String successorPartNumber = "";
    public String successorMappedPartNumber = "";
    public String rfmeaFlags = "";
    public String rfmenFlags = "";
    public String primusCodeForward = ""; // Nur bei PRIMUS-Ersetzungen
    public String primusCodeBackward = ""; // Nur bei PRIMUS-Ersetzungen
    public String replacementSeqNo = "";
    public Source source = Source.UNKNOWN;
    public iPartsDataReleaseState releaseState = iPartsDataReleaseState.UNKNOWN;
    public String predecessorSourceGUID;
    public String successorSourceGUID;
    private boolean includePartsLoaded;
    private Collection<IncludePart> includeParts;
    private boolean isVirtual;

    public iPartsReplacement() {

    }

    public iPartsReplacement(iPartsPRIMUSReplacementCacheObject primusReplacementCacheObject,
                             EtkDataPartListEntry predecessorEntry, EtkDataPartListEntry successorEntry) {
        this.predecessorEntry = predecessorEntry;
        this.successorEntry = successorEntry;
        setSuccessorPartNumber(primusReplacementCacheObject.getSuccessorPartNo());
        // Der PSS InfoTyp wird zerlegt in:
        // RFMEA: 1. Stelle PSSInfoType --> Stelle 4, 2. Stelle PSSInfoType --> Stelle 7
        // RFMEN: 3. Stelle PSSInfoType --> Stelle 7
        String infoType = primusReplacementCacheObject.getInfoType();
        if (StrUtils.isValid(infoType) && (infoType.length() >= 3)) {
            this.rfmeaFlags = new iPartsRFMEA(infoType.charAt(0), infoType.charAt(1)).getAsString();
            this.rfmenFlags = new iPartsRFMEN(infoType.charAt(2)).getAsString();
        }
        this.primusCodeForward = primusReplacementCacheObject.getCodeForward();
        this.primusCodeBackward = primusReplacementCacheObject.getCodeBackward();
        this.source = Source.PRIMUS;
        this.releaseState = iPartsDataReleaseState.RELEASED;
        List<iPartsPRIMUSIncludePartCacheObject> primusIncludePartCacheObjects = primusReplacementCacheObject.getIncludeParts();
        if ((primusIncludePartCacheObjects != null) && !primusIncludePartCacheObjects.isEmpty()) {
            this.includeParts = new TreeSet<>(IncludePart.partNoComparator);
            for (iPartsPRIMUSIncludePartCacheObject primusIncludePartCacheObject : primusIncludePartCacheObjects) {
                this.includeParts.add(new IncludePart(primusIncludePartCacheObject));
            }
        }
        this.includePartsLoaded = true;
        this.isVirtual = true; // Sonst wird diese virtuelle Ersetzung z.B. kopiert beim Kopieren von Stücklisteneinträgen
    }

    public iPartsReplacement(iPartsDataReplacePart replacementDataObject, EtkDataPartListEntry predecessorEntry,
                             EtkDataPartListEntry successorEntry) {
        this.predecessorEntry = predecessorEntry;
        this.successorEntry = successorEntry;
        setSuccessorPartNumber(replacementDataObject.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_MATNR));
        this.rfmeaFlags = replacementDataObject.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEA);
        this.rfmenFlags = replacementDataObject.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEN);
        this.replacementSeqNo = replacementDataObject.getFieldValue(iPartsConst.FIELD_DRP_SEQNO);
        this.source = replacementDataObject.getSource();
        this.releaseState = replacementDataObject.getStatus();
        this.predecessorSourceGUID = replacementDataObject.getFieldValue(iPartsConst.FIELD_DRP_SOURCE_GUID);
        this.successorSourceGUID = replacementDataObject.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_SOURCE_GUID);
        this.includePartsLoaded = false;
        this.isVirtual = false;
    }

    /**
     * Klont diese Ersetzung, wobei der Vorgänger und Nachfolger durch die korrespondierenden Stücklisteneinträge der übergebenen
     * {@code destPartListEntriesMap} ersetzt werden.
     *
     * @param destPartListEntriesMap
     */
    public iPartsReplacement cloneMe(Map<String, iPartsDataPartListEntry> destPartListEntriesMap) {
        iPartsReplacement destReplacement = cloneMe();
        if (predecessorEntry != null) {
            destReplacement.predecessorEntry = destPartListEntriesMap.get(predecessorEntry.getAsId().getKLfdnr());
        }

        if (successorEntry != null) {
            destReplacement.successorEntry = destPartListEntriesMap.get(successorEntry.getAsId().getKLfdnr());
        }
        return destReplacement;
    }

    public iPartsReplacement cloneMe() {
        iPartsReplacement destReplacement = new iPartsReplacement();
        if (predecessorEntry != null) {
            destReplacement.predecessorEntry = predecessorEntry;
        }

        if (successorEntry != null) {
            destReplacement.successorEntry = successorEntry;
        }

        destReplacement.setSuccessorPartNumber(successorPartNumber);
        destReplacement.rfmeaFlags = rfmeaFlags;
        destReplacement.rfmenFlags = rfmenFlags;
        destReplacement.primusCodeForward = primusCodeForward;
        destReplacement.primusCodeBackward = primusCodeBackward;
        destReplacement.replacementSeqNo = replacementSeqNo;
        destReplacement.source = source;
        destReplacement.releaseState = releaseState;
        destReplacement.predecessorSourceGUID = predecessorSourceGUID;
        destReplacement.successorSourceGUID = successorSourceGUID;
        destReplacement.includePartsLoaded = includePartsLoaded;
        destReplacement.isVirtual = isVirtual;

        if (includeParts != null) {
            destReplacement.includeParts = new DwList<>(includeParts.size());
            for (IncludePart includePart : includeParts) {
                destReplacement.includeParts.add(includePart.cloneMe());
            }
        }

        return destReplacement;
    }

    public void setSuccessorPartNumber(String successorPartNumber) {
        this.successorPartNumber = successorPartNumber;
        this.successorMappedPartNumber = successorPartNumber;
    }

    public Collection<IncludePart> getIncludeParts(EtkProject project) {
        if (predecessorEntry == null) {
            return null;
        }
        if (!includePartsLoaded && (project != null)) {
            // Mitlieferteile laden
            iPartsDataIncludePartList includePartList =
                    iPartsDataIncludePartList.loadIncludePartsForReplacement(project, getAsDataReplacePart(project));

            if (!includePartList.isEmpty()) {
                // Mitlieferteile erzeugen
                includeParts = new DwList<IncludePart>(includePartList.size());
                for (iPartsDataIncludePart dataIncludePart : includePartList) {
                    IncludePart includePart = new IncludePart(dataIncludePart.getAttributes());
                    includeParts.add(includePart);
                }
            }

            includePartsLoaded = true;
        }
        return includeParts;
    }

    /**
     * Erzeugt eine {@link iPartsDataIncludePartList} mit den Daten der Mitlieferteile der Ersetzung z.B. zur Anzeige in
     * einem {@link de.docware.apps.etk.base.forms.common.components.DataObjectGrid}.
     *
     * @param project
     * @param checkIfExists tru: überprüft, ob die Mitlieferteile bereits in der DB existieren
     * @param origin
     * @return
     */
    public iPartsDataIncludePartList getIncludePartsAsDataIncludePartList(EtkProject project, boolean checkIfExists, DBActionOrigin origin) {
        iPartsDataIncludePartList resultList = new iPartsDataIncludePartList();

        Collection<IncludePart> loadedIncludeParts = getIncludeParts(project);
        if (loadedIncludeParts != null) {
            iPartsDataReplacePart dataReplacePart = getAsDataReplacePart(project);
            for (IncludePart includePart : loadedIncludeParts) {
                iPartsIncludePartId includeId = new iPartsIncludePartId(dataReplacePart, includePart.seqNr);
                iPartsDataIncludePart dataIncludePart = new iPartsDataIncludePart(project, includeId);
                if (checkIfExists) {
                    if (!dataIncludePart.existsInDB()) {
                        dataIncludePart.initAttributesWithEmptyValues(origin);
                    }
                } else {
                    dataIncludePart.initAttributesWithEmptyValues(origin);
                }
                dataIncludePart.setFieldValue(iPartsConst.FIELD_DIP_INCLUDE_MATNR, includePart.partNumber, origin);
                dataIncludePart.setFieldValue(iPartsConst.FIELD_DIP_INCLUDE_QUANTITY, includePart.quantity, origin);

                resultList.add(dataIncludePart, origin);

            }
        }

        return resultList;
    }

    /**
     * Erzeugt eine {@link iPartsDataIncludePartList} mit den Daten der Mitlieferteile der Ersetzung z.B. zur Anzeige in
     * einem {@link de.docware.apps.etk.base.forms.common.components.DataObjectGrid}.
     *
     * @param project
     * @return
     */
    public iPartsDataIncludePartList getIncludePartsAsDataIncludePartList(EtkProject project) {
        return getIncludePartsAsDataIncludePartList(project, false, DBActionOrigin.FROM_DB);
    }

    /**
     * Setzt alle Mitlieferteile dieser Ersetzung.
     *
     * @param includeParts
     */
    public void setIncludeParts(Collection<IncludePart> includeParts) {
        this.includeParts = includeParts;
        includePartsLoaded = true;
    }

    /**
     * Fügt das übergebene Mitlieferteil dieser Ersetzung hinzu, falls es noch nicht existiert. Die laufende Nummer
     * des Mitlieferteils wird ergänzt, falls notwendig. Falls die Sequenznummer nicht gesetzt ist,
     * wird sie gesetzt (höchste Sequenznummer + 1).
     *
     * @param includePartForReplacement
     */
    public void addIncludePart(IncludePart includePartForReplacement) {
        if (includePartForReplacement == null) {
            return;
        }
        if (includeParts == null) {
            includeParts = new DwList<>();
        } else {
            for (IncludePart existingIncludePart : includeParts) {
                if (existingIncludePart.isSame(includePartForReplacement)) {
                    return;
                }
            }
        }

        if (StrUtils.isEmpty(includePartForReplacement.seqNr)) {
            int maxSeqNo = 0;
            for (IncludePart includePart : includeParts) {
                maxSeqNo = Math.max(maxSeqNo, Integer.parseInt(includePart.seqNr));
            }
            includePartForReplacement.seqNr = StrUtils.prefixStringWithCharsUpToLength(String.valueOf(maxSeqNo + 1), '0',
                                                                                       iPartsIncludePartId.SEQNO_LENGTH, false);
        }
        includeParts.add(includePartForReplacement);
        includePartsLoaded = true;
    }

    /**
     * Erzeugt ein {@link iPartsDataReplacePart}-Objekt mit den Daten dieser Ersetzung z.B. zur Anzeige in einem {@link de.docware.apps.etk.base.forms.common.components.DataObjectGrid}.
     *
     * @param project
     * @param checkIfExists true: überprüft, ob die Ersetzung bereits in der DB existiert
     * @return
     */
    public iPartsDataReplacePart getAsDataReplacePart(EtkProject project, boolean checkIfExists) {
        iPartsReplacePartId replacePartId = getAsReplacePartId();
        if (replacePartId == null) {
            return null;
        }

        // iPartsDataReplacePart aus den Daten erzeugen
        iPartsDataReplacePart dataReplacePart = new iPartsDataReplacePart(project, replacePartId);
        if (checkIfExists) {
            if (!dataReplacePart.existsInDB()) {
                dataReplacePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
        } else {
            dataReplacePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
        dataReplacePart.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_MATNR, successorPartNumber, DBActionOrigin.FROM_DB);
        if (successorEntry != null) {
            dataReplacePart.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_LFDNR, successorEntry.getAsId().getKLfdnr(), DBActionOrigin.FROM_DB);
        }
        dataReplacePart.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEA, rfmeaFlags, DBActionOrigin.FROM_DB);
        dataReplacePart.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEN, rfmenFlags, DBActionOrigin.FROM_DB);

        dataReplacePart.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DRP_PSS_CODE_FORWARD, primusCodeForward,
                                                 true, DBActionOrigin.FROM_DB);
        dataReplacePart.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DRP_PSS_CODE_BACK, primusCodeBackward,
                                                 true, DBActionOrigin.FROM_DB);

        dataReplacePart.setFieldValue(iPartsConst.FIELD_DRP_SOURCE, source.getDbValue(), DBActionOrigin.FROM_DB);

        dataReplacePart.setFieldValue(iPartsConst.FIELD_DRP_STATUS, releaseState.getDbValue(), DBActionOrigin.FROM_DB);

        if (isCreatedFromConstructionReplacement()) {
            dataReplacePart.setFieldValue(iPartsConst.FIELD_DRP_SOURCE_GUID, predecessorSourceGUID, DBActionOrigin.FROM_DB);
            dataReplacePart.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_SOURCE_GUID, successorSourceGUID, DBActionOrigin.FROM_DB);
        }

        return dataReplacePart;
    }

    /**
     * Erzeugt ein {@link iPartsDataReplacePart}-Objekt mit den Daten dieser Ersetzung z.B. zur Anzeige in einem {@link de.docware.apps.etk.base.forms.common.components.DataObjectGrid}.
     *
     * @param project
     * @return
     */
    public iPartsDataReplacePart getAsDataReplacePart(EtkProject project) {
        return getAsDataReplacePart(project, false);
    }

    public iPartsReplacePartId getAsReplacePartId() {
        if (predecessorEntry == null) {
            return null;
        }
        return new iPartsReplacePartId(predecessorEntry.getAsId().getKVari(), predecessorEntry.getAsId().getKVer(),
                                       predecessorEntry.getAsId().getKLfdnr(), replacementSeqNo);
    }

    /**
     * Setzt die laufende Nummer dieser Ersetzung.
     *
     * @param seqNo
     */
    public void setReplacementSeqNo(int seqNo) {
        this.replacementSeqNo = EtkDbsHelper.formatLfdNr(seqNo);
    }

    /**
     * Gibt an, ob es für diese Ersetzung geladene Mitlieferteile gibt.
     *
     * @param project
     * @return
     */
    public boolean hasIncludeParts(EtkProject project) {
        Collection<IncludePart> loadedIncludeParts = getIncludeParts(project);
        return (loadedIncludeParts != null) && !loadedIncludeParts.isEmpty();
    }

    public void clearIncludeParts() {
        includeParts = null;
        includePartsLoaded = false;
    }

    /**
     * Ist die Ersetzung nicht austauschbar?
     *
     * @return
     */
    public boolean isNotReplaceable() {
        return (new iPartsRFMEA(rfmeaFlags).isEvalPEMToForRealReplacement()) && (new iPartsRFMEN(rfmenFlags).isNotReplaceable());
    }

    /**
     * DAIMLER-6970: Soll die Ersetzung trotz evtl. ausgefiltertem Nachfolger angezeigt werden?
     *
     * @return
     */
    public boolean isVisibleIfSuccessorFiltered() {
        // PRIMUS-Ersetzungen sollen generell auch dann noch angezeigt werden, wenn der Nachfolger ausgefiltert wurde
        if (source == Source.PRIMUS) {
            return true;
        }

        iPartsRFMEA rfmea = new iPartsRFMEA(rfmeaFlags); // A0
        iPartsRFMEN rfmen = new iPartsRFMEN(rfmenFlags); // X oder 1
        return rfmea.isSuccessorVisibleDespiteFilter() && rfmen.isPredecessorReplaceable();
    }

    /**
     * Ist diese Ersetzung virtuell (aus Vererbung oder von PRIMUS)?
     *
     * @return
     */
    public boolean isVirtual() {
        return isVirtual;
    }

    /**
     * Ist diese Ersetzung virtuell aus Vererbung?
     *
     * @return
     */
    public boolean isVirtualInherited() {
        return isVirtual() && (source != iPartsReplacement.Source.PRIMUS);
    }

    private void setVirtual() {
        isVirtual = true;
    }

    /**
     * Wurde diese Ersetzung aus einer Konstruktions-Ersetzung erzeugt bzw. basierend auf einer Konstruktions-Ersetzung
     * nachträglich verändert? In diesem Fall sind {@link #predecessorSourceGUID} und {@link #successorSourceGUID} gefüllt.
     *
     * @return
     */
    public boolean isCreatedFromConstructionReplacement() {
        return StrUtils.isValid(predecessorSourceGUID, successorSourceGUID);
    }

    /**
     * Ermittelt die Vergleichsgröße um zu prüfen ob Ersetzungen identisch sind.
     * Beim Nachfolger kann entweder der Stücklisteneintrag oder nur eine Materialnummer angegeben sein. Wenn der
     * Stücklisteneintrag angegeben ist, wird hier für den Vergleich nur die laufende Nummer verwendet, da
     * Ersetzungen nur innerhalb der Stückliste zulässig sind.
     * Beim Vorgänger gibt es nur die Materialnummer
     *
     * @param isSuccessor
     * @return
     */
    public String getReplacementCompareNumber(boolean isSuccessor) {
        if (isSuccessor) {
            if (successorEntry != null) {
                return successorEntry.getAsId().getKLfdnr();
            } else {
                return successorPartNumber;
            }
        } else {
            if (predecessorEntry != null) {
                return predecessorEntry.getAsId().getKLfdnr();
            }
        }
        return "";
    }

    public boolean isSuccessorEqual(iPartsReplacement other) {
        boolean successorEqual;
        if ((this.successorEntry != null) && (other.successorEntry != null)) {
            successorEqual = this.successorEntry.getAsId().equals(other.successorEntry.getAsId());
        } else {
            successorEqual = this.successorPartNumber.equals(other.successorPartNumber);
        }
        return successorEqual;
    }

    /**
     * Liefert einen Schlüssel für Vergleiche zwischen Ersetzungen zurück basierend auf allen relevanten Dateninhalten dieser
     * Ersetzung.
     *
     * @param includePredecessorAndSuccessorPLEIds Sollen die IDs der Stücklisteneinträge von Vorgänger und Nachfolger im
     *                                             Schlüssel enthalten sein?
     * @param includeRFMEFlags                     Sollen die RFME-Flags im Schlüssel enthalten sein?
     * @param includeSourceAndState                Sollen die Quelle und der Status im Schüssel enthalten sein?
     * @return
     * @see iPartsDataReplacePart#getCompareKey(boolean, boolean, boolean) Der Rückgabewert ist kompatibel zu dieser Methode.
     */
    public String getCompareKey(boolean includePredecessorAndSuccessorPLEIds, boolean includeRFMEFlags, boolean includeSourceAndState) {
        StringBuilder sb = new StringBuilder();

        if (includePredecessorAndSuccessorPLEIds) {
            sb.append(predecessorEntry.getAsId().toDBString());
            sb.append(IdWithType.DB_ID_DELIMITER);
            if (successorEntry != null) {
                sb.append(successorEntry.getAsId().toDBString());
                sb.append(IdWithType.DB_ID_DELIMITER);
            }
        }

        sb.append(successorPartNumber);

        if (includeRFMEFlags) {
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(rfmeaFlags);
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(rfmenFlags);
        }

        if (includeSourceAndState) {
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(source.getDbValue());
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(releaseState.getDbValue());
        }

        if (isCreatedFromConstructionReplacement()) {
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(predecessorSourceGUID);
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(successorSourceGUID);
        }

        return sb.toString();
    }

    public boolean isValuesEqual(iPartsReplacement other) {
        return this.predecessorEntry.getAsId().equals(other.predecessorEntry.getAsId()) &&
               this.rfmeaFlags.equals(other.rfmeaFlags) &&
               this.rfmenFlags.equals(other.rfmenFlags) &&
               (this.source == other.source) &&
               (this.releaseState == other.releaseState);
    }

    public String getAsStringForTitle(String dbLanguage) {
        String predecessorPartText = predecessorEntry.getPart().getDisplayValue(EtkDbConst.FIELD_M_MATNR, dbLanguage);

        EtkDataPart successorPart;
        if (successorEntry != null) {
            successorPart = successorEntry.getPart();
        } else {
            successorPart = EtkDataObjectFactory.createDataPart(predecessorEntry.getEtkProject(), successorMappedPartNumber, "");
        }
        String successorPartText = successorPart.getDisplayValue(EtkDbConst.FIELD_M_MATNR, dbLanguage);

        return TranslationHandler.translate("!!Ersetzung von '%1' durch '%2'", predecessorPartText, successorPartText);
    }

    /**
     * Sind die Mitlieferteile dieser Ersetzung editierbar?
     *
     * @return
     */
    public boolean isIncludePartsEditable() {
        return !isVirtual() && (source != iPartsReplacement.Source.VTNV) && (source != iPartsReplacement.Source.TS7)
               && (source != iPartsReplacement.Source.PRIMUS);
    }

    public iPartsDataPartListEntry getReplacementPartAsiPartsEntry(boolean isSuccessorDirection) {
        if (isSuccessorDirection) {
            if (this.successorEntry instanceof iPartsDataPartListEntry) {
                return (iPartsDataPartListEntry)this.successorEntry;
            }
        } else {
            if (this.predecessorEntry instanceof iPartsDataPartListEntry) {
                return (iPartsDataPartListEntry)this.predecessorEntry;
            }
        }
        return null;
    }

    /**
     * Datenklasse für ein Mitlieferteil mit Menge
     */
    public static class IncludePart {

        public static Comparator<iPartsReplacement.IncludePart> seqNoComparator = Comparator.comparing(o -> o.seqNr);
        public static Comparator<iPartsReplacement.IncludePart> partNoComparator = Comparator.comparing(o -> o.partNumber);

        public String partNumber;
        public String quantity;
        public String seqNr;

        public IncludePart() {
        }

        public IncludePart(EtkDataPartListEntry partListEntry) {
            partNumber = partListEntry.getPart().getAsId().getMatNr();
            quantity = partListEntry.getFieldValue(iPartsConst.FIELD_K_MENGE);
        }

        public IncludePart(iPartsPRIMUSIncludePartCacheObject includePartCacheObject) {
            partNumber = includePartCacheObject.getIncludePartNo();
            quantity = includePartCacheObject.getQuantity();
        }

        public IncludePart(DBDataObjectAttributes attributes) {
            partNumber = attributes.getField(iPartsConst.FIELD_DIP_INCLUDE_MATNR).getAsString();
            quantity = attributes.getField(iPartsConst.FIELD_DIP_INCLUDE_QUANTITY).getAsString();
            seqNr = attributes.getField(iPartsConst.FIELD_DIP_SEQNO).getAsString();
        }

        public IncludePart cloneMe() {
            IncludePart destIncludePart = new IncludePart();
            destIncludePart.partNumber = partNumber;
            destIncludePart.quantity = quantity;
            destIncludePart.seqNr = seqNr;
            return destIncludePart;
        }

        public boolean isSame(IncludePart otherIncludePart) {
            return partNumber.equals(otherIncludePart.partNumber) && quantity.equals(otherIncludePart.quantity);
        }
    }
}