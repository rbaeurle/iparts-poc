/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAOHistoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAOHistory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.Calendar;
import java.util.List;

/**
 * Helper für Author Order History Elemente {@link iPartsDataAOHistory}
 */
public class iPartsDataAOHistoryHelper implements iPartsConst {

    public enum ACTION_KEYS {
        STATUS("!!Statusänderung"),
        USER("!!Zuweisung"),
        MSG("!!Bemerkung"),
        UNKNOWN("");

        private String description;

        ACTION_KEYS(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static ACTION_KEYS getActionKey(String value) {
        for (ACTION_KEYS actionKey : ACTION_KEYS.values()) {
            if (actionKey.name().equals(value)) {
                return actionKey;
            }
        }
        return ACTION_KEYS.UNKNOWN;
    }

    private static final String AO_HISTORY_DELIMITER = "#";

    /**
     * Historie-Eintrag für die Statusänderung des übergebenen Autorenauftrags erzeugen
     *
     * @param dataAuthorOrder
     * @param fromStatus
     * @param toStatus
     * @param project
     * @return
     */
    public static iPartsDataAOHistory createAndSetStatus(iPartsDataAuthorOrder dataAuthorOrder, iPartsAuthorOrderStatus fromStatus,
                                                         iPartsAuthorOrderStatus toStatus, EtkProject project) {
        iPartsDataAOHistory dataAOHistory = createDataAOHistory(dataAuthorOrder, project);
        setChangeStatus(dataAOHistory, fromStatus, toStatus);
        return dataAOHistory;
    }

    /**
     * Historie-Eintrag für die Delegierungsänderung des übergebenen Autorenauftrags erzeugen
     *
     * @param dataAuthorOrder
     * @param startUserName
     * @param destUserName
     * @param project
     * @return
     */
    public static iPartsDataAOHistory createAndSetUserChange(iPartsDataAuthorOrder dataAuthorOrder, String startUserName,
                                                             String destUserName, EtkProject project) {
        iPartsDataAOHistory dataAOHistory = createDataAOHistory(dataAuthorOrder, project);
        setChangeUser(dataAOHistory, startUserName, destUserName);
        return dataAOHistory;
    }

    /**
     * Historie-Eintrag für den übergebenen Autorenauftrag samt Grund des Eintrags erzeugen
     *
     * @param dataAuthorOrder
     * @param message
     * @param project
     * @return
     */
    public static iPartsDataAOHistory createAndSetMessage(iPartsDataAuthorOrder dataAuthorOrder, String message, EtkProject project) {
        iPartsDataAOHistory dataAOHistory = createDataAOHistory(dataAuthorOrder, project);
        setChangeMessage(dataAOHistory, message);
        return dataAOHistory;
    }

    public static List<String> getSplittedActionString(String actionString) {
        return splitActionString(actionString);
    }

    /**
     * Historie-Eintrag für den übergebenen Autorenauftrag erzeugen. Beim Erzeugen wird der aktuelle Benutzer und das
     * Erstellungsdatum gespeichert.
     *
     * @param dataAuthorOrder
     * @param project
     * @return
     */
    protected static iPartsDataAOHistory createDataAOHistory(iPartsDataAuthorOrder dataAuthorOrder, EtkProject project) {
        int nextLfdNr = dataAuthorOrder.getHistories().size() + 1;
        iPartsAOHistoryId aoHistoryId = new iPartsAOHistoryId(dataAuthorOrder.getAsId().getAuthorGuid(), nextLfdNr);
        iPartsDataAOHistory dataAOHistory = new iPartsDataAOHistory(project, aoHistoryId);
        dataAOHistory.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        dataAOHistory.setFieldValue(FIELD_DAH_CHANGE_USER_ID, iPartsDataAuthorOrder.getLoginAcronym(), DBActionOrigin.FROM_EDIT);
        dataAOHistory.setFieldValueAsDateTime(FIELD_DAH_CHANGE_DATE, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
        return dataAOHistory;
    }

    protected static void setChangeStatus(iPartsDataAOHistory dataAOHistory, iPartsAuthorOrderStatus fromStatus, iPartsAuthorOrderStatus toStatus) {
        dataAOHistory.setAction(buildStatusAction(fromStatus, toStatus));
    }

    protected static void setChangeUser(iPartsDataAOHistory dataAOHistory, String startUserName, String destUserName) {
        dataAOHistory.setAction(buildUserAction(startUserName, destUserName));
    }

    protected static void setChangeMessage(iPartsDataAOHistory dataAOHistory, String message) {
        dataAOHistory.setAction(buildMessageAction(message));
    }

    protected static String buildStatusAction(iPartsAuthorOrderStatus fromStatus, iPartsAuthorOrderStatus toStatus) {
        return buildActionString(ACTION_KEYS.STATUS, fromStatus.getDBValue(), toStatus.getDBValue());
    }

    protected static String buildUserAction(String startUserName, String destUserName) {
        return buildActionString(ACTION_KEYS.USER, startUserName, destUserName);
    }

    protected static String buildMessageAction(String message) {
        return buildActionString(ACTION_KEYS.MSG, message, null);
    }

    protected static String buildActionString(ACTION_KEYS actionKey, String paramOne, String paramTwo) {
        if (paramTwo == null) {
            paramTwo = "";
        }
        return actionKey.name() + AO_HISTORY_DELIMITER + paramOne + AO_HISTORY_DELIMITER + paramTwo;
    }

    protected static List<String> splitActionString(String actionString) {
        List<String> result = new DwList<String>();
        String[] splitOneList = StrUtils.toStringArray(actionString, AO_HISTORY_DELIMITER, true);
        for (int lfdNr = 0; lfdNr < splitOneList.length; lfdNr++) {
            result.add(splitOneList[lfdNr].trim());
        }
        while (result.size() < 3) {
            result.add("");
        }
        return result;
    }
}
