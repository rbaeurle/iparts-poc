/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.VinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;

public class iPartsAskFinModelDialog extends AbstractJavaViewerForm {

    public static FinModelNoContainer askForFinIdOrModelNo(AbstractJavaViewerForm parentForm, FinId finId, String modelNo, VinId vinId) {
        // Bei gültiger FIN/VIN oder Baumuster diese gleich zurückliefern ohne Dialog
        if (finId.isValidId()) {
            if (vinId.isValidId()) {
                return new FinModelNoContainer(finId, null, vinId);
            } else {
                return new FinModelNoContainer(finId, null, null);
            }
        } else if ((vinId != null) && vinId.isValidId()) {
            return new FinModelNoContainer(null, null, vinId);
        } else if (iPartsModel.isModelNumberValid(modelNo)) {
            return new FinModelNoContainer(null, modelNo, null);
        }

        iPartsAskFinModelDialog dlg = new iPartsAskFinModelDialog(parentForm.getConnector(), parentForm);
        dlg.setFinId(finId);
        dlg.setModelNo(modelNo);
        if (dlg.showModal() == ModalResult.OK) {
            FinModelNoContainer resultContainer = new FinModelNoContainer(dlg.getFinId(), dlg.getModelNo(), dlg.getVinId());
            if (resultContainer.modelNo.isEmpty()) {
                resultContainer.modelNo = dlg.getVinFallbackModelNo();
            }
            return resultContainer;
        }
        return new FinModelNoContainer();
    }

    public static class FinModelNoContainer {

        public FinId finId;
        public String modelNo;
        public VinId vinId;

        public FinModelNoContainer() {
            this(null, null, null);
        }

        public FinModelNoContainer(FinId finId, String modelNo, VinId vinId) {
            if (finId == null) {
                finId = new FinId();
            }
            if (modelNo == null) {
                modelNo = "";
            }
            if (vinId == null) {
                vinId = new VinId();
            }
            this.finId = finId;
            this.modelNo = modelNo;
            this.vinId = vinId;
        }

        public boolean isFinIdValid() {
            return finId.isValidId();
        }

        public boolean isModelNoValid() {
            return iPartsModel.isModelNumberValid(modelNo);
        }

        public boolean isVinIdValid() {
            return vinId.isValidId();
        }

        public boolean isContainerValid() {
            return isFinIdValid() || isModelNoValid() || isVinIdValid();
        }
    }

    private iPartsGuiFinModelTextField textFieldFinModel;

    /**
     * Erzeugt eine Instanz von iPartsAskFinModelDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsAskFinModelDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        textFieldFinModel = new iPartsGuiFinModelTextField(true);
        AbstractConstraints constraints = mainWindow.textfieldFinModel.getConstraints();
        textFieldFinModel.setConstraints(constraints);
        mainWindow.textfieldFinModel.removeFromParent();
        mainWindow.panelMain.addChild(textFieldFinModel);
        textFieldFinModel.initForVinFallback(getProject());
        textFieldFinModel.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                checkFINModelData();
            }
        });
        mainWindow.title.setTitle("!!FIN/VIN/Baumuster eingeben");
        mainWindow.labelFinModel.setText("!!FIN/VIN/Baumuster");
        mainWindow.pack();
        mainWindow.setWidth(mainWindow.getWidth() + 78);
        setModelNo("");
        checkFINModelData();
        textFieldFinModel.requestFocus();
    }

    public FinId getFinId() {
        if (isFinValid()) {
            return textFieldFinModel.getFinId();
        }
        return new FinId();
    }

    public void setFinId(FinId finId) {
        if (finId == null) {
            finId = new FinId();
        }
        if (!isModelNoValid() && textFieldFinModel.getTrimmedText().isEmpty()) {
            textFieldFinModel.setFinId(finId);
        }
    }

    public boolean isFinValid() {
        return textFieldFinModel.isFinValid();
    }

    public String getModelNo() {
        if (textFieldFinModel.isModelNoValid()) {
            return textFieldFinModel.getModelNo();
        }
        return "";
    }

    public void setModelNo(String modelNo) {
        if (modelNo == null) {
            modelNo = "";
        }
        if (!isFinValid() && textFieldFinModel.getTrimmedText().isEmpty()) {
            textFieldFinModel.setModelNo(modelNo);
        }
    }

    public boolean isModelNoValid() {
        return textFieldFinModel.isModelNoValid();
    }

    public String getVinFallbackModelNo() {
        if (textFieldFinModel.isVinFallbackModelValid()) {
            return textFieldFinModel.getVinFallbackModel();
        }
        return "";
    }

    public VinId getVinId() {
        if (isVinValid()) {
            return textFieldFinModel.getVinId();
        }
        return new VinId();
    }

    public void setVinId(VinId vinId) {
        if (vinId == null) {
            vinId = new VinId();
        }
        if (!isModelNoValid() && textFieldFinModel.getTrimmedText().isEmpty()) {
            textFieldFinModel.setVin(vinId.getVIN());
        }
    }

    public boolean isVinValid() {
        return textFieldFinModel.isVinValid();
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

    private void checkFINModelData() {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, textFieldFinModel.isFinOrModelValid());
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
        private de.docware.framework.modules.gui.controls.GuiLabel labelFinModel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldFinModel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_0;

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
            title.setTitle("!!FIN/Baumuster eingeben");
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
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            labelFinModel = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelFinModel.setName("labelFinModel");
            labelFinModel.__internal_setGenerationDpi(96);
            labelFinModel.registerTranslationHandler(translationHandler);
            labelFinModel.setScaleForResolution(true);
            labelFinModel.setMinimumWidth(10);
            labelFinModel.setMinimumHeight(10);
            labelFinModel.setText("!!FIN/Baumuster");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelFinModelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 8, 8, 4);
            labelFinModel.setConstraints(labelFinModelConstraints);
            panelMain.addChild(labelFinModel);
            textfieldFinModel = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldFinModel.setName("textfieldFinModel");
            textfieldFinModel.__internal_setGenerationDpi(96);
            textfieldFinModel.registerTranslationHandler(translationHandler);
            textfieldFinModel.setScaleForResolution(true);
            textfieldFinModel.setMinimumWidth(200);
            textfieldFinModel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldFinModelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 8, 4, 8, 8);
            textfieldFinModel.setConstraints(textfieldFinModelConstraints);
            panelMain.addChild(textfieldFinModel);
            label_0 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_0.setName("label_0");
            label_0.__internal_setGenerationDpi(96);
            label_0.registerTranslationHandler(translationHandler);
            label_0.setScaleForResolution(true);
            label_0.setMinimumWidth(10);
            label_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 10, 1, 1, 0.0, 100.0, "c", "v", 0, 0, 0, 0);
            label_0.setConstraints(label_0Constraints);
            panelMain.addChild(label_0);
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