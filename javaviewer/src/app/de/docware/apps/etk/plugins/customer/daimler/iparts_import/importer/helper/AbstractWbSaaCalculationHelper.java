/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket.iPartsDataWorkBasketCalc;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket.iPartsDataWorkBasketCalcList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket.iPartsWorkBasketSaaCalcId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCTTHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Basisklasse für die Vorverdichtung der KEM-From und -To Daten für den Bom-DB und MBS-Importer
 */
public abstract class AbstractWbSaaCalculationHelper {

    private static final String VIRTUAL_FIELD_MAX_KEM_FROM_DATE = VirtualFieldsUtils.addVirtualFieldMask("MAX_KEM_FROM_DATE");

    private EtkProject project;
    private iPartsImportDataOrigin source;
    private AbstractDataImporter importer;
    //          modelNo     saaBkNo      Container
    private Map<String, Map<String, List<SaaAVContainer>>> saaAvMap;
    protected iPartsEdsStructureHelper edsStructureHelper;
    private Map<String, List<String>> hmoMap;
    private int saveCount;
    private boolean saveToDB;


    public AbstractWbSaaCalculationHelper(EtkProject project, iPartsImportDataOrigin source, AbstractDataImporter importer) {
        this.project = project;
        this.source = source;
        this.importer = importer;
        this.saveCount = 0;
        this.saveToDB = true;
        this.saaAvMap = new HashMap<>();
        this.edsStructureHelper = iPartsEdsStructureHelper.getInstance();
    }

    public EtkProject getProject() {
        return project;
    }

    public iPartsImportDataOrigin getSource() {
        return source;
    }

    public void setSaveToDB(boolean value) {
        saveToDB = value;
    }

    public boolean getSaveToDB() {
        return saveToDB;
    }

    public int getSaveCount() {
        return saveCount;
    }

    public void clear() {
        saaAvMap.clear();
        saveCount = 0;
    }

    /**
     * Ein dataObject wird analysiert, ein SaaAVContainer gebaut und ggf zur saaAvMap hinzugefügt
     *
     * @param existsInDB
     * @param dataObject
     */
    public void addToMap(boolean existsInDB, EtkDataObject dataObject) {
        SaaAVContainer saaAVContainer = new SaaAVContainer(existsInDB, dataObject);
        addToMap(saaAVContainer);
    }

    /**
     * Abschließende Routine, um die maximalen Zeitintervalle für jedes Element in der saaAvMap zu bestimmen
     * und zu speichern
     */
    public void handleSaaAVEntries() {
        int totalCount = 0;
        int cttCount = 0;

        if (!saaAvMap.isEmpty()) {
            // für Testzwecke das Speichern disabled
//            saveToDB = false;
            hmoMap = null;
            if (source == iPartsImportDataOrigin.EDS) {
                hmoMap = iPartsCTTHelper.getSaaToHmoMapping(getProject());
            }
            for (Map.Entry<String, Map<String, List<SaaAVContainer>>> entry : saaAvMap.entrySet()) {
                totalCount += entry.getValue().size();
            }
            fireMessage("Verdichtung der Datumswerte für %1-Importer. Überprüft werden %2 Elemente.",
                        source.getOrigin(), String.valueOf(totalCount));

            for (Map.Entry<String, Map<String, List<SaaAVContainer>>> entry : saaAvMap.entrySet()) {
                String modelNo = entry.getKey();
                // bestehende Einträge aus der DA_WB_SAA_CALCULATE Tabelle für ein Model holen
                Map<iPartsWorkBasketSaaCalcId, iPartsDataWorkBasketCalc> bmCalcMap = getStoredCalcMap(modelNo);
                for (Map.Entry<String, List<SaaAVContainer>> saaEntry : entry.getValue().entrySet()) {
                    String saaBkNo = saaEntry.getKey();
                    boolean doCalculate = false;
                    // wenn einer der Container modified ist, dann Model und saaBkNo überprüfen
                    for (SaaAVContainer saaAvContainer : saaEntry.getValue()) {
                        if (saaAvContainer.isModified()) {
                            doCalculate = true;
                        }
                    }
                    if (doCalculate) {
                        // Bestimmung der max Zeitwerte für Model und saaBkNo + Eintrag in bmCalcMap
                        calcEDS_MBS_ModelData(modelNo, saaBkNo, bmCalcMap);
                    }
                }
                if (!bmCalcMap.isEmpty()) {
                    // Einträge aus bmCalMap speichern (nur neue oder modifizierte)
                    for (iPartsDataWorkBasketCalc dataWorkBasketCalc : bmCalcMap.values()) {
                        if (dataWorkBasketCalc.isNew() || dataWorkBasketCalc.isModified()) {
                            saveCount++;
                            if (dataWorkBasketCalc.getAsId().getSource().equals(iPartsImportDataOrigin.SAP_CTT.getOrigin())) {
                                cttCount++;
                            }
                            if (saveToDB && (importer != null)) {
                                importer.saveToDB(dataWorkBasketCalc, false);
                            }
                        }
                    }
                }
            }
            String strCount = String.valueOf(saveCount);
            if (saveCount == 0) {
                strCount = TranslationHandler.translate("!!keine");
            }
            fireMessage("!!Verdichtung für %1-Importer beendet. Gespeichert/Aktualisiert wurden %2 Elemente.",
                        source.getOrigin(), strCount);
            if ((saveCount > 0) && (cttCount > 0) && (source == iPartsImportDataOrigin.EDS)) {
                fireMessage("!!Davon %1 Elemente für %2.",
                            String.valueOf(cttCount), iPartsImportDataOrigin.SAP_CTT.getOrigin());
            }
        }
    }


    /**
     * Mit der jeweiligen DataObjectList searchSortAndFillWithJoin zusammenbauen, ausführen und in der
     * Callback-Routine die max Zeitwerte bestimmen und in die bmCalcMap eintragen
     *
     * @param modelNo
     * @param saaBkNo
     * @param bmCalcMap
     */
    protected void calcEDS_MBS_ModelData(String modelNo, String saaBkNo, Map<iPartsWorkBasketSaaCalcId, iPartsDataWorkBasketCalc> bmCalcMap) {
        EtkDataObjectList<? extends EtkDataObject> list = getDataObjectListForSearch();

        list.clear(DBActionOrigin.FROM_DB);
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(getTableName(), getModelField()),
                                                     TableAndFieldName.make(getTableName(), getSaaField()) };
        String[] whereValues = new String[]{ modelNo, saaBkNo };

        String[] sortFields = new String[]{ TableAndFieldName.make(getTableName(), getModelField()),
                                            TableAndFieldName.make(getTableName(), getSaaField()) };

        EtkDisplayFields selectFields = buildSelectedFieldSet();

        Map<String, EtkDataObject> attribJoinMap = new HashMap<>();
        Set<String> keyFieldNames = new HashSet<>();
        keyFieldNames.add(getModelField());
        keyFieldNames.add(getSaaField());

        VarParam<Integer> counter = new VarParam<>(0);
        EtkDataObjectList.FoundAttributesCallback callback = createFoundAttributesCallback(counter, attribJoinMap, keyFieldNames);

        list.searchSortAndFillWithJoin(getProject(), null,
                                       selectFields,
                                       whereTableAndFields, whereValues,
                                       false, sortFields, true, null, false, true,
                                       false, callback, true);
        if (!attribJoinMap.isEmpty()) {
            // aus den gefundenen DataObject (mit max Zeitintervall) iPartsDataWorkBasketCalc bilden und zur bmCalcMap hinzufügen
            for (EtkDataObject dataObject : attribJoinMap.values()) {
                addToWbCalcMap(dataObject.getAttributes(), bmCalcMap);
            }
        } else {
//            System.out.println("nix in " + modelNo + " " + saaBkNo);
        }

    }

    /**
     * aus dem Attributen den MinRelease und MaxRelease-Wert herausholen, iPartsDataWorkBasketCalc bilden und zur bmCalcMap hinzufügen
     *
     * @param attributes
     * @param bmCalcMap
     */
    protected void addToWbCalcMap(DBDataObjectAttributes attributes, Map<iPartsWorkBasketSaaCalcId, iPartsDataWorkBasketCalc> bmCalcMap) {
        // DAIMLER-11363: Gültigkeitsdatum Ab = Bis bedeutet, das die entsprechenden SAAs bereits in SAP gelöscht wurden bzw. nie gebaut wurden
        // => ignorieren
        if (isMBSEqualStartEndDate(attributes)) {
            return;
        }
        iPartsWorkBasketSaaCalcId calcId;
        if ((source == iPartsImportDataOrigin.EDS) && (hmoMap != null)) {
            String saaBkNo = getSaaBkNo(attributes);
            if (hmoMap.get(saaBkNo) != null) {
                calcId = new iPartsWorkBasketSaaCalcId(iPartsImportDataOrigin.SAP_CTT.getOrigin(), getModelNo(attributes), saaBkNo);
            } else {
                calcId = new iPartsWorkBasketSaaCalcId(source.getOrigin(), getModelNo(attributes), saaBkNo);
            }
        } else {
            calcId = new iPartsWorkBasketSaaCalcId(source.getOrigin(), getModelNo(attributes), getSaaBkNo(attributes));
        }
        iPartsDataWorkBasketCalc dataWorkBasketCalc = bmCalcMap.get(calcId);
        if (dataWorkBasketCalc == null) {
            dataWorkBasketCalc = new iPartsDataWorkBasketCalc(getProject(), calcId);
            dataWorkBasketCalc.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            bmCalcMap.put(calcId, dataWorkBasketCalc);
        }
        dataWorkBasketCalc.setMinReleasedFrom(getKemFromDate(attributes), DBActionOrigin.FROM_EDIT);
        dataWorkBasketCalc.setMaxReleasedTo(getKemToDate(attributes), DBActionOrigin.FROM_EDIT);
        dataWorkBasketCalc.setCode(getCode(attributes), DBActionOrigin.FROM_EDIT);
        String factories = getFactories(attributes);
        if (factories == null) {
            factories = "";
        }
        dataWorkBasketCalc.setFactories(factories, DBActionOrigin.FROM_EDIT);
    }


    /**
     * Callback, um relevante Treffer herauszuholen und das max Zeitintervall zu bestimmen
     *
     * @param counter
     * @param attribJoinMap
     * @param keyFieldNames
     * @return
     */
    protected EtkDataObjectList.FoundAttributesCallback createFoundAttributesCallback(final VarParam<Integer> counter,
                                                                                      final Map<String, EtkDataObject> attribJoinMap,
                                                                                      final Set<String> keyFieldNames) {
        return new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                counter.setValue(counter.getValue() + 1);
//                if (searchWasCanceled()) {
//                    return false;
//                }
                // Nur gültige SAA- oder BK-Nummern zulassen
                if (!isValidSaaBkNo(attributes)) {
                    return false;
                }
                if (!isMBSConditionValid(attributes)) {
                    return false;
                }

                // Daten aufakkumulieren (KemDates und ArrayIds)
                String key = buildKey(attributes, keyFieldNames);
                EtkDataObject storedData = attribJoinMap.get(key);
                if (storedData == null) {
                    accumulateKemDates(attributes, null);
                    EtkDataObject dataObject = createDataObjectFromAttributes(attributes);
                    attribJoinMap.put(key, dataObject);
                } else {
                    accumulateKemDates(attributes, storedData.getAttributes());
                }
                return false;
            }

            /**
             * Hilfs-Key für die Bestimmung gleicher EDS_MODEL Einträge bzgl. {@code fieldNames} bestimmen
             *
             * @param attributes
             * @param fieldNames
             * @return
             */
            private String buildKey(DBDataObjectAttributes attributes, Collection<String> fieldNames) {
                StringBuilder str = new StringBuilder();
                for (String fieldName : fieldNames) {
                    if (str.length() > 0) {
                        str.append("&");
                    }
                    str.append(attributes.getFieldValue(fieldName));
                }
                return str.toString();
            }


        };
    }

    /**
     * KEM-Dates aufakkumulieren (Min/Max)
     *
     * @param currentAttributes
     * @param storedAttributes
     */
    protected void accumulateKemDates(DBDataObjectAttributes currentAttributes, DBDataObjectAttributes storedAttributes) {
        String kemToDate = getKemToDate(currentAttributes);
        // der erste Datensatz wird einfach nur gemerkt, das akkumulieren beginnt sobald es mehrere Datensätze gibt
        if (storedAttributes != null) {
            String kemFromDate = getKemFromDate(currentAttributes);
            String lastKemFromDate = getKemFromDate(storedAttributes);
            String lastKemToDate = getKemToDate(storedAttributes);
            if (lastKemFromDate.compareTo(kemFromDate) > 0) {
                setKemFromDate(storedAttributes, kemFromDate);
            }
            if (!lastKemToDate.isEmpty()) {
                if (iPartsEDSDateTimeHandler.isFinalStateDbDateTime(kemToDate)) {
                    kemToDate = "";
                }
                if (kemToDate.isEmpty()) {
                    setKemToDate(storedAttributes, kemToDate);
                } else {
                    if (lastKemToDate.compareTo(kemToDate) < 0) {
                        setKemToDate(storedAttributes, kemToDate);
                    }
                }
            }

            // Code und Werke vom Datensatz mit dem neusten/höchsten DateFrom übernehmen (dieses im virtuellen Feld
            // VIRTUAL_FIELD_MAX_KEM_FROM_DATE merken)
            String lastMaxKemFromDate = storedAttributes.getFieldValue(VIRTUAL_FIELD_MAX_KEM_FROM_DATE);
            if (kemFromDate.compareTo(lastMaxKemFromDate) > 0) {
                storedAttributes.addField(VIRTUAL_FIELD_MAX_KEM_FROM_DATE, kemFromDate, DBActionOrigin.FROM_DB);
                setCode(storedAttributes, getCode(currentAttributes));
                String factories = getFactories(currentAttributes);
                if (factories != null) {
                    setFactories(storedAttributes, factories);
                }
            }
        } else {
            if (iPartsEDSDateTimeHandler.isFinalStateDbDateTime(kemToDate)) {
                setKemToDate(currentAttributes, "");
            }

            currentAttributes.addField(VIRTUAL_FIELD_MAX_KEM_FROM_DATE, getKemFromDate(currentAttributes), DBActionOrigin.FROM_DB);
        }
    }


    /**
     * Laden bereits bestehender iPartsDataWorkBasketCalc-Elemente nach modelNo und source
     *
     * @param modelNo
     * @return
     */
    private Map<iPartsWorkBasketSaaCalcId, iPartsDataWorkBasketCalc> getStoredCalcMap(String modelNo) {
        Map<iPartsWorkBasketSaaCalcId, iPartsDataWorkBasketCalc> bmCalcMap = new HashMap<>();
        iPartsDataWorkBasketCalcList dataWorkBasketCalcList = iPartsDataWorkBasketCalcList.loadWorkBasketBySourceAndModel(getProject(),
                                                                                                                          source,
                                                                                                                          modelNo);
        iPartsDataWorkBasketCalcList dataWorkBasketCalcCTTList = new iPartsDataWorkBasketCalcList();
        if (source == iPartsImportDataOrigin.EDS) {
            dataWorkBasketCalcCTTList = iPartsDataWorkBasketCalcList.loadWorkBasketBySourceAndModel(getProject(),
                                                                                                    iPartsImportDataOrigin.SAP_CTT,
                                                                                                    modelNo);
        }
        for (iPartsDataWorkBasketCalc dataWorkBasketCalc : dataWorkBasketCalcList) {
            bmCalcMap.put(dataWorkBasketCalc.getAsId(), dataWorkBasketCalc);
        }
        for (iPartsDataWorkBasketCalc dataWorkBasketCalc : dataWorkBasketCalcCTTList) {
            bmCalcMap.put(dataWorkBasketCalc.getAsId(), dataWorkBasketCalc);
        }
        return bmCalcMap;
    }


    /**
     * Einen SaaAVContainer nochmals nach Bedingungen abfragen und ggf zur saaAvMap hinzufügen
     *
     * @param saaAvContainer
     */
    protected void addToMap(SaaAVContainer saaAvContainer) {
        if (saaAvContainer.isModified()) {
            addToMap(saaAvContainer.modelNo, saaAvContainer.saaBkNo, saaAvContainer);
        }
    }

    /**
     * DAIMLER-11363: Gültigkeitsdatum Ab = Bis bedeutet, das die entsprechenden SAAs bereits in SAP gelöscht wurden bzw. nie gebaut wurden
     * => ignorieren
     *
     * @param attributes
     * @return
     */
    protected boolean isMBSEqualStartEndDate(DBDataObjectAttributes attributes) {
        return false;
    }

    /**
     * Einen SaaAVContainer zur saaAvMap hinzufügen
     *
     * @param modelNo
     * @param saaBkNo
     * @param saaAvContainer
     */
    protected void addToMap(String modelNo, String saaBkNo, SaaAVContainer saaAvContainer) {
        Map<String, List<SaaAVContainer>> saaMap = saaAvMap.get(modelNo);
        if (saaMap == null) {
            saaMap = new HashMap<>();
            saaAvMap.put(modelNo, saaMap);
        }
        List<SaaAVContainer> avContainerList = saaMap.get(saaBkNo);
        if (avContainerList == null) {
            avContainerList = new DwList<>();
            saaMap.put(saaBkNo, avContainerList);
        }
        avContainerList.add(saaAvContainer);
    }

    protected void fireMessage(String key, String... placeHolderTexts) {
        if (importer != null) {
            importer.getMessageLog().fireMessage(TranslationHandler.translate(key, placeHolderTexts),
                                                 MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    //===============================================================================
    /* Getter/Setter für die verschiedenen Tabellen */
    protected abstract String getTableName();

    protected abstract String getModelField();

    protected abstract String getSaaField();

    protected abstract String getFieldModelReleaseTo();

    protected abstract String getFieldModelReleaseFrom();

    protected abstract String getFieldCode();

    protected abstract String getFieldFactories();

    protected String getModelNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getModelField());
    }

    protected String getSaaBkNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getSaaField());
    }

    protected String getKemToDate(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldModelReleaseTo());
    }

    protected String getKemFromDate(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldModelReleaseFrom());
    }

    protected DBDataObjectAttribute getKemToDateAttrib(DBDataObjectAttributes attributes) {
        return attributes.getField(getFieldModelReleaseTo(), false);
    }

    protected DBDataObjectAttribute getKemFromDateAttrib(DBDataObjectAttributes attributes) {
        return attributes.getField(getFieldModelReleaseFrom(), false);
    }

    protected void setKemFromDate(DBDataObjectAttributes attributes, String value) {
        attributes.addField(getFieldModelReleaseFrom(), value, DBActionOrigin.FROM_DB);
    }

    protected void setKemToDate(DBDataObjectAttributes attributes, String value) {
        attributes.addField(getFieldModelReleaseTo(), value, DBActionOrigin.FROM_DB);
    }

    protected String getCode(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldCode());
    }

    protected String getFactories(DBDataObjectAttributes attributes) {
        if (StrUtils.isValid(getFieldFactories())) {
            return attributes.getFieldValue(getFieldFactories());
        }
        return null;
    }

    protected void setCode(DBDataObjectAttributes attributes, String value) {
        attributes.addField(getFieldCode(), value, DBActionOrigin.FROM_DB);
    }

    protected void setFactories(DBDataObjectAttributes attributes, String value) {
        attributes.addField(getFieldFactories(), value, DBActionOrigin.FROM_DB);
    }

    protected abstract EtkDataObjectList<? extends EtkDataObject> getDataObjectListForSearch();

    protected abstract EtkDataObject createDataObjectFromAttributes(DBDataObjectAttributes attributes);

    protected abstract EtkDisplayFields buildSelectedFieldSet();

    protected abstract boolean isValidSaaBkNo(DBDataObjectAttributes attributes);

    protected abstract boolean isMBSConditionValid(DBDataObjectAttributes attributes);


    protected class SaaAVContainer {

        private boolean existInDB;
        private String modelNo;
        private String saaBkNo;
        private boolean isKemDateFromModified;
        private boolean isKemDateToModified;

        public SaaAVContainer(boolean existsInDb, EtkDataObject dataObject) {
            this.existInDB = existsInDb;
            this.modelNo = getModelNo(dataObject.getAttributes());
            this.saaBkNo = getSaaBkNo(dataObject.getAttributes());
            this.isKemDateFromModified = false;
            DBDataObjectAttribute attribute = getKemFromDateAttrib(dataObject.getAttributes());
            if ((attribute != null) && this.existInDB) {
                this.isKemDateFromModified = attribute.isModified();
            }
            this.isKemDateToModified = false;
            attribute = getKemToDateAttrib(dataObject.getAttributes());
            if ((attribute != null) && this.existInDB) {
                this.isKemDateToModified = attribute.isModified();
            }
        }

        public boolean isModified() {
            if (!existInDB) {
                return true;
            }
            return isKemDateFromModified || isKemDateToModified;
        }

        public String getStoredModelNo() {
            return modelNo;
        }

        public String getStoredSaaBkNo() {
            return saaBkNo;
        }
    }


}
