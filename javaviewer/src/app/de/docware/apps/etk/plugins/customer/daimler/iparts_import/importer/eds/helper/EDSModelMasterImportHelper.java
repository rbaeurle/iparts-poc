/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAAModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.util.misc.id.IdWithType;

import java.util.Map;
import java.util.Set;

/**
 * Helper für das Importieren der "ELDAS SAA-Gültigkeit zu Baumuster, SAA/BK Nummer => Baumusternummer".
 * EDS (Bom-DB) Baumusterinhalt-Stammdaten Importer (T43RB2I)
 */
public class EDSModelMasterImportHelper extends EDSImportHelper {

    public EDSModelMasterImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
        super(project, mapping, tableName);
    }

    /**
     * Funktion zum Anlegen von Einträgen in DA_EDS_SAA_MODELS, falls es noch keinen entsprechenden Datensatz gibt.
     * Gleiche Funktionalität für Urladungsimporter und Änderungsdienst.
     *
     * @param importer
     * @param dataObject
     * @param importToDB
     * @param usedIds
     */
    public void createSaaModelEntryIfNotExists(AbstractDataImporter importer, iPartsDataEDSModelContent dataObject,
                                               boolean importToDB, Set<IdWithType> usedIds) {
        // Bei jedem Datensatz ohne Aggregatebaumuster als untere Sachnummer (B2I_SNRU != "D*") ist zu püfen ...
        String uSaaNo = dataObject.getFieldValue(FIELD_EDS_MODEL_MSAAKEY);
        String modelNo = dataObject.getFieldValue(FIELD_EDS_MODEL_MODELNO);
        iPartsDataSAAModels saaModels = createSaaModelEntryIfNotExists(getProject(), uSaaNo, modelNo, usedIds, iPartsImportDataOrigin.EDS);
        if (importToDB && (saaModels != null)) {
            importer.saveToDB(saaModels);
        }
    }

    /**
     * Zuordung Aggregatebaumuster zum Baumuster in DA_MODELS_AGGS anlegen, falls noch nicht vorhanden.
     * Gleiche Funktionalität für Urladungsimporter und Änderungsdienst.
     *
     * @param importer
     * @param dataObject
     * @param importToDB
     * @param usedIds
     */
    public void createModelsAggsEntryIfNotExists(AbstractDataImporter importer, iPartsDataEDSModelContent dataObject, boolean importToDB, Set<IdWithType> usedIds) {
        // Fahrzeug oder Aggregatebaumuster (B2I_SNR)
        String modelNo = dataObject.getFieldValue(FIELD_EDS_MODEL_MODELNO);
        // Untere Sachnummer (B2I_SNRU)
        String saaNoOrAggModel = dataObject.getFieldValue(FIELD_EDS_MODEL_MSAAKEY);
        iPartsDataModelsAggs modelAgg = createModelsAggsEntryIfNotExists(getProject(), modelNo, saaNoOrAggModel, usedIds, iPartsImportDataOrigin.EDS);
        if (importToDB && (modelAgg != null)) {
            importer.saveToDB(modelAgg);
        }
    }
}
