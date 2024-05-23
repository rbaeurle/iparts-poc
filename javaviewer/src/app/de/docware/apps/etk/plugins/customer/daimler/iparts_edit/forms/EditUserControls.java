/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EditControls;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.EtkPluginApi;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.config.db.datatypes.DatatypeUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBExtendedDataTypeProvider;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableHeader;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.AbstractLayout;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.font.DefaultFont;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.output.swing.SwingHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.HTMLUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.j2ee.EC;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class EditUserControls extends AbstractJavaViewerForm implements iPartsConst {

    public enum EditResult {STORED, UNMODIFIED, ERROR}

    private String noMasterDataExistsText = "!!Keine Stammdaten vorhanden.";

    public class GuiTextAreaRelatedInfo extends GuiTextArea {

        public static final String TYPE = "textarearelatedinfo";

        public GuiTextAreaRelatedInfo() {
            super();
            setType(TYPE);
            setEditable(false);
            setMinimumHeight(getPreferredHeight());
            setBorder(0, Color.WHITE);
            setLineWrap(true);
        }

        public GuiTextAreaRelatedInfo(String text) {
            this();
            setText(text);
        }

        @Override
        public void setText(String text) {
            if (EC.isAlreadyHtml(text)) {
                text = StrUtils.convertHTMLtoString(text);
            }
            super.setText(text);
        }

        @Override
        public void setFontStyle(DWFontStyle style) {
            super.setFontStyle(style);
        }

        public void calcMinimumHeight(int colWidth) {
            Font actFont = DefaultFont.getInstance();
            if (getFontStyle() == DWFontStyle.BOLD) {
                actFont = DefaultFont.getInstance().deriveFont(Font.BOLD);
            }
            int minHeight = HTMLUtils.getMinHeightWrappableText(getText(), colWidth, actFont, 16);
            setMinimumHeight(minHeight);
            //setMaximumHeight(minHeight);
        }
    }

    protected EtkEditFields editFields;
    protected String tableName;
    protected DBDataObjectAttributes attributes;
    protected IdWithType id;
    protected EditControls editControls;
    protected boolean readOnly = false;
    protected boolean firstReadOnly = true;
    private boolean useRelatedInfoStyle = true;
    private GuiTable table;
    protected EtkEditFields externalEditFields;
    protected Dimension calculatedPanelSize;

    /**
     * Erzeugt eine Instanz von EditUserControls.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                            IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        this(dataConnector, parentForm, tableName, id, attributes, externalEditFields, false, "", null);
    }

    public EditUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                            IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                            boolean useRelatedInfoStyle, String noMasterDataExistsText) {
        this(dataConnector, parentForm, tableName, id, attributes, externalEditFields, useRelatedInfoStyle, noMasterDataExistsText,
             null);
    }

    public EditUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                            IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                            boolean useRelatedInfoStyle, String noMasterDataExistsText, EditControls editControls) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.tableName = tableName;
        if (editControls != null) {
            this.editControls = editControls;
        } else {
            this.editControls = new EditControls();
        }
        this.useRelatedInfoStyle = useRelatedInfoStyle;
        this.externalEditFields = externalEditFields;
        this.attributes = attributes;
        this.id = id;
        if (StrUtils.isValid(noMasterDataExistsText)) {
            this.noMasterDataExistsText = noMasterDataExistsText;
        }
        setAttributes();
        postCreateGui();
    }

    /**
     * Konstruktor mit eigenen {@link EtkEditFields} und der Möglichkeit den RelatedInfoStyle zu setzen
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     * @param id
     * @param externalEditFields
     * @param useRelatedInfoStyle
     */
    public EditUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                            IdWithType id, EtkEditFields externalEditFields, boolean useRelatedInfoStyle) {
        this(dataConnector, parentForm, tableName, id, null, externalEditFields, useRelatedInfoStyle, "");
    }

    public EditUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                            IdWithType id, EtkEditFields externalEditFields) {
        this(dataConnector, parentForm, tableName, id, null, externalEditFields);
    }

    public EditUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id) {
        this(dataConnector, parentForm, tableName, id, null);
    }

    public EditUserControls(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.tableName = tableName;
        this.id = id;
        this.editControls = new EditControls();
        this.useRelatedInfoStyle = true;
        setAttributes();
        postCreateGui();
    }

    public Dimension getCalculatedPanelSize() {
        return calculatedPanelSize;
    }

    protected void setCalculatedPanelSize(int totalWidth, int totalHeight) {
        if ((totalWidth > 0) && (totalHeight > 0)) {
            // Window-Elemente von der Höhe abziehen
            totalHeight -= (mainWindow.title.getPreferredHeight() + mainWindow.buttonPanel.getPreferredHeight() - 8);
            // keine negativen Werte
            totalHeight = Math.max(totalHeight, 0);
            totalWidth = Math.max(totalWidth, 0);
            calculatedPanelSize = new Dimension(totalWidth, totalHeight);
        } else {
            calculatedPanelSize = new Dimension();
        }
    }

    protected void setWindowName(String name) {
        mainWindow.setName(name);
    }

    protected String[] getWhereValues() {
        String idFirstPrimaryKey = id.getValue(1);
        List<iPartsVirtualNode> virtualList = null;
        if (idFirstPrimaryKey.startsWith(iPartsVirtualNode.VIRTUAL_INDICATOR)) {
            virtualList = iPartsVirtualNode.parseVirtualIds(idFirstPrimaryKey);
        }
        String[] whereValues;
        if ((virtualList != null) && !virtualList.isEmpty()) {
            IdWithType virtualId = virtualList.get(0).getId();
            whereValues = virtualId.toStringArrayWithoutType();
        } else {
            whereValues = id.toStringArrayWithoutType();
        }
        return whereValues;
    }

    protected void setAttributes() {
        if (tableName == null) {
            return;
        }
        String[] whereValues = getWhereValues();
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(tableName);
        List<String> pkFields = tableDef.getPrimaryKeyFields();
        String[] whereFields = new String[pkFields.size()];
        for (int i = 0; i < pkFields.size(); i++) {
            whereFields[i] = pkFields.get(i);
        }

        EtkRecord rec = getProject().getEtkDbs().getRecord(tableName, whereFields, whereValues);
        if (attributes == null) {
            attributes = new DBDataObjectAttributes();
        }
        iPartsEditUserControlsHelper.setMissingAttributes(rec, attributes, tableName, getProject());
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    protected void postCreateGui() {
        clearEditFieldsPanel();
        editFields = new EtkEditFields();
        if (attributes != null) {
            if (externalEditFields != null) {
                editFields.assign(externalEditFields);
            } else {
                EtkEditFieldHelper.getEditFields(getProject(), tableName, editFields, false);
                // Übergebene EditFields können vor der Übergabe modifiziert werden. EditFields die über den Tabellennamen
                // erzeugt wurden können nur einzeln modifiziert werden (in prepareControls() via modifyEditControl()).
                // Hier können die erzeugten EditFields auf einen Schlag und noch vor der ganzen Verarbeitung modifiziert
                // werden
                modifyEditFieldsCreatedFromTablename();
            }

            // PSK-Teilestammfelder nur als PSK-Benutzer anzeigen
            EtkEditFieldHelper.removePSKMatFieldsIfNecessary(editFields, iPartsRight.checkPSKInSession());

            // Virtuelle Felder dürfen generell nicht editierbar sein, Ausnahme: es wird ein Editor zur Verfügung gestellt
            for (EtkEditField editField : editFields.getFields()) {
                String fieldName = editField.getKey().getFieldName();
                if (VirtualFieldsUtils.isVirtualField(fieldName)) {
                    editField.setEditierbar(isVirtualFieldEditable(editField.getKey().getTableName(), fieldName));
                }
            }

            if (useRelatedInfoStyle) {
                prepareControlsForRelatedInfo(editFields);
            } else {
                removeDoubleEditFields(editFields);
                prepareControls(editFields);
                doEnableButtons(null);
            }

            //prepareEDS_SAAD(editFields);


            //preparePart(editFields);
            mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
        } else {
            GuiLabel noMaterialLabel = new GuiLabel(noMasterDataExistsText);
            noMaterialLabel.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 100, 100, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_VERTICAL, 0, 0, 0, 0));
            addEditFieldChild(noMaterialLabel);
            setReadOnly(true);
            int height = 4 * (noMaterialLabel.getPreferredHeight() + 8);
            height += (mainWindow.title.getPreferredHeight() + mainWindow.buttonPanel.getPreferredHeight() - 8);
            setCalculatedPanelSize(mainWindow.panelEditFields.getPreferredWidth(), height);
        }
    }

    protected void removeDoubleEditFields(EtkEditFields eFields) {
        Set<String> foundFieldNames = new HashSet<>();
        Iterator<EtkEditField> iterator = eFields.getFields().iterator();
        while (iterator.hasNext()) {
            EtkEditField editField = iterator.next();
            if (editField.isVisible()) {
                String key = editField.getKey().getName();
                if (!foundFieldNames.add(key)) {
                    // ist doppelt => entfernen
                    iterator.remove();
                }
            }
        }
    }

    protected boolean isVirtualFieldEditable(String tableName, String fieldName) {
        return EtkPluginApi.hasEditorForVirtualField(tableName, fieldName);
    }

    protected void clearEditFieldsPanel() {
        mainWindow.panelEditFields.removeAllChildren();
    }

    protected void addEditFieldChild(AbstractGuiControl child) {
        mainWindow.panelEditFields.addChild(child);
    }

    protected void setEditFieldLayout(AbstractLayout layout) {
        mainWindow.panelEditFields.setLayout(layout);
    }

    private void preparePart(EtkEditFields eFields) {
        iPartsPartId partId = new iPartsPartId("A0000074699", "");
        EtkDataPart part = EtkDataObjectFactory.createDataPart();
        part.init(getProject());
        part.loadFromDB(partId);
        EtkEditFieldHelper.getEditFieldMaterialEdit(getProject(), eFields);
        prepareControls(eFields);
    }

    private void prepareEDS_SAAD(EtkEditFields eFields) {
        EtkEditFieldHelper.getEditFields(getProject(), iPartsConst.TABLE_DA_SAA, eFields, false);
        EtkRecord rec = getProject().getDB().getRecord(iPartsConst.TABLE_DA_SAA, new String[]{ iPartsConst.FIELD_DS_CONST_DESC }, new String[]{ "Z53988801" });
        DBDataObjectAttributes attribs = DBDataObjectAttributes.getFromRecord(rec, DBActionOrigin.FROM_DB);
        prepareControls(eFields);
    }

    protected void prepareControls(EtkEditFields eFields) {
        int gridY = 0;
        int totalHeight = 0;
        mainWindow.panelEditFields.setLayout(new LayoutGridBag(false));
        EventListener listener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doEnableButtons(event);
            }
        };

        String viewerLanguage = getConfig().getCurrentViewerLanguage();
        List<String> fallbackLanguages = getConfig().getDataBaseFallbackLanguages();
        for (EtkEditField field : eFields.getVisibleEditFields()) {
            DBDataObjectAttribute attrib = getAttributeFromKey(field);
            String initialValue = calculateInitialValue(field, attrib);
            EtkDataArray initialDataArray = calculateInitialDataArray(field, attrib);
            String dbLanguage = calculateDBLanguage(field);
            String labelText = null;
            if (!field.isDefaultText()) {
                labelText = field.getText().getTextByNearestLanguage(viewerLanguage, fallbackLanguages);
            }
            EditControl ctrl = editControls.createForEdit(null, getProject(), field.getKey().getTableName(), field.getKey().getFieldName(), dbLanguage,
                                                          getProject().getViewerLanguage(), initialValue, labelText, gridY);
            if (initialDataArray != null) {
                ctrl.getEditControl().setArray(initialDataArray);
            }
            modifyEditControl(ctrl, field, initialValue, initialDataArray);
            insertEditControl(ctrl, field, gridY, listener);
            handleMultiLangControl(ctrl, field, attrib);
            totalHeight += ctrl.getEditControl().getControl().getPreferredHeight() + 8;
            gridY++;
        }
        resizeForm(totalHeight);
    }

    protected String calculateInitialValue(EtkEditField field, DBDataObjectAttribute attrib) {
        String initialValue = "";
        if (attrib != null) {
            if (field.isMultiLanguage()) {
                setAttributeToMultiLang(attrib, field.getKey().getTableName());
            } else if (!field.isArray()) { // Arrays werden in calculateInitialDataArray() behandelt
                initialValue = attrib.getAsString();
            }
        }
        return initialValue;
    }

    protected EtkDataArray calculateInitialDataArray(EtkEditField field, DBDataObjectAttribute attrib) {
        EtkDataArray initialDataArray = null;
        if ((attrib != null) && field.isArray()) { // Spezialbehandlung für Arrays
            initialDataArray = setAttributeToArray(attrib, field.getKey().getTableName());
        }
        return initialDataArray;
    }

    protected String calculateDBLanguage(EtkEditField field) {
        // aktuelle DB-Sprache verwenden, falls keine explizit andere DB-Sprache für dieses Feld gewünscht ist
        String dbLanguage = field.getLanguage();
        if (dbLanguage.isEmpty()) {
            dbLanguage = getProject().getDBLanguage();
        }
        return dbLanguage;
    }

    protected void handleMultiLangControl(EditControl ctrl, EtkEditField field, DBDataObjectAttribute attrib) {
        if (field.isMultiLanguage()) {
            EtkMultiSprache multiEdit;
            if (attrib != null) {

                DBExtendedDataTypeProvider tempLanguageProvider = EtkDataObject.getTempExtendedDataTypeProvider(getProject(), field.getKey().getTableName());

                EtkMultiSprache multiLanguageFromDB = attrib.getAsMultiLanguage(tempLanguageProvider, true);
                if (multiLanguageFromDB != null) {
                    multiEdit = multiLanguageFromDB.cloneMe();
                } else {
                    multiEdit = new EtkMultiSprache();
                }
            } else {
                multiEdit = new EtkMultiSprache();
            }
            multiEdit.completeWithLanguages(getProject().getConfig().getDatabaseLanguages());
            if (field.getKey().getName().equals(TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_ADDTEXT))) {
                iPartsGuiNeutralTextCompleteEditControl ntCtrl = (iPartsGuiNeutralTextCompleteEditControl)ctrl.getEditControl().getControl();
                ntCtrl.setConnector(getConnector());
                ntCtrl.setMultiLanguage(multiEdit);
            } else {
                ((GuiMultiLangEdit)ctrl.getEditControl().getControl()).setMultiLanguage(multiEdit);
            }
        }
    }

    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
    }

    protected void modifyEditFieldsCreatedFromTablename() {

    }

    protected void insertEditControl(EditControl ctrl, EtkEditField field, int gridY, EventListener listener) {
        ctrl.getLabel().setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
        ctrl.getLabel().setConstraints(createLabelConstraints(ctrl.getLabel(), gridY));
        ctrl.getEditControl().getControl().setConstraints(createValueConstraints(gridY));
        if (isMandatoryField(field)) {
            ctrl.getLabel().setFontStyle(DWFontStyle.BOLD);
        }
        ctrl.getEditControl().setReadOnly(!field.isEditierbar());
        ctrl.getEditControl().getControl().addEventListener(listener);

        addEditControlChild(ctrl);
    }

    protected void addEditControlChild(EditControl ctrl) {
        addEditFieldChild(ctrl.getLabel());
        addEditFieldChild(ctrl.getEditControl().getControl());
    }

    public boolean addExtraRowElement(AbstractGuiControl guiControl) {
        if (!useRelatedInfoStyle) {
            int gridY = -1;
            if (editControls.size() > 0) {
                EditControl ctrl = editControls.get(editControls.size() - 1);
                AbstractConstraints constraints = ctrl.getAbstractGuiControl().getConstraints();
                if (constraints instanceof ConstraintsGridBag) {
                    gridY = ((ConstraintsGridBag)constraints).getGridy();
                }
            }
            if (gridY >= 0) {
                gridY++;
                GuiPanel panel = new GuiPanel();
                panel.setConstraints(createPanelConstraints(gridY));
                LayoutBorder panelLayout = new LayoutBorder();
                panel.setLayout(panelLayout);
                guiControl.setConstraints(new ConstraintsBorder());
                panel.addChild(guiControl);
                int extraHeight = panel.getPreferredHeight() + 8;
                addEditFieldChild(panel);
                setHeight(getHeight() + extraHeight);
                setCalculatedPanelSize((int)calculatedPanelSize.getWidth(), getHeight());
                return true;
            }
        }
        return false;
    }

    protected void resizeForm(int totalHeight) {
        int totalWidth = 0;
        int extraWidth = 0;
        //Bestimmen, welche Labels mehr Platz brauchen und die max Breite der Controls
        for (AbstractGuiControl control : mainWindow.panelEditFields.getChildren()) {
            if (control instanceof GuiLabel) {
                totalWidth = Math.max(totalWidth, control.getPreferredWidth());
            } else {
                extraWidth = Math.max(extraWidth, control.getPreferredWidth());
            }
        }
        Dimension screenSize = FrameworkUtils.getScreenSize();
        screenSize.width -= getScreenBorderWidth();
        screenSize.height -= getScreenBorderHeight();
        final int requiredAdditionalWidth = getRequiredAdditionalWidth();
        //falls die Breite der Controls größer ist als die Screen-Breite
        if (totalWidth + extraWidth + requiredAdditionalWidth > screenSize.width) {
            int newMaxControlWidth = screenSize.width - totalWidth - requiredAdditionalWidth;
            for (AbstractGuiControl control : mainWindow.panelEditFields.getChildren()) {
                if (!(control instanceof GuiLabel)) {
                    if (control instanceof GuiTextArea) {
                        control.setMaximumWidth(newMaxControlWidth);
                        control.setMinimumWidth(control.getMaximumWidth());
                    } else {
                        control.setMaximumWidth(newMaxControlWidth + 14); // 14px wegen vertikalem Scrollbar von GuiTextAreas
                        if (control.getMinimumWidth() > control.getMaximumWidth()) {
                            control.setMinimumWidth(control.getMaximumWidth());
                        }
                    }
                }
            }
        }
        totalWidth = Math.min(Math.max(getWidth(), totalWidth + extraWidth + requiredAdditionalWidth), screenSize.width);
        totalHeight = Math.min(Math.max(getHeight(), totalHeight + getRequiredAdditionalHeight()), screenSize.height);
        setHeight(totalHeight);
        setWidth(totalWidth);
        setCalculatedPanelSize(totalWidth, totalHeight);
        if (!editControls.isEmpty()) {
            for (EditControl editControl : editControls) {
                if (editFields.getVisibleEditFields().get(editControl.getIndexInList()).isEditierbar()) {
                    editControl.getEditControl().getControl().requestFocus();
                    break;
                }
            }
        }
    }

    /**
     * Liefert die zusätzlich zu den Labels und Controls benötigte Breite zurück.
     *
     * @return
     */
    protected int getRequiredAdditionalWidth() {
        return 60; // 60px wegen Fensterrand und Scrollbar
    }

    /**
     * Liefert die zusätzlich zu den Controls benötigte Höhe zurück.
     *
     * @return
     */
    protected int getRequiredAdditionalHeight() {
        return 150; // 150px wegen Fensterrand und Titel
    }

    /**
     * Liefert die Breite des Randes ausgehend von der maximalen Bildschirm- bzw. Browserbreite zurück.
     *
     * @return
     */
    protected int getScreenBorderWidth() {
        return 200; // 200px damit auch der Calendar Platz hat
    }

    /**
     * Liefert die Höhe des Randes ausgehend von der maximalen Bildschirm- bzw. Browserhöhe zurück.
     *
     * @return
     */
    protected int getScreenBorderHeight() {
        return 20; // 20px für einen schmalen Rand
    }

    protected void prepareControlsForRelatedInfo(EtkEditFields eFields) {

        mainWindow.panelEditFields.setLayout(new LayoutBorder());
        GuiScrollPane scrollPaneGrid = new GuiScrollPane();
        scrollPaneGrid.setName("scrollPaneGrid");
        scrollPaneGrid.__internal_setGenerationDpi(96);
        scrollPaneGrid.registerTranslationHandler(getUITranslationHandler());
        scrollPaneGrid.setScaleForResolution(true);
        scrollPaneGrid.setMinimumWidth(10);
        scrollPaneGrid.setMinimumHeight(10);
        scrollPaneGrid.setHorizontalScrollEnabled(false);
        scrollPaneGrid.addEventListener(new EventListener(Event.ON_RESIZE_EVENT) {
            public void fire(Event event) {
                resizeColumns(event);
            }
        });

        table = new GuiTable();
        table.setName("table");
        table.__internal_setGenerationDpi(96);
        table.registerTranslationHandler(getUITranslationHandler());
        table.setScaleForResolution(true);
        table.setMinimumWidth(100);
        table.setMinimumHeight(100);
        table.setName("");
//        tablePictureOrder.addEventListener(new de.docware.framework.modules.gui.event.EventListener("mouseDoubleClickedEvent") {
//            public void fire(de.docware.framework.modules.gui.event.Event event) {
//                onMouseDblClickEvent(event);
//            }
//        });
        table.setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);
//        tablePictureOrder.addEventListener(new de.docware.framework.modules.gui.event.EventListener("tableSelectionEvent") {
//            public void fire(de.docware.framework.modules.gui.event.Event event) {
//                onTableSelectionChangedEvent(event);
//            }
//        });
        ConstraintsBorder tableConstraints = new ConstraintsBorder();
        table.setConstraints(tableConstraints);
        table.setContextMenu(createContextMenu());

        scrollPaneGrid.addChild(table);
        ConstraintsBorder scrollPaneGridConstraints = new ConstraintsBorder();
        scrollPaneGrid.setConstraints(scrollPaneGridConstraints);
        addEditFieldChild(scrollPaneGrid);

        GuiTableHeader tableHeader = new GuiTableHeader();
        tableHeader.addChild(new GuiLabel());
        tableHeader.addChild(new GuiLabel());
        table.setHeader(tableHeader);
        table.setShowHeader(false);

        int totalHeight = 0;
        for (EtkEditField field : eFields.getVisibleEditFields()) {
            DBDataObjectAttribute attrib = getAttributeFromKey(field);
            if (attrib != null) {
                if (!field.isInRelatedInfoAnzeigen()) {
                    totalHeight += 16;
                }
            }
        }
        totalHeight = Math.min(531, totalHeight + mainWindow.getHeight());    //170
        setHeight(totalHeight);
        setCalculatedPanelSize(0, totalHeight);
    }

    private GuiContextMenu createContextMenu() {
        GuiContextMenu contextmenuTable = new GuiContextMenu();
        contextmenuTable.setName("contextmenuTable");
        contextmenuTable.setMenuName("contextmenuTablePictureOrder");
        //contextmenuTable.setParentControl(this);
        EditToolbarButtonMenuHelper toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), null);
        GuiMenuItem menuItemCopy = toolbarHelper.createCopyMenuForTable(table, getUITranslationHandler());
        contextmenuTable.addChild(menuItemCopy);
        return contextmenuTable;
    }

    private void setTableContent(int col1Width, int col2Width) {
        for (EtkEditField field : editFields.getVisibleEditFields()) {
            DBDataObjectAttribute attrib = getAttributeFromKey(field);
            if (attrib != null) {
                // EditFields besitzen kein Attribute inRelatedInfoAnzeigen
                if (true /*field.isInRelatedInfoAnzeigen()*/) {
                    String key = field.getText().getText(getConfig().getCurrentViewerLanguage());
                    if (key.isEmpty()) {
                        key = field.getKey().getFieldName();
                    }

                    // aktuelle DB-Sprache verwenden, falls keine explizit andere DB-Sprache für dieses Feld gewünscht ist
                    String dbLanguage = calculateDBLanguage(field);
                    String initialValue;
                    if (field.isMultiLanguage()) {
                        setAttributeToMultiLang(attrib, field.getKey().getTableName());
                        initialValue = attrib.getMultiLanguageText(dbLanguage, EtkDataObject.getTempExtendedDataTypeProvider(getProject(), field.getKey().getTableName())); // reiner Text, keine HTML-Tags notwendig
                    } else {
                        initialValue = getProject().getVisObject().asHintHtml(field.getKey().getTableName(),
                                                                              field.getKey().getFieldName(),
                                                                              attrib,
                                                                              dbLanguage, true).getStringResult();
                    }

                    addCellContent(table, key, initialValue, col1Width, col2Width);
                }
            }
        }
    }

    private int addCellContent(GuiTable table, String htmlKey, String htmlValue, int col1Width, int col2Width) {
        GuiTableRow row = new GuiTableRow();
        GuiTextAreaRelatedInfo textAreaKey = new GuiTextAreaRelatedInfo(htmlKey);
        textAreaKey.setFontStyle(DWFontStyle.BOLD);
        textAreaKey.calcMinimumHeight(col1Width);
        row.addChild(textAreaKey, () -> textAreaKey.getTextRepresentation());
        AbstractGuiControl valueComp;
        if (DatatypeUtils.containsImageTag(htmlValue) || DatatypeUtils.containsBooleanImageTag(htmlValue)) {
            valueComp = new GuiLabel(htmlValue);
        } else {
            valueComp = new GuiTextAreaRelatedInfo(htmlValue);
            ((GuiTextAreaRelatedInfo)valueComp).calcMinimumHeight(col2Width);
        }
        row.addChild(valueComp, () -> valueComp.getTextRepresentation());
        table.addRow(row);
        Color background = table.getBackgroundColorForRow(table.getRowCount() - 1);
        valueComp.setBackgroundColor(background);
        textAreaKey.setBackgroundColor(background);
        return textAreaKey.getMinimumHeight();
    }

    private void resizeColumns(Event event) {
        int tableWidth = 0;
        if (event != null) {        // wirklicher Resize
            tableWidth = event.getIntParameter(Event.EVENT_PARAMETER_NEWWIDTH);
            if (tableWidth == 0) {
                return;
            }
            // unter JEE 17px für mögliche vertikale Scrollbar abziehen
            if (J2EEHandler.isJ2EE()) {
                tableWidth -= 17;
            }
        }

        int maxWidth = tableWidth;
        int diff1 = 0;
        int diff2 = 0;
        if (true /*isEmbedded()*/) {
            // Bei geringer Größe herausfinden, ob vertikale Scrollbar angezeigt wird -> Breite anpassen
            // Ist leider nur für Swing möglich
            if (SwingHandler.isSwing() && (table.getParent() != null)) {
                diff1 = 3;      // Bei Swing sind die Bereiche zwischen Text und Textfeldrahmen größer
                diff2 = 7;
                if (GuiScrollPane.TYPE.equals(table.getParent().getType())) {
                    GuiScrollPane pane = (GuiScrollPane)table.getParent();
                    JScrollPane scrollPane = (JScrollPane)pane.getSwingComponent();
                    if (scrollPane.getVerticalScrollBar().isShowing()) {
                        maxWidth -= scrollPane.getVerticalScrollBar().getWidth();
                    }
                }
            }
        }
        int colWidth = 80 + 20; // Abstand vor/nach Text zum Rand berücksichtigen
        int colWidthUnwrapped = colWidth;

        // Breite der ersten Spalte aufgrund der Keys bestimmen
        GuiTextAreaRelatedInfo keyDummyControl = new GuiTextAreaRelatedInfo();
        keyDummyControl.setFontStyle(DWFontStyle.BOLD);
        Font keyFont = keyDummyControl.getFont();
        for (EtkEditField field : editFields.getVisibleEditFields()) {
            DBDataObjectAttribute attrib = getAttributeFromKey(field);
            if (attrib != null) {
                // EditFields besitzen kein Attribute inRelatedInfoAnzeigen
                if (true /*field.isInRelatedInfoAnzeigen()*/) {
                    String key = field.getText().getText(getConfig().getCurrentViewerLanguage());
                    if (key.isEmpty()) {
                        key = field.getKey().getFieldName();
                    }
                    colWidth = Math.max(colWidth, getMaxWidthUnwrappableText(key, keyFont));
                    colWidthUnwrapped = Math.max(colWidthUnwrapped, HTMLUtils.getTextDimension(keyFont, key).getWidth() + 14);
                }
            }
        }

        int newWidth = Math.min(colWidth, maxWidth / 2);
        if (colWidthUnwrapped < maxWidth / 3) {
            newWidth = colWidthUnwrapped;
        }
        table.removeRows();
        setTableContent(newWidth - 8 - diff1, maxWidth - newWidth - 9 - diff2);
        table.setColumnWidth(0, newWidth);
        table.setColumnWidth(1, maxWidth - newWidth - 4);

    }

    /**
     * Bestimmt die maximale Breite, die benötigt wird, um den Text darzustellen, falls jedes Wort in einer neuen Zeile steht.
     */
    protected int getMaxWidthUnwrappableText(String text, Font font) {
        String[] parts = text.split(" ");
        int width = -1;
        for (String part : parts) {
            width = Math.max(width, HTMLUtils.getTextDimension(font, part).getWidth());
        }
        return width;
    }


    protected DBDataObjectAttribute getAttributeFromKey(EtkEditField field) {
        return attributes.getField(field.getKey().getFieldName(), false);
    }

    protected int getHeight() {
        return mainWindow.getHeight();
    }

    protected void setHeight(int totalHeight) {
        mainWindow.setHeight(totalHeight);
        mainWindow.setMaximumHeight(totalHeight);
    }

    protected int getWidth() {
        return mainWindow.getWidth();
    }

    protected void setWidth(int totalWidth) {
        mainWindow.setWidth(totalWidth);
        mainWindow.setMaximumWidth(totalWidth);
    }

    /**
     * Setzt die minimale und auch maximale Breite der Edit-Controls inkl. der {@link GuiScrollPane} drumrum, wobei aufgrund
     * von möglichen vertikalen Scrollbalken die effektive Breite 20 Pixel geringer ausfällt.
     *
     * @param editControlsWidth
     */
    public void setEditControlsWidth(int editControlsWidth) {
        mainWindow.panelGrid.setMinimumWidth(editControlsWidth);
        mainWindow.panelGrid.setMaximumWidth(editControlsWidth);
        mainWindow.panelEditFields.setMaximumWidth(editControlsWidth - 20); // 20 Pixel für vertikale Scrollbalken
    }

    public void setControlsHeightByPreferredHeight() {
        int preferredHeight = getPanelEditFields().getPreferredHeight();
        mainWindow.panelGrid.setMinimumHeight(preferredHeight);
        mainWindow.panelGrid.setMaximumHeight(preferredHeight);
    }

    protected void removeChildFromPanelMain(AbstractGuiControl control) {
        mainWindow.panelMain.removeChild(control);
    }

    protected void addChildToPanelMain(AbstractGuiControl control) {
        mainWindow.panelMain.addChild(control);
    }

    /**
     * Fügt das übergebene {@link AbstractGuiControl} dem unteren Teil des SplitPanes hinzu. Gesamthöhe und
     * Divider-Position können ebenfalls übergeben werden
     *
     * @param guiControl
     * @param dividerPos
     * @param height
     */
    protected void addChildAsSplitPaneElement(AbstractGuiControl guiControl, int dividerPos, int height) {
        AbstractGuiControl panelGrid = getGui();
        removeChildFromPanelMain(panelGrid);
        AbstractConstraints panelGridConstraints = panelGrid.getConstraints();
        panelGrid.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        GuiSplitPane splitPaneMaster = new GuiSplitPane();
        splitPaneMaster.setName("splitpaneModelMapping");
        splitPaneMaster.setHorizontal(false);
        splitPaneMaster.setDividerSize(6);
        splitPaneMaster.setFirstChild(panelGrid);
        splitPaneMaster.setSecondChild(guiControl);
        splitPaneMaster.setConstraints(panelGridConstraints);
        splitPaneMaster.setDividerPosition(dividerPos);
        addChildToPanelMain(splitPaneMaster);
        setHeight(height);
    }

    /**
     * Fügt das übergebene {@link AbstractGuiControl} dem unteren Teil des SplitPanes hinzu. Gesamthöhe muss ebenfalls
     * übergeben werden. Divider-Position berechnet sich aus der übergebenen Höhe.
     *
     * @param guiControl
     * @param height
     */
    protected void addChildAsSplitPaneElement(AbstractGuiControl guiControl, int height) {
        int dividerPos = getDividerPosition(height);
        addChildAsSplitPaneElement(guiControl, dividerPos, height);
    }

    protected int getDividerPosition(int height) {
        height = getCalculatedHeight(height);
        // damit der Divider nicht ganz unten hängt
        return Math.min((height / 3) * 2, (int)getCalculatedPanelSize().getHeight() - 46);
    }

    protected int getCalculatedHeight(int height) {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        return Math.min(getHeight() + height, screenSize.height - 20); // 20px wegen Fensterrand und Titel
    }


    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelGrid;
    }

    public GuiWindow getWindow() {
        return mainWindow;
    }

    /**
     * Liefert die {@link GuiScrollPane} mit den Edit-Controls darin zurück.
     *
     * @return
     */
    public GuiScrollPane getScrollPane() {
        return mainWindow.scrollPane;
    }

    /**
     * Liefert das {@link GuiPanel} mit den Edit-Controls zurück.
     *
     * @return
     */
    public GuiPanel getPanelEditFields() {
        return mainWindow.panelEditFields;
    }

    public ModalResult showModal() {
        // Damit die Fenster immer einen Test-Namen haben. Normalerweise sollte dieser sinnvoll von aussen gesetzt werden,
        // als Fallback wird der in der Oberfläche angezeigte Titel vewendet
        if (mainWindow.getName().isEmpty()) {
            mainWindow.setName(mainWindow.title.getTitle());
        }
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public GuiButtonOnPanel addButton(GuiButtonOnPanel.ButtonType buttonType, final ModalResult modalResult, String text,
                                      FrameworkImage icon) {
        mainWindow.buttonPanel.setButtonVisible(buttonType, true);
        GuiButtonOnPanel buttonOnPanel;
        if (buttonType == GuiButtonOnPanel.ButtonType.CUSTOM) {
            buttonOnPanel = mainWindow.buttonPanel.addCustomButton(text, modalResult);
        } else {
            buttonOnPanel = mainWindow.buttonPanel.getButtonOnPanel(buttonType);
            buttonOnPanel.setText(text);
        }
        buttonOnPanel.setIcon(icon);
        buttonOnPanel.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                mainWindow.setModalResult(modalResult);
                close();
            }
        });
        return buttonOnPanel;
    }

    public GuiButtonOnPanel getButton(GuiButtonOnPanel.ButtonType buttonType, String text) {
        if (buttonType == GuiButtonOnPanel.ButtonType.CUSTOM) {
            return mainWindow.buttonPanel.getCustomButtonOnPanel(text);
        } else {
            return mainWindow.buttonPanel.getButtonOnPanel(buttonType);
        }
    }

    public void setMainTitle(String title) {
        mainWindow.setTitle(title);
        setTitle(title);
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public DBDataObjectAttributes getAttributes() {
        return attributes;
    }

    public boolean idIsChanged(IdWithType testId) {
        return !id.equals(testId);
    }

    public EtkEditFields getExternalEditFields() {
        return externalEditFields;
    }

    public void setExternalEditFields(EtkEditFields externalEditFields) {
        this.externalEditFields = externalEditFields;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    protected boolean checkReadOnlyValue(boolean readOnly) {
        if (attributes == null) {
            readOnly = true;
        }
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        readOnly = checkReadOnlyValue(readOnly);
        if (firstReadOnly || (this.readOnly != readOnly)) {
            firstReadOnly = false;
            this.readOnly = readOnly;
            for (EditControl ctrl : editControls) {
                EditControlFactory editControl = ctrl.getEditControl();
                boolean controlReadOnly = getReadOnlyValueForControl(editControl, readOnly);
                if (!controlReadOnly) {
                    editControl.setReadOnly(!editFields.getVisibleEditFields().get(ctrl.getIndexInList()).isEditierbar());
                } else {
                    editControl.setReadOnly(true);
                }
            }
        }
        if (readOnly) {
            mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, false);
            mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.CANCEL);
        } else {
            mainWindow.buttonPanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, true);
            mainWindow.buttonPanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE /*ModalResult.OK*/);
        }
        doEnableButtons(null);
    }

    protected boolean getReadOnlyValueForControl(EditControlFactory editControl, boolean readOnly) {
        if (EtkPluginApi.isReadOnlyForEditControl(editControl.getControl(), editControl.getValues(), editControl.getOptions(),
                                                  readOnly, getAttributes())) {
            return true;
        }
        return readOnly;
    }

    protected void doEnableButtons(Event event) {
        enableOKButton(readOnly || checkForModified());
    }

    protected void enableOKButton(boolean enabled) {
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, enabled);
    }

    protected void setOKButtonTooltip(String tooltip) {
        // Tooltips auf disabled Buttons funktionieren nicht immer (Chrome) -> deswegen setzen wir den Tooltip auf das Panel
        mainWindow.buttonPanel.setTooltip(tooltip);
    }

    protected void setOKButtonText(String text) {
        GuiButtonOnPanel okButton = mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
        if (okButton != null) {
            okButton.setText(text);
        }
    }

    protected boolean checkForModified() {
        DBDataObjectAttributes clonedAttributes = getCurrentAttributes();
        if (attributes != null) {
            return clonedAttributes.isModified();
        }
        return true;
    }

    /**
     * Überträgt die Werte in das übergebene Attribute. Wenn das Attribute this.attribute ist, dann werden die ursprünglichen Werte überschrieben und das modified-Flag
     * kann nicht mehr ermittelt werden. Deshalb immer mit einem neuen Attribut aufrufen, außer beim ecten Abspeichern des Datensatzes this.attribute verwenden
     *
     * @param index
     * @param field
     * @param attrib
     */
    protected void fillAttribByEditControlValue(int index, EtkEditField field, DBDataObjectAttribute attrib) {
        EditControl controlByFeldIndex = editControls.getControlByFeldIndex(index);
        iPartsEditUserControlsHelper.fillAttribByEditControlValue(getProject(), controlByFeldIndex, field, attrib, tableName, id);
    }

    /**
     * Liefert zu einem FeldNamen den zugehörigen aktuellen Editwert (via [@link DBDataObjectAttribute})
     *
     * @param fieldName
     * @return
     */
    protected DBDataObjectAttribute getCurrentAttributeValue(String fieldName) {
        EditControl editControl = getEditControlByFieldName(fieldName);
        EtkEditField field = editFields.getFeldByName(tableName, fieldName);
        return getCurrentAttributeValue(editControl, field);
    }

    protected DBDataObjectAttribute getCurrentAttributeValueByTableAndFieldName(String tableAndFieldName) {
        String tName = TableAndFieldName.getTableName(tableAndFieldName);
        String fName = TableAndFieldName.getFieldName(tableAndFieldName);
        EditControl editControl = getEditControlByTableAndFieldName(tName, fName);
        EtkEditField field = editFields.getFeldByName(tName, fName);
        return getCurrentAttributeValue(editControl, field);
    }

    protected DBDataObjectAttribute getCurrentAttributeValue(EditControl editControl, EtkEditField field) {
        if ((editControl != null) && (field != null)) {
            DBDataObjectAttribute attrib = attributes.getField(field.getKey().getFieldName(), false);
            // Test, ob das Feld überhaupt im Editor ist
            if (attrib != null) { // hier keine Logausgabe machen, wenn attrib null ist, weil das in collectEditValues() bereits gemacht wird
                // Einen Clone des Attributes erzeugen, damit das Original im Fill nicht verändert wird
                DBDataObjectAttribute clonedAttrib = new DBDataObjectAttribute(attrib);
                iPartsEditUserControlsHelper.fillAttribByEditControlValue(getProject(), editControl, field, clonedAttrib, field.getKey().getTableName(), id);
                return clonedAttrib;
            }
        }
        return null;
    }

    /**
     * Liefert ein Kopie der Attribute zurück, die die aktuellen Eingaben des Editfeldes enthalten
     */
    protected DBDataObjectAttributes getCurrentAttributes() {
        if (attributes == null) {
            return null;
        }
        DBDataObjectAttributes result = attributes.cloneMe(DBActionOrigin.FROM_DB);
        int index = 0;
        for (EtkEditField field : editFields.getVisibleEditFields()) {
            if (!field.isEditierbar()) { // Nicht editierbare Felder ignorieren
                index++;
                continue;
            }

            String fieldName = field.getKey().getFieldName();
            if (VirtualFieldsUtils.isVirtualField(fieldName)) { // virtuelle Felder ignorieren
                index++;
                continue;
            }

            DBDataObjectAttribute clonedAttribute = result.getField(fieldName, false);
            if (clonedAttribute != null) { // hier keine Logausgabe machen, wenn attrib null ist, weil das in collectEditValues() bereits gemacht wird
                fillAttribByEditControlValue(index, field, clonedAttribute);
            }
            index++;
        }
        return result;
    }


    /**
     * Liefert ein Attribute zurück, dass die aktuellen Eingaben des Editfeldes enthält. Mit diesen Werten kann geprüft werden, ob z.B. die Eingabe vom Benutzer gültig
     * ist.
     *
     * @param index
     * @param field
     * @return
     */
    public DBDataObjectAttribute getCurrentAttribByEditControlValue(int index, EtkEditField field) {

        DBDataObjectAttribute attrib = attributes.getField(field.getKey().getFieldName(), false);

        // Test, ob das Feld überhaupt im Editor ist
        if (attrib != null) { // hier keine Logausgabe machen, wenn attrib null ist, weil das in collectEditValues() bereits gemacht wird

            // Einen Clone des Attributes erzeugen, damit das Original im Fill nicht verändert wird
            DBDataObjectAttribute clonedAttrib = new DBDataObjectAttribute(attrib);
            fillAttribByEditControlValue(index, field, clonedAttrib);
            return clonedAttrib;
        }
        return null;
    }

    public EditControl getEditControlByTableAndFieldName(String tableName, String fieldName) {
        int index = editFields.getIndexOfVisibleTableAndFeldName(new EtkDisplayFieldKeyNormal(tableName, fieldName));
        if (index != -1) {
            return editControls.getControlByFeldIndex(index);
        }
        return null;
    }

    public EditControl getEditControlByFieldName(String fieldName) {
        return getEditControlByTableAndFieldName(tableName, fieldName);
    }

    public AbstractGuiControl getEditGuiControlByFieldName(String fieldName) {
        EditControl ctrl = getEditControlByFieldName(fieldName);
        if (ctrl != null) {
            return ctrl.getEditControl().getControl();
        }
        return null;
    }

    protected boolean checkValues() {
        if ((attributes != null) && (tableName != null)) {
            int index = 0;
            EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(tableName);
            List<String> pkFields = tableDef.getPrimaryKeyFields();
            List<String> emptyFields = new DwList<String>();
            for (EtkEditField field : editFields.getVisibleEditFields()) {
                if (isFieldPKValueOrMandatory(field, pkFields)) {
                    DBDataObjectAttribute attrib = attributes.getField(field.getKey().getFieldName());
                    if (isMandatoryAttributeValueEmpty(field, attrib)) {
                        emptyFields.add(TranslationHandler.translate(editControls.getControlByFeldIndex(index).getLabel().getText()));
                    }
                }
                index++;
            }
            if (!iPartsEditUserControlsHelper.handleEmptyFields(emptyFields)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Überprüft, ob bei einem Muss-Feld der Attributwert leer ist
     *
     * @param field
     * @param attrib
     * @return
     */
    protected boolean isMandatoryAttributeValueEmpty(EtkEditField field, DBDataObjectAttribute attrib) {
        return (attrib != null) && attrib.isEmpty(); // hier keine Logausgabe machen, wenn attrib null ist, weil das in collectEditValues() bereits gemacht wird
    }

    /**
     * überprüft, ob da Feld ein PK-Feld oder Mandatory (field.isMussFeld()) ist
     * Dient zur Feststellung der Empty-Fields und deren Anzeige
     *
     * @param field
     * @param pkFields
     * @return
     */
    protected boolean isFieldPKValueOrMandatory(EtkEditField field, List<String> pkFields) {
        return pkFields.contains(field.getKey().getFieldName()) || isMandatoryField(field);
    }

    /**
     * Beendet das Editieren und legt die editierten Werte in den ursprünglichen Datenstrukturen ab.
     *
     * @return {@link EditResult#STORED} falls das Ablegen der editierten Werte in die ursprünglichen Datenstrukturen ohne
     * Fehler durchgeführt werden konnte,
     * {@link EditResult#UNMODIFIED} falls keine Daten verändert wurden und
     * {@link EditResult#ERROR} falls es Fehler gab
     */
    public EditResult stopAndStoreEdit() {
        if (!readOnly) {
            if (checkCompletionOfFormValues()) {
                DBDataObjectAttributes oldAttributes = null;
                if (attributes != null) {
                    oldAttributes = attributes.cloneMe(DBActionOrigin.FROM_DB);
                }
                collectEditValues();
                if (!isModified()) {
                    return EditResult.UNMODIFIED;
                } else {
                    if (checkValues()) {
                        // Falls in den Controls eigene Objekte (kombinierte Texte, Werkeinsatzdaten) angelegt wurden, diese hier speichern
                        saveAdditionalData();
                        return EditResult.STORED;
                    } else {
                        if (oldAttributes != null) {
                            attributes.assign(oldAttributes, DBActionOrigin.FROM_DB);
                        }
                        return EditResult.ERROR;
                    }
                }
            } else {
                return EditResult.ERROR;
            }
        } else {
            return EditResult.UNMODIFIED;
        }
    }

    protected void onButtonOKAction(Event event) {
        EditResult editResult = stopAndStoreEdit();
        if (editResult == EditResult.UNMODIFIED) {
            onButtonCancelAction(null);
            mainWindow.setModalResult(ModalResult.CANCEL);
            close();
        } else if (editResult == EditResult.STORED) {
            mainWindow.setModalResult(ModalResult.OK);
            close();
        }
    }

    protected void onButtonCancelAction(Event event) {
        close();
    }

    /**
     * Hier besteht die Möglichkeit zu überprüfen, welche Felder ausgefüllt
     * sind bzw sein sollten
     * Dabei sollten hier NICHT die attributes benutzt werden
     *
     * @return
     */
    protected boolean checkCompletionOfFormValues() {
        return true;
    }

    protected boolean isModified() {
        return attributes.isModified();
    }

    protected void saveAdditionalData() {
        int index = 0;
        for (EtkEditField field : editFields.getVisibleEditFields()) {
            if (!field.isEditierbar()) { // Nicht editierbare Felder ignorieren
                if (!EtkPluginApi.hasEditorForVirtualField(field.getKey().getTableName(), field.getKey().getFieldName())) {
                    index++;
                    continue;
                }
            }

            DBDataObjectAttribute attrib = getAttributeFromKey(field);
            if (attrib != null) {
                EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
                if (ctrl != null) {
                    ctrl.saveAdditionalData(getAttributes());
                }
            }
            index++;
        }
    }

    /**
     * Schreibt die Werte der Editcontrols in die Attribute. Die Originalwerte werden dadurch überschrieben
     */
    protected void collectEditValues() {
        int index = 0;
        for (EtkEditField field : editFields.getVisibleEditFields()) {
            if (!field.isEditierbar()) { // Nicht editierbare Felder ignorieren
                index++;
                continue;
            }

            DBDataObjectAttribute attrib = getAttributeFromKey(field);
            if (attrib == null) {
                if (!VirtualFieldsUtils.isVirtualField(field.getKey().getFieldName())) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR,
                                                       new RuntimeException("DBDataAttribute for field \"" + field.getKey().getFieldName()
                                                                            + "\" not found!"));
                }
            } else {
                EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
                String value = null;
                if (ctrl != null) {
                    value = ctrl.getTextFromExtraControls();
                }
                if (value != null) {
                    attrib.setValueAsString(value, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                } else {
                    fillAttribByEditControlValue(index, field, attrib);
                }
            }
            index++;
        }
    }

    protected ConstraintsGridBag createLabelConstraints(GuiLabel label, int gridy) {
        String anchor = ConstraintsGridBag.ANCHOR_EAST;
        if (label.getVerticalAlignment() == AbstractVerticalAlignmentControl.VerticalAlignment.TOP) {
            anchor = ConstraintsGridBag.ANCHOR_NORTHEAST;
        } else if (label.getVerticalAlignment() == AbstractVerticalAlignmentControl.VerticalAlignment.BOTTOM) {
            anchor = ConstraintsGridBag.ANCHOR_SOUTHEAST;
        }
        return new ConstraintsGridBag(0, gridy, 1, 1, 0.0, 0.0, anchor, ConstraintsGridBag.FILL_NONE,
                                      4, 0, 4, 4);
    }

    protected ConstraintsGridBag createValueConstraints(int gridy) {
        return new ConstraintsGridBag(1, gridy, 1, 1, 100.0, 0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                                      4, 4, 4, 0);
    }

    protected ConstraintsGridBag createPanelConstraints(int gridy) {
        return new ConstraintsGridBag(0, gridy, 2, 1, 100.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                      4, 0, 4, 0);
    }

    protected void setAttributeToMultiLang(DBDataObjectAttribute attrib, String tableName) {
        if (attrib.getType() != DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
            attrib.setValueAsMultiLanguage(new EtkMultiSprache(), DBActionOrigin.FROM_DB);
        }
        EtkMultiSprache multi = getProject().getDbLayer().loadMultiLanguageAttribute(attrib, tableName);
        attrib.setTextIdForMultiLanguage(attrib.getMultiLanguageTextNr(), multi.getTextId(), DBActionOrigin.FROM_DB);

        // Dieser Umweg muss gegangen werden, da sonst nicht alle bereits geänderten aber noch nicht in der DB abgespeicherten
        // Texte übernommen werden
        for (Map.Entry<String, String> languageText : multi.getLanguagesAndTexts().entrySet()) {
            String lang = languageText.getKey();
            // nur Text aus DB setzen, wenn nicht schon ein Text vorhanden ist
            attrib.setPreloadValueForMultiLanguage(lang, languageText.getValue(), true);
        }
        attrib.setMultiLanguageCompleteLoaded(true);
    }

    protected EtkDataArray setAttributeToArray(DBDataObjectAttribute attrib, String tableName) {
        if (attrib.getType() != DBDataObjectAttribute.TYPE.ARRAY) {
            attrib.setValueAsArray(null, DBActionOrigin.FROM_DB);
        }
        EtkDataArray dataArray = null;
        if (!attrib.getArrayId().isEmpty()) {
            dataArray = (EtkDataArray)attrib.getValue();
            if (dataArray == null) {
                dataArray = getProject().getDbLayer().loadArrayAttribute(attrib, tableName);
            }

            // Leere Arrays als null behandeln
            if (dataArray.isEmpty()) {
                attrib.setValueAsArray(null, DBActionOrigin.FROM_DB);
                dataArray = null;
            }
        }

        return dataArray;
    }

    /**
     * Ist das Feld ein Mussfeld?
     * Defaultverhalten ist die Feldkonfiguration. Es kann aber sein dass ein Verhalten erzwungen werden muss und die
     * Feldkonfiguration außer Kraft gesetzt werden muss.
     *
     * @param field
     * @return
     */
    protected boolean isMandatoryField(EtkEditField field) {
        return field.isMussFeld();
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelEditFields;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfield_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfield_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_2;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButtonTextField buttontextfield_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(500);
            this.setHeight(170);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            title.setConstraints(titleConstraints);
            panelMain.addChild(title);
            panelGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGrid.setName("panelGrid");
            panelGrid.__internal_setGenerationDpi(96);
            panelGrid.registerTranslationHandler(translationHandler);
            panelGrid.setScaleForResolution(true);
            panelGrid.setMinimumWidth(10);
            panelGrid.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelGrid.setLayout(panelGridLayout);
            scrollPane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollPane.setName("scrollPane");
            scrollPane.__internal_setGenerationDpi(96);
            scrollPane.registerTranslationHandler(translationHandler);
            scrollPane.setScaleForResolution(true);
            scrollPane.setMinimumWidth(10);
            scrollPane.setMinimumHeight(10);
            panelEditFields = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelEditFields.setName("panelEditFields");
            panelEditFields.__internal_setGenerationDpi(96);
            panelEditFields.registerTranslationHandler(translationHandler);
            panelEditFields.setScaleForResolution(true);
            panelEditFields.setMinimumWidth(10);
            panelEditFields.setMinimumHeight(10);
            panelEditFields.setPaddingTop(8);
            panelEditFields.setPaddingLeft(8);
            panelEditFields.setPaddingRight(8);
            panelEditFields.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelEditFieldsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelEditFields.setLayout(panelEditFieldsLayout);
            label_0 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_0.setName("label_0");
            label_0.__internal_setGenerationDpi(96);
            label_0.registerTranslationHandler(translationHandler);
            label_0.setScaleForResolution(true);
            label_0.setMinimumWidth(10);
            label_0.setMinimumHeight(10);
            label_0.setText("Lable1");
            label_0.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 4, 0, 4, 4);
            label_0.setConstraints(label_0Constraints);
            panelEditFields.addChild(label_0);
            textfield_0 = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfield_0.setName("textfield_0");
            textfield_0.__internal_setGenerationDpi(96);
            textfield_0.registerTranslationHandler(translationHandler);
            textfield_0.setScaleForResolution(true);
            textfield_0.setMinimumWidth(200);
            textfield_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfield_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 0);
            textfield_0.setConstraints(textfield_0Constraints);
            panelEditFields.addChild(textfield_0);
            label_1 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_1.setName("label_1");
            label_1.__internal_setGenerationDpi(96);
            label_1.registerTranslationHandler(translationHandler);
            label_1.setScaleForResolution(true);
            label_1.setMinimumWidth(10);
            label_1.setMinimumHeight(10);
            label_1.setText("ein längerer Text");
            label_1.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_1Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 0, 4, 4);
            label_1.setConstraints(label_1Constraints);
            panelEditFields.addChild(label_1);
            textfield_1 = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfield_1.setName("textfield_1");
            textfield_1.__internal_setGenerationDpi(96);
            textfield_1.registerTranslationHandler(translationHandler);
            textfield_1.setScaleForResolution(true);
            textfield_1.setMinimumWidth(200);
            textfield_1.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfield_1Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 0);
            textfield_1.setConstraints(textfield_1Constraints);
            panelEditFields.addChild(textfield_1);
            label_2 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_2.setName("label_2");
            label_2.__internal_setGenerationDpi(96);
            label_2.registerTranslationHandler(translationHandler);
            label_2.setScaleForResolution(true);
            label_2.setMinimumWidth(10);
            label_2.setMinimumHeight(10);
            label_2.setText("weiteres Label");
            label_2.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_2Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 0, 4, 4);
            label_2.setConstraints(label_2Constraints);
            panelEditFields.addChild(label_2);
            buttontextfield_0 = new de.docware.framework.modules.gui.controls.GuiButtonTextField();
            buttontextfield_0.setName("buttontextfield_0");
            buttontextfield_0.__internal_setGenerationDpi(96);
            buttontextfield_0.registerTranslationHandler(translationHandler);
            buttontextfield_0.setScaleForResolution(true);
            buttontextfield_0.setMinimumWidth(10);
            buttontextfield_0.setMinimumHeight(10);
            buttontextfield_0.setButtonVisible(true);
            buttontextfield_0.setButtonText("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttontextfield_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 0);
            buttontextfield_0.setConstraints(buttontextfield_0Constraints);
            panelEditFields.addChild(buttontextfield_0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelEditFieldsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelEditFields.setConstraints(panelEditFieldsConstraints);
            scrollPane.addChild(panelEditFields);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollPane.setConstraints(scrollPaneConstraints);
            panelGrid.addChild(scrollPane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            panelGrid.setConstraints(panelGridConstraints);
            panelMain.addChild(panelGrid);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonOKAction(event);
                }
            });
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCancelActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonCancelAction(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            buttonPanel.setConstraints(buttonPanelConstraints);
            panelMain.addChild(buttonPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}