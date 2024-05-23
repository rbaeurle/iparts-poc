/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.*;

/**
 * Liste von {@link iPartsDataDictTextKind}s.
 */
public class iPartsDataDictTextKindList extends EtkDataObjectList<iPartsDataDictTextKind> implements iPartsConst {

    public iPartsDataDictTextKindList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictTextKind}s.
     *
     * @param project
     * @param loadForAllLanguages Soll die Textartbenennung für alle Sprachen geladen werden?
     * @return
     */
    public static iPartsDataDictTextKindList loadAllTextKindList(EtkProject project, boolean loadForAllLanguages) {
        iPartsDataDictTextKindList list = new iPartsDataDictTextKindList();
        list.loadAllTextKindsFromDB(project, loadForAllLanguages, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictTextKind}s sortiert nach dem Textartnamen und liefert diese
     * in einer TreeMap mit Textartname auf {@link iPartsDataDictTextKind} zurück.
     *
     * @param project
     * @return
     */
    public static TreeMap<String, iPartsDataDictTextKind> loadAllTextKindListSortedByName(EtkProject project) {
        iPartsDataDictTextKindList textKindList = loadAllTextKindList(project, false);
        TreeMap<String, iPartsDataDictTextKind> textKinds = new TreeMap<>();
        String viewerLang = project.getConfig().getCurrentViewerLanguage();
        for (iPartsDataDictTextKind dataTextKind : textKindList) {
            textKinds.put(dataTextKind.getName(viewerLang), dataTextKind);
        }
        return textKinds;
    }

    public static Collection<iPartsDataDictTextKind> loadSpecialTextKindListSortedByName(EtkProject project, EnumSet<DictTextKindTypes> textKindTypes) {
        Set<String> foreignIds = new LinkedHashSet<>();
        for (DictTextKindTypes textKindType : textKindTypes) {
            foreignIds.add(textKindType.getMadId());
        }
        return loadSpecialTextKindListSortedByName(project, foreignIds);
    }

    public static Collection<iPartsDataDictTextKind> loadSpecialTextKindListSortedByName(EtkProject project, Set<String> foreignIds) {
        iPartsDataDictTextKindList textKindList = loadAllTextKindList(project, false);
        TreeMap<String, iPartsDataDictTextKind> textKinds = new TreeMap<>();
        for (iPartsDataDictTextKind dataTextKind : textKindList) {
            if (foreignIds.remove(dataTextKind.getFieldValue(FIELD_DA_DICT_TK_FOREIGN_TKIND))) {
                textKinds.put(dataTextKind.getName(project.getConfig().getCurrentViewerLanguage()), dataTextKind);
            }
            if (foreignIds.isEmpty()) {
                break;
            }
        }
        return textKinds.values();
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictTextKind}s.
     *
     * @param project
     * @param loadForAllLanguages Soll die Textartbenennung für alle Sprachen geladen werden?
     * @param origin
     */
    private void loadAllTextKindsFromDB(EtkProject project, boolean loadForAllLanguages, DBActionOrigin origin) {
        clear(origin);
        if (loadForAllLanguages) {
            searchSortAndFillWithMultiLangValueForAllLanguages(project, null, FIELD_DA_DICT_TK_NAME, null, null, false,
                                                               null, false);
        } else {
            searchAndFill(project, TABLE_DA_DICT_TXTKIND, null, null, LoadType.COMPLETE, origin);
        }
    }

    @Override
    protected iPartsDataDictTextKind getNewDataObject(EtkProject project) {
        return new iPartsDataDictTextKind(project, null);
    }
}
