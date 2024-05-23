/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.images.iPartsDefaultImages;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiToggleButton;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.awt.*;
import java.util.Map;

/**
 * Darstellung von Hauptschalter und Checkboxen für Einzelfilter für Verwendung in Filterdialog.
 */
public class iPartsFilterSwitchboardPanel {

    AbstractJavaViewerForm parentForm;
    EtkProject project;
    private iPartsFilterSwitchboard switchboardState;
    private OnChangeEvent onChangeEvent;
    private boolean withEnable = true;  // schaltet das Dimmen aller Filter-Checkboxen bei Haupschalter OFF ein
    private boolean withButton = true;  // zeichnet anstelle der CheckBox einen ToggleButton mit Bild für den Hauptschalter
    private GuiToggleButton toggleButton;

    /**
     * Repräsentiert Checkbox für einen Einzelfilter
     */
    private class iPartsFilterCheckBox extends GuiCheckbox {

        public static final String TYPE = "ipartsfiltercheckbox";

        private iPartsFilterSwitchboard.FilterTypes activatableFilter;
        private Color originalForegroundColor = null;

        public iPartsFilterCheckBox() {
            super();
            setType(TYPE);
            activatableFilter = null;
            setForegroundColor(Colors.clDesignForeground.getColor());
        }

        public iPartsFilterCheckBox(iPartsFilterSwitchboard.FilterTypes activatableFilter) {
            this();
            this.activatableFilter = activatableFilter;
        }

        public iPartsFilterSwitchboard.FilterTypes getActivatableFilter() {
            return activatableFilter;
        }

        public boolean isActivated() {
            if (switchboardState != null) {
                return switchboardState.isOnlyFilterActivated(activatableFilter);
            }
            return false;
        }

        public void setActivated(boolean value) {
            if (switchboardState != null) {
                switchboardState.setFilterActivated(activatableFilter, value);
            }
        }

        public void setInvalid(String toolTip) {
            if (!StrUtils.isEmpty(toolTip)) {
                if (isSelected() || !isEnabled()) {
                    setTooltip(toolTip);
                    handleForeGroundColor(true);
                } else {
                    setTooltip("");
                    handleForeGroundColor(false);
                }
            } else {
                setTooltip("");
                handleForeGroundColor(false);
            }
        }

        private void handleForeGroundColor(boolean setForegroundColor) {
            if (setForegroundColor) {
                if (originalForegroundColor == null) {
                    originalForegroundColor = getForegroundColor();
                    if (Colors.clDefault.getColor().equals(originalForegroundColor)) {
                        originalForegroundColor = Colors.clDesignForeground.getColor();
                    }
                }
                setForegroundColor(iPartsPlugin.clPlugin_iParts_FilterSelectInvalidForegroundColor.getColor()); // Color.red;
            } else {
                if (originalForegroundColor != null) {
                    setForegroundColor(originalForegroundColor);
                    originalForegroundColor = null;
                }
            }
        }
    }

    /**
     * Erzeugt eine Instanz von iPartsActivateFilterDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsFilterSwitchboardPanel(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, EtkProject project) {
        this.parentForm = parentForm;
        this.project = project;
        $$internalCreateGui$$(null);
        this.switchboardState = null;
        this.onChangeEvent = null;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        initGui();
    }

    public int getPreferredWith() {
        return panelMain.panel.getPreferredWidth();
    }

    private void initGui() {
        panelMain.panel.removeAllChildren();
        if (switchboardState != null) {
            int y = 0;
            for (iPartsFilterSwitchboard.FilterTypes entry : switchboardState.getFilterItemMap().keySet()) {
                if (entry.isVisible()) {
                    iPartsFilterCheckBox checkbox = new iPartsFilterCheckBox(entry);
                    checkbox.setName("checkbox_" + entry.name());
                    checkbox.__internal_setGenerationDpi(96);
                    checkbox.registerTranslationHandler(getUITranslationHandler());
                    checkbox.setScaleForResolution(true);
                    checkbox.setMinimumWidth(10);
                    checkbox.setMinimumHeight(10);
                    checkbox.setText(checkbox.getActivatableFilter().getDescription(getProject()));
                    checkbox.setSelected(checkbox.isActivated());
                    checkbox.setConstraints(getComboxConstraints(y));
                    checkbox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                        public void fire(Event event) {
                            doOnChangeEvent(event);
                        }
                    });
                    panelMain.panel.addChild(checkbox);
                    y++;
                }
            }
            if (y > 0) {
                GuiLabel label_0 = new GuiLabel();
                label_0.setName("label_0");
                label_0.__internal_setGenerationDpi(96);
                label_0.registerTranslationHandler(getUITranslationHandler());
                label_0.setScaleForResolution(true);
                label_0.setMinimumWidth(10);
                label_0.setMinimumHeight(10);
//                ConstraintsGridBag label_0Constraints = new ConstraintsGridBag(0, y + 5, 1, 1, 0.0, 100.0, "c", "n", 0, 0, 0, 0);
                ConstraintsGridBag label_0Constraints = getComboxConstraints(y + 5);
                label_0Constraints.setWeighty(100.0);
                label_0.setConstraints(label_0Constraints);
                panelMain.panel.addChild(label_0);

            }
            panelMain.checkboxMasterSwitch.setSelected(switchboardState.isMainSwitchActive());
            if (withEnable) {
                onEnableAllEntries(panelMain.checkboxMasterSwitch.isSelected(), false);
            }
            if (withButton) {
                toggleButton = new GuiToggleButton("!!Filter aktiv", iPartsDefaultImages.edit_toggle_on.getImage(), iPartsDefaultImages.edit_toggle_off.getImage());
                toggleButton.setMinimumHeight(42);
                toggleButton.setName("filterToggleButton");
                ConstraintsGridBag constraints = (ConstraintsGridBag)panelMain.checkboxMasterSwitch.getConstraints();
                panelMain.checkboxMasterSwitch.removeFromParent();
                toggleButton.setConstraints(constraints);
                panelMain.panelMasterSwitch.addChild(toggleButton);
                toggleButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        OnToggleImage(event);
                    }
                });
                toggleButton.setSelected(switchboardState.isMainSwitchActive());
                OnToggleImage(null);
            }
        }
    }

    /**
     * Grafik-Hauptschalter geklickt.
     *
     * @param event
     */
    private void OnToggleImage(Event event) {
        if (toggleButton.isSelected()) {
            toggleButton.setText("!!Filter aktiv");
            toggleButton.setIcon(iPartsDefaultImages.edit_toggle_on.getImage());
        } else {
            toggleButton.setText("!!Filter inaktiv");
            toggleButton.setIcon(iPartsDefaultImages.edit_toggle_off.getImage());
        }
        if (switchboardState != null) {
            switchboardState.setMainSwitchActive(toggleButton.isSelected());
            if (withEnable) {
                if (event == null) {
                    // Initialisierung noch vor dem ShowModal()
                    onEnableAllEntries(toggleButton.isSelected(), false);
                } else {
                    // Jetzt hat wirklich jemand auf den "Filter aktiv"/"Filter inaktiv" gedrückt.
                    onEnableAllEntries(toggleButton.isSelected(), true);
                }
            }
        }
    }

    private ConstraintsGridBag getComboxConstraints(int y) {
        double weightx = 0.0;
        if (y == 0) {
            weightx = 100.0;
        }
        return new ConstraintsGridBag(0, y, 1, 1, weightx, 0.0, "c", "h", 8, 8, 8, 8);
    }

    public AbstractGuiControl getGui() {
        return panelMain;
    }

    public void setPanelTitle(String title) {
        panelMain.panel.setTitle(title);
    }

    public boolean isCompleteFilterActive() {
        if (withButton) {
            return toggleButton.isSelected();
        } else {
            return panelMain.checkboxMasterSwitch.isSelected();
        }
    }

    public void setSwitchboardState(iPartsFilterSwitchboard switchboardState) {
        this.switchboardState = switchboardState;
        initGui();
    }

    public iPartsFilterSwitchboard getFilterSelection() {
        if (switchboardState != null) {
            for (AbstractGuiControl child : panelMain.panel.getChildren()) {
                if (child instanceof iPartsFilterCheckBox) {
                    iPartsFilterCheckBox checkbox = (iPartsFilterCheckBox)child;
                    checkbox.setActivated(checkbox.isSelected());
                }
            }
        }
        return switchboardState;
    }

    public OnChangeEvent getOnChangeEvent() {
        return onChangeEvent;
    }

    public void setOnChangeEvent(OnChangeEvent onChangeEvent) {
        this.onChangeEvent = onChangeEvent;
    }

    public void enableCheckBox(iPartsFilterSwitchboard.FilterTypes activatableFilter, boolean enabled) {
        iPartsFilterCheckBox checkbox = findCheckBox(activatableFilter);
        if (checkbox != null) {
            checkbox.setEnabled(enabled);
        }
    }

    public void selectCheckBox(iPartsFilterSwitchboard.FilterTypes activatableFilter, boolean select) {
        iPartsFilterCheckBox checkbox = findCheckBox(activatableFilter);
        if (checkbox != null) {
            checkbox.setSelected(select);
        }
    }

    /**
     * (De-)Selektiert alle Filter
     *
     * @param select
     */
    public void onSelectAllEntries(boolean select) {
        if (switchboardState != null) {
            OnChangeEvent oldOnChangeEvent = onChangeEvent;
            onChangeEvent = null;
            for (AbstractGuiControl child : panelMain.panel.getChildren()) {
                if (child instanceof iPartsFilterCheckBox) {
                    iPartsFilterCheckBox checkbox = (iPartsFilterCheckBox)child;

                    // Checkbox nur selektieren, wenn sie aktiviert werden darf
                    if (select && !switchboardState.isFilterCanBeActivated(checkbox.getActivatableFilter())) {
                        continue;
                    }

                    checkbox.setSelected(select);
//                    if (checkbox.isEnabled()) {
//                        checkbox.setSelected(select);
//                    } else if (!select) {
//                        // DAIMLER-3546
//                        checkbox.setSelected(select);
//                    }
                }
            }
            onChangeEvent = oldOnChangeEvent;
        }
    }

    /**
     * Checkboxen für Einzelfilter enablen oder disablen.
     *
     * @param enable
     */
    public void onEnableAllEntries(boolean enable, boolean inToggleMode) {
        if (switchboardState != null) {
            OnChangeEvent oldOnChangeEvent = onChangeEvent;
            onChangeEvent = null;
            for (AbstractGuiControl child : panelMain.panel.getChildren()) {
                if (child instanceof iPartsFilterCheckBox) {
                    iPartsFilterCheckBox checkbox = (iPartsFilterCheckBox)child;

                    // Checkbox nur enablen, wenn sie aktiviert werden darf
                    if (enable && !switchboardState.isFilterCanBeActivated(checkbox.getActivatableFilter())) {
                        continue;
                    }

                    checkbox.setEnabled(enable);
                }
            }
            onChangeEvent = oldOnChangeEvent;
        }
    }

    public void setWarnings(Map<iPartsFilterSwitchboard.FilterTypes, String> warnings) {
        if (switchboardState != null) {
            for (AbstractGuiControl child : panelMain.panel.getChildren()) {
                if (child instanceof iPartsFilterCheckBox) {
                    iPartsFilterCheckBox checkbox = (iPartsFilterCheckBox)child;
                    checkbox.setInvalid(warnings.get(checkbox.activatableFilter));
                }
            }
        }
    }

    public iPartsFilterCheckBox findCheckBox(iPartsFilterSwitchboard.FilterTypes activatableFilter) {
        if (switchboardState != null) {
            for (AbstractGuiControl child : panelMain.panel.getChildren()) {
                if (child instanceof iPartsFilterCheckBox) {
                    iPartsFilterCheckBox checkbox = (iPartsFilterCheckBox)child;
                    if (checkbox.getActivatableFilter() == activatableFilter) {
                        return checkbox;
                    }
                }
            }
        }
        return null;
    }

    private void doOnChangeEvent(Event event) {
        if (event != null) {
            if (event.getSource() instanceof iPartsFilterCheckBox) {
                iPartsFilterCheckBox checkbox = (iPartsFilterCheckBox)event.getSource();
                if (onChangeEvent != null) {
                    //eigenen Event schicken
                    onChangeEvent.onChange();
                }
            }
        }
    }

    private void doOnMasterSwitchChangeEvent(Event event) {
        if (switchboardState != null) {
            switchboardState.setMainSwitchActive(panelMain.checkboxMasterSwitch.isSelected());
            if (withEnable) {
                onEnableAllEntries(panelMain.checkboxMasterSwitch.isSelected(), false);
            }
        }
    }

    private EtkProject getProject() {
        return project;
    }

    private TranslationHandler getUITranslationHandler() {
        return parentForm.getUITranslationHandler();
    }

    private AbstractJavaViewerFormIConnector getConnector() {
        return parentForm.getConnector();
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        panelMain = new PanelMainClass(translationHandler);
        panelMain.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected PanelMainClass panelMain;

    private class PanelMainClass extends de.docware.framework.modules.gui.controls.GuiPanel {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkbox_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkbox_1;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMasterSwitch;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxMasterSwitch;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separator_0;

        private PanelMainClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setMinimumWidth(10);
            this.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(panelMainLayout);
            scrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpane.setName("scrollpane");
            scrollpane.__internal_setGenerationDpi(96);
            scrollpane.registerTranslationHandler(translationHandler);
            scrollpane.setScaleForResolution(true);
            scrollpane.setMinimumWidth(10);
            scrollpane.setMinimumHeight(10);
            panel = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel.setName("panel");
            panel.__internal_setGenerationDpi(96);
            panel.registerTranslationHandler(translationHandler);
            panel.setScaleForResolution(true);
            panel.setMinimumWidth(10);
            panel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panel.setLayout(panelLayout);
            checkbox_0 = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkbox_0.setName("checkbox_0");
            checkbox_0.__internal_setGenerationDpi(96);
            checkbox_0.registerTranslationHandler(translationHandler);
            checkbox_0.setScaleForResolution(true);
            checkbox_0.setMinimumWidth(10);
            checkbox_0.setMinimumHeight(10);
            checkbox_0.setText("!!Baumuster-Filter");
            checkbox_0.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    doOnChangeEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkbox_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 8, 8, 8, 8);
            checkbox_0.setConstraints(checkbox_0Constraints);
            panel.addChild(checkbox_0);
            checkbox_1 = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkbox_1.setName("checkbox_1");
            checkbox_1.__internal_setGenerationDpi(96);
            checkbox_1.registerTranslationHandler(translationHandler);
            checkbox_1.setScaleForResolution(true);
            checkbox_1.setMinimumWidth(10);
            checkbox_1.setMinimumHeight(10);
            checkbox_1.setText("!!letzter Filter");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkbox_1Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 8, 8, 8, 8);
            checkbox_1.setConstraints(checkbox_1Constraints);
            panel.addChild(checkbox_1);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel.setConstraints(panelConstraints);
            scrollpane.addChild(panel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpane.setConstraints(scrollpaneConstraints);
            this.addChild(scrollpane);
            panelMasterSwitch = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMasterSwitch.setName("panelMasterSwitch");
            panelMasterSwitch.__internal_setGenerationDpi(96);
            panelMasterSwitch.registerTranslationHandler(translationHandler);
            panelMasterSwitch.setScaleForResolution(true);
            panelMasterSwitch.setMinimumWidth(10);
            panelMasterSwitch.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMasterSwitchLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMasterSwitch.setLayout(panelMasterSwitchLayout);
            checkboxMasterSwitch = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxMasterSwitch.setName("checkboxMasterSwitch");
            checkboxMasterSwitch.__internal_setGenerationDpi(96);
            checkboxMasterSwitch.registerTranslationHandler(translationHandler);
            checkboxMasterSwitch.setScaleForResolution(true);
            checkboxMasterSwitch.setMinimumWidth(10);
            checkboxMasterSwitch.setMinimumHeight(10);
            checkboxMasterSwitch.setFontStyle(1);
            checkboxMasterSwitch.setText("!!Filter einschalten");
            checkboxMasterSwitch.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    doOnMasterSwitchChangeEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxMasterSwitchConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 8, 8, 8, 8);
            checkboxMasterSwitch.setConstraints(checkboxMasterSwitchConstraints);
            panelMasterSwitch.addChild(checkboxMasterSwitch);
            separator_0 = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator_0.setName("separator_0");
            separator_0.__internal_setGenerationDpi(96);
            separator_0.registerTranslationHandler(translationHandler);
            separator_0.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag separator_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            separator_0.setConstraints(separator_0Constraints);
            panelMasterSwitch.addChild(separator_0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMasterSwitchConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMasterSwitchConstraints.setPosition("north");
            panelMasterSwitch.setConstraints(panelMasterSwitchConstraints);
            this.addChild(panelMasterSwitch);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}