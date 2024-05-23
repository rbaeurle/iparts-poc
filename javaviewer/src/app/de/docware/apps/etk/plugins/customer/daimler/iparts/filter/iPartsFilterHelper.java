/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter;

import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataObjectWithPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.timeslice.iPartsFilterTimeSliceHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureNode;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.collections.diskmappedlist.compare.DiskMappedKeyValueListCompare;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.booleanfunctionparser.*;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;
import de.docware.util.misc.booleanfunctionparser.model.PositiveAndNegativeTerms;
import de.docware.util.misc.id.IdWithType;

import java.util.*;
import java.util.stream.Collectors;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard.FilterTypes.COUNTRY_VALIDITY_FILTER;
import static de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard.FilterTypes.SPECIFICATION_FILTER;

/**
 * Basisfilterfunktionen für den iPartsFilter
 */
public class iPartsFilterHelper {

    private static final ObjectInstanceLRUList<iPartsProductId, Map<KgTuId, ModuleValidities>> productKgTuToModelValiditiesCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_PRODUCT,
                                                                                                                                                               iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static void clearCache() {
        productKgTuToModelValiditiesCache.clear();
    }

    public static boolean ignoreInvalidFactories(iPartsFilter filter) {
        if (filter != null) {
            return filter.ignoreInvalidFactories();
        }
        return false;
    }

    /**
     * Liefert den übersetzten Quellennamen für den Filtergrund zurück ("vom Baumuster" bzw. "von der Datenkarte")
     * abhängig vom Flag <i>isModelFilter</i>.
     *
     * @param isModelFilter
     * @return
     */
    public static String getFilterReasonSourceName(boolean isModelFilter) {
        return getFilterReasonSourceName(isModelFilter, false);
    }

    /**
     * Liefert den übersetzten Quellennamen für den Filtergrund zurück ("vom Baumuster" bzw. "von der Datenkarte")
     * abhängig vom Flag <i>isModelFilter</i>. Mit Unterscheidung ob die Parent-Datenkarte verwendet wurde oder nicht.
     *
     * @param isModelFilter
     * @param isParentDataCard normal false; true wenn in Ausnahmefällen ein Aggregat mit der Parent-Datenkarte (Fahrzeug-Datenkarte) gefilter wurde
     * @return
     */
    public static String getFilterReasonSourceName(boolean isModelFilter, boolean isParentDataCard) {
        if (isModelFilter) {
            return TranslationHandler.translate("!!vom Baumuster");
        } else {
            return TranslationHandler.translate(isParentDataCard ? "!!von der Fahrzeug-Datenkarte" : "!!von der Datenkarte");
        }
    }

    /**
     * Liefert das formatierte Datum für das übergebene <i>date</i> zurück inkl. Auswertung von "unendlich".
     *
     * @param date
     * @param project
     * @return
     */
    public static String getFilterReasonDate(long date, EtkProject project) {
        if ((date == 0) || (date == Long.MIN_VALUE) || (date == Long.MAX_VALUE)) {
            return TranslationHandler.translate("!!unendlich");
        }

        String language = project.getViewerLanguage();
        DateConfig dateConfig = DateConfig.getInstance(project.getConfig());
        return dateConfig.formatDate(language, String.valueOf(date));
    }

    /**
     * Basisfilterfunktion für den Lenkungsfilter.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param steeringValue
     * @param isModelFilter
     * @param filter
     * @return
     */
    static boolean basicCheckSteeringFilter(EtkDataPartListEntry partListEntry, String steeringValue, boolean isModelFilter,
                                            iPartsFilter filter) {
        if (!StrUtils.isEmpty(steeringValue)) {
            String steering = partListEntry.getFieldValue(iPartsConst.FIELD_K_STEERING);
            if (!StrUtils.isEmpty(steering)) {
                //manuell eingesteller Lenkungsfilter, dieser Filter geht vor den eventuell im Baumuster eingestellten Filterwerten
                if (!steering.equals(steeringValue)) {
                    if ((filter != null) && filter.isWithFilterReason()) {
                        filter.setFilterReasonForPartListEntry(partListEntry, isModelFilter ? iPartsFilterSwitchboard.FilterTypes.MODEL : iPartsFilterSwitchboard.FilterTypes.STEERING,
                                                               "!!%1 \"%2\" ungleich \"%3\" %4", TranslationHandler.translate("!!Lenkung"),
                                                               steering, steeringValue, getFilterReasonSourceName(isModelFilter));
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Basisfilterfunktion für den Federfilter, es wird einfach getestet, ob die PartNo des Entries in der übergebenen Liste ist
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param springNumbers
     * @return
     */
    static boolean basicSpringFilter(EtkDataPartListEntry partListEntry, Collection<String> springNumbers) {
        String partNo = partListEntry.getFieldValue(iPartsConst.FIELD_K_MATNR);
        return springNumbers.contains(partNo);
    }


    /**
     * Basisfilterfunktion für den Getriebefilter
     * laut Confluence (https://confluence.docware.de/confluence/pages/viewpage.action?pageId=22282583):
     * Liegt eine Getriebeart GA oder GM vor, dann werden nur Teilepositionen ausgegeben,
     * deren Getriebeart leer ist bzw. deren Getriebeart mit der zweiten Stelle der vorgegebenen Getriebeart übereinstimmt.
     * <p/>
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param gearboxValue
     * @param filter
     * @return
     */
    static boolean basicCheckGearBoxFilter(EtkDataPartListEntry partListEntry, String gearboxValue, iPartsFilter filter) {
        if (!StrUtils.isEmpty(gearboxValue)) {

            String gearbox = partListEntry.getFieldValue(iPartsConst.FIELD_K_GEARBOX_TYPE);
            if (!StrUtils.isEmpty(gearbox)) {
                //Getriebefilter
                if ((gearboxValue.length() >= 2) && (gearbox.length() >= 2)) {
                    if (gearbox.toUpperCase().charAt(1) != gearboxValue.toUpperCase().charAt(1)) {
                        if ((filter != null) && filter.isWithFilterReason()) {
                            filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.GEARBOX,
                                                                   "!!%1 \"%2\" ungleich \"%3\" %4", TranslationHandler.translate("!!Getriebeart"),
                                                                   gearbox, gearboxValue, getFilterReasonSourceName(false));
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Basisfilterfunktion für den Baumustergültigkeitsfilter.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param modelValidityArray
     * @param modelNo
     * @return
     */
    static boolean basicCheckModelValidityFilter(EtkDataArray modelValidityArray, String modelNo) {
        if (!StrUtils.isEmpty(modelNo)) {
            return containsModelNo(modelValidityArray, modelNo);
        }
        return true;
    }

    /**
     * Basisfilterfunktion für den Baumustergültigkeitsfilter.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param modelNo
     * @param filter
     * @return
     */
    static boolean basicCheckModelValidityFilter(EtkDataPartListEntry partListEntry, String modelNo, iPartsFilter filter) {
        if (!basicCheckModelValidityFilter(partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_MODEL_VALIDITY), modelNo)) {
            if ((filter != null) && filter.isWithFilterReason()) {
                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                       "!!Baumuster \"%1\" nicht enthalten in Baumuster-Gültigkeit",
                                                       modelNo);
            }
            return false;
        }
        return true;

    }


    /**
     * Testet einen Stücklisteneintrag, ob er bei den Saas der übergebenen Datenkarte gültig ist.
     * Hier wird nicht getestet, ob der Eintrag ein Verweis auf eine freischwebende SA ist
     *
     * @param partListEntry
     * @param dataCard
     * @param isModelFilter
     * @param isSAAssembly
     * @param filter
     * @return
     */
    static boolean basicCheckSaStrichValidityFilter(EtkDataPartListEntry partListEntry, AbstractDataCard dataCard, boolean isModelFilter,
                                                    boolean isSAAssembly, iPartsFilter filter) {
        // Bestimme die SAAs über den Datenkarte-zu-SAAs-Cache im Filter. Falls der Filter null ist, werden die SAAs direkt
        // über die Datenkarte bestimmt
        Set<String> saas = (filter != null) ? filter.getSaasForDataCard(dataCard) : dataCard.getSaasForFilter().getAllCheckedValues();
        boolean result = checkSAStrichValidity(partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_SA_VALIDITY), saas);

        if (!result && (filter != null) && filter.isWithFilterReason()) {
            iPartsFilterSwitchboard.FilterTypes filterType = getFilterType(isSAAssembly, isModelFilter);
            String filterReasonSource;
            if (isModelFilter) {
                filterReasonSource = getFilterReasonSourceName(isModelFilter);
            } else if (dataCard.isVehicleDataCard()) {
                VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                filterReasonSource = TranslationHandler.translate(vehicleDataCard.getActiveAggregates().isEmpty() ? "!!Fahrzeug-Datenkarte" : "!!Fahrzeug-Datenkarte und allen Aggregate-Datenkarten");
            } else {
                AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCard;
                filterReasonSource = TranslationHandler.translate((aggregateDataCard.getParentDatacard() == null) ? "!!Aggregate-Datenkarte" : "!!Aggregate- und Fahrzeug-Datenkarte");
            }
            filter.setFilterReasonForPartListEntry(partListEntry, filterType, "!!Nicht gültig für SAAs oder Baukasten zur %1",
                                                   filterReasonSource);
        }
        return result;
    }

    /**
     * Testet einen Stücklisteneintrag, ob er bei den übergebenen Saas gültig ist und setzt optinal den Änderungsgrund
     * Hier wird nicht getestet, ob der Eintrag ein Verweis auf eine freischwebende SA ist
     *
     * @param partListEntry
     * @param saas
     * @param isModelFilter
     * @param isSAAssembly
     * @param filter
     * @return
     */
    static boolean basicCheckSaStrichValidityFilter(EtkDataPartListEntry partListEntry, Set<String> saas, boolean isModelFilter,
                                                    boolean isSAAssembly, iPartsFilter filter) {
        boolean result = basicCheckSaStrichValidityFilter(partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_SA_VALIDITY), saas);


        if (!result && (filter != null) && filter.isWithFilterReason()) {
            iPartsFilterSwitchboard.FilterTypes filterType = getFilterType(isSAAssembly, isModelFilter);
            filter.setFilterReasonForPartListEntry(partListEntry, filterType, "!!Keine SAA oder Baukasten %1 in SAA/Baukasten-Gültigkeit enthalten",
                                                   getFilterReasonSourceName(isModelFilter));
        }
        return result;

    }

    static boolean basicCheckSaStrichValidityFilter(EtkDataArray dataArray, Set<String> saas) {
        return checkSAStrichValidity(dataArray, saas);

    }

    /**
     * Testet einen Stücklisteneintrag, ob er bei den Saas der übergebenen Datenkarte gültig ist
     * Hier wird nicht getestet, ob der Eintrag ein Verweis auf eine freischwebende SA ist
     *
     * @param dataArray
     * @param saas
     * @return
     */
    private static boolean checkSAStrichValidity(EtkDataArray dataArray, Set<String> saas) {
        if (saas != null) {
            if ((dataArray == null) || dataArray.isEmpty()) {
                // Array kann auch null sein, beim Rootknoten
                return true;
            }

            // dataArray ist nicht leer, saas aber schon -> SAA-Gültigkeit muss false ergeben
            if (saas.isEmpty()) {
                return false;
            }

            for (DBDataObjectAttribute saaNoFromArray : dataArray.getAttributes()) {
                if (saas.contains(saaNoFromArray.getAsString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Überprüft ob freie SAs ausgegeben werden dürfen. Falls nicht, werden sie ausgefiltert
     *
     * @param filter
     * @param partListEntry
     * @param filterEntries
     */
    public static void checkShowLooseSas(iPartsFilter filter, iPartsDataPartListEntry partListEntry, iPartsFilterPartsEntries filterEntries) {
        // Soll die Admin-Konfiguration überhaupt herangezogen werden?
        if (!filter.isIgnoreLooseSaConfiguration()) {
            // Es handelt sich um eine freie SA oder Teile innerhalb einer freien SA
            boolean isSaAssembly = partListEntry.isAssembly() && (iPartsVirtualNode.isKgSaNode(partListEntry.getVirtualNodesPathForDestinationAssembly())
                                                                  || iPartsVirtualNode.isKgSaNode(iPartsVirtualNode.parseVirtualIds(partListEntry.getOwnerAssemblyId())));
            boolean isInLooseSa = filterEntries.getPartListEntriesOwnerAssembly().isSAAssembly();
            if (isSaAssembly || isInLooseSa) {
                if (!checkConfigurationForLooseSas(filter) && !showLooseSasInProduct(partListEntry)) {
                    filterEntries.hideAll(filter, iPartsFilterSwitchboard.FilterTypes.CONFIGURATION, "!!Freie SAs nur bei FIN/Aggregateident-Einstieg ausgeben");
                    filterEntries.setFinished(true);
                }
            }
        }
    }

    /**
     * Überprüft, ob das Produkt an der Verwendungsstelle freie SAs im Baum / in der Stückliste zulässt.
     *
     * @param partListEntry
     * @return
     */
    private static boolean showLooseSasInProduct(iPartsDataPartListEntry partListEntry) {
        String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(partListEntry.getOwnerAssemblyId());
        if (StrUtils.isValid(productNumber)) {
            return iPartsProduct.getInstance(partListEntry.getEtkProject(), new iPartsProductId(productNumber)).showLooseSasWhileFiltering();
        }
        return false;
    }

    /**
     * Überprüft, ob die Admin-Konfiguration für die freien SAs und der Zustand der aktuellen Datenkarte es erlauben
     * freie SAs auszugeben
     *
     * @param filter
     * @return {@code true} wenn die freien SAs ausgegeben werden sollen, {@code false} wenn nicht
     */
    public static boolean checkConfigurationForLooseSas(iPartsFilter filter) {
        if (!filter.isIgnoreLooseSaConfiguration() && filter.isShowSAsOnlyForFin()) {
            return filter.getCurrentDataCard().isDataCardLoaded() || !filter.getCurrentDataCard().isModelLoaded();
        }
        return true;
    }

    /**
     * Filterung Stücklisteneintrag der auf eine frei schwebendes SA-Modul zeigt.
     * Nur wenn diese SA-Nummer in der Liste ist, dann wird das Modul angezeigt
     *
     * @param partsListEntry Stücklisteneintrag
     * @param saNumbers
     * @param isModelFilter
     * @param filter
     * @return
     */
    public static boolean basicCheckSaModuleFilter(iPartsDataPartListEntry partsListEntry, Set<String> saNumbers, boolean isModelFilter,
                                                   iPartsFilter filter) {
        List<iPartsVirtualNode> virtualNodes = null;
        if (partsListEntry.getOwnerAssembly().isRetailSaStructurePartList()) {
            AssemblyId destinationAssemblyId = partsListEntry.getDestinationAssemblyId();
            virtualNodes = iPartsVirtualNode.parseVirtualIds(destinationAssemblyId);
        } else if (partsListEntry.getOwnerAssembly().getEbeneName().equals(iPartsConst.PARTS_LIST_TYPE_STRUCT_SA)) {
            virtualNodes = iPartsVirtualNode.parseVirtualIds(partsListEntry.getOwnerAssemblyId());
        }

        if (Utils.isValid(virtualNodes)) {
            iPartsVirtualNode lastNode = virtualNodes.get(virtualNodes.size() - 1);
            if (lastNode.getType() == iPartsNodeType.KGSA) {
                /**
                 * Freies in KG eingehängtes SA-Modul darf nur angezeigt werden, wenn eines der SAAs aus der 'virtuellen Datenkarte'
                 * (saaNumbersList) zur SA gehört.
                 * Aus einer SAA-Nummer ergibt sich durch Entfernen der letzten beiden Stellen die SA-Nummer.
                 */
                String saNumber = ((KgSaId)lastNode.getId()).getSa();
                boolean result = saNumbers.contains(saNumber);
                if (!result && (filter != null) && filter.isWithFilterReason()) {
                    filter.setFilterReasonForPartListEntry(partsListEntry, isModelFilter ? iPartsFilterSwitchboard.FilterTypes.MODEL : iPartsFilterSwitchboard.FilterTypes.DATACARD_SA,
                                                           "!!Freie SA \"%1\" nicht enthalten in den SAAs %2",
                                                           iPartsNumberHelper.formatPartNo(partsListEntry.getEtkProject(), saNumber),
                                                           getFilterReasonSourceName(isModelFilter));

                }
                return result;
            }
        }

        return true;
    }

    /**
     * Testet, ob das übergebene PSK-Varianten-Gültigkeiten-Array für die übergebenen PSK-Varianten gültig ist.
     *
     * @param dataArray
     * @param pskVariants
     * @return
     */
    static boolean basicCheckPSKVariantsValidityFilter(EtkDataArray dataArray, Set<String> pskVariants) {
        // Ohne ausgewählte PSK-Varianten ist das Ergebnis immer gültig
        if ((pskVariants == null) || pskVariants.isEmpty()) {
            return true;
        }

        // Die Prüfung bzgl. PSK-Varianten-Gültigkeiten ist ansonsten analog zu den SAA-Gültigkeiten
        return checkSAStrichValidity(dataArray, pskVariants);
    }

    /**
     * Basisfilterfunktion für den Endnummernfilter für Stücklisten
     *
     * @param partListEntry
     * @param partListEntriesValidForEndNumber
     * @param dataCard
     * @param filter
     * @return
     */
    static public boolean basicCheckEndNumberFilter(iPartsDataPartListEntry partListEntry, Map<String, Boolean> partListEntriesValidForEndNumber,
                                                    AbstractDataCard dataCard, iPartsFilter filter) {
        return basicCheckEndNumberFilter(partListEntry, partListEntriesValidForEndNumber, dataCard, null, null, filter);
    }

    /**
     * Basisfilterfunktion für den Endnummernfilter für Farbvarianteninhalte
     *
     * @param vehicleDataCard
     * @param etkProject
     * @param colortablePEMEvaluation
     * @param filter
     * @return
     */
    static public boolean checkColorEndNumberFilter(VehicleDataCard vehicleDataCard, EtkProject etkProject,
                                                    iPartsColorTable.AbstractColortablePEMEvaluation colortablePEMEvaluation,
                                                    iPartsFilter filter) {
        return basicCheckEndNumberFilter(null, null, vehicleDataCard, colortablePEMEvaluation, etkProject, filter);
    }

    /**
     * Basisfilterfunktion für den Endnummernfilter
     * Wird für Stücklisteneinträge und Farbvarianteninhalte verwendet.
     * <p/>
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry                    null für den Aufruf für Farbvarianteninhalt
     * @param partListEntriesValidForEndNumber null für den Aufruf für Farbvarianteninhalt
     * @param dataCard                         Fahrzeug- oder Aggregate-Datenkarte
     * @param colortablePEMEvaluation          PEMs und Werksdaten für Farbinhalte oder Farben zu Teil; null für den
     *                                         Aufruf für Stücklisteneintrag (<i>partListEntry</i> != null)
     * @param etkProject
     * @param filter
     * @return
     */
    static private boolean basicCheckEndNumberFilter(iPartsDataPartListEntry partListEntry, Map<String, Boolean> partListEntriesValidForEndNumber,
                                                     AbstractDataCard dataCard, iPartsColorTable.AbstractColortablePEMEvaluation colortablePEMEvaluation,
                                                     EtkProject etkProject, iPartsFilter filter) {
        boolean isPartListEntryEndnumber = partListEntry != null;

        // Bei der Prüfung von Werkseinssatzdaten für Farbvarianteninhalte (mit partListEntry == null) gibt es Parameterabhängigkeiten
        if (!isPartListEntryEndnumber && (partListEntriesValidForEndNumber != null)) {
            Logger.getLogger().throwRuntimeException("basicCheckEndNumberFilter(): invalid parameter combination");
        }

        // wenn wir einen PartListEntry haben, bekommen wir daraus unser Projekt
        if ((etkProject == null) && isPartListEntryEndnumber) {
            etkProject = partListEntry.getEtkProject();
        }

        // Ohne Datenkarte macht die Filterung keinen Sinn ==> nichts unterdrücken sondern alles anzeigen.
        if (dataCard == null) {
            return true;
        }
        // Es muss relevante Werkseinsatzdaten für gültige Werke geben oder nur Werkseinsatzdaten für ungültige Werke
        if (isPartListEntryEndnumber) {
            boolean isRelevantForEndNumberFilter = partListEntry.isValidFactoryDataRelevantForEndNumberFilter()
                                                   || ((partListEntry.getFactoryDataForRetail() != null) && !partListEntry.getFactoryDataForRetail().hasValidFactories());
            if (!isRelevantForEndNumberFilter) {
                setPartListEntryValidForEndNumberFilter(partListEntry, partListEntriesValidForEndNumber);
                return true;
            }
        } else if (colortablePEMEvaluation != null) {
            // Bei Farbvarianten den Endnummernfilter überspringen, wenn beide PEM-Auswerten-Flags false sind
            if (!colortablePEMEvaluation.isEvalPemFrom() && !colortablePEMEvaluation.isEvalPemTo()) {
                return true;
            }
        }

        iPartsDocumentationType documentationType;
        if (isPartListEntryEndnumber) {
            documentationType = partListEntry.getOwnerAssembly().getDocumentationType();
        } else {
            documentationType = iPartsDocumentationType.DIALOG;
        }

        if (!documentationType.isTruckDocumentationType()) {
            // partListEntry.getFactoryDataValidity() hier nicht verwenden, da bei nicht vorhandenen Ersetzungen (egal
            // ob für gültige oder ungültige WE) der Endnummernfilter einfach überhaupt nicht laufen soll (siehe Abbruchkriterium
            // mit "isRelevantForEndNumberFilter" weiter oben); da unterscheidet sich obige Methode im Detail und deswegen
            // hier die separaten Fallunterscheidungen

            // 1. Liegen zur Teileposition keine Werksdaten vor, dann wird die Teileposition angezeigt.
            if (isPartListEntryEndnumber) {
                if ((partListEntry.getFactoryDataForRetail() == null) || (ignoreInvalidFactories(filter) && (partListEntry.getFactoryDataForRetail().getFactoryDataMap() == null))) {
                    setPartListEntryValidForEndNumberFilter(partListEntry, partListEntriesValidForEndNumber);
                    return true;
                } else if (!partListEntry.hasValidFactoryDataForRetail()) { // 2. Liegen zur Teileposition Werksdaten vor, dann ist der Retailfilter für Teilepositionen anzuwenden.
                    setFilterReasonForPartListEntry(partListEntry, filter, iPartsFilterSwitchboard.FilterTypes.END_NUMBER,
                                                    "!!Nur ungültige Werkseinsatzdaten vorhanden");
                    return false;
                }
            } else if (colortablePEMEvaluation == null) {
                // Keine Ausfilterung für einen Stücklisteneintrag -> filter.setFilterReasonForPartListEntry() kann hier
                // nicht aufgerufen werden, weil der Filtergrund an colortablePEMEvaluation gehängt werden müsste, was aber
                // null ist
                return false; // Farbvarianteninhalte müssen Werkseinsatzdaten haben
            }
        }

        // Falls die Datenkarte für den Endnummern-Filter gar keine gültigen Werte enthält, den restlichen Endnummern-Filter
        // überspringen; setPartListEntryValidForEndNumberFilter() muss nicht aufgerufen werden, weil die Gültigkeit der
        // Datenkarte ja nicht vom Stücklisteneintrag abhängig ist und demzufolge bei allen Stücklisteneinträgen false ergibt.
        List<iPartsProduct> productList = null;
        iPartsProduct product = null;
        if (documentationType.isPKWDocumentationType()) {
            if (isPartListEntryEndnumber) {
                // die Gültigkeit ist produktabhängig
                iPartsDataAssembly assembly = partListEntry.getOwnerAssembly();
                iPartsProductId productId = assembly.getProductIdFromModuleUsage();
                if (productId != null) {
                    product = iPartsProduct.getInstance(etkProject, productId);
                    productList = new ArrayList<>(1);
                    productList.add(product);
                }
            }
        }
        if (!dataCard.isValidForEndNumberFilter(productList, etkProject)) {
            return true;
        }

        boolean isVehicle = dataCard instanceof VehicleDataCard;

        // Die Werte zur Berechnung der Endnummer aus der Datenkarte extrahieren und für ELDAS und DIALOG bereitstellen.
        String wmi;
        iPartsModelId modelId = new iPartsModelId(dataCard.getModelNo());
        String factorySignFromDatacard = dataCard.getFactorySign(etkProject);
        if (isVehicle) {
            FinId fin = ((VehicleDataCard)dataCard).getFinId();
            wmi = fin.getWorldManufacturerIdentifier();
        } else {
            wmi = "";
        }

        iPartsFactoryModel factoryModelInstance = iPartsFactoryModel.getInstance(etkProject);
        iPartsFactoryModel.SerialNoAndFactory serialNoAndFactoryFromDatacard = dataCard.getSerialNumberWithOverflowAndFactory(etkProject);
        int datacardSerialNumber = serialNoAndFactoryFromDatacard.getSerialNumber();

        // Für Aggregate-Datenkarten kann die Datenkarte des Fahrzeugs (wenn vorhanden) eine Rolle spielen
        // Wir bestimmen uns daher auch alle filterrelevanten Daten aus dieser Datenkarte
        iPartsFactoryModel.SerialNoAndFactory parentSerialNoAndFactoryFromDatacard = null;
        int parentDatacardSerialNumber = FinId.INVALID_SERIAL_NUMBER;
        String parentWmi = null;
        VehicleDataCard parentDataCard = null;
        iPartsModelId parentModelId = null;
        String parentFactorySignFromDatacard = null;
        if (!isVehicle && (((AggregateDataCard)dataCard).getParentDatacard() != null)) {
            parentDataCard = ((AggregateDataCard)dataCard).getParentDatacard();
            parentSerialNoAndFactoryFromDatacard = parentDataCard.getSerialNumberWithOverflowAndFactory(etkProject);
            parentDatacardSerialNumber = parentSerialNoAndFactoryFromDatacard.getSerialNumber();
            FinId parentFin = parentDataCard.getFinId();
            parentWmi = parentFin.getWorldManufacturerIdentifier();
            parentModelId = new iPartsModelId(parentDataCard.getModelNo());
            parentFactorySignFromDatacard = parentDataCard.getFactorySign(etkProject);
        }

        boolean isWithFilterReason = (filter != null) && filter.isWithFilterReason();

        // ----------
        // Truck
        // ----------
        if (documentationType.isTruckDocumentationType() && isPartListEntryEndnumber) {
            iPartsFactoryData factoryDataForRetail = partListEntry.getFactoryDataForRetail();
            if (factoryDataForRetail != null) {
                VarParam<Boolean> onlyFactoryDataWithDifferentWMI = new VarParam<>(true);
                int validCounter = 0; // Zähler für Datensätze, die bezüglich dem WHC (WMI) Vergleich nicht berücksichtigt werden
                int totalCounter = 0; // Zähler für alle Datensätze
                // Über alle Werkseinsatzdaten/Rückmeldedaten iterieren und auf Endnummerngültigkeit prüfen
                // Bei migrierten ELDAS Daten enthält factoryDataForRetail.getFactoryDataMap() immer nur genau einen Eintrag für eine
                // leere Werksnummer, da es bei ELDAS-Werkseinsatzdaten keine Werksnummern gibt.
                // Bei in iParts erzeugten ELDAS Daten gibt es eine Werksnummer. Diese Daten sind analog DIALOG zu filtern
                for (List<iPartsFactoryData.DataForFactory> dataForFactoryList : factoryDataForRetail.getFactoryDataMap().values()) {
                    for (iPartsFactoryData.DataForFactory dataForFactory : dataForFactoryList) {
                        Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> filteredIdentsFrom = null;
                        Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> filteredIdentsTo = null;
                        // Vorfilterung nach WMI
                        // es werden nur Rückmeldedaten berücksichtigt deren WMI zur Datenkarte passt, oder leer ist
                        if (dataForFactory.hasPEMFrom() && dataForFactory.isResponseDataAvailableForPEMFrom()) {
                            Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> idents = dataForFactory.identsFrom;
                            totalCounter += idents.size();
                            filteredIdentsFrom = preFilterResponseDataForELDAS(idents, isVehicle, dataCard,
                                                                               datacardSerialNumber, wmi, parentDataCard,
                                                                               parentDatacardSerialNumber, parentWmi,
                                                                               onlyFactoryDataWithDifferentWMI);
                        }
                        if (dataForFactory.hasPEMTo() && dataForFactory.isResponseDataAvailableForPEMTo()) {
                            Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> idents = dataForFactory.identsTo;
                            totalCounter += idents.size();
                            filteredIdentsTo = preFilterResponseDataForELDAS(idents, isVehicle, dataCard,
                                                                             datacardSerialNumber, wmi, parentDataCard,
                                                                             parentDatacardSerialNumber, parentWmi,
                                                                             onlyFactoryDataWithDifferentWMI);
                        }

                        if (filteredIdentsFrom != null) {
                            validCounter += filteredIdentsFrom.size();
                        }
                        if (filteredIdentsTo != null) {
                            validCounter += filteredIdentsTo.size();
                        }


                        // DAIMLER-14948: Es ist zu prüfen, ob zur PEM-AB ein Ident vorliegt.
                        if ((filteredIdentsFrom != null) && !filteredIdentsFrom.isEmpty() && partListEntry.isPEMFromRelevant()) {
                            // Pem ab Filtern
                            boolean pemFromResult = checkResponseIdent(filteredIdentsFrom, dataForFactory.pemFrom, etkProject, factoryModelInstance,
                                                                       dataCard.getEndNumber(etkProject), filter, partListEntry, true,
                                                                       isVehicle, dataCard, datacardSerialNumber, wmi, modelId, factorySignFromDatacard, serialNoAndFactoryFromDatacard,
                                                                       parentDataCard, parentDatacardSerialNumber, parentWmi, parentModelId, parentFactorySignFromDatacard, parentSerialNoAndFactoryFromDatacard);
                            if (!pemFromResult) {
                                return false; // bei true wird weiter geprüft
                            }
                        }

                        // DAIMLER-14948: Es ist zu prüfen, ob zur PEM-BIS ein Ident vorliegt.
                        if ((filteredIdentsTo != null) && !filteredIdentsTo.isEmpty() && partListEntry.isPEMToRelevant()) {
                            // Pem bis filtern
                            boolean pemToResult = checkResponseIdent(filteredIdentsTo, dataForFactory.pemTo, etkProject, factoryModelInstance,
                                                                     dataCard.getEndNumber(etkProject), filter, partListEntry, false,
                                                                     isVehicle, dataCard, datacardSerialNumber, wmi, modelId, factorySignFromDatacard, serialNoAndFactoryFromDatacard,
                                                                     parentDataCard, parentDatacardSerialNumber, parentWmi, parentModelId, parentFactorySignFromDatacard, parentSerialNoAndFactoryFromDatacard);
                            if (!pemToResult) {
                                return false; // bei true wird weiter geprüft
                            }
                        }
                    }
                }

                // Wenn die WMI Angaben aller Rückmeldedaten nicht zur WMI der FIN passen, dann soll der Stücklisteneintrag
                // ausgefiltert werden. Und das nur, wenn es Werkseinsatzdaten gab, die bezüglich dem WMI geprüft wurden.
                // es wurden Einträge ausgefiltert und es gab keinen einzigen erfolgreichen WMI Check
                if ((validCounter != totalCounter) && onlyFactoryDataWithDifferentWMI.getValue()) {
                    if (isWithFilterReason) {
                        filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.END_NUMBER,
                                                               "!!WMIs der Rückmeldedaten sind weder leer noch passen " +
                                                               "sie zum WMI \"%1\" der FIN", wmi);
                    }
                    return false;
                }
            }

            // ----------
            // PKW
            // ----------
        } else if (documentationType.isPKWDocumentationType()) {
            if (isPartListEntryEndnumber && (partListEntriesValidForEndNumber != null) && Utils.objectEquals(partListEntriesValidForEndNumber.get(partListEntry.getAsId().getKLfdnr()), true)) {
                // Endnummernfilter muss nicht mehr berechnet werden, da dieser Stücklisteneintrag bereits als gültig markiert
                // wurde bzgl. des Endnummernfilters durch Ersetzungen
                return true;
            }

            // Bei Farbvarianten die Baureihe bestimmen
            iPartsSeriesId seriesId = null;
            if (!isPartListEntryEndnumber) {
                String colorTableId = colortablePEMEvaluation.getColorTableId();
                if (StrUtils.isValid(colorTableId) && colorTableId.startsWith(ColorTableHelper.QFT_COLORTABLE_ID_PREFIX)) {
                    seriesId = new iPartsSeriesId(ColorTableHelper.extractSeriesNumberFromTableId(colorTableId));
                }
            }

            // Sollen die Rückmelde-Idents ausgewertet werden?
            // Normal ja; am Produkt kann aber eine Ausnahme eingetragen sein.
            // Gilt nur für Fahrzeugdatenkarten mit gültigem TTZ-Datum und Stücklistenfilter oder Farbvariantenfilter mit
            // mindestens einem Produkt mit TTZ-Filter für die Baureihe
            boolean withFilterIdents;
            long filterDate;
            if (isVehicle && ((VehicleDataCard)dataCard).isDateOfTechnicalStateValid()
                && ((isPartListEntryEndnumber && (product != null) && product.isTtzFilter()) // Filterung mit TTZ in der Stückliste
                    || (!isPartListEntryEndnumber && (filter != null) && filter.isTTZFilterForSeries(seriesId, etkProject)))) { // Filterung mit TTZ für Farbvarianten
                withFilterIdents = false;
                filterDate = iPartsFactoryData.getFactoryDateFromDateString(((VehicleDataCard)dataCard).getDateOfTechnicalState(),
                                                                            "VehicleDataCard.dateOfTechnicalState");
            } else {
                // der Normalfall
                withFilterIdents = true;
                filterDate = iPartsFactoryData.getFactoryDateFromDateString(dataCard.getTechnicalApprovalDate(), "VehicleDataCard.technicalApprovalDate");
            }

            // Liste aller möglichen Werkseinsatzdatenlisten (normalerweise nur für die Werksnummer der Datenkarte, bei
            // Motor-Datenkarten mit alter Ident-Systematik aber für alle Werksnummern der Werkseinsatzdaten)
            List<List<? extends iPartsFactoryData.AbstractDataForFactory>> relevantDataForFactoryLists = new DwList<>();
            if (!isOldEngineIdentDataCard(dataCard, isVehicle)) {
                // 3. Liegen nach dem Retail-Filter zur Teileposition keine Werksdaten zum ermittelten Werk vor,
                // dann ist die Teileposition nicht auszugeben.
                String factoryNumber = dataCard.getFactoryNumber(etkProject);

                // 3.a) Kein Werk
                if (StrUtils.isEmpty(factoryNumber)) {
                    setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                          "!!Keine Werksnummer für den WMI und Werkskennbuchstaben der FIN bzw. des Aggregate-Idents gefunden");
                    return false;
                }

                // 3.b) Keine Daten zum Werk
                List<? extends iPartsFactoryData.AbstractDataForFactory> dataForFactoryList = null;
                // Nicht relevante Werke sollen wie "keine Werksdaten" behandelt werden
                if (iPartsFactories.getInstance(etkProject).isValidForFilter(factoryNumber)) {
                    if (isPartListEntryEndnumber) {
                        dataForFactoryList = partListEntry.getFactoryDataForRetail().getDataForFactory(factoryNumber);
                    } else {
                        dataForFactoryList = colortablePEMEvaluation.getFactoryData().getDataForFactory(factoryNumber);
                    }
                }
                if ((dataForFactoryList == null) || dataForFactoryList.isEmpty()) {
                    setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                          "!!Keine (relevanten) Werkseinsatzdaten für die Werksnummer \"%1\" vorhanden",
                                                          factoryNumber);
                    return false;
                }

                // Nur die Werkseinsatzdatenliste für die Werksnummer von der Datenkarte hinzufügen
                relevantDataForFactoryLists.add(dataForFactoryList);
            } else {
                // Alle Werkseinsatzdatenlisten hinzufügen
                if (isPartListEntryEndnumber) {
                    relevantDataForFactoryLists.addAll(partListEntry.getFactoryDataForRetail().getFactoryDataMap().values());
                } else {
                    relevantDataForFactoryLists.addAll(colortablePEMEvaluation.getFactoryData().getFactoryDataMap().values());
                }
            }

            // 4. Es liegen Werksdaten zum ermittelten Werk vor (bzw. alte Ident-Systematik bei Motoren)
            String spikeIdent = dataCard.getSpikeIdent(etkProject);

            // Merker, ob mindestens ein Aufruf von basicCheckEndNumberFilterForDIALOGFactoryList() false zurückgeliefert
            // hat und demzufolge die Rückmelde-Idents nicht gepasst haben. Bei mehr als einer Werkseinsatzdatenliste
            // in relevantDataForFactoryLists müssen nämlich alle Rückmeldedaten überprüft werden. Nur, wenn in partListEntriesValidForEndNumber
            // explizit der Stücklisteneintrag als gültig gekennzeichnet wurde (weil ein Rückmelde-Ident gepasst hat) oder
            // kein Aufruf von basicCheckEndNumberFilterForDIALOGFactoryList() explizit false zurückgeliefert hat, ist der
            // Stücklisteneintrag gültig
            boolean atLeastOneResponseIdentIsInvalid = false;

            // Filterung der Idents nach Montagewerk?
            String identFactorySignFilterValue = null;
            boolean isIdentFactoryFiltering = (product != null) && product.isIdentFactoryFiltering();
            if (isIdentFactoryFiltering) {
                identFactorySignFilterValue = factorySignFromDatacard;
            }

            // Über die Liste relevantDataForFactoryLists mit den Werkseinsatzdatenlisten iterieren
            for (List<? extends iPartsFactoryData.AbstractDataForFactory> dataForFactoryList : relevantDataForFactoryLists) {
                if (basicCheckEndNumberFilterForDIALOGFactoryList(dataForFactoryList, isPartListEntryEndnumber, partListEntry,
                                                                  colortablePEMEvaluation, isVehicle, dataCard, datacardSerialNumber, factorySignFromDatacard,
                                                                  serialNoAndFactoryFromDatacard, wmi, modelId, spikeIdent,
                                                                  partListEntriesValidForEndNumber, factoryModelInstance,
                                                                  filter, withFilterIdents, identFactorySignFilterValue, filterDate,
                                                                  etkProject)) {
                    // Überprüfen, ob der Stücklisteneintrag für den Endnummernfilter explizit als gültig gesetzt wurde
                    // Falls ja: direkt mit true rausspringen
                    if (isPartListEntryEndnumber && (partListEntriesValidForEndNumber != null)) {
                        Boolean partListEntryValid = partListEntriesValidForEndNumber.get(partListEntry.getAsId().getKLfdnr());
                        if (Utils.objectEquals(partListEntryValid, true)) {
                            return true;
                        }
                    }
                } else {
                    atLeastOneResponseIdentIsInvalid = true;
                }
            }

            if (!atLeastOneResponseIdentIsInvalid) {
                // Stücklisteneintrag ist gültig für den Endnummernfilter
                setPartListEntryValidForEndNumberFilter(partListEntry, partListEntriesValidForEndNumber);
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * Vorfilterung der Rückmeldedaten für ELDAS nach WMI
     * Dazu wird je nach Typ mit dem WMI der eigenen oder der parent-Datenkarte verglichen.
     *
     * @param idents
     * @param isVehicle
     * @param dataCard
     * @param datacardSerialNumber
     * @param wmi
     * @param parentDataCard
     * @param parentDatacardSerialNumber
     * @param parentWmi
     * @param onlyFactoryDataWithDifferentWMI
     * @return eine Map die nur noch gültige Rückmeldedaten enthält
     */
    private static Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> preFilterResponseDataForELDAS(
            Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> idents, boolean isVehicle, AbstractDataCard dataCard, int datacardSerialNumber,
            String wmi, VehicleDataCard parentDataCard, int parentDatacardSerialNumber, String parentWmi, VarParam<Boolean> onlyFactoryDataWithDifferentWMI) {

        Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> filteredIdents = new TreeMap<>();

        for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> identEntrySet : idents.entrySet()) {
            iPartsFactoryData.IdentWithModelNumber identWithModelNumber = identEntrySet.getKey();
            Set<String> spikes = identEntrySet.getValue();

            String eldasType = identWithModelNumber.eldasType;
            if (StrUtils.isEmpty(eldasType)) {
                continue;
            }

            boolean isVehicleType;
            if (identWithModelNumber.source == iPartsImportDataOrigin.IPARTS) {
                isVehicleType = eldasType.equals(iPartsConst.AGGREGATE_TYPE_CAR);
            } else {
                if (eldasType.length() < 2) {
                    continue;
                }

                // Rückmeldetyp muss für Fahrzeuge AF bzw. BF sein und für Aggregate A? bzw. B? mit ? != F
                char responseTypeFirstChar = eldasType.charAt(0);
                boolean isIdentFrom = responseTypeFirstChar == 'A';
                boolean isIdentTo = responseTypeFirstChar == 'B';
                if (!(isIdentFrom || isIdentTo)) {
                    continue;
                }
                char responseTypeSecondChar = eldasType.charAt(1);
                isVehicleType = responseTypeSecondChar == 'F';
            }
            if (isVehicle && !isVehicleType) {
                continue;
            }

            // Bestimmen ob ggf. die Parent-Datenkarte für die Filterung herangezogen wird. Hier gibt es eine Ausnahme für Aggregat FH
            int filterDataCardSerialNumber = datacardSerialNumber;
            String filterWmi = wmi;
            if ((parentDataCard != null)
                && isVehicleType
                && (((AggregateDataCard)dataCard).getAggregateType() == DCAggregateTypes.CAB)) {
                // Datenkarte des Fahrzeugs verwenden
                filterDataCardSerialNumber = parentDatacardSerialNumber;
                filterWmi = parentWmi;
            }

            // Bei ungültigem Millionenüberlauf ist keine Filterung möglich!
            if (filterDataCardSerialNumber == FinId.INVALID_SERIAL_NUMBER) {
                continue;
            }

            String eldasWMI = identWithModelNumber.eldasWMI;
            // DAIMLER-5994: Liegen nur Rückmeldedaten vor, deren WHC (WMI) nicht leer sind und nicht mit der
            // WMI der FIN übereinstimmen, dann ist die Teileposition auszufiltern. Pro Iteration wird überprüft,
            // ob die WMI Angabe der Rückmeldedaten nicht leer ist und nicht mit der WMI Angabe der FIN übereinstimmt.
            if (onlyFactoryDataWithDifferentWMI != null) {
                onlyFactoryDataWithDifferentWMI.setValue(onlyFactoryDataWithDifferentWMI.getValue() && (!eldasWMI.isEmpty() && !eldasWMI.equals(filterWmi)));
            }

            // WMI in den Rückmeldedaten muss leer sein oder dem WMI aus der Datenkarte entsprechen
            if (!(StrUtils.isEmpty(eldasWMI) || eldasWMI.equals(filterWmi))) {
                continue;
            }

            filteredIdents.put(identWithModelNumber, spikes);
        }
        return filteredIdents;
    }

    /**
     * Prüfung der Rückmelde-Idents für ELDAS mit Unterscheidung nach Pem ab/bis
     *
     * @param idents
     * @param pem
     * @param etkProject
     * @param factoryModelInstance
     * @param dataCardOriginalEndNumber
     * @param filter
     * @param partListEntry
     * @param isPEMfrom
     * @param isVehicle
     * @param dataCard
     * @param datacardSerialNumber
     * @param wmi
     * @param modelId
     * @param factorySignFromDatacard
     * @param serialNoAndFactoryFromDatacard
     * @param parentDataCard
     * @param parentDatacardSerialNumber
     * @param parentWmi
     * @param parentModelId
     * @param parentFactorySignFromDatacard
     * @param parentSerialNoAndFactoryFromDatacard
     * @return
     */
    private static boolean checkResponseIdent(Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> idents, String pem,
                                              EtkProject etkProject, iPartsFactoryModel factoryModelInstance,
                                              int dataCardOriginalEndNumber, iPartsFilter filter, iPartsDataPartListEntry partListEntry,
                                              boolean isPEMfrom, boolean isVehicle, AbstractDataCard dataCard, int datacardSerialNumber,
                                              String wmi, iPartsModelId modelId, String factorySignFromDatacard,
                                              iPartsFactoryModel.SerialNoAndFactory serialNoAndFactoryFromDatacard, VehicleDataCard parentDataCard,
                                              int parentDatacardSerialNumber, String parentWmi, iPartsModelId parentModelId,
                                              String parentFactorySignFromDatacard,
                                              iPartsFactoryModel.SerialNoAndFactory parentSerialNoAndFactoryFromDatacard) {
        boolean validResponseSerialNumberFound = false;
        boolean matchingIdentFromFound = false;
        List<String> usedDatacardSpikeIdents = new DwList<>();
        List<String> usedDatacardIdents = new DwList<>();
        boolean usedParentDatacard = false;
        boolean usedOwnDatacard = false;
        for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> identWithModelEntry : idents.entrySet()) {
            boolean isVehicleType;
            iPartsFactoryData.IdentWithModelNumber identWithModel = identWithModelEntry.getKey();
            String eldasType = identWithModel.eldasType;
            if (identWithModel.source == iPartsImportDataOrigin.IPARTS) {
                isVehicleType = eldasType.equals(iPartsConst.AGGREGATE_TYPE_CAR);
            } else {
                isVehicleType = (eldasType.length() == 2) && (eldasType.charAt(1) == 'F');
            }

            // Bestimmen ob ggf. die Parent-Datenkarte für die Filterung herangezogen wird. Hier gibt es eine Ausnahme für Aggregat FH
            boolean isParentDataCard = false;
            boolean filterIsVehicle = isVehicle;
            AbstractDataCard filterDataCard = dataCard;
            int filterDataCardSerialNumber = datacardSerialNumber;
            String filterWmi = wmi;
            iPartsModelId filterModelId = modelId;
            String filterFactorySignFromDatacard = factorySignFromDatacard;
            iPartsFactoryModel.SerialNoAndFactory filterSerialNoAndFactoryFromDatacard = serialNoAndFactoryFromDatacard;
            if ((parentDataCard != null)
                && isVehicleType
                && (((AggregateDataCard)dataCard).getAggregateType() == DCAggregateTypes.CAB)) {
                // Datenkarte des Fahrzeugs verwenden
                isParentDataCard = true;
                usedParentDatacard = true;
                filterIsVehicle = true;
                filterDataCard = parentDataCard;
                filterDataCardSerialNumber = parentDatacardSerialNumber;
                filterWmi = parentWmi;
                filterModelId = parentModelId;
                filterFactorySignFromDatacard = parentFactorySignFromDatacard;
                filterSerialNoAndFactoryFromDatacard = parentSerialNoAndFactoryFromDatacard;
                dataCardOriginalEndNumber = filterDataCard.getEndNumber(etkProject);
            }

            if (!isParentDataCard) {
                usedOwnDatacard = true;
            }

            usedDatacardSpikeIdents.add(filterDataCard.getSpikeIdent(etkProject));
            usedDatacardIdents.add(String.valueOf(filterDataCardSerialNumber));

            SerialNoWithLength responseSerialNoWithLength = getResponseSerialNo(etkProject, filterDataCard, filterIsVehicle, filterWmi,
                                                                                filterModelId, identWithModel.ident, filterFactorySignFromDatacard,
                                                                                factoryModelInstance, filterSerialNoAndFactoryFromDatacard, false);

            if (!responseSerialNoWithLength.isInvalidSerialNo()) { // Ident mit evtl. Millionenüberlauf ist gültig
                validResponseSerialNumberFound = true;
                int dataCardSerialForCheck = filterDataCardSerialNumber;
                int responseSerialForCheck = responseSerialNoWithLength.getSerialNo(); // mit evtl. Längen Kürzung
                // Wenn der Rückmelde-Ident nicht vollständig verwendet werden soll, dann muss auch
                // von der Datenkarte der Original-Ident (ohne Millionenüberlauf) in der gleichen Länge verwendet werden
                if (responseSerialNoWithLength.hasLengthLimit()) {
                    dataCardSerialForCheck = SerialNoWithLength.getSerialWithLength(dataCardOriginalEndNumber, responseSerialNoWithLength.getLength());
                }

                // Start -- DAIMLER-14948: Filterlogik für Rückmeldedaten inkl. Berücksichtigung der Ausreißer
                Set<String> identSpikes = identWithModelEntry.getValue();
                String spikeIdent = filterDataCard.getSpikeIdent(etkProject);
                Optional<Boolean> result;
                if (isPEMfrom) {
                    result = doTruckResponsDataFromCheck(etkProject, partListEntry, filter, idents, pem,
                                                         dataCardSerialForCheck, responseSerialForCheck,
                                                         identSpikes, spikeIdent, identWithModel,
                                                         responseSerialNoWithLength, filterDataCard,
                                                         isParentDataCard);
                } else {
                    result = doTruckResponsDataToCheck(etkProject, partListEntry, filter, idents, pem,
                                                       dataCardSerialForCheck, responseSerialForCheck,
                                                       identSpikes, spikeIdent, identWithModel,
                                                       responseSerialNoWithLength, filterDataCard,
                                                       isParentDataCard);
                }
                // Ist es leer, dann hat nur ein Ident existiert und die Position war nicht gültig -> gleich rausspringen
                if (!result.isPresent()) {
                    return false;
                }
                if (result.get()) {
                    matchingIdentFromFound = true;
                    break;
                }
                // Ende -- DAIMLER-14948: Filterlogik für Rückmeldedaten inkl. Berücksichtigung der Ausreißer
            }
        }

        if (validResponseSerialNumberFound && !matchingIdentFromFound) {
            if ((filter != null) && filter.isWithFilterReason()) {
                String filterReasonString;
                if (isPEMfrom) {
                    filterReasonString = "!!PEM-ab \"%1\": Alle relevanten Idents ab > Idents der Datenkarte " +
                                         "(verwendete Ausreißer Idents: %3) (verwendete Idents der Datenkarte: %4)";
                } else {
                    filterReasonString = "!!PEM-bis \"%1\": Alle relevanten Idents bis < oder <= Idents der Datenkarte " +
                                         "(verwendete Ausreißer Idents: %3) (verwendete Idents der Datenkarte: %4)";
                }

                String filterReasonSource;
                if (usedParentDatacard && usedOwnDatacard) {
                    filterReasonSource = TranslationHandler.translate("!!von der Fahrzeug- und der Aggregate-Datenkarte");
                } else {
                    filterReasonSource = getFilterReasonSourceName(false, usedParentDatacard);
                }

                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.END_NUMBER,
                                                       filterReasonString,
                                                       pem, StrUtils.stringListToString(usedDatacardSpikeIdents, ", "),
                                                       StrUtils.stringListToString(usedDatacardIdents, ", "),
                                                       filterReasonSource);
            }
            return false;
        }
        return true;
    }

    /**
     * Prüfung der Truck BIS Rückmeldedaten inkl. Ausreißer
     *
     * @param etkProject
     * @param partListEntry
     * @param filter
     * @param idents
     * @param pem
     * @param dataCardSerialForCheck
     * @param responseSerialForCheck
     * @param identSpikes
     * @param spikeIdent
     * @param identWithModel
     * @param responseSerialNoWithLength
     * @param filterDataCard
     * @param isParentDataCard
     * @return
     */
    private static Optional<Boolean> doTruckResponsDataToCheck(EtkProject etkProject, iPartsDataPartListEntry partListEntry,
                                                               iPartsFilter filter, Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> idents,
                                                               String pem, int dataCardSerialForCheck, int responseSerialForCheck,
                                                               Set<String> identSpikes, String spikeIdent,
                                                               iPartsFactoryData.IdentWithModelNumber identWithModel,
                                                               SerialNoWithLength responseSerialNoWithLength,
                                                               AbstractDataCard filterDataCard, boolean isParentDataCard) {
        // 2. Falls Ident-Bis vorliegen ist zu prüfen, ob der Ident der FIN < dem Ident in den Rückmeldedaten
        // ist. Bei Truck Rückmeldedaten aus iParts soll die Ident Filterung wie bei DIALOG stattfinden:
        // Ident der FIN <= dem Ident in den Rückmeldedaten
        boolean identCompareResult;
        if (identWithModel.source == iPartsImportDataOrigin.IPARTS) {
            identCompareResult = (dataCardSerialForCheck < responseSerialForCheck);
        } else {
            identCompareResult = (dataCardSerialForCheck <= responseSerialForCheck);
        }
        if (identCompareResult) {
            // 2.1 Falls ja, ist prüfen ob der Ident als Ausreißer-Ident angegeben ist
            if ((identSpikes != null) && identSpikes.contains(spikeIdent)) {
                // 2.1.1 Falls ja, dann wird Teileposition nicht ausgegeben
                if (idents.size() == 1) {
                    // Bei nur einem zu prüfenden Ident, können wir direkt rausspringen, da die Position ungültig ist
                    setSpikeFilterReason(etkProject, partListEntry, filter, pem, spikeIdent, identWithModel,
                                         responseSerialForCheck, dataCardSerialForCheck, false);
                    return Optional.empty();
                }
            } else {
                // 2.1.2 Falls nein, dann wird die Teileposition ausgegeben
                return Optional.of(true);
            }
        } else {
            // 2.2 Falls nein, ist zu prüfen ob der Ident als Ausreißer-Ident angegeben ist
            if ((identSpikes != null) && identSpikes.contains(spikeIdent)) {
                // 2.2.1 Falls ja, dann wird die Teileposition ausgegeben
                return Optional.of(true);
            } else {
                // 2.2.2 Falls nein, dann wird die Teileposition nicht ausgegeben
                if (idents.size() == 1) {
                    // Bei nur einem zu prüfenden Ident, können wir direkt rausspringen, da die Position ungültig ist
                    setNonSpikeFilterReason(filter, responseSerialForCheck, dataCardSerialForCheck,
                                            responseSerialNoWithLength, false, identWithModel,
                                            partListEntry, etkProject, pem, filterDataCard, isParentDataCard);
                    return Optional.empty();
                }
            }
        }
        return Optional.of(false);
    }

    /**
     * Prüfung der Truck AB Rückmeldedaten inkl. Ausreißer
     *
     * @param etkProject
     * @param partListEntry
     * @param filter
     * @param idents
     * @param pem
     * @param dataCardSerialForCheck
     * @param responseSerialForCheck
     * @param identSpikes
     * @param spikeIdent
     * @param identWithModel
     * @param responseSerialNoWithLength
     * @param filterDataCard
     * @param isParentDataCard
     * @return
     */
    private static Optional<Boolean> doTruckResponsDataFromCheck(EtkProject etkProject, iPartsDataPartListEntry partListEntry,
                                                                 iPartsFilter filter, Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> idents,
                                                                 String pem, int dataCardSerialForCheck,
                                                                 int responseSerialForCheck, Set<String> identSpikes,
                                                                 String spikeIdent, iPartsFactoryData.IdentWithModelNumber identWithModel,
                                                                 SerialNoWithLength responseSerialNoWithLength,
                                                                 AbstractDataCard filterDataCard, boolean isParentDataCard) {
        // 1. Falls Ident-Ab vorliegen, ist zu prüfen, ob der Ident der FIN >= dem Ident in den Rückmeldedaten ist
        if (dataCardSerialForCheck >= responseSerialForCheck) {
            // 1.1 Falls ja, ist zu prüfen, ob der Ident als Ausreißer-Ident angegeben ist.
            if ((identSpikes != null) && identSpikes.contains(spikeIdent)) {
                // 1.1.1 Falls ja, dann wird die Teileposition nicht ausgegeben
                if (idents.size() == 1) {
                    // Bei nur einem zu prüfenden Ident, können wir direkt rausspringen, da die Position ungültig ist
                    setSpikeFilterReason(etkProject, partListEntry, filter, pem, spikeIdent, identWithModel,
                                         responseSerialForCheck, dataCardSerialForCheck, true);
                    return Optional.empty();
                }
            } else {
                // 1.1.2 Falls nein, dann wird für die Teileposition die BIS-Einsatzdaten durchgeführt (beim
                // Aufruf von checkResponseIdent() für PEM-BIS)
                return Optional.of(true);
            }
        } else {
            // 1.2 Falls nein, ist zu prüfen ob der Ident als Ausreißer-Ident angegeben ist.
            if ((identSpikes != null) && identSpikes.contains(spikeIdent)) {
                // 1.2.1 Falls ja, dann wird für die Teileposition die BIS-Einsatzdaten durchgeführt (beim
                // Aufruf von checkResponseIdent() für PEM-BIS)
                return Optional.of(true);
            } else {
                // 1.2.2 Falls nein, dann wird die Teileposition nicht ausgegeben
                if (idents.size() == 1) {
                    // Bei nur einem zu prüfenden Ident, können wir direkt rausspringen, da die Position ungültig ist
                    setNonSpikeFilterReason(filter, responseSerialForCheck, dataCardSerialForCheck,
                                            responseSerialNoWithLength, true, identWithModel,
                                            partListEntry, etkProject, pem, filterDataCard, isParentDataCard);
                    return Optional.empty();
                }

            }
        }
        return Optional.of(false);
    }

    private static void setSpikeFilterReason(EtkProject etkProject, iPartsDataPartListEntry partListEntry,
                                             iPartsFilter filter, String pem, String spikeIdent,
                                             iPartsFactoryData.IdentWithModelNumber identWithModel,
                                             int responseSerialForCheck, int dataCardSerialForCheck, boolean isFromData) {
        setEndNumberFilterReasonForDataObject(partListEntry, null, filter, etkProject,
                                              isFromData ? "!!PEM ab \"%1\": Nachzügler \"%2\" zum Ident ab \"%3\" (%4)"
                                                         : "!!PEM bis \"%1\": Nachzügler \"%2\" zum Ident ab \"%3\" (%4)",
                                              pem, spikeIdent, identWithModel.ident,
                                              String.valueOf(responseSerialForCheck),
                                              spikeIdent, String.valueOf(dataCardSerialForCheck),
                                              getFilterReasonSourceName(false));
    }

    private static void setNonSpikeFilterReason(iPartsFilter filter, int responseSerialForCheck, int dataCardSerialForCheck,
                                                SerialNoWithLength responseSerialNoWithLength, boolean isPEMfrom,
                                                iPartsFactoryData.IdentWithModelNumber identWithModel,
                                                iPartsDataPartListEntry partListEntry, EtkProject etkProject, String pem,
                                                AbstractDataCard filterDataCard, boolean isParentDataCard) {
        if ((filter != null) && filter.isWithFilterReason()) {
            String responseSerialForReason = String.valueOf(responseSerialForCheck);
            String dataCardSerialForReason = String.valueOf(dataCardSerialForCheck);
            if (responseSerialNoWithLength.hasLengthLimit()) {
                int length = responseSerialNoWithLength.getLength();
                responseSerialForReason = StrUtils.prefixStringWithCharsUpToLength(responseSerialForReason, '0', length);
                dataCardSerialForReason = StrUtils.prefixStringWithCharsUpToLength(dataCardSerialForReason, '0', length);
            }
            String filterReasonString;
            String compareSign;
            if (isPEMfrom) {
                compareSign = ">";
                filterReasonString = "!!PEM-ab \"%1\": Ident-ab \"%2\" (%3) %7 \"%4\" (%5) %6";
            } else {
                compareSign = "<";
                if (identWithModel.source == iPartsImportDataOrigin.IPARTS) {
                    compareSign = "<=";
                }
                filterReasonString = "!!PEM-bis \"%1\": Ident-bis \"%2\" (%3) %7 \"%4\" (%5) %6";
            }
            filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.END_NUMBER,
                                                   filterReasonString,
                                                   pem, identWithModel.ident,
                                                   responseSerialForReason,
                                                   filterDataCard.getSpikeIdent(etkProject), dataCardSerialForReason,
                                                   getFilterReasonSourceName(false, isParentDataCard), compareSign);
        }
    }

    /**
     * Interne Endnummernfilterlogik für DIALOG
     *
     * @param dataForFactoryList
     * @param isPartListEntryEndnumber         todo obsolet weil aus partListEntry ableitbar? (wurde vom Aufrufer auch so abgeleitet)
     * @param partListEntry
     * @param colortablePEMEvaluation
     * @param isVehicle                        todo obsolet weil aus dataCard ableitbar? (wurde vom Aufrufer auch so abgeleitet)
     * @param dataCard
     * @param datacardSerialNumber
     * @param factorySignFromDatacard
     * @param serialNoAndFactoryFromDatacard
     * @param wmi
     * @param modelId
     * @param spikeIdent
     * @param partListEntriesValidForEndNumber
     * @param factoryModelInstance
     * @param filter
     * @param withFilterIdents
     * @param identFactorySignFilterValue
     * @param filterDate
     * @param etkProject
     * @return
     */
    private static boolean basicCheckEndNumberFilterForDIALOGFactoryList(List<? extends iPartsFactoryData.AbstractDataForFactory> dataForFactoryList,
                                                                         boolean isPartListEntryEndnumber, iPartsDataPartListEntry partListEntry,
                                                                         iPartsColorTable.AbstractColortablePEMEvaluation colortablePEMEvaluation,
                                                                         boolean isVehicle, AbstractDataCard dataCard, int datacardSerialNumber,
                                                                         String factorySignFromDatacard, iPartsFactoryModel.SerialNoAndFactory serialNoAndFactoryFromDatacard,
                                                                         String wmi, iPartsModelId modelId, String spikeIdent,
                                                                         Map<String, Boolean> partListEntriesValidForEndNumber,
                                                                         iPartsFactoryModel factoryModelInstance, iPartsFilter filter,
                                                                         boolean withFilterIdents, String identFactorySignFilterValue,
                                                                         long filterDate, EtkProject etkProject) {
        boolean isWithFilterReason = (filter != null) && filter.isWithFilterReason();
        boolean result = true;
        Set<String> factorySignGrouping = factoryModelInstance.getFactorySignGrouping(wmi, factorySignFromDatacard, modelId,
                                                                                      serialNoAndFactoryFromDatacard.getFactoryNumber());
        for (iPartsFactoryData.AbstractDataForFactory dataForFactory : dataForFactoryList) {
            // Liegt eine WKB-Gruppierung vor, dann sollen nur die Idents geprüft werden, deren Kennbuchstabe mit den
            // WKBs der Gruppierung übereinstimmen
            Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> fromIdentsWithValidFactorySigns = filterIdentsByFactoryGrouping(dataForFactory,
                                                                                                                                     factorySignGrouping,
                                                                                                                                     identFactorySignFilterValue,
                                                                                                                                     true);
            Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> toIdentsWithValidFactorySigns = filterIdentsByFactoryGrouping(dataForFactory,
                                                                                                                                   factorySignGrouping,
                                                                                                                                   identFactorySignFilterValue,
                                                                                                                                   false);
            if (!isValidFactorySignData(factorySignGrouping, partListEntry, colortablePEMEvaluation, dataForFactory, fromIdentsWithValidFactorySigns,
                                        toIdentsWithValidFactorySigns, wmi, factorySignFromDatacard, modelId, serialNoAndFactoryFromDatacard,
                                        filter, etkProject)) {
                return false;
            }

            // Bei den Inhalten der Variantentabellen sollen Daten zu PEM ab / bis nur ausgewertet werden
            // wenn die entsprechenden Flags am Datensatz true sind.
            boolean colorTableEvalPemFrom = false;
            boolean colorTableEvalPemTo = false;
            if (!isPartListEntryEndnumber && (colortablePEMEvaluation != null)) {
                colorTableEvalPemFrom = colortablePEMEvaluation.isEvalPemFrom();
                colorTableEvalPemTo = colortablePEMEvaluation.isEvalPemTo();
            }
            boolean pemFromResult = true;
            boolean pemToResult = true;
            // DAIMLER-3993, PSEUDO-Ersetzungen migrieren und Einsatzdaten anhand RFME-Daten auswerten
            if (colorTableEvalPemFrom || (isPartListEntryEndnumber && partListEntry.isPEMFromRelevant())) {
                pemFromResult = checkDIALOGPemFromData(partListEntry, colortablePEMEvaluation, filter, dataForFactory,
                                                       fromIdentsWithValidFactorySigns, etkProject, dataCard, wmi,
                                                       datacardSerialNumber, modelId, filterDate, factoryModelInstance, spikeIdent,
                                                       serialNoAndFactoryFromDatacard, factorySignFromDatacard, withFilterIdents,
                                                       isVehicle, isWithFilterReason);

                // Wenn über die WKB-Gruppierung mehrere Idents möglich sind, dann darf hier noch nicht rausgesprungen werden
                if ((factorySignGrouping == null) && !pemFromResult) {
                    return false;
                }
            }

            // DAIMLER-3993, PSEUDO-Ersetzungen migrieren und Einsatzdaten anhand RFME-Daten auswerten
            if (colorTableEvalPemTo || (isPartListEntryEndnumber && partListEntry.isPEMToRelevant())) {
                pemToResult = checkDIALOGPemToData(partListEntry, colortablePEMEvaluation, filter, dataForFactory, partListEntriesValidForEndNumber,
                                                   toIdentsWithValidFactorySigns, etkProject, dataCard, wmi,
                                                   datacardSerialNumber, modelId, filterDate, factoryModelInstance, spikeIdent,
                                                   serialNoAndFactoryFromDatacard, factorySignFromDatacard, withFilterIdents,
                                                   isVehicle, isWithFilterReason);

                // Wenn über die WKB-Gruppierung mehrere Idents möglich sind, dann darf hier noch nicht rausgesprungen werden
                if ((factorySignGrouping == null) && !pemToResult) {
                    return false;
                }
            }

            // DAIMLER-6966
            // Ist nur am AB ein Ident zu den ermittelten Buchstaben vorhanden, dann ist an dieser Stelle die Ident-Prüfung
            // durchzuführen. BIS ist als gültig anzusehen.
            // Ist nur am BIS ein Ident zu den ermittelten Buchstaben vorhanden, dann ist an dieser Stelle die Ident-Prüfung
            // durchzuführen. AB ist als gültig anzusehen.
            if ((factorySignGrouping != null) && ((isPartListEntryEndnumber && partListEntry.isPEMFromRelevant() && partListEntry.isPEMToRelevant())
                                                  || (colorTableEvalPemFrom && colorTableEvalPemTo))) {
                if ((fromIdentsWithValidFactorySigns != null) && (toIdentsWithValidFactorySigns == null) && pemFromResult) {
                    return true;
                }
                if ((fromIdentsWithValidFactorySigns == null) && (toIdentsWithValidFactorySigns != null) && pemToResult) {
                    return true;
                }
            }
            result = pemFromResult && pemToResult;
        } // Ende der for-Schleife über die Werksdaten

        // Es wurde vorher nicht explizit mit true oder false rausgesprungen -> es gab offensichtlich keine passenden bzw.
        // nicht passenden Idents usw. -> die übergebene Werkseinsatzdatenliste ist daher gültig
        return result;
    }

    private static boolean checkDIALOGPemToData(iPartsDataPartListEntry partListEntry, iPartsColorTable.AbstractColortablePEMEvaluation colortablePEMEvaluation,
                                                iPartsFilter filter, iPartsFactoryData.AbstractDataForFactory dataForFactory,
                                                Map<String, Boolean> partListEntriesValidForEndNumber,
                                                Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> toIdentsWithValidFactorySigns,
                                                EtkProject etkProject, AbstractDataCard dataCard, String wmi,
                                                int datacardSerialNumber, iPartsModelId modelId, long filterDate,
                                                iPartsFactoryModel factoryModelInstance, String spikeIdent,
                                                iPartsFactoryModel.SerialNoAndFactory serialNoAndFactoryFromDatacard,
                                                String factorySignFromDatacard, boolean withFilterIdents, boolean isVehicle,
                                                boolean isWithFilterReason) {
        int modelYearCodeIndex = iPartsModelYearCode.findModelYearCode(dataForFactory.stCodeTo);
        // e. BIS-Einsatzdaten prüfen:
        // e.i. Es ist zu prüfen, ob zur PEM-BIS Idents vorliegen und die Seriennummer gültig ist
        if (withFilterIdents && dataForFactory.isResponseDataAvailableForPEMTo() && (toIdentsWithValidFactorySigns != null)
            && (datacardSerialNumber != FinId.INVALID_SERIAL_NUMBER)) {
            boolean validResponseSerialNumberFound = false;
            boolean matchingIdentFromFound = false;
            for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> identEntry : toIdentsWithValidFactorySigns.entrySet()) {
                String identTo = identEntry.getKey().ident;
                Set<String> identToSpikes = identEntry.getValue();

                // e.ii. Falls Ident-Bis vorliegen ist zu prüfen, ob der Ident der FIN < dem Ident in den Rückmeldedaten ist,
                // dabei ist der normalerweise der Millionenüberlauf zu beachten.
                SerialNoWithLength responseSerialNoWithLength = getResponseSerialNo(etkProject, dataCard, isVehicle, wmi,
                                                                                    modelId, identTo, factorySignFromDatacard,
                                                                                    factoryModelInstance, serialNoAndFactoryFromDatacard, true);
                // Bei ungültigem Millionenüberlauf die Position überspringen.
                if (responseSerialNoWithLength.isInvalidSerialNo()) {
                    continue;
                }
                validResponseSerialNumberFound = true;

                // e.ii.1. [JA] Falls der Ident der FIN < dem Ident der Rückmeldedaten ist, ...
                int responseSerialNumber = responseSerialNoWithLength.getSerialNo();
                if (datacardSerialNumber < responseSerialNumber) {
                    // e.ii.1. ... prüfen, ob der Ident als Ausreißer angegeben ist.
                    // e.ii.1.a. Falls [JA], wird die Teileposition NICHT ausgegeben, ist ja ein Ausreißer.
                    if ((identToSpikes != null) && identToSpikes.contains(spikeIdent)) {
                        if (toIdentsWithValidFactorySigns.size() == 1) {
                            setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                                  "!!PEM bis \"%1\": Vorläufer \"%2\" zum Ident bis \"%3\" (%4)",
                                                                  dataForFactory.pemTo, spikeIdent, identTo,
                                                                  String.valueOf(responseSerialNumber),
                                                                  spikeIdent, String.valueOf(datacardSerialNumber),
                                                                  getFilterReasonSourceName(false));
                            return false;
                        }
                        continue;
                    } else { // e.ii.1.b. Falls [NEIN], dann wird für die Teileposition der Modelljahrcode-Bis geprüft
                        matchingIdentFromFound = true;
                        break;
                    }
                } else { // e.ii.2. [NEIN] Falls der Ident der FIN [NICHT] < dem Ident der Rückmeldedaten ist, prüfen, ob der Ident als Ausreisser-Ident angebeben ist.
                    // e.ii.2.a. Falls [JA], dann wird für die Teileposition der Modelljahrcode-Bis geprüft
                    if ((identToSpikes != null) && identToSpikes.contains(spikeIdent)) {
                        matchingIdentFromFound = true;
                        break;
                    } else {
                        // e.ii.2.b. Falls [NEIN], wird die Teileposition nicht ausgegeben
                        if (toIdentsWithValidFactorySigns.size() == 1) {
                            setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                                  "!!PEM bis \"%1\": Ident bis \"%2\" (%3) <= \"%4\" (%5) %6",
                                                                  dataForFactory.pemTo, identTo,
                                                                  String.valueOf(responseSerialNumber),
                                                                  spikeIdent, String.valueOf(datacardSerialNumber),
                                                                  getFilterReasonSourceName(false));
                            return false;
                        }

                        continue;
                    }
                }
            }

            // DAIMLER-6218
            if (matchingIdentFromFound && !checkDIALOGModelYearCodeForPemToData(modelYearCodeIndex, dataCard, partListEntry,
                                                                                colortablePEMEvaluation, filter, etkProject,
                                                                                dataForFactory, filterDate)) {
                return false;
            }

            // Wenn es potenzielle Idents gibt, aber kein Ident gültig ist
            if (validResponseSerialNumberFound && !matchingIdentFromFound) {
                setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                      "!!PEM bis \"%1\": Alle relevanten Idents bis <= \"%2\" (%3) %4",
                                                      dataForFactory.pemTo, spikeIdent, String.valueOf(datacardSerialNumber),
                                                      getFilterReasonSourceName(false));
                return false;
            }
            setPartListEntryValidForEndNumberFilter(partListEntry, partListEntriesValidForEndNumber);
            return true;
        } else { // e.iii. Liegt kein Ident-Bis in den Werksdaten vor, dann ist zu prüfen, ob ein Filterdatum vorliegt
            // e.iii.1. Falls kein Filterdatum vorliegt, dann wird für die Teileposition der Modelljahrcode-Bis geprüft
            // e.iii.2. Falls ein Filterdatum vorliegt, wird geprüft, ob PEM-BIS-Termin >= Filter-Datum ist.
            if (isFactoryDateValidForFilterDate(filterDate, dataForFactory, true)) {
                // e.iii.2.a. falls der PEM-BIS-Termin >= dem Filterdatum ist: Modelljahrcode-Bis prüfen

                // e.iv. Liegen zu PEM-Bis keine Idents vor, dann ist zu prüfen, ob in den Werksdaten ein Modelljahrcode-Bis (PODW_STCB) vorliegt.
                // e.iv.1. Falls in den Werksdaten ein Modelljahrcode vorliegt, prüfen, ob der Modelljahrcode in der Code-Liste
                // der Datenkarte vorkommt.
                if (!checkDIALOGModelYearCodeForPemToData(modelYearCodeIndex, dataCard, partListEntry, colortablePEMEvaluation,
                                                          filter, etkProject, dataForFactory, filterDate)) {
                    // e.iv.1.a. Falls ja, dann wird die Teileposition nicht ausgegeben
                    return false;
                } else {
                    // e.iv.1.b. Alle Prüfungen sind OK -> die Teileposition wird ausgegeben.
                    setPartListEntryValidForEndNumberFilter(partListEntry, partListEntriesValidForEndNumber);
                    return true;
                }
            } else {
                // e.iii.2.b. Falls nicht, wird die Teileposition nicht ausgegeben.
                if (isWithFilterReason) {
                    String dateSource = "!!Schlussabnahmedatum";
                    if (!withFilterIdents) {
                        dateSource = "!!TTZ-Datum";
                    }
                    setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                          "!!PEM bis \"%1\": Keine Rückmeldedaten vorhanden sowie Termin bis \"%2\" < %5 \"%3\" %4",
                                                          dataForFactory.pemTo, iPartsFilterHelper.getFilterReasonDate(dataForFactory.dateTo, etkProject),
                                                          iPartsFilterHelper.getFilterReasonDate(filterDate, etkProject),
                                                          getFilterReasonSourceName(false), TranslationHandler.translate(dateSource));
                }
                return false;
            }
        } // Ende: liegen keine zur PEM-BIS keine Idents vor
    }

    private static boolean checkDIALOGPemFromData(iPartsDataPartListEntry partListEntry, iPartsColorTable.AbstractColortablePEMEvaluation colortablePEMEvaluation,
                                                  iPartsFilter filter, iPartsFactoryData.AbstractDataForFactory dataForFactory,
                                                  Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> fromIdentsWithValidFactorySigns,
                                                  EtkProject etkProject, AbstractDataCard dataCard, String wmi,
                                                  int datacardSerialNumber, iPartsModelId modelId,
                                                  long filterDate, iPartsFactoryModel factoryModelInstance, String spikeIdent,
                                                  iPartsFactoryModel.SerialNoAndFactory serialNoAndFactoryFromDatacard,
                                                  String factorySignFromDatacard, boolean withFilterIdents, boolean isVehicle,
                                                  boolean isWithFilterReason) {

        int modelYearCodeIndex = iPartsModelYearCode.findModelYearCode(dataForFactory.stCodeFrom);
        // d.1. Es ist zu prüfen, ob zur PEM-AB Idents vorliegen und die Seriennummer gültig ist
        if (withFilterIdents && dataForFactory.isResponseDataAvailableForPEMFrom() && (fromIdentsWithValidFactorySigns != null)
            && (datacardSerialNumber != FinId.INVALID_SERIAL_NUMBER)) {
            boolean validResponseSerialNumberFound = false;
            boolean matchingIdentFromFound = false;
            for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> identEntry : fromIdentsWithValidFactorySigns.entrySet()) {
                String identFrom = identEntry.getKey().ident;
                Set<String> identFromSpikes = identEntry.getValue();

                // d.ii. Falls Ident-Ab vorliegen ist zu prüfen, ob der Ident der FIN >= dem Ident in den Rückmeldedaten ist.
                // Dabei ist normalerweise der Millionenüberlauf zu beachten.

                SerialNoWithLength responseSerialNoWithLength = getResponseSerialNo(etkProject, dataCard, isVehicle, wmi,
                                                                                    modelId, identFrom, factorySignFromDatacard,
                                                                                    factoryModelInstance, serialNoAndFactoryFromDatacard, true);
                // Bei ungültigem Millionenüberlauf die Position überspringen.
                if (responseSerialNoWithLength.isInvalidSerialNo()) {
                    continue;
                }
                validResponseSerialNumberFound = true;

                // d.ii.1. [JA] Falls der Ident der FIN >= dem Ident der Rückmeldedaten ist, prüfen, ob der Ident
                // als Ausreißer angegeben ist.
                int responseSerialNumber = responseSerialNoWithLength.getSerialNo();
                if (datacardSerialNumber >= responseSerialNumber) {
                    // d.ii.1.a. Falls [JA], wird die Teileposition NICHT ausgegeben, ist ja ein Ausreißer.
                    if ((identFromSpikes != null) && identFromSpikes.contains(spikeIdent)) {
                        if (fromIdentsWithValidFactorySigns.size() == 1) {
                            setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                                  "!!PEM ab \"%1\": Nachzügler \"%2\" zum Ident ab \"%3\" (%4)",
                                                                  dataForFactory.pemFrom, spikeIdent, identFrom,
                                                                  String.valueOf(responseSerialNumber),
                                                                  spikeIdent, String.valueOf(datacardSerialNumber),
                                                                  getFilterReasonSourceName(false));
                            return false;
                        }
                        continue;
                    } else { // d.ii.1.b. Falls [NEIN], dann wird für die Teileposition der Modelljahrcode-Ab geprüft
                        // nichts machen, da BIS-Einsatzdaten noch geprüft werden müssen
                        matchingIdentFromFound = true;
                        break;
                    }
                } else { // d.ii.2. [NEIN] Falls die Endnummer aus der Datenkarte ist zu klein ist, prüfen, ob der Ident als Ausreißer angegeben ist.
                    // d.ii.2.a. Falls [JA], dann wird für die Teileposition der Modelljahrcode-Ab geprüft
                    if ((identFromSpikes != null) && identFromSpikes.contains(spikeIdent)) {
                        // nichts machen, da BIS-Einsatzdaten noch geprüft werden müssen
                        matchingIdentFromFound = true;
                        break;
                    } else { // d.ii.2.b. Falls [NEIN], dann wird die Teileposition nicht ausgegeben
                        if (fromIdentsWithValidFactorySigns.size() == 1) {
                            setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                                  "!!PEM ab \"%1\": Ident ab \"%2\" (%3) > \"%4\" (%5) %6",
                                                                  dataForFactory.pemFrom, identFrom,
                                                                  String.valueOf(responseSerialNumber),
                                                                  spikeIdent, String.valueOf(datacardSerialNumber),
                                                                  getFilterReasonSourceName(false));
                            return false;
                        }

                        continue;
                    }
                }
            }

            // DAIMLER-6218
            if (matchingIdentFromFound && !checkDIALOGModelYearCodeForPemFromData(modelYearCodeIndex, dataCard, partListEntry,
                                                                                  colortablePEMEvaluation, filter, etkProject,
                                                                                  dataForFactory, filterDate)) {
                return false;
            }

            // Wenn es potenzielle Idents gibt, aber kein Ident gültig ist
            if (validResponseSerialNumberFound && !matchingIdentFromFound) {
                setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                      "!!PEM ab \"%1\": Alle relevanten Idents ab > \"%2\" (%3) %4",
                                                      dataForFactory.pemFrom, spikeIdent, String.valueOf(datacardSerialNumber),
                                                      getFilterReasonSourceName(false));
                return false;
            }
        } else { // d.iii. Liegt kein Ident-AB in den Werksdaten vor, dann ist zu prüfen, ob ein Filterdatum vorliegt.
            // d.iii.1. Falls kein Filterdatum vorliegt, dann wird für die Teileposition der Modelljahrcode-Ab geprüft
            // d.iii.2. Falls ein Filterdatum vorliegt, wird geprüft, ob PEM-AB-Termin < Filter-Datum ist.
            if (isFactoryDateValidForFilterDate(filterDate, dataForFactory, false)) {
                // d.iii.2.a. falls der PEM-AB-Termin < dem Filterdatum ist: Modelljahrcode-Ab prüfen

                // d.iv. Liegen zu PEM-AB keine Idents vor, dann ist zu prüfen, ob in den Werksdaten ein Modelljahrcode-Ab (PODW_STCA) vorliegt
                // d.iv.1. Falls in den Werksdaten ein Modelljahrcode vorliegt, prüfen, ob der vorherige Modelljahrcode
                // in der Code-Liste der Datenkarte vorkommt.
                if (!checkDIALOGModelYearCodeForPemFromData(modelYearCodeIndex, dataCard, partListEntry, colortablePEMEvaluation,
                                                            filter, etkProject, dataForFactory, filterDate)) {
                    // d.iv.1.a. Falls ja, dann wird Teileposition nicht ausgegeben
                    return false;
                } else {
                    // d.iv.1.b. Nichts machen, da BIS-Einsatzdaten noch geprüft werden müssen
                }
            } else {
                // d.iii.2.b. PEM-AB-Termin >= Filterdatum -> Stücklisteneintrag wird ausgefiltert
                if (isWithFilterReason) {
                    String dateSource = "!!Schlussabnahmedatum";
                    if (!withFilterIdents) {
                        dateSource = "!!TTZ-Datum";
                    }
                    setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                          "!!PEM ab \"%1\": Keine Rückmeldedaten vorhanden sowie Termin ab \"%2\" >= %5 \"%3\" %4",
                                                          dataForFactory.pemFrom, iPartsFilterHelper.getFilterReasonDate(dataForFactory.dateFrom, etkProject),
                                                          iPartsFilterHelper.getFilterReasonDate(filterDate, etkProject),
                                                          getFilterReasonSourceName(false), TranslationHandler.translate(dateSource));
                }
                return false;
            }
        }
        return true;
    }

    private static boolean checkDIALOGModelYearCodeForPemFromData(int modelYearCodeIndex, AbstractDataCard dataCard, iPartsDataPartListEntry partListEntry,
                                                                  iPartsColorTable.AbstractColortablePEMEvaluation colortablePEMEvaluation,
                                                                  iPartsFilter filter, EtkProject etkProject, iPartsFactoryData.AbstractDataForFactory dataForFactory,
                                                                  long filterDate) {
        // Soll Modelljahrcode überhaupt geprüft werden?
        if (shouldModelYearCodeBeChecked(filterDate, dataForFactory.dateFrom)) {
            if (modelYearCodeIndex >= 0) {
                // Holt den 1 kleineren Vorgänger-Code aus der Liste der Modelljahr-Codes
                String[] searchModelYearCodes = iPartsModelYearCode.getPredecessors(modelYearCodeIndex, 1, false);

                // Sucht nach einem passenden Code in den Fahrzeugdatenkarten-Codes -> bei Treffer ausfiltern
                if (iPartsModelYearCode.dataCardContainsOneModelYearCode(dataCard, searchModelYearCodes)) {
                    setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                          "!!PEM ab \"%1\": Vorheriger Modelljahr-Code \"%2\" ist auf der Datenkarte enthalten bei \"%3\" als \"Modelljahr-Code ab\" von den Werkseinsatzdaten",
                                                          dataForFactory.pemFrom, searchModelYearCodes[0],
                                                          dataForFactory.stCodeFrom);
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkDIALOGModelYearCodeForPemToData(int modelYearCodeIndex, AbstractDataCard dataCard, iPartsDataPartListEntry partListEntry,
                                                                iPartsColorTable.AbstractColortablePEMEvaluation colortablePEMEvaluation,
                                                                iPartsFilter filter, EtkProject etkProject, iPartsFactoryData.AbstractDataForFactory dataForFactory,
                                                                long filterDate) {
        // Soll Modelljahrcode überhaupt geprüft werden?
        if (shouldModelYearCodeBeChecked(filterDate, dataForFactory.dateTo)) {
            if (modelYearCodeIndex >= 0) {
                // Sucht nach einem passenden Code in den Fahrzeugdatenkarten-Codes -> bei Treffer ausfiltern
                if (iPartsModelYearCode.dataCardContainsOneModelYearCode(dataCard, dataForFactory.stCodeTo)) {
                    setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                          "!!PEM bis \"%1\": Modelljahr-Code \"%2\" ist auf der Datenkarte enthalten und identisch zum \"Modelljahr-Code bis\" von den Werkseinsatzdaten",
                                                          dataForFactory.pemTo, dataForFactory.stCodeTo);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * DAIMLER-7803 Modelljahrcode prüfen, wenn...
     *
     * @param filterDate  Filter-Datum von der Datenkarte
     * @param factoryDate Datum ab bzw. bis vom Werkseinsatzdaten-Datensatz
     * @return
     */
    private static boolean shouldModelYearCodeBeChecked(long filterDate, long factoryDate) {
        // Wenn Filter-Datum leer oder ungültig ist -> Modelljahrcode prüfen
        if (filterDate <= 0) {
            return true;
        }

        // Wenn Einsatzdatum unendlich oder ungültig ist -> Modelljahrcode prüfen
        if (factoryDate <= 0) {
            return true;
        }

        // Wenn Einsatzdatum im Zeitraum +/- 2 Jahre zum Filterdatum ist -> Modelljahrcode prüfen
        long filterDateYear = filterDate / 10000000000L; // yyyyMMddHHmmss / 10000000000 = yyyy
        long factoryDateYear = factoryDate / 10000000000L; // yyyyMMddHHmmss / 10000000000 = yyyy
        long diff = Math.abs(filterDateYear - factoryDateYear);
        return diff <= 2;
    }

    /**
     * Prüfung, ob die Konstellation von WKB-Gruppierung und Idents gültig ist (DAIMLER-6966).
     *
     * @param factorySignGrouping
     * @param partListEntry
     * @param colortablePEMEvaluation
     * @param dataForFactory
     * @param fromIdentsWithValidFactorySigns
     * @param toIdentsWithValidFactorySigns
     * @param wmi
     * @param factorySignFromDatacard
     * @param modelId
     * @param serialNoAndFactoryFromDatacard
     * @param filter
     * @param etkProject
     * @return
     */
    private static boolean isValidFactorySignData(Set<String> factorySignGrouping, iPartsDataPartListEntry partListEntry,
                                                  iPartsColorTable.AbstractColortablePEMEvaluation colortablePEMEvaluation, iPartsFactoryData.AbstractDataForFactory dataForFactory,
                                                  Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> fromIdentsWithValidFactorySigns,
                                                  Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> toIdentsWithValidFactorySigns,
                                                  String wmi, String factorySignFromDatacard, iPartsModelId modelId,
                                                  iPartsFactoryModel.SerialNoAndFactory serialNoAndFactoryFromDatacard,
                                                  iPartsFilter filter, EtkProject etkProject) {
        if (factorySignGrouping != null) {
            boolean isOnlyPemFromRelevant;
            boolean isOnlyPemToRelevant;
            boolean bothPemRelevant;
            boolean isForColortableContent = false;
            if (partListEntry != null) {
                isOnlyPemFromRelevant = partListEntry.isPEMFromRelevant() && !partListEntry.isPEMToRelevant();
                isOnlyPemToRelevant = !partListEntry.isPEMFromRelevant() && partListEntry.isPEMToRelevant();
                bothPemRelevant = partListEntry.isPEMFromRelevant() && partListEntry.isPEMToRelevant();
            } else if (colortablePEMEvaluation != null) {
                isOnlyPemFromRelevant = colortablePEMEvaluation.isEvalPemFrom() && !colortablePEMEvaluation.isEvalPemTo();
                isOnlyPemToRelevant = !colortablePEMEvaluation.isEvalPemFrom() && colortablePEMEvaluation.isEvalPemTo();
                bothPemRelevant = colortablePEMEvaluation.isEvalPemFrom() && colortablePEMEvaluation.isEvalPemTo();
                isForColortableContent = true;
            } else {
                // Kann eigentlich nicht passieren
                return false;
            }

            boolean noValidFromData = (fromIdentsWithValidFactorySigns == null) && (dataForFactory.identsFrom != null);
            boolean noValidToData = (toIdentsWithValidFactorySigns == null) && (dataForFactory.identsTo != null);

            // Falls nur PEM-AB-auswerten gesetzt ist und Idents zum Werk vorhanden sind, dann muss mindestens ein Ident
            // mit den ermittelten Buchstaben vorhanden sein. Falls nicht, die Teileposition ausfiltern
            boolean isInvalidPemFromData = isOnlyPemFromRelevant && noValidFromData;

            // Falls nur PEM-BIS-auswerten gesetzt ist und Idents zum Werk vorhanden sind, dann muss mindestens ein
            // Ident mit den ermittelten Buchstaben vorhanden sein. Falls nicht, die Teileposition ausfiltern.
            boolean isInvalidPemToData = isOnlyPemToRelevant && noValidToData;

            // Falls PEM-AB- und PEM-BIS-auswerten gesetzt sind und Idents zum Werk bei AB oder bei BIS vorhanden sind,
            // dann muss mindestens am AB oder am BIS ein Ident mit den ermittelten Buchstaben vorhanden sein. Falls
            // nicht, die Teileposition ausfiltern.
            boolean isInvalidPemFromAndToData = bothPemRelevant && noValidFromData && noValidToData;

            // Bei Farben gibt es keinen Filtergrund -> gleich rausspringen
            if (isForColortableContent && (isInvalidPemFromData || isInvalidPemToData || isInvalidPemFromAndToData)) {
                return false;
            }

            if (isInvalidPemFromData) {
                setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                      "!!PEM ab \"%1\": keine gültigen Idents zur WKB-Gruppierung von \"%2\", \"%3\", \"%4\" und \"%5\"",
                                                      dataForFactory.pemFrom, wmi, factorySignFromDatacard, modelId.getModelTypeNumber(),
                                                      serialNoAndFactoryFromDatacard.getFactoryNumber());
                return false;
            } else if (isInvalidPemToData) {
                setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                      "!!PEM bis \"%1\": keine gültigen Idents zur WKB-Gruppierung von \"%2\", \"%3\", \"%4\" und \"%5\"",
                                                      dataForFactory.pemTo, wmi, factorySignFromDatacard, modelId.getModelTypeNumber(),
                                                      serialNoAndFactoryFromDatacard.getFactoryNumber());
                return false;
            } else if (isInvalidPemFromAndToData) {
                setEndNumberFilterReasonForDataObject(partListEntry, colortablePEMEvaluation, filter, etkProject,
                                                      "!!PEM ab \"%1\", PEM bis \"%2\": keine gültigen Idents zur WKB-Gruppierung von \"%3\", \"%4\", \"%5\" und \"%6\"",
                                                      dataForFactory.pemFrom, dataForFactory.pemTo, wmi, factorySignFromDatacard,
                                                      modelId.getModelTypeNumber(), serialNoAndFactoryFromDatacard.getFactoryNumber());
                return false;
            }
        }
        return true;
    }

    private static Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> filterIdentsByFactoryGrouping(iPartsFactoryData.AbstractDataForFactory dataForFactory,
                                                                                                          Set<String> factorySignGrouping,
                                                                                                          String identFactorySignFilterValue,
                                                                                                          boolean fromIdents) {
        boolean isWithIdentFactorySignFilter = StrUtils.isValid(identFactorySignFilterValue);
        Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> identsMap = fromIdents ? dataForFactory.identsFrom : dataForFactory.identsTo;
        if (factorySignGrouping == null) {
            if ((identsMap == null) || identsMap.isEmpty()) {
                return null;
            } else if (!isWithIdentFactorySignFilter) { // Keine Bandsteuerung und auch keine Filterung nach Montagewerk
                return identsMap;
            }
        }

        Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> result = null;
        if (identsMap != null) {
            for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> identEntry : identsMap.entrySet()) {
                String factory = StrUtils.cutIfLongerThan(identEntry.getKey().ident, 1);

                // Montagewerk muss passen
                if (isWithIdentFactorySignFilter && !Utils.objectEquals(factory, identFactorySignFilterValue)) {
                    continue;
                }

                // Bandsteuerung muss passen
                if ((factorySignGrouping == null) || factorySignGrouping.contains(factory)) {
                    if (result == null) {
                        result = new HashMap<>();
                    }
                    result.put(identEntry.getKey(), identEntry.getValue());
                }
            }
        }
        return result;
    }

    private static boolean isFactoryDateValidForFilterDate(long filterDate, iPartsFactoryData.AbstractDataForFactory dataForFactory,
                                                           boolean isDateTo) {
        if (filterDate <= 0) {
            return true;
        }
        if (isDateTo) { // Vergleiche für Datum BIS
            // 5.1.4.2.1 Falls der PEM-BIS-Termin > dem Filterdatum ist, wird die Teileposition ausgegeben.
            long realDateTo = dataForFactory.getDateToWithInfinity();
            return realDateTo >= filterDate;
        } else { // Vergleiche für Datum AB
            // 4.4.2.1 falls der PEM-AB-Termin < dem Filterdatum ist
            return dataForFactory.dateFrom < filterDate;
        }
    }

    /**
     * Ermittelt die Seriennummer mit Millionenüberlauf für Fahrzeuge und Aggregate
     *
     * @param etkProject
     * @param dataCard
     * @param isVehicle
     * @param wmi
     * @param modelId
     * @param responseIdent
     * @param factorySignFromDatacard
     * @param factoryModelInstance
     * @param serialNoAndFactoryFromDatacard // aus WMI-Abfrage ermittelt
     * @return {@link FinId#INVALID_SERIAL_NUMBER} wenn Seriennummer nicht ermittelt werden kann oder Werke für FIN/Aggregat
     * und Rückmelde-Ident nicht passen
     */
    private static int getResponseSerialNoWithMillionOverflow(EtkProject etkProject, AbstractDataCard dataCard, boolean isVehicle, String wmi,
                                                              iPartsModelId modelId, String responseIdent, String factorySignFromDatacard,
                                                              iPartsFactoryModel factoryModelInstance,
                                                              iPartsFactoryModel.SerialNoAndFactory serialNoAndFactoryFromDatacard) {
        if (isVehicle) {
            String steeringNumberFromFIN = "";
            if (dataCard instanceof VehicleDataCard) {
                VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                // Lenkung ist wichtig für die CKD Unterscheidung für das Werk "0501"
                // Hier nur echte echte Links- oder Rechtslenkungswerte weitergeben
                if (vehicleDataCard.getFinId().isSteeringValid(true)) {
                    steeringNumberFromFIN = vehicleDataCard.getFinId().getSteering();
                }
            }
            return getResponseSerialNumberWithMillionForVehicle(wmi, modelId, responseIdent, factorySignFromDatacard, factoryModelInstance,
                                                                dataCard.getEndNumberLength(etkProject), serialNoAndFactoryFromDatacard, steeringNumberFromFIN);
        } else {
            String aggregateType = ((AggregateDataCard)dataCard).getAggregateTypeFromModel(etkProject);
            return getResponseSerialNumberWithMillionForAggregate(aggregateType, modelId, responseIdent, factorySignFromDatacard,
                                                                  factoryModelInstance, dataCard.getEndNumberLength(etkProject),
                                                                  serialNoAndFactoryFromDatacard);
        }
    }

    /**
     * Ermittelt die Seriennummer (i.d.R. mit Millionenüberlauf) für Fahrzeuge und Aggregate
     * <p>
     * Alte Motoridents: 6-stellig
     * Neue Motoridents: 7-stellig
     *
     * @param etkProject
     * @param dataCard
     * @param isVehicle
     * @param wmi
     * @param modelId
     * @param responseIdent
     * @param factorySignFromDatacard
     * @param factoryModelInstance
     * @param serialNoAndFactoryFromDatacard // aus WMI-Abfrage ermittelt
     * @return {@link FinId#INVALID_SERIAL_NUMBER} wenn Seriennummer nicht ermittelt werden kann oder Werke für FIN/Aggregat
     * und Rückmelde-Ident nicht passen
     */
    private static SerialNoWithLength getResponseSerialNo(EtkProject etkProject, AbstractDataCard dataCard, boolean isVehicle, String wmi,
                                                          iPartsModelId modelId, String responseIdent, String factorySignFromDatacard,
                                                          iPartsFactoryModel factoryModelInstance,
                                                          iPartsFactoryModel.SerialNoAndFactory serialNoAndFactoryFromDatacard,
                                                          boolean isColorTableFilter) {

        if (!isColorTableFilter && !isVehicle && StrUtils.isValid(responseIdent) && (responseIdent.length() > 2)) {
            AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCard;
            if (aggregateDataCard.getAggregateType() == DCAggregateTypes.ENGINE) {
                boolean checkEndnumberOnly = true; // keine Prüfung der Werksnummer -> kein Millionenüberlauf

                // Ist bei alten Motoridents 6-stellig, bei neuen 7-stellig.
                int endNumberLength = dataCard.getEndNumberLength(etkProject);

                // gilt sowohl für alte als auch neue Motoridents
                if ((responseIdent.length() != 8) && (responseIdent.length() != 6)) {
                    // alter Ident der weder 8 noch 6 stellig ist
                    // -> keine Endnummernfilterung möglich -> gültig
                    return SerialNoWithLength.createInvalidSerialNo();
                } else if ((responseIdent.length() == 8) && !Character.isDigit(responseIdent.charAt(1))) {
                    // alter Ident, 8-stellig mit 2.Stelle keine Zahl
                    // -> keine Endnummernfilterung möglich -> gültig
                    return SerialNoWithLength.createInvalidSerialNo();
                }

                // Spezialfälle für neue Motor Idents (DK: immer 7-stellig)
                if (aggregateDataCard.isNewIdentSecification()) {
                    if ((responseIdent.length() == 8) && (responseIdent.charAt(0) != '*')) {
                        // neuer Ident, 8-stellig ohne führendes *
                        // -> Filterung wie bisher (7 stellige Endnummer mit Werkskennbuchstabe und Millionenüberlauf)
                        checkEndnumberOnly = false;
                    } else if (responseIdent.length() == 6) {
                        // neuer Ident, 6-stellig
                        // -> Werkskennbuchstabe wird nicht geprüft; Vergleiche 6 Stellen des Ident mit 6 Stellen des Motorident (DK)
                        endNumberLength = 6;
                    }
                }
                // alte Motor Idents (Endnummer ist immer 6-stellig)
                // -> Vergleiche letzte 6 Stellen aus Ident (Rückmeldedaten) mit letzte 6 Stellen des Motoridents

                if (checkEndnumberOnly) {
                    // In einigen Fällen müssen für Motor-Idents nur die letzten x Stellen der Endnummer berücksichtigt werden und kein Millionenüberlauf
                    // berechnet werden; dafür muss vorher responseIdent evtl. schon gekürzt werden, damit der Ident korrekt
                    // erzeugt werden kann
                    if (responseIdent.length() > endNumberLength) {
                        responseIdent = responseIdent.substring(responseIdent.length() - endNumberLength);
                    }
                    iPartsFactoryData.Ident ident = createResponseIdent(responseIdent, "", endNumberLength); // Werkskennbuchstabe wird hier nicht verwendet
                    return new SerialNoWithLength(ident.endNumber, endNumberLength);
                }
            }
        }
        return new SerialNoWithLength(getResponseSerialNoWithMillionOverflow(etkProject, dataCard, isVehicle, wmi, modelId, responseIdent, factorySignFromDatacard,
                                                                             factoryModelInstance, serialNoAndFactoryFromDatacard));
    }

    /**
     * Handelt es sich bei der übergebenen Datenkarte um eine Motor-Datenkarte mit alter Ident-Systematik?
     *
     * @param dataCard
     * @param isVehicle
     * @return
     */
    private static boolean isOldEngineIdentDataCard(AbstractDataCard dataCard, boolean isVehicle) {
        if (!isVehicle) {
            AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCard;
            return (aggregateDataCard.getAggregateType() == DCAggregateTypes.ENGINE) && aggregateDataCard.isOldIdentSpecification();
        }

        return false;
    }

    /**
     * Seriennummer mit Millionenüberlauf aus WMI Tabelle ermitteln für Fahrzeug-Rückmelde-Ident
     *
     * @param wmi
     * @param modelId
     * @param responseIdent
     * @param factorySignFromDatacard
     * @param factoryModelInstance
     * @param endNumberLength
     * @param serialNoAndFactory
     * @return INVALID_SERIAL_NUMBER wenn Seriennummer nicht ermittelt werden kann oder Werke für FIN/Aggregat und Rückmelde-Ident nicht passen
     */
    private static int getResponseSerialNumberWithMillionForVehicle(String wmi, iPartsModelId modelId, String responseIdent, String factorySignFromDatacard, iPartsFactoryModel factoryModelInstance,
                                                                    int endNumberLength, iPartsFactoryModel.SerialNoAndFactory serialNoAndFactory, String steeringNumberFromFIN) {
        iPartsFactoryModel.SerialNoAndFactory responseSerialNoAndFactory = getResponseSerialNumberAndFactoryForWMI(wmi, modelId, responseIdent,
                                                                                                                   factorySignFromDatacard, factoryModelInstance,
                                                                                                                   endNumberLength, steeringNumberFromFIN);

        if (!responseSerialNoAndFactory.compareFactory(serialNoAndFactory, true)) {
            return FinId.INVALID_SERIAL_NUMBER;
        }

        return responseSerialNoAndFactory.getSerialNumber();
    }

    /**
     * Seriennummer mit Millionenüberlauf aus WMI Tabelle ermitteln für Aggregate-Rückmelde-Ident
     *
     * @param aggregateType
     * @param modelId
     * @param responseIdent
     * @param factorySignFromDatacard
     * @param factoryModelInstance
     * @param endNumberLength
     * @param serialNoAndFactoryFromDatacard
     * @return INVALID_SERIAL_NUMBER wenn Seriennummer nicht ermittelt werden kann oder Werke für FIN/Aggregat und Rückmelde-Ident nicht passen
     */
    private static int getResponseSerialNumberWithMillionForAggregate(String aggregateType, iPartsModelId modelId, String responseIdent, String factorySignFromDatacard,
                                                                      iPartsFactoryModel factoryModelInstance, int endNumberLength,
                                                                      iPartsFactoryModel.SerialNoAndFactory serialNoAndFactoryFromDatacard) {
        iPartsFactoryModel.SerialNoAndFactory responseSerialNoAndFactory = getResponseSerialNumberAndFactoryForAggregate(aggregateType, modelId,
                                                                                                                         responseIdent,
                                                                                                                         factorySignFromDatacard, factoryModelInstance,
                                                                                                                         endNumberLength);

        if (!responseSerialNoAndFactory.compareFactory(serialNoAndFactoryFromDatacard, true)) {
            return FinId.INVALID_SERIAL_NUMBER;
        }

        return responseSerialNoAndFactory.getSerialNumber();
    }

    /**
     * Endnummer mit Millionenüberlauf und Werk für Response-Ident ermitteln
     *
     * @param wmi                     WMI bzw. WHC aus der Datenkarte
     * @param modelId                 aus der Datenkarte
     * @param responseIdent
     * @param factorySignFromDatacard wird nur ausgewertet wenn der Response-Ident selbst kein Werkskennzeichen besitzt
     * @param factoryModelInstance    Cache für DA_FACTORY_MODEL
     * @return
     */
    private static iPartsFactoryModel.SerialNoAndFactory getResponseSerialNumberAndFactoryForWMI(String wmi, iPartsModelId modelId,
                                                                                                 String responseIdent, String factorySignFromDatacard,
                                                                                                 iPartsFactoryModel factoryModelInstance,
                                                                                                 int endNumberLength, String steeringNumberFromFIN) {
        iPartsFactoryData.Ident ident = createResponseIdent(responseIdent, factorySignFromDatacard, endNumberLength);

        // Millionenüberlauf berechnen
        return factoryModelInstance.getSerialNumberWithOverflowAndFactoryForWMI(wmi, ident.factorySign, modelId, ident.endNumber,
                                                                                endNumberLength, steeringNumberFromFIN);
    }

    private static iPartsFactoryModel.SerialNoAndFactory getResponseSerialNumberAndFactoryForAggregate(String aggregateType, iPartsModelId modelId,
                                                                                                       String responseIdent, String factorySignFromAggregate,
                                                                                                       iPartsFactoryModel factoryModelInstance,
                                                                                                       int endNumberLength) {
        iPartsFactoryData.Ident ident = createResponseIdent(responseIdent, factorySignFromAggregate, endNumberLength);

        // Millionenüberlauf berechnen
        return factoryModelInstance.getSerialNumberWithOverflowAndFactoryForAggregate(ident.factorySign, modelId, aggregateType,
                                                                                      ident.endNumber, endNumberLength);
    }

    private static iPartsFactoryData.Ident createResponseIdent(String responseIdent, String factorySign, int endNumberLength) {
        iPartsFactoryData.Ident ident = new iPartsFactoryData.Ident(responseIdent, endNumberLength);

        // Werkskennbuchstabe von der FIN übernehmen falls nicht vorhanden für korrekte Millionenüberlaufsberechnung
        if (StrUtils.isEmpty(ident.factorySign)) {
            ident.factorySign = factorySign;
        }
        return ident;
    }

    private static void setFilterReasonForDataObject(iPartsDataPartListEntry partListEntry, iPartsColorTable.AbstractColortablePEMEvaluation colorTableObject,
                                                     iPartsFilter filter, EtkProject project, iPartsFilterSwitchboard.FilterTypes partListEntryFilterType,
                                                     iPartsColorFilter.ColorTableToPartFilterTypes colorTableToPartFilterType,
                                                     iPartsColorFilter.ColorTableContentFilterTypes colorTableContentFilterType,
                                                     String filterReasonTranslationKey, String... filterReasonPlaceholders) {
        if (partListEntry != null) {
            setFilterReasonForPartListEntry(partListEntry, filter, partListEntryFilterType, filterReasonTranslationKey,
                                            filterReasonPlaceholders);
        } else if (colorTableObject != null) {
            setFilterReasonForColorTable(colorTableObject, filter, project, colorTableToPartFilterType, colorTableContentFilterType,
                                         filterReasonTranslationKey, filterReasonPlaceholders);
        }
    }

    private static void setFilterReasonForPartListEntry(EtkDataPartListEntry partListEntry, iPartsFilter filter,
                                                        iPartsFilterSwitchboard.FilterTypes partListEntryFilterType,
                                                        String filterReasonTranslationKey, String... filterReasonPlaceholders) {
        if ((filter != null) && filter.isWithFilterReason() && (filterReasonTranslationKey != null)) {
            filter.setFilterReasonForPartListEntry(partListEntry, partListEntryFilterType, filterReasonTranslationKey,
                                                   filterReasonPlaceholders);
        }
    }

    private static void setFilterReasonForColorTable(iPartsColorTable.AbstractColortablePEMEvaluation colorTableObject,
                                                     iPartsFilter filter, EtkProject etkProject, iPartsColorFilter.ColorTableToPartFilterTypes colorTableToPartFilterType,
                                                     iPartsColorFilter.ColorTableContentFilterTypes colorTableContentFilterType,
                                                     String filterReasonTranslationKey, String... filterReasonPlaceholders) {
        if ((filter != null) && filter.isWithFilterReason() && (filterReasonTranslationKey != null)) {
            if (colorTableObject instanceof iPartsColorTable.ColorTableToPart) {
                filter.setFilterReasonForColorTableToPart(((iPartsColorTable.ColorTableToPart)colorTableObject).getDataColorTableToPart(false, etkProject),
                                                          colorTableToPartFilterType, filterReasonTranslationKey, filterReasonPlaceholders);
            } else {
                filter.setFilterReasonForColorTableContent(((iPartsColorTable.ColorTableContent)colorTableObject).getDataColorTableContent(false, etkProject),
                                                           colorTableContentFilterType, filterReasonTranslationKey, filterReasonPlaceholders);
            }
        }
    }

    private static void setEndNumberFilterReasonForDataObject(iPartsDataPartListEntry partListEntry, iPartsColorTable.AbstractColortablePEMEvaluation colorTableObject,
                                                              iPartsFilter filter, EtkProject project, String filterReasonTranslationKey,
                                                              String... filterReasonPlaceholders) {
        setFilterReasonForDataObject(partListEntry, colorTableObject, filter, project, iPartsFilterSwitchboard.FilterTypes.END_NUMBER,
                                     iPartsColorFilter.ColorTableToPartFilterTypes.END_NUMBER, iPartsColorFilter.ColorTableContentFilterTypes.END_NUMBER,
                                     filterReasonTranslationKey, filterReasonPlaceholders);
    }

    /**
     * Stücklisteintrag in partListEntriesValidForEndNumber als gültig kennzeichnen.
     *
     * @param partListEntry                    akt. Stücklisteneintrag
     * @param partListEntriesValidForEndNumber filterrelevante PVs des akt. Stücklisteneintrags; null wenn keine Kennzeichnung erfolgen soll
     */
    private static void setPartListEntryValidForEndNumberFilter(iPartsDataPartListEntry partListEntry, Map<String, Boolean> partListEntriesValidForEndNumber) {
        if ((partListEntry == null) || (partListEntriesValidForEndNumber == null)) { // Aufruf von basicCheckEndNumberFilter() ohne Kennzeichnung von gültigen Stücklisteneinträgen
            return;
        }

        // Stücklisteneintrag selbst als gültig markieren
        partListEntriesValidForEndNumber.put(partListEntry.getAsId().getKLfdnr(), true);
    }

    /**
     * PEM Termin ab/bis der übergebenen Werkseinsatzdaten auf -/+ unendlich setzen, wenn die dazugehörigen Flags
     * "PEM ab/bis auswerten" nicht gesetzt sind sowie Filterung von Baumuster-bezogenen Idents.
     *
     * @param factoryData
     * @param models      Optionale Baumuster-Nummern für die Filterung von Baumuster-bezogenen Idents; bei {@code null}
     *                    findet keine Baumuster-bezogene Filterung statt
     * @param steering    Optionale Lenkung für die Filterung von Lenkungs-bezogenen Idents; bei {@code null} findet keine
     *                    Lenkungs-bezogene Filterung statt
     * @return
     */
    static iPartsFactoryData modifyAndFilterAllFactoryData(iPartsFactoryData factoryData, Collection<String> models, String steering) {
        // Wenn es keine Werkseinsatzdaten gibt oder die beiden Flags "PEM ab/bis auswerten" gesetzt sind sowie keine Baumuster
        // und keine Lenkung übergeben wurden, müssen die Werkseinsatzdaten nicht manipuliert werden
        if ((factoryData == null) || (factoryData.getFactoryDataMap() == null) || (factoryData.isEvalPemFrom() && factoryData.isEvalPemTo()
                                                                                   && (models == null) && (steering == null))) {
            return factoryData;
        }


        iPartsFactoryData modifiedFactoryData = new iPartsFactoryData();
        modifiedFactoryData.setEvalPemFrom(factoryData.isEvalPemFrom());
        modifiedFactoryData.setEvalPemTo(factoryData.isEvalPemTo());

        for (Map.Entry<String, List<iPartsFactoryData.DataForFactory>> factoryDataEntry : factoryData.getFactoryDataMap().entrySet()) {
            List<iPartsFactoryData.DataForFactory> factoryDataList = new DwList<>(factoryDataEntry.getValue().size());
            for (iPartsFactoryData.DataForFactory dataForFactory : factoryDataEntry.getValue()) {
                iPartsFactoryData.DataForFactory oldDataForFactory = dataForFactory;
                dataForFactory = modifyFactoryDataAccordingToPEMRelevantFlags(dataForFactory, modifiedFactoryData.isEvalPemFrom(),
                                                                              modifiedFactoryData.isEvalPemTo(), false);
                if ((models != null) || (steering != null)) {
                    dataForFactory = filterIdentsByModelsAndSteering(dataForFactory, models, steering, oldDataForFactory != dataForFactory);
                }
                factoryDataList.add(dataForFactory);
            }

            if (!factoryDataList.isEmpty()) {
                modifiedFactoryData.setDataForFactory(factoryDataEntry.getKey(), factoryDataList);
            }
        }

        return modifiedFactoryData;
    }

    /**
     * PEM Termin ab/bis vom übergebenen Werkseinsatzdaten-Datensatz auf -/+ unendlich setzen, wenn die dazugehörigen Flags
     * "PEM ab/bis auswerten" nicht gesetzt sind.
     *
     * @param dataForFactory
     * @param evalPemFrom
     * @param evalPemTo
     * @param alreadyCloned  Flag, ob der übergebene Werkseinsatzdaten-Datensatz bereits geklont wurde und deswegen nicht
     *                       erneut geklont werden muss
     * @return
     */
    private static iPartsFactoryData.DataForFactory modifyFactoryDataAccordingToPEMRelevantFlags(iPartsFactoryData.DataForFactory dataForFactory,
                                                                                                 boolean evalPemFrom, boolean evalPemTo,
                                                                                                 boolean alreadyCloned) {
        // DAIMLER-6672 Flags "PEM ab/bis auswerten" berücksichtigen
        if (!evalPemFrom) {
            if (dataForFactory.dateFrom != 0) {
                iPartsFactoryData.DataForFactory modifiedDataForFactory;
                if (alreadyCloned) {
                    modifiedDataForFactory = dataForFactory;
                } else {
                    modifiedDataForFactory = new iPartsFactoryData.DataForFactory();
                    modifiedDataForFactory.assign(dataForFactory);
                    alreadyCloned = true;
                }

                // PEM ab komplett entfernen
                modifiedDataForFactory.addFilterInfo(TranslationHandler.translate("!!\"PEM ab\" wird nicht ausgewertet"));
                modifiedDataForFactory.pemFrom = "";
                modifiedDataForFactory.stCodeFrom = "";
                modifiedDataForFactory.dateFrom = 0;
                modifiedDataForFactory.identsFrom = null;
                dataForFactory = modifiedDataForFactory;
            }
        }

        if (!evalPemTo) {
            if (dataForFactory.dateTo != 0) {
                iPartsFactoryData.DataForFactory modifiedDataForFactory;
                if (alreadyCloned) {
                    modifiedDataForFactory = dataForFactory;
                } else {
                    modifiedDataForFactory = new iPartsFactoryData.DataForFactory();
                    modifiedDataForFactory.assign(dataForFactory);
                }

                // PEM bis komplett entfernen
                modifiedDataForFactory.addFilterInfo(TranslationHandler.translate("!!\"PEM bis\" wird nicht ausgewertet"));
                modifiedDataForFactory.pemTo = "";
                modifiedDataForFactory.stCodeTo = "";
                modifiedDataForFactory.dateTo = 0;
                modifiedDataForFactory.identsTo = null;
                dataForFactory = modifiedDataForFactory;
            }
        }

        return dataForFactory;
    }

    /**
     * Basisfilterfunktion für die Vorfilterung vom Zeitscheibenfilter der Werkseinsatzdaten im Baumuster-Filter. Werkseinsatzdaten,
     * die nicht mit der Zeitscheibe des Baumusters überlappen, werden entfernt. Die eigentliche Filterung des Stücklisteneintrags
     * findet jedoch noch nicht statt, da dafür die Vorfilterung von ALLEN Stücklisteneinträgen abgeschlossen sein muss
     * aufgrund der Vererbung von Werkseinsatzdaten durch Ersetzungen. Dies wird erst in {@link #basicCheckTimeSliceFilterForOneModel(iPartsDataPartListEntry, long, long, iPartsFilter)}
     * gemacht.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param validFrom     Baumuster gültig ab
     * @param validTo       Baumuster gültig bis
     * @param model         Baumuster-Nummer für die Filterung von Baumuster-bezogenen Idents
     * @param steering      Optionale Lenkung für die Filterung von Lenkungs-bezogenen Idents; bei {@code null} findet
     *                      keine Lenkungs-bezogene Filterung statt
     * @param filter
     */
    static void basicCheckTimeSlicePreFilter(iPartsDataPartListEntry partListEntry, long validFrom, long validTo, String model,
                                             String steering, iPartsFilter filter) {
        Set<String> models = new HashSet<>(1);
        models.add(model);

        // Beim Enddatum muss mit Long.MAX_VALUE gerechnet werden für unendlich anstatt mit 0
        if (validTo == 0) {
            validTo = Long.MAX_VALUE;
        }

        // Zunächst den Vergleich mit der maximalen Zeitscheibe aller Werkseinsatzdaten durchführen
        // Falls dies erfolgreich ist, die Zeitscheiben von vorhandenen Werkseinsatzdaten zu gültigen Werken mit der
        // Baumuster-Zeitscheibe vergleichen inkl. Korrektur von negativen Zeitintervallen (was keinen Sinn macht, wenn
        // sogar bei maximaler Zeitscheibe es keine Überlappung gibt)
        iPartsFactoryData factoryDataForRetailUnfiltered = partListEntry.getFactoryDataForRetailUnfiltered();
        iPartsFactoryData factoryDataWithoutReplacements;
        if (basicCheckTimeSliceFilterForMaxFactoryDataTimeSlice(partListEntry, validFrom, validTo, filter)
            && partListEntry.hasValidFactoryDataForRetailUnfiltered()) {
            factoryDataWithoutReplacements = new iPartsFactoryData();
            factoryDataWithoutReplacements.setEvalPemFrom(factoryDataForRetailUnfiltered.isEvalPemFrom());
            factoryDataWithoutReplacements.setEvalPemTo(factoryDataForRetailUnfiltered.isEvalPemTo());

            for (Map.Entry<String, List<iPartsFactoryData.DataForFactory>> factoryDataEntry : factoryDataForRetailUnfiltered.getFactoryDataMap().entrySet()) {
                List<iPartsFactoryData.DataForFactory> factoryDataList = new DwList<>(factoryDataEntry.getValue().size());
                for (iPartsFactoryData.DataForFactory dataForFactory : factoryDataEntry.getValue()) {
                    boolean alreadyCloned = false;

                    // DAIMLER-6564 Negative Zeitintervalle als gültig auswerten
                    boolean isValid = true;
                    long dateToWithInfinity = dataForFactory.getDateToWithInfinity();
                    if (dataForFactory.dateFrom > dateToWithInfinity) {
                        // "BM-Gültigkeit-Bis >= Termin-Ab der Werkseinsatzdaten" und "BM-Gültigkeit-Ab <= Termin-Bis der Werkseinsatzdaten"
                        if ((validTo >= dataForFactory.dateFrom) && (validFrom <= dateToWithInfinity)) {
                            iPartsFactoryData.DataForFactory modifiedDataForFactory = new iPartsFactoryData.DataForFactory();
                            modifiedDataForFactory.assign(dataForFactory);
                            modifiedDataForFactory.addFilterInfo(TranslationHandler.translate("!!Negatives Zeitintervall korrigiert auf \"PEM Termin bis\""));
                            modifiedDataForFactory.dateFrom = modifiedDataForFactory.dateTo;
                            dataForFactory = modifiedDataForFactory;
                            alreadyCloned = true;
                        } else { // Werkseinsatzdaten sind ungültig
                            isValid = false;
                        }
                    }

                    // Filterung mit der Baumuster-Zeitscheibe
                    if (isValid && iPartsFilterTimeSliceHelper.isInModelTimeSlice(dataForFactory.dateFrom, dateToWithInfinity,
                                                                                  validFrom, validTo)) {
                        // DAIMLER-6672/DAIMLER-6991 Flags "PEM ab/bis auswerten" berücksichtigen
                        iPartsFactoryData.DataForFactory oldDataForFactory = dataForFactory;
                        dataForFactory = modifyFactoryDataAccordingToPEMRelevantFlags(dataForFactory, factoryDataWithoutReplacements.isEvalPemFrom(),
                                                                                      factoryDataWithoutReplacements.isEvalPemTo(),
                                                                                      alreadyCloned);
                        if (oldDataForFactory != dataForFactory) {
                            alreadyCloned = true;
                        }

                        // DAIMLER-6571: Baumuster-bezogene Idents ausfiltern falls die Werkseinsatzdaten überhaupt gültig sind
                        dataForFactory = filterIdentsByModelsAndSteering(dataForFactory, models, steering, alreadyCloned);

                        factoryDataList.add(dataForFactory);
                    }
                }

                if (!factoryDataList.isEmpty()) {
                    factoryDataWithoutReplacements.setDataForFactory(factoryDataEntry.getKey(), factoryDataList);
                }
            }
        } else {
            // Keine Überlappung der maximalen Zeitscheibe aller Werkseinsatzdaten mit der Baumuster-Zeitscheibe
            // -> Alle Werkseinsatzdaten sind ungültig (sofern überhaupt welche vorhanden waren) -> leere Werkseinsatzdaten
            // mit den entsprechenden Flags von den Original-Werkseinsatzdaten erzeugen
            if (factoryDataForRetailUnfiltered != null) {
                factoryDataWithoutReplacements = new iPartsFactoryData();
                factoryDataWithoutReplacements.setHasFactoryDataWithInfiniteDates(factoryDataForRetailUnfiltered.hasFactoryDataWithInfiniteDates());
                factoryDataWithoutReplacements.setEvalPemFrom(factoryDataForRetailUnfiltered.isEvalPemFrom());
                factoryDataWithoutReplacements.setEvalPemTo(factoryDataForRetailUnfiltered.isEvalPemTo());
            } else {
                factoryDataWithoutReplacements = null;
            }
        }

        partListEntry.setFactoryDataForRetailWithoutReplacements(factoryDataWithoutReplacements);
    }

    /**
     * Basisfilterfunktion für den Zeitscheibenfilter der bereits gefilterten Werkseinsatzdaten im Baumuster-Filter, wenn
     * nur genau ein Baumuster für die Stückliste gültig ist. Vor dem Aufruf dieser Funktion muss
     * {@link #basicCheckTimeSlicePreFilter(iPartsDataPartListEntry, long, long, String, String, iPartsFilter)}
     * aufgerufen worden sein.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param validFrom
     * @param validTo
     * @param filter
     * @return
     */
    static boolean basicCheckTimeSliceFilterForOneModel(iPartsDataPartListEntry partListEntry, long validFrom, long validTo,
                                                        iPartsFilter filter) {
        // Zunächst den Vergleich mit der maximalen Zeitscheibe aller Werkseinsatzdaten durchführen (dies muss hier leider
        // nochmal gemacht werden, weil in basicCheckTimeSlicePreFilter() das Ausfiltern von Stücklisteneinträgen nicht
        // möglich ist, da diese Methode nicht pro iPartsFilterPartsEntries aufgerufen wird sondern nur einmal zentral
        // für die gesamte Stückliste
        if (!basicCheckTimeSliceFilterForMaxFactoryDataTimeSlice(partListEntry, validFrom, validTo, filter)) {
            return false;
        }

        if (partListEntry.hasValidFactoryDataForRetailUnfiltered()) {
            if (!partListEntry.hasValidFactoryDataForRetailWithoutReplacements()) { // Überprüfung der Werkseinsatzdaten VOR der Vererbung
                // Es sind keine gültigen Werkseinsatzdaten nach der Filterung mit der Baumuster-Zeitscheibe mehr übrig
                // -> Stücklisteneintrag wird ausgefiltert
                if ((filter != null) && filter.isWithFilterReason()) {
                    EtkProject project = partListEntry.getEtkProject();
                    filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                           "!!Keine gültigen Werkseinsatzdaten überlappen mit der Zeitscheibe \"%1\" bis \"%2\" %3",
                                                           iPartsFilterHelper.getFilterReasonDate(validFrom, project),
                                                           iPartsFilterHelper.getFilterReasonDate(validTo, project),
                                                           getFilterReasonSourceName(true));
                }
                return false;
            }
        } else if (!ignoreInvalidFactories(filter) && (partListEntry.getFactoryDataForRetail() != null)) {
            // Werkseinsatzdaten nur für ungültige Werke -> ausfiltern falls sie nicht ignoriert werden sollen
            if ((filter != null) && filter.isWithFilterReason()) {
                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL, "!!Nur ungültige Werkseinsatzdaten vorhanden");
            }
            return false;
        }
        return true; // keine Werkseinsatzdaten vorhanden -> gültig
    }

    /**
     * Basisfilterfunktion für den Zeitscheibenfilter der Werkseinsatzdaten im Baumuster-Filter, wenn mehrere Baumuster
     * für die Stückliste gültig sind.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param validFrom
     * @param validTo
     * @param models        Baumuster-Nummern für die Filterung von Baumuster-bezogenen Idents
     * @param steering      Optionale Lenkung für die Filterung von Lenkungs-bezogenen Idents; bei {@code null} findet
     *                      keine Lenkungs-bezogene Filterung statt
     * @return
     */
    static boolean basicCheckTimeSliceFilterForMultipleModels(iPartsDataPartListEntry partListEntry, long validFrom, long validTo,
                                                              Collection<String> models, String steering, iPartsFilter filter) {
        // Auch hier muss die maximale Zeitscheibe der ungefilterten Werkseinsatzdaten mit der Baumuster-Zeitscheibe
        // verglichen werden (analog zu basicCheckTimeSliceFilterForOneModel())
        boolean checkTimeSlice = basicCheckTimeSliceFilterForMaxFactoryDataTimeSlice(partListEntry, validFrom, validTo, filter);

        // DAIMLER-6672 Flags "PEM ab/bis auswerten" berücksichtigen
        partListEntry.setFactoryDataForRetailWithoutReplacements(modifyAndFilterAllFactoryData(partListEntry.getFactoryDataForRetailUnfiltered(),
                                                                                               models, steering));

        return checkTimeSlice;
    }

    /**
     * Basisfilterfunktion für den Zeitscheibenfilter der maximalen Zeitscheibe aller ungefilterten Werkseinsatzdaten im
     * Baumuster-Filter.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param validFrom
     * @param validTo
     * @param filter
     * @return
     */
    private static boolean basicCheckTimeSliceFilterForMaxFactoryDataTimeSlice(iPartsDataPartListEntry partListEntry, long validFrom,
                                                                               long validTo, iPartsFilter filter) {
        // Zeitscheibe des Stücklisteneintrags bei vorhandenen ungefilterten Werkseinsatzdaten zu gültigen Werken mit der
        // Baumuster-Zeitscheibe vergleichen
        iPartsFactoryData factoryDataUnfiltered = partListEntry.getFactoryDataForRetailUnfiltered();
        if (partListEntry.hasValidFactoryDataForRetailUnfiltered()) {
            long timeSliceDateFrom;
            if (factoryDataUnfiltered.hasFactoryDataWithInfiniteDates()) {
                timeSliceDateFrom = 0; // DAIMLER-6916: -unendlich
            } else {
                timeSliceDateFrom = partListEntry.getTimeSliceDateFrom(false);
            }
            long timeSliceDateTo = partListEntry.getTimeSliceDateTo(false);
            boolean result = iPartsFilterTimeSliceHelper.isInModelTimeSlice(timeSliceDateFrom,
                                                                            timeSliceDateTo,
                                                                            validFrom, validTo);
            if (!result && (filter != null) && filter.isWithFilterReason()) {
                EtkProject project = partListEntry.getEtkProject();
                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                       "!!Zeitscheibe \"%1\" bis \"%2\" überlappt nicht mit \"%3\" bis \"%4\" %5",
                                                       iPartsFilterHelper.getFilterReasonDate(timeSliceDateFrom, project),
                                                       iPartsFilterHelper.getFilterReasonDate(timeSliceDateTo, project),
                                                       iPartsFilterHelper.getFilterReasonDate(validFrom, project),
                                                       iPartsFilterHelper.getFilterReasonDate(validTo, project),
                                                       getFilterReasonSourceName(true));
            }
            return result;
        } else if ((factoryDataUnfiltered != null) && factoryDataUnfiltered.hasFactoryDataWithInfiniteDates()) { // DAIMLER-6916
            if ((filter != null) && filter.isWithFilterReason()) {
                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL, "!!Nur Werkseinsatzdaten mit ungültigen Datumswerten vorhanden");
            }
            return false;
        } else if (!ignoreInvalidFactories(filter) && (factoryDataUnfiltered != null)) {
            // Werkseinsatzdaten nur für ungültige Werke -> ausfiltern falls sie nicht ignoriert werden sollen
            if ((filter != null) && filter.isWithFilterReason()) {
                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL, "!!Nur ungültige Werkseinsatzdaten vorhanden");
            }
            return false;
        }
        return true; // keine Werkseinsatzdaten vorhanden -> gültig
    }

    /**
     * Baumuster-bezogene Rückmeldedaten entfernen, wenn diese nicht zu den übergebenen Baumustern bzw. Lenkung passen.
     *
     * @param dataForFactory
     * @param models         Optionale Baumuster-Nummern für die Filterung von Baumuster-bezogenen Idents; bei {@code null}
     *                       findet keine Baumuster-bezogene Filterung statt
     * @param steering       Optionale Lenkung für die Filterung von Lenkungs-bezogenen Idents; bei {@code null} findet
     *                       keine Lenkungs-bezogene Filterung statt
     * @param alreadyCloned  Flag, ob der übergebene Werkseinsatzdaten-Datensatz bereits geklont wurde und deswegen nicht
     *                       erneut geklont werden muss
     * @return
     */
    private static iPartsFactoryData.DataForFactory filterIdentsByModelsAndSteering(iPartsFactoryData.DataForFactory dataForFactory,
                                                                                    Collection<String> models, String steering,
                                                                                    boolean alreadyCloned) {
        if ((models == null) && (steering == null)) {
            return dataForFactory;
        }

        String filterReasonText = null;
        if (models != null) {
            if (steering != null) {
                filterReasonText = "!!Baumuster/Lenkung";
            } else {
                filterReasonText = "!!Baumuster";
            }
        } else if (steering != null) {
            filterReasonText = "!!Lenkung";
        }

        // DAIMLER-6571: Baumuster- und Lenkungs-bezogene Idents filtern
        // Idents ab
        if (dataForFactory.hasPEMFrom() && (dataForFactory.identsFrom != null)) {
            boolean filterIdentsFrom = true;
            iPartsFactoryData.DataForFactory modifiedDataForFactory = dataForFactory;
            if (!alreadyCloned) {
                filterIdentsFrom = false;
                for (iPartsFactoryData.IdentWithModelNumber identWithModel : dataForFactory.identsFrom.keySet()) {
                    if ((models != null) && !identWithModel.model.isEmpty()) { // Ident ist Baumuster-bezogen
                        if (!models.contains(identWithModel.model)) { // Ident muss ausgefiltert werden
                            filterIdentsFrom = true;
                        }
                    }

                    if (!filterIdentsFrom && (steering != null) && !identWithModel.steering.isEmpty()) { // Ident ist Lenkungs-bezogen
                        if (!steering.equals(identWithModel.steering)) { // Ident muss ausgefiltert werden
                            filterIdentsFrom = true;
                        }
                    }

                    if (filterIdentsFrom) {
                        // dataForFactory klonen
                        modifiedDataForFactory = new iPartsFactoryData.DataForFactory();
                        modifiedDataForFactory.assign(dataForFactory);
                        alreadyCloned = true;
                        break;
                    }
                }
            }

            // Baumuster- und Lenkungs-bezogene Idents ab filtern
            if (filterIdentsFrom) {
                int removedIdentsFromCounter = 0;
                Iterator<Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>>> identsFromIterator = modifiedDataForFactory.identsFrom.entrySet().iterator();
                while (identsFromIterator.hasNext()) {
                    iPartsFactoryData.IdentWithModelNumber identWithModel = identsFromIterator.next().getKey();
                    if ((models != null) && !identWithModel.model.isEmpty()) { // Ident ist Baumuster-bezogen
                        if (!models.contains(identWithModel.model)) { // Ident muss ausgefiltert werden
                            identsFromIterator.remove();
                            removedIdentsFromCounter++;
                            continue;
                        }
                    }

                    if ((steering != null) && !identWithModel.steering.isEmpty()) { // Ident ist Lenkungs-bezogen
                        if (!steering.equals(identWithModel.steering)) { // Ident muss ausgefiltert werden
                            identsFromIterator.remove();
                            removedIdentsFromCounter++;
                        }
                    }
                }

                if (modifiedDataForFactory.identsFrom.isEmpty()) {
                    modifiedDataForFactory.identsFrom = null;
                }

                if ((removedIdentsFromCounter > 0) && (filterReasonText != null)) {
                    modifiedDataForFactory.addFilterInfo(TranslationHandler.translate("!!%1 \"Idents ab\" wegen %2 ausgefiltert",
                                                                                      String.valueOf(removedIdentsFromCounter),
                                                                                      TranslationHandler.translate(filterReasonText)));
                }
            }

            dataForFactory = modifiedDataForFactory;
        }

        // Idents bis
        if (dataForFactory.hasPEMTo() && (dataForFactory.identsTo != null)) {
            boolean filterIdentsTo = true;
            iPartsFactoryData.DataForFactory modifiedDataForFactory = dataForFactory;
            if (!alreadyCloned) {
                filterIdentsTo = false;
                for (iPartsFactoryData.IdentWithModelNumber identWithModel : dataForFactory.identsTo.keySet()) {
                    if ((models != null) && !identWithModel.model.isEmpty()) { // Ident ist Baumuster-bezogen
                        if (!models.contains(identWithModel.model)) { // Ident muss ausgefiltert werden
                            filterIdentsTo = true;
                        }
                    }

                    if (!filterIdentsTo && (steering != null) && !identWithModel.steering.isEmpty()) { // Ident ist Lenkungs-bezogen
                        if (!steering.equals(identWithModel.steering)) { // Ident muss ausgefiltert werden
                            filterIdentsTo = true;
                        }
                    }

                    if (filterIdentsTo) {
                        // dataForFactory klonen
                        modifiedDataForFactory = new iPartsFactoryData.DataForFactory();
                        modifiedDataForFactory.assign(dataForFactory);
                        break;
                    }
                }
            }

            // Baumuster- und Lenkungs-bezogene Idents bis filtern
            if (filterIdentsTo) {
                int removedIdentsToCounter = 0;
                Iterator<Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>>> identsToIterator = modifiedDataForFactory.identsTo.entrySet().iterator();
                while (identsToIterator.hasNext()) {
                    iPartsFactoryData.IdentWithModelNumber identWithModel = identsToIterator.next().getKey();
                    if (models != null) {
                        if (!identWithModel.model.isEmpty()) { // Ident ist Baumuster-bezogen
                            if (!models.contains(identWithModel.model)) { // Ident muss ausgefiltert werden
                                identsToIterator.remove();
                                removedIdentsToCounter++;
                                continue;
                            }
                        }
                    }

                    if ((steering != null) && !identWithModel.steering.isEmpty()) { // Ident ist Lenkungs-bezogen
                        if (!steering.equals(identWithModel.steering)) { // Ident muss ausgefiltert werden
                            identsToIterator.remove();
                            removedIdentsToCounter++;
                        }
                    }
                }

                if (modifiedDataForFactory.identsTo.isEmpty()) {
                    modifiedDataForFactory.identsTo = null;
                }

                if ((removedIdentsToCounter > 0) && (filterReasonText != null)) {
                    modifiedDataForFactory.addFilterInfo(TranslationHandler.translate("!!%1 \"Idents bis\" wegen %2 ausgefiltert",
                                                                                      String.valueOf(removedIdentsToCounter),
                                                                                      TranslationHandler.translate(filterReasonText)));
                }
            }

            dataForFactory = modifiedDataForFactory;
        }

        return dataForFactory;
    }

    /**
     * Basisfilterfunktion für den Ausführungsartfilter.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param aaValue
     * @param filter
     * @return
     */
    static boolean basicCheckAAFilter(EtkDataPartListEntry partListEntry, String aaValue, iPartsFilter filter) {
        if (!StrUtils.isEmpty(aaValue)) {
            String entryAAValue = partListEntry.getFieldValue(iPartsConst.FIELD_K_AA);
            if (!StrUtils.isEmpty(entryAAValue)) {
                if (!entryAAValue.equals(aaValue)) {
                    if ((filter != null) && filter.isWithFilterReason()) {
                        filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                               "!!%1 \"%2\" ungleich \"%3\" %4", TranslationHandler.translate("!!Ausführungsart"),
                                                               entryAAValue, aaValue, getFilterReasonSourceName(true));
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Basisfilterfunktion für die Entfallteile
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param omittedParts
     * @param filter
     * @return
     */
    public static boolean basicCheckOmittedPartFilter(EtkDataPartListEntry partListEntry, iPartsOmittedParts omittedParts,
                                                      iPartsFilter filter) {
        if (omittedParts.isOmittedPart(partListEntry)) {
            if ((filter != null) && filter.isWithFilterReason()) {
                String partNumberFormatted = iPartsNumberHelper.formatPartNo(partListEntry.getEtkProject(), partListEntry.getPart().getAsId().getMatNr());
                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.OMITTED_PARTS, "!!\"%1\" ist ein Entfallteil",
                                                       partNumberFormatted);
            }
            return false;
        }
        return true;
    }

    /**
     * Implementierung des Wegfallsachnummern-Filters im Baumuster-Filter
     * Hier wird nur das Entfallteil selbst entfernt (im Gegensatz zur Variante aus dem Datenkarten-Filter).
     * Ausgefiltert wird, wenn der Dokumentationstyp DIALOG ist oder DIALOG_IPARTS in Kombination damit, dass der Eintrag
     * nur baumusterbildende Code hat.
     *
     * @param partListEntry
     * @param modelBuildingCodes Alle relevanten baumusterbildenden Code um zu identifizieren, ob das Teil technische Code hat
     * @param documentationType
     * @param omittedParts
     * @param filter
     * @return
     */
    public static boolean basicCheckOmittedPartsInModelFilter(EtkDataPartListEntry partListEntry, Set<String> modelBuildingCodes,
                                                              iPartsDocumentationType documentationType, iPartsOmittedParts omittedParts,
                                                              iPartsFilter filter) {
        EtkProject project = partListEntry.getEtkProject();
        // prüfen ob der partlistEntry eine Wegfallsachnummer hat
        if (omittedParts.isOmittedPart(partListEntry)) {
            // Doku-Typ und Code überprüfen:
            // Bei Einträgen, die technische Code haben und Doku-Typ DIALOG iParts sind, soll der
            // WegfallSNR-Filter erst im Datenkarten-Filter laufen
            if (documentationType != iPartsDocumentationType.DIALOG_IPARTS) {
                if ((filter != null) && filter.isWithFilterReason()) {
                    String partNumberFormatted = iPartsNumberHelper.formatPartNo(project, partListEntry.getPart().getAsId().getMatNr());
                    filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.OMITTED_PARTS,
                                                           "!!Entfallteil \"%1\" im %2 entfernt (Dokumentationstyp nicht %3)",
                                                           partNumberFormatted, iPartsFilterSwitchboard.FilterTypes.MODEL.getDescription(project),
                                                           iPartsDocumentationType.DIALOG_IPARTS.getExportValue());
                }
                return false;
            } else {
                Set<String> technicalCodes = DaimlerCodes.getCodeSet(partListEntry.getFieldValue(iPartsConst.FIELD_K_CODES));
                technicalCodes.removeAll(modelBuildingCodes);
                if (technicalCodes.isEmpty()) {
                    if ((filter != null) && filter.isWithFilterReason()) {
                        String partNumberFormatted = iPartsNumberHelper.formatPartNo(project, partListEntry.getPart().getAsId().getMatNr());
                        filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.OMITTED_PARTS,
                                                               "!!Entfallteil \"%1\" im %2 entfernt (Dokumentationstyp %3, aber nur baumusterbildende Code)",
                                                               partNumberFormatted, iPartsFilterSwitchboard.FilterTypes.MODEL.getDescription(project),
                                                               iPartsDocumentationType.DIALOG_IPARTS.getExportValue());
                    }
                    return false;
                }
                // der else Zweig (DIALOG-IPARTS und technische Code) wird im Datenkarten Filter behandelt
            }
        }
        return true;
    }

    /**
     * Basisfilterfunktion für die Entfallpositionen
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param filter
     * @return
     */
    public static boolean basicCheckOmittedPartListEntryFilter(EtkDataPartListEntry partListEntry, iPartsFilter filter) {
        if (partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT)) {
            if ((filter != null) && filter.isWithFilterReason()) {
                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.OMITTED_PART_LIST_ENTRIES,
                                                       "!!In der Ausgabe unterdrückt");
            }
            return false;
        }
        return true;
    }

    /**
     * Basisfilterfunktion für die zusätzlichen Stücklisteneinträge zu einer DIALOG-Position, die nur im Baumuster-Filter
     * berücksichtigt werden sollen.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param filter
     * @return
     */
    public static boolean basicCheckOnlyModelFilterForPartListEntryFilter(EtkDataPartListEntry partListEntry, iPartsFilter filter) {
        if (partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_ONLY_MODEL_FILTER)) {
            if ((filter != null) && filter.isWithFilterReason()) {
                filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                       "!!Nur für den Baumuster-Filter relevant und danach unterdrückt");
            }
            return false;
        }
        return true;
    }

    /**
     * Filter für Teilepositionen, die einen Leitungssatz-BK als Teilenummer haben.
     * Logik (DAIMLER-11879):
     * - TP gültig, wenn ET-KZ am Teilestamm = "E"
     * - TP gültig, wenn ET-KZ am Teilestamm = "K" und das sonstige-KZ = "LA"
     * <p>
     * Alle anderen Konstellationen sind ungültig. Bei ET-KZ = "K" und sonstige-KZ = "LA" soll statt der eigentlichen
     * Sachnummer eine "Dummy"-Sachnummer ausgegeben werden.
     *
     * @param filterEntries
     * @param filter
     */
    public static void basicCheckWireHarnessFilter(iPartsFilterPartsEntries filterEntries, iPartsFilter filter) {
        iPartsWireHarness wireHarness = (filter != null) ? filter.getWireHarnessCache(filterEntries.getEtkProject())
                                                         : iPartsWireHarness.getInstance(filterEntries.getEtkProject());
        // Alle Positionen ungültig setzen, die weder ET-KZ = "E" noch ET-KZ = "K" haben
        filterEntries.getVisibleEntries().stream()
                .filter(entry -> {
                    if (wireHarness.isWireHarness(entry.getPart().getAsId())) {
                        String etkzFromPart = entry.getPart().getFieldValue(iPartsConst.FIELD_M_ETKZ);
                        return !etkzFromPart.equals(iPartsWireHarnessHelper.WIRE_HARNESS_VALID_ETKZ)
                               && !etkzFromPart.equals(iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK);
                    }
                    return false;
                }).forEach(invalidEntry -> hideWireHarnessEntry(filterEntries, invalidEntry, filter,
                                                                "!!Leitungssatz-Baukasten mit ET-KZ ungleich \"%1\" oder \"%2\"",
                                                                iPartsWireHarnessHelper.WIRE_HARNESS_VALID_ETKZ,
                                                                iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK));

        VarParam<EtkDataPart> dummyDataPart = new VarParam<>(null);

        // Alle Positionen berücksichtigen, die ET-KZ = "K" haben
        filterEntries.getVisibleEntries().stream()
                .filter(entry -> wireHarness.isWireHarness(entry.getPart().getAsId())
                                 && entry.getPart().getFieldValue(iPartsConst.FIELD_M_ETKZ).equals(iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK))
                .forEach(entry -> {
                    // Sonstige-KZ bestimmen und prüfen
                    if (iPartsWireHarnessHelper.hasValidAdditionalWireHarnessFlag(entry)) {
                        // Die Original-Teilenummer merken
                        entry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR, entry.getFieldValue(iPartsConst.FIELD_K_MATNR),
                                                       true, DBActionOrigin.FROM_DB);

                        // Die gecachte Dummy-Sachnummer holen
                        if (dummyDataPart.getValue() == null) {
                            dummyDataPart.setValue(wireHarness.getWireHarnessDummyDataPart());
                        }

                        // Sonstige-KZ = "LA" -> TP gültig und Dummy-Sachnummer ausgeben
                        if (entry.getPart() instanceof iPartsDataPart) {
                            ((iPartsDataPart)entry.getPart()).setMappedMatNr(dummyDataPart.getValue().getAsId().getMatNr());
                        }
                        entry.setAggregatedDataObject(EtkDataObjectWithPart.AGGREGATE_NAME_PART, dummyDataPart.getValue());
                    } else {
                        // Sonstige-KZ nicht "LA" -> TP nicht gültig
                        hideWireHarnessEntry(filterEntries, entry, filter,
                                             "!!Leitungssatz-Baukasten mit ET-KZ = \"%1\" aber sonstige-KZ ungleich \"%2\"",
                                             iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK,
                                             iPartsWireHarnessHelper.WIRE_HARNESS_ADDITIONAL_CHECK_VALID_FLAG);
                    }
                });
    }

    /**
     * Hilfsmethode zum ungültig setzen von Leitungssatz-BK Teilepositionen
     *
     * @param filterEntries
     * @param entry
     * @param filter
     * @param reason
     * @param placeHolder
     */
    private static void hideWireHarnessEntry(iPartsFilterPartsEntries filterEntries, iPartsDataPartListEntry entry,
                                             iPartsFilter filter, String reason, String... placeHolder) {
        filterEntries.hideEntry(entry);
        if (filter.isWithFilterReason()) {
            filter.setFilterReasonForPartListEntry(entry, iPartsFilterSwitchboard.FilterTypes.WIRE_HARNESS, reason, placeHolder);
        }
    }

    /**
     * Liefert zum übergebenen Leitungssatz-BK die gefilterten Einzelteile zurück
     *
     * @param project
     * @param wireHarnessNumber
     * @param selectFields      - optionale selectFields
     * @return
     */
    public static List<iPartsDataWireHarness> getFilteredWireHarnessComponent(EtkProject project, String wireHarnessNumber,
                                                                              EtkDisplayFields selectFields) {
        iPartsDataWireHarnessList dataWireHarnessList = iPartsDataWireHarnessList.loadOneWireHarness(project, wireHarnessNumber,
                                                                                                     selectFields);
        List<iPartsDataWireHarness> result = new ArrayList<>();
        // Im Cache wird nur die M_TEXTNR für alle Sprachen gehalten. Alle anderen MultiLang Felder sowie die
        // Array-Felder werden nicht mitgeladen. Wenn weitere Felder benötigt werden, werden sie beim Zugriff
        // nachgeladen
        // -> Alle Array und MultiLang Felder bestimmen
        String[] multiLangAndArrayFields = selectFields.getFields()
                .stream()
                .filter(field -> field.isArray() || field.isMultiLanguage())
                .map(field -> field.getKey().getFieldName())
                .toArray(String[]::new);

        dataWireHarnessList.forEach(wireHarness -> result.addAll(getValidWireHarnessComponent(project, wireHarness,
                                                                                              multiLangAndArrayFields)));
        return result;
    }


    /**
     * Liefert die gültigen Leitungssatz-BK Einzelteile zurück
     *
     * @param wireHarnessPart
     * @param partTextAndArrayFieldsToLoad
     * @return
     */
    public static List<iPartsDataWireHarness> getValidWireHarnessComponent(EtkProject project,
                                                                           iPartsDataWireHarness wireHarnessPart,
                                                                           String... partTextAndArrayFieldsToLoad) {
        List<iPartsDataWireHarness> result = new ArrayList<>();
        if (wireHarnessPart.getAttributes().fieldExists(iPartsConst.FIELD_M_ETKZ)) {
            String etkzValue = wireHarnessPart.getFieldValue(iPartsConst.FIELD_M_ETKZ);
            // Hat ein Einzelteil ET-KZ = "E", dann ist es gültig
            if (etkzValue.equals(iPartsWireHarnessHelper.WIRE_HARNESS_VALID_ETKZ)) {
                result.add(wireHarnessPart);
            } else if (etkzValue.equals(iPartsWireHarnessHelper.WIRE_HARNESS_SIMPLIFIED_PART_ETKZ_VALUE)) {
                // Besitzt ein Einzelteil ET-KZ = "V", dann sollen statt dem Einzelteil die dazu gemappten
                // Teile aus DA_WH_SIMPLIFIED_PARTS angezeigt werden. Das eigentliche Teil wird nicht angezeigt.
                // Existiert zum Einzelteil kein Mapping, wird gar nichts angezeigt
                Optional<List<EtkDataPart>> simplifiedParts
                        = iPartsWireHarnessSimplifiedParts.getInstance(project).getSimplifiedPartsForWHPart(wireHarnessPart.getAsId().getSubSnr(),
                                                                                                            partTextAndArrayFieldsToLoad);

                simplifiedParts.ifPresent(parts -> parts
                        .forEach(simplifiedPart -> {
                            // Ersetze beim Original DataObject die MAT Felder. Da zu einer Teilenummer mehrere Teile gemappt
                            // werden können, wird das Original pro Mapping geklont und mit den jeweiligen Werten befüllt
                            iPartsDataWireHarness clonedData = wireHarnessPart.cloneMe(project);
                            clonedData.assignAttributesValues(project, simplifiedPart.getAttributes(), false, DBActionOrigin.FROM_DB);
                            clonedData.setFieldValue(iPartsConst.FIELD_DWH_SUB_SNR, simplifiedPart.getAsId().getMatNr(), DBActionOrigin.FROM_DB);
                            clonedData.updateIdFromPrimaryKeys();
                            result.add(clonedData);
                        }));
            }
        }
        return result;
    }

    /**
     * Basisfilterfunktion für die AS-Produktklassen-Gültigkeit.
     * Die Basisfunktionen sind static, damit das Ergebnis nicht von Zustandsvariablen der Filterklasse abhängen können
     *
     * @param partListEntry
     * @param productClass
     * @param filter
     * @return
     */
    static boolean basicCheckProductClassValidityFilter(EtkDataPartListEntry partListEntry, String productClass,
                                                        iPartsFilter filter) {
        List<String> productClassesFromPartListEntry = partListEntry.getFieldValueAsSetOfEnum(iPartsConst.FIELD_K_PCLASSES_VALIDITY);

        if (productClassesFromPartListEntry.isEmpty()) {
            return true;
        }

        for (String productClassFromPartListEntry : productClassesFromPartListEntry) {
            if (productClass.equals(productClassFromPartListEntry)) {
                return true;
            }
        }

        if ((filter != null) && filter.isWithFilterReason()) {
            filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.AS_PRODUCT_CLASS,
                                                   "!!Keine AS-Produktklasse enthalten in AS-Produktklassen-Gültigkeit");
        }
        return false;
    }

    public static boolean basicCheckCodeFilterForDatacard(iPartsDataPartListEntry partListEntry, Set<String> positiveCodes,
                                                          boolean isExtendedCodeFilter, iPartsFilter filter) {
        boolean result = basicCheckCodeFilterForDatacard(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED),
                                                         positiveCodes);
        if (!result && (filter != null) && filter.isWithFilterReason()) {
            filter.setFilterReasonForPartListEntry(partListEntry, isExtendedCodeFilter ? iPartsFilterSwitchboard.FilterTypes.EXTENDED_CODE : iPartsFilterSwitchboard.FilterTypes.DATACARD_CODE,
                                                   "!!Code nicht gültig für die Code %1",
                                                   getFilterReasonSourceName(false));
        }
        return result;
    }

    public static boolean basicCheckCodeFilterForDatacard(String code, Set<String> positiveCodes) {
        return basicCheckCodeFilter(code, positiveCodes, null);
    }

    public static boolean basicCheckCodeFilter(String code, Set<String> dataCardCodes, Set<String> omittedCodes) {
        return basicCheckCodeFilter(code, dataCardCodes, omittedCodes, null, null);
    }

    public static boolean basicCheckCodeFilter(Conjunction codeFunction, Set<String> positiveCodes, Set<String> skipCodesForScoring,
                                               Set<String> evalOnlyThisCodes, VarParam<Integer> outNumberCodeMatches,
                                               VarParam<Integer> outNumberPositiveCodeMatches) {
        Disjunction disjunction = new Disjunction(codeFunction);
        return basicCheckCodeFilter(disjunction, positiveCodes, skipCodesForScoring, evalOnlyThisCodes, outNumberCodeMatches,
                                    outNumberPositiveCodeMatches);
    }

    public static boolean basicCheckCodeFilter(String codeRule, Set<String> positiveCodes, Set<String> omittedCodes, Set<String> evalOnlyThisCodes,
                                               VarParam<Integer> outNumberPositiveCodeMatches) {
        // wenn kein Code gesetzt => immer sichtbar
        if (DaimlerCodes.isEmptyCodeString(codeRule)) {
            return true;
        }

        // Hier wird keine DNF benötigt, weil einfach nur die Code-Gültigkeit und das Zählen der positiven Code-Matches
        // durchgeführt werden soll
        BooleanFunction parser = DaimlerCodes.getFunctionParser(codeRule);
        BooleanFunctionTermTreeAnalyzer analyzer;
        try {
            analyzer = new BooleanFunctionTermTreeAnalyzer(parser);
            return basicCheckCodeFilter(analyzer.getTerms(), positiveCodes, omittedCodes, evalOnlyThisCodes, null, outNumberPositiveCodeMatches);

        } catch (BooleanFunctionSyntaxException e) {

            RuntimeException runtimeException = new RuntimeException("Error in code \"" + codeRule + "\": " + e.getMessage(), e);
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, runtimeException);
            return true;
        }


    }

    /**
     * Basisfilterroutine, die die Anzahl der positiven Codes, Matches und Non-Matches zählt und auf bestimmte Codes eingeschränkt
     * werden kann.
     *
     * @param codeFunction                 Coderegel, ggf. auch nur eine Teilkonjunktion einer Coderegel
     * @param positiveCodes                Diese Codes sind gerade gesetzt (z.B. stehen auf der Datenkarte)
     * @param skipCodesForScoring          Code die beim Scoring nicht berücksichtigt werden sollen
     * @param evalOnlyThisCodes            Kann null sein, Nur diese Codes sollen beachtet werden. Wir für die Baumusterfilterung
     *                                     benötigt. Dort werden nur die Baumusterbildenden Codes beachtet
     * @param outNumberCodeMatches         Für Rückgabe der Anzahl der Codes (aus {@code positiveCodes}, die in der Regel
     *                                     vorkommen (sowohl mit positivem als auch negativem Match); kann null sein
     * @param outNumberPositiveCodeMatches Für Rückgabe der Anzahl der Codes (aus {@code positiveCodes}, die in der Regel
     *                                     mit positivem Match vorkommen; kann null sein
     * @return
     */
    public static boolean basicCheckCodeFilter(Disjunction codeFunction, Set<String> positiveCodes, Set<String> skipCodesForScoring,
                                               Set<String> evalOnlyThisCodes, VarParam<Integer> outNumberCodeMatches,
                                               VarParam<Integer> outNumberPositiveCodeMatches) {

        // Spezialbehandlung mit Schnellausstieg für einen leeren Code -> ist immer true, enthält aber keine Codes
        if (codeFunction.isEmpty()) {
            if (outNumberCodeMatches != null) {
                outNumberCodeMatches.setValue(0);
            }
            if (outNumberPositiveCodeMatches != null) {
                outNumberPositiveCodeMatches.setValue(0);
            }
            return true;
        }


        try {
            BooleanFunction codeParser = new BooleanFunction(codeFunction);

            if ((outNumberCodeMatches != null) || (outNumberPositiveCodeMatches != null)) {
                // Zähle, wie viele der positiven Codes in der Coderegel auch vorkommen
                // ist zwar mathematisch Käse, braucht aber Daimler, weil dort true + true wahrer ist als ein einfaches true
                // ermittelt werden muss das vor der Umwandlung in die Disjunktive Normalform, weil wegen dem ausmultiplizieren noch mehr trues rauskommen könnten
                // https://confluence.docware.de/confluence/x/KgFGAQ
                // Hier werden zwei verschiedene Dinge gezählt:
                // 1. wie oft die Codes aus dem Set positive Codes im auszuwertenden Code vorkommen (siehe oben)
                // 2. wieviele Codes mit einem positiven Vorzeichen (outNumberPositiveCodeMatches) oder einem negativen (negativeCodeMatches)
                // im auszuwertenden Code vorkommen (diese Auswertung ist nur pro Teilkonjunktion sinnvoll)

                int outMatches = 0;
                int posMatches = 0;

                PositiveAndNegativeTerms positiveAndNegativeTerms = codeFunction.getPositiveAndNegativeTerms();

                for (String variableName : positiveAndNegativeTerms.getPositiveTerms()) {
                    if ((skipCodesForScoring != null) && skipCodesForScoring.contains(variableName)) {
                        continue;
                    }
                    if (positiveCodes.contains(variableName)) {
                        // Vorkommen der Variablen zählen
                        outMatches++;
                        posMatches++;
                    }
                }

                for (String variableName : positiveAndNegativeTerms.getNegativeTerms()) {
                    if ((skipCodesForScoring != null) && skipCodesForScoring.contains(variableName)) {
                        continue;
                    }
                    if (positiveCodes.contains(variableName)) {
                        // Vorkommen der Variablen zählen
                        outMatches++;
                    }
                }

                if (outNumberCodeMatches != null) {
                    outNumberCodeMatches.setValue(outMatches);
                }
                if (outNumberPositiveCodeMatches != null) {
                    outNumberPositiveCodeMatches.setValue(posMatches);
                }
            }


            /**
             * Boolesche Auswertung der Coderegel:
             * - Setze in der Coderegel der Teileposition alle Codes aus der Liste der Positiv-Codes auf true.
             * - Setze in der Coderegel der Teileposition alle Codes aus der Liste der Negativ-Codes auf false.
             *
             * Wenn evalOnlyThisCodes nicht null ist, sollen nur diese Codes eine Auswirkung auf das Ergebnis haben.
             * Anwendungsfall ist z.B. eine Coderegel die BM-bildende und technische Codes enthält, und wo nur die BM-bildenden
             * Codes einen Einfluss auf das Ergebnis haben sollen.
             * Damit im Beispiel die technischen Codes keinen Einfluss haben macht man dieses:
             * - Setze in der Coderegel der Teileposition alle anderen Codes auf true falls sie positiv angeschrieben sind
             *   bzw. auf false, falls sie negativ angeschrieben sind.
             */

            if (evalOnlyThisCodes != null) {
                // Nur wenn nicht alle Codes ausgewertet werden sollen, dann brauchen wir die Disjunctive Normalform

                // Hier etwas gruselig wieder nach Daimler-Code wandeln und die DNF holen. Wir machen das, weil dort ein Cache der DNFs ist
                codeParser = DaimlerCodes.getDnfCodeFunctionOriginal(DaimlerCodes.fromFunctionParser(codeParser));
            }

            // Vor der Auswertung der Coderegel muss der CodeParser geklont werden, da durch setBoolValue() auf einem BooleanFunctionBoolField
            // ansonsten die Coderegel (die evtl. aus dem Cache kommt) verändert werden würde
            codeParser = codeParser.cloneMe();

            List<BooleanFunctionElement> functionElements = codeParser.getFunctionElements();
            int i = 0;
            while (i < functionElements.size()) {
                if (functionElements.get(i) instanceof BooleanFunctionBoolField) {
                    BooleanFunctionBoolField currentField = (BooleanFunctionBoolField)functionElements.get(i);
                    String varName = currentField.getVariableName();

                    if ((evalOnlyThisCodes == null) || evalOnlyThisCodes.contains(varName)) {
                        // Alle Codes werden berücksichtigt oder
                        currentField.setBoolValue(positiveCodes.contains(varName));
                    } else {
                        // ein Code, der nicht ausgewertet werden soll. Setze den Wert so, dass am Ende des Tages true rauskommt
                        // also bei negierten Werten auf false und bei den anderen auf true.
                        boolean boolValue = true;
                        if (i > 0) {   // ein Code an Index 0 bedeutet implizit ein Plus-Operator
                            BooleanFunctionElement previousFunctionElement = functionElements.get(i - 1);
                            if (previousFunctionElement instanceof BooleanFunctionOperator) {
                                if (((BooleanFunctionOperator)previousFunctionElement).getValue() == BooleanOperator.NOT) {
                                    boolValue = false;
                                }
                            }
                        }
                        currentField.setBoolValue(boolValue);
                    }
                }
                i++;
            }
            // Werte die Coderegel der Teileposition nach boolescher Logik aus.
            return codeParser.calculateWithSyntaxCheck();

        } catch (BooleanFunctionSyntaxException e) {

            String code = DaimlerCodes.fromFunctionParser(new BooleanFunction(codeFunction));

            RuntimeException runtimeException = new RuntimeException("Error in code \"" + code + "\": " + e.getMessage(), e);
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, runtimeException);
            return true;
        }

    }

    static private boolean containsModelNo(EtkDataArray array, String modelNo) {
        if (array.isEmpty()) {
            return true;
        }
        return array.containsValue(modelNo);
    }

    static public boolean containsModelNo(Collection<String> modelNumbers, String modelNumber) {
        return modelNumbers.contains(modelNumber);
    }

    /**
     * Liefert nur die Produkte aus der übergebenen Liste zurück, die nach einem Auto-Product-Select noch valide sind.
     * Der Auto-Product-Select erfolgt mit der übergebenen {@link FinId}.
     *
     * @param project
     * @param products
     * @param finId
     * @param modelId
     * @param positiveCodes
     * @return Produkte, die nach einem Auto-Product-Select noch valide sind
     */
    public static List<iPartsProduct> getAutoSelectProductsForFIN(EtkProject project, List<iPartsProduct> products, FinId finId,
                                                                  iPartsModelId modelId, Set<String> positiveCodes) {
        // 0 Produkte
        if ((products == null) || products.isEmpty()) {
            return products;
        }

        // DAIMLER-6814: Ist ein Baumuster nur einem Produkt zugeordnet, dann wird das Produkt zur Filterung herangezogen,
        // auch wenn die Ident-AB/BIS-Werte und/oder die Coderegel nicht zutreffen.
        int visibleProductCount = 0;
        iPartsProduct visibleProduct = null;
        for (iPartsProduct product : products) {
            if (product.isRetailRelevant()) { // == FIELD_DP_PRODUCT_VISIBLE
                visibleProduct = product;
                visibleProductCount++;
                if (visibleProductCount > 1) {
                    break;
                }
            }
        }

        // Genau 1 Produkt sichtbar und gültig
        if (visibleProductCount == 1) {
            products = new DwList<>(1);
            products.add(visibleProduct);
            return products;
        }

        // Nur 1 (unsichtbares) Produkt in der Liste -> trotzdem dieses Produkt zurückgeben ohne APS-Check wegen Anzeige
        // in iPartsEdit
        if (products.size() == 1) {
            return products;
        }

        // Anzahl Produkte 0 oder > 1
        List<iPartsProduct> autoSelectProducts = new DwList<>(products.size());
        for (iPartsProduct product : products) {
            if (isProductValidAfterAutoProdSelect(project, finId.getWorldManufacturerIdentifier(), finId.getFactorySign(),
                                                  modelId, finId.getSerialNumber(), product, positiveCodes,
                                                  finId.getSteering())) {
                autoSelectProducts.add(product);
            }
        }
        return autoSelectProducts;
    }

    /**
     * Auto-Select-Produkte ermitteln
     *
     * @param modelNo
     * @param finId
     * @param codes
     * @param project
     * @return
     */
    public static List<iPartsProduct> getAutoSelectProducts(String modelNo, FinId finId, TwoGridValues codes, EtkProject project) {
        iPartsModelId modelId = new iPartsModelId(modelNo);
        // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster sollen beim Edit aber nicht bei den
        // Webservices berücksichtigt werden.
        List<iPartsProduct> productsForModel = iPartsProductHelper.getProductsForModelAndSessionType(project, modelId, null, null);
        boolean hasTechnicalCodes = !codes.getCheckedValues(false).isEmpty();
        return getAutoSelectProductsForFIN(project, productsForModel, finId, modelId, hasTechnicalCodes ? codes.getAllCheckedValues() : null);
    }

    /**
     * Ist das Produkt nach einem Auto-Product-Select mit den übergebenen Werten noch gültig?
     *
     * @param project
     * @param wmi
     * @param factorySign
     * @param modelId
     * @param serialNumber
     * @param product
     * @param positiveCodes
     * @return
     */
    public static boolean isProductValidAfterAutoProdSelect(EtkProject project, String wmi, String factorySign,
                                                            iPartsModelId modelId, int serialNumber, iPartsProduct product,
                                                            Set<String> positiveCodes, String steeringValueFromFIN) {
        // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster sollen beim Edit aber nicht bei den
        // Webservices berücksichtigt werden.
        // Die Berücksichtigung der Baumuster-Sichtbarkeiten kann in den Admin-Optionen konfiguriert werden.
        Session session = Session.get();
        boolean checkModelVisibility = iPartsPlugin.isCheckModelVisibility();
        Set<String> models = (!checkModelVisibility || ((session != null) && session.canHandleGui())) ? product.getModelNumbers(project) : product.getVisibleModelNumbers(project);
        if (modelId.isModelNumberValid(true) && !models.contains(modelId.getModelNumber())) {
            // Baumuster ist für dieses Produkt nicht im AS sichtbar
            return false;
        }

        // Nur Fahrzeug-Produkte mit Auto-Product-Select filtern (Produkte mit leerem Aggregatetyp müssen auch gefiltert
        // werden, da diese noch keine importierten Stücklisten haben und der Aggregatetyp auch durchaus Fahrzeug sein könnte)
        if (!product.getAggregateType().isEmpty() && !product.getAggregateType().equals(iPartsConst.AGGREGATE_TYPE_CAR)) {
            return true;
        }

        if (!checkAutoProductSelectIdents(project, wmi, factorySign, modelId, serialNumber, product, steeringValueFromFIN)) {
            return false;
        }

        return checkAutoProductSelectCodes(product, positiveCodes);
    }


    /**
     * Funktion, die aus den übergebenen Produkten die {@code DP_DISABLED_FILTERS} in Filtertypen umwandelt und zurückgibt.
     *
     * @param products
     * @return
     */
    public static Set<iPartsFilterSwitchboard.FilterTypes> getDisabledFilters(List<iPartsProduct> products) {
        // TreeSet, damit die Tests nicht fehlschlagen weil die Aufzählung in unterschiedlichen Reihenfolgen kommt
        Set<iPartsFilterSwitchboard.FilterTypes> disabledFilters = new TreeSet<>();
        if (!products.isEmpty()) {
            // ALLE definierten Filter holen und überprüfen, auch wenn aktuell nur ein paar wenige abgeschaltet werden können.
            EnumSet<iPartsFilterSwitchboard.FilterTypes> allFilters = iPartsFilterSwitchboard.FilterTypes.all();
            for (iPartsProduct product : products) {
                // Über alle definierten Filter iterieren und speichern, welche abgeschaltet sind.
                for (iPartsFilterSwitchboard.FilterTypes filterType : allFilters) {
                    if (product.getDisabledFilters().contains(filterType.getDBValue())) {
                        disabledFilters.add(filterType);
                    }
                }
            }
        }
        return disabledFilters;
    }

    /**
     * Funktion, die aus dem übergebenen Produkt die {@code DP_DISABLED_FILTERS} in Filtertypen umwandelt und zurückgibt.
     * Ruft {@link #getDisabledFilters(List)} auf.
     *
     * @param product
     * @return
     */
    public static Set<iPartsFilterSwitchboard.FilterTypes> getDisabledFilters(iPartsProduct product) {
        ArrayList<iPartsProduct> productList = new ArrayList<>();
        productList.add(product);
        return getDisabledFilters(productList);
    }


    /**
     * Ist das Fahrzeug-Produkt nach einem Auto-Product-Select mit den übergebenen Werten noch gültig?
     *
     * @param project
     * @param finId
     * @param modelId
     * @param product
     * @param positiveCodes
     * @return
     */
    public static boolean isProductValidAfterAutoProdSelect(EtkProject project, FinId finId, iPartsModelId modelId, iPartsProduct product,
                                                            Set<String> positiveCodes) {
        return isProductValidAfterAutoProdSelect(project, finId.getWorldManufacturerIdentifier(), finId.getFactorySign(),
                                                 modelId, finId.getSerialNumber(), product, positiveCodes,
                                                 finId.getSteering());
    }

    /**
     * Abgleich der Produkt-Idents mit der Endnummer der FIN (mit Millionenüberlauf) für den Auto-Product-Select
     *
     * @param project
     * @param wmi
     * @param factorySign
     * @param modelId
     * @param serialNumber
     * @param product
     * @return
     */
    public static boolean checkAutoProductSelectIdents(EtkProject project, String wmi, String factorySign, iPartsModelId modelId,
                                                       int serialNumber, iPartsProduct product, String steeringValueFromFIN) {
        // Daten aus der FIN auf Gültigkeit überprüfen ohne diese kein Ident-Check notwendig ist -> true
        if ((serialNumber == FinId.INVALID_SERIAL_NUMBER) || StrUtils.isEmpty(factorySign) || !iPartsModel.isModelNumberValid(modelId.getModelNumber())) {
            return true;
        }

        // Ohne Idents am Produkt -> true
        List<iPartsIdentRange> productIdents = product.getIdentsForAutoSelect();
        if (productIdents.isEmpty()) {
            return true;
        }

        // Endnummer der FIN mit Millionenüberlauf
        iPartsFactoryModel.SerialNoAndFactory finEndnumberWithOverflowAndFactory = iPartsFactoryModel.getInstance(project).getSerialNumberWithOverflowAndFactoryForWMI(wmi, factorySign, modelId,
                                                                                                                                                                       serialNumber, FinId.IDENT_NO_LENGTH,
                                                                                                                                                                       steeringValueFromFIN);

        // Pro Ident-Entry kann es ein Ab- und ein Bis-Ident geben. Laut Confluence ist das Produkt nur gültig, wenn
        // Ab-Ident <= FIN-Ident und Bis-Ident > FIN-Ident
        boolean foundInvalidIdentEntry = false;
        for (iPartsIdentRange identRange : productIdents) {
            iPartsFactoryModel.SerialNoAndFactory identFromEndnumberWithOverflow = createProductIdentWithOverflowAndFactory(project, identRange.getFromIdent(),
                                                                                                                            wmi, modelId, steeringValueFromFIN);
            iPartsFactoryModel.SerialNoAndFactory identToEndnumberWithOverflow = createProductIdentWithOverflowAndFactory(project, identRange.getToIdent(),
                                                                                                                          wmi, modelId, steeringValueFromFIN);
            // Ab-Ident oder Bis-Ident ist befüllt
            if (identFromEndnumberWithOverflow.compareFactory(finEndnumberWithOverflowAndFactory, false)
                || identToEndnumberWithOverflow.compareFactory(finEndnumberWithOverflowAndFactory, false)) {
                boolean validIdentEntry = true;

                // Wenn der Ab-Ident befüllt ist -> Prüfe, ob Ab-Ident gegenüber FIN-Ident gültig ist. Sollte der
                // Ab-Ident leer sein, ist der Ident-Entry bis zum Bis-Ident Check noch gültig (Wir haben auch Datensätze
                // bei denen nur der Ab-Ident oder nur der Bis-Ident befüllt ist)
                if (identFromEndnumberWithOverflow.getSerialNumber() != FinId.INVALID_SERIAL_NUMBER) {
                    validIdentEntry = identFromEndnumberWithOverflow.getSerialNumber() <= finEndnumberWithOverflowAndFactory.getSerialNumber();
                }

                // War der Ab-Ident gültig (entweder durch die erfüllte Bedingung oder er war leer), dann prüfe, ob der
                // Bis-Ident gültig ist.
                // War der Ab-Ident ungültig, dann wird die Prüfung des Bis-Ident übersprungen.
                if (validIdentEntry && (identToEndnumberWithOverflow.getSerialNumber() != FinId.INVALID_SERIAL_NUMBER)) {
                    validIdentEntry = identToEndnumberWithOverflow.getSerialNumber() >= finEndnumberWithOverflowAndFactory.getSerialNumber();
                }

                // Gleiches Werk, beide Idents (zusammen) gültig -> Das Produkt ist gültig
                // War einer der Idents leer und der andere gültig, dann haben wir hier ebenfalls ein gültiges Produkt.
                // Alle anderen Fälle führen zu einem ungültigen Ident-Intervall -> nächstes Ident-Intervall überprüfen
                if (validIdentEntry) {
                    // Hier kann man direkt rausspringen, da wir ein Ident-Intervall haben, in dem die FIN ausdrücklich
                    // gültig ist
                    return true;
                } else {
                    foundInvalidIdentEntry = true;
                }
            }
        }
        return !foundInvalidIdentEntry;
    }

    /**
     * Abgleich der Produkt-Codebedingung mit den übergebenen Code für den Auto-Product-Select
     *
     * @param product
     * @param positiveCodes
     * @return
     */
    public static boolean checkAutoProductSelectCodes(iPartsProduct product, Set<String> positiveCodes) {
        if ((positiveCodes != null) && !positiveCodes.isEmpty() && StrUtils.isValid(product.getCodeForAutoSelect())) {
            return basicCheckCodeFilter(product.getCodeForAutoSelect(), positiveCodes, null, null, null);
        }
        return true;
    }

    /**
     * Erstellt einen Produkt-Ident mit Millionenüberlauf und Werksnummer
     *
     * @param project
     * @param ident
     * @param wmi
     * @param modelId
     * @return
     */
    private static iPartsFactoryModel.SerialNoAndFactory createProductIdentWithOverflowAndFactory(EtkProject project, String ident,
                                                                                                  String wmi, iPartsModelId modelId, String steeringValueFromFIN) {
        if (!StrUtils.isValid(ident) || (ident.length() < FinId.IDENT_NO_LENGTH)) {
            return new iPartsFactoryModel.SerialNoAndFactory();
        }
        iPartsFactoryData.Ident identObject = new iPartsFactoryData.Ident(ident, FinId.IDENT_NO_LENGTH);
        return iPartsFactoryModel.getInstance(project).getSerialNumberWithOverflowAndFactoryForWMI(wmi, identObject.factorySign,
                                                                                                   modelId, identObject.endNumber,
                                                                                                   FinId.IDENT_NO_LENGTH, steeringValueFromFIN);
    }

    public static iPartsFilterSwitchboard.FilterTypes getFilterType(boolean isSAAssembly, boolean isModelFilter) {
        if (isSAAssembly) {
            return iPartsFilterSwitchboard.FilterTypes.SA_STRICH;
        } else if (isModelFilter) {
            return iPartsFilterSwitchboard.FilterTypes.MODEL;
        } else {
            return iPartsFilterSwitchboard.FilterTypes.DATACARD_SA;
        }
    }

    /**
     * Filtert die übergebenen Ereignisse mit dem Ereignis von der Datenkarte.
     *
     * @param filter
     * @param partListEntry     Falls nicht {@code null} wird der entsprechende Filtergrund an den Stücklisteneintrag geschrieben
     * @param colorTableObject  Falls nicht {@code null} wird der entsprechende Filtergrund an das Farbvariantentabellen-Objekt geschrieben
     * @param eventFromId
     * @param eventToId
     * @param eventFromDataCard
     * @param assemblyData
     * @param project
     * @return
     */
    public static boolean basicCheckEventFilter(iPartsFilter filter, iPartsDataPartListEntry partListEntry, iPartsColorTable.AbstractColortablePEMEvaluation colorTableObject,
                                                String eventFromId, String eventToId, iPartsEvent eventFromDataCard, iPartsFilter.FilterCachedAssemblyData assemblyData,
                                                EtkProject project) {
        // Voraussetzungen:
        // - Auswertung nur bei ereignisgesteuerten Baureihen
        // - Konnte zur Datenkarte kein Ereignis ermittelt werden bzw. wurde im Filter-Dialog kein Ereignis eingegeben,
        //   dann wird die Ereignisprüfung übersprungen. Teileposition ist vorerst gültig.
        // - mind. ein Ereignis muss gesetzt sein
        if (assemblyData.isSeriesFromProductForModuleEventControlled() && (eventFromDataCard != null) && !StrUtils.isEmpty(eventFromId, eventToId)) {
            int datacardEventSeqNo = assemblyData.getEventSeqNo(eventFromDataCard.getEventId());
            String type = TranslationHandler.translate((partListEntry == null) ? "!!Farbvariante" : "!!Teileposition");
            // Ereignis-Ab Prüfung
            // Gibt es kein Ereignis-AB => AB-Prüfung true
            if (StrUtils.isValid(eventFromId)) {
                int eventFromSeqNo = assemblyData.getEventSeqNo(eventFromId);
                if ((eventFromSeqNo > -1) && (datacardEventSeqNo < eventFromSeqNo)) {
                    setFilterReasonForDataObject(partListEntry, colorTableObject, filter, project, iPartsFilterSwitchboard.FilterTypes.DATACARD_CODE,
                                                 null, iPartsColorFilter.ColorTableContentFilterTypes.EVENT,
                                                 "!!Ereignis ab \"%1\" von der %2 liegt nach dem Ereignis \"%3\" von der Datenkarte",
                                                 eventFromId, type, eventFromDataCard.getEventId());
                    return false;
                }
            }
            // Ereignis-Bis Prüfung
            // Gibt es kein Ereignis-BIS => BIS-Prüfung true
            if (StrUtils.isValid(eventToId)) {
                int eventToSeqNo = assemblyData.getEventSeqNo(eventToId);
                if ((eventToSeqNo > -1) && (datacardEventSeqNo >= eventToSeqNo)) {
                    setFilterReasonForDataObject(partListEntry, colorTableObject, filter, project, iPartsFilterSwitchboard.FilterTypes.DATACARD_CODE,
                                                 null, iPartsColorFilter.ColorTableContentFilterTypes.EVENT,
                                                 "!!Ereignis bis \"%1\" von der %2 liegt vor oder ist gleich dem Ereignis \"%3\" von der Datenkarte",
                                                 eventToId, type, eventFromDataCard.getEventId());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Filtert die übergebene Zeichnungsreferenz mit dem Ereignis von der Datenkarte.
     *
     * @param image
     * @param eventFromDatacard
     * @param assemblyData
     * @return
     */
    public static boolean basicCheckEventFilterForPictureReference(iPartsDataImage image, iPartsEvent eventFromDatacard,
                                                                   iPartsFilter.FilterCachedAssemblyData assemblyData) {
        String eventFromId = image.getFieldValue(iPartsConst.FIELD_I_EVENT_FROM);
        String eventToId = image.getFieldValue(iPartsConst.FIELD_I_EVENT_TO);
        return basicCheckEventFilter(null, null, null, eventFromId, eventToId, eventFromDatacard, assemblyData, null);

    }

    /**
     * Testet einen Stücklisteneintrag, ob er bzgl. seiner Ländergültigkeit für das übergebene Land gültig ist.
     *
     * @param partListEntry
     * @param country       Ausgewähltes Land (muss in Großbuchstaben sein)
     * @param filter
     * @return
     */
    public static boolean basicCheckCountryValidityFilter(iPartsDataPartListEntry partListEntry, String country, iPartsFilter filter) {
        if (StrUtils.isEmpty(country)) { // Kein Land ausgewählt
            return true;
        }

        Set<String> countryValidities = partListEntry.getCountryValidities();

        if (countryValidities.isEmpty()) { // Keine Ländergültigkeit -> immer gültig
            return true;
        }
        boolean result = countryValidities.contains(country.toUpperCase());
        if (!result) { // Ländergültigkeit prüfen
            if ((filter != null) && filter.isWithFilterReason()) {
                setFilterReasonForPartListEntry(partListEntry, filter, COUNTRY_VALIDITY_FILTER, "!!Land \"%1\" %2 nicht in Ländergültigkeit enthalten",
                                                country, iPartsFilterHelper.getFilterReasonSourceName(false));
            }
        }
        return result;
    }

    /**
     * Testet einen Stücklisteneintrag, ob er bzgl. seiner Spezifikationen für die übergebenen Spezifikationen gültig ist.
     *
     * @param partListEntry
     * @param specValidities
     * @param filter
     * @return
     */
    public static boolean basicCheckSpecificationFilter(iPartsDataPartListEntry partListEntry, Set<String> specValidities,
                                                        iPartsFilter filter) {
        if (!Utils.isValid(specValidities)) { // Keine Spezifikationen ausgewählt
            return true;
        }

        Set<String> specValiditiesOfPartListEntry = partListEntry.getSpecValidities();
        if (specValiditiesOfPartListEntry.isEmpty()) { // Keine Spezifikationen -> immer gültig
            return true;
        }
        for (String specValidity : specValiditiesOfPartListEntry) { // Spezifikationen prüfen
            if (specValidities.contains(specValidity)) {
                return true;
            }
        }

        if ((filter != null) && filter.isWithFilterReason()) {
            setFilterReasonForPartListEntry(partListEntry, filter, SPECIFICATION_FILTER, "!!Keine Spezifikation %1 in Spezifikations-Gültigkeit enthalten",
                                            iPartsFilterHelper.getFilterReasonSourceName(false));
        }
        return false;
    }

    public static boolean replacementChainContainsPartListEntry(iPartsDataPartListEntry sourcePartlistEntry,
                                                                iPartsDataPartListEntry searchPartlistEntry) {
        if (!sourcePartlistEntry.hasPredecessors() && !sourcePartlistEntry.hasSuccessors()) {
            return false;
        }
        if (sourcePartlistEntry.getAsId().equals(searchPartlistEntry.getAsId())) {
            return true;
        }

        // Bereits besuchte Stücklisteneinträge merken um Endlosschleifen zu verhindern
        Set<iPartsDataPartListEntry> visitedEntries = new HashSet<>();
        Stack<iPartsDataPartListEntry> replacementStack = new Stack<>();

        replacementStack.push(sourcePartlistEntry);
        while (!replacementStack.isEmpty()) {
            iPartsDataPartListEntry currentNode = replacementStack.pop();
            if (currentNode.getAsId().equals(searchPartlistEntry.getAsId())) {
                return true;
            }
            if (visitedEntries.add(currentNode)) {
                // Alle Vorgänger auf den Stack legen
                Collection<iPartsReplacement> predecessors = currentNode.getPredecessors();
                if (predecessors != null && !predecessors.isEmpty()) {
                    for (iPartsReplacement predecessor : predecessors) {
                        if (predecessor.predecessorEntry instanceof iPartsDataPartListEntry) {
                            replacementStack.push((iPartsDataPartListEntry)(predecessor.predecessorEntry));
                        }
                    }
                }

                // Alle Nachfolger auf den Stack legen
                Collection<iPartsReplacement> successors = currentNode.getSuccessors();
                if (successors != null && !successors.isEmpty()) {
                    for (iPartsReplacement successor : successors) {
                        if (successor.successorEntry instanceof iPartsDataPartListEntry) {
                            replacementStack.push((iPartsDataPartListEntry)(successor.successorEntry));
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Ermittelt die Vergleichsgröße um zu prüfen ob Ersetzungen identisch sind.
     * Beim Nachfolger kann entweder der Stücklisteneintrag oder nur eine Materialnummer angegeben sein. Wenn der
     * Stücklisteneintrag angegeben ist, wird hier für den Vergleich nur die laufende Nummer verwendet, da
     * Ersetzungen nur innerhalb der Stückliste zulässig sind.
     * Beim Vorgänger gibt es nur die Materialnummer
     *
     * @param replacement
     * @param isSuccessor
     * @return
     */
    private static String getReplacementCompareNumber(iPartsReplacement replacement, boolean isSuccessor) {
        if (isSuccessor) {
            if (replacement.successorEntry != null) {
                return replacement.successorEntry.getAsId().getKLfdnr();
            } else {
                return replacement.successorPartNumber;
            }
        } else {
            if (replacement.predecessorEntry != null) {
                return replacement.predecessorEntry.getAsId().getKLfdnr();
            }
        }
        return "";
    }

    private static boolean findReplacementInList(iPartsReplacement searchReplacement,
                                                 Collection<iPartsReplacement> searchList,
                                                 boolean isSuccessor) {
        String searchNum = searchReplacement.getReplacementCompareNumber(isSuccessor);
        String searchRFME = searchReplacement.rfmeaFlags + "|" + searchReplacement.rfmenFlags;

        for (iPartsReplacement replacement : searchList) {
            String replacementNum = replacement.getReplacementCompareNumber(isSuccessor);
            String replacementRFME = replacement.rfmeaFlags + "|" + replacement.rfmenFlags;

            if (searchNum.equals(replacementNum) && searchRFME.equals(replacementRFME)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkIdenticalReplacements(iPartsDataPartListEntry entry1, iPartsDataPartListEntry entry2) {
        if ((!entry1.hasSuccessors() && !entry2.hasSuccessors()) && (!entry1.hasPredecessors() && !entry2.hasPredecessors())) {
            // beide Einträge haben weder Vorgänger noch Nachfolger
            return true;
        }
        if ((entry1.hasSuccessors() && !entry2.hasSuccessors()) || (!entry1.hasSuccessors() && entry2.hasSuccessors())) {
            // Ein Eintrag hat Nachfolger, der andere aber nicht
            return false;
        }
        if ((entry1.hasPredecessors() && !entry2.hasPredecessors()) || (!entry1.hasPredecessors() && entry2.hasPredecessors())) {
            // Ein Eintrag hat Vorgänger, der andere aber nicht
            return false;
        }

        if (entry1.hasSuccessors() && entry2.hasSuccessors()) {
            Collection<iPartsReplacement> successors1 = entry1.getSuccessors();
            Collection<iPartsReplacement> successors2 = entry2.getSuccessors();
            // zuerst muss die Anzahl übereinstimmen und dann noch alle Nachfolger
            if (successors1.size() != successors2.size()) {
                return false;
            }
            for (iPartsReplacement successor1 : successors1) {
                if (!findReplacementInList(successor1, successors2, true)) {
                    return false;
                }
            }
        }

        if (entry1.hasPredecessors() && entry2.hasPredecessors()) {
            Collection<iPartsReplacement> predecessors1 = entry1.getPredecessors();
            Collection<iPartsReplacement> predecessors2 = entry2.getPredecessors();
            // zuerst muss die Anzahl übereinstimmen und dann noch alle Vorgänger
            if (predecessors1.size() != predecessors2.size()) {
                return false;
            }
            for (iPartsReplacement predecessor1 : predecessors1) {
                if (!findReplacementInList(predecessor1, predecessors2, false)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Daten aus Baumuster-Datenkarte in Fahrzeug-Datenkarte übernehmen
     *
     * @param modelDataCard nur lesender Zugriff
     * @param dataCard      wird gefüllt
     */
    public static void adoptDCToModelDC(EtkProject project, VehicleDataCard modelDataCard, VehicleDataCard dataCard) {
        // Aggregatebaumuster
        TwoGridValues twoGridValues = adoptOneDCToModelDC(modelDataCard.getAggregateModelNumbers(), dataCard.getAggregateModelNumbers());
        dataCard.setAggregateModelNumbers(twoGridValues);
        // SAAs
        twoGridValues = adoptOneDCToModelDC(modelDataCard.getSaas(), dataCard.getSaas());
        dataCard.setSaas(twoGridValues);
        // Code
        dataCard.setCodes(dataCard.getCodes().cloneMe());

        // Die Anreicherung um Baumuster-Daten auch für alle enthaltenen Aggregate-Datenkarten durchführen
        for (AggregateDataCard aggregateDataCard : dataCard.getActiveAggregates()) {
            String aggregateModelNo = aggregateDataCard.getModelNo();
            if (StrUtils.isValid(aggregateModelNo)) {
                AggregateDataCard aggregateModelDatacard = new AggregateDataCard(true);
                aggregateModelDatacard.fillByModel(project, aggregateModelNo);
                //Model- und Vehicle-Datacard zusammenführen
                adoptDCToModelDC(aggregateModelDatacard, aggregateDataCard);
            }
        }
    }

    /**
     * Daten aus Aggregate-Datenkarte in Fahrzeug-Datenkarte übernehmen
     *
     * @param modelDataCard nur lesender Zugriff
     * @param dataCard      wird gefüllt
     */
    public static void adoptDCToModelDC(AggregateDataCard modelDataCard, AggregateDataCard dataCard) {
        // SAAs
        TwoGridValues twoGridValues = adoptOneDCToModelDC(modelDataCard.getSaas(), dataCard.getSaas());
        dataCard.setSaas(twoGridValues);
        // Code
        dataCard.setCodes(dataCard.getCodes().cloneMe());
    }

    private static TwoGridValues adoptOneDCToModelDC(TwoGridValues modelGridValues, TwoGridValues vehicleGridValues) {
        TwoGridValues modelStates = modelGridValues.cloneMe();
        TwoGridValues vehicleStates = vehicleGridValues.cloneMe();

        DiskMappedKeyValueListCompare listComp = new DiskMappedKeyValueListCompare(true, true, true);
        for (TwoGridValues.ValueState valueStates : modelGridValues.getTopGridValues()) {
            listComp.putFirst(valueStates.value, valueStates.value);
        }
        for (TwoGridValues.ValueState valueStates : vehicleStates.getTopGridValues()) {
            listComp.putSecond(valueStates.value, valueStates.value);
        }

        Iterator<DiskMappedKeyValueEntry> iter;
        //nur im Baumuster
        iter = listComp.getOnlyInFirstItems().getIterator();
        while (iter.hasNext()) {
            DiskMappedKeyValueEntry entry = iter.next();
            modelStates.toggleSingleValue(entry.getValue(), true);
        }

        //in beiden
        iter = listComp.getEqualItems().getIterator();
        while (iter.hasNext()) {
            DiskMappedKeyValueEntry entry = iter.next();
            vehicleStates.removeSingleValue(entry.getValue(), true);
        }
        listComp.cleanup();

        return new TwoGridValues(modelStates.getTopGridValues(), vehicleStates.getTopGridValues());
    }

    /**
     * Check, ob Strukturknoten gültige Kinder enthält oder bezüglich seinen Produkten selber gültig ist
     *
     * @param filter
     * @param partListEntry
     * @param node
     * @return
     */
    public static boolean isStructureNodeWithProductsVisible(iPartsFilter filter, iPartsDataPartListEntry partListEntry,
                                                             iPartsStructureNode node) {
        // Wenn der Knoten gar keine Kinder hat, ist er nicht sichtbar
        if (!node.hasProduct() && !node.hasModel() && !node.hasSeries() && node.getAllSubNodes().isEmpty()) {
            return false;
        }

        // Hat der Benutzer beide Eigenschaften, brauchen wir keine Filterung auf Produkt-Ebene durchzuführen
        if (filter.isCarAndTruckRightsInSession()) {
            return true;
        }

        // Check, ob gültig aufgrund seiner Kinder
        boolean childrenInvalid = false;
        if (!node.getChildren().isEmpty()) {
            if (node.getChildren().stream().anyMatch(childNode -> isStructureNodeWithProductsVisible(filter, partListEntry, childNode))) {
                // Knoten hat Kinder und mind. eins ist gültig (Kind hat Produkte, bei denen mind. eins gültig ist oder
                // hat Struktur-Unterknoten) -> Knoten wird angezeigt
                return true;
            } else {
                // Knoten hat Kinder aber keines ist gültig bzw. sichtbar
                childrenInvalid = true;
            }
        }

        // Check, ob gültig aufgrund seiner Produkte
        boolean productsInvalid = false;
        if (!node.getProductList().isEmpty()) {
            boolean productClassValid = false;
            Set<String> asProductClasses = null;
            if (filter.isSessionWithGui()) {
                asProductClasses = node.getProductClassesIncludingParentNodes();
            }
            if ((asProductClasses != null) && !asProductClasses.isEmpty()) {
                // Prüfen, ob mindestens eine der AS-Produktklassen des Knotens zu den Eigenschaften des Benutzers passt
                Set<String> validASProductClasses = iPartsRight.checkCarAndVanInSession() ? iPartsProduct.AS_PRODUCT_CLASSES_CAR_AND_VAN
                                                                                          : iPartsProduct.AS_PRODUCT_CLASSES_TRUCK_AND_BUS;
                for (String productClass : asProductClasses) {
                    if (validASProductClasses.contains(productClass)) {
                        productClassValid = true;
                        break;
                    }
                }

                // Falls keine AS-Produktklasse passt, muss noch nach Spezialkatalogen und PSK-Produkten gesucht werden
                if (!productClassValid) {
                    if (node.getProductList().stream().anyMatch(structureProductNode -> {
                        iPartsProduct product = iPartsProduct.getInstance(partListEntry.getEtkProject(),
                                                                          structureProductNode.getProductId());

                        // Spezialkataloge sind immer sichtbar
                        if (product.isSpecialCatalog()) {
                            return true;
                        }

                        // PSK Check
                        if (product.isPSK()) {
                            // PSK Produkte sollen bei gesetztem PSK Recht unabhängig von den AS-Produktklassen angezeigt werden.
                            // Ist es ein PSK Produkt und hat der Benutzer keine PSK Rechte, fliegt der Knoten raus.
                            return doPSKProductCheck(filter, partListEntry);
                        }

                        return false;
                    })) {
                        // Knoten hat Spezialkataloge oder PSK-Produkte -> Knoten wird angezeigt
                        return true;
                    } else {
                        // Knoten hat keine PSK-Produkte
                        productsInvalid = true;
                    }
                }
            } else {
                productClassValid = true;
            }

            if (productClassValid) {
                if (node.getProductList().stream().anyMatch(structureProductNode -> isProductNodeVisible(filter, partListEntry,
                                                                                                         structureProductNode.getProductId()))) {
                    // Knoten hat Produkte und mind. eins ist gültig -> Knoten wird angezeigt
                    return true;
                } else {
                    // Knoten hat Produkte aber keines ist gültig
                    productsInvalid = true;
                }
            }
        }

        // Knoten hat weder Kinder noch Produkte (keine Produkt-Struktur-Knoten) -> Knoten anzeigen
        return !productsInvalid && !childrenInvalid;
    }

    /**
     * Check, ob der Produkt-Knoten sichtbar ist. Beinhaltet einen PSK- und einen Benutzer-Eigenschaften-Check für den
     * Benutzer der aktuellen Session.
     *
     * @param partListEntry
     * @param productId
     * @return
     */
    public static boolean isProductNodeVisible(iPartsFilter filter, iPartsDataPartListEntry partListEntry, iPartsProductId productId) {
        iPartsProduct product = iPartsProduct.getInstance(partListEntry.getEtkProject(), productId);
        // PSK Check vor dem AS Produktklassen-Check
        if (product.isPSK()) {
            // PSK Produkte sollen bei gesetztem PSK Recht unabhängig von den AS Produktklassen angezeigt werden.
            // Ist es ein PSK Produkt und hat der Benutzer keine PSK Rechte, fliegt der Knoten raus.
            return doPSKProductCheck(filter, partListEntry);
        }

        // Bei Webservices werden alle Produkte zurückgeliefert, weil das Frontend entscheidet, welche Produkte angezeigt
        // werden sollen. Hat der Benutzer beide Eigenschaften, brauchen wir die Produkte nicht zu filtern
        if (!filter.isSessionWithGui() || filter.isCarAndTruckRightsInSession()) {
            return true;
        }

        return isProductVisibleForUserInSession(product);
    }

    /**
     * Check, ob das Produkt sichtbar ist für den Benutzer der aktuellen Session bzgl. der Benutzer-Eigenschaften OHNE
     * Berücksichtigung von PSK.
     *
     * @param project
     * @param productId
     * @return
     */
    public static boolean isProductVisibleForUserInSession(EtkProject project, iPartsProductId productId) {
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        return isProductVisibleForUserInSession(product);
    }

    /**
     * Überprüft, ob das Produkt zu den Eigenschaften des Benutzers der aktuellen Session passt
     *
     * @param product
     * @return
     */
    public static boolean isProductVisibleForUserInSession(iPartsProduct product) {
        return isProductVisibleForUserProperties(product, iPartsRight.checkCarAndVanInSession(), iPartsRight.checkTruckAndBusInSession());
    }

    /**
     * Überprüft, ob das Produkt zu den übergebenen Eigenschaften des Benutzers passt
     *
     * @param product
     * @param carAndVanInSession
     * @param truckAndBusInSession
     * @return
     */
    public static boolean isProductVisibleForUserProperties(iPartsProduct product, boolean carAndVanInSession, boolean truckAndBusInSession) {
        // Check, ob es ein PKW Produkt ist und der Benutzer die PKW Eigenschaft hat
        if (product.isCarAndVanProduct() && carAndVanInSession) {
            // Produkt passt zu den Eigenschaften
            return true;
        }

        // Check, ob es ein LKW Produkt ist und der Benutzer die LKW Eigenschaft hat
        // Produkt passt zu den Eigenschaften?
        return product.isTruckAndBusProduct() && truckAndBusInSession;
    }

    /**
     * Gekapseltes isModuleVisibleForUserSession, wenn man nur die Modulnummer besitzt
     *
     * @param project
     * @param isPSKAllowed
     * @param hasBothCarAndTruckRights
     * @param hasNeitherCarNorTruckRights
     * @param moduleNumber
     * @param productValidityMap
     * @return
     */
    public static boolean isModuleVisibleForUserSession(EtkProject project, boolean isPSKAllowed, boolean hasBothCarAndTruckRights,
                                                        boolean hasNeitherCarNorTruckRights, String moduleNumber,
                                                        HashMap<iPartsProductId, Boolean> productValidityMap) {
        iPartsAssemblyId assemblyId = new iPartsAssemblyId(moduleNumber, "");
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
            iPartsProductId productId = iPartsAssembly.getProductIdFromModuleUsage();
            iPartsDocumentationType documentationType = iPartsAssembly.getDocumentationType();
            return iPartsFilterHelper.isModuleVisibleForUserSession(project, documentationType, isPSKAllowed,
                                                                    hasBothCarAndTruckRights, hasNeitherCarNorTruckRights,
                                                                    moduleNumber, productId, productValidityMap);
        }
        return false;
    }

    /**
     * Überprüft, ob das Modul zu den Eigenschaften des Benutzers der aktuellen Session passt
     *
     * @param documentationType
     * @param isPSKAllowed
     * @param hasBothCarAndTruckRights
     * @param hasNeitherCarNorTruckRights
     * @param moduleNumber
     * @param productId
     * @param project
     * @param productValidityMap
     * @return
     */
    public static boolean isModuleVisibleForUserSession(EtkProject project, iPartsDocumentationType documentationType,
                                                        boolean isPSKAllowed, boolean hasBothCarAndTruckRights,
                                                        boolean hasNeitherCarNorTruckRights, String moduleNumber,
                                                        iPartsProductId productId, HashMap<iPartsProductId, Boolean> productValidityMap) {
        // PSK Produkte sollen immer angezeigt werden, wenn der Benutzer die PSK Rechte hat. Der Check auf Basis
        // der Produkte soll keinen Einfluss darauf haben. Deshalb der Check, ob es ein PSK Modul ist und falls
        // der Benutzer das PSK Recht hat, dann das Modul anzeigen. Hat er es nicht, Modul ausblenden.
        boolean valid = false;
        if (documentationType.isPSKDocumentationType() && isPSKAllowed) {
            valid = true;
        } else if (hasBothCarAndTruckRights) {
            // Keine weitere Prüfung notwendig, wenn der Benutzer beide Eigenschaften hat
            valid = true;
        } else if (!hasNeitherCarNorTruckRights || isPSKAllowed) { // Ohne eine der beiden Benutzer-Eigenschaften und ohne PSK-Recht brauchen wir gar nicht weitermachen
            // Ist es kein PSK Modul, entscheidet der Produkt und Rechte Check, ob das Modul angezeigt wird oder nicht.
            // Hat der Benutzer beide Eigenschaften, brauche wir die Module nicht zu filtern.
            if (productId == null) {
                AssemblyId assemblyId = new AssemblyId(moduleNumber, "");
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
                if (assembly instanceof iPartsDataAssembly) {
                    iPartsDataAssembly iPartsDataAssembly = (iPartsDataAssembly)assembly;
                    iPartsSAId saId = iPartsDataAssembly.getSAId();
                    if (saId != null) {
                        iPartsSA sa = iPartsSA.getInstance(project, saId);
                        // Ist die freie SA nur in PSK Produkten verortet, dann darf der Benutzer sie nur sehen, wenn er auch die PSK
                        // Eigenschaft hat. Hat er sie nicht, darf die freie SA nicht angezeigt werden.
                        if (sa.isOnlyInPSKProducts(project)) {
                            return isPSKAllowed;
                        }
                    }
                    // Ohne eine der beiden Benutzer-Eigenschaften brauchen wir bzgl. freier SAs gar nicht weitermachen
                    if (!hasNeitherCarNorTruckRights) {
                        // Ohne Produkt könnte es ein SA-TU sein -> SA-TU-Sichtbarkeit prüfen
                        valid = iPartsDataAssembly.checkSAVisibilityInSession(true, false);
                    }
                }
            } else if (productValidityMap.computeIfAbsent(productId, id -> iPartsProduct.getInstance(project, id).isProductVisibleForUserInSession())) {
                // Produkt vom Modul ist gültig für die Benuzter-Eigenschaften
                valid = true;
            }
        }
        return valid;
    }

    /**
     * Überprüft, ob die Quelle des Datenobjektes zu den Eigenschaften des Benutzers der aktuellen Session passt
     *
     * @param source
     * @return
     */
    public static boolean isDataObjectSourceValidForUserInSession(String source) {
        return isDataObjectSourceValidForUserProperties(source, iPartsRight.checkCarAndVanInSession(), iPartsRight.checkTruckAndBusInSession());
    }

    /**
     * Überprüft, ob die Quelle des Datenobjektes zu den übergebenen Eigenschaften des Benutzers passt
     *
     * @param source
     * @param carAndVanInSession
     * @param truckAndBusInSession
     * @return
     */
    public static boolean isDataObjectSourceValidForUserProperties(String source, boolean carAndVanInSession, boolean truckAndBusInSession) {
        return iPartsImportDataOrigin.isSourceVisible(source, carAndVanInSession, truckAndBusInSession);
    }

    /**
     * Überprüft, ob die freie SA zu den Eigenschaften des Benutzers der aktuellen Session passt
     *
     * @param saId
     * @param project
     * @return
     */
    public static boolean isSAVisibleForUserInSession(iPartsSAId saId, EtkProject project) {
        return isSAVisibleForUserInSession(iPartsSA.getInstance(project, saId), project);
    }

    /**
     * Überprüft, ob die freie SA zu den Eigenschaften des Benutzers der aktuellen Session passt
     *
     * @param sa
     * @param project
     * @return
     */
    public static boolean isSAVisibleForUserInSession(iPartsSA sa, EtkProject project) {
        // Check, ob es eine PKW SA ist und der Benutzer die PKW Eigenschaft hat
        if (sa.isCarAndVanSA(project) && iPartsRight.checkCarAndVanInSession()) {
            // Produkt passt zu den Eigenschaften
            return true;
        }

        // Check, ob es eine LKW SA ist und der Benutzer die LKW Eigenschaft hat
        // Produkt passt zu den Eigenschaften?
        return sa.isTruckAndBusSA(project) && iPartsRight.checkTruckAndBusInSession();
    }

    /**
     * Überprüft, ob ein AS-Baumuster zu den Eigenschaften des Benutzers der aktuellen Session passt
     *
     * @param modelNo
     * @param modelSource
     * @param carAndVanInSession
     * @param truckAndBusInSession
     * @param project
     * @return
     */
    public static boolean isASModelVisibleForUserInSession(String modelNo, String modelSource,
                                                           boolean carAndVanInSession, boolean truckAndBusInSession,
                                                           EtkProject project) {
        // Als erste prüfen, ob das BM nur in PSK Produkten hängt
        List<iPartsProduct> productsForModel = getValidProducts(project, modelNo);
        // null nur, wenn alle Produkte PSK und der Benutzer keine PSK Rechte hat
        if (productsForModel == null) {
            return false;
        }
        // Abkürzung falls beide Benutzer-Eigenschaften vorhanden sind
        if (carAndVanInSession && truckAndBusInSession) {
            return true;
        }

        boolean isSourceValid = iPartsImportDataOrigin.isSourceVisible(modelSource, carAndVanInSession, truckAndBusInSession);
        if (isSourceValid) {
            return true;
        }

        // Wenn die Quelle für den aktuellen Benutzer ungültig ist, soll geprüft werden, ob eines der Produkte für das Baumuster
        // für den Benutzer sichtbar ist. Ist das für eines der Produkte der Fall, dann soll der Datensatz angezeigt werden.
        // Keine Produkte? Dann Datensatz aufgrund ungültigter Quelle nicht anzeigen.
        if (productsForModel.isEmpty()) {
            return false;
        }
        // Beim ersten sichtbaren Produkt, den Datensatz anzeigen
        for (iPartsProduct product : productsForModel) {
            // Ist es ein PSK Produkt kann es hier nur vorkommen, wenn oben beim sammeln der Produkte der Benutzer PSK
            // Rechte hatte. Somit ist es gültig unabhängig von den PKW/Truck Eigenschaften
            if (product.isPSK()) {
                return true;
            }
            if (isProductVisibleForUserProperties(product, carAndVanInSession, truckAndBusInSession)) {
                return true;
            }
        }
        return false;
    }

    private static List<iPartsProduct> getValidProducts(EtkProject project, String modelNo) {
        List<iPartsProduct> productsForModel = iPartsProductHelper.getProductsForModel(project, new iPartsModelId(modelNo), null, null, false);
        if (!productsForModel.isEmpty()) {
            // Benutzer hat keine PSK Rechte -> PSK Produkte ausfiltern
            if (!iPartsRight.checkPSKInSession()) {
                productsForModel = productsForModel.stream().filter(product -> !product.isPSK()).collect(Collectors.toList());
                // Falls alle Produkte PSK Produkte sind, darf das BM nicht ausgegeben werden
                if (productsForModel.isEmpty()) {
                    return null;
                }
            }
        }
        return productsForModel;
    }

    /**
     * Führt den PSK Check durch. Besitzt der Benutzer die PSK-Eigenschaft, ist das PSK Produkt gültig. Ansonsten nicht.
     *
     * @param filter
     * @param partListEntry
     * @return
     */
    private static boolean doPSKProductCheck(iPartsFilter filter, iPartsDataPartListEntry partListEntry) {
        if (!iPartsRight.checkPSKInSession()) {
            filter.setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.PSK,
                                                   "!!Keine Rechte zum Anzeigen von PSK-Produkten");
            return false;
        }
        return true;
    }

    /**
     * Filtert den Konstruktionsknoten im Baum
     *
     * @param project
     * @param virtualNodesPath
     * @return
     */
    public static boolean filterConstructionNode(EtkProject project, List<iPartsVirtualNode> virtualNodesPath) {
        // DAIMLER-12069
        // Anwender ohne Recht "Stücklisten anzeigen" -> der gesamte Knoten für die konstr. Stückliste ist ausgeblendet
        if (!iPartsRight.VIEW_PARTS_DATA.checkRightInSession()) {
            return false;
        }

        // Konstruktion allgemein
        boolean showConstruction = project.isEditModeActive() && iPartsRight.EDIT_PARTS_DATA.checkRightInSession();
        // Ab Daimler-9386 gibt es ein neues Recht, das die Anzeige der konstruktiven Stückliste regelt
        if (!(showConstruction || iPartsRight.VIEW_CONSTRUCTION_PARTS_DATA.checkRightInSession())) {
            return false;
        }

        // Anwender mit Recht "Stücklisten anzeigen" ohne neue Eigenschaften ->  der gesamte Knoten für die konstr. Stückliste
        // ist ausgeblendet
        boolean truckAndBus = iPartsRight.checkTruckAndBusInSession();
        boolean carAndVan = iPartsRight.checkCarAndVanInSession();
        if (!truckAndBus && !carAndVan) {
            return false;
        }

        // Anwender mit "Stücklisten anzeigen" + Daimler Truck AG -> es werden die Truck-Knoten und Unterknoten angezeigt
        // (DS_Construction=EDS_MODEL OR MBS_MODEL OR CTT_MODEL)
        if (!truckAndBus && (iPartsVirtualNode.isMBSConstStructNode(virtualNodesPath)
                             || iPartsVirtualNode.isEDSConstStructNode(virtualNodesPath)
                             || iPartsVirtualNode.isCTTConstStructNode(virtualNodesPath))) {
            return false;
        }

        // Anwender mit "Stücklisten anzeigen" + Mercedes-Benz AG -> es werden die DIALOG-Knoten und Unterknoten angezeigt
        // (DS_Construction=DIALOG_SERIES)
        return carAndVan || !iPartsVirtualNode.isDIALOGConstStructNode(virtualNodesPath);
    }

    /**
     * Liefert valide Werke zu allen Produkten, die über die referenzierte Baureihe der Teileposition ermittelt wurden.
     * Die Werke eines Produkts werden nur herangezogen, wenn mind. ein Baumuster des Produkts die gleiche Ausführungsart
     * hat wie die Teileposition.
     *
     * @return
     */
    public static Set<String> getProductFactoriesForReferencedSeriesAndAA(EtkProject project, iPartsSeriesId seriesId, String aaValue) {
        List<iPartsProduct> productsWithReferencedSeries = iPartsProduct.getAllProductsForReferencedSeries(project, seriesId);
        Set<String> productFactories = new TreeSet<>();
        for (iPartsProduct product : productsWithReferencedSeries) {
            if (product.hasModelsWithAA(project, aaValue)) {
                productFactories.addAll(product.getProductFactories(project));
            }
        }
        return productFactories;
    }

    // Methode selbst nicht synchronized, da ObjectInstanceLRUList synchronized ist und die Methode wegen sehr häufigem
    // Aufruf nur möglichst kurz blockieren soll (falls Daten geladen werden müssen)
    public static Map<KgTuId, ModuleValidities> getKgTuModuleValidities(iPartsProductId productId, EtkProject project) {
        Map<KgTuId, ModuleValidities> kgTuModuleValidities = productKgTuToModelValiditiesCache.get(productId);
        if (kgTuModuleValidities == null) {
            kgTuModuleValidities = loadKgTuModuleValidities(productId, project);
            productKgTuToModelValiditiesCache.put(productId, kgTuModuleValidities);
        }
        return kgTuModuleValidities;
    }

    /**
     * Überprüft, ob der übergebene {@code kgTuNode} mit der {@link KgTuId} basierend auf dem übergebenen {@link iPartsFilter}
     * sichtbar sein soll oder nur leere Kind-Knoten enthalten würde, weswegen der {@code kgTuNode} gar nicht erst ausgegeben
     * werden soll.
     *
     * @param productId
     * @param modelNumbers
     * @param kgTuId
     * @param kgTuNode
     * @param filter
     * @param project
     * @return
     */
    public static boolean isKgTuNodeVisible(iPartsProductId productId, Set<String> modelNumbers, KgTuId kgTuId, iPartsCatalogNode kgTuNode,
                                            iPartsFilter filter, EtkProject project) {
        if (kgTuId.isEmpty()) {
            return false;
        }

        // Bei Requests OHNE FIN bzw. nicht geladener Datenkarte aber mit einem Baumuster prüfen, ob Baumuster-Filter aktiv
        // und Baumuster in der Datenkarte nicht leer ist (sollte bei validem IdentContext eigentlich immer so sein)
        boolean checkModelValidities = filter.isModelFilterActiveAndValid(null);

        // Prüfung der SAA- und Code-Gültigkeiten NUR MIT FIN und geladener Datenkarte, deshalb Prüfung auf "echte Datenkarte".
        // Zudem nur Prüfung, wenn Datenkarten-SA Filter aktiviert ist.
        AbstractDataCard dataCard = filter.getCurrentDataCard();
        boolean checkCodeSAAValidities = dataCard.isDataCardLoaded() && filter.isDatacardSaFilterActive(null);

        // Wenn keine Gültigkeiten geprüft werden müssen, dann ist der Knoten gültig
        if (!checkModelValidities && !checkCodeSAAValidities) {
            return true;
        }

        // Code und Baumuster NUR von den relevanten Datenkarten für die übergebenen Baumuster, die SAAs aber von allen Datenkarten
        if (modelNumbers == null) {
            modelNumbers = Collections.emptySet();
        }
        List<AbstractDataCard> relevantDataCards = dataCard.getFilterRelevantDatacardsForModels(project, modelNumbers);

        // SAAs aller relevanten Datenkarten aufsammeln
        Set<String> saasFromDataCard = new HashSet<>();
        for (AbstractDataCard relevantDataCard : relevantDataCards) {
            saasFromDataCard.addAll(filter.getSaasForDataCard(relevantDataCard));
        }

        if (kgTuId.isKgNode()) {
            if (kgTuNode == null) {
                return false;
            }

            // Alle TU- und SA-Knoten der KG überprüfen, ob mindestens einer davon sichtbar ist -> KG ist auch sichtbar
            Map<KgTuId, ModuleValidities> kgTuModuleValidities = null;
            Set<String> sasFromDataCard = null;
            for (iPartsCatalogNode tuCatalogNode : kgTuNode.getChildren()) {
                if (tuCatalogNode.getId() instanceof KgTuId) { // KG/TU
                    if (kgTuModuleValidities == null) {
                        kgTuModuleValidities = getKgTuModuleValidities(productId, project);
                    }

                    KgTuId tuId = (KgTuId)tuCatalogNode.getId();
                    if (isModuleValiditiesMatch(kgTuModuleValidities.get(tuId), relevantDataCards, saasFromDataCard, checkModelValidities,
                                                checkCodeSAAValidities)) {
                        return true;
                    }
                } else if (tuCatalogNode.getId() instanceof KgSaId) { // KG/SA
                    // Ähnlich zu iPartsFilter.isSaVisible(), aber simpler und schneller
                    if (sasFromDataCard == null) {
                        sasFromDataCard = iPartsFilter.retrieveSasFromSaas(saasFromDataCard);
                    }

                    KgSaId saId = (KgSaId)tuCatalogNode.getId();
                    if (sasFromDataCard.contains(saId.getSa())) {
                        return true;
                    }
                }
            }

            return false; // Kein TU oder SA ist sichtbar -> KG ist auch nicht sichtbar
        } else if (kgTuId.isTuNode()) {
            Map<KgTuId, ModuleValidities> kgTuModuleValidities = getKgTuModuleValidities(productId, project);
            return isModuleValiditiesMatch(kgTuModuleValidities.get(kgTuId), relevantDataCards, saasFromDataCard, checkModelValidities,
                                           checkCodeSAAValidities);
        }

        return false;
    }

    /**
     * Überprüft, ob der übergebene {@link iPartsCatalogNode} inkl. aller Kind-Knoten mit dem aktuellen Filter leer ist.
     *
     * @param catalogNode
     * @param project
     * @return
     */
    public static boolean isCatalogNodeEmptyWithFilter(iPartsCatalogNode catalogNode, EtkProject project) {
        for (iPartsCatalogNode childNode : catalogNode.getChildren()) {
            IdWithType childNodeId = childNode.getId();
            if ((childNodeId instanceof KgTuId) || (childNodeId instanceof KgSaId)) {
                if (!isCatalogNodeEmptyWithFilter(childNode, project)) {
                    return false;
                }
            } else if (childNodeId instanceof AssemblyId) {
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, (AssemblyId)childNodeId);
                if (!assembly.getPartList(assembly.getEbene()).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isModuleValiditiesMatch(ModuleValidities moduleValidities, List<AbstractDataCard> relevantDataCards,
                                                   Set<String> saasFromDataCard, boolean checkBMValidities, boolean checkCodeSAAValidities) {
        if ((moduleValidities == null) || moduleValidities.isEmpty()) {
            return true;
        }

        if (checkBMValidities) {
            // Baumuster-Gültigkeit
            if (!relevantDataCards.isEmpty()) {
                boolean modelValid = false;
                for (AbstractDataCard dataCard : relevantDataCards) {
                    Set<String> modelsFromDataCard = new HashSet<>(1);
                    modelsFromDataCard.add(dataCard.getModelNo());
                    if (isArrayValidityMatch(moduleValidities.modelValidities, modelsFromDataCard)) {
                        modelValid = true;
                        break;
                    }
                }
                if (!modelValid) {
                    return false;
                }
            }
        }

        if (checkCodeSAAValidities) {
            // SAA-Gültigkeiten aller Stücklisteneinträge
            if (!isArrayValidityMatch(moduleValidities.saaValidities, saasFromDataCard)) {
                return false;
            }

            // Code-Gültigkeit
            if (!relevantDataCards.isEmpty()) {
                boolean codeValid = false;
                for (AbstractDataCard dataCard : relevantDataCards) {
                    if (dataCard.getFilterCodes().getCheckedValues(false).isEmpty()) { // Gibt es technische Code?
                        // DAIMLER-12305 Bei fehlenden Code auf der Datenkarte muss die Gültigkeit true ergeben
                        codeValid = true;
                        break;
                    } else {
                        // Falls es technische Code gibt, dann die Gültigkeit prüfen
                        Set<String> codeFromDataCard = dataCard.getFilterCodes().getAllCheckedValues();
                        if (iPartsFilterHelper.basicCheckCodeFilterForDatacard(moduleValidities.codeValidity, codeFromDataCard)) {
                            codeValid = true;
                            break;
                        }
                    }
                }
                return codeValid;
            }
        }

        return true;
    }

    private static boolean isArrayValidityMatch(Set<String> arrayValidities, Set<String> validityFromDataCard) {
        // Code analog zu iPartsFilterHelper.checkSAStrichValidity()
        if ((arrayValidities == null) || arrayValidities.isEmpty()) {
            return true;
        }

        // DAIMLER-12305 Bei fehlenden Werten auf der Datenkarte muss die Gültigkeit true ergeben
        if (validityFromDataCard.isEmpty()) {
            return true;
        }

        for (String arrayValidity : arrayValidities) {
            if (validityFromDataCard.contains(arrayValidity)) {
                return true;
            }
        }

        return false;
    }

    private static Map<KgTuId, ModuleValidities> loadKgTuModuleValidities(iPartsProductId productId, EtkProject project) {
        // Alle Gültigkeiten aller Module des Produkts laden und pro KG/TU in einer Map ablegen
        iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
        Map<KgTuId, ModuleValidities> kgTuToModuleValiditiesMap = new HashMap<>();
        iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForProduct(project, productId);
        for (iPartsDataModuleEinPAS dataModuleEinPAS : moduleEinPASList) {
            String codeValidity = dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_CODE_VALIDITY);
            EtkDataArray modelValidityArray = dataModuleEinPAS.getFieldValueAsArrayOriginal(iPartsConst.FIELD_DME_MODEL_VALIDITY);
            modelValidityArray = EtkDataArray.getNullForEmptyArray(modelValidityArray);
            String saaValidities = dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SAA_VALIDITY);

            // Alle Gültigkeiten leer?
            if (DaimlerCodes.isEmptyCodeString(codeValidity) && (modelValidityArray == null) && saaValidities.isEmpty()) {
                continue;
            }

            String moduleNo = dataModuleEinPAS.getAsId().getModuleNumber();
            iPartsAssemblyId moduleId = new iPartsAssemblyId(moduleNo, "");
            if (productStructures.getModule(moduleId, project) != null) {
                KgTuId kgTuId = new KgTuId(dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG),
                                           dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU));
                if (kgTuId.isValidId()) {
                    ModuleValidities moduleValidities = kgTuToModuleValiditiesMap.computeIfAbsent(kgTuId, key -> new ModuleValidities());

                    // Code-Gültigkeiten der Module für den TU verodern
                    if (DaimlerCodes.isEmptyCodeString(codeValidity)) {
                        moduleValidities.codeValidity = ";"; // Leere Code-Gültigkeit ergibt verodert ";" als Code-Gültigkeit
                    } else if (!Utils.objectEquals(moduleValidities.codeValidity, ";")) { // ";" verodert mit irgendwas bleibt immer ";"
                        codeValidity = "(" + codeValidity + ")";
                        if (moduleValidities.codeValidity != null) {
                            moduleValidities.codeValidity += "/" + codeValidity;
                        } else {
                            moduleValidities.codeValidity = codeValidity;
                        }
                    }

                    // Baumuster-Gültigkeit
                    if (modelValidityArray != null) {
                        Set<String> modelValiditySet = moduleValidities.modelValidities;
                        if (modelValiditySet == null) {
                            modelValiditySet = new HashSet<>();
                            moduleValidities.modelValidities = modelValiditySet;
                        }
                        modelValiditySet.addAll(modelValidityArray.getArrayAsStringList());
                    }

                    // SAA-Gültigkeiten aller Stücklisteneinträge
                    if (!saaValidities.isEmpty()) {
                        Set<String> saaValiditiesSet = moduleValidities.saaValidities;
                        if (saaValiditiesSet == null) {
                            saaValiditiesSet = new HashSet<>();
                            moduleValidities.saaValidities = saaValiditiesSet;
                        }

                        // trim() nicht notwendig, weil Feldinhalt korrekt vom Programm erzeugt wurde
                        saaValiditiesSet.addAll(StrUtils.toStringList(saaValidities, iPartsDataAssembly.ARRAY_VALIDITIES_FOR_FILTER_DELIMITER,
                                                                      false, false));
                    }
                }
            }
        }

        return kgTuToModuleValiditiesMap;
    }


    /**
     * Endnummer als Integer inkl. Anzahl der Stellen die geprüft werden sollen
     * Wird z.B. bei Motor-Idents benötigt, da hier evtl. nur die letzten 6 Stellen des Idents verglichen werden sollen,
     * obwohl dieser eigentlich länger ist
     */
    private static class SerialNoWithLength {

        private int serialNo = FinId.INVALID_SERIAL_NUMBER;
        private int length = -1;

        private SerialNoWithLength(int serialNo, int length) {
            this.serialNo = serialNo;
            this.length = length;
        }

        private SerialNoWithLength(int serialNo) {
            this.serialNo = serialNo;
            this.length = -1;
        }

        private static SerialNoWithLength createInvalidSerialNo() {
            return new SerialNoWithLength(FinId.INVALID_SERIAL_NUMBER, -1);
        }

        private boolean isInvalidSerialNo() {
            return serialNo == FinId.INVALID_SERIAL_NUMBER;
        }

        private boolean hasLengthLimit() {
            return length != -1;
        }

        /**
         * kürzt die Endnummer auf die gespeicherte Länge
         *
         * @return letzte {@code length} Stellen der {@code serialNo} als Integer
         */
        private int getSerialNo() {
            if (length == -1) {
                return serialNo;
            }
            String serialStr = String.valueOf(serialNo);
            if (serialStr.length() <= length) {
                return serialNo;
            }
            serialStr = serialStr.substring(serialStr.length() - length);
            return Integer.parseInt(serialStr);
        }

        private int getLength() {
            return length;
        }

        /**
         * Kürzt die übergebene Endnummer auf die letzten {@code length} Stellen
         *
         * @param serialNo Ident
         * @param length   Länge
         * @return letzte {@code length} Stellen der {@code serialNo} als Integer
         */
        private static int getSerialWithLength(int serialNo, int length) {
            SerialNoWithLength tempSerialWithLength = new SerialNoWithLength(serialNo, length);
            return tempSerialWithLength.getSerialNo();
        }
    }


    /**
     * Container für Modul-Gültigkeiten
     */
    private static class ModuleValidities {

        public String codeValidity;
        public Set<String> modelValidities;
        public Set<String> saaValidities;

        public boolean isEmpty() {
            return DaimlerCodes.isEmptyCodeString(codeValidity) && ((modelValidities == null) || modelValidities.isEmpty())
                   && ((saaValidities == null) || saaValidities.isEmpty());
        }
    }
}