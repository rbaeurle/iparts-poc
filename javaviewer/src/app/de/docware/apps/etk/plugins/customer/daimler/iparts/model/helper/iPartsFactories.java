/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactoriesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsFactoriesId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.*;

/**
 * Repräsentation der Werke
 * Inhalt der Tabelle (DA_FACTORIES) mit diversen Caches für unterschiedliche Zugriffsarten.
 */
public class iPartsFactories implements iPartsConst {

    private static final char KEY_DELIMITER = '|';

    private static ObjectInstanceStrongLRUList<Object, iPartsFactories> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    // Maps für die Werke
    protected Map<String, iPartsDataFactories> factoriesByLetterCode = new TreeMap<String, iPartsDataFactories>(); // sortiert nach Werkskennbuchstaben
    protected Map<String, iPartsDataFactories> factoriesByFactoryNumber = new LinkedHashMap<String, iPartsDataFactories>(); // über die DB bereits sortiert nach Werksnummer
    protected Map<String, iPartsDataFactories> factoriesByPEMLetterCode = new LinkedHashMap<String, iPartsDataFactories>(); // über die DB bereits sortiert nach Werksnummer

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsFactories getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsFactories.class, "Factories", false);
        iPartsFactories result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsFactories();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        iPartsDataFactoriesList factories = new iPartsDataFactoriesList();
        factories.loadSortedByFactoryNumber(project);
        for (iPartsDataFactories dataFactory : factories) {
            // Notwendig, damit der mehrsprachige Text sauber im Cache geladen ist, weil später die DB-Verbindung vom EtkProject
            // bereits abgebaut sein könnte und es dann zu einer Exception "database connection is not active (connection pool
            // is null)" kommt
            dataFactory.getFieldValueAsMultiLanguage(FIELD_DF_DESC);

            factoriesByLetterCode.put(dataFactory.getAsId().getLetterCode(), dataFactory);
            factoriesByFactoryNumber.put(dataFactory.getFactoryNumber(), dataFactory);

            // Datenquelle nur dann zum Schlüssel hinzufügen, wenn sie nicht leer ist (UNKNOWN)
            iPartsImportDataOrigin dataSource = dataFactory.getDataSource();
            String dataSourceString;
            if (dataSource != iPartsImportDataOrigin.UNKNOWN) {
                dataSourceString = KEY_DELIMITER + dataSource.getOrigin();
            } else {
                dataSourceString = "";
            }
            factoriesByPEMLetterCode.put(dataFactory.getPEMLetterCode() + dataSourceString, dataFactory);
        }
    }

    /**
     * Liefert das {@link iPartsDataFactories} zum übergegebenen Werkskennbuchstaben
     *
     * @param factoryLetterCode Werkskennbuchstabe
     * @return iPartsDataFactories oder null
     */
    public iPartsDataFactories getDataFactoryByLetterCode(String factoryLetterCode) {
        return factoriesByLetterCode.get(factoryLetterCode);
    }

    /**
     * Liefert das {@link iPartsDataFactories} Objekt zum übergegebenen PEM-Kennbuchstaben und Datenquelle {@link iPartsImportDataOrigin}.
     *
     * @param pemLetterCode PEM-Kennbuchstabe
     * @param dataSource    Datenquelle
     * @return iPartsDataFactories oder null
     */
    public iPartsDataFactories getDataFactoryByPEMLetterCodeAndDataSource(String pemLetterCode, iPartsImportDataOrigin dataSource) {
        // 1. Versuch mit PEM-Kennbuchstabe und Datenquelle
        iPartsDataFactories result = factoriesByPEMLetterCode.get(pemLetterCode + KEY_DELIMITER + dataSource);

        // 2. Versuch mit leerer Datenquelle (allgemein gültig)
        if (result == null) {
            result = factoriesByPEMLetterCode.get(pemLetterCode);
        }
        return result;
    }

    /**
     * Werksnummer über den/die PEM-Kennbuchstaben und die Datenquelle bestimmen.
     * Der/die PEM-Kennbuchstabe(n) stecken im ersten oder den ersten beiden Zeichen der PEM.
     * Wird nichts gefunden, wird ein zweites Mal direkt in den Werkskennbuchstaben gesucht.
     *
     * @param pem
     * @param dataSource
     * @return {@code null} falls keine Werksnummer gefunden wurde
     */
    public String getFactoryNumberForPEMAndDataSource(String pem, iPartsImportDataOrigin dataSource) {
        // Der/die PEM-Kennbuchstabe(n) stecken im ersten oder den ersten beiden Zeichen der PEM.
        char secondChar = pem.charAt(1);
        String factoryLetterCode = Character.isDigit(secondChar) ? pem.substring(0, 1) : pem.substring(0, 2);
        // Zugriff über den Cache
        iPartsDataFactories dataFactories = getDataFactoryByPEMLetterCodeAndDataSource(factoryLetterCode, dataSource);
        if (dataFactories == null) {
            // Zweiter Versuch direkt auf den Werkskennbuchstaben gehen
            dataFactories = getDataFactoryByLetterCode(factoryLetterCode);
            if (dataFactories == null) {
                return null;
            }
        }
        return dataFactories.getFactoryNumber();
    }

    /**
     * Liefert Werksnummer aus dem Cache für einen gegebenen Werkskennbuchstaben
     *
     * @param factoryId Werkskennbuchstabe
     * @return Werksnummer / DF_FACTORY_NO
     */
    public String getFactoryNumber(iPartsFactoriesId factoryId) {
        iPartsDataFactories dataFactory = factoriesByLetterCode.get(factoryId.getLetterCode());
        if (dataFactory != null) {
            return dataFactory.getFactoryNumber();
        } else {
            return null;
        }
    }

    /**
     * Liefert die Werksbeschreibung für einen gegebenen Werkskennbuchstaben
     *
     * @param factoryId Werkskennbuchstabe
     * @param language  Sprache
     * @return Beschreibung des Werks in der angegebenen Sprache
     */
    public String getFactoryDescription(iPartsFactoriesId factoryId, String language) {
        iPartsDataFactories dataFactory = factoriesByLetterCode.get(factoryId.getLetterCode());
        if (dataFactory != null) {
            return dataFactory.getFactoryText(language);
        } else {
            return null;
        }
    }

    /**
     * Liefert den PEM-Kennbuchstaben zu einem übergebenen Werkskennbuchstaben.
     *
     * @param factoryId
     * @return
     */
    public String getPEMLetterCode(iPartsFactoriesId factoryId) {
        iPartsDataFactories dataFactory = factoriesByLetterCode.get(factoryId.getLetterCode());
        if (dataFactory != null) {
            return dataFactory.getPEMLetterCode();
        } else {
            return null;
        }
    }

    /**
     * Liefert die Datenquelle zu einem übergebenen Werkskennbuchstaben.
     *
     * @param factoryId
     * @return
     */
    public iPartsImportDataOrigin getDataSource(iPartsFactoriesId factoryId) {
        iPartsDataFactories dataFactory = factoriesByLetterCode.get(factoryId.getLetterCode());
        if (dataFactory != null) {
            return dataFactory.getDataSource();
        } else {
            return null;
        }
    }

    /**
     * Liefert das {@link iPartsDataFactories} zu einer für eine gegebene Werksnummer
     *
     * @param factoryNumber Werksnummer
     * @return iPartsDataFactories oder null
     */
    public iPartsDataFactories getDataFactoryByFactoryNumber(String factoryNumber) {
        return factoriesByFactoryNumber.get(factoryNumber);
    }

    /**
     * Liefert den Werkskennbuchstaben für eine gegebene Werksnummer
     *
     * @param factoryNumber Werksnummer
     * @return Werkskenner
     */
    public String getFactoryLetterCode(String factoryNumber) {
        iPartsDataFactories dataFactory = factoriesByFactoryNumber.get(factoryNumber);
        if (dataFactory != null) {
            return dataFactory.getAsId().getLetterCode();
        } else {
            return null;
        }
    }

    /**
     * Liefert den PEM-Kennbuchstaben zu einer übergebenen Werksnummer.
     *
     * @param factoryNumber
     * @return
     */
    public String getFactoryPemLetterCode(String factoryNumber) {
        iPartsDataFactories dataFactory = factoriesByFactoryNumber.get(factoryNumber);
        if (dataFactory != null) {
            return dataFactory.getPEMLetterCode();
        } else {
            return null;
        }
    }

    /**
     * Liefert die Datenquelle zu einer übergebenen Werksnummer.
     *
     * @param factoryNumber
     * @return
     */
    public iPartsImportDataOrigin getFactoryDataSource(String factoryNumber) {
        iPartsDataFactories dataFactory = factoriesByFactoryNumber.get(factoryNumber);
        if (dataFactory != null) {
            return dataFactory.getDataSource();
        } else {
            return null;
        }
    }

    /**
     * Liefert eine unveränderliche Liste aller {@link iPartsDataFactories}-Objekte sortiert nach der Werksnummer
     *
     * @return
     */
    public Collection<iPartsDataFactories> getDataFactories() {
        return Collections.unmodifiableCollection(factoriesByFactoryNumber.values());
    }

    public boolean isValidForFilter(String factoryNumber) {
        iPartsDataFactories factoryData = getDataFactoryByFactoryNumber(factoryNumber);
        if (factoryData == null) {
            // Da Werke explizit auf "nicht relevant" gesetzt werden, sind Werke, die nicht eingetragen sind oder nicht existieren
            // initial gültig
            return true;
        }
        return factoryData.isValidForFilter();

    }
}