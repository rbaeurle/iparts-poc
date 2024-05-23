package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiUserSelectComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditFormComboboxHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.combimodules.useradmin.db.UserDbObject;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiComboBoxMode;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Dialog für die Auswahl von einem Benutzer (abhängig vom {@link iPartsUserTypes}).
 */
public class SelectUserDialog extends AbstractJavaViewerForm {

    private boolean isReadOnly = false;
    private iPartsUserTypes userType;
    private String selectUserId;
    private String[] excludeUserIds;
    private iPartsRight right;

    // TextFelder für ReadOnly Betrieb
    private GuiTextField textFieldUser = null;
    private iPartsGuiUserSelectComboBox comboBoxUser;
    protected EventListeners eventListeners;

    /**
     * Liefert die ID der ausgewählten virtuellen Benutzergruppe bzw. die ID vom ausgewählten Benutzer für alle Autoren-Rollen
     * und Qualitätsprüfer zurück.
     *
     * @param dataConnector
     * @param parentForm
     * @param selectUserId
     * @param right         Recht für die Ermittlung der Benutzer und virtuellen Benutzergruppen (speziell bzgl. Gültigkeitsbereich)
     * @param subTitle
     * @return
     */
    public static String selectAssignableUser(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                              String selectUserId, iPartsRight right, String subTitle) {
        return selectUser(dataConnector, parentForm, selectUserId, right, iPartsUserTypes.ASSIGN, subTitle,
                          EditToolbarButtonAlias.EDIT_ORDER_ASSIGN.getImage());
    }

    /**
     * Liefert die ID der ausgewählten virtuellen Benutzergruppe bzw. die ID vom ausgewählten Benutzer für alle Autoren-Rollen
     * zurück.
     *
     * @param dataConnector
     * @param parentForm
     * @param selectUserId
     * @param right         Recht für die Ermittlung der Benutzer und virtuellen Benutzergruppen (speziell bzgl. Gültigkeitsbereich)
     * @return
     */
    public static String selectAuthorUser(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          String selectUserId, iPartsRight right) {
        return selectUser(dataConnector, parentForm, selectUserId, right, iPartsUserTypes.AUTHOR, "!!Benutzergruppe oder Autor",
                          EditToolbarButtonAlias.EDIT_ORDER_ASSIGN.getImage());
    }

    /**
     * Liefert die ID der ausgewählten virtuellen Benutzergruppe bzw. die ID vom ausgewählten Benutzer für die Rolle
     * Qualitätsprüfer zurück.
     *
     * @param dataConnector
     * @param parentForm
     * @param selectUserId
     * @param right         Recht für die Ermittlung der Benutzer und virtuellen Benutzergruppen (speziell bzgl. Gültigkeitsbereich)
     * @return
     */
    public static String selectQAUser(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                      String selectUserId, iPartsRight right) {
        return selectUser(dataConnector, parentForm, selectUserId, right, iPartsUserTypes.QA, "!!Benutzergruppe oder Qualitätsprüfer",
                          EditToolbarButtonAlias.EDIT_ORDER_ASSIGN.getImage());
    }

    /**
     * Liefert die ID der ausgewählten virtuellen Benutzergruppe zurück bzw. die ID vom ausgewählten Benutzer für den
     * gewünschten Typ zurück.
     *
     * @param dataConnector
     * @param parentForm
     * @param selectUserId
     * @param right         Recht für die Ermittlung der Benutzer und virtuellen Benutzergruppen (speziell bzgl. Gültigkeitsbereich)
     * @param userType
     * @param subTitle
     * @param image
     * @return
     */
    public static String selectUser(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                    String selectUserId, iPartsRight right, iPartsUserTypes userType,
                                    String subTitle, FrameworkImage image) {
        SelectUserDialog dlg = new SelectUserDialog(dataConnector, parentForm, selectUserId, null, right, userType, false,
                                                    subTitle, image);
        dlg.setTitle(userType.getTitle());

        if (dlg.showModal(parentForm.getRootParentWindow()) == ModalResult.OK) {
            return dlg.getSelectedUserId();
        }
        return null;
    }

    /**
     * Liefert die IDs der ausgewählten virtuellen Benutzergruppen zurück bzw. die IDs von den ausgewählten Benutzern für
     * den gewünschten Typ zurück.
     *
     * @param dataConnector
     * @param parentForm
     * @param excludeUserIds IDs der Benutzer, die nicht auswählbar sein sollen
     * @param right          Recht für die Ermittlung der Benutzer und virtuellen Benutzergruppen (speziell bzgl. Gültigkeitsbereich)
     * @param userType
     * @param subTitle
     * @param image
     * @return
     */
    public static List<String> selectUsers(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           String[] excludeUserIds, iPartsRight right, iPartsUserTypes userType,
                                           String subTitle, FrameworkImage image) {
        SelectUserDialog dlg = new SelectUserDialog(dataConnector, parentForm, null, excludeUserIds, right, userType, true,
                                                    subTitle, image);
        dlg.setTitle(userType.getTitle());

        if (dlg.showModal(parentForm.getRootParentWindow()) == ModalResult.OK) {
            return dlg.getSelectedUserIds();
        }
        return null;
    }

    /**
     * Erzeugt eine Instanz von SelectUserDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public SelectUserDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                            String selectUserId, String[] excludeUserIds, iPartsRight right, iPartsUserTypes userType,
                            boolean multiSelect, String subTitle, FrameworkImage image) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.eventListeners = new EventListeners();
        this.userType = userType;
        this.selectUserId = selectUserId;
        this.excludeUserIds = excludeUserIds;
        this.right = right;
        if (StrUtils.isValid(subTitle)) {
            mainWindow.labelUser.setText(subTitle);
        }
        postCreateGui(image, multiSelect);
    }

    private void postCreateGui(FrameworkImage image, boolean multiSelect) {
        if (image == null) {
            mainWindow.image.setVisible(false);
        } else {
            mainWindow.image.setImage(image);
        }
        comboBoxUser = new iPartsGuiUserSelectComboBox();
        if (multiSelect) {
            comboBoxUser.setMode(GuiComboBoxMode.Mode.CHECKBOX);
        }
        comboBoxUser.setName("comboBoxUser");
        RComboBox.replaceGuiComboBox(mainWindow.comboboxUser, comboBoxUser);

        comboBoxUser.init(userType.getUsersMap(iPartsUserAdminDb.getLoginUserIdForSession(), right, true), true, selectUserId,
                          excludeUserIds);
        comboBoxUser.requestFocus();
        comboBoxUser.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                fireOnChangeEvent(event);
            }
        });

        mainWindow.pack();
        enableButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        return showModal(null);
    }

    public ModalResult showModal(GuiWindow parentWindow) {
        return mainWindow.showModal(parentWindow);
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public iPartsUserTypes getUserType() {
        return userType;
    }

    /**
     * Liefert die (erste) ausgewählte Benutzer-ID bzw. ID der virtuellen Benutzergruppe zurück.
     *
     * @return {@code null} falls keine Auswahl stattgefunden hat
     */
    public String getSelectedUserId() {
        return comboBoxUser.getSelectedUserObject();
    }

    /**
     * Liefert die ausgewählten Benutzer-IDs bzw. IDs der virtuellen Benutzergruppen zurück.
     *
     * @return {@code null} falls keine Auswahl stattgefunden hat
     */
    public List<String> getSelectedUserIds() {
        return comboBoxUser.getSelectedUserObjects();
    }

    public boolean isValid() {
        return getSelectedUserId() != null;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * ReadOnly setzen: Die ComboBox wird durch ein Textfeld (nicht editierbar und spezielle Hintergrundfarbe) ersetzt.
     *
     * @param value
     */
    public void setReadOnly(boolean value) {
        if (value != isReadOnly) {
            if (value) {
                boolean setName = textFieldUser == null;
                textFieldUser = EditFormComboboxHelper.replaceComboBoxByTextField(comboBoxUser, textFieldUser, mainWindow.panelMain);
                if (setName) {
                    textFieldUser.setName("textFieldUser");
                }
            } else {
                EditFormComboboxHelper.replaceTextFieldByComboBox(textFieldUser, comboBoxUser, mainWindow.panelMain);
            }
            isReadOnly = value;
        }
    }

    /**
     * Eventlistener für Änderugen bei jeder ComboBox setzen
     *
     * @param eventListener
     */
    public void addEventListener(EventListener eventListener) {
        eventListeners.addEventListener(eventListener);
    }

    /**
     * Eventlistener für Änderugen bei jeder ComboBox löschen
     *
     * @param eventListener
     */
    public void removeEventListener(EventListener eventListener) {
        eventListeners.removeEventListener(eventListener);
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

    private void enableButtons() {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isValid());
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
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
        private de.docware.framework.modules.gui.controls.GuiImage image;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelUser;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelUser;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<UserDbObject> comboboxUser;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setTitle("!!Benutzergruppe oder Benutzer auswählen");
            this.setResizable(false);
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
            panelMain.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            image = new de.docware.framework.modules.gui.controls.GuiImage();
            image.setName("image");
            image.__internal_setGenerationDpi(96);
            image.registerTranslationHandler(translationHandler);
            image.setScaleForResolution(true);
            image.setMinimumWidth(10);
            image.setMinimumHeight(10);
            image.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag imageConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "n", "n", 0, 0, 0, 0);
            image.setConstraints(imageConstraints);
            panelMain.addChild(image);
            panelUser = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelUser.setName("panelUser");
            panelUser.__internal_setGenerationDpi(96);
            panelUser.registerTranslationHandler(translationHandler);
            panelUser.setScaleForResolution(true);
            panelUser.setMinimumWidth(10);
            panelUser.setMinimumHeight(10);
            panelUser.setPaddingLeft(4);
            panelUser.setPaddingRight(4);
            panelUser.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelUserLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelUser.setLayout(panelUserLayout);
            labelUser = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelUser.setName("labelUser");
            labelUser.__internal_setGenerationDpi(96);
            labelUser.registerTranslationHandler(translationHandler);
            labelUser.setScaleForResolution(true);
            labelUser.setMinimumWidth(10);
            labelUser.setMinimumHeight(10);
            labelUser.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            labelUser.setText("!!Benutzer");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelUserConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 4, 0);
            labelUser.setConstraints(labelUserConstraints);
            panelUser.addChild(labelUser);
            comboboxUser = new de.docware.framework.modules.gui.controls.GuiComboBox<UserDbObject>();
            comboboxUser.setName("comboboxUser");
            comboboxUser.__internal_setGenerationDpi(96);
            comboboxUser.registerTranslationHandler(translationHandler);
            comboboxUser.setScaleForResolution(true);
            comboboxUser.setMinimumWidth(350);
            comboboxUser.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxUserConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 100.0, "c", "h", 4, 0, 0, 0);
            comboboxUser.setConstraints(comboboxUserConstraints);
            panelUser.addChild(comboboxUser);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelUserConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "b", 0, 0, 0, 0);
            panelUser.setConstraints(panelUserConstraints);
            panelMain.addChild(panelUser);
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