/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

/**
 * Editor für PictureIds von Zusatzgrafiken
 */
public class iPartsGuiPictureIdsTextField extends GuiButtonTextField {

    public static final String TYPE = "ipartspictureidstextfield";

    // Defaultwerte
    //private EditCharCase editCaseModeDefaultValue = EditCharCase.eecNormal;  // case-insensitiv,

    // Spezifische Eigenschaften der Komponente
    //private EditCharCase editCaseMode = editCaseModeDefaultValue;
    private AbstractJavaViewerFormIConnector connector;

    public iPartsGuiPictureIdsTextField() {
        super();
        setType(TYPE);
    }

    public AbstractJavaViewerFormIConnector getConnector() {
        return connector;
    }

    public void setConnector(AbstractJavaViewerFormIConnector connector) {
        this.connector = connector;
        if (connector != null) {
            super.addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                public void fire(Event event) {
                    String newPicturesIds = EditPictureField.editPictureIds(getConnector(), null, getText());
                    if (!newPicturesIds.equals(getText())) {
                        setText(newPicturesIds);
                    }
                }
            });
        } else {
            super.removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    @Override
    public void setEditable(boolean isEditable) {
        super.setEditable(isEditable);
        this.getButton().setEnabled(isEditable);
    }


}
