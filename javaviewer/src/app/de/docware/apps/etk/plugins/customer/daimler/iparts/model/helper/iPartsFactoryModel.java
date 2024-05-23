/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactoryModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactoryModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Repräsentation der Werke und Baumusterpräfixe für die Berechnung vom Millionenüberlauf (DA_FACTORY_MODEL)
 */
public class iPartsFactoryModel implements iPartsConst {

    private static final char KEY_DELIMITER = '|';
    private static final String CKD_SPECIAL_FACTORY = "0501";
    private static final Set<String> CKD_STEERING_VALUES = new HashSet<>();

    static {
        CKD_STEERING_VALUES.add("5");
        CKD_STEERING_VALUES.add("6");
    }

    private static ObjectInstanceStrongLRUList<Object, iPartsFactoryModel> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    // Maps für die Werke
    protected Map<String, List<iPartsDataFactoryModel>> factoriesByWMIFactorySignAndAggType = new HashMap<String, List<iPartsDataFactoryModel>>();
    protected Map<String, List<iPartsDataFactoryModel>> factoriesByFactoryNumberSignAndAggType = new HashMap<String, List<iPartsDataFactoryModel>>();
    protected Map<String, List<iPartsDataFactoryModel>> additionalFactoriesByFactoryAndModel = new HashMap<String, List<iPartsDataFactoryModel>>();
    protected Map<String, List<iPartsDataFactoryModel>> mainFactoriesForAdditionalFactoryAndModel = new HashMap<String, List<iPartsDataFactoryModel>>();
    protected Map<String, Set<String>> factoryGrouping = new HashMap<>();

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsFactoryModel getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsFactoryModel.class, "FactoryModel", false);
        iPartsFactoryModel result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsFactoryModel();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        iPartsDataFactoryModelList dataFactoryModels = new iPartsDataFactoryModelList();
        dataFactoryModels.load(project);
        Map<String, Map<String, Set<String>>> factoryGroupingForGroupsign = new HashMap<>();
        for (iPartsDataFactoryModel dataFactoryModel : dataFactoryModels) {
            // Cache für WMI (WMI kann bei Aggregaten auch leer sein)
            String wmi = dataFactoryModel.getAsId().getWorldManufacturerIdentifier();
            String wmiKey = wmi + KEY_DELIMITER + dataFactoryModel.getAsId().getFactorySign() + KEY_DELIMITER + dataFactoryModel.getAsId().getAggType();
            List<iPartsDataFactoryModel> dataFactoryModelsForWMI = factoriesByWMIFactorySignAndAggType.get(wmiKey);
            if (dataFactoryModelsForWMI == null) {
                dataFactoryModelsForWMI = new DwList<iPartsDataFactoryModel>();
                factoriesByWMIFactorySignAndAggType.put(wmiKey, dataFactoryModelsForWMI);
            }
            dataFactoryModelsForWMI.add(dataFactoryModel);

            // Cache für Werksnummer
            String factoryNumber = dataFactoryModel.getAsId().getFactory();
            if (!factoryNumber.isEmpty()) {
                String factoryKey = factoryNumber + KEY_DELIMITER + dataFactoryModel.getAsId().getFactorySign()
                                    + KEY_DELIMITER + dataFactoryModel.getAsId().getAggType();
                List<iPartsDataFactoryModel> dataFactoryModelsForFactoryNumber = factoriesByFactoryNumberSignAndAggType.get(factoryKey);
                if (dataFactoryModelsForFactoryNumber == null) {
                    dataFactoryModelsForFactoryNumber = new DwList<iPartsDataFactoryModel>();
                    factoriesByFactoryNumberSignAndAggType.put(factoryKey, dataFactoryModelsForFactoryNumber);
                }
                dataFactoryModelsForFactoryNumber.add(dataFactoryModel);
            }

            if (!factoryNumber.isEmpty()) {
                // Cache für Zusatzwerke
                String additionalFactory = dataFactoryModel.getAsId().getAddFactory();
                if (!additionalFactory.isEmpty()) {
                    // Cache für die Zusatzwerke zum Werk (Map mit Werken, die jeweils eine Liste der Zusatzwerke enthält)
                    List<iPartsDataFactoryModel> addFactoryModelsForFactoryNumber = additionalFactoriesByFactoryAndModel.get(factoryNumber);
                    if (addFactoryModelsForFactoryNumber == null) {
                        addFactoryModelsForFactoryNumber = new DwList<iPartsDataFactoryModel>();
                        additionalFactoriesByFactoryAndModel.put(factoryNumber, addFactoryModelsForFactoryNumber);
                    }
                    addFactoryModelsForFactoryNumber.add(dataFactoryModel);

                    // Cache für die Hauptwerke zum Zusatzwerk (Map mit Zusatzwerken, die jeweils eine Liste der Hauptwerke enthält)
                    List<iPartsDataFactoryModel> mainFactoriesList = mainFactoriesForAdditionalFactoryAndModel.get(additionalFactory);
                    if (mainFactoriesList == null) {
                        mainFactoriesList = new DwList<iPartsDataFactoryModel>();
                        mainFactoriesForAdditionalFactoryAndModel.put(additionalFactory, mainFactoriesList);
                    }
                    mainFactoriesList.add(dataFactoryModel);
                }
            }
            if (dataFactoryModel.hasFactorySignGrouping()) {
                String factorySignGroup = dataFactoryModel.getFactorySignGroup();
                Map<String, Set<String>> tempFactoryGrouping = factoryGroupingForGroupsign.get(factorySignGroup);
                if (tempFactoryGrouping == null) {
                    tempFactoryGrouping = new HashMap<>();
                    factoryGroupingForGroupsign.put(factorySignGroup, tempFactoryGrouping);
                }

                String modelPrefix = dataFactoryModel.getAsId().getModelNumberPrefix();
                String key = makeFactoryGroupingKey(dataFactoryModel.getAsId().getWorldManufacturerIdentifier(), "",
                                                    dataFactoryModel.getAsId().getFactory(), modelPrefix);

                Set<String> grouping = tempFactoryGrouping.get(key);
                if (grouping == null) {
                    grouping = new HashSet<>();
                    tempFactoryGrouping.put(key, grouping);
                }
                grouping.add(dataFactoryModel.getAsId().getFactorySign());

                key = makeFactoryGroupingKey(dataFactoryModel.getAsId().getWorldManufacturerIdentifier(), dataFactoryModel.getAsId().getFactorySign(),
                                             dataFactoryModel.getAsId().getFactory(), modelPrefix);
                factoryGrouping.put(key, grouping);
            }
        }
    }

    private String makeFactoryGroupingKey(String wmi, String factorySign, String factoryNumber, String modelPrefix) {
        String key = wmi + KEY_DELIMITER + factoryNumber;
        if (StrUtils.isValid(factorySign)) {
            key += KEY_DELIMITER + factorySign;
        }
        if (StrUtils.isValid(modelPrefix)) {
            key += KEY_DELIMITER + modelPrefix;
        }
        return key;
    }

    private String makeFactoryGroupingKey(String wmi, String factorySign, String factoryNumber, iPartsModelId modelId) {
        if (modelId != null) {
            return makeFactoryGroupingKey(wmi, factorySign, factoryNumber, modelId.getModelTypeNumber());
        }
        return makeFactoryGroupingKey(wmi, factorySign, factoryNumber, "");
    }

    /**
     * Liefert Werksnummer aus dem Cache für einen gegebenen WMI, Werkskennbuchstaben und optionales Baumuster für Fahrzeug
     *
     * @param wmi
     * @param factorySign
     * @param modelId
     * @param steeringNumberFromFIN - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return
     */
    public String getFactoryNumberForWMI(String wmi, String factorySign, iPartsModelId modelId, String steeringNumberFromFIN) {
        return getFactoryNumberForWMI(wmi, factorySign, modelId, AGGREGATE_TYPE_CAR, steeringNumberFromFIN);
    }

    /**
     * Liefert Werksnummer aus dem Cache für einen gegebenen Aggregatetyp, Werkskennbuchstaben und optionales Baumuster für Aggregat
     *
     * @param aggregateType
     * @param factorySign
     * @param modelId
     * @return
     */
    public String getFactoryNumberForAggregate(String aggregateType, String factorySign, iPartsModelId modelId) {
        return getFactoryNumberForWMI("", factorySign, modelId, aggregateType, "");
    }

    /**
     * Liefert die Werksnummer aus dem Cache für einen gegebenen WMI, Werkskennbuchstaben, optionales Baumuster und Aggregatetyp
     *
     * @param wmi                   WMI
     * @param factorySign           Werkskennbuchstabe
     * @param modelId               Baumuster
     * @param steeringNumberFromFIN - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return Werksnummer
     */
    public String getFactoryNumberForWMI(String wmi, String factorySign, iPartsModelId modelId, String aggregateType, String steeringNumberFromFIN) {
        // Bandkennzahl und Bandbündelung sind irrelevant für die Werksnummer (da diese nur vom WMI, Werkskennbuchstaben,
        // Baumuster und Aggregatetyp abhängig ist)
        iPartsDataFactoryModel dataFactoryModel = getDataFactoryModelForWMI(wmi, factorySign, modelId, aggregateType,
                                                                            FinId.INVALID_SERIAL_NUMBER, 0, steeringNumberFromFIN);
        if (dataFactoryModel != null) {
            return dataFactoryModel.getAsId().getFactory();
        } else {
            return null;
        }
    }

    /**
     * Liefert alle Werkskennbuchstaben für eine gegebene Werksnummer, optionale Baureihe und optionalen Aggregatetyp zurück.
     *
     * @param factoryNumber Werksnummer
     * @param seriesId      Baureihe
     * @param aggregateType Optionaler Aggregatetyp
     * @return
     */
    public Set<String> getFactorySignsForFactoryNumberAndSeries(String factoryNumber, iPartsSeriesId seriesId, String aggregateType) {
        Set<String> factorySigns = new TreeSet<String>();
        Set<String> defaultFactorySigns = new TreeSet<String>();

        String seriesNumber = null;
        if (seriesId != null) {
            seriesNumber = seriesId.getSeriesNumber();
        }

        for (Map.Entry<String, List<iPartsDataFactoryModel>> factoryNumberSignAndAggTypeEntry : factoriesByFactoryNumberSignAndAggType.entrySet()) {
            String factoryNumberSignAndAggType = factoryNumberSignAndAggTypeEntry.getKey();
            if (factoryNumberSignAndAggType.startsWith(factoryNumber + KEY_DELIMITER)
                && (StrUtils.isEmpty(aggregateType) || factoryNumberSignAndAggType.endsWith(KEY_DELIMITER + aggregateType))) {
                boolean seriesMatch = false;
                boolean defaultEntry = false;
                for (iPartsDataFactoryModel dataFactoryModel : factoryNumberSignAndAggTypeEntry.getValue()) {
                    // Baureihe (Baumusterpräfix) aus den Daten holen
                    String modelPrefix = dataFactoryModel.getFieldValue(FIELD_DFM_MODEL_PREFIX);
                    if ((seriesNumber == null) && modelPrefix.isEmpty()) { // Baureihe leer
                        seriesMatch = true;
                        break;
                    } else if (!StrUtils.isEmpty(seriesNumber) && seriesNumber.equals(modelPrefix)) { // identische Baureihe
                        seriesMatch = true;
                        break;
                    } else if (modelPrefix.isEmpty()) {
                        defaultEntry = true;
                    }
                }

                if (seriesMatch || defaultEntry) {
                    // Werkskennbuchstabe hinzufügen (befindet sich zwischen Werksnummer und Aggregatetyp getrennt durch KEY_DELIMITER)
                    String factorySignAndAggType = StrUtils.stringAfterCharacter(factoryNumberSignAndAggType, factoryNumber + KEY_DELIMITER);
                    String factorySign = StrUtils.stringUpToCharacter(factorySignAndAggType, KEY_DELIMITER);
                    if (seriesMatch) {
                        factorySigns.add(factorySign);
                    } else if (defaultEntry) {
                        defaultFactorySigns.add(factorySign);
                    }
                }
            }
        }

        // Falls keine Werkskennbuchstaben mit der übergebenen Baureihe gefunden wurden, die Werkskennbuchstaben ohne
        // Baureihe (Baumusterpräfix) zurückgeben
        if (!factorySigns.isEmpty()) {
            return factorySigns;
        } else {
            return defaultFactorySigns;
        }
    }

    /**
     * Liefert einen kommaseparierten String mit allen Werkskennbuchstaben für eine gegebene Werksnummer, optionale Baureihe
     * und optionalen Aggregatetyp zurück inkl. CKD-Kenner für Werksnummer {@code 0501}.
     *
     * @param factoryNumber Werksnummer
     * @param seriesId      Baureihe
     * @param aggregateType Optionaler Aggregatetyp
     * @return
     */
    public String getFactorySignsStringForFactoryNumberAndSeries(String factoryNumber, iPartsSeriesId seriesId, String aggregateType) {
        String factorySignsString = StrUtils.stringListToString(getFactorySignsForFactoryNumberAndSeries(factoryNumber, seriesId,
                                                                                                         aggregateType), ", ");

        // CKD-Kennung bei Werksnummer 0501
        if (factoryNumber.equals("0501")) {
            factorySignsString += " (CKD)";
        }

        return factorySignsString;
    }

    /**
     * Liefert die berechnete Seriennummer inkl. Millionenüberlauf mit Hilfe vom Cache für einen gegebenen WMI, Werkskennbuchstaben,
     * optionales Baumuster und Seriennummer für ein Fahrzeug
     *
     * @param wmi
     * @param factorySign
     * @param modelId
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @param steeringNumberFromFIN       - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return
     */
    public int getSerialNumberWithOverflowForWMI(String wmi, String factorySign, iPartsModelId modelId, int serialNumberWithoutOverflow,
                                                 int serialNumberLength, String steeringNumberFromFIN) {
        return getSerialNumberWithOverflowForWMI(wmi, factorySign, modelId, AGGREGATE_TYPE_CAR, serialNumberWithoutOverflow,
                                                 serialNumberLength, steeringNumberFromFIN);
    }

    /**
     * Liefert die berechnete Seriennummer inkl. Millionenüberlauf mit Hilfe vom Cache für einen gegebenen WMI, Werkskennbuchstaben,
     * optionales Baumuster, Aggregatetyp und Seriennummer.
     *
     * @param wmi                         WMI
     * @param factorySign                 Werkskennbuchstabe
     * @param modelId                     Baumuster
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @param steeringNumberFromFIN       - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return {@link FinId#INVALID_SERIAL_NUMBER} falls keine Seriennummer inkl. Millionenüberlauf berechnet werden konnte;
     * die Seriennummer ohne den Millionenüberlauf falls der Werkskennbuchstabe {@code null} bzw. leer ist
     */
    public int getSerialNumberWithOverflowForWMI(String wmi, String factorySign, iPartsModelId modelId, String aggregateType,
                                                 int serialNumberWithoutOverflow, int serialNumberLength, String steeringNumberFromFIN) {
        SerialNoAndFactory serialNoAndFactory = getSerialNumberWithOverflowAndFactoryForWMIOrAggregate(wmi, factorySign, modelId,
                                                                                                       aggregateType, serialNumberWithoutOverflow,
                                                                                                       serialNumberLength, steeringNumberFromFIN);
        return serialNoAndFactory.serialNumber;
    }

    /**
     * Seriennummer mit Millonenüberlauf und Werk für Fahrzeug
     *
     * @param wmi
     * @param factorySign
     * @param modelId
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @param steeringNumberFromFIN       - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return
     */
    public SerialNoAndFactory getSerialNumberWithOverflowAndFactoryForWMI(String wmi, String factorySign, iPartsModelId modelId,
                                                                          int serialNumberWithoutOverflow, int serialNumberLength,
                                                                          String steeringNumberFromFIN) {
        return getSerialNumberWithOverflowAndFactoryForWMIOrAggregate(wmi, factorySign, modelId, AGGREGATE_TYPE_CAR,
                                                                      serialNumberWithoutOverflow, serialNumberLength,
                                                                      steeringNumberFromFIN);
    }

    /**
     * Seriennummer mit Millonenüberlauf und Werk für Aggregat
     *
     * @param factorySign
     * @param modelId
     * @param aggregateType
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @return
     */
    public SerialNoAndFactory getSerialNumberWithOverflowAndFactoryForAggregate(String factorySign, iPartsModelId modelId, String aggregateType,
                                                                                int serialNumberWithoutOverflow, int serialNumberLength) {
        return getSerialNumberWithOverflowAndFactoryForWMIOrAggregate("", factorySign, modelId, aggregateType, serialNumberWithoutOverflow,
                                                                      serialNumberLength, "");
    }

    /**
     * Seriennummer mit Millonenüberlauf und Werk für Fahrzeug oder Aggregat
     *
     * @param wmi
     * @param factorySign
     * @param modelId
     * @param aggregateType
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @param steeringNumberFromFIN       - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return
     */
    public SerialNoAndFactory getSerialNumberWithOverflowAndFactoryForWMIOrAggregate(String wmi, String factorySign, iPartsModelId modelId,
                                                                                     String aggregateType, int serialNumberWithoutOverflow,
                                                                                     int serialNumberLength, String steeringNumberFromFIN) {
        if (serialNumberWithoutOverflow == FinId.INVALID_SERIAL_NUMBER) {
            return new SerialNoAndFactory();
        }

        if (StrUtils.isEmpty(factorySign)) {
            return new SerialNoAndFactory(serialNumberWithoutOverflow, "", "");
        }

        iPartsDataFactoryModel dataFactoryModel = getDataFactoryModelForWMI(wmi, factorySign, modelId, aggregateType,
                                                                            serialNumberWithoutOverflow, serialNumberLength,
                                                                            steeringNumberFromFIN);
        return getSerialNumberWithOverflowAndFactory(dataFactoryModel, serialNumberWithoutOverflow, serialNumberLength);
    }

    /**
     * Liefert die berechnete Seriennummer inkl. Millionenüberlauf mit Hilfe vom Cache für eine gegebenen Werksnummer,
     * Werkskennbuchstaben, optionales Baumuster und Seriennummer.
     *
     * @param factoryNumber               Werksnummer
     * @param factorySign                 Werkskennbuchstabe
     * @param modelId                     Baumuster
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @return {@link FinId#INVALID_SERIAL_NUMBER} falls keine Seriennummer inkl. Millionenüberlauf berechnet werden konnte;
     * die Seriennummer ohne den Millionenüberlauf falls der Werkskennbuchstabe {@code null} bzw. leer ist
     */
    public int getSerialNumberWithOverflowForFactoryNumber(String factoryNumber, String factorySign, iPartsModelId modelId, String aggregateType,
                                                           int serialNumberWithoutOverflow, int serialNumberLength) {
        if (serialNumberWithoutOverflow == FinId.INVALID_SERIAL_NUMBER) {
            return FinId.INVALID_SERIAL_NUMBER;
        }

        if (StrUtils.isEmpty(factorySign)) {
            return serialNumberWithoutOverflow;
        }

        iPartsDataFactoryModel dataFactoryModel = getDataFactoryModelForFactoryNumber(factoryNumber, factorySign, modelId,
                                                                                      aggregateType, serialNumberWithoutOverflow,
                                                                                      serialNumberLength);
        return getSerialNumberWithOverflow(dataFactoryModel, serialNumberWithoutOverflow, serialNumberLength);
    }

    /**
     * Liefert die berechnete Seriennummer inkl. Millionenüberlauf mit Hilfe vom Cache für das übergebene {@link iPartsDataFactoryModel}
     *
     * @param dataFactoryModel
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @return {@link FinId#INVALID_SERIAL_NUMBER} falls keine Seriennummer inkl. Millionenüberlauf berechnet werden konnte
     */
    private int getSerialNumberWithOverflow(iPartsDataFactoryModel dataFactoryModel, int serialNumberWithoutOverflow, int serialNumberLength) {
        SerialNoAndFactory serialNoAndFactory = getSerialNumberWithOverflowAndFactory(dataFactoryModel, serialNumberWithoutOverflow,
                                                                                      serialNumberLength);
        return serialNoAndFactory.serialNumber;
    }

    private SerialNoAndFactory getSerialNumberWithOverflowAndFactory(iPartsDataFactoryModel dataFactoryModel, int serialNumberWithoutOverflow,
                                                                     int serialNumberLength) {
        if (serialNumberWithoutOverflow == FinId.INVALID_SERIAL_NUMBER) {
            return new SerialNoAndFactory();
        }

        if (dataFactoryModel != null) {
            int seqNo = Math.max(1, dataFactoryModel.getFieldValueAsInteger(FIELD_DFM_SEQ_NO));

            // Bei einem Datensatz mit Bandsteuerung die Seriennummernlänge um 1 Zeichen verkürzen für die Bandkennzahl
            if (dataFactoryModel.hasBeltControl()) {
                serialNumberLength--;
            }

            // Millionenüberlauf anhand der Reihenfolgenummer beginnend bei 1 berechnen unter Berücksichtigung von serialNumberLength
            int serialNumber;
            if (serialNumberLength >= 1) {
                int overflow = (int)Math.pow(10, serialNumberLength); // Millionenüberlauf

                // Modulo overflow für das Entfernen der Bandkennzahl aus der Seriennummer bei Bandsteuerung; kann aber
                // auch sonst nicht schaden, da die Seriennummer ja nicht größer als der Seriennummernlänge und damit der
                // Millionenüberlauf sein darf
                serialNumber = (seqNo - 1) * overflow + serialNumberWithoutOverflow % overflow;
            } else {
                serialNumber = serialNumberWithoutOverflow;
            }

            String factoryNumber = dataFactoryModel.getFieldValue(FIELD_DFM_FACTORY);
            return new SerialNoAndFactory(serialNumber, factoryNumber, dataFactoryModel.getAsId().getBeltGrouping());
        } else {
            return new SerialNoAndFactory();
        }
    }

    /**
     * Wird eine Bandsteuerung verwendet für den gegebenen WMI, Werkskennbuchstaben, optionales Baumuster, Aggregatetyp
     * und Seriennummer?
     *
     * @param wmi
     * @param factorySign
     * @param modelId
     * @param aggregateType
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @param steeringNumberFromFIN       - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return
     */
    public boolean hasBeltControl(String wmi, String factorySign, iPartsModelId modelId, String aggregateType, int serialNumberWithoutOverflow,
                                  int serialNumberLength, String steeringNumberFromFIN) {
        List<iPartsDataFactoryModel> dataFactoryModels = factoriesByWMIFactorySignAndAggType.get(wmi + KEY_DELIMITER + factorySign
                                                                                                 + KEY_DELIMITER + aggregateType);
        iPartsDataFactoryModel dataFactoryModel = getDataFactoryModel(dataFactoryModels, modelId, serialNumberWithoutOverflow,
                                                                      serialNumberLength, aggregateType, steeringNumberFromFIN);
        if (dataFactoryModel != null) {
            return dataFactoryModel.hasBeltControl();
        } else {
            return false;
        }
    }

    /**
     * Liefert die Bandkennzahl aus dem Cache für einen gegebenen WMI, Werkskennbuchstaben, optionales Baumuster, Aggregatetyp
     * und Seriennummer.
     *
     * @param wmi
     * @param factorySign
     * @param modelId
     * @param aggregateType
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @param steeringNumberFromFIN       - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return
     */
    public String getBeltSign(String wmi, String factorySign, iPartsModelId modelId, String aggregateType, int serialNumberWithoutOverflow,
                              int serialNumberLength, String steeringNumberFromFIN) {
        iPartsDataFactoryModel dataFactoryModel = getDataFactoryModelForWMI(wmi, factorySign, modelId, aggregateType,
                                                                            serialNumberWithoutOverflow, serialNumberLength,
                                                                            steeringNumberFromFIN);
        if (dataFactoryModel != null) {
            return dataFactoryModel.getAsId().getBeltSign();
        } else {
            return null;
        }
    }

    /**
     * Liefert die Bandbündelung aus dem Cache für einen gegebenen WMI, Werkskennbuchstaben, optionales Baumuster, Aggregatetyp
     * und Seriennummer.
     *
     * @param wmi
     * @param factorySign
     * @param modelId
     * @param aggregateType
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @param steeringNumberFromFIN       - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return
     */
    public String getBeltGrouping(String wmi, String factorySign, iPartsModelId modelId, String aggregateType, int serialNumberWithoutOverflow,
                                  int serialNumberLength, String steeringNumberFromFIN) {
        iPartsDataFactoryModel dataFactoryModel = getDataFactoryModelForWMI(wmi, factorySign, modelId, aggregateType,
                                                                            serialNumberWithoutOverflow, serialNumberLength,
                                                                            steeringNumberFromFIN);
        if (dataFactoryModel != null) {
            return dataFactoryModel.getAsId().getBeltGrouping();
        } else {
            return null;
        }
    }

    /**
     * Liefert das passende {@link iPartsDataFactoryModel}-Objekt aus dem Cache für einen gegebenen WMI, Werkskennbuchstaben,
     * optionales Baumuster, Aggregatetyp und Seriennummer.
     *
     * @param wmi                         WMI
     * @param factorySign                 Werkskennbuchstabe
     * @param modelId                     Baumuster
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @param steeringNumberFromFIN       - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return
     */
    public iPartsDataFactoryModel getDataFactoryModelForWMI(String wmi, String factorySign, iPartsModelId modelId, String aggregateType,
                                                            int serialNumberWithoutOverflow, int serialNumberLength, String steeringNumberFromFIN) {
        List<iPartsDataFactoryModel> dataFactoryModels = factoriesByWMIFactorySignAndAggType.get(wmi + KEY_DELIMITER + factorySign
                                                                                                 + KEY_DELIMITER + aggregateType);
        return getDataFactoryModel(dataFactoryModels, modelId, serialNumberWithoutOverflow, serialNumberLength, aggregateType, steeringNumberFromFIN);
    }

    /**
     * Liefert das passende {@link iPartsDataFactoryModel}-Objekt aus dem Cache für eine gegebene Werksnummer, Werkskennbuchstaben,
     * optionales Baumuster, Aggregatetyp und Seriennummer.
     *
     * @param factoryNumber               Werksnummer
     * @param factorySign                 Werkskennbuchstabe
     * @param modelId                     Baumuster
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @return
     */
    public iPartsDataFactoryModel getDataFactoryModelForFactoryNumber(String factoryNumber, String factorySign, iPartsModelId modelId,
                                                                      String aggregateType, int serialNumberWithoutOverflow,
                                                                      int serialNumberLength) {
        List<iPartsDataFactoryModel> dataFactoryModels = factoriesByFactoryNumberSignAndAggType.get(factoryNumber + KEY_DELIMITER + factorySign
                                                                                                    + KEY_DELIMITER + aggregateType);
        return getDataFactoryModel(dataFactoryModels, modelId, serialNumberWithoutOverflow, serialNumberLength, aggregateType, "");
    }

    /**
     * Liefert das passende {@link iPartsDataFactoryModel}-Objekt aus der übergebenen Liste für das optionale Baumuster
     * und Seriennummer (nur relevant für Bandsteuerung).
     *
     * @param dataFactoryModels
     * @param modelId
     * @param serialNumberWithoutOverflow Seriennummer ohne den Millionenüberlauf
     * @param serialNumberLength
     * @param steeringNumberFromFIN       - WICHTIG: Die Original Lenkungsnummer aus der FIN (nicht "L" oder "R")
     * @return
     */
    private iPartsDataFactoryModel getDataFactoryModel(List<iPartsDataFactoryModel> dataFactoryModels, iPartsModelId modelId,
                                                       int serialNumberWithoutOverflow, int serialNumberLength, String aggType,
                                                       String steeringNumberFromFIN) {
        iPartsDataFactoryModel foundFactoryModel = null;
        if (dataFactoryModels != null) {
            String modelNumber = null;
            if (modelId != null) {
                modelNumber = modelId.getModelNumber();
            }

            String beltSignFromSerialNumber = null;
            // Kenner für die Sonderbehandlung von CKD Fahrzeugen
            boolean isSpecialCKDCase = aggType.equals(iPartsConst.AGGREGATE_TYPE_CAR)
                                       && SteeringIdentKeys.getSteeringIdentKeyByValue(steeringNumberFromFIN).isValidLeftOrRightSteering();
            boolean isSteeringForSpecialCKDFactory = isSpecialCKDCase && CKD_STEERING_VALUES.contains(steeringNumberFromFIN);

            for (iPartsDataFactoryModel dataFactoryModel : dataFactoryModels) {
                // Datensatz mit Bandsteuerung?
                if (dataFactoryModel.hasBeltControl() && (serialNumberWithoutOverflow != FinId.INVALID_SERIAL_NUMBER)
                    && (serialNumberLength >= 1)) {
                    if (beltSignFromSerialNumber == null) {
                        // Seriennummer falls notwendig vorne mit 0 auffüllen bis die gewünschte Seriennummernlänge erreicht ist
                        // -> die erste Ziffer davon ist dann die Bandkennzahl
                        String serialNumberString = StrUtils.prefixStringWithCharsUpToLength(String.valueOf(serialNumberWithoutOverflow),
                                                                                             '0', serialNumberLength);
                        beltSignFromSerialNumber = serialNumberString.substring(0, 1);
                    }

                    // Vergleich der Bandkennzahl vom Datensatz mit der Bandkennzahl von der Seriennummer
                    if (!dataFactoryModel.getAsId().getBeltSign().equals(beltSignFromSerialNumber)) {
                        continue;
                    }
                }
                // Sonderlogik für CKD Fahrzeuge (Aggregattyp "Fahrzeug" und valide Lenkungsnummer (aus der FIN extrahiert):
                // Wenn Lenkung auf Datenkarte "5" oder "6", dann nehmen wir nur die Daten zum Werk 0501.
                // Bei Lenkung != "5" und Lenkung != "6" nehmen wir nur die Daten zu Werken != 0501.
                if (isSpecialCKDCase) {
                    String factory = dataFactoryModel.getAsId().getFactory();
                    boolean isSpecialCKDFactory = factory.equals(CKD_SPECIAL_FACTORY);
                    if ((isSteeringForSpecialCKDFactory && !isSpecialCKDFactory) || (!isSteeringForSpecialCKDFactory && isSpecialCKDFactory)) {
                        continue;
                    }
                }

                String modelPrefix = dataFactoryModel.getFieldValue(FIELD_DFM_MODEL_PREFIX);
                if (modelPrefix.isEmpty() && (foundFactoryModel == null)) {
                    foundFactoryModel = dataFactoryModel; // Treffer ohne Berücksichtigung vom Baumuster
                } else if (!StrUtils.isEmpty(modelNumber) && modelNumber.startsWith(modelPrefix)) {
                    return dataFactoryModel; // bester Treffer inkl. Berücksichtigung vom Baumuster
                }
            }
            // Wenn es sich um einen speziellen CKD Sonderfall handelt und wir für das Werk 0501 nichts gefunden haben,
            // dann suche nach vorhandenen Daten zu Werken != "0501"
            if (isSpecialCKDCase && isSteeringForSpecialCKDFactory && (foundFactoryModel == null)) {
                foundFactoryModel = getDataFactoryModel(dataFactoryModels, modelId, serialNumberWithoutOverflow, serialNumberLength, aggType, "");
            }
        }

        return foundFactoryModel;
    }

    /**
     * Gibt alle Zusatzwerke zur übergebenen Werksnummer und optionalen Baureihe zurück.
     * Falls nach keiner Baureihe gesucht wird, werden alle passenden Zusatzwerke zurückgegeben.
     *
     * @param factoryNumber
     * @param seriesId
     * @return
     */
    public Collection<String> getAdditionalFactoriesForFactory(String factoryNumber, iPartsSeriesId seriesId) {
        // Holt sich die Liste der Zusatzwerke passend zum übergebenen Werk.
        List<iPartsDataFactoryModel> dataFactoryModels = additionalFactoriesByFactoryAndModel.get(factoryNumber);
        return getAdditionalOrMainFactories(dataFactoryModels, seriesId, true);
    }

    /**
     * Suche das Hauptwerk zum übergebenen Zusatzwerk und optionaler Baureihe. Wird kein Hauptwerk gefunden, dann wird
     * {@code null} zurückgegeben.
     * Falls nach keiner Baureihe gesucht wird, wird nur ein Hauptwerk ohne Baumusterpräfix (Baureihe) zurückgegeben.
     *
     * @param additionalFactoryNumber
     * @param seriesId
     * @return
     */
    public String getMainFactoryForAdditionalFactory(String additionalFactoryNumber, iPartsSeriesId seriesId) {
        // Holt sich die Liste der Hauptwerke passend zum übergebenen Zusatzwerk.
        List<iPartsDataFactoryModel> dataFactoryModels = mainFactoriesForAdditionalFactoryAndModel.get(additionalFactoryNumber);
        Collection<String> mainFactories = getAdditionalOrMainFactories(dataFactoryModels, seriesId, false);
        if (!mainFactories.isEmpty()) {
            return mainFactories.iterator().next();
        } else {
            return null;
        }
    }

    private Collection<String> getAdditionalOrMainFactories(List<iPartsDataFactoryModel> dataFactoryModels, iPartsSeriesId seriesId,
                                                            boolean returnAdditionalFactories) {
        Set<String> resultSet = new TreeSet<String>();
        if (dataFactoryModels != null) {
            // Die Baureihennummer bestimmen
            String seriesNumber = null;
            if (seriesId != null) {
                seriesNumber = seriesId.getSeriesNumber();
            }

            // Jetzt alle Elemente der Liste checken, ob die Baureihe passt (oder egal, wenn nicht nach einer Baureihe gesucht wird).
            for (iPartsDataFactoryModel dataFactoryModel : dataFactoryModels) {
                // Baureihe (Baumusterpräfix) und Werksnummer aus den Daten holen
                String modelPrefix = dataFactoryModel.getFieldValue(FIELD_DFM_MODEL_PREFIX);
                String factoryNumber;
                if (returnAdditionalFactories) {
                    factoryNumber = dataFactoryModel.getAsId().getAddFactory();
                } else {
                    factoryNumber = dataFactoryModel.getAsId().getFactory();
                }

                // Wenn nicht nach einer Baureihe gesucht wird, alle Werke zum Ergebnis hinzufügen falls nach
                // Zusatzwerken gesucht wird bzw. falls der Baumusterpräfix leer ist
                if ((seriesNumber == null) && (returnAdditionalFactories || modelPrefix.isEmpty())) {
                    resultSet.add(factoryNumber);
                } else if (!StrUtils.isEmpty(seriesNumber) && seriesNumber.equals(modelPrefix)) { // Werke mit identischer Baureihe zum Ergebnis hinzufügen
                    resultSet.add(factoryNumber);
                }
            }
        }
        return resultSet;
    }

    public Set<String> getFactorySignGrouping(String wmi, String factorySignFromDatacard, iPartsModelId modelId, String factoryNumber) {
        String key = makeFactoryGroupingKey(wmi, factorySignFromDatacard, factoryNumber, modelId);
        Set<String> grouping = factoryGrouping.get(key);
        if (grouping == null) {
            key = makeFactoryGroupingKey(wmi, factorySignFromDatacard, factoryNumber, "");
            grouping = factoryGrouping.get(key);
        }
        return grouping;
    }

    /**
     * Bündelt die aus WMI-Abfrage ermittelte Seriennummer mit Millionenüberlauf mit dem Werk.
     * Das später hinzugekommene Kennzeichen für die Bandbündelung kann dabei als ein Unterwerk
     * verstanden werden und dient demselben Zweck.
     * Die Bündelung der Seriennummer mit dem Werk ist im Endnummernfilter nötig, damit nur Idents
     * aus zusammengehörenden Werken bzw. Bändern verglichen werden.
     */
    public static class SerialNoAndFactory implements RESTfulTransferObjectInterface {

        @JsonProperty
        private int serialNumber = FinId.INVALID_SERIAL_NUMBER; // aus WMI-Abfrage ermittelte Seriennummer mit Millionenüberlauf
        @JsonProperty
        private String factoryNumber = "";  // Werksnummer aus WMI-Tabelle
        @JsonProperty
        private String beltGrouping = "";   // Bandbündelung aus WMI-Tabelle

        public SerialNoAndFactory() {
        }

        public SerialNoAndFactory(int serialNumber, String factoryNumber, String beltGrouping) {
            this.serialNumber = serialNumber;
            this.factoryNumber = factoryNumber;
            this.beltGrouping = beltGrouping;
        }

        public int getSerialNumber() {
            return serialNumber;
        }

        public boolean isValid() {
            return getSerialNumber() != FinId.INVALID_SERIAL_NUMBER;
        }

        /**
         * Vergleicht Werk und Band des Idents mit einem anderen Ident.
         *
         * @param compareSerialNoAndFactory
         * @param emptyFactoryAllowed       wenn true werden leere Werksnummern als valide interpretiert
         * @return
         */
        public boolean compareFactory(SerialNoAndFactory compareSerialNoAndFactory, boolean emptyFactoryAllowed) {
            if (emptyFactoryAllowed && (compareSerialNoAndFactory.factoryNumber.isEmpty() || factoryNumber.isEmpty())) {
                return true;
            }
            return compareSerialNoAndFactory.factoryNumber.equals(factoryNumber) && compareSerialNoAndFactory.beltGrouping.equals(beltGrouping);
        }

        public String getFactoryNumber() {
            return factoryNumber;
        }
    }
}