/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsEditBaseValidationForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.iPartsEditAssemblyListValidationForm;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonLayout;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.util.StrUtils;

import java.util.List;
import java.util.Set;

/**
 * Related Info Dialog für die Anzeige der Baumusterauswertung
 */
public class iPartsRelatedInfoModelEvaluationForm extends RelatedInfoBaseForm implements iPartsConst {

    public static final String CONFIG_KEY_MODEL_EVALUATION_DIALOG = "Plugin/iPartsEdit/ModelEvaluationDIALOG";
    public static final String CONFIG_KEY_MODEL_EVALUATION_EDS = "Plugin/iPartsEdit/ModelEvaluationEDS";

    protected iPartsEditBaseValidationForm validationContentForm;
    private GuiLabel labelSelectedModels = new GuiLabel();

    public enum ModelValidationResult {
        VALID("VALID"),
        NOT_RELEVANT("NOT_RELEVANT"),
        INVISIBLE_PART_VALID("INVISIBLE_PART_VALID");

        protected String dbValue;

        public static ModelValidationResult getFromDbValue(String dbValue) {
            for (ModelValidationResult value : values()) {
                if (value.getDbValue().equals(dbValue)) {
                    return value;
                }
            }

            return null;
        }

        ModelValidationResult(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }
    }

    /**
     * Erzeugt einen neuen Dialog für die Anzeige der Baumusterauswertung
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsRelatedInfoModelEvaluationForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        postCreateGui();
    }

    protected void postCreateGui() {
        validationContentForm = new iPartsEditAssemblyListValidationForm(getConnector(), this, true, false) {
            @Override
            protected boolean logPerformanceMessages() {
                return false;
            }

            @Override
            public EtkDisplayFields getDisplayFields(String configKey) {
                // Felder für die Qualitätsprüfung Farbvarianten und Fehler entfernen
                EtkDisplayFields displayFields = super.getDisplayFields(configKey);

                EtkDisplayField colortableQualityCheckField = displayFields.getFeldByName(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_COLORTABLE_QUALITY_CHECK);
                if (colortableQualityCheckField != null) {
                    colortableQualityCheckField.setVisible(false);
                }

                EtkDisplayField qualityCheckErrorField = displayFields.getFeldByName(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_QUALITY_CHECK_ERROR);
                if (qualityCheckErrorField != null) {
                    qualityCheckErrorField.setVisible(false);
                }

                return displayFields;
            }

            @Override
            protected void validatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly, String selectedModel) {
                for (EtkDataPartListEntry partListEntry : partlist) {
                    // Zunächst den Filter ausführen mit Filtergrund und danach das Filterergebnis
                    // an das virtuelle Feld für die Baumusterauswertung übertragen
                    boolean partListEntryValidForModel = filterForModelEvaluation.checkFilter(partListEntry);
                    ModelValidationResult validationResult;
                    if (partListEntryValidForModel) {
                        validationResult = ModelValidationResult.VALID;
                    } else {
                        // DAIMLER-8054: Die folgenden Stücklisteneinträge sind rausgeflogen weil sie Entfallteile sind
                        // oder weil das Kennzeichen "nur Baumuster-Filter" oder "unterdrücken" gesetzt ist. Für solche
                        // Stücklisteneinträge soll die Filterung nochmal durchgeführt werden, ohne die entsprechenden Filter.
                        if (isPartListEntryInvisibleAndValid(partListEntry)) {
                            validationResult = ModelValidationResult.INVISIBLE_PART_VALID;
                        } else {
                            validationResult = ModelValidationResult.NOT_RELEVANT;
                        }
                    }
                    partListEntry.setFieldValue(createVirtualFieldNameForModelOrFINEvaluation(selectedModel), validationResult.getDbValue(),
                                                DBActionOrigin.FROM_DB);
                }
            }

            @Override
            public String createVirtualFieldNameForModelOrFINEvaluation(String modelNumber) {
                return VirtualFieldsUtils.addVirtualFieldMask(iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION
                                                              + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_SPACER
                                                              + modelNumber);
            }

            @Override
            protected String getDisplayFieldConfigKey(String partListType) {
                if ((partListType.equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_RETAIL))) {
                    return CONFIG_KEY_MODEL_EVALUATION_DIALOG;
                } else if (partListType.equals(iPartsConst.PARTS_LIST_TYPE_EDS_RETAIL)) {
                    return CONFIG_KEY_MODEL_EVALUATION_EDS;
                }

                return null;
            }

            @Override
            protected boolean isValidationForPartListPossible(iPartsDataAssembly assembly) {
                return true; // Nur simple Baumuster-Auswertung
            }
        };

        // speichern der selektierten Baumuster in der Session aktivieren
        validationContentForm.saveModelSelectionInSession(true);
        createModelSelectionHeaderPanel();
    }

    protected void createModelSelectionHeaderPanel() {
        // Button und Label für die Baumusterauswahl hinzufügen
        GuiPanel panelHeader = validationContentForm.getMainPanelHeader();
        panelHeader.removeAllChildren();
        panelHeader.setLayout(new LayoutGridBag(false));
        GuiToolbar toolbar = new GuiToolbar(ToolButtonStyle.SMALL);
        toolbar.setName("toolbar");
        toolbar.setButtonLayout(ToolButtonLayout.IMAGE_WEST);

        GuiToolButton toolButtonSelectModels = new GuiToolButton("!!Baumuster auswählen...");
        toolButtonSelectModels.setName("toolButtonSelectModels");
        toolButtonSelectModels.setGlyph(DefaultImages.module.getImage());
        toolButtonSelectModels.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                Set<String> selectedSet = validationContentForm.selectModels(true);
                if (selectedSet != null) {
                    updateSelectedModelsLabel();
                }
            }
        });

        toolbar.addChild(toolButtonSelectModels);
        toolbar.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0.0, 100.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_VERTICAL,
                                                      2, 2, 2, 2));
        panelHeader.addChild(toolbar);

        labelSelectedModels.setName("labelSelectedModels");
        labelSelectedModels.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER,
                                                                  ConstraintsGridBag.FILL_HORIZONTAL, 1, 8, 0, 4));
        panelHeader.addChild(labelSelectedModels);
        panelHeader.setVisible(true);
    }

    @Override
    public AbstractGuiControl getGui() {
        return validationContentForm.getGui();
    }

    public ModalResult showModal() {
        return validationContentForm.showModal();
    }

    protected void updateSelectedModelsLabel() {
        labelSelectedModels.setText(StrUtils.stringListToString(validationContentForm.getSelectedModels(), ", "));
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (forceUpdateAll || (getConnector().getActiveRelatedSubForm() == this || getConnector().getActiveRelatedSubForm() == parentForm)) {
            validationContentForm.updateData(this, forceUpdateAll);
            updateSelectedModelsLabel();
        }
    }
}