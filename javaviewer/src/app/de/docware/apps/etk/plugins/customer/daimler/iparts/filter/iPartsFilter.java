/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableToPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.datacard.DatacardFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.dialog.DIALOGModelFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.timeslice.iPartsFilterTimeSliceHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureNode;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.enums.EnumUtils;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;
import de.docware.util.misc.booleanfunctionparser.model.PositiveAndNegativeTerms;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.io.IOException;
import java.util.*;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard.FilterTypes.*;


/**
 * Methodik abgeschaut von DraegerMtFilter
 */
public class iPartsFilter {

    // Unterfilter des Baumusterfilters
    public enum ModelFilterTypes {
        STEERING,       // Lenkung
        CODE,           // Codefilterung
        SA,             // komplette SA-Filterung, also mit Navigation und in Stücklisten. Kann für freie SAAs mit dem SA_STRICH ausgeschalten werden
        SA_STRICH,      // Keine SA-Strich Filterung in den freien SAs
        AA,             // Ausführungsart
        OMITTED_PART,   // Entfallteile
        MODEL,          // Baumustervalidity-Filter
        TIME_SLICE;     // Zeitscheibenfilter

        public static EnumSet<ModelFilterTypes> all() {
            return EnumSet.allOf(ModelFilterTypes.class);
        }

        public static EnumSet<ModelFilterTypes> forPictures() {
            return EnumSet.of(CODE, MODEL, SA);
        }
    }

    private static final int MAX_CACHE_SIZE_ASSEMBLY_FILTER_CACHE = 100;

    private static final Set<String> EMPTY_STRING_HASHSET = Collections.unmodifiableSet(new HashSet<>());

    // Sofern in den beiden folgenden Matrizen bei beiden jeweils null (für immer gültig) drinsteht, wird in den entsprechenden
    // Filtern aus Performancegründen auch nicht explizit nach dem Aggregatetyp der relevanten Datenkarten gefiltert.

    // Matrix für die gültigen Aggregatetypen pro Filter bei DIALOG-Stücklisten (null bedeutet immer gültig)
    private static final Map<iPartsFilterSwitchboard.FilterTypes, DCAggregateTypes[]> VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP = new HashMap<>();

    static {
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(MODEL, null);
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(END_NUMBER, new DCAggregateTypes[]{ DCAggregateTypes.VEHICLE,
                                                                                     DCAggregateTypes.ENGINE,
                                                                                     DCAggregateTypes.TRANSMISSION,
                                                                                     DCAggregateTypes.TRANSFER_CASE,
                                                                                     DCAggregateTypes.AXLE,
                                                                                     DCAggregateTypes.CAB,
                                                                                     DCAggregateTypes.AFTER_TREATMENT_SYSTEM,
                                                                                     DCAggregateTypes.ELECTRO_ENGINE,
                                                                                     // Alles außer FUEL_CELL
                                                                                     DCAggregateTypes.HIGH_VOLTAGE_BATTERY,
                                                                                     DCAggregateTypes.STEERING,
                                                                                     DCAggregateTypes.EXHAUST_SYSTEM,
                                                                                     DCAggregateTypes.PLATFORM });
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(SPECIAL_ZB_NUMBER, new DCAggregateTypes[]{ DCAggregateTypes.ENGINE,
                                                                                            DCAggregateTypes.HIGH_VOLTAGE_BATTERY });
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(DATACARD_SA, null);
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(SA_STRICH, null);
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(STEERING, null);
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(GEARBOX, null);
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(SPRING, new DCAggregateTypes[]{ DCAggregateTypes.VEHICLE });
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(DATACARD_CODE, null);
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(EXTENDED_CODE, null);
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(EXTENDED_COLOR, new DCAggregateTypes[]{ DCAggregateTypes.VEHICLE });
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(PSK_VARIANTS, null);
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(COUNTRY_VALIDITY_FILTER, null);
        VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.put(SPECIFICATION_FILTER, null);
    }

    // Matrix für die gültigen Aggregatetypen pro Filter bei ELDAS-Stücklisten (null bedeutet immer gültig)
    private static final Map<iPartsFilterSwitchboard.FilterTypes, DCAggregateTypes[]> VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP = new HashMap<>();

    static {
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(MODEL, null);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(END_NUMBER, new DCAggregateTypes[]{ DCAggregateTypes.VEHICLE,
                                                                                    DCAggregateTypes.ENGINE,
                                                                                    DCAggregateTypes.CAB,
                                                                                    DCAggregateTypes.AFTER_TREATMENT_SYSTEM,
                                                                                    DCAggregateTypes.PLATFORM });
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(SPECIAL_ZB_NUMBER, new DCAggregateTypes[]{ DCAggregateTypes.ENGINE,
                                                                                           DCAggregateTypes.HIGH_VOLTAGE_BATTERY });
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(DATACARD_SA, null);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(SA_STRICH, null);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(STEERING, null);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(GEARBOX, null);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(SPRING, new DCAggregateTypes[0]);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(DATACARD_CODE, null);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(EXTENDED_CODE, new DCAggregateTypes[0]);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(EXTENDED_COLOR, new DCAggregateTypes[0]);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(PSK_VARIANTS, null);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(COUNTRY_VALIDITY_FILTER, null);
        VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.put(SPECIFICATION_FILTER, null);
    }

    private static final String[] FIELDS_FOR_DUPLICATE_CHECK = new String[]{ EtkDbConst.FIELD_K_POS, EtkDbConst.FIELD_K_MATNR,
                                                                             iPartsConst.FIELD_K_STEERING, iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED,
                                                                             iPartsConst.FIELD_K_WW,
                                                                             iPartsConst.FIELD_K_EVENT_FROM, iPartsConst.FIELD_K_EVENT_TO };
    //iPartsConst.FIELD_K_AA schon in Dialog Positionsvarianten enthalten

    private static final Set<String> ACTIVE_FILTER_FIELDS_KATALOG = new HashSet<>();

    static {
        // K_SOURCE_TYPE und K_SOURCE_GUID brauchen wir immer
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SOURCE_TYPE));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SOURCE_GUID));
        // Materialnummer auch, ist aber wahrscheinlich eh immer drin
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_MATNR));
        // K_POS wird für Module benötigt, die relevant sind für den Feder-Filter
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_POS));
        // K_ONLY_MODEL_FILTER brauchen wir auch immer, um erkennen zu können, ob ein Stücklisteneintrag nur im
        // Baumuster-Filter verwendet werden soll
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_ONLY_MODEL_FILTER));

        // Für den Baumuster-Filter
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_AA));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_MODEL_VALIDITY));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SA_VALIDITY));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_STEERING));

        // Für den Datenkarten-SA-Filter
//        activeFilterFieldsKatalog.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SA_VALIDITY)); // schon für den Baumuster-Filter

        // Für den Datenkarten-Code-Filter
//        activeFilterFieldsKatalog.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES)); // schon für den Baumuster-Filter
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES_REDUCED));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVENT_FROM));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVENT_TO));

        // Für den Lenkungs-Filter
//        activeFilterFieldsKatalog.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_STEERING)); // schon für den Baumuster-Filter

        // Für den Getriebe-Filter
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_GEARBOX_TYPE));

        // Für den Verdichtungs-Filter
        for (String field : FIELDS_FOR_DUPLICATE_CHECK) {
            if (!VirtualFieldsUtils.isVirtualField(field)) {
                ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, field));
            }
        }
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_DATEFROM));

        // Für den PRIMUS-Nachfolger
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_USE_PRIMUS_SUCCESSOR));

        // Für DIALOG-Ersetzungen, falls diese nachgeladen werden müssen
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_MIN_KEM_DATE_FROM));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_MAX_KEM_DATE_TO));

        // Für den Entfallpositions-Filter
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_OMIT));

        // Diese beiden Felder werden für das virtuelle Feld RETAIL_CODES_REDUCED benötigt
//        activeFilterFieldsKatalog.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES)); // schon für den Baumuster-Filter
//        activeFilterFieldsKatalog.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES_REDUCED)); // schon für den Baumuster-Filter

        // Für die Verdichtung mit Mengenaddition
        // Virtuelle Felder wie RETAIL_COMB_TEXT müssen hier nicht angegeben werden, weil sie ja sowieso berechnet werden
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_MENGE));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_FAIL_LOCLIST));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_WW));

        // Für den Endnummern-Filter
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVAL_PEM_FROM));
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVAL_PEM_TO));

        // Für den Ländergültigkeits-Filter
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_COUNTRY_VALIDITY));

        // Für den Spezifikations-Filter
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SPEC_VALIDITY));

        // Für den Produktklassen-Filter
        ACTIVE_FILTER_FIELDS_KATALOG.add(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_PCLASSES_VALIDITY));
    }

    private static final Set<String> ACTIVE_FILTER_FIELDS_MAT = new HashSet<>();

    static {
        // Für die Leitungssatzbaukästen
        ACTIVE_FILTER_FIELDS_MAT.add(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_LAYOUT_FLAG));
        ACTIVE_FILTER_FIELDS_MAT.add(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_ETKZ));

        // Für die Gleichteile
        ACTIVE_FILTER_FIELDS_MAT.add(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_BASE_MATNR));
        ACTIVE_FILTER_FIELDS_MAT.add(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR_MBAG));
        ACTIVE_FILTER_FIELDS_MAT.add(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR_DTAG));
    }


    private static final Set<String> ACTIVE_FILTER_FIELDS_IMAGES = new HashSet<>();

    static {
        ACTIVE_FILTER_FIELDS_IMAGES.add(TableAndFieldName.make(EtkDbConst.TABLE_IMAGES, iPartsConst.FIELD_I_CODES));
        ACTIVE_FILTER_FIELDS_IMAGES.add(TableAndFieldName.make(EtkDbConst.TABLE_IMAGES, iPartsConst.FIELD_I_MODEL_VALIDITY));
        ACTIVE_FILTER_FIELDS_IMAGES.add(TableAndFieldName.make(EtkDbConst.TABLE_IMAGES, iPartsConst.FIELD_I_SAA_CONSTKIT_VALIDITY));
    }

    public static final EnumSet<iPartsFilterSwitchboard.FilterTypes> VALID_ELDAS_AGGREGATE_IN_DIALOG_VEHICLE_FILTER_TYPES = EnumSet.of(iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                                                                                                       iPartsFilterSwitchboard.FilterTypes.END_NUMBER,
                                                                                                                                       iPartsFilterSwitchboard.FilterTypes.REMOVE_DUPLICATES,
                                                                                                                                       iPartsFilterSwitchboard.FilterTypes.AGG_MODELS,
                                                                                                                                       iPartsFilterSwitchboard.FilterTypes.DATACARD_CODE);

    /**
     * Die gecachten FilterEntries des Moduls (gilt nur für identische parentAssembly).
     * Die Filterentries sind die mitzubeachtenden Entries (Positionsvarianten oder Entries mit gleichem Hotspot) zu einem Entry.
     */
    public static class FilterCachedPartsEntries {

        private AssemblyId parentAssemblyId;
        private Map<PartListEntryId, iPartsFilterPartsEntries> cachedPartEntries;

        public FilterCachedPartsEntries() {
            parentAssemblyId = AssemblyId.getRootId();
            cachedPartEntries = new HashMap<>();
        }

        public synchronized iPartsFilterPartsEntries getCachedPartsEntries(iPartsDataPartListEntry partListEntry) {
            return cachedPartEntries.get(partListEntry.getAsId());
        }

        public synchronized void addCachedPartsEntries(iPartsFilterPartsEntries entries) {
            for (Collection<iPartsDataPartListEntry> positionsVariants : entries.getAllPositionsVariants()) {
                for (iPartsDataPartListEntry entry : positionsVariants) {
                    add(entry, entries);
                }
            }
        }

        private void add(iPartsDataPartListEntry entry, iPartsFilterPartsEntries entries) {
            if (!entry.getOwnerAssembly().getAsId().equals(parentAssemblyId)) {
                // Wir sind in einer anderen Baugruppe, den Cache können wir löschen, da der Cache immer nur für das aktuelle Modul gehalten werden soll
                parentAssemblyId = entry.getOwnerAssembly().getAsId();
                cachedPartEntries = new HashMap<>();
            }
            cachedPartEntries.put(entry.getAsId(), entries);
        }
    }


    /**
     * Die gecachten Werte eines Moduls.
     */
    public static class FilterCachedAssemblyData {

        private final EtkProject project;
        private final boolean isRetailPartList;
        private final Map<String, Boolean> partListEntriesValidForEndNumber; // Für den Endnummernfilter gültige Stücklisteneinträge (Map von kLfdnr auf Boolean)
        private List<AbstractDataCard> relevantDatacards;
        private boolean isEldasAggregateInDIALOGVehicle;
        private Set<iPartsFilterSwitchboard.FilterTypes> disabledFilters; // am Produkt deaktivierte Filter
        private final boolean isSeriesFromProductForModuleEventControlled; // Ist die Baureihe zum Produkt am Modul ereignisgesteuert?
        private iPartsDialogSeries referencedSeries; // Das verknüpfte Baureihen-Cache-Objekt

        public FilterCachedAssemblyData(iPartsDataAssembly assembly) {
            project = assembly.getEtkProject();
            isRetailPartList = assembly.isRetailPartList();
            partListEntriesValidForEndNumber = new HashMap<>();
            isSeriesFromProductForModuleEventControlled = assembly.isSeriesFromProductModuleUsageEventControlled();

            iPartsProductId productIdFromModuleUsage = assembly.getProductIdFromModuleUsage();
            if (productIdFromModuleUsage != null) {
                disabledFilters = iPartsFilterHelper.getDisabledFilters(iPartsProduct.getInstance(assembly.getEtkProject(),
                                                                                                  productIdFromModuleUsage));
                iPartsProduct product = iPartsProduct.getInstance(project, productIdFromModuleUsage);
                if (product.getReferencedSeries() != null) {
                    referencedSeries = iPartsDialogSeries.getInstance(project, product.getReferencedSeries());
                }
            }
        }

        public boolean isSeriesFromProductForModuleEventControlled() {
            return isSeriesFromProductForModuleEventControlled;
        }

        public int getEventSeqNo(String eventId) {
            if (referencedSeries != null) {
                return referencedSeries.getEventOrdinal(eventId);
            }
            return -1;
        }

        public List<AbstractDataCard> getRelevantDatacards() {
            return relevantDatacards;
        }

        public void setRelevantDatacards(List<AbstractDataCard> relevantDatacards) {
            if (this.relevantDatacards == relevantDatacards) {
                return;
            }

            this.relevantDatacards = relevantDatacards;

            isEldasAggregateInDIALOGVehicle = false;
            if (isRetailPartList && (relevantDatacards != null)) {
                for (AbstractDataCard relevantDatacard : relevantDatacards) {
                    if (relevantDatacard instanceof AggregateDataCard) {
                        if (((AggregateDataCard)relevantDatacard).isEldasAggregateInDIALOGVehicle(project)) {
                            isEldasAggregateInDIALOGVehicle = true;
                            break;
                        }
                    }
                }
            }
        }

        public boolean isFilterDisabled(iPartsFilterSwitchboard.FilterTypes filterType, iPartsDocumentationType documentationType) {
            // Ist der Filter am Produkt deaktiviert oder handelt es sich um ein ELDAS-Aggregat in einem DIALOG-Produkt?
            // (dort sind nur alle nicht-sichtbaren Filter zulässig sowie die in eldasAggregateInDIALOGVehicleFilterTypes
            // aufgelisteten)
            return ((disabledFilters != null) && disabledFilters.contains(filterType))
                   || (isEldasAggregateInDIALOGVehicle && documentationType.isTruckDocumentationType() && filterType.isVisible()
                       && !VALID_ELDAS_AGGREGATE_IN_DIALOG_VEHICLE_FILTER_TYPES.contains(filterType));
        }
    }

    /**
     * Die gecachten Werte des Baumusters. Gilt nur für das parentAssembly, da bei anderen Assemblies ein anderes Baumuster gemeint sein kann
     */
    public class FilterCachedModelData {

        private final long productModelsValidFrom;
        private long productModelsValidTo;
        private final String steering;
        private final AssemblyId parentAssemblyId;
        private final Set<String> saas;
        private final Set<String> saModules;
        private Set<String> modelBuildingCodeSetWithAAFromDataCards;
        private Set<String> modelBuildingCodeSetWithOutAAFromDataCards;

        public FilterCachedModelData(iPartsDataAssembly parentAssembly, String modelNo) {
            parentAssemblyId = parentAssembly.getAsId();
            iPartsDataProductModels dataProductModels = null;
            //bei iPartsDataAssembly: Product über ModuleUsage bestimmen
            iPartsProductId partsProductId = parentAssembly.getProductIdFromModuleUsage();
            EtkProject project = parentAssembly.getEtkProject();
            if (partsProductId != null) {
                //jetzt überprüfen, ob es eine Product-Models Beziehung gibt
                dataProductModels = iPartsProductModels.getInstance(project).getProductModelsByModelAndProduct(project, modelNo,
                                                                                                               partsProductId.getProductNumber());

            }

            iPartsModelId modelId = new iPartsModelId(modelNo);
            productModelsValidFrom = StrUtils.strToLongDef(iPartsProductModelHelper.getValidFromValue(project, dataProductModels, modelId), 0);
            productModelsValidTo = StrUtils.strToLongDef(iPartsProductModelHelper.getValidToValue(project, dataProductModels, modelId), 0);
            if (productModelsValidTo == 0) { // Beim Enddatum muss mit Long.MAX_VALUE gerechnet werden für unendlich anstatt mit 0
                productModelsValidTo = Long.MAX_VALUE;
            }

            if (dataProductModels != null) {
                steering = dataProductModels.getFieldValue(iPartsConst.FIELD_DPM_STEERING);
            } else {
                steering = "";
            }

            // SAAs diese Baumusters laden
            iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNo));
            saas = new HashSet<>();
            saas.addAll(model.getSaas(project));

            saModules = retrieveSasFromSaas(saas);

            // SAAs von Datenkarte und Baumuster können sich unterscheiden. Damit hier nichts weggefiltert wird, was später
            // im SA-Strich-Filter noch erhalten bleiben würde, werden die SAAs und freien SAs zusammengemischt (DAIMLER-4271)
            for (AbstractDataCard dataCard : getRelevantDatacardsForAssembly(parentAssembly)) {
                saas.addAll(dataCard.getSaasForFilter().getAllCheckedValues());
                saModules.addAll(dataCard.getDataCardSaNumbers());
            }
        }


        public long getProductModelsValidFrom() {
            return productModelsValidFrom;
        }

        public long getProductModelsValidTo() {
            return productModelsValidTo;
        }

        public String getSteering() {
            return steering;
        }

        public AssemblyId getParentAssemblyId() {
            return parentAssemblyId;
        }


        /**
         * Die in diesem Baumuster verwendeten SAAs (SA-Strich)
         *
         * @return
         */
        public Set<String> getSaas() {
            return saas;
        }

        /**
         * Die in diesem Baumuster verwendeten SAs. Also alle SAAs ohne die Strichausfühung (letzten 2 Stellen)
         *
         * @return
         */
        public Set<String> getSaModules() {
            return saModules;
        }

        /**
         * Alle tatsächlichen baumusterbildenden Code von allen relevanten Datenkarten für dieses Baumuster
         *
         * @param withAAModelBuildingCode
         * @return
         */
        public Set<String> getModelBuildingCodeSetFromDataCards(boolean withAAModelBuildingCode) {
            if (withAAModelBuildingCode) {
                return modelBuildingCodeSetWithAAFromDataCards;
            } else {
                return modelBuildingCodeSetWithOutAAFromDataCards;
            }
        }
    }

    /**
     * Zusammenfassung aller gecachten Daten -> cachedData = new FilterCachedData(); löscht den Cache
     */
    private class FilterCachedData {

        private boolean hideEmptyTUs;
        private final Map<String, DIALOGModelFilter> dialogModelWithAAModelBuildingCodeFilterMap = Collections.synchronizedMap(new HashMap<>());
        private final Map<String, DIALOGModelFilter> dialogModelWithoutAAModelBuildingCodeFilterMap = Collections.synchronizedMap(new HashMap<>());
        private final ObjectInstanceStrongLRUList<AssemblyId, FilterCachedAssemblyData> assemblyDataMap =
                new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_ASSEMBLY_FILTER_CACHE,
                                                  iPartsConst.MAX_CACHE_LIFE_TIME_CORE);
        private final Map<String, FilterCachedModelData> modelDataMap = Collections.synchronizedMap(new HashMap<>());
        private FilterCachedPartsEntries partsEntries;
        private final Map<AssemblyId, Set<String>> relevantModelNumbersForAssembly = Collections.synchronizedMap(new HashMap<>());
        private final Map<AssemblyId, Boolean> assemblyHasSubAssemblies = Collections.synchronizedMap(new HashMap<>());
        private final Map<AbstractDataCard, Set<String>> saasForDataCard = Collections.synchronizedMap(new HashMap<>()); // Cache für alle SAAs zu einer Datenkarte
        private final Map<iPartsSeriesId, Boolean> seriesToTTZFilterMap = Collections.synchronizedMap(new HashMap<>());
        private final Map<iPartsProductId, iPartsCatalogNode> productToKgTuRootNodeMap = Collections.synchronizedMap(new HashMap<>());
        private final Map<iPartsProductId, Boolean> productIsSpecialCatalogMap = Collections.synchronizedMap(new HashMap<>());

        public FilterCachedData() {
            // Leere Stücklisten nur in der GUI prüfen, um bei den Webservices die Performance nicht zu verschlechtern
            if (isSessionWithGui()) {
                EtkProject project = JavaViewerApplication.getInstance().getProject();
                if (project != null) {
                    hideEmptyTUs = iPartsUserSettingsHelper.isHideEmptyTUs(project);
                }
            }
        }

        public boolean isHideEmptyTUs() {
            return hideEmptyTUs;
        }

        Boolean hasSubAssemblies(AssemblyId assemblyId) {
            return assemblyHasSubAssemblies.get(assemblyId);
        }

        void setHasSubAssemblies(AssemblyId assemblyId, boolean hasSubAssemblies) {
            assemblyHasSubAssemblies.put(assemblyId, hasSubAssemblies);
        }

        DIALOGModelFilter getDialogModelFilter(EtkProject project, String modelNo, boolean withAAModelBuildingCode) {
            Map<String, DIALOGModelFilter> destFilterMap;
            if (withAAModelBuildingCode) {
                destFilterMap = dialogModelWithAAModelBuildingCodeFilterMap;
            } else {
                destFilterMap = dialogModelWithoutAAModelBuildingCodeFilterMap;
            }
            DIALOGModelFilter dialogModelFilter = destFilterMap.get(modelNo);
            if (dialogModelFilter == null) {
                dialogModelFilter = new DIALOGModelFilter(modelNo, withAAModelBuildingCode);
                dialogModelFilter.createModelBuildingCodeSets(project);
                destFilterMap.put(modelNo, dialogModelFilter);
            }
            return dialogModelFilter;
        }

        DIALOGModelFilter getDialogModelFilter(EtkProject project, String modelNo) {
            return getDialogModelFilter(project, modelNo, true);
        }

        FilterCachedAssemblyData getAssemblyData(iPartsDataAssembly assembly) {
            FilterCachedAssemblyData assemblyData = assemblyDataMap.get(assembly.getAsId());
            if (assemblyData == null) {
                assemblyData = new FilterCachedAssemblyData(assembly);
                assemblyDataMap.put(assembly.getAsId(), assemblyData);
            }

            return assemblyData;
        }

        FilterCachedModelData getModelData(iPartsDataAssembly assembly, String modelNo) {
            FilterCachedModelData result = modelDataMap.get(modelNo);
            if ((result == null) || !result.parentAssemblyId.equals(assembly.getAsId())) {
                result = new FilterCachedModelData(assembly, modelNo);
                modelDataMap.put(modelNo, result);
            }
            return result;
        }

        iPartsFilterPartsEntries getCachedPartsEntries(iPartsDataPartListEntry partListEntry) {
            if (partsEntries != null) {
                return partsEntries.getCachedPartsEntries(partListEntry);
            }
            return null;
        }

        List<AbstractDataCard> getRelevantDatacards(iPartsDataAssembly assembly) {
            FilterCachedAssemblyData assemblyData = getAssemblyData(assembly);
            List<AbstractDataCard> relevantDatacards = assemblyData.getRelevantDatacards();
            if (relevantDatacards == null) {
                relevantDatacards = currentDataCard.getRelevantDatacardsForAssembly(assembly);
                assemblyData.setRelevantDatacards(relevantDatacards);
            }

            return relevantDatacards;
        }

        void addCachedPartsEntries(iPartsFilterPartsEntries entries) {
            if (partsEntries == null) {
                partsEntries = new FilterCachedPartsEntries();
            }
            partsEntries.addCachedPartsEntries(entries);
        }


        public Set<String> getSaasForDataCard(AbstractDataCard dataCard) {
            Set<String> saas = saasForDataCard.get(dataCard);
            if (saas == null) {
                saas = dataCard.getSaasForFilter().getAllCheckedValues();
                saasForDataCard.put(dataCard, saas);
            }
            return saas;
        }

        /**
         * Liefert alle tatsächlichen baumusterbildenden Code von allen relevanten Datenkarten für das übergebene Baumuster
         * in dem angegebenen Modul.
         *
         * @param assembly
         * @param modelNumber
         * @return
         */
        public Set<String> getModelBuildingCodeSetFromDataCards(iPartsDataAssembly assembly, String modelNumber, boolean withAAModelBuildingCode) {
            FilterCachedModelData modelData = getModelData(assembly, modelNumber);
            Set<String> modelBuildingCodeSetFromDataCards = modelData.getModelBuildingCodeSetFromDataCards(withAAModelBuildingCode);
            if (modelBuildingCodeSetFromDataCards == null) {
                modelBuildingCodeSetFromDataCards = new HashSet<>();
                List<String> models = new DwList<>(1);
                models.add(modelNumber);
                List<AbstractDataCard> relevantDatacards = getRelevantDatacardsForModels(assembly.getEtkProject(), models);
                for (AbstractDataCard relevantDatacard : relevantDatacards) {
                    // Nur tatsächlich ausgewählte baumusterbildenden Code (aus dem topGrid) verwenden
                    modelBuildingCodeSetFromDataCards.addAll(relevantDatacard.getCodes().getCheckedValues(true));
                }
                if (withAAModelBuildingCode) {
                    modelData.modelBuildingCodeSetWithAAFromDataCards = modelBuildingCodeSetFromDataCards;
                } else {
                    iPartsModel model = iPartsModel.getInstance(assembly.getEtkProject(), new iPartsModelId(modelNumber));
                    modelBuildingCodeSetFromDataCards.remove(model.getAusfuehrungsArt());
                    modelData.modelBuildingCodeSetWithOutAAFromDataCards = modelBuildingCodeSetFromDataCards;
                }
            }
            return modelBuildingCodeSetFromDataCards;
        }


        public Set<String> getModelBuildingCodeSetFromDataCards(iPartsDataAssembly assembly, String modelNumber) {
            return getModelBuildingCodeSetFromDataCards(assembly, modelNumber, true);
        }

        public Set<String> getRelevantModelNumbers(iPartsDataAssembly assembly) {
            Set<String> result = relevantModelNumbersForAssembly.get(assembly.getAsId());
            if (result == null) {
                result = new LinkedHashSet<>();
                List<AbstractDataCard> dataCards = getRelevantDatacardsForAssembly(assembly);
                for (AbstractDataCard dataCard : dataCards) {
                    result.add(dataCard.getModelNo());
                }
                relevantModelNumbersForAssembly.put(assembly.getAsId(), result);
            }
            return result;
        }

        /**
         * Soll die übergebene Baureihe mit TTZ gefiltert werden?
         *
         * @param seriesId
         * @param project
         * @return
         */
        public boolean isTTZFilterForSeries(iPartsSeriesId seriesId, EtkProject project) {
            if (seriesId == null) {
                return false;
            }

            return seriesToTTZFilterMap.computeIfAbsent(seriesId, series -> {
                List<iPartsProduct> products = iPartsProduct.getAllProductsForReferencedSeries(project, series);
                for (iPartsProduct product : products) {
                    if (product.isTtzFilter()) {
                        return true;
                    }
                }

                return false;
            });
        }

        public iPartsCatalogNode getKgTuRootNode(iPartsProductId productId, EtkProject project) {
            return productToKgTuRootNodeMap.computeIfAbsent(productId, productIdKey -> {
                iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productIdKey);
                iPartsProduct product = iPartsProduct.getInstance(project, productIdKey);
                return productStructures.getKgTuStructureWithoutCarPerspective(project, product.isStructureWithAggregates());
            });
        }

        public boolean isSpecialCatalog(iPartsProductId productId, EtkProject project) {
            return productIsSpecialCatalogMap.computeIfAbsent(productId, productIdKey -> {
                iPartsProduct product = iPartsProduct.getInstance(project, productIdKey);
                return product.isSpecialCatalog();
            });
        }
    }

    private static final String SESSION_ATTRIB_IPARTS_FILTER = "iPartsFilter";

    private final boolean isSessionWithGUI;
    private final boolean carAndTruckRightsInSession;
    private iPartsFilterSwitchboard switchboardState;
    private final EnumSet<ModelFilterTypes> activeModelSubFilters;
    private FilterCachedData cachedData;
    private AbstractDataCard currentDataCard;
    private boolean isMultiLayerFilter;
    private boolean withFilterReason;
    private String filterValueProductClass;
    private boolean ignoreLooseSaConfiguration;
    private boolean configOptionIgnoreInvalidFactories;
    private boolean configOptionShowSAsOnlyForFin;
    // An Bildern kann ein Kenner gesetzt werden, dass die Bilder nur bei einer echten (geladenen) Datenkarte angezeigt
    // werden sollen. Mit filterImagesWithOnlyFINFlag kann gesteuert werden, ob diese Prüfung durchgeführt werden soll
    private boolean filterImagesWithOnlyFINFlag;
    private final iPartsEqualPartsHelper equalPartsHelper = new iPartsEqualPartsHelper();
    private iPartsOmittedParts omittedPartsCache;
    private iPartsWireHarness wireHarnessCache;

    public static iPartsFilter get() {
        Session session = Session.get();
        if (session == null) { // Kann z.B. im Wartungsmodus passieren
            return new iPartsFilter();
        }

        iPartsFilter filter = (iPartsFilter)session.getAttribute(SESSION_ATTRIB_IPARTS_FILTER);
        if (filter == null) {
            filter = new iPartsFilter();
            session.setAttribute(SESSION_ATTRIB_IPARTS_FILTER, filter);
        }
        return filter;
    }

    public static void disableAllFilters() {
        try {
            iPartsFilter filter = iPartsFilter.get();
            filter.setAllRetailFilterActiveForDataCard(null, null, false);
            filter.setProductClassFilterActive(false);
            filter.setIgnoreLooseSaConfiguration(false);
            filter.setFilterImagesWithOnlyFINFlag(true);
        } catch (RuntimeException e) {
            // Beim Zugriff auf die DWK oder etk_viewer.config können IOExceptions auftreten, wenn der aktuelle Thread
            // z.B. aufgrund einer lang laufenden und abgebrochenen Suche bereits beendet wurde
            if (!(e.getCause() instanceof IOException)) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            }
        }
    }


    public static void clearCache() {
        iPartsFilterHelper.clearCache();

        // dialogModelFilter in allen GUI-Sessions auf null setzen
        for (Session session : SessionManager.getInstance().getSessionsPerSessionIdCopy().values()) {
            if (session.canHandleGui()) {
                iPartsFilter filter = (iPartsFilter)session.getAttribute(SESSION_ATTRIB_IPARTS_FILTER);
                if (filter != null) {
                    filter.clearCacheData();
                }
            }
        }
    }

    public static Set<String> retrieveSasFromSaas(Set<String> saaNumbers) {
        // Aus den SAAs gültige freie SAs für das Baumuster bestimmen
        Set<String> saNumbers = new HashSet<>();
        for (String saaNumber : saaNumbers) {
            saNumbers.add(StrUtils.copySubString(saaNumber, 0, saaNumber.length() - 2)); // letzten beiden Stellen sicher abschneiden
        }
        return saNumbers;
    }


    /**
     * Konstruktor mit Initialisierungen
     */
    public iPartsFilter() {
        switchboardState = new iPartsFilterSwitchboard();
        activeModelSubFilters = ModelFilterTypes.all();
        currentDataCard = new VehicleDataCard(true);
        ignoreLooseSaConfiguration = false;
        filterImagesWithOnlyFINFlag = true;
        Session session = Session.get();
        isSessionWithGUI = (session != null) && session.canHandleGui();
        carAndTruckRightsInSession = iPartsRight.checkUserHasBothVehicleTypeRightsInSession();
        clearCacheData();
    }

    /**
     * Gibt an, ob die Konfigurationseinstellung (Admin-Modus) für freie SAs ignoriert werden soll
     *
     * @param ignoreConfiguration
     */
    public void setIgnoreLooseSaConfiguration(boolean ignoreConfiguration) {
        this.ignoreLooseSaConfiguration = ignoreConfiguration;
    }

    public boolean isIgnoreLooseSaConfiguration() {
        return ignoreLooseSaConfiguration;
    }

    public boolean isFilterImagesWithOnlyFINFlag() {
        return filterImagesWithOnlyFINFlag;
    }

    public void setFilterImagesWithOnlyFINFlag(boolean filterImagesWithOnlyFINFlag) {
        this.filterImagesWithOnlyFINFlag = filterImagesWithOnlyFINFlag;
    }

    /**
     * Vermutlich temporäre Konfig option mit der sich die Logik im Baumuster-Zeitscheiben- und Endnummern-Filter
     * bzgl. der Behandling ungültiger Werke steuern lässt.
     * Wenn die option <code>true</code> ist, werden Werksdaten zu ungültigen Werken (nicht für das jeweilige Produkt
     * gültig) ignoriert, also so behandelt als ob sie nicht existieren.
     * Wenn es nur Werksdaten zu ungültigen Werken gibt wird der Eintrag so behandelt als ob es keine Werkseinsatzdaten gibt
     * Zugehöriges Ticket: DAIMLER-6366
     *
     * @return
     */
    public boolean ignoreInvalidFactories() {
        return configOptionIgnoreInvalidFactories;
    }

    public boolean isShowSAsOnlyForFin() {
        return configOptionShowSAsOnlyForFin;
    }

    public iPartsFilterSwitchboard getSwitchboardState() {
        return switchboardState;
    }

    public void setSwitchboardState(iPartsFilterSwitchboard switchboardState) {
        this.switchboardState = switchboardState;
        setMultiLayerFiltering(isAggModelsFilterActive());
    }

    public void setCurrentDataCard(AbstractDataCard currentDataCard, EtkProject project) {
        if (currentDataCard == null) {
            currentDataCard = new VehicleDataCard(true);
        }
        if (!Utils.objectEquals(currentDataCard, getCurrentDataCard())) {
            clearCacheData();
        }
        this.currentDataCard = currentDataCard;

        if (iPartsEqualPartsHelper.SHOW_EQUAL_PARTS) {
            equalPartsHelper.setProducts(getProductListForDataCard(project, currentDataCard, false));
        }
    }

    public AbstractDataCard getCurrentDataCard() {
        if (currentDataCard == null) {
            currentDataCard = new VehicleDataCard(true);
        }
        return currentDataCard;
    }

    private VehicleDataCard getVehicleDataCard(boolean includeParentVehicleDataCard) {
        if (currentDataCard.isVehicleDataCard()) {
            return (VehicleDataCard)currentDataCard;
        } else if (includeParentVehicleDataCard && currentDataCard.isAggregateDataCard()) {
            return ((AggregateDataCard)currentDataCard).getParentDatacard();
        }
        return null;
    }

    private AggregateDataCard getAggregateDataCard() {
        if (currentDataCard.isAggregateDataCard()) {
            return (AggregateDataCard)currentDataCard;
        }
        return null;
    }

    /**
     * Aktiviert nur den Baumuster-Filter und die Filterung mit Filtergrund z.B. für Qualitätsprüfungen.
     *
     * @return Das iPartsFilterSwitchboard mit dem aktivierten Baumuster-Filter
     */
    public iPartsFilterSwitchboard activateOnlyModelFilterWithFilterReason() {
        // Nur den Baumuster-Filter aktivieren
        final iPartsFilterSwitchboard filterForModelEvaluationSwitchboard = new iPartsFilterSwitchboard();
        filterForModelEvaluationSwitchboard.setMainSwitchActive(true);
        filterForModelEvaluationSwitchboard.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.MODEL, true);
        setSwitchboardState(filterForModelEvaluationSwitchboard);

        // Filterung mit Filtergrund durchführen, damit die Stückliste im ungefilterten Zustand bleibt und keine Elemente
        // entfernt werden
        setWithFilterReason(true);

        return filterForModelEvaluationSwitchboard;
    }

    /**
     * Erzeugt und setzt eine virtuelle Baumuster-Datenkarten für das übergebene Baumuster.
     *
     * @param modelNo
     * @param project
     * @return Die Erzeugte Baumuster-Datenkarte
     */
    public AbstractDataCard setDataCardByModel(String modelNo, EtkProject project) {
        AbstractDataCard dataCard = AbstractDataCard.createModelDatacardByModelType(project, modelNo);
        setCurrentDataCard(dataCard, project);
        return dataCard;
    }

    /**
     * Ermitteln zum gegebenen Stücklisteneintrag alle relevanten Baumuster
     *
     * @param assembly
     * @return
     */
    public Set<String> getRelevantModelNumbers(iPartsDataAssembly assembly) {
        return cachedData.getRelevantModelNumbers(assembly);
    }

    public void clearCacheData() {
        omittedPartsCache = null;
        wireHarnessCache = null;
        cachedData = new FilterCachedData();
        if (currentDataCard != null) {
            currentDataCard.clearCache();
        }
        configOptionIgnoreInvalidFactories = iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_IGNORE_INVALID_FACTORIES_IN_FILTER);
        configOptionShowSAsOnlyForFin = iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_SHOW_SAS_ONLY_FOR_FIN);
    }

    public iPartsOmittedParts getOmittedPartsCache(EtkProject project) {
        iPartsOmittedParts omittedPartsCacheLocal = omittedPartsCache;
        if (omittedPartsCacheLocal == null) {
            omittedPartsCacheLocal = iPartsOmittedParts.getInstance(project);
            omittedPartsCache = omittedPartsCacheLocal;
        }
        return omittedPartsCacheLocal;
    }

    public iPartsWireHarness getWireHarnessCache(EtkProject project) {
        iPartsWireHarness wireHarnessCacheLocal = wireHarnessCache;
        if (wireHarnessCacheLocal == null) {
            wireHarnessCacheLocal = iPartsWireHarness.getInstance(project);
            wireHarnessCache = wireHarnessCacheLocal;
        }
        return wireHarnessCacheLocal;
    }

    public Set<String> getDatacardSaNumbersForAllRelevantDatacards(EtkProject project, Collection<String> models) {
        Set<String> result = new TreeSet<>();
        for (AbstractDataCard dataCard : getRelevantDatacardsForModels(project, models)) {
            result.addAll(dataCard.getDataCardSaNumbers());
        }
        return result;
    }

    /**
     * Filterwert für die AS-Produktklassen-Gültigkeit bei Spezial-Produkten.
     *
     * @return
     */
    public String getFilterValueProductClass() {
        return filterValueProductClass;
    }

    /**
     * Filterwert für die AS-Produktklassen-Gültigkeit bei Spezial-Produkten.
     *
     * @param filterValueProductClass
     */
    public void setFilterValueProductClass(String filterValueProductClass) {
        this.filterValueProductClass = filterValueProductClass;
    }

    /**
     * Überprüft, ob das übergebene Modul mit den aktuellen Filtereinstellungen sichtbar ist (also nicht ausgeblendet wird).
     *
     * @param assembly
     * @return
     */
    public boolean isModuleVisible(iPartsDataAssembly assembly) {
        // Ist der Filter-Hauptschalter aktiv und wird das Modul ausgeblendet?
        if (switchboardState.isMainSwitchActive()) {
            if ((assembly != null) && !assembly.getAsId().isVirtual() && assembly.getModuleMetaData().getFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Überprüft, ob die übergebene freie SA mit den aktuellen Filtereinstellungen sichtbar ist.
     *
     * @param project
     * @param saNumber
     * @param productId
     * @return
     */
    public boolean isSaVisible(EtkProject project, String saNumber, iPartsProductId productId) {
        // Check, ob die Ausgabe der freien SAs schon über die Konfiguration verhindert wird
        if (!iPartsFilterHelper.checkConfigurationForLooseSas(this)) {
            // Bei bestimmten Produkten sollen die freien SAs doch ausgegeben werden
            if ((productId == null) || !productId.isValidId() || !iPartsProduct.getInstance(project, productId).showLooseSasWhileFiltering()) {
                return false;
            }
        }
        // Nicht sichtbar falls Baumuster-Filterung oder Datenkarten SA-Filterung aktiv und SA nicht in den Baumustern bzw.
        // nicht auf der Datenkarte enthalten
        Set<String> models = currentDataCard.getFilterModelNumbers(project);
        if (isDatacardSaFilterActive(null)) {
            return getDatacardSaNumbersForAllRelevantDatacards(project, models).contains(saNumber); // Ist auf Datenkarte?
        } else if (isModelFilterActive(null)) {
            if (getDatacardSaNumbersForAllRelevantDatacards(project, models).contains(saNumber)) { // Ist auf Datenkarte?
                return true;
            } else {
                // SAs in den Baumustern überprüfen (FilterCachedModelData kann hier nicht verwendet werden, weil wir hier
                // keine Assembly haben)
                for (String modelNumber : models) {
                    iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNumber));
                    if (retrieveSasFromSaas(model.getSaas(project)).contains(saNumber)) {
                        return true;
                    }
                }
            }

            return false; // SA weder auf Datenkarte noch in den Baumustern enthalten
        }

        return true;
    }

    /**
     * Ist der Baumuster-Filter aktiv und auch zulässig (also ein Baumuster in der Datenkarte gesetzt)?
     *
     * @return
     */
    public boolean isModelFilterActiveAndValid(iPartsDataAssembly assembly) {
        // Baumuster-Filter macht nur Sinn bei gesetztem Baumuster in der Datenkarte
        return isModelFilterActive(assembly) && iPartsModel.isModelNumberValid(currentDataCard.getFilterModelNo());
    }

    public boolean isModelFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(MODEL, assembly);
    }

    public void setModelFilterActive(boolean active) {
        switchboardState.setFilterActivated(MODEL, active);
        if (!active) {
            clearCacheData();
        }
    }

    public boolean isSaStrichFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(SA_STRICH, assembly);
    }

    public void setSaStrichFilterActive(boolean active) {
        switchboardState.setFilterActivated(SA_STRICH, active);
    }


    public boolean isDatacardCodeFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(DATACARD_CODE, assembly);
    }

    public void setDatacardCodeFilterActive(boolean active) {
        switchboardState.setFilterActivated(DATACARD_CODE, active);
    }

    public void setOmittedPartsFilterActive(boolean active) {
        switchboardState.setFilterActivated(OMITTED_PARTS, active);
    }

    public boolean isOmittedPartsFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(OMITTED_PARTS, assembly);
    }

    public void setOmittedPartListEntriesFilterActive(boolean active) {
        switchboardState.setFilterActivated(OMITTED_PART_LIST_ENTRIES, active);
    }

    public boolean isOmittedPartListEntriesFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(OMITTED_PART_LIST_ENTRIES, assembly);
    }

    public void setWireHarnessFilterActive(boolean active) {
        switchboardState.setFilterActivated(WIRE_HARNESS, active);
    }

    public boolean isWireHarnessFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(WIRE_HARNESS, assembly);
    }

    public void setOnlyModelFilterActive(boolean active) {
        switchboardState.setFilterActivated(ONLY_MODEL_FILTER, active);
    }

    public boolean isOnlyModelFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(ONLY_MODEL_FILTER, assembly);
    }

    public boolean isAggModelsFilterActive() {
        return switchboardState.isFilterActivated(AGG_MODELS);
    }

    public void setAggModelsFilterActive(boolean active) {
        switchboardState.setFilterActivated(AGG_MODELS, active);
        setMultiLayerFiltering(active);
    }

    public boolean isSteeringFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(STEERING, assembly);
    }

    public void setSteeringFilterActive(boolean active) {
        switchboardState.setFilterActivated(STEERING, active);
    }

    public boolean isGearboxFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(GEARBOX, assembly);
    }

    public void setGearboxFilterActive(boolean active) {
        switchboardState.setFilterActivated(GEARBOX, active);
    }

    public boolean isDatacardSaFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(DATACARD_SA, assembly);
    }

    public void setDatacardSaFilterActive(boolean active) {
        switchboardState.setFilterActivated(DATACARD_SA, active);
    }

    public boolean isEndNumberFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(END_NUMBER, assembly);
    }

    public void setEndNumberFilterActive(boolean active) {
        switchboardState.setFilterActivated(END_NUMBER, active);
    }

    public boolean isSpringFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(SPRING, assembly);
    }

    public void setSpringFilterActive(boolean active) {
        switchboardState.setFilterActivated(SPRING, active);
    }

    public boolean isExtendedCodesFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(EXTENDED_CODE, assembly);
    }

    public void setExtendedCodeFilterActive(boolean active) {
        switchboardState.setFilterActivated(EXTENDED_CODE, active);
    }

    public boolean isExtendendColorFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(EXTENDED_COLOR, assembly);
    }

    public void setExtendedColorFilterActive(boolean active) {
        switchboardState.setFilterActivated(EXTENDED_COLOR, active);
    }

    public boolean isPSKVariantsFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(PSK_VARIANTS, assembly);
    }

    public void setPSKVariantsFilterActive(boolean active) {
        switchboardState.setFilterActivated(PSK_VARIANTS, active);
    }

    public boolean isCountryValidityFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(COUNTRY_VALIDITY_FILTER, assembly);
    }

    public void setCountryValidityFilterActive(boolean active) {
        switchboardState.setFilterActivated(COUNTRY_VALIDITY_FILTER, active);
    }

    public boolean isSpecificationFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(SPECIFICATION_FILTER, assembly);
    }

    public void setSpecificationFilterActive(boolean active) {
        switchboardState.setFilterActivated(SPECIFICATION_FILTER, active);
    }

    public boolean isRemoveDuplicatesFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(REMOVE_DUPLICATES, assembly);
    }

    public void setRemoveDuplicatesActive(boolean active) {
        switchboardState.setFilterActivated(REMOVE_DUPLICATES, active);
    }

    public boolean isProductClassFilterActive(iPartsDataAssembly assembly) {
        return isFilterActive(AS_PRODUCT_CLASS, assembly);
    }

    public void setProductClassFilterActive(boolean active) {
        switchboardState.setFilterActivated(AS_PRODUCT_CLASS, active);
    }

    public boolean isFilterActive() {
        return switchboardState.isAnyActivated();
    }

    /**
     * Aktiviert oder deaktiviert einen Unterfilter des Baumusterfilters
     *
     * @param modelFilterType
     * @param active
     */
    public void setModelSubFilterActive(ModelFilterTypes modelFilterType, boolean active) {
        if (active) {
            activeModelSubFilters.add(modelFilterType);
        } else {
            activeModelSubFilters.remove(modelFilterType);
        }
    }

    public void setMultiLayerFiltering(boolean enabled) {
        if (isMultiLayerFilter != enabled) {
            isMultiLayerFilter = enabled;
            clearCache();
        }
    }

    public boolean isMultiLayerFiltering() {
        return isMultiLayerFilter;
    }

    public boolean isFilterActive(iPartsFilterSwitchboard.FilterTypes filterType, iPartsDataAssembly assembly) {
        if (assembly == null) {
            // wenn keine Assembly angegeben ist, den generellen Schalterzustand verwenden
            return switchboardState.isFilterActivated(filterType);
        } else {
            if (switchboardState.isFilterActivated(filterType)) {
                // PSK-Varianten-Filter abhängig davon machen, ob das Modul auch ein PSK-Modul ist
                if ((filterType == PSK_VARIANTS) && !assembly.isPSKAssembly()) {
                    return false;
                }

                // Grundsätzlich ist der Filter aktiv, aber er könnte bei Retail-Stücklisten noch am Produkt deaktiviert sein
                return !assembly.isRetailPartList() || !cachedData.getAssemblyData(assembly).isFilterDisabled(filterType, assembly.getDocumentationType());
            } else {
                return false;
            }
        }
    }

    /**
     * Produkte über die aktuelle Datenkarte bestimmen und als Liste zurückgeben.
     *
     * @param project
     * @param dataCard
     * @param withAPS
     * @return Wird nichts gefunden, wird eine leere Liste zurückgegeben.
     */
    public List<iPartsProduct> getProductListForDataCard(EtkProject project, AbstractDataCard dataCard, boolean withAPS) {
        // Produkte über die Datenkarte bestimmen und DP_DISABLED_FILTERS berücksichtigen.
        List<iPartsProduct> products;
        if ((project != null) && (dataCard != null)) {
            iPartsModelId modelFromDataCard = new iPartsModelId(dataCard.getModelNo());
            // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster sollen beim Edit aber nicht bei den
            // Webservices berücksichtigt werden.
            products = iPartsProductHelper.getProductsForModelAndSessionType(project, modelFromDataCard, null,
                                                                             iPartsProductHelper.PRODUCTS_FOR_SERIES_MODE.FILTER_ALL);

            // Auto-Product-Select
            if (withAPS && (dataCard instanceof VehicleDataCard)) {
                VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                products = iPartsFilterHelper.getAutoSelectProductsForFIN(project, products, vehicleDataCard.getFinId(),
                                                                          new iPartsModelId(vehicleDataCard.getModelNo()),
                                                                          vehicleDataCard.getCodes().getAllCheckedValues());
            }
        } else {
            products = new DwList<>();
        }
        return products;
    }

    /**
     * Alle sinnvollen Einzelfilter für die übergebene Datenkarte (de-)aktivieren und die Datenkarte für die Filterung verwenden
     * (z.B. für Webservices)
     *
     * @param project
     * @param dataCard     Datenkarte für die Filterung
     * @param filterActive
     */
    public void setAllRetailFilterActiveForDataCard(EtkProject project, AbstractDataCard dataCard, boolean filterActive) {
        setCurrentDataCard(dataCard, project);

        // Alle Filter deaktivieren, die eine echte geladene Datenkarte benötigen, wenn die Datenkarte nicht geladen werden konnte
        boolean dataCardLoaded = getCurrentDataCard().isDataCardLoaded();
        setModelFilterActive(filterActive);
        setSaStrichFilterActive(filterActive);
        setDatacardCodeFilterActive(filterActive && dataCardLoaded);
        setAggModelsFilterActive(filterActive);
        setSteeringFilterActive(filterActive);
        setGearboxFilterActive(filterActive && dataCardLoaded);
        setRemoveDuplicatesActive(filterActive);
        setDatacardSaFilterActive(filterActive && dataCardLoaded);

        // datacard.isValidForEndNumberFilter() kann an dieser Stelle mit der Einführung der TTZ-Produkte nicht mehr bestimmt werden
        // und hat vielleicht auch früher nicht gestimmt, weil eingebettete Aggregate gar nicht berücksichtigt wurden.
        setEndNumberFilterActive(filterActive);

        setSpringFilterActive(filterActive && dataCardLoaded);
        setExtendedCodeFilterActive(filterActive && dataCardLoaded);
        setExtendedColorFilterActive(filterActive && dataCardLoaded);

        // Ländergültigkeits-Filter nur aktivieren, falls ein Land in der Fahrzeugdatenkarte gesetzt wurde
        setCountryValidityFilterActive(filterActive && (dataCard instanceof VehicleDataCard) && StrUtils.isValid(((VehicleDataCard)dataCard).getCountry()));

        // Spezifikations-Filter nur aktivieren, falls eine Spezifikation in der Fahrzeugdatenkarte gesetzt wurde
        setSpecificationFilterActive(filterActive && (dataCard instanceof VehicleDataCard) && Utils.isValid(((VehicleDataCard)dataCard).getSpecValidities(project)));

        setFilterMainSwitchActive(filterActive);
    }

    /**
     * Alle sinnvollen Einzelfilter für die übergebene Datenkarte (de-)aktivieren und die Datenkarte für die Filterung verwenden
     * für Spezial-Produkte.
     *
     * @param project
     * @param dataCard     Datenkarte für die Filterung
     * @param filterActive
     */
    public void setAllRetailFilterActiveForSpecialProduct(EtkProject project, AbstractDataCard dataCard, boolean filterActive) {
        setCurrentDataCard(dataCard, project);

        setProductClassFilterActive(filterActive);

        // Alle Filter deaktivieren, die eine echte geladene Datenkarte benötigen, wenn die Datenkarte nicht geladen werden konnte
        boolean dataCardLoaded = getCurrentDataCard().isDataCardLoaded();
        setDatacardCodeFilterActive(filterActive && dataCardLoaded);

        // Ländergültigkeits-Filter nur aktivieren, falls ein Land in der Fahrzeugdatenkarte gesetzt wurde
        setCountryValidityFilterActive(filterActive && dataCardLoaded && (dataCard instanceof VehicleDataCard) && StrUtils.isValid(((VehicleDataCard)dataCard).getCountry()));

        // Spezifikations-Filter nur aktivieren, falls eine Spezifikation in der Fahrzeugdatenkarte vorhanden ist
        setSpecificationFilterActive(filterActive && dataCardLoaded && (dataCard instanceof VehicleDataCard) && Utils.isValid(((VehicleDataCard)dataCard).getSpecValidities(project)));

        setFilterMainSwitchActive(filterActive);
    }

    /**
     * (De-)aktiviert den kompletten Filter ("Hauptschalter").
     *
     * @param filterActive
     */
    public void setFilterMainSwitchActive(boolean filterActive) {
        switchboardState.setMainSwitchActive(filterActive);
    }

    private iPartsAssemblyId getFilterAssemblyId(iPartsDataPartListEntry partListEntry) {
        // Bei der (Verwendungs-)Suche ist die kLfdnr null und in diesem Fall muss die OwnerAssemblyId anstatt die
        // DestinationAssemblyId überprüft werden
        if (partListEntry.getAsId().getKLfdnr() == null) {
            return partListEntry.getOwnerAssemblyId();
        } else {
            return partListEntry.getDestinationAssemblyId();
        }
    }

    /**
     * Durchführung einer Filterung
     *
     * @param etkDataObject Datensatz, der gefiltert werden soll
     * @return {@code true}, falls keine Filterung durchgeführt wurde; {@code false}, wenn der Datensatz ausgefiltert wurde
     */
    public boolean checkFilter(EtkDataObject etkDataObject) {
        setMultiLayerFiltering(isAggModelsFilterActive()); // mehrstufige Filterung über den Aggregate-Filter aktivieren

        if (etkDataObject instanceof iPartsDataPartListEntry) {
            // Konstruktions-Knoten, Konstruktions-Stücklisten und PSK-Produkte filtern
            iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)etkDataObject;
            EtkProject project = partListEntry.getEtkProject();
            iPartsAssemblyId ownerAssemblyId = partListEntry.getOwnerAssemblyId();
            if (ownerAssemblyId.isVirtual()) {
                // Flag für Fahrzeug-Produkte mit Aggregaten vom virtuellen Knoten muss mit dem Flag der aktuellen Session übereinstimmen
                boolean productStructureWithAggregatesForSession = iPartsProduct.isProductStructureWithAggregatesForSession();
                List<iPartsVirtualNode> virtualNodesPath = iPartsVirtualNode.parseVirtualIds(ownerAssemblyId);
                if (!checkProductStructureWithAggregatesForVirtualNodePath(virtualNodesPath, productStructureWithAggregatesForSession)) {
                    return false;
                }

                iPartsAssemblyId destinationAssemblyId = getFilterAssemblyId(partListEntry);
                if (destinationAssemblyId.isVirtual()) {
                    List<iPartsVirtualNode> destVirtualNodesPath = iPartsVirtualNode.parseVirtualIds(destinationAssemblyId);

                    // Flag für Produkte mit Aggregaten vom virtuellen Knoten muss mit dem Flag der aktuellen Session übereinstimmen
                    if (!checkProductStructureWithAggregatesForVirtualNodePath(destVirtualNodesPath, productStructureWithAggregatesForSession)) {
                        return false;
                    }

                    boolean filterConstruction = false;
                    if (iPartsVirtualNode.isStructureNode(destVirtualNodesPath)) { // Konstruktions-Knoten in der Struktur
                        iPartsStructureId structureId = new iPartsStructureId(destVirtualNodesPath.get(0).getId().getValue(1));
                        iPartsStructureNode structureNode = iPartsStructure.getInstance(etkDataObject.getEtkProject()).findNodeInAllChilds(structureId);
                        if (structureNode != null) {
                            // Falls es ein Konstruktions-Knoten ist, bisherige Logik anwenden (Konstruktion wird ja
                            // weiter unten gefiltert). Ansonsten Check, ob der Strukturknoten angezeigt werden darf
                            if (structureNode.isConstructionNode()) {
                                filterConstruction = true;
                            } else if (isSessionWithGui()  // Filterung nur bei GUI Sessions durchführen
                                       && !iPartsFilterHelper.isStructureNodeWithProductsVisible(this, partListEntry, structureNode)) {
                                return false;
                            }
                        }
                    } else if (iPartsVirtualNode.isConstructionRelevant(destVirtualNodesPath)) { // Echte Konstruktions-Knoten
                        filterConstruction = true;
                    } else if (iPartsVirtualNode.isProductNode(destVirtualNodesPath)) {
                        // (PSK-)Produkte, zu denen die Eigenschaften des Benutzers nicht passen, ausfiltern (PKW/Van, Truck/Bus und PSK)
                        String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(destinationAssemblyId);
                        if (StrUtils.isValid(productNumber)) {
                            // Check, ob der Knoten für die aktuellen Eigenschaften gültig ist
                            iPartsProductId productId = new iPartsProductId(productNumber);
                            if (!iPartsFilterHelper.isProductNodeVisible(this, partListEntry, productId)) {
                                return false;
                            }
                        }
                    } else if (!isKgTuOrModuleVisible(destVirtualNodesPath, destinationAssemblyId, destinationAssemblyId,
                                                      project)) {
                        if (isWithFilterReason()) {
                            setFilterReasonForPartListEntry(partListEntry, TU_VALIDITIES_FILTER, "!!Kein TU ist für die Datenkarte gültig in diesem KG/TU");
                        }
                        return false;
                    }

                    // Ohne GUI (also z.B. in Hintergrund-Jobs) die Konstruktions-Knoten nicht filtern
                    if (filterConstruction && isSessionWithGui() && !iPartsFilterHelper.filterConstructionNode(etkDataObject.getEtkProject(),
                                                                                                               destVirtualNodesPath)) {
                        return false;
                    }
                } else if (!isKgTuOrModuleVisible(virtualNodesPath, ownerAssemblyId, destinationAssemblyId, project)) {
                    if (isWithFilterReason()) {
                        setFilterReasonForPartListEntry(partListEntry, TU_VALIDITIES_FILTER, "!!TU ist für die Datenkarte nicht gültig");
                    }
                    return false;
                }
            }

            // Bei aktiviertem Grund für die Ausfilterung die virtuellen Felder vorab immer zunächst leer befüllen, damit
            // sie vorhanden sind
            if (withFilterReason) {
                clearFilterReasonForDataObject(partListEntry, false);
            }

            // unabhängig davon, ob irgendwelche Filter aktiviert sind, sollen EinPAS Knoten standardmäßig ausgefiltert werden
            Boolean showEinPASMapping = Session.get().getAttribute(iPartsPlugin.SESSION_KEY_SHOW_EINPAS_MAPPING, false);
            if (!showEinPASMapping) {
                List<iPartsVirtualNode> path = partListEntry.getVirtualNodesPathForDestinationAssembly();
                if (iPartsVirtualNode.isEinPASConstructionNode(path)) {
                    return false;
                }

                // In der (Verwendungs-)Suche kann es vorkommen, dass man hier mit einem PartlistEntry reinkommt, das eigentlich
                // in einem EinPAS Knoten liegt. Daher muss hier die eigentliche ID und nicht nur die DestinationAssembly
                // geprüft werden
                if (ownerAssemblyId.isVirtual()) {
                    path = iPartsVirtualNode.parseVirtualIds(ownerAssemblyId);
                    if (iPartsVirtualNode.isEinPASConstructionNode(path)) {
                        return false;
                    }
                }
            }

            // Ist das Modul ausgeblendet?
            iPartsDataAssembly assemblyToCheckForHiddenModule = getAssemblyToCheckForHiddenModule(partListEntry);
            if (!isModuleVisible(assemblyToCheckForHiddenModule)) {
                if (isWithFilterReason()) {
                    setFilterReasonForPartListEntry(partListEntry, MODULE_HIDDEN, "!!SA/TU ist ausgeblendet");
                }
                return false;
            }

            // Ist der HM/M/SM-Knoten ausgeblendet? Inkl. Performance-Verbessung, weil es sich nur dann um einen HM/M/SM-Knoten
            // handeln kann, wenn assemblyToCheckForHiddenModule null ist (es sich also nicht um eine Retail-Stückliste handelt)
            if ((assemblyToCheckForHiddenModule == null) && !filterHiddenHmMSmNode(partListEntry)) {
                if (isWithFilterReason()) {
                    setFilterReasonForPartListEntry(partListEntry, HMMSM_HIDDEN, "!!HM/M/SM-Knoten ist ausgeblendet");
                }
                return false;
            }

            iPartsDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
            // Negative Zeitintervalle von allen Werkseinsatzdaten des Moduls korrigieren
            correctNegativeTimeIntervalsForFactoryData(ownerAssembly);

            // Ist der Filter aktiv und handelt es sich nicht um eine Konstruktions-Stückliste?
            if (isFilterActive() && !ownerAssembly.isConstructionRelevantAssembly()) {
                // Check, ob es sich um Navigations-TU handelt
                boolean isNavigationModule = EditModuleHelper.isCarPerspectiveAssembly(ownerAssembly);
                boolean isRetailPartList = !isNavigationModule && ownerAssembly.isRetailPartList();
                if (isRetailPartList) {
                    boolean otherEqualPartNumber = setEqualPartNumber(partListEntry);

                    // PRIMUS-Nachfolger verwenden (aber nicht im Edit)?
                    if (!otherEqualPartNumber && isRemoveDuplicatesFilterActive(ownerAssembly) && !ownerAssembly.isEditMode()) {
                        partListEntry.replaceMaterialByPRIMUSSuccessorIfNeeded(this);
                    }
                }
                if (isNavigationModule) {
                    // Das Navigationsmodul hat nur spezifische Filter. Hier also eine Abzweigung um nicht alle Filter
                    // zu durchlaufen
                    if (!filterNavigationModuleEntry(ownerAssembly, partListEntry)) {
                        return false;
                    }
                } else if (!partListEntry.isSpecialProductEntry()) {
                    iPartsFilterPartsEntries filterEntries = cachedData.getCachedPartsEntries(partListEntry);

                    if (filterEntries == null) {
                        // Im Cache sind noch keine filterEntries -> ermittele die für diesen Entry mit zu betrachtenden anderen Stücklisteneinträge
                        // und filtere alle. Am Ende das Filterergebnis in den Cache
                        filterEntries = iPartsFilterPartsEntries.getInstance(partListEntry);
                        // Die Ausgabe von freien SAs kann über die Admin-Konfiguration und Produkteigenschaften gesteuert werden.
                        // Hier wird überprüft, ob es sich um eine freie SA handelt und ob sie angezeigt werden darf.
                        iPartsFilterHelper.checkShowLooseSas(this, partListEntry, filterEntries);
                        filterPartsEntries(filterEntries);

                        if (isRetailPartList) {
                            // Wegfallsachnummern aufräumen
                            if (isOmittedPartsFilterActive(ownerAssembly)) {
                                iPartsOmittedParts omittedPartsCacheLocal = getOmittedPartsCache(project);
                                for (iPartsDataPartListEntry visibleEntry : filterEntries.getVisibleEntries()) {
                                    if (!iPartsFilterHelper.basicCheckOmittedPartFilter(visibleEntry, omittedPartsCacheLocal, this)) {
                                        filterEntries.hideEntry(visibleEntry);
                                    }
                                }
                            }

                            // Entfallpositionen entfernen
                            if (isOmittedPartListEntriesFilterActive(ownerAssembly)) {
                                for (iPartsDataPartListEntry visibleEntry : filterEntries.getVisibleEntries()) {
                                    if (!iPartsFilterHelper.basicCheckOmittedPartListEntryFilter(visibleEntry, this)) {
                                        filterEntries.hideEntry(visibleEntry);
                                    }
                                }
                            }
                        }

                        // Bei aktiviertem Grund für die Ausfilterung bei allen sichtbaren Stücklisteneinträge auf jeden Fall
                        // den Filtergrund wieder entfernen
                        if (withFilterReason) {
                            for (iPartsDataPartListEntry visiblePartListEntry : filterEntries.getVisibleEntries()) {
                                clearFilterReasonForDataObject(visiblePartListEntry, true);
                            }
                        }

                        cachedData.addCachedPartsEntries(filterEntries);
                    }

                    // Bei negativem Filterergebnis prüfen, ob sich das effektiv relevante Modul im Baugruppenbaum vom
                    // Modul des aktuell geprüften Stücklisteneintrags unterscheidet. Falls ja, dann das Filterergebnis
                    // für dieses letzte sichtbare Modul bestimmen und dieses als Ergebnis verwenden.
                    boolean entryVisible = filterEntries.isEntryVisible(partListEntry);
                    if (!entryVisible && !filterEntries.isFinished() && (assemblyToCheckForHiddenModule != null)
                        && !assemblyToCheckForHiddenModule.getAsId().equals(ownerAssemblyId)) {
                        EtkDataPartListEntry hiddenSingleSubAssembly = ownerAssembly.getHiddenSingleSubAssembly(null);
                        if (hiddenSingleSubAssembly != null) {
                            entryVisible = checkFilter(hiddenSingleSubAssembly);
                        }
                    }
                    return entryVisible;
                } else {
                    // Stückliste aus einem Spezial-Produkt
                    if (!filterSpecialProductPartsListEntry(ownerAssembly, partListEntry)) {
                        return false;
                    }
                }
                return true;
            }
        } else if (etkDataObject instanceof iPartsDataImage) {
            if (isFilterActive()) {
                iPartsDataImage image = (iPartsDataImage)etkDataObject;
                EtkProject project = image.getEtkProject();
                iPartsDataAssembly iPartsAssembly = getiPartsDataAssembly(project, image.getAssemblyId());
                if (iPartsAssembly != null) {
                    Set<String> models = getRelevantModelNumbers(iPartsAssembly);
                    EnumSet<ModelFilterTypes> activeModelFilters = ModelFilterTypes.forPictures();
                    if (isModelFilterActiveAndValid(iPartsAssembly)) {
                        if (!checkModelFilterForPicture(iPartsAssembly, models, image, activeModelFilters)) {
                            return false;
                        }
                    }
                    if (currentDataCard != null) {
                        if (!filterDatacardForPicture(iPartsAssembly, image)) {
                            return false;
                        }
                    } else {
                        // Falls das Bild nur mit einer geladenen Datenkarte angezeigt werden soll und diese Bilder
                        // ausgefiltert werden sollen, dann hier ausfiltern
                        if (isFilterImagesWithOnlyFINFlag() && isImageOnlyValidForFIN(image)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Filter ein Navigations-TU mit den dafür spezifischen Filter
     *
     * @param ownerAssembly
     * @param partListEntry
     * @return
     */
    private boolean filterNavigationModuleEntry(iPartsDataAssembly ownerAssembly, iPartsDataPartListEntry partListEntry) {
        // Datenkarten-Code-Filter nur bei vorhandenen technischen Code
        if (isDatacardCodeFilterActive(ownerAssembly) && currentDataCard.hasTechnicalCodes()) {
            if (!iPartsFilterHelper.basicCheckCodeFilterForDatacard(partListEntry, currentDataCard.getFilterCodes().getAllCheckedValues(),
                                                                    false, this)) {
                return false;
            }
        }
        boolean dataCardSaFilterActive = isDatacardSaFilterActive(ownerAssembly);

        // SAA-Gültigkeiten-Filter
        if (dataCardSaFilterActive) {
            List<AbstractDataCard> relevantDataCardsForAssembly = getRelevantDatacardsForAssembly(ownerAssembly);
            if (!filterSAAsFromDataCards(partListEntry, relevantDataCardsForAssembly, false)) {
                return false;
            }
        }

        EtkProject project = partListEntry.getEtkProject();
        iPartsDataAssembly destAssembly = getiPartsDataAssembly(project, partListEntry.getDestinationAssemblyId());

        // Produkt zum TU im Stücklisteneintrag
        iPartsProduct product = (destAssembly != null) ? getProductFromAssembly(project, destAssembly) : null;

        // TU- und Produktsichtbarkeit prüfen
        if (((destAssembly != null) && !isModuleVisible(destAssembly)) || ((product != null) && !product.isRetailRelevantFromDB())) {
            if (withFilterReason) {
                setFilterReasonForPartListEntry(partListEntry, MODULE_HIDDEN, "!!SA/TU ist ausgeblendet");
            }
            return false;
        }

        Set<String> modelNumbersForAggModulesChecks = getCurrentDataCard().getFilterModelNumbers(project);

        // BM-Gültigkeiten-Filter
        if (isModelFilterActiveAndValid(ownerAssembly)) {
            // Der "normale" Gültigkeiten Filter
            Set<String> models = getRelevantModelNumbers(ownerAssembly);
            for (String modelNumber : models) {
                if (!iPartsFilterHelper.basicCheckModelValidityFilter(partListEntry, modelNumber, this)) {
                    return false;
                }
            }
            //Zusätzlich zu den normalen BM Gültigkeiten: Prüfen, ob ein möglicher TU-Stücklisteneintrag zu einem
            // Produkt gehört, zu dem die im Filter eingestellten BM nicht gültig sind
            if ((destAssembly != null) && (product != null)) {
                // BM des Produkts bestimmen
                Set<String> modelsFromProduct = product.getVisibleModelNumbers(project);
                if (!modelsFromProduct.isEmpty()) {
                    // CHeck, ob ein BM aus dem Filter zu den BM vom Produkt passt
                    boolean relevantModelFound = modelNumbersForAggModulesChecks.stream().anyMatch(modelsFromProduct::contains);
                    if (!relevantModelFound) {
                        // Falls es keinen Treffer gibt, ist die Position nicht gültig
                        if (withFilterReason) {
                            if (modelNumbersForAggModulesChecks.size() == 1) {
                                setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                                "!!Das relevante Baumuster \"%1\" ist im Produkt \"%2\" nicht enthalten",
                                                                modelNumbersForAggModulesChecks.iterator().next(),
                                                                product.getAsId().getProductNumber());
                            } else {
                                setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.MODEL,
                                                                "!!Keines der relevanten Baumuster \"%1\" ist im Produkt \"%2\" enthalten",
                                                                String.join(", ", modelNumbersForAggModulesChecks),
                                                                product.getAsId().getProductNumber());
                            }
                        }
                        return false;
                    }
                }
            }
        }

        // Modul-Gültigkeiten prüfen
        if ((destAssembly != null) && (product != null)) {
            // KG/TU vom Ziel-Modul ermitteln
            List<EtkDataPartListEntry> parentDestAssemblyEntries = destAssembly.getParentAssemblyEntries(false);
            if (Utils.isValid(parentDestAssemblyEntries)) {
                AssemblyId destKgTuAssemblyId = parentDestAssemblyEntries.get(0).getOwnerAssemblyId();
                if (destKgTuAssemblyId.isValidId()) {
                    KgTuId destKgTuId = iPartsVirtualNode.getKgTuFromAssemblyId(destKgTuAssemblyId);
                    if ((destKgTuId != null) && !isKgTuVisible(product.getAsId(), destKgTuId, modelNumbersForAggModulesChecks,
                                                               partListEntry.getEtkProject())) {
                        if (withFilterReason) {
                            setFilterReasonForPartListEntry(partListEntry, TU_VALIDITIES_FILTER, "!!TU ist für die Datenkarte nicht gültig");
                        }
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private iPartsDataAssembly getiPartsDataAssembly(EtkProject project, AssemblyId assemblyId) {
        if (assemblyId.isValidId()) {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
            if (assembly instanceof iPartsDataAssembly) {
                return (iPartsDataAssembly)assembly;
            }
        }
        return null;
    }

    private iPartsProduct getProductFromAssembly(EtkProject project, iPartsDataAssembly assembly) {
        iPartsProductId productId = assembly.getProductIdFromModuleUsage();
        if ((productId != null) && productId.isValidId()) {
            return iPartsProduct.getInstance(project, productId);
        }
        return null;
    }

    /**
     * Filtert eine Stücklistenposition eines Spezial-Produkts
     *
     * @param ownerAssembly
     * @param partListEntry
     * @return
     */
    private boolean filterSpecialProductPartsListEntry(iPartsDataAssembly ownerAssembly, iPartsDataPartListEntry partListEntry) {
        if (isProductClassFilterActive(ownerAssembly) && StrUtils.isValid(filterValueProductClass)) {
            if (!iPartsFilterHelper.basicCheckProductClassValidityFilter(partListEntry, filterValueProductClass, this)) {
                return false;
            }
        }

        // Datenkarten-Code-Filter nur bei vorhandenen technischen Code
        if (isDatacardCodeFilterActive(ownerAssembly) && currentDataCard.hasTechnicalCodes()) {
            if (!iPartsFilterHelper.basicCheckCodeFilterForDatacard(partListEntry, currentDataCard.getFilterCodes().getAllCheckedValues(),
                                                                    false, this)) {
                return false;
            }
        }

        // Ländergültigkeits-Filter
        if (isCountryValidityFilterActive(ownerAssembly)) {
            String country = null;
            VehicleDataCard vehicleDataCard = getVehicleDataCard(false);
            if (vehicleDataCard != null) {
                country = vehicleDataCard.getCountry();
            }
            if (StrUtils.isValid(country)) {
                if (!iPartsFilterHelper.basicCheckCountryValidityFilter(partListEntry, country, this)) {
                    return false;
                }
            }
        }

        // Spezifikations-Filter
        if (isSpecificationFilterActive(ownerAssembly)) {
            VehicleDataCard vehicleDataCard = getVehicleDataCard(false);
            if ((vehicleDataCard != null) && ownerAssembly.isSpecTypeRelevant()) {
                iPartsSpecType specType = ownerAssembly.getSpecType();
                Set<String> specValiditiesAndQuantitiesForSpecType = vehicleDataCard.getSpecValiditiesAndQuantitiesForSpecType(partListEntry.getEtkProject(), specType).keySet();
                if (!iPartsFilterHelper.basicCheckSpecificationFilter(partListEntry, specValiditiesAndQuantitiesForSpecType, this)) {
                    return false;
                }
            }
        }

        // Entfallpositionen entfernen (K_OMIT)
        if (isOmittedPartListEntriesFilterActive(ownerAssembly)) {
            if (!iPartsFilterHelper.basicCheckOmittedPartListEntryFilter(partListEntry, this)) {
                return false;
            }
        }
        return true;
    }

    private boolean isImageOnlyValidForFIN(iPartsDataImage image) {
        return image.getFieldValueAsBoolean(iPartsConst.FIELD_I_ONLY_FIN_VISIBLE);
    }

    public boolean checkProductStructureWithAggregatesForVirtualNodePath(List<iPartsVirtualNode> virtualNodesPath, boolean productStructureWithAggregatesForSession) {
        if (!virtualNodesPath.isEmpty()) {
            iPartsNodeType nodeType = virtualNodesPath.get(0).getType();
            if (nodeType.isProductType() && (nodeType != iPartsNodeType.PRODUCT_KGTU_COMMON) && (nodeType != iPartsNodeType.PRODUCT_EINPAS_COMMON)
                && (nodeType.isProductStructureWithAggregates() != productStructureWithAggregatesForSession)) {
                return false;
            }
        }
        return true;
    }

    private boolean isKgTuOrModuleVisible(List<iPartsVirtualNode> virtualNodesPath, AssemblyId assemblyIdForProductKgTu,
                                          AssemblyId destinationAssemblyId, EtkProject project) {
        if (!(switchboardState.isMainSwitchActive() && StrUtils.isValid(currentDataCard.getFilterModelNo())
              && iPartsVirtualNode.isProductKgTuNode(virtualNodesPath))) {
            return true;
        }

        iPartsDataAssembly iPartsDestAssembly = getiPartsDataAssembly(project, destinationAssemblyId);
        if (iPartsDestAssembly != null) {
            String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(assemblyIdForProductKgTu);
            if (StrUtils.isValid(productNumber)) {
                KgTuId kgTuId = iPartsVirtualNode.getKgTuFromAssemblyId(assemblyIdForProductKgTu);
                if ((kgTuId != null) && !EditModuleHelper.isCarPerspectiveKgTuId(kgTuId)) {
                    iPartsProductId productId = new iPartsProductId(productNumber);
                    if (!cachedData.isSpecialCatalog(productId, project)) { // Bei Spezial-Produkten keine Filterung der KG/TUs über gültige Baumuster usw. möglich
                        Set<String> modelNumbers = getRelevantModelNumbers(iPartsDestAssembly);

                        // Wenn ein Modul für kein relevantes Baumuster der Datenkarte gültig ist, dann ist das gesamte Modul
                        // für die Datenkarte nicht gültig
                        if (modelNumbers.isEmpty()) {
                            return false;
                        }

                        // Aktuell geht die Prüfung der Modul-Gültigkeiten nur für KG/TU-Knoten von Aggregaten oder Fahrzeugen
                        // ohne dazugemischte Aggregate-Produkte bzw. für die KG/TU-Knoten von Fahrzeug-Modulen
                        boolean checkModuleValidities = !virtualNodesPath.get(0).getType().isProductStructureWithAggregates();
                        if (!checkModuleValidities && iPartsDestAssembly.isRetailPartList()) {
                            // Bei Retail-Stücklisten prüfen, ob deren Produkt identisch zum Produkt der KG/TU-Struktur ist
                            // (dann handelt es sich um ein Fahrzeug-Modul)
                            iPartsProductId productIdFromDestAssembly = iPartsDestAssembly.getProductIdFromModuleUsage();
                            if ((productIdFromDestAssembly != null) && productIdFromDestAssembly.getProductNumber().equals(productNumber)) {
                                checkModuleValidities = true;
                            }
                        }
                        if (checkModuleValidities && !isKgTuVisible(productId, kgTuId, modelNumbers, project)) {
                            return false;
                        }
                    }

                    // Optional überprüfen, ob die Ziel-Retail-Stückliste leer ist
                    if (cachedData.isHideEmptyTUs() && kgTuId.isTuNode() && iPartsDestAssembly.getPartList(iPartsDestAssembly.getEbene()).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean isKgTuVisible(iPartsProductId productId, KgTuId kgTuId, Set<String> modelNumbers, EtkProject project) {
        // Bei Filterung OHNE FIN bzw. nicht geladener Datenkarte aber mit einem Baumuster prüfen, ob Baumuster-Filter aktiv
        // und Baumuster in der Datenkarte nicht leer ist (sollte bei validem IdentContext eigentlich immer so sein)
        boolean checkModelValidities = isModelFilterActiveAndValid(null);

        // Prüfung der SAA- und Code-Gültigkeiten NUR MIT FIN und geladener Datenkarte, deshalb Prüfung auf "echte Datenkarte".
        // Zudem nur Prüfung, wenn Datenkarten-SA Filter aktiviert ist.
        boolean checkCodeSAAValidities = currentDataCard.isDataCardLoaded() && isDatacardSaFilterActive(null);

        // Nur weitermachen, falls Baumuster- oder SAA-Gültigkeiten geprüft werden müssen
        if (checkModelValidities || checkCodeSAAValidities) {
            iPartsCatalogNode kgTuRootNode = cachedData.getKgTuRootNode(productId, project);
            iPartsCatalogNode kgTuNode = kgTuRootNode.getNode(kgTuId);
            if (!iPartsFilterHelper.isKgTuNodeVisible(productId, modelNumbers, kgTuId, kgTuNode, this, project)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSessionWithGui() {
        return isSessionWithGUI;
    }

    public boolean isCarAndTruckRightsInSession() {
        return carAndTruckRightsInSession;
    }

    private iPartsDataAssembly getAssemblyToCheckForHiddenModule(iPartsDataPartListEntry partListEntry) {
        // Ist der Filter-Hauptschalter aktiv?
        if (switchboardState.isMainSwitchActive()) {
            iPartsDataAssembly assemblyToCheck = null;
            iPartsAssemblyId assemblyIdToCheck = getFilterAssemblyId(partListEntry);
            if (assemblyIdToCheck.isVirtual()) { // Modul in der Struktur ausgeblendet?
                List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyIdToCheck);
                if (!virtualNodes.isEmpty()) {
                    iPartsVirtualNode lastNode = virtualNodes.get(virtualNodes.size() - 1);
                    if (lastNode.isParentNodeForRetailPartLists()) {
                        EtkDataAssembly destinationAssembly = EtkDataObjectFactory.createDataAssembly(partListEntry.getEtkProject(),
                                                                                                      assemblyIdToCheck);
                        destinationAssembly = destinationAssembly.getLastHiddenSingleSubAssemblyOrThis(null);
                        if (destinationAssembly instanceof iPartsDataAssembly) {
                            assemblyToCheck = (iPartsDataAssembly)destinationAssembly;
                        }
                    }
                }
            } else if (partListEntry.getOwnerAssembly().isPartListEditable()) { // (Editierbares) Modul des Stücklisteneintrags ausgeblendet?
                assemblyToCheck = partListEntry.getOwnerAssembly();
            }
            return assemblyToCheck;
        }

        return null;
    }

    private boolean filterHiddenHmMSmNode(iPartsDataPartListEntry partListEntry) {
        boolean showHiddenHmMSmNodes = iPartsUserSettingsHelper.isShowHiddenHmMSmNodes(partListEntry.getEtkProject());
        if (!showHiddenHmMSmNodes) { // DAIMLER-7296 Filter aktiv?
            iPartsAssemblyId assemblyIdToCheck = getFilterAssemblyId(partListEntry);
            if (assemblyIdToCheck.isVirtual()) {
                List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(assemblyIdToCheck);
                if (iPartsVirtualNode.isHmMSmNode(virtualNodes)) { // Ist der HM/M/SM-Knoten ausgeblendet?
                    iPartsSeriesId seriesId = (iPartsSeriesId)virtualNodes.get(0).getId();
                    HmMSm hmMSmStructure = HmMSm.getInstance(partListEntry.getEtkProject(), seriesId);
                    HmMSmId hmMSmId = (HmMSmId)virtualNodes.get(1).getId();
                    HmMSmNode hmMSmNode = hmMSmStructure.getNode(hmMSmId);
                    if ((hmMSmNode != null) && hmMSmNode.isHidden()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void correctNegativeTimeIntervalsForFactoryData(iPartsDataAssembly assembly) {
        // DAIMLER-6564 Negative Zeitintervalle als gültig auswerten
        // Korrektur EINMALIG pro Retail-Stückliste immer vornehmen, wenn der Filter-Hauptschalter aktiv ist, der Baumuster-Filter
        // aber inaktiv
        if (switchboardState.isMainSwitchActive() && !isModelFilterActiveAndValid(assembly) && assembly.isRetailPartList() && (assembly.getFilteredWithModelNumber() == null)) {
            // Wir sind in einer Retail-Stückliste
            iPartsDocumentationType documentationType = assembly.getDocumentationType();

            // DIALOG-Filterung
            if (documentationType.isPKWDocumentationType()) {
                // Lenkung für die Filterung von Lenkungs-bezogenen Idents
                String steering = null;
                if (isSteeringFilterActive(assembly)) {
                    steering = getSteeringValue();
                    if (steering.isEmpty()) {
                        steering = null;
                    }
                }

                for (EtkDataPartListEntry partListEntry : assembly.getPartListUnfiltered(null)) {
                    if (partListEntry instanceof iPartsDataPartListEntry) {
                        iPartsDataPartListEntry iPartsPLE = (iPartsDataPartListEntry)partListEntry;
                        iPartsFactoryData factoryDataForRetailUnfiltered = iPartsPLE.getFactoryDataForRetailUnfiltered();
                        iPartsFactoryData factoryDataWithoutReplacements = factoryDataForRetailUnfiltered;
                        if (iPartsPLE.hasValidFactoryDataForRetailUnfiltered()) {

                            // Zunächst überprüfen, ob überhaupt negative Zeitintervalle in den Werkseinsatzdaten
                            // vorliegen, weil wir uns ansonsten das Klonen sparen können
                            boolean foundNegativeTimeInterval = false;
                            factoryDataLoop:
                            for (List<iPartsFactoryData.DataForFactory> factoryDataList : factoryDataForRetailUnfiltered.getFactoryDataMap().values()) {
                                for (iPartsFactoryData.DataForFactory dataForFactory : factoryDataList) {
                                    if (dataForFactory.dateFrom > dataForFactory.getDateToWithInfinity()) {
                                        foundNegativeTimeInterval = true;
                                        break factoryDataLoop;
                                    }
                                }
                            }

                            if (foundNegativeTimeInterval) {
                                factoryDataWithoutReplacements = new iPartsFactoryData();
                                factoryDataWithoutReplacements.setEvalPemFrom(factoryDataForRetailUnfiltered.isEvalPemFrom());
                                factoryDataWithoutReplacements.setEvalPemTo(factoryDataForRetailUnfiltered.isEvalPemTo());

                                for (Map.Entry<String, List<iPartsFactoryData.DataForFactory>> factoryDataEntry : factoryDataForRetailUnfiltered.getFactoryDataMap().entrySet()) {
                                    List<iPartsFactoryData.DataForFactory> factoryDataList = new DwList<>(factoryDataEntry.getValue().size());
                                    for (iPartsFactoryData.DataForFactory dataForFactory : factoryDataEntry.getValue()) {
                                        if (dataForFactory.dateFrom > dataForFactory.getDateToWithInfinity()) {
                                            iPartsFactoryData.DataForFactory modifiedDataForFactory = new iPartsFactoryData.DataForFactory();
                                            modifiedDataForFactory.assign(dataForFactory);
                                            modifiedDataForFactory.addFilterInfo(TranslationHandler.translate("!!Negatives Zeitintervall korrigiert auf \"PEM Termin bis\""));
                                            modifiedDataForFactory.dateFrom = modifiedDataForFactory.dateTo;
                                            dataForFactory = modifiedDataForFactory;
                                        }
                                        factoryDataList.add(dataForFactory);
                                    }

                                    if (!factoryDataList.isEmpty()) {
                                        factoryDataWithoutReplacements.setDataForFactory(factoryDataEntry.getKey(),
                                                                                         factoryDataList);
                                    }
                                }
                            }

                        }

                        // DAIMLER-6672 Flags "PEM ab/bis auswerten" berücksichtigen
                        // DAIMLER-6571 Hier keine Baumuster-bezogene Ident-Filterung
                        factoryDataWithoutReplacements = iPartsFilterHelper.modifyAndFilterAllFactoryData(factoryDataWithoutReplacements,
                                                                                                          null, steering);

                        iPartsPLE.setFactoryDataForRetailWithoutReplacements(factoryDataWithoutReplacements);
                    }
                }

                assembly.setFilteredWithModelNumber("-"); // "-" als Dummy-Baumuster für die erfolgte "Baumuster-Filterung" setzen
            }
        }
    }

    /**
     * Durchführung einer nachträglichen Filterung für die gesamte bereits gefilterte Stückliste.
     *
     * @param partListEntries Stückliste, die nachträglich gefiltert werden soll. Normalerweise ist das die
     *                        gefilterte Stückliste. In der Filterabsicherung ist es allerdings die UNGEFILTERTE
     *                        Stückliste
     */
    public List<EtkDataPartListEntry> postFilterForFilteredPartList(List<EtkDataPartListEntry> partListEntries) {
        if (!partListEntries.isEmpty()) {
            EtkDataAssembly ownerAssembly = partListEntries.get(0).getOwnerAssembly();
            if (ownerAssembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)ownerAssembly;
                if (iPartsAssembly.getDocumentationType().isPKWDocumentationType() && isRemoveDuplicatesFilterActive(iPartsAssembly)) {
                    // Set mit KlfdNr von Position die künstlich erzeugt wurden (addierte Mengen und aufgesammelte Fehlerorte)
                    Set<String> mergedEntriesKlfdNr = new HashSet<>();
                    List<EtkDataPartListEntry> resultPartListEntries = mergeSimilarPartListEntries(partListEntries, mergedEntriesKlfdNr);

                    // Den zusätzlichen Verdichtungsfilter nur durchlaufen, wenn die Datenkarte geladen und der Endnummernfilter atkiv ist
                    if (getCurrentDataCard().isDataCardLoaded() && isEndNumberFilterActive(iPartsAssembly)) {
                        resultPartListEntries = removeSimilarEntriesWithoutCodeCheck(resultPartListEntries, mergedEntriesKlfdNr);
                    }
                    return resultPartListEntries;
                }
            }
        }

        return partListEntries;
    }

    /**
     * Nachträglicher Verdichtungsfilter, der nach der Mengenaddition läuft. Hierbei sollen alle Positionne mit gleicher
     * POSE und POSV verglichen werden, wobei einige Attributte gleich sein müssen. Bei gleichen Attributen wird nur die
     * Position mit dem höchsten SDATA (KEM-AB-Datum) ausgegeben.
     *
     * @param partListEntries
     * @param mergedEntriesKlfdNr - Positionen, die bei der Mengenaddition erzeugt wurden. Diese dürfen nicht berücksichtigt werden
     * @return
     */
    private List<EtkDataPartListEntry> removeSimilarEntriesWithoutCodeCheck(List<EtkDataPartListEntry> partListEntries, Set<String> mergedEntriesKlfdNr) {
        Map<String, List<iPartsDataPartListEntry>> similarPartListEntriesMap
                = getSimilarPartListEntriesMap(partListEntries, iPartsPLE -> StrUtils.stringArrayToString("\t",
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO),
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_HM),
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_M),
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SM),
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE),
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV),
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_WW),
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_ETKZ),
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_AA),
                                                                                                          iPartsPLE.getFieldValue(EtkDbConst.FIELD_K_POS),
                                                                                                          iPartsPLE.getFieldValue(iPartsConst.FIELD_K_HIERARCHY),
                                                                                                          iPartsPLE.getFieldValue(iPartsConst.FIELD_K_STEERING),
                                                                                                          iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT),
                                                                                                          iPartsPLE.getFieldValue(EtkDbConst.FIELD_K_MATNR)));

        Set<String> hiddenPartListEntryKLfdnrSet = new HashSet<>(); // Laufende Nummern der ausgefilterten Stücklisteneinträge
        for (List<iPartsDataPartListEntry> similarPartListEntries : similarPartListEntriesMap.values()) {
            for (int i = 0; i < (similarPartListEntries.size() - 1); i++) {
                String currentEntryKlfdNr = similarPartListEntries.get(i).getAsId().getKLfdnr();
                // Check, ob die aktuelle Position schon zu den nicht angezeigten oder den zusammengelegten Positionen gehört
                if (!hiddenPartListEntryKLfdnrSet.contains(currentEntryKlfdNr) && !mergedEntriesKlfdNr.contains(currentEntryKlfdNr)) {
                    for (int j = i + 1; j < similarPartListEntries.size(); j++) {
                        iPartsDataPartListEntry followingEntry = similarPartListEntries.get(j);
                        // Check, ob die nachfolgende Position schon zu den nicht angezeigten oder den zusammengelegten Positionen gehört
                        if (!hiddenPartListEntryKLfdnrSet.contains(followingEntry.getAsId().getKLfdnr()) && !mergedEntriesKlfdNr.contains(followingEntry.getAsId().getKLfdnr())) {
                            iPartsDataPartListEntry currentEntry = similarPartListEntries.get(i);
                            if (checkForIdenticalFootNotes(currentEntry, followingEntry) && iPartsFilterHelper.checkIdenticalReplacements(currentEntry, followingEntry)) {
                                // Die Teileposition mit dem höchsten SDA (= K_DATEFROM) wird ausgegeben.
                                // Sind beide gleich, wird die ERSTE getroffene Teilepos ausgegeben.
                                String currentDateFrom = currentEntry.getFieldValue(iPartsConst.FIELD_K_DATEFROM);
                                String followingDateFrom = followingEntry.getFieldValue(iPartsConst.FIELD_K_DATEFROM);

                                iPartsDataPartListEntry hideEntry;
                                if (currentDateFrom.compareTo(followingDateFrom) >= 0) { // currentDateFrom >= followingDateFrom
                                    hideEntry = followingEntry;
                                } else {
                                    hideEntry = currentEntry;
                                }

                                String hideEntryKLfdnr = hideEntry.getAsId().getKLfdnr();
                                if (!hiddenPartListEntryKLfdnrSet.contains(hideEntryKLfdnr)) { // Doppelte Übernahme vermeiden
                                    hiddenPartListEntryKLfdnrSet.add(hideEntryKLfdnr);
                                    if (withFilterReason) {
                                        setFilterReasonForPartListEntry(hideEntry, REMOVE_DUPLICATES,
                                                                        "!!Verdichtung nach Mengenaddition (Teileposition mit gleicher POSE und PV aber kleinerem \"Datum ab\")");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Wegen der Verdichtung ausgefilterte Stücklisteneinträge entfernen
        // Bei Filterung mit Filtergrund dürfen keine Stücklisteneinträge entfernt werden
        if (!hiddenPartListEntryKLfdnrSet.isEmpty() && !withFilterReason) {
            List<EtkDataPartListEntry> result = new ArrayList<>(partListEntries.size() - hiddenPartListEntryKLfdnrSet.size());
            for (EtkDataPartListEntry filterEntry : partListEntries) {
                String kLfdnr = filterEntry.getAsId().getKLfdnr();
                if (!hiddenPartListEntryKLfdnrSet.contains(kLfdnr)) {
                    result.add(filterEntry);
                }
            }
            return result;
        }
        return partListEntries;
    }

    /**
     * Durchführung der Filterung für eine komplette {@link iPartsFilterPartsEntries}-Datenstruktur.
     *
     * @param filterEntries Alle zu einem PartsListEntry für die Filterung relevanten Entries (Filterstatus wird hier gespeichert)
     */
    public void filterPartsEntries(iPartsFilterPartsEntries filterEntries) {
        iPartsDataAssembly ownerAssembly = filterEntries.getPartListEntriesOwnerAssembly();
        if (!filterEntries.isFinished()) {
            if (isModelFilterActiveAndValid(ownerAssembly)) {
                EnumSet<ModelFilterTypes> activeModelFilters = activeModelSubFilters;
                if (!isSaStrichFilterActive(ownerAssembly)) {
                    activeModelFilters = EnumUtils.minus(activeModelFilters, ModelFilterTypes.SA_STRICH);
                }
                Set<String> models = getRelevantModelNumbers(ownerAssembly);
                checkModelFilter(models, filterEntries, activeModelFilters, -1);
            }

            // DAIMLER-6809: Zusätzliche Stücklisteneinträge zu einer DIALOG-Position, die nur im Baumuster-Filter berücksichtigt
            // werden sollen, ab hier ausblenden
            if (ownerAssembly.isRetailPartList() && isOnlyModelFilterActive(ownerAssembly)) {
                for (EtkDataPartListEntry partListEntry : filterEntries.getAllPositionsVariantsAsSingleList()) {
                    if (!iPartsFilterHelper.basicCheckOnlyModelFilterForPartListEntryFilter(partListEntry, this)) {
                        filterEntries.hideEntry(partListEntry);
                    }
                }
            }
        }

        if (!filterEntries.isFinished()) {
            // Filter mit den Werten aus der Datenkarte
            if (currentDataCard != null) {
                filterDatacard(ownerAssembly, filterEntries);
            }
        }
    }

    /**
     * Zeichnungsreferenzen über Datenkarte filtern. Zeichnungsreferenzen können nur bezüglich ihrer Code, Baumustergültigkeiten und
     * Saa/BK-Gültigkeiten gefiltert werden. Analog zum Datenkartenfilter über Stücklistenpositionen.
     *
     * @param assembly
     * @param image
     */
    private boolean filterDatacardForPicture(iPartsDataAssembly assembly, iPartsDataImage image) {
        boolean isRetailPartList = assembly.isRetailPartList();

        // Ereignis-Filterung der Ereignisse an der Zeichnungsreferenz
        if (!iPartsFilterHelper.basicCheckEventFilterForPictureReference(image, getCurrentDataCard().getEvent(),
                                                                         cachedData.getAssemblyData(assembly))) {
            return false;
        }

        List<AbstractDataCard> relevantDatacardsForAssembly = getRelevantDatacardsForAssembly(assembly);

        if (isRetailPartList) {
            // Falls das Bild nur mit einer geladenen Datenkarte angezeigt werden soll und dr Kenner am Bild gesetzt ist,
            // hier das Bild ausfiltern
            if (isFilterImagesWithOnlyFINFlag() && !currentDataCard.isDataCardLoaded() && isImageOnlyValidForFIN(image)) {
                return false;
            }
            // Codefilter
            if (isDatacardCodeFilterActive(assembly)) {
                // Wenn es in keiner Datenkarte einen positiven Treffer für die Codes gibt, dann fliegt die Zeichnung raus
                boolean totalCodeFilterResult = relevantDatacardsForAssembly.isEmpty();
                for (AbstractDataCard datacard : relevantDatacardsForAssembly) {
                    if (datacard.hasTechnicalCodes()) {
                        if (iPartsFilterHelper.basicCheckCodeFilterForDatacard(image.getFieldValue(iPartsConst.FIELD_I_CODES), datacard.getFilterCodes().getAllCheckedValues())) {
                            totalCodeFilterResult = true;
                            break;
                        }
                    } else {
                        totalCodeFilterResult = true;
                        break;
                    }
                }
                if (!totalCodeFilterResult) {
                    return false;
                }
            }

            // SAA-Filter mit den Werten aus der Datenkarte (echt oder simulierte Datenkarte)
            if (isDatacardSaFilterActive(assembly)) {
                boolean isSAAssembly = assembly.isSAAssembly();
                if (isSaStrichFilterActive(assembly) || !isSAAssembly) {
                    // Wenn es in keiner Datenkarte für den SAA Filter einen Treffer gibt, nur dann fliegt die Zeichnung raus
                    boolean totalSaaFilterResult = relevantDatacardsForAssembly.isEmpty();
                    for (AbstractDataCard datacard : relevantDatacardsForAssembly) {
                        Set<String> saas = getSaasForDataCard(datacard);
                        if (iPartsFilterHelper.basicCheckSaStrichValidityFilter(image.getFieldValueAsArray(iPartsConst.FIELD_I_SAA_CONSTKIT_VALIDITY), saas)) {
                            totalSaaFilterResult = true;
                            break;
                        }
                    }
                    if (!totalSaaFilterResult) {
                        return false;
                    }
                }
            }

            // PSK-Varianten-Filter mit den Werten aus der Datenkarte
            if (isPSKVariantsFilterActive(assembly)) {
                // Wenn es in keiner Datenkarte für den PSK-Varianten-Filter einen Treffer gibt, nur dann fliegt die Zeichnung raus
                boolean totalPSKVariantsFilterResult = relevantDatacardsForAssembly.isEmpty();
                for (AbstractDataCard datacard : relevantDatacardsForAssembly) {
                    Set<String> pskVariants = datacard.getPskVariants();
                    if (iPartsFilterHelper.basicCheckPSKVariantsValidityFilter(image.getFieldValueAsArray(iPartsConst.FIELD_I_PSK_VARIANT_VALIDITY), pskVariants)) {
                        totalPSKVariantsFilterResult = true;
                        break;
                    }
                }
                if (!totalPSKVariantsFilterResult) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Zusammengehörende PartList Entries über Datenkarte filtern.
     *
     * @param assembly
     * @param filterEntries Alle zu einem PartsListEntry für die Filterung relevanten Entries (Filterstatus wird hier gespeichert)
     */
    private void filterDatacard(iPartsDataAssembly assembly, iPartsFilterPartsEntries filterEntries) {
        // Verdichtungsfilter, entferne Dupletten
        iPartsDocumentationType documentationType = filterEntries.getPartListEntriesOwnerAssembly().getDocumentationType();
        boolean isRetailPartList = filterEntries.getPartListEntriesOwnerAssembly().isRetailPartList();
        if (isRetailPartList) {
            if (isRemoveDuplicatesFilterActive(assembly) && !filterEntries.isFinished() && (documentationType.isPKWDocumentationType())) {
                checkRemoveDuplicatesFilter(filterEntries);
            }

            // Feder-Filter
            if (isSpringFilterActive(assembly) && !filterEntries.isFinished() && filterEntries.isRelevantForSameHotSpotFilters()) {
                checkSpringFilter(filterEntries);
            }

            // Spezieller ZB-Sachnummern Filter
            if (!filterEntries.isFinished() && filterEntries.isRelevantForSameHotSpotFilters()) {
                checkSpecialZBFilter(filterEntries);
            }
        }

        if (!filterEntries.isFinished()) {
            for (iPartsDataPartListEntry entry : filterEntries.getVisibleEntries()) {
                if (!checkDatacardFilterOneEntry(entry)) {
                    filterEntries.hideEntry(entry);
                }
            }
        }

        // Gesammelte BM-bildende Code speichern, damit sie im erweiterten Code-Filter und im Wegfallsachnummern-Filter
        // verwendet werden können.
        // Das ist hier nur möglich weil für EXTENDED_CODE und OMITTED_PARTS die gleichen Datenkarten relevant sind
        if (!filterEntries.isFinished() && isRetailPartList
            && (isExtendedCodesFilterActive(assembly) || (isDatacardCodeFilterActive(assembly) && isOmittedPartsFilterActive(assembly) &&
                                                          (documentationType == iPartsDocumentationType.DIALOG_IPARTS)))) {
            Set<String> allModelBuildingCodes = new HashSet<>();
            Set<String> datacardCodes = new HashSet<>();
            List<AbstractDataCard> relevantDatacards = getRelevantDatacardsForAssembly(assembly);
            relevantDatacards = getRelevantDataCardsForFilterType(relevantDatacards, documentationType, EXTENDED_CODE,
                                                                  filterEntries.getEtkProject());
            if (!relevantDatacards.isEmpty()) {
                // Aus allen Baumustern die baumusterbildenden Code ermitteln und diese beim erweiteren Codefilter im Scoring ignorieren
                // Normalerweise hat man hier nur ein Baumuster, in dem seltenen Fall, dass es zwei Baumuster sind, dann stört es nicht, wenn
                // evtl. zu viele baumusterbildende Code übergeben werden, weil diese Code sich nicht mit den technischen Code überschneiden können
                for (AbstractDataCard dataCard : relevantDatacards) {
                    if (dataCard.hasFilterTechnicalCodes()) {
                        datacardCodes.addAll(dataCard.getFilterCodes().getAllCheckedValues());
                    }
                    DIALOGModelFilter dialogModelFilter = cachedData.getDialogModelFilter(filterEntries.getEtkProject(),
                                                                                          dataCard.getFilterModelNo(), (documentationType != iPartsDocumentationType.DIALOG));
                    allModelBuildingCodes.addAll(dialogModelFilter.getModelBuildingCodeSet()); // Alle BM-bildenden Code des Baumusters
                }
            }

            // Erweiterter Code-Filter für Datenkarten
            if (isExtendedCodesFilterActive(assembly)) {
                if (!datacardCodes.isEmpty()) {
                    Set<String> skipCodesForExtendedCodeFilter;
                    iPartsProduct product = iPartsProduct.getInstance(assembly.getEtkProject(), assembly.getProductIdFromModuleUsage());
                    boolean skipModelCodes = product.skipModelCodeInExtendedCodeFilter();
                    if (skipModelCodes) {
                        skipCodesForExtendedCodeFilter = allModelBuildingCodes;
                    } else {
                        skipCodesForExtendedCodeFilter = null;
                    }
                    DatacardFilter.extendedCodeFilter(filterEntries, datacardCodes, skipCodesForExtendedCodeFilter, this);
                }
            }

            // Wegfallsachnummern-Filter
            // Nur anwenden, wenn Datenkarten technische Code besitzen, der Dokutyp "DIALOG_IPARTS" ist und
            // Datenkarten-Code- und Wegfallsachnummer-Filter aktiv sind
            if (!filterEntries.isFinished() && !datacardCodes.isEmpty() && isDatacardCodeFilterActive(assembly) &&
                isOmittedPartsFilterActive(assembly) && (documentationType == iPartsDocumentationType.DIALOG_IPARTS)) {
                DatacardFilter.omittedPartsInDatacardFilter(filterEntries, allModelBuildingCodes, this);
            }
        }

        // Leitungssatz-Baukasten Filterung durchführen
        // Nur answenden, wenn Leitungssatz-BK Filter und die Adminoption aktiv sind und die Stückliste eine
        // Retail Stückliste ist
        if (!filterEntries.isFinished() && isRetailPartList
            && iPartsWireHarnessHelper.isWireHarnessFilterActive(this, assembly)) {
            iPartsFilterHelper.basicCheckWireHarnessFilter(filterEntries, this);
        }

        // PSK-Varianten-Filter mit den Werten aus der Datenkarte erst ganz am Ende durchführen
        if (!filterEntries.isFinished() && isRetailPartList && isPSKVariantsFilterActive(assembly)) {
            List<AbstractDataCard> relevantDatacardsForAssembly = getRelevantDatacardsForAssembly(assembly);
            if (!relevantDatacardsForAssembly.isEmpty()) {
                for (iPartsDataPartListEntry partListEntry : filterEntries.getVisibleEntries()) {
                    // Wenn es in keiner Datenkarte für den PSK-Varianten-Filter einen Treffer gibt, nur dann fliegt der Stücklisteneintrag raus
                    boolean totalPSKVariantsFilterResult = false;
                    for (AbstractDataCard datacard : relevantDatacardsForAssembly) {
                        Set<String> pskVariants = datacard.getPskVariants();
                        if (iPartsFilterHelper.basicCheckPSKVariantsValidityFilter(partListEntry.getFieldValueAsArray(iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY),
                                                                                   pskVariants)) {
                            totalPSKVariantsFilterResult = true;
                            break;
                        }
                    }
                    if (!totalPSKVariantsFilterResult) {
                        if (isWithFilterReason()) {
                            setFilterReasonForPartListEntry(partListEntry, iPartsFilterSwitchboard.FilterTypes.PSK_VARIANTS,
                                                            "!!Keine PSK-Variante von der Datenkarte in PSK-Varianten-Gültigkeit enthalten");
                        }
                        filterEntries.hideEntry(partListEntry);
                    }
                }
            }
        }

        if (!filterEntries.isFinished() && isRetailPartList) {
            // Ländergültigkeits-Filter
            if (isCountryValidityFilterActive(assembly)) {
                checkCountryValidityFilter(filterEntries);
            }

            // Spezifikations-Filter
            if (!filterEntries.isFinished() && isSpecificationFilterActive(assembly)) {
                checkSpecificationFilter(filterEntries);
            }
        }
    }

    private void checkSpecialZBFilter(iPartsFilterPartsEntries filterEntries) {
        if (filterEntries.getPartListEntriesOwnerAssembly().isSpecialZBFilterRelevant()) {
            List<AbstractDataCard> relevantDatacards = getRelevantDatacardsForAssembly(filterEntries.getPartListEntriesOwnerAssembly());
            relevantDatacards = getRelevantDataCardsForFilterType(relevantDatacards, filterEntries.getPartListEntriesOwnerAssembly().getDocumentationType(),
                                                                  SPECIAL_ZB_NUMBER, filterEntries.getEtkProject());
            if (relevantDatacards.isEmpty()) {
                return;
            }
            DCAggregateTypes aggTypeForFilter = filterEntries.getPartListEntriesOwnerAssembly().getModuleMetaData().getAggTypeForSpecialZBFilter();
            if (aggTypeForFilter == DCAggregateTypes.UNKNOWN) {
                return;
            }

            String aggPartNumber = null;
            for (AbstractDataCard datacard : relevantDatacards) {
                if ((datacard instanceof AggregateDataCard) && (((AggregateDataCard)datacard).getAggregateType() == aggTypeForFilter)) {
                    String aggNumber = ((AggregateDataCard)datacard).getObjectNo();
                    if (StrUtils.isValid(aggNumber)) {
                        aggPartNumber = aggNumber;
                        break;
                    }
                }
            }

            if (StrUtils.isEmpty(aggPartNumber)) {
                return;
            }

            for (Collection<iPartsDataPartListEntry> positionsVariants : filterEntries.getAllPositionsVariants()) {
                for (iPartsDataPartListEntry entry : positionsVariants) {
                    if (filterEntries.isEntryVisible(entry)) {
                        String partNo = entry.getFieldValue(iPartsConst.FIELD_K_MATNR);
                        if (partNo.equals(aggPartNumber)) {
                            filterEntries.setHighPrio(entry);
                        }
                    }
                }
            }

            if (filterEntries.hasHighPrioEntries()) {
                // Es wurde mind. ein Teil für den speziellen ZB Filter gefunden. Alle Teile die nicht relevant sind
                // sollen an diesem Hotspot nicht dargestellt werden
                String filterReason = "";
                if (isWithFilterReason()) {
                    filterReason = TranslationHandler.translate("!!Teilenummer passt nicht zur gültigen %1 ZB-Sachnummer %2",
                                                                TranslationHandler.translate(aggTypeForFilter.getDescription()), aggPartNumber);
                }

                filterEntries.hideAllExceptHighPrioEntries(this, SPECIAL_ZB_NUMBER, filterReason);
                filterEntries.setFinished(true);
            }
        }
    }

    private void checkSpringFilter(iPartsFilterPartsEntries filterEntries) {
        if (filterEntries.getPartListEntriesOwnerAssembly().isSpringRelevant()) {
            List<AbstractDataCard> relevantDatacards = getRelevantDatacardsForAssembly(filterEntries.getPartListEntriesOwnerAssembly());
            relevantDatacards = getRelevantDataCardsForFilterType(relevantDatacards, filterEntries.getPartListEntriesOwnerAssembly().getDocumentationType(),
                                                                  SPRING, filterEntries.getEtkProject());
            if (relevantDatacards.isEmpty()) {
                return;
            }

            // DAIMLER-3293 Die Federteilenummern können über alle Datenkarten zusammen gesammelt werden
            Collection<String> springPartNumbers = new ArrayList<>();
            for (AbstractDataCard datacard : relevantDatacards) {
                springPartNumbers.addAll(datacard.getSpringPartNumbers(filterEntries.getEtkProject()));
            }

            for (Collection<iPartsDataPartListEntry> positionsVariants : filterEntries.getAllPositionsVariants()) {
                for (iPartsDataPartListEntry entry : positionsVariants) {
                    if (filterEntries.isEntryVisible(entry)) {
                        if (iPartsFilterHelper.basicSpringFilter(entry, springPartNumbers)) {
                            filterEntries.setHighPrio(entry);
                        }
                    }
                }
            }

            if (filterEntries.hasHighPrioEntries()) {
                // Es wurde ein Teil für den Federfilter gefunden, alle Teile die nicht federfilterrelevant sind sollen an diesem Hotspot
                // nicht dargestellt werden
                // Die Filterung ist danach auch beendet.

                String filterReason = "";
                if (isWithFilterReason()) {
                    String springPartNumbersString = "";
                    for (String springPartNumber : springPartNumbers) {
                        if (!springPartNumbersString.isEmpty()) {
                            springPartNumbersString += ", ";
                        }
                        springPartNumbersString += iPartsNumberHelper.formatPartNo(filterEntries.getEtkProject(), springPartNumber);
                    }
                    filterReason = TranslationHandler.translate("!!Teil gehört nicht zu den gültigen Federnummern: %1",
                                                                springPartNumbersString);
                }

                filterEntries.hideAllExceptHighPrioEntries(this, SPRING, filterReason);
                filterEntries.setFinished(true);
            }

            // DAIMLER-14649: Code-Filter ausführen bei mehr als 1 Teileposition auf einem Hotspot (filterEntries enthält
            // nur die Stücklisteneinträge für einen Hotspot)
            List<iPartsDataPartListEntry> visibleEntriesForHotspot = filterEntries.getVisibleEntries();
            if (visibleEntriesForHotspot.size() > 1) {
                Set<String> positiveCodes = currentDataCard.getFilterCodes().getAllCheckedValues();
                for (iPartsDataPartListEntry partListEntry : visibleEntriesForHotspot) {
                    if (!iPartsFilterHelper.basicCheckCodeFilterForDatacard(partListEntry, positiveCodes, false, this)) {
                        filterEntries.hideEntry(partListEntry);
                    }
                }
            }
        }
    }

    /**
     * Sucht nach Dupletten, "Verdichtungsfilter"
     *
     * @param filterEntries
     */
    private void checkRemoveDuplicatesFilter(iPartsFilterPartsEntries filterEntries) {
        for (List<iPartsDataPartListEntry> positionsVariants : filterEntries.getAllPositionsVariants()) {
            for (int i = positionsVariants.size() - 1; i >= 0; i--) {
                iPartsDataPartListEntry entry1 = positionsVariants.get(i);
                if (filterEntries.isEntryVisible(entry1)) {
                    for (int j = i - 1; j >= 0; j--) {
                        iPartsDataPartListEntry entry2 = positionsVariants.get(j);

                        if (filterEntries.isEntryVisible(entry2)) {
                            if (checkIsDuplicate(entry1, entry2)) {
                                // Den Eintrag mit dem kleineren SDA (= K_DATEFROM) ausblenden
                                String dateFrom1 = entry1.getFieldValue(iPartsConst.FIELD_K_DATEFROM);
                                String dateFrom2 = entry2.getFieldValue(iPartsConst.FIELD_K_DATEFROM);

                                iPartsDataPartListEntry hideEntry;
                                if (dateFrom1.compareTo(dateFrom2) > 0) { // dateFrom1 > dateFrom2
                                    hideEntry = entry2;
                                } else {
                                    hideEntry = entry1;
                                }

                                filterEntries.hideEntry(hideEntry);
                                if (withFilterReason) {
                                    setFilterReasonForPartListEntry(hideEntry, REMOVE_DUPLICATES, "!!Doppelter Datensatz mit kleinerem \"Datum ab\"");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkCountryValidityFilter(iPartsFilterPartsEntries filterEntries) {
        String country = null;
        VehicleDataCard vehicleDataCard = getVehicleDataCard(true);
        if (vehicleDataCard != null) {
            country = vehicleDataCard.getCountry();
        }
        if (StrUtils.isValid(country)) {
            for (iPartsDataPartListEntry entry : filterEntries.getVisibleEntries()) {
                if (!iPartsFilterHelper.basicCheckCountryValidityFilter(entry, country, this)) {
                    filterEntries.hideEntry(entry);
                }
            }
        }
    }

    private void checkSpecificationFilter(iPartsFilterPartsEntries filterEntries) {
        VehicleDataCard vehicleDataCard = getVehicleDataCard(true);
        if ((vehicleDataCard != null) && filterEntries.getPartListEntriesOwnerAssembly().isSpecTypeRelevant()) {
            iPartsSpecType specType = filterEntries.getPartListEntriesOwnerAssembly().getSpecType();
            Set<String> specValiditiesAndQuantitiesForSpecType = vehicleDataCard.getSpecValiditiesAndQuantitiesForSpecType(filterEntries.getEtkProject(), specType).keySet();
            for (iPartsDataPartListEntry entry : filterEntries.getVisibleEntries()) {
                if (!iPartsFilterHelper.basicCheckSpecificationFilter(entry, specValiditiesAndQuantitiesForSpecType, this)) {
                    filterEntries.hideEntry(entry);
                }
            }
        }
    }

    /**
     * Zusammenfassen (mit Mengenaddition und Fehlerorten) mehrerer Teilepositionen unter verschiedenen Kriterien.
     * Siehe: DAIMLER-4260, "Verdichtung und Mengen-Addition in der Filterung"
     *
     * @param filterEntries       Normalerweise die gefilterte Stückliste, in der Filterabsicherung allerdings die
     *                            UNGEFILTERTE Stückliste
     * @param mergedEntriesKlfdNr
     * @return
     */
    private List<EtkDataPartListEntry> mergeSimilarPartListEntries(List<EtkDataPartListEntry> filterEntries, Set<String> mergedEntriesKlfdNr) {
        // Alle ähnlichen Stücklisteneinträge zu Listen zusammenfassen und diese dann untereinander vergleichen
        Map<String, List<iPartsDataPartListEntry>> similarPartListEntriesMap = getSimilarPartListEntriesMap(filterEntries, iPartsPLE -> {
            // DAIMLER-6820: Check auf identischen Wahlweise-Kenner aus DIALOG (nur 1. Zeichen vergleichen)
            // Der Wahlweise-Kenner im Feld K_WW kann hier nicht verwendet werden, weil dieser bei unterschiedlichen
            // DIALOG-Positionen auf jeden Fall unterschiedlich ist
            String ww = iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_WW);
            if (!ww.isEmpty()) {
                ww = ww.substring(0, 1); // 1. Zeichen des WW-Kenners
            }
            return StrUtils.stringArrayToString("\t", iPartsPLE.getFieldValue(EtkDbConst.FIELD_K_POS),
                                                iPartsPLE.getFieldValue(EtkDbConst.FIELD_K_MATNR),
                                                iPartsPLE.getFieldValue(iPartsConst.FIELD_K_STEERING),
                                                iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED),
                                                iPartsPLE.getFieldValue(iPartsConst.FIELD_K_EVENT_FROM),
                                                iPartsPLE.getFieldValue(iPartsConst.FIELD_K_EVENT_TO),
                                                ww,
                                                iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT),
                                                iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO),
                                                iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_HM),
                                                iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_M),
                                                iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SM),
                                                iPartsPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_AA));
        });

        // Jetzt die Stücklisteneinträge mit gleichem Schlüssel vergleichen
        Set<String> hiddenPartListEntryKLfdnrSet = new HashSet<>(); // Laufende Nummern der ausgefilterten Stücklisteneinträge
        Map<String, EtkDataPartListEntry> mergedPartListEntryMap = new HashMap<>(); // Laufende Nummern der zusammengeführten Stücklisteneinträge auf die entsprechenden Instanzen
        for (List<iPartsDataPartListEntry> similarPartListEntries : similarPartListEntriesMap.values()) {
            for (int i = 0; i < (similarPartListEntries.size() - 1); i++) {
                if (!hiddenPartListEntryKLfdnrSet.contains(similarPartListEntries.get(i).getAsId().getKLfdnr())) {
                    for (int j = i + 1; j < similarPartListEntries.size(); j++) {
                        iPartsDataPartListEntry currentEntry = similarPartListEntries.get(i);
                        iPartsDataPartListEntry followingEntry = similarPartListEntries.get(j);
                        if (!hiddenPartListEntryKLfdnrSet.contains(followingEntry.getAsId().getKLfdnr())) {
                            if (checkIsPartListEntrySimilar(currentEntry, followingEntry)) {
                                // Die Teileposition mit dem höchsten SDA (= K_DATEFROM) wird ausgegeben.
                                // Sind beide gleich, wird die ERSTE getroffene Teilepos ausgegeben.
                                String currentDateFrom = currentEntry.getFieldValue(iPartsConst.FIELD_K_DATEFROM);
                                String followingDateFrom = followingEntry.getFieldValue(iPartsConst.FIELD_K_DATEFROM);

                                iPartsDataPartListEntry hideEntry;
                                iPartsDataPartListEntry storeEntry;
                                int storeIndex;
                                if (currentDateFrom.compareTo(followingDateFrom) >= 0) { // currentDateFrom >= followingDateFrom
                                    hideEntry = followingEntry;
                                    storeEntry = currentEntry;
                                    storeIndex = i;
                                } else {
                                    hideEntry = currentEntry;
                                    storeEntry = followingEntry;
                                    storeIndex = j;
                                }

                                String hideEntryKLfdnr = hideEntry.getAsId().getKLfdnr();
                                if (!hiddenPartListEntryKLfdnrSet.contains(hideEntryKLfdnr)) { // Doppelte Übernahme vermeiden
                                    hiddenPartListEntryKLfdnrSet.add(hideEntryKLfdnr);

                                    // storeEntry klonen (hier explizit inkl. der Retail-Werkseinsatzdaten, damit keine
                                    // falschen Werkseinsatzdaten angezeigt werden)
                                    iPartsDataPartListEntry clonedEntry = (iPartsDataPartListEntry)storeEntry.cloneMe(storeEntry.getEtkProject());
                                    clonedEntry.setOwnerAssembly(storeEntry.getOwnerAssembly());
                                    iPartsFactoryData factoryDataForRetailWithoutReplacements = storeEntry.getFactoryDataForRetailWithoutReplacements();
                                    if (factoryDataForRetailWithoutReplacements != null) {
                                        factoryDataForRetailWithoutReplacements = factoryDataForRetailWithoutReplacements.cloneMe();
                                    }
                                    iPartsFactoryData factoryDataForRetail = storeEntry.getFactoryDataForRetail();
                                    if (factoryDataForRetail != null) {
                                        factoryDataForRetail = factoryDataForRetail.cloneMe();
                                    }
                                    clonedEntry.setFactoryDataForRetail(factoryDataForRetailWithoutReplacements, factoryDataForRetail);

                                    // Den Klon in similarPartListEntries und mergedPartListEntryMap setzen, damit der Stücklisteneintrag
                                    // der ungefilterten Stückliste nicht verändert wird
                                    similarPartListEntries.set(storeIndex, clonedEntry);
                                    mergedPartListEntryMap.put(clonedEntry.getAsId().getKLfdnr(), clonedEntry);

                                    mergeSimilarPartListEntryValues(hideEntry, clonedEntry);

                                    if (withFilterReason) {
                                        setFilterReasonForPartListEntry(hideEntry, REMOVE_DUPLICATES,
                                                                        "!!Verdichtung mit Mengenaddition (Teileposition mit gleichem Inhalt aber kleinerem \"Datum ab\")");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (hiddenPartListEntryKLfdnrSet.isEmpty() && mergedPartListEntryMap.isEmpty()) { // Es war keine Verdichtung notwendig
            return filterEntries;
        } else { // Wegen der Verdichtung ausgefilterte Stücklisteneinträge entfernen
            List<EtkDataPartListEntry> newFilterEntries = new DwList<>(filterEntries.size() - (withFilterReason ? 0 : hiddenPartListEntryKLfdnrSet.size()));
            for (EtkDataPartListEntry filterEntry : filterEntries) {
                String kLfdnr = filterEntry.getAsId().getKLfdnr();

                // Bei Filterung mit Filtergrund dürfen keine Stücklisteneinträge entfernt werden
                if (withFilterReason || !hiddenPartListEntryKLfdnrSet.contains(kLfdnr)) {
                    EtkDataPartListEntry mergedPartListEntry = mergedPartListEntryMap.get(kLfdnr);
                    if (mergedPartListEntry != null) { // Verdichteten Stücklisteneintrag zum Ergebnis hinzufügen
                        newFilterEntries.add(mergedPartListEntry);
                        mergedEntriesKlfdNr.add(kLfdnr);
                    } else {
                        newFilterEntries.add(filterEntry);
                    }
                }
            }
            return newFilterEntries;
        }
    }

    /**
     * Liefert alle Stücklisteneinträge gruppiert nach ihrem übergebenen {@link GroupKey}.
     *
     * @param filterEntries
     * @param groupKey
     * @return
     */
    private Map<String, List<iPartsDataPartListEntry>> getSimilarPartListEntriesMap(List<EtkDataPartListEntry> filterEntries, GroupKey groupKey) {
        Map<String, List<iPartsDataPartListEntry>> similarPartListEntriesMap = new HashMap<>();
        for (EtkDataPartListEntry partListEntry : filterEntries) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                if (withFilterReason && partListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED)) {
                    // hier über den Filtergrund ermitteln ob der Stücklisteneintrag übersprungen werden muss
                    continue;
                }

                iPartsDataPartListEntry iPartsPLE = (iPartsDataPartListEntry)partListEntry;
                String similarKey = groupKey.createKey(iPartsPLE);

                List<iPartsDataPartListEntry> similarPartListEntries = similarPartListEntriesMap.computeIfAbsent(similarKey,
                                                                                                                 key -> new ArrayList<>());
                similarPartListEntries.add(iPartsPLE);
            }
        }
        return similarPartListEntriesMap;
    }


    private boolean checkIsDuplicate(iPartsDataPartListEntry entry1, iPartsDataPartListEntry entry2) {
        // Wenn ein Feld in fieldsForDuplicateCheck nicht gleich ist, dann ist es kein Duplikat.
        for (String fieldName : FIELDS_FOR_DUPLICATE_CHECK) {
            if (!entry1.getFieldValue(fieldName).equals(entry2.getFieldValue(fieldName))) {
                return false;
            }
        }

        String posV1 = entry1.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV);
        String posV2 = entry2.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV);
        if (!posV1.equals(posV2)) {
            return false;
        }

        if (!entry1.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT)
                .equals(entry2.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT))) {
            return false;
        }

        if (!checkForIdenticalFootNotes(entry1, entry2)) {
            return false;
        }

        if (!checkIsFactoryDataSimilar(entry1, entry2)) {
            return false;
        }

        // Es darf nicht verdichtet werden falls die Teilepositionen unterschiedliche Ersetzungen haben
        if (!iPartsFilterHelper.checkIdenticalReplacements(entry1, entry2)) {
            return false;
        }

        return true;
    }

    private boolean checkForIdenticalFootNotes(iPartsDataPartListEntry entry1, iPartsDataPartListEntry entry2) {
        Collection<iPartsFootNote> footNotes1 = entry1.getFootNotes();
        Collection<iPartsFootNote> footNotes2 = entry2.getFootNotes();

        // Fußnoten auf Gleichheit prüfen
        if ((footNotes1 != null) && (footNotes2 != null)) {
            if (footNotes1.size() != footNotes2.size()) {
                return false;
            }

            // Über beide Fußnotenlisten iterieren, falls ein Teil eine andere FußnotenID hat, dann sind die Fußnoten unterschiedlich
            Iterator<iPartsFootNote> iter1 = footNotes1.iterator();
            Iterator<iPartsFootNote> iter2 = footNotes2.iterator();
            while (iter1.hasNext() && iter2.hasNext()) {

                iPartsFootNote note1 = iter1.next();
                iPartsFootNote note2 = iter2.next();
                if (!note1.getFootNoteId().equals(note2.getFootNoteId())) {
                    return false;
                }
            }
        }

        if (((footNotes1 != null) && (footNotes2 == null))
            || ((footNotes1 == null) && (footNotes2 != null))) {
            // Ein Teil hat Fußnoten, das andere nicht
            return false;
        }

        return true;
    }

    /**
     * Überträgt/addiert spezielle Werte von einem PartListEntry zum anderen.
     * Es wird darauf geachtet, dass die Datenübernahme nur einmalig geschieht.
     * Denn sonst wird die Menge bei jedem Durchlauf immer höher.
     *
     * @param fromEntry
     * @param toEntry
     */
    private void mergeSimilarPartListEntryValues(iPartsDataPartListEntry fromEntry, iPartsDataPartListEntry toEntry) {
        // Die zu betrachtenden Mengen holen.
        String quantityFrom = fromEntry.getFieldValue(iPartsConst.FIELD_K_MENGE);
        String quantityTo = toEntry.getFieldValue(iPartsConst.FIELD_K_MENGE);

        // Spezialfall: Besitzt eine Teilepos. die Menge NB = "Nach Bedarf", dann wird die Menge auf "Nach Bedarf" gesetzt ...
        if ((quantityFrom.equals(iPartsConst.MENGE_NACH_BEDARF)) || (quantityTo.equals(iPartsConst.MENGE_NACH_BEDARF))) {
            toEntry.setFieldValue(iPartsConst.FIELD_K_MENGE, iPartsConst.MENGE_NACH_BEDARF, DBActionOrigin.FROM_DB);
        } else {
            // ... ansonsten die Mengen addieren und setzen.
            int valueToSet = StrUtils.strToIntDef(quantityFrom, 0) + StrUtils.strToIntDef(quantityTo, 0);
            toEntry.setFieldValue(iPartsConst.FIELD_K_MENGE, Integer.toString(valueToSet), DBActionOrigin.FROM_DB);
        }

        // Die Fehlerorte werden ebenfalls zusammengefasst.
        List<String> fromFailLocList = StrUtils.toStringList(fromEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION), ",", false);
        if (!fromFailLocList.isEmpty()) { // Zusammenfassen ist nur dann notwendig, wenn fromEntry überhaupt Fehlerorte hat
            List<String> toFailLocList = StrUtils.toStringList(toEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION), ",", false);

            // Duplikate über ein LinkedHashSet vermeiden (Reihenfolge muss erhalten bleiben mit den Fehlerorten vom toEntry am Anfang)
            Set<String> failLocSet = new LinkedHashSet<>(toFailLocList);
            failLocSet.addAll(fromFailLocList);

            toEntry.setFieldValue(iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION, StrUtils.stringListToString(failLocSet, ","), DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Vergleicht verschiedene Werte anhand derer entschieden wird, ob zwei ähnliche Stücklisteneinträge auch wirklich als
     * "gleich" anzusehen sind im Sinne von DAIMLER-4260.
     *
     * @param entry1
     * @param entry2
     * @return
     */
    private boolean checkIsPartListEntrySimilar(iPartsDataPartListEntry entry1, iPartsDataPartListEntry entry2) {
        // Bei identischer Teileposition aber unterschiedlicher DIALOG-Position wird nur eine Teileposition mit addierten
        // Mengen und zusammengefassten Fehlerorten angezeigt.
        // Gleiche DIALOG-Position bedeutet: gleicher Hotspot, Teilenummer, Lenkung, Coderegel (String), Ergänzungstext,
        // sprachneutraler Text, BR, HM, M, SM und Ausführungsart (diese Strings werden alle vorher bereits in mergeSimilarPartListEntries()
        // auf Gleichheit geprüft) sowie identische Fußnoten und Werkseinsatzdaten (sofern relevant).
        // ...aber mindestens eines der Attribute POS oder PV unterscheiden sich
        if ((entry1.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE).equals(entry2.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE)))
            && (entry1.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV).equals(entry2.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV)))) {
            return false;
        }

        if (!checkIsFactoryDataSimilar(entry1, entry2)) {
            return false;
        }

        // Fußnoten müssen identisch sein
        if (!checkForIdenticalFootNotes(entry1, entry2)) {
            return false;
        }

        // Es darf nur verdichtet werden wenn zwischen den beiden Teilen KEINE Ersetzung existiert
        // (innerhalb der gesamten Ersetzungskette)
        if (iPartsFilterHelper.replacementChainContainsPartListEntry(entry1, entry2)) {
            return false;
        }
        if (iPartsFilterHelper.replacementChainContainsPartListEntry(entry2, entry1)) {
            return false;
        }

        return true;
    }

    private boolean checkIsFactoryDataSimilar(iPartsDataPartListEntry entry1, iPartsDataPartListEntry entry2) {
        // Falls die Werksdaten bei der Filterung ausgewertet werden, müssen diese ebenfalls identisch sein. Dies bedeutet,
        // gleiche PEM-Nummer und gleiche Modelljahrcode (Steuercode)

        iPartsFactoryData.ValidityType factoryDataValidity1 = entry1.getFactoryDataValidity();
        iPartsFactoryData.ValidityType factoryDataValidity2 = entry2.getFactoryDataValidity();
        // VALID ist der einzige Fall bei dem ein PEM Auswerte Flag gesetzt ist. Hier sind die Werkseinsatzdaten relevant
        // und müssen gleich sein zur Verdichtung
        // DAIMLER-8325: Falls beide Einträge andere Typen besitzen, können sich die Werkseinsatzdaten unterscheiden
        if ((factoryDataValidity1 != iPartsFactoryData.ValidityType.VALID) && (factoryDataValidity2 != iPartsFactoryData.ValidityType.VALID)) {
            factoryDataValidity1 = iPartsFactoryData.ValidityType.NOT_AVAILABLE;
            factoryDataValidity2 = iPartsFactoryData.ValidityType.NOT_AVAILABLE;
        }

        // Falls sich der Gültigkeitstyp der Werkseinsatzdaten der beiden Stücklisteneinträge bereits unterscheidet,
        // kann ein Vergleich übersprungen werden.
        if (factoryDataValidity1 != factoryDataValidity2) {
            return false;
        }

        if (factoryDataValidity1 == iPartsFactoryData.ValidityType.VALID) { // Muss wegen obiger Prüfung dann auch für entry2 gelten
            iPartsFactoryData factoryDataForRetail1 = entry1.getFactoryDataForRetail();
            iPartsFactoryData factoryDataForRetail2 = entry2.getFactoryDataForRetail();

            // Wenn schon die Anzahl der zu vergleichenden Werkseinsatzdaten unterschiedlich ist, können sie nicht gleich sein.
            if (factoryDataForRetail1.getFactoryDataMap().size() != factoryDataForRetail2.getFactoryDataMap().size()) {
                return false;
            }

            // Flags "PEM ab/bis auswerten" müssen identisch sein
            if ((factoryDataForRetail1.isEvalPemFrom() != factoryDataForRetail2.isEvalPemFrom())
                || (factoryDataForRetail1.isEvalPemTo() != factoryDataForRetail2.isEvalPemTo())) {
                return false;
            }

            // PEM ab/bis und Steuercode ab/bis der Werkseinsatzdaten müssen identisch sein
            for (Map.Entry<String, List<iPartsFactoryData.DataForFactory>> factoryDataEntry : factoryDataForRetail1.getFactoryDataMap().entrySet()) {
                List<iPartsFactoryData.DataForFactory> factoryDataList1 = factoryDataEntry.getValue();
                List<iPartsFactoryData.DataForFactory> factoryDataList2 = factoryDataForRetail2.getDataForFactory(factoryDataEntry.getKey());
                if ((factoryDataList2 == null) || (factoryDataList1.size() != factoryDataList2.size())) {
                    return false;
                }

                for (iPartsFactoryData.DataForFactory dataForFactory1 : factoryDataList1) {
                    String pemFrom1 = dataForFactory1.pemFrom;
                    String stCodeFrom1 = dataForFactory1.stCodeFrom;
                    String pemTo1 = dataForFactory1.pemTo;
                    String stCodeTo1 = dataForFactory1.stCodeTo;

                    boolean foundFrom = !factoryDataForRetail1.isEvalPemFrom();
                    boolean foundTo = !factoryDataForRetail1.isEvalPemTo();
                    for (iPartsFactoryData.DataForFactory dataForFactory2 : factoryDataList2) {
                        if (!foundFrom && dataForFactory2.pemFrom.equals(pemFrom1) && dataForFactory2.stCodeFrom.equals(stCodeFrom1)) {
                            foundFrom = true;
                        }
                        if (!foundTo && dataForFactory2.pemTo.equals(pemTo1) && dataForFactory2.stCodeTo.equals(stCodeTo1)) {
                            foundTo = true;
                        }
                        if (foundFrom && foundTo) {
                            break;
                        }
                    }

                    if (!foundFrom || !foundTo) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Filter mit den Werten aus der Datenkarte (auch virtuelle Datenkarte).
     *
     * @param partListEntry
     * @return
     */

    private boolean checkDatacardFilterOneEntry(iPartsDataPartListEntry partListEntry) {
        iPartsDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
        boolean isRetailPartList = ownerAssembly.isRetailPartList();
        List<AbstractDataCard> relevantDatacardsForAssembly = getRelevantDatacardsForAssembly(ownerAssembly);
        if (isRetailPartList) {
            // Lenkungsfilter ist manuell eingeschalten bzw. kommt aus der Datenkarte
            if (isSteeringFilterActive(ownerAssembly)) {
                if (!iPartsFilterHelper.basicCheckSteeringFilter(partListEntry, getSteeringValue(), false, this)) {
                    return false;
                }
            }

            // Getriebefilter
            if (isGearboxFilterActive(ownerAssembly)) {
                if (!iPartsFilterHelper.basicCheckGearBoxFilter(partListEntry, getGearboxValue(), this)) {
                    return false;
                }
            }

            // Codefilter
            if (isDatacardCodeFilterActive(ownerAssembly)) {
                // Ereignis-Filterung der Ereignisse an der Stücklistenposition
                String eventFromId = partListEntry.getFieldValue(iPartsConst.FIELD_K_EVENT_FROM);
                String eventToId = partListEntry.getFieldValue(iPartsConst.FIELD_K_EVENT_TO);
                if (!iPartsFilterHelper.basicCheckEventFilter(this, partListEntry, null, eventFromId, eventToId,
                                                              getCurrentDataCard().getEvent(), cachedData.getAssemblyData(ownerAssembly),
                                                              partListEntry.getEtkProject())) {
                    return false;
                }

                // Wenn es in keiner Datenkarte einen positiven Treffer für die Codes gibt, dann fliegt der Stücklisteneintrag raus
                boolean totalCodeFilterResult = relevantDatacardsForAssembly.isEmpty();
                for (AbstractDataCard datacard : relevantDatacardsForAssembly) {
                    if (datacard.hasTechnicalCodes()) {
                        if (iPartsFilterHelper.basicCheckCodeFilterForDatacard(partListEntry, datacard.getFilterCodes().getAllCheckedValues(), false, this)) {
                            totalCodeFilterResult = true;
                            break;
                        }
                    } else {
                        totalCodeFilterResult = true;
                        break;
                    }
                }
                if (!totalCodeFilterResult) {
                    return false;
                } else if (withFilterReason) {
                    clearFilterReasonForDataObject(partListEntry, true);
                }
            }
        }

        // SAA-Filter mit den Werten aus der Datenkarte (echt oder simulierte Datenkarte)
        if (isDatacardSaFilterActive(ownerAssembly)) {
            // Filterung der SAAs. Die SAAs werden in den freischwebenden SAs gefiltert und in den
            // Eldas-Produkten, die vom Typ Globalbaumuster sind.
            // Bei den anderen werden im Baumusterfilter alle Stücklisteneinträge durchgelassen.
            // Der Baumusterfilter wird bei den anderen Dokumentationsmethoden über die ModelValidity ermittelt
            // siehe auch: Confluence: Filter für ELDAS-Produkte https://confluence.docware.de/confluence/x/VwFUAQ

            // Das Abschalten der SA-Strich-Filterung wirkt nur innerhalb der freien SAs
            // In allen anderen Modulen wird normal gefiltert
            // Macht EPC so und soll so sein
            if (isRetailPartList) {
                boolean isSAAssembly = ownerAssembly.isSAAssembly();
                if (isSaStrichFilterActive(ownerAssembly) || !isSAAssembly) {
                    if (!filterSAAsFromDataCards(partListEntry, relevantDatacardsForAssembly, isSAAssembly)) {
                        return false;
                    }
                }
            } else if (ownerAssembly.isRetailSaStructurePartList() || ownerAssembly.getEbeneName().equals(iPartsConst.PARTS_LIST_TYPE_STRUCT_SA)) {
                // SAA-Filter um die freien SAs aus der Struktur zu entfernen
                // Die eigentlichen Stücklisteneinträge der SA werden oben gefiltert
                // Teste ob ein freischwebendes SA-Modul überhaupt in diesem Baumuster gültig ist
                // Nur ausfiltern wenn es in keiner Datenkarte einen positiven Treffer gibt
                boolean totalSaFilterResult = relevantDatacardsForAssembly.isEmpty();
                for (AbstractDataCard dataCard : relevantDatacardsForAssembly) {
                    if (iPartsFilterHelper.basicCheckSaModuleFilter(partListEntry, dataCard.getDataCardSaNumbers(), false, this)) {
                        totalSaFilterResult = true;
                        break;
                    }
                }
                if (!totalSaFilterResult) {
                    return false;
                } else if (withFilterReason) {
                    clearFilterReasonForDataObject(partListEntry, true);
                }
            }

        }

        if (isRetailPartList) {
            // Endnummernfilter
            if (isEndNumberFilterActive(ownerAssembly)) {
                List<AbstractDataCard> relevantDatacardsForFilterType = getRelevantDataCardsForFilterType(relevantDatacardsForAssembly,
                                                                                                          ownerAssembly.getDocumentationType(),
                                                                                                          END_NUMBER,
                                                                                                          ownerAssembly.getEtkProject());
                FilterCachedAssemblyData assemblyData = cachedData.getAssemblyData(ownerAssembly);
                // Ausfiltern wenn es in keiner Datenkarte einen positiv Treffer gibt
                boolean totalEndNumberFilterResult = relevantDatacardsForFilterType.isEmpty();
                for (AbstractDataCard dataCard : relevantDatacardsForFilterType) {
                    if (iPartsFilterHelper.basicCheckEndNumberFilter(partListEntry, assemblyData.partListEntriesValidForEndNumber,
                                                                     dataCard, this)) {
                        totalEndNumberFilterResult = true;
                        break;
                    }
                }
                if (!totalEndNumberFilterResult) {
                    return false;
                } else if (withFilterReason) {
                    clearFilterReasonForDataObject(partListEntry, true);
                }
            }
        }

        return true;
    }

    private boolean filterSAAsFromDataCards(iPartsDataPartListEntry partListEntry, List<AbstractDataCard> relevantDataCardsForAssembly, boolean isSAAssembly) {
        // Wenn es in keiner Datenkarte für den SAA Filter einen Treffer gibt, nur dann fliegt die Stückliste raus
        boolean totalSaaFilterResult = relevantDataCardsForAssembly.isEmpty();
        for (AbstractDataCard datacard : relevantDataCardsForAssembly) {
            if (iPartsFilterHelper.basicCheckSaStrichValidityFilter(partListEntry, datacard, false, isSAAssembly, this)) {
                totalSaaFilterResult = true;
                break;
            }
        }
        if (!totalSaaFilterResult) {
            return false;
        } else if (withFilterReason) {
            clearFilterReasonForDataObject(partListEntry, true);
        }
        return true;
    }

    /**
     * Enthält das übergebene Produkt eines der eingestellten Baumuster unter Berücksichtigung von AutoProductSelect?
     *
     * @param parentAssembly
     * @param modelNumber
     * @param numberOfModels
     * @param partListEntry
     * @return
     */
    private boolean virtualIdIsValidForModel(iPartsProduct product, EtkDataAssembly parentAssembly, String modelNumber,
                                             int numberOfModels, EtkDataPartListEntry partListEntry) {
        if (!iPartsFilterHelper.containsModelNo(product.getModelNumbers(parentAssembly.getEtkProject()), modelNumber)) {
            if (withFilterReason) {
                if (isMultiLayerFilter && (numberOfModels > 1)) {
                    setFilterReasonForPartListEntry(partListEntry, MODEL,
                                                    "!!Keines der %1 relevanten Baumuster ist im Produkt \"%2\" enthalten",
                                                    String.valueOf(numberOfModels), product.getAsId().getProductNumber());
                } else {
                    setFilterReasonForPartListEntry(partListEntry, MODEL,
                                                    "!!Baumuster \"%1\" ist nicht im Produkt \"%2\" enthalten",
                                                    modelNumber, product.getAsId().getProductNumber());
                }
            }
            return false;
        }
        return true;

    }

    /**
     * Ist dieses Produkt nach dem Auto-Product-Select noch gültig?
     *
     * @param product
     * @param partListEntry
     * @return
     */
    private boolean isValidProductAfterAutoSelect(iPartsProduct product, EtkDataPartListEntry partListEntry) {
        // Nur Fahrzeug-Produkte mit Auto-Product-Select filtern (Produkte mit leerem Aggregatetyp müssen auch gefiltert
        // werden, da diese noch keine importierten Stücklisten haben und der Aggregatetyp auch durchaus Fahrzeug sein könnte)
        if (!product.getAggregateType().isEmpty() && !product.getAggregateType().equals(iPartsConst.AGGREGATE_TYPE_CAR)) {
            return true;
        }
        VehicleDataCard vehicleDataCard = getVehicleDataCard(false);
        if (vehicleDataCard == null) {
            return true;
        }

        FinId finId = getFinId();
        // gültige FIN für Auto-Product-Select
        if (finId.isValidId()) {
            // 1. APS Check: Idents mit Millionenüberlauf
            boolean validAPSIdents = iPartsFilterHelper.checkAutoProductSelectIdents(partListEntry.getEtkProject(), finId.getWorldManufacturerIdentifier(),
                                                                                     finId.getFactorySign(), new iPartsModelId(finId.getFullModelNumber()),
                                                                                     finId.getSerialNumber(), product, finId.getSteering());
            if (!validAPSIdents) {
                if (withFilterReason) {
                    setFilterReasonForPartListEntry(partListEntry, MODEL,
                                                    "!!Auto-Product-Select: Produkt \"%1\" ist nicht gültig für die Endnummer der FIN",
                                                    product.getAsId().getProductNumber());
                }
                return false;
            }
        }

        // 2. Code Check: Codebedingung positiv -> true
        Set<String> positiveCodes = vehicleDataCard.hasTechnicalCodes() ? vehicleDataCard.getCodes().getAllCheckedValues() : null;
        boolean validAPSCodes = iPartsFilterHelper.checkAutoProductSelectCodes(product, positiveCodes);
        if (!validAPSCodes) {
            if (withFilterReason) {
                setFilterReasonForPartListEntry(partListEntry, MODEL,
                                                "!!Auto-Product-Select: Produkt \"%1\" ist nicht gültig für die Codes der Datenkarte",
                                                product.getAsId().getProductNumber());
            }
            return false;
        }


        return true;
    }

    /**
     * Überprüft, ob der übergebene virtuelle ID-String ein Produkt enthält, welches bei dem übergebenen Baumuster und nach
     * dem Auto-Product-Select gültig ist.
     *
     * @param virtualIdString
     * @param parentAssembly
     * @param modelNumber
     * @param numberOfModels
     * @param partListEntry
     * @return
     */
    private boolean checkProductValidForModel(String virtualIdString, EtkDataAssembly parentAssembly, String modelNumber,
                                              int numberOfModels, EtkDataPartListEntry partListEntry) {
        List<iPartsVirtualNode> virtualNodesPath = iPartsVirtualNode.parseVirtualIds(virtualIdString);
        boolean result = true;
        if (iPartsVirtualNode.isProductNode(virtualNodesPath)) {
            iPartsProductId productId = (iPartsProductId)virtualNodesPath.get(0).getId();
            iPartsProduct product = iPartsProduct.getInstance(parentAssembly.getEtkProject(), productId);
            // ist das BM im Produkt enthalten?
            result = virtualIdIsValidForModel(product, parentAssembly, modelNumber, numberOfModels, partListEntry);
            if (result) {
                // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster sollen beim Edit aber nicht bei den
                // Webservices berücksichtigt werden.
                List<iPartsProduct> productsForModel = iPartsProductHelper.getProductsForModelAndSessionType(parentAssembly.getEtkProject(),
                                                                                                             new iPartsModelId(modelNumber),
                                                                                                             null, null);
                if (!productsForModel.contains(product)) { // Im zu prüfenden Produkt ist das Baumuster gar nicht sichtbar
                    return false;
                }

                // DAIMLER-6814: Ist ein Baumuster nur einem Produkt zugeordnet, dann wird das Produkt zur Filterung
                // herangezogen, auch wenn die Ident-AB/BIS-Werte und/oder die Coderegel nicht zutreffen.
                int visibleProductCount = 0;
                for (iPartsProduct productForModel : productsForModel) {
                    if (productForModel.isRetailRelevant()) { // == FIELD_DP_PRODUCT_VISIBLE
                        visibleProductCount++;
                        if (visibleProductCount > 1) {
                            break;
                        }
                    }
                }

                // BM in Produkt enthalten -> APS-Check
                // APS-Check nur durchführen bei mehr als einem sichtbaren Produkt und bei nur unsichtbaren Produkten; bei nur einem
                // (unsichtbaren) Produkt in der Liste -> kein APS-Check durchführen wegen Anzeige in iPartsEdit
                result = (((visibleProductCount == 1) && product.isRetailRelevant()) || (productsForModel.size() == 1)
                          || ((visibleProductCount != 1) && isValidProductAfterAutoSelect(product, partListEntry)));
            }
        }
        return result;
    }

    /**
     * Baumusterfilterung für Zeichnungsreferenzen. Zeichnungsreferenzen können nur bezüglich ihrer Code, Baumustergültigkeiten und
     * Saa/BK-Gültigkeiten gefiltert werden. Analog zum Baumusterfilter über Stücklistenpositionen.
     *
     * @param assembly
     * @param models
     * @param image
     * @param activeFilterElems
     * @return
     */
    private boolean checkModelFilterForPicture(iPartsDataAssembly assembly, Collection<String> models, iPartsDataImage image, EnumSet<ModelFilterTypes> activeFilterElems) {
        if (models.isEmpty()) {
            return true;
        }

        for (String modelNumber : models) {
            if (StrUtils.isEmpty(modelNumber)) {
                continue;
            }
            if (assembly.isRetailPartList()) {
                // Es soll nur in der Retail-Stückliste und in den Webservices gefiltert werden
                iPartsDocumentationType documentationType = assembly.getDocumentationType();
                if (documentationType.isPKWDocumentationType()) {
                    EtkProject project = assembly.getEtkProject();
                    DIALOGModelFilter dialogModelFilter = cachedData.getDialogModelFilter(project, modelNumber);
                    // Baumusterbildende Codes
                    if (activeFilterElems.contains(ModelFilterTypes.CODE)) {
                        Set<String> modelBuildingCodeSet = dialogModelFilter.getModelBuildingCodeSet(); // Alle BM-bildenden Code des Baumusters

                        // Für die Code-Prüfung nur die tatsächlichen baumusterbildenden Code von allen relevanten Datenkarten
                        // für das Baumuster verwenden
                        Set<String> positiveModelBuildingCodeSet = cachedData.getModelBuildingCodeSetFromDataCards(assembly,
                                                                                                                   modelNumber);

                        if (!iPartsFilterHelper.basicCheckCodeFilter(image.getFieldValue(iPartsConst.FIELD_I_CODES),
                                                                     positiveModelBuildingCodeSet,
                                                                     null, modelBuildingCodeSet, null)) {
                            return false;
                        }
                    }
                }

                // Filterung Baumustergültigkeit (bei SAs nicht notwendig)
                if (!assembly.isSAAssembly() && activeFilterElems.contains(ModelFilterTypes.MODEL)) {
                    if (!iPartsFilterHelper.basicCheckModelValidityFilter(image.getFieldValueAsArray(iPartsConst.FIELD_I_MODEL_VALIDITY), modelNumber)) {
                        return false;
                    }
                }

                // Filterung Saa/BK-Gültigkeiten
                if (activeFilterElems.contains(ModelFilterTypes.SA)) {
                    FilterCachedModelData modelData = cachedData.getModelData(assembly, modelNumber);
                    if (!iPartsFilterHelper.basicCheckSaStrichValidityFilter(image.getFieldValueAsArray(iPartsConst.FIELD_I_SAA_CONSTKIT_VALIDITY), modelData.getSaas())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Baumusterfilter. Der Baumusterfilter filtert mehr als nur das Baumuster, welche Filter angewendet werden sollen übergibt man
     * mit activeFilterElems. Normalerweise FilterTypes.all()
     *
     * @param models
     * @param filterEntries     Alle zu einem PartsListEntry für die Filterung relevanten Entries (Filterstatus wird hier gespeichert)
     * @param activeFilterElems
     * @param buildDate         Zu diesem Datum ungültige rausfiltern. Für Alle -1
     * @return
     */
    private boolean checkModelFilter(Collection<String> models, iPartsFilterPartsEntries filterEntries, EnumSet<ModelFilterTypes> activeFilterElems,
                                     long buildDate) {
        if (models.isEmpty()) {
            // Aus der Suche kann man hier hinkommen. Der Datensatz ist so zusagen willkürlich aus der Datenbank
            // und hat mit den eingestellten Baumustern nichts zu tun. Über die normale Navigation kommt man hier nicht
            // hin, da diese Knoten über die Stukturfilterung schon ausgeblendet sind.
            filterEntries.hideAll(this, MODEL, "!!Kein gültiges Baumuster für dieses Modul");
            return true;
        }

        iPartsDataAssembly assembly = filterEntries.getPartListEntriesOwnerAssembly();
        if (assembly == null) {
            Logger.getLogger().throwRuntimeException("iPartsDataAssembly must be not null!");
            return false;
        }
        EtkProject project = assembly.getEtkProject();

        // Lenkung für die Filterung von Lenkungs-bezogenen Idents
        String steering = null;
        if (isSteeringFilterActive(assembly)) {
            steering = getSteeringValue();
            if (steering.isEmpty()) {
                steering = null;
            }
        }

        Map<String, iPartsFilterPartsEntries> tempFilterEntries = new TreeMap<>();
        boolean isRetailPartList = assembly.isRetailPartList();
        if (models.size() > 1) {
            for (String model : models) {
                tempFilterEntries.put(model, filterEntries.cloneMe());
            }

            // Bei mehreren gültigen Baumustern für die Stückliste (möglich bei einer Fahrzeug-Baumuster-Datenkarte mit
            // mehreren Aggregate-Baumuster-Datenkarten für dieselbe Stückliste) findet keine Vorfilterung für alle
            // Werkseinsatzdaten von ALLEN Stücklisteneinträgen statt -> nur alte Baumuster-Zeitscheiben-Filterung weiter unten
            if (assembly.getFilteredWithModelNumber() != null) {
                assembly.clearAllFactoryDataForRetailForPartList();
            }
        } else {
            String modelNumber = models.iterator().next();
            tempFilterEntries.put(modelNumber, filterEntries);

            if (isRetailPartList) {
                // Wir sind in einer Retail-Stückliste und in keinem Strukturknoten oder der Konstruktiuon
                iPartsDocumentationType documentationType = assembly.getDocumentationType();

                // Die freischwebenden SAs müssen etwas anders gefiltert werden. Ausführungsart etc. sind in den SAs ja nicht gesetzt
                boolean isSa = assembly.isSAAssembly();

                // DIALOG-Filterung
                if (documentationType.isPKWDocumentationType()) {
                    // Zeitscheibenfilter
                    if (!isSa && activeFilterElems.contains(ModelFilterTypes.TIME_SLICE)) {
                        // Baumuster-Filter mit verschiedenen Baumusternummern hintereinander darf nie passieren -> RuntimeException im
                        // DEVELOPMENT-Modus und ansonsten nur Error-Log
                        String filteredWithModelNumber = assembly.getFilteredWithModelNumber();
                        boolean doPreFilter = true;
                        // Stückliste wurde schon vorgefiltert
                        if (filteredWithModelNumber != null) {
                            // Check, ob das aktuelle BM das BM ist, mit dem die Stückliste vorgefiltert wurde
                            if (!Utils.objectEquals(filteredWithModelNumber, modelNumber)) {
                                // Baumuster-Filter mit mehreren Baumustern! Stückliste mit dem neuen BM vorfiltern!
                                String errorText = "Assembly \"" + assembly.getAsId().getKVari() + "\" is filtered by multiple models in the DIALOG model filter: "
                                                   + filteredWithModelNumber + ", " + modelNumber;
                                if (Constants.DEVELOPMENT) {
                                    Logger.getLogger().throwRuntimeException(errorText);
                                } else {
                                    Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, errorText + "\n" + Utils.getStackTrace());
                                }
                            } else {
                                doPreFilter = false;
                            }
                        }
                        // Vorfiltern, falls das erste Mal oder anderes BM
                        if (doPreFilter) {
                            FilterCachedModelData modelData = cachedData.getModelData(assembly, modelNumber);

                            // Vorfilterung für alle Werkseinsatzdaten von ALLEN Stücklisteneinträgen (auch solche, die evtl. bereits
                            // ausgefiltert wurden) basierend auf der Baumuster-Zeitscheibe durchführen
                            for (EtkDataPartListEntry partListEntry : assembly.getPartListUnfiltered(null)) {
                                if (partListEntry instanceof iPartsDataPartListEntry) {
                                    iPartsFilterHelper.basicCheckTimeSlicePreFilter((iPartsDataPartListEntry)partListEntry,
                                                                                    modelData.getProductModelsValidFrom(),
                                                                                    modelData.getProductModelsValidTo(),
                                                                                    modelNumber, steering, this);
                                }
                            }
                            assembly.setFilteredWithModelNumber(modelNumber);
                        }
                    }
                }
            }
        }

        Set<EtkDataPartListEntry> validPartListEntries = new HashSet<>();

        for (String modelNumber : models) {
            if (StrUtils.isEmpty(modelNumber)) {
                continue;
            }

            iPartsFilterPartsEntries currentFilterEntries = tempFilterEntries.get(modelNumber);

            if (isRetailPartList) {
                // Wir sind in einer Retail-Stückliste und in keinem Strukturknoten oder der Konstruktiuon
                iPartsDocumentationType documentationType = assembly.getDocumentationType();

                // Die freischwebenden SAs müssen etwas anders gefiltert werden. Ausführungsart etc. sind in den SAs ja nicht gesetzt
                boolean isSa = assembly.isSAAssembly();

                // DIALOG-Filterung
                if (documentationType.isPKWDocumentationType()) {
                    DIALOGModelFilter dialogModelFilter = cachedData.getDialogModelFilter(project, modelNumber, (documentationType != iPartsDocumentationType.DIALOG));

                    // Ausführungsart (nicht bei freischwebenden SAAs)
                    if (!isSa && activeFilterElems.contains(ModelFilterTypes.AA)) {
                        for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                            if (!iPartsFilterHelper.basicCheckAAFilter(partListEntry, dialogModelFilter.getAA(), this)) {
                                currentFilterEntries.hideEntry(partListEntry);
                            }
                        }
                    }

                    // Zeitscheibenfilter
                    if (!isSa && activeFilterElems.contains(ModelFilterTypes.TIME_SLICE)) {
                        // Zeitscheiben-Filter der Werksdaten zur PV (nur gültige Werke) zur Baumuster-Zeitscheibe
                        FilterCachedModelData modelData = cachedData.getModelData(assembly, modelNumber);

                        for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                            // Unterscheidung zwischen der neuen Filtermethode (geht nur bei genau einem gültigen Baumuster
                            // für die Stückliste) und der alten Filtermethode (bei mehreren gültigen Baumustern)
                            boolean filterResult;
                            if (models.size() == 1) {
                                // Jetzt nach obigem Vorfilter die eigentliche Filterung der Stücklisteneinträge basierend
                                // auf den gefilterten Werkseinsatzdaten durchführen
                                filterResult = iPartsFilterHelper.basicCheckTimeSliceFilterForOneModel(partListEntry,
                                                                                                       modelData.getProductModelsValidFrom(),
                                                                                                       modelData.getProductModelsValidTo(), this);
                            } else {
                                filterResult = iPartsFilterHelper.basicCheckTimeSliceFilterForMultipleModels(partListEntry,
                                                                                                             modelData.getProductModelsValidFrom(),
                                                                                                             modelData.getProductModelsValidTo(),
                                                                                                             models, steering, this);
                            }
                            if (!filterResult) {
                                currentFilterEntries.hideEntry(partListEntry);
                            }
                        }
                    }

                    // Baumusterbildende Codes (nicht bei freischwebenden SAAs)
                    // Diese Filterung darf erst nach dem Zeitscheibenfilter gemacht werden, da nur danach die Werkseinsatzdaten
                    // korrekt gefiltert sind, die zur Bestimmung der Zeitscheibe des Stücklisteneintrags benötigt werden
                    // (die Zeitscheibe des Stücklisteneintrags wiederum wird im Code-Filter benötigt)
                    if (!isSa && activeFilterElems.contains(ModelFilterTypes.CODE)) {
                        // DAIMLER-7002 Zeitscheibe für BM-Scoring berechnen (wenn es mehrere gültige Baumuster für das
                        // Modul gibt, bleibt am Ende die berechnete Zeitscheibe für das BM-Scoring vom letzten Baumuster
                        // am Stücklisteneintrag stehen, was aber egal ist, weil diese Daten sowieso nur im BM-Scoring
                        // direkt danach verwendet werden)
                        FilterCachedModelData modelData = cachedData.getModelData(assembly, modelNumber);
                        for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                            partListEntry.setTimeSliceDates(true, modelData.getProductModelsValidFrom(), modelData.getProductModelsValidTo());
                        }

                        dialogModelFilter.filterByCodeRule(currentFilterEntries, cachedData.getModelBuildingCodeSetFromDataCards(assembly, modelNumber, dialogModelFilter.isFilterWithAAModelBuildingCode()),
                                                           buildDate, this);
                    }

                    // Wegfallsachnummer Filter
                    if (!isSa && activeFilterElems.contains(ModelFilterTypes.OMITTED_PART)) {
                        Set<String> modelBuildingCodes = dialogModelFilter.getModelBuildingCodeSet(); // Alle BM-bildenden Code des Baumusters
                        iPartsOmittedParts omittedPartsCacheLocal = getOmittedPartsCache(project);
                        for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                            if (!iPartsFilterHelper.basicCheckOmittedPartsInModelFilter(partListEntry, modelBuildingCodes,
                                                                                        documentationType, omittedPartsCacheLocal, this)) {
                                currentFilterEntries.hideEntry(partListEntry);
                            }
                        }
                    }
                }

                // Lenkung
                if (activeFilterElems.contains(ModelFilterTypes.STEERING)) {
                    // Im Baumuster kann eben auch die Lenkung angegeben sein und deshalb muss beim Baumusterfilter auch der Lenkungsfilter manchmal ausgewertet werden
                    FilterCachedModelData modelData = cachedData.getModelData(assembly, modelNumber);
                    for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                        if (!iPartsFilterHelper.basicCheckSteeringFilter(partListEntry, modelData.getSteering(), true, this)) {
                            currentFilterEntries.hideEntry(partListEntry);
                        }
                    }
                }

                // Filterung Baumustergültigkeit für alle Eldas Dokumethoden (nicht notwendig für SAs)
                // Wegen Einheitlichkeit auch bei Dialog, obwohl im Moment dort nie Daten vorhanden sind
                if (!isSa && activeFilterElems.contains(ModelFilterTypes.MODEL)) {
                    boolean datacardSaFilterActive = isDatacardSaFilterActive(assembly);
                    for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                        // DAIMLER-6067: Für eine Teileposition wird die Baumustergültigkeit ignoriert, wenn:
                        // eine SAA-Gültigkeit an der Teileposition vorliegt, der Datenkarten-SA Filter aktiviert ist,
                        // es sich um die Dokumethoden BCT/E, BCS+/GBM, BCS+ oder +/- handelt, es sich um eine echte Datenkarte handelt
                        boolean doNotCheckBM = !partListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_SA_VALIDITY).isEmpty() &&
                                               currentDataCard.isDataCardLoaded() &&
                                               documentationType.isTruckDocumentationType() &&
                                               datacardSaFilterActive;
                        if (!doNotCheckBM && !iPartsFilterHelper.basicCheckModelValidityFilter(partListEntry, modelNumber, this)) {
                            currentFilterEntries.hideEntry(partListEntry);
                        }
                    }
                }

                // SAA-Filter um die Baumusterteile zu ermitteln
                if (activeFilterElems.contains(ModelFilterTypes.SA)) {
                    // Filterung der SAAs. Die SAAs werden in den freischwebenden SAs gefiltert und in den
                    // Eldas-Produkten, die vom Typ Globalbaumuster sind.
                    // Bei den anderen werden im Baumusterfilter alle Stücklisteneinträge durchgelassen.
                    // Der Baumusterfilter wird bei den anderen Dokumentationsmethoden über die ModelValidity ermittelt
                    // siehe auch: Confluence: Filter für ELDAS-Produkte https://confluence.docware.de/confluence/x/VwFUAQ

                    // Die eigentlichen freischwebenden SA-Module werden später bei der Struktur rausgefiltert (siehe unten)
                    boolean isGBM = (documentationType == iPartsDocumentationType.BCS_PLUS_GLOBAL_BM);
                    if (isGBM || isSa) {

                        // Falls der SA-Strich-Filter nicht gesetzt ist, so filtere die SA-Strich in den freien SAs nicht
                        // In allen anderen Stücklisten soll trotzdem nach SA-Strich gefiltert werden
                        if (activeFilterElems.contains(ModelFilterTypes.SA_STRICH) || isGBM) {

                            FilterCachedModelData modelData = cachedData.getModelData(assembly, modelNumber);

                            for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                                if (!iPartsFilterHelper.basicCheckSaStrichValidityFilter(partListEntry, modelData.getSaas(),
                                                                                         true, isSa, this)) {
                                    currentFilterEntries.hideEntry(partListEntry);
                                }
                            }
                        }
                    }
                }

            } else if (assembly.isRetailStructurePartList()) {
                //Filterung in Retail-Strukturbaum
                for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                    AssemblyId assemblyId = partListEntry.getOwnerAssemblyId();
                    if (iPartsVirtualNode.isVirtualId(assemblyId)) {
                        if (!checkProductValidForModel(assemblyId.getKVari(), assembly, modelNumber, models.size(), partListEntry)) {
                            currentFilterEntries.hideEntry(partListEntry);
                            continue;
                        }
                    }

                    PartId partId = partListEntry.getPart().getAsId();
                    if (iPartsVirtualNode.isVirtualId(partId)) {
                        if (!checkProductValidForModel(partId.getMatNr(), assembly, modelNumber, models.size(), partListEntry)) {
                            currentFilterEntries.hideEntry(partListEntry);
                        }

                    }
                }
            } else if (assembly.isRetailSaStructurePartList()) {
                // SAA-Filter um die freien SAs aus der Struktur zu entfernen
                // Die eigentlichen Stücklisteneinträge der SA werden oben gefiltert
                if (activeFilterElems.contains(ModelFilterTypes.SA)) {
                    // Teste ob ein freischwebendes SA-Modul überhaupt in diesem Baumuster gültig ist
                    FilterCachedModelData modelData = cachedData.getModelData(assembly, modelNumber);

                    for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                        if (!iPartsFilterHelper.basicCheckSaModuleFilter(partListEntry, modelData.getSaModules(), true, this)) {
                            currentFilterEntries.hideEntry(partListEntry);
                        }
                    }
                }
            } else if (assembly.isStructurePartList() && !assembly.isRoot()) {
                // Filterung im Strukturbaum; hier sollen alle Einträge, die keine Kindknoten mehr haben, entfernt werden.
                // Die Filterung wird nur für Knoten, deren Parent nicht der Root ist, durchgeführt, damit die erste Ebene
                // (Aggregate, Fahrzeuge, Spezialkataloge, Konstruktion) auf jeden Fall erhalten bleibt.
                for (iPartsDataPartListEntry partListEntry : currentFilterEntries.getVisibleEntries()) {
                    iPartsAssemblyId destinationAssemblyId = partListEntry.getDestinationAssemblyId();
                    // Filterung macht nur für virtuelle Assemblies Sinn, sonst wären wir schon zu tief im Baum gelandet
                    if (partListEntry.isAssembly() && destinationAssemblyId.isVirtual()) {
                        // Im Cache merken, ob eine Assembly sichtbare Kinder hat oder nicht, da sonst Stücklisten inkl.
                        // deren Filterung sehr häufig aufgerufen werden würden
                        Boolean hasSubAssemblies = cachedData.hasSubAssemblies(destinationAssemblyId);
                        if (hasSubAssemblies == null) { // Neuen Cache-Eintrag erzeugen
                            hasSubAssemblies = true;
                            iPartsDataAssembly destinationAssembly = getiPartsDataAssembly(project, destinationAssemblyId);
                            if (destinationAssembly != null) {
                                // Nur bis zur Strukturebene PARTS_LIST_TYPE_PRODUCT prüfen. Weiter unten im Baum kommt
                                // dann die normale Filterung zum Einsatz
                                if ((destinationAssembly.isStructurePartList() || destinationAssembly.isRetailStructurePartList()) &&
                                    !destinationAssembly.hasSubAssemblies(true)) {
                                    hasSubAssemblies = false;
                                }
                            }
                            cachedData.setHasSubAssemblies(destinationAssemblyId, hasSubAssemblies);
                        }

                        if (!hasSubAssemblies) {
                            filterEntries.hideEntry(partListEntry);
                        }
                    }
                }
            }

            if (models.size() > 1) {
                // Prüfen welche Stücklisteneinträge für dieses Baumuster gültig sind
                validPartListEntries.addAll(currentFilterEntries.getVisibleEntries());
            }
        }

        if (models.size() > 1) {
            // alle gemerkten lfrdnr in den eigentlichen filterEntries setzen
            filterEntries.setVisibleEntries(validPartListEntries, this, MODEL, "!!Stücklisteneintrag ungültig für Baumuster");
        }
        return true;
    }

    /**
     * Für Filterung benötigte Felder.
     * Wir brauchen diese Info weil die Felder im Zweifel erst Mal geladen werden müssen.
     *
     * @param neededTables Liste der Tabellen für die Felder zurück geliefert werden soll
     * @return Liste von Feldern (Format <TabellenName>.<FeldName>))
     */
    public Set<String> getActiveFilterFields(Set<String> neededTables) {
        Set<String> result = null;

        boolean containsKatalogTable = neededTables.contains(EtkDbConst.TABLE_KATALOG);
        boolean containsMatTable = neededTables.contains(EtkDbConst.TABLE_MAT);
        boolean containsImagesTable = neededTables.contains(EtkDbConst.TABLE_IMAGES);

        // Die benötigten Filter-Felder IMMER zurückliefern (auch bei deaktiviertem Filter-Hauptschalter bzw. einzelnen
        // deaktivierten Filtern), damit Stücklisten nach dem Einschalten von Filtern nicht aufgrund von fehlenden Feldern
        // neu aus der DB geladen werden müssen
        if (containsKatalogTable) {
            if (!containsMatTable && !containsImagesTable) {
                return ACTIVE_FILTER_FIELDS_KATALOG;
            } else {
                result = new HashSet<>(ACTIVE_FILTER_FIELDS_KATALOG);
            }
        }

        if (containsMatTable) {
            if (!containsKatalogTable && !containsImagesTable) {
                return ACTIVE_FILTER_FIELDS_MAT;
            } else {
                if (result == null) {
                    result = new HashSet<>(ACTIVE_FILTER_FIELDS_MAT);
                } else {
                    result.addAll(ACTIVE_FILTER_FIELDS_MAT);
                }
            }
        }

        if (containsImagesTable) {
            if (!containsKatalogTable && !containsMatTable) {
                return ACTIVE_FILTER_FIELDS_IMAGES;
            } else {
                result.addAll(ACTIVE_FILTER_FIELDS_IMAGES);
            }
        }

        return result;
    }

    /**
     * DAIMLER-6972: Vergleich der Werkseinsatzdaten der Farbvariantentabelle über alle Werke mit der Zeitscheibe aller Baumuster
     * Also die Zeitscheibe, die sich aus Minimal- und Maximal-Gültigkeitswert aller gültigen Baumuster für das Produkt ergibt
     * -> es muss mindestens eine Überlappung zwischen der Zeitscheibe und den Werkseinsatzdaten geben.
     *
     * @param ownerAssembly
     * @param colorTableToPart
     * @param filter
     * @return
     */
    public static boolean isColorTableValidForModelTimeSlice(iPartsDataAssembly ownerAssembly, iPartsColorTable.ColorTableToPart colorTableToPart,
                                                             iPartsFilter filter) {
        if ((colorTableToPart == null) || (colorTableToPart.getFactoryData() == null)) {
            return false;
        }
        iPartsProductId productId = ownerAssembly.getProductIdFromModuleUsage();
        if (productId != null) {
            EtkProject project = ownerAssembly.getEtkProject();
            iPartsProduct product = iPartsProduct.getInstance(project, productId);
            long minModelsValidFrom = product.getMinModelsValidFrom(project);
            long maxModelsValidTo = product.getMaxModelsValidTo(project);
            for (List<iPartsColorFactoryDataForRetail.DataForFactory> dataForFactoryList : colorTableToPart.getFactoryData().getFactoryDataMap().values()) {
                for (iPartsColorFactoryDataForRetail.DataForFactory dataForFactory : dataForFactoryList) {
                    if (iPartsFilterTimeSliceHelper.isInModelTimeSlice(dataForFactory.dateFrom, dataForFactory.dateTo,
                                                                       minModelsValidFrom, maxModelsValidTo)) {
                        return true;
                    }
                }
            }

            if ((filter != null) && filter.isWithFilterReason()) {
                filter.setFilterReasonForColorTableToPart(colorTableToPart.getDataColorTableToPart(false, project),
                                                          iPartsColorFilter.ColorTableToPartFilterTypes.MODEL,
                                                          "!!Keine gültigen Werkseinsatzdaten überlappen mit der maximalen Baumuster-Zeitscheibe \"%1\" bis \"%2\" vom Produkt \"%3\"",
                                                          iPartsFilterHelper.getFilterReasonDate(minModelsValidFrom, project),
                                                          iPartsFilterHelper.getFilterReasonDate(maxModelsValidTo, project),
                                                          productId.getProductNumber());
            }

            return false;
        } else {
            // Keine Prüfung mit der maximalen Baumusterzeitscheibe möglich, weil kein Produkt ermittelt
            // werden kann -> Farbvariantentabelle als gültig kennzeichnen
            return true;
        }
    }

    /**
     * Filtert die übergebenen Farbvarianten für den Retail zum übergebenen <i>partListEntry</i> für das aktuelle Baumuster
     * bzw. Datenkarte und liefert das Ergebnis zurück.
     * Aktuell gibt es keine Farbvarianten bei Aggregaten, also muss dieser Filter nicht für die mehrstufige
     * Filterung angepasst werden
     *
     * @param colorTableForRetailWithoutFilter
     * @param partListEntry
     * @return Gefilterter Klon von <i>colorTableForRetailWithoutFilter</i> falls der
     * Farbfilter oder erweiterte Farbfilter aktiv und ein Baumuster bzw.
     * Datenkarte gesetzt ist; ansonsten wird das Original-Objekt zurückgegeben
     */
    public iPartsColorTable getColorTableForRetailFiltered(iPartsColorTable colorTableForRetailWithoutFilter,
                                                           iPartsDataPartListEntry partListEntry) {
        if (colorTableForRetailWithoutFilter == null) {
            return null;
        }

        iPartsDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
        iPartsDocumentationType documentationType = ownerAssembly.getDocumentationType();
        EtkProject project = partListEntry.getEtkProject();

        // Filterung nur bei DIALOG-Stücklisten oder bei ELDAS Stücklisten, wenn die TU markiert wurde (Farb-Tabellenfußnoten vor PRIMUS Variantentabellen).
        if (documentationType.isPKWDocumentationType()) {
            // Laut Daimler nur für Fahrzeuge relevant, daher direkte Verwendung von currentDataCard
            boolean modelFilterActiveAndValid = isModelFilterActiveAndValid(ownerAssembly);
            boolean datacardCodeFilterActiveAndValid = isDatacardCodeFilterActive(ownerAssembly) && (currentDataCard.hasFilterTechnicalCodes()
                                                                                                     || (currentDataCard.getEvent() != null));
            boolean endNumberFilterActive = isEndNumberFilterActive(ownerAssembly);
            if (endNumberFilterActive && currentDataCard.isVehicleDataCard()) {
                FinId fin = ((VehicleDataCard)currentDataCard).getFinId();
                if ((fin == null) || !fin.isValidId()) {
                    endNumberFilterActive = false;
                }
            }
            boolean extendendColorFilterActiveAndValid = isExtendendColorFilterActive(ownerAssembly) && currentDataCard.hasFilterTechnicalCodes();
            if (extendendColorFilterActiveAndValid) {
                // Eigentlich ist die folgende Überprüfung unnötig, da weiter oben schon auf DIALOG-Stückliste überprüft
                // wird und Stücklisteneinträge in Aggregaten laut Daimler sowieso keine Farbvariantentabellen referenzieren
                List<AbstractDataCard> relevantDatacards = getRelevantDatacardsForAssembly(ownerAssembly);
                relevantDatacards = getRelevantDataCardsForFilterType(relevantDatacards, documentationType, EXTENDED_COLOR,
                                                                      partListEntry.getOwnerAssembly().getEtkProject());
                if (relevantDatacards.isEmpty()) {
                    extendendColorFilterActiveAndValid = false;
                }
            }

            // Ist mindestens einer der relevanten Filter aktiv?
            if (modelFilterActiveAndValid || datacardCodeFilterActiveAndValid || endNumberFilterActive || extendendColorFilterActiveAndValid) {
                // Gefilterte Retail-Daten erstellen
                iPartsColorTable colorTableForRetailFiltered = new iPartsColorTable();
                for (iPartsColorTable.ColorTable colorTable : colorTableForRetailWithoutFilter.getColorTablesMap().values()) {
                    boolean colorTableValid = false;
                    for (iPartsColorTable.ColorTableToPart colorTableToPart : colorTable.colorTableToPartsMap.values()) {
                        if (isWithFilterReason()) {
                            clearFilterReasonForDataObject(colorTableToPart.getDataColorTableToPart(false, project), true);
                        }

                        List<iPartsColorTable.ColorTableContent> colorTableContentsFiltered = null;
                        if (colorTableToPart.getFactoryData() != null) {
                            if (modelFilterActiveAndValid) {
                                colorTableValid = isColorTableValidForModelTimeSlice(ownerAssembly, colorTableToPart, this);
                            } else {
                                colorTableValid = true;
                            }

                            if (colorTableValid && endNumberFilterActive) {
                                // Endnummernfilterung für Farbtabelle zu Teil
                                colorTableValid = iPartsFilterHelper.checkColorEndNumberFilter(getVehicleDataCard(false), project,
                                                                                               colorTableToPart, this);
                            }

                            if (colorTableValid || isWithFilterReason()) { // Bei Filterabsicherung auch ausgefilterte Farbvariantentabellen anzeigen
                                if (isWithFilterReason()) {
                                    for (iPartsColorTable.ColorTableContent colorTableContent : colorTable.colorTableContents) {
                                        clearFilterReasonForDataObject(colorTableContent.getDataColorTableContent(false, project), true);
                                    }
                                }

                                // Set mit allen Baumustern deren Zeitscheiben sich mit den Zeitscheiben der Farb-Werkseinsatzdaten überschneiden
                                Set<String> validModels = new LinkedHashSet<>();
                                Set<String> modelBuildingCodeSet;

                                // Baumuster-Filter
                                if (modelFilterActiveAndValid) {
                                    colorTableContentsFiltered = checkModelFilterForColors(project, ownerAssembly,
                                                                                           colorTable.colorTableContents, validModels,
                                                                                           getRelevantModelNumbers(ownerAssembly));
                                } else {
                                    // Keine Baumuster-Filterung
                                    colorTableContentsFiltered = colorTable.colorTableContents;
                                }

                                // Weitere Filter nach dem Baumuster-Filter
                                List<iPartsColorTable.ColorTableContent> colorTableContentsFilterReason = null;
                                if (colorTableContentsFiltered != null) {
                                    List<iPartsColorTable.ColorTableContent> colorTableContentsModelFiltered = colorTableContentsFiltered;
                                    colorTableContentsFiltered = null;
                                    for (iPartsColorTable.ColorTableContent colorTableContent : colorTableContentsModelFiltered) {
                                        boolean validColorTableContent = true;
                                        if (datacardCodeFilterActiveAndValid) {
                                            // Ergeignisfilterung für Farben:
                                            // - AS Ereignisse haben Vorrang
                                            // - Unterdrückt das AS Ereignis das Konstruktions-Ereignis, dann wird so weitergearbeitet
                                            // als ob kein Ereignis vorhanden wäre
                                            // - Wenn keine Ereignisse vorhanden sind, dann is der Datensatz vorerst gültig

                                            // Ereignis-Ab der Farbe bestimmen
                                            String eventFromId = colorTableContent.getFilterEventFromId();

                                            // Ereignis-Bis der Farbe bestimmen
                                            String eventToId = colorTableContent.getFilterEventToId();

                                            // Kein Ereignis vorhanden -> vorerst gültig
                                            if (!StrUtils.isEmpty(eventFromId, eventToId)) {
                                                validColorTableContent &= iPartsFilterHelper.basicCheckEventFilter(this, null,
                                                                                                                   colorTableContent,
                                                                                                                   eventFromId, eventToId,
                                                                                                                   getCurrentDataCard().getEvent(),
                                                                                                                   cachedData.getAssemblyData(ownerAssembly),
                                                                                                                   project);
                                            }

                                            // Datenkarten-Code-Filterung (Abgleich Code der Farbe mit den Code der Datenkarte)
                                            if (validColorTableContent) {
                                                validColorTableContent &= iPartsFilterHelper.basicCheckCodeFilter(colorTableContent.code,
                                                                                                                  currentDataCard.getFilterCodes().getAllCheckedValues(),
                                                                                                                  null, null, null);
                                                if (!validColorTableContent && isWithFilterReason()) {
                                                    setFilterReasonForColorTableContent(colorTableContent.getDataColorTableContent(false, project),
                                                                                        iPartsColorFilter.ColorTableContentFilterTypes.DATACARD_CODE,
                                                                                        "!!Code nicht gültig für die Code %1",
                                                                                        iPartsFilterHelper.getFilterReasonSourceName(false));
                                                }
                                            }
                                        }

                                        if (validColorTableContent && endNumberFilterActive) {
                                            // Endnummernfilterung
                                            validColorTableContent &= iPartsFilterHelper.checkColorEndNumberFilter(getVehicleDataCard(false),
                                                                                                                   project,
                                                                                                                   colorTableContent, this);
                                        }

                                        if (validColorTableContent) {
                                            if (colorTableContentsFiltered == null) {
                                                colorTableContentsFiltered = new DwList<>();
                                            }
                                            colorTableContentsFiltered.add(colorTableContent);
                                        }

                                        if (isWithFilterReason()) {
                                            // Für die folgenden Berechnungen (speziell erweiterter Farb-Filter und Verdichtung)
                                            // dürfen nur die nicht sowieso schon ausgefilterten Farbvarianteninhalte betrachtet
                                            // werden (colorTableContentsFiltered), aber später in der Anzeige werden alle
                                            // benötigt (colorTableContentsFilterReason)
                                            if (colorTableContentsFilterReason == null) {
                                                colorTableContentsFilterReason = new DwList<>();
                                            }
                                            colorTableContentsFilterReason.add(colorTableContent);
                                        }
                                    }
                                }

                                if (colorTableContentsFiltered != null) { // Es gibt gültige Farbvarianteninhalte
                                    // Erweiterter Farb-Filter
                                    if (extendendColorFilterActiveAndValid) { // Erweiterter Farb-Filter soll abschaltbar sein
                                        for (String modelnumber : validModels) {
                                            DIALOGModelFilter dialogModelFilter = cachedData.getDialogModelFilter(project,
                                                                                                                  modelnumber);
                                            modelBuildingCodeSet = dialogModelFilter.getModelBuildingCodeSet(); // Alle BM-bildenden Code des Baumusters
                                            DatacardFilter.extendedCodeFilterScoringForColor(colorTableContentsFiltered,
                                                                                             currentDataCard.getFilterCodes().getAllCheckedValues(),
                                                                                             modelBuildingCodeSet, this, project);
                                        }
                                    }

                                    // Verdichtung von gleichen Farben mit identischen Code
                                    for (int i = 0; i < colorTableContentsFiltered.size(); i++) {
                                        iPartsColorTable.ColorTableContent colorTableContent = colorTableContentsFiltered.get(i);
                                        for (int j = i + 1; j < colorTableContentsFiltered.size(); j++) {
                                            iPartsColorTable.ColorTableContent colorTableContentToCheck = colorTableContentsFiltered.get(j);
                                            if (colorTableContent.colorNumber.equals(colorTableContentToCheck.colorNumber)
                                                && !colorTableContent.isEvalPemFrom() && !colorTableContent.isEvalPemTo()
                                                && !colorTableContentToCheck.isEvalPemFrom() && !colorTableContentToCheck.isEvalPemTo()
                                                && DaimlerCodes.equalsCodeString(colorTableContent.code, colorTableContentToCheck.code)) {
                                                // Gleiche Farbe, Flags "PEM ab/bis auswerten" sind nicht gesetzt und gleiche Code
                                                // -> Verdichtung durchführen, indem der Farbvarianteninhalt entfernt wird
                                                iPartsColorTable.ColorTableContent removedColorTableContent = colorTableContentsFiltered.remove(j);
                                                if ((removedColorTableContent != null) && isWithFilterReason()) {
                                                    setFilterReasonForColorTableContent(removedColorTableContent.getDataColorTableContent(false, project),
                                                                                        iPartsColorFilter.ColorTableContentFilterTypes.REMOVE_DUPLICATES,
                                                                                        "!!Verdichtung von gleichen Varianten mit identischen Code");
                                                }

                                                j--; // Vergleichs-Index korrigieren, da der Eintrag ja gelöscht wurde
                                            }
                                        }
                                    }
                                }

                                if (isWithFilterReason()) {
                                    // Bei Filterabsicherung sollen alle Farbvarianteninhalte angezeigt werden (auch wenn
                                    // sie eigentlich ausgefiltert worden wären)
                                    colorTableContentsFiltered = colorTableContentsFilterReason;
                                }
                            }
                        } else {
                            if (isWithFilterReason()) {
                                setFilterReasonForColorTableToPart(colorTableToPart.getDataColorTableToPart(false, project),
                                                                   iPartsColorFilter.ColorTableToPartFilterTypes.MODEL,
                                                                   "!!Keine oder nur ungültige Werkseinsatzdaten vorhanden");

                                // Datensatz für die Filterabsicherung künstlich hinzufügen über das Setzen von leeren Farbvarianteninhalten
                                colorTableContentsFiltered = new DwList<>();
                            }
                        }

                        if (colorTableContentsFiltered != null) {
                            // Bei Filterabsicherung auch Farbvariantentabelle ohne Farbvarianteninhalte anzeigen
                            if (!colorTableContentsFiltered.isEmpty() || isWithFilterReason()) {
                                iPartsColorTable.ColorTable colorTableFiltered = new iPartsColorTable.ColorTable();
                                colorTableFiltered.colorTableId = colorTable.colorTableId;
                                colorTableFiltered.colorTableContents = colorTableContentsFiltered;
                                colorTableFiltered.colorSign = colorTable.colorSign;

                                iPartsColorTable.ColorTableToPart colorTableToPartFiltered = colorTableToPart.cloneMe(false, true);
                                colorTableFiltered.addColorTableToPart(colorTableToPartFiltered);

                                colorTableForRetailFiltered.addColorTable(colorTableFiltered);
                            }
                        }

                        // Wenn ein Datensatz von colorTableToPart gültig ist, dann ist die gesamte Farbtabelle gültig
                        // In der Filterabsicherung hingegen sind alle Datensätze relevant
                        if (colorTableValid && !isWithFilterReason()) {
                            break;
                        }
                    }
                }

                if (!colorTableForRetailFiltered.getColorTablesMap().isEmpty()) {
                    return colorTableForRetailFiltered;
                } else {
                    return null;
                }
            } else {
                return colorTableForRetailWithoutFilter;
            }
        } else if (documentationType.isTruckDocumentationType() && ownerAssembly.getModuleMetaData().isShowColorTablefootnotes()) {
            iPartsColorTable colorTableForRetailFiltered = new iPartsColorTable();
            for (iPartsColorTable.ColorTable colorTable : colorTableForRetailWithoutFilter.getColorTablesMap().values()) {
                boolean colorTableValid = false;
                for (iPartsColorTable.ColorTableToPart colorTableToPart : colorTable.colorTableToPartsMap.values()) {
                    if (colorTableToPart.dataOrigin != iPartsImportDataOrigin.PRIMUS) {
                        if (!colorTableValid) {
                            colorTableValid = true;
                            colorTableForRetailFiltered.addColorTable(colorTable);
                            if (!isWithFilterReason()) { // In der Filterabsicherung sind alle Datensätze relevant
                                break;
                            }
                        } else {
                            if (isWithFilterReason()) {
                                setFilterReasonForColorTableToPart(colorTableToPart.getDataColorTableToPart(false, project),
                                                                   iPartsColorFilter.ColorTableToPartFilterTypes.ORIGIN,
                                                                   "!!Verdichtung für gleiche Variantentabelle mit Ursprung ungleich PRIMUS");
                            }
                        }
                    } else {
                        if (isWithFilterReason()) {
                            setFilterReasonForColorTableToPart(colorTableToPart.getDataColorTableToPart(false, project),
                                                               iPartsColorFilter.ColorTableToPartFilterTypes.ORIGIN,
                                                               "!!Variantentabelle hat Ursprung PRIMUS");
                            colorTableForRetailFiltered.addColorTable(colorTable); // Farbvariantentabelle für die Filterabsicherung künstlich hinzufügen
                        }
                    }
                }
            }
            if (colorTableForRetailFiltered.getColorTablesMap().isEmpty()) {
                return null;
            } else {
                return colorTableForRetailFiltered;
            }
        } else {
            return colorTableForRetailWithoutFilter;
        }
    }

    public iPartsColorTable.ColorTableContent checkModelFilterForColor(EtkProject project, iPartsDataAssembly ownerAssembly,
                                                                       List<iPartsColorTable.ColorTableContent> colorTableContents,
                                                                       String modelNumber) {
        List<iPartsColorTable.ColorTableContent> colorTableContentsToFilter = new DwList<>();
        for (iPartsColorTable.ColorTableContent colorTableContent : colorTableContents) {
            colorTableContentsToFilter.add(colorTableContent.cloneMe(true, true));
        }
        Set<String> modelToFilter = new HashSet<>();
        modelToFilter.add(modelNumber);
        List<iPartsColorTable.ColorTableContent> colorTableContentFiltered = checkModelFilterForColors(project, ownerAssembly,
                                                                                                       colorTableContentsToFilter,
                                                                                                       new HashSet<>(), modelToFilter);
        if ((colorTableContentFiltered != null) && !colorTableContentFiltered.isEmpty()) {
            return colorTableContentFiltered.get(0);
        }
        return null;
    }

    /**
     * Baumuster-Filter für Farbtabelleninhalte
     *
     * @param project
     * @param ownerAssembly        Die Assembly für welche die Farbtabelle gefiltert wird.
     * @param colorTableContents
     * @param validModels          Wird mit allen Baumustern befüllt,
     *                             für die mindestens eine Farbe die Baumusterzeitscheibenprüfung bestanden hat.
     * @param relevantModelNumbers
     * @return
     */
    public List<iPartsColorTable.ColorTableContent> checkModelFilterForColors(EtkProject project, iPartsDataAssembly ownerAssembly,
                                                                              List<iPartsColorTable.ColorTableContent> colorTableContents,
                                                                              Set<String> validModels, Set<String> relevantModelNumbers) {

        List<iPartsColorTable.ColorTableContent> colorTableContentsFiltered = null;
        List<iPartsColorTable.ColorTableContent> colorTableContentsFilterReason = null;
        for (iPartsColorTable.ColorTableContent colorTableContent : colorTableContents) {
            boolean validColorTableContent = false;

            // Vergleich vom Gültigkeitsintervall der Werkseinsatzdaten für Farbvarianteninhalte mit der Zeitscheibe
            // vom Baumuster (wurde in Daimler-4163 von Prüfung gegen Zeitscheibe des Stücklisteneintrags geändert)
            for (String modelNumber : relevantModelNumbers) {
                validColorTableContent = isColorTableContentInModelTimeSlice(ownerAssembly, validModels, colorTableContent,
                                                                             validColorTableContent, modelNumber);
            }

            if (validColorTableContent) {
                // Baumuster Code-Filterung (für alle verfügbaren Baumuster)
                boolean validColorTableContentForOneModel = false;
                for (String modelNumber : validModels) {
                    DIALOGModelFilter dialogModelFilter = cachedData.getDialogModelFilter(project, modelNumber);
                    Set<String> modelBuildingCodeSet = dialogModelFilter.getModelBuildingCodeSet(); // Alle BM-bildenden Code des Baumusters

                    // Für die Code-Prüfung nur die tatsächlichen baumusterbildenden Code von allen
                    // relevanten Datenkarten für das Baumuster verwenden
                    Set<String> positiveModelBuildingCodeSet = cachedData.getModelBuildingCodeSetFromDataCards(ownerAssembly,
                                                                                                               modelNumber);

                    if (iPartsFilterHelper.basicCheckCodeFilter(colorTableContent.code, positiveModelBuildingCodeSet,
                                                                null, modelBuildingCodeSet, null)) {
                        validColorTableContentForOneModel = true;
                        break;
                    }
                }
                if (!validColorTableContentForOneModel) {
                    if (isWithFilterReason()) {
                        setFilterReasonForColorTableContent(colorTableContent.getDataColorTableContent(false, project),
                                                            iPartsColorFilter.ColorTableContentFilterTypes.MODEL,
                                                            "!!Code nicht gültig für die Code %1",
                                                            iPartsFilterHelper.getFilterReasonSourceName(true));
                    }
                    validColorTableContent = false;
                }
            } else {
                if (isWithFilterReason()) {
                    if (relevantModelNumbers.size() == 1) {
                        FilterCachedModelData modelData = cachedData.getModelData(ownerAssembly, relevantModelNumbers.iterator().next());
                        setFilterReasonForColorTableContent(colorTableContent.getDataColorTableContent(false, project),
                                                            iPartsColorFilter.ColorTableContentFilterTypes.MODEL,
                                                            "!!Keine gültigen Werkseinsatzdaten überlappen mit der Zeitscheibe \"%1\" bis \"%2\" %3",
                                                            iPartsFilterHelper.getFilterReasonDate(modelData.getProductModelsValidFrom(), project),
                                                            iPartsFilterHelper.getFilterReasonDate(modelData.getProductModelsValidTo(), project),
                                                            iPartsFilterHelper.getFilterReasonSourceName(true));
                    } else {
                        setFilterReasonForColorTableContent(colorTableContent.getDataColorTableContent(false, project),
                                                            iPartsColorFilter.ColorTableContentFilterTypes.MODEL,
                                                            "!!Keine gültigen Werkseinsatzdaten überlappen mit der Zeitscheibe eines relevanten Baumusters");
                    }
                }
            }

            if (validColorTableContent) {
                if (colorTableContentsFiltered == null) {
                    colorTableContentsFiltered = new DwList<>();
                }
                colorTableContentsFiltered.add(colorTableContent);
            }
            if (isWithFilterReason()) {
                // Für die folgenden Berechnungen (speziell Teilkonjunktionen vergleichen) dürfen nur die nicht sowieso
                // schon ausgefilterten Farbvarianteninhalte betrachtet werden (colorTableContentsFiltered), aber später
                // in der Anzeige werden alle benötigt (colorTableContentsFilterReason)
                if (colorTableContentsFilterReason == null) {
                    colorTableContentsFilterReason = new DwList<>();
                }
                colorTableContentsFilterReason.add(colorTableContent);
            }
        }

        // DAIMLER-7068 Teilkonjunktionen vergleichen und Flags "PEM ab/bis auswerten" setzen
        if (colorTableContentsFiltered != null) {
            if (colorTableContentsFiltered.size() >= 2) {
                // Kleinste PEM-ab-Termine pro Farbvariante suchen
                ColorTableContentToPEMFromDate colorTableContentToPEMFromDate = null;
                for (iPartsColorTable.ColorTableContent colorTableContent : colorTableContentsFiltered) {
                    for (List<iPartsColorFactoryDataForRetail.DataForFactory> dataForFactoryList : colorTableContent.getFactoryData().getFactoryDataMap().values()) {
                        for (iPartsColorFactoryDataForRetail.DataForFactory dataForFactory : dataForFactoryList) {
                            String pemFromDate = String.valueOf(dataForFactory.dateFrom);
                            if (colorTableContentToPEMFromDate == null) {
                                colorTableContentToPEMFromDate = new ColorTableContentToPEMFromDate(colorTableContent.colorTableContentId, pemFromDate);
                            } else if (colorTableContentToPEMFromDate.containsId(colorTableContent.colorTableContentId)) {
                                String currentDate = colorTableContentToPEMFromDate.getCurrentPemDateFrom(colorTableContent.colorTableContentId);
                                if (pemFromDate.compareTo(currentDate) < 0) { // Wenn pemFromDate leer ist (also -unendlich), dann ist das Ergebnis auch < 0
                                    colorTableContentToPEMFromDate.addColorTableContentId(colorTableContent.colorTableContentId, pemFromDate);
                                }
                            } else {
                                colorTableContentToPEMFromDate.addColorTableContentId(colorTableContent.colorTableContentId, pemFromDate);
                            }
                        }
                    }
                }

                // Teilkonjunktionen vergleichen
                List<iPartsColorTable.ColorTableContent> colorTableContentsModelFiltered = colorTableContentsFiltered;
                colorTableContentsFiltered = new DwList<>(colorTableContentsModelFiltered.size());
                for (iPartsColorTable.ColorTableContent searchItem : colorTableContentsModelFiltered) {
                    iPartsColorTableContentId searchItemId = searchItem.colorTableContentId;
                    boolean evalPEMs = false; // Sollen die PEM auswerten Flags gesetzt werden?
                    boolean youngest = true; // Handelt es sich bei dem aktuellen Datensatz um den jüngsten Datensatz in der aktuellen Farbenreihenfolge?
                    boolean oldest = true; // Handelt es sich bei dem aktuellen Datensatz um den ältesten Datensatz in der aktuellen Farbenreihenfolge?

                    String searchCode = searchItem.code;
                    String searchES2 = searchItem.colorNumber;

                    validModelsLoop:
                    for (String modelNumber : validModels) {
                        DIALOGModelFilter dialogModelFilter = cachedData.getDialogModelFilter(project, modelNumber);
                        Set<String> positiveModelBuildingCodeSetFromModel = dialogModelFilter.getPositiveModelBuildingCodeSet(); // Alle positiven BM-bildenden Code vom Baumuster
                        Set<String> modelBuildingCodeSet = dialogModelFilter.getModelBuildingCodeSet();

                        // Terme und Modelljahr-Code für searchCode bestimmen
                        Collection<ConjunctionWithModelYearCodesForColors> searchTerms;
                        try {
                            // Klonen der DNF findet in basicCheckCodeFilter() innerhalb von
                            // extractTermsForWithModelYearCodesColorCodeMatch() statt
                            Disjunction searchDisjunctions = DaimlerCodes.getDnfCodeOriginal(searchCode);
                            searchTerms = extractTermsForWithModelYearCodesColorCodeMatch(searchDisjunctions,
                                                                                          positiveModelBuildingCodeSetFromModel,
                                                                                          modelBuildingCodeSet);
                        } catch (BooleanFunctionSyntaxException e) {
                            searchTerms = new HashSet<>();
                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, new RuntimeException(e.getMessage(), e));
                        }

                        for (iPartsColorTable.ColorTableContent compareItem : colorTableContentsModelFiltered) {
                            // Hier alle Elemente außer dem eigenen betrachten
                            iPartsColorTableContentId compareItemId = compareItem.colorTableContentId;
                            if (!compareItemId.equals(searchItemId)) {
                                String compareCode = compareItem.code;
                                String compareES2 = compareItem.colorNumber;

                                // Unterschiedlicher ES2-Schlüssel UND gleiche Coderegel (= mindestens eine Teilkonjunktion
                                // der Coderegel muss gleich sein inkl. der Checks aus DAIMLER-7068)
                                if (!searchES2.equals(compareES2) && partialConjunctionOfColorCodeMatches(searchTerms,
                                                                                                          searchCode,
                                                                                                          compareCode,
                                                                                                          positiveModelBuildingCodeSetFromModel,
                                                                                                          modelBuildingCodeSet)) {
                                    evalPEMs = true;
                                    if (colorTableContentToPEMFromDate != null) {
                                        String searchDate = colorTableContentToPEMFromDate.getCurrentPemDateFrom(searchItemId);
                                        if (searchDate != null) {
                                            String compareDate = colorTableContentToPEMFromDate.getCurrentPemDateFrom(compareItemId);
                                            // Vergleich macht nur Sinn, wenn es auch einen PEM-ab Termin für den Vergleichs-Datensatz
                                            // gibt (ansonsten war das Werk ungültig)
                                            if (compareDate != null) {
                                                if (searchDate.compareTo(compareDate) < 0) {
                                                    youngest = false;
                                                } else if (searchDate.compareTo(compareDate) > 0) {
                                                    oldest = false;
                                                }
                                            }
                                        } else { // Es gibt keinen PEM-ab Termin für diesen Datensatz -> das Werk war ungültig
                                            youngest = false;
                                            oldest = false;
                                        }
                                    }
                                }
                            }

                            if (evalPEMs && !youngest && !oldest) { // Frühzeitiger Abbruch, wenn weitere Prüfungen irrelevant sind
                                break validModelsLoop;
                            }
                        }
                    }

                    // evalPEMs = true, wenn wir zwei unterschiedliche Farben getroffen haben, die gleiche Teilkonjunktionen
                    // besitzen
                    boolean evalPEMFrom = evalPEMs;
                    boolean evalPEMTo = evalPEMs;

                    // Hab ich mehrere unterschiedliche Farben mit gleichen Teilkonjunktionen UND das Einsatzdatum-Ab ist bei
                    // allen gleich, dann ist jeder Datensatz in dieser Gruppe, der "älteste" und der "jüngste". Wenn das
                    // der Fall ist, dann bekommen alle Datensätze in dieser Gruppe beide Flags.
                    boolean allColorsHaveSameDate = oldest && youngest;

                    if (evalPEMs && !allColorsHaveSameDate) {
                        // Der älteste Datensatz (kleinstes Datum) bekommt nur das "PEM bis auswerten"-Flag
                        if (oldest) {
                            evalPEMFrom = false;
                        }
                        // Der jüngste Datensatz (größtes Datum) bekommt nur das "PEM ab auswerten"-Flag
                        if (youngest) {
                            evalPEMTo = false;
                        }
                    }

                    // Die Flags "PEM ab/bis auswerten" entsprechend setzen (mit Klonen der Farbvariante
                    // falls notwendig)
                    boolean alreadyCloned = false;
                    iPartsColorTable.ColorTableContent originalSearchItem = searchItem;
                    if (evalPEMFrom && !searchItem.isEvalPemFrom()) {
                        searchItem = searchItem.cloneMe(true, true);
                        alreadyCloned = true;
                        searchItem.setEvalPemFrom(true);
                    }
                    if (evalPEMTo && !searchItem.isEvalPemTo()) {
                        if (!alreadyCloned) {
                            searchItem = searchItem.cloneMe(true, true);
                        }
                        searchItem.setEvalPemTo(true);
                    }

                    colorTableContentsFiltered.add(searchItem);

                    if (isWithFilterReason() && (originalSearchItem != searchItem)) {
                        // Wenn der aktuell betrachtete ColorTableContent (searchItem) verändert wurde, dann muss der entsprechende
                        // Eintrag in colorTableContentsFilterReason durch den neuen ersetzt werden (beim Klonen bleibt
                        // der Filtergrund erhalten)
                        int originalIndex = colorTableContentsFilterReason.indexOf(originalSearchItem);
                        if (originalIndex >= 0) {
                            colorTableContentsFilterReason.set(originalIndex, searchItem);
                        }
                    }
                }
            } else {
                // Keine Vergleich von Teilkonjunktionen notwendig -> colorTableContentsFiltered
                // bleibt erhalten
            }
        }

        if (isWithFilterReason()) {
            return colorTableContentsFilterReason;
        } else {
            return colorTableContentsFiltered;
        }
    }

    public boolean isColorTableContentInModelTimeSlice(iPartsDataAssembly ownerAssembly,
                                                       Set<String> validModels, iPartsColorTable.ColorTableContent colorTableContent,
                                                       boolean validColorTableContent, String modelNumber) {
        FilterCachedModelData modelData = cachedData.getModelData(ownerAssembly, modelNumber);

        factoryDataLoop:
        for (List<iPartsColorFactoryDataForRetail.DataForFactory> dataForFactoryList : colorTableContent.getFactoryData().getFactoryDataMap().values()) {
            for (iPartsColorFactoryDataForRetail.DataForFactory dataForFactory : dataForFactoryList) {
                long timeSliceDateFrom;
                if (colorTableContent.getFactoryData().hasFactoryDataWithInfiniteDates()) {
                    timeSliceDateFrom = 0; // DAIMLER-8299: -unendlich
                } else {
                    timeSliceDateFrom = dataForFactory.dateFrom;
                }

                if (iPartsFilterTimeSliceHelper.isInModelTimeSlice(timeSliceDateFrom, dataForFactory.getDateToWithInfinity(),
                                                                   modelData.getProductModelsValidFrom(),
                                                                   modelData.getProductModelsValidTo())) {
                    validColorTableContent = true;
                    validModels.add(modelNumber);
                    break factoryDataLoop;
                }
            }
        }
        return validColorTableContent;
    }

    /**
     * Sucht eine Übereinstimmung in den Teilkonjunktionen der beiden Codes von Farbvarianten.
     *
     * @param searchTerms
     * @param searchCodesStr
     * @param compareCodesStr
     * @param positiveModelBuildingCodeSetFromModels
     * @param allModelBuildingCodeSet
     * @return
     */
    private boolean partialConjunctionOfColorCodeMatches(Collection<ConjunctionWithModelYearCodesForColors> searchTerms, String searchCodesStr,
                                                         String compareCodesStr, Set<String> positiveModelBuildingCodeSetFromModels,
                                                         Set<String> allModelBuildingCodeSet) {
        // Spezialfall für leere Coderegeln
        if (DaimlerCodes.isEmptyCodeString(searchCodesStr) && DaimlerCodes.isEmptyCodeString(compareCodesStr)) {
            return true;
        }

        // Leere Strings oder Null-Pointer erst garnicht überprüfen.
        if (StrUtils.isValid(searchCodesStr, compareCodesStr)) {
            try {
                // Klonen der DNF findet in basicCheckCodeFilter() innerhalb von
                // extractTermsForWithModelYearCodesColorCodeMatch() statt
                Disjunction compareDisjunction = DaimlerCodes.getDnfCodeOriginal(compareCodesStr);
                Collection<ConjunctionWithModelYearCodesForColors> compareTerms = extractTermsForWithModelYearCodesColorCodeMatch(compareDisjunction,
                                                                                                                                  positiveModelBuildingCodeSetFromModels,
                                                                                                                                  allModelBuildingCodeSet);

                // wenn beide leer sind, dann sind sie gleich
                if (searchTerms.isEmpty() && compareTerms.isEmpty()) {
                    return true;
                }
                // wenn nur eines von beiden leer ist können sie nicht gleich sein
                if (searchTerms.isEmpty() || compareTerms.isEmpty()) {
                    return false;
                }

                // Jeden Code der ersten Code-Liste ...
                for (ConjunctionWithModelYearCodesForColors searchTerm : searchTerms) {
                    // ... in der zweiten Liste suchen ...
                    for (ConjunctionWithModelYearCodesForColors compareTerm : compareTerms) {
                        if (searchTerm.getTerms().equals(compareTerm.getTerms())) {
                            // Positive und negative Modelljahr-Code prüfen
                            if (!searchTerm.getNegativeModelYearCodes().isEmpty() && !compareTerm.getPositiveModelYearCodes().isEmpty()) {
                                if (searchTerm.getNegativeModelYearCodes().containsAll(compareTerm.getPositiveModelYearCodes())) {
                                    continue;
                                }
                            }

                            if (!searchTerm.getPositiveModelYearCodes().isEmpty() && !compareTerm.getNegativeModelYearCodes().isEmpty()) {
                                if (compareTerm.getNegativeModelYearCodes().containsAll(searchTerm.getPositiveModelYearCodes())) {
                                    continue;
                                }
                            }

                            // Bei der ersten Übereinstimmung "gefunden" zurückgeben, wenn obige Modelljahr-Code-Prüfungen
                            // nicht zugeschlagen haben
                            return true;
                        }
                    }
                }
            } catch (BooleanFunctionSyntaxException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_CODES, LogType.ERROR, new RuntimeException(e.getMessage(), e));
            }
        }

        // Nix gefunden!
        return false;
    }

    /**
     * Pro Teilkonjunktion ein Set der für den Vergleich relevanten technischen Code sowie positiven und negativen Modelljahr-Code
     * erzeugen.
     *
     * @param disjunction
     * @param allModelBuildingCodeSet
     * @return
     */
    private Collection<ConjunctionWithModelYearCodesForColors> extractTermsForWithModelYearCodesColorCodeMatch(Disjunction disjunction,
                                                                                                               Set<String> positiveModelBuildingCodeSetFromModels,
                                                                                                               Set<String> allModelBuildingCodeSet) throws BooleanFunctionSyntaxException {
        // Map von identischen Termen (technischen Code) auf ConjunctionWithModelYearCodesForColors
        Map<Set<String>, ConjunctionWithModelYearCodesForColors> compareElements = new HashMap<>();
        for (Conjunction conjunction : disjunction) {
            if (iPartsFilterHelper.basicCheckCodeFilter(conjunction, positiveModelBuildingCodeSetFromModels, null, allModelBuildingCodeSet,
                                                        null, null)) {
                ConjunctionWithModelYearCodesForColors conjunctionWithModelYearCodes = getTermsForPartialConjunctionWithModelYearCodes(conjunction,
                                                                                                                                       allModelBuildingCodeSet);
                if (!conjunctionWithModelYearCodes.isEmpty()) {
                    // Positive und negative Modelljahr-Code von identischen Termen (technischen Code) zusammenfassen
                    Set<String> terms = conjunctionWithModelYearCodes.getTerms();
                    ConjunctionWithModelYearCodesForColors existingConjunctionWithModelYearCodes = compareElements.get(terms);
                    if (existingConjunctionWithModelYearCodes != null) {
                        for (String positiveModelYearCode : conjunctionWithModelYearCodes.getPositiveModelYearCodes()) {
                            existingConjunctionWithModelYearCodes.addPositiveModelYearCode(positiveModelYearCode);
                        }
                        for (String negativeModelYearCode : conjunctionWithModelYearCodes.getNegativeModelYearCodes()) {
                            existingConjunctionWithModelYearCodes.addNegativeModelYearCode(negativeModelYearCode);
                        }
                    } else {
                        compareElements.put(terms, conjunctionWithModelYearCodes);
                    }
                }
            }
        }
        return compareElements.values();
    }

    /**
     * Ermittelt aus der Teilkonjunktion alle positiven Terme, die kein BM-bildender Code und kein Modelljahr-Code sind
     * (also alle technischen Code) und merkt sich pro Teilkonjunktion die positiven und negativen Modelljahr-Code.
     *
     * @param conjunction
     * @param allModelBuildingCodeSet
     * @return
     */
    private ConjunctionWithModelYearCodesForColors getTermsForPartialConjunctionWithModelYearCodes(Conjunction conjunction,
                                                                                                   Set<String> allModelBuildingCodeSet) throws BooleanFunctionSyntaxException {
        ConjunctionWithModelYearCodesForColors compareTerms = new ConjunctionWithModelYearCodesForColors();
        PositiveAndNegativeTerms positiveAndNegativeTerms = conjunction.getPositiveAndNegativeTerms(false);

        for (String positiveTerm : positiveAndNegativeTerms.getPositiveTerms()) {
            if (iPartsModelYearCode.isModelYearCodeValue(positiveTerm)) {
                compareTerms.addPositiveModelYearCode(positiveTerm);
            } else if (!allModelBuildingCodeSet.contains(positiveTerm)) {
                // nur positive Code, die kein BM-bildender Code und kein Modelljahrcode sind, der Vergleichsliste hinzufügen
                compareTerms.addTerm(positiveTerm);
            }
        }

        for (String negativeTerm : positiveAndNegativeTerms.getNegativeTerms()) {
            if (iPartsModelYearCode.isModelYearCodeValue(negativeTerm)) {
                compareTerms.addNegativeModelYearCode(negativeTerm);
            }
        }

        return compareTerms;
    }

    private List<AbstractDataCard> getRelevantDatacardsForModels(EtkProject project, Collection<String> models) {
        List<AbstractDataCard> result = new DwList<>();

        if (!isMultiLayerFilter) {
            result.add(currentDataCard);
            return result;
        }

        return currentDataCard.getFilterRelevantDatacardsForModels(project, models);
    }

    public List<AbstractDataCard> getRelevantDatacardsForAssembly(iPartsDataAssembly assembly) {
        if (!isMultiLayerFilter || currentDataCard.isAggregateDataCard()) {
            List<AbstractDataCard> result = new DwList<>();
            result.add(currentDataCard);
            return result;
        }
        return cachedData.getRelevantDatacards(assembly);
    }

    public boolean hasSaaInAnyRelevantDatacard(iPartsDataAssembly assembly) {
        for (AbstractDataCard dataCard : getRelevantDatacardsForAssembly(assembly)) {
            boolean hasSaas = hasSaaNumbersForFilter(dataCard);
            if (hasSaas) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAggregateModels() {
        return currentDataCard.hasFilterAggregateModels();
    }

    public String getSteeringValue() {
        return currentDataCard.getSteeringValue();
    }

    public String getGearboxValue() {
        return currentDataCard.getGearboxValue();
    }

    public FinId getFinId() {
        VehicleDataCard vehicleDataCard = getVehicleDataCard(false);
        if (vehicleDataCard != null) {
            return vehicleDataCard.getFinId();
        }
        return new FinId();
    }

    /**
     * Flag, ob der Grund für die Ausfilterung als virtuelle Felder an die Stücklisteneinträge geschrieben werden soll.
     *
     * @return
     */
    public boolean isWithFilterReason() {
        return withFilterReason;
    }

    /**
     * Flag, ob der Grund für die Ausfilterung als virtuelle Felder an die Stücklisteneinträge geschrieben werden soll.
     *
     * @param withFilterReason
     */
    public void setWithFilterReason(boolean withFilterReason) {
        this.withFilterReason = withFilterReason;
    }

    /**
     * Entfernt den Grund für die Ausfilterung und markiert das übergebene {@link EtkDataObject} als nicht ausgefiltert.
     *
     * @param dataObject
     * @param overwriteFilterReason Flag, ob ein vorhandener Grund sowie Flag für die Ausfilterung überschrieben werden sollen
     */
    public void clearFilterReasonForDataObject(EtkDataObject dataObject, boolean overwriteFilterReason) {
        if (!withFilterReason) {
            return;
        }

        setFilterReasonForDataObject(dataObject, false, "", "", overwriteFilterReason);
    }

    /**
     * Setzt den Grund für die Ausfilterung des übergebenen Stücklisteneintrags aufgrund des Filters, Übersetzungskey für
     * den Grund sowie optionalen Platzhaltern für die Übersetzung vom Grund.
     *
     * @param partListEntry
     * @param filterType
     * @param filterReasonTranslationKey
     * @param filterReasonPlaceholders
     */
    public void setFilterReasonForPartListEntry(EtkDataPartListEntry partListEntry, iPartsFilterSwitchboard.FilterTypes filterType, String filterReasonTranslationKey,
                                                String... filterReasonPlaceholders) {
        setFilterReasonForDataObject(partListEntry, filterType, filterReasonTranslationKey, filterReasonPlaceholders);
    }

    /**
     * Setzt den Grund für die Ausfilterung der übergebenen Farbvariantentabelle aufgrund des Filters, Übersetzungskey für
     * den Grund sowie optionalen Platzhaltern für die Übersetzung vom Grund.
     *
     * @param dataColorTableToPart
     * @param filterType
     * @param filterReasonTranslationKey
     * @param filterReasonPlaceholders
     */
    public void setFilterReasonForColorTableToPart(iPartsDataColorTableToPart dataColorTableToPart, iPartsColorFilter.ColorTableToPartFilterTypes filterType,
                                                   String filterReasonTranslationKey, String... filterReasonPlaceholders) {
        setFilterReasonForDataObject(dataColorTableToPart, filterType, filterReasonTranslationKey, filterReasonPlaceholders);
    }

    /**
     * Setzt den Grund für die Ausfilterung der übergebenen Farbvariante aufgrund des Filters, Übersetzungskey für den Grund
     * sowie optionalen Platzhaltern für die Übersetzung vom Grund.
     *
     * @param dataColorTableContent
     * @param filterType
     * @param filterReasonTranslationKey
     * @param filterReasonPlaceholders
     */
    public void setFilterReasonForColorTableContent(iPartsDataColorTableContent dataColorTableContent, iPartsColorFilter.ColorTableContentFilterTypes filterType,
                                                    String filterReasonTranslationKey, String... filterReasonPlaceholders) {
        setFilterReasonForDataObject(dataColorTableContent, filterType, filterReasonTranslationKey, filterReasonPlaceholders);
    }

    /**
     * Setzt den Grund für die Ausfilterung des übergebenen {@link EtkDataObject}s auf die entsprechenden Werte.
     *
     * @param dataObject
     * @param filterType
     * @param filterReasonTranslationKey
     * @param filterReasonPlaceholders
     */
    private void setFilterReasonForDataObject(EtkDataObject dataObject, Enum filterType, String filterReasonTranslationKey,
                                              String[] filterReasonPlaceholders) {
        if (!withFilterReason || (filterType == null) || (filterReasonTranslationKey == null)) {
            return;
        }

        String filterReason = TranslationHandler.translate(filterReasonTranslationKey, filterReasonPlaceholders);
        setFilterReasonForDataObject(dataObject, true, filterType.name(), filterReason, true);
    }

    /**
     * Setzt den Grund für die Ausfilterung des übergebenen {@link EtkDataObject}s auf die entsprechenden Werte.
     *
     * @param dataObject
     * @param isFiltered
     * @param filterName
     * @param filterReason
     * @param overwriteFilterReason Flag, ob ein vorhandener Grund sowie Flag für die Ausfilterung überschrieben werden sollen
     */
    private void setFilterReasonForDataObject(EtkDataObject dataObject, boolean isFiltered, String filterName, String filterReason,
                                              boolean overwriteFilterReason) {
        if ((dataObject == null) || (dataObject.getAttributes() == null)) {
            return;
        }

        // attributeExists() versucht das Feld nachzuladen, was unnötig aufwändig ist
        boolean filterReasonExists = dataObject.getAttributes().getField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, false) != null;
        if (overwriteFilterReason || !filterReasonExists) {
            dataObject.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, SQLStringConvert.booleanToPPString(isFiltered),
                                                true, DBActionOrigin.FROM_DB);

            // Bei isFiltered den Filtername und Filtergrund nur dann setzen, wenn dieser nicht bereits einen gültigen Wert enthält
            // -> der erste Filtergrund bleibt erhalten
            if (!isFiltered || !filterReasonExists || dataObject.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTER_NAME).isEmpty()) {
                dataObject.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTER_NAME, filterName,
                                                    true, DBActionOrigin.FROM_DB);
                dataObject.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_DESCRIPTION, filterReason,
                                                    true, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Liefert die gefilterten relevanten Datenkarten für den übergebenen Dokumentationstyp und Filtertyp basierend auf den
     * Aggregatetypen der übergebenen Datenkarten zurück.
     *
     * @param relevantDataCards
     * @param documentationType
     * @param filterType
     * @return
     */
    public List<AbstractDataCard> getRelevantDataCardsForFilterType(List<AbstractDataCard> relevantDataCards,
                                                                    iPartsDocumentationType documentationType,
                                                                    iPartsFilterSwitchboard.FilterTypes filterType,
                                                                    EtkProject project) {
        if (relevantDataCards.isEmpty()) {
            return relevantDataCards;
        }

        // Liste von nicht relevanten Datenkarten aufbauen
        List<AbstractDataCard> notRelevantDataCards = new DwList<>(relevantDataCards.size());
        List<AbstractDataCard> filteredDataCards = new DwList<>(relevantDataCards.size());

        // Für Austauschaggregate ist der Endnummernfilter nicht relevant
        for (AbstractDataCard relevantDataCard : relevantDataCards) {
            if ((relevantDataCard.isAggregateDataCard()) && (filterType == END_NUMBER)) {
                AggregateIdent aggregateIdent = ((AggregateDataCard)relevantDataCard).getAggIdent(project);
                if ((aggregateIdent != null) && aggregateIdent.isExchangeAggregate()) {
                    notRelevantDataCards.add(relevantDataCard);
                    continue;
                }
            }
            filteredDataCards.add(relevantDataCard);
        }

        // filteredDataCards sind jetzt die relevantDataCards abzüglich der notRelevantDataCards

        DCAggregateTypes[] validAggregateTypes;
        if (documentationType.isPKWDocumentationType()) {
            validAggregateTypes = VALID_AGGREGATE_TYPES_FOR_DIALOG_MAP.get(filterType);
        } else if (documentationType.isTruckDocumentationType()) {
            validAggregateTypes = VALID_AGGREGATE_TYPES_FOR_ELDAS_MAP.get(filterType);
        } else {
            return filteredDataCards; // Bei anderen Dokumentationsmethoden müsste die Matrix eigentlich erweitert werden
        }

        // null bedeutet immer gültig
        if (validAggregateTypes == null) {
            return filteredDataCards;
        }

        // Nach dem Aggregatetyp der relevanten Datenkarten filtern
        filteredDataCards = new DwList<>(relevantDataCards.size());
        for (AbstractDataCard relevantDataCard : relevantDataCards) {
            DCAggregateTypes aggregateTypeFromDataCard;
            if (relevantDataCard.isAggregateDataCard()) {
                if (filterType == END_NUMBER) {
                    AggregateIdent aggregateIdent = ((AggregateDataCard)relevantDataCard).getAggIdent(project);
                    if ((aggregateIdent != null) && aggregateIdent.isExchangeAggregate()) {
                        continue;
                    }
                }
                aggregateTypeFromDataCard = ((AggregateDataCard)relevantDataCard).getAggregateType();
            } else {
                aggregateTypeFromDataCard = DCAggregateTypes.VEHICLE;
            }

            for (DCAggregateTypes aggregateType : validAggregateTypes) {
                if (aggregateType == aggregateTypeFromDataCard) {
                    filteredDataCards.add(relevantDataCard);
                    break;
                }
            }
        }

        // nicht relevante Datenkarten entfernen
        for (AbstractDataCard notRelevantDatacard : notRelevantDataCards) {
            filteredDataCards.remove(notRelevantDatacard);
        }

        return filteredDataCards;
    }

    /**
     * Liefert alle Filtertypen zurück, die für die übergebene Datenkarte und Dokumentationstyp aufgrund des Aggregatetyps
     * deaktiviert sind.
     *
     * @param dataCard
     * @param documentationType
     * @return
     */
    public List<iPartsFilterSwitchboard.FilterTypes> getDeactivatedFilterTypesForDataCard(AbstractDataCard dataCard, iPartsDocumentationType documentationType,
                                                                                          EtkProject project) {
        List<AbstractDataCard> dataCards = new DwList<>(1);
        dataCards.add(dataCard);
        List<iPartsFilterSwitchboard.FilterTypes> deactivatedFilters = new DwList<>();
        for (iPartsFilterSwitchboard.FilterTypes filter : iPartsFilterSwitchboard.FilterTypes.values()) {
            List<AbstractDataCard> relevantDataCards = getRelevantDataCardsForFilterType(dataCards, documentationType, filter, project);
            if (relevantDataCards.isEmpty()) {
                deactivatedFilters.add(filter);
            }
        }
        return deactivatedFilters;
    }

    /**
     * Liefert alle SAAs zur übergebenen Datenkarte.
     *
     * @param dataCard
     * @return
     */
    public Set<String> getSaasForDataCard(AbstractDataCard dataCard) {
        return cachedData.getSaasForDataCard(dataCard);
    }

    /**
     * Hat die übergebene Datenkarte Filter-relevante SAAs?
     *
     * @param dataCard
     * @return
     */
    public boolean hasSaaNumbersForFilter(AbstractDataCard dataCard) {
        return !getSaasForDataCard(dataCard).isEmpty();
    }

    /**
     * Soll die übergebene Baureihe mit TTZ gefiltert werden?
     *
     * @param seriesId
     * @param project
     * @return
     */
    public boolean isTTZFilterForSeries(iPartsSeriesId seriesId, EtkProject project) {
        return cachedData.isTTZFilterForSeries(seriesId, project);
    }

    /**
     * Setzt die Gleichteile-Teilenummer falls im Admin-Modus aktiviert und ein Mapping vorhanden ist.
     *
     * @param partListEntry
     * @return Gibt es eine abweichende Gleichteile-Teilenummer?
     */
    public boolean setEqualPartNumber(iPartsDataPartListEntry partListEntry) {
        if (equalPartsHelper.isShowEqualParts()) {
            return partListEntry.setEqualPartNumber(this);
        }
        return false;
    }

    /**
     * Liefert die Gleichteile-Teilenummer für das übergebene {@link EtkDataPart} zurück.
     *
     * @param part
     * @return
     */
    public String getEqualPartNumber(EtkDataPart part) {
        return equalPartsHelper.getEqualPartNumber(part);
    }

    /**
     * Liefert den Gleichteile-Typ basierend auf dem Baumuster der Haupt-Datenkarte zurück.
     *
     * @return
     */
    public iPartsEqualPartType getEqualPartTypeForMainModel() {
        if (equalPartsHelper.isShowEqualParts()) {
            if (equalPartsHelper.isCarAndVan()) {
                return iPartsEqualPartType.MB;
            } else if (equalPartsHelper.isTruckAndBus()) {
                return iPartsEqualPartType.DT;
            }
        }
        return iPartsEqualPartType.NONE;
    }


    /**
     * Hilfsklasse für das kleinste PEM-ab-Datum von Werkseinsatzdaten von Farbvarianten.
     */
    private class ColorTableContentToPEMFromDate {

        private final Map<iPartsColorTableContentId, String> colorTableContentIds;

        public ColorTableContentToPEMFromDate(iPartsColorTableContentId colorTableContentId, String pemDateFrom) {
            this.colorTableContentIds = new HashMap<>();
            addColorTableContentId(colorTableContentId, pemDateFrom);
        }

        public void addColorTableContentId(iPartsColorTableContentId colorTableContentId, String pemDateFrom) {
            if (colorTableContentId != null) {
                colorTableContentIds.put(colorTableContentId, pemDateFrom);
            }
        }

        public boolean containsId(iPartsColorTableContentId contentId) {
            return colorTableContentIds.containsKey(contentId);
        }

        public String getCurrentPemDateFrom(iPartsColorTableContentId contentId) {
            return colorTableContentIds.get(contentId);
        }
    }


    /**
     * Hilfsklasse für eine Teilkonjunktion (einzelne Terme) inkl. positiven und negativen Modelljahr-Code von Code-Regeln
     * von Farbvarianten.
     */
    private class ConjunctionWithModelYearCodesForColors {

        private Set<String> terms;
        private Set<String> positiveModelYearCodes;
        private Set<String> negativeModelYearCodes;

        public boolean isEmpty() {
            return getTerms().isEmpty() && getPositiveModelYearCodes().isEmpty() && getNegativeModelYearCodes().isEmpty();
        }

        public void addTerm(String term) {
            if (terms == null) {
                terms = new HashSet<>();
            }
            terms.add(term);
        }

        public void addPositiveModelYearCode(String positiveModelYearCode) {
            if (positiveModelYearCodes == null) {
                positiveModelYearCodes = new HashSet<>();
            }
            positiveModelYearCodes.add(positiveModelYearCode);
        }

        public void addNegativeModelYearCode(String negativeModelYearCode) {
            if (negativeModelYearCodes == null) {
                negativeModelYearCodes = new HashSet<>();
            }
            negativeModelYearCodes.add(negativeModelYearCode);
        }

        public Set<String> getTerms() {
            if (terms != null) {
                return Collections.unmodifiableSet(terms);
            } else {
                return EMPTY_STRING_HASHSET;
            }
        }

        public Set<String> getPositiveModelYearCodes() {
            if (positiveModelYearCodes != null) {
                return Collections.unmodifiableSet(positiveModelYearCodes);
            } else {
                return EMPTY_STRING_HASHSET;
            }
        }

        public Set<String> getNegativeModelYearCodes() {
            if (negativeModelYearCodes != null) {
                return Collections.unmodifiableSet(negativeModelYearCodes);
            } else {
                return EMPTY_STRING_HASHSET;
            }
        }
    }


    /**
     * Schlüssel für die Gruppierung von Stücklisteneinträgen
     */
    private interface GroupKey {

        String createKey(iPartsDataPartListEntry iPartsPLE);
    }
}
