/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;

/**
 * Abstraktes Form für die Related Info zum Bearbeiten von Daten in iParts.
 */
public class AbstractRelatedInfoEditDataForm extends RelatedInfoBaseForm {

    protected EditFormIConnector editModuleFormConnector;
    protected GuiPanel editPanel;

    public AbstractRelatedInfoEditDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        editPanel = new GuiPanel(new LayoutBorder());
        GuiPanel fillerPanel = new GuiPanel();
        fillerPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        editPanel.addChild(fillerPanel);
        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            editModuleFormConnector = ((iPartsRelatedInfoEditContext)getConnector().getEditContext()).getEditFormConnector();
        }
    }

    @Override
    public AbstractGuiControl getGui() {
        return editPanel;
    }
}