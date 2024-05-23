/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiTabbedPane;
import de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

import java.util.List;

public class iPartsRelatedInfoVariantsToPartDataFormWithTab extends AbstractRelatedInfoPartlistDataForm {

    private iPartsRelatedInfoVariantsToPartDataForm variantsToPartDataForm;

    protected iPartsRelatedInfoVariantsToPartDataFormWithTab(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    protected void postCreateGui() {

        final GuiTabbedPane tabbedPane = (GuiTabbedPane)getGui();


        // Erster Tab ist die Ansicht Variantentabelle zu Teil mit den zugehörigen Varianten unten
        final GuiTabbedPaneEntry variantsToPartTab = new GuiTabbedPaneEntry(iPartsConst.RELATED_INFO_VARIANTS_TO_PART_DATA_TEXT);
        variantsToPartDataForm =
                new iPartsRelatedInfoVariantsToPartDataForm(getConnector(), this, getRelatedInfoMainForm(), getRelatedInfo());
        variantsToPartTab.addChild(variantsToPartDataForm.getGui());
        tabbedPane.addChild(variantsToPartTab);

        // Zweiter Tab ist die Filterabsicherung für Variantentabellen
        final GuiTabbedPaneEntry variantsFilterReasonTab = new GuiTabbedPaneEntry("!!Filterabsicherung");
        final iPartsRelatedInfoVariantsFilterReasonDataForm variantsFilterReasonForm =
                new iPartsRelatedInfoVariantsFilterReasonDataForm(getConnector(), getParentForm(), getRelatedInfoMainForm(),
                                                                  getRelatedInfo());

        variantsFilterReasonTab.addChild(variantsFilterReasonForm.getGui());
        tabbedPane.addChild(variantsFilterReasonTab);

        // Dritter Tab ist die Baumusterauswertung für Variantentabellen
        final GuiTabbedPaneEntry variantsModelEvaluationTab = new GuiTabbedPaneEntry("!!Baumusterauswertung");
        final iPartsRelatedInfoVariantsModelEvaluationForm variantsModelEvaluationForm =
                new iPartsRelatedInfoVariantsModelEvaluationForm(getConnector(), getParentForm(), getRelatedInfo());

        variantsModelEvaluationTab.addChild(variantsModelEvaluationForm.getGui());
        tabbedPane.addChild(variantsModelEvaluationTab);

        tabbedPane.addEventListener(new EventListener(Event.TABBED_PANE_TAB_CHANGED_EVENT) {
            @Override
            public void fire(Event event) {
                if (tabbedPane.getActiveTab() == variantsFilterReasonTab.getTabIndex()) {
                    // Die Filterabsicherung nur durchführen, wenn sie auch angezeigt werden soll
                    variantsFilterReasonForm.updateView();
                } else if (tabbedPane.getActiveTab() == variantsModelEvaluationTab.getTabIndex()) {
                    // Die Baumusterauswertung nur durchführen, wenn sie auch angezeigt werden soll
                    variantsModelEvaluationForm.updateValidation();
                }
            }
        });

        tabbedPane.selectTab(variantsToPartTab);
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        return null;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.tabbedPane;
    }

    @Override
    public boolean hideRelatedInfo() {
        return variantsToPartDataForm.hideRelatedInfo();
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
        private de.docware.framework.modules.gui.controls.GuiTabbedPane tabbedPane;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(1000);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(0);
            title.setMinimumHeight(50);
            title.setVisible(false);
            title.setBorderWidth(0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            tabbedPane = new de.docware.framework.modules.gui.controls.GuiTabbedPane();
            tabbedPane.setName("tabbedPane");
            tabbedPane.__internal_setGenerationDpi(96);
            tabbedPane.registerTranslationHandler(translationHandler);
            tabbedPane.setScaleForResolution(true);
            tabbedPane.setMinimumWidth(10);
            tabbedPane.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tabbedPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tabbedPane.setConstraints(tabbedPaneConstraints);
            this.addChild(tabbedPane);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}
