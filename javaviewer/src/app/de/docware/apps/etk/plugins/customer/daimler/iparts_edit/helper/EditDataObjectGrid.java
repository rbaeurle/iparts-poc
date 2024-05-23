/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;

/**
 * Erweiterung {@link DataObjectGrid} für {@link EditToolbarButtonAlias}
 */
public class EditDataObjectGrid extends DataObjectFilterGrid {

    public EditDataObjectGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
    }

    protected void postCreateGui() {
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), getToolBar());
        super.postCreateGui();
    }

    protected EditToolbarButtonMenuHelper getToolbarHelper() {
        return (EditToolbarButtonMenuHelper)toolbarHelper;
    }

    public void enableToolbarButtonAndMenu(EditToolbarButtonAlias alias, boolean enabled) {
        getToolbarHelper().enableToolbarButtonAndMenu(alias, getContextMenuTable(), enabled);
    }

    public void showToolbarButtonAndMenu(EditToolbarButtonAlias alias) {
        getToolbarHelper().showToolbarButtonAndMenu(alias, getContextMenuTable());
    }

    public void hideToolbarButtonAndMenu(EditToolbarButtonAlias alias) {
        getToolbarHelper().hideToolbarButtonAndMenu(alias, getContextMenuTable());
    }

    public void changeToolbarButtonAndMenuTooltip(ToolbarButtonAlias alias, String tooltip) {
        getToolbarHelper().changeToolbarButtonAndMenuTooltip(alias, getContextMenuTable(), tooltip);
    }
}
