/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

import java.util.Calendar;

/**
 * {@link GuiButtonTextField} zur Einagbe und Anzeige eines Internal Textes
 */
public class iPartsGuiInternalTextField extends GuiButtonTextField {

    public static final String TYPE = "iPartsGuiInternalTextField";

    private int maxTextLength = 66;
    private AbstractJavaViewerForm parentForm;
    private iPartsDataInternalText dataInternalText;

    /**
     * normaler Konstruktor
     */
    public iPartsGuiInternalTextField() {
        setType(TYPE);
    }

    /**
     * Konstruktor, der zu einem Stücklisteintrag den neusten InternalText lädt und anzeigt
     *
     * @param currentParentForm
     * @param currentPartListEntry
     */
    public iPartsGuiInternalTextField(AbstractJavaViewerForm currentParentForm, EtkDataPartListEntry currentPartListEntry) {
        this();
        init(currentParentForm, currentPartListEntry);
        if ((currentParentForm != null) && (currentPartListEntry != null)) {
            iPartsDataInternalText currentDataInternalText = iPartsDataInternalTextList.getYoungestDataInternalText(currentParentForm.getProject(),
                                                                                                                    currentPartListEntry.getAsId());
            if (currentDataInternalText != null) {
                dataInternalText = currentDataInternalText;
                setText(dataInternalText.getText());
            } else {
                dataInternalText = createInternalTextData(currentParentForm, currentPartListEntry);
            }
            setEnabled(dataInternalText.getUserId().equals(iPartsUserAdminDb.getLoginUserName()));
        }
    }

    public void init(AbstractJavaViewerForm currentParentForm, EtkDataPartListEntry currentPartListEntry) {
        if (currentParentForm != null) {
            this.parentForm = currentParentForm;
            dataInternalText = createInternalTextData(parentForm, currentPartListEntry);
            if (dataInternalText != null) {
                super.addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        doEditInternalText(event);
                    }
                });
            }
        } else {
            super.removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    public void init(AbstractJavaViewerForm currentParentForm, iPartsDataInternalText workDataInternalText) {
        if (currentParentForm != null) {
            this.parentForm = currentParentForm;
            if (workDataInternalText != null) {
                this.dataInternalText = workDataInternalText;
            } else {
                iPartsDataInternalTextId id = new iPartsDataInternalTextId(iPartsUserAdminDb.getLoginUserName());
                dataInternalText = new iPartsDataInternalText(parentForm.getProject(), id);
                dataInternalText.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            setText(dataInternalText.getText());
        } else {
            super.removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
        }
    }

    private void doEditInternalText(Event event) {
        dataInternalText.setText(getText());
        iPartsDataInternalText newDataInternalText = editShowInternalText(parentForm, dataInternalText,
                                                                          isEditable());
        if (newDataInternalText != null) {
            init(parentForm, newDataInternalText);
        }
    }

    public static iPartsDataInternalText editShowInternalText(AbstractJavaViewerForm parentForm, iPartsDataInternalText workDataInternalText,
                                                              boolean isEdit) {
        iPartsDataInternalText newDataInternalText = null;
        if (parentForm != null) {
            if (isEdit) {
                if (workDataInternalText == null) {
                    newDataInternalText = EditInternalTextForm.createInternalTextForm(parentForm, workDataInternalText);
                } else {
                    newDataInternalText = EditInternalTextForm.editInternalTextForm(parentForm, workDataInternalText);
                }
            } else {
                EditInternalTextForm.showInternalTextForm(parentForm, workDataInternalText);
            }
        }
        return newDataInternalText;
    }

    public static iPartsDataInternalText createInternalText(AbstractJavaViewerForm parentForm, EtkDataPartListEntry partListEntry) {
        iPartsDataInternalText newDataInternalText = createInternalTextData(parentForm, partListEntry);
        if (newDataInternalText != null) {
            newDataInternalText = EditInternalTextForm.createInternalTextForm(parentForm, newDataInternalText);
        }
        return newDataInternalText;
    }

    private static iPartsDataInternalText createInternalTextData(AbstractJavaViewerForm parentForm, EtkDataPartListEntry partListEntry) {
        iPartsDataInternalText dataInternalText = null;
        if ((parentForm != null) && (partListEntry != null)) {
            boolean isConstructionPartListEntry = false;
            boolean isEDSConstructionPartListEntry = false;
            EtkDataAssembly assembly = partListEntry.getOwnerAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                isConstructionPartListEntry = ((iPartsDataAssembly)assembly).isDialogSMConstructionAssembly();
                // todo Auskommentieren, wenn Interner Text in EDS erlaubt ist
//                if (!isConstructionPartListEntry) {
//                    isEDSConstructionPartListEntry = ((iPartsDataAssembly)assembly).isEdsConstructionAssembly();
//                    if (isEDSConstructionPartListEntry) {
//                        isConstructionPartListEntry = true;
//                    }
//                }
            }
            iPartsDataInternalTextId id;
            if (!isConstructionPartListEntry) {
                id = new iPartsDataInternalTextId(iPartsUserAdminDb.getLoginUserName(), partListEntry.getAsId());
            } else {
                if (isEDSConstructionPartListEntry) {
                    // todo Ausfüllen, wenn Interner Text in EDS erlaubt ist
                    id = new iPartsDataInternalTextId(iPartsUserAdminDb.getLoginUserName(), partListEntry.getAsId());
                } else {
                    // Key aus Konstruktion lesen
                    String dialogGUID = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
                    iPartsDialogId dialogId = new iPartsDialogId(dialogGUID);
                    id = new iPartsDataInternalTextId(iPartsUserAdminDb.getLoginUserName(), dialogId);
                }
            }
            dataInternalText = new iPartsDataInternalText(parentForm.getProject(), id);
            dataInternalText.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
        return dataInternalText;
    }

    @Override
    public void setEditable(boolean isEditable) {
        super.setEditable(isEditable);
        this.getButton().setEnabled(isEditable);
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    public boolean isModified() {
        String text = getText();
        if (!dataInternalText.getText().equals(text)) {
            return true;
        }
        if (dataInternalText.isModified()) {
            if (dataInternalText.getText().trim().isEmpty()) {
                return (dataInternalText.getAttachment().length > 0);
            }
            return true;
        }
        return dataInternalText.isModified();
    }

    public iPartsDataInternalText getAndUpdateDataInternalText() {
        String text = getText();
        if (!dataInternalText.getText().equals(text)) {
            dataInternalText.setText(text);
            if (dataInternalText.getFieldValue(iPartsConst.FIELD_DIT_CHANGE_DATE).isEmpty()) {
                dataInternalText.setChangeTimeStamp(dataInternalText.getCreationTimeStamp(), DBActionOrigin.FROM_EDIT);
            } else {
                dataInternalText.setChangeTimeStamp(Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
            }
        }
        return dataInternalText;
    }
}
