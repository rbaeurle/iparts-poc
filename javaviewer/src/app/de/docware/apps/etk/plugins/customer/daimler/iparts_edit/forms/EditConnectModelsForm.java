/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelProperties;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Dialog zur Auswahl einer verknüpften Baureihe bzw. Baumustern.
 */
public class EditConnectModelsForm extends AbstractJavaViewerForm {

    private final iPartsProductId productId;
    private iPartsGuiModelSelectTextField modelsTextField;
    private iPartsGuiConstModelSelectTextField modelsConstTextField;
    private String partialModelNumberWithWildCard;
    private MasterDataProductCharacteristics masterDataProductCharacteristics;


    // Die Instanz die die onChangeEvents Aktionen abarbeitet
    protected EventListeners eventOnChangeListeners;

    /**
     * Erzeugt eine Instanz von EditConnectModelsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditConnectModelsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                 iPartsProductId productId) {
        super(dataConnector, parentForm);
        this.productId = productId;
        $$internalCreateGui$$(null);
        this.eventOnChangeListeners = new EventListeners();
        postCreateGui();
        setDocuType(iPartsDocumentationType.UNKNOWN);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        EventListener listener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                fireChangeEvents();
            }
        };

        modelsTextField = new iPartsGuiModelSelectTextField(getProject());
        EditControlFactory.setDefaultLayout(modelsTextField);
        modelsTextField.init(this);
        modelsTextField.setModelNumberSearchFieldVisible(true);
        modelsTextField.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_NORTH));
        modelsTextField.addEventListener(listener);
//        mainWindow.panelModels.addChild(modelsTextField);

        modelsConstTextField = new iPartsGuiConstModelSelectTextField(getProject(), productId);
        EditControlFactory.setDefaultLayout(modelsConstTextField);
        modelsConstTextField.init(this);
        modelsConstTextField.setModelNumberSearchFieldVisible(true);
        modelsConstTextField.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        modelsConstTextField.addEventListener(listener);
//        mainWindow.panelModels.addChild(modelsConstTextField);

        modelsTextField.setName("linked_models");
        modelsConstTextField.setName("linked_constmodels");

        resizeForm(mainWindow.panelMain.getPreferredHeight());
    }

    @Override
    public GuiPanel getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    @Override
    public AbstractJavaViewerFormIConnector getConnector() {
        return super.getConnector();
    }

    public iPartsProductId getProductId() {
        return productId;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setPanelTitle(String title) {
        mainWindow.panelMain.setTitle(title);
    }

    public String getPanelTitle() {
        return mainWindow.panelMain.getTitle();
    }

    /**
     * Setzt die aktuellen Eigenschaften des Produkts
     *
     * @param masterDataProductCharacteristics
     */
    public void setProductCharacteristics(MasterDataProductCharacteristics masterDataProductCharacteristics) {
        this.masterDataProductCharacteristics = masterDataProductCharacteristics;
        checkModelTextField();
        checkModeForCurrentProductCharacteristics();
    }

    /**
     * Überprüft, ob die Baumuster-Auswahl editierbar ist
     */
    private void checkModelTextField() {
        if (isCarOrTruckProduct()) {
            modelsTextField.setEditable(true);
            modelsTextField.reinit(this, masterDataProductCharacteristics);
        } else {
            modelsTextField.setEditable(false);
        }
    }

    /**
     * Liefert zurück, ob das Produkt entweder PKW und/oder Truck Eigenschaften besitzt
     *
     * @return
     */
    private boolean isCarOrTruckProduct() {
        return (masterDataProductCharacteristics != null) && (masterDataProductCharacteristics.isCarAndVanProduct() || masterDataProductCharacteristics.isTruckAndBusProduct());
    }

    /**
     * Setzt die Baumuster- oder Baureihen-Auswahl in Abhänigkeit der Pordukteigenschaften
     */
    private void checkModeForCurrentProductCharacteristics() {
        if ((masterDataProductCharacteristics == null) || (!masterDataProductCharacteristics.isCarAndVanProduct() && !masterDataProductCharacteristics.isTruckAndBusProduct())) {
            mainWindow.panelModels.setEnabled(false);
        } else {
            mainWindow.panelModels.setEnabled(masterDataProductCharacteristics.isCarAndVanProduct() || masterDataProductCharacteristics.isTruckAndBusProduct());
        }
    }

    public void setDocuType(iPartsDocumentationType docuType) {
        switch (docuType) {
            case DIALOG:
            case DIALOG_IPARTS:
                modelsTextField.removeFromParent();
                mainWindow.panelModels.addChild(modelsConstTextField);
                break;
            default:
                modelsConstTextField.removeFromParent();
                mainWindow.panelModels.addChild(modelsTextField);
                break;
        }
    }

    /**
     * (Partielle) Baumusternummer, die auch Wildcards enthalten kann, für die Vorfilterung der Baumusterauswahl
     *
     * @return
     */
    public String getPartialModelNumberWithWildCard() {
        return partialModelNumberWithWildCard;
    }

    /**
     * (Partielle) Baumusternummer, die auch Wildcards enthalten kann, für die Vorfilterung der Baumusterauswahl
     *
     * @param partialModelNumberWithWildCard
     */
    public void setPartialModelNumberWithWildCard(String partialModelNumberWithWildCard) {
        this.partialModelNumberWithWildCard = partialModelNumberWithWildCard;
        modelsTextField.setPartialModelNumberWithWildCard(partialModelNumberWithWildCard);
        modelsConstTextField.setPartialModelNumberWithWildCard(partialModelNumberWithWildCard);
        modelsConstTextField.setEditable(!StrUtils.isEmpty(partialModelNumberWithWildCard));
    }

    public void setModelIds(List<iPartsModelId> modelIds) {
        EtkDataArray dataArray = new EtkDataArray();
        if ((modelIds != null) && !modelIds.isEmpty()) {
            List<String> list = new DwList<>();
            for (iPartsModelId modelId : modelIds) {
                list.add(modelId.getModelNumber());
            }
            dataArray.add(list);
        }
        modelsTextField.setArray(dataArray);
        modelsConstTextField.setArray(dataArray);
    }

    public List<iPartsModelId> getModelIds() {
        List<iPartsModelId> list = new DwList<>();
        if (modelsTextField.getParent() == null) {
            if (!modelsConstTextField.getArray().isEmpty()) {
                List<String> modelNumbers = modelsConstTextField.getArray().getArrayAsStringList();
                for (String modelNumber : modelNumbers) {
                    list.add(new iPartsModelId(modelNumber.trim()));
                }
            }
        } else {
            if (!modelsTextField.getArray().isEmpty()) {
                List<String> modelNumbers = modelsTextField.getArray().getArrayAsStringList();
                for (String modelNumber : modelNumbers) {
                    list.add(new iPartsModelId(modelNumber.trim()));
                }
            }
        }
        return list;
    }

    public List<iPartsDataModel> getAllASDataModels() {
        List<iPartsDataModel> result = new DwList<>();
        if (modelsConstTextField.getParent() != null) {
            for (iPartsDataModel dataModel : modelsConstTextField.getASModelList()) {
                result.add(dataModel);
            }
        }
        return result;
    }

    public List<iPartsDataModel> getModifiedASDataModels() {
        List<iPartsDataModel> result = new DwList<>();
        if (modelsConstTextField.getParent() != null) {
            for (iPartsDataModel dataModel : modelsConstTextField.getASModelList()) {
                if ((dataModel.isNew()) || (dataModel.isModifiedWithChildren())) {
                    result.add(dataModel);
                }
            }
        }
        return result;
    }

    public List<iPartsDataModelProperties> getModifiedConstDataModels() {
        List<iPartsDataModelProperties> result = new DwList<>();
        if (modelsConstTextField.getParent() != null) {
            for (iPartsDataModelProperties dataModelProperties : modelsConstTextField.getConstModelList()) {
                if ((dataModelProperties.isNew()) || (dataModelProperties.isModifiedWithChildren())) {
                    result.add(dataModelProperties);
                }
            }
        }
        return result;
    }

    public void setReadOnly(boolean readOnly) {
        modelsTextField.setEditable(!readOnly);
        modelsConstTextField.setEditable(!readOnly);
    }

    /**
     * Füge den übergebenen Eventlistener hinzu
     */
    public void addEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(Event.ON_CHANGE_EVENT)) {
            eventOnChangeListeners.addEventListener(eventListener);
        }
    }

    /**
     * Entferne den übergebenen Eventlistener
     */
    public void removeEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(Event.ON_CHANGE_EVENT)) {
            eventOnChangeListeners.removeEventListener(eventListener);
        }
    }

    /**
     * Schaltet (z.B. für das Befüllen einer Combobox) die EventListener ab.
     * Diese können dann mit switchOnEventListeners wieder aktiviert werden.
     * Hierbei sollte grundsätzlich eine try-finally-Konstruktion verwendet werden,
     * um sicherzustellen, dass die Reaktivierung auch im Fehlerfall durchgeführt wird.
     */
    public void switchOffEventListeners() {
        eventOnChangeListeners.setActive(false);
    }

    /**
     * Schaltet (z.B. nach Befüllen einer Combobox) die registrierten EventListener wieder ein.
     * Diese Methode sollte grundsätzlich im Finally eines Try-Blocks gerufen werden, der vor der
     * Abschaltung der Listener beginnt.
     */
    public void switchOnEventListeners() {
        eventOnChangeListeners.setActive(true);
    }

    private void buttonOKClick(Event event) {
        close();
    }

    private void fireChangeEvents() {
        if (eventOnChangeListeners.isActive()) {
            // Eigenen Kind-Thread starten, weil der aufrufende (Warte-)Thread ansonsten zwischendrin abgebrochen werden könnte,
            // was z.B. bei IO-Aktionen Exceptions verursacht
            Session.startChildThreadInSession(thread -> {
                final List<EventListener> listeners = eventOnChangeListeners.getListeners(Event.ON_CHANGE_EVENT);
                if (listeners.size() > 0) {
                    Session.invokeThreadSafeInSession(() -> {
                        for (EventListener listener : listeners) {
                            listener.fire(new Event(Event.ON_CHANGE_EVENT));
                        }
                    });
                }
            });
        }
    }

    protected void resizeForm(int totalHeight) {
        totalHeight = Math.min(206, totalHeight + mainWindow.getHeight());
        mainWindow.setHeight(totalHeight);
        mainWindow.setMaximumHeight(totalHeight);
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
        private de.docware.framework.modules.gui.controls.GuiLabel labelModel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelModels;

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
            panelMain.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            panelMain.setTitle("!!Verknüpfung");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            labelModel = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelModel.setName("labelModel");
            labelModel.__internal_setGenerationDpi(96);
            labelModel.registerTranslationHandler(translationHandler);
            labelModel.setScaleForResolution(true);
            labelModel.setMinimumWidth(10);
            labelModel.setMinimumHeight(10);
            labelModel.setText("!!Baumuster");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelModelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 8, 8, 0);
            labelModel.setConstraints(labelModelConstraints);
            panelMain.addChild(labelModel);
            panelModels = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelModels.setName("panelModels");
            panelModels.__internal_setGenerationDpi(96);
            panelModels.registerTranslationHandler(translationHandler);
            panelModels.setScaleForResolution(true);
            panelModels.setMinimumWidth(10);
            panelModels.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelModelsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelModels.setLayout(panelModelsLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelModelsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "b", 8, 8, 8, 8);
            panelModels.setConstraints(panelModelsConstraints);
            panelMain.addChild(panelModels);
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
                    buttonOKClick(event);
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