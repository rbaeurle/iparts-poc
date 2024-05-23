/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminOrgCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditFormComboboxHelper;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Einfache Form für die Verwaltung von AS-PLM Users via Gruppen
 */
public class EditASPLMContractorForm extends AbstractJavaViewerForm {

    private boolean isReadOnly = false;

    // TextFelder für ReadOnly Betrieb
    private GuiTextField textfieldUserGroup = null;
    private GuiTextField textfieldUser = null;
    private final ASPLMContractor contractor;
    private RComboBox<iPartsDataASPLMGroup> userGroupComboBox;
    private RComboBox<iPartsDataASPLMUser> userComboBox;

    protected EventListeners eventListeners;

    /**
     * Erzeugt eine Instanz von EditASPLMContractorForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditASPLMContractorForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.eventListeners = new EventListeners();
        this.contractor = new ASPLMContractor(getProject());
        postCreateGui();
        setReadOnly(false);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        setTitle("!!Auftragnehmer festlegen");
        // zusätzlichen Button verstecken
        setButtonChangeListener(null);
        userGroupComboBox = new RComboBox<>();
        RComboBox.replaceGuiComboBox(mainWindow.comboboxUserGroup, userGroupComboBox);
        userComboBox = new RComboBox<>();
        RComboBox.replaceGuiComboBox(mainWindow.comboboxUser, userComboBox);
        init();
        ThemeManager.get().render(mainWindow);
        mainWindow.equaldimensionpanel.setMinimumHeight(Math.max(userGroupComboBox.getPreferredHeight(), userComboBox.getPreferredHeight()));
    }

    private void init() {
        fillGroupItems();
        enableButtons();
    }

    public void clearComboboxes() {
        fillGroupItems();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public GuiPanel getContractorPanelToBeAdded() {
        setSnapConstraints();
        mainWindow.equaldimensionpanel.removeFromParent();
        return mainWindow.equaldimensionpanel;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        close();
        return modalResult;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    /**
     * Überprüfungsfunktion, ob initialisiert ist
     *
     * @return
     */
    public boolean isInit() {
        return contractor != null;
    }

    /**
     * Test, ob alle Contractor Felder besetzt sind
     *
     * @return
     */
    public boolean isValid() {
        if (isInit()) {
            if (isUserGroupMustField() && (getSelectedASPLMGroupId() == null)) {
                return false;
            }
            return !isUserMustField() || (getSelectedASPLMUserId() != null);
        }
        return false;
    }

    /**
     * Holen der selektierten Gruppe (nicht bei ReadOnly)
     *
     * @return
     */
    public String getSelectedASPLMGroupId() {
        if (!isReadOnly) {
            iPartsDataASPLMGroup dataGroup = getSelectedDataGroup();
            if (dataGroup != null) {
                return dataGroup.getASPLMGroupId();
            }
        }
        return null;
    }

    /**
     * Holen der selektierten Gruppen-GUID (nicht bei ReadOnly)
     *
     * @return
     */
    public String getSelectedGroupGuid() {
        if (!isReadOnly) {
            iPartsDataASPLMGroup dataGroup = getSelectedDataGroup();
            if (dataGroup != null) {
                return dataGroup.getAsId().getGroupGuid();
            }
        }
        return null;
    }

    /**
     * Vorselektion einer Gruppe (nicht bei ReadOnly)
     *
     * @param asplmGroupGuid
     */
    public void setSelectedGroupGuid(String asplmGroupGuid) {
        if (!isReadOnly) {
            internSetSelectedASPLMGroupGuid(asplmGroupGuid);
        }
    }

    /**
     * Holen des selektierten Users (nicht bei ReadOnly)
     *
     * @return
     */
    public String getSelectedASPLMUserId() {
        if (!isReadOnly) {
            iPartsDataASPLMUser dataUser = getSelectedDataUser();
            if (dataUser != null) {
                return dataUser.getUserASPLMId();
            }
        }
        return null;
    }

    /**
     * Holen der selektierten User-GUID (nicht bei ReadOnly)
     *
     * @return
     */
    public String getSelectedUserGuid() {
        if (!isReadOnly) {
            iPartsDataASPLMUser dataUser = getSelectedDataUser();
            if (dataUser != null) {
                return dataUser.getAsId().getUserGuid();
            }
        }
        return null;
    }

    /**
     * Vorselektion eines Users (nicht bei ReadOnly)
     * Macht nur Sinn, wenn VORHER {@link #setSelectedGroupGuid} aufgerufen wurde.
     *
     * @param asplmUserGuid
     */
    public void setSelectedUserGuid(String asplmUserGuid) {
        if (!isReadOnly) {
            internSetSelectedASPLMUserGuid(asplmUserGuid);
        }
    }

    /**
     * ReadOnly setzen
     * Die Comboboxen werden durch TextFields (editable=false + spezielle Background Color) ersetzt
     *
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly) {
        if (readOnly != isReadOnly) {
            if (readOnly) {
                boolean setNames = (textfieldUserGroup == null);
                textfieldUserGroup = EditFormComboboxHelper.replaceComboBoxByTextField(userGroupComboBox, textfieldUserGroup, mainWindow.panelUserGroup);
                textfieldUser = EditFormComboboxHelper.replaceComboBoxByTextField(userComboBox, textfieldUser, mainWindow.panelUser);
                if (setNames) {
                    textfieldUserGroup.setName("textfieldContractorUserGroup");
                    textfieldUser.setName("textfieldContractorUser");
                }
            } else {
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfieldUserGroup, userGroupComboBox, mainWindow.panelUserGroup);
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfieldUser, userComboBox, mainWindow.panelUser);
            }
            isReadOnly = readOnly;
        }
    }

    /**
     * Labeltext für Gruppe verändern
     *
     * @param text
     */
    public void setUserGroupLabelText(String text) {
        setUserGroupLabelText(text, false);
    }

    /**
     * Labeltext für Gruppe verändern und als Muss-Eintrag kennzeichnen
     *
     * @param text
     * @param isMustField
     */
    public void setUserGroupLabelText(String text, boolean isMustField) {
        setLabelText(mainWindow.labelUserGroup, text, isMustField);
    }

    /**
     * Abfrage, ob Gruppe ein Mussfeld ist
     *
     * @return
     */
    public boolean isUserGroupMustField() {
        return isMustField(mainWindow.labelUserGroup);
    }

    public boolean isUserGroupComboBoxFilled() {
        return userGroupComboBox.getItemCount() != 0;
    }

    /**
     * Labeltext für User verändern
     *
     * @param text
     */
    public void setUserLabelText(String text) {
        setUserLabelText(text, false);
    }

    /**
     * Labeltext für User verändern und als Muss-Eintrag kennzeichnen
     *
     * @param text
     * @param isMustField
     */
    public void setUserLabelText(String text, boolean isMustField) {
        setLabelText(mainWindow.labelUser, text, isMustField);
    }

    /**
     * Abfrage, ob User ein Mussfeld ist
     *
     * @return
     */
    public boolean isUserMustField() {
        return isMustField(mainWindow.labelUser);
    }

    /**
     * Nur bei ReadOnly: setzen eines Gruppen-Namens
     *
     * @param name
     */
    public void setUserGroupName(String name) {
        if (isReadOnly && (textfieldUserGroup != null)) {
            textfieldUserGroup.setText(name);
        }
    }

    public void setUserGroupNameFromID(String groupGUID) {
        iPartsDataASPLMGroup group = contractor.getGroupForID(groupGUID);
        if (group != null) {
            setUserGroupName(group.getGroupAlias());
        }
    }

    public void setUserNameFromID(String groupGUID, String userGUID) {
        iPartsDataASPLMUser user = contractor.getUserForID(groupGUID, userGUID);
        if (user != null) {
            setUserName(user.getUserASPLMFirstName() + " " + user.getUserASPLMLastName());
        }
    }

    /**
     * Nur bei ReadOnly: Holen des Gruppen-Namens
     *
     * @return
     */
    public String getUserGroupName() {
        if (isReadOnly && (textfieldUserGroup != null)) {
            return textfieldUserGroup.getText();
        }
        return null;
    }

    /**
     * Nur bei ReadOnly: Setzen eines User-Namens
     *
     * @param name
     */
    public void setUserName(String name) {
        if (isReadOnly && (textfieldUser != null)) {
            textfieldUser.setText(name);
        }
    }

    /**
     * Nur bei ReadOnly: Holen des User-Namens
     *
     * @return
     */
    public String getUserName() {
        if (isReadOnly && (textfieldUser != null)) {
            return textfieldUser.getText();
        }
        return null;
    }

    /**
     * Eventlistener für Änderungen bei jeder ComboBox setzen
     *
     * @param eventListener
     */
    public void addEventListener(EventListener eventListener) {
        eventListeners.addEventListener(eventListener);
    }

    /**
     * Eventlistener für Änderungen bei jeder ComboBox löschen
     *
     * @param eventListener
     */
    public void removeEventListener(EventListener eventListener) {
        eventListeners.removeEventListener(eventListener);
    }

    /**
     * Eventlistener für den zusätzlichen Button setzen (damit wird der Button automatisch sichtbar)
     * oder Löschen (damit wird der Button automatisch unsichtbar)
     *
     * @param eventListener
     */
    public void setButtonChangeListener(EventListener eventListener) {
        if (eventListener != null) {
            mainWindow.buttonChange.addEventListener(eventListener);
            mainWindow.panelChange.setVisible(true);
        } else {
            mainWindow.buttonChange.removeEventListeners(Event.ACTION_PERFORMED_EVENT);
            mainWindow.panelChange.setVisible(false);
        }
    }

    private void setLabelText(GuiLabel label, String text, boolean isMustField) {
        label.setText(text);
        DWFontStyle fontStyle = DWFontStyle.PLAIN;
        if (isMustField) {
            fontStyle = DWFontStyle.BOLD;
        }
        label.setFontStyle(fontStyle);
    }

    private boolean isMustField(GuiLabel label) {
        return label.getFontStyle() == DWFontStyle.BOLD;
    }

    private void fillGroupItems() {
        userGroupComboBox.switchOffEventListeners();
        userGroupComboBox.removeAllItems();
        for (iPartsDataASPLMGroup dataGroup : contractor.getGroupList()) {
            userGroupComboBox.addItem(dataGroup, buildGroupComboText(dataGroup));
        }
        userGroupComboBox.setEnabled(true);
        userGroupComboBox.setSelectedIndex(-1);
        userGroupComboBox.switchOnEventListeners();
        EditFormComboboxHelper.clearComboBox(userComboBox);
    }

    private void fillUserItems(iPartsDataASPLMUserList userList) {
        userComboBox.switchOffEventListeners();
        userComboBox.removeAllItems();
        if (!userList.isEmpty()) {
            userComboBox.addItem(null, "");
            for (iPartsDataASPLMUser dataUser : userList) {
                userComboBox.addItem(dataUser, buildUserComboText(dataUser));
            }
            userComboBox.setEnabled(true);
            userComboBox.setSelectedIndex(-1);
        } else {
            userComboBox.setEnabled(false);
        }
        userComboBox.switchOnEventListeners();
    }

    private iPartsDataASPLMGroup getSelectedDataGroup() {
        return userGroupComboBox.getSelectedUserObject();
    }

    private iPartsDataASPLMUser getSelectedDataUser() {
        if (StrUtils.isValid(userComboBox.getSelectedItem())) {
            return userComboBox.getSelectedUserObject();
        }
        return null;
    }

    private iPartsASPLMGroupId getSelectedUserGroupId() {
        iPartsDataASPLMGroup dataGroup = getSelectedDataGroup();
        if (dataGroup != null) {
            return dataGroup.getAsId();
        }
        return null;
    }

    public GuiLabel getUserGroupLabel() {
        return mainWindow.labelUserGroup;
    }

    private void internSetSelectedASPLMGroupGuid(String asplmGroupGuid) {
        for (iPartsDataASPLMGroup dataGroup : userGroupComboBox.getUserObjects()) {
            if (dataGroup.getAsId().getGroupGuid().equals(asplmGroupGuid)) {
                userGroupComboBox.setSelectedUserObject(dataGroup);
                return;
            }
        }
        userGroupComboBox.setSelectedIndex(-1);
    }

    private void internSetSelectedASPLMUserGuid(String asplmUserGuid) {
        for (iPartsDataASPLMUser dataUser : userComboBox.getUserObjects()) {
            if ((dataUser != null) && dataUser.getAsId().getUserGuid().equals(asplmUserGuid)) {
                userComboBox.setSelectedUserObject(dataUser);
                return;
            }
        }
        userComboBox.setSelectedIndex(-1);
    }

    /**
     * Hier wird der Text für die Gruppen-Combobox erzeugt
     * (kann überschrieben werden)
     *
     * @param dataGroup
     * @return
     */
    protected String buildGroupComboText(iPartsDataASPLMGroup dataGroup) {
        if (dataGroup != null) {
            //kein MultilangFeld für Gruppen
            return dataGroup.getGroupAlias();
        }
        return "";
    }

    /**
     * Hier wird der Text für die User-Combobox erzeugt
     * (kann überschrieben werden)
     *
     * @param dataUser
     * @return
     */
    protected String buildUserComboText(iPartsDataASPLMUser dataUser) {
        if (dataUser != null) {
            return dataUser.getUserASPLMFirstName() + " " + dataUser.getUserASPLMLastName();
        }
        return "";
    }

    /**
     * @param event
     */
    private void fireOnChangeEvent(Event event) {
        for (EventListener listener : eventListeners.getListeners(Event.ON_CHANGE_EVENT)) {
            listener.fire(event);
        }
        enableButtons();
    }

    /**
     * Sollte die Form aufgeschnappt werden, so müssen die Constraints angepaßt werden
     */
    private void setSnapConstraints() {
        ConstraintsGridBag gridBag = (ConstraintsGridBag)mainWindow.labelUserGroup.getConstraints();
        gridBag.setInsets(0, 0, 0, 4);
        mainWindow.labelUserGroup.setConstraints(gridBag);
        gridBag = (ConstraintsGridBag)userGroupComboBox.getConstraints();
        gridBag.setInsets(0, 4, 0, 0);
        userGroupComboBox.setConstraints(gridBag);
        gridBag = (ConstraintsGridBag)mainWindow.labelUser.getConstraints();
        gridBag.setInsets(0, 16, 0, 4);
        mainWindow.labelUser.setConstraints(gridBag);
        gridBag = (ConstraintsGridBag)userComboBox.getConstraints();
        gridBag.setInsets(0, 4, 0, 4);
        userComboBox.setConstraints(gridBag);
    }

    private void enableButtons() {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isValid());
    }

    /**
     * Callback für UserGroup-Combobox
     * (=> besetzen der User-Combobox)
     *
     * @param event
     */
    private void onChange_CB_UserGroup(Event event) {
        iPartsASPLMGroupId groupId = getSelectedUserGroupId();
        if (groupId != null) {
            fillUserItems(contractor.getUserList(groupId));
            fireOnChangeEvent(event);
        } else {
            EditFormComboboxHelper.clearComboBox(userComboBox);
        }
    }

    /**
     * Callback für User-Combobox
     * (i.A. nicht verwendet)
     *
     * @param event
     */
    private void onChange_CB_User(Event event) {
        fireOnChangeEvent(event);
    }

    private static class ASPLMContractor {

        private final EtkProject project;
        private final iPartsDataASPLMGroupList groups;
        private final Map<iPartsASPLMGroupId, iPartsDataASPLMUserList> users;

        public ASPLMContractor(EtkProject project) {
            this.project = project;
            this.groups = determineValidGroups();
            this.users = new LinkedHashMap<>();
        }

        /**
         * Interne Organisation: Valide Gruppen werden aufgrund der Unternehmenszugehörigkeit ermittelt
         * Externe Organisation: Valide Gruppen werden aufrgrund der Lieferantennummer ermittelt
         *
         * @return Liste der für den Benutzer validen Gruppen
         */
        public iPartsDataASPLMGroupList determineValidGroups() {
            String userId = iPartsUserAdminDb.getLoginUserIdForSession();
            String orgId = "";
            if (StrUtils.isValid(userId)) {
                orgId = iPartsUserAdminCache.getInstance(userId).getOrgId();
            }
            if (StrUtils.isValid(orgId)) {
                iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(orgId);
                if (orgCache.isInternalOrganisation()) {
                    String[] company = null;
                    boolean isCarAndVan = iPartsRight.checkCarAndVanInSession();
                    boolean isTruckAndBus = iPartsRight.checkTruckAndBusInSession();
                    if (isCarAndVan && !isTruckAndBus) {
                        company = new String[]{ iPartsTransferConst.COMPANY_VALUE_MBAG };
                    } else if (isTruckAndBus && !isCarAndVan) {
                        company = new String[]{ iPartsTransferConst.COMPANY_VALUE_DTAG };
                    } else if (isCarAndVan && isTruckAndBus) {
                        company = new String[]{ iPartsTransferConst.COMPANY_VALUE_MBAG, iPartsTransferConst.COMPANY_VALUE_DTAG };
                    }

                    if (company != null) {
                        return iPartsDataASPLMGroupList.loadGroupListFilteredWithCompany(project, company);
                    }
                } else {
                    String bstSupplierId = orgCache.getBSTSupplierId();
                    if (!bstSupplierId.isEmpty()) {
                        return iPartsDataASPLMGroupList.loadGroupListFilteredWithSupplierNo(project, bstSupplierId);
                    }
                }
            }
            return new iPartsDataASPLMGroupList();
        }

        public iPartsDataASPLMGroupList getGroupList() {
            return groups;
        }

        public iPartsDataASPLMUserList getUserList(iPartsASPLMGroupId groupId) {
            return internGetUserList(groupId);
        }

        private iPartsDataASPLMUserList internGetUserList(iPartsASPLMGroupId groupId) {
            iPartsDataASPLMUserList userList = users.get(groupId);
            if (userList == null) {
                userList = iPartsDataASPLMUserList.loadUsersByGroupId(project, groupId);
                users.put(groupId, userList);
            }
            return userList;
        }

        public iPartsDataASPLMGroup getGroupForID(String groupGUID) {
            for (iPartsDataASPLMGroup group : groups) {
                if (group.getAsId().getGroupGuid().equals(groupGUID)) {
                    return group;
                }
            }
            return null;
        }

        public iPartsDataASPLMUser getUserForID(String groupGUID, String userGUID) {
            iPartsDataASPLMGroup groupForID = getGroupForID(groupGUID);
            if (groupForID == null) {
                return null;
            }

            iPartsDataASPLMUserList userList = internGetUserList(groupForID.getAsId());
            if (userList != null) {
                for (iPartsDataASPLMUser user : userList) {
                    if (user.getAsId().getUserGuid().equals(userGUID)) {
                        return user;
                    }
                }
            }
            return null;
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler
                                                 translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel equaldimensionpanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelUserGroup;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelUserGroup;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsDataASPLMGroup> comboboxUserGroup;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelUser;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelUser;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsDataASPLMUser> comboboxUser;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelChange;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton buttonChange;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(600);
            this.setHeight(190);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            equaldimensionpanel = new de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel();
            equaldimensionpanel.setName("equaldimensionpanel");
            equaldimensionpanel.__internal_setGenerationDpi(96);
            equaldimensionpanel.registerTranslationHandler(translationHandler);
            equaldimensionpanel.setScaleForResolution(true);
            equaldimensionpanel.setMinimumWidth(10);
            equaldimensionpanel.setMinimumHeight(10);
            equaldimensionpanel.setHorizontal(true);
            de.docware.framework.modules.gui.layout.LayoutAbsolute equaldimensionpanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutAbsolute();
            equaldimensionpanel.setLayout(equaldimensionpanelLayout);
            panelUserGroup = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelUserGroup.setName("panelUserGroup");
            panelUserGroup.__internal_setGenerationDpi(96);
            panelUserGroup.registerTranslationHandler(translationHandler);
            panelUserGroup.setScaleForResolution(true);
            panelUserGroup.setMinimumWidth(0);
            panelUserGroup.setMinimumHeight(0);
            panelUserGroup.setMaximumWidth(2147483647);
            panelUserGroup.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelUserGroupLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelUserGroupLayout.setCentered(false);
            panelUserGroup.setLayout(panelUserGroupLayout);
            labelUserGroup = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelUserGroup.setName("labelUserGroup");
            labelUserGroup.__internal_setGenerationDpi(96);
            labelUserGroup.registerTranslationHandler(translationHandler);
            labelUserGroup.setScaleForResolution(true);
            labelUserGroup.setMinimumWidth(10);
            labelUserGroup.setMinimumHeight(10);
            labelUserGroup.setText("!!Gruppe");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelUserGroupConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            labelUserGroup.setConstraints(labelUserGroupConstraints);
            panelUserGroup.addChild(labelUserGroup);
            comboboxUserGroup = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsDataASPLMGroup>();
            comboboxUserGroup.setName("comboboxUserGroup");
            comboboxUserGroup.__internal_setGenerationDpi(96);
            comboboxUserGroup.registerTranslationHandler(translationHandler);
            comboboxUserGroup.setScaleForResolution(true);
            comboboxUserGroup.setMinimumWidth(50);
            comboboxUserGroup.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChange_CB_UserGroup(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxUserGroupConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 100.0, "c", "b", 4, 4, 4, 4);
            comboboxUserGroup.setConstraints(comboboxUserGroupConstraints);
            panelUserGroup.addChild(comboboxUserGroup);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panelUserGroupConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, -1, -1, -1, false, false);
            panelUserGroup.setConstraints(panelUserGroupConstraints);
            equaldimensionpanel.addChild(panelUserGroup);
            panelUser = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelUser.setName("panelUser");
            panelUser.__internal_setGenerationDpi(96);
            panelUser.registerTranslationHandler(translationHandler);
            panelUser.setScaleForResolution(true);
            panelUser.setMinimumWidth(0);
            panelUser.setMinimumHeight(0);
            panelUser.setMaximumWidth(2147483647);
            panelUser.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelUserLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelUserLayout.setCentered(false);
            panelUser.setLayout(panelUserLayout);
            labelUser = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelUser.setName("labelUser");
            labelUser.__internal_setGenerationDpi(96);
            labelUser.registerTranslationHandler(translationHandler);
            labelUser.setScaleForResolution(true);
            labelUser.setMinimumWidth(10);
            labelUser.setMinimumHeight(10);
            labelUser.setText("!!Benutzer");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelUserConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            labelUser.setConstraints(labelUserConstraints);
            panelUser.addChild(labelUser);
            comboboxUser = new de.docware.framework.modules.gui.controls.GuiComboBox<de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsDataASPLMUser>();
            comboboxUser.setName("comboboxUser");
            comboboxUser.__internal_setGenerationDpi(96);
            comboboxUser.registerTranslationHandler(translationHandler);
            comboboxUser.setScaleForResolution(true);
            comboboxUser.setMinimumWidth(50);
            comboboxUser.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChange_CB_User(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxUserConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 100.0, "c", "b", 4, 4, 4, 4);
            comboboxUser.setConstraints(comboboxUserConstraints);
            panelUser.addChild(comboboxUser);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panelUserConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, -1, -1, -1, false, false);
            panelUser.setConstraints(panelUserConstraints);
            equaldimensionpanel.addChild(panelUser);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder equaldimensionpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            equaldimensionpanel.setConstraints(equaldimensionpanelConstraints);
            panelMain.addChild(equaldimensionpanel);
            panelChange = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelChange.setName("panelChange");
            panelChange.__internal_setGenerationDpi(96);
            panelChange.registerTranslationHandler(translationHandler);
            panelChange.setScaleForResolution(true);
            panelChange.setMinimumWidth(10);
            panelChange.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelChangeLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelChangeLayout.setCentered(false);
            panelChange.setLayout(panelChangeLayout);
            buttonChange = new de.docware.framework.modules.gui.controls.GuiButton();
            buttonChange.setName("buttonChange");
            buttonChange.__internal_setGenerationDpi(96);
            buttonChange.registerTranslationHandler(translationHandler);
            buttonChange.setScaleForResolution(true);
            buttonChange.setMinimumWidth(5);
            buttonChange.setMinimumHeight(10);
            buttonChange.setMnemonicEnabled(true);
            buttonChange.setText("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonChangeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "n", "b", 4, 4, 4, 4);
            buttonChange.setConstraints(buttonChangeConstraints);
            panelChange.addChild(buttonChange);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelChangeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelChangeConstraints.setPosition("east");
            panelChange.setConstraints(panelChangeConstraints);
            panelMain.addChild(panelChange);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}