/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.config.partlist.EtkSectionInfo;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.mechanic.*;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.plugins.EtkPluginApi;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsDataPicOrderModules;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsDataPicOrderModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyEinPasBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyProductKgTu;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsLoader;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementKEMHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.KGTUAutosetTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sort.SortStringCache;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Erweiterung von {@link EtkDataAssembly} um iParts-spezifische Methoden und Daten.
 */
public class iPartsDataAssembly extends EtkDataAssembly implements iPartsConst {

    private static ObjectInstanceLRUList<Object, EtkMultiSprache> assemblyTextsCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_ASSEMBLY_META_DATA,
                                                                                                                   iPartsPlugin.getCachesLifeTime(), true);
    private static ObjectInstanceLRUList<Object, iPartsDataModule> dataModulesCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_ASSEMBLY_META_DATA,
                                                                                                                  iPartsConst.MAX_CACHE_LIFE_TIME_CORE, true);
    private static ObjectInstanceLRUList<Object, iPartsProductId> productIdFromModuleUsageCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_ASSEMBLY_META_DATA,
                                                                                                                              iPartsConst.MAX_CACHE_LIFE_TIME_CORE, true);

    private static ObjectInstanceLRUList<PoolId, FrameworkImage> previewImageCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_ASSEMBLY_META_DATA,
                                                                                                                 iPartsConst.MAX_CACHE_LIFE_TIME_CORE, true);

    private static final iPartsDialogBCTEPrimaryKey EMPTY_BCTE_PRIMARY_KEY = new iPartsDialogBCTEPrimaryKey("", "", "", "", "", "", "", "", "", "");
    private static final EnumSet<SerializedDBDataObjectState> RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM = EnumSet.of(SerializedDBDataObjectState.NEW,
                                                                                                                           SerializedDBDataObjectState.MODIFIED,
                                                                                                                           SerializedDBDataObjectState.REPLACED);
    private static final Set<String> validConnectAASet = new TreeSet<>();

    private static final Set<String> RELEASED_STATE_SET = new HashSet(1);

    public static final String ARRAY_VALIDITIES_FOR_FILTER_DELIMITER = "|";

    public static final EtkDisplayFields NEEDED_DISPLAY_FIELDS = new EtkDisplayFields();

    static {
        RELEASED_STATE_SET.add(iPartsDataReleaseState.RELEASED.getDbValue());

        // Benötigte Felder aus der KATALOG-Tabelle
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_POS), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_AA), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_WW), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_WW_EXTRA_PARTS), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_VIRTUAL_MAT_TYPE), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SOURCE_TYPE), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SOURCE_CONTEXT), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SOURCE_REF1), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SOURCE_REF2), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SOURCE_GUID), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVAL_PEM_FROM), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVAL_PEM_TO), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_OMIT), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES_REDUCED), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVENT_FROM), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVENT_TO), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_DATETO), false, false)); // Für virtuelle DIALOG-Fußnoten
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_AUTO_CREATED), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_ENTRY_LOCKED), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_COUNTRY_VALIDITY), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SPEC_VALIDITY), false, false));

        // Benötigte Felder für die Ermittlung der passenden Vorgänger/ Nachfolger Stände für Ersetzungen inkl. PRIMUS-Ersetzungen
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_DATEFROM), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_MIN_KEM_DATE_FROM), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_MAX_KEM_DATE_TO), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_USE_PRIMUS_SUCCESSOR), false, false));

        // Benötigt für die Qualitätsprüfung
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_HIERARCHY), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_MENGE), false, false));

        // Benötigt für das Scoring der Nachfolger z.B. bei virtuellen Ersetzungen
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_STEERING), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_PRODUCT_GRP), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SA_VALIDITY), false, true));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_MODEL_VALIDITY), false, true));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_GEARBOX_TYPE), false, false));

        // Benötigte Felder aus der MAT-Tabelle
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_BESTNR), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_REFSER), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR), true, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_ADDTEXT), true, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_LAYOUT_FLAG), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_ETKZ), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_BASE_MATNR), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR_MBAG), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR_DTAG), false, false));

        // Benötigte Felder aus der MAT-Tabelle für Validierungen, um zu vermeiden, dass die Daten nachgeladen werden müssen
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_VARIANT_SIGN), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_VERKSNR), false, false));

        // Für Stücklisten-Icons benötigte Felder
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, FIELD_M_IMAGE_AVAILABLE), false, false));

        // Benötigte Felder aus der MAT-Tabelle für Reman-Varianten-Alternativteile
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_PARTNO_BASIC), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_PARTNO_SHORTBLOCK), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_PARTNO_LONGBLOCK), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_PARTNO_LONGBLOCK_PLUS), false, false));
    }

    private List<iPartsVirtualNode> virtualNodesPath;

    private iPartsDataModule cachedDataModule;

    private Set<PartListEntryId> wwUnfilteredPartlistEntryIds = new HashSet<>(); // Alle PartListEntryIds mit Wahlweise-Teilen
    private Map<String, List<EtkDataPartListEntry>> wwGUIDtoUnfilteredPartlistEntriesMap = new HashMap<>(); // Map von Wahlweise-GUID (Set) auf enthaltene Stücklisteneinträge
    private Map<iPartsDialogBCTEPrimaryKey, KgTuId> bcteKeyToPredictedKGTUMap;

    private iPartsDIALOGPositionsHelper dialogPositionsHelper; // CacheObjekt für die DIALOG-Positionen der Stückliste

    private iPartsDocumentationType documentationType; // Dokumentationstyp (immer nur über Getter verwenden!)
    private iPartsModuleTypes moduleType; // Modultyp (immer nur über Getter verwenden!)
    private boolean productIdFromModuleUsageLoaded;
    private iPartsProductId productIdFromModuleUsage; // Produkt-ID für die (erste) Verwendung von dieser Stückliste
    private String filteredWithModelNumber;
    private boolean isEditMode;
    private Boolean isPSKAssembly; // Boolean, damit bei null die einmalige Berechnung stattfinden kann
    private iPartsSAId saId;
    private Map<PartListEntryId, List<iPartsDataModuleCemat>> cematMapForModule;
    private Map<String, iPartsDataGenInstallLocation> genInstallLocationMap;
    private iPartsEqualPartsHelper equalPartsHelper;

    public static void clearAssemblyMetaDataCaches() {
        synchronized (assemblyTextsCache) {
            assemblyTextsCache.clear();
        }
        synchronized (dataModulesCache) {
            dataModulesCache.clear();
        }
        clearProductIdFromModuleUsageCache();
        synchronized (previewImageCache) {
            previewImageCache.clear();
        }
    }

    public static void clearProductIdFromModuleUsageCache() {
        synchronized (productIdFromModuleUsageCache) {
            productIdFromModuleUsageCache.clear();
        }
    }

    private static Object createModuleMetaDataHashObject(EtkProject project, AssemblyId assemblyId) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataAssembly.class, assemblyId.getKVari());
    }

    public static void removeAssemblyMetaDataFromCache(EtkProject project, AssemblyId assemblyId) {
        Object hashObject = createModuleMetaDataHashObject(project, assemblyId);
        synchronized (assemblyTextsCache) {
            assemblyTextsCache.removeKey(hashObject);
        }
        synchronized (dataModulesCache) {
            dataModulesCache.removeKey(hashObject);
        }
        synchronized (productIdFromModuleUsageCache) {
            productIdFromModuleUsageCache.removeKey(hashObject);
        }
    }

    /**
     * Liefert die mehrsprachigen Baugruppen-Benennung <b>OHNE Berücksichtigung von ChangeSets</b> zurück und ist primär
     * für die Webservices gedacht.
     * <br/><b>Diese Methode darf daher NICHT für Edit-Funktionen mit aktivem Autoren-Auftrag verwendet werden!</b>
     *
     * @param project
     * @param assemblyId
     * @return
     */
    public static EtkMultiSprache getAssemblyText(EtkProject project, AssemblyId assemblyId) {
        Object hashObject = createModuleMetaDataHashObject(project, assemblyId);
        synchronized (assemblyTextsCache) {
            EtkMultiSprache multiLanguage = assemblyTextsCache.get(hashObject);
            if (multiLanguage == null) {
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
                multiLanguage = assembly.getPart().getFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR);
                if (multiLanguage == null) {
                    multiLanguage = new EtkMultiSprache("!!Material für das Modul '%1' nicht gefunden", project.getConfig().getDatabaseLanguages(),
                                                        assemblyId.toString(", "));
                }
                assemblyTextsCache.put(hashObject, multiLanguage);
            }
            return multiLanguage;
        }
    }

    public static EtkDataPartListEntry addVirtualParentNodesPathToParentAssemblyEntries(List<EtkDataPartListEntry> parentAssemblyEntries, boolean filtered,
                                                                                        EtkProject project, iPartsVirtualNode... virtualParentNodes) {
        PartListEntryId entryId = new PartListEntryId(iPartsVirtualNode.getVirtualIdString(virtualParentNodes), "", null);
        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, entryId);

        if (!filtered || project.getFilter().checkFilter(partListEntry, project.getDBLanguage())) {
            if (parentAssemblyEntries != null) {
                parentAssemblyEntries.add(partListEntry);
            }
            return partListEntry;
        }

        return null;
    }

    public static Set<String> getValidConnectAASet() {
        return validConnectAASet;
    }

    public boolean isConstructionSeriesAssembly() {
        if (getAsId().isVirtual()) {
            List<iPartsVirtualNode> virtualNodesPath = getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsVirtualNode.isSeriesNode(virtualNodesPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isProductModelAssembly() {
        if (getAsId().isVirtual()) {
            List<iPartsVirtualNode> virtualNodesPath = getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsVirtualNode.isProductNode(virtualNodesPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isConstructionModelAssembly() {
        if (getAsId().isVirtual()) {
            List<iPartsVirtualNode> virtualNodesPath = getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsVirtualNode.isModelNode(virtualNodesPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check, ob es sich um einen Subknoten in der HM/M/SM Konstruktion handelt inkl. Baureihe
     *
     * @return
     */
    public boolean isHmHSmConstructionAssembly() {
        if (getAsId().isVirtual()) {
            List<iPartsVirtualNode> virtualNodesPath = getVirtualNodesPath();
            if ((virtualNodesPath != null) && (virtualNodesPath.size() >= 1) && (virtualNodesPath.get(0).getType() == iPartsNodeType.DIALOG_HMMSM)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check, ob es sich um einen Subknoten in der EDS Konstruktion handelt
     *
     * @return
     */
    public boolean isEdsConstructionAssembly() {
        if (getAsId().isVirtual()) {
            List<iPartsVirtualNode> virtualNodesPath = getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsVirtualNode.isEdsConstNode(virtualNodesPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check, ob es sich um einen Subknoten in der MBS Konstruktion handelt
     *
     * @return
     */
    public boolean isMBSConstructionAssembly() {
        if (getAsId().isVirtual()) {
            List<iPartsVirtualNode> virtualNodesPath = getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsVirtualNode.isMBSConstNode(virtualNodesPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check, ob es sich um einen Subknoten in der CTT Konstruktion handelt
     *
     * @return
     */
    public boolean isCTTConstructionAssembly() {
        if (getAsId().isVirtual()) {
            List<iPartsVirtualNode> virtualNodesPath = getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsVirtualNode.isCTTConstNode(virtualNodesPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handelt es sich bei der Assembly um eine DIALOG SM-Stückliste aus der Konstruktion?
     *
     * @return
     */
    public boolean isDialogSMConstructionAssembly() {
        return isModuleTypeOf(EnumSet.of(iPartsModuleTypes.Dialog_SM_Construction));
    }

    /**
     * Handelt es sich bei der Assembly um eine EDS OPS Scope-Stückliste aus der Konstruktion?
     *
     * @return
     */
    public boolean isEdsLowerStructureConstructionAssembly() {
        return isModuleTypeOf(EnumSet.of(iPartsModuleTypes.EDS_SAA_SCOPE_Construction, iPartsModuleTypes.EDS_SAA_SUB_MODULE_Construction));
    }

    /**
     * Handelt es sich bei der Assembly um eine SAA-Stückliste aus der Konstruktion?
     *
     * @return
     */
    public boolean isSaaPartsListConstructionAssembly() {
        return isModuleTypeOf(EnumSet.of(iPartsModuleTypes.EDS_SAA_Construction, iPartsModuleTypes.CTT_SAA_Construction));
    }

    /**
     * Handelt es sich bei der Assembly um eine MBS-Struktur-Stückliste aus der Konstruktion?
     *
     * @return
     */
    public boolean isMBSStructureConstructionAssembly() {
        return isModuleTypeOf(EnumSet.of(iPartsModuleTypes.MBS_LIST_NUMBER_Construction, iPartsModuleTypes.MBS_CON_GROUP_Construction));
    }

    /**
     * Handelt es sich bei der Assembly um eine für die Konstruktion relevante Stückliste (Strukturknoten werden hier nicht
     * berücksichtigt)?
     *
     * @return
     */
    public boolean isConstructionRelevantAssembly() {
        if (getAsId().isVirtual()) {
            List<iPartsVirtualNode> virtualNodesPath = getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsVirtualNode.isConstructionRelevant(virtualNodesPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSAAssembly() {
        String partListType = getEbeneName();
        return partListType.equals(iPartsConst.PARTS_LIST_TYPE_SA_RETAIL);
    }

    /**
     * Liefert die SA-ID für dieses SA-Modul zurück.
     *
     * @return {@code null} falls dieses Modul kein SA-Modul ist oder keine SA für dieses SA-Modul gefunden werden konnte
     */
    public iPartsSAId getSAId() {
        if (isSAAssembly()) {
            if (saId == null) {
                iPartsDataSAModulesList dataSAModulesList = iPartsDataSAModulesList.loadDataForModule(getEtkProject(), new iPartsModuleId(getAsId().getKVari()));
                for (iPartsDataSAModules dataSAModule : dataSAModulesList) {
                    String saNumber = dataSAModule.getAsId().getSaNumber();
                    if (!saNumber.isEmpty()) {
                        saId = new iPartsSAId(saNumber);
                        break;
                    }
                }
            }
            return saId;
        }

        return null;
    }

    /**
     * Handelt es sich bei der Assembly um einen Retail-Stücklistentyp?
     *
     * @return
     */
    public boolean isRetailPartList() {
        return isModuleTypeOf(iPartsModuleTypes.getRetailModuleTypes());
    }

    /**
     * Handelt es sich bei der Assembly um einen Spezialkatalog z.B. Lacke und Leder
     *
     * @return
     */
    public boolean isSpecialProductPartList() {
        return isModuleTypeOf(iPartsModuleTypes.getSpecialProductModuleTypes());
    }

    /**
     * Handelt es sich um ein bearbeitbares Modul?
     * Nicht bearbeitbar sind z.B. die Konstruktionsdaten und die virtuellen übergeordneten Strukturmodule (z.B. KG-Knoten)
     *
     * @return
     */
    public boolean isPartListEditable() {
        return isModuleTypeOf(iPartsModuleTypes.getEditableModuleTypes());
    }

    /**
     * Überprüft, ob dieses Modul für den eingeloggten Benutzer der aktuellen Session angesehen bzw. editiert werden darf
     * bzgl. der PSK-Erlaubnis und zeigt optional einen Dialog falls dafür keine Rechte vorhanden sind.
     *
     * @param readOnly
     * @param showDialog
     * @return
     */
    public boolean checkPSKInSession(boolean readOnly, boolean showDialog) {
        // Wenn PSK-Produkte nicht erlaubt sind, das Modul aber eine PSK-Doku-Methode hat -> nicht zulässig
        if (!iPartsRight.checkPSKInSession() && getDocumentationType().isPSKDocumentationType()) {
            if (showDialog) {
                MessageDialog.showWarning(TranslationHandler.translate("!!Keine Rechte zum %1 vom TU \"%2\" mit PSK-Dokumentationsmethode für den Benutzer \"%3\".",
                                                                       TranslationHandler.translate(!readOnly ? "!!Editieren" : "!!Anzeigen"),
                                                                       getAsId().getKVari(), iPartsUserAdminDb.getLoginUserFullName()));
            }
            return false;
        }

        // PSK-Flag am Produkt überprüfen
        iPartsProductId productId = getProductIdFromModuleUsage();
        if ((productId != null) && !iPartsRight.checkProductEditableInSession(productId, null, true, getEtkProject())) {
            return false;
        }

        return true;
    }

    /**
     * Überprüft, ob dieses Modul einer freien SA für den eingeloggten Benutzer der aktuellen Session angesehen bzw. editiert
     * werden darf bzgl. der Benutzer-Eigenschaften und zeigt optional einen Dialog falls dafür keine Rechte vorhanden sind.
     *
     * @param readOnly
     * @param showDialog
     * @return
     */
    public boolean checkSAVisibilityInSession(boolean readOnly, boolean showDialog) {
        if (!isSAAssembly()) { // Falls es sich nicht um ein SA-Modul handelt, dann ist es sichtbar
            return true;
        }

        iPartsSAId saId = getSAId();
        if (saId == null) {
            // Falls keine SA zum SA-Modul gefunden werden konnte, dann das SA-Modul so behandeln, wie wenn es noch nicht
            // verortet wäre -> nur für Truck-Autoren sichtbar
            return iPartsRight.checkTruckAndBusInSession();
        }

        if (!iPartsFilterHelper.isSAVisibleForUserInSession(saId, getEtkProject())) {
            if (showDialog) {
                MessageDialog.showWarning(TranslationHandler.translate("!!Keine Rechte zum %1 vom SA-TU \"%2\" für den Benutzer \"%3\".",
                                                                       TranslationHandler.translate(!readOnly ? "!!Editieren" : "!!Anzeigen"),
                                                                       getAsId().getKVari(), iPartsUserAdminDb.getLoginUserFullName()));
            }
            return false;
        }

        return true;
    }

    /**
     * Handelt es sich um ein PSK-Modul mit entsprechender PSK-Doku-Methode bzw. dazugehörigem PSK-Produkt?
     *
     * @return
     */
    public boolean isPSKAssembly() {
        if (isPSKAssembly == null) {
            if (!getAsId().isVirtual()) {
                // Bei einer echten Stückliste zunächst die Doku-Methode und als Fallback das Produkt der Stückliste überprüfen
                if (getDocumentationType().isPSKDocumentationType()) {
                    isPSKAssembly = true;
                } else {
                    // PSK-Flag am Produkt überprüfen
                    iPartsProductId productId = getProductIdFromModuleUsage();
                    if (productId != null) {
                        isPSKAssembly = iPartsProduct.getInstance(getEtkProject(), productId).isPSK();
                    } else {
                        isPSKAssembly = false;
                    }
                }
            } else {
                // Bei einer virtuellen Stückliste das Produkt der virtuellen AssemblyId überprüfen (falls vorhanden)
                String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(getAsId());
                if (StrUtils.isValid(productNumber)) {
                    isPSKAssembly = iPartsProduct.getInstance(getEtkProject(), new iPartsProductId(productNumber)).isPSK();
                } else {
                    isPSKAssembly = false;
                }
            }
        }
        return isPSKAssembly;
    }

    public iPartsModuleTypes getModuleType() {
        if (moduleType == null) {
            moduleType = iPartsModuleTypes.getType(getEbeneName());
        }
        return moduleType;
    }

    private boolean isModuleTypeOf(EnumSet<iPartsModuleTypes> moduleTypes) {
        return moduleTypes.contains(getModuleType());
    }

    /**
     * Handelt es sich bei der Assembly um einen Produkt-Strukturknoten im Retailbaum?
     *
     * @return
     */
    public boolean isRetailStructurePartList() {
        return isModuleTypeOf(iPartsModuleTypes.getRetailStructureModuleTypes());
    }

    /**
     * Handelt es sich bei der Assembly um einen Strukturknoten in welchem die freien SAs hängen können?
     *
     * @return
     */
    public boolean isRetailSaStructurePartList() {
        String partListType = getEbeneName();
        return (partListType.equals(iPartsConst.PARTS_LIST_TYPE_STRUCT_KG));
    }

    /**
     * Handelt es sich bei der Assembly um einen Strukturknoten (keinen Produkt-Strukturknoten)?
     *
     * @return
     */
    public boolean isStructurePartList() {
        String partListType = getEbeneName();
        return (partListType.equals(iPartsConst.PARTS_LIST_TYPE_STRUCTURE));
    }

    public iPartsDataAssembly(EtkProject project, String kVari, String kVer, boolean cacheAssemblyEntries) {
        super(project, kVari, kVer, cacheAssemblyEntries);
    }

    @Override
    public iPartsAssemblyId createId(String... idValues) {
        return new iPartsAssemblyId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsAssemblyId getAsId() {
        return (iPartsAssemblyId)super.getAsId();
    }

    @Override
    public void setId(IdWithType id, DBActionOrigin origin) {
        virtualNodesPath = null; // wird beim nächsten Aufruf von getVirtualNodesPath() neu bestimmt
        super.setId(id, origin);
    }

    @Override
    public void initAttributesWithEmptyValues(DBActionOrigin origin) {
        super.initAttributesWithEmptyValues(origin);

        // Module müssen IMMER so initialisiert werden
        String kVari = getAsId().getKVari();
        setAttributeValue(EtkDbConst.FIELD_K_SACH, kVari, origin);
        setAttributeValue(EtkDbConst.FIELD_K_SVER, getAsId().getKVer(), origin);
        setAttributeValue(EtkDbConst.FIELD_K_MATNR, kVari, origin);
        setAttributeValue(EtkDbConst.FIELD_K_MVER, getAsId().getKVer(), origin);
        setAttributeValue(EtkDbConst.FIELD_K_LFDNR, StrUtils.prefixStringWithCharsUpToLength("0", '0', 5), origin);
        setFieldValueAsInteger(EtkDbConst.FIELD_K_SEQNR, 0, origin);
        getPart().setFieldValueAsBoolean(FIELD_M_ASSEMBLY, true, origin); // Flag für Baugruppe am Materialstamm setzen
    }

    @Override
    public void unloadPartList() {
        super.unloadPartList();
        wwUnfilteredPartlistEntryIds.clear();
        wwGUIDtoUnfilteredPartlistEntriesMap.clear();
        bcteKeyToPredictedKGTUMap = null;
        dialogPositionsHelper = null;
        cematMapForModule = null;
        genInstallLocationMap = null;
        equalPartsHelper = null;
    }

    @Override
    protected String getHeading(List<String> fieldList, EtkMultiSprache multiLangHeadingText, String rootHeading, NavigationPath navPath,
                                int imageIndex, String dbLang, String viewerLang, boolean isHeading1) {
        if (isRoot()) {
            return rootHeading;
        } else {
            String headingPattern;
            if (isHeading1) {
                headingPattern = multiLangHeadingText.getTextByNearestLanguage(dbLang, project.getDataBaseFallbackLanguages());
            } else {
                headingPattern = multiLangHeadingText.getTextByNearestLanguage(viewerLang, project.getDataBaseFallbackLanguages());
            }

            if (isHeading1 && headingPattern.equals("")) {
                headingPattern = "%1 (%2)";

                fieldList = new ArrayList<String>();
                fieldList.add(TableAndFieldName.make(EtkDbConst.TABLE_MAT, EtkDbConst.FIELD_M_MATNR));
                fieldList.add(TableAndFieldName.make(EtkDbConst.TABLE_MAT, EtkDbConst.FIELD_M_TEXTNR));
            }
            if (fieldList.size() > 0) {
                for (int i = 0; i < fieldList.size(); i++) {
                    String fieldAndTabName = fieldList.get(i);

                    String tabName = TableAndFieldName.getTableName(fieldAndTabName);
                    String fieldName = TableAndFieldName.getFieldName(fieldAndTabName);

                    String str = "";
                    String textFromPlugin = EtkPluginApi.getAdditionalModuleHeaderFields(this, navPath, tabName, fieldName);

                    if (textFromPlugin != null) {
                        str = textFromPlugin;
                    } else if (tabName.equals(EtkConfigConst.UEBERSCHRIFT_FELDTYP_FUNCTION)) {
                        if (fieldAndTabName.equals(TableAndFieldName.make(EtkConfigConst.UEBERSCHRIFT_FELDTYP_FUNCTION, EtkConfigConst.UEBERSCHRIFT_KEY_BLATT_NUMMER))) {
                            if (imageIndex >= 0) {
                                str = String.valueOf(imageIndex + 1);
                            }
                        } else {
                            if (fieldAndTabName.equals(TableAndFieldName.make(EtkConfigConst.UEBERSCHRIFT_FELDTYP_FUNCTION, EtkConfigConst.UEBERSCHRIFT_KEY_BLATT_ANZAHL))) {
                                int imageCount = getImageCount();
                                if (imageCount > 0) {
                                    str = String.valueOf(imageCount);
                                }
                            }
                        }
                    } else {
                        if (null != getEtkProject().getConfig().findField(tabName, fieldName)) {
                            DBDataObjectAttribute attribute = null;
                            if (tabName.equals(EtkDbConst.TABLE_KATALOG)) {
                                attribute = getAttributeForVisObject(fieldName);
                            } else if (tabName.equals(EtkDbConst.TABLE_MAT)) {
                                attribute = getPart().getAttributeForVisObject(fieldName);
                            } else if (tabName.equals(EtkDbConst.TABLE_PREISE)) {
                                attribute = getPart().getPriceForActCurrencyAndCountry().getAttributeForVisObject(fieldName);
                            } else if (tabName.equals(EtkDbConst.TABLE_IMAGES)) {
                                List<EtkDataImage> images = getImages();
                                int imageCount = images.size();
                                if ((imageIndex >= 0) && (imageCount > imageIndex)) {
                                    attribute = images.get(imageIndex).getAttributeForVisObject(fieldName);
                                }
                            }

                            if (attribute != null) {
                                str = getEtkProject().getVisObject().asString(tabName, fieldName, attribute, dbLang, true);
                            }
                        }
                    }
                    headingPattern = headingPattern.replace("%" + (i + 1), str);
                }
            }
            return headingPattern;
        }
    }

    @Override
    protected EtkDisplayFields getMustResultFieldsForPartListEntriesWithoutPrimaryKeys(EtkEbenenDaten partlistType, boolean subAssembliesOnly) {
        EtkDisplayFields fields = super.getMustResultFieldsForPartListEntriesWithoutPrimaryKeys(partlistType, subAssembliesOnly);
        if (!subAssembliesOnly) { // komplette Stückliste
            if (!getAsId().isVirtual()) { // echte Stückliste
                // Felder für Wahlweise, virtuellen Materialtyp usw. müssen immer dabei sein
                for (EtkDisplayField displayField : NEEDED_DISPLAY_FIELDS.getFields()) {
                    fields.addFeldIfNotExists(displayField);
                }
                if (isPSKAssembly()) {
                    // Bei PSK-Modulen wird das Feld K_PSK_VARIANT_VALIDITY für die Filterung benötigt
                    fields.addFeldIfNotExists(new EtkDisplayField(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY,
                                                                  false, true));
                }
            } else { // minimale Felder für virtuelle Stücklisten
                fields.addFeldIfNotExists(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false));
                fields.addFeldIfNotExists(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VER, false, false));
                fields.addFeldIfNotExists(new EtkDisplayField(TABLE_KATALOG, FIELD_K_LFDNR, false, false));
                fields.addFeldIfNotExists(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SEQNR, false, false));

                fields.addFeldIfNotExists(new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false));
                fields.addFeldIfNotExists(new EtkDisplayField(TABLE_MAT, FIELD_M_BESTNR, false, false));
                fields.addFeldIfNotExists(new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false));
            }
        }
        return fields;
    }

    @Override
    protected synchronized DBDataObjectList<EtkDataPartListEntry> loadPartlistUsingCache(boolean subAssembliesOnly, EtkDisplayFields fields,
                                                                                         boolean loadAdditionalData) {
        // Pseudo-Transaktionen werden nur für komplette nicht-virtuelle Stücklisten mit Zusatzdaten an dieser Stelle benötigt
        // als Klammer um loadPartlist() und afterLoadPartlist(), damit keine zwei Pseudo-Transaktionen entstehen
        startPseudoTransactionForActiveChangeSet(true);
        try {
            return super.loadPartlistUsingCache(subAssembliesOnly, fields, loadAdditionalData);
        } finally {
            stopPseudoTransactionForActiveChangeSet();
        }
    }

    /**
     * Stückliste laden mit allen Feldern über die angegebene Feldkonfiguration.
     * Das Flag loadAdditionalData wirkt sich aktuell nicht auf virtuelle Stücklisten aus. Das könnte in Zukunft mal sinnvoll sein, dann
     * müsste man das unten bei virtualAssembly.loadPartList() mitgeben. Dann müssen aber auch alle Verwendungen angepasst werden, da
     * die Methode abstrakt ist. Daher machen wir es erst wenn wir Use Cases haben.
     *
     * @param subAssembliesOnly  Sollen NUR die Unterbaugruppen von dieser Baugruppe geladen werden und NICHT auch normale
     *                           Stücklisteneinträge?
     * @param fields             Liste der nötigen Ergebnisfelder
     * @param loadAdditionalData Sollen zusätzliche Daten für die Stücklisteneinträge geladen werden (nur relevant für Ableitungen)
     * @return
     */
    @Override
    protected synchronized DBDataObjectList<EtkDataPartListEntry> loadPartlist(boolean subAssembliesOnly, EtkDisplayFields fields,
                                                                               boolean loadAdditionalData) {
        // Pseudo-Transaktionen werden nur für komplette nicht-virtuelle Stücklisten mit Zusatzdaten an dieser Stelle benötigt
        // als Klammer um loadPartlist() und afterLoadPartlist(), damit keine zwei Pseudo-Transaktionen entstehen
        startPseudoTransactionForActiveChangeSet(true);
        try {
            if (!getAsId().isVirtual()) { // echte Stückliste
                DBDataObjectList<EtkDataPartListEntry> partlist = super.loadPartlist(subAssembliesOnly, fields, loadAdditionalData);

                if (!subAssembliesOnly && loadAdditionalData) {
                    // Stücklisteneinträge um virtuelles Feld für kombinierten Text erweitern
                    addVirtualCombinedTextFieldToPartlist(partlist, fields);

                    // Stücklisteneinträge um virtuelle Felder für den DIALOG BCTE-Schlüssel in der Retail-Sicht erweitern
                    addVirtualAfterSalesDIALOGFieldsToPartlist(partlist);

                    // Ersetzungen für die gesamte Stückliste laden
                    loadAllReplacementsForPartList(partlist);

                    // Alle Fußnoten inkl. deren Texte für die gesamte Stückliste laden
                    loadAllFootNotesForPartList(partlist);

                    // Alle Werkseinsatzdaten für die gesamte Stückliste laden
                    loadAllFactoryDataForRetailForPartList(partlist);

                    // Alle Farbvarianten für die gesamte Stückliste laden
                    loadAllColorTableForRetailForPartList(partlist);

                    // Zusatzmaterialien (ES1, ES2) für die gesamte Stückliste laden
                    loadAllAlternativePartsForPartList(partlist);

                    // virtuelle Felder für Anzeige DIALOG Änderungen laden
                    loadAllDIALOGChangesForPartList(partlist);

                    // virtuelle Felder für die Cemat EinPAS Referenz laden falls notwendig
                    if (fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_CEMAT_EINPAS, false)) {
                        loadAllCematEinPASForPartList(partlist);
                    }
                }

                return partlist;
            } else { // virtuelle Stückliste
                iPartsVirtualAssembly virtualAssembly = iPartsVirtualAssemblyFactory.createVirtualAssembly(getEtkProject(),
                                                                                                           getVirtualNodesPath(),
                                                                                                           getAsId());

                if (virtualAssembly != null) {
                    setCacheAssemblyEntries(virtualAssembly.isCacheAssemblyEntries());

                    // Verbindung zur Baugruppe herstellen
                    DBDataObjectList<EtkDataPartListEntry> partList = virtualAssembly.loadPartList(subAssembliesOnly, fields);
                    for (EtkDataPartListEntry entry : partList) {
                        entry.setOwnerAssembly(this);
                        if (entry instanceof iPartsDataPartListEntry) {
                            ((iPartsDataPartListEntry)entry).setDataLoadedFlagsForVirtualPLE(true);
                        }
                    }
                    calculateValidConnectAAList();
                    return partList;
                } else {
                    Logger.getLogger().throwRuntimeException(getAsId().toString() + " is not a valid virtual node ID.");
                    return null;
                }
            }
        } finally {
            stopPseudoTransactionForActiveChangeSet();
        }
    }

    @Override
    protected synchronized void afterLoadPartlist(boolean subAssembliesOnly, EtkDisplayFields fields, DBDataObjectList<EtkDataPartListEntry> partlist,
                                                  boolean loadAdditionalData) {
        super.afterLoadPartlist(subAssembliesOnly, fields, partlist, loadAdditionalData);

        // Zusätzliche Daten nur für komplette Stücklisten mit Flag loadAdditionalData laden
        if (!subAssembliesOnly && loadAdditionalData) {
            if (!getAsId().isVirtual()) { // Echte Stückliste
                // Wahlweise-Sets für alle Stücklisteneinträge bestimmen
                createWWSetsForPartList(partlist); // Benötigt keine Pseudo-Transaktion, da keine DB-Aktionen stattfinden

                // Virtuelle Felder nachkarteln
                iPartsFilter filter = iPartsFilter.get();
                Session session = Session.get();
                boolean isSessionWithGUI = (session != null) && session.canHandleGui();
                Set<String> seriesNoSet = null;
                boolean isDIALOGPartList = getDocumentationType().isPKWDocumentationType();
                if (isDIALOGPartList) {
                    seriesNoSet = new HashSet<>();
                }
                for (EtkDataPartListEntry entry : partlist) {
                    if (entry instanceof iPartsDataPartListEntry) {

                        // Die um AS-/Zubehör-Code reduzierten Code-Regeln berechnen.
                        iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)entry;
                        iPartsPartListEntry.calculateRetailCodesReducedAndFiltered(filter);

                        // Original-Fehlerorte berechnen
                        iPartsPartListEntry.calculateOriginalFailLocation();

                        // virtuelles MAT-Feld DA_HAS_MAPPED_MATNR besetzen in GUI-Sessions
                        if (isSessionWithGUI) {
                            iPartsPartListEntry.calculateAndSetEqualPartType();
                        }
                        if (isDIALOGPartList) {
                            iPartsDialogBCTEPrimaryKey bcteKey = iPartsPartListEntry.getDialogBCTEPrimaryKey();
                            if (bcteKey != null) {
                                seriesNoSet.add(bcteKey.getHmMSmId().getSeries());
                            }
                        }
                    }
                }

                if (seriesNoSet != null) {
                    genInstallLocationMap = new HashMap<>();
                    for (String seriesNo : seriesNoSet) {
                        genInstallLocationMap.putAll(iPartsDataGenInstallLocationLRUCache.getInstance(getEtkProject(),
                                                                                                      new iPartsSeriesId(seriesNo)).getGenInstallLocationMap());
                    }
                }

                // Vererbte Fehlerorte und generische Verbauorte berechnen (dafür müssen alle Original-Fehlerorte bzw. generischen
                // Verbauorte schon berechnet worden sein, deswegen Aufruf in einer eigenen Schleife)
                Map<String, List<EtkDataPartListEntry>> partListEntriesForHotspotMap = new HashMap<>();
                for (EtkDataPartListEntry partListEntry : partlist) {
                    List<EtkDataPartListEntry> partListEntriesForFieldValue = partListEntriesForHotspotMap.computeIfAbsent(partListEntry.getFieldValue(FIELD_K_POS),
                                                                                                                           hotspot -> new ArrayList<>());
                    partListEntriesForFieldValue.add(partListEntry);
                    if (isDIALOGPartList) {
                        partListEntry.getAttributes().addFields(getGenInstallLocationAttributes(partListEntry), DBActionOrigin.FROM_DB);
                    }
                }

                for (EtkDataPartListEntry entry : partlist) {
                    if (entry instanceof iPartsDataPartListEntry) {
                        ((iPartsDataPartListEntry)entry).calculateInheritedFailLocation(true, partListEntriesForHotspotMap);
                    }
                }
            } else { // Virtuelle Stückliste
                iPartsVirtualAssembly virtualAssembly = iPartsVirtualAssemblyFactory.createVirtualAssembly(getEtkProject(),
                                                                                                           getVirtualNodesPath(),
                                                                                                           getAsId());
                if (virtualAssembly != null) {
                    virtualAssembly.afterLoadPartlist(subAssembliesOnly, fields, partlist, loadAdditionalData);
                }
            }

            // virtuelles Feld für den GenVO-Text laden
            if (fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.K_GENVO_TEXT, false)) {
                loadAllGenVoTexts(partlist);
            }
        }
    }

    @Override
    protected synchronized void afterClonePartListEntriesInCache(DBDataObjectList<EtkDataPartListEntry> sourcePartListEntries,
                                                                 DBDataObjectList<EtkDataPartListEntry> destPartListEntries) {
        super.afterClonePartListEntriesInCache(sourcePartListEntries, destPartListEntries);

        boolean isRelevantForReplacements = false;
        if (!getAsId().isVirtual()) { // Echte Stückliste
            isRelevantForReplacements = true;
        } else {
            // DIALOG-Ersetzungen in der Konstruktion
            if (iPartsVirtualNode.isHmMSmNode(getVirtualNodesPath())) {
                HmMSmId hmMSmId = (HmMSmId)getVirtualNodesPath().get(1).getId();
                if (hmMSmId.isSmNode()) {
                    isRelevantForReplacements = true;
                }
            }
        }

        if (isRelevantForReplacements) {
            // Map von KLfdnr auf Ziel-Stücklisteneintrag aufbauen
            Map<String, iPartsDataPartListEntry> partListEntriesMap = new HashMap<>(destPartListEntries.size());
            for (EtkDataPartListEntry destPartListEntry : destPartListEntries) {
                if (destPartListEntry instanceof iPartsDataPartListEntry) {
                    partListEntriesMap.put(destPartListEntry.getAsId().getKLfdnr(), (iPartsDataPartListEntry)destPartListEntry);
                }
            }

            // Ersetzungen klonen und dabei die Referenzen für Vorgänger und Nachfolger durch die Ziel-Stücklisteneinträge ersetzen
            for (EtkDataPartListEntry sourcePartListEntry : sourcePartListEntries) {
                if (sourcePartListEntry instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry destPartListEntry = partListEntriesMap.get(sourcePartListEntry.getAsId().getKLfdnr());
                    if (destPartListEntry != null) {
                        destPartListEntry.assignReplacements((iPartsDataPartListEntry)sourcePartListEntry, partListEntriesMap);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void ensureAllAdditionalDataIsLoaded(DBDataObjectList<EtkDataPartListEntry> partlist, EtkDisplayFields fields) {
        super.ensureAllAdditionalDataIsLoaded(partlist, fields);

        if (partlist.isEmpty() || getAsId().isVirtual()) { // Leere oder virtuelle Stückliste?
            return;
        }

        EtkDataPartListEntry firstPartListEntry = partlist.get(0);
        if (firstPartListEntry instanceof iPartsDataPartListEntry) {
            // Pseudo-Transaktionen werden nur für komplette nicht-virtuelle Stücklisten mit Zusatzdaten an dieser Stelle benötigt
            // als Klammer um loadPartlist() und afterLoadPartlist(), damit keine zwei Pseudo-Transaktionen entstehen
            startPseudoTransactionForActiveChangeSet(true);
            try {
                iPartsDataPartListEntry iPartsPLE = (iPartsDataPartListEntry)firstPartListEntry;
                if (!iPartsPLE.attributeExists(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, false)
                    || !iPartsPLE.attributeExists(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT_SOURCE_GENVO, false)) {
                    addVirtualCombinedTextFieldToPartlist(partlist, fields);
                }
                if (!iPartsPLE.isReplacementsLoaded()) {
                    loadAllReplacementsForPartList(partlist);
                }
                if (!iPartsPLE.isFootNotesLoaded()) {
                    loadAllFootNotesForPartList(partlist);
                }
                if (!iPartsPLE.isFactoryDataForRetailLoaded()) {
                    loadAllFactoryDataForRetailForPartList(partlist);
                }
                if (!iPartsPLE.isColorTableLoaded()) {
                    loadAllColorTableForRetailForPartList(partlist);
                }
                if (!iPartsPLE.attributeExists(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE, false)) {
                    loadAllDIALOGChangesForPartList(partlist);
                }
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }
        }
    }

    /**
     * Hier wird für jeden SM Knoten berechnet für welche AA am Produkt, das zur jeweiligen BR zugeordnet ist,
     * die Connect-Daten angezeigt werden
     */
    private void calculateValidConnectAAList() {
        IdWithType lastNodeId = getVirtualNodesPath().get(getVirtualNodesPath().size() - 1).getId();
        if ((lastNodeId instanceof HmMSmId) && ((HmMSmId)lastNodeId).isSmNode()) {
            validConnectAASet.clear();
            iPartsSeriesId seriesId = ((HmMSmId)lastNodeId).getSeriesId();
            List<iPartsProduct> productList = iPartsProductHelper.getProductsForSeries(getEtkProject(), seriesId, PRODUCT_STRUCTURING_TYPE.KG_TU,
                                                                                       iPartsProductHelper.PRODUCTS_FOR_SERIES_MODE.FILTER_ALL);

            if (!productList.isEmpty()) {
                for (iPartsProduct product : productList) {
                    if (product.isWireHarnessDataVisible()) {
                        validConnectAASet.addAll(product.getAAsFromModels(getEtkProject()));
                    }
                }
            }
        }
    }

    /**
     * Stücklisteneinträge um virtuelle Felder für den DIALOG BCTE-Schlüssel in der Retail-Sicht erweitern
     *
     * @param partlist
     */
    private void addVirtualAfterSalesDIALOGFieldsToPartlist(DBDataObjectList<EtkDataPartListEntry> partlist) {
        iPartsDocumentationType documentationType = getDocumentationType();
        if (!documentationType.isPKWDocumentationType()) { // Virtuelle Felder nur für Dokumentationsmethode DIALOG befüllen
            return;
        }

        for (EtkDataPartListEntry partListEntry : partlist) {
            String sourceGUID = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);

            // Wenn der Schlüssel nichts Vernünftiges enthielt, einen definiert leeren verwenden.
            if (bctePrimaryKey == null) {
                bctePrimaryKey = EMPTY_BCTE_PRIMARY_KEY;
            }

            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO, bctePrimaryKey.seriesNo, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_HM, bctePrimaryKey.hm, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_M, bctePrimaryKey.m, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SM, bctePrimaryKey.sm, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE, bctePrimaryKey.posE, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV, bctePrimaryKey.posV, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_WW, bctePrimaryKey.ww, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_ETKZ, bctePrimaryKey.et, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_AA, bctePrimaryKey.aa, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SDATA, bctePrimaryKey.sData, true, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Virtuelles Feld für kombinierten Text zu Stückliste hinzufügen
     *
     * @param partlist
     * @param fields   Bei {@code null} wird davon ausgegangen, dass die virtuellen Felder für den Ergänzungstext und sprachneutralen
     *                 Text benötigt werden.
     */
    public void addVirtualCombinedTextFieldToPartlist(DBDataObjectList<EtkDataPartListEntry> partlist, EtkDisplayFields fields) {
        Map<String, String> neutralTextsForModule = new HashMap<>(); // sprachneutralen Texte am Material für Modul: k_lfdnr -> neutraler Text
        String dbLanguage = getEtkProject().getDBLanguage();
        for (EtkDataPartListEntry partListEntry : partlist) {
            // Aktuelle DB-Sprache für die Abfrage verwenden (nur mit dieser Sprache wird die Stückliste ja geladen);
            // withLanguageFallback darf nicht true sein, weil ansonsten für alle Stücklisteneinträge, die gar keinen
            // sprachneutralen Text haben unnötigerweise das EtkMultiSprache vollständig aus der DB geladen werden
            // würde, um die Rückfallsprachen auswerten zu können, weil der Algorithmus annimmt, dass bei einem leeren
            // Text die Übersetzung für die gewünschte Sprache schlichtweg fehlen würde und deswegen die Rückfallsprachen
            // alle durchlaufen werden würden

            EtkDataPart dataPart = partListEntry.getPart();
            boolean oldLogLoadFieldIfNeeded = dataPart.isLogLoadFieldIfNeeded();
            try {
                // Bei der Übernahme aus der Konstruktion ist M_ADDTEXT nicht geladen am Material (was bei den teilweise
                // sehr großen Konstruktions-Stücklisten auch ungünstig wäre, weil es sich um ein mehrsprachiges Feld handelt),
                // was hier zu entsprechenden Log-Meldungen bzgl. Nachladen führen würde
                dataPart.setLogLoadFieldIfNeeded(false);
                String neutralText = dataPart.getFieldValue(iPartsConst.FIELD_M_ADDTEXT, dbLanguage, false);
                if (!neutralText.isEmpty()) {
                    neutralTextsForModule.put(partListEntry.getAsId().getKLfdnr(), neutralText);
                }
            } finally {
                dataPart.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
        }
        // Das Cache-Objekt für die neutralen Texte holen
        DictTextCache neutralCache = null;
        if ((fields == null) || fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT, false)
            || fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL, false)) {
            neutralCache = DictTextCache.getInstanceWithAllTextStates(DictTextKindTypes.NEUTRAL_TEXT, project.getDBLanguage());
        }

        Set<String> alreadyFoundAddTexts = new HashSet<>();
        Set<String> alreadyFoundNeutralTexts = new HashSet<>();
        Map<String, String> combTextMap = null; // komb. Erg.texte der Stückliste (key = k_lfdnr)
        Map<String, List<EtkMultiSprache>> multiLangMap = null;
        Map<String, Boolean> combTextSourceGenVOMap = null; // Ergänzungstexte Quelle GenVO der Stückliste (key = k_lfdnr)
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (combTextMap == null) {
                iPartsDataCombTextList combTextList = iPartsDataCombTextList.loadForModule(getAsId(), getEtkProject());
                multiLangMap = combTextList.buildSeqNoCombTextsMap();
                combTextMap = combTextList.getCombTexts(neutralTextsForModule, multiLangMap, getEtkProject());
                combTextSourceGenVOMap = new HashMap<>();

                // Ergänzungstexte Quelle GenVO?
                for (iPartsDataCombText dataCombText : combTextList) {
                    if (dataCombText.getFieldValueAsBoolean(FIELD_DCT_SOURCE_GENVO)) {
                        combTextSourceGenVOMap.put(dataCombText.getFieldValue(FIELD_DCT_SEQNO), true);
                    }
                }
            }

            String kLfdnr = partListEntry.getAsId().getKLfdnr();
            String combinedText = combTextMap.get(kLfdnr);
            List<EtkMultiSprache> combinedMultiTextList = multiLangMap.get(kLfdnr);
            if (neutralCache != null) {
                // Die Elemente für den Ergänzungstext und den sprachneutralen Text bestimmen
                VarParam<String> addText = new VarParam<>("");
                VarParam<String> neutralText = new VarParam<>("");
                iPartsCombTextHelper.extractAddTextAndNeutralText(project, kLfdnr, combinedMultiTextList, neutralCache,
                                                                  addText, neutralText, alreadyFoundAddTexts,
                                                                  alreadyFoundNeutralTexts);
                iPartsDataPartListEntry.setCombinedText(partListEntry, combinedText, addText.getValue(), neutralText.getValue(), combinedMultiTextList, getEtkProject());
            } else {
                iPartsDataPartListEntry.setCombinedText(partListEntry, combinedText, null, null, combinedMultiTextList, getEtkProject());
            }

            // Ergänzungstexte Quelle GenVO?
            String sourceGenVO = SQLStringConvert.booleanToPPString(Utils.objectEquals(combTextSourceGenVOMap.get(kLfdnr), Boolean.TRUE));
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT_SOURCE_GENVO, sourceGenVO,
                                                   true, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Berechnete virtuelle Felder für Anzeige DIALOG Änderungen zu Stückliste hinzufügen
     *
     * @param partlist
     */
    private void loadAllDIALOGChangesForPartList(DBDataObjectList<EtkDataPartListEntry> partlist) {
        // DIALOG Änderungen werden nur bei aktivem iParts Edit Plug-in für echte DIALOG-Stücklisten (nicht PSK!) benötigt
        if (iPartsPlugin.isEditPluginActive() && getDocumentationType().isDIALOGDocumentationType() && !isPSKAssembly()) {
            final EtkProject etkProject = getEtkProject();

            // Join für Änderungen an BCTE-Keys
            final Map<String, iPartsDataDIALOGChangeList> dataDialogChangesForBCTEMap = new HashMap<>();

            EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes attributes) {
                    String bcteKey = attributes.getFieldValue(FIELD_DDC_BCTE);
                    iPartsDataDIALOGChangeList changeList = dataDialogChangesForBCTEMap.get(bcteKey);
                    if (changeList == null) {
                        changeList = new iPartsDataDIALOGChangeList();
                        dataDialogChangesForBCTEMap.put(bcteKey, changeList);
                    }

                    iPartsDialogChangesId changeId = new iPartsDialogChangesId(attributes.getFieldValue(FIELD_DDC_DO_TYPE),
                                                                               attributes.getFieldValue(FIELD_DDC_DO_ID),
                                                                               attributes.getFieldValue(FIELD_DDC_HASH));
                    iPartsDataDIALOGChange dataDIALOGChange = new iPartsDataDIALOGChange(etkProject, changeId);
                    dataDIALOGChange.setFieldValue(FIELD_DDC_KATALOG_ID, attributes.getFieldValue(FIELD_DDC_KATALOG_ID), DBActionOrigin.FROM_DB);
                    changeList.add(dataDIALOGChange, DBActionOrigin.FROM_DB);
                    return false;
                }
            };

            iPartsDataDIALOGChangeList allDataDIALOGChangesForBCTE = new iPartsDataDIALOGChangeList();
            allDataDIALOGChangesForBCTE.searchSortAndFillWithJoin(etkProject, null, null,
                                                                  new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                                                TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER) },
                                                                  new String[]{ getAsId().getKVari(), getAsId().getKVer() },
                                                                  new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SOURCE_GUID) },
                                                                  new String[]{ "" }, false, null, false, false, foundAttributesCallback,
                                                                  new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                                                 new String[]{ FIELD_DDC_BCTE },
                                                                                                 new String[]{ FIELD_K_SOURCE_GUID },
                                                                                                 false, false));

            // Join für Änderungen an Materialien
            final Map<String, iPartsDataDIALOGChangeList> dataDialogChangesForMatMap = new HashMap<>();

            foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes attributes) {
                    String seriesNumber = attributes.getFieldValue(FIELD_DDC_SERIES_NO);
                    String matNr = attributes.getFieldValue(FIELD_DDC_MATNR);
                    String matKey = seriesNumber + "@" + matNr;
                    iPartsDataDIALOGChangeList changeList = dataDialogChangesForMatMap.get(matKey);
                    if (changeList == null) {
                        changeList = new iPartsDataDIALOGChangeList();
                        dataDialogChangesForMatMap.put(matKey, changeList);
                    }

                    iPartsDialogChangesId changeId = new iPartsDialogChangesId(attributes.getFieldValue(FIELD_DDC_DO_TYPE),
                                                                               attributes.getFieldValue(FIELD_DDC_DO_ID),
                                                                               attributes.getFieldValue(FIELD_DDC_HASH));
                    iPartsDataDIALOGChange dataDIALOGChange = new iPartsDataDIALOGChange(etkProject, changeId);
                    dataDIALOGChange.setFieldValue(FIELD_DDC_KATALOG_ID, attributes.getFieldValue(FIELD_DDC_KATALOG_ID), DBActionOrigin.FROM_DB);
                    changeList.add(dataDIALOGChange, DBActionOrigin.FROM_DB);
                    return false;
                }
            };

            // Baureihe kann im Join leider nicht berücksichtigt werden, da diese kein separates Feld am Stücklisteneintrag
            // ist sondern nur aus K_SOURCE_GUID extrahiert werden kann -> Filterung weiter unten in der Zuweisung über
            // virtuelles Feld DA_AS_DIALOG_SERIES_NO
            iPartsDataDIALOGChangeList allDataDIALOGChangesForMatNr = new iPartsDataDIALOGChangeList();
            allDataDIALOGChangesForMatNr.searchSortAndFillWithJoin(etkProject, null, null,
                                                                   new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                                                 TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER) },
                                                                   new String[]{ getAsId().getKVari(), getAsId().getKVer() },
                                                                   new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_MATNR) },
                                                                   new String[]{ "" }, false, null, false, false, foundAttributesCallback,
                                                                   new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                                                  new String[]{ FIELD_DDC_MATNR },
                                                                                                  new String[]{ FIELD_K_MATNR },
                                                                                                  false, false));


            iPartsProductId productId = this.getProductIdFromModuleUsage();
            Set<String> productFactories = new TreeSet<>();
            if (productId != null) {
                iPartsProduct product = iPartsProduct.getInstance(etkProject, productId);
                productFactories.addAll(product.getProductFactories(etkProject));
            }

            // Änderungen den Stücklisteneinträgen zuweisen
            for (EtkDataPartListEntry partListEntry : partlist) {
                // Änderungen an BCTE-Key
                String bcteKey = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
                iPartsDataDIALOGChangeList dataDIALOGChangesForBCTE = dataDialogChangesForBCTEMap.get(bcteKey);

                // Einschränken auf konkreten Stücklisteneintrag
                if (dataDIALOGChangesForBCTE != null) {
                    dataDIALOGChangesForBCTE = dataDIALOGChangesForBCTE.filterForPartListEntry(partListEntry.getAsId());
                }

                // Änderungen an Material
                String seriesNumber = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO);
                String matNo = partListEntry.getFieldValue(FIELD_K_MATNR);
                String matKey = seriesNumber + "@" + matNo;
                iPartsDataDIALOGChangeList dataDIALOGChangesForMat = dataDialogChangesForMatMap.get(matKey);

                // Einschränken auf konkreten Stücklisteneintrag
                if (dataDIALOGChangesForMat != null) {
                    dataDIALOGChangesForMat = dataDIALOGChangesForMat.filterForPartListEntry(partListEntry.getAsId());
                }

                // Die DialogChanges zu den Werksdaten zu Farben zusätzlich auf die relevanten Werke zum Produkt einschränken
                if (dataDIALOGChangesForMat != null) {
                    dataDIALOGChangesForMat = dataDIALOGChangesForMat.filterForColorTableFactories(productFactories);
                }

                // Gibt es Änderungen?
                boolean hasChanges = (dataDIALOGChangesForBCTE != null) || (dataDIALOGChangesForMat != null);
                partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE,
                                                       SQLStringConvert.booleanToPPString(hasChanges),
                                                       true, DBActionOrigin.FROM_DB);

                String changeReason = iPartsPartlistTextHelper.getDIALOGChangeReason(dataDIALOGChangesForBCTE, dataDIALOGChangesForMat);
                partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_CHANGE_REASON, changeReason,
                                                       true, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Berechnetes virtuelles Feld für den GenVO-Text hinzufügen
     *
     * @param partlist
     */
    private void loadAllGenVoTexts(DBDataObjectList<EtkDataPartListEntry> partlist) {
        // GenVO-Texte werden nur bei aktivem iParts Edit Plug-in für echte DIALOG-Retail/Konstruktions-Stücklisten benötigt
        if (iPartsPlugin.isEditPluginActive() && (getDocumentationType().isPKWDocumentationType() || isDialogSMConstructionAssembly())) {
            final EtkProject etkProject = getEtkProject();

            // GenVO-Texte über die GenVO-Nummern den Stücklisteneinträgen zuweisen
            iPartsGenVoTextsCache genVoTextCache = iPartsGenVoTextsCache.getInstance(etkProject);
            String dbLanguage = etkProject.getDBLanguage();
            List<String> dataBaseFallbackLanguages = etkProject.getConfig().getDataBaseFallbackLanguages();
            for (EtkDataPartListEntry partListEntry : partlist) {
                String genVoNumber = getGenInstallLocationAttributes(partListEntry).getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO);
                String genVoText;
                if (StrUtils.isValid(genVoNumber)) {
                    genVoText = genVoTextCache.getGenVoText(genVoNumber, dbLanguage, dataBaseFallbackLanguages);
                } else {
                    genVoText = "";
                }
                partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.K_GENVO_TEXT, StrUtils.getEmptyOrValidString(genVoText),
                                                       true, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Berechnete virtuelle Felder für die Cemat EinPAS-Referenz zur Stückliste hinzufügen
     *
     * @param partlist
     */
    private void loadAllCematEinPASForPartList(DBDataObjectList<EtkDataPartListEntry> partlist) {
        Map<PartListEntryId, List<iPartsDataModuleCemat>> cematMapForModule = getCematMapForModule();
        for (EtkDataPartListEntry partListEntry : partlist) {
            String cematEinPASValue = iPartsDataModuleCematList.buildEinPasVersionCematString(cematMapForModule, partListEntry);
            attributes.addField(iPartsDataVirtualFieldsDefinition.RETAIL_CEMAT_EINPAS, cematEinPASValue, true, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Zusatzmaterialien (ES1, ES2) an Stücklisteneinträge hängen
     *
     * @param partlist
     */
    public void loadAllAlternativePartsForPartList(DBDataObjectList<EtkDataPartListEntry> partlist) {
        /**
         * Mit SQL-Schnittstelle zu Fuß hätte ich im Join noch hinzugefügt "and katalog.k_ver = ''". Die Methode searchSortAndFillWithJoin()
         * unterstützt das nicht. Es gab noch noch den Vorschlag statt des nicht vorhandenen M_BASE_MVER-Feldes mit
         * M_VER zu joinen. Das hat mir nicht gefallen. Die Diskussion ist eh akademisch.
         *
         * Weiterhin hätte ich zur Sicherheit noch in der Where-Bedingung hinzugefügt: "and M_BASE_MATNR<>M_MATNR". Dann hätte man das normale
         * Material ausgeschlossen egal ob der künftige Import das Feld M_BASE_MATNR für normale Materialnummern füllt oder nicht.
         * So muss es nun leer bleiben. Die besagte Methode unterstützt dies aber auch nicht.
         */

        // alle Matfelder, die wir brauchen müssen wir hier angeben; sollte man eines vergessen, würde beim Zugriff auf das feld ein loadFromDB()
        // erfolgen und das Fremdfeld K_LFDNR rausfliegen.
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_LFDNR, false, false));

        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_BASE_MATNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_1, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_2, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_STATE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_SECURITYSIGN_REPAIR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_IS_DELETED, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR_MBAG, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR_DTAG, false, false));

        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                     TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER) };
        String[] whereValues = new String[]{ getAsId().getKVari(), getAsId().getKVer() };

        // Upper-Aufruf für K_MATNR, damit kein Index für K_MATNR verwendet wird, da die Ergebnismenge für <> '' extrem
        // groß wäre; mit diesem Trick nehmen wir der DB die Möglichkeit, durch "Optimierungen" doch wieder langsam zu werden
        // Für die getRecords()-Simulation von einfachen Joins wird allerdings zusätzlich auch das echte Feld K_MATNR ohne
        // upper()-Aufruf benötigt, damit die Filterung korrekt funktioniert
        String[] whereNotTableAndFields = new String[]{ "upper(" + TableAndFieldName.make(TABLE_KATALOG, FIELD_K_MATNR) + ")",
                                                        TableAndFieldName.make(TABLE_KATALOG, FIELD_K_MATNR),
                                                        TableAndFieldName.make(TABLE_MAT, FIELD_M_AS_ES_1) };
        String[] whereNotValues = new String[]{ "", "", "" };

        String[] sourceJoinFields = new String[]{ FIELD_M_BASE_MATNR/*, ""*/ };
        String[] joinTableFields = new String[]{ FIELD_K_MATNR/*, FIELD_K_MVER*/ };
        EtkDataObjectList.JoinData joinData = new EtkDataObjectList.JoinData(TABLE_KATALOG, sourceJoinFields, joinTableFields, false, false);

        EtkDataPartList dataPartList = new EtkDataPartList();
        String dbLanguage = getEtkProject().getDBLanguage();

        // Bei PostgreSQL einen expliziten Hint setzen, damit der Query Planner keinen langsamen Execution Plan erzeugt
        if (getEtkProject().getEtkDbs().getDatabaseType(DBDatabaseDomain.MAIN) == DatabaseType.POSTGRES) {
            dataPartList.setPrependedCommentOrHintForSearchWithJoin("/*+ Leading( ( katalog mat ) ) NestLoop(katalog mat) */");
        }

        dataPartList.searchSortAndFillWithJoin(getEtkProject(), dbLanguage, selectFields, whereTableAndFields, whereValues,
                                               whereNotTableAndFields, whereNotValues, false, new String[]{ FIELD_M_MATNR },
                                               false, false, null, joinData);

        /**
         * Zuordnung k_lfdnr ->> Alternativteile ermitteln
         * Dabei entfernen wir ungültige Teile und hängen an die gültigen Teile noch das DataObject für DA_ES1
         */
        Map<String, Set<EtkDataPart>> alternativePartsByLfdnr = new TreeMap<>();
        iPartsES1 es1Cache = iPartsES1.getInstance(getEtkProject());
        for (EtkDataPart dataPart : dataPartList) {
            // M_IS_DELETED nachträglich ausfiltern und nicht in die whereFields aufnehmen, damit es keine Probleme bei
            // der Verwendung von Indizes gibt und das Statement unnötig langsam wird
            if (dataPart.getFieldValueAsBoolean(FIELD_M_IS_DELETED)) {
                continue;
            }

            String baseMatNr = dataPart.getFieldValue(FIELD_M_BASE_MATNR);
            String es1Code = dataPart.getFieldValue(FIELD_M_AS_ES_1);
            String es2Code = dataPart.getFieldValue(FIELD_M_AS_ES_2);

            // Nur Materialien mit leerem ES2 oder passender Fußnote am Basisteil (DAIMLER-9557)...
            if ((es2Code.isEmpty()) || es1Cache.checkFootnoteValidity(es1Code, baseMatNr, getEtkProject())) {
                // ... und State iPartsES1.VALID_MAT_STATE_FOR_ES1=30 werden als Alternativteil erkannt und berücksichtigt.
                String state = dataPart.getFieldValue(FIELD_M_STATE);
                if (StrUtils.isValid(es1Code) && state.equals(iPartsES1.VALID_MAT_STATE_FOR_ES1)) {
                    String es1Type = es1Cache.getType(es1Code);
                    if (StrUtils.isValid(es1Type)) {
                        addES1ToPart(dataPart, es1Code, es1Type);

                        String k_lfdnr = dataPart.getFieldValue(FIELD_K_LFDNR);
                        Set<EtkDataPart> alternativeParts = alternativePartsByLfdnr.get(k_lfdnr);
                        if (alternativeParts == null) {
                            alternativeParts = new LinkedHashSet<>();
                            alternativePartsByLfdnr.put(k_lfdnr, alternativeParts);
                        }
                        alternativeParts.add(dataPart);
                    }
                }
            }
        }

        // Alternativteile den Stücklisteneinträgen zuweisen
        boolean isZBAggTypeEngine = getModuleMetaData().getAggTypeForSpecialZBFilter() == DCAggregateTypes.ENGINE;
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                Set<EtkDataPart> parts = alternativePartsByLfdnr.get(partListEntry.getAsId().getKLfdnr());

                // Motor-Reman-Varianten als Alternativteile hinzufügen
                if (isZBAggTypeEngine) {
                    EtkDataPart dataPart = partListEntry.getPart();
                    String partNoBasic = dataPart.getFieldValue(FIELD_M_PARTNO_BASIC);
                    String partNoShortBlock = dataPart.getFieldValue(FIELD_M_PARTNO_SHORTBLOCK);
                    String partNoLongBlock = dataPart.getFieldValue(FIELD_M_PARTNO_LONGBLOCK);
                    String partNoLongBlockPlus = dataPart.getFieldValue(FIELD_M_PARTNO_LONGBLOCK_PLUS);
                    if (!StrUtils.isEmpty(partNoBasic, partNoShortBlock, partNoLongBlock, partNoLongBlockPlus)) {
                        if (parts == null) {
                            parts = new LinkedHashSet<>();
                        }
                        addEngineRemanVariantAlternativePart(parts, partNoBasic, iPartsDataES1.TYPE_ENGINE_BASIC);
                        addEngineRemanVariantAlternativePart(parts, partNoLongBlockPlus, iPartsDataES1.TYPE_ENGINE_LONG_BLOCK_PLUS);
                        addEngineRemanVariantAlternativePart(parts, partNoLongBlock, iPartsDataES1.TYPE_ENGINE_LONG_BLOCK);
                        addEngineRemanVariantAlternativePart(parts, partNoShortBlock, iPartsDataES1.TYPE_ENGINE_SHORT_BLOCK);
                    }
                }
                if (parts != null) {
                    ((iPartsDataPartListEntry)partListEntry).setAlternativeParts(parts);
                }
            }
        }
    }

    private void addES1ToPart(EtkDataPart dataPart, String es1Code, String es1Type) {
        // DataObject für DA_ES1 an dataPart hängen
        iPartsES1Id es1Id = new iPartsES1Id(es1Code, "");
        iPartsDataES1 dataES1 = new iPartsDataES1(getEtkProject(), es1Id);
        dataES1.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        dataES1.setFieldValue(FIELD_DES_TYPE, es1Type, DBActionOrigin.FROM_DB);
        DBDataObjectList children = new DBDataObjectList();
        children.add(dataES1, DBActionOrigin.FROM_DB);
        dataPart.setChildren(TABLE_DA_ES1, children);
    }

    private void addEngineRemanVariantAlternativePart(Set<EtkDataPart> parts, String engineRemanVariantPartNo, String es1Type) {
        if (engineRemanVariantPartNo.isEmpty()) {
            return;
        }

        EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getEtkProject(), new PartId(engineRemanVariantPartNo, ""));
        if (!dataPart.existsInDB()) {
            // Materialstamm nicht gefunden (kann eigentlich nicht passieren) -> Basisdaten befüllen
            dataPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            dataPart.setFieldValue(FIELD_M_BESTNR, engineRemanVariantPartNo, DBActionOrigin.FROM_DB);
            dataPart.setFieldValue(FIELD_M_BASE_MATNR, numberHelper.getBasePartNoFromDialogInputPartNo(engineRemanVariantPartNo),
                                   DBActionOrigin.FROM_DB);
            dataPart.setFieldValue(FIELD_M_AS_ES_1, numberHelper.getES1FromDialogInputPartNo(engineRemanVariantPartNo),
                                   DBActionOrigin.FROM_DB);
        }
        addES1ToPart(dataPart, dataPart.getFieldValue(FIELD_M_AS_ES_1), es1Type);
        parts.add(dataPart);
    }

    /**
     * Lädt alle Fußnoten inkl. deren Texte für die gesamte Stückliste
     *
     * @param partlist
     */
    public void loadAllFootNotesForPartList(DBDataObjectList<EtkDataPartListEntry> partlist) {
        // Bei aktivem ChangeSet mit prinzipiell erlaubter getRecords()-Simulation keinen Dreifach-Join sondern einen Einzel-Join
        // und Nachladen von Daten verwenden, damit die Joins mittels getRecords()-Simulation anstatt mit einer forcierten
        // Pseudo-Transaktion laufen können
        boolean useTripleJoin = EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS || !isRevisionChangeSetActive();
        if (!useTripleJoin) {
            EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
            if (revisionsHelper != null) {
                // Der dreifache Join kann trotz aktivem ChangeSet auch dann verwendet werden, wenn es keine veränderten
                // oder neuen SerializedDBDataObjects in den Tabellen DA_FN_CONTENT und DA_FN gibt (weil dafür keine where-Bedingungen
                // vorhanden sind) sowie keine veränderten oder neuen in den Tabellen DA_FN_KATALOG_REF und KATALOG für
                // die aktuelle AssemblyId
                useTripleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_FN_CONTENT, null,
                                                                                       RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                if (useTripleJoin) {
                    useTripleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_FN, null,
                                                                                           RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                    if (useTripleJoin) {
                        String[] assemblyPKValues = { getAsId().getKVari(), getAsId().getKVer() };
                        useTripleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_FN_KATALOG_REF, assemblyPKValues,
                                                                                               RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                        if (useTripleJoin) {
                            useTripleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_KATALOG, assemblyPKValues,
                                                                                                   RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                        }
                    }
                }
            }
        }

        // Join von den Fußnotentexten (DA_FN_CONTENT) auf die Fußnoten-Katalog-Tabelle (DA_FN_KATALOG_REF) sowie
        // die Fußnotenstammdaten (DA_FN) und zuletzt mit der KATALOG-Tabelle für alle Stücklisteneinträge des Moduls
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VER, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_LFDNR, false, false));

        EtkDisplayFields selectFieldsForFootNote = new EtkDisplayFields();
        selectFieldsForFootNote.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_ID, false, false));
        selectFieldsForFootNote.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_NAME, false, false));
        selectFieldsForFootNote.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_STANDARD, false, false));
        selectFieldsForFootNote.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_TYPE, false, false));

        EtkDisplayFields selectFieldsForFnContent = new EtkDisplayFields();
        selectFieldsForFnContent.addFeld(new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO, false, false));
        selectFieldsForFnContent.addFeld(new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT, true, false));
        selectFieldsForFnContent.addFeld(new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT_NEUTRAL, false, false));

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_SEQNO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_FN_SEQNO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_FN_MARKED, false, false));

        EtkDataObjectList.JoinData footNoteJoinData = new EtkDataObjectList.JoinData(TABLE_DA_FN,
                                                                                     new String[]{ FIELD_DFNC_FNID },
                                                                                     new String[]{ FIELD_DFN_ID },
                                                                                     false, false);
        EtkDataObjectList.JoinData katalogJoinData = new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                                    new String[]{ TableAndFieldName.make(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_MODULE),
                                                                                                  TableAndFieldName.make(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_MODVER),
                                                                                                  TableAndFieldName.make(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_SEQNO) },
                                                                                    new String[]{ FIELD_K_VARI, FIELD_K_VER, FIELD_K_LFDNR },
                                                                                    false, false);

        EtkProject etkProject = getEtkProject();
        String dbLanguage = etkProject.getDBLanguage();
        iPartsDataFootNoteContentList footNoteContentList = new iPartsDataFootNoteContentList();

        if (useTripleJoin) {
            selectFields.addFelder(selectFieldsForFootNote);
            selectFields.addFelder(selectFieldsForFnContent);
            footNoteContentList.searchSortAndFillWithJoin(etkProject, dbLanguage, selectFields,
                                                          new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                                        TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER),
                                                                        // getRecords()-Simulation von Joins unterstützen durch diese redundanten where-Felder
                                                                        TableAndFieldName.make(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_MODULE),
                                                                        TableAndFieldName.make(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_MODVER) },
                                                          new String[]{ getAsId().getKVari(), getAsId().getKVer(),
                                                                        getAsId().getKVari(), getAsId().getKVer() }, false,
                                                          new String[]{ TableAndFieldName.make(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_FN_SEQNO),
                                                                        TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO) },
                                                          false, null,
                                                          new EtkDataObjectList.JoinData(TABLE_DA_FN_KATALOG_REF,
                                                                                         new String[]{ FIELD_DFNC_FNID },
                                                                                         new String[]{ FIELD_DFNK_FNID },
                                                                                         false, false),
                                                          footNoteJoinData, katalogJoinData);
        } else {
            // Zunächst alle Fußnoten-Katalog-Referenzdaten sortiert nach DFNK_FN_SEQNO und Fußnoten-IDs über einen simplen Join bestimmen
            final Set<DBDataObjectAttributes> fnCatalogueRefAttributesList = new TreeSet<>((o1, o2) -> {
                // Sortierung nach lfdNr vom Stücklisteneintrag und danach Fußnoten-Sequenznummer
                int result = o1.getFieldValue(FIELD_DFNK_SEQNO).compareTo(o2.getFieldValue(FIELD_DFNK_SEQNO));
                if (result != 0) {
                    return result;
                }
                return o1.getFieldValue(FIELD_DFNK_FN_SEQNO).compareTo(o2.getFieldValue(FIELD_DFNK_FN_SEQNO));
            });
            final Map<String, iPartsDataFootNoteContentList> fnContentAttributesMap = new HashMap();

            EtkDataObjectList.FoundAttributesCallback foundFnCatalogueRefSimpleJoinCallback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes dataAttributes) {
                    fnCatalogueRefAttributesList.add(dataAttributes);
                    String fnId = dataAttributes.getFieldValue(FIELD_DFNK_FNID);
                    fnContentAttributesMap.put(fnId, null);
                    return false;
                }
            };

            iPartsDataFootNoteCatalogueRefList fnCatalogueRefList = new iPartsDataFootNoteCatalogueRefList();
            fnCatalogueRefList.searchSortAndFillWithJoin(etkProject, dbLanguage, selectFields,
                                                         new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                                       TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER),
                                                                       // getRecords()-Simulation von Joins unterstützen durch diese redundanten where-Felder
                                                                       TableAndFieldName.make(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_MODULE),
                                                                       TableAndFieldName.make(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_MODVER) },
                                                         new String[]{ getAsId().getKVari(), getAsId().getKVer(),
                                                                       getAsId().getKVari(), getAsId().getKVer() }, false,
                                                         null, // Sortierung funktioniert mit einem reinen Callback nicht
                                                         false, foundFnCatalogueRefSimpleJoinCallback, katalogJoinData);

            if (!fnCatalogueRefAttributesList.isEmpty()) {
                selectFields.clear();
                selectFields.addFelder(selectFieldsForFootNote);
                selectFields.addFelder(selectFieldsForFnContent);

                // Die Daten aus DA_FN und DA_FN_CONTENT für jede Fußnoten-ID jeweils über einen simplen Join laden
                for (String fnId : ArrayUtil.toStringArray(fnContentAttributesMap.keySet())) {
                    iPartsDataFootNoteContentList singleFootNoteContentList = new iPartsDataFootNoteContentList();
                    singleFootNoteContentList.searchSortAndFillWithJoin(etkProject, dbLanguage, selectFields,
                                                                        new String[]{ TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_FNID),
                                                                                      // getRecords()-Simulation von Joins unterstützen durch diese redundanten where-Felder
                                                                                      TableAndFieldName.make(TABLE_DA_FN, FIELD_DFN_ID) },
                                                                        new String[]{ fnId, fnId }, false,
                                                                        new String[]{ TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO) },
                                                                        false, null, footNoteJoinData);
                    if (!singleFootNoteContentList.isEmpty()) {
                        fnContentAttributesMap.put(fnId, singleFootNoteContentList);
                    }
                }

                // Alle Daten in der footNoteContentList zusammenführen
                for (DBDataObjectAttributes fnCatalogueRefAttributes : fnCatalogueRefAttributesList) {
                    String fnId = fnCatalogueRefAttributes.getFieldValue(FIELD_DFNK_FNID);

                    // Daten aus DA_FN und DA_FN_CONTENT wie bei einem Inner-Join pro Zeile aus DA_FN_CONTENT hinzufügen
                    iPartsDataFootNoteContentList singleFootNoteContentList = fnContentAttributesMap.get(fnId);
                    if (singleFootNoteContentList != null) {
                        for (iPartsDataFootNoteContent dataFootNoteContent : singleFootNoteContentList) {
                            iPartsDataFootNoteContent mergedFootNoteContent = dataFootNoteContent.cloneMe(etkProject);
                            mergedFootNoteContent.getAttributes().addFields(fnCatalogueRefAttributes, DBActionOrigin.FROM_DB);

                            // Zusammengeführte Daten in footNoteContentList ablegen für die weitere Verarbeitung weiter unten
                            footNoteContentList.add(mergedFootNoteContent, DBActionOrigin.FROM_DB);
                        }
                    } else {
                        // Wenn keine Daten gefunden wurden, hier nichts machen, weil es sich um Inner-Joins und keine
                        // LeftOuter-Joins handelt
                    }
                }
            }
        }

        iPartsPartFootnotesCache partFootNotesCache = iPartsPartFootnotesCache.getInstance(getEtkProject());
        iPartsDIALOGFootNotesCache dialogFootNotesCache = iPartsDIALOGFootNotesCache.getInstance(getEtkProject());
        iPartsPRIMUSReplacementsLoader primusReplacementsLoader = new iPartsPRIMUSReplacementsLoader(getEtkProject(), partlist);

        // Bei einer leeren Ergebnisliste in allen Stücklisteneinträgen die Fußnoten als geladen markieren
        if (footNoteContentList.isEmpty()) {
            for (EtkDataPartListEntry partListEntry : partlist) {
                if (partListEntry instanceof iPartsDataPartListEntry) {
                    // Alle aufgesammelten Fußnoten
                    List<iPartsFootNote> allCachedFootNotes = new ArrayList<>();
                    // Mögliche Teilestamm- und DIALOG-Fußnoten
                    iPartsFootNoteHelper.addPartAndDIALOGFootnotes(partListEntry, allCachedFootNotes, new HashSet<>(),
                                                                   partFootNotesCache, dialogFootNotesCache);
                    // Check, ob virtuelle Fußnoten angehängt werden sollen
                    Collection<iPartsFootNote> footNotes = iPartsVirtualFootnoteHelper.addVirtualFootnotes((iPartsDataPartListEntry)partListEntry,
                                                                                                           allCachedFootNotes,
                                                                                                           primusReplacementsLoader);
                    ((iPartsDataPartListEntry)partListEntry).setFootNotes(footNotes);
                }
            }
            return;
        }

        List<String> dbFallbackLanguages = project.getDataBaseFallbackLanguages();

        // Map von laufende Nummer der Stückliste auf Map von Fußnoten-ID auf iPartsFootNote aufbauen
        Map<String, Map<String, iPartsFootNote>> allFootNotesMap = new HashMap<>();
        for (iPartsDataFootNoteContent dataFootNoteContent : footNoteContentList) {
            // Map von laufende Nummer auf Map von Fußnoten-ID auf iPartsFootNote bestimmen
            String kLfdnr = dataFootNoteContent.getFieldValue(FIELD_K_LFDNR);

            Map<String, iPartsFootNote> footNotes = allFootNotesMap.get(kLfdnr);
            if (footNotes == null) {
                footNotes = new LinkedHashMap<>(); // Reihenfolge muss erhalten bleiben, da footNoteContentList bereits sortiert ist
                allFootNotesMap.put(kLfdnr, footNotes);
            }

            // Fußnote bestimmen
            String footNoteNumber = dataFootNoteContent.getFieldValue(FIELD_DFN_ID);
            iPartsFootNote footNote = footNotes.get(footNoteNumber);
            if (footNote == null) {
                String footNoteName = dataFootNoteContent.getFieldValue(FIELD_DFN_NAME);
                boolean isStandardFootnote = dataFootNoteContent.getFieldValueAsBoolean(FIELD_DFN_STANDARD);
                String dfnType = dataFootNoteContent.getFieldValue(FIELD_DFN_TYPE);
                footNote = new iPartsFootNote(new iPartsFootNoteId(footNoteNumber), footNoteName, new DwList<>(),
                                              isStandardFootnote, iPartsFootnoteType.getFromDBValue(dfnType));
                footNote.setIsMarked(dataFootNoteContent.getFieldValueAsBoolean(FIELD_DFNK_FN_MARKED));
                footNotes.put(footNoteNumber, footNote);
            }

            // Eigentlichen Fußnotentext hinzufügen
            footNote.getFootNoteTexts(etkProject).add(dataFootNoteContent.getText(dbLanguage, dbFallbackLanguages));
        }

        // Fußnoten den Stücklisteneinträgen zuweisen
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                // Collection für Fußnoten, die der Teileposition hinzugefügt werden
                Collection<iPartsFootNote> extendedFootnotes = new LinkedHashSet<>();
                // Set mit allen Fußnoten, die verarbeitet wurden (damit keine Doppelungen vorkommen)
                Set<iPartsFootNoteId> allCachedFootnotes = new HashSet<>();
                // Teilestamm und DIALOG Fußnoten hinzufügen
                iPartsFootNoteHelper.addPartAndDIALOGFootnotes(partListEntry, extendedFootnotes, allCachedFootnotes,
                                                               partFootNotesCache, dialogFootNotesCache);
                // Geladene Fußnoten
                Map<String, iPartsFootNote> footNotes = allFootNotesMap.get(partListEntry.getAsId().getKLfdnr());
                if ((footNotes != null) && !footNotes.isEmpty()) {
                    for (iPartsFootNote footNote : footNotes.values()) {
                        if (!allCachedFootnotes.contains(footNote.getFootNoteId())) {
                            extendedFootnotes.add(footNote);
                        }
                    }
                }
                extendedFootnotes = iPartsVirtualFootnoteHelper.addVirtualFootnotes((iPartsDataPartListEntry)partListEntry,
                                                                                    extendedFootnotes, primusReplacementsLoader);
                ((iPartsDataPartListEntry)partListEntry).setFootNotes(extendedFootnotes);
            }
        }
    }

    /**
     * Callback für das Laden der Werkseinsatzdaten. Hier werden schon diverse Vorfilterungen und Gruppierungen durchgeführt.
     * Das Ergebnis ist eine Map von laufender Nummer des Stücklisteneintrages auf die relevanten Werkseinsatzdaten.
     *
     * @param dataAttributes    Die Attribute der Werkseinsatzdaten
     * @param factoriesInstance Werks-Cache
     * @param seriesIdMap       Map von lfdNr auf Baureihe; wird hier befüllt
     * @param validStates       Gibt an, welche States die Datensätze haben dürfen, um berücksichtigt zu werden. Normalerweise
     *                          nur {@code RELEASED}.
     * @param allFactoryDataMap Rückgabewert: hier werden die Werkseinsatzdaten pro lfdNr in einer Map aufgesammelt
     * @param responseData
     * @param responseSpikes
     * @return
     */
    public boolean loadFactoryDataCallback(DBDataObjectAttributes dataAttributes, iPartsFactories factoriesInstance,
                                           Map<String, iPartsSeriesId> seriesIdMap, Set<String> validStates,
                                           Map<String, iPartsFactoryData> allFactoryDataMap, iPartsResponseData responseData,
                                           iPartsResponseSpikes responseSpikes) {
        // Nur freigegebene Datensätze übrig lassen.
        String status = dataAttributes.getFieldValue(FIELD_DFD_STATUS);
        if (StrUtils.isValid(status) && !validStates.contains(status)) {
            return false;
        }

        // Wenn das Werk nicht Filter-relevant ist, soll so getan werden als ob dazu keine Werksdaten existieren
        String factory = dataAttributes.getFieldValue(FIELD_DFD_FACTORY);
        if (!factoriesInstance.isValidForFilter(factory)) {
            return false;
        }

        String kLfdnr = dataAttributes.getFieldValue(FIELD_K_LFDNR);
        String pemFrom = dataAttributes.getFieldValue(FIELD_DFD_PEMA);
        String pemTo = dataAttributes.getFieldValue(FIELD_DFD_PEMB);
        long dateFrom = iPartsFactoryData.getFactoryDateFromDateString(dataAttributes.getFieldValue(FIELD_DFD_PEMTA),
                                                                       TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTA));
        long dateTo = iPartsFactoryData.getFactoryDateFromDateString(dataAttributes.getFieldValue(FIELD_DFD_PEMTB),
                                                                     TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTB));

        // Bei DIALOG muss nach den gültigen Werken für das Produkt und Baureihe gefiltert werden
        boolean isFactoryDataValid = true;
        boolean hasFactoryDataWithInfiniteDates = false;
        iPartsSeriesId seriesId = null;
        if (getDocumentationType().isPKWDocumentationType()) {
            // Prüfung auf gültiges Werk
            seriesId = getSeriesFromPartListEntryAttributes(dataAttributes, kLfdnr, seriesIdMap);
            Set<String> validFactories = FactoriesHelper.getValidFactories(getEtkProject(), getProductIdFromModuleUsage());
            if (!validFactories.contains(factory)) {
                isFactoryDataValid = false; // Werk ist ungültig
            }

            // Prüfung auf PEM-Termin-AB und PEM-TERMIN-BIS (nur sinnvoll bei gültigem Werk)
            if (isFactoryDataValid) {
                // DAIMLER-3744 und DAIMLER-6916: Das Teil wurde nie eingebaut (ursprünglich war PEMTA bzw. PEMTB 999999...),
                // aber beim Baumuster-Zeitscheibenfilter muss dies berücksichtigt werden
                if ((dateFrom == 0) && (dateTo == 0)) {
                    isFactoryDataValid = false;
                    hasFactoryDataWithInfiniteDates = true;
                }
                // Seit DAIMLER-6564 müssen hier Werkseinsatzdaten mit negativen Zeitintervallen (PEM Termin ab > PEM Termin bis)
                // durchkommen, weil diese erst später korrigiert bzw. ausgefiltert werden
            }

        }

        // Map von laufende Nummer auf iPartsFactoryDataForRetail bestimmen
        String adatString = dataAttributes.getFieldValue(FIELD_DFD_ADAT);
        long adat = StrUtils.strToLongDef(adatString, 0);
        String dataId = dataAttributes.getFieldValue(FIELD_DFD_DATA_ID);
        List<iPartsFactoryData.DataForFactory> dataForFactoryList = null;
        iPartsFactoryData.DataForFactory dataForFactory = null;
        iPartsFactoryData factoryData = allFactoryDataMap.get(kLfdnr);
        if (factoryData == null) {
            factoryData = new iPartsFactoryData();
            factoryData.setHasFactoryDataWithInfiniteDates(hasFactoryDataWithInfiniteDates);
            allFactoryDataMap.put(kLfdnr, factoryData);

            if (!isFactoryDataValid) { // Datensatz ist ungültig -> nur neues iPartsFactoryDataForRetail für die laufende Nummer merken
                return false;
            }
        } else {
            if (hasFactoryDataWithInfiniteDates) { // Flag nur auf true setzen, aber nicht zurücksetzen
                factoryData.setHasFactoryDataWithInfiniteDates(hasFactoryDataWithInfiniteDates);
            }
            if (!isFactoryDataValid) { // Datensatz ist ungültig -> keine weiteren Aktionen notwendig
                return false;
            }

            dataForFactoryList = factoryData.getDataForFactory(factory);

            // Bei DIALOG höchstes ADAT ermitteln und AS-Daten anstatt Produktionsdaten verwenden falls vorhanden
            if (getDocumentationType().isPKWDocumentationType()) {
                if (dataForFactoryList != null) {
                    if (dataId.equals(iPartsFactoryDataTypes.FACTORY_DATA_AS.getDbValue())) { // Logik analog zu FactoryDataHelper.filterFactoryDataForRetail()
                        if (!dataForFactoryList.isEmpty()) {
                            // Seit DAIMLLER-4807 kann es meherere AS-Datensätze für Werkseinsatzdaten geben. Dabei
                            // soll nur das neueste freigegeben ADAT genutzt werden. Falls das bestehende ADAT
                            // größer ist als das neue, wird das bestehende genommen. Neue Rückmeldedaten werden
                            // dann an den bestehenden Datensatz gehängt.
                            iPartsFactoryData.DataForFactory otherDataForFactory = dataForFactoryList.get(0);
                            if (otherDataForFactory.factoryDataId.getDataId().equals(iPartsFactoryDataTypes.FACTORY_DATA_AS.getDbValue())
                                && (otherDataForFactory.adat >= adat)) {
                                // Werkseinsatzdaten bereits gefunden -> neue Rückmeldedaten hinzufügen
                                dataForFactory = otherDataForFactory;
                            } else {
                                // Alle anderen Werkseinsatzdaten können nur von der Produktion kommen -> entfernen, da AS-Daten gewinnen
                                dataForFactoryList.clear();
                            }
                        }
                    } else { // Produktions-Daten
                        Iterator<iPartsFactoryData.DataForFactory> iterator = dataForFactoryList.iterator();
                        while (iterator.hasNext()) {
                            iPartsFactoryData.DataForFactory otherDataForFactory = iterator.next();
                            if (otherDataForFactory.factoryDataId.getDataId().equals(iPartsFactoryDataTypes.FACTORY_DATA_AS.getDbValue())) {
                                return false; // Diese Werkseinsatzdaten sind Produktions-Daten, aber es existieren schon AS-Daten, die Vorrang haben
                            } else if (otherDataForFactory.adat < adat) { // Logik analog zu FactoryDataHelper.filterForLatestFactoryData()
                                iterator.remove(); // Andere Produktions-Werkseinsatzdaten sind älter als diese
                            } else if (otherDataForFactory.adat == adat) {
                                // Analog zum ADAT muss auch die Sequenznummer geprüft werden, wenn das ADAT gleich ist
                                String seqNoSortString = Utils.toSortString(dataAttributes.getFieldValue(FIELD_DFD_SEQ_NO));
                                String otherDataSeqNoSortString = Utils.toSortString(otherDataForFactory.factoryDataId.getSeqNo());
                                int compareResult = otherDataSeqNoSortString.compareTo(seqNoSortString);
                                // Hat der andere Datensatz eine kleinere Sequenznummer ist er älter als der neue
                                // analog zum ADAT, alten Datensatz löschen
                                if (compareResult < 0) {
                                    iterator.remove();
                                } else if (compareResult == 0) {
                                    // Sequenznummer gleich, analog zu ADAT: Werkseinsatzdaten bereits gefunden
                                    // -> neue Rückmeldedaten hinzufügen
                                    dataForFactory = otherDataForFactory;
                                } else {
                                    // Diese Werkseinsatzdaten sind älter als otherDataForFactory
                                    return false;
                                }
                            } else {
                                return false; // Diese Werkseinsatzdaten sind älter als otherDataForFactory
                            }
                        }
                    }
                }
            }
        }

        if (dataForFactoryList == null) {
            dataForFactoryList = new DwList<>();
            factoryData.setDataForFactory(factory, dataForFactoryList);
        }

        // Neue Werkseinsatzdaten erzeugen falls notwendig und der Liste der Werkseinsatzdaten hinzufügen
        if (dataForFactory == null) {
            dataForFactory = new iPartsFactoryData.DataForFactory();
            dataForFactory.factoryDataId = new iPartsFactoryDataId(dataAttributes.getFieldValue(FIELD_DFD_GUID),
                                                                   factory, dataAttributes.getFieldValue(FIELD_DFD_SPKZ),
                                                                   adatString, dataId,
                                                                   dataAttributes.getFieldValue(FIELD_DFD_SEQ_NO));
            dataForFactory.adat = adat;
            dataForFactory.releaseState = iPartsDataReleaseState.getTypeByDBValue(dataAttributes.getFieldValue(FIELD_DFD_STATUS));
            dataForFactory.seriesNumber = dataAttributes.getFieldValue(FIELD_DFD_SERIES_NO);
            dataForFactory.pemFrom = pemFrom;
            dataForFactory.pemTo = pemTo;
            dataForFactory.stCodeFrom = dataAttributes.getFieldValue(FIELD_DFD_STCA);
            dataForFactory.stCodeTo = dataAttributes.getFieldValue(FIELD_DFD_STCB);
            dataForFactory.dateFrom = dateFrom;
            dataForFactory.dateTo = dateTo;
            dataForFactory.eldasFootNoteId = dataAttributes.getFieldValue(FIELD_DFD_FN_ID);
            dataForFactoryList.add(dataForFactory);
        }

        // Rückmeldedaten und Ausreißer zu PEMs bestimmen u. Werkseinsatzdaten zuweisen wenn gültig
        String aa = dataAttributes.getFieldValue(FIELD_K_AA);
        assignResponseDataAndSpikesToFactoryData(dataForFactory, pemFrom, factory, seriesId, aa, true, responseData, responseSpikes);
        assignResponseDataAndSpikesToFactoryData(dataForFactory, pemTo, factory, seriesId, aa, false, responseData, responseSpikes);

        return false;
    }

    /**
     * Lädt alle Werkseinsatzdaten für den Retail (Filterung und Webservices) für die übergebenen Stücklisteneinträge (normalerweise
     * die gesamte Stückliste).
     *
     * @param partlist
     */
    public void loadAllFactoryDataForRetailForPartList(DBDataObjectList<EtkDataPartListEntry> partlist) {

        // Map von laufender Nummer der Stückliste (nur für gefundene Werkseinsatzdaten) auf Baureihe
        final Map<String, iPartsSeriesId> seriesIdMap = new HashMap<>();

        // Map von laufende Nummer der Stückliste auf iPartsFactoryDataForRetail aufbauen
        final Map<String, iPartsFactoryData> allFactoryDataMap = new HashMap<>();

        final iPartsFactories factoriesInstance = iPartsFactories.getInstance(getEtkProject());

        // Rückmeldedaten-Cache; wird verwendet, um relevante Rückmeldedaten den Werkseinsatzdaten zuzuordnen
        final iPartsResponseData responseData = iPartsResponseData.getInstance(getEtkProject());
        // Ausreißer-Cache; wird verwendet, um relevante Ausreißer den Rückmeldedaten zuzuordnen
        final iPartsResponseSpikes responseSpikes = iPartsResponseSpikes.getInstance(getEtkProject());

        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes dataAttributes) {
                return loadFactoryDataCallback(dataAttributes, factoriesInstance, seriesIdMap,
                                               RELEASED_STATE_SET, allFactoryDataMap, responseData, responseSpikes);
            }
        };

        // Join mit der KATALOG-Tabelle für alle Stücklisteneinträge des Moduls mit Outer Join von den Werkseinsatzdaten
        // (DA_FACTORY_DATA) auf die Rückmeldedaten (DA_RESPONSE_DATA) sowie Outer Join auf die Ausreißer (DA_RESPONSE_SPIKES)
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VER, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_LFDNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_TYPE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_CONTEXT, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_AA, false, false));

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_GUID, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_FACTORY, false, false)); // muss ein gültiges Werk sein
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_SPKZ, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_ADAT, false, false)); // notwendig für Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_DATA_ID, false, false)); // notwendig für Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_SERIES_NO, false, false)); // notwendig für Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMA, false, false)); // Handelt es sich um eine PEM ab?
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMB, false, false)); // Handelt es sich um eine PEM bis?
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTA, false, false)); // Termin ab
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTB, false, false)); // Termin bis
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_STCA, false, false)); // Steuercode ab
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_STCB, false, false)); // Steuercode bis
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_FN_ID, false, false)); // ELDAS Fußnoten-ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_STATUS, false, false)); // Der Freigabestatus

        iPartsDataFactoryDataList factoryDataContentList = new iPartsDataFactoryDataList();

        if (partlist.size() > 1) { // Werkseinsatzdaten für mehrere Stücklisteneinträge per Join laden
            factoryDataContentList.searchSortAndFillWithJoin(getEtkProject(), null, selectFields,
                                                             new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                                           TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER) },
                                                             new String[]{ getAsId().getKVari(), getAsId().getKVer() }, false,
                                                             null, // sortFields in Kombination im FoundAttributesCallback sind wirkungslos
                                                             false, foundAttributesCallback,
                                                             new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                                            new String[]{ FIELD_DFD_GUID },
                                                                                            new String[]{ FIELD_K_SOURCE_GUID },
                                                                                            false, false));
        } else if (partlist.size() == 1) { // Bei genau einem Stücklisteneintrag brauchen wir keinen Join
            // Dieser Fall wird zum Aktualisieren der bearbeiteten Werkseinsatzdaten verwendet
            EtkDataPartListEntry partListEntry = partlist.get(0);
            factoryDataContentList.searchSortAndFill(getEtkProject(), TABLE_DA_FACTORY_DATA, null,
                                                     new String[]{ FIELD_DFD_GUID },
                                                     new String[]{ partListEntry.getFieldValue(FIELD_K_SOURCE_GUID) },
                                                     null, null, null,
                                                     DBDataObjectList.LoadType.COMPLETE, false, DBActionOrigin.FROM_DB);

            for (iPartsDataFactoryData factoryDataContent : factoryDataContentList) {
                DBDataObjectAttributes factoryDataAttributes = factoryDataContent.getAttributes();

                // Notwendige Attribute der KATALOG-Tabelle hinzufügen, damit der Datensatz wie beim Join aussieht
                factoryDataAttributes.addField(FIELD_K_VARI, partListEntry.getAsId().getKVari(), DBActionOrigin.FROM_DB);
                factoryDataAttributes.addField(FIELD_K_VER, partListEntry.getAsId().getKVer(), DBActionOrigin.FROM_DB);
                factoryDataAttributes.addField(FIELD_K_LFDNR, partListEntry.getAsId().getKLfdnr(), DBActionOrigin.FROM_DB);
                factoryDataAttributes.addField(FIELD_K_SOURCE_TYPE, partListEntry.getFieldValue(FIELD_K_SOURCE_TYPE), DBActionOrigin.FROM_DB);
                factoryDataAttributes.addField(FIELD_K_SOURCE_CONTEXT, partListEntry.getFieldValue(FIELD_K_SOURCE_CONTEXT), DBActionOrigin.FROM_DB);
                factoryDataAttributes.addField(FIELD_K_AA, partListEntry.getFieldValue(FIELD_K_AA), DBActionOrigin.FROM_DB);

                // Callback aufrufen wie beim Join
                foundAttributesCallback.foundAttributes(factoryDataAttributes);
            }
        }

        // Wg. Mio-Überlaufsbestimmung Aggregatetyp für das Modul bestimmen
        String aggregateType = "";
        iPartsProductId productId = getProductIdFromModuleUsage();
        if (productId != null) {
            iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), productId);
            aggregateType = product.getAggregateType();  // das ist der Aggregatetyp mit ggf. mehreren Stellen z.B. "GA"
        }

        // Werkseinsatzdaten den Stücklisteneinträgen zuweisen (Verdichten der Zusatzwerke darf seit DAIMLER-6018 erst nach
        // der Zeitscheibenfilterung im Baumusterfilter gemacht werden. Siehe iPartsDataAssembly.setFactoryDataForRetailWithoutReplacements)
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsFactoryData factoryData = null;
                if (!allFactoryDataMap.isEmpty()) {
                    factoryData = allFactoryDataMap.get(partListEntry.getAsId().getKLfdnr());
                }

                iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                iPartsPartListEntry.setAggregateType(aggregateType);

                if (factoryData != null) {
                    // Flags für PEM ab/bis auswerten setzen
                    factoryData.setEvalPemFrom(partListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED));
                    factoryData.setEvalPemTo(partListEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED));

                    // Werkseinsatzdaten dem Stücklisteneintrag zuweisen
                    iPartsPartListEntry.setFactoryDataForRetailUnfiltered(factoryData);
                } else {
                    // Auch null setzen, damit in dem Stücklisteneintrag die Werkseinsatzdaten als geladen markiert werden
                    iPartsPartListEntry.setFactoryDataForRetailUnfiltered(null);
                }
            }
        }
    }

    /**
     * ResponseData zu PEM bestimmen, auf Gültigkeit prüfen, und filterrelevante Felder an Werkseinsatzdaten hängen
     *
     * @param dataForFactory
     * @param pem
     * @param factory
     * @param seriesId       für DIALOG, sonst null
     * @param aa             für DIALOG, sonst null
     * @param isFrom         {@code true} für PEM-ab, {@code false} für PEM-bis
     * @param responseData
     * @param responseSpikes
     */
    private void assignResponseDataAndSpikesToFactoryData(iPartsFactoryData.AbstractDataForFactory dataForFactory,
                                                          String pem, String factory, iPartsSeriesId seriesId, String aa,
                                                          boolean isFrom, iPartsResponseData responseData, iPartsResponseSpikes responseSpikes) {
        if (!StrUtils.isValid(pem)) {
            return;
        }

        List<iPartsDataResponseData> dataResponseDataList = responseData.getResponseData(pem);
        if (dataResponseDataList == null) {
            return;
        }

        for (iPartsDataResponseData dataResponseData : dataResponseDataList) {
            if (!dataResponseData.isReleased()) {
                continue;
            }

            if (!dataResponseData.getFieldValue(FIELD_DRD_FACTORY).equals(factory)) {
                continue;
            }

            // Prüfungen für DIALOG (bei ELDAS sind seriesId und aa nicht gesetzt)
            String responseDataSeriesNo = dataResponseData.getFieldValue(FIELD_DRD_SERIES_NO);
            String responseDataAA = dataResponseData.getFieldValue(FIELD_DRD_AA);
            if (!ResponseDataHelper.filterResponseDataOrSpikesforDIALOGEntry(responseDataSeriesNo, responseDataAA,
                                                                             (seriesId != null) ? seriesId.getSeriesNumber() : "", aa)) {
                continue;
            }

            // Falls gültige Rückmeldedaten vorliegen, diese hinzufügen
            String ident = dataResponseData.getFieldValue(FIELD_DRD_IDENT).toUpperCase();
            String eldasWMI = dataResponseData.getFieldValue(FIELD_DRD_WHC);
            String eldasType = dataResponseData.getFieldValue(FIELD_DRD_TYPE);
            iPartsImportDataOrigin source = dataResponseData.getSource();

            // Ausreißer bestimmen
            Set<iPartsDataResponseSpike> dataResponseSpikes = responseSpikes.getResponseSpikes(pem, ident);
            Set<String> spikeIdents = null;
            if (dataResponseSpikes != null) {
                spikeIdents = new TreeSet<>();
                for (iPartsDataResponseSpike dataResponseSpike : dataResponseSpikes) {
                    if (!dataResponseSpike.isReleased()) {
                        continue;
                    }

                    if (!dataResponseSpike.getFieldValue(FIELD_DRS_FACTORY).equals(factory)) {
                        continue;
                    }

                    // Prüfungen für DIALOG (bei ELDAS sind seriesId und aa nicht gesetzt)
                    String responseSpikeSeriesNo = dataResponseSpike.getFieldValue(FIELD_DRS_SERIES_NO);
                    String responseSpikeAA = dataResponseSpike.getFieldValue(FIELD_DRS_AA);
                    if (!ResponseDataHelper.filterResponseDataOrSpikesforDIALOGEntry(responseSpikeSeriesNo, responseSpikeAA,
                                                                                     (seriesId != null) ? seriesId.getSeriesNumber() : "", aa)) {
                        continue;
                    }

                    String spikeIdent = dataResponseSpike.getAsId().getSpikeIdent();
                    if (!spikeIdent.isEmpty()) {
                        spikeIdents.add(spikeIdent);
                    }
                }
            }

            String correctedModelNumber = dataResponseData.getCorrectedModelNumber();
            String steering = dataResponseData.getFieldValue(FIELD_DRD_STEERING);
            if (isFrom) {
                dataForFactory.addIdentFrom(ident, correctedModelNumber, steering, eldasWMI, eldasType, source, spikeIdents);
            } else {
                dataForFactory.addIdentTo(ident, correctedModelNumber, steering, eldasWMI, eldasType, source, spikeIdents);
            }
        }
    }

    public void loadAllColorTableForRetailForPartList(DBDataObjectList<EtkDataPartListEntry> partlist) {
        loadAllColorTableForRetailForPartList(partlist, "");
    }

    /**
     * Lädt alle Farbvariantentabellen und Farbvarianteninhalte für den Retail (Filterung und Webservices) für die gesamte
     * Stückliste. Die Werkseinsatzdaten zur Stückliste müssen vorher bereits geladen worden sein, damit der Retailfilter
     * funktioniert.
     *
     * @param partlist
     * @param partNo   Optionale Teilenummer zum Einschränken des Ladevorgangs
     */
    public void loadAllColorTableForRetailForPartList(DBDataObjectList<EtkDataPartListEntry> partlist, String partNo) {
        // Wenn das Flag zur Anzeige der (Farb-)varianten nicht gesetzt ist, gar nicht erst weitermachen.
        iPartsDataModule moduleMetaData = getModuleMetaData();
        if (!moduleMetaData.isVariantsVisible()) {
            return;
        }

        boolean showColorTablefootnotes = moduleMetaData.isShowColorTablefootnotes();
        final boolean isDIALOGPartList = getDocumentationType().isPKWDocumentationType();

        // Map von laufender Nummer der Stückliste (nur für gefundene Farbvariantentabellen) auf Baureihe
        final Map<String, iPartsSeriesId> seriesIdMap = new HashMap<>();

        // Set aller Farbvariantentabellen-IDs
        final Set<String> allColorTableIds = new LinkedHashSet<>();

        final Map<String, iPartsColorTable> allColorTableMap;
        final Set<String> partlistEntriesWithColoredPart = new HashSet<>();
        final Set<String> partlistEntriesWithUnfilteredColorTables = new HashSet<>();
        // Unterscheidung, ob alle Farbtabellen (samt Werkseinsatzdaten) der ganzen Stückliste oder nur die Farbtabellen
        // zur übergebenen Teilenummer berechnet werden sollen
        if (StrUtils.isValid(partNo)) {
            allColorTableMap = getPartListEntryToColorTableMapForPartNo(partlist, partNo, isDIALOGPartList, seriesIdMap,
                                                                        allColorTableIds, partlistEntriesWithColoredPart,
                                                                        partlistEntriesWithUnfilteredColorTables);
        } else {
            // Map k_lfdnr -> Farbvariantentabellen (inkl. Werkseinsatz- und Rückmeldedaten) bestimmen sowie Set aller Farbtvariantentabellen-IDs
            allColorTableMap = getPartListEntryToColorTableMap(isDIALOGPartList, seriesIdMap, allColorTableIds, partlistEntriesWithColoredPart, partlistEntriesWithUnfilteredColorTables);
        }

        // Falls es überhaupt keine relevanten Farbvariantentabellen-IDs gibt, sofort null für die Farbvariantentabellen bei allen
        // Stücklisteneinträge setzen
        boolean isELDASPartList = getDocumentationType().isTruckDocumentationType();
        boolean hasNoValidColorTables = allColorTableIds.isEmpty();
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                String kLfdnr = iPartsPartListEntry.getAsId().getKLfdnr();
                if (hasNoValidColorTables) {
                    // Auch null setzen, damit in dem Stücklisteneintrag die Farbvariantentabellen als geladen markiert werden
                    iPartsPartListEntry.setColorTableForRetailWithoutFilter(null);
                }

                iPartsPartListEntry.setHasColorTablesUnfiltered(partlistEntriesWithUnfilteredColorTables.contains(kLfdnr));

                // Abhängig vom Stücklistentyp wird hier bestimmt, ob die Stücklistenpositionen Farbteile enthalten. Bei DIALOG,
                // wenn QFT Farbtabellen mit gleicher Baureihe zur Teilenummer vorhanden sind (siehe getPartListEntryToColorTableMap()).
                // Bei ELDAS, wenn es Farbtabellen zum Material gibt oder Farbfußnoten vorhanden sind (abhängig von showColorTablefootnotes).
                if (isDIALOGPartList || !showColorTablefootnotes) {
                    iPartsPartListEntry.setColoredPart(partlistEntriesWithColoredPart.contains(kLfdnr));
                }
                if (isELDASPartList && showColorTablefootnotes) {
                    iPartsPartListEntry.setColoredPart(iPartsPartListEntry.hasColorTableFootNotes());
                }
            }
        }

        if (hasNoValidColorTables) {
            return;
        }

        // Farbvarianteninhalte für alle relevanten Farbvariantentabellen unabhängig vom Stücklisteneintrag bestimmen
        // (inkl. Werkeinsatzdaten + Rückmeldedaten)
        final iPartsColorTable genericColorTableForRetail = createGenericColorTableForRetail(isDIALOGPartList, allColorTableIds);

        // Farbvariantentabellen und gefilterte Farbvarianteninhalte den Stücklisteneinträgen zuweisen
        assignColorTablesToPartListEntries(isDIALOGPartList, partlist, allColorTableMap, genericColorTableForRetail, seriesIdMap);
    }

    /**
     * Zuordnung Stücklisteintrag-ID zu Farbvariantentabellen bestimmen.
     * Außerdem werden die Farbvariantentabellen IDs bestimmt.
     *
     * @param isDIALOGPartList
     * @param seriesIdMap                    Zuordnung k_lfdnr -> Baureihe; OUTPUT
     * @param allColorTableIds               Farbvariantentabellen IDs; OUTPUT
     * @param partlistEntriesWithColoredPart Set, das mit allen laufenden Nummern von Stücklisteneinträgen befüllt wird,
     *                                       die ein farbiges Teil haben
     * @return Map von laufender Nummer der Stückliste auf {@link iPartsColorTable}
     */
    public Map<String, iPartsColorTable> getPartListEntryToColorTableMap(final boolean isDIALOGPartList,
                                                                         final Map<String, iPartsSeriesId> seriesIdMap,
                                                                         final Set<String> allColorTableIds,
                                                                         final Set<String> partlistEntriesWithColoredPart,
                                                                         final Set<String> partlistEntriesWithUnfilteredColorTables) {
        // Bei aktivem ChangeSet mit prinzipiell erlaubter getRecords()-Simulation keinen Dreifach-Join sondern einen Einzel-Join
        // und Nachladen von Daten verwenden, damit die Joins mittels getRecords()-Simulation anstatt mit einer forcierten
        // Pseudo-Transaktion laufen können
        boolean useTripleJoin = EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS || !isRevisionChangeSetActive();
        if (!useTripleJoin) {
            EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
            if (revisionsHelper != null) {
                // Der dreifache Join kann trotz aktivem ChangeSet auch dann verwendet werden, wenn es keine veränderten
                // oder neuen SerializedDBDataObjects in den Tabellen DA_COLORTABLE_PART, DA_COLORTABLE_DATA und DA_COLORTABLE_FACTORY
                // gibt (weil dafür keine where-Bedingungen vorhanden sind) sowie keine veränderten oder neuen SerializedDBDataObjects
                // in der Tabelle KATALOG für die aktuelle AssemblyId
                useTripleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_COLORTABLE_PART, null,
                                                                                       RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                if (useTripleJoin) {
                    useTripleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_COLORTABLE_DATA, null,
                                                                                           RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                    if (useTripleJoin) {
                        useTripleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_COLORTABLE_FACTORY, null,
                                                                                               RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                        if (useTripleJoin) {
                            String[] assemblyPKValues = { getAsId().getKVari(), getAsId().getKVer() };
                            useTripleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_KATALOG, assemblyPKValues,
                                                                                                   RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                        }
                    }
                }
            }
        }

        // Rückmeldedaten-Cache; wird verwendet, um relevante Rückmeldedaten den Werkseinsatzdaten zuzuordnen
        final iPartsResponseData responseData = iPartsResponseData.getInstance(getEtkProject());
        // Ausreißer-Cache; wird verwendet, um relevante Ausreißer den Rückmeldedaten zuzuordnen
        final iPartsResponseSpikes responseSpikes = iPartsResponseSpikes.getInstance(getEtkProject());

        // Map von laufender Nummer der Stückliste auf iPartsColorTableForRetail
        final Map<String, iPartsColorTable> allColorTableMap = new HashMap<>();

        // Callback für die Farbvariantentabellen am Teil
        EtkDataObjectList.FoundAttributesCallback foundColorTableToPartCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes dataAttributes) {
                String kLfdnr = dataAttributes.getFieldValue(FIELD_K_LFDNR);

                String tableId = dataAttributes.getFieldValue(FIELD_DCTP_TABLE_ID);
                String partNoFromPartListEntry = dataAttributes.getFieldValue(FIELD_K_MATNR);

                iPartsSeriesId seriesId = null;
                if (isDIALOGPartList) {
                    seriesId = getSeriesFromPartListEntryAttributes(dataAttributes, kLfdnr, seriesIdMap);
                }

                // Checken, ob das Material ein Farbteil ist: Tabelle und Materialnummer müssen vorhanden sein
                if (StrUtils.isValid(tableId, partNoFromPartListEntry)) {
                    if (isDIALOGPartList) {
                        String validSeriesForColorTable = dataAttributes.getFieldValue(FIELD_DCTD_VALID_SERIES);

                        // Check, ob Baureihen stimmen
                        if (StrUtils.isValid(validSeriesForColorTable) && seriesId.getSeriesNumber().equals(validSeriesForColorTable)) {
                            partlistEntriesWithColoredPart.add(kLfdnr);
                        }
                    } else { // ELDAS
                        partlistEntriesWithColoredPart.add(kLfdnr);
                    }
                }

                // Hier kommt auch ein leerer Status an!
                String status = dataAttributes.getFieldValue(FIELD_DCCF_STATUS);
                if (StrUtils.isValid(status) && !status.equals(iPartsDataReleaseState.RELEASED.getDbValue())) {
                    return false;
                }

                String source = dataAttributes.getFieldValue(FIELD_DCTP_SOURCE);

                if (!source.equals(iPartsImportDataOrigin.PRIMUS.getOrigin())) {
                    partlistEntriesWithUnfilteredColorTables.add(kLfdnr);
                }

                // Bei DIALOG müssen einige Filterungen durchgeführt werden
                if (!checkDialogPartListForColorTableData(isDIALOGPartList, source, dataAttributes, seriesId)) {
                    return false;
                }

                String aa = dataAttributes.getFieldValue(FIELD_K_AA);
                calcColorTableFactoryData(allColorTableMap, allColorTableIds, kLfdnr, dataAttributes, aa, seriesId, isDIALOGPartList,
                                          responseData, responseSpikes);
                return false;
            }
        };

        // Diverse (Outer) Joins mit der KATALOG-Tabelle für alle Stücklisteneinträge des Moduls auf die Tabellen für
        // die Farbvarianten am Teil inkl. deren Werkseinsatzdaten (DA_COLOR*)
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VER, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_LFDNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_TYPE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_CONTEXT, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_GUID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_AA, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_MATNR, false, false));

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_TABLE_ID, false, false)); // Farbvariantentabelle
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_POS, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATA, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATB, false, false)); // Datum bis für den Retail-Filter (historische Daten entfernen)
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_ETKZ, false, false)); // ETKZ für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SOURCE, false, false)); // Quelle für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_STATUS, false, false)); // Freigabestatus der Variantentabelle
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_EVAL_PEM_FROM, false, false)); // Flag "PEM-ab auswerten" der Variantentabelle
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_EVAL_PEM_TO, false, false)); // Flag "PEM-bis auswerten" der Variantentabelle

        EtkDisplayFields selectFieldsForColorTableData = new EtkDisplayFields();
        selectFieldsForColorTableData.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_VALID_SERIES, false, false)); // gültige Baureihe für den Retail-Filter
        selectFieldsForColorTableData.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_FIKZ, false, false));

        EtkDisplayFields selectFieldsForColorTableFactory = new EtkDisplayFields();
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_POS, false, false)); // Werk für den Retail-Filter
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_SDATA, false, false)); // Werk für den Retail-Filter
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_FACTORY, false, false)); // Werk für den Retail-Filter
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_ADAT, false, false)); // ADAT für den Retail-Filter (historische Werkseinsatzdaten entfernen)
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_DATA_ID, false, false)); // Kenner für Konstruktion oder AS für Farbvariantentabelle am Teil oder Farbvarianteninhalt
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMA, false, false)); // PEM ab für den Retail-Filter
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTA, false, false)); // PEM Termin ab für den Retail-Filter
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMB, false, false)); // PEM bis für den Retail-Filter
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTB, false, false)); // PEM Termin bis für den Retail-Filter
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCA, false, false)); // Steuercode ab für den Retail-Filter
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCB, false, false)); // Steuercode bis für den Retail-Filter
        selectFieldsForColorTableFactory.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STATUS, false, false)); // Freigabestatus

        EtkDataObjectList.JoinData katalogJoinData = new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                                    new String[]{ FIELD_DCTP_PART },
                                                                                    new String[]{ FIELD_K_MATNR },
                                                                                    false, false);

        EtkProject etkProject = getEtkProject();
        final List<DBDataObjectAttributes> colorTablePartAttributesList = new DwList<>();
        final Map<String, DBDataObjectAttributes> colorTableDataAttributesMap = new HashMap<>();
        final Map<iPartsColorTableToPartId, List<DBDataObjectAttributes>> colorTableFactoryAttributesMap = new HashMap();
        EtkDataObjectList.JoinData[] joinDatas;
        EtkDataObjectList.FoundAttributesCallback foundColorTableSimpleJoinCallback = null;
        if (useTripleJoin) {
            selectFields.addFelder(selectFieldsForColorTableData);
            selectFields.addFelder(selectFieldsForColorTableFactory);
            joinDatas = new EtkDataObjectList.JoinData[]{ katalogJoinData,
                                                          new EtkDataObjectList.JoinData(TABLE_DA_COLORTABLE_DATA,
                                                                                         new String[]{ FIELD_DCTP_TABLE_ID },
                                                                                         new String[]{ FIELD_DCTD_TABLE_ID },
                                                                                         true, false),
                                                          new EtkDataObjectList.JoinData(TABLE_DA_COLORTABLE_FACTORY, // Werkseinsatzdaten für die Farbvariantentabellen am Teil
                                                                                         new String[]{ FIELD_DCTP_TABLE_ID,
                                                                                                       FIELD_DCTP_POS,
                                                                                                       FIELD_DCTP_SDATA },
                                                                                         new String[]{ FIELD_DCCF_TABLE_ID,
                                                                                                       FIELD_DCCF_POS,
                                                                                                       FIELD_DCCF_SDATA },
                                                                                         true, false) };

        } else {
            joinDatas = new EtkDataObjectList.JoinData[]{ katalogJoinData };

            // Zunächst die Farbvariantentabellen zum Teil inkl. Daten aus der Tabelle KATALOG ermitteln
            foundColorTableSimpleJoinCallback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes dataAttributes) {
                    colorTablePartAttributesList.add(dataAttributes);
                    String colorTableId = dataAttributes.getFieldValue(FIELD_DCTP_TABLE_ID);
                    colorTableDataAttributesMap.put(colorTableId, null);
                    colorTableFactoryAttributesMap.put(new iPartsColorTableToPartId(colorTableId,
                                                                                    dataAttributes.getFieldValue(FIELD_DCTP_POS),
                                                                                    dataAttributes.getFieldValue(FIELD_DCTP_SDATA)),
                                                       null);
                    return false;
                }
            };
        }

        iPartsDataColorTableToPartList colorTableToPartList = new iPartsDataColorTableToPartList();
        colorTableToPartList.searchSortAndFillWithJoin(etkProject, etkProject.getDBLanguage(), selectFields,
                                                       new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                                     TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER),
                                                                     TableAndFieldName.make(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_STATUS) }, // nur freigegebene laden
                                                       new String[]{ getAsId().getKVari(), getAsId().getKVer(), iPartsDataReleaseState.RELEASED.getDbValue() }, false,
                                                       null, // Sortierung funktioniert mit einem reinen Callback nicht
                                                       false, useTripleJoin ? foundColorTableToPartCallback : foundColorTableSimpleJoinCallback,
                                                       joinDatas);

        if (!useTripleJoin) {
            // Die Daten aus DA_COLORTABLE_DATA für jede Farbvariantentabellen-ID laden
            String[] fieldNamesForColorTableData = selectFieldsForColorTableData.getAsFieldNamesArray();
            loadColorTableDataAttributes(colorTableDataAttributesMap, fieldNamesForColorTableData);

            // Die Daten aus DA_COLORTABLE_FACTORY für jede iPartsColorTableToPartId laden
            String[] fieldNamesForColorTableFactory = selectFieldsForColorTableFactory.getAsFieldNamesArray();
            for (iPartsColorTableToPartId colorTableToPartId : colorTableFactoryAttributesMap.keySet().toArray(new iPartsColorTableToPartId[colorTableFactoryAttributesMap.size()])) {
                DBDataObjectAttributesList colorTableFactoryAttributesList = etkProject.getEtkDbs().getAttributesList(TABLE_DA_COLORTABLE_FACTORY,
                                                                                                                      fieldNamesForColorTableFactory,
                                                                                                                      new String[]{ FIELD_DCCF_TABLE_ID,
                                                                                                                                    FIELD_DCCF_POS,
                                                                                                                                    FIELD_DCCF_SDATA },
                                                                                                                      colorTableToPartId.toStringArrayWithoutType());
                if (!colorTableFactoryAttributesList.isEmpty()) {
                    colorTableFactoryAttributesMap.put(colorTableToPartId, colorTableFactoryAttributesList);
                }
            }

            // Alle zusammengeführten Daten über den foundColorTableToPartCallback auswerten
            for (DBDataObjectAttributes mergedAttributes : colorTablePartAttributesList) {
                String colorTableId = mergedAttributes.getFieldValue(FIELD_DCTP_TABLE_ID);
                iPartsColorTableToPartId colorTableToPartId = new iPartsColorTableToPartId(colorTableId,
                                                                                           mergedAttributes.getFieldValue(FIELD_DCTP_POS),
                                                                                           mergedAttributes.getFieldValue(FIELD_DCTP_SDATA));

                // Daten aus DA_COLORTABLE_DATA wie bei einem LeftOuter-Join hinzufügen
                DBDataObjectAttributes colorTableDataAttributes = colorTableDataAttributesMap.get(colorTableId);
                if (colorTableDataAttributes != null) {
                    mergedAttributes.addFields(colorTableDataAttributes, DBActionOrigin.FROM_DB);
                } else {
                    // Keine Daten gefunden -> leere Felder hinzufügen
                    mergedAttributes.addEmptyStringFields(fieldNamesForColorTableData, DBActionOrigin.FROM_DB);
                }

                // Daten aus DA_COLORTABLE_FACTORY wie bei einem LeftOuter-Join hinzufügen
                List<DBDataObjectAttributes> colorTableFactoryAttributesList = colorTableFactoryAttributesMap.get(colorTableToPartId);
                if (colorTableFactoryAttributesList != null) {
                    // Für jeden Datensatz aus DA_COLORTABLE_FACTORY den Callback separat aufrufen
                    for (DBDataObjectAttributes colorTableFactoryAttributes : colorTableFactoryAttributesList) {
                        mergedAttributes.addFields(colorTableFactoryAttributes, DBActionOrigin.FROM_DB);

                        // Zusammengeführte Daten an den foundColorTableToPartCallback übergeben
                        foundColorTableToPartCallback.foundAttributes(mergedAttributes);
                    }
                } else {
                    // Keine Daten gefunden -> leere Felder hinzufügen
                    mergedAttributes.addEmptyStringFields(fieldNamesForColorTableFactory, DBActionOrigin.FROM_DB);

                    // Zusammengeführte Daten an den foundColorTableToPartCallback übergeben
                    foundColorTableToPartCallback.foundAttributes(mergedAttributes);
                }
            }
        }

        return allColorTableMap;
    }


    /**
     * Berechnet für die übergebenen Stücklistenpositionen eine Map mit klfdNr auf Farbtabelle. Jedes Farbtabellen-Objekt
     * enthält die Werkseinsatzdaten zu Beziehung Farbtabelle zu Teil (WX10 und VX10).
     *
     * @param partList
     * @param partNo
     * @param isDIALOGPartList
     * @param seriesIdMap
     * @param allColorTableIds
     * @param partlistEntriesWithColoredPart Set, das mit allen laufenden Nummern von Stücklisteneinträgen befüllt wird,
     *                                       die ein farbiges Teil haben
     * @return
     */
    public Map<String, iPartsColorTable> getPartListEntryToColorTableMapForPartNo(final DBDataObjectList<EtkDataPartListEntry> partList,
                                                                                  final String partNo, final boolean isDIALOGPartList,
                                                                                  final Map<String, iPartsSeriesId> seriesIdMap,
                                                                                  final Set<String> allColorTableIds,
                                                                                  final Set<String> partlistEntriesWithColoredPart,
                                                                                  final Set<String> partlistEntriesWithUnfilteredColorTables) {
        // Bei aktivem ChangeSet mit prinzipiell erlaubter getRecords()-Simulation keinen Zweifach-Join sondern einen Einzel-Join
        // und Nachladen von Daten verwenden, damit die Joins mittels getRecords()-Simulation anstatt mit einer forcierten
        // Pseudo-Transaktion laufen können
        boolean useDoubleJoin = EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS || !isRevisionChangeSetActive();
        if (!useDoubleJoin) {
            EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
            if (revisionsHelper != null) {
                // Der dreifache Join kann trotz aktivem ChangeSet auch dann verwendet werden, wenn es keine veränderten oder
                // neuen SerializedDBDataObjects in den Tabellen DA_COLORTABLE_PART, DA_COLORTABLE_DATA und DA_COLORTABLE_FACTORY
                // gibt (weil dafür keine where-Bedingungen vorhanden sind)
                useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_COLORTABLE_PART, null,
                                                                                       RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                if (useDoubleJoin) {
                    useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_COLORTABLE_DATA, null,
                                                                                           RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                    if (useDoubleJoin) {
                        useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_COLORTABLE_FACTORY, null,
                                                                                               RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                    }
                }
            }
        }

        // Rückmeldedaten-Cache; wird verwendet, um relevante Rückmeldedaten den Werkseinsatzdaten zuzuordnen
        final iPartsResponseData responseData = iPartsResponseData.getInstance(getEtkProject());
        // Ausreißer-Cache; wird verwendet, um relevante Ausreißer den Rückmeldedaten zuzuordnen
        final iPartsResponseSpikes responseSpikes = iPartsResponseSpikes.getInstance(getEtkProject());

        // Map von laufender Nummer der Stückliste auf iPartsColorTableForRetail
        final Map<String, iPartsColorTable> allColorTableMap = new HashMap<>();

        // Callback für die Farbvariantentabellen am Teil (ähnlich zu getPartListEntryToColorTableMap() aber mit anderen Optimierungen)
        EtkDataObjectList.FoundAttributesCallback foundColorTableToPartCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes dataAttributes) {
                // Hier kommt auch ein leerer Status an!
                String status = dataAttributes.getFieldValue(FIELD_DCCF_STATUS);
                if (StrUtils.isValid(status) && !status.equals(iPartsDataReleaseState.RELEASED.getDbValue())) {
                    return false;
                }

                String source = dataAttributes.getFieldValue(FIELD_DCTP_SOURCE);
                String tableId = dataAttributes.getFieldValue(FIELD_DCTP_TABLE_ID);

                for (EtkDataPartListEntry partListEntry : partList) {
                    String kLfdnr = partListEntry.getFieldValue(FIELD_K_LFDNR);
                    iPartsSeriesId seriesId = getSeriesFromPartListEntryAttributes(partListEntry.getAttributes(), kLfdnr, seriesIdMap);
                    String validSeriesForColorTable = dataAttributes.getFieldValue(FIELD_DCTD_VALID_SERIES);

                    // Checken, ob das Material ein Farbteil ist
                    if (partListEntry instanceof iPartsDataPartListEntry) {
                        String partNoFromPartListEntry = partListEntry.getPart().getAsId().getMatNr();

                        // Tabelle und Materialnummer müssen vorhanden sein und die Teilenummern müssen übereinstimmen
                        if (StrUtils.isValid(tableId, partNoFromPartListEntry) && partNoFromPartListEntry.equals(partNo)) {
                            if (isDIALOGPartList) {
                                // Check, ob Baureihen stimmen
                                if (StrUtils.isValid(validSeriesForColorTable) && seriesId.getSeriesNumber().equals(validSeriesForColorTable)) {
                                    partlistEntriesWithColoredPart.add(kLfdnr);
                                }
                            } else { // ELDAS
                                partlistEntriesWithColoredPart.add(kLfdnr);
                            }
                        }
                    }

                    if (!source.equals(iPartsImportDataOrigin.PRIMUS.getOrigin())) {
                        partlistEntriesWithUnfilteredColorTables.add(kLfdnr);
                    }

                    // Bei DIALOG müssen einige Filterungen durchgeführt werden
                    if (!checkDialogPartListForColorTableData(isDIALOGPartList, source, dataAttributes, seriesId)) {
                        continue;
                    }

                    String aa = partListEntry.getFieldValue(FIELD_K_AA);
                    calcColorTableFactoryData(allColorTableMap, allColorTableIds, kLfdnr, dataAttributes, aa, seriesId, isDIALOGPartList,
                                              responseData, responseSpikes);

                }

                return false;
            }
        };

        // Diverse (Outer) Joins für die Teilenummer auf die Tabellen für die Farbvarianten am Teil inkl. deren Werkseinsatzdaten (DA_COLOR*)
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_TABLE_ID, false, false)); // Farbvariantentabelle
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_POS, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATA, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATB, false, false)); // Datum bis für den Retail-Filter (historische Daten entfernen)
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_ETKZ, false, false)); // ETKZ für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SOURCE, false, false)); // Quelle für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_STATUS, false, false)); // Freigabestatus der Variantentabelle
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_EVAL_PEM_FROM, false, false)); // PEM ab auswerten
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_EVAL_PEM_TO, false, false)); // PEM bis auswerten

        EtkDisplayFields selectFieldsForColorTableData = new EtkDisplayFields();
        selectFieldsForColorTableData.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_VALID_SERIES, false, false)); // gültige Baureihe für den Retail-Filter
        selectFieldsForColorTableData.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_FIKZ, false, false));

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_FACTORY, false, false)); // Werk für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_ADAT, false, false)); // ADAT für den Retail-Filter (historische Werkseinsatzdaten entfernen)
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_DATA_ID, false, false)); // Kenner für Konstruktion oder AS für Farbvariantentabelle am Teil oder Farbvarianteninhalt
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMA, false, false)); // PEM ab für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTA, false, false)); // PEM Termin ab für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMB, false, false)); // PEM bis für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTB, false, false)); // PEM Termin bis für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCA, false, false)); // Steuercode ab für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCB, false, false)); // Steuercode bis für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STATUS, false, false)); // Freigabestatus

        EtkDataObjectList.JoinData colorTableFactoryJoinData = new EtkDataObjectList.JoinData(TABLE_DA_COLORTABLE_FACTORY, // Werkseinsatzdaten für die Farbvariantentabellen am Teil
                                                                                              new String[]{ FIELD_DCTP_TABLE_ID,
                                                                                                            FIELD_DCTP_POS,
                                                                                                            FIELD_DCTP_SDATA },
                                                                                              new String[]{ FIELD_DCCF_TABLE_ID,
                                                                                                            FIELD_DCCF_POS,
                                                                                                            FIELD_DCCF_SDATA },
                                                                                              true, false);

        final List<DBDataObjectAttributes> colorTablePartAttributesList = new DwList<>();
        final Map<String, DBDataObjectAttributes> colorTableDataAttributesMap = new HashMap<>();
        EtkDataObjectList.JoinData[] joinDatas;
        EtkDataObjectList.FoundAttributesCallback foundColorTableSimpleJoinCallback = null;
        if (useDoubleJoin) {
            selectFields.addFelder(selectFieldsForColorTableData);
            joinDatas = new EtkDataObjectList.JoinData[]{ new EtkDataObjectList.JoinData(TABLE_DA_COLORTABLE_DATA,
                                                                                         new String[]{ FIELD_DCTP_TABLE_ID },
                                                                                         new String[]{ FIELD_DCTD_TABLE_ID },
                                                                                         true, false),
                                                          colorTableFactoryJoinData };
        } else {
            joinDatas = new EtkDataObjectList.JoinData[]{ colorTableFactoryJoinData };

            // Zunächst die Farbvariantentabellen und Daten aus DA_COLORTABLE_FACTORY zur Teilenummer ermitteln
            foundColorTableSimpleJoinCallback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes dataAttributes) {
                    colorTablePartAttributesList.add(dataAttributes);
                    String colorTableId = dataAttributes.getFieldValue(FIELD_DCTP_TABLE_ID);
                    colorTableDataAttributesMap.put(colorTableId, null);
                    return false;
                }
            };
        }

        iPartsDataColorTableToPartList colorTableToPartList = new iPartsDataColorTableToPartList();
        colorTableToPartList.searchSortAndFillWithJoin(getEtkProject(), getEtkProject().getDBLanguage(), selectFields,
                                                       new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_PART),
                                                                     TableAndFieldName.make(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_STATUS) }, // nur freigegebene laden
                                                       new String[]{ partNo, iPartsDataReleaseState.RELEASED.getDbValue() }, false,
                                                       null, // Sortierung funktioniert mit einem reinen Callback nicht
                                                       false, useDoubleJoin ? foundColorTableToPartCallback : foundColorTableSimpleJoinCallback,
                                                       joinDatas);

        if (!useDoubleJoin) {
            // Die Daten aus DA_COLORTABLE_DATA für jede Farbvariantentabellen-ID laden
            String[] fieldNamesForColorTableData = selectFieldsForColorTableData.getAsFieldNamesArray();
            loadColorTableDataAttributes(colorTableDataAttributesMap, fieldNamesForColorTableData);

            // Alle Daten über den foundColorTableToPartCallback zusammen auswerten
            for (DBDataObjectAttributes mergedAttributes : colorTablePartAttributesList) {
                String colorTableId = mergedAttributes.getFieldValue(FIELD_DCTP_TABLE_ID);

                // Daten aus DA_COLORTABLE_DATA wie bei einem LeftOuter-Join hinzufügen
                DBDataObjectAttributes colorTableDataAttributes = colorTableDataAttributesMap.get(colorTableId);
                if (colorTableDataAttributes != null) {
                    mergedAttributes.addFields(colorTableDataAttributes, DBActionOrigin.FROM_DB);
                } else {
                    // Keine Daten gefunden -> leere Felder hinzufügen
                    mergedAttributes.addEmptyStringFields(fieldNamesForColorTableData, DBActionOrigin.FROM_DB);
                }

                // Zusammengeführte Daten an den foundColorTableToPartCallback übergeben
                foundColorTableToPartCallback.foundAttributes(mergedAttributes);
            }
        }

        return allColorTableMap;
    }

    private void loadColorTableDataAttributes(Map<String, DBDataObjectAttributes> colorTableDataAttributesMap, String[] fieldNamesForColorTableData) {
        EtkProject etkProject = getEtkProject();
        for (String colorTableId : ArrayUtil.toStringArray(colorTableDataAttributesMap.keySet())) {
            DBDataObjectAttributes colorTableDataAttributes = etkProject.getEtkDbs().getAttributes(TABLE_DA_COLORTABLE_DATA,
                                                                                                   fieldNamesForColorTableData,
                                                                                                   new String[]{ FIELD_DCTD_TABLE_ID },
                                                                                                   new String[]{ colorTableId });
            if (colorTableDataAttributes != null) {
                colorTableDataAttributesMap.put(colorTableId, colorTableDataAttributes);
            }
        }
    }

    /**
     * Lädt die kombinierten Texte für alle Stücklisteneinträge des Moduls für alle Sprachen, damit das Löschen und Kopieren
     * von Stücklisteneinträgen schneller durchgeführt werden kann, weil dann nicht einzeln pro Stücklisteneintrag aie
     * kombinierten Texte für alle Sprachen mit einem teuren simulierten Join geladen werden müssen.
     * Nach dem Kopieren bzw. Löschen muss {@link #clearAllDataCombTextListsForPartList()} aufgerufen werden, wenn dieses
     * Modul bzw. die Stücklisteneinträge in weiteren Edit-Aktionen ohne Nebenwirkungen verwendet werden können.
     *
     * @see #clearAllDataCombTextListsForPartList()
     */
    public void loadAllDataCombTextListsForPartList() {
        loadAllDataCombTextListsForPartList(getPartListUnfiltered(null).getAsList(), true);
    }

    /**
     * Lädt die kombinierten Texte für alle übergebenen Stücklisteneinträge dieses Moduls für die aktuelle DB-Sprache oder
     * für alle Sprachen.
     *
     * @param partlist
     * @param loadAllLanguages
     */
    public void loadAllDataCombTextListsForPartList(List<EtkDataPartListEntry> partlist, boolean loadAllLanguages) {
        // Kombinierte Texte für alle Stücklisteneinträge des Moduls für alle Sprachen laden
        iPartsDataCombTextList combTextList = loadAllLanguages ? iPartsDataCombTextList.loadForModuleAndAllLanguages(getEtkProject(), getAsId())
                                                               : iPartsDataCombTextList.loadForModule(getAsId(), getEtkProject());
        Map<String, iPartsDataCombTextList> lfdNrToCombTextListMap = new HashMap<>();
        for (iPartsDataCombText dataCombText : combTextList) {
            iPartsDataCombTextList combTextListForPLE = lfdNrToCombTextListMap.computeIfAbsent(dataCombText.getAsId().getPartListEntryId().getKLfdnr(),
                                                                                               lfdNr -> new iPartsDataCombTextList());
            combTextListForPLE.add(dataCombText, DBActionOrigin.FROM_DB);
        }

        // Kombinierte Texte an jedem Stücklisteneintrag setzen
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataCombTextList combTextListForPLE = lfdNrToCombTextListMap.get(partListEntry.getAsId().getKLfdnr());
                if (combTextListForPLE == null) {
                    combTextListForPLE = new iPartsDataCombTextList();
                }
                ((iPartsDataPartListEntry)partListEntry).setDataCombTextList(combTextListForPLE);
            }
        }
    }

    /**
     * Führt für die Berechnung der Werkseinsatzdaten einer Beziehung "Farbtabelle zu Teil" mehrere DIALOG-spezifische Prüfungen
     * durch.
     *
     * @param isDIALOGPartList
     * @param source
     * @param dataAttributes
     * @param seriesId
     * @return
     */
    public boolean checkDialogPartListForColorTableData(boolean isDIALOGPartList, String source, DBDataObjectAttributes dataAttributes,
                                                        iPartsSeriesId seriesId) {
        // Bei DIALOG müssen einige Filterungen durchgeführt werden
        if (isDIALOGPartList) {
            String factoryPart = dataAttributes.getFieldValue(FIELD_DCCF_FACTORY);
            String pemFromPart = dataAttributes.getFieldValue(FIELD_DCCF_PEMA);
            String pemToPart = dataAttributes.getFieldValue(FIELD_DCCF_PEMB);
            long dateFromPart = iPartsFactoryData.getFactoryDateFromDateString(dataAttributes.getFieldValue(FIELD_DCCF_PEMTA),
                                                                               TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTA));
            long dateToPart = iPartsFactoryData.getFactoryDateFromDateString(dataAttributes.getFieldValue(FIELD_DCCF_PEMTB),
                                                                             TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTB));
            String dataIdPart = dataAttributes.getFieldValue(FIELD_DCCF_DATA_ID);
            if (!source.isEmpty() &&
                !source.equals(iPartsImportDataOrigin.MAD.getOrigin()) &&
                !source.equals(iPartsImportDataOrigin.DIALOG.getOrigin()) &&
                !source.equals(iPartsImportDataOrigin.IPARTS.getOrigin())) {
                return false; // Quelle ist ungültig für DIALOG
            }

            String colorTablePartEtkz = dataAttributes.getFieldValue(FIELD_DCTP_ETKZ);
            if (!colorTablePartEtkz.isEmpty()) {
                return false; // ETKZ muss jeweils leer sein
            }

            // Werkseinsatzdaten für Farbvariantentabellen am Teil und Farbvarianteninhalte müssen vorhanden sein
            // (entspricht quasi einem Inner Join - wg. ELDAS brauchen wir aber den Outer Join)
            if (!StrUtils.isValid(dataIdPart)) {
                return false; // mindestens an einer Stelle fehlen Werkseinsatzdaten
            }

            if (seriesId != null) {
                if (seriesId.getSeriesNumber().isEmpty()) {
                    return false; // Stücklisteneintrag hat keine Baureihe -> ungültig
                }
                String validSeriesForColorTable = dataAttributes.getFieldValue(FIELD_DCTD_VALID_SERIES);
                if (!seriesId.getSeriesNumber().equals(validSeriesForColorTable)) {
                    return false; // Baureihe ist ungültig
                }
            }

            // Prüfung auf PEM-Termin-AB <= PEM-TERMIN-BIS (nur sinnvoll falls es eine PEM ab und PEM bis gibt)
            if (StrUtils.isValid(pemFromPart, pemToPart)) {
                if ((dateToPart == 0) && (dateFromPart == 0)) {
                    return false;
                }
                long realDateTo = (dateToPart == 0) ? Long.MAX_VALUE : dateToPart; // 0 bei Datum bis bedeutet unendlich (ursprünglich war PEMTA bzw. PEMTB 999999...)
                if (dateFromPart > realDateTo) {
                    return false; // PEM-Termin-AB <= PEM-TERMIN-BIS ist nicht erfüllt
                    // Änderung von < nach <= in DAIMLER-4167
                }
            }

            if (!factoryPart.isEmpty()) {
                Set<String> validFactories = FactoriesHelper.getValidFactories(getEtkProject(), getProductIdFromModuleUsage());
                if (!validFactories.contains(factoryPart)) {
                    return false; // Werk ist ungültig
                }
            }
        } else if (!source.equals(iPartsImportDataOrigin.PRIMUS.getOrigin()) && !source.equals(iPartsImportDataOrigin.IPARTS.getOrigin())) {
            return false; // Quelle ist ungültig für Dokumentationsmethode != DIALOG
        }
        return true;
    }

    /**
     * Berechnet eine Farbtabelle und die Werkseinsatzdaten zur Beziehung "Farbtabelle zu Teil".
     *
     * @param allColorTableMap
     * @param allColorTableIds
     * @param kLfdnr
     * @param dataAttributes
     * @param aa
     * @param seriesId
     * @param isDIALOGPartList
     * @param responseData
     * @param responseSpikes
     */
    public void calcColorTableFactoryData(Map<String, iPartsColorTable> allColorTableMap, Set<String> allColorTableIds,
                                          String kLfdnr, DBDataObjectAttributes dataAttributes, String aa,
                                          iPartsSeriesId seriesId, boolean isDIALOGPartList, iPartsResponseData responseData,
                                          iPartsResponseSpikes responseSpikes) {
        // Map-Eintrag von Materialnummer auf iPartsColorTableForRetail bestimmen
        iPartsColorTable colorTables = allColorTableMap.get(kLfdnr);
        if (colorTables == null) {
            colorTables = new iPartsColorTable();
            allColorTableMap.put(kLfdnr, colorTables);
        }

        // Map-Eintrag von Farbvariantentabellen-ID auf ColorTable bestimmen
        String colorTableId = dataAttributes.getFieldValue(FIELD_DCTP_TABLE_ID);
        allColorTableIds.add(colorTableId);
        iPartsColorTable.ColorTable colorTable = colorTables.getColorTable(colorTableId);
        String pos = dataAttributes.getFieldValue(FIELD_DCTP_POS);
        String sdata = dataAttributes.getFieldValue(FIELD_DCTP_SDATA);
        if (colorTable == null) {
            colorTable = new iPartsColorTable.ColorTable();
            colorTable.colorTableId = new iPartsColorTableDataId(colorTableId);
            String colorSign = dataAttributes.getFieldValue(FIELD_DCTD_FIKZ);
            if (StrUtils.isValid(colorSign)) {
                colorTable.colorSign = colorSign;
            }
            colorTables.addColorTable(colorTable);
        }

        iPartsColorTableToPartId colorTableToPartId = new iPartsColorTableToPartId(colorTableId, pos, sdata);
        iPartsColorTable.ColorTableToPart colorTableToPart = colorTable.colorTableToPartsMap.get(colorTableToPartId);
        if (colorTableToPart == null) {
            colorTableToPart = new iPartsColorTable.ColorTableToPart();
            colorTableToPart.colorTableId = colorTableToPartId;

            // Quelle wichtig, um später zu unterscheiden, ob es sich um PRIMUS oder DIALOG Farbtabellen handelt
            iPartsImportDataOrigin dataOrigin = iPartsImportDataOrigin.getTypeFromCode(dataAttributes.getFieldValue(FIELD_DCTP_SOURCE));
            colorTableToPart.dataOrigin = dataOrigin;

            // PEM Flags setzen
            colorTableToPart.setEvalPemFrom(dataAttributes.getField(FIELD_DCTP_EVAL_PEM_FROM).getAsBoolean());
            colorTableToPart.setEvalPemTo(dataAttributes.getField(FIELD_DCTP_EVAL_PEM_TO).getAsBoolean());
        }

        colorTable.addColorTableToPart(colorTableToPart);

        if (isDIALOGPartList) { // Werkseinsatzdaten gibt es nur bei DIALOG
            // Werkseinsatzdaten für Farbvariantentabellen am Teil
            colorTableToPart.setFactoryData(getColorFactoryDataForRetail(colorTableToPart.getFactoryData(), dataAttributes,
                                                                         colorTableId, seriesId, aa,
                                                                         iPartsFactoryDataTypes.COLORTABLE_PART_AS,
                                                                         responseData, responseSpikes));
        }
    }

    /**
     * Farbvariantentabellen für benötigte Farbvariantentabellen IDs erstellen und anreichern mit Farbvarianteninhalten,
     * Werkseinsatz- und Rückmeldedaten.
     *
     * @param isDIALOGPartList
     * @param allColorTableIds Um diese Farbvariantentabellen IDs geht es; INPUT
     */
    public iPartsColorTable createGenericColorTableForRetail(final boolean isDIALOGPartList, final Set<String> allColorTableIds) {
        // Bei aktivem ChangeSet mit prinzipiell erlaubter getRecords()-Simulation keinen Zweifach-Join sondern einen Einzel-Join
        // und Nachladen von Daten verwenden, damit die Joins mittels getRecords()-Simulation anstatt mit einer forcierten
        // Pseudo-Transaktion laufen können
        boolean useDoubleJoin = EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS || !isRevisionChangeSetActive();
        if (!useDoubleJoin) {
            EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
            if (revisionsHelper != null) {
                // Der doppelte Join kann trotz aktivem ChangeSet auch dann verwendet werden, wenn es keine veränderten oder
                // neuen SerializedDBDataObjects in den Tabellen DA_COLORTABLE_CONTENT, DA_COLORTABLE_FACTORY und DA_COLOR_NUMBER
                // gibt (weil dafür durch die OR-Bedingung für DA_COLORTABLE_CONTENT und die LeftOuter-Joins bei den beiden
                // anderen Tabellen keine getRecords()-Simulation möglich ist bzw. keine where-Bedingungen vorhanden sind)
                useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_COLORTABLE_CONTENT, null,
                                                                                       RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                if (useDoubleJoin) {
                    useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_COLORTABLE_FACTORY, null,
                                                                                           RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                    if (useDoubleJoin) {
                        useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_COLOR_NUMBER, null,
                                                                                               RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                    }
                }
            }
        }

        // Generisches iPartsColorTableForRetail für alle relevanten Farbvariantentabellen unabhängig vom Stücklisteneintrag
        iPartsColorTable genericColorTableForRetail = new iPartsColorTable();
        final Map<String, EtkMultiSprache> colorToDescriptionMap = new HashMap<>();
        if (useDoubleJoin) {
            addGenericColorTableForRetail(isDIALOGPartList, allColorTableIds, genericColorTableForRetail, colorToDescriptionMap, true);
        } else {
            // Die getRecords()-Simulation kann keine OR-Bedingungen -> ein Aufruf pro Farbvariantentabellen-ID
            Set<String> colorTableIdSet = new HashSet<>(1);
            for (String colorTableId : allColorTableIds) {
                colorTableIdSet.clear();
                colorTableIdSet.add(colorTableId);
                addGenericColorTableForRetail(isDIALOGPartList, colorTableIdSet, genericColorTableForRetail, colorToDescriptionMap, false);
            }
        }

        // Generische Farbvarianteninhalte sortieren
        for (iPartsColorTable.ColorTable genericColorTable : genericColorTableForRetail.getColorTablesMap().values()) {
            genericColorTable.sortColorTableContents();
        }

        return genericColorTableForRetail;
    }

    private void addGenericColorTableForRetail(final boolean isDIALOGPartList, final Set<String> allColorTableIds,
                                               iPartsColorTable genericColorTableForRetail, Map<String, EtkMultiSprache> colorToDescriptionMap,
                                               boolean useDoubleJoin) {
        String fieldDctcTableId = TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_TABLE_ID);
        String[] whereFields = new String[allColorTableIds.size()];
        Arrays.fill(whereFields, fieldDctcTableId);
        String[] whereValues = new String[whereFields.length];
        int i = 0;
        for (String colorTableId : allColorTableIds) {
            whereValues[i] = colorTableId;
            i++;
        }

        // Rückmeldedaten-Cache; wird verwendet, um relevante Rückmeldedaten den Werkseinsatzdaten zuzuordnen
        final iPartsResponseData responseData = iPartsResponseData.getInstance(getEtkProject());
        // Ausreißer-Cache; wird verwendet, um relevante Ausreißer den Rückmeldedaten zuzuordnen
        final iPartsResponseSpikes responseSpikes = iPartsResponseSpikes.getInstance(getEtkProject());

        // Map von Farbvariantentabellen-ID auf einen generischen ColorTableContent unabhängig vom Stücklisteneintrag
        final Map<String, iPartsColorTable.ColorTableContent> genericColorTableContentMap = new HashMap<>();

        // Callback für die Farbvarianteninhalte
        final EtkProject etkProject = getEtkProject();
        EtkDataObjectList.FoundAttributesCallback foundColorTableContentCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes dataAttributes) {
                if (!useDoubleJoin) { // Join auf Farben simulieren?
                    // Benennung der Farbe über den lokalen Cache colorToDescriptionMap ermitteln
                    String colorKey = dataAttributes.getFieldValue(FIELD_DCTC_COLOR_VAR);
                    EtkMultiSprache colorDescription = colorToDescriptionMap.get(colorKey);
                    if (colorDescription == null) {
                        iPartsDataColorNumber dataColorNumber = new iPartsDataColorNumber(etkProject, new iPartsColorNumberId(colorKey));
                        if (dataColorNumber.existsInDB()) {
                            colorDescription = dataColorNumber.getFieldValueAsMultiLanguage(FIELD_DCN_DESC);
                        } else {
                            colorDescription = new EtkMultiSprache();
                        }
                        colorToDescriptionMap.put(colorKey, colorDescription);
                    }
                    DBDataObjectAttribute descriptionAttribute = new DBDataObjectAttribute(FIELD_DCN_DESC, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE,
                                                                                           false);
                    descriptionAttribute.setValueAsMultiLanguage(colorDescription, DBActionOrigin.FROM_DB);
                    dataAttributes.addField(descriptionAttribute, DBActionOrigin.FROM_DB);
                }
                return loadColorTableContentDataCallback(dataAttributes, isDIALOGPartList, genericColorTableContentMap,
                                                         genericColorTableForRetail, RELEASED_STATE_SET, responseData, responseSpikes);
            }
        };

        // Diverse (Outer) Joins mit den Tabellen für die Farbvarianteninhalte inkl. deren Werkseinsatzdaten (DA_COLOR*)
        // mit OR-Abfrage über alle relevanten Farbvariantentabellen-IDs
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_TABLE_ID, false, false));      // Farbvariantentabelle
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_POS, false, false));           // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_SDATA, false, false));         // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_COLOR_VAR, false, false));     // Farbvariante
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_PGRP, false, false));          // Produktgruppe für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_CODE, false, false));          // Code Konstruktion für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_ETKZ, false, false));          // ETKZ vom Inhalt für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_CODE_AS, false, false));       // Code AS für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVAL_PEM_FROM, false, false)); // Auswertung PEM ab
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVAL_PEM_TO, false, false));   // Auswertung PEM bis
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_SOURCE, false, false));        // Quelle für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_STATUS, false, false));        // Freigabestatus der Variante
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_FROM, false, false));    // Ereignis ab
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_TO, false, false));      // Ereignis bis
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_FROM_AS, false, false)); // AS-Ereignis ab
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_TO_AS, false, false));   // AS-Ereignis bis

        EtkDisplayFields selectFieldsForColorNumber = new EtkDisplayFields();
        selectFieldsForColorNumber.addFeld(new EtkDisplayField(TABLE_DA_COLOR_NUMBER, FIELD_DCN_DESC, true, false));    // Name der Farbvariante

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_FACTORY, false, false));       // Werk für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_ADAT, false, false));          // ADAT für den Retail-Filter (historische Werkseinsatzdaten entfernen)
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_DATA_ID, false, false));       // Kenner für Konstruktion oder AS für Farbvariantentabelle am Teil oder Farbvarianteninhalt
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMA, false, false));          // PEM ab für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTA, false, false));         // PEM Termin ab für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMB, false, false));          // PEM bis für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTB, false, false));         // PEM Termin bis für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCA, false, false));          // Steuercode ab für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STCB, false, false));          // Steuercode bis für den Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_STATUS, false, false));        // Freigabestatus

        EtkDataObjectList.JoinData colorTableFactoryJoinData = new EtkDataObjectList.JoinData(TABLE_DA_COLORTABLE_FACTORY, // Werkseinsatzdaten für die Farbvarianteninhalte
                                                                                              new String[]{ fieldDctcTableId,
                                                                                                            TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_POS),
                                                                                                            TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_SDATA) },
                                                                                              new String[]{ FIELD_DCCF_TABLE_ID,
                                                                                                            FIELD_DCCF_POS,
                                                                                                            FIELD_DCCF_SDATA },
                                                                                              true, false);

        EtkDataObjectList.JoinData[] joinDatas;
        if (useDoubleJoin) {
            selectFields.addFelder(selectFieldsForColorNumber);
            joinDatas = new EtkDataObjectList.JoinData[]{ colorTableFactoryJoinData,
                                                          new EtkDataObjectList.JoinData(TABLE_DA_COLOR_NUMBER,
                                                                                         new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_COLOR_VAR) },
                                                                                         new String[]{ FIELD_DCN_COLOR_NO },
                                                                                         true, false) };
        } else {
            joinDatas = new EtkDataObjectList.JoinData[]{ colorTableFactoryJoinData };
        }

        iPartsDataColorTableContentList colorTableContentList = new iPartsDataColorTableContentList();
        colorTableContentList.searchSortAndFillWithJoin(etkProject, etkProject.getDBLanguage(), selectFields,
                                                        whereFields, whereValues, whereFields.length > 1,
                                                        null, // Sortierung funktioniert mit einem reinen Callback nicht
                                                        false, foundColorTableContentCallback, joinDatas);

    }

    public boolean loadColorTableContentDataCallback(DBDataObjectAttributes dataAttributes, boolean isDIALOGPartList,
                                                     Map<String, iPartsColorTable.ColorTableContent> genericColorTableContentMap,
                                                     iPartsColorTable genericColorTableForRetail, Set<String> validStates,
                                                     iPartsResponseData responseData, iPartsResponseSpikes responseSpikes) {
        // Nur freigegebene Datensätze übrig lassen.
        String status = dataAttributes.getFieldValue(FIELD_DCCF_STATUS);
        if (StrUtils.isValid(status) && !validStates.contains(status)) {
            return false;
        }
        status = dataAttributes.getFieldValue(FIELD_DCTC_STATUS);
        if (StrUtils.isValid(status) && !validStates.contains(status)) {
            return false;
        }

        String source = dataAttributes.getFieldValue(FIELD_DCTC_SOURCE);

        // Werkseinsatzdaten für Farbvarianteninhalte
        String factoryContent = dataAttributes.getFieldValue(FIELD_DCCF_FACTORY);
        String pemFromContent = dataAttributes.getFieldValue(FIELD_DCCF_PEMA);
        String pemToContent = dataAttributes.getFieldValue(FIELD_DCCF_PEMB);
        long dateFromContent = iPartsFactoryData.getFactoryDateFromDateString(dataAttributes.getFieldValue(FIELD_DCCF_PEMTA),
                                                                              TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTA));
        long dateToContent = iPartsFactoryData.getFactoryDateFromDateString(dataAttributes.getFieldValue(FIELD_DCCF_PEMTB),
                                                                            TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTB));
        String dataIdContent = dataAttributes.getFieldValue(FIELD_DCCF_DATA_ID);

        // Bei DIALOG müssen einige Filterungen durchgeführt werden
        if (isDIALOGPartList) {
            if (!source.isEmpty() &&
                !source.equals(iPartsImportDataOrigin.MAD.getOrigin()) &&
                !source.equals(iPartsImportDataOrigin.DIALOG.getOrigin()) &&
                !source.equals(iPartsImportDataOrigin.IPARTS.getOrigin())) {
                return false; // Quelle ist ungültig für DIALOG
            }

            // Werkseinsatzdaten für Farbvariantentabellen am Teil und Farbvarianteninhalte müssen vorhanden sein
            // (entspricht quasi einem Inner Join - wg. ELDAS brauchen wir aber den Outer Join)
            if (!StrUtils.isValid(dataIdContent)) {
                return false; // mindestens an einer Stelle fehlen Werkseinsatzdaten
            }

            // Prüfung auf PEM-Termin-AB <= PEM-TERMIN-BIS (nur sinnvoll falls es eine PEM ab und PEM bis gibt)
            if (StrUtils.isValid(pemFromContent, pemToContent)) {
                if ((dateToContent == 0) && (dateFromContent == 0)) {
                    return false;
                }
                long realDateTo = (dateToContent == 0) ? Long.MAX_VALUE : dateToContent; // 0 bei Datum bis bedeutet unendlich (ursprünglich war PEMTA bzw. PEMTB 999999...)
                if (dateFromContent > realDateTo) {
                    return false; // PEM-Termin-AB <= PEM-TERMIN-BIS ist nicht erfüllt
                    // Änderung von < nach <= in DAIMLER-4167
                }
            }

            if (!factoryContent.isEmpty()) {
                Set<String> validFactories = FactoriesHelper.getValidFactories(getEtkProject(), getProductIdFromModuleUsage());
                if (!validFactories.contains(factoryContent)) {
                    return false; // Werk ist ungültig
                }
            }
        } else if (!source.equals(iPartsImportDataOrigin.PRIMUS.getOrigin())) {
            return false; // Quelle ist ungültig für Dokumentationsmethode != DIALOG
        }

        String colorTableId = dataAttributes.getFieldValue(FIELD_DCTC_TABLE_ID);

        // Map-Eintrag von Materialnummer, Farbvariantentabellen-ID und Farbvariantennummer auf ColorTableContent bestimmen
        String pos = dataAttributes.getFieldValue(FIELD_DCTC_POS);
        String sdata = dataAttributes.getFieldValue(FIELD_DCTC_SDATA);
        iPartsColorTableContentId colorTableContentId = new iPartsColorTableContentId(colorTableId, pos, sdata);
        String colorNumber = dataAttributes.getFieldValue(FIELD_DCTC_COLOR_VAR);
        String eventFromId = dataAttributes.getFieldValue(FIELD_DCTC_EVENT_FROM);
        String eventToId = dataAttributes.getFieldValue(FIELD_DCTC_EVENT_TO);
        String eventFromASId = dataAttributes.getFieldValue(FIELD_DCTC_EVENT_FROM_AS);
        String eventToASId = dataAttributes.getFieldValue(FIELD_DCTC_EVENT_TO_AS);
        String colorTableContentKey = colorTableContentId.toString("|");
        iPartsColorTable.ColorTableContent colorTableContent = genericColorTableContentMap.get(colorTableContentKey);
        if (colorTableContent == null) {
            colorTableContent = new iPartsColorTable.ColorTableContent();
            colorTableContent.colorTableContentId = colorTableContentId;
            colorTableContent.sdata = StrUtils.strToLongDef(sdata, 0);
            colorTableContent.colorNumber = colorNumber;
            colorTableContent.eventFrom = eventFromId;
            colorTableContent.eventTo = eventToId;
            colorTableContent.eventFromAS = eventFromASId;
            colorTableContent.eventToAS = eventToASId;

            // getAsMultiLanguage(null, false) ist ausreichend, weil DCN_DESC bereits inkl. Rückfallsprachen geladen
            // wurde und das Nachladen der Sprachen pro Farbe hier auch viel zu teuer wäre
            if (dataAttributes.fieldExists(FIELD_DCN_DESC)) {
                colorTableContent.colorName = dataAttributes.getField(FIELD_DCN_DESC).getAsMultiLanguage(null, false);
            } else {
                colorTableContent.colorName = new EtkMultiSprache();
            }

            colorTableContent.productGroup = dataAttributes.getFieldValue(FIELD_DCTC_PGRP);
            colorTableContent.code = dataAttributes.getFieldValue(FIELD_DCTC_CODE_AS);
            if (colorTableContent.code.isEmpty()) { // AS-Code hat Vorrang vor Konstruktions-Code
                colorTableContent.code = dataAttributes.getFieldValue(FIELD_DCTC_CODE);
            }
            colorTableContent.etkz = dataAttributes.getFieldValue(FIELD_DCTC_ETKZ);

            iPartsColorTable.ColorTable colorTable = genericColorTableForRetail.getColorTable(colorTableId);
            if (colorTable == null) {
                colorTable = new iPartsColorTable.ColorTable();
                colorTable.colorTableId = new iPartsColorTableDataId(colorTableId);
                genericColorTableForRetail.addColorTable(colorTable);
            }
            colorTable.colorTableContents.add(colorTableContent);

            genericColorTableContentMap.put(colorTableContentKey, colorTableContent);
        }

        if (isDIALOGPartList) { // Werkseinsatzdaten gibt es nur bei DIALOG
            // PEMs bei Farbvarianteninhalten je nach Flag auswerten
            colorTableContent.setEvalPemFrom(dataAttributes.getField(FIELD_DCTC_EVAL_PEM_FROM).getAsBoolean());
            colorTableContent.setEvalPemTo(dataAttributes.getField(FIELD_DCTC_EVAL_PEM_TO).getAsBoolean());
            colorTableContent.setFactoryData(getColorFactoryDataForRetail(colorTableContent.getFactoryData(), dataAttributes,
                                                                          colorTableId, null, null,
                                                                          iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS,
                                                                          responseData, responseSpikes));
        }

        return false;
    }

    /**
     * Farbvariantentabellen und gefilterte Farbvarianteninhalte kombinieren und den Stücklisteneinträgen zuweisen
     *
     * @param isDIALOGPartList
     * @param partlist
     * @param allColorTableMap           Zuordnung k_lfdnr -> Farbvariantentabellen; INPUT
     * @param genericColorTableForRetail Zuordnung Farbvariantentabelle -> Farbvarianteninhalte; INPUT
     * @param seriesIdMap                Zuordnung k_lfdnr -> Baureihe (nur für gefundene Farbvariantentabellen); INPUT, OUTPUT
     */
    private void assignColorTablesToPartListEntries(boolean isDIALOGPartList, DBDataObjectList<EtkDataPartListEntry> partlist,
                                                    final Map<String, iPartsColorTable> allColorTableMap,
                                                    iPartsColorTable genericColorTableForRetail, final Map<String, iPartsSeriesId> seriesIdMap) {
        // Rückmeldedaten-Cache; wird verwendet, um relevante Rückmeldedaten den Werkseinsatzdaten zuzuordnen
        final iPartsResponseData responseData = iPartsResponseData.getInstance(getEtkProject());
        // Ausreißer-Cache; wird verwendet, um relevante Ausreißer den Rückmeldedaten zuzuordnen
        final iPartsResponseSpikes responseSpikes = iPartsResponseSpikes.getInstance(getEtkProject());

        // Farbvariantentabellen und gefilterte Farbvarianteninhalte den Stücklisteneinträgen zuweisen
        for (EtkDataPartListEntry partListEntry : partlist) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                String kLfdnr = partListEntry.getFieldValue(FIELD_K_LFDNR);
                iPartsColorTable colorTable = null;
                if (!allColorTableMap.isEmpty()) {
                    colorTable = allColorTableMap.get(kLfdnr);
                }
                if (colorTable != null) {
                    iPartsSeriesId seriesId = null;
                    String aa = null;
                    if (isDIALOGPartList) {
                        seriesId = getSeriesFromPartListEntryAttributes(partListEntry.getAttributes(), kLfdnr, seriesIdMap);
                        aa = partListEntry.getFieldValue(FIELD_K_AA);
                    }

                    // Generische Farbvarianteninhalte an die konkreten Farbvariantentabellen am Teil kopieren,
                    // Werkseinsatzdaten filtern und gefilterte Rückmeldedaten hinzufügen
                    Map<String, iPartsColorTable.ColorTable> destColorTablesMap = colorTable.getColorTablesMap();
                    String[] destTableIds = destColorTablesMap.keySet().toArray(new String[destColorTablesMap.size()]);
                    for (String destTableId : destTableIds) {
                        iPartsColorTable.ColorTable genericColorTableContents = genericColorTableForRetail.getColorTable(destTableId);
                        iPartsColorTable.ColorTable destColorTable = destColorTablesMap.get(destTableId);
                        if (genericColorTableContents != null) {
                            for (iPartsColorTable.ColorTableContent colorTableContent : genericColorTableContents.colorTableContents) {
                                if (isDIALOGPartList) { // Werkseinsatzdaten filtern und gefilterte Rückmeldedaten hinzufügen
                                    iPartsColorFactoryDataForRetail genericFactoryData = colorTableContent.getFactoryData();
                                    iPartsColorFactoryDataForRetail destFactoryData = new iPartsColorFactoryDataForRetail();
                                    for (Map.Entry<String, List<iPartsColorFactoryDataForRetail.DataForFactory>> genericDataForFactoriesEntry : genericFactoryData.getFactoryDataMap().entrySet()) {
                                        String factoryNumber = genericDataForFactoriesEntry.getKey();
                                        List<iPartsColorFactoryDataForRetail.DataForFactory> genericDataForFactories = genericDataForFactoriesEntry.getValue();
                                        List<iPartsColorFactoryDataForRetail.DataForFactory> destDataForFactories = new DwList<iPartsColorFactoryDataForRetail.DataForFactory>(genericDataForFactories.size());
                                        for (iPartsColorFactoryDataForRetail.DataForFactory genericDataForFactory : genericDataForFactories) {
                                            iPartsColorFactoryDataForRetail.DataForFactory destDataForFactory = new iPartsColorFactoryDataForRetail.DataForFactory();
                                            destDataForFactory.assign(genericDataForFactory);

                                            // Gefilterte Rückmeldedaten hinzufügen
                                            assignResponseDataAndSpikesToFactoryData(destDataForFactory, destDataForFactory.pemFrom,
                                                                                     factoryNumber, seriesId, aa, true,
                                                                                     responseData, responseSpikes);
                                            assignResponseDataAndSpikesToFactoryData(destDataForFactory, destDataForFactory.pemTo,
                                                                                     factoryNumber, seriesId, aa, false,
                                                                                     responseData, responseSpikes);

                                            destDataForFactories.add(destDataForFactory);
                                        }
                                        if (!destDataForFactories.isEmpty()) {
                                            destFactoryData.setDataForFactory(factoryNumber, destDataForFactories);
                                        }
                                    }

                                    // Falls es gültige Werkseinsatzdaten gibt, den Farbvarianteninhalt mit den Werkseinsatzdazen hinzufügen
                                    if (!destFactoryData.getFactoryDataMap().isEmpty()) {
                                        iPartsColorTable.ColorTableContent destColorTableContent = colorTableContent.cloneMe(false, false);
                                        destColorTableContent.setFactoryData(destFactoryData);
                                        destColorTable.colorTableContents.add(destColorTableContent);
                                    }
                                } else {
                                    destColorTable.colorTableContents.add(colorTableContent.cloneMe(false, false));
                                }
                            }
                        }

                        // Falls es keine gefilterten Farbvarianteninhalte für die Farbvariantentabelle gibt, den Eintrag entfernen
                        if (destColorTable.colorTableContents.isEmpty()) {
                            colorTable.removeColorTable(destTableId);
                        }
                    }

                    // Falls es am Ende überhaupt keine gefilterten Farbvarianteninhalte für irgendeine Farbvariantentabelle
                    // gibt, muss colorTable auf null gesetzt werden, weil es dann keine gültigen Farbvariantentabellen für diesen
                    // Stücklisteneintrag gibt
                    if (colorTable.getColorTablesMap().isEmpty()) {
                        colorTable = null;
                    }

                    iPartsPartListEntry.setColorTableForRetailWithoutFilter(colorTable);
                } else {
                    // Auch null setzen, damit in dem Stücklisteneintrag die Farbvariantentabellen als geladen markiert werden
                    iPartsPartListEntry.setColorTableForRetailWithoutFilter(null);
                }
            }
        }

    }


    /**
     * Werkseinsatzdaten (optional inkl. Rückmeldedaten u. Ausreißer bei vorhandenem Wert für <i>aa</i>) bestimmen für
     * Farbvariantentabelle oder Farbvarianteninhalt.
     *
     * @param factoryData
     * @param dataAttributes
     * @param colorTableId
     * @param seriesId
     * @param aa                        Muss {@code != null} sein, damit Rückmeldedaten u. Ausreißer bestimmt werden
     * @param factoryDataTypeAfterSales
     * @param responseData
     * @param responseSpikes
     * @return
     */
    public iPartsColorFactoryDataForRetail getColorFactoryDataForRetail(iPartsColorFactoryDataForRetail factoryData, DBDataObjectAttributes dataAttributes,
                                                                        String colorTableId, iPartsSeriesId seriesId, String aa,
                                                                        iPartsFactoryDataTypes factoryDataTypeAfterSales,
                                                                        iPartsResponseData responseData, iPartsResponseSpikes responseSpikes) {

        String pos = dataAttributes.getFieldValue(FIELD_DCCF_POS);
        String factory = dataAttributes.getFieldValue(FIELD_DCCF_FACTORY);
        String adatString = dataAttributes.getFieldValue(FIELD_DCCF_ADAT);
        String dataId = dataAttributes.getFieldValue(FIELD_DCCF_DATA_ID);
        String sdata = dataAttributes.getFieldValue(FIELD_DCCF_SDATA);
        String pemFrom = dataAttributes.getFieldValue(FIELD_DCCF_PEMA);
        String pemTo = dataAttributes.getFieldValue(FIELD_DCCF_PEMB);
        long dateFrom = iPartsFactoryData.getFactoryDateFromDateString(dataAttributes.getFieldValue(FIELD_DCCF_PEMTA),
                                                                       TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTA));
        long dateTo = iPartsFactoryData.getFactoryDateFromDateString(dataAttributes.getFieldValue(FIELD_DCCF_PEMTB),
                                                                     TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTB));
        String stca = dataAttributes.getFieldValue(FIELD_DCCF_STCA);
        String stcb = dataAttributes.getFieldValue(FIELD_DCCF_STCB);
        String releaseStateStr = dataAttributes.getFieldValue(FIELD_DCCF_STATUS);

        long adat = StrUtils.strToLongDef(adatString, 0);
        List<iPartsColorFactoryDataForRetail.DataForFactory> dataForFactoryList = null;
        iPartsColorFactoryDataForRetail.DataForFactory dataForFactory = null;
        if (factoryData == null) {
            factoryData = new iPartsColorFactoryDataForRetail();
        } else {
            dataForFactoryList = factoryData.getDataForFactory(factory);

            // Bei DIALOG höchstes ADAT ermitteln und AS-Daten anstatt Produktionsdaten verwenden falls vorhanden
            if (getDocumentationType().isPKWDocumentationType()) {
                if (dataForFactoryList != null) {
                    if (dataId.equals(factoryDataTypeAfterSales.getDbValue())) { // Logik analog zu ColorTableHelper.hasValidFactoryDate()
                        if (!dataForFactoryList.isEmpty()) {
                            // Es gibt nur einen AS-Datensatz für Werkseinsatzdaten, aber mehrere Rückmeldedaten dafür
                            iPartsColorFactoryDataForRetail.DataForFactory otherDataForFactory = dataForFactoryList.get(0);
                            if (otherDataForFactory.factoryDataId.getDataId().equals(factoryDataTypeAfterSales.getDbValue())
                                && (otherDataForFactory.adat >= adat)) {
                                // Werkseinsatzdaten bereits gefunden -> neue Rückmeldedaten hinzufügen
                                dataForFactory = otherDataForFactory;
                            } else {
                                // Alle anderen Werkseinsatzdaten können nur von der Produktion kommen -> entfernen, da AS-Daten gewinnen
                                dataForFactoryList.clear();
                            }
                        }
                    } else { // Produktions-Daten
                        Iterator<iPartsColorFactoryDataForRetail.DataForFactory> iterator = dataForFactoryList.iterator();
                        while (iterator.hasNext()) {
                            iPartsColorFactoryDataForRetail.DataForFactory otherDataForFactory = iterator.next();
                            if (otherDataForFactory.factoryDataId.getDataId().equals(factoryDataTypeAfterSales.getDbValue())) {
                                return factoryData;
                            } else if (otherDataForFactory.adat < adat) { // Logik analog zu ColorTableHelper.filterForLatestColorTableFactoryData()
                                iterator.remove(); // Andere Produktions-Werkseinsatzdaten sind älter als diese
                            } else if (otherDataForFactory.adat == adat) {
                                dataForFactory = otherDataForFactory; // Werkseinsatzdaten bereits gefunden -> neue Rückmeldedaten hinzufügen
                            } else {
                                return factoryData;
                            }
                        }
                    }
                }
            }
        }

        if (dataForFactoryList == null) {
            dataForFactoryList = new DwList<>();
            factoryData.setDataForFactory(factory, dataForFactoryList);
        }

        // Neue Werkseinsatzdaten erzeugen falls notwendig und der Liste der Werkseinsatzdaten hinzufügen
        if (dataForFactory == null) {
            // Bei DIALOG Prüfung auf PEM-Termin-AB und PEM-TERMIN-BIS unendlich
            if (getDocumentationType().isPKWDocumentationType()) {
                // DAIMLER-8299: Die Farbe wurde nie eingebaut (ursprünglich war PEMTA bzw. PEMTB 999999...),
                // aber beim Baumuster-Zeitscheibenfilter muss dies berücksichtigt werden
                if ((dateFrom == 0) && (dateTo == 0)) {
                    factoryData.setHasFactoryDataWithInfiniteDates(true);
                    return factoryData; // Werkseinsatzdaten selbst sind ungültig -> nicht hinzufügen
                }
            }

            dataForFactory = new iPartsColorFactoryDataForRetail.DataForFactory();
            dataForFactory.factoryDataId = new iPartsColorTableFactoryId(colorTableId, pos, factory, adatString, dataId, sdata);
            dataForFactory.adat = adat;
            dataForFactory.releaseState = iPartsDataReleaseState.getTypeByDBValue(releaseStateStr);
            dataForFactory.pemFrom = pemFrom;
            dataForFactory.pemTo = pemTo;
            dataForFactory.stCodeFrom = stca;
            dataForFactory.stCodeTo = stcb;
            dataForFactory.dateFrom = dateFrom;
            dataForFactory.dateTo = dateTo;

            // Rückmeldedaten und Ausreißer zu PEMs nur dann ermitteln falls die Ausführungsart bekannt ist
            if (aa != null) {
                assignResponseDataAndSpikesToFactoryData(dataForFactory, pemFrom, factory, seriesId, aa, true, responseData, responseSpikes);
                assignResponseDataAndSpikesToFactoryData(dataForFactory, pemTo, factory, seriesId, aa, false, responseData, responseSpikes);
            }

            dataForFactoryList.add(dataForFactory);
        }

        return factoryData;
    }

    /**
     * Baureihe für den Stücklisteneintrag aus den übergebenen Attributen, laufender Nummer und dem Cache bestimmen
     *
     * @param partListEntryAttributes
     * @param kLfdnr
     * @param seriesIdMap             Zuordnung k_lfdnr -> Baureihe; INPUT, OUTPUT
     * @return
     */
    private iPartsSeriesId getSeriesFromPartListEntryAttributes(DBDataObjectAttributes partListEntryAttributes, String kLfdnr,
                                                                Map<String, iPartsSeriesId> seriesIdMap) {
        // Baureihe und Ausführungsart für den Stücklisteneintrag bestimmen
        iPartsSeriesId seriesId = seriesIdMap.get(kLfdnr);
        if (seriesId == null) {
            // Baureihe aus dem Stücklisteneintrag bestimmen

            seriesId = getSeriesId(partListEntryAttributes);

            seriesIdMap.put(kLfdnr, seriesId);
        }
        return seriesId;
    }

    /**
     * Lädt alle Ersetzungen für die gesamte Stückliste
     *
     * @param partlist
     */
    public void loadAllReplacementsForPartList(DBDataObjectList<EtkDataPartListEntry> partlist) {
        // prüfen ob diese Assembly teil einer Versorgungsrelevanten Baureihe ist, und nur dann vererbte Ersetzungen
        // für Vorgänger- und Nachfolgerstände generieren
        loadAllReplacementsForPartList(partlist, isSeriesRelevantForImport());
    }

    /**
     * Ist die Baureihe (falls vorhanden) versorgungsrelevant für DIALOG-Importe?
     *
     * @return
     */
    public boolean isSeriesRelevantForImport() {
        iPartsSeriesId seriesId = getSeriesId();
        if (seriesId != null) {
            return iPartsDIALOGSeriesValidityCache.getInstance(getEtkProject()).isSeriesValidForDIALOGImport(seriesId.getSeriesNumber());
        } else {
            return false;
        }
    }

    public void loadAllReplacementsForPartList(DBDataObjectList<EtkDataPartListEntry> partlist, boolean isSeriesRelevantForImport) {
        String[] assemblyPKValues = { getAsId().getKVari(), getAsId().getKVer() };

        // Bei aktivem ChangeSet mit prinzipiell erlaubter getRecords()-Simulation keinen Zweifach-Join sondern einen Einzel-Join
        // mit Nachladen von Daten verwenden, damit die Joins mittels getRecords()-Simulation anstatt mit einer forcierten
        // Pseudo-Transaktion laufen können
        boolean useDoubleJoin = EtkDataObjectList.PSEUDO_TRANSACTIONS_FOR_SIMPLE_JOINS || !isRevisionChangeSetActive();
        if (!useDoubleJoin) {
            EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
            if (revisionsHelper != null) {
                // Der doppelte Join kann trotz aktivem ChangeSet auch dann verwendet werden, wenn es keine veränderten oder
                // neuen SerializedDBDataObjects in der Tabelle DA_INCLUDE_PART gibt (weil dafür keine where-Bedingung möglich
                // ist) sowie keine veränderten oder neuen SerializedDBDataObjects in der Tabelle KATALOG und DA_REPLACE_PART
                // für die aktuelle AssemblyId
                useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_INCLUDE_PART, null,
                                                                                       RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                if (useDoubleJoin) {
                    useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_KATALOG, assemblyPKValues,
                                                                                           RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                    if (useDoubleJoin) {
                        useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_REPLACE_PART, assemblyPKValues,
                                                                                               RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                        if (useDoubleJoin) {
                            useDoubleJoin = revisionsHelper.getSerializedObjectsByPKValuesAndState(TABLE_DA_INCLUDE_PART, assemblyPKValues,
                                                                                                   RELEVANT_SERIALIZED_OBJECT_STATES_FOR_JOINS_SIM) == null;
                        }
                    }
                }
            }
        }

        final Map<String, EtkDataPartListEntry> partListEntriesMap = iPartsReplacementHelper.createLfdNrToPartlistEntryMap(partlist);
        final Map<String, List<DBDataObjectAttributes>> includePartAttributesMap = new HashMap<>();
        final Set<String> replacementRelevantStatesDBValues = iPartsDataReleaseState.getReplacementRelevantStatesDBValues();
        final Map<String, List<iPartsReplacement>> allPredecessors = new HashMap<>();
        final Map<String, List<iPartsReplacement>> allSuccessors = new HashMap<>();

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_INCLUDE_PART, FIELD_DIP_SEQNO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_INCLUDE_PART, FIELD_DIP_INCLUDE_MATNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_INCLUDE_PART, FIELD_DIP_INCLUDE_QUANTITY, false, false));

        if (!useDoubleJoin) {
            // Separat zunächst alle Mitlieferteile für das Modul laden und in includePartAttributesMap ablegen
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_INCLUDE_PART, FIELD_DIP_VARI, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_INCLUDE_PART, FIELD_DIP_VER, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_INCLUDE_PART, FIELD_DIP_LFDNR, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_INCLUDE_PART, FIELD_DIP_REPLACE_MATNR, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_INCLUDE_PART, FIELD_DIP_REPLACE_LFDNR, false, false));

            EtkDataObjectList.FoundAttributesCallback foundIncludeAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes dataAttributes) {
                    String includePartKey = dataAttributes.getFieldValue(FIELD_DIP_VARI) + '|' + dataAttributes.getFieldValue(FIELD_DIP_VER)
                                            + '|' + dataAttributes.getFieldValue(FIELD_DIP_LFDNR) + '|' + dataAttributes.getFieldValue(FIELD_DIP_REPLACE_MATNR)
                                            + '|' + dataAttributes.getFieldValue(FIELD_DIP_REPLACE_LFDNR);
                    List<DBDataObjectAttributes> includePartAttributesList = includePartAttributesMap.computeIfAbsent(includePartKey,
                                                                                                                      s -> new ArrayList<>());
                    includePartAttributesList.add(dataAttributes);

                    return false;
                }
            };

            iPartsDataIncludePartList includePartList = new iPartsDataIncludePartList();
            includePartList.searchSortAndFillWithJoin(getEtkProject(), null, selectFields,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_INCLUDE_PART, FIELD_DIP_VARI),
                                                                    TableAndFieldName.make(TABLE_DA_INCLUDE_PART, FIELD_DIP_VER) },
                                                      assemblyPKValues, false, null, false, foundIncludeAttributesCallback);
        }

        boolean useDoubleJoinFinal = useDoubleJoin;
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes dataAttributes) {
                if (!useDoubleJoinFinal) { // Join auf Mitlieferteile simulieren?
                    String includePartKey = dataAttributes.getFieldValue(FIELD_DRP_VARI) + '|' + dataAttributes.getFieldValue(FIELD_DRP_VER)
                                            + '|' + dataAttributes.getFieldValue(FIELD_DRP_LFDNR) + '|' + dataAttributes.getFieldValue(FIELD_DRP_REPLACE_MATNR)
                                            + '|' + dataAttributes.getFieldValue(FIELD_DRP_REPLACE_LFDNR);
                    List<DBDataObjectAttributes> includePartAttributesList = includePartAttributesMap.get(includePartKey);
                    if (includePartAttributesList != null) {
                        // Für jedes Mitlieferteil den Callback separat aufrufen
                        for (DBDataObjectAttributes includePartAttributes : includePartAttributesList) {
                            dataAttributes.addFields(includePartAttributes, DBActionOrigin.FROM_DB); // Attribute der Mitlieferteile hinzufügen
                            foundReplacementsCallback(dataAttributes, replacementRelevantStatesDBValues, partListEntriesMap,
                                                      allPredecessors, allSuccessors);
                        }
                        return false;
                    } else { // Keine Mitlieferteile vorhanden -> leere Felder für Mitlieferteile hinzufügen
                        dataAttributes.addField(FIELD_DIP_SEQNO, "", DBActionOrigin.FROM_DB);
                        dataAttributes.addField(FIELD_DIP_INCLUDE_MATNR, "", DBActionOrigin.FROM_DB);
                        dataAttributes.addField(FIELD_DIP_INCLUDE_QUANTITY, "", DBActionOrigin.FROM_DB);
                    }
                }
                foundReplacementsCallback(dataAttributes, replacementRelevantStatesDBValues, partListEntriesMap,
                                          allPredecessors, allSuccessors);
                return false;
            }
        };

        // Join mit der KATALOG-Tabelle für alle Stücklisteneinträge des Moduls mit Outer Join von den Ersetzungen
        // (DA_REPLACE_PART) auf die Mitlieferteile (DA_INCLUDE_PART)
        if (!useDoubleJoin) {
            selectFields.clear();
        }
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VARI, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_VER, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_LFDNR, false, false));

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_SEQNO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_MATNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_LFDNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_RFMEA, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_RFMEN, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_SOURCE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_SOURCE_GUID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_REPLACE_SOURCE_GUID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_PART, FIELD_DRP_STATUS, false, false));

        EtkDataObjectList.JoinData replacePartJoinData = new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                                        new String[]{ FIELD_DRP_VARI,
                                                                                                      FIELD_DRP_VER,
                                                                                                      FIELD_DRP_LFDNR },
                                                                                        new String[]{ FIELD_K_VARI,
                                                                                                      FIELD_K_VER,
                                                                                                      FIELD_K_LFDNR },
                                                                                        false, false);

        EtkDataObjectList.JoinData includePartJoinData = new EtkDataObjectList.JoinData(TABLE_DA_INCLUDE_PART,
                                                                                        new String[]{ FIELD_DRP_VARI,
                                                                                                      FIELD_DRP_VER,
                                                                                                      FIELD_DRP_LFDNR,
                                                                                                      FIELD_DRP_REPLACE_MATNR,
                                                                                                      FIELD_DRP_REPLACE_LFDNR },
                                                                                        new String[]{ FIELD_DIP_VARI,
                                                                                                      FIELD_DIP_VER,
                                                                                                      FIELD_DIP_LFDNR,
                                                                                                      FIELD_DIP_REPLACE_MATNR,
                                                                                                      FIELD_DIP_REPLACE_LFDNR },
                                                                                        true, false);

        EtkDataObjectList.JoinData[] joinDatas;
        if (useDoubleJoin) {
            joinDatas = new EtkDataObjectList.JoinData[]{ replacePartJoinData, includePartJoinData };
        } else {
            joinDatas = new EtkDataObjectList.JoinData[]{ replacePartJoinData };
        }

        iPartsDataReplacePartList replacementsList = new iPartsDataReplacePartList();
        replacementsList.searchSortAndFillWithJoin(getEtkProject(), null, selectFields,
                                                   new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                                 TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER),
                                                                 // getRecords()-Simulation von Joins unterstützen durch diese redundanten where-Felder
                                                                 TableAndFieldName.make(TABLE_DA_REPLACE_PART, FIELD_DRP_VARI),
                                                                 TableAndFieldName.make(TABLE_DA_REPLACE_PART, FIELD_DRP_VER) },
                                                   new String[]{ getAsId().getKVari(), getAsId().getKVer(),
                                                                 getAsId().getKVari(), getAsId().getKVer() }, false,
                                                   null, // sortFields funktionieren bei reinem Callback nicht
                                                   false, foundAttributesCallback, joinDatas);

        if (isSeriesRelevantForImport) {
            iPartsReplacementKEMHelper replacementKEMHelper = new iPartsReplacementKEMHelper(partlist);
            // die Ersetzungen für Vorgänger / Nachfolger Stände zusätzlich erzeugen
            replacementKEMHelper.createAndAddReplacementForAllKEMS(allSuccessors, allPredecessors);
        }

        if (getDocumentationType().isTruckDocumentationType()) { // Bei PKW werden die PRIMUS-Ersetzungen erst bei der Filterung ermittelt
            iPartsPRIMUSReplacementsLoader primusReplacementsLoader = new iPartsPRIMUSReplacementsLoader(getEtkProject(), partlist);
            primusReplacementsLoader.addPrimusReplacementsForPartList(allSuccessors, false);
        }

        // Ersetzungen sortieren und den Stücklisteneinträgen zuweisen
        for (EtkDataPartListEntry partListEntry : partlist) {
            List<iPartsReplacement> predecessorsOfPartListEntry = null;
            if (!allPredecessors.isEmpty()) {
                predecessorsOfPartListEntry = allPredecessors.get(partListEntry.getAsId().getKLfdnr());
                if (predecessorsOfPartListEntry != null) {
                    Collections.sort(predecessorsOfPartListEntry, iPartsReplacementHelper.seqNoComparator);
                }
            }
            List<iPartsReplacement> successorsOfPartListEntry = null;
            if (!allSuccessors.isEmpty()) {
                successorsOfPartListEntry = allSuccessors.get(partListEntry.getAsId().getKLfdnr());
                if (successorsOfPartListEntry != null) {
                    Collections.sort(successorsOfPartListEntry, iPartsReplacementHelper.seqNoComparator);
                }
            }
            // Ruft indirekt auch updatePEMFlagsFromReplacements() auf
            ((iPartsDataPartListEntry)partListEntry).setReplacements(predecessorsOfPartListEntry, successorsOfPartListEntry);
        }
    }

    /**
     * Callback für das Laden der Ersetzungen inklusive deren Mitlieferteilen.
     *
     * @param dataAttributes     Die Attribute des Mitlieferteils ({@link iPartsDataIncludePart}) plus denen seiner zugehörigen
     *                           Ersetzung ({@link iPartsDataReplacePart}). Die Attribute des Mitlieferteils sind leer,
     *                           falls es keine Mitlieferteile zur Ersetzung gibt.
     * @param validStates        Gibt an, welchen Status die Datensätze haben dürfen, um berücksichtigt zu werden.
     *                           Wenn {@code null}, werden alle Datensätze unabhängig vom Status berücksichtigt
     * @param partListEntriesMap
     * @param allPredecessors    Map von laufender Nummer des Nachfolgers in Stückliste auf Liste mit Vorgänger-Ersetzungen für diesen.
     * @param allSuccessors      Map von laufender Nummer des Vorgängers in Stückliste auf Liste mit Nachfolger-Ersetzungen für diesen.
     */
    public void foundReplacementsCallback(DBDataObjectAttributes dataAttributes, Set<String> validStates,
                                          Map<String, EtkDataPartListEntry> partListEntriesMap,
                                          Map<String, List<iPartsReplacement>> allPredecessors,
                                          Map<String, List<iPartsReplacement>> allSuccessors) {
        // Datensätze je nach Status ausfiltern (z.B. im Retail "neu", da diese geprüft werden müssen bevor sie retailrelevant werden)
        String status = dataAttributes.getFieldValue(FIELD_DRP_STATUS);
        if ((validStates != null) && StrUtils.isValid(status) && !validStates.contains(status)) {
            return;
        }

        // Vorgänger bestimmen
        String kLfdnr = dataAttributes.getFieldValue(FIELD_DRP_LFDNR);

        // Ersetzung (Nachfolger) bestimmen
        String successorLfdnr = dataAttributes.getFieldValue(FIELD_DRP_REPLACE_LFDNR);

        iPartsDataReplacePart dummyReplacePart = new iPartsDataReplacePart(getEtkProject(), new iPartsReplacePartId());
        dummyReplacePart.setAttributes(dataAttributes, DBActionOrigin.FROM_DB);
        iPartsReplacement replacement = iPartsReplacement.createReplacement(dummyReplacePart, partListEntriesMap);
        if (replacement == null) {
            return;
        }

        // Vorab schon mal das Flag setzen, dass die Mitlieferteile geladen wurden -> wird später bei Bedarf
        // durch tatsächlich vorhandene Mitlieferteile überschrieben
        replacement.setIncludeParts(null);

        replacement = iPartsReplacementHelper.addToMapIfNotExists(allSuccessors, kLfdnr, replacement);

        // Evtl. Mitlieferteil hinzufügen
        String includePartNumber = dataAttributes.getFieldValue(iPartsConst.FIELD_DIP_INCLUDE_MATNR);
        if (!includePartNumber.isEmpty()) {
            // Liste der Mitlieferteile bei Bedarf erzeugen
            Collection<iPartsReplacement.IncludePart> includeParts = replacement.getIncludeParts(null);
            if (includeParts == null) {
                includeParts = new TreeSet<>(iPartsReplacement.IncludePart.seqNoComparator);
                replacement.setIncludeParts(includeParts);
            }

            iPartsReplacement.IncludePart includePart = new iPartsReplacement.IncludePart(dataAttributes);
            includeParts.add(includePart);
        }

        // Ersetzung auch als Vorgänger hinzufügen wenn der Nachfolger sich in der Stückliste befindet
        if (!successorLfdnr.isEmpty()) {
            iPartsReplacementHelper.addToMapIfNotExists(allPredecessors, successorLfdnr, replacement);
        }
    }


    /**
     * Wahlweise-Sets für Stücklisteneinträge bestimmen
     *
     * @param partlist
     */
    public void createWWSetsForPartList(DBDataObjectList<EtkDataPartListEntry> partlist) {
        wwUnfilteredPartlistEntryIds = new HashSet<PartListEntryId>();
        wwGUIDtoUnfilteredPartlistEntriesMap = new HashMap<String, List<EtkDataPartListEntry>>();
        for (EtkDataPartListEntry partListEntry : partlist) {
            String wwGUID = partListEntry.getFieldValue(FIELD_K_WW);
            if (!wwGUID.isEmpty()) { // Wahlweise-GUID (Set) vorhanden -> Stücklisteneintrag als wahlweise markieren
                wwUnfilteredPartlistEntryIds.add(partListEntry.getAsId());

                // Wahlweise-Set aufbauen
                List<EtkDataPartListEntry> wwSetPartListEntries = wwGUIDtoUnfilteredPartlistEntriesMap.get(wwGUID);
                if (wwSetPartListEntries == null) {
                    wwSetPartListEntries = new DwList<EtkDataPartListEntry>();
                    wwGUIDtoUnfilteredPartlistEntriesMap.put(wwGUID, wwSetPartListEntries);
                }
                wwSetPartListEntries.add(partListEntry);
            } else {
                String wwExtraParts = partListEntry.getFieldValue(FIELD_K_WW_EXTRA_PARTS);
                if (!wwExtraParts.isEmpty()) { // Extra Wahlweise-Teile vorhanden -> Stücklisteneintrag als wahlweise markieren
                    wwUnfilteredPartlistEntryIds.add(partListEntry.getAsId());
                }
            }
        }
    }

    /**
     * Liefert die {@link KgTuId}, in die der Stücklisteneintrag mit diesem BCTE-Schlüssel vorraussichtlich übernommen
     * werden soll. Diese Vorhersage wurde durch eine KI bestimmt, deren Ergebnisse in DA_HMMSM_KGTU hinterlegt sind.
     *
     * @param requestedBCTEKey
     * @return
     */
    public KgTuId getPredictedKGTUforBCTEKey(iPartsDialogBCTEPrimaryKey requestedBCTEKey) {
        if (bcteKeyToPredictedKGTUMap == null) {
            bcteKeyToPredictedKGTUMap = new HashMap<>();
            String dialogSourceContext = requestedBCTEKey.getHmMSmId().getDIALOGSourceContext();
            iPartsDataKgTuPredictionList kgTuPredictions = iPartsDataKgTuPredictionList.loadListForHmMSmWithSeries(getEtkProject(), dialogSourceContext);
            for (iPartsDataKgTuPrediction kgTuPrediction : kgTuPredictions) {
                String bcteKeyString = kgTuPrediction.getAsId().getDialogId();
                iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(bcteKeyString);
                bcteKeyToPredictedKGTUMap.put(bcteKey, kgTuPrediction.getKgTuId());
            }
        }
        return bcteKeyToPredictedKGTUMap.get(requestedBCTEKey);
    }

    public Map<PartListEntryId, List<iPartsDataModuleCemat>> getCematMapForModule() {
        if (cematMapForModule == null) {
            cematMapForModule = iPartsDataModuleCematList.loadCematMapForModule(getEtkProject(), getAsId());
        }
        return cematMapForModule;
    }

    public DBDataObjectAttributes getGenInstallLocationAttributes(EtkDataPartListEntry partListEntry) {
        iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
        if (genInstallLocationMap == null) {
            if (bcteKey != null) {
                genInstallLocationMap = iPartsDataGenInstallLocationList.loadAllReleasedDataForHmMSmIdAsMap(getEtkProject(),
                                                                                                            bcteKey.getHmMSmId());
            }
        }
        return iPartsDataGenInstallLocationList.getGenInstallLocationAttributesForBcteKey(genInstallLocationMap, bcteKey);
    }

    /**
     * Gibt alle Wahlweise-{@link EtkDataPartListEntry}s für die angegebene Wahlweise-GUID zurück
     *
     * @param wwGUID
     * @return {@code null} falls keine Wahlweise-{@link EtkDataPartListEntry}s vorhanden sind für die angegebene Wahlweise-GUID
     */
    public List<EtkDataPartListEntry> getWWPartListEntries(String wwGUID) {
        if (wwGUIDtoUnfilteredPartlistEntriesMap != null) {
            return wwGUIDtoUnfilteredPartlistEntriesMap.get(wwGUID);
        }

        return null;
    }

    /**
     * Ermittelt die nächste freie Wahlweise GUID (Nummer).
     * Dabei wird einfach die maximale verwendete Nummer ermittelt und 1 addiert. Vorhandene Lücken werden
     * nicht gefüllt. Im Fehlerfall wird {@code 99} zurückgeliefert
     *
     * @return
     */
    public String getNextUnusedWWGUID() {
        return iPartsWWPartsHelper.getNextUnusedWWGUID(wwGUIDtoUnfilteredPartlistEntriesMap.keySet());
    }

    @Override
    public synchronized boolean hasSubAssemblies(boolean filtered) {
        if (getAsId().isVirtual()) {
            iPartsVirtualAssembly virtualAssembly = iPartsVirtualAssemblyFactory.createVirtualAssembly(getEtkProject(), getVirtualNodesPath(), getAsId());

            if (virtualAssembly != null) {
                iPartsVirtualAssembly.SubAssemblyState state = virtualAssembly.getSubAssemblyState();
                if (state == iPartsVirtualAssembly.SubAssemblyState.HAS_ALWAYS) {
                    return true;
                }
                if (state == iPartsVirtualAssembly.SubAssemblyState.HAS_NEVER) {
                    return false;
                }
                // Bei einem MBS/CTT Baumuster die Subknoten nur laden, wenn sie auch wirklich ausgewählt wurden
                if (iPartsVirtualNode.isMBSConstNode(getVirtualNodesPath())) {
                    Map<String, Set<String>> userFilterValues = SessionKeyHelper.getSelectedMBSModelMapWithUserSettingsCheck(getEtkProject());
                    if (Utils.isValid(userFilterValues)) {
                        // Fahrzeug BM nur laden, wenn welche ausgewählt wurden
                        if (iPartsVirtualNode.isMBSVehicleConstNode(getVirtualNodesPath()) && userFilterValues.containsKey(MODEL_NUMBER_PREFIX_CAR)) {
                            return true;
                        }
                        // Aggregate BM nur laden, wenn welche ausgewählt wurden
                        if (iPartsVirtualNode.isMBSAggConstNode(getVirtualNodesPath()) && userFilterValues.containsKey(MODEL_NUMBER_PREFIX_AGGREGATE)) {
                            return true;
                        }
                    }
                    return false;
                } else if (iPartsVirtualNode.isCTTConstNode(getVirtualNodesPath())) {
                    Map<String, Set<String>> userFilterValues = SessionKeyHelper.getSelectedCTTModelMapWithUserSettingsCheck(getEtkProject());
                    if (Utils.isValid(userFilterValues)) {
                        // Fahrzeug BM nur laden, wenn welche ausgewählt wurden
                        if (iPartsVirtualNode.isCTTVehicleConstNode(getVirtualNodesPath()) && userFilterValues.containsKey(MODEL_NUMBER_PREFIX_CAR)) {
                            return true;
                        }
                        // Aggregate BM nur laden, wenn welche ausgewählt wurden
                        if (iPartsVirtualNode.isCTTAggConstNode(getVirtualNodesPath()) && userFilterValues.containsKey(MODEL_NUMBER_PREFIX_AGGREGATE)) {
                            return true;
                        }
                    }
                    return false;
                }
                // Default
            } else {
                Logger.getLogger().throwRuntimeException(getAsId().toString() + " is not a valid virtual node ID.");
            }
        } else if (EditModuleHelper.isCarPerspectiveAssembly(getEbeneName())) {
            return false;
        }

        // Default
        return super.hasSubAssemblies(filtered);
    }

    @Override
    public EtkDataPartListEntry getHiddenSingleSubAssembly(EtkEbenenDaten partListType) {
        // für CarPerspectiveAssembly nichts zurückliefern, damit im Baum die Baugruppen nicht
        // unterhalb des CarPerspectiveAssemblies dargestellt werden
        if (partListType == null) {
            partListType = getEbene();
        }
        if (EditModuleHelper.isCarPerspectiveAssembly(partListType.getName())) {
            return null;
        }
        return super.getHiddenSingleSubAssembly(partListType);
    }

    @Override
    protected synchronized DBDataObjectList<EtkDataPartListEntry> getSubAssemblyOrPartListEntries(EtkEbenenDaten partlistType) {
        DBDataObjectList<EtkDataPartListEntry> result = super.getSubAssemblyOrPartListEntries(partlistType);
        // für CarPerspectiveAssembly nichts zurückliefern, damit im Baum die Baugruppen nicht
        // unterhalb des CarPerspectiveAssemblies dargestellt werden
        if (!result.isEmpty() && EditModuleHelper.isCarPerspectiveAssembly(getEbeneName())) {
            result = new DBDataObjectList<>();
        }
        return result;
    }

    @Override
    protected DBDataObjectAttributes internalLoad(IdWithType id, String[] resultFields) {
        if (!(id instanceof iPartsAssemblyId)) {
            throw new RuntimeException("iPartsDataAssembly.internalLoad(): ID must be an instance of iPartsAssemblyId");
        }

        iPartsAssemblyId assemblyId = (iPartsAssemblyId)id;
        if (!assemblyId.isVirtual()) {
            return super.internalLoad(id, resultFields);
        } else {
            iPartsVirtualAssembly virtualAssembly = iPartsVirtualAssemblyFactory.createVirtualAssembly(getEtkProject(),
                                                                                                       getVirtualNodesPath(),
                                                                                                       assemblyId);

            if (virtualAssembly != null) {
                return virtualAssembly.loadAssemblyHeadAttributes(resultFields);
            } else {
                Logger.getLogger().throwRuntimeException(id.toString() + " is not a valid virtual node ID.");
                return null;
            }
        }
    }

    /**
     * Lädt den Dokumentationstyp des Moduls aus der DB.
     *
     * @return
     */
    private void loadDocumentationType() {
        if (getAsId().isVirtual()) {
            documentationType = iPartsDocumentationType.UNKNOWN;
            return;
        }

        iPartsDataModule dataModule = getModuleMetaData();
        if (dataModule.existsInDB()) {
            this.documentationType = dataModule.getDocumentationType();
        } else if (getEtkProject().getEtkDbs().getActive()) {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), getAsId());
            if (assembly.existsInDB()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not load documentation type because module meta data for \""
                                                                          + getAsId().getKVari() + "\" does not exist in the database.");
            }
        }
        // Fallback auf UNKNOWN Dokumentationstyp
        if (documentationType == null) {
            documentationType = iPartsDocumentationType.UNKNOWN;
        }
    }

    public iPartsDocumentationType getDocumentationType() {
        if (documentationType == null) {
            loadDocumentationType();
        }
        return documentationType;
    }

    public void setDocumentationType(iPartsDocumentationType documentationType) {
        this.documentationType = documentationType;
    }

    public List<iPartsVirtualNode> getVirtualNodesPath() {
        if ((virtualNodesPath == null) && getAsId().isVirtual()) {
            virtualNodesPath = iPartsVirtualNode.parseVirtualIds(getAsId());
        }

        return virtualNodesPath;
    }

    /**
     * Liefert die erste {@link AssemblyId} der darüberliegenden Stückliste auf Basis ihrer Stücklisteneinträge zurück.
     *
     * @return
     */
    public AssemblyId getFirstParentAssemblyIdFromParentEntries() {
        AssemblyId parentAssemblyId = null;
        List<EtkDataPartListEntry> parentAssemblies = getParentAssemblyEntries(false);
        if (!parentAssemblies.isEmpty()) {
            // Erste Vater-Baugruppe zurückliefern (z.B. haben HM/M/SM-Knoten nur eine Vater-Baugruppe)
            parentAssemblyId = parentAssemblies.get(0).getOwnerAssemblyId();
        }
        return parentAssemblyId;
    }

    @Override
    public List<EtkDataPartListEntry> getParentAssemblyEntries(boolean filtered) {
        if (!getAsId().isVirtual()) {
            List<EtkDataPartListEntry> result = new DwList<>();

            // Klammer für eine Pseudo-Transaktion, damit nicht mehrere Pseudo-Transaktionen stattfinden (falls notwendig)
            startPseudoTransactionForActiveChangeSet(true);
            try {
                // Normale Assemblies können im iParts unter den virtuellen Strukturen EinPAS und KG/TU sein
                // Deshalb müssen diese Verwendungen hier auch ermittelt werden.
                iPartsVirtualAssemblyEinPasBase.addParentAssemblyEntriesForRetailStructures(getAsId(), filtered, getEtkProject(), result);

                // Suche in KG/SA nur dann, wenn die Assembly nicht bereits in EinPAS bzw. KG/TU verortet ist (beides gleichzeitig
                // kann nicht sein)
                if (result.isEmpty()) {
                    // Normale Assemblies können im iParts unter der virtuellen Struktur KG/SA in einer SA sein
                    // Deshalb müssen diese Verwendungen hier auch ermittelt werden.
                    iPartsVirtualAssemblyProductKgTu.addParentAssemblyEntriesForKgSaStruct(getAsId(), filtered, getEtkProject(), result);
                }

                // Sollte die Assembly in keiner virtuellen Struktur eingehängt sein, jetzt die normale Methode zur Bestimmung
                // der ParentAssemblyEntries aufrufen, da es sich vielleicht um eine reguläre Katalog-Struktur handelt (haben
                // wir bisher in iParts aber eigentlich nicht)
                if (result.isEmpty()) {
                    result = super.getParentAssemblyEntries(filtered);
                }
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }

            return result;
        } else {
            iPartsVirtualAssembly virtualAssembly = iPartsVirtualAssemblyFactory.createVirtualAssembly(getEtkProject(), getVirtualNodesPath(), getAsId());

            if (virtualAssembly != null) {
                List<EtkDataPartListEntry> result = new ArrayList<>();
                virtualAssembly.getParentAssemblyEntries(filtered, result);
                return result;
            } else {
                Logger.getLogger().throwRuntimeException(getAsId().toString() + " is not a valid virtual node ID.");
                return null;
            }
        }
    }

    /**
     * Noch eine weitere Methode um eine neue iPartsAssebly zu erzeugen.
     * Für FIELD_DM_VARIANTS_VISIBLE wird der Default mitgegeben.
     * <p>
     * Voraussetzung: setPKValues sind gesetzt und es ist initAttributesWithEmptyValues gelaufen
     *
     * @param moduleType
     * @param name
     * @param productId         Kann {@code null} sein, dann ist es ein freischwebendes Modul (SA)
     * @param saModulesId       Nur bei freischwebenden SA-Modulen vorhanden
     * @param isOrderable
     * @param documentationType
     * @param checkIfExistsInDB Flag, ob sicherheitshalber überprüft werden soll, ob ein Datensatz für die iPartsAssembly
     *                          und deren Verortung bereits in der Datenbank existiert
     * @param techChangeSet     Optionales technisches {@link iPartsRevisionChangeSet} zum zusätzlichen Abspeichern
     *                          der Daten neben dem Speichern direkt in der DB, wobei ein aktives Edit-{@link iPartsRevisionChangeSet}
     *                          Vorrang hat
     * @return
     */
    public boolean create_iPartsAssembly(iPartsModuleTypes moduleType, EtkMultiSprache name, iPartsProductId productId,
                                         iPartsSAModulesId saModulesId, boolean isOrderable, iPartsDocumentationType documentationType,
                                         boolean moduleIsSpringFilterRelevant, DCAggregateTypes aggTypeForSpecialZBFilter,
                                         boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        return create_iPartsAssembly(moduleType, name, productId, saModulesId, isOrderable, documentationType,
                                     moduleIsSpringFilterRelevant, aggTypeForSpecialZBFilter,
                                     iPartsDataModule.DM_VARIANTS_VISIBLE_DEFAULT, checkIfExistsInDB, techChangeSet);
    }

    /**
     * Erzeugen eines neuen iPartsAssemblies
     * Voraussetzung: setPKValues sind gesetzt und es ist initAttributesWithEmptyValues gelaufen
     *
     * @param moduleType
     * @param name
     * @param productId                 Kann {@code null} sein, dann ist es ein freischwebendes Modul (SA)
     * @param saModulesId               Nur bei freischwebenden SA-Modulen vorhanden
     * @param isOrderable
     * @param documentationType
     * @param isSpringFilterRelevant    Flag, ob das Modul relevant ist für den Feder-Filter.
     * @param aggTypeForSpecialZBFilter Aggregattyp, der relevant ist für den speziellen ZB Sachnummern Filter.
     * @param isVariantsVisible         Flag, ob Farbvarianten angezeigt werden soll
     * @param checkIfExistsInDB         Flag, ob sicherheitshalber überprüft werden soll, ob ein Datensatz für die iPartsAssembly
     *                                  und deren Verortung bereits in der Datenbank existiert
     * @param techChangeSet             Optionales technisches {@link iPartsRevisionChangeSet} zum zusätzlichen Abspeichern
     *                                  der Daten neben dem Speichern direkt in der DB, wobei ein aktives Edit-{@link iPartsRevisionChangeSet}
     *                                  Vorrang hat
     * @return
     */
    public boolean create_iPartsAssembly(iPartsModuleTypes moduleType, EtkMultiSprache name, iPartsProductId productId,
                                         iPartsSAModulesId saModulesId, boolean isOrderable, iPartsDocumentationType documentationType,
                                         boolean isSpringFilterRelevant, DCAggregateTypes aggTypeForSpecialZBFilter,
                                         boolean isVariantsVisible, boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        List<String> enumAutoValues = new DwList<>();
        if (isSpringFilterRelevant) {
            enumAutoValues.add(KGTUAutosetTypes.FF.getDbValue());
        }
        if (isVariantsVisible) {
            enumAutoValues.add(KGTUAutosetTypes.VA.getDbValue());
        }
        return create_iPartsAssembly(moduleType, name, productId, saModulesId, isOrderable, documentationType,
                                     aggTypeForSpecialZBFilter, checkIfExistsInDB, enumAutoValues, techChangeSet);
    }

    /**
     * Erzeugen eines neuen iPartsAssemblies
     * Voraussetzung: setPKValues sind gesetzt und es ist initAttributesWithEmptyValues gelaufen
     *
     * @param moduleType
     * @param name
     * @param productId                 Kann {@code null} sein, dann ist es ein freischwebendes Modul (SA)
     * @param saModulesId               Nur bei freischwebenden SA-Modulen vorhanden
     * @param isOrderable
     * @param documentationType
     * @param aggTypeForSpecialZBFilter Aggregattyp, der relevant ist für den speziellen ZB Sachnummern Filter.
     * @param checkIfExistsInDB         Flag, ob sicherheitshalber überprüft werden soll, ob ein Datensatz für die iPartsAssembly
     *                                  und deren Verortung bereits in der Datenbank existiert
     * @param enumAutoValues            Liste von Enum-DBValues, welche Werte in den TU-Metadaten gesetzt werden sollen
     * @param techChangeSet             Optionales technisches {@link iPartsRevisionChangeSet} zum zusätzlichen Abspeichern
     *                                  der Daten neben dem Speichern direkt in der DB, wobei ein aktives Edit-{@link iPartsRevisionChangeSet}
     *                                  Vorrang hat
     * @return
     */
    public boolean create_iPartsAssembly(iPartsModuleTypes moduleType, EtkMultiSprache name, iPartsProductId productId,
                                         iPartsSAModulesId saModulesId, boolean isOrderable, iPartsDocumentationType documentationType,
                                         DCAggregateTypes aggTypeForSpecialZBFilter, boolean checkIfExistsInDB, List<String> enumAutoValues,
                                         iPartsRevisionChangeSet techChangeSet) {
        boolean result = !getAsId().isVirtual();
        if (result) {
            //Kopfdatensatz bilden
            String kVari = getAsId().getKVari();
            setAttributeValue(EtkDbConst.FIELD_K_EBENE, moduleType.getDbValue(), DBActionOrigin.FROM_EDIT);
            setFieldValueAsBoolean(EtkDbConst.FIELD_K_BESTFLAG, isOrderable, DBActionOrigin.FROM_EDIT);

            //Material besetzen
            part.setAttributeValue(EtkDbConst.FIELD_M_MATNR, kVari, DBActionOrigin.FROM_EDIT);
            part.setAttributeValue(EtkDbConst.FIELD_M_VER, getAsId().getKVer(), DBActionOrigin.FROM_EDIT);
            part.updateOldId();

            if (!checkIfExistsInDB || !part.existsInDB()) {
                part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }

            part.setAttributeValue(EtkDbConst.FIELD_M_BESTNR, kVari, DBActionOrigin.FROM_EDIT);
            part.setFieldValueAsBoolean(iPartsConst.FIELD_M_ASSEMBLY, true, DBActionOrigin.FROM_EDIT); // Flag für Baugruppe am Materialstamm setzen

            //Materialname besetzen
            if (name != null) {
                part.setTextNrForMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, name.getTextId().isEmpty() ? kVari : name.getTextId(),
                                               DBActionOrigin.FROM_EDIT);
                String textNr;
                if (name.getTextId().isEmpty()) {
                    textNr = getEtkProject().getDbLayer().getNewTextNr(TableAndFieldName.make(tableName, EtkDbConst.FIELD_M_TEXTNR),
                                                                       getAsId().toString("|"));
                } else {
                    textNr = name.getTextId();
                }
                part.setTextNrForMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, textNr, DBActionOrigin.FROM_EDIT);

                part.setFieldValueAsMultiLanguage(EtkDbConst.FIELD_M_TEXTNR, name, DBActionOrigin.FROM_EDIT);
            }

            //Eintrag in DA_MODULE Tabelle vorbereiten
            iPartsDataModule moduleEntry = new iPartsDataModule(getEtkProject(), new iPartsModuleId(kVari));
            moduleEntry.initWithDefaultValues();
            result = true;
            // Dokumentationstyp setzen
            if (result && (documentationType != iPartsDocumentationType.UNKNOWN)) {
                moduleEntry.setDocuType(documentationType, DBActionOrigin.FROM_EDIT);
            }

            if (result) {
                moduleEntry.setFieldValue(FIELD_DM_ZB_PART_NO_AGG_TYPE, DCAggregateTypes.getDBAggregateTypeByDCAggregateType(aggTypeForSpecialZBFilter), DBActionOrigin.FROM_EDIT);
                if (Utils.isValid(enumAutoValues)) {
                    for (String enumValue : enumAutoValues) {
                        KGTUAutosetTypes autosetType = KGTUAutosetTypes.getType(enumValue);
                        if (autosetType != KGTUAutosetTypes.UNKNOWN) {
                            moduleEntry.setFieldValueAsBoolean(autosetType.getDestFieldName(), true, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }

            iPartsDataProductModules productModulesEntry = null;
            if (result && (productId != null) && productId.isValidId()) {
                //Eintrag in DA_PRODUCT_MODULES Tabelle vorbereiten; bei freischwebenden Modulen (SA) ist die productId null,
                // weil es keine direkte Verbindung zum Produkt gibt
                productModulesEntry = getProductModuleEntry(productId);
                result &= (productModulesEntry != null);
            }

            iPartsDataSAModules saModulesEntry = null;
            if (result && (saModulesId != null) && saModulesId.isValidId()) {
                // Eintrag in DA_SA_MODULES (nur bei freischwebenden SA-Modulen)
                saModulesEntry = getSAModulesEntry(saModulesId);
                result &= (saModulesEntry != null);
            }

            if (result) {
                boolean revisionChangeSetActiveForEdit = isRevisionChangeSetActiveForEdit();
                if (revisionChangeSetActiveForEdit || (techChangeSet != null)) {
                    // Temporäre EtkDataObjectList verwenden, um die Änderungen am ChangeSet zu bündeln
                    EtkDataObjectList tempDataObjectList = new GenericEtkDataObjectList();

                    if (productModulesEntry != null) {
                        tempDataObjectList.add(productModulesEntry, DBActionOrigin.FROM_EDIT);
                    }
                    if (saModulesEntry != null) {
                        tempDataObjectList.add(saModulesEntry, DBActionOrigin.FROM_EDIT);
                    }
                    tempDataObjectList.add(moduleEntry, DBActionOrigin.FROM_EDIT);
                    tempDataObjectList.add(this, DBActionOrigin.FROM_EDIT);

                    if (revisionChangeSetActiveForEdit) {
                        getRevisionsHelper().addDataObjectListToActiveChangeSetForEdit(tempDataObjectList);
                    } else {
                        techChangeSet.addDataObjectList(tempDataObjectList, false, false);
                    }
                }
                if (!revisionChangeSetActiveForEdit) {
                    // alles speichern
                    if (productModulesEntry != null) {
                        productModulesEntry.saveToDB(checkIfExistsInDB, PrimaryKeyExistsInDB.CHECK);
                    }
                    if (saModulesEntry != null) {
                        saModulesEntry.saveToDB(checkIfExistsInDB, PrimaryKeyExistsInDB.CHECK);
                    }
                    moduleEntry.saveToDB(checkIfExistsInDB, PrimaryKeyExistsInDB.CHECK);
                    super.saveToDB(checkIfExistsInDB, PrimaryKeyExistsInDB.CHECK);
                }

                // Modul-Benennung und Modul-Metadaten direkt im Cache setzen
                Object hashObject = createModuleMetaDataHashObject(getEtkProject(), getAsId());
                if (name != null) {
                    synchronized (assemblyTextsCache) {
                        assemblyTextsCache.put(hashObject, name);
                    }
                } else {
                    removeAssemblyMetaDataFromCache(getEtkProject(), getAsId());
                }

                synchronized (dataModulesCache) {
                    dataModulesCache.put(hashObject, moduleEntry);
                }
                if ((productId != null) && productId.isValidId()) {
                    synchronized (productIdFromModuleUsageCache) {
                        productIdFromModuleUsageCache.put(hashObject, productId);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Löscht dieses Modul aus der DB inkl. aller referenzierten Datensätze wie Fußnoten. Optional können auch alle Verwendungen
     * des Moduls gelöscht werden (außer bei Importern sollte dies auch immer gemacht werden).
     *
     * @param deleteModuleUsages Auch alle Verwendungen des Moduls löschen?
     * @return
     */
    public boolean delete_iPartsAssembly(boolean deleteModuleUsages) {
        return delete_iPartsAssembly(deleteModuleUsages, false);
    }

    /**
     * Löscht dieses Modul aus der DB inkl. aller referenzierten Datensätze wie Fußnoten. Optional können auch alle Verwendungen
     * des Moduls gelöscht werden (außer bei Importern sollte dies auch immer gemacht werden).
     *
     * @param deleteModuleUsages         Auch alle Verwendungen des Moduls löschen?
     * @param calledWhileDeletingProduct Wird dieses Modul gerade während dem Löschen eines kompletten Produkts gelöscht?
     * @return
     */
    public boolean delete_iPartsAssembly(boolean deleteModuleUsages, boolean calledWhileDeletingProduct) {
        boolean result = !getAsId().isVirtual();
        if (result) {
            EtkDbObjectsLayer dbLayer = getEtkProject().getDbLayer();
            dbLayer.startTransaction();
            dbLayer.startBatchStatement();
            try {
                if (deleteModuleUsages) {
                    // alle Verwendungen des Moduls in Produkten löschen
                    dbLayer.delete(TABLE_DA_PRODUCT_MODULES, new String[]{ FIELD_DPM_MODULE_NO }, new String[]{ getAsId().getKVari() });

                    // alle Verortungen des Moduls löschen
                    dbLayer.delete(TABLE_DA_MODULES_EINPAS, new String[]{ FIELD_DME_MODULE_NO }, new String[]{ getAsId().getKVari() });

                    // Die Bildaufträge des Moduls löschen
                    dbLayer.delete(TABLE_DA_PICORDER_MODULES, new String[]{ FIELD_DA_POM_MODULE_NO }, new String[]{ getAsId().getKVari() });

                    // Eintrag in DA_MODULES Tabelle löschen
                    iPartsDataModule moduleEntry = getModuleMetaData();
                    moduleEntry.deleteFromDB(true);
                }

                // Während ein komplettes Produkt gelöscht wird, können keine SA-Module vorkommen und die Werkseinsatzdaten
                // werden bereits in iPartsDataProduct gelöscht
                if (!calledWhileDeletingProduct) {
                    iPartsDataSAModulesList dataSAModulesList = iPartsDataSAModulesList.loadDataForModule(getEtkProject(), new iPartsModuleId(getAsId().getKVari()));
                    if (!dataSAModulesList.isEmpty()) { // Es handelt sich um ein SA-Modul
                        // Es dürfte eigentlich nur einen Eintrag geben, aber sicher ist sicher
                        for (iPartsDataSAModules dataSAModule : dataSAModulesList) {
                            String saNumber = dataSAModule.getAsId().getSaNumber();

                            // ELDAS Fußnoten löschen
                            iPartsDataFootNoteList dataFootNoteList = new iPartsDataFootNoteList();
                            dataFootNoteList.loadELDASFootNoteListForProductOrSAFromDB(getEtkProject(), saNumber);
                            dataFootNoteList.deleteFromDB(getEtkProject(), true);

                            // EPC Fußnoten löschen
                            dataFootNoteList = new iPartsDataFootNoteList();
                            dataFootNoteList.loadEPCFootNoteListForProductOrSAFromDB(getEtkProject(), saNumber);
                            dataFootNoteList.deleteFromDB(getEtkProject(), true);

                            // SAA-Fußnoten-Referenzen löschen (eigentlich nur bei ELDAS-SA- und EPC-SA-Modulen notwendig)
                            iPartsDataFootNoteSaaRefList dataFootNoteSaaRefList = new iPartsDataFootNoteSaaRefList();
                            dataFootNoteSaaRefList.loadFootNotesForAllSaasOfSaFromDB(getEtkProject(), saNumber);
                            dataFootNoteSaaRefList.deleteFromDB(getEtkProject(), true);

                            // ELDAS Rückmeldedaten löschen
                            iPartsDataResponseDataList dataResponseDataList = new iPartsDataResponseDataList();
                            dataResponseDataList.loadELDASResponseDataListForProductOrSAFromDB(getEtkProject(), saNumber);
                            dataResponseDataList.deleteFromDB(getEtkProject(), true);

                            // EPC Rückmeldedaten löschen
                            dataResponseDataList = new iPartsDataResponseDataList();
                            dataResponseDataList.loadEPCResponseDataListForProductOrSAFromDB(getEtkProject(), saNumber);
                            dataResponseDataList.deleteFromDB(getEtkProject(), true);

                        }

                        // Einträge in DA_SA_MODULES löschen
                        dataSAModulesList.deleteFromDB(getEtkProject(), true);
                    }

                    if (!getDocumentationType().isPKWDocumentationType()) {
                        // Werkseinsatzdaten löschen (eigentlich nur bei ELDAS-und EPC-Modulen notwendig; Pattern für ELDAS bzw. EPC GUID ist kVari_*)
                        iPartsDataFactoryDataList dataFactoryDataList = new iPartsDataFactoryDataList();
                        dataFactoryDataList.loadELDASAndEPCFactoryDataListForProductOrSAFromDB(getEtkProject(), getAsId().getKVari());
                        dataFactoryDataList.deleteFromDB(getEtkProject(), true);
                    }
                }

                // Referenzen des Moduls in DA_COMB_TEXT löschen
                iPartsDataCombTextList.deleteCombTextsForAssembly(getEtkProject(), getAsId());

                // Referenzen des Moduls in DA_FN_KATALOG_REF löschen
                iPartsDataFootNoteCatalogueRefList.deleteFootNotesForAssembly(getEtkProject(), getAsId());

                // Ersetzungen und Mitlieferteile des Moduls löschen
                iPartsDataReplacePartList.deleteReplacementsForAssembly(getEtkProject(), getAsId());
                iPartsDataIncludePartList.deleteIncludePartsForAssembly(getEtkProject(), getAsId());

                // Modul selbst löschen (dazu vorher die Stückliste und die Zeichnungen explizit anfordern, damit diese als
                // Kind-Elemente geladen sind)
                // Es müssen alle Spalten geladen werden, damit die Arrayfelder und Mehrsprachigen Felder mitgelöscht werden

                //TODO JAVA_VIEWER-3388

                getPartListUnfiltered(null, true, false);
                getImages();
                super.deleteFromDB(true);
                clear(DBActionOrigin.FROM_EDIT);
                dbLayer.endBatchStatement();
                dbLayer.commit();
            } catch (RuntimeException e) {
                dbLayer.cancelBatchStatement();
                dbLayer.rollback();
                throw e;
            }
        }
        return result;
    }

    @Override
    public EtkDataObjectList deleteReferencedData(List<? extends EtkDataObject> otherDataObjectsToBeDeleted) {
        if ((getRevisionsHelper() != null) && getRevisionsHelper().isRevisionChangeSetActiveForEdit()) {
            GenericEtkDataObjectList dataObjectsToBeDeleted = new GenericEtkDataObjectList();

            // Stücklisteneinträge inkl. der dort referenzierten Daten
            DBDataObjectList<EtkDataPartListEntry> partListUnfiltered = getPartListUnfiltered(null);
            EtkProject project = getEtkProject();
            List<EtkDataPartListEntry> partListEntriesToBeDeleted = partListUnfiltered.getAsList();
            for (EtkDataPartListEntry partListEntry : partListEntriesToBeDeleted) {
                if (partListEntry instanceof iPartsDataPartListEntry) {
                    ((iPartsDataPartListEntry)partListEntry).collectPreloadedReferencedDataForDelete(dataObjectsToBeDeleted,
                                                                                                     true, partListEntriesToBeDeleted);
                }
                dataObjectsToBeDeleted.delete(partListEntry, true, DBActionOrigin.FROM_EDIT);

                // Evtl. vorhandene PK-Reservierung auch löschen
                iPartsDataReservedPKList.deleteReservedPrimaryKey(project, partListEntry.getAsId());
            }

            // Liste der Stücklisteneinträge leeren, da diese ja nun als gelöscht markiert wurden und nicht mehr weiter behandelt werden sollen
            partListUnfiltered.clear(DBActionOrigin.FROM_DB);

            // ELDAS Werkseinsatzdaten und kombinierte Texte hängen zwar eigentlich auch am Stücklisteneintrag, werden hier aber
            // als Batch gelöscht, um Nachladen und Pseudo-Transaktionen zu verhindern

            if (!getDocumentationType().isPKWDocumentationType()) {
                // Werkseinsatzdaten löschen (eigentlich nur bei ELDAS-und EPC-Modulen notwendig; Pattern für ELDAS bzw. EPC GUID ist kVari_*)
                iPartsDataFactoryDataList dataFactoryDataList = new iPartsDataFactoryDataList();
                dataFactoryDataList.loadELDASAndEPCFactoryDataListForProductOrSAFromDB(project, getAsId().getKVari());
                dataFactoryDataList.deleteAll(DBActionOrigin.FROM_EDIT);
                dataObjectsToBeDeleted.addAll(dataFactoryDataList, DBActionOrigin.FROM_EDIT);
            }

            // Referenzen des Moduls in DA_COMB_TEXT löschen
            iPartsDataCombTextList combTextsForAssembly = iPartsDataCombTextList.loadForModule(getAsId(), project);
            combTextsForAssembly.deleteAll(DBActionOrigin.FROM_EDIT);
            dataObjectsToBeDeleted.addAll(combTextsForAssembly, DBActionOrigin.FROM_EDIT);

            // DA_MODULE
            iPartsDataModuleList dataModuleList = iPartsDataModuleList.loadDataForModuleNumber(project, getAsId().getKVari());
            for (iPartsDataModule dataModule : dataModuleList) {
                dataObjectsToBeDeleted.delete(dataModule, true, DBActionOrigin.FROM_EDIT);
            }

            // DA_PRODUCT_MODULES
            iPartsDataProductModulesList dataProductModulesList = iPartsDataProductModulesList.loadDataProductModulesList(project, getAsId());
            for (iPartsDataProductModules dataProductModules : dataProductModulesList) {
                dataObjectsToBeDeleted.delete(dataProductModules, true, DBActionOrigin.FROM_EDIT);
            }

            // DA_MODULES_EINPAS
            iPartsDataModuleEinPASList dataModuleEinPASList = iPartsDataModuleEinPASList.loadForModule(project, getAsId());
            for (iPartsDataModuleEinPAS dataModuleEinPAS : dataModuleEinPASList) {
                dataObjectsToBeDeleted.delete(dataModuleEinPAS, true, DBActionOrigin.FROM_EDIT);

                // nicht mehr benötigte KG/TUs entfernen
                String kg = dataModuleEinPAS.getFieldValue(FIELD_DME_SOURCE_KG);
                String tu = dataModuleEinPAS.getFieldValue(FIELD_DME_SOURCE_TU);
                String productNumber = dataModuleEinPAS.getAsId().getProductNumber();
                String moduleNumber = dataModuleEinPAS.getAsId().getModuleNumber();

                if (StrUtils.isValid(kg, productNumber, moduleNumber)) {
                    iPartsProductId productId = new iPartsProductId(productNumber);

                    iPartsDataModuleEinPASList moduleList = iPartsDataModuleEinPASList.loadForKgTu(project, productId, new KgTuId(kg, tu));
                    if (moduleList.size() == 1) {
                        String foundModuleNumber = moduleList.get(0).getAsId().getModuleNumber();
                        if (foundModuleNumber.equals(moduleNumber)) {
                            // Dieses Modul ist das einzige in dem TU => TU entfernen
                            iPartsDataKgTuAfterSales kgTuAs = new iPartsDataKgTuAfterSales(project, new iPartsDataKgTuAfterSalesId(productNumber, kg, tu));
                            if ((getRevisionsHelper() != null) && getRevisionsHelper().checkIfObjectCreatedInActiveEditChangeSet(kgTuAs.getAsId())) {
                                dataObjectsToBeDeleted.delete(kgTuAs, true, DBActionOrigin.FROM_EDIT);
                            }
                            // KG-Id vom KG Eintrag, der eventuell gelöscht werden soll
                            iPartsDataKgTuAfterSales kgAs = new iPartsDataKgTuAfterSales(project, new iPartsDataKgTuAfterSalesId(productNumber, kg, ""));
                            // KG darf auch nur gelöscht werden, wenn kein Edit aktiv ist (Importer) oder die KG im aktuellen Autorenauftrag erzeugt wurde
                            if ((getRevisionsHelper() != null) && getRevisionsHelper().checkIfObjectCreatedInActiveEditChangeSet(kgAs.getAsId())) {
                                // Prüfen ob der TU der einzige in der KG war
                                KgTuForProduct kgTuForProduct = KgTuForProduct.getInstance(project, productId);
                                Collection<KgTuNode> tuNodeList = kgTuForProduct.getTuNodeList(kg);

                                // Prüfen, ob es den KG-Knoten überhaupt gibt (eigentlich Fehler, wenn nicht)
                                if (tuNodeList != null) {
                                    if (tuNodeList.size() < 2) {
                                        boolean deleteKG = false;
                                        if (tuNodeList.isEmpty()) {
                                            deleteKG = true;
                                        } else if ((tuNodeList.size() == 1) && StrUtils.isValid(tu)) {
                                            if (tuNodeList.iterator().next().getId().getTu().equals(tu)) {
                                                deleteKG = true;
                                            }
                                        }
                                        if (deleteKG) {
                                            // diese TU war die einzige in der KG => KG löschen
                                            dataObjectsToBeDeleted.delete(kgAs, true, DBActionOrigin.FROM_EDIT);
                                        }
                                    }

                                    // Cache für KG/TU Benennungen für dieses Produkt löschen
                                    KgTuForProduct.removeKgTuForProductFromCache(project, productId);
                                }
                            }
                        }
                    }
                }
            }

            // Die Bildaufträge des Moduls löschen
            iPartsDataPicOrderModulesList picOrderModules = iPartsDataPicOrderModulesList.loadPicOrderModulesListForModule(project, getAsId().getKVari());
            for (iPartsDataPicOrderModules picOrderModule : picOrderModules) {
                dataObjectsToBeDeleted.delete(picOrderModule, true, DBActionOrigin.FROM_EDIT);
            }

            // SA-Module können aktuell nicht angelegt und auch nicht gelöscht werden
            // DA_SA_MODULES
            iPartsDataSAModulesList dataSAModulesList = iPartsDataSAModulesList.loadDataForModule(project, new iPartsModuleId(getAsId().getKVari()));
            for (iPartsDataSAModules dataSAModules : dataSAModulesList) {
                // TODO Fußnoten und Rückmeldedaten für ELDAS-SA-Module löschen analog zu delete_iPartsAssembly()

                dataObjectsToBeDeleted.delete(dataSAModules, true, DBActionOrigin.FROM_EDIT);
            }

            if (!dataObjectsToBeDeleted.isEmptyIncludingDeletedList()) {
                return dataObjectsToBeDeleted;
            }
        }

        return null;
    }

    /**
     * Liefert das {@link iPartsDataModule} für dieses Modul aus der Tabelle {@code DA_MODULE} zurück
     * {@code DA_MODULE} enthält Zusatz-Metadaten zum Modul.
     *
     * @return
     */
    public iPartsDataModule getModuleMetaData() {
        if (cachedDataModule == null) { // Lokales Feld in dieser Instanz für noch schnelleren Zugriff
            // iPartsDataModule im globalen Cache suchen bzw. aus der DB laden und in den globalen Cache legen
            Object hashObject = createModuleMetaDataHashObject(getEtkProject(), getAsId());
            iPartsDataModule dataModuleFromCache;
            synchronized (dataModulesCache) {
                dataModuleFromCache = dataModulesCache.get(hashObject);
                if (dataModuleFromCache == null) {
                    iPartsModuleId moduleId = new iPartsModuleId(getAsId().getKVari());
                    dataModuleFromCache = new iPartsDataModule(getEtkProject(), moduleId);

                    // Nur dann in den Cache legen, wenn der Datensatz auch in der DB existiert
                    if (dataModuleFromCache.loadFromDB(dataModuleFromCache.getAsId())) {
                        dataModulesCache.put(hashObject, dataModuleFromCache);
                    }
                }
            }

            // dataModuleFromCache aus dem Cache unbedingt klonen, da ansonsten das EtkProject nicht stimmt!
            cachedDataModule = dataModuleFromCache.cloneMe(getEtkProject());
        }
        return cachedDataModule;
    }

    private iPartsDataProductModules getProductModuleEntry(iPartsProductId productId) {
        iPartsProductModulesId productModulesId = new iPartsProductModulesId(productId.getProductNumber(), getAsId().getKVari());
        iPartsDataProductModules productModuleEntry = new iPartsDataProductModules(getEtkProject(), productModulesId);
        productModuleEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        return productModuleEntry;
    }

    private iPartsDataSAModules getSAModulesEntry(iPartsSAModulesId saModulesId) {
        iPartsDataSAModules saModulesEntry = new iPartsDataSAModules(getEtkProject(), saModulesId);
        saModulesEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        saModulesEntry.setFieldValue(iPartsConst.FIELD_DSM_MODULE_NO, getAsId().getKVari(), DBActionOrigin.FROM_EDIT);
        return saModulesEntry;
    }

    /**
     * Tested, ob dieses Modul von einem Aggregat stammt und ermittelt die Baumuster, die in diesem Aggregateprodukt enthalten sind
     * Der Rückgabewert wird als String formatiert, so dass er in der Überschrift angezeigt werden kann
     *
     * @param navPath
     * @return
     */
    public String getAggNoForHeading(NavigationPath navPath) {
        // Fahrzeugprodukt anhand des Navigationspfades ermitteln
        iPartsProductId productId = getProductIdFromNavPath(navPath);
        if (productId != null) {
            iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), productId);
            // Die Aggregateprodukte dieses Fahrzeugs ermitteln
            List<iPartsProductId> aggregatProducts = product.getAggregateProductsForModule(getEtkProject(), getAsId());


            // Die Baumuster, dieser aggregateProdukte ermitteln
            Set<String> models = new TreeSet<>();
            for (iPartsProductId aggregateId : aggregatProducts) {
                iPartsProduct aggregat = iPartsProduct.getInstance(getEtkProject(), aggregateId);
                models.addAll(aggregat.getModelNumbers(getEtkProject()));
            }

            // String für die Überschrift formatieren
            if (models.size() > 0) {
                StringBuilder result = new StringBuilder("(");
                boolean firstOne = true;
                for (String model : models) {
                    if (!firstOne) {
                        result.append(", ");
                    } else {
                        firstOne = false;
                    }
                    String modelNo = getEtkProject().getVisObject().asString(TABLE_DA_MODEL, FIELD_DM_MODEL_NO, model, getEtkProject().getDBLanguage());
                    result.append(modelNo);
                }
                result.append(") ");
                return result.toString();
            }
        }
        return null;
    }

    /**
     * Anhand des Navigationspfades das Fahrzeugprodukt ermitteln
     *
     * @param navPath
     * @return
     */
    private iPartsProductId getProductIdFromNavPath(NavigationPath navPath) {
        // In der Suche kann der NavPath auch null sein
        if (navPath != null) {
            // Den Navigationspfad nach oben durchsuchen und nach einem Fahrzeugproduktnoten suchen
            for (int i = navPath.size() - 1; i >= 0; i--) {
                if (iPartsVirtualNode.isVirtualId(navPath.get(i).getOwnerAssemblyId())) {
                    List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(navPath.get(i).getOwnerAssemblyId());

                    if (virtualNodes != null) {
                        if (iPartsVirtualNode.isProductNode(virtualNodes)) {
                            return (iPartsProductId)virtualNodes.get(0).getId();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Anhand der (ersten) Verwendung von dieser Stückliste das Produkt ermitteln
     *
     * @return {@code null} falls keine Verwendung gefunden wurde
     */
    public iPartsProductId getProductIdFromModuleUsage() {
        if (!productIdFromModuleUsageLoaded) { // Flag für lokales Feld in dieser Instanz für noch schnelleren Zugriff
            if (!getAsId().isVirtual()) {
                // iPartsProductId im globalen Cache suchen bzw. aus der DB laden und in den globalen Cache legen
                Object hashObject = createModuleMetaDataHashObject(getEtkProject(), getAsId());
                iPartsProductId productIdFromCache;
                synchronized (productIdFromModuleUsageCache) {
                    productIdFromCache = productIdFromModuleUsageCache.get(hashObject);
                    if (productIdFromCache == null) {
                        iPartsDataProductModulesList dataProductModules = iPartsDataProductModulesList.loadDataProductModulesList(getEtkProject(), getAsId());
                        if (!dataProductModules.isEmpty()) {
                            productIdFromCache = new iPartsProductId(dataProductModules.get(0).getAsId().getProductNumber());

                            // Nur dann in den Cache legen, wenn der Datensatz auch in der DB existiert
                            productIdFromModuleUsageCache.put(hashObject, productIdFromCache);
                        }
                    }
                }

                productIdFromModuleUsage = productIdFromCache;
            }
            productIdFromModuleUsageLoaded = true;
        }
        return productIdFromModuleUsage;
    }

    /**
     * Diese Funktion NICHT verwenden. Setzt die ProduktId für diese Assembly manuell. Wird nur gebraucht um bei
     * der automatischen Freigabe in einer Dummy-Assembly das Produkt zu setzen, was für das Laden der Farben wichtig ist.
     *
     * @param productId
     */
    public void setProductIdFromModuleUsage(iPartsProductId productId) {
        productIdFromModuleUsage = productId;
        productIdFromModuleUsageLoaded = true;
    }

    public void recalcSeqNrForDaimlerDIALOGImport(Map<PartListEntryId, String> sortOrderList) {
        DBDataObjectList<EtkDataPartListEntry> localEntries = getPartListUnfiltered(null, true, false);
        CacheLists cache = getCache();
        cache.clearPartListEntries();

        // Standardsortierung
        localEntries = sortDaimlerDIALOGPartList(localEntries, sortOrderList);

        setChildren(CHILDREN_NAME_PART_LIST_ENTRIES, localEntries);
        cache.setInternalPartListEntriesWithAllFields(true);
        cache.setInternalPartListEntriesFields(cache.getMustResultFieldsForPartListEntries(null, false));
    }

    private DBDataObjectList<EtkDataPartListEntry> sortDaimlerDIALOGPartList(DBDataObjectList<EtkDataPartListEntry> localEntries,
                                                                             final Map<PartListEntryId, String> sortOrderList) {
        final SortStringCache sortStringCache = new SortStringCache();
        List<EtkDataPartListEntry> tempList = localEntries.getAsList();

        Collections.sort(tempList, new Comparator<EtkDataPartListEntry>() {
            @Override
            public int compare(EtkDataPartListEntry o1, EtkDataPartListEntry o2) {
                String s1 = o1.getFieldValue(FIELD_K_POS).trim();
                String s2 = o2.getFieldValue(FIELD_K_POS).trim();

                // Spezialbehandlungen für leere Hotspots bei Stücklisteneinträgen, die nur für den Baumuster-Filter relevant
                // sind und eigentlich aus anderen Modulen kommen (diese sollen ganz hinten an die Stückliste angehängt werden)
                if (s1.isEmpty() && o1.getFieldValueAsBoolean(FIELD_K_ONLY_MODEL_FILTER)) {
                    if (!s2.isEmpty() || !o2.getFieldValueAsBoolean(FIELD_K_ONLY_MODEL_FILTER)) {
                        return 1; // s1 nach hinten (also s2 nach vorne)
                    }
                } else if (s2.isEmpty() && o2.getFieldValueAsBoolean(FIELD_K_ONLY_MODEL_FILTER)) {
                    if (!s1.isEmpty() || !o1.getFieldValueAsBoolean(FIELD_K_ONLY_MODEL_FILTER)) {
                        return -1; // s2 nach hinten (also s1 nach vorne)
                    }
                } else {
                    s1 = sortStringCache.getSortString(s1, true);
                    s2 = sortStringCache.getSortString(s2, true);
                    int result = s1.compareTo(s2);
                    if (result != 0) {
                        return result;
                    }
                }

                String c1 = sortOrderList.get(o1.getAsId());
                String c2 = sortOrderList.get(o2.getAsId());
                int result = c1.compareTo(c2);
                if (result != 0) {
                    return result;
                }

                s1 = o1.getFieldValue(FIELD_K_LFDNR);
                s2 = o2.getFieldValue(FIELD_K_LFDNR);
                return s1.compareTo(s2);
            }
        });

        int seqNr = 1;
        for (EtkDataPartListEntry partListEntry : tempList) {
            partListEntry.setFieldValueAsInteger(EtkDbConst.FIELD_K_SEQNR, seqNr, DBActionOrigin.FROM_EDIT);
            seqNr++;
        }
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();
        result.addAll(tempList, DBActionOrigin.FROM_DB);
        return result;
    }

    /**
     * Liefert alle passenden Positionsvarianten für einen Stücklisteneintrag in dieser
     * Stückliste zurück. Als Kriterium werden die BCTE-Schlüssel verglichen.
     *
     * @param removeOwnEntry {@code true} wenn der Stücklisteneintrag selbst nicht enthalten sein soll
     * @param partListEntry  Stücklisteneintrag
     * @return Alle Positionsvarianten oder leere Liste, falls keine vorhanden sind und removeOwnEntry {@code true} ist.
     * {@code null}, falls weder der Dokumentationstyp DIALOG ist noch die Assembly eine SM-Stücklisten
     * oder falls der Stücklisteneintrag nicht zur Stückliste gehört oder falls er eine ungültige DIALOG-GUID besitzt
     */
    public List<EtkDataPartListEntry> getAllPositionVariants(EtkDataPartListEntry partListEntry, boolean removeOwnEntry) {
        if (!getDocumentationType().isPKWDocumentationType() && !isDialogSMConstructionAssembly()) {
            return null;
        }
        if (!partListEntry.getOwnerAssemblyId().equals(getAsId())) {
            return null;
        }
        if (dialogPositionsHelper == null) {
            dialogPositionsHelper = new iPartsDIALOGPositionsHelper(getPartListUnfiltered(null));
        }
        List<EtkDataPartListEntry> result = dialogPositionsHelper.getAllPositionVariants(partListEntry, removeOwnEntry);
        if (result != null) {
            return Collections.unmodifiableList(result);
        }
        return result;
    }

    /**
     * Liefert alle passenden Positionsvarianten für einen Stücklisteneintrag in dieser
     * Stückliste zurück. Als Kriterium werden die BCTE-Schlüssel verglichen. Aber nur solche mit gleicher AA
     * wie der Stücklisteneintrag.
     *
     * @param removeOwnEntry {@code true} wenn der Stücklisteneintrag selbst nicht enthalten sein soll
     * @param partListEntry  Stücklisteneintrag
     * @return Alle Positionsvarianten oder leere Liste, falls keine vorhanden sind und removeOwnEntry {@code true} ist.
     * {@code null}, falls weder der Dokumentationstyp DIALOG ist noch die Assembly eine SM-Stücklisten
     * oder falls der Stücklisteneintrag nicht zur Stückliste gehört oder falls er eine ungültige DIALOG-GUID besitzt
     */
    public List<EtkDataPartListEntry> getPositionVariants(EtkDataPartListEntry partListEntry, boolean removeOwnEntry) {
        if (!getDocumentationType().isPKWDocumentationType() && !isDialogSMConstructionAssembly()) {
            return null;
        }
        if (!partListEntry.getOwnerAssemblyId().equals(getAsId())) {
            return null;
        }
        if (dialogPositionsHelper == null) {
            dialogPositionsHelper = new iPartsDIALOGPositionsHelper(getPartListUnfiltered(null));
        }
        List<EtkDataPartListEntry> result = dialogPositionsHelper.getPositionVariantsWithAACheck(partListEntry, removeOwnEntry);
        if (result != null) {
            return Collections.unmodifiableList(result);
        }
        return result;
    }

    @Override
    public void clearFilteredPartLists() {
        super.clearFilteredPartLists();
        setFilteredWithModelNumber(null); // Bisherige Baumuster-Filterung zurücksetzen

        // Gefilterte Werkseinsatzdaten, Farbvarianten-Daten und Cache für den DataObjects von kombinierten Texten in den
        // geladenen Stücklisteneinträgen zurücksetzen
        if (isPartListLoaded()) {
            clearAllFactoryDataForRetailForPartList();
            clearAllColortableDataForRetailFilteredForPartList();
            clearAllDataCombTextListsForPartList();
        }
    }

    public boolean hasPartListEntriesWithConstructionKits(EtkEbenenDaten partListType) {
        DBDataObjectList<EtkDataPartListEntry> partListUnfiltered = getPartListUnfiltered(partListType);
        for (EtkDataPartListEntry partListEntry : partListUnfiltered) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                if (((iPartsDataPartListEntry)partListEntry).hasConstructionKits()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public synchronized void clearCache() {
        super.clearCache();
        documentationType = null;
        cachedDataModule = null;
        moduleType = null;
    }

    /**
     * In diesem Modul greift der Federfilter
     *
     * @return
     */
    public boolean isSpringRelevant() {
        if (isRetailPartList()) {
            iPartsDataModule module = getModuleMetaData();
            if (module.existsInDB()) {
                return module.isSpringFilterRelevant();
            }
        }
        return false;
    }

    /**
     * In diesem Modul greift der spezielle ZB Sachnummern Filter
     *
     * @return
     */
    public boolean isSpecialZBFilterRelevant() {
        if (isRetailPartList()) {
            iPartsDataModule module = getModuleMetaData();
            if (module.existsInDB()) {
                return module.isZBNumberFilterRelevant();
            }
        }
        return false;
    }

    /**
     * Hat dieses Modul eine {@link iPartsSpecType}?
     *
     * @return
     */
    public boolean isSpecTypeRelevant() {
        return iPartsSpecType.isSpecTypeRelevant(getSpecType());
    }

    /**
     * Liefert die {@link iPartsSpecType} dieses Moduls
     *
     * @return
     */
    public iPartsSpecType getSpecType() {
        if (isModuleTypeOf(iPartsModuleTypes.getEditableModuleTypes())) {
            iPartsDataModule module = getModuleMetaData();
            if (module.existsInDB()) {
                return module.getSpecType();
            }
        }
        return iPartsSpecType.UNKNOWN;
    }


    /**
     * Das Module ist Bestandteil eines Spezialkataloges z.B. Lacke und Betriebsstoffe
     *
     * @return
     */
    public boolean isSpecialProduct() {
        if (isSpecialProductPartList()) {
            return true;
        }
        if (getAsId().isVirtual()) {
            List<iPartsVirtualNode> virtualNodesPath = getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsVirtualNode.isProductNode(virtualNodesPath)) {
                    iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), (iPartsProductId)virtualNodesPath.get(0).getId());
                    if (product.isSpecialCatalog()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Liefert den Aggregatetyp vom Produkt, wobei anhand der (ersten) Verwendung von dieser Stückliste das Produkt ermittelt wird.
     *
     * @return {@code null} falls kein Produkt ermittelt werden konnte
     */
    public String getAggregateTypeOfProductFromModuleUsage() {
        // Aggregatetyp über das Produkt zur Stückliste bestimmen
        iPartsProductId productId = getProductIdFromModuleUsage();
        if (productId != null) {
            return iPartsProduct.getInstance(getEtkProject(), productId).getAggregateType();
        } else {
            return null;
        }
    }

    /**
     * Entfernt die Werkseinsatzdaten für den Retail zu allen PartListEntries MIT Berücksichtigung von Ersetzungen, damit
     * diese bei der ersten Verwendung aufgrund der Werkseinsatzdaten für den Retail OHNE Berücksichtigung von Ersetzungen
     * neu berechnet werden.
     */
    public void clearAllFactoryDataForRetailForPartList() {
        // Alle Werkseinsatzdaten für den Retail MIT Berücksichtigung von Ersetzungen für alle Stücklisteneinträge löschen,
        // damit diese neu berechnet werden
        for (EtkDataPartListEntry partListEntry : getPartListUnfiltered(null)) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPLE = (iPartsDataPartListEntry)partListEntry;
                iPartsPLE.clearFactoryDataForRetail();
                iPartsPLE.clearFilteredReplacements();
            }
        }

        // Baumusternummer für den Baumuster-Filter ebenfalls zurücksetzen, damit die Werkseinsatzdaten neu gefiltert werden
        setFilteredWithModelNumber(null);
    }

    /**
     * Entfernt die gefiltertern Farbtabellen für die gesamte Stückliste. Dadurch wird bei der nächsten Abfrage die
     * Filterung neu durchgeführt. Die geladenen (ungefilterten) Farbtabellen bleiben erhalten
     */
    public void clearAllColortableDataForRetailFilteredForPartList() {
        for (EtkDataPartListEntry partListEntry : getPartListUnfiltered(null)) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                ((iPartsDataPartListEntry)partListEntry).clearColorTableForRetailFiltered();
            }
        }
    }

    /**
     * Entfernt alle Ersetzungen an den Stücklisteneinträgen damit sie beim nächsten Abruf neu berechnet werden
     */
    public void clearAllReplacementsForPartList() {
        // Alle Ersetzungen für alle Stücklisteneinträge löschen, damit diese neu berechnet werden
        for (EtkDataPartListEntry partListEntry : getPartListUnfiltered(null)) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                ((iPartsDataPartListEntry)partListEntry).clearReplacements();
            }
        }
    }

    /**
     * Entfernt alle Cache-Einträge für kombinierte Texte an allen Stücklisteneinträgen
     */
    public void clearAllDataCombTextListsForPartList() {
        for (EtkDataPartListEntry partListEntry : getPartListUnfiltered(null)) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                ((iPartsDataPartListEntry)partListEntry).setDataCombTextList(null);
            }
        }
    }

    /**
     * Speichert die veränderten Stücklisteneinträge in der Datenbank oder im ChangeSet, je nachdem was gerade aktiv ist.
     *
     * @return {@code true} falls ein Stücklisteneintrag modifiziert war und deshalb etwas gespeichert wurde
     */
    public boolean savePartListEntries() {
        boolean somethingModified = false;
        if (getEtkProject().isRevisionChangeSetActiveForEdit()) {
            DBDataObjectList<EtkDataPartListEntry> entryList = getPartListUnfiltered(null);

            somethingModified = !getEtkProject().getRevisionsHelper().getActiveRevisionChangeSetForEdit().addDataObjectList(entryList).isEmpty();

            entryList.resetModifiedFlags();

            if (somethingModified) {
                // Das Assembly für den Auftrag als geändert markieren
                markAssemblyInChangeSetAsChanged();
                EtkDataAssembly.removeDataAssemblyFromCache(getEtkProject(), getAsId());
            }
        } else {
            getEtkProject().getDbLayer().startTransaction();
            try {
                DBDataObjectList<EtkDataPartListEntry> entryList = getPartListUnfiltered(null);

                List<EtkDataPartListEntry> deletedList = entryList.getDeletedList();

                for (EtkDataPartListEntry entry : deletedList) {
                    entry.deleteFromDB();
                    somethingModified = true;
                }

                for (EtkDataPartListEntry entry : entryList) {
                    if (entry.isModifiedWithChildren()) {
                        entry.saveToDB();
                        somethingModified = true;
                    }
                }

                entryList.resetModifiedFlags();

                getEtkProject().getDbLayer().commit();
                if (somethingModified) {
                    EtkDataAssembly.removeDataAssemblyFromCache(getEtkProject(), getAsId());
                }
            } catch (Exception e) {
                getEtkProject().getDbLayer().rollback();
                Logger.getLogger().handleRuntimeException(e);
            }
        }

        return somethingModified;
    }

    public iPartsSeriesId getSeriesId(DBDataObjectAttributes partListEntryAttributes) {
        String seriesNo = "";
        iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromDbValue(partListEntryAttributes.getFieldValue(FIELD_K_SOURCE_TYPE));
        if (sourceType == iPartsEntrySourceType.DIALOG) {
            String sourceContext = partListEntryAttributes.getFieldValue(FIELD_K_SOURCE_CONTEXT);
            HmMSmId hmMSmId = HmMSmId.getHmMSmIdFromDIALOGSourceContext(sourceContext);
            if (hmMSmId != null) {
                seriesNo = hmMSmId.getSeries();
            }
        }
        return new iPartsSeriesId(seriesNo);
    }

    /**
     * Ermittelt die Baureihe zur Assembly über das zugehörige Produkt
     * Sollte es eine referenzierte Baureihe geben, wird diese verwendet, sonst wird die Baureihe über die ModelTypes ermittelt
     *
     * @return
     */
    public iPartsSeriesId getSeriesId() {
        iPartsProductId productId = getProductIdFromModuleUsage();
        if (productId != null) {
            iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), productId);
            if (product != null) {
                iPartsSeriesId seriesId = iPartsProduct.getInstance(getEtkProject(), productId).getReferencedSeries();
                if (seriesId == null) {
                    Set<String> modelTypesForProduct = iPartsProduct.getInstance(getEtkProject(), productId).getAllModelTypes(getEtkProject());
                    if (!modelTypesForProduct.isEmpty()) {
                        seriesId = new iPartsSeriesId((String)modelTypesForProduct.toArray()[0]);
                    }
                }
                return seriesId;
            }
        }
        return null;
    }

    /**
     * Liefert die Baumusternummer zurück, mit der die Baumuster-Filterung dieser Stückliste durchgeführt wurde.
     *
     * @return {@code null} falls noch keine Baumuster-Filterung durchgeführt wurde oder der Baumuster-Filter deaktiviert ist
     */
    public String getFilteredWithModelNumber() {
        return filteredWithModelNumber;
    }

    /**
     * Setzt die Baumusternummer, mit der die Baumuster-Filterung dieser Stückliste durchgeführt wurde.
     *
     * @param filteredWithModelNumber
     */
    public void setFilteredWithModelNumber(String filteredWithModelNumber) {
        this.filteredWithModelNumber = filteredWithModelNumber;
    }

    /**
     * Gibt zurück, ob die Baureihe am Produkt zum Modul eventgesteuert ist.
     *
     * @return
     */
    public boolean isSeriesFromProductModuleUsageEventControlled() {
        iPartsProductId productId = getProductIdFromModuleUsage();
        if (productId != null) {
            iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), productId);
            if (product != null) {
                return product.isReferencedSeriesEventControlled(getEtkProject());
            }
        }
        return false;
    }

    @Override
    protected List<EtkDataPartListEntry> filterList(DBDataObjectList<EtkDataPartListEntry> entries, String language) {
        List<EtkDataPartListEntry> filteredPartListEntries = super.filterList(entries, language);
        return postFilterProcessing(filteredPartListEntries);
    }

    /**
     * Zusätzliche Anpassung der Stückliste nach der eigentlichen Filterung
     *
     * @param filteredPartListEntries
     * @return
     */
    private List<EtkDataPartListEntry> postFilterProcessing(List<EtkDataPartListEntry> filteredPartListEntries) {
        List<EtkDataPartListEntry> result = iPartsFilter.get().postFilterForFilteredPartList(filteredPartListEntries);
        // In der DIALOG Konstruktionsstückliste müssen Stücklistentexte entfernt werden, wenn die dazugehörige
        // Stücklistenposition gefiltert wurde
        if (isVirtual() && isDialogSMConstructionAssembly()) {
            result = iPartsPartlistTextHelper.filterPositionTextsWithoutVisibleEntry(getEtkProject(), result);
        }
        return result;
    }

    @Override
    public void markAssemblyInChangeSetAsChanged() {
        // Virtuelle Stücklisten dürfen im ChangeSet nie als geändert markiert werden
        if (!getAsId().isVirtual()) {
            super.markAssemblyInChangeSetAsChanged();
        }
    }

    @Override
    public boolean isVirtual() {
        return getAsId().isVirtual();
    }

    /**
     * Befindet sich diese Stückliste im Edit-Modus?
     *
     * @return
     */
    public boolean isEditMode() {
        return isEditMode;
    }

    /**
     * Befindet sich diese Stückliste im Edit-Modus?
     *
     * @param isEditMode
     */
    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    /**
     * Liefert alle SAA/BK-Gültigkeiten aller Stücklisteneinträge getrennt durch {@link #ARRAY_VALIDITIES_FOR_FILTER_DELIMITER}
     * in einem String zurück.
     *
     * @return Leerer String falls mindestens ein Stücklisteneintrag keine SAA/BK-Gültigkeit hat und demzufolge für alle
     * SAAs/BKs gültig ist
     */
    private String getAllSAAValiditiesForFilter() {
        Set<String> allSAAValidities = new TreeSet<>();
        EtkEbenenDaten minimalPartsListType = new EtkEbenenDaten();
        minimalPartsListType.addFeldIfNotExists(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SA_VALIDITY, false, true));
        for (EtkDataPartListEntry partListEntry : getPartListUnfiltered(minimalPartsListType, false, false)) { // Minimale Stückliste
            EtkDataArray saaValidities = partListEntry.getFieldValueAsArray(FIELD_K_SA_VALIDITY);
            if ((saaValidities == null) || saaValidities.isEmpty()) {
                return "";
            }
            allSAAValidities.addAll(saaValidities.getArrayAsStringList());
        }
        return StrUtils.stringListToString(allSAAValidities, ARRAY_VALIDITIES_FOR_FILTER_DELIMITER);
    }

    /**
     * Speichert bei Truck-TUs (keine freien SAs) alle SAA/BK-Gültigkeiten aller Stücklisteneinträge in der Tabelle {@code DA_MODULES_EINPAS}
     * für die schnellere Filterung direkt in der DB.
     */
    public void saveAllSAAValiditiesForFilter() {
        if (!getDocumentationType().isTruckDocumentationType() || isSAAssembly()) {
            return;
        }

        String allSAAValidites = getAllSAAValiditiesForFilter();
        iPartsDataModuleEinPASList dataModuleEinPASList = iPartsDataModuleEinPASList.loadForModule(getEtkProject(), getAsId());
        for (iPartsDataModuleEinPAS dataModuleEinPAS : dataModuleEinPASList) {
            dataModuleEinPAS.setFieldValue(FIELD_DME_SAA_VALIDITY, allSAAValidites, DBActionOrigin.FROM_EDIT);
        }

        // Speichern direkt in der DB
        dataModuleEinPASList.saveToDB(getEtkProject(), false);
    }

    /**
     * Liefert alle Vorschaubilder zur Anzeige in einer Stückliste für dieses Modul zurück.
     *
     * @return
     */
    public List<FrameworkImage> getPreviewImages() {
        List<FrameworkImage> previewImages = new DwList<>();
        String dbLanguage = getEtkProject().getDBLanguage();
        int maxHeight = getEtkProject().getConfig().getMaxIconHeight(ConfigBase.IconSizeType.PARTSLIST);
        for (EtkDataImage dataImage : getImages()) {
            EtkDataPool dataPool = dataImage.getBestImageVariant(dbLanguage, EtkDataImage.IMAGE_USAGE_2D_FILLED); // Explizit Pixelgrafiken anfordern
            if (dataPool != null) {
                FrameworkImage image = previewImageCache.get(dataPool.getAsId());
                if (image == null) {
                    // Zeichnung auf die gewünschte maximale Höhe reduzieren
                    image = FrameworkImage.getFromByteArray(dataPool.getImgBytes(), null, dataPool.getAsId().toString("_", true));
                    image = image.getVersionAsNewFrameworkImage(MimeTypes.MIME_TYPE_PNG, "", (int)((double)image.getWidth() / (double)image.getHeight() * maxHeight), maxHeight);

                    // Synchronisierung erst nach dem u.U. langsamen Laden/Konvertieren machen
                    synchronized (previewImageCache) {
                        FrameworkImage cachedImage = previewImageCache.get(dataPool.getAsId());
                        if (cachedImage == null) {
                            previewImageCache.put(dataPool.getAsId(), image);
                        } else {
                            image = cachedImage;
                        }
                    }
                }
                previewImages.add(image);
            }
        }
        return previewImages;
    }
}