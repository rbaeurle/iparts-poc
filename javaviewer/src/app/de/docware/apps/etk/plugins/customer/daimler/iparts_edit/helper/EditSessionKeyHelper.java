/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashSet;
import java.util.Set;

/**
 * Hilfsroutinen zur Verwaltung der definierten Session-Attribute (iPartsPlufin)
 */
public class EditSessionKeyHelper {

    private static final int MAX_CACHE_SIZE_MARK_KEM = 10;
    private static final int CACHE_LIFETIME_MARK_KEM = -1;

    /**
     * Liefert den Eintrag zu Key aus den Session-Attributen
     *
     * @return
     */
    private static ObjectInstanceStrongLRUList<AssemblyId, Set<String>> getSessionKey(String key) {
        return (ObjectInstanceStrongLRUList<AssemblyId, Set<String>>)Session.get().getAttribute(key);
    }

    /**
     * Setzt den Eintrag SESSION_KEY_MARK_EDS_KEM in den Session-Attributen
     *
     * @param markMap
     */
    private static void setSessionKey(String key, ObjectInstanceStrongLRUList<AssemblyId, Set<String>> markMap) {
        Session.get().setAttribute(key, markMap);
    }

    /**
     * Löscht den Eintrag für eine AssemblyId in Session-Attribute SESSION_KEY_MARK_EDS_KEM
     *
     * @param assemblyId
     */
    private static void resetSessionKeyForAssembly(String key, AssemblyId assemblyId) {
        ObjectInstanceStrongLRUList<AssemblyId, Set<String>> markMap = getSessionKey(key);
        if (markMap != null) {
            markMap.remove(assemblyId);
        }
    }

    /**
     * Setzt einen Eintrag für eine AssemblyId und KemNo im Session-Attribute SESSION_KEY_MARK_EDS_KEM
     *
     * @param assemblyId
     * @param kemNo
     */
    private static void setSessionKey(String key, AssemblyId assemblyId, String kemNo) {
        if ((assemblyId == null) || !assemblyId.isValidId() || !StrUtils.isValid(kemNo)) {
            return;
        }
        ObjectInstanceStrongLRUList<AssemblyId, Set<String>> markMap = getSessionKey(key);
        if (markMap == null) {
            markMap = new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_MARK_KEM, CACHE_LIFETIME_MARK_KEM);
            Set<String> kemSet = new HashSet<>();
            kemSet.add(kemNo);
            markMap.put(assemblyId, kemSet);
            setSessionKey(key, markMap);
        } else {
            Set<String> kemSet = markMap.get(assemblyId);
            if (kemSet == null) {
                kemSet = new HashSet<>();
                markMap.put(assemblyId, kemSet);
            }
            kemSet.add(kemNo);
        }
    }

    /**
     * Holt den  Eintrag für eine AssemblyId aus dem Session-Attribute SESSION_KEY_MARK_EDS_KEM
     *
     * @param assemblyId
     * @return
     */
    private static Set<String> getKemNoSetFromSessionKey(String key, AssemblyId assemblyId) {
        ObjectInstanceStrongLRUList<AssemblyId, Set<String>> markMap = getSessionKey(key);
        if (markMap != null) {
            return markMap.get(assemblyId);
        }
        return null;
    }

    /*=== Hilfsroutinen für SESSION_KEY_MARK_EDS_KEM ===*/

    /**
     * Liefert den Eintrag SESSION_KEY_MARK_EDS_KEM aus den Session-Attributen
     *
     * @return
     */
    public static ObjectInstanceStrongLRUList<AssemblyId, Set<String>> getSessionKeyForMark() {
        return getSessionKey(iPartsPlugin.SESSION_KEY_MARK_EDS_KEM);
    }

    /**
     * Setzt den Eintrag SESSION_KEY_MARK_EDS_KEM in den Session-Attributen
     *
     * @param markMap
     */
    public static void setSessionKeyForMark(ObjectInstanceStrongLRUList<AssemblyId, Set<String>> markMap) {
        setSessionKey(iPartsPlugin.SESSION_KEY_MARK_EDS_KEM, markMap);
    }

    /**
     * Löscht den Eintrag SESSION_KEY_MARK_EDS_KEM in den Session-Attributen
     */
    public static void resetSessionKeyForMark() {
        setSessionKeyForMark(null);
    }

    /**
     * Löscht den Eintrag für eine AssemblyId in Session-Attribute SESSION_KEY_MARK_EDS_KEM
     *
     * @param assemblyId
     */
    public static void resetSessionKeyForMark(AssemblyId assemblyId) {
        resetSessionKeyForAssembly(iPartsPlugin.SESSION_KEY_MARK_EDS_KEM, assemblyId);
    }

    /**
     * Setzt einen Eintrag für eine AssemblyId und KemNo im Session-Attribute SESSION_KEY_MARK_EDS_KEM
     *
     * @param assemblyId
     * @param kemNo
     */
    public static void setSessionKeyForMark(AssemblyId assemblyId, String kemNo) {
        setSessionKey(iPartsPlugin.SESSION_KEY_MARK_EDS_KEM, assemblyId, kemNo);
    }

    /**
     * Holt den  Eintrag für eine AssemblyId aus dem Session-Attribute SESSION_KEY_MARK_EDS_KEM
     *
     * @param assemblyId
     * @return
     */
    public static Set<String> getKemNoSetFromSessionKeyForMark(AssemblyId assemblyId) {
        return getKemNoSetFromSessionKey(iPartsPlugin.SESSION_KEY_MARK_EDS_KEM, assemblyId);
    }

    /**
     * Überprüft, ob es einen Eintrag für die AssemblyId gibt
     *
     * @param assemblyId
     * @return
     */
    public static boolean isKemMarkSetForAssembly(AssemblyId assemblyId) {
        Set<String> kemSet = getKemNoSetFromSessionKeyForMark(assemblyId);
        return ((kemSet != null) && !kemSet.isEmpty());
    }

    public static String buildKemNoList(AssemblyId assemblyId) {
        Set<String> kemSet = getKemNoSetFromSessionKeyForMark(assemblyId);
        return buildKemNoList(kemSet);
    }

    /**
     * bildet aus der Liste der KEM-Nummern einen komma-separierten String
     * Sind mehr als 5 KEM-Nummern vorhanden, wird der String mit '...' beendet
     *
     * @param kemSet
     * @return
     */
    private static String buildKemNoList(Set<String> kemSet) {
        if ((kemSet == null) || kemSet.isEmpty()) {
            return null;
        }
        StringBuilder kemNos = new StringBuilder();
        int count = 0;
        for (String kemNo : kemSet) {
            if (count > 0) {
                kemNos.append(", ");
            }
            kemNos.append(kemNo);
            count++;
            if (count >= 5) {
                kemNos.append("...");
                break;
            }
        }
        return kemNos.toString();
    }


    /*=== Ende Hilfsroutinen für SESSION_KEY_MARK_EDS_KEM ===*/
    /*=== Hilfsroutinen für SESSION_KEY_MARK_MBS_KEM ===*/

    /**
     * Liefert den Eintrag SESSION_KEY_MARK_MBS_KEM aus den Session-Attributen
     *
     * @return
     */
    public static ObjectInstanceStrongLRUList<AssemblyId, Set<String>> getSessionKeyForMarkMBS() {
        return getSessionKey(iPartsPlugin.SESSION_KEY_MARK_MBS_KEM);
    }

    /**
     * Setzt den Eintrag SESSION_KEY_MARK_MBS_KEM in den Session-Attributen
     *
     * @param markMap
     */
    public static void setSessionKeyForMarkMBS(ObjectInstanceStrongLRUList<AssemblyId, Set<String>> markMap) {
        setSessionKey(iPartsPlugin.SESSION_KEY_MARK_MBS_KEM, markMap);
    }

    /**
     * Löscht den Eintrag SESSION_KEY_MARK_MBS_KEM in den Session-Attributen
     */
    public static void resetSessionKeyForMarkMBS() {
        setSessionKeyForMark(null);
    }

    /**
     * Löscht den Eintrag für eine AssemblyId in Session-Attribute SESSION_KEY_MARK_MBS_KEM
     *
     * @param assemblyId
     */
    public static void resetSessionKeyForMarkMBS(AssemblyId assemblyId) {
        resetSessionKeyForAssembly(iPartsPlugin.SESSION_KEY_MARK_MBS_KEM, assemblyId);
    }

    /**
     * Setzt einen Eintrag für eine AssemblyId und KemNo im Session-Attribute SESSION_KEY_MARK_MBS_KEM
     *
     * @param assemblyId
     * @param kemNo
     */
    public static void setSessionKeyForMarkMBS(AssemblyId assemblyId, String kemNo) {
        setSessionKey(iPartsPlugin.SESSION_KEY_MARK_MBS_KEM, assemblyId, kemNo);
    }

    /**
     * Holt den  Eintrag für eine AssemblyId aus dem Session-Attribute SESSION_KEY_MARK_MBS_KEM
     *
     * @param assemblyId
     * @return
     */
    public static Set<String> getKemNoSetFromSessionKeyForMarkMBS(AssemblyId assemblyId) {
        return getKemNoSetFromSessionKey(iPartsPlugin.SESSION_KEY_MARK_MBS_KEM, assemblyId);
    }

    /**
     * Überprüft, ob es einen Eintrag für die AssemblyId gibt
     *
     * @param assemblyId
     * @return
     */
    public static boolean isKemMarkSetForAssemblyMBS(AssemblyId assemblyId) {
        Set<String> kemSet = getKemNoSetFromSessionKeyForMarkMBS(assemblyId);
        return ((kemSet != null) && !kemSet.isEmpty());
    }

    public static String buildKemNoListMBS(AssemblyId assemblyId) {
        Set<String> kemSet = getKemNoSetFromSessionKeyForMarkMBS(assemblyId);
        return buildKemNoList(kemSet);
    }

    /*=== Ende Hilfsroutinen für SESSION_KEY_MARK_MBS_KEM ===*/
}
