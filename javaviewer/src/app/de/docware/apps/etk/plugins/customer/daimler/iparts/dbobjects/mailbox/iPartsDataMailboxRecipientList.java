/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.ArrayUtil;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;
import java.util.Set;

public class iPartsDataMailboxRecipientList extends EtkDataObjectList<iPartsDataMailboxRecipient> implements iPartsConst {

    public iPartsDataMailboxRecipientList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Funktion, die die Ergebnisfelder für alle Select-Statements zusammenstellt.
     *
     * @param project
     * @return
     */
    private EtkDisplayFields getSelectFields(EtkProject project) {
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_MESSAGE_TO));
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_MESSAGE));
        return selectFields;
    }

    /**
     * Sortierfelder:
     * Prinzipiell sollen alle in dieser Klasse zusammengestellten Ergebnislisten gleich sortiert sein,
     * daher gibt es diese zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return
     */
    private String[] getSortFields() {
        String[] sortFields = new String[]{ FIELD_DMSG_CREATION_DATE };
        return sortFields;
    }

    /**
     * Erzeugt und lädt eine Liste ALLER {@link iPartsDataMailboxRecipient}s aus DA_MESSAGE_TO
     * mit den dazu-ge-joint-en Daten aus DA_MESSAGE.
     *
     * @param project
     * @param origin
     */
    public void loadAllMailboxMessages(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = getSelectFields(project);
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DMT_GUID },
                                  TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_GUID },
                                  false, false, null,
                                  null, false, getSortFields(), false);

    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMailboxRecipient}s für die gesendeten Nachrichten von einem User
     * aus DE_MESSAGE_TO mit den dazu-ge-joint-en Daten aus DE_MESSAGE.
     *
     * @param project
     * @param creationUserId
     * @param includeReadMessages Sollen auch die gelesenen Nachrichten mitgeladen werden?
     */
    public void loadAllMailboxMessagesForCreator(EtkProject project, String creationUserId, boolean includeReadMessages) {
        clear(DBActionOrigin.FROM_DB);

        int whereFieldsSize = 2;
        if (includeReadMessages) {
            whereFieldsSize = 1;
        }
        String[] whereFields = new String[whereFieldsSize];
        String[] whereValues = new String[whereFieldsSize];
        whereFields[0] = TableAndFieldName.make(TABLE_DA_MESSAGE, FIELD_DMSG_CREATION_USER_ID);
        whereValues[0] = creationUserId;

        if (!includeReadMessages) {
            whereFields[1] = TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_READ_BY_USER_ID);
            whereValues[1] = "";
        }

        searchSortAndFillWithJoin(project, null, getSelectFields(project), whereFields, whereValues, false, getSortFields(),
                                  false, null, false, false, false, null, false,
                                  new EtkDataObjectList.JoinData(TABLE_DA_MESSAGE,
                                                                 new String[]{ FIELD_DMT_GUID },
                                                                 new String[]{ FIELD_DMSG_GUID },
                                                                 false, false));
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMailboxRecipient}s für einen User aus DA_MESSAGE_TO
     * mit den dazu-ge-joint-en Daten aus DA_MESSAGE.
     *
     * @param project
     * @param origin
     */
    public void loadAllMailboxMessagesForRecipient(EtkProject project, iPartsMailboxRecipientId id, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = getSelectFields(project);
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DMT_GUID },
                                  TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_GUID },
                                  false, false,
                                  new String[]{ TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_USER_ID) }, new String[]{ id.getUserID() },
                                  false, getSortFields(), false);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMailboxRecipient}s für einen Gruppe aus DA_MESSAGE_TO
     * mit den dazu-ge-joint-en Daten aus DA_MESSAGE.
     *
     * @param project
     * @param origin
     */
    public void loadAllMailboxMessagesForGroup(EtkProject project, iPartsMailboxRecipientId id, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = getSelectFields(project);
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DMT_GUID },
                                  TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_GUID },
                                  false, false,
                                  new String[]{ TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_GROUP_ID) },
                                  new String[]{ id.getGroupID() },
                                  false, getSortFields(), false);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMailboxRecipient}s für eine Rolle in einer Organisation
     * aus DA_MESSAGE_TO mit den dazu-ge-joint-en Daten aus DA_MESSAGE.
     *
     * @param project
     * @param origin
     */
    public void loadAllMailboxMessagesForOrganisationRole(EtkProject project, iPartsMailboxRecipientId id, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = getSelectFields(project);
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DMT_GUID },
                                  TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_GUID },
                                  false, false,
                                  new String[]{ TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_ORGANISATION_ID),
                                                TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_ROLE_ID) },
                                  new String[]{ id.getOrganisationID(), id.getRoleID() },
                                  false, getSortFields(), false);
    }


    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMailboxRecipient}s für eine Rolle in einer Gruppe
     * einer Organisation  aus DA_MESSAGE_TO mit den dazu-ge-joint-en Daten aus DA_MESSAGE.
     *
     * @param project
     * @param origin
     */
    public void loadAllMailboxMessagesForOrganisationGroupRole(EtkProject project, iPartsMailboxRecipientId id, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = getSelectFields(project);
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DMT_GUID },
                                  TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_GUID },
                                  false, false,
                                  new String[]{ TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_GROUP_ID),
                                                TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_ORGANISATION_ID),
                                                TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_ROLE_ID) },
                                  new String[]{ id.getGroupID(),
                                                id.getOrganisationID(),
                                                id.getRoleID() },
                                  false, getSortFields(), false);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMailboxRecipient}s für eine GUID aus {@code DA_MESSAGE_TO} mit den
     * dazu-ge-joint-en Daten aus {@code DA_MESSAGE}.
     *
     * @param project
     * @param messageGUID
     * @param origin
     */
    public void loadAllMailboxMessagesForGUID(EtkProject project, String messageGUID, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = getSelectFields(project);
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DMT_GUID },
                                  TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_GUID },
                                  false, false,
                                  new String[]{ TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_GUID) },
                                  new String[]{ messageGUID },
                                  false, getSortFields(), false);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMailboxRecipient}s bei denen entweder die
     * UserId oder virtuelle Benutzergruppe oder Organisationseinheit übereinstimmen.
     *
     * @param project
     * @param userId
     * @param virtualUserGroupIds
     * @param organisationId
     * @param includeReadMessages Sollen auch die gelesenen Nachrichten mitgeladen werden?
     */
    public void loadAllMailboxMessagesForUserOrGroupOrOrg(EtkProject project, String userId, Set<String> virtualUserGroupIds,
                                                          String organisationId, boolean includeReadMessages) {
        clear(DBActionOrigin.FROM_DB);
        // folgendes Statement wird hier gebaut:
        // (Rolle = leer) UND (UserId ODER Organisation ODER Benutzergruppe passt zum User) UND ggf. (Gelesen = leer)
        // Erste Dimension = UND; Zweite Dimension = ODER
        int firstDimSize = 3;
        if (includeReadMessages) {
            firstDimSize = 2;
        }
        String[][] whereFields = new String[firstDimSize][];
        String[][] whereValues = new String[firstDimSize][];
        EtkDataObjectList.addElemsTo2dArray(whereFields, 0, TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_ROLE_ID));
        EtkDataObjectList.addElemsTo2dArray(whereValues, 0, "");

        List<String> whereFieldList = new DwList<>();
        List<String> whereValueList = new DwList<>();
        whereFieldList.add(TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_USER_ID));
        whereValueList.add(userId);
        whereFieldList.add(TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_ORGANISATION_ID));
        whereValueList.add(organisationId);
        for (String virtualUserGroupId : virtualUserGroupIds) {
            whereFieldList.add(TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_GROUP_ID));
            whereValueList.add(virtualUserGroupId);
        }
        EtkDataObjectList.addElemsTo2dArray(whereFields, 1, ArrayUtil.toArray(whereFieldList));
        EtkDataObjectList.addElemsTo2dArray(whereValues, 1, ArrayUtil.toArray(whereValueList));

        if (!includeReadMessages) {
            EtkDataObjectList.addElemsTo2dArray(whereFields, 2, TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_READ_BY_USER_ID));
            EtkDataObjectList.addElemsTo2dArray(whereValues, 2, "");
        }

/*
  Beispiel:
  ---------

       select da_message_to.dmt_guid, da_message_to.dmt_user_id, da_message_to.dmt_group_id,
              da_message_to.dmt_organisation_id, da_message_to.dmt_role_id, da_message_to.dmt_read_by_user_id,
              da_message_to.dmt_read_date, da_message_to.t_stamp, da_message.dmsg_guid, da_message.dmsg_type,
              da_message.dmsg_do_type, da_message.dmsg_do_id, da_message.dmsg_series_no, da_message.dmsg_subject,
              da_message.dmsg_message, da_message.dmsg_creation_user_id, da_message.dmsg_creation_date, da_message.t_stamp
         from ipartsora.da_message
   inner join ipartsora.da_message_to on ((da_message_to.dmt_guid = da_message.dmsg_guid))
        where (da_message_to.dmt_role_id = ' '
               and (da_message_to.dmt_user_id = 'sg'
                    or da_message_to.dmt_organisation_id = 'DAIMLER'
                    or da_message_to.dmt_group_id = '@@vug@@DAIMLER&[IPARTS.Authors]'
                    or da_message_to.dmt_group_id = '@@vug@@DAIMLER&[IPARTS.QualityInspectors]'
                   )
               and da_message_to.dmt_read_by_user_id = ' '
              ):
*/

        searchSortAndFillWithJoin(project, null, getSelectFields(project),
                                  whereFields, whereValues,
                                  false, getSortFields(), null, false,
                                  null, false, false, false,
                                  null,
                                  false, new EtkDataObjectList.JoinData(TABLE_DA_MESSAGE,
                                                                        new String[]{ FIELD_DMT_GUID },
                                                                        new String[]{ FIELD_DMSG_GUID },
                                                                        false, false));
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMailboxRecipient}s bei denen die Organisationseinheit leer
     * ist oder passt und die Rolle zum User passt.
     *
     * @param project
     * @param organisationId
     * @param roleIds
     * @param includeReadMessages sollen auch die gelesene Nachrichten mitgeladen werden?
     */
    public void loadAllMailboxMessagesForOrgAndOrRole(EtkProject project, String organisationId, Set<String> roleIds,
                                                      boolean includeReadMessages) {
        clear(DBActionOrigin.FROM_DB);

        // folgendes Statement wird hier gebaut:
        // (Organisation = leer ODER passt zum User) UND (Rolle passt zum User) UND ggf. (Gelesen = leer)
        // Erste Dimension = UND; Zweite Dimension = ODER

        int firstDimSize = 3;
        if (includeReadMessages) {
            firstDimSize = 2;
        }
        String[][] whereFields = new String[firstDimSize][];
        String[][] whereValues = new String[firstDimSize][];
        EtkDataObjectList.addElemsTo2dArray(whereFields, 0,
                                            TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_ORGANISATION_ID),
                                            TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_ORGANISATION_ID));
        EtkDataObjectList.addElemsTo2dArray(whereValues, 0, "", organisationId);

        List<String> whereFieldList = new DwList<>();
        List<String> whereValueList = new DwList<>();
        for (String roleId : roleIds) {
            whereFieldList.add(TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_ROLE_ID));
            whereValueList.add(roleId);
        }
        EtkDataObjectList.addElemsTo2dArray(whereFields, 1, ArrayUtil.toArray(whereFieldList));
        EtkDataObjectList.addElemsTo2dArray(whereValues, 1, ArrayUtil.toArray(whereValueList));

        if (!includeReadMessages) {
            EtkDataObjectList.addElemsTo2dArray(whereFields, 2, TableAndFieldName.make(TABLE_DA_MESSAGE_TO, FIELD_DMT_READ_BY_USER_ID));
            EtkDataObjectList.addElemsTo2dArray(whereValues, 2, "");
        }

/*
  Beispiel:
  ---------

       select da_message_to.dmt_guid, da_message_to.dmt_user_id, da_message_to.dmt_group_id,
              da_message_to.dmt_organisation_id, da_message_to.dmt_role_id, da_message_to.dmt_read_by_user_id,
              da_message_to.dmt_read_date, da_message_to.t_stamp, da_message.dmsg_guid, da_message.dmsg_type,
              da_message.dmsg_do_type, da_message.dmsg_do_id, da_message.dmsg_series_no, da_message.dmsg_subject,
              da_message.dmsg_message, da_message.dmsg_creation_user_id, da_message.dmsg_creation_date, da_message.t_stamp
         from ipartsora.da_message
   inner join ipartsora.da_message_to on ((da_message_to.dmt_guid = da_message.dmsg_guid))
        where (
                (
                     da_message_to.dmt_organisation_id = ' '
                  or da_message_to.dmt_organisation_id = 'DAIMLER'
                )
                and
                (
                     da_message_to.dmt_role_id = 'IPARTS.Admin'
                  or da_message_to.dmt_role_id = 'IPARTS.QualityInspector'
                  or da_message_to.dmt_role_id = 'IPARTS.Author'
                  or da_message_to.dmt_role_id = 'IPARTS.Reader'
                  or da_message_to.dmt_role_id = 'RegisteredUser'
                  or da_message_to.dmt_role_id = 'UserAdminAdmin'
                )
                and
                (
                  da_message_to.dmt_read_by_user_id = ' '
                )
              )
*/

        searchSortAndFillWithJoin(project, null, getSelectFields(project),
                                  whereFields, whereValues,
                                  false, getSortFields(), null, false,
                                  null, false, false, false,
                                  null,
                                  false, new EtkDataObjectList.JoinData(TABLE_DA_MESSAGE,
                                                                        new String[]{ FIELD_DMT_GUID },
                                                                        new String[]{ FIELD_DMSG_GUID },
                                                                        false, false));
    }

    /**
     * Lädt alle {@link iPartsDataMailboxRecipient}s für Nachrichten mit Wiedervorlagedatum.
     *
     * @param project
     */
    public void loadAllMailboxMessagesWithResubmissionDate(EtkProject project) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereNotFields = new String[]{ TableAndFieldName.make(TABLE_DA_MESSAGE, FIELD_DMSG_RESUBMISSION_DATE) };
        String[] whereNotValues = new String[]{ "" };
        searchSortAndFillWithJoin(project, null, getSelectFields(project), null, null, whereNotFields, whereNotValues,
                                  false, getSortFields(), false, false, null,
                                  new EtkDataObjectList.JoinData(TABLE_DA_MESSAGE,
                                                                 new String[]{ FIELD_DMT_GUID },
                                                                 new String[]{ FIELD_DMSG_GUID },
                                                                 false, false));
    }

    @Override
    protected iPartsDataMailboxRecipient getNewDataObject(EtkProject project) {
        return new iPartsDataMailboxRecipient(project, null);
    }
}
