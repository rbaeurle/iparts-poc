/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAAModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.AbstractMBSDataHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDistributionHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.structure.AbstractMBSStructureHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.structure.MBSNavigationStructureHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.structure.MBSPartlistStructureHandler;
import de.docware.framework.modules.config.common.Language;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

/**
 * Importhelper mit Funktionen für den SAP-MBS Multi-Handler Importer
 */
public class MBSImportHelper extends iPartsMainImportHelper {

    private static final Set<String> BOOLEAN_TRUE_VALUES = new HashSet<>();

    static {
        BOOLEAN_TRUE_VALUES.add("J");
        BOOLEAN_TRUE_VALUES.add("I");
        BOOLEAN_TRUE_VALUES.add(SQLStringConvert.booleanToPPString(true));
    }

    private iPartsNumberHelper numberHelper = new iPartsNumberHelper();

    public MBSImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
        super(project, mapping, tableName);
    }

    public iPartsNumberHelper getNumberHelper() {
        return numberHelper;
    }

    /**
     * Die Import-Werte komplett in einen Datensatz übertragen (SAP-MBS spezifisch)
     * Das Mapping besitzt dabei den folgenden Aufbau:
     * Key = DB-FieldName, Value = Import-Spaltenname
     *
     * @param dataObject
     * @param importRec
     */
    public void fillOverrideCompleteDataForSAPMBS(EtkDataObject dataObject, Map<String, String> importRec) {
        fillOverrideCompleteDataReverse(dataObject, importRec, Language.DE);
    }

    /**
     * Wandelt die Import-Datumswerte in die iParts Datenbank-Datumswerte um.
     *
     * @param importDateTime
     * @return
     */
    public String getMBSDateTimeValue(String importDateTime) {
        String dateTimeISO = XMLImportExportDateHelper.getTimeZoneDateAsISODate(importDateTime);
        if (isFinalStateDateTime(dateTimeISO)) {
            return "";
        }
        return dateTimeISO;
    }

    /**
     * Überprüft, ob der Import-Datums-Wert "unendlich" bedeutet.
     *
     * @param dateTime
     * @return
     */
    public boolean isFinalStateDateTime(String dateTime) {
        return (dateTime == null) || dateTime.isEmpty() || dateTime.startsWith("9");
    }

    /**
     * Legt ein {@link iPartsDataSAAModels} Objekt an, sofern es nicht schon existiert
     *
     * @param importer
     * @param model
     * @param subSNR
     * @param usedIds
     */
    public boolean addSaaModelsData(MBSDataImporter importer, String model, String subSNR, Set<IdWithType> usedIds) {
        if (StrUtils.isValid(model, subSNR)) {
            iPartsDataSAAModels saaModels = createSaaModelEntryIfNotExists(getProject(), subSNR, model, usedIds, iPartsImportDataOrigin.SAP_MBS);
            if (saaModels != null) {
                return importer.saveToDB(saaModels);
            }
        }
        return false;
    }

    /**
     * Legt ein {@link iPartsDataModelsAggs} Objekt an, sofern es nicht schon existiert. Aggregatebaumuster können
     * länger als sieben Stellen sein, daher wird in so einem Fall das Baumuster auf die ersten 7 Stellen gekürzt.
     *
     * @param importer
     * @param vehicleModel
     * @param aggsModel
     * @param usedIds
     */
    public boolean addVehicleModelToAggsModelsData(MBSDataImporter importer, String vehicleModel, String aggsModel, Set<IdWithType> usedIds) {
        if (StrUtils.isValid(vehicleModel, aggsModel) && aggsModel.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE)) {
            if (aggsModel.length() > 7) {
                aggsModel = StrUtils.copySubString(aggsModel, 0, 7);
            }
            iPartsDataModelsAggs modelsAggs = createModelsAggsEntryIfNotExists(getProject(), vehicleModel, aggsModel, usedIds, iPartsImportDataOrigin.SAP_MBS);
            if (modelsAggs != null) {
                return importer.saveToDB(modelsAggs);
            }
        }
        return false;
    }

    /**
     * Wandelt die SAP-MBS spezifischen Boolean Werte um, bevor sie in einem {@link EtkDataObject} gespeichert werden.
     *
     * @return
     */
    @Override
    protected boolean handleValueOfBooleanField(String value) {
        return BOOLEAN_TRUE_VALUES.contains(value.toUpperCase());
    }

    @Override
    protected String handleValueOfSpecialField(String sourceField, String value) {
        if (sourceField.equals(AbstractMBSStructureHandler.CODE)) {
            value = value.trim();
            if (value.startsWith("5")) { // DAIMLER-10803: Führende 5 entfernen bei der Code-Regel
                value = value.substring(1);
            }
            return value;
        }
        return super.handleValueOfSpecialField(sourceField, value);
    }

    /**
     * Erzeugt alle - nach dem aktuellen Stand - möglichen Stammdaten Handler
     *
     * @param project
     * @param importer
     * @return
     */
    public static List<AbstractMBSDataHandler> createAllMasterDataHandlers(EtkProject project, MBSDataImporter importer) {
        List<AbstractMBSDataHandler> handlers = new ArrayList<>();
        handlers.add(new MBSPartDataHandler(project, importer));
        handlers.add(new MBSModelDataHandler(project, importer));
        handlers.add(new MBSCodeDataHandler(project, importer));
        handlers.add(new MBSBaseListNumberHandler(project, importer));
        handlers.add(new MBSBaseListConGroupNumberHandler(project, importer));
        handlers.add(new MBSPartsListNumberHandler(project, importer));
        handlers.add(new MBSPartsListConGroupNumberHandler(project, importer));
        handlers.add(new MBSCTTListDataHandler(project, importer));
        handlers.sort(Comparator.comparing(AbstractMBSDataHandler::getHandlerName));
        return handlers;
    }

    /**
     * Erzeugt alle - nach dem aktuellen Stand - möglichen Struktur Handler
     *
     * @param project
     * @param importer
     * @param addHandlersAsSubHandlers Sollen die {@link AbstractMBSStructureHandler} auch gleich als Sub-Handler registriert
     *                                 werden?
     * @return
     */
    public static List<AbstractMBSStructureHandler> createAllStructureDataHandlers(EtkProject project, MBSDataImporter importer,
                                                                                   boolean addHandlersAsSubHandlers) {
        MBSDistributionHandler distributionHandler = new MBSDistributionHandler(project, importer, "Structure");
        List<AbstractMBSStructureHandler> handlers = new ArrayList<>();
        handlers.add(new MBSNavigationStructureHandler(project, importer, distributionHandler));
        handlers.add(new MBSPartlistStructureHandler(project, importer, distributionHandler));
        handlers.sort(Comparator.comparing(AbstractMBSDataHandler::getHandlerName));

        if (addHandlersAsSubHandlers) {
            for (AbstractMBSStructureHandler handler : handlers) {
                distributionHandler.registerSubHandler(handler);
            }
        }

        return handlers;
    }

    /**
     * Fügt dem übergebenen Wert vorne ein "Z" hinzu sofern der Originalwert mit "W" anfängt
     *
     * @param item
     * @param returnDifferentRetailSAA
     * @return
     */
    public String addSaaPrefixIfNeeded(String item, boolean returnDifferentRetailSAA) {
        if (StrUtils.isValid(item) && item.startsWith(CTT_LIST_NUMBER_PREFIX)) {
            item = SAA_NUMBER_PREFIX + item;
        }

        if (returnDifferentRetailSAA) {
            return getRetailSAA(item);
        }

        return item;
    }

    /**
     * Liefert für den übergebenen String im DB-Format eine evtl. abweichende Retail-SAA zurück ("Z0*"-SAAs aus der Konstruktion
     * werden zu "Z *"-SAAs im Retail) bzw. den Original-String als Fallback.
     *
     * @param item
     * @return
     */
    public String getRetailSAA(String item) {
        String retailSaaNumber = numberHelper.getDifferentRetailSAA(item);
        if (retailSaaNumber != null) {
            return retailSaaNumber;
        } else {
            return item;
        }
    }
}
