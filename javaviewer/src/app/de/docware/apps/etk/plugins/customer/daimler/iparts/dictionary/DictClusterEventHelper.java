package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaCacheId;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Alle benötigten DICTIONARY Cluster Events an einer Stelle
 */
public class DictClusterEventHelper {

    /**
     * Alle Lexikon-Caches in allen Cluster-Knoten löschen
     */
    public static void fireClearAllDictionaryCachesClusterEvent() {
        fireClearAllDictionaryCachesClusterEvent(null, true);
    }

    /**
     * Bestimmte Textarten im Lexikon-Cache optional in allen Cluster-Knoten löschen ({@code textKindTypeList} enthält die
     * Textarten)
     *
     * @param textKindTypeList
     * @param fireInAllClusters
     */
    public static void fireClearAllDictionaryCachesClusterEvent(List<iPartsDictMetaCacheId> textKindTypeList, boolean fireInAllClusters) {
        iPartsDataChangedEventByEdit<iPartsDictMetaCacheId> dictChangeEvent =
                new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.DICTIONARY, null, textKindTypeList, true);
        if (fireInAllClusters) {
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(dictChangeEvent);
        } else {
            ApplicationEvents.fireEventInAllProjects(dictChangeEvent, true, true, null);
        }
    }

    /**
     * Neuer Eintrag in den Lexikon-Cache
     *
     * @param dataDictMeta
     * @param dictTextKindType
     * @param multiLang
     */
    public static void fireNewDictionaryClusterEvent(iPartsDataDictMeta dataDictMeta, iPartsDataDictTextKind dictTextKindType,
                                                     EtkMultiSprache multiLang) {
        fireNewDictionaryClusterEvent(dataDictMeta, dictTextKindType.getForeignTextKindType(), multiLang);
    }

    /**
     * Neuer Eintrag in den Lexikon-Cache
     *
     * @param dataDictMeta
     * @param dictTextKindType
     * @param multiLang
     */
    public static void fireNewDictionaryClusterEvent(iPartsDataDictMeta dataDictMeta, DictTextKindTypes dictTextKindType,
                                                     EtkMultiSprache multiLang) {
        // Wurde ein Text hinzugefügt oder editiert, dann müssen die Caches geleert werden
        String state = dataDictMeta.getState();
        List<iPartsDictMetaCacheId> list = new DwList<>();
        for (Language lang : multiLang.getLanguages()) {
            iPartsDictMetaCacheId dictMetaCacheId = new iPartsDictMetaCacheId(dataDictMeta, dictTextKindType, multiLang,
                                                                              lang, !state.equals(iPartsDictConst.DICT_STATUS_RELEASED));
            list.add(dictMetaCacheId);
        }
        fireDictionaryClusterEvent(list, iPartsDataChangedEventByEdit.Action.NEW);
    }

    /**
     * Lexikon-Cache nach Treffern abfragen und nur die gesuchte TextId zulassen
     *
     * @param dataDictMeta
     * @param dictTextKindType
     * @param multiLang
     * @return
     */
    public static List<iPartsDictMetaCacheId> prepareDictMetaCacheIdListForTextId(iPartsDataDictMeta dataDictMeta, iPartsDataDictTextKind dictTextKindType,
                                                                                  EtkMultiSprache multiLang) {
        List<iPartsDictMetaCacheId> list = iPartsDictMetaCacheId.prepareDictMetaCacheIdList(dataDictMeta,
                                                                                            dictTextKindType.getForeignTextKindType(),
                                                                                            multiLang);
        if (list == null) {
            return null;
        }
        List<iPartsDictMetaCacheId> foundList = new DwList<>();
        String searchTextId = dataDictMeta.getAsId().getTextId();
        list.forEach((dictMetaCacheId) -> {
            if (dictMetaCacheId.getTextId().equals(searchTextId)) {
                foundList.add(dictMetaCacheId);
            }
        });
        if (foundList.isEmpty()) {
            return null;
        }
        return foundList;
    }

    /**
     * Löschen eines Eintrags im Lexikon-Cache
     *
     * @param dataDictMeta
     * @param dictTextKindType
     * @param multiLang
     */
    public static void fireDeleteDictionaryClusterEvent(iPartsDataDictMeta dataDictMeta, iPartsDataDictTextKind dictTextKindType,
                                                        EtkMultiSprache multiLang) {
        List<iPartsDictMetaCacheId> list = prepareDictMetaCacheIdListForTextId(dataDictMeta, dictTextKindType, multiLang);
        fireDictionaryClusterEvent(list, iPartsDataChangedEventByEdit.Action.DELETED);
    }


    /**
     * Verändern eines Eintrags im Lexikon-Cache
     *
     * @param dataDictMeta
     * @param oldDataDictMeta
     * @param dictTextKindType
     * @param multiLang
     */
    public static void fireChangedDictionaryClusterEvent(iPartsDataDictMeta dataDictMeta, iPartsDataDictMeta oldDataDictMeta,
                                                         iPartsDataDictTextKind dictTextKindType, EtkMultiSprache multiLang) {
        // Wurde ein Text hinzugefügt oder editiert, dann müssen die Caches geleert werden
        String oldState = oldDataDictMeta.getState();
        boolean newCheckNotReleasedTexts = !dataDictMeta.getState().equals(iPartsDictConst.DICT_STATUS_RELEASED);
        if (!oldState.equals(dataDictMeta.getState())) {
            // Status hat sich geändert
            boolean oldCheckNotReleasedTexts = !oldState.equals(iPartsDictConst.DICT_STATUS_RELEASED);
            if (oldCheckNotReleasedTexts != newCheckNotReleasedTexts) {
                // Status wurde so geändert, dass beide Lexikon-Caches betroffen sind
                // => erstmal den alten Text löschen
                DictClusterEventHelper.fireDeleteDictionaryClusterEvent(oldDataDictMeta, dictTextKindType, oldDataDictMeta.getMultiLang());
                // => neuen Text einfügen
                DictClusterEventHelper.fireNewDictionaryClusterEvent(dataDictMeta, dictTextKindType, multiLang);
                return;
            } else {
                // Statusänderung bezieht sich nur auf einen Cache
                // => weiter mit Untersuchung von Textänderungen
            }
        }

        // Status ist gleichgeblieben oder bezieht sich nur auf einen Cache
        // => weiter mit Untersuchung von Textänderungen
        List<iPartsDictMetaCacheId> modifyList = new DwList<>();
        List<iPartsDictMetaCacheId> deleteList = new DwList<>();
        List<iPartsDictMetaCacheId> insertList = new DwList<>();
        EtkMultiSprache oldMultiLang = oldDataDictMeta.getMultiLang().cloneMe();
        for (Language lang : multiLang.getLanguages()) {
            if (oldMultiLang.containsLanguage(lang, true)) {
                if (!multiLang.getText(lang.getCode()).equals(oldMultiLang.getText(lang.getCode()))) {
                    // Text in dieser Sprache hat sich geändert
                    modifyList.add(new iPartsDictMetaCacheId(dataDictMeta, dictTextKindType.getForeignTextKindType(), lang,
                                                             newCheckNotReleasedTexts));
                }
                oldMultiLang.removeLanguage(lang.getCode());
            } else {
                // Text in Sprache neu dazugekommen
                insertList.add(new iPartsDictMetaCacheId(dataDictMeta, dictTextKindType.getForeignTextKindType(), lang,
                                                         newCheckNotReleasedTexts));
            }
        }

        if (!oldMultiLang.getSprachen().isEmpty()) {
            // diese Einträge müssen gelöscht werden
            for (Language lang : oldMultiLang.getLanguages()) {
                deleteList.add(new iPartsDictMetaCacheId(oldDataDictMeta, dictTextKindType.getForeignTextKindType(), lang,
                                                         newCheckNotReleasedTexts));
            }
        }

        if (!modifyList.isEmpty()) {
            DictClusterEventHelper.fireDictionaryClusterEvent(modifyList, iPartsDataChangedEventByEdit.Action.MODIFIED);
        }
        if (!deleteList.isEmpty()) {
            DictClusterEventHelper.fireDictionaryClusterEvent(deleteList, iPartsDataChangedEventByEdit.Action.DELETED);
        }
        if (!insertList.isEmpty()) {
            DictClusterEventHelper.fireDictionaryClusterEvent(insertList, iPartsDataChangedEventByEdit.Action.NEW);
        }
    }

    /**
     * Den Cluster-Event vorbereiten und feuern
     *
     * @param list
     * @param action
     */
    public static void fireDictionaryClusterEvent(List<iPartsDictMetaCacheId> list, iPartsDataChangedEventByEdit.Action action) {
        if (list != null) {
            iPartsDataChangedEventByEdit<iPartsDictMetaCacheId> dictChangeEvent =
                    new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.DICTIONARY, action, list, false);
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(dictChangeEvent);
        }
    }
}
