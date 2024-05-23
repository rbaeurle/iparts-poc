/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.DatacardIdentOrderElem;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;

/**
 * {@link GuiTextField} zur Eingabe einer FIN oder eine Aggregate-Idents.
 */
public class iPartsGuiAggIdentTextField extends iPartsGuiAlphaNumTextField {

    public static final String TYPE = "ipartsaggidenttextfield";

    // Die Instanz die die onChangeEvents Aktionen abarbeitet
    private boolean isMandatory;
    private DatacardIdentOrderElem identOrderElem;
    protected GuiContextMenu contextMenu;
    protected AbstractJavaViewerForm parentForm;
    private GuiMenuItem showMenuItem;

    public iPartsGuiAggIdentTextField() {
        super();
        setWithWhitspaces(false);
        setType(TYPE);
        this.identOrderElem = null;
        this.setValidator(new GuiControlValidator() {
            @Override
            public ValidationState validate(AbstractGuiControl control) {
                boolean isValid = true;
                // ist Überprüfung notwendig?
                if (isMandatory() && (identOrderElem != null)) {
                    // Eingabe ist Valid, wenn leer oder ident gültig
                    isValid = identOrderElem.getAggregateIdent().isEmpty() || identOrderElem.getAggregateIdent().isValidId();
                }
                if (showMenuItem != null) {
                    showMenuItem.setEnabled(!getText().isEmpty());
                }
                return new ValidationState(isValid);
            }
        });
        contextMenu = new GuiContextMenu();
        contextMenu.setName("aggIdentContextMenu");
        contextMenu.__internal_setGenerationDpi(96);
        contextMenu.registerTranslationHandler(translationHandler);
        contextMenu.setScaleForResolution(true);
        contextMenu.setMinimumWidth(10);
        contextMenu.setMinimumHeight(10);
        showMenuItem = new GuiMenuItem();
        showMenuItem.setName("showMenuItem");
        showMenuItem.__internal_setGenerationDpi(96);
        showMenuItem.registerTranslationHandler(translationHandler);
        showMenuItem.setScaleForResolution(true);
        showMenuItem.setMinimumWidth(10);
        showMenuItem.setMinimumHeight(10);
        showMenuItem.setMnemonicEnabled(true);
        showMenuItem.setText("!!Ident anzeigen");
        //copyMenuItem.setIcon(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToClipboard"));
        showMenuItem.addEventListener(new EventListener("menuItemEvent") {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                doShowIdent(event);
            }
        });
        contextMenu.addChild(showMenuItem);
        contextMenu.setParentControl(this);
        this.parentForm = null;
    }

    public iPartsGuiAggIdentTextField(boolean isMandatory) {
        this();
        this.isMandatory = isMandatory;
    }

    public iPartsGuiAggIdentTextField(DatacardIdentOrderElem identOrderElem) {
        this();
        this.identOrderElem = identOrderElem;
    }

    public iPartsGuiAggIdentTextField(DatacardIdentOrderElem identOrderElem, boolean isMandatory) {
        this(identOrderElem);
        this.isMandatory = isMandatory;
    }

    public void setMandatoryInput(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public DatacardIdentOrderElem getIdentOrderElem() {
        return identOrderElem;
    }

    public void setIdent(DatacardIdentOrderElem identOrderElem) {
        this.identOrderElem = identOrderElem;
        onTextChanged(null);
    }

    public void setIdentSilent(DatacardIdentOrderElem identOrderElem) {
        this.identOrderElem = identOrderElem;
    }

    public void clear() {
        setText("");
        if (identOrderElem != null) {
            identOrderElem.getAggregateIdent().setIdent("");
        }
    }

    public void setValue(String value) {
        setText(value);
        if (!eventOnChangeListeners.isActive()) {
            onTextChanged(null);
        }
    }

    public void setContextMenu(boolean doSet, AbstractJavaViewerForm parentForm) {
        if (doSet) {
            if (parentForm != null) {
                this.parentForm = parentForm;
                this.setContextMenu(contextMenu);
            }
        } else {
            this.setContextMenu(null);
            this.parentForm = null;
        }
    }

    protected void onTextChanged(de.docware.framework.modules.gui.event.Event event) {
        if (identOrderElem != null) {
            identOrderElem.getAggregateIdent().setIdent(getTrimmedText());
        }
        toggleBackColor(getValidationState().isValid());
        if (isEditable() && (event != null)) {
            fireChangeEvents();
        }
    }

    private void doShowIdent(de.docware.framework.modules.gui.event.Event event) {
        iPartsShowAggIdentDialog.showIdentMembers(parentForm, identOrderElem);
    }

    /**
     * Wird überschrieben, damit es von außen nicht mehr gesetzt werden kann
     * Es werden nie Leerzeichen erlaubt
     *
     * @param withWhitspaces
     */
    @Override
    public void setWithWhitspaces(boolean withWhitspaces) {
        super.setWithWhitspaces(false);
    }
}
