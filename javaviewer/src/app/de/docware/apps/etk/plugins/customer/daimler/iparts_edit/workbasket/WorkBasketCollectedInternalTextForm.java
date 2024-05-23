/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsWorkBasketFollowUpDateId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsShowDataObjectsDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.Calendar;
import java.util.List;

/**
 * Dialog zur Darstellung der Metadaten aus NutzDok-SAA/-KEM und dem Internen Text für alle Arbeitsvorräte
 */
public class WorkBasketCollectedInternalTextForm extends AbstractJavaViewerForm implements iPartsConst {

    public static final String TABLE_FOLLOWUP_DATE_REPLACE = TABLE_DA_INTERNAL_TEXT;
    public static final String FIELD_FOLLOWUP_DATE_REPLACE = FIELD_DIT_CHANGE_DATE;

    public static boolean showInternalTextFormForWorkBasketEx(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                              iPartsWorkBasketInternalTextId wbIntTextId, DBDataObjectAttribute etsUnconfirmedAttribute) {
        WorkBasketCollectedInternalTextForm dlg = new WorkBasketCollectedInternalTextForm(dataConnector, parentForm, wbIntTextId);
        EventListener eventListener = new EventListener(Event.MENU_ITEM_EVENT) {

            @Override
            public void fire(Event event) {
                String saaOrKemNo = wbIntTextId.getSaaBkKemValue();
                if (StrUtils.isValid(saaOrKemNo)) {
                    String refType;
                    if (wbIntTextId.isKEM()) {
                        refType = iPartsWSWorkBasketItem.TYPE.KEM.name();
                    } else {
                        refType = iPartsWSWorkBasketItem.TYPE.SAA.name();
                    }
                    iPartsShowDataObjectsDialog.showNutzDokAnnotations(dataConnector, parentForm, saaOrKemNo, refType);
                }
            }
        };
        dlg.addContextMenuToMetaDataForm("showAnnotaion", WorkBasketNutzDokRemarkHelper.NUTZDOK_ANNOTATION_MENU_TEXT, null, eventListener);

        dlg.showModal();
        boolean result = dlg.isModified();
        if (result) {
            if ((etsUnconfirmedAttribute != null) && dlg.isETSModified) {
                String fieldName;
                if (wbIntTextId.isSaaOrBk()) {
                    fieldName = FIELD_DNS_ETS_UNCONFIRMED;
                } else {
                    fieldName = FIELD_DNK_ETS_UNCONFIRMED;
                }

                EtkDataObject dataObject = dlg.getDataObjectFromMetaForm();
                etsUnconfirmedAttribute.assign(dataObject.getAttribute(fieldName, false));
            }
        }
        return result;
    }

    public static boolean showInternalTextFormForWorkBasket(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                            iPartsWorkBasketInternalTextId wbIntTextId) {
        return showInternalTextFormForWorkBasketEx(dataConnector, parentForm, wbIntTextId, null);
    }

    protected double splitPaneDividerRatio = 0.2d;
    private iPartsShowDataObjectsDialog metaDataForm;
    private iPartsInternalTextForWorkbasketForm internalTextForm;
    private iPartsWorkBasketInternalTextId wbIntTextId;
    private EditUserControlsForFollowUpDate eCtrl;
    private GuiButton buttonSave;
    private iPartsDataInternalText followUpDataObject;
    private boolean isFollowUpDateModified;
    private boolean isETSModified;
    private GuiButton etsExtensionButton;

    public WorkBasketCollectedInternalTextForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               iPartsWorkBasketInternalTextId wbIntTextId) {
        this(dataConnector, parentForm, wbIntTextId, true);
    }

    /**
     * Erzeugt eine Instanz von WorkBasketCollectedInternalTextForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public WorkBasketCollectedInternalTextForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               iPartsWorkBasketInternalTextId wbIntTextId, boolean showVertical) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.wbIntTextId = wbIntTextId;
        this.isFollowUpDateModified = false;
        this.isETSModified = false;
        postCreateGui(showVertical);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(boolean showVertical) {
        if (showVertical) {
            mainWindow.splitpane.setHorizontal(true);
            splitPaneDividerRatio = 0.4d;
        }
        List<EtkDataObject> dataObjects = new DwList<>();
        String tableName;
        String key;
        String additionalText = "";
        if (wbIntTextId.isSaaOrBk()) {
            iPartsDataNutzDokSAA saaData = new iPartsDataNutzDokSAA(getProject(), new iPartsNutzDokSAAId(wbIntTextId.getSaaBkKemValue()));
            if (!saaData.existsInDB()) {
                additionalText = "!!nicht vorhanden";
                saaData.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            }
            dataObjects.add(saaData);
            tableName = saaData.getTableName();
            key = "!!Nutzdok-SAA Metadaten";
        } else {
            iPartsDataNutzDokKEM kemData = new iPartsDataNutzDokKEM(getProject(), new iPartsNutzDokKEMId(wbIntTextId.getSaaBkKemValue()));
            if (!kemData.existsInDB()) {
                additionalText = "!!nicht vorhanden";
                kemData.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            }
            dataObjects.add(kemData);
            tableName = kemData.getTableName();
            key = "!!Nutzdok-KEM Metadaten";
        }

        EtkDisplayFields displayFields = null;
        if (!showVertical) {
            displayFields = iPartsShowDataObjectsDialog.getDisplayFieldsFromTableDef(getProject(), tableName);
        }
        metaDataForm = new iPartsShowDataObjectsDialog(getConnector(), this, displayFields, dataObjects, false, showVertical,
                                                       false, 70);
        addEtsExtensionPanel();

        mainWindow.panel_saa_center.addChildBorderCenter(metaDataForm.getGui());
        if (StrUtils.isValid(additionalText)) {
            key = TranslationHandler.translate(key) + " (" + TranslationHandler.translate(additionalText) + ")";

        }
        GuiLabel label = new GuiLabel(key);
        label.setFontStyle(DWFontStyle.BOLD);
        mainWindow.panel_saa_top.addChildBorderWest(label);

        internalTextForm = new iPartsInternalTextForWorkbasketForm(getConnector(), this,
                                                                   wbIntTextId);
        setTitle(TranslationHandler.translate("!!Internen Text bearbeiten bei \"%1\"",
                                              TranslationHandler.translate(wbIntTextId.getWbType().getTitle())));
        String subTitle = "!!SAA: \"%1\"";
        String value = wbIntTextId.getSaaBkKemValue();
        if (wbIntTextId.isKEM()) {
            subTitle = "!!KEM: \"%1\"";
        } else {
            value = iPartsNumberHelper.formatPartNo(getProject(), value);
        }
        setSubTitle(TranslationHandler.translate(subTitle, value));

        mainWindow.panel_kem_center.addChildBorderCenter(internalTextForm.getGui());
        label = new GuiLabel("!!Internen Text bearbeiten");
        label.setFontStyle(DWFontStyle.BOLD);
        mainWindow.panel_kem_top.addChildBorderWest(label);

        createFollowUpDateElements();

        internalTextForm.fillDataGrid(false);

        mainWindow.splitpane.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.splitpane) {
            @Override
            public void fireOnce(Event event) {
                if (showVertical) {
                    int width = event.getIntParameter(Event.EVENT_PARAMETER_NEWWIDTH);
                    mainWindow.splitpane.setDividerPosition((int)(width * splitPaneDividerRatio) + 31);
                } else {
                    int height = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                    mainWindow.splitpane.setDividerPosition((int)(height * splitPaneDividerRatio) + 31);
                }
            }
        });
    }

    private void addEtsExtensionPanel() {
        GuiPanel panel = new GuiPanel();
        panel.setLayout(new LayoutBorder());
        panel.setPadding(0, 4, 4, 0);
        etsExtensionButton = new GuiButton("!!ET-Sichten angleichen");
        etsExtensionButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                // Dialog für ET-Sichten angleichen
                handleEtsExtension();
                handleEtsExtensionButtonEnabled();
            }
        });
        panel.addChildBorderEast(etsExtensionButton);
        metaDataForm.addControlToPanelMain(panel, ConstraintsBorder.POSITION_SOUTH);
        handleEtsExtensionButtonEnabled();
    }

    private void handleEtsExtensionButtonEnabled() {
        boolean isEnabled = false;
        EtkDataObject dataObject = getDataObjectFromMetaForm();
        if (dataObject != null) {
            if (wbIntTextId.isSaaOrBk()) {
                isEnabled = ((iPartsDataNutzDokSAA)dataObject).hasEtsUnconfirmedValues();
            } else {
                isEnabled = ((iPartsDataNutzDokKEM)dataObject).hasEtsUnconfirmedValues();
            }
        }
        etsExtensionButton.setEnabled(isEnabled);
    }

    protected EtkDataObject getDataObjectFromMetaForm() {
        List<List<EtkDataObject>> gridList = metaDataForm.getGridList();
        if (!gridList.isEmpty()) {
            List<EtkDataObject> dataObjectList = gridList.get(0);
            String tableName;
            if (wbIntTextId.isSaaOrBk()) {
                tableName = TABLE_DA_NUTZDOK_SAA;
            } else {
                tableName = TABLE_DA_NUTZDOK_KEM;
            }
            for (EtkDataObject dataObject : dataObjectList) {
                if (dataObject.getTableName().equals(tableName)) {
                    return dataObject;
                }
            }
        }
        return null;
    }

    private void handleEtsExtension() {
        EtkDataObject dataObject = WorkbasketEtsExtensionForm.showEtsExtensionForm(this, wbIntTextId, getDataObjectFromMetaForm());
        if (dataObject != null) {
            // Speichern im technischen ChangeSet wird beim OK-Event gemacht
            List<EtkDataObject> list = new DwList<>();
            list.add(dataObject);
            // Anzeige updaten
            metaDataForm.init(list);
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setSubTitle(String subTitle) {
        mainWindow.title.setSubtitle(subTitle);
    }

    public boolean isModified() {
        return internalTextForm.isModified() || isFollowUpDateModified || isETSModified;
    }

    public void addContextMenuToMetaDataForm(String menuItemName, String menuItemText, FrameworkImage menuItemIcon, EventListener eventListener) {
        metaDataForm.addContextMenu(menuItemName, menuItemText, menuItemIcon, eventListener);
    }

    private boolean doSaveFollowUpDate() {
        EditUserControls.EditResult editResult = eCtrl.stopAndStoreEdit();
        if (editResult == EditUserControls.EditResult.STORED) {
            DBDataObjectAttributes attributes = eCtrl.getAttributes().cloneMe(DBActionOrigin.FROM_DB);
            boolean existsInDB = true;
            if (followUpDataObject == null) {
                iPartsWorkBasketFollowUpDateId followUpId = new iPartsWorkBasketFollowUpDateId(wbIntTextId);
                iPartsDataInternalTextId id = new iPartsDataInternalTextId(iPartsUserAdminDb.getLoginUserName(), followUpId);
                followUpDataObject = new iPartsDataInternalText(getProject(), id);
                followUpDataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                existsInDB = false;
            }

            GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();
            String followUpDate = attributes.getFieldValue(FIELD_FOLLOWUP_DATE_REPLACE);
            followUpDataObject.setFollowUpDateAsDB(followUpDate, DBActionOrigin.FROM_EDIT);
            if (StrUtils.isValid(followUpDate)) {
                if (existsInDB) {
                    followUpDataObject.setChangeTimeStamp(Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
                }
                modifiedDataObjects.add(followUpDataObject, DBActionOrigin.FROM_EDIT);
            } else {
                if (existsInDB) {
                    modifiedDataObjects.delete(followUpDataObject, true, DBActionOrigin.FROM_EDIT);
                }
            }
            if (!modifiedDataObjects.isEmpty() || !modifiedDataObjects.getDeletedList().isEmpty()) {
                if (iPartsInternalTextForWorkbasketForm.saveDataObjectsWithTechnicalChangeSet(getProject(), modifiedDataObjects)) {
                    eCtrl.setAlternateAttributes(attributes);
                    WorkBasketInternalTextCache.updateWorkBasketCache(getProject(), wbIntTextId.getWbType());
                    isFollowUpDateModified = true;
                }
            }
            return true;
        }
        return false;
    }

    private boolean doAddFollowUpDate(GenericEtkDataObjectList modifiedDataObjects) {
        EditUserControls.EditResult editResult = eCtrl.stopAndStoreEdit();
        if (editResult == EditUserControls.EditResult.STORED) {
            DBDataObjectAttributes attributes = eCtrl.getAttributes().cloneMe(DBActionOrigin.FROM_DB);
            boolean existsInDB = true;
            if (followUpDataObject == null) {
                iPartsWorkBasketFollowUpDateId followUpId = new iPartsWorkBasketFollowUpDateId(wbIntTextId);
                iPartsDataInternalTextId id = new iPartsDataInternalTextId(iPartsUserAdminDb.getLoginUserName(), followUpId);
                followUpDataObject = new iPartsDataInternalText(getProject(), id);
                followUpDataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                existsInDB = false;
            }

            String followUpDate = attributes.getFieldValue(FIELD_FOLLOWUP_DATE_REPLACE);
            followUpDataObject.setFollowUpDateAsDB(followUpDate, DBActionOrigin.FROM_EDIT);
            if (StrUtils.isValid(followUpDate)) {
                if (existsInDB) {
                    followUpDataObject.setChangeTimeStamp(Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
                }
                modifiedDataObjects.add(followUpDataObject, DBActionOrigin.FROM_EDIT);
            } else {
                if (existsInDB) {
                    modifiedDataObjects.delete(followUpDataObject, true, DBActionOrigin.FROM_EDIT);
                }
            }
            return true;
        }
        return false;
    }

    private boolean doAddNutzDok(GenericEtkDataObjectList modifiedDataObjects) {
        EtkDataObject dataObject = getDataObjectFromMetaForm();
        if ((dataObject != null) && dataObject.isModifiedWithChildren()) {
            modifiedDataObjects.add(dataObject, DBActionOrigin.FROM_EDIT);
            isETSModified = true;
            return true;
        }
        return false;
    }

    private boolean saveChangesInChangeSet(boolean followUpModified, boolean nutzDokModified) {
        GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();
        boolean result = true;
        if (followUpModified) {
            result = doAddFollowUpDate(modifiedDataObjects);
        }
        if (result && nutzDokModified) {
            result = doAddNutzDok(modifiedDataObjects);
        }
        if (result) {
            if (!modifiedDataObjects.isEmpty() || !modifiedDataObjects.getDeletedList().isEmpty()) {
                if (iPartsInternalTextForWorkbasketForm.saveDataObjectsWithTechnicalChangeSet(getProject(), modifiedDataObjects)) {
                    eCtrl.setAlternateAttributes(eCtrl.getAttributes().cloneMe(DBActionOrigin.FROM_DB));
                    WorkBasketInternalTextCache.updateWorkBasketCache(getProject(), wbIntTextId.getWbType());
                    isFollowUpDateModified = true;
                }
            }
        }
        return result;
    }

    public void buttonCloseEvent(Event event) {
        boolean doClose = true;
        EtkDataObject dataObject = getDataObjectFromMetaForm();
        boolean followUpModified = (buttonSave != null) && buttonSave.isEnabled();
        boolean nutzDokModified = (dataObject != null) && dataObject.isModifiedWithChildren();

        if (followUpModified || nutzDokModified) {
            StringBuilder str = new StringBuilder();
            if (followUpModified) {
                str.append(TranslationHandler.translate("!!%1 ist geändert.", getTextForFollowUpDateWithFallback()));
                str.append("\n");
            }
            if (nutzDokModified) {
                str.append(TranslationHandler.translate("!!ET-Sichten wurden angeglichen."));
                str.append("\n");
            }
            str.append("\n");
            str.append(TranslationHandler.translate("!!Speichern?"));
            if (ModalResult.YES == MessageDialog.showYesNo(str.toString())) {
                doClose = saveChangesInChangeSet(followUpModified, nutzDokModified);
            }
        }
        if (doClose) {
            close();
        }
    }

    private void createFollowUpDateElements() {
        GuiPanel panel = new GuiPanel();
        panel.setLayout(new LayoutBorder());
        panel.setPadding(4);
        buttonSave = new GuiButton();
        buttonSave.setName("buttonfollowupdate");
        buttonSave.setText("!!Speichern");
//            buttonSave.setEnabled(false);
        buttonSave.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doSaveFollowUpDate();
            }
        });
        panel.addChildBorderCenter(buttonSave);

        followUpDataObject = WorkBasketInternalTextCache.getFollowUpDataObject(getProject(), wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue());
        String followUpDate = "";
        iPartsDataInternalTextId id;
        if (followUpDataObject != null) {
            followUpDate = followUpDataObject.getFollowUpDateAsDB();
            id = followUpDataObject.getAsId();
        } else {
            iPartsWorkBasketFollowUpDateId followUpId = new iPartsWorkBasketFollowUpDateId(wbIntTextId);
            id = new iPartsDataInternalTextId(iPartsUserAdminDb.getLoginUserName(), followUpId);
        }

        // FIELD_DIT_CHANGE_DATE als Platzhalter, damit kein virtuelles Feld benutzt wird
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        attributes.addField(FIELD_FOLLOWUP_DATE_REPLACE, followUpDate, DBActionOrigin.FROM_DB);

        EtkEditFields externalEditFields = new EtkEditFields();
        EtkEditField editField = new EtkEditField(TABLE_FOLLOWUP_DATE_REPLACE, FIELD_FOLLOWUP_DATE_REPLACE, false, false);
        // damit der Text richtig gesetzt ist
        editField.setText(getTextForFollowUpDate());
        editField.setDefaultText(false);
        externalEditFields.addField(editField);
        externalEditFields.loadStandards(getConfig());

        eCtrl = new EditUserControlsForFollowUpDate(getConnector(), this, TABLE_FOLLOWUP_DATE_REPLACE,
                                                    id, attributes, externalEditFields);
        EditControl editControl = eCtrl.getEditControlByFieldName(FIELD_FOLLOWUP_DATE_REPLACE);
        editControl.getLabel().setPaddingTop(12);
        editControl.getLabel().setPaddingRight(4);

        mainWindow.panel_kem_bottom.addChildBorderWest(editControl.getLabel());
        mainWindow.panel_kem_bottom.addChildBorderCenter(editControl.getAbstractGuiControl());
        mainWindow.panel_kem_bottom.addChildBorderEast(panel);
        mainWindow.panel_kem_bottom.setTitle(editControl.getLabel().getText());
    }

    private EtkMultiSprache getTextForFollowUpDate() {
        EtkDisplayField displayField = new EtkDisplayField(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_FOLLOWUP_DATE, false, false);
        displayField.loadStandards(getConfig());
        return displayField.getText();
    }

    private String getTextForFollowUpDateWithFallback() {
        EtkMultiSprache multi = getTextForFollowUpDate();
        if (multi != null) {
            return multi.getTextByNearestLanguage(getProject().getViewerLanguage(), getProject().getDataBaseFallbackLanguages());
        }
        return TranslationHandler.translate("!!Wiedervorlagetermin");
    }


    private class EditUserControlsForFollowUpDate extends EditUserControls {

        public EditUserControlsForFollowUpDate(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                               IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
            super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        }

        @Override
        protected void setAttributes() {

        }

        @Override
        protected boolean isVirtualFieldEditable(String tableName, String fieldName) {
            return true;
        }

        @Override
        protected void enableOKButton(boolean enabled) {
            super.enableOKButton(enabled);
            buttonSave.setEnabled(enabled);
        }

        @Override
        public boolean isModified() {
            return checkForModified();
        }

        @Override
        protected boolean checkValues() {
            boolean result = super.checkValues();
            if (result) {
                String inputDate = attributes.getFieldValue(FIELD_FOLLOWUP_DATE_REPLACE);
                if (StrUtils.isValid(inputDate)) {
                    String currentDate = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
                    if (currentDate.compareTo(inputDate) > 0) {
                        String msg = TranslationHandler.translate("!!%1 muss in der Zukunft liegen.", getTextForFollowUpDateWithFallback());
                        MessageDialog.showError(msg);
                        result = false;
                    }
                }
            }
            return result;
        }

        public void setAlternateAttributes(DBDataObjectAttributes attributes) {
            attributes.setLoaded(true);
            this.attributes = attributes;
            doEnableButtons(null);
        }
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
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_saa;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_saa_top;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_saa_center;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_saa_bottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_kem;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_kem_top;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_kem_center;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_kem_bottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
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
            splitpane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpane.setName("splitpane");
            splitpane.__internal_setGenerationDpi(96);
            splitpane.registerTranslationHandler(translationHandler);
            splitpane.setScaleForResolution(true);
            splitpane.setMinimumWidth(10);
            splitpane.setMinimumHeight(10);
            splitpane.setHorizontal(false);
            splitpane.setDividerPosition(197);
            splitpane_saa = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_saa.setName("splitpane_saa");
            splitpane_saa.__internal_setGenerationDpi(96);
            splitpane_saa.registerTranslationHandler(translationHandler);
            splitpane_saa.setScaleForResolution(true);
            splitpane_saa.setMinimumWidth(0);
            splitpane_saa.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_saaLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_saa.setLayout(splitpane_saaLayout);
            panel_saa_top = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_saa_top.setName("panel_saa_top");
            panel_saa_top.__internal_setGenerationDpi(96);
            panel_saa_top.registerTranslationHandler(translationHandler);
            panel_saa_top.setScaleForResolution(true);
            panel_saa_top.setMinimumWidth(10);
            panel_saa_top.setMinimumHeight(10);
            panel_saa_top.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_saa_topLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_saa_top.setLayout(panel_saa_topLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_saa_topConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_saa_topConstraints.setPosition("north");
            panel_saa_top.setConstraints(panel_saa_topConstraints);
            splitpane_saa.addChild(panel_saa_top);
            panel_saa_center = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_saa_center.setName("panel_saa_center");
            panel_saa_center.__internal_setGenerationDpi(96);
            panel_saa_center.registerTranslationHandler(translationHandler);
            panel_saa_center.setScaleForResolution(true);
            panel_saa_center.setMinimumWidth(10);
            panel_saa_center.setMinimumHeight(10);
            panel_saa_center.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_saa_centerLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_saa_center.setLayout(panel_saa_centerLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_saa_centerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_saa_center.setConstraints(panel_saa_centerConstraints);
            splitpane_saa.addChild(panel_saa_center);
            panel_saa_bottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_saa_bottom.setName("panel_saa_bottom");
            panel_saa_bottom.__internal_setGenerationDpi(96);
            panel_saa_bottom.registerTranslationHandler(translationHandler);
            panel_saa_bottom.setScaleForResolution(true);
            panel_saa_bottom.setMinimumWidth(10);
            panel_saa_bottom.setMinimumHeight(10);
            panel_saa_bottom.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_saa_bottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_saa_bottom.setLayout(panel_saa_bottomLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_saa_bottomConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_saa_bottomConstraints.setPosition("south");
            panel_saa_bottom.setConstraints(panel_saa_bottomConstraints);
            splitpane_saa.addChild(panel_saa_bottom);
            splitpane.addChild(splitpane_saa);
            splitpane_kem = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_kem.setName("splitpane_kem");
            splitpane_kem.__internal_setGenerationDpi(96);
            splitpane_kem.registerTranslationHandler(translationHandler);
            splitpane_kem.setScaleForResolution(true);
            splitpane_kem.setMinimumWidth(0);
            splitpane_kem.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_kemLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_kem.setLayout(splitpane_kemLayout);
            panel_kem_top = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_kem_top.setName("panel_kem_top");
            panel_kem_top.__internal_setGenerationDpi(96);
            panel_kem_top.registerTranslationHandler(translationHandler);
            panel_kem_top.setScaleForResolution(true);
            panel_kem_top.setMinimumWidth(10);
            panel_kem_top.setMinimumHeight(10);
            panel_kem_top.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_kem_topLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_kem_top.setLayout(panel_kem_topLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_kem_topConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_kem_topConstraints.setPosition("north");
            panel_kem_top.setConstraints(panel_kem_topConstraints);
            splitpane_kem.addChild(panel_kem_top);
            panel_kem_center = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_kem_center.setName("panel_kem_center");
            panel_kem_center.__internal_setGenerationDpi(96);
            panel_kem_center.registerTranslationHandler(translationHandler);
            panel_kem_center.setScaleForResolution(true);
            panel_kem_center.setMinimumWidth(10);
            panel_kem_center.setMinimumHeight(10);
            panel_kem_center.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_kem_centerLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_kem_center.setLayout(panel_kem_centerLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_kem_centerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_kem_center.setConstraints(panel_kem_centerConstraints);
            splitpane_kem.addChild(panel_kem_center);
            panel_kem_bottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_kem_bottom.setName("panel_kem_bottom");
            panel_kem_bottom.__internal_setGenerationDpi(96);
            panel_kem_bottom.registerTranslationHandler(translationHandler);
            panel_kem_bottom.setScaleForResolution(true);
            panel_kem_bottom.setMinimumWidth(10);
            panel_kem_bottom.setMinimumHeight(10);
            panel_kem_bottom.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_kem_bottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_kem_bottom.setLayout(panel_kem_bottomLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_kem_bottomConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_kem_bottomConstraints.setPosition("south");
            panel_kem_bottom.setConstraints(panel_kem_bottomConstraints);
            splitpane_kem.addChild(panel_kem_bottom);
            splitpane.addChild(splitpane_kem);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpane.setConstraints(splitpaneConstraints);
            panelMain.addChild(splitpane);
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
            buttonpanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCloseActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    buttonCloseEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
//</editor-fold>
}