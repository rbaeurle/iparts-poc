/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;

/**
 * Abstrakte Klasse für {@link GuiButtonTextField}, die sich auf Produkt Eigenschaften beziehen
 */
public abstract class AbstractProductRelatedButtonTextField extends GuiButtonTextField implements iPartsConst {

    private final EtkProject project;
    private iPartsProductId productId;

    public AbstractProductRelatedButtonTextField(EtkProject project) {
        this.project = project;
        super.setEditable(false);
    }

    protected EtkProject getProject() {
        return project;
    }

    protected iPartsProductId getProductId() {
        return productId;
    }

    protected void setProductId(iPartsProductId productId) {
        this.productId = productId;
    }

    @Override
    public void setEditable(boolean isEditable) {
        getButton().setEnabled(isEditable);
    }

    public abstract void init(final AbstractJavaViewerForm parentForm, final iPartsProductId productId);

    public abstract boolean isModified();
}
