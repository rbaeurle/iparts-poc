package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableFactoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableFactoryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.AbstractVariantFactoryDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedEditHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsVariantFactoryDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiDockingPanel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.util.misc.id.IdWithType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditUserMultiChangeControlsForVariantsToParts extends EditUserMultiChangeControlsWithAdditionalForms implements iPartsConst {

    public static final String LABEL_FOR_AS_MULTI_EDIT = "!!Massendatenbearbeitung:";

    /**
     * Erzeugt nach dem Edit die Datenobjekte, die im Multi-Edit verändert wurden
     *
     * @param assembly
     * @param selectedVariants
     * @param editedValues
     * @param multiControl
     * @return
     */
    public static EtkDataObjectList createDataObjectListForSelectedEntries(EtkDataAssembly assembly, List<iPartsDataColorTableContent> selectedVariants,
                                                                           DBDataObjectAttributes editedValues, EditUserMultiChangeControlsForVariantsToParts multiControl) {
        GenericEtkDataObjectList dataObjectListToBeSaved = new GenericEtkDataObjectList();
        EtkProject project = multiControl.getProject();
        List<iPartsDataColorTableFactory> colorTableFactoryList = multiControl.getCurrentFactoryDataList();
        iPartsDataColorTableFactoryList colorTableFactoryListWithoutDummyData = new iPartsDataColorTableFactoryList();

        // Dummy-Einträge der Varianten in den Werkseinsatzdaten mit den echten Variantendaten tauschen
        for (iPartsDataColorTableFactory colorTableFactory : colorTableFactoryList) {
            for (iPartsDataColorTableContent selectedVariant : selectedVariants) {
                iPartsColorTableFactoryId colorTableFactoryId = new iPartsColorTableFactoryId(selectedVariant.getAsId().getColorTableId(),
                                                                                              selectedVariant.getAsId().getPosition(),
                                                                                              colorTableFactory.getFactory(),
                                                                                              colorTableFactory.getADat(),
                                                                                              colorTableFactory.getAsId().getDataId(),
                                                                                              selectedVariant.getAsId().getSDATA());
                iPartsDataColorTableFactory tempFactoryData = colorTableFactory.cloneMe(project);
                tempFactoryData.setId(colorTableFactoryId, DBActionOrigin.FROM_EDIT);
                dataObjectListToBeSaved.add(tempFactoryData, DBActionOrigin.FROM_EDIT);
                colorTableFactoryListWithoutDummyData.add(tempFactoryData, DBActionOrigin.FROM_EDIT);
            }
        }

        if (editedValues != null) {
            for (iPartsDataColorTableContent selectedVariant : selectedVariants) {
                for (Map.Entry<String, DBDataObjectAttribute> editedValue : editedValues.entrySet()) {
                    String fieldname = editedValue.getKey();
                    if (selectedVariant.attributeExists(fieldname)) {
                        DBDataObjectAttribute attribute = editedValue.getValue();
                        selectedVariant.setFieldValue(fieldname, attribute.getAsString(), DBActionOrigin.FROM_EDIT);
                    }
                }
                if (selectedVariant.isModifiedWithChildren()) {
                    dataObjectListToBeSaved.add(selectedVariant, DBActionOrigin.FROM_EDIT);
                }

                iPartsDataColorTableFactoryList colorContentFactoryDataListForRetail = iPartsDataColorTableFactoryList.loadColorTableFactoryListForColorTableContentIdForAS(project,
                                                                                                                                                                            selectedVariant.getAsId());
                EtkDataObjectList<iPartsDataColorTableFactory> changedDataObjects = iPartsRelatedEditHelper.updateStatusValuesVariants(colorTableFactoryListWithoutDummyData,
                                                                                                                                       colorContentFactoryDataListForRetail.getAsList(),
                                                                                                                                       iPartsConst.FIELD_DCCF_ADAT,
                                                                                                                                       iPartsConst.FIELD_DCCF_STATUS, true);
                dataObjectListToBeSaved.addAll(changedDataObjects, DBActionOrigin.FROM_EDIT);
            }

            if (dataObjectListToBeSaved.isModifiedWithChildren()) {
                if (!selectedVariants.isEmpty()) {
                    // Das Source-Assembly (Ausgangspunkt der Selektion im Edit) als ChangeSet-Eintrag hinzufügen
                    AssemblyId masterAssemblyId = assembly.getAsId();
                    // zusätzlich Eintrag für die Assembly
                    EtkDataAssembly dataAssembly = EtkDataObjectFactory.createDataAssembly(project, masterAssemblyId);
                    // anstelle von dataAssembly.markAssemblyInChangeSetAsChanged();
                    if (dataAssembly.existsInDB()) {
                        dataAssembly.getAttributes().markAsModified();
                        dataObjectListToBeSaved.add(dataAssembly, DBActionOrigin.FROM_EDIT);
                    }
                }
            }

            return dataObjectListToBeSaved;
        }
        return null;
    }

    private GuiCheckbox factoryDataCheckbox;
    VariantsFactoryDataFormForMultiChange variantFactoryDataForm;

    public EditUserMultiChangeControlsForVariantsToParts(AbstractJavaViewerFormIConnector dataConnector,
                                                         AbstractJavaViewerForm parentForm, EtkEditFields externalEditFields,
                                                         DBDataObjectAttributes initialAttributes,
                                                         EtkDataPartListEntry currentPartListEntry) {
        super(dataConnector, parentForm, externalEditFields, initialAttributes, true, true, false, UnifySource.AFTERSALES);
        this.tableName = TABLE_DA_COLORTABLE_CONTENT;
        addMasterPanel(currentPartListEntry);
    }

    protected void addMasterPanel(EtkDataPartListEntry currentPartListEntry) {
        GuiPanel masterPanel = new GuiPanel(); // Panel für die aufgeschnappten Editoren
        masterPanel.setLayout(new LayoutBorder());
        boolean addMasterPanel = addFactoryDataEditorToMasterPanel(masterPanel, currentPartListEntry);
        if (addMasterPanel) {
            addPanelAsSplitPaneElement(masterPanel);
        }
    }

    private AbstractGuiControl getFactoryDataEditorAndPreviewFromRelInfo(EtkDataPartListEntry currentPartListEntry) {
        variantFactoryDataForm = new VariantsFactoryDataFormForMultiChange(getConnector(),
                                                                           this,
                                                                           currentPartListEntry,
                                                                           true,
                                                                           true);

        return variantFactoryDataForm.getGui();
    }

    private boolean addFactoryDataEditorToMasterPanel(GuiPanel masterPanel, EtkDataPartListEntry currentPartListEntry) {
        // Editor für Werksdaten
        AbstractGuiControl factoryControl = getFactoryDataEditorAndPreviewFromRelInfo(currentPartListEntry);
        if (factoryControl != null) {
            GuiDockingPanel factoryDataDockingPanel = createDockingControl("!!Werksdaten", "!!Werksdaten anzeigen");
            GuiPanel factoryDataPanel = new GuiPanel(); // Panel für die Werksdatenverarbeitung
            factoryDataPanel.setLayout(new LayoutGridBag());
            factoryDataPanel.setConstraints(new ConstraintsBorder());
            addFactoryDataDialog(factoryDataPanel, factoryControl);
            factoryDataDockingPanel.addChild(factoryDataPanel);
            masterPanel.addChildBorderCenter(factoryDataDockingPanel);
            return true;
        }
        return false;
    }

    protected void addFactoryDataDialog(GuiPanel panel, AbstractGuiControl factoryControl) {
        if (factoryControl != null) {
            factoryDataCheckbox = createCheckBox();
            addFormDialogWithCheckbox(panel, factoryControl, factoryDataCheckbox, "!!Werksdaten:");
        }
    }

    @Override
    protected String getLabelText() {
        return LABEL_FOR_AS_MULTI_EDIT;
    }

    @Override
    protected boolean isOneCheckBoxChecked() {
        boolean somethingSelected = false;
        if (!super.isOneCheckBoxChecked()) {
            if (factoryDataCheckbox != null) {
                somethingSelected = isPasteCheckboxSelected();
            }
        } else {
            somethingSelected = true;
        }
        return somethingSelected;
    }

    public boolean isPasteCheckboxSelected() {
        return factoryDataCheckbox.isSelected();
    }

    public List<iPartsDataColorTableFactory> getCurrentFactoryDataList() {
        return variantFactoryDataForm.getCurrentFactoryDataList();
    }

    private class VariantsFactoryDataFormForMultiChange extends AbstractVariantFactoryDataForm {

        private List<iPartsDataColorTableFactory> currentFactoryDataList = new ArrayList<>();

        public VariantsFactoryDataFormForMultiChange(AbstractJavaViewerFormIConnector dataConnector,
                                                     AbstractJavaViewerForm parentForm, EtkDataPartListEntry partListEntry,
                                                     boolean enableEditMode, boolean retailFilter) {

            super(dataConnector, parentForm, iPartsVariantFactoryDataForm.CONFIG_KEY_VARIANT_FACTORY_DATA_AS + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
                  iPartsVariantFactoryDataForm.CONFIG_KEY_VARIANT_FACTORY_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS, partListEntry, null, enableEditMode,
                  retailFilter);
            setCheckboxShowHistoryVisible(false);
            setShowHistory(false);
            if (gridTop instanceof DataObjectFilterGridWithStatus) {
                ((DataObjectFilterGridWithStatus)gridTop).getStatusContextMenu().setVisible(false);
            }
            // Rückmeldedaten bearbeiten ausblenden
            hideEditResponseData();
            dataToGrid(true);
        }

        @Override
        protected void doNew(boolean top) {
            List<IdWithType> selectedColorTableFactory = getSelectedObjectIds(true);
            iPartsDataColorTableFactory colorTableFactoryData = EditUserControlsForColorTableAndContentFactoryData.
                    showCreateColorTableContentFactoryDataWithoutColorTableContentId(getConnector(), this, null);
            if (colorTableFactoryData != null) {
                // SDATA kommt von der Variante und muss hier leer sein, da wir keine eindeutige Variante haben
                colorTableFactoryData.setFieldValue(FIELD_DCCF_SDATA, "", DBActionOrigin.FROM_EDIT);
                iPartsDataColorTableFactoryList colorTableFactoryTempList = new iPartsDataColorTableFactoryList();
                colorTableFactoryTempList.add(colorTableFactoryData, DBActionOrigin.FROM_EDIT);
                // Alle als selektiert annehmen, damit alle in der Ergebnisliste der geänderten Datenobjekte vorhanden sind
                // Das ganze wird später richtig ausgefiltert
                colorTableFactoryTempList.addAll(currentFactoryDataList, DBActionOrigin.FROM_EDIT);
                // Status setzen, damit später die Filterung funkioniert
                EtkDataObjectList<iPartsDataColorTableFactory> changedDataObjects = iPartsRelatedEditHelper.updateStatusValuesVariants(colorTableFactoryTempList,
                                                                                                                                       currentFactoryDataList,
                                                                                                                                       iPartsConst.FIELD_DCCF_ADAT,
                                                                                                                                       getStatusFieldName(), true);
                colorTableFactoryTempList = new iPartsDataColorTableFactoryList();
                colorTableFactoryTempList.addAll(changedDataObjects, DBActionOrigin.FROM_EDIT);
                // Es soll nur der letzte Stand ausgegeben werden
                filterFactoryData(colorTableFactoryTempList);
                currentFactoryDataList = colorTableFactoryTempList.getAsList();
                dataToGrid(true);
                setSelectedObjectIds(true, selectedColorTableFactory);
                factoryDataCheckbox.setSelected(true);
            }
        }

        @Override
        protected void doEdit(boolean top) {
            iPartsDataColorTableFactory selectedColorTableFactory = getColorTableFactoryFromSelection(true);
            iPartsDataColorTableFactory colorTableFactoryData = EditUserControlsForColorTableAndContentFactoryData.
                    showCreateColorTableContentFactoryDataWithoutColorTableContentId(getConnector(), this, selectedColorTableFactory);
            if (colorTableFactoryData != null) {
                // SDATA kommt von der Variante und muss hier leer sein, da wir keine eindeutige Variante haben
                colorTableFactoryData.setFieldValue(FIELD_DCCF_SDATA, "", DBActionOrigin.FROM_EDIT);
                // Ursprünglicher Datensatz entfernen und den editieren hinzufügen
                currentFactoryDataList.remove(selectedColorTableFactory);
                currentFactoryDataList.add(colorTableFactoryData);
                dataToGrid(true);
                setSelectedObjectId(true, colorTableFactoryData.getAsId());
                factoryDataCheckbox.setSelected(true);
            }
        }

        @Override
        protected void doDelete(boolean top) {
            EtkDataObjectList<iPartsDataColorTableFactory> selectedColorTableFactory = getColorTableFactoryFromMultiSelection(true);
            currentFactoryDataList.removeAll(selectedColorTableFactory.getAsList());
            dataToGrid(true);
            if (currentFactoryDataList.isEmpty()) {
                factoryDataCheckbox.setSelected(false);
            }
        }

        @Override
        protected void createAndAddDataObjectsToGrid(boolean top) {
            if (currentFactoryDataList != null) {
                iPartsDataColorTableFactoryList dataColorTableFactoryList = new iPartsDataColorTableFactoryList();
                dataColorTableFactoryList.addAll(currentFactoryDataList, DBActionOrigin.FROM_EDIT);
                addDataObjectListToGrid(true, dataColorTableFactoryList);
            }
        }

        @Override
        protected List<IdWithType> getSelectedObjectIds(boolean top) {
            return getGrid(top).getSelectedObjectIds(iPartsConst.TABLE_DA_COLORTABLE_FACTORY);
        }

        @Override
        protected void setSelectedObjectIds(boolean top, List<IdWithType> selectedIds) {
            getGrid(top).setSelectedObjectIds(selectedIds, iPartsConst.TABLE_DA_COLORTABLE_FACTORY);
        }

        protected void setSelectedObjectId(boolean top, IdWithType selectedIds) {
            getGrid(top).setSelectedObjectId(selectedIds, iPartsConst.TABLE_DA_COLORTABLE_FACTORY, true, true);
        }

        public List<iPartsDataColorTableFactory> getCurrentFactoryDataList() {
            return currentFactoryDataList;
        }
    }
}
