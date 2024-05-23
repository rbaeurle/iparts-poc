/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author;

import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.ChangeSetId;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.combimodules.useradmin.db.RightScope;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.sql.terms.AbstractCondition;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.ConditionList;
import de.docware.util.sql.terms.Fields;

import java.util.*;

/**
 * Liste von {@link iPartsDataAuthorOrder}s.
 */
public class iPartsDataAuthorOrderList extends EtkDataObjectList<iPartsDataAuthorOrder> implements iPartsConst {

    public iPartsDataAuthorOrderList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert die {@link iPartsDataAuthorOrder} zum aktuell aktiven {@link iPartsRevisionChangeSet} für Edit im übergebenen
     * {@link EtkProject}.
     *
     * @param project
     * @return {@code null}, falls kein {@link iPartsRevisionChangeSet} aktiv oder keinen {@link iPartsDataAuthorOrder} gefunden
     */
    public static iPartsDataAuthorOrder getAuthorOrderByActiveChangeSetForEdit(EtkProject project) {
        EtkRevisionsHelper revisionsHelper = project.getEtkDbs().getRevisionsHelper();
        if ((revisionsHelper != null) && revisionsHelper.isRevisionChangeSetActiveForEdit()) {
            AbstractRevisionChangeSet activeEditChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
            if (activeEditChangeSet instanceof iPartsRevisionChangeSet) {
                return getAuthorOrderByChangeSet(activeEditChangeSet);
            }
        }
        return null;
    }

    /**
     * Liefert die {@link iPartsDataAuthorOrder} zum aktuell aktiven {@link iPartsRevisionChangeSet} (nicht für Edit) im übergebenen
     * {@link EtkProject}.
     *
     * @param project
     * @return {@code null}, falls kein {@link iPartsRevisionChangeSet} aktiv oder keinen {@link iPartsDataAuthorOrder} gefunden
     */
    public static iPartsDataAuthorOrder getAuthorOrderByActiveChangeSet(EtkProject project) {
        EtkRevisionsHelper revisionsHelper = project.getRevisionsHelper();
        if (revisionsHelper != null) {
            Collection<AbstractRevisionChangeSet> activeRevisionChangeSets = revisionsHelper.getActiveRevisionChangeSets();
            if ((activeRevisionChangeSets != null) && !activeRevisionChangeSets.isEmpty()) {
                AbstractRevisionChangeSet activeChangeSet = activeRevisionChangeSets.iterator().next();
                if (activeChangeSet instanceof iPartsRevisionChangeSet) {
                    return iPartsDataAuthorOrderList.getAuthorOrderByChangeSet(activeChangeSet);
                }
            }
        }
        return null;
    }

    /**
     * Liefert die {@link iPartsDataAuthorOrder} zum übergebenen {@link iPartsRevisionChangeSet}.
     *
     * @param changeSet
     * @return {@code null} falls kein {@link iPartsRevisionChangeSet} aktiv oder keinen {@link iPartsDataAuthorOrder} gefunden
     */
    public static iPartsDataAuthorOrder getAuthorOrderByChangeSet(AbstractRevisionChangeSet changeSet) {
        return getAuthorOrderByChangeSetId(changeSet.getProject(), changeSet.getChangeSetId());
    }

    /**
     * Liefert die {@link iPartsDataAuthorOrder} zur übergebenen {@link ChangeSetId}.
     *
     * @param project
     * @param changeSetId
     * @return {@code null} falls kein {@link iPartsRevisionChangeSet} aktiv oder keinen {@link iPartsDataAuthorOrder} gefunden
     */
    public static iPartsDataAuthorOrder getAuthorOrderByChangeSetId(EtkProject project, ChangeSetId changeSetId) {
        iPartsDataAuthorOrderList list = new iPartsDataAuthorOrderList();
        list.loadAuthorOrdersByChangeSetFromDB(project, changeSetId.getGUID(), DBActionOrigin.FROM_DB);
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Erzeugt und lädt die Liste der aktivierbaren Autoren-Aufräge für den übergebenen {@code userName}.
     *
     * @param project
     * @param userName
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListByActivateable(EtkProject project, String userName) {
        iPartsDataAuthorOrderList list = new iPartsDataAuthorOrderList();
        list.loadAuthorOrdersActivateableForUserFromDB(project, userName, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine zeitlich sortierte Liste aller freigegebenen {@link iPartsDataAuthorOrderList}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataAuthorOrderList loadAuthorOrderListAllApproved(EtkProject project) {
        iPartsDataAuthorOrderList list = new iPartsDataAuthorOrderList();
        list.loadAllApprovedAuthorOrdersFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine zeitlich sortierte Liste aller {@link iPartsDataAuthorOrder} für den übergebenen {@code creationUserName},
     * {@code currentUserName} oder Status. Sind alle null, so werden alle {@link iPartsDataAuthorOrder} geladen.
     *
     * @param project
     * @param origin
     */
    private void loadAllApprovedAuthorOrdersFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_AUTHOR_ORDER, new String[]{ FIELD_DAO_STATUS },
                          new String[]{ iPartsAuthorOrderStatus.getEndState().getDBValue() },
                          new String[]{ FIELD_DAO_CREATION_DATE }, LoadType.COMPLETE, true, origin);

    }

    /**
     * Freigegebene Datensätze, deren Freigabedatum zwischen dateFrom und dateTo liegt
     *
     * @param project
     * @param dateFrom
     * @param dateTo
     * @return
     */
    public static iPartsDataAuthorOrderList loadAllApprovedAuthorOrdersInTimeIntervallFromDB(EtkProject project, String dateFrom, String dateTo) {

        iPartsDataAuthorOrderList list = new iPartsDataAuthorOrderList();

        // Alle Spalten aus DA_AUTHOR_ORDER bestimmen
        EtkDatabaseTable tableDef = project.getConfig().getDBDescription().findTable(TABLE_DA_AUTHOR_ORDER);
        if (tableDef == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "No table definition found for " + TABLE_DA_AUTHOR_ORDER);
            return list;
        }
        Set<String> selectFields = new HashSet<>(tableDef.getAllFieldsNoBlob());
        selectFields.remove(FIELD_STAMP);

        DBSQLQuery query = project.getEtkDbs().getDBForDomain(DBDatabaseDomain.MAIN).getNewQuery();
        query.select(new Fields(selectFields)).from(TABLE_DA_AUTHOR_ORDER);

        List<AbstractCondition> conditions = new ArrayList<>();
        conditions.add(new Condition(iPartsConst.FIELD_DAO_STATUS, Condition.OPERATOR_EQUALS, iPartsAuthorOrderStatus.getEndState().getDBValue()));
        // Condition sieht so aus: FIELD_DAO_RELDATE between 'dateFrom' and 'dateTo'
        // Das 'and' wird durch die Aneindanderreihung der Conditions hinzugefügt
        conditions.add(new Condition(iPartsConst.FIELD_DAO_RELDATE, "between", dateFrom));
        conditions.add(new Condition("", "", dateTo));

        ConditionList whereConditions = new ConditionList(conditions);
        query.where(whereConditions);
        query.orderByDescending(iPartsConst.FIELD_DAO_RELDATE);

        DBDataSet dataSet = null;
        try {
            dataSet = query.executeQuery();
            if (dataSet != null) {
                while (dataSet.next()) {
                    EtkRecord record = dataSet.getRecord(selectFields);
                    DBDataObjectAttributes attributes = DBDataObjectAttributes.getFromRecord(record, DBActionOrigin.FROM_DB);
                    if (attributes != null) {
                        list.fillAndAddObjectFromAttributes(project, attributes, DBActionOrigin.FROM_DB);
                    }
                }
            }

        } finally {
            if (dataSet != null) {
                dataSet.close();
            }
        }

        return list;
    }

    /**
     * Sucht den {@link iPartsDataAuthorOrder} für die übergebene {@code editRevisionChangeSetId}.
     *
     * @param project
     * @param editRevisionChangeSetId
     * @param origin
     */
    private void loadAuthorOrdersByChangeSetFromDB(EtkProject project, String editRevisionChangeSetId, DBActionOrigin origin) {
        clear(origin);

        String[] sortFields = new String[]{ FIELD_DAO_CREATION_DATE };
        String[] whereFields = new String[]{ FIELD_DAO_CHANGE_SET_ID };
        String[] whereValues = new String[]{ editRevisionChangeSetId };

        searchSortAndFill(project, TABLE_DA_AUTHOR_ORDER, whereFields, whereValues, sortFields, LoadType.COMPLETE, true, origin);
    }

    /**
     * Lädt die Liste der aktivierbaren Autoren-Aufräge für den übergebenen {@code userName}.
     *
     * @param project
     * @param userName
     * @param origin
     */
    private void loadAuthorOrdersActivateableForUserFromDB(EtkProject project, String userName, DBActionOrigin origin) {
        clear(origin);

        String[] sortFields = new String[]{ FIELD_DAO_CREATION_DATE };
        String[] whereFields = new String[]{ FIELD_DAO_CURRENT_USER_ID };
        String[] whereValues = new String[]{ userName };

        EnumSet<iPartsAuthorOrderStatus> notActivateableStates = iPartsAuthorOrderStatus.getAllNonActivateableStates();
        String[] whereNotFields = new String[notActivateableStates.size()];
        String[] whereNotValues = new String[notActivateableStates.size()];
        int index = 0;
        for (iPartsAuthorOrderStatus aoStatus : notActivateableStates) {
            whereNotFields[index] = FIELD_DAO_STATUS;
            whereNotValues[index] = aoStatus.getDBValue();
            index++;
        }

        searchSortAndFill(project, TABLE_DA_AUTHOR_ORDER, null, whereFields, whereValues,
                          whereNotFields, whereNotValues, sortFields, LoadType.COMPLETE, true, origin);
    }

    @Override
    protected iPartsDataAuthorOrder getNewDataObject(EtkProject project) {
        return new iPartsDataAuthorOrder(project, null);
    }

    public iPartsDataAuthorOrder fillAndAddObjectFromAttributes(EtkProject project, DBDataObjectAttributes attributes, DBActionOrigin origin) {
        return fillAndAddDataObjectFromAttributes(project, attributes, LoadType.COMPLETE, true, origin);
    }

    @Override
    protected iPartsDataAuthorOrder fillAndAddDataObjectFromAttributes(EtkProject project, DBDataObjectAttributes attributes, LoadType loadType,
                                                                       boolean addToList, DBActionOrigin origin) {
        iPartsDataAuthorOrder dataAuthorOrder = super.fillAndAddDataObjectFromAttributes(project, attributes, loadType,
                                                                                         addToList, origin);

        // Gesamtstatus Bildaufträge initial auf leer setzen
        dataAuthorOrder.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DAO_TOTAL_PICORDERS_STATE, "", true,
                                                 origin);
        return dataAuthorOrder;
    }

    /**
     * Filtert diese Liste von Autoren-Aufträgen für den übergebenen Benutzer anhand dessen Organisation, BST ID und Rechten.
     *
     * @param userName
     * @return Gefilterte Liste der Autoren-Aufträge oder diese Liste falls keine Filterung notwendig ist
     */
    public iPartsDataAuthorOrderList filterByUser(EtkProject project, String userName) {
        iPartsDataAuthorOrderList filteredAuthorOrderList = new iPartsDataAuthorOrderList();
        iPartsUserAdminCacheElement userCache = iPartsUserAdminCache.getCacheByUserName(userName);
        if (userCache == null) { // Ohne gültigen Benutzer gar keine Autoren-Aufträge zurückgeben
            return filteredAuthorOrderList;
        }

        boolean isCarAndVan = iPartsRight.checkCarAndVanInSession();
        boolean isTruckAndBus = iPartsRight.checkTruckAndBusInSession();

        RightScope rightScopForViewAuthorOrders = userCache.getUserRightScope(iPartsRight.VIEW_AUTHOR_ORDERS);
        if (rightScopForViewAuthorOrders == RightScope.NONE) {
            return filteredAuthorOrderList; // Ohne das Recht gar keine Autoren-Aufträge zurückgeben
        }
        boolean hasGlobalViewRight = rightScopForViewAuthorOrders == RightScope.GLOBAL;

        iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(userCache.getOrgId());
        if (orgCache == null) { // Ohne gültige Organisation gar keine Autoren-Aufträge zurückgeben
            return filteredAuthorOrderList;
        }

        boolean isInternalOrganisation = orgCache.isInternalOrganisation();

        // Ab Daimler-9736 wird über die BST-Id gefiltert und nicht mehr über den zugewiesenen Benutzer
        // bzw. der zugewiesenen Gruppe
        String supplierIdOfUserOrga = orgCache.getBSTSupplierId();
        for (iPartsDataAuthorOrder dataAuthorOrder : this) {
            boolean isAuthorOrderValid;
            String bstId = dataAuthorOrder.getBstId();
            if (!bstId.isEmpty()) {
                iPartsWorkOrderId id = new iPartsWorkOrderId(bstId);
                iPartsDataWorkOrder currentOrder = iPartsWorkOrderCache.getInstance(id, project).getDataWorkOrder(project);
                if (hasGlobalViewRight || isInternalOrganisation) {  // Der Gültigkeitsbereich GLOBAL verhält sich wie das Flag interne Organisation
                    isAuthorOrderValid = currentOrder.isVisibleForUserProperties(isCarAndVan, isTruckAndBus);

                    // Autoren-Aufträge (z.B. nach einem Import) sollen im DEV-Modus sichtbar sein, wenn es den Arbeitsauftrag
                    // nicht gibt (dann hat er Status neu), der Benutzer aber globale Rechte hat
                    if (!isAuthorOrderValid && Constants.DEVELOPMENT && hasGlobalViewRight && currentOrder.isNew()) {
                        isAuthorOrderValid = true;
                    }
                } else {
                    String supplierId = currentOrder.getSupplierNo();
                    isAuthorOrderValid = supplierId.equals(supplierIdOfUserOrga);
                }
            } else {
                isAuthorOrderValid = hasGlobalViewRight; // Mit globalem Recht sind auch Autoren-Aufträge ohne Arbeitsauftrags-ID gültig
            }
            if (isAuthorOrderValid) {
                filteredAuthorOrderList.add(dataAuthorOrder, DBActionOrigin.FROM_DB);
            }
        }

        return filteredAuthorOrderList;
    }
}
