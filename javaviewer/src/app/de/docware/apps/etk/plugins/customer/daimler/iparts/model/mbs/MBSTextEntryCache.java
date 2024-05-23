package de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSPartlist;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSPartlistList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructureList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cache für alle MBS Texte, die als eigene Stücklistenpositionen in der MBS Struktur oder in MBS Stücklisten angezeigt
 * werden
 */
public class MBSTextEntryCache implements iPartsConst {

    // Alle MBS Knoten für das übergebene {@link EtkProject}
    private static ObjectInstanceLRUList<Object, MBSTextEntryCache> instances = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS,
                                                                                                            MAX_CACHE_LIFE_TIME_CORE);


    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized MBSTextEntryCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), MBSTextEntryCache.class, null, false);
        MBSTextEntryCache instance = instances.get(hashObject);
        if (instance == null) {
            instance = new MBSTextEntryCache();
            instance.load(project);
            instances.put(hashObject, instance);
        }
        return instance;
    }

    private Map<String, Set<iPartsDataMBSStructure>> textsForStructure;
    private Map<String, Set<iPartsDataMBSPartlist>> textsForPartlists;

    public MBSTextEntryCache() {
        this.textsForStructure = new HashMap<>();
        this.textsForPartlists = new HashMap<>();
    }

    private void load(EtkProject project) {
        // Textpositionen, die in der Struktur vorkommen, können komplett geladen werden, da es nicht so viele gibt
        iPartsDataMBSStructureList structureList = new iPartsDataMBSStructureList();
        structureList.searchSortAndFill(project, TABLE_DA_STRUCTURE_MBS,
                                        new String[]{ FIELD_DSM_SUB_SNR },
                                        new String[]{ "" },
                                        new String[]{ FIELD_DSM_SNR },
                                        DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);

        for (iPartsDataMBSStructure structure : structureList) {
            String listNumber = structure.getFieldValue(FIELD_DSM_SNR);
            Set<iPartsDataMBSStructure> textObjects = textsForStructure.computeIfAbsent(listNumber, entry -> new LinkedHashSet<>());
            textObjects.add(structure);
        }
    }

    public Set<iPartsDataMBSStructure> getTextsForStructureListNumber(String listNumber) {
        return textsForStructure.get(listNumber);
    }

    /**
     * Liefert alle Textpositionen zur übergebenen oberen Sachnummer. Alle in der DB gefundenen Texte werden in den Cache
     * gelegt.
     *
     * @param project
     * @param upperNo
     * @return
     */
    public Set<iPartsDataMBSPartlist> getTextsForPartlistUpperNo(EtkProject project, String upperNo) {
        Set<iPartsDataMBSPartlist> textsForUpperNo = textsForPartlists.get(upperNo);
        // Wurden die Texte noch nicht geladen, muss hier in der DB geschaut werden, ob Texte vorhanden sind
        if (textsForUpperNo == null) {
            iPartsDataMBSPartlistList partList = new iPartsDataMBSPartlistList();
            partList.searchSortAndFill(project, TABLE_DA_PARTSLIST_MBS,
                                       new String[]{ FIELD_DPM_SNR, FIELD_DPM_SUB_SNR },
                                       new String[]{ upperNo, "" },
                                       new String[]{ FIELD_DPM_SNR },
                                       DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
            textsForUpperNo = new LinkedHashSet<>(partList.getAsList());
            textsForPartlists.put(upperNo, textsForUpperNo);
        }
        return textsForUpperNo;
    }


}
