/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.form;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCheckboxFormHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.AbstractMBSDataHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDistributionHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.structure.AbstractMBSStructureHandler;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.*;

/**
 * Dialog zum Auswählen der SAP-MBS Importdaten
 */
public class MBSDataSelectionForm implements iPartsConst {

    private List<GuiCheckbox> checkboxes;
    private String title;
    private String subTitle;
    private String allCheckboxTitle;

    /**
     * Erzeugt eine Instanz von MBSDataSelectionForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public MBSDataSelectionForm(String title, String subTitle, String allCheckboxTitle) {
        this.title = title;
        this.subTitle = subTitle;
        this.allCheckboxTitle = allCheckboxTitle;
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    public ModalResult showModal() {
        mainWindow.pack();
        return mainWindow.showModal();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.title.setTitle(title);
        mainWindow.title.setSubtitle(subTitle);
        enableOKButton(false);
    }

    /**
     * Erzeugt die Checkboxen für alle möglichen SAP-MBS Datentypen
     *
     * @param handlers
     */
    private void createSelection(List<? extends AbstractMBSDataHandler> handlers) {
        checkboxes = new ArrayList<>();
        int gridY = 0;
        // Erst die Checkbox um alle auswählen zu können
        final GuiCheckbox checkboxAll = iPartsCheckboxFormHelper.createSelectAllCheckbox(checkboxes, gridY, "all_data", TranslationHandler.getUiTranslationHandler());
        checkboxAll.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                enableOKButton(getSelectedHandler().length > 0);
            }
        });
        mainWindow.panelHandler.addChild(checkboxAll);
        mainWindow.panelHandler.addChild(iPartsCheckboxFormHelper.makeLabel(gridY, "all_data", allCheckboxTitle, TranslationHandler.getUiTranslationHandler()));
        gridY++;
        GuiSeparator separator = iPartsCheckboxFormHelper.createSeparator(gridY);
        mainWindow.panelHandler.addChild(separator);
        // Erzeuge für jeden möglichen Handler eine Checkbox samt Label
        addHandlers(handlers, gridY, checkboxAll);
    }

    /**
     * Erzeugt für alle übergebenen {@link AbstractMBSDataHandler} eine Checkbox und ein Label
     *
     * @param handlers
     * @param gridY
     * @param checkboxAll
     */
    private void addHandlers(List<? extends AbstractMBSDataHandler> handlers, int gridY, GuiCheckbox checkboxAll) {
        for (AbstractMBSDataHandler handler : handlers) {
            gridY++;
            GuiCheckbox checkbox = iPartsCheckboxFormHelper.makeCheckbox(gridY, handler.getHandlerName(), TranslationHandler.getUiTranslationHandler());
            checkbox.setUserObject(handler);
            checkbox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    // Wurden von Hand alle Einzel-Checkboxen selektiert, dann muss die Haupt-Checkbox auch ausgewählt werden
                    if (getSelectedHandler().length == handlers.size()) {
                        iPartsCheckboxFormHelper.setCheckboxValue(checkboxAll, true);
                    } else {
                        iPartsCheckboxFormHelper.setCheckboxValue(checkboxAll, false);
                    }
                    enableOKButton(getSelectedHandler().length > 0);
                }
            });
            mainWindow.panelHandler.addChild(checkbox);
            checkboxes.add(checkbox);
            GuiLabel label = iPartsCheckboxFormHelper.makeLabel(gridY, handler.getHandlerName(), handler.getHandlerName(), TranslationHandler.getUiTranslationHandler());
            mainWindow.panelHandler.addChild(label);
        }
    }

    private void enableOKButton(boolean enabled) {
        mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setEnabled(enabled);
    }

    /**
     * Liefert die vom Benutzer ausgewählten {@link AbstractMBSDataHandler}s
     *
     * @return
     */
    public AbstractMBSDataHandler[] getSelectedHandler() {
        // Erst alle ausgewählten Checkboxen sammeln
        Set<AbstractMBSDataHandler> selectedHandler = new TreeSet<>(Comparator.comparing(AbstractMBSDataHandler::getHandlerName));
        // Alle Checkboxen durchlaufen und die selektierten mit AbstractSAPDataHandler filtern und dann die einzelnen Handler in allHandler ablegen.
        checkboxes.stream().filter(checkbox -> checkbox.isSelected() && (checkbox.getUserObject() instanceof AbstractMBSDataHandler)).forEach(checkBox -> {
            AbstractMBSDataHandler handler = (AbstractMBSDataHandler)checkBox.getUserObject();
            if (handler.hasDistributionHandler()) {
                // Ist ein Haupt-Handler vorhanden, dann soll dieser für das Auslesen der XML Elemente genutzt werden
                MBSDistributionHandler distributionHandler = handler.getDistributionHandler();
                // Den Sub-Handler am Haupt-Handler registrieren
                distributionHandler.registerSubHandler((AbstractMBSStructureHandler)handler);
                selectedHandler.add(distributionHandler);
            } else {
                selectedHandler.add(handler);
            }
        });
        return selectedHandler.toArray(new AbstractMBSDataHandler[selectedHandler.size()]);
    }

    /**
     * Setzt beim übergebenen {@link MBSDataImporter} die vom Benutzer ausgewählten und übergebenen {@link AbstractMBSDataHandler}s
     *
     * @param importer
     * @param handlers
     * @return
     */
    public boolean setSelectedHandler(MBSDataImporter importer, List<? extends AbstractMBSDataHandler> handlers) {
        createSelection(handlers);
        if (showModal() == ModalResult.OK) {
            importer.setDataHandlers(getSelectedHandler());
            return true;
        }
        return false;
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(120);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelHandler;

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
            mainPanel.__internal_setGenerationDpi(120);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            mainPanel.setMinimumWidth(10);
            mainPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanel.setLayout(mainPanelLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(120);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            mainPanel.addChild(title);
            panelHandler = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelHandler.setName("panelHandler");
            panelHandler.__internal_setGenerationDpi(120);
            panelHandler.registerTranslationHandler(translationHandler);
            panelHandler.setScaleForResolution(true);
            panelHandler.setMinimumWidth(10);
            panelHandler.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelHandlerLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelHandler.setLayout(panelHandlerLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelHandlerConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelHandler.setConstraints(panelHandlerConstraints);
            mainPanel.addChild(panelHandler);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(120);
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