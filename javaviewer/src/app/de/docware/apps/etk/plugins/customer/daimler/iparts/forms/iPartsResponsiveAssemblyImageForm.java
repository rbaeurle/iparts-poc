/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageFormIConnector;
import de.docware.apps.etk.base.viewermain.forms.responsive.ResponsiveAssemblyImageForm;

/**
 * iParts-spezifische Erweiterung vom {@link ResponsiveAssemblyImageForm}.
 */
public class iPartsResponsiveAssemblyImageForm extends ResponsiveAssemblyImageForm {

    public iPartsResponsiveAssemblyImageForm(AssemblyImageFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
    }

    @Override
    public void modifyConnectorBeforeUpdateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.modifyConnectorBeforeUpdateData(sender, forceUpdateAll);
        iPartsAssemblyImageForm.modifyConnectorForPreferSVGImages(this);
    }
}