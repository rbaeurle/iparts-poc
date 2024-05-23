/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsFactoryDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sort.BetterSortMap;

import java.util.*;

/**
 * Datenklasse für die Werkseinsatzdaten für einen Stücklisteneintrag aufbereitet (Filterung und Webservices).
 */
public class iPartsFactoryData {

    public static final long INVALID_DATE = -1;

    private Map<String, List<DataForFactory>> factoryDataMap; // Werkseinsatzdaten pro Werksnummer (TreeMap wg. Sortierung mit "Andisort")
    private boolean evalPemFrom;
    private boolean evalPemTo;
    private boolean hasFactoryDataWithInfiniteDates;

    /**
     * Liefert den übergebenen Datum-String im Format {@code yyyyMMdd} oder {@code yyyyMMddHHmmss} als Long-Wert im Format
     * {@code yyyyMMddHHmmss} zurück mit {@code 0} für einen leeren String (unendlich).
     *
     * @param dateString
     * @param context    Optionaler Kontext für die Fehlermeldung bei ungültigem Datum-String
     * @return
     */
    public static long getFactoryDateFromDateString(String dateString, String context) {
        long date = INVALID_DATE;
        if (dateString == null) {
            return INVALID_DATE;
        } else if (dateString.isEmpty()) {
            return 0;
        } else if (dateString.length() == 8) { // Format yyyyMMdd
            // Zielformat ist yyyyMMddHHmmss
            date = StrUtils.strToLongDef(StrUtils.padStringWithCharsUpToLength(dateString, '0', 14), INVALID_DATE);
        } else if (dateString.length() == 14) { // Format yyyyMMddHHmmss
            date = StrUtils.strToLongDef(dateString, INVALID_DATE);
        } else if (dateString.length() == 17) { // Format yyyyMMddHHmmssSSS
            date = StrUtils.strToLongDef(dateString.substring(0, 14), INVALID_DATE);
        }

        if (date == INVALID_DATE) {
            String errorText = "The date string '" + dateString + "' is no valid date in the format yyyyMMdd, yyyyMMddHHmmss or yyyyMMddHHmmssSSS";
            if (StrUtils.isValid(context)) {
                errorText += " (" + context + ")";
            }
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, errorText);
        }

        return date;
    }

    /**
     * Klont diese Werkseinsatzdaten.
     *
     * @return
     */
    public iPartsFactoryData cloneMe() {
        return cloneMe(null);
    }

    /**
     * Klont diese Werkseinsatzdaten, wobei NUR die Daten für die übergebene Werksnummer übernommen werden.
     *
     * @param factoryNumberOnly Bei leer oder {@code null} werden alle Daten übernommen
     * @return
     */
    public iPartsFactoryData cloneMe(String factoryNumberOnly) {
        boolean allFactoryNumbers = StrUtils.isEmpty(factoryNumberOnly);
        iPartsFactoryData clone = new iPartsFactoryData();
        if (hasValidFactories()) {
            for (Map.Entry<String, List<DataForFactory>> dataForFactoryEntry : factoryDataMap.entrySet()) {
                if (allFactoryNumbers || dataForFactoryEntry.getKey().equals(factoryNumberOnly)) {
                    List<DataForFactory> dataForFactoryCloneList = new DwList<>(dataForFactoryEntry.getValue().size());
                    for (DataForFactory dataForFactory : dataForFactoryEntry.getValue()) {
                        DataForFactory dataForFactoryClone = new DataForFactory();
                        dataForFactoryClone.assign(dataForFactory);
                        dataForFactoryCloneList.add(dataForFactoryClone);
                    }
                    clone.setDataForFactory(dataForFactoryEntry.getKey(), dataForFactoryCloneList);
                }
            }
        }
        clone.evalPemFrom = evalPemFrom;
        clone.evalPemTo = evalPemTo;
        clone.hasFactoryDataWithInfiniteDates = hasFactoryDataWithInfiniteDates;
        return clone;
    }

    /**
     * Liefert zurück, ob es Werkseinsatzdaten mit gültigen Werken gibt. Bei {@code false} gibt es zwar Werkseinsatzdaten,
     * aber nur solche für ungültige Werke.
     *
     * @return
     */
    public boolean hasValidFactories() {
        return factoryDataMap != null;
    }

    /**
     * Liefert zurück, ob es gültige Werkseinsatzdaten gibt. Bei {@code false} gibt es zwar Werkseinsatzdaten, aber nur
     * solche für ungültige Werke bzw. nur solche, die z.B. nach der Vererbung durch Ersetzungen keine PEM ab oder bis bzw.
     * ELDAS-Daten mehr haben.
     *
     * @return
     */
    public boolean hasValidFactoryData() {
        if (hasValidFactories()) {
            for (List<DataForFactory> dataForFactoryList : factoryDataMap.values()) {
                for (DataForFactory dataForFactory : dataForFactoryList) {
                    if ((isEvalPemFrom() && dataForFactory.hasPEMFrom()) || (isEvalPemTo() && dataForFactory.hasPEMTo())
                        || !StrUtils.isEmpty(dataForFactory.eldasFootNoteId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Liefert die unveränderliche Map von Werksnummer auf Werkseinsatzdaten für das entsprechende Werk zurück.
     *
     * @return {@code null} falls es keine Werkseinsatzdaten mit gültigen Werken gibt
     */
    public Map<String, List<DataForFactory>> getFactoryDataMap() {
        if (hasValidFactories()) {
            return Collections.unmodifiableMap(factoryDataMap);
        } else {
            return null;
        }
    }

    /**
     * Liefert alle Werkseinsatzdaten für das übergebene Werk zurück.
     *
     * @param factory
     * @return
     */
    public List<DataForFactory> getDataForFactory(String factory) {
        if (hasValidFactories()) {
            return factoryDataMap.get(factory);
        } else {
            return null;
        }
    }

    /**
     * Setzt alle Werkseinsatzdaten für das übergebene Werk.
     * Das muss dann ein gültiges Werk sein.
     *
     * @param factory
     * @param dataForFactory
     */
    public void setDataForFactory(String factory, List<DataForFactory> dataForFactory) {
        if (factoryDataMap == null) {
            factoryDataMap = new BetterSortMap<String, List<DataForFactory>>();
        }
        factoryDataMap.put(factory, dataForFactory);
    }

    /**
     * Ersetzt in den existierenden Werkseinsatzdaten nur die Teile, die für PEM ab oder PEM bis relevant sind.
     * Zuvor werden die bisherigen Daten für PEM ab oder bis gelöscht. Wenn Daten ersetzt wurden, wird auch das
     * Flag zum markieren vererbter Daten gesetzt.
     * Ersetzt werden folgende Daten: PEM, PEM Datum, Steuercode, Idents inkl. Ausreißer
     *
     * @param pemFactoryDataForRetail Die zu setzenden Daten (bei null oder fehlendem Inhalt wird nichts gemacht)
     * @param isPemFrom               Bei {@code true} werden PEM ab Daten ersetzt, bei {@code false} PEM bis Daten
     */
    public void setPemData(iPartsFactoryData pemFactoryDataForRetail, boolean isPemFrom) {
        if (factoryDataMap != null) {
            if (isPemFrom) {
                removeAllPemFromData();
            } else {
                removeAllPemToData();
            }
        }

        if ((pemFactoryDataForRetail != null) && (pemFactoryDataForRetail.getFactoryDataMap() != null)) {
            if (factoryDataMap == null) {
                factoryDataMap = new BetterSortMap<>();
            }

            for (Map.Entry<String, List<DataForFactory>> entry : pemFactoryDataForRetail.getFactoryDataMap().entrySet()) {
                String factory = entry.getKey();
                List<DataForFactory> pemFactoryData = entry.getValue();
                if ((pemFactoryData != null) && !pemFactoryData.isEmpty()) { // Diese Liste enthält immer nur ein Element
                    DataForFactory pemData = pemFactoryData.get(0);
                    List<DataForFactory> existingFactoryData = factoryDataMap.get(factory);
                    if (existingFactoryData == null) {
                        existingFactoryData = new DwList<>();
                        factoryDataMap.put(factory, existingFactoryData);
                    }
                    if (existingFactoryData.isEmpty()) {
                        // neuen Datensatz erstellen und initialisieren
                        DataForFactory emptyDataForFactory = new DataForFactory();
                        emptyDataForFactory.initWithEmptyValues();

                        // Alle relevanten Daten von den zu vererbenden Werkseinsatzdaten pemData übernehmen
                        emptyDataForFactory.adat = pemData.adat;
                        emptyDataForFactory.releaseState = pemData.releaseState;
                        emptyDataForFactory.dateFrom = 0; // zunächst unendlich (anstatt ungültig)
                        emptyDataForFactory.dateTo = 0; // zunächst unendlich (anstatt ungültig)
                        emptyDataForFactory.factoryDataId = pemData.factoryDataId;
                        emptyDataForFactory.seriesNumber = pemData.seriesNumber;
                        emptyDataForFactory.eldasFootNoteId = pemData.eldasFootNoteId;
                        emptyDataForFactory.filterInfo = pemData.filterInfo;

                        existingFactoryData.add(emptyDataForFactory);
                    }

                    // Setze das eine ermittelte PEM Datum usw. an alle vorhandenen Datensätze (normalerweise gibts nur einen)
                    for (DataForFactory existingData : existingFactoryData) {
                        if (isPemFrom) {
                            existingData.addFilterInfo(TranslationHandler.translate("!!PEM ab ersetzt durch Vererbung"));
                            existingData.pemFrom = pemData.pemFrom;
                            existingData.dateFrom = pemData.dateFrom;
                            existingData.stCodeFrom = pemData.stCodeFrom;
                            existingData.assignIdentsFrom(pemData);
                        } else {
                            existingData.addFilterInfo(TranslationHandler.translate("!!PEM bis ersetzt durch Vererbung"));
                            existingData.pemTo = pemData.pemTo;
                            existingData.dateTo = pemData.dateTo;
                            existingData.stCodeTo = pemData.stCodeTo;
                            existingData.assignIdentsTo(pemData);
                        }
                        existingData.setInherited(true);
                    }
                }
            }
        }
    }

    /**
     * Entfernt alle Werkseinsatzdaten für das übergebene Werk.
     *
     * @param factory
     */
    public void removeDataForFactory(String factory) {
        if (factoryDataMap != null) {
            factoryDataMap.remove(factory);
            if (factoryDataMap.isEmpty()) {
                factoryDataMap = null;
            }
        }
    }

    private void removeAllPemFromData() {
        if (factoryDataMap != null) {
            for (List<DataForFactory> dataForFactoryList : factoryDataMap.values()) {
                for (DataForFactory dataForFactory : dataForFactoryList) {
                    dataForFactory.pemFrom = "";
                    dataForFactory.dateFrom = INVALID_DATE;
                    dataForFactory.identsFrom = null;
                    dataForFactory.stCodeFrom = "";
                }
            }
        }
    }

    private void removeAllPemToData() {
        if (factoryDataMap != null) {
            for (List<DataForFactory> dataForFactoryList : factoryDataMap.values()) {
                for (DataForFactory dataForFactory : dataForFactoryList) {
                    dataForFactory.pemTo = "";
                    dataForFactory.dateTo = INVALID_DATE;
                    dataForFactory.identsTo = null;
                    dataForFactory.stCodeTo = "";
                }
            }
        }
    }

    public boolean isEvalPemFrom() {
        return evalPemFrom;
    }

    public void setEvalPemFrom(boolean evalPemFrom) {
        this.evalPemFrom = evalPemFrom;
    }

    public boolean isEvalPemTo() {
        return evalPemTo;
    }

    public void setEvalPemTo(boolean evalPemTo) {
        this.evalPemTo = evalPemTo;
    }

    /**
     * Flag, ob es Werkseinsatzdaten mit -/+ unendlich gibt (die ansonsten ignoriert werden und nur im Baumuster-Filter
     * eine Rolle spielen).
     *
     * @return
     */
    public boolean hasFactoryDataWithInfiniteDates() {
        return hasFactoryDataWithInfiniteDates;
    }

    /**
     * Flag, ob es Werkseinsatzdaten mit -/+ unendlich gibt (die ansonsten ignoriert werden und nur im Baumuster-Filter
     * eine Rolle spielen).
     *
     * @param hasFactoryDataWithInfiniteDates
     */
    public void setHasFactoryDataWithInfiniteDates(boolean hasFactoryDataWithInfiniteDates) {
        this.hasFactoryDataWithInfiniteDates = hasFactoryDataWithInfiniteDates;
    }

    /**
     * Liefert für das übergebene Werk den Werkseinsatzdatensatz mit dem höchsten ADAT. Optional können valide Statuswerte
     * übergeben werden, die der Datensatz mit dem höchsten ADAT haben muss.
     *
     * @param factory
     * @param validStates
     * @return
     */
    public DataForFactory getNewestDataFactoryForFactoryAndStates(String factory, iPartsDataReleaseState... validStates) {
        List<DataForFactory> datasForFactory = getDataForFactory(factory);
        if (datasForFactory != null) {
            if (datasForFactory.size() > 1) {
                Collections.sort(datasForFactory, new Comparator<DataForFactory>() {
                    @Override
                    public int compare(DataForFactory o1, DataForFactory o2) {
                        // Zuerst nach ADAT sortieren
                        long result = o1.adat - o2.adat;
                        if (result != 0) {
                            return (int)Math.signum(result); // -1 bzw. 1
                        }

                        // Danach nach Sequenznummer sortieren
                        return o1.factoryDataId.getSeqNo().compareTo(o2.factoryDataId.getSeqNo());
                    }
                });
            }
            if ((validStates != null) && (validStates.length > 0)) {
                for (int index = datasForFactory.size() - 1; index >= 0; index--) {
                    DataForFactory dataForFactory = datasForFactory.get(index);
                    iPartsDataReleaseState state = dataForFactory.releaseState;
                    for (iPartsDataReleaseState validState : validStates) {
                        if (validState == state) {
                            return dataForFactory;
                        }
                    }
                }
            } else {
                return datasForFactory.get(datasForFactory.size() - 1);
            }
        }
        return null;
    }

    /**
     * Abstrakte Klasse für Werkseinsatzdaten inkl. Rückmeldedaten und Ausreißern für ein konkretes Werk.
     */
    public static class AbstractDataForFactory {

        public long adat; // theoretisch redundant, da in factoryDataId ebenfalls enthalten (dort aber als String -> für die Filterung hier auch als long)
        public iPartsDataReleaseState releaseState;
        public String pemFrom;
        public String pemTo;
        public String stCodeFrom;
        public String stCodeTo;
        public long dateFrom = INVALID_DATE;
        public long dateTo = INVALID_DATE;
        public Map<IdentWithModelNumber, Set<String>> identsFrom; // Idents ab mit Baumusternummer inkl. dazu passende Ausreißer in den Sets
        public Map<IdentWithModelNumber, Set<String>> identsTo; // Idents bis mit Baumusternummer inkl. dazu passende Ausreißer in den Sets
        private boolean isInherited; // Handelt es sich um vererbte Daten (vom Vorgänger oder Nachfolger übernommen)

        public void assign(AbstractDataForFactory source) {
            adat = source.adat;
            releaseState = source.releaseState;
            pemFrom = source.pemFrom;
            pemTo = source.pemTo;
            stCodeFrom = source.stCodeFrom;
            stCodeTo = source.stCodeTo;
            dateFrom = source.dateFrom;
            dateTo = source.dateTo;

            assignIdentsFrom(source);
            assignIdentsTo(source);

            isInherited = source.isInherited;
        }

        public void initWithEmptyValues() {
            adat = INVALID_DATE;
            releaseState = iPartsDataReleaseState.RELEASED;
            pemFrom = "";
            pemTo = "";
            stCodeFrom = "";
            stCodeTo = "";
            dateFrom = INVALID_DATE;
            dateTo = INVALID_DATE;
            identsFrom = null;
            identsTo = null;
            isInherited = false;
        }

        public void assignIdentsFrom(AbstractDataForFactory source) {
            if (source.identsFrom != null) {
                identsFrom = new TreeMap<>();
                for (Map.Entry<IdentWithModelNumber, Set<String>> identEntry : source.identsFrom.entrySet()) {
                    Set<String> spikeIdents = identEntry.getValue();
                    if (spikeIdents != null) {
                        spikeIdents = new TreeSet<>(spikeIdents);
                    }
                    identsFrom.put(identEntry.getKey().cloneMe(), spikeIdents);
                }
            } else {
                identsFrom = null;
            }
        }

        public void assignIdentsTo(AbstractDataForFactory source) {
            if (source.identsTo != null) {
                identsTo = new TreeMap<>();
                for (Map.Entry<IdentWithModelNumber, Set<String>> identEntry : source.identsTo.entrySet()) {
                    Set<String> spikeIdents = identEntry.getValue();
                    if (spikeIdents != null) {
                        spikeIdents = new TreeSet<>(spikeIdents);
                    }
                    identsTo.put(identEntry.getKey().cloneMe(), spikeIdents);
                }
            } else {
                identsTo = null;
            }
        }

        public boolean hasPEMFrom() {
            return StrUtils.isValid(pemFrom);
        }

        public boolean hasPEMTo() {
            return StrUtils.isValid(pemTo);
        }

        public void setDateFrom(String dateFromString) {
            dateFrom = getFactoryDateFromDateString(dateFromString, "iPartsFactoryData.dateFrom");
        }

        public void setDateTo(String dateToString) {
            dateTo = getFactoryDateFromDateString(dateToString, "iPartsFactoryData.dateTo");
        }

        /**
         * Liefert das Enddatum inkl. unendlich bei 0 zurück.
         *
         * @return
         */
        public long getDateToWithInfinity() {
            if (dateTo == 0) {
                return Long.MAX_VALUE;
            } else {
                return dateTo;
            }
        }

        public boolean hasValidDateFrom() {
            return hasPEMFrom() && (dateFrom >= 0);
        }

        public boolean hasValidDateTo() {
            return hasPEMTo() && (dateTo >= 0);
        }

        public void addIdentFrom(String identFrom, String modelNumber, String steering, String eldasWMI, String eldasType, iPartsImportDataOrigin source, Set<String> spikeIdents) {
            if (StrUtils.isEmpty(identFrom)) {
                return;
            }

            if (identsFrom == null) {
                identsFrom = new TreeMap<>();
            }
            identsFrom.put(new IdentWithModelNumber(identFrom, modelNumber, steering, eldasWMI, eldasType, source), spikeIdents);
        }

        public void addIdentTo(String identTo, String modelNumber, String steering, String eldasWMI, String eldasType, iPartsImportDataOrigin source, Set<String> spikeIdents) {
            if (StrUtils.isEmpty(identTo)) {
                return;
            }

            if (identsTo == null) {
                identsTo = new TreeMap<>();
            }
            identsTo.put(new IdentWithModelNumber(identTo, modelNumber, steering, eldasWMI, eldasType, source), spikeIdents);
        }

        /**
         * Liste der {@link Ident}s ab bestehend aus Werkskennbuchstaben und Endnummern für die übergebene Endnummernlänge.
         *
         * @param endNumberLength
         * @return {@code null} falls keine Idents ab existieren
         */
        public List<Ident> getIdentsFrom(int endNumberLength) {
            if (isResponseDataAvailableForPEMFrom()) {
                List<Ident> identsFromList = new DwList<>(identsFrom.size());
                for (IdentWithModelNumber identWithModelFrom : identsFrom.keySet()) {
                    Ident ident = new Ident(identWithModelFrom.ident, endNumberLength);
                    identsFromList.add(ident);
                }
                return identsFromList;
            } else {
                return null;
            }
        }

        /**
         * Liste der {@link Ident}s bis bestehend aus Werkskennbuchstaben und Endnummern für die übergebene Endnummernlänge.
         *
         * @param endNumberLength
         * @return {@code null} falls keine Idents bis existieren
         */
        public List<Ident> getIdentsTo(int endNumberLength) {
            if (isResponseDataAvailableForPEMTo()) {
                List<Ident> identsToList = new DwList<>(identsTo.size());
                for (IdentWithModelNumber identWithModelTo : identsTo.keySet()) {
                    Ident ident = new Ident(identWithModelTo.ident, endNumberLength);
                    identsToList.add(ident);
                }
                return identsToList;
            } else {
                return null;
            }
        }

        public boolean isResponseDataAvailableForPEMFrom() {
            return (identsFrom != null) && !identsFrom.isEmpty();
        }

        public boolean isResponseDataAvailableForPEMTo() {
            return (identsTo != null) && !identsTo.isEmpty();
        }

        public void setInherited(boolean isInherited) {
            this.isInherited = isInherited;
        }

        public boolean isInherited() {
            return isInherited;
        }

    }


    /**
     * Werkseinsatzdaten inkl. Rückmeldedaten und Ausreißern für ein konkretes Werk.
     * Baureihe und Ausführungsart müssen schon vorher aufgrund des Stücklisteneintrags ausgefiltert werden für die Rückmeldedaten.
     */
    public static class DataForFactory extends AbstractDataForFactory {

        public iPartsFactoryDataId factoryDataId;
        public String seriesNumber;
        public String eldasFootNoteId;
        private String filterInfo = "";

        public void assign(DataForFactory source) {
            super.assign(source);
            factoryDataId = source.factoryDataId;
            seriesNumber = source.seriesNumber;
            eldasFootNoteId = source.eldasFootNoteId;
            filterInfo = source.filterInfo;
        }

        public void initWithEmptyValues() {
            super.initWithEmptyValues();
            factoryDataId = new iPartsFactoryDataId();
            seriesNumber = "";
            eldasFootNoteId = "";
            filterInfo = "";
        }

        public void addFilterInfo(String filterInfo) {
            if (!this.filterInfo.isEmpty()) {
                this.filterInfo += "; ";
            }
            this.filterInfo += filterInfo;
        }

        public String getFilterInfo() {
            return filterInfo;
        }
    }


    /**
     * Simple Datenstruktur bestehend aus einem Ident und der dazugehörigen optionalen Baumusternummer.
     */
    public static class IdentWithModelNumber implements Comparable<IdentWithModelNumber> {

        public String ident;
        public String model;
        public String steering;

        public String eldasWMI;
        public String eldasType; // z.B. AF,BF,AM,BM
        public iPartsImportDataOrigin source;

        public IdentWithModelNumber(String ident, String model, String steering, String eldasWMI, String eldasType, iPartsImportDataOrigin source) {
            this.ident = ident;
            if (model == null) {
                model = "";
            }
            this.model = model;
            if (steering == null) {
                steering = "";
            }
            this.steering = steering;
            if (eldasWMI == null) {
                eldasWMI = "";
            }
            this.eldasWMI = eldasWMI;
            if (eldasType == null) {
                eldasType = "";
            }
            this.eldasType = eldasType;
            if (source == null) {
                source = iPartsImportDataOrigin.UNKNOWN;
            }
            this.source = source;
        }

        public IdentWithModelNumber cloneMe() {
            return new IdentWithModelNumber(ident, model, steering, eldasWMI, eldasType, source);
        }

        @Override
        public int compareTo(IdentWithModelNumber o) {
            int result = ident.compareTo(o.ident);
            if (result == 0) {
                result = model.compareTo(o.model);
            }
            if (result == 0) {
                result = steering.compareTo(o.steering);
            }
            if (result == 0) {
                result = eldasWMI.compareTo(o.eldasWMI);
            }
            if (result == 0) {
                result = eldasType.compareTo(o.eldasType);
            }
            if (result == 0) {
                result = source.compareTo(o.source);
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IdentWithModelNumber)) {
                return false;
            }

            IdentWithModelNumber otherOIdentWithModelNumber = (IdentWithModelNumber)obj;
            return Utils.objectEquals(ident, otherOIdentWithModelNumber.ident) &&
                   Utils.objectEquals(model, otherOIdentWithModelNumber.model) &&
                   Utils.objectEquals(steering, otherOIdentWithModelNumber.steering) &&
                   Utils.objectEquals(eldasWMI, otherOIdentWithModelNumber.eldasWMI) &&
                   Utils.objectEquals(eldasType, otherOIdentWithModelNumber.eldasType) &&
                   source.equals(otherOIdentWithModelNumber.source);
        }

        @Override
        public int hashCode() {
            return ident.hashCode() + model.hashCode() + steering.hashCode() + eldasWMI.hashCode() + eldasType.hashCode() + source.hashCode();
        }

        @Override
        public String toString() {
            return ident + " (" + model + "; " + steering + "; " + eldasWMI + "; " + eldasType + "; " + source.getOrigin() + ")";
        }
    }


    /**
     * Ident bestehend aus Werkskennbuchstaben und Endnummer.
     */
    public static class Ident {

        public String factorySign = "";
        public int endNumber = FinId.INVALID_SERIAL_NUMBER;

        public Ident() {
        }

        public Ident(String identString, int endNumberLength) {
            if (StrUtils.isEmpty(identString)) {
                return;
            }

            if (identString.length() <= endNumberLength) { // Ident hat das Format "Endnummer" (oder ist sogar kürzer -> Problem mit alten Aggregate-Idents)
                endNumber = StrUtils.strToIntDef(identString, FinId.INVALID_SERIAL_NUMBER);
            } else if (identString.length() == (endNumberLength + 1)) { // Ident hat das Format "Werkskennbuchstabe+Endnummer"
                factorySign = identString.substring(0, 1);
                endNumber = StrUtils.strToIntDef(identString.substring(1), FinId.INVALID_SERIAL_NUMBER);
            }
        }
    }

    public enum ValidityType {
        VALID,
        VALID_FOR_CONSTRUCTION,
        VALID_NOT_ENDNUMBER_FILTER_RELEVANT,
        INVALID,
        NOT_AVAILABLE // keine Werkseinsatzdaten vorhanden
    }
}