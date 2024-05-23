/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminCacheElement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataSet;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.*;

import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Helfer zum Laden von Autorenaufträgen mit Bildauftragsinformationen
 */
public class iPartsAuthorOrderAndPicLoader implements iPartsConst {

    /**
     * Erzeugt eine {@link ConditionList}, um alle nicht-freigegebenen Autoren-Aufträge (optional inkl. Startzustand)
     * abfragen zu können.
     *
     * @param includeStartState
     * @return
     */
    public static ConditionList createConditionListForNotApprovedAuthorOrders(boolean includeStartState) {
        List<Condition> conditions = new ArrayList<>();
        for (iPartsAuthorOrderStatus authorOrderStatus : iPartsAuthorOrderStatus.values()) {
            // Unbekannt und Endzustand ausfiltern; Startzustand optional ausfiltern
            if ((authorOrderStatus != iPartsAuthorOrderStatus.UNKNOWN) && !iPartsAuthorOrderStatus.isEndState(authorOrderStatus)
                && (includeStartState || !iPartsAuthorOrderStatus.isStartState(authorOrderStatus))) {
                conditions.add(new Condition(FIELD_DAO_STATUS, Condition.OPERATOR_EQUALS, authorOrderStatus.getDBValue()));
            }
        }
        return new ConditionList(conditions, true);
    }

    /**
     * Erzeugt und lädt eine zeitlich sortierte Liste aller {@link iPartsDataAuthorOrderList}s für den übergebenen {@code creationUserName}.
     *
     * @param project
     * @param creationUserName
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListByCreationUser(EtkProject project, String creationUserName) {
        List<AbstractCondition> conditions = new ArrayList<>();
        conditions.add(new Condition(FIELD_DAO_CREATION_USER_ID, Condition.OPERATOR_EQUALS, creationUserName));
        conditions.add(createConditionListForNotApprovedAuthorOrders(true));
        return loadAuthorOrdersWithConditionsAndPicOrderState(project, new ConditionList(conditions),
                                                              DBActionOrigin.FROM_DB).filterByUser(project, creationUserName);
    }

    /**
     * Erzeugt und lädt eine zeitlich sortierte und für den übergebenen Benutzer gefilterte Liste aller {@link iPartsDataAuthorOrder}s
     * für die Qualitätsprüfung.
     *
     * @param project
     * @param userName
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListAllQA(EtkProject project, String userName) {
        return loadQAAuthorOrders(project, null).filterByUser(project, userName);
    }

    /**
     * Erzeugt und lädt eine zeitlich sortierte Liste aller {@link iPartsDataAuthorOrder}s für die Qualitätsprüfung für
     * die der aktive Benutzer gleich {@code userName} ist.
     *
     * @param project
     * @param userName
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListMyQA(EtkProject project, String userName) {
        return loadQAAuthorOrders(project, userName).filterByUser(project, userName);
    }

    private static iPartsDataAuthorOrderList loadQAAuthorOrders(EtkProject project, String userName) {
        List<Condition> conditions = new ArrayList<>();
        if (userName != null) {
            conditions.add(new Condition(FIELD_DAO_CURRENT_USER_ID, Condition.OPERATOR_EQUALS, userName));
        }
        conditions.add(new Condition(FIELD_DAO_STATUS, Condition.OPERATOR_EQUALS, iPartsAuthorOrderStatus.getRealPrevState(iPartsAuthorOrderStatus.getEndState()).getDBValue()));
        return loadAuthorOrdersWithConditionsAndPicOrderState(project, new ConditionList(conditions), DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt eine zeitlich sortierte Liste aller nicht freigegebenen {@link iPartsDataAuthorOrder} für den {@code creationUserName},
     * bei denen die aktuelle Benutzergruppe bzw. der aktuelle Benutzer nicht leer ist.
     *
     * @param project
     * @param creationUserName
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListByDelegate(EtkProject project, String creationUserName) {
        // Hier haben wir den Sonderfall, dass wir OR und AND Bedingungen in einer Abfrage haben
        List<AbstractCondition> conditions = new ArrayList<>();
        conditions.add(new Condition(FIELD_DAO_CURRENT_GRP_ID, Condition.OPERATOR_NOT_EQUALS, ""));
        conditions.add(new Condition(FIELD_DAO_CURRENT_USER_ID, Condition.OPERATOR_NOT_EQUALS, ""));
        ConditionList whereOrConditions = new ConditionList(conditions, true);

        conditions.clear();
        conditions.add(new Condition(FIELD_DAO_CREATION_USER_ID, Condition.OPERATOR_EQUALS, creationUserName));
        conditions.add(createConditionListForNotApprovedAuthorOrders(true));
        ConditionList whereConditions = new ConditionList(conditions);
        return loadAuthorOrdersWithConditionsAndPicOrderState(project, whereConditions, whereOrConditions,
                                                              DBActionOrigin.FROM_DB).filterByUser(project, creationUserName);
    }

    /**
     * Erzeugt und lädt eine zeitlich sortierte und für den übergebenen Benutzer gefilterte Liste aller nicht freigegebenen
     * {@link iPartsDataAuthorOrderList}s.
     *
     * @param project
     * @param userName
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListAll(EtkProject project, String userName) {
        return loadAuthorOrdersWithConditionsAndPicOrderState(project, createConditionListForNotApprovedAuthorOrders(true),
                                                              DBActionOrigin.FROM_DB).filterByUser(project, userName);
    }

    /**
     * Lädt eine zeitlich sortierte und für den übergebenen Benutzer gefilterte Liste aller nicht freigegebenen {@link iPartsDataAuthorOrder},
     * bei denen sich der Ersteller von dem übergebenen {@code userName} unterscheidet.
     *
     * @param project
     * @param userName
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListAllExceptUser(EtkProject project, String userName) {
        List<AbstractCondition> conditions = new ArrayList<>();
        conditions.add(createConditionListForNotApprovedAuthorOrders(true));
        conditions.add(new Condition(FIELD_DAO_CREATION_USER_ID, Condition.OPERATOR_NOT_EQUALS, userName));
        return loadAuthorOrdersWithConditionsAndPicOrderState(project, new ConditionList(conditions),
                                                              DBActionOrigin.FROM_DB).filterByUser(project, userName);
    }

    /**
     * Lädt eine zeitlich sortierte Liste aller {@link iPartsDataAuthorOrder}s für die der Ersteller oder aktive Benutzer
     * gleich {@code userName} und der Status weder Start- noch End-Status ist.
     *
     * @param project
     * @param userName
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListActiveOwn(EtkProject project, String userName) {
        // Hier haben wir den Sonderfall, dass wir OR und AND Bedingungen in einer Abfrage haben
        List<AbstractCondition> conditions = new ArrayList<>();
        conditions.add(new Condition(FIELD_DAO_CREATION_USER_ID, Condition.OPERATOR_EQUALS, userName));
        conditions.add(new Condition(FIELD_DAO_CURRENT_USER_ID, Condition.OPERATOR_EQUALS, userName));
        ConditionList whereOrConditions = new ConditionList(conditions, true);

        conditions.clear();
        conditions.add(createConditionListForNotApprovedAuthorOrders(false));
        ConditionList whereConditions = new ConditionList(conditions);
        return loadAuthorOrdersWithConditionsAndPicOrderState(project, whereConditions, whereOrConditions,
                                                              DBActionOrigin.FROM_DB).filterByUser(project, userName);

    }

    /**
     * Erzeugt und lädt eine zeitlich sortierte Liste aller {@link iPartsDataAuthorOrder}s, deren Benutzergruppen sich
     * mit den Benutzergruppen des übergebenen {@code userName} überschneiden und der Status weder Start- noch End-Status ist.
     *
     * @param project
     * @param userName
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListActiveOwnUserGroups(EtkProject project, String userName) {
        iPartsUserAdminCacheElement userCache = iPartsUserAdminCache.getCacheByUserName(userName);
        if (userCache == null) {
            return null;
        }

        List<AbstractCondition> conditions = new ArrayList<>();
        conditions.add(createConditionListForNotApprovedAuthorOrders(false));
        conditions.add(new Condition(FIELD_DAO_CURRENT_GRP_ID, Condition.OPERATOR_NOT_EQUALS, ""));

        // Alle nicht freigegebenen Autoren-Aufträge mit vorhandener aktueller virtueller Benutzergruppe laden
        iPartsDataAuthorOrderList allAOWithGroupId = loadAuthorOrdersWithConditionsAndPicOrderState(project, new ConditionList(conditions),
                                                                                                    DBActionOrigin.FROM_DB);
        allAOWithGroupId = allAOWithGroupId.filterByUser(project, userName);

        // Alle durchlaufen und prüfen, ob die Benutzergruppe Teil der Benutzergruppen des übergebenen Benutzers ist
        iPartsDataAuthorOrderList result = new iPartsDataAuthorOrderList();
        for (iPartsDataAuthorOrder dataAuthorOrder : allAOWithGroupId) {
            if (userCache.isMemberOfVirtualUserGroup(dataAuthorOrder.getCurrentUserGroupId())) {
                result.add(dataAuthorOrder, DBActionOrigin.FROM_DB);
            }
        }
        return result;
    }

    private static iPartsDataAuthorOrderList loadAuthorOrdersWithConditionsAndPicOrderState(EtkProject project, ConditionList whereConditions,
                                                                                            DBActionOrigin origin) {
        return loadAuthorOrdersWithConditionsAndPicOrderState(project, whereConditions, null, origin);
    }

    /**
     * Lädt alle Autorenaufträge für die übergebenen Bedingungen samt Join auf {@code DA_CHANGE_SET_ENTRY}, um an den Gesamtstatus
     * aller möglicher Bildaufträge innerhalb der einzelnen Autorenaufträge zu bestimmen.
     * <p>
     * Die Bildaufträge in freigegebenen Autorenaufträgen werden nicht berücksichtigt.
     *
     * @param project
     * @param whereConditions
     * @param whereOrConditions
     * @param origin
     * @return
     */
    private static iPartsDataAuthorOrderList loadAuthorOrdersWithConditionsAndPicOrderState(EtkProject project, ConditionList whereConditions,
                                                                                            ConditionList whereOrConditions, DBActionOrigin origin) {
        iPartsDataAuthorOrderList result = new iPartsDataAuthorOrderList();

        // Alle Spalten aus DA_AUTHOR_ORDER und DA_CHANGE_SET_ENTRY bestimmen
        EtkDatabaseTable tableDef = project.getConfig().getDBDescription().findTable(TABLE_DA_AUTHOR_ORDER);
        if (tableDef == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "No table definition found for " + TABLE_DA_AUTHOR_ORDER);
            return result;
        }
        Set<String> selectFields = new HashSet<>(tableDef.getAllFieldsNoBlob());

        // Für die Bildauftrags-GUID
        selectFields.add(FIELD_DCE_DO_TYPE);
        selectFields.add(FIELD_DCE_DO_ID);

        selectFields.remove(FIELD_STAMP); // Feld wird nicht benötigt und wäre sonst auch mehrdeutig

        DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        query.select(new Fields(selectFields)).from(TABLE_DA_AUTHOR_ORDER);
        query.where(whereConditions);
        // Fall die Abfrage auch "OR"-Bedingungen enthält, hier hinzufügen
        if (whereOrConditions != null) {
            query.and(whereOrConditions);
        }
        query.join(new LeftOuterJoin(TABLE_DA_CHANGE_SET_ENTRY,
                                     new Condition(TableAndFieldName.make(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CHANGE_SET_ID),
                                                   Condition.OPERATOR_EQUALS, new Fields(TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_GUID))),
                                     new Condition(TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                                   Condition.OPERATOR_EQUALS, iPartsPicOrderModulesId.TYPE)));
        query.orderByDescending(FIELD_DAO_CREATION_DATE);

        DBDataSet dataSet = null;
        try {
            // Query ausführen
            dataSet = query.executeQuery();
            if (dataSet != null) {
                Set<iPartsAuthorOrderId> authorOrdersToSkip = new HashSet<>();
                Map<iPartsAuthorOrderId, iPartsDataAuthorOrder> addedAuthorOrders = new HashMap<>();
                Calendar currentDateTime = Calendar.getInstance();
                while (dataSet.next()) {
                    EtkRecord record = dataSet.getRecord(selectFields);
                    DBDataObjectAttributes attributes = DBDataObjectAttributes.getFromRecord(record, DBActionOrigin.FROM_DB);
                    if (attributes != null) {
                        iPartsAuthorOrderId authorOrderId = new iPartsAuthorOrderId(attributes.getFieldValue(FIELD_DAO_GUID));

                        // Check, ob man den Autorenauftrag-Datensatz überspringen soll
                        if (authorOrdersToSkip.contains(authorOrderId)) {
                            continue;
                        }

                        iPartsAuthorOrderStatus aoState = iPartsAuthorOrderStatus.getFromDBValue(attributes.getFieldValue(FIELD_DAO_STATUS));

                        // Freigegebene Autorenaufträge sollen für den Bildauftrag Status nicht berücksichtigt werden
                        if (iPartsAuthorOrderStatus.isEndState(aoState)) {
                            authorOrdersToSkip.add(authorOrderId);
                            continue;
                        }

                        iPartsDataAuthorOrder authorOrder = addedAuthorOrders.get(authorOrderId);
                        if (authorOrder == null) {
                            // Den Autorenauftrag aus den gefundenen Attributen zusammenbauen
                            authorOrder = result.fillAndAddObjectFromAttributes(project, attributes, origin);
                            // DA_CHANGE_SET_ENTRY Einträge entfernen
                            authorOrder.removeForeignTablesAttributes();
                            addedAuthorOrders.put(authorOrderId, authorOrder);
                        }

                        // Check, ob beim Join auf DA_CHANGE_SET_ENTRY ein Eintrag für den Bildauftrag gefunden wurde
                        if (attributes.getFieldValue(FIELD_DCE_DO_TYPE).equals(iPartsPicOrderModulesId.TYPE)) {
                            // Bildauftrags-GUID aus den ChangeSetEntry-Daten extrahieren
                            IdWithType doId = IdWithType.fromDBString(iPartsPicOrderModulesId.TYPE, attributes.getFieldValue(FIELD_DCE_DO_ID));
                            if ((doId != null) && doId.isValidId()) {
                                iPartsPicOrderModulesId picOrderModulesId = new iPartsPicOrderModulesId(doId.toStringArrayWithoutType());
                                if (picOrderModulesId.isValidId()) {
                                    String picOrderGUID = picOrderModulesId.getOrderGuid();
                                    if (StrUtils.isValid(picOrderGUID)) {
                                        // Mit der GUID den Bildauftrag laden
                                        iPartsDataPicOrder picOrder = new iPartsDataPicOrder(project, new iPartsPicOrderId(picOrderGUID));
                                        // Weitermachen, wenn der Bildauftrag existiert, nicht ungültig gesetzt wurde, nicht storniert wurde,
                                        // nicht erfolgreich abgeschlossen und nicht durch einen Änderungsauftrag ersetzt wurde
                                        if (picOrder.existsInDB() && !picOrder.isInvalid() && !picOrder.isCancelled() && !picOrder.finishedSucessfully() && !picOrder.isReplacedByChange()) {
                                            // Um an einen Gesamtstatus zu kommen, muss folgendes berücksichtigt werden:
                                            // 1. Hat der Bildauftrag einen Status, der relevant für die Autorenauftragssicht ist?
                                            // 2. Ist der Bildauftrag bezüglich seinem Fertigstellungstermin überfällig?
                                            // 3. Hat der Autorenauftrag schon einen Gesamtstatus aus einem anderen Bildauftrag?
                                            //
                                            // Es werden die drei Statuswerte aus den oben genannten Fällen bestimmt und
                                            // bezüglich ihrer Priorität gegeneinander abgeglichen. Der Status mit der
                                            // höchsten Prio wird an den Autorenauftrag geschrieben
                                            iPartsTransferStates stateFromPicOrder = null;

                                            // Check, ob der Status des Bidlafutrags aus der DB relevant für die Autorenauftragssicht ist
                                            iPartsTransferStates picOrderStatus = picOrder.getStatus();
                                            if ((picOrderStatus != null) && picOrderStatus.isRelevantForAutorOrder()) {
                                                stateFromPicOrder = picOrderStatus;
                                            }

                                            // Check, ob der Bildauftrag überfällig ist
                                            Calendar targetDate = picOrder.getFieldValueAsDateTime(FIELD_DA_PO_TARGETDATE);
                                            boolean isOverdue = (targetDate != null) && DateUtils.dateIsAfterDate(currentDateTime.getTime(), targetDate.getTime());
                                            if (isOverdue) {
                                                // Ist der Bildauftrag überfällig, dann prüfe, ob die Prio höher ist als
                                                // die vom Status des Bildauftrags (Fall 1 und 2 von oben)
                                                if ((stateFromPicOrder == null) || iPartsTransferStates.OVERDUE.hasHigherPrioThan(stateFromPicOrder)) {
                                                    stateFromPicOrder = iPartsTransferStates.OVERDUE;
                                                }
                                            }

                                            // Ist der Bildauftragstatus nicht relevant für die Autorenauftragssicht und
                                            // Bildauftrag nicht überfällig, dann mache mit dem nächsten Treffer weiter
                                            if (stateFromPicOrder == null) {
                                                continue;
                                            }

                                            // Vergleiche den aktuellen Status am Autorenafutrag (kann auch null sein) mit
                                            // dem Status am gefundenen Bildauftrag und setze den Status neu falls die Priorität
                                            // höher ist bzw. es bisher noch keinen Status gegeben hat
                                            iPartsTransferStates currentState = authorOrder.getCurrentPicOrderState();
                                            if (stateFromPicOrder.hasHigherPrioThan(currentState)) {
                                                authorOrder.setFieldValue(iPartsDataVirtualFieldsDefinition.DAO_TOTAL_PICORDERS_STATE,
                                                                          stateFromPicOrder.getDBValue(), DBActionOrigin.FROM_DB);
                                            }

                                            // Hat der gesetzte Status die höchste Priorität, dann braucht man weitere Treffer
                                            // nicht mehr prüfen
                                            if (stateFromPicOrder.hasHighestPrio()) {
                                                authorOrdersToSkip.add(authorOrderId);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            // Verbindung schließen
            if (dataSet != null) {
                dataSet.close();
            }
        }
        return result;
    }
}
