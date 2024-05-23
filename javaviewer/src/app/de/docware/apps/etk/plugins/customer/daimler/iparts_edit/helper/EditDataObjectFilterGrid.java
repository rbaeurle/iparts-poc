/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EditCreateMode;
import de.docware.apps.etk.base.forms.common.EnumCheckRComboBox;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.framework.combimodules.useradmin.db.UserAdminRoleCache;
import de.docware.framework.combimodules.useradmin.db.UserAdminUserCache;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Erweiterung {@link DataObjectFilterGrid} für {@link EditToolbarButtonAlias}
 */
public class EditDataObjectFilterGrid extends DataObjectFilterGrid {

    public EditDataObjectFilterGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
    }

    @Override
    protected void postCreateGui() {
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), getToolBar());
        super.postCreateGui();
    }

    @Override
    protected EditToolbarButtonMenuHelper getToolbarHelper() {
        return (EditToolbarButtonMenuHelper)toolbarHelper;
    }

    public void enableToolbarButtonAndMenu(EditToolbarButtonAlias alias, boolean enabled) {
        getToolbarHelper().enableToolbarButtonAndMenu(alias, getContextMenuTable(), enabled);
    }

    public void showToolbarButtonAndMenu(EditToolbarButtonAlias alias) {
        getToolbarHelper().showToolbarButtonAndMenu(alias, getContextMenuTable());
    }

    public void hideToolbarButtonAndMenu(EditToolbarButtonAlias alias) {
        getToolbarHelper().hideToolbarButtonAndMenu(alias, getContextMenuTable());
    }


    /**
     * Spaltenfilter für {@link EtkDataObject}s mit Feldern, die virtuelle Benutzergruppen enthalten, nach denen über ein
     * SetOfEnum automatisch basierend auf den konkreten Datensätzen gefiltert werden soll.
     * Zusätzlich können Felder angegeben werden, die Benutzernamen enthalten. Die Visualisierung dieser Felder erfolgt
     * als vollständiger Benutzername. Das DataObject für den Filter ist aber weiterhin der ursprüngliche DB-Wert.
     */
    protected class VirtualUserGroupDataObjectColumnFilterFactory extends DataObjectColumnFilterFactory {

        private Class<? extends EtkDataObject> dataObjectClass;
        private String[] virtualUserGroupFields;
        private String[] fullNameUserFields;
        private String[] roleFields;
        private String[] organisationFields;

        public VirtualUserGroupDataObjectColumnFilterFactory(EtkProject project, Class<? extends EtkDataObject> dataObjectClass,
                                                             String[] virtualUserGroupFields, String[] fullNameUserFields,
                                                             String[] roleFields, String[] organisationFields) {
            super(project);
            this.dataObjectClass = dataObjectClass;
            this.virtualUserGroupFields = virtualUserGroupFields;
            this.fullNameUserFields = fullNameUserFields;
            this.roleFields = roleFields;
            this.organisationFields = organisationFields;
        }

        @Override
        protected boolean changeColumnTableFilterValues(int column, EditControlFactory editControl) {
            if ((editControl.getValues().editCreateMode == EditCreateMode.ecmTableColumnFilter)) {
                String fieldName = editControl.getFieldName();
                boolean isVirtualUserGroupField = (virtualUserGroupFields != null) && (ArrayUtil.indexOf(virtualUserGroupFields, fieldName) >= 0);
                boolean isFullNameUserField = (fullNameUserFields != null) && (ArrayUtil.indexOf(fullNameUserFields, fieldName) >= 0);
                boolean isRoleField = (roleFields != null) && (ArrayUtil.indexOf(roleFields, fieldName) >= 0);
                boolean isOrganisationField = (organisationFields != null) && (ArrayUtil.indexOf(organisationFields, fieldName) >= 0);
                if (isVirtualUserGroupField || isFullNameUserField || isRoleField || isOrganisationField) {
                    // Trick, um im Tabellenfilter ein SetOfEnum Eingabefeld zu erzeugen, das als Tokens
                    // die Werte aus der zugehörigen Spalte der Tabelle enthält
                    editControl.getValues().field = editControl.getField().cloneMe();  // zur Sicherheit, damit die Originalwerte sich nicht ändern
                    editControl.getValues().field.setType(EtkFieldType.feSetOfEnum);  // behaupte, das Feld ist ein SetOfEnum
                    editControl.getOptions().handleAsSetOfEnum = true;  // und soll als SetOfEnum behandelt werden
                    editControl.getOptions().searchDisjunctive = true;

                    // alles Weitere übernimmt EditControlFactory und das FilterInterface
                    AbstractGuiControl guiCtrl = EditControlFactory.doCreateEnumCheckBoxForTableColumnFilter(editControl.getValues(),
                                                                                                             editControl.getOptions());
                    if (guiCtrl != null) {
                        editControl.setControl(guiCtrl);

                        if (guiCtrl instanceof EnumCheckRComboBox) {
                            EnumCheckRComboBox comboBox = (EnumCheckRComboBox)guiCtrl;
                            comboBox.removeAllItems();

                            // Virtuelle Benutzergruppen als Enum für den Tabellenspaltenfilter setzen
                            if (isVirtualUserGroupField && (dataObjectClass != null)) {
                                // Benennungen der virtuellen Benutzergruppen bestimmen
                                Map<String, String> virtualUserGroupsMap = new HashMap<>(); // Map mit ID auf Name
                                virtualUserGroupsMap.put(" ", ""); // Token -> Wert
                                for (EtkDataObject dataObject : getDataObjectList(dataObjectClass)) {
                                    String virtualUserGroupId = dataObject.getFieldValue(fieldName);
                                    if (StrUtils.isValid(virtualUserGroupId)) {
                                        if (!virtualUserGroupsMap.containsKey(virtualUserGroupId)) {
                                            virtualUserGroupsMap.put(virtualUserGroupId, iPartsVirtualUserGroup.getVirtualUserGroupName(virtualUserGroupId));
                                        }
                                    }
                                }

                                // Sortierte Map mit Name auf ID
                                TreeMap<String, String> virtualUserGroupNamesMap = new TreeMap<>();
                                for (Map.Entry<String, String> virtualUserGroupEntry : virtualUserGroupsMap.entrySet()) {
                                    virtualUserGroupNamesMap.put(virtualUserGroupEntry.getValue(), virtualUserGroupEntry.getKey());
                                }

                                // Sortierte virtuelle Benutzergruppen hinzufügen
                                for (Map.Entry<String, String> virtualUserGroupEntry : virtualUserGroupNamesMap.entrySet()) {
                                    comboBox.addToken(virtualUserGroupEntry.getValue(), virtualUserGroupEntry.getKey());
                                }
                            } else if (isFullNameUserField) {
                                Map<String, String> userNamesMap = new TreeMap<>();
                                String dbLanguage = getProject().getDBLanguage();
                                userNamesMap.put("", " "); // Wert -> Token
                                for (GuiTableRowWithObjects entry : getEntriesAsList()) {
                                    EtkDataObject objectForTable = entry.getObjectForTable(editControl.getTableName());
                                    if (objectForTable != null) {
                                        String userName = objectForTable.getFieldValue(editControl.getField().getName());
                                        if (!userName.isEmpty()) {
                                            String userFullNameByUserName = UserAdminUserCache.getUserFullNameByUserName(userName, dbLanguage);
                                            if (iPartsConst.TECHNICAL_USERS.contains(userName)) {
                                                iPartsUserAdminCacheElement userCache = iPartsUserAdminCache.getCacheByUserName(userName);
                                                if (userCache == null) {
                                                    userFullNameByUserName = userName; // Bei technischen Benutzern Fallback auf den Benutzernamen falls der Benutzer nicht in der Benutzerverwaltung existiert
                                                }
                                            }
                                            userNamesMap.put(userFullNameByUserName, userName);
                                        }
                                    }
                                }
                                for (Map.Entry<String, String> userNameEntry : userNamesMap.entrySet()) {
                                    comboBox.addToken(userNameEntry.getValue(), userNameEntry.getKey());
                                }
                            } else if (isRoleField) {
                                Map<String, String> roleNamesMap = new TreeMap<>();
                                String dbLanguage = getProject().getDBLanguage();
                                roleNamesMap.put("", " "); // Wert -> Token
                                for (GuiTableRowWithObjects entry : getEntriesAsList()) {
                                    EtkDataObject objectForTable = entry.getObjectForTable(editControl.getTableName());
                                    if (objectForTable != null) {
                                        String roleId = objectForTable.getFieldValue(fieldName);
                                        if (StrUtils.isValid(roleId)) {
                                            roleNamesMap.put(UserAdminRoleCache.getInstance(roleId).getRoleName(dbLanguage), roleId);
                                        }
                                    }
                                }
                                for (Map.Entry<String, String> roleNameEntry : roleNamesMap.entrySet()) {
                                    comboBox.addToken(roleNameEntry.getValue(), roleNameEntry.getKey());
                                }
                            } else if (isOrganisationField) {
                                Map<String, String> orgNamesMap = new TreeMap<>();
                                String dbLanguage = getProject().getDBLanguage();
                                orgNamesMap.put("", " "); // Wert -> Token
                                for (GuiTableRowWithObjects entry : getEntriesAsList()) {
                                    EtkDataObject objectForTable = entry.getObjectForTable(editControl.getTableName());
                                    if (objectForTable != null) {
                                        String orgId = objectForTable.getFieldValue(fieldName);
                                        if (StrUtils.isValid(orgId)) {
                                            orgNamesMap.put(iPartsUserAdminOrgCache.getInstance(orgId).getOrgName(dbLanguage), orgId);
                                        }
                                    }
                                }
                                for (Map.Entry<String, String> orgNameEntry : orgNamesMap.entrySet()) {
                                    comboBox.addToken(orgNameEntry.getValue(), orgNameEntry.getKey());
                                }
                            }
                            comboBox.setActToken(editControl.getInitialValue());
                        }
                        return true;
                    }
                }
            }
            return super.changeColumnTableFilterValues(column, editControl);
        }
    }
}
