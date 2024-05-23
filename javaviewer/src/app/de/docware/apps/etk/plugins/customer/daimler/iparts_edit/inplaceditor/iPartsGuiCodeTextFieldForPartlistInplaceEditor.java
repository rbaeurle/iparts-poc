/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.InplaceEdit.InplaceEditor;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiCodeTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditEqualizeFieldsHelper;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.table.GuiTableInPlaceEditorFactoryWithValidationInterface;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;
import java.util.Set;

/**
 * Implementierung den Inplace Editors für die Aftersales Codebedingung bei Stücklisteneinträgen mit Validierung über
 * Baureihe, Datum ab und Produktgruppe
 */
public class iPartsGuiCodeTextFieldForPartlistInplaceEditor extends InplaceEditor {

    public static final String DEFAULT_TABLE_AND_FIELD_NAME = TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES);

    protected String lastValidatedInput;
    private String productGroup = "";

    public iPartsGuiCodeTextFieldForPartlistInplaceEditor() {
        super(new iPartsGuiCodeTextField());
        lastValidatedInput = null;
    }

    @Override
    public void init(AbstractJavaViewerFormIConnector connector) {
        lastValidatedInput = null;
        iPartsGuiCodeTextField codeTextfield = getCodeTextfield();
        if ((connector instanceof AssemblyListFormIConnector) && (codeTextfield != null)) {
            AssemblyListFormIConnector dataConnector = (AssemblyListFormIConnector)connector;
            EtkDataAssembly assembly = dataConnector.getCurrentAssembly();
            iPartsDocumentationType docuType = iPartsDocumentationType.UNKNOWN;
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly ipartsAssembly = (iPartsDataAssembly)assembly;
                iPartsProductId productId = ipartsAssembly.getProductIdFromModuleUsage();
                if (productId != null) {
                    iPartsProduct product = iPartsProduct.getInstance(connector.getProject(), productId);
                    productGroup = product.getProductGroup();
                }
                docuType = ipartsAssembly.getDocumentationType();
            }
            // zum Zwischenspeichern der Werte (Speziell dataConnector)
            codeTextfield.init(dataConnector.getProject(), docuType, "", "", "", "", null);
        }
    }

    @Override
    public boolean updateEditor(Object cellContent, List<EtkDataObject> dataObjects, int width) {
        iPartsGuiCodeTextField codeTextfield = getCodeTextfield();
        if ((codeTextfield != null) && (dataObjects != null) && !dataObjects.isEmpty()) {
            if (codeTextfield.getProject() != null) {
                String cellText = getDBCellContentText(dataObjects, cellContent);

                for (EtkDataObject dataObject : dataObjects) {
                    if (dataObject instanceof iPartsDataPartListEntry) {
                        iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)dataObject;
                        String aa = partListEntry.getFieldValue(iPartsConst.FIELD_K_AA);
                        String dateFrom = partListEntry.getFieldValue(iPartsConst.FIELD_K_DATEFROM);
                        String seriesNumber = "";
                        if (partListEntry.getSeriesId() != null) {
                            seriesNumber = partListEntry.getSeriesId().getSeriesNumber();
                        }

                        String productGroupForCodeCheck = partListEntry.getFieldValue(iPartsConst.FIELD_K_PRODUCT_GRP);
                        if (productGroupForCodeCheck.isEmpty()) {
                            productGroupForCodeCheck = productGroup;
                        }

                        codeTextfield.init(codeTextfield.getProject(), codeTextfield.getDocumentationType(), seriesNumber, productGroupForCodeCheck, dateFrom, aa, iPartsGuiCodeTextField.CODE_TEST_TYPE.PRODUCTGRP_ONLY);
                        lastValidatedInput = null;
                        break;
                    }
                }
                codeTextfield.setText(cellText);
            }
        }
        return true;
    }

    @Override
    public boolean isModified(AbstractGuiControl inPlaceEditor, Object originalCellContent) {
        iPartsGuiCodeTextField codeTextfield = getCodeTextfield();
        if ((codeTextfield != null) && (inPlaceEditor == codeTextfield)) {
            String editedCode = codeTextfield.getTextCaseMode();
            String originalCode = getDBCellContentText(null, originalCellContent);
            if (!originalCode.equals(editedCode)) { // DataObject nur aktualisieren wenn der Code tatsächlich geändert wurde
                return true;
            }
        }
        return false;
    }

    @Override
    public String getEditResult(EtkDataObject dataObject) {
        iPartsGuiCodeTextField codeTextfield = getCodeTextfield();
        if (codeTextfield != null) {
            if (getFieldName().equals(iPartsConst.FIELD_K_CODES)) {
                return iPartsGuiCodeTextField.getConstCodesForEmptyASCodes(codeTextfield.getTextCaseMode(), dataObject, iPartsConst.FIELD_K_CODES_CONST);
            } else {
                return DaimlerCodes.beautifyCodeString(codeTextfield.getTextCaseMode());
            }
        }
        return null;
    }

    @Override
    public GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult validate() {
        iPartsGuiCodeTextField codeTextfield = getCodeTextfield();
        if (codeTextfield != null) {
            String currentInput = codeTextfield.getTextCaseMode();
            if ((lastValidatedInput != null) && lastValidatedInput.equals(currentInput)) {
                return GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult.UNMODIFIED;
            }
            lastValidatedInput = currentInput;
            if (codeTextfield.checkInput()) {
                return GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult.VALID;
            } else {
                return GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult.INVALID;
            }
        }
        return GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult.INVALID;
    }

    @Override
    public GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult validateForced() {
        lastValidatedInput = null;
        return validate();
    }

    @Override
    public GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult onValidationResult(GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult validationResult) {
        iPartsGuiCodeTextField codeTextfield = getCodeTextfield();
        if (validationResult.isInvalid() && (codeTextfield != null)) {
            // Bei fehlgeschlagener Validierung Fehlermeldung anzeigen; der inPlaceEditor bleibt offen
            String errorMessage = codeTextfield.getErrorMessage();
            if (StrUtils.isValid(errorMessage)) {
                MessageDialog.showError(errorMessage);
                codeTextfield.requestFocus();
            } else {
                String warning = codeTextfield.getWarningMessage();
                if (StrUtils.isValid(warning)) {
                    // Warnungen anzeigen, jedoch Valid zurückgeben (falls gefordert)
                    MessageDialog.showWarning(warning);
                    if (warningsAreValid) {
                        validationResult = GuiTableInPlaceEditorFactoryWithValidationInterface.ValidationResult.VALID;
                    } else {
                        codeTextfield.requestFocus();
                    }
                }
            }
        }
        return validationResult;
    }

    protected iPartsGuiCodeTextField getCodeTextfield() {
        if ((editorControl instanceof iPartsGuiCodeTextField)) {
            return (iPartsGuiCodeTextField)this.editorControl;
        }
        return null;
    }

    public Set<AssemblyId> saveAdditionalDataFromInplaceControl(EtkProject project, EtkDataPartListEntry partListEntry) {
        List<String> logMessages = new DwList<>();
        EditModuleHelper.updateCodeFieldsForPartListEntry(partListEntry.getFieldValue(getFieldName()),
                                                          partListEntry, true, logMessages);

        if (!logMessages.isEmpty()) {
            MessageDialog.showWarning(StrUtils.stringListToString(logMessages, "\n"), "!!Fehler im Codestring");
            return null;
        } else {
            return EditEqualizeFieldsHelper.doEqualizeForInplaceEditor(project, partListEntry);
        }
    }
}
