/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCheckboxFormHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPartlistTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds.iPartsDialogPartlistTextkind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds.iPartsSaaPartlistTextkind;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog zum Auswählen der Zusatztexte in der Konstruktionsstückliste
 */
public class iPartsTextkindForConstPartlistForm extends AbstractJavaViewerForm {

    public enum PartlistTypeForPartlistTexts {
        DIALOG, EDS_BCS, CTT
    }


    public static void showDIALOGTextkindSelection(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        iPartsTextkindForConstPartlistForm form = new iPartsTextkindForConstPartlistForm(dataConnector, parentForm, PartlistTypeForPartlistTexts.DIALOG);
        if (form.showModal() == ModalResult.OK) {
            iPartsUserSettingsHelper.setDIALOGPartListTextKinds(dataConnector.getProject(),
                                                                iPartsDialogPartlistTextkind.getTextKindsAsString(form.getSelectedDIALOGTextkinds()));
            iPartsEditPlugin.assemblyListEditFilterChanged(dataConnector);
        }
    }

    public static void showEDSBCSTextkindSelection(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        iPartsTextkindForConstPartlistForm form = new iPartsTextkindForConstPartlistForm(dataConnector, parentForm, PartlistTypeForPartlistTexts.EDS_BCS);
        if (form.showModal() == ModalResult.OK) {
            iPartsUserSettingsHelper.setEDSBCSPartListTextKinds(dataConnector.getProject(),
                                                                iPartsSaaPartlistTextkind.getTextKindsAsString(form.getSelectedSaaPartsListTextkinds()));
            iPartsEditPlugin.assemblyListEditFilterChanged(dataConnector);
        }
    }

    public static void showCTTTextkindSelection(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        iPartsTextkindForConstPartlistForm form = new iPartsTextkindForConstPartlistForm(dataConnector, parentForm, PartlistTypeForPartlistTexts.CTT);
        if (form.showModal() == ModalResult.OK) {
            iPartsUserSettingsHelper.setCTTPartListTextKinds(dataConnector.getProject(),
                                                             iPartsSaaPartlistTextkind.getTextKindsAsString(form.getSelectedSaaPartsListTextkinds()));
            iPartsEditPlugin.assemblyListEditFilterChanged(dataConnector);
        }
    }

    private PartlistTypeForPartlistTexts typeForPartlistTexts;
    private List<GuiCheckbox> checkboxes;

    /**
     * Erzeugt eine Instanz von iPartsTextkindForConstPartlistForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsTextkindForConstPartlistForm(AbstractJavaViewerFormIConnector dataConnector,
                                              AbstractJavaViewerForm parentForm, PartlistTypeForPartlistTexts typeForPartlistTexts) {
        super(dataConnector, parentForm);
        this.typeForPartlistTexts = typeForPartlistTexts;
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.mainPanel;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.title.setTitle("!!Konfiguration Stücklistentexte");
        mainWindow.title.setSubtitle("!!Textarten für Stückliste auswählen");
        addTextKindControls();
    }

    private ModalResult showModal() {
        mainWindow.pack();
        return mainWindow.showModal();
    }

    private void addTextKindControls() {
        // Hole alle für diese Session ausgewählten Stücklistentexte
        checkboxes = new ArrayList<>();
        GuiCheckbox checkboxAll = null;
        int gridY = 0;
        if (typeForPartlistTexts != PartlistTypeForPartlistTexts.CTT) {
            checkboxAll = iPartsCheckboxFormHelper.createSelectAllCheckbox(checkboxes, gridY, "all_textkind", getUITranslationHandler());
            // Falls alle Checkboxen selektiert wurden, muss die Haupt-Checkbox auch selektiert werden
            iPartsCheckboxFormHelper.setCheckboxValue(checkboxAll, allCheckboxesSelected());
            mainWindow.controlPanel.addChild(checkboxAll);
            mainWindow.controlPanel.addChild(iPartsCheckboxFormHelper.makeLabel(gridY, "all_textkind", "!!Alle Texte", getUITranslationHandler()));
            gridY++;
            GuiSeparator separator = iPartsCheckboxFormHelper.createSeparator(gridY);
            mainWindow.controlPanel.addChild(separator);
        }
        if (typeForPartlistTexts == PartlistTypeForPartlistTexts.DIALOG) {
            fillCheckBoxForDIALOG(gridY, checkboxAll);
        } else {
            fillCheckBoxForSaaPartsList(gridY, checkboxAll);
        }
    }

    /**
     * Befüllt den Dialog mit den möglichen Textarten für SAA Stücklisten samt Checkboxen
     *
     * @param gridY
     * @param checkboxAll
     */
    private void fillCheckBoxForSaaPartsList(int gridY, GuiCheckbox checkboxAll) {
        boolean isCTTPartsList = typeForPartlistTexts == PartlistTypeForPartlistTexts.CTT;
        Set<iPartsSaaPartlistTextkind> selectedTextKinds = (isCTTPartsList)
                                                           ? iPartsPartlistTextHelper.getSelectedCTTTextkinds(getProject())
                                                           : iPartsPartlistTextHelper.getSelectedEDSBCSTextkinds(getProject());
        List<iPartsSaaPartlistTextkind> textKinds = isCTTPartsList ? iPartsSaaPartlistTextkind.getValidValuesForCTT()
                                                                   : iPartsSaaPartlistTextkind.getAllValidValuesSortedByPrefix();
        for (iPartsSaaPartlistTextkind textkind : textKinds) {
            gridY++;
            GuiCheckbox checkbox = iPartsCheckboxFormHelper.makeCheckbox(gridY, textkind.getTxtKindShort(), getUITranslationHandler());
            checkbox.setUserObject(textkind);
            if (checkboxAll != null) {
                checkbox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                    @Override
                    public void fire(Event event) {
                        // Wurden von Hand alle Einzel-Checkboxen selektiert, dann muss die Haupt-Checkbox auch ausgewählt werden
                        if (getSelectedSaaPartsListTextkinds().size() == textKinds.size()) {
                            iPartsCheckboxFormHelper.setCheckboxValue(checkboxAll, true);
                        } else {
                            iPartsCheckboxFormHelper.setCheckboxValue(checkboxAll, false);
                        }
                    }
                });
            }

            // Falls Checkboxen selektiert wurden, muss geprüft werden, ob die aktuelle eine der selektierten ist
            iPartsCheckboxFormHelper.setCheckboxValue(checkbox, selectedTextKinds.contains(textkind));
            mainWindow.controlPanel.addChild(checkbox);
            checkboxes.add(checkbox);
            GuiLabel label = iPartsCheckboxFormHelper.makeLabel(gridY, textkind.getTxtKindShort(), textkind.getTxtKindDescriptionWithPrefix(), getUITranslationHandler());
            mainWindow.controlPanel.addChild(label);
        }
    }

    private boolean allCheckboxesSelected() {
        if (typeForPartlistTexts == PartlistTypeForPartlistTexts.DIALOG) {
            Set<iPartsDialogPartlistTextkind> selectedTextKinds = iPartsPartlistTextHelper.getSelectedDIALOGTextkinds(getProject());
            return selectedTextKinds.size() == iPartsDialogPartlistTextkind.getValidValues().size();
        } else {
            boolean isCTTPartsList = typeForPartlistTexts == PartlistTypeForPartlistTexts.CTT;
            Set<iPartsSaaPartlistTextkind> selectedTextKinds = (isCTTPartsList)
                                                               ? iPartsPartlistTextHelper.getSelectedCTTTextkinds(getProject())
                                                               : iPartsPartlistTextHelper.getSelectedEDSBCSTextkinds(getProject());
            List<iPartsSaaPartlistTextkind> textKinds = isCTTPartsList ? iPartsSaaPartlistTextkind.getValidValuesForCTT()
                                                                       : iPartsSaaPartlistTextkind.getAllValidValues();
            return selectedTextKinds.size() == textKinds.size();
        }
    }

    /**
     * Befüllt den Dialog mit den möglichen Textarten für DIALOG samt Checkboxen
     *
     * @param gridY
     * @param checkboxAll
     */
    private void fillCheckBoxForDIALOG(int gridY, GuiCheckbox checkboxAll) {
        Set<iPartsDialogPartlistTextkind> selectedTextKinds = iPartsPartlistTextHelper.getSelectedDIALOGTextkinds(getProject());
        for (iPartsDialogPartlistTextkind textkind : iPartsDialogPartlistTextkind.getValidValuesSortedByPrefix()) {
            gridY++;
            GuiCheckbox checkbox = iPartsCheckboxFormHelper.makeCheckbox(gridY, textkind.getTxtKindShort(), getUITranslationHandler());
            checkbox.setUserObject(textkind);
            checkbox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    // Wurden von Hand alle Einzel-Checkboxen selektiert, dann muss die Haupt-Checkbox auch ausgewählt werden
                    if (getSelectedDIALOGTextkinds().size() == iPartsDialogPartlistTextkind.getValidValues().size()) {
                        iPartsCheckboxFormHelper.setCheckboxValue(checkboxAll, true);
                    } else {
                        iPartsCheckboxFormHelper.setCheckboxValue(checkboxAll, false);
                    }
                }
            });
            // Falls Checkboxen selektiert wurden, muss geprüft werden, ob die aktuelle eine der selektierten ist
            if (selectedTextKinds != null) {
                iPartsCheckboxFormHelper.setCheckboxValue(checkbox, selectedTextKinds.contains(textkind));
            }
            mainWindow.controlPanel.addChild(checkbox);
            checkboxes.add(checkbox);
            GuiLabel label = iPartsCheckboxFormHelper.makeLabel(gridY, textkind.getTxtKindShort(), textkind.getTxtKindDescriptionWithPrefix(), getUITranslationHandler());
            mainWindow.controlPanel.addChild(label);
        }
    }

    public Set<iPartsDialogPartlistTextkind> getSelectedDIALOGTextkinds() {
        Set<iPartsDialogPartlistTextkind> result = new HashSet<>();
        for (GuiCheckbox checkbox : checkboxes) {
            if (checkbox.isSelected()) {
                Object textkind = checkbox.getUserObject();
                if (textkind instanceof iPartsDialogPartlistTextkind) {
                    result.add((iPartsDialogPartlistTextkind)textkind);
                }
            }
        }
        return result;
    }

    public Set<iPartsSaaPartlistTextkind> getSelectedSaaPartsListTextkinds() {
        Set<iPartsSaaPartlistTextkind> result = new HashSet<>();
        for (GuiCheckbox checkbox : checkboxes) {
            if (checkbox.isSelected()) {
                Object textkind = checkbox.getUserObject();
                if (textkind instanceof iPartsSaaPartlistTextkind) {
                    result.add((iPartsSaaPartlistTextkind)textkind);
                }
            }
        }
        return result;
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
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel controlPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutFlow mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutFlow();
            this.setLayout(mainWindowLayout);
            mainPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanel.setName("mainPanel");
            mainPanel.__internal_setGenerationDpi(96);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            mainPanel.setMinimumWidth(10);
            mainPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanel.setLayout(mainPanelLayout);
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
            mainPanel.addChild(title);
            controlPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            controlPanel.setName("controlPanel");
            controlPanel.__internal_setGenerationDpi(96);
            controlPanel.registerTranslationHandler(translationHandler);
            controlPanel.setScaleForResolution(true);
            controlPanel.setMinimumWidth(10);
            controlPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag controlPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            controlPanel.setLayout(controlPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder controlPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            controlPanel.setConstraints(controlPanelConstraints);
            mainPanel.addChild(controlPanel);
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
            mainPanel.addChild(buttonpanel);
            this.addChild(mainPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}