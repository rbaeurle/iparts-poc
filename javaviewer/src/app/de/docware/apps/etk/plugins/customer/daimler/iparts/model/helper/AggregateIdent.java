/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactoryModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactoryModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Basisklasse für Aggregate_Ids with Type
 */
public abstract class AggregateIdent extends IdWithType {

    public static final int SPECIFICATION_DONT_CARE = 0;
    public static final int SPECIFICATION_NEW = 1;
    public static final int SPECIFICATION_OLD = 2;
    protected static final String UNKNOWN_VALUE = "??";
    protected EtkProject project;
    @JsonProperty
    protected int identSpecification;  // 0: don't care; 1: isNew 2: isOld
    @JsonProperty
    protected DCAggregateTypes aggType;

    public void setProject(EtkProject project) {
        this.project = project;
    }

    /**
     * Ermittelt die Baumusternummer aus dem Aggregate-Ident (immer die ersten 6 Stellen).
     *
     * @param ident
     * @return
     */
    public static String getModelFromIdent(String ident) {
        return StrUtils.copySubString(ident, 0, 6);
    }

    public static synchronized void clearCache() {
        FactoryListHelper.clearCache();
    }

    public AggregateIdent(EtkProject project, String TYPE, String aggregateIdent, DCAggregateTypes aggType) {
        super(TYPE, new String[]{ aggregateIdent });
        this.project = project;
        setIdentSpecification(SPECIFICATION_DONT_CARE);
        this.aggType = aggType;
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public AggregateIdent() {
        this(null, "", "", DCAggregateTypes.UNKNOWN);
    }

    public abstract DatacardIdentOrderTypes getIdentType();

    public String getIdent() {
        return getValue(1);
    }

    public void setIdent(String ident) {
        id[0] = ident;
    }

    public int getIdentSpecification() {
        return identSpecification;
    }

    public void setIdentSpecification(int identSpecification) {
        this.identSpecification = identSpecification;
    }

    /**
     * Setzt die alte Systematik für den Ident.
     */
    public void setIdentSpecificationOld() {
        setIdentSpecification(SPECIFICATION_OLD);// 0: don't care; 1: isNew 2: isOld
    }

    /**
     * Setzt die neue Systematik für den Ident.
     */
    public void setIdentSpecificationNew() {
        setIdentSpecification(SPECIFICATION_NEW);// 0: don't care; 1: isNew 2: isOld
    }

    /**
     * Handelt es sich um einen Ident nach alter Systematik?
     * Achtung! Wenn {@code false} zurückgeliefert wird, heißt das nicht zwangsläufig, dass es sich um einen Ident nach
     * neuer Systematik handelt. Dazu muss {@link #isNewIdentSpecification()} aufgerufen werden.
     *
     * @return
     */
    public boolean isOldIdentSpecification() {
        return getIdentSpecification() == AggregateIdent.SPECIFICATION_OLD;
    }

    /**
     * Handelt es sich um einen Ident nach neuer Systematik?
     * Achtung! Wenn {@code false} zurückgeliefert wird, heißt das nicht zwangsläufig, dass es sich um einen Ident nach
     * alter Systematik handelt. Dazu muss {@link #isOldIdentSpecification()} aufgerufen werden.
     *
     * @return
     */
    public boolean isNewIdentSpecification() {
        return getIdentSpecification() == AggregateIdent.SPECIFICATION_NEW;
    }

    public String getModelNumber() {
        if (isModelNumberValid()) {
            return extractModelNumber();
        }
        return "";
    }

    public boolean isFactorySignValid() {
        String factorySign = extractFactorySign();
        if (StrUtils.isValid(factorySign)) {
            // mögliche Werte testen
            return containsToken(getIdentType(), factorySign);
        }
        return false;
    }

    public String getFactorySign() {
        if (isFactorySignValid()) {
            return extractFactorySign();
        }
        return "";
    }

    /**
     * Komplette Baumusternummer (mit Sachnummernkennbuchstabe C/D)
     *
     * @return
     */
    public String getFullModelNo() {
        if (isModelNumberValid()) {
            return iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + extractModelNumber();
        }
        return "";
    }

    public String getFormattedIdent() {
        return getIdent();
    }

    /**
     * Liefert die anzuzeigenden Informationen zum Aggregate-Ident.
     * Die Liste wird durch Ableitung aggregate-spezifisch ergänzt.
     *
     * @return
     */
    public List<String[]> getDisplayGridValues() {
        List<String[]> result = new DwList<String[]>();
        String modelNo = setValueWithDefault(extractModelNumber());
        result.add(new String[]{ "!!Baumuster", modelNo, getFullModelNo() });
        return result;
    }

    protected iPartsFactoryModel.SerialNoAndFactory getSerialNumberAndFactory() {
        if (isModelNumberValid()) {
            iPartsModelId modelId = new iPartsModelId(getFullModelNo());
            String aggregateType = getAggregateTypeByModel();
            iPartsFactoryModel factoryModelInstance = iPartsFactoryModel.getInstance(project);
            // Millionenüberlauf für die Endnummer aus der Datenkarte berechnen und auf Gültigkeit prüfen
            iPartsFactoryModel.SerialNoAndFactory serialNumberWithOverflowAndFactory = factoryModelInstance.getSerialNumberWithOverflowAndFactoryForWMIOrAggregate(
                    getWMI(), extractFactorySign(), modelId, aggregateType, getSerialNumber(), getSerialNumberLength(), "");
            return serialNumberWithOverflowAndFactory;
        }
        return new iPartsFactoryModel.SerialNoAndFactory();
    }

    public String getAggregateTypeByModel() {
        if (isModelNumberValid()) {
            iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(getFullModelNo()));
            return model.getAggregateType();
        }
        return "";
    }

    public DCAggregateTypes getAggType() {
        return this.aggType;
    }

    public boolean isModelNumberValid() {
        String modelNumber = extractModelNumber();
        if (StrUtils.isValid(modelNumber)) {
            return StrUtils.isDigit(modelNumber) && (modelNumber.length() == 6);
        }
        return false;
    }

    protected iPartsDataFactories getFactoryNumber(String factoryNumber) {
        return iPartsFactories.getInstance(project).getDataFactoryByFactoryNumber(factoryNumber);
    }

    protected iPartsDataFactoryModelList getFactoryModelList(String aggType) {
        return FactoryListHelper.getInstance().getFactoryModelList(project, aggType);
    }

    protected iPartsDataFactoryModelList getFactoryModelList(DatacardIdentOrderTypes aggType) {
        return getFactoryModelList(aggType.getDbValue());
    }

    /**
     * Befüllt die Werke Combobox
     */
    public void fillFactoryCombobox(RComboBox<String> comboboxFactory) {
        fillFactoryCombobox(comboboxFactory, "");
    }

    protected void fillFactoryCombobox(RComboBox<String> comboboxFactory, String enumKey) {
        comboboxFactory.switchOffEventListeners();
        comboboxFactory.removeAllItems();
        EnumValue enumValues = getEnums(enumKey);
        if (enumValues != null) {
            comboboxFactory.addItem("");
            String language = project.getConfig().getCurrentViewerLanguage();
            for (Map.Entry<String, EnumEntry> enumSet : enumValues.entrySet()) {
                comboboxFactory.addItem(enumSet.getKey(), enumSet.getKey() + " " + enumSet.getValue().getEnumText().getText(language));
            }
        }
        comboboxFactory.switchOnEventListeners();
    }

    public void fillFactoryCombobox(RComboBox<String> comboboxFactory, DatacardIdentOrderTypes aggType) {
        iPartsDataFactoryModelList factoryModelList = getFactoryModelList(aggType);
        comboboxFactory.switchOffEventListeners();
        comboboxFactory.removeAllItems();
        if (!factoryModelList.isEmpty()) {
            comboboxFactory.addItem("");
            Set<String> factoryNoSet = new HashSet<>();
            String language = project.getConfig().getCurrentViewerLanguage();
            for (iPartsDataFactoryModel dataFactoryModel : factoryModelList) {
                String factory = dataFactoryModel.getAsId().getFactory();
                String factoryNo = extractFactoryNoFromFactory(factory);
                if (!factoryNo.isEmpty()) {
                    if (!factoryNoSet.contains(factoryNo)) {
                        String textToShow = buildTextToShow(dataFactoryModel, language);
                        comboboxFactory.addItem(factoryNo, textToShow.trim());
                        factoryNoSet.add(factoryNo);
                    }
                } else {
                    String textToShow = buildTextToShow(dataFactoryModel, language);
                    comboboxFactory.addItem(factoryNo, textToShow.trim());
                }
            }
        }
        comboboxFactory.switchOnEventListeners();
    }

    protected String buildTextToShow(iPartsDataFactoryModel dataFactoryModel, String language) {
        String factory = dataFactoryModel.getAsId().getFactory();
        String factoryNo = extractFactoryNoFromFactory(factory);
        String factoryAdditional = "";
        if (!factoryNo.isEmpty()) {
            iPartsDataFactories dataFactories = getFactoryNumber(factoryNo);
            if (dataFactories != null) {
                factoryAdditional = StrUtils.replaceFirstSubstring(factory, factoryNo, "");
                factory = dataFactories.getFactoryText(language) + factoryAdditional;
            } else {
                return factoryNo;
            }
        }
        return factoryNo + " " + factory;
    }

    protected String extractFactoryNoFromFactory(String factory) {
        factory = factory.trim();
        if (!factory.isEmpty()) {
            int len = factory.length();
            int i = 0;
            while (i < len) {
                if (!Character.isDigit(factory.charAt(i))) {
                    break;
                }
                i++;
            }
            if (i > 0) {
                factory = StrUtils.copySubString(factory, 0, i);
            } else {
                factory = "";
            }
        }
        return factory;
    }

    protected EnumValue getEnums(String enumKey) {
        if (project != null) {
            return project.getEtkDbs().getEnumValue(enumKey);
        }
        return null;
    }

    protected boolean containsEnumToken(String enumKey, String enumToken) {
        EnumValue enumValue = getEnums(enumKey);
        if (enumValue != null) {
            return enumValue.tokenExists(enumToken);
        }
        return false;
    }

    protected String getEnumText(String enumKey, String key) {
        if (project != null) {
            EnumEntry enumEntry = getEnums(enumKey).get(key);
            if (enumEntry != null) {
                return enumEntry.getEnumText().getText(project.getViewerLanguage());
            }
        }
        return UNKNOWN_VALUE;
    }

    protected boolean containsToken(DatacardIdentOrderTypes aggType, String enumToken) {
        Set<String> validTokensMap = FactoryListHelper.getInstance().getValidFactoryKeys(project, aggType.getDbValue());
        if (validTokensMap != null) {
            return validTokensMap.contains(enumToken);
        }
        return false;
    }

    protected String getShowText(DatacardIdentOrderTypes aggType, String key) {
        if (project != null) {
            iPartsFactoryModel factoryModelInstance = iPartsFactoryModel.getInstance(project);
            iPartsModelId modelId = new iPartsModelId(getFullModelNo());
            iPartsModel model = iPartsModel.getInstance(project, modelId);
            String aggregateTypeFromModel = model.getAggregateType();
            String factoryNumber = factoryModelInstance.getFactoryNumberForAggregate(aggregateTypeFromModel, key,
                                                                                     modelId);
            if (StrUtils.isValid(factoryNumber)) {
                iPartsDataFactories dataFactories = getFactoryNumber(factoryNumber);
                if (dataFactories != null) {
                    String factoryText = dataFactories.getFactoryText(project.getViewerLanguage());
                    return StrUtils.isValid(factoryText) ? (factoryNumber + " " + factoryText) : factoryNumber;
                } else {
                    return factoryNumber;
                }
            }
            // Rückfallposition, falls die reale Berechnung nichts gefunden hat (zB Model nicht vorhanden)
            iPartsDataFactoryModelList factoryModelList = FactoryListHelper.getInstance().getFactoryModelList(project, aggType.getDbValue());
            for (iPartsDataFactoryModel dataFactoryModel : factoryModelList) {
                if (dataFactoryModel.getAsId().getFactorySign().equals(key)) {
                    // Kenntlichmachung, dass dies Rückfallposition ist
                    return TranslationHandler.translate("!!Fallback: %1", buildTextToShow(dataFactoryModel, project.getViewerLanguage()));
                }
            }
        }
        return UNKNOWN_VALUE;
    }

    protected String extractModelNumber() {
        return getModelFromIdent(getIdent());
    }

    public String extractFactorySign() {
        return "";
    }

    /**
     * Liefert die Seriennummer im Original als String zurück inkl. führender Nullen.
     *
     * @return
     */
    public String extractSerialNumberString() {
        return "";
    }

    public int getSerialNumberLength() {
        return -1;
    }

    public boolean isSerialNumberValid() {
        String ident = extractSerialNumberString();
        return StrUtils.isValid(ident) && (ident.length() == getSerialNumberLength()) && StrUtils.isDigit(ident);
    }

    /**
     * Liefert die Seriennummer als String zurück inkl. führender Nullen sofern gültig (ansonsten leer).
     *
     * @return
     */
    public String getSerialNumberString() {
        if (isSerialNumberValid()) {
            return extractSerialNumberString();
        }
        return "";
    }

    /**
     * Liefert die Seriennummer als Integer für die Berechnung vom Millionenüberlauf zurück.
     *
     * @return
     */
    public int getSerialNumber() {
        if (isSerialNumberValid()) {
            return StrUtils.strToIntDef(extractSerialNumberString(), FinId.INVALID_SERIAL_NUMBER);
        }
        return FinId.INVALID_SERIAL_NUMBER;
    }

    public String setValueWithDefault(String value) {
        if (value.isEmpty()) {
            return UNKNOWN_VALUE;
        }
        return value;
    }

    /**
     * Liefert den WMI zurück (nur bei Fahrzeugen in der FIN/VIN vorhanden).
     *
     * @return
     */
    protected String getWMI() {
        return "";
    }

    /**
     * Fügt der übergebenen Liste mit Ident-Bedeutungen die Einträge für Seriennummer/Endnummer mit frei wählbarem Text
     * sowie optional Millionenüberlauf hinzu.
     *
     * @param displayGridValues
     * @param endNumberText
     * @param addSerialNumberOverflow
     */
    protected void addDisplayGridValuesForSerialNumber(List<String[]> displayGridValues, String endNumberText, boolean addSerialNumberOverflow) {
        displayGridValues.add(new String[]{ endNumberText, extractSerialNumberString(), getSerialNumberDescription(project) });

        if (addSerialNumberOverflow) {
            // Millionenüberlauf berechnen
            iPartsFactoryModel.SerialNoAndFactory serialNoAndFactory = getSerialNumberAndFactory();
            String serialNumber;
            if (serialNoAndFactory.isValid()) {
                serialNumber = String.valueOf(serialNoAndFactory.getSerialNumber());
            } else {
                int serialNumberInt = getSerialNumber();
                if (serialNumberInt != FinId.INVALID_SERIAL_NUMBER) {
                    serialNumber = String.valueOf(serialNumberInt);
                } else {
                    serialNumber = UNKNOWN_VALUE;
                }
            }
            displayGridValues.add(new String[]{ "!!Millionenüberlauf", getSerialNumberString(), serialNumber });
        }
    }

    /**
     * Liefert die Beschreibung für die Seriennummer inkl. Bandsteuerung zurück.
     *
     * @param project
     * @return
     */
    public String getSerialNumberDescription(EtkProject project) {
        String serialNumberString = getSerialNumberString();
        if (serialNumberString.isEmpty()) {
            return UNKNOWN_VALUE;
        }

        // Beschreibung für Bandsteuerung falls vorhanden
        iPartsFactoryModel factoryModel = iPartsFactoryModel.getInstance(project);
        String wmi = getWMI();
        String factoryKey = getFactorySign();
        iPartsModelId modelId = new iPartsModelId(getFullModelNo());
        String aggregateType = getAggregateTypeByModel();
        int serialNumberWithoutOverflow = getSerialNumber();
        int serialNumberLength = getSerialNumberLength();
        if (factoryModel.hasBeltControl(wmi, factoryKey, modelId, aggregateType, serialNumberWithoutOverflow, serialNumberLength, "")) {
            String beltSign = factoryModel.getBeltSign(wmi, factoryKey, modelId, aggregateType, serialNumberWithoutOverflow,
                                                       serialNumberLength, "");
            String beltGrouping = factoryModel.getBeltGrouping(wmi, factoryKey, modelId, aggregateType, serialNumberWithoutOverflow,
                                                               serialNumberLength, "");
            return TranslationHandler.translate("!!Band-Kennzahl: %1; Band-Bündelung: %2; Endnummer: %3", beltSign, beltGrouping,
                                                serialNumberString.substring(1));
        } else {
            return serialNumberString;
        }
    }

    /**
     * Handelt es sich um ein Austauschaggregat?
     *
     * @return
     */
    public boolean isExchangeAggregate() {
        return false;
    }

    private static class FactoryListHelper {

        private static FactoryListHelper instance = null;
        private Map<String, iPartsDataFactoryModelList> factoriesModelMap;
        private Map<String, Set<String>> aggFactoryKeyMap;

        public static FactoryListHelper getInstance() {
            if (instance == null) {
                instance = new FactoryListHelper();
            }
            return instance;
        }

        public static void clearCache() {
            instance = null;
        }

        private FactoryListHelper() {
            factoriesModelMap = new HashMap<>();
            aggFactoryKeyMap = new HashMap<>();
        }

        protected iPartsDataFactoryModelList getFactoryModelList(EtkProject project, String aggType) {
            iPartsDataFactoryModelList result = factoriesModelMap.get(aggType);
            if (result == null) {
                result = new iPartsDataFactoryModelList();
                result.loadByAggType(project, aggType);
                factoriesModelMap.put(aggType, result);
            }
            return result;
        }

        protected Set<String> getValidFactoryKeys(EtkProject project, String aggType) {
            Set<String> result = aggFactoryKeyMap.get(aggType);
            if (result == null) {
                iPartsDataFactoryModelList dataFactoryModelList = getFactoryModelList(project, aggType);
                result = new TreeSet<String>();
                for (iPartsDataFactoryModel dataFactoryModel : dataFactoryModelList) {
                    result.add(dataFactoryModel.getAsId().getFactorySign());
                }
                aggFactoryKeyMap.put(aggType, result);
            }
            return result;
        }
    }
}
