/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModule;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPASList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleEinPASId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControlForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiModelSelectTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.HTMLUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.Collection;

/**
 * Form für die Related Info zum Bearbeiten der Modul-Stammdarten in iParts.
 */
public class iPartsRelatedInfoEditModuleDataForm extends AbstractRelatedInfoEditDataForm implements iPartsConst {

    private static final int EDIT_CONTROLS_WIDTH = 800;

    private AssemblyId loadedAssemblyId;
    private iPartsModuleId moduleIdForEdit;
    private iPartsDataModuleEinPASList dataModuleEinPASList;

    private GuiPanel editControlsPanel = new GuiPanel(new LayoutGridBag());
    private EditUserControls editControls;
    private EditUserControls validitiesEditControls;
    private AbstractGuiControl editControlsGui;
    private AbstractGuiControl validitiesEditControlsGui;

    public iPartsRelatedInfoEditModuleDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
    }

    private void resetEditControls() {
        editControlsPanel.removeAllChildren();
        editControlsGui = null;
        editControls = null;
        validitiesEditControlsGui = null;
        validitiesEditControls = null;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        AssemblyId currentAssemblyId = getConnector().getRelatedInfoData().getSachAssemblyId();
        if ((getConnector().getActiveRelatedSubForm() == this) && !Utils.objectEquals(currentAssemblyId, loadedAssemblyId)) {
            // Beim ersten Aufruf von updateData() mit loadedModuleId == null die EditControls nicht zurücksetzen
            if (loadedAssemblyId != null) {
                resetEditControls();
            }
            loadedAssemblyId = currentAssemblyId;
        }
    }


    @Override
    public void showRelatedInfo() {
        // EditControls für die Bearbeitung vom Modul erzeugen und zum editPanel hinzufügen
        if (editControls == null) {
            EtkProject project = getProject();
            EtkDataAssembly currentAssembly = EtkDataObjectFactory.createDataAssembly(project, getConnector().getRelatedInfoData().getSachAssemblyId());

            // getLastHiddenSingleSubAssemblyOrThis() ist notwendig für ausgeblendete Modul-Knoten im Baugruppenbaum
            currentAssembly = currentAssembly.getLastHiddenSingleSubAssemblyOrThis(null);

            if ((currentAssembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)currentAssembly).isPartListEditable()) {
                moduleIdForEdit = new iPartsModuleId(currentAssembly.getAsId().getKVari());
                if (editModuleFormConnector == null) { // dürfte eigentlich nicht passieren
                    editModuleFormConnector = new EditModuleFormConnector(getConnector());
                    addOwnConnector(editModuleFormConnector);
                    EtkDataAssembly editAssembly = EtkDataObjectFactory.createDataAssembly(project, loadedAssemblyId);
                    editModuleFormConnector.setCurrentAssembly(editAssembly);
                }

                // Modul-Stammdaten
                editControls = new EditUserControls(editModuleFormConnector, this, TABLE_DA_MODULE, moduleIdForEdit);
                // Docu Typ für SAs anpassen (hier dürfen DIALOG und DIALOG iParts nicht enthalten sein)
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)currentAssembly;
                if (iPartsAssembly.isSAAssembly()) {
                    EditControl editControl = editControls.getEditControlByFieldName(FIELD_DM_DOCUTYPE);
                    iPartsEditUserControlsHelper.modifyDocuRelevantControlForSA(editControl);
                }
                editControlsGui = editControls.getGui();
                editControls.setReadOnly(!getConnector().isEditContext() || !iPartsEditPlugin.isModuleMasterDataEditable());

                // 250 Pixel Abzug für Related Info Buttons und Rahmen
                editControls.setEditControlsWidth(Math.min(EDIT_CONTROLS_WIDTH, FrameworkUtils.getScreenSize().width - 250));
                editControls.setControlsHeightByPreferredHeight();

                EditControl editControlForSourceTU = editControls.getEditControlByFieldName(iPartsConst.FIELD_DM_SOURCE_TU);
                if (editControlForSourceTU != null) {
                    editControlForSourceTU.getEditControl().setReadOnly(true);
                }

                EditControl editControlForDocuMethod = editControls.getEditControlByFieldName(FIELD_DM_DOCUTYPE);
                if (editControlForDocuMethod != null) {
                    AbstractGuiControl control = editControlForDocuMethod.getAbstractGuiControl();
                    if (control instanceof EnumRComboBox) {
                        EnumRComboBox docuMethodComboBox = (EnumRComboBox)control;
                        docuMethodComboBox.setMaximumRowCount(20);

                        // PSK-Doku-Methoden nur bei PSK-Produkten anzeigen
                        if (!iPartsAssembly.isPSKAssembly() || !iPartsRight.checkPSKInSession()) {
                            EditUserControlForProduct.removePSKDocuMethodsFromComboBox(docuMethodComboBox);
                        }
                    }
                }

                editControlsPanel.addChildGridBag(editControlsGui, 0, 0, 1, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                  ConstraintsGridBag.FILL_BOTH, 0, 0, 0, 0);

                editPanel.addChildBorderWest(editControlsPanel);

                // Modul-Gültigkeiten
                iPartsModuleEinPASId moduleEinPASId = null;
                Collection<String> modelValidity = null;
                String saaValidity = "";
                dataModuleEinPASList = iPartsDataModuleEinPASList.loadForModule(project, currentAssembly.getAsId());
                if (dataModuleEinPASList.isEmpty()) {
                    // Keine Verortung darf eigentlich nicht vorkommen -> notfalls eine neue Dummy-Verortung ohne KG/TU erzeugen
                    iPartsProductId productId = iPartsAssembly.getProductIdFromModuleUsage();
                    if (productId != null) {
                        moduleEinPASId = new iPartsModuleEinPASId(productId.getProductNumber(), currentAssembly.getAsId().getKVari(),
                                                                  EtkDbsHelper.formatLfdNr(1));
                    }
                    dataModuleEinPASList.add(new iPartsDataModuleEinPAS(project, moduleEinPASId), DBActionOrigin.FROM_EDIT);
                } else {
                    iPartsDataModuleEinPAS firstDataModuleEinPAS = dataModuleEinPASList.get(0);
                    moduleEinPASId = firstDataModuleEinPAS.getAsId();
                    EtkDataArray modelValidityArray = firstDataModuleEinPAS.getFieldValueAsArrayOriginal(FIELD_DME_MODEL_VALIDITY);
                    if (modelValidityArray != null) {
                        modelValidity = modelValidityArray.getArrayAsStringList();
                    }
                    saaValidity = firstDataModuleEinPAS.getFieldValue(FIELD_DME_SAA_VALIDITY);
                }

                // SA-TUs sind nicht über DA_MODULES_EINPAS verortet -> keine Modul-Gültigkeiten pflegbar
                if (moduleEinPASId == null) {
                    // Leeres Panel als vertikalen Filler
                    editControlsPanel.addChildGridBag(new GuiPanel(), 0, 2, 1, 1, 100, 100, ConstraintsGridBag.ANCHOR_WEST,
                                                      ConstraintsGridBag.FILL_BOTH, 0, 0, 0, 0);

                    return;
                }

                // Explizite Gültigkeiten-Editfelder
                EtkEditFields validitiesEditFields = new EtkEditFields();
                validitiesEditFields.addField(new EtkEditField(TABLE_DA_MODULES_EINPAS, FIELD_DME_CODE_VALIDITY, false));
                validitiesEditFields.addField(new EtkEditField(TABLE_DA_MODULES_EINPAS, FIELD_DME_MODEL_VALIDITY, false, true));
                EtkEditField saaValidityEditField = validitiesEditFields.addField(new EtkEditField(TABLE_DA_MODULES_EINPAS,
                                                                                                   FIELD_DME_SAA_VALIDITY, false));
                saaValidityEditField.setEditierbar(false);

                validitiesEditControls = new EditUserControls(editModuleFormConnector, this, TABLE_DA_MODULES_EINPAS,
                                                              moduleEinPASId, validitiesEditFields);
                validitiesEditControlsGui = validitiesEditControls.getGui();
                validitiesEditControls.setReadOnly(!getConnector().isEditContext() || !iPartsEditPlugin.isModuleMasterDataEditable());

                // Baumuster Gültigkeiten
                EditControl modelValidityEditControl = validitiesEditControls.getEditControlByFieldName(FIELD_DME_MODEL_VALIDITY);
                if ((modelValidityEditControl != null) && (modelValidityEditControl.getEditControl().getControl() instanceof iPartsGuiModelSelectTextField)) {
                    iPartsGuiModelSelectTextField modelTextField = (iPartsGuiModelSelectTextField)modelValidityEditControl.getEditControl().getControl();
                    iPartsProductId productId = iPartsAssembly.getProductIdFromModuleUsage();
                    modelTextField.init(this);
                    modelTextField.setProductId(productId);
                    if (modelValidity != null) {
                        modelTextField.addDataArrayFromSelection(modelValidity);
                    }
                }

                // SAA Gültigkeiten
                EditControl saaValidityEditControl = validitiesEditControls.getEditControlByFieldName(FIELD_DME_SAA_VALIDITY);
                if (saaValidityEditControl != null) {
                    final int saasPerLine = 7;
                    if (!saaValidity.isEmpty()) {
                        // SAA-Gültigkeiten formatiert anzeigen
                        StringBuilder formattedSAAs = new StringBuilder();
                        VarParam<Integer> saaIndex = new VarParam<>(0);
                        StrUtils.toStringList(saaValidity, iPartsDataAssembly.ARRAY_VALIDITIES_FOR_FILTER_DELIMITER,
                                              false, false).forEach(saa -> {
                            if (formattedSAAs.length() > 0) {
                                formattedSAAs.append(", ");
                                if (saaIndex.getValue() % saasPerLine == 0) {
                                    formattedSAAs.append("\n");
                                }
                            }
                            formattedSAAs.append(iPartsNumberHelper.formatPartNo(project, saa));
                            saaIndex.setValue(saaIndex.getValue() + 1);
                        });
                        saaValidityEditControl.getEditControl().setText(formattedSAAs.toString());
                    }

                    // Optimale Höhe für die GuiTextArea berechnen (es passen 7 SAAs in eine Zeile)
                    int lineHeight = HTMLUtils.getTextDimension(saaValidityEditControl.getEditControl().getControl().getFont(), saaValidity).getHeight();
                    saaValidityEditControl.getEditControl().getControl().setMinimumHeight((int)(Math.ceil((StrUtils.countCharacters(saaValidity, '|') + 1)
                                                                                                          / (double)saasPerLine) * lineHeight));
                }

                // 250 Pixel Abzug für Related Info Buttons und Rahmen
                validitiesEditControls.setEditControlsWidth(Math.min(EDIT_CONTROLS_WIDTH, FrameworkUtils.getScreenSize().width - 250));
                validitiesEditControls.setControlsHeightByPreferredHeight();

                // Separator
                editControlsPanel.addChildGridBag(new GuiSeparator(DWOrientation.HORIZONTAL), 0, 1, 1, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                  ConstraintsGridBag.FILL_HORIZONTAL, 8, 0, 8, 0);

                editControlsPanel.addChildGridBag(validitiesEditControlsGui, 0, 2, 1, 1, 100, 100, ConstraintsGridBag.ANCHOR_WEST,
                                                  ConstraintsGridBag.FILL_BOTH, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected boolean hideRelatedInfoCalled() {
        if ((editControls == null) || !getConnector().isEditContext()) {
            return true;
        }

        // Speichern der Daten
        boolean moduleDataChanged = false;
        EditUserControls.EditResult editResult = editControls.stopAndStoreEdit();
        EditUserControls.EditResult validitiesEditResult;
        if (validitiesEditControls != null) {
            validitiesEditResult = validitiesEditControls.stopAndStoreEdit();
        } else {
            validitiesEditResult = EditUserControls.EditResult.UNMODIFIED;
        }
        if ((editResult == EditUserControls.EditResult.STORED) && (editModuleFormConnector != null) && (validitiesEditResult != EditUserControls.EditResult.ERROR)) {
            DBDataObjectAttributes attributes = editControls.getAttributes();
            iPartsModuleId newModuleId = new iPartsModuleId(attributes.getField(FIELD_DM_MODULE_NO).getAsString());

            if (isRevisionChangeSetActiveForEdit()) { // Speichern im aktiven Änderungsset (ist wohl immer der Fall)
                iPartsDataModule dataModule = new iPartsDataModule(getProject(), newModuleId);
                dataModule.loadFromDB(newModuleId); // Laden, damit das Flag isNew korrekt gesetzt ist
                dataModule.setAttributes(attributes, DBActionOrigin.FROM_EDIT);
                if (dataModule.isModifiedWithChildren()) {
                    addDataObjectToActiveChangeSetForEdit(dataModule);
                    moduleDataChanged = true;
                }
            } // Speichern direkt in der DB wird nicht mehr unterstützt
        }

        if ((validitiesEditResult == EditUserControls.EditResult.STORED) && (editModuleFormConnector != null) && (editResult != EditUserControls.EditResult.ERROR)) {
            DBDataObjectAttributes attributes = validitiesEditControls.getAttributes();
            iPartsModuleEinPASId moduleEinPASId = new iPartsModuleEinPASId(attributes.getFieldValue(FIELD_DME_PRODUCT_NO),
                                                                           attributes.getFieldValue(FIELD_DME_MODULE_NO),
                                                                           attributes.getFieldValue(FIELD_DME_LFDNR));
            if (isRevisionChangeSetActiveForEdit()) { // Speichern im aktiven Änderungsset (ist wohl immer der Fall)
                iPartsDataModuleEinPAS dataModuleEinPAS = new iPartsDataModuleEinPAS(getProject(), moduleEinPASId);
                if (!dataModuleEinPAS.existsInDB()) {
                    dataModuleEinPAS.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }

                dataModuleEinPAS.setAttributes(attributes, DBActionOrigin.FROM_EDIT);
                if (dataModuleEinPAS.isModifiedWithChildren()) {
                    addDataObjectToActiveChangeSetForEdit(dataModuleEinPAS);
                    moduleDataChanged = true;

                    if (!dataModuleEinPASList.isEmpty()) {
                        // Primärschlüsselfelder aus den Attributen entfernen, da diese in den anderen Datensätzen natürlich
                        // unterschiedlich sind
                        attributes = attributes.cloneMe(DBActionOrigin.FROM_DB);
                        attributes.deleteField(FIELD_DME_PRODUCT_NO, DBActionOrigin.FROM_DB);
                        attributes.deleteField(FIELD_DME_MODULE_NO, DBActionOrigin.FROM_DB);
                        attributes.deleteField(FIELD_DME_LFDNR, DBActionOrigin.FROM_DB);

                        // Andere Datensätze für dasselbe Modul ebenfalls anpassen (aktuell gibt es aber eigentlich
                        // immer nur genau einen)
                        for (iPartsDataModuleEinPAS otherDataModuleEinPAS : dataModuleEinPASList) {
                            if (!otherDataModuleEinPAS.getAsId().equals(moduleEinPASId)) {
                                otherDataModuleEinPAS.setAttributes(attributes, DBActionOrigin.FROM_EDIT);
                                if (otherDataModuleEinPAS.isModifiedWithChildren()) {
                                    addDataObjectToActiveChangeSetForEdit(otherDataModuleEinPAS);
                                }
                            }
                        }
                    }
                }
            } // Speichern direkt in der DB wird nicht mehr unterstützt
        }

        if (moduleDataChanged && (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext)) {
            setModifiedByEdit(true);
            iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)getConnector().getEditContext();
            editContext.setFireDataChangedEvent(true);
            editContext.setUpdateModuleMasterData(true);
            editContext.setUpdateEditAssemblyData(true);
        }

        if ((editResult != EditUserControls.EditResult.ERROR) && (validitiesEditResult != EditUserControls.EditResult.ERROR)) {
            resetEditControls();

            // EditControls nur dann neu erzeugen, wenn diese Related Info gerade sichtbar ist und die Related Info nicht
            // gerade mit OK geschlossen wird (bei Abbrechen kann der Benutzer ja Veto einlegen!)
            if ((getConnector().getActiveRelatedSubForm() == this) && (getModalResultOfRelatedInfoMainForm() != ModalResult.OK)) {
                showRelatedInfo();
            }
            return true;
        } else {
            return false;
        }
    }
}