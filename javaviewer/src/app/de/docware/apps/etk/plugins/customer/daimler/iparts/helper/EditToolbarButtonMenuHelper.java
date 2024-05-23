/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;

/**
 * Helper Klasse für das Handling mit Toolbars und Menus in iPartsEdit.
 */
public class EditToolbarButtonMenuHelper extends ToolbarButtonMenuHelper {

    public EditToolbarButtonMenuHelper(AbstractJavaViewerFormIConnector dataConnector, GuiToolbar guiToolbar) {
        super(dataConnector, guiToolbar);
    }
}
