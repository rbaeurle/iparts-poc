/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EditControls;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.EtkPluginApi;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsMatrixEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsMatrixEditFields;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBExtendedDataTypeProvider;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiMultiLangEdit;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.misc.DWBoundsInt;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.misc.DWPoint;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.GuiUtils;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;

public class EditMatrixUserControls extends AbstractJavaViewerForm {

    public enum MATRIX_LAYOUT {
        DEFAULT,
        EMTY_COLS,
        EMPTY_ROWS,
        EMPTY_COLS_AND_ROWS;
    }

    private final static int DEFAULT_COL_WIDTH = 20; // in Characters
    private final static int DEFAULT_LABEL_WIDTH = 9;
    private final static int DEFAULT_ROW_HEIGHT = 21;  // in Pixeln

    private final int HORZ_GAP = DWLayoutManager.get().getDefaultPadding();
    private final int VERT_GAP = DWLayoutManager.get().getDefaultPadding();
    protected iPartsMatrixEditFields matrixEditFields;
    protected EditControls editControls;
    protected String tableName;
    protected DBDataObjectAttributes attributes;
    protected IdWithType id;
    protected boolean readOnly = false;
    protected boolean firstReadOnly = true;
    protected iPartsMatrixEditFields externalMatrixEditFields;
    protected UltraEditHelper ultraEditHelper;
    protected Dimension calculatedPanelSize;

    private String noMasterDataExistsText = "!!Keine Stammdaten vorhanden.";

    /**
     * Erzeugt eine Instanz von EditMatrixUserControls.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditMatrixUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                  IdWithType id, DBDataObjectAttributes attributes, iPartsMatrixEditFields externalMatrixEditFields,
                                  MATRIX_LAYOUT matrixLayout, String noMasterDataExistsText) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.tableName = tableName;
        this.editControls = new EditControls();
        this.matrixEditFields = new iPartsMatrixEditFields();
        this.externalMatrixEditFields = externalMatrixEditFields;
        this.attributes = attributes;
        this.id = id;
        if (StrUtils.isValid(noMasterDataExistsText)) {
            this.noMasterDataExistsText = noMasterDataExistsText;
        }
        this.ultraEditHelper = new UltraEditHelper();
        switch (matrixLayout) {
            case EMTY_COLS:
                ultraEditHelper.setWithEmptyCols(true);
                break;
            case EMPTY_ROWS:
                ultraEditHelper.setWithEmptyRows(true);
                break;
            case EMPTY_COLS_AND_ROWS:
                ultraEditHelper.setWithEmptyCols(true);
                ultraEditHelper.setWithEmptyRows(true);
                break;
            case DEFAULT:
                ultraEditHelper.setWithEmptyCols(iPartsUserSettingsHelper.isMatrixEditEmptyCols(dataConnector.getProject()));
                ultraEditHelper.setWithEmptyRows(iPartsUserSettingsHelper.isMatrixEditEmptyRows(dataConnector.getProject()));
                break;
        }
        setAttributes();
        postCreateGui();
    }

    public EditMatrixUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                  IdWithType id, iPartsMatrixEditFields externalMatrixEditFields, MATRIX_LAYOUT matrixLayout) {
        this(dataConnector, parentForm, tableName, id, null, externalMatrixEditFields, matrixLayout, "");
    }

    public EditMatrixUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                  IdWithType id, iPartsMatrixEditFields externalMatrixEditFields) {
        this(dataConnector, parentForm, tableName, id, null, externalMatrixEditFields, MATRIX_LAYOUT.DEFAULT, "");
    }

    public EditMatrixUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                  IdWithType id) {
        this(dataConnector, parentForm, tableName, id, null, null, MATRIX_LAYOUT.DEFAULT, "");
    }

    /**
     * nur zum Testen
     *
     * @param dataConnector
     * @param parentForm
     * @param externalMatrixEditFields
     */
    public static void showTestMatrixUserControls(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                  iPartsMatrixEditFields externalMatrixEditFields) {
        String tableName = iPartsConst.TABLE_DA_SERIES;
        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        if (externalMatrixEditFields == null) {
            externalMatrixEditFields = new iPartsMatrixEditFields();
            EtkEditFields editFields = new EtkEditFields();
            EtkEditFieldHelper.getEditFields(dataConnector.getProject(), tableName, editFields, false);
            // einfache Aufteilung
/*
            addTo(editFields, tableName, iPartsConst.FIELD_DS_SERIES_NO, 0, 0, 40, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_SERIES_NO, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_TYPE, 1, 0, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_TYPE, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_NAME, 2, 0, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_NAME, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_SDATA, 0, 1, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_SDATA, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_SDATB, 1, 1, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_SDATB, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_PRODUCT_GRP, 2, 1, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_PRODUCT_GRP, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_COMPONENT_FLAG, 1, 2, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_COMPONENT_FLAG, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_SPARE_PART, 0, 3, 30, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_SPARE_PART, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_IMPORT_RELEVANT, 1, 3, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_IMPORT_RELEVANT, attributes);
*/
            // Aufteilung über mehrere Spalten
            addTo(editFields, tableName, iPartsConst.FIELD_DS_SERIES_NO, 0, 0, 40, 2, externalMatrixEditFields, iPartsConst.FIELD_DS_SERIES_NO, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_TYPE, 2, 0, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_TYPE, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_NAME, 0, 1, 50, 2, externalMatrixEditFields, iPartsConst.FIELD_DS_NAME, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_SDATA, 2, 1, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_SDATA, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_SDATB, 0, 2, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_SDATB, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_PRODUCT_GRP, 1, 2, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_PRODUCT_GRP, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_COMPONENT_FLAG, 2, 2, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_COMPONENT_FLAG, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_SPARE_PART, 0, 3, 30, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_SPARE_PART, attributes);
            addTo(editFields, tableName, iPartsConst.FIELD_DS_IMPORT_RELEVANT, 2, 3, 50, 1, externalMatrixEditFields, iPartsConst.FIELD_DS_IMPORT_RELEVANT, attributes);
        }

        iPartsSeriesId seriesId = new iPartsSeriesId("C253");
        EditMatrixUserControls dlg = new EditMatrixUserControls(dataConnector, parentForm, tableName, seriesId, null, externalMatrixEditFields, MATRIX_LAYOUT.DEFAULT, "");

        dlg.showModal();
    }

    public static void addTo(EtkEditFields editFields, String tableName, String fieldName, int colNo, int rowNo, int width, int gridWidth, iPartsMatrixEditFields externalMatrixEditFields,
                             String value, DBDataObjectAttributes attributes) {
        EtkEditField field = editFields.getFeldByName(tableName, fieldName, false);
        if (field != null) {
            iPartsMatrixEditField matrixEditField = new iPartsMatrixEditField(field);
            matrixEditField.setColNo(colNo);
            matrixEditField.setRowNo(rowNo);
            matrixEditField.setWidth(width);
            matrixEditField.setLabelWidth(width * 55 / 100);
            matrixEditField.setGridwidth(gridWidth);
            externalMatrixEditFields.addField(matrixEditField);
            if (attributes != null) {
                attributes.addField(fieldName, value, DBActionOrigin.FROM_DB);
            }
        }
    }

    public static void addTo(EtkEditFields editFields, String tableName, String fieldName, int colNo, int rowNo, int width, int gridWidth, iPartsMatrixEditFields externalMatrixEditFields) {
        addTo(editFields, tableName, fieldName, colNo, rowNo, width, gridWidth, externalMatrixEditFields, null, null);
    }

    /**
     * Ist das Feld ein Mussfeld?
     * Defaultverhalten ist die Feldkonfiguration. Es kann aber sein dass ein Verhalten erzwungen werden muss und die
     * Feldkonfiguration außer Kraft gesetzt werden muss.
     *
     * @param field
     * @return
     */
    protected static boolean isMandatoryField(EtkEditField field) {
        return field.isMussFeld();
    }

    public Dimension getCalculatedPanelSize() {
        return calculatedPanelSize;
    }

    protected void setCalculatedPanelSize(int totalWidth, int totalHeight) {
        if ((totalWidth > 0) && (totalHeight > 0)) {
            // Window-Elemente von der Höhe abziehen
            totalHeight -= (mainWindow.title.getPreferredHeight() + mainWindow.buttonpanel.getPreferredHeight() - 8);
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
        String[] whereValues = null;
        if (id != null) {
            String idFirstPrimaryKey = id.getValue(1);
            if (idFirstPrimaryKey != null) {
                List<iPartsVirtualNode> virtualList = null;
                if (idFirstPrimaryKey.startsWith(iPartsVirtualNode.VIRTUAL_INDICATOR)) {
                    virtualList = iPartsVirtualNode.parseVirtualIds(idFirstPrimaryKey);
                }
                if ((virtualList != null) && !virtualList.isEmpty()) {
                    IdWithType virtualId = virtualList.get(0).getId();
                    whereValues = virtualId.toStringArrayWithoutType();
                } else {
                    whereValues = id.toStringArrayWithoutType();
                }
            }
        }
        return whereValues;
    }

    protected void setAttributes() {
        if (tableName == null) {
            return;
        }
        String[] whereValues = getWhereValues();
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(tableName);
        String[] whereFields = ArrayUtil.toStringArray(tableDef.getPrimaryKeyFields());
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

        if (attributes != null) {
            if (externalMatrixEditFields != null) {
                matrixEditFields.assign(externalMatrixEditFields);
            } else {
                // MatrixEditFields laden
                EtkEditFieldHelper.loadMatrixEditFields(getProject(), tableName, matrixEditFields, false);
            }

            // PSK-Teilestammfelder nur als PSK-Benutzer anzeigen
            EtkEditFieldHelper.removePSKMatFieldsIfNecessary(matrixEditFields, iPartsRight.checkPSKInSession());

            ultraEditHelper.calculateHeader(matrixEditFields);
            prepareControls(matrixEditFields);
            doEnableButtons(null);
        } else {
            GuiLabel noMaterialLabel = new GuiLabel(noMasterDataExistsText);
            noMaterialLabel.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 100, 100, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_VERTICAL, 0, 0, 0, 0));
            getPanelElements().setLayout(new LayoutGridBag());
            getPanelElements().addChild(noMaterialLabel);
            int height = 4 * (noMaterialLabel.getPreferredHeight() + 8);
            height += (mainWindow.title.getPreferredHeight() + mainWindow.buttonpanel.getPreferredHeight() - 8);
            setCalculatedPanelSize(getPanelElements().getPreferredWidth(), height);
            setReadOnly(true);
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void addButton(GuiButtonOnPanel.ButtonType buttonType, final ModalResult modalResult, String text) {
        mainWindow.buttonpanel.setButtonVisible(buttonType, true);
        mainWindow.buttonpanel.getButtonOnPanel(buttonType).setText(text);
        mainWindow.buttonpanel.getButtonOnPanel(buttonType).addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                mainWindow.setModalResult(modalResult);
                close();
            }
        });
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

    public iPartsMatrixEditFields getExternalEditFields() {
        return externalMatrixEditFields;
    }

    public void setExternalEditFields(iPartsMatrixEditFields externalMatrixEditFields) {
        this.externalMatrixEditFields = externalMatrixEditFields;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        readOnly = checkReadOnlyValue(readOnly);
        if (firstReadOnly || (this.readOnly != readOnly)) {
            firstReadOnly = false;
            this.readOnly = readOnly;
            for (EditControl ctrl : editControls) {
                boolean controlReadOnly = readOnly;
                EditControlFactory editControl = ctrl.getEditControl();
                if (EtkPluginApi.isReadOnlyForEditControl(editControl.getControl(), editControl.getValues(), editControl.getOptions(),
                                                          controlReadOnly, getAttributes())) {
                    controlReadOnly = true;
                }

                if (!controlReadOnly) {
                    editControl.setReadOnly(!matrixEditFields.getFeld(ctrl.getIndexInList()).isEditierbar());
                } else {
                    editControl.setReadOnly(true);
                }
            }
        }
        if (readOnly) {
            mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, false);
            mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.CANCEL);
        } else {
            mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, true);
            mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE /*ModalResult.OK*/);
        }
        doEnableButtons(null);
    }

    public boolean isWithEmptyCols() {
        return ultraEditHelper.isWithEmptyCols();
    }

    public boolean isWithEmptyRows() {
        return ultraEditHelper.isWithEmptyRows();
    }

    protected boolean checkReadOnlyValue(boolean readOnly) {
        if (attributes == null) {
            readOnly = true;
        }
        return readOnly;
    }

    protected void doEnableButtons(Event event) {
        enableOKButton(readOnly || checkForModified());
    }

    protected void enableOKButton(boolean enabled) {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, enabled);
    }

    protected boolean checkForModified() {
        DBDataObjectAttributes clonedAttributes = getCurrentAttributes();
        if (attributes != null) {
            return clonedAttributes.isModified();
        }
        return true;
    }

    /**
     * Liefert ein Kopie der Attribute zurück, die die aktuellen Eingaben des Editfeldes enthalten
     */
    protected DBDataObjectAttributes getCurrentAttributes() {
        if (attributes == null) {
            return null;
        }
        DBDataObjectAttributes result = attributes.cloneMe(DBActionOrigin.FROM_DB);
        for (iPartsMatrixEditField field : matrixEditFields.getVisibleEditFields()) {
            if (!field.isEditierbar()) { // Nicht editierbare Felder ignorieren
                continue;
            }

            String fieldName = field.getKey().getFieldName();
            if (VirtualFieldsUtils.isVirtualField(fieldName)) { // virtuelle Felder ignorieren
                continue;
            }

            DBDataObjectAttribute clonedAttribute = result.getField(fieldName, false);
            if (clonedAttribute != null) { // hier keine Logausgabe machen, wenn attrib null ist, weil das in collectEditValues() bereits gemacht wird
                fillAttribByEditControlValue(field, clonedAttribute);
            }
        }
        return result;
    }

    /**
     * Überträgt die Werte in das übergebene Attribute. Wenn das Attribute this.attribute ist, dann werden die ursprünglichen Werte überschrieben und das modified-Flag
     * kann nicht mehr ermittelt werden. Deshalb immer mit einem neuen Attribut aufrufen, außer beim ecten Abspeichern des Datensatzes this.attribute verwenden
     *
     * @param field
     * @param attrib
     */
    protected void fillAttribByEditControlValue(iPartsMatrixEditField field, DBDataObjectAttribute attrib) {
        EditControl controlByFeldIndex = ultraEditHelper.getEditControlByCoordinates(field);
        iPartsEditUserControlsHelper.fillAttribByEditControlValue(getProject(), controlByFeldIndex, field, attrib, tableName, id);
    }

    /**
     * Liefert ein Attribute zurück, dass die aktuellen Eingaben des Editfeldes enthält. Mit diesen Werten kann geprüft werden, ob z.B. die Eingabe vom Benutzer gültig
     * ist.
     *
     * @param field
     * @return
     */
    public DBDataObjectAttribute getCurrentAttribByEditControlValue(iPartsMatrixEditField field) {

        DBDataObjectAttribute attrib = attributes.getField(field.getKey().getFieldName(), false);

        // Test, ob das Feld überhaupt im Editor ist
        if (attrib != null) { // hier keine Logausgabe machen, wenn attrib null ist, weil das in collectEditValues() bereits gemacht wird

            // Einen Clone des Attributes erzeugen, damit das Original im Fill nicht verändert wird
            DBDataObjectAttribute clonedAttrib = new DBDataObjectAttribute(attrib);
            fillAttribByEditControlValue(field, clonedAttrib);
            return clonedAttrib;
        }
        return null;
    }

    protected DBDataObjectAttribute getCurrentAttributeValue(String tableAndfieldName) {
        iPartsMatrixEditField field = matrixEditFields.getFeldByName(TableAndFieldName.getTableName(tableAndfieldName),
                                                                     TableAndFieldName.getFieldName(tableAndfieldName));
        if (field != null) {
            return getCurrentAttribByEditControlValue(field);
        }
        return null;
    }

    public EditControl getEditControlByFieldName(String tableAndFieldName) {
        iPartsMatrixEditField field = matrixEditFields.getFeldByName(TableAndFieldName.getTableName(tableAndFieldName),
                                                                     TableAndFieldName.getFieldName(tableAndFieldName));
        if (field != null) {
            return ultraEditHelper.getEditControlByCoordinates(field);
        }
        return null;
    }

    public AbstractGuiControl getEditGuiControlByFieldName(String tableAndFieldName) {
        iPartsMatrixEditField field = matrixEditFields.getFeldByName(TableAndFieldName.getTableName(tableAndFieldName),
                                                                     TableAndFieldName.getFieldName(tableAndFieldName));
        if (field != null) {
            EditControlFactory ctrl = ultraEditHelper.getEditControlFactoryByCoordinates(field);
            if (ctrl != null) {
                return ctrl.getControl();
            }
        }
        return null;
    }

    protected boolean checkValues() {
        if ((attributes != null) && (tableName != null)) {
            EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(tableName);
            List<String> pkFields = tableDef.getPrimaryKeyFields();
            List<String> emptyFields = new DwList<String>();
            for (iPartsMatrixEditField field : matrixEditFields.getVisibleEditFields()) {
                if (pkFields.contains(field.getKey().getFieldName()) || isMandatoryField(field)) {
                    DBDataObjectAttribute attrib = attributes.getField(field.getKey().getFieldName());
                    if ((attrib != null) && attrib.isEmpty()) { // hier keine Logausgabe machen, wenn attrib null ist, weil das in collectEditValues() bereits gemacht wird
                        emptyFields.add(TranslationHandler.translate(ultraEditHelper.getEditControlByCoordinates(field).getLabel().getText()));
                    }
                }
            }
            if (!iPartsEditUserControlsHelper.handleEmptyFields(emptyFields)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Beendet das Editieren und legt die editierten Werte in den ursprünglichen Datenstrukturen ab.
     *
     * @return {@link EditUserControls.EditResult#STORED} falls das Ablegen der editierten Werte in die ursprünglichen Datenstrukturen ohne
     * Fehler durchgeführt werden konnte,
     * {@link EditUserControls.EditResult#UNMODIFIED} falls keine Daten verändert wurden und
     * {@link EditUserControls.EditResult#ERROR} falls es Fehler gab
     */
    public EditUserControls.EditResult stopAndStoreEdit() {
        if (!readOnly) {
            if (checkCompletionOfFormValues()) {
                collectEditValues();
                if (!isModified()) {
                    return EditUserControls.EditResult.UNMODIFIED;
                } else {
                    if (checkValues()) {
                        // Falls in den Controls eigene Objekte (kombinierte Texte, Werkeinsatzdaten) angelegt wurden, diese hier speichern
                        saveAdditionalData();
                        return EditUserControls.EditResult.STORED;
                    } else {
                        return EditUserControls.EditResult.ERROR;
                    }
                }
            } else {
                return EditUserControls.EditResult.ERROR;
            }
        } else {
            return EditUserControls.EditResult.UNMODIFIED;
        }
    }

    protected void saveAdditionalData() {
        for (iPartsMatrixEditField field : matrixEditFields.getVisibleEditFields()) {
            if (!field.isEditierbar()) { // Nicht editierbare Felder ignorieren
                if (!EtkPluginApi.hasEditorForVirtualField(field.getKey().getTableName(), field.getKey().getFieldName())) {
                    continue;
                }
            }

            DBDataObjectAttribute attrib = getAttributeFromKey(field);
            if (attrib != null) {
                EditControlFactory ctrl = ultraEditHelper.getEditControlFactoryByCoordinates(field);
                if (ctrl != null) {
                    ctrl.saveAdditionalData(getAttributes());
                }
            }
        }
    }

    /**
     * Schreibt die Werte der Editcontrols in die Attribute. Die Originalwerte werden dadurch überschrieben
     */
    protected void collectEditValues() {
        for (iPartsMatrixEditField field : matrixEditFields.getVisibleEditFields()) {
            if (!field.isEditierbar()) { // Nicht editierbare Felder ignorieren
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
                EditControlFactory ctrl = ultraEditHelper.getEditControlFactoryByCoordinates(field);
                String value = null;
                if (ctrl != null) {
                    value = ctrl.getTextFromExtraControls();
                }
                if (value != null) {
                    attrib.setValueAsString(value, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                } else {
                    fillAttribByEditControlValue(field, attrib);
                }
            }
        }
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

    private void onButtonOKAction(Event event) {
        EditUserControls.EditResult editResult = stopAndStoreEdit();
        if (editResult == EditUserControls.EditResult.UNMODIFIED) {
            onButtonCancelAction(null);
            mainWindow.setModalResult(ModalResult.CANCEL);
            close();
        } else if (editResult == EditUserControls.EditResult.STORED) {
            mainWindow.setModalResult(ModalResult.OK);
            close();
        }
    }

    protected void onButtonCancelAction(Event event) {
        close();
    }

    protected GuiPanel getPanelElements() {
        return mainWindow.panelElements;
    }

    protected void clearEditFieldsPanel() {
        getPanelElements().removeAllChildren();
    }

    protected void removeChildFromPanelMain(AbstractGuiControl control) {
        mainWindow.panelMain.removeChild(control);
    }

    protected void addChildToPanelMain(AbstractGuiControl control) {
        mainWindow.panelMain.addChild(control);
    }

    protected void prepareControls(iPartsMatrixEditFields matrixEditFields) {
        int gridY;
        EventListener listener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doEnableButtons(event);
            }
        };

        String viewerLanguage = getConfig().getCurrentViewerLanguage();
        List<String> fallbackLanguages = getConfig().getDataBaseFallbackLanguages();
        for (int colNo = ultraEditHelper.getStartCol(); colNo <= ultraEditHelper.getEndCol(); colNo++) {
            HeaderElem headerElem = ultraEditHelper.getHeaderElem(colNo);
            if (headerElem != null) {
                for (iPartsMatrixEditField field : headerElem.getEditFields()) {
                    DBDataObjectAttribute attrib = getAttributeFromKey(field);
                    String initialValue = calculateInitialValue(field, attrib);
                    EtkDataArray initialDataArray = calculateInitialDataArray(field, attrib);
                    String dbLanguage = calculateDBLanguage(field);
                    String labelText = null;
                    if (!field.isDefaultText()) {
                        labelText = field.getText().getTextByNearestLanguage(viewerLanguage, fallbackLanguages);
                    }
                    gridY = matrixEditFields.getIndexOfTableAndFeldName(field.getKey());
                    EditControl ctrl = editControls.createForEdit(null, getProject(), field.getKey().getTableName(), field.getKey().getFieldName(), dbLanguage,
                                                                  getProject().getViewerLanguage(), initialValue, labelText, gridY);
                    if (initialDataArray != null) {
                        ctrl.getEditControl().setArray(initialDataArray);
                    }
                    modifyEditControl(ctrl, field, initialValue, initialDataArray);
                    ultraEditHelper.prepareEditControl(ctrl, field, listener);
                    handleMultiLangControl(ctrl, field, attrib);
                    //gridY++;
                }
            }
        }
        ultraEditHelper.insertEditControls(getPanelElements());
        resizeForm();
    }

    protected DBDataObjectAttribute getAttributeFromKey(EtkEditField field) {
        return attributes.getField(field.getKey().getFieldName(), false);
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

    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
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
                ntCtrl.enableButton();
                ntCtrl.setMultiLanguage(multiEdit);
            } else {
                ((GuiMultiLangEdit)ctrl.getEditControl().getControl()).setMultiLanguage(multiEdit);
            }
        }
    }

    protected void resizeForm() {
        int totalHeight;
        int totalWidth;
        Dimension screenSize = FrameworkUtils.getScreenSize();
        screenSize.width -= 200;  // damit auch der Calendar Platz hat
        screenSize.height -= 20;
        totalWidth = Math.min(Math.max(getWidth(), ultraEditHelper.getTotalWidth() + 60), screenSize.width); // 60px wegen Fensterrand und Scrollbar
        totalHeight = Math.min(ultraEditHelper.getTotalHeight() + 120, screenSize.height); // 120px wegen Fensterrand und Titel
        setHeight(totalHeight);
        setWidth(totalWidth);
        setCalculatedPanelSize(totalWidth, totalHeight);
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

    protected boolean isVirtualFieldEditable(String tableName, String fieldName) {
        return EtkPluginApi.hasEditorForVirtualField(tableName, fieldName);
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

    protected class UltraEditHelper {

        private Map<Integer, HeaderElem> headerMap;
        private Map<Integer, Integer> rowHeigthMap;
        private int factor;
        private DWBoundsInt colValues;
        private DWBoundsInt rowValues;
        private boolean isInCalculation;
        private boolean withEmptyCols;
        private boolean withEmptyRows;
        private ExtraHeaderElem extraHeaderElem;

        public UltraEditHelper() {
            headerMap = new HashMap<>();
            rowHeigthMap = new HashMap<>();
            setFactor();
            isInCalculation = false;
            withEmptyCols = false;
            withEmptyRows = false;
        }

        public boolean isWithEmptyCols() {
            return withEmptyCols;
        }

        public void setWithEmptyCols(boolean withEmptyCols) {
            this.withEmptyCols = withEmptyCols;
        }

        public boolean isWithEmptyRows() {
            return withEmptyRows;
        }

        public void setWithEmptyRows(boolean withEmptyRows) {
            this.withEmptyRows = withEmptyRows;
        }

        public int getStartCol() {
            return colValues.getMinValue();
        }

        public int getEndCol() {
            return colValues.getMaxValue();
        }

        public HeaderElem getHeaderElem(int colNo) {
            return headerMap.get(colNo);
        }

        public int getTotalWidth() {
            int totalWidth = 0;
            for (int colNo = getStartCol(); colNo <= getEndCol(); colNo++) {
                HeaderElem headerElem = getHeaderElem(colNo);
                if (headerElem != null) {
                    totalWidth += headerElem.getPixelColWidth();
                }
            }
            return totalWidth;
        }

        public int getTotalHeight() {
            int totalHeight = 0;
            for (Integer height : rowHeigthMap.values()) {
                totalHeight += height;
            }
            return totalHeight + 2 * VERT_GAP;
        }

        public void calculateHeader(iPartsMatrixEditFields matrixEditFields) {
            headerMap.clear();
            rowHeigthMap.clear();
            for (iPartsMatrixEditField matrixEditField : matrixEditFields.getFields()) {
                String fieldName = matrixEditField.getKey().getFieldName();
                if (VirtualFieldsUtils.isVirtualField(fieldName)) {
                    matrixEditField.setEditierbar(isVirtualFieldEditable(matrixEditField.getKey().getTableName(), fieldName));
                }
                if (matrixEditField.isVisible()) {
                    HeaderElem headerElem = getHeaderElem(matrixEditField.getColNo());
                    if (headerElem == null) {
                        headerElem = new HeaderElem(matrixEditField);
                        headerMap.put(matrixEditField.getColNo(), headerElem);
                    }
                    headerElem.addEditField(matrixEditField);
                }
            }
            calcHorizontalCoordinates();
        }

        public void prepareEditControl(EditControl ctrl, iPartsMatrixEditField field, EventListener listener) {
            HeaderElem headerElem = getHeaderElem(field.getColNo());
            if (headerElem != null) {
                GuiLabel label = ctrl.getLabel();
                AbstractGuiControl control = ctrl.getEditControl().getControl();
                label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
                label.setHtmlEllipsisEnabled(true);
                label.setConstraints(createLabelConstraints());
                label.setMaximumWidth(headerElem.getPixelLabelWidth());
                label.setMinimumWidth(headerElem.getPixelLabelWidth());
                control.setConstraints(createValueConstraints());
                if (isMandatoryField(field)) {
                    label.setFontStyle(DWFontStyle.BOLD);
                }
                ctrl.getEditControl().setReadOnly(!field.isEditierbar());
                ctrl.getEditControl().getControl().addEventListener(listener);
                headerElem.addRowElem(field.getRowNo(), ctrl);
            }
        }

        public void insertEditControls(GuiPanel panelElements) {
            rowValues = calculateRowNumbers();
            calculateRowHeights();

            EventListener listener = new EventListener(Event.ON_RESIZE_EVENT) {
                @Override
                public void fire(Event event) {
                    doCellResize(event);
                }
            };

            int totalHeight = VERT_GAP;
            isInCalculation = true;
            for (int rowNo = getStartRow(); rowNo <= getEndRow(); rowNo++) {
                for (int colNo = getStartCol(); colNo <= getEndCol(); colNo++) {
                    HeaderElem headerElem = getHeaderElem(colNo);
                    if (headerElem != null) {
                        EditControl ctrl = headerElem.getRowElem(rowNo);
                        if (ctrl != null) {
                            int totalWidth = headerElem.getPixelColWidth();
                            iPartsMatrixEditField matrixEditField = headerElem.getEditFieldByRowNo(rowNo);
                            if (matrixEditField != null) {
                                totalWidth = calculateTotalWidth(matrixEditField, colNo, totalWidth);
                            }
                            GuiPanel cellPanel = createCellPanel(headerElem, totalHeight, totalWidth, rowHeigthMap.get(rowNo));
                            cellPanel.setName("panelCell" + ctrl.getIndexInList());
                            DWPoint cellIndex = new DWPoint(colNo, rowNo);
                            cellPanel.setUserObject(cellIndex);
                            cellPanel.addEventListener(listener);
                            cellPanel.addChild(ctrl.getLabel());
                            cellPanel.addChild(ctrl.getEditControl().getControl());
                            panelElements.addChild(cellPanel);
                        }
                    }
                }
                totalHeight += rowHeigthMap.get(rowNo);
            }
            isInCalculation = false;
        }

        public void resizeEditControls() {
            calculateRowHeights();

            int totalHeight = VERT_GAP;
            isInCalculation = true;
            for (int rowNo = getStartRow(); rowNo <= getEndRow(); rowNo++) {
                for (int colNo = getStartCol(); colNo <= getEndCol(); colNo++) {
                    HeaderElem headerElem = getHeaderElem(colNo);
                    if (headerElem != null) {
                        EditControl ctrl = headerElem.getRowElem(rowNo);
                        if (ctrl != null) {
                            AbstractGuiControl guiControl = ctrl.getLabel().getParent();
                            if (guiControl instanceof GuiLabel) {
                                GuiPanel cellPanel = (GuiPanel)guiControl;
                                ConstraintsAbsolute panelConstraints = new ConstraintsAbsolute(headerElem.getPixelColStart(), totalHeight,
                                                                                               headerElem.getPixelColWidth(), rowHeigthMap.get(rowNo), -1, -1, -1);
                                cellPanel.setConstraints(panelConstraints);
                            }
                        }
                    }
                }
                totalHeight += rowHeigthMap.get(rowNo);
            }
            isInCalculation = false;
        }

        // todo: noch nicht relevant. Wird später ausgebaut.
        private void doCellResize(Event event) {
//            if (!isInCalculation) {
//                if (event.getSource() instanceof GuiPanel) {
//                    GuiPanel cellPanel = (GuiPanel)event.getSource();
//                    if ((cellPanel.getUserObject() != null) && (cellPanel.getUserObject() instanceof DWPoint)) {
//                        DWPoint cellIndex = (DWPoint)cellPanel.getUserObject();
//                    }
//                }
//            }
        }

        private int getStartRow() {
            return rowValues.getMinValue();
        }

        private int getEndRow() {
            return rowValues.getMaxValue();
        }

        private EditControl getEditControlByCoordinates(int colNo, int rowNo) {
            HeaderElem headerElem = getHeaderElem(colNo);
            if (headerElem != null) {
                return headerElem.getRowElem(rowNo);
            }
            return null;
        }

        public EditControl getEditControlByCoordinates(iPartsMatrixEditField matrixEditField) {
            return getEditControlByCoordinates(matrixEditField.getColNo(), matrixEditField.getRowNo());
        }

        public EditControlFactory getEditControlFactoryByCoordinates(iPartsMatrixEditField matrixEditField) {
            EditControl eCtrl = getEditControlByCoordinates(matrixEditField.getColNo(), matrixEditField.getRowNo());
            if (eCtrl != null) {
                return eCtrl.getEditControl();
            }
            return null;
        }

        public int addExtraRowElement(AbstractGuiControl guiControl) {
            int nextRow = getEndRow();
            int totalHeight = VERT_GAP;
            int totalWidth = HORZ_GAP;
            if (extraHeaderElem == null) {
                extraHeaderElem = new ExtraHeaderElem();
                HeaderElem headerElem = getHeaderElem(getEndCol());
                if (headerElem != null) {
                    totalWidth += headerElem.getPixelColStart() + headerElem.getPixelColWidth();
                }
                for (int rowNo = getStartRow(); rowNo <= getEndRow(); rowNo++) {
                    totalHeight += rowHeigthMap.get(rowNo);
                }
            } else {
                nextRow += extraHeaderElem.getEndRow();
                totalWidth = extraHeaderElem.getPixelColWidth();
                totalHeight = extraHeaderElem.getRowHeight(nextRow);
            }

            int rowHeight = guiControl.getPreferredHeight() + 2 * VERT_GAP;
            GuiPanel cellPanel = createCellPanel(HORZ_GAP, totalHeight, totalWidth, rowHeight);
            nextRow++;
            DWPoint cellIndex = new DWPoint(0, nextRow);
            cellPanel.setUserObject(cellIndex);
            cellPanel.setLayout(new LayoutBorder());
            guiControl.setConstraints(new ConstraintsBorder());
            cellPanel.addChild(guiControl);
            getPanelElements().addChild(cellPanel);
            extraHeaderElem.addExtraRowElem(nextRow, cellPanel);
            extraHeaderElem.setRowHeight(nextRow, rowHeight);
            extraHeaderElem.setSize(HORZ_GAP, totalWidth);
            return rowHeight;
        }

        private int calculateTotalWidth(iPartsMatrixEditField matrixEditField, int StartColNo, int totalWidth) {
            int gridWidth = matrixEditField.getGridwidth();
            if (gridWidth > 1) {
                gridWidth--;
                for (int colNo = StartColNo + 1; colNo <= getEndCol(); colNo++) {
                    HeaderElem headerElem = getHeaderElem(colNo);
                    if (headerElem != null) {
                        totalWidth += headerElem.getPixelColWidth() + 2 * HORZ_GAP;
                    }
                    gridWidth--;
                    if (gridWidth <= 0) {
                        break;
                    }
                }
            }
            return totalWidth;
        }

        private void calculateRowHeights() {
            rowHeigthMap.clear();
            for (int rowNo = getStartRow(); rowNo <= getEndRow(); rowNo++) {
                int maxRowHeight = 0;
                for (int colNo = getStartCol(); colNo <= getEndCol(); colNo++) {
                    EditControl ctrl = getEditControlByCoordinates(colNo, rowNo);
                    if (ctrl != null) {
                        maxRowHeight = Math.max(maxRowHeight, ctrl.getEditControl().getControl().getPreferredHeight() + 2 * VERT_GAP);
                    } else {
                        if (withEmptyRows) {
                            maxRowHeight = Math.max(maxRowHeight, DEFAULT_ROW_HEIGHT + 2 * VERT_GAP);
                        }
                    }
                }
                rowHeigthMap.put(rowNo, maxRowHeight);
            }
        }

        private DWBoundsInt calculateRowNumbers() {
            int startRow = Integer.MAX_VALUE;
            int endRow = 0;
            for (int colNo = getStartCol(); colNo <= getEndCol(); colNo++) {
                HeaderElem headerElem = getHeaderElem(colNo);
                if (headerElem != null) {
                    int actRow = headerElem.getFirstRowNo();
                    if (actRow >= 0) {
                        startRow = Math.min(startRow, actRow);
                    }
                    actRow = headerElem.getLastRowNo();
                    if (actRow >= 0) {
                        endRow = Math.max(endRow, actRow);
                    }
                }
            }
            return new DWBoundsInt(startRow, endRow);
        }

        private void fillEmptyColValues() {
            if (withEmptyCols) {
                DWBoundsInt currentColValues = calculateColNumbers();
                for (int colNo = currentColValues.getMinValue(); colNo <= currentColValues.getMaxValue(); colNo++) {
                    HeaderElem headerElem = getHeaderElem(colNo);
                    if (headerElem == null) {
                        headerElem = new HeaderElem(colNo, DEFAULT_COL_WIDTH, DEFAULT_LABEL_WIDTH);
                        headerMap.put(colNo, headerElem);
                    }
                }
            }
        }

        private DWBoundsInt calculateColNumbers() {
            int startCol = Integer.MAX_VALUE;
            int endCol = 0;
            for (Integer col : headerMap.keySet()) {
                if (col < startCol) {
                    startCol = col;
                }
                if (col > endCol) {
                    endCol = col;
                }
            }
            return new DWBoundsInt(startCol, endCol);
        }

        private void calcHorizontalCoordinates() {
            fillEmptyColValues();
            colValues = calculateColNumbers();
            int x = HORZ_GAP;
            for (int colNo = getStartCol(); colNo <= getEndCol(); colNo++) {
                HeaderElem headerElem = getHeaderElem(colNo);
                if (headerElem != null) {
                    headerElem.sortByRow();
                    headerElem.setPixelColStart(x);
                    headerElem.setPixelColWidth(headerElem.getColWidth() * factor);
                    headerElem.setPixelLabelWidth(headerElem.getLabelWidth() * factor);
                    x += headerElem.getPixelColWidth() + 2 * HORZ_GAP;
                }
            }
        }

        private void setFactor() {
            GuiLabel label = new GuiLabel();
            label.setFontSize(DWLayoutManager.get().getFontSize());
            factor = GuiUtils.getTextWidth_A(label.getFont());
        }

        private GuiPanel createCellPanel(HeaderElem headerElem, int totalHeight, int totalWidth, int preferredHeight) {
            return createCellPanel(headerElem.getPixelColStart(), totalHeight, totalWidth, preferredHeight);
        }

        private GuiPanel createCellPanel(int left, int top, int width, int height) {
            GuiPanel panel = new GuiPanel();
            panel.setName("panelCell00");
            panel.__internal_setGenerationDpi(96);
            panel.registerTranslationHandler(getUITranslationHandler());
            panel.setScaleForResolution(true);
            panel.setMinimumWidth(10);
            panel.setMinimumHeight(10);
            LayoutGridBag panelLayout = new LayoutGridBag();
            panel.setLayout(panelLayout);

            // Chrome ist bei Absolutlayout und zIndex = 0 oder leer extrem langsam. Deshalb zIndex auf 1 fest setzen.
            // siehe JV-12670
            ConstraintsAbsolute panelConstraints = new ConstraintsAbsolute(left, top, width, height, -1, -1, 1);
            panel.setConstraints(panelConstraints);

            return panel;
        }

        private ConstraintsGridBag createLabelConstraints() {
            return new ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE,
                                          VERT_GAP, HORZ_GAP, VERT_GAP, HORZ_GAP / 2);
        }

        private ConstraintsGridBag createValueConstraints() {
            return new ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                          VERT_GAP, HORZ_GAP / 2, VERT_GAP, HORZ_GAP);
        }
    }

    private class HeaderElem {

        private int colNo;
        private int colWidth;
        private int labelWidth;
        private List<iPartsMatrixEditField> editFields;
        protected int pixelColStart;
        protected int pixelColWidth;
        private int pixelLabelWidth;
        private Map<Integer, EditControl> rowMap;

        public HeaderElem() {
            this(0, 100, 40);
        }

        public HeaderElem(iPartsMatrixEditField matrixEditField) {
            this(matrixEditField.getColNo(), matrixEditField.getWidth(), matrixEditField.getLabelWidth());
        }

        private HeaderElem(int colNo, int colWidth, int labelWidth) {
            this.colNo = colNo;
            this.colWidth = colWidth;
            this.labelWidth = labelWidth;
            editFields = new DwList<>();
            rowMap = new LinkedHashMap<>();
        }

        public int getColWidth() {
            return colWidth;
        }

        public int getLabelWidth() {
            return labelWidth;
        }

        public java.util.List<iPartsMatrixEditField> getEditFields() {
            return editFields;
        }

        public int getPixelColWidth() {
            return pixelColWidth;
        }

        public void setPixelColWidth(int pixelColWidth) {
            this.pixelColWidth = pixelColWidth;
        }

        public int getPixelColStart() {
            return pixelColStart;
        }

        public void setPixelColStart(int pixelColStart) {
            this.pixelColStart = pixelColStart;
        }

        public int getPixelLabelWidth() {
            return pixelLabelWidth;
        }

        public void setPixelLabelWidth(int pixelLabelWidth) {
            this.pixelLabelWidth = pixelLabelWidth;
        }

        public void addEditField(iPartsMatrixEditField matrixEditField) {
            if (colNo == matrixEditField.getColNo()) {
                this.colWidth = Math.max(colWidth, matrixEditField.getWidth());
                this.labelWidth = Math.max(labelWidth, matrixEditField.getLabelWidth());
                editFields.add(matrixEditField);
            }
        }

        public void sortByRow() {
            Collections.sort(editFields, new Comparator<iPartsMatrixEditField>() {
                @Override
                public int compare(iPartsMatrixEditField o1, iPartsMatrixEditField o2) {
                    if (o1.getRowNo() == o2.getRowNo()) {
                        return 0;
                    } else {
                        if (o1.getRowNo() < o2.getRowNo()) {
                            return -1;
                        }
                    }
                    return 1;
                }
            });
        }

        public void addRowElem(int rowNo, EditControl ctrl) {
            rowMap.put(rowNo, ctrl);
        }

        public EditControl getRowElem(int rowNo) {
            return rowMap.get(rowNo);
        }

        public int getFirstRowNo() {
            if (!editFields.isEmpty()) {
                return editFields.get(0).getRowNo();
            }
            return -1;
        }

        public int getLastRowNo() {
            if (!editFields.isEmpty()) {
                return editFields.get(editFields.size() - 1).getRowNo();
            }
            return -1;
        }

        public iPartsMatrixEditField getEditFieldByRowNo(int rowNo) {
            if (!editFields.isEmpty()) {
                for (iPartsMatrixEditField matrixEditField : editFields) {
                    if (matrixEditField.getRowNo() == rowNo) {
                        return matrixEditField;
                    }
                }
            }
            return null;
        }
    }

    private class ExtraHeaderElem extends HeaderElem {

        private Map<Integer, GuiPanel> extraRowMap;
        private Map<Integer, Integer> rowHeightMap;

        public ExtraHeaderElem() {
            super();
            extraRowMap = new HashMap<>();
            rowHeightMap = new HashMap<>();
        }

        public void addExtraRowElem(int rowNo, GuiPanel panel) {
            extraRowMap.put(rowNo, panel);
        }

        public GuiPanel getExtraRowElem(int rowNo) {
            return extraRowMap.get(rowNo);
        }

        public int getEndRow() {
            return extraRowMap.size();
        }

        public void setSize(int pixelColStart, int pixelColWidth) {
            this.pixelColStart = pixelColStart;
            this.pixelColWidth = pixelColWidth;
        }

        public void setRowHeight(int rowNo, int rowHeight) {
            rowHeightMap.put(rowNo, rowHeight);
        }

        public int getRowHeight(int rowNo) {
            Integer rowHeight = rowHeightMap.get(rowNo);
            if (rowHeight != null) {
                return rowHeight;
            } else {
                return 0;
            }
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
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelElements;

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
            scrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpane.setName("scrollpane");
            scrollpane.__internal_setGenerationDpi(96);
            scrollpane.registerTranslationHandler(translationHandler);
            scrollpane.setScaleForResolution(true);
            scrollpane.setMinimumWidth(10);
            scrollpane.setMinimumHeight(10);
            panelElements = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelElements.setName("panelElements");
            panelElements.__internal_setGenerationDpi(96);
            panelElements.registerTranslationHandler(translationHandler);
            panelElements.setScaleForResolution(true);
            panelElements.setMinimumWidth(10);
            panelElements.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutAbsolute panelElementsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutAbsolute();
            panelElements.setLayout(panelElementsLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelElementsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelElements.setConstraints(panelElementsConstraints);
            scrollpane.addChild(panelElements);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpane.setConstraints(scrollpaneConstraints);
            panelMain.addChild(scrollpane);
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
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonOKAction(event);
                }
            });
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCancelActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonCancelAction(event);
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
}