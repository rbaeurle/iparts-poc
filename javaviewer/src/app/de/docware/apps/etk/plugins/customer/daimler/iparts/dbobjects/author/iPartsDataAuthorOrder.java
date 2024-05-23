/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDataAOHistoryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.framework.combimodules.useradmin.db.RightScope;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.Calendar;
import java.util.Set;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_AUTHOR_ORDER.
 */
public class iPartsDataAuthorOrder extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DAO_GUID };

    public static final String CHILDREN_NAME_HISTORIES = "iPartsDataAuhtorOrder.histories";

    public static String getLoginAcronym() {
        return iPartsUserAdminDb.getLoginUserName();
    }

    protected iPartsDataAOHistoryList historyList;
    protected iPartsDataChangeSet dataChangeSet = null;

    public iPartsDataAuthorOrder(EtkProject project, iPartsAuthorOrderId id) {
        super(KEYS);
        tableName = TABLE_DA_AUTHOR_ORDER;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsAuthorOrderId(idValues[0]);
    }

    @Override
    public iPartsAuthorOrderId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsAuthorOrderId)id;
    }

    @Override
    public void setChildren(String childrenName, DBDataObjectList<? extends DBDataObject> children) {
        super.setChildren(childrenName, children);
        if (childrenName.equals(CHILDREN_NAME_HISTORIES)) {
            historyList = (iPartsDataAOHistoryList)children;
        }
    }

    @Override
    public iPartsDataAuthorOrder cloneMe(EtkProject project) {
        iPartsDataAuthorOrder clone = new iPartsDataAuthorOrder(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public void clear(DBActionOrigin origin) {
        super.clear(origin);
        setChildren(CHILDREN_NAME_HISTORIES, null);
    }

    @Override
    public void initAttributesWithEmptyValues(DBActionOrigin origin) {
        super.initAttributesWithEmptyValues(origin);

        // Gesamtstatus Bildaufträge initial auf leer setzen
        getAttributes().addField(iPartsDataVirtualFieldsDefinition.DAO_TOTAL_PICORDERS_STATE, "", true, DBActionOrigin.FROM_DB);
    }

    protected synchronized void loadHistories() {
        if (historyList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_HISTORIES, iPartsDataAOHistoryList.loadAOHistoryList(getEtkProject(), getAsId().getAuthorGuid(), false));
    }

    public iPartsDataAOHistoryList getHistories() {
        loadHistories();
        return historyList;
    }

    public synchronized void addHistory(iPartsDataAOHistory aoHistory, DBActionOrigin origin) {
        getHistories().add(aoHistory, origin);
    }

    @Override
    public void deleteFromDB(boolean forceDelete) {
        getDataChangeSet();
        if (dataChangeSet != null) {
            dataChangeSet.deleteFromDB(forceDelete);
        }
        super.deleteFromDB(forceDelete);
    }

    // Convenience Method
    public void setStatus(iPartsAuthorOrderStatus state, DBActionOrigin origin) {
        setFieldValue(FIELD_DAO_STATUS, state.getDBValue(), origin);
    }

    public iPartsAuthorOrderStatus getStatus() {
        return iPartsAuthorOrderStatus.getFromDBValue(getFieldValue(iPartsConst.FIELD_DAO_STATUS));
    }

    public void setCurrentCreationDate(DBActionOrigin origin) {
        setFieldValueAsDateTime(FIELD_DAO_CREATION_DATE, Calendar.getInstance(), origin);
    }

    /**
     * Setzt den Login-Namen des Erstellers.
     *
     * @param userName
     * @param origin
     */
    public void setCreationUser(String userName, DBActionOrigin origin) {
        setFieldValue(FIELD_DAO_CREATION_USER_ID, userName, origin);
    }

    /**
     * Liefert den Login-Namen des Erstellers zurück.
     *
     * @return
     */
    public String getCreationUser() {
        return getFieldValue(iPartsConst.FIELD_DAO_CREATION_USER_ID);
    }

    /**
     * Überprüft, ob der aktuell eingeloggte Benutzer dem Ersteller entspricht.
     *
     * @return
     */
    public boolean isCreationUserEqualLoginUser() {
        return getCreationUser().equals(getLoginAcronym());
    }

    /**
     * Liefert den Login-Namen des aktuellen Benutzers zurück.
     *
     * @return
     */
    public String getCurrentUser() {
        return getFieldValue(iPartsConst.FIELD_DAO_CURRENT_USER_ID);
    }

    /**
     * Überprüft, ob der aktuell eingeloggte Benutzer dem aktuellen Bearbeiter entspricht.
     *
     * @return
     */
    public boolean isCurrentUserEqualLoginUser() {
        return getCurrentUser().equals(getLoginAcronym());
    }

    /**
     * Setzt den Login-Namen des aktuellen Benutzers.
     *
     * @param userName
     * @param origin
     */
    public void setCurrentUser(String userName, DBActionOrigin origin) {
        setFieldValue(FIELD_DAO_CURRENT_USER_ID, userName, origin);
    }

    /**
     * Überprüft, ob der aktuell eingeloggte Benutzer dem aktuellen Bearbeiter entspricht bzw. als Fallback dem Ersteller
     * falls noch kein aktueller Bearbeiter und keine aktuelle virtuelle Benutzergruppe zugewiesen wurde.
     *
     * @return
     */
    public boolean isCurrentUserEqualLoginUserWithFallbackToCreationUser() {
        String currentUser = getCurrentUser();
        if (currentUser.isEmpty() && getCurrentUserGroupId().isEmpty()) {
            return isCreationUserEqualLoginUser();
        } else {
            return currentUser.equals(getLoginAcronym());
        }
    }

    /**
     * Überprüft, ob der aktuell eingeloggte Benutzer dem aktuellen Bearbeiter entspricht, Mitglieder der aktuellen virtuellen
     * Benutzergruppe ist oder als Fallback dem Ersteller entspricht falls noch kein aktueller Bearbeiter zugewiesen wurde
     * und es keine aktuelle virtuelle Benutzergruppe gibt.
     *
     * @param forceAssignUserOrGroup Handelt es sich um eine Übernahme trotz bereits vorhandener Zuweisung an einen anderen
     *                               Benutzer?
     * @return
     */
    public boolean isAssignAllowedForCurrentUser(boolean forceAssignUserOrGroup) {
        String currentUser = getCurrentUser();
        if (currentUser.isEmpty() || forceAssignUserOrGroup) {
            String currentVirtualUserGroupId = getCurrentUserGroupId();
            if (currentVirtualUserGroupId.isEmpty()) {
                if (forceAssignUserOrGroup) {
                    if (isCurrentUserEqualLoginUser()) {
                        return true;
                    } else {
                        String loginUserId = iPartsUserAdminDb.getLoginUserIdForSession();
                        if (loginUserId != null) {
                            return isForceAssignAllowed(loginUserId, getCreationUserGroupId());
                        } else {
                            return false;
                        }
                    }
                } else {
                    return isCreationUserEqualLoginUser();
                }
            } else {
                // Prüfen, ob der eingeloggte Benutzer Mitglied der aktuellen virtuellen Benutzergruppe ist
                String loginUserId = iPartsUserAdminDb.getLoginUserIdForSession();
                if (loginUserId != null) {
                    // Bei Übernahme mit globalem Recht nicht die aktuelle virtuellen Benutzergruppe prüfen
                    if (forceAssignUserOrGroup) {
                        return isForceAssignAllowed(loginUserId, currentVirtualUserGroupId);
                    } else {
                        return iPartsUserAdminCache.getInstance(loginUserId).isMemberOfVirtualUserGroup(currentVirtualUserGroupId);
                    }
                } else {
                    return false;
                }
            }
        } else {
            return isCurrentUserEqualLoginUser();
        }
    }

    private boolean isForceAssignAllowed(String userId, String userGroupId) {
        // Bei Übernahme mit globalem Recht nicht die virtuellen Benutzergruppe prüfen
        RightScope forceAssignRightScope = iPartsUserAdminCache.getInstance(userId).getUserRightScope(iPartsRight.FORCE_ASSIGN_USER_OR_GROUP);
        if (userGroupId.isEmpty() || (forceAssignRightScope == RightScope.GLOBAL)) {
            return true;
        } else {
            iPartsUserAdminCacheElement userAdminCache = iPartsUserAdminCache.getInstance(userId);
            Set<String> validOrgs = userAdminCache.getValidOrgIdsForAssignUserOrGroup(iPartsRight.FORCE_ASSIGN_USER_OR_GROUP);
            String roleId = iPartsVirtualUserGroup.getRoleIdFromVirtualUserGroupId(userGroupId);
            for (String validOrg : validOrgs) {
                // Prüfen, ob der eingeloggte Benutzer Mitglied der virtuellen Benutzergruppe ist inkl. Berücksichtigung
                // vom Organisationsbaum
                if (iPartsVirtualUserGroup.getVirtualUserGroupId(validOrg, roleId).equals(userGroupId)) {
                    return true;
                }
            }
            return false;
        }
    }

    public String getChangeSetValue() {
        return getFieldValue(iPartsConst.FIELD_DAO_CHANGE_SET_ID);
    }

    public iPartsChangeSetId getChangeSetId() {
        return new iPartsChangeSetId(getChangeSetValue());
    }

    public iPartsDataChangeSet getDataChangeSet() {
        if (dataChangeSet == null) {
            iPartsChangeSetId changeSetId = getChangeSetId();
            if (changeSetId.isValidId()) {
                dataChangeSet = new iPartsDataChangeSet((EtkProject)project, changeSetId);
                if (!dataChangeSet.loadFromDB(changeSetId)) {
                    dataChangeSet = null;
                }
            }
        }
        return dataChangeSet;
    }

    public void refreshDataChangeSet() {
        dataChangeSet = null;
    }

    public void createChangeSetInDBIfNotExists() {
        iPartsChangeSetId changeSetId = getChangeSetId();
        if (!changeSetId.isEmpty()) {
            iPartsRevisionChangeSet revChangeSet = new iPartsRevisionChangeSet(changeSetId, getEtkProject(), false);
            revChangeSet.createInDBIfNotExists(iPartsChangeSetSource.AUTHOR_ORDER);
        }
    }

    public boolean isChangeSetEmpty() {
        getDataChangeSet();
        if (dataChangeSet != null) {
            return dataChangeSet.getStatus() == iPartsChangeSetStatus.NEW;
        }
        return true;
    }

    public void setChangeSetId(iPartsChangeSetId changeSetId) {
        if (changeSetId != null) {
            setFieldValue(FIELD_DAO_CHANGE_SET_ID, changeSetId.getGUID(), DBActionOrigin.FROM_EDIT);
        }
    }

    public String getAuthorOrderName() {
        return getFieldValue(FIELD_DAO_NAME);
    }

    public void setAuthorOrderName(String name) {
        setFieldValue(FIELD_DAO_NAME, name, DBActionOrigin.FROM_DB);
    }

    public boolean isChangeSetIdEmpty() {
        return StrUtils.isEmpty(getChangeSetId().getGUID());
    }

    public boolean isActivatable() {
        return !isChangeSetIdEmpty() && !iPartsAuthorOrderStatus.isEndState(getStatus()) &&
               !iPartsAuthorOrderStatus.isStartState(getStatus());
    }

    public void changeStatus(iPartsAuthorOrderStatus state) {
        if (getStatus() != state) {
            addHistory(iPartsDataAOHistoryHelper.createAndSetStatus(this, getStatus(), state, getEtkProject()), DBActionOrigin.FROM_EDIT);
        }
        setStatus(state, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Setzt den aktuellen Benutzer bzw. die aktuelle virtuelle Benutzergruppe basierend auf der übergebenen ID.
     *
     * @param userIdOrVirtualUserGroupId
     * @param clearVirtualUserGroup      Flag, ob die aktuelle virtuelle Benutzergruppe entfernt werden soll (z.B. bei Statuswechsel)
     */
    public void setCurrentUserIdOrVirtualUserGroupId(String userIdOrVirtualUserGroupId, boolean clearVirtualUserGroup) {
        // Hat sich der Benutzer bzw. die virtuelle Benutzergruppe geändert, muss ein Eintrag erzeugt werden
        String historyEntryUserName = null;
        String newUserName;
        String newVirtualUserGroupId;
        if (iPartsVirtualUserGroup.isVirtualUserGroupId(userIdOrVirtualUserGroupId)) {
            if (!getCurrentUser().isEmpty() || !getCurrentUserGroupId().equals(userIdOrVirtualUserGroupId)) {
                historyEntryUserName = userIdOrVirtualUserGroupId;
            }
            newUserName = "";
            newVirtualUserGroupId = userIdOrVirtualUserGroupId;
        } else {
            if (!userIdOrVirtualUserGroupId.isEmpty()) {
                String userName = iPartsUserAdminCache.getInstance(userIdOrVirtualUserGroupId).getUserName(getEtkProject().getDBLanguage());
                if (!getCurrentUser().equals(userName)) {
                    historyEntryUserName = userName;
                }
                newUserName = userName;
            } else { // Aktuellen Benutzer sowie virtuelle Benutzergruppe entfernen
                historyEntryUserName = "";
                newUserName = "";
            }
            if (clearVirtualUserGroup) {
                newVirtualUserGroupId = "";
            } else {
                newVirtualUserGroupId = null; // Virtuelle Benutzergruppe bleibt erhalten
            }
        }

        if (historyEntryUserName != null) {
            String currentUser = getCurrentUser();
            if (currentUser.isEmpty()) {
                currentUser = getCurrentUserGroupId();
            }
            addHistory(iPartsDataAOHistoryHelper.createAndSetUserChange(this, currentUser, historyEntryUserName, getEtkProject()),
                       DBActionOrigin.FROM_EDIT);
        }

        setCurrentUser(newUserName, DBActionOrigin.FROM_EDIT);
        if (newVirtualUserGroupId != null) {
            setCurrentUserGroupId(newVirtualUserGroupId, DBActionOrigin.FROM_EDIT);
        }
    }

    public String getCurrentUserGroupId() {
        return getFieldValue(iPartsConst.FIELD_DAO_CURRENT_GRP_ID);
    }

    public void setCurrentUserGroupId(String groupId, DBActionOrigin origin) {
        setFieldValue(FIELD_DAO_CURRENT_GRP_ID, groupId, origin);
    }

    public String getCreationUserGroupId() {
        return getFieldValue(iPartsConst.FIELD_DAO_CREATOR_GRP_ID);
    }

    public void setCreationUserGroupId(String groupId, DBActionOrigin origin) {
        setFieldValue(FIELD_DAO_CREATOR_GRP_ID, groupId, origin);
    }

    public void setBstId(String bstId, DBActionOrigin origin) {
        setFieldValue(FIELD_DAO_BST_ID, bstId, origin);
    }

    public String getBstId() {
        return getFieldValue(FIELD_DAO_BST_ID);
    }

    public iPartsTransferStates getCurrentPicOrderState() {
        return iPartsTransferStates.getFromDB(getFieldValue(iPartsDataVirtualFieldsDefinition.DAO_TOTAL_PICORDERS_STATE));
    }

    /**
     * Übernimmt das Freigabedatum vom Changeset an den Autorenauftrag
     *
     * @param project
     * @param changeSetId
     */
    public void setCommitDateFromChangeset(EtkProject project, iPartsChangeSetId changeSetId) {
        iPartsDataChangeSet dataChangeSet = new iPartsDataChangeSet(project, changeSetId);
        if (dataChangeSet.existsInDB()) {
            Calendar commitDate = dataChangeSet.getCommitDate();
            if (commitDate != null) {
                setFieldValueAsDateTime(FIELD_DAO_RELDATE, commitDate, DBActionOrigin.FROM_EDIT);
                saveToDB();
            }
        }
    }

    public void setCommitDateForHistory(String commitDate) {
        if (commitDate != null) {
            setFieldValue(FIELD_DAO_RELDATE, commitDate, DBActionOrigin.FROM_DB);
        }
    }

    public String getCommitDate() {
        Calendar commitDate = getFieldValueAsDateTime(FIELD_DAO_RELDATE);
        if (commitDate != null) {
            return DateUtils.toyyyyMMddHHmmss_Calendar(commitDate);
        }
        return "";
    }

    public String getDescription() {
        return getFieldValue(FIELD_DAO_DESC);
    }

    public void setDescription(String descr, DBActionOrigin origin) {
        setFieldValue(FIELD_DAO_DESC, descr, origin);
    }
}
