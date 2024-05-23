/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.FactoryDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.*;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBExtendedDataTypeProvider;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Helper für das Verarbeiten von EditControls
 */
public class iPartsEditUserControlsHelper implements iPartsConst {

    private static final String missingFactoryText = "!!nicht enthalten in Werke zu Produkt";
    private static final String noValidFactoryText = "!!Keine gültigen Werke zum Produkt gefunden";

    /**
     * SAA-/Baukastengültigkeiten sind oft abhängig von dem Input der Baumustergültigkeit. Beide Controls werden in der
     * Liste aller editControls gesucht. Sind beide vorhanden, erhält das Control für die Baumustergültigkeit einen
     * Listener, der das SAA-/BK-Control bei jeder Änderung mit Baumuszter versorgt.
     *
     * @param editControls
     * @param modelValidityFieldname
     * @param saaBkValidityFieldname
     */
    public static void connectModelAndSaaBkValidityControls(EditControls editControls, String modelValidityFieldname, String saaBkValidityFieldname) {
        EditControl modelValidityControl = findControlByFieldname(editControls, modelValidityFieldname);
        EditControl saaBkValidityControl = findControlByFieldname(editControls, saaBkValidityFieldname);

        if ((modelValidityControl == null) || (saaBkValidityControl == null)) {
            return;
        }
        iPartsGuiSAABkSelectTextField saaBkSelectTextField = null;
        if (saaBkValidityControl.getEditControl().getControl() instanceof iPartsGuiSAABkSelectTextField) {
            saaBkSelectTextField = (iPartsGuiSAABkSelectTextField)saaBkValidityControl.getEditControl().getControl();
        }

        iPartsGuiModelSelectTextField modelSelectTextField = null;
        if (modelValidityControl.getEditControl().getControl() instanceof iPartsGuiModelSelectTextField) {
            modelSelectTextField = (iPartsGuiModelSelectTextField)modelValidityControl.getEditControl().getControl();
        }
        if ((saaBkSelectTextField != null) && (modelSelectTextField != null)) {
            modelSelectTextField.assignRelatedSaaBkControl(saaBkSelectTextField);

        }
    }

    /**
     * Sucht in der Liste aller EditControls nach dem EditControl, dass zum übergebenen Feldnamen passt.
     *
     * @param editControls
     * @param fieldname
     * @return
     */
    public static EditControl findControlByFieldname(EditControls editControls, String fieldname) {
        for (EditControl control : editControls) {
            if (control.getEditControl().getFieldName().equals(fieldname)) {
                return control;
            }
        }
        return null;
    }

    /**
     * Modifiziert speziell für die Werkseinsatzdaten benötigte EditControls
     *
     * @param connector
     * @param specialFields
     * @param ctrl
     * @param initialValue
     */
    public static void modifyFactoryEditControl(AbstractJavaViewerFormIConnector connector, EditControl ctrl,
                                                Map<String, Object> specialFields, String initialValue) {
        String factoryFieldname = ctrl.getEditControl().getFieldName();
        if (specialFields.get(factoryFieldname) == null) {
            GuiComboBox<String> factoryControl = new GuiComboBox<>();
            Optional<iPartsDataAssembly> ownerAssembly = getAssemblyFromEntry(connector);
            if (ownerAssembly.isPresent()) {
                Set<String> productFactories = new TreeSet<>();
                if (ownerAssembly.get().isSAAssembly()) {
                    FactoryDataHelper.fillFactoriesForSaAssembly(connector.getProject(), ownerAssembly.get(), productFactories);
                } else {
                    FactoryDataHelper.fillFactoriesForProductAssembly(connector.getProject(), ownerAssembly.get(), productFactories);
                }
                if (!productFactories.isEmpty()) {
                    addValuesToFactoryControl(factoryControl, productFactories, initialValue);
                }
            }
            if (factoryControl.getItemCount() == 0) {
                factoryControl.addItem("", noValidFactoryText);
                factoryControl.setSelectedItem(noValidFactoryText);
            }
            ctrl.getEditControl().setControl(factoryControl);
            specialFields.put(factoryFieldname, ctrl);
        }
    }

    private static Optional<iPartsDataAssembly> getAssemblyFromEntry(AbstractJavaViewerFormIConnector connector) {
        Optional<iPartsDataPartListEntry> iPartsPartListEntry = getPartListEntryFromConnector(connector);
        return iPartsPartListEntry.map(iPartsDataPartListEntry::getOwnerAssembly);
    }

    /**
     * Fügt dem übergebenen <code>factoryControl</code> die Werke hinzu und setzt die Selektion
     *
     * @param factoryControl
     * @param productFactories
     * @param initialValue
     */
    private static void addValuesToFactoryControl(GuiComboBox<String> factoryControl, Set<String> productFactories, String initialValue) {
        factoryControl.addItems(productFactories);
        if (productFactories.contains(initialValue)) {
            factoryControl.setSelectedItem(initialValue);
        } else {
            if (!initialValue.isEmpty()) {
                // Aktuell gewähltes Werk ist nicht in den Werken zum Produkt enthalten
                // => Hinweistext ausgeben und speichern nicht zulassen
                String text = TranslationHandler.translate("!!%1 - %2", initialValue,
                                                           TranslationHandler.translate(missingFactoryText));
                factoryControl.addItem(initialValue, text);
                factoryControl.setSelectedItem(text);
            }
        }
    }

    private static Optional<iPartsDataPartListEntry> getPartListEntryFromConnector(AbstractJavaViewerFormIConnector connector) {
        if (connector instanceof RelatedInfoFormConnector) {
            EtkProject project = connector.getProject();
            EtkDataPartListEntry partListEntry = ((RelatedInfoFormConnector)connector).getRelatedInfoData().getAsPartListEntry(project);
            if (partListEntry instanceof iPartsDataPartListEntry) {
                return Optional.of((iPartsDataPartListEntry)partListEntry);
            }
        }
        return Optional.empty();
    }

    /**
     * Initialisiert das übergebene Werkseinsatzdatenobjekt mit den richtigen Parameter (Stücklistenpositionen- und Farbtabellen-Werkseinsatzdaten)
     *
     * @param dataObject
     * @param isNew
     * @param factoryDataTypes
     * @param adatFieldname
     * @param dataIdFieldname
     * @param sourceFieldname
     * @param statusFieldname
     */
    public static void prepareForFactoryDataEdit(EtkDataObject dataObject, boolean isNew, iPartsFactoryDataTypes factoryDataTypes,
                                                 String adatFieldname, String dataIdFieldname, String sourceFieldname,
                                                 String statusFieldname, Set<String> fieldNamesToModifyAfterEdit) {
        DBActionOrigin origin = DBActionOrigin.FROM_DB;
        if (fieldNamesToModifyAfterEdit == null) {
            fieldNamesToModifyAfterEdit = new HashSet<>();
            origin = DBActionOrigin.FROM_EDIT;
        }
        dataObject.setFieldValueAsDateTime(adatFieldname, Calendar.getInstance(), origin);
        fieldNamesToModifyAfterEdit.add(adatFieldname);
        if (!isNew && (iPartsImportDataOrigin.getTypeFromCode(dataObject.getFieldValue(sourceFieldname)) == iPartsImportDataOrigin.IPARTS)) {
            return;
        }
        if ((dataIdFieldname != null) && (factoryDataTypes != null)) {
            dataObject.setFieldValue(dataIdFieldname, factoryDataTypes.getDbValue(), origin);
            fieldNamesToModifyAfterEdit.add(dataIdFieldname);
        }
        dataObject.setFieldValue(sourceFieldname, iPartsImportDataOrigin.IPARTS.getOrigin(), origin);
        fieldNamesToModifyAfterEdit.add(sourceFieldname);

        if (isNew) {
            dataObject.setFieldValue(statusFieldname, iPartsDataReleaseState.RELEASED.getDbValue(), origin);
            fieldNamesToModifyAfterEdit.add(statusFieldname);
        }
    }

    public static void prepareForFactoryDataEdit(EtkDataObject dataObject, boolean isNew, iPartsFactoryDataTypes factoryDataTypes,
                                                 String adatFieldname, String dataIdFieldname, String sourceFieldname,
                                                 String statusFieldname) {
        prepareForFactoryDataEdit(dataObject, isNew, factoryDataTypes, adatFieldname, dataIdFieldname, sourceFieldname,
                                  statusFieldname, null);
    }

    /**
     * Passt in Abhängigkeit des Initialwertes die Eigenschaften und den Inhalt der Doku-Relevanz Combobox an
     *
     * @param ctrl
     * @param initialValue
     */
    public static void handleDocuRelevantControl(EtkProject project, EditControl ctrl, String initialValue) {
        if (ctrl.getEditControl().getControl() instanceof EnumComboBox) {
            EnumComboBox enumComboBox = (EnumComboBox)ctrl.getEditControl().getControl();
            removeValueFromComboBox(enumComboBox, "", iPartsDocuRelevant.DOCU_DOCUMENTED.getDisplayValue(project),
                                    iPartsDocuRelevant.DOCU_DOCUMENTED_IN_AUTHOR_ORDER.getDisplayValue(project));
            if (initialValue.isEmpty()) {
                enumComboBox.setActToken(iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED.getDbValue());
            }
        } else if (ctrl.getEditControl().getControl() instanceof EnumRComboBox) {
            // Kopierter Part, der wieder entfernt werden kann, wenn GuiCombobox und RCombobox zusammengeführt wurden
            EnumRComboBox enumComboBox = (EnumRComboBox)ctrl.getEditControl().getControl();
            removeValueFromRComboBox(enumComboBox, "", iPartsDocuRelevant.DOCU_DOCUMENTED.getDisplayValue(project),
                                     iPartsDocuRelevant.DOCU_DOCUMENTED_IN_AUTHOR_ORDER.getDisplayValue(project));
            if (initialValue.isEmpty()) {
                enumComboBox.setActToken(iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED.getDbValue());
            }
        }
    }

    /**
     * Entfernt die beiden Werte PSK_PKW, PSK_TRUCK, DIALOG und DIALOG iParts aus der Combobox für den Dokumentationstyp, da diese
     * Werte für SAs nicht zulässig sind.
     *
     * @param ctrl
     */
    public static void modifyDocuRelevantControlForSA(EditControl ctrl) {
        if (ctrl != null) {
            AbstractGuiControl abstractGuiControl = ctrl.getAbstractGuiControl();
            if (abstractGuiControl instanceof EnumComboBox) {
                EnumComboBox docuTypeBox = (EnumComboBox)(abstractGuiControl);
                removeUserObjectFromCombobox(docuTypeBox, iPartsDocumentationType.DIALOG.getDBValue());
                removeUserObjectFromCombobox(docuTypeBox, iPartsDocumentationType.DIALOG_IPARTS.getDBValue());
                removeUserObjectFromCombobox(docuTypeBox, iPartsDocumentationType.PSK_PKW.getDBValue());
                removeUserObjectFromCombobox(docuTypeBox, iPartsDocumentationType.PSK_TRUCK.getDBValue());
            } else if (abstractGuiControl instanceof EnumRComboBox) {
                EnumRComboBox docuTypeBox = (EnumRComboBox)(abstractGuiControl);
                removeUserObjectFromRCombobox(docuTypeBox, iPartsDocumentationType.DIALOG.getDBValue());
                removeUserObjectFromRCombobox(docuTypeBox, iPartsDocumentationType.DIALOG_IPARTS.getDBValue());
                removeUserObjectFromRCombobox(docuTypeBox, iPartsDocumentationType.PSK_PKW.getDBValue());
                removeUserObjectFromRCombobox(docuTypeBox, iPartsDocumentationType.PSK_TRUCK.getDBValue());
            }
        }
    }

    public static void handleDIALOGStatusControl(EditControl ctrl, String initialValue) {
        if (ctrl.getEditControl().getControl() instanceof EnumComboBox) {
            EnumComboBox enumComboBox = (EnumComboBox)ctrl.getEditControl().getControl();
            removeValueFromComboBox(enumComboBox, "");
            removeTokensFromComboBox(enumComboBox, iPartsDataReleaseState.getNotEditRelevantReleaseStatesDBValues());
            if (initialValue.isEmpty()) {
                enumComboBox.setActToken(iPartsDataReleaseState.NEW.getDbValue());
            }
        } else if (ctrl.getEditControl().getControl() instanceof EnumRComboBox) {
            // Kopierter Part, der wieder entfernt werden kann, wenn GuiCombobox und RCombobox zusammengeführt wurden
            EnumRComboBox enumComboBox = (EnumRComboBox)ctrl.getEditControl().getControl();
            removeValueFromRComboBox(enumComboBox, "");
            removeTokensFromRComboBox(enumComboBox, iPartsDataReleaseState.getNotEditRelevantReleaseStatesDBValues());
            if (initialValue.isEmpty()) {
                enumComboBox.setActToken(iPartsDataReleaseState.NEW.getDbValue());
            }
        }
    }

    private static void removeValueFromComboBox(EnumComboBox enumComboBox, String... values) {
        for (String value : values) {
            if (enumComboBox.getIndexOfItem(value) > -1) {
                enumComboBox.removeItem(value);
            }
        }
    }

    private static void removeValueFromRComboBox(EnumRComboBox enumComboBox, String... values) {
        for (String value : values) {
            if (enumComboBox.getIndexOfItem(value) > -1) {
                enumComboBox.removeItem(value);
            }
        }
    }

    private static void removeTokensFromComboBox(EnumComboBox enumComboBox, List<String> tokens) {
        for (String token : tokens) {
            int index = enumComboBox.getIndexByToken(token);
            if (index > -1) {
                enumComboBox.removeItem(index);
            }
        }
    }

    private static void removeTokensFromRComboBox(EnumRComboBox enumComboBox, List<String> tokens) {
        for (String token : tokens) {
            int index = enumComboBox.getIndexByToken(token);
            if (index > -1) {
                enumComboBox.removeItem(index);
            }
        }
    }

    private static void removeUserObjectFromRCombobox(EnumRComboBox comboBox, String userObjectDbValue) {
        int index = -1;
        for (int i = 0; i < comboBox.getUserObjects().size(); i++) {
            if (comboBox.getUserObject(i).equals(userObjectDbValue)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            comboBox.removeItem(index);
        }
    }

    private static void removeUserObjectFromCombobox(EnumComboBox comboBox, String userObjectDbValue) {
        int index = -1;
        for (int i = 0; i < comboBox.getUserObjects().size(); i++) {
            if (comboBox.getUserObject(i).equals(userObjectDbValue)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            comboBox.removeItem(index);
        }
    }

    /**
     * Spezielle Prüfungen der PEM-Kenner an einer Stücklistenposition nach dem manuellen Edit
     *
     * @param partListEntryForEdit
     * @param editContext
     * @param oldEvalPemFrom
     * @param oldEvalPemTo
     */
    public static void handlePemFlagsAfterEdit(EtkDataPartListEntry partListEntryForEdit, iPartsRelatedInfoEditContext editContext, boolean oldEvalPemFrom, boolean oldEvalPemTo) {
        // Hat sich "PEM ab/bis auswerten" geändert?
        if ((oldEvalPemFrom != partListEntryForEdit.getFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_FROM))
            || (oldEvalPemTo != partListEntryForEdit.getFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_TO))) {
            // "PEM ab/bis auswerten an den Werkseinsatzdaten des Stücklisteneintrags setzen
            if (partListEntryForEdit instanceof iPartsDataPartListEntry) {
                // Aktualisiert auch die "PEM ab/bis auswerten"-Flags an den Werkseinsatzdaten falls notwendig
                ((iPartsDataPartListEntry)partListEntryForEdit).updatePEMFlagsFromReplacements();
            }

            if (editContext != null) {
                editContext.setUpdateRetailFactoryData(true);
                editContext.setFireDataChangedEvent(true);
            }
        }
    }

    /**
     * Spezielle Prüfungen der Code an einer Stücklistenposition nach dem manuellen Edit
     *
     * @param partListEntryForEdit
     */
    public static void handleCodeFieldAfterEdit(EtkDataPartListEntry partListEntryForEdit) {
        if (partListEntryForEdit.getAttribute(iPartsConst.FIELD_K_CODES).isModified()) { // Code-Regel verändert?
            String codeString = iPartsGuiCodeTextField.getConstCodesForEmptyASCodes(partListEntryForEdit.getFieldValue(iPartsConst.FIELD_K_CODES),
                                                                                    partListEntryForEdit, iPartsConst.FIELD_K_CODES_CONST);
            partListEntryForEdit.setFieldValue(iPartsConst.FIELD_K_CODES, codeString, DBActionOrigin.FROM_EDIT);

            // Alle Code-Felder in der DB für den Stücklisteneintrag VOR der Serialisierung aktualisieren
            List<String> logMessages = new DwList<>();
            boolean oldLogLoadFieldIfNeeded = partListEntryForEdit.isLogLoadFieldIfNeeded();
            partListEntryForEdit.setLogLoadFieldIfNeeded(false);
            try {
                EditModuleHelper.updateCodeFieldsForPartListEntry(codeString, partListEntryForEdit, false, logMessages);
            } finally {
                partListEntryForEdit.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
            if (!logMessages.isEmpty()) {
                MessageDialog.showWarning(StrUtils.stringListToString(logMessages, "\n"), "!!Fehler im Codestring");
            }
        }
    }

    /**
     * Zurücksetzen bestimmter virtueller Felder an einer Stücklistenposition
     *
     * @param partlistEntry
     */
    public static void clearVirtualFieldsForReload(EtkDataPartListEntry partlistEntry) {
        // Kombinierten Text, virtuelle Ereignis-Felder sowie virtuelle Code-Felder am aktuellen Stücklisteneintrag
        // zurücksetzen und dadurch neu laden
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_TITLE_FROM);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_TITLE_TO);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_CODE_FROM);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_EVENT_CODE_TO);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_REDUCED);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_FILTERED);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_WITH_EVENTS);
        partlistEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_CHANGED_CODE);
    }

    /**
     * Überprüft, das übergeben {@link iPartsGuiCodeTextField} Code-Feld und liefert eine Fehlermeldung, wenn der
     * Code nicht valide ist.
     *
     * @param codeTextField
     * @return
     */
    public static boolean checkCodeFieldWithErrorMessage(iPartsGuiCodeTextField codeTextField) {
        if (codeTextField != null) {
            return codeTextField.checkInputWithErrorMessage();
        }
        return true;
    }

    public static boolean checkMaterialFieldWithErrorMessage(iPartsGuiMaterialSelectTextField materialTextField) {
        if (materialTextField != null) {
            if (!materialTextField.checkInput()) {
                String errorMessage = materialTextField.getErrorMessage();
                String warningMessage = materialTextField.getWarningMessage();
                if (StrUtils.isValid(errorMessage)) {
                    MessageDialog.showError(errorMessage);
                    return false;
                } else {
                    if (StrUtils.isValid(warningMessage)) {
                        // Warnung anzeigen, jedoch kein Fehler
                        MessageDialog.showWarning(warningMessage);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sucht das {@link iPartsGuiCodeTextField} in den übergebenen {@link EditControls} und liefert es zurück
     *
     * @param index
     * @param editControls
     * @return
     */
    public static iPartsGuiCodeTextField getCodeFieldFromEditControls(int index, EditControls editControls) {
        if ((index != -1) && (editControls != null) && (editControls.size() > 0)) {
            EditControl ctrl = editControls.getControlByFeldIndex(index);
            if ((ctrl != null) && (ctrl.getEditControl().getControl() != null)) {
                if (ctrl.getEditControl().getControl() instanceof iPartsGuiCodeTextField) {
                    return (iPartsGuiCodeTextField)ctrl.getEditControl().getControl();
                }
            }
        }
        return null;
    }

    /**
     * Durchläuft die übergebenen {@link EtkEditFields} und liefert die Felder zurück, die nicht virtuell oder virtuell
     * und editierbar sind.
     *
     * @param editFields
     * @param unifySource
     * @return
     */
    public static EtkEditFields getEditFieldsWithoutNonEditableEditFields(EtkEditFields editFields,
                                                                          EditUserMultiChangeControls.UnifySource unifySource) {
        EtkEditFields result = new EtkEditFields();
        if ((editFields != null) && (editFields.size() > 0)) {
            for (EtkEditField editField : editFields.getFields()) {
                if (editField.getKey().isVirtualKey()) {
                    // Wenn das Vereinheitlichen nicht aus AS oder Konstruktion kommt, dann dürfen virtuelle
                    // Felder für den Edit nicht angeboten werden
                    if (unifySource == EditUserMultiChangeControls.UnifySource.OTHER) {
                        continue;
                    } else if ((unifySource == EditUserMultiChangeControls.UnifySource.CONSTURCTION) && !iPartsDataVirtualFieldsDefinition.editableVirtualFieldsForConst.contains(editField.getKey().getFieldName())) {
                        continue;
                    } else if ((unifySource == EditUserMultiChangeControls.UnifySource.AFTERSALES) && !iPartsDataVirtualFieldsDefinition.editableVirtualFieldsForAS.contains(editField.getKey().getFieldName())) {
                        continue;
                    }
                }
                result.addFeld(editField);
            }
        }
        return result;
    }

    /**
     * Überträgt die Werte in das übergebene Attribute. Wenn das Attribute this.attribute ist, dann werden die ursprünglichen Werte überschrieben und das modified-Flag
     * kann nicht mehr ermittelt werden. Deshalb immer mit einem neuen Attribut aufrufen, außer beim ecten Abspeichern des Datensatzes this.attribute verwenden
     *
     * @param controlByFeldIndex
     * @param field
     * @param attrib
     */
    public static void fillAttribByEditControlValue(EtkProject project, EditControl controlByFeldIndex, EtkEditField field,
                                                    DBDataObjectAttribute attrib, String tableName, IdWithType id) {
        if (controlByFeldIndex == null) {
            return;
        }

        EditControlFactory ctrl = controlByFeldIndex.getEditControl();
        if (field.isMultiLanguage()) {
            EtkMultiSprache multi = ctrl.getMultiLangText();
            //DBDataObjectAttribute enthält keine Sprachen mit leeren Texten
            EtkMultiSprache newMulti = new EtkMultiSprache();
            newMulti.setTextId(multi.getTextId());

            // Die Sprachen, die in dem ursprünglichen MultiSprachFeld vorhanden waren
            // Um einen schon vorhandenen Spracheintrag zu löschen muss der Text auf leer gesetzt werden
            // Die Logik beim Speichern ist:
            // Sprache hat eine Übersetzung -> wird als neue Übersetzung gespeichert
            // Sprache in der vorherigen Version vorhanden, jetzt leer -> leere Übersetzung wird abgespeichert
            // Sprache in der vorherigen Version vorhanden, jetzt nicht vorhanden -> Übersetzung bleibt die Alte
            // Sprache in der vorherigen Version nicht vorhanden, jetzt leer -> Übersetzung wird nicht gespeichert
            //
            // Deshalb muss unterschieden werden, ob die Sprache in ursprünglichen Multisprache vorhanden oder gar nicht vorhanden ist.
            // Falls eine leer Übersetzung drin ist, so gilt das auch als gefüllte Sprache

            Set<String> originLanguages = attrib.getAsMultiLanguage(null, false).getSprachen();
            for (Map.Entry<String, String> languageEntry : multi.getLanguagesAndTexts().entrySet()) {
                String text = languageEntry.getValue();
                if (originLanguages.contains(languageEntry.getKey())) {
                    // Die Sprache war ursprünglich auch schon vorhanden -> Übernehemen, egal ob die Übersetzung leer ist
                    // Falls die Übersetzung leer ist, dann bedeutet das, dass die Übersetzung dieser Sprache gelöscht wurde
                    newMulti.setText(languageEntry.getKey(), text);
                } else {
                    // Die Übersetzung ist leer und die Sprache existierte vorher auch noch nicht -> Diese Sprache wird nicht abgespeichert sondern bleibt einfach leer
                    if (!text.isEmpty()) {
                        newMulti.setText(languageEntry.getKey(), text);
                    }

                }
            }
            String textNr = attrib.getMultiLanguageTextNr();
            String textId = attrib.getMultiLanguageTextId();
            if (!textNr.isEmpty() && textId.isEmpty()) { // Altes EtkMultiSprache aus der DB laden, damit der Vergleich in setValueAsMultiLanguage() korrekt funktioniert
                DBExtendedDataTypeProvider tempLanguageProvider = EtkDataObject.getTempExtendedDataTypeProvider(project, field.getKey().getTableName());
                attrib.getAsMultiLanguage(tempLanguageProvider, true);
            }
            attrib.setValueAsMultiLanguage(newMulti, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        } else if (field.isArray()) {
            EtkDataArray dataArray = ctrl.getArray();
            if (!dataArray.isEmpty()) {
                String arrayId = attrib.getArrayId();
                if (arrayId.isEmpty()) { // Neue Array-ID erzeugen und im Attribut setzen
                    // Im Multi-Edit kann es vorkommen, dass keine valide ID benutzt wird
                    String startValue = (id != null) ? id.toString("|") : "";
                    arrayId = project.getDbLayer().getNewArrayNo(TableAndFieldName.make(tableName, attrib.getName()), startValue, false);
                    attrib.setIdForArray(arrayId, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                } else { // Altes EtkDataArray aus der DB laden, damit der Vergleich in setValueAsArray() korrekt funktioniert
                    DBExtendedDataTypeProvider tempArrayProvider = EtkDataObject.getTempExtendedDataTypeProvider(project, field.getKey().getTableName());
                    attrib.getAsArray(tempArrayProvider);
                }

                dataArray.setArrayId(arrayId);
                attrib.setValueAsArray(dataArray, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            } else {
                attrib.setIdForArray("", DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                attrib.setValueAsArray(null, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            }
        } else {
            String value = ctrl.getText();
            if (ctrl.getField().getType() == EtkFieldType.feSetOfEnum) {
                // bei SetOfEnum darf die Reihenfolge der Tokens keine Rolle spielen
                // gespeichert wird SetOfEnum als TYPE.STRING
                if (SetOfEnumDataType.isValidSetOfEnumTag(value) && SetOfEnumDataType.isValidSetOfEnumTag(attrib.getAsString())) {
                    List<String> tokens = SetOfEnumDataType.parseSetofEnum(value, false, true);
                    List<String> currentTokens = SetOfEnumDataType.parseSetofEnum(attrib.getAsString(), false, true);
                    if (tokens.size() != currentTokens.size()) {
                        attrib.setValueAsString(value, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                    } else if (!tokens.isEmpty()) {
                        if (!tokens.containsAll(currentTokens)) {
                            attrib.setValueAsString(value, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                        }
                    }
                } else {
                    attrib.setValueAsString(value, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                }
            } else {
                attrib.setValueAsString(value, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            }
        }
    }

    /**
     * Fügt fehlende Attribute aus dem übergebenen {@link EtkRecord} zu den übergebenen <code>attributes</code> hinzu
     *
     * @param rec
     * @param attributes
     * @param tableName
     * @param project
     */
    public static void setMissingAttributes(EtkRecord rec, DBDataObjectAttributes attributes, String tableName, EtkProject project) {
        if (rec != null) {
            DBDataObjectAttributes loadedAttributes = DBDataObjectAttributes.getFromRecord(rec, DBActionOrigin.FROM_DB);
            // evtl. fehlende Attribute ergänzen
            EtkDatabaseTable table = null;
            for (Map.Entry<String, DBDataObjectAttribute> loadedAttributeEntry : loadedAttributes.entrySet()) {
                if (!attributes.containsKey(loadedAttributeEntry.getKey())) {
                    DBDataObjectAttribute attribute = loadedAttributeEntry.getValue();

                    // Korrekten Typ (MultiLang oder Array setzen)
                    if (table == null) {
                        table = project.getConfig().getDBDescription().findTable(tableName);
                    }
                    if (table != null) {
                        EtkDatabaseField dbField = table.getField(loadedAttributeEntry.getKey());
                        if (dbField != null) {
                            if (dbField.isMultiLanguage()) {
                                attribute.setTextNrForMultiLanguage(attribute.getAsString(), DBActionOrigin.FROM_DB);
                            } else if (dbField.isArray()) {
                                attribute.setIdForArray(attribute.getAsString(), DBActionOrigin.FROM_DB);
                            }
                        }
                    }

                    attributes.addField(attribute, DBActionOrigin.FROM_DB);
                }
            }
        }
    }

    /**
     * Gibt eine Meldung aus, wenn übergebene Felder leer waren
     *
     * @param emptyFields
     * @return
     */
    public static boolean handleEmptyFields(List<String> emptyFields) {
        if (!emptyFields.isEmpty()) {
            if (emptyFields.size() > 1) {
                emptyFields.add(0, TranslationHandler.translate("!!Die folgenden Felder dürfen nicht leer sein:"));
            } else {
                emptyFields.add(0, TranslationHandler.translate("!!Das folgende Feld darf nicht leer sein:"));
            }
            MessageDialog.showError(emptyFields);
            return false;
        }
        return true;
    }

    /**
     * Modifiziert spezielle GuiControls für die Bearbeitung/Neuanlage von Baureihen
     *
     * @param project
     * @param ctrl
     * @param field
     * @param initialValue
     * @param parentForm
     * @param id
     * @param listener
     */
    public static void modifySeriesFormEditControls(EtkProject project, EditControl ctrl, EtkEditField field, String initialValue, AbstractJavaViewerForm parentForm, IdWithType id, EventListener listener) {
        if (field.getKey().getTableName().equals(TABLE_DA_SERIES)) {
            String fieldname = field.getKey().getFieldName();
            String series = id.getValue(1);
            iPartsSeriesId seriesId = new iPartsSeriesId(series);
            if (fieldname.equals(MasterDataSeriesForm.FIELD_DS_SOP)) {
                if (ctrl.getEditControl().getControl() instanceof iPartsGuiSeriesSOPField) {
                    iPartsGuiSeriesSOPField seriesSOPField = (iPartsGuiSeriesSOPField)ctrl.getEditControl().getControl();
                    seriesSOPField.init(parentForm, seriesId);
                    if (initialValue.isEmpty()) {
                        seriesSOPField.setSelectedIndex(1);
                    } else {
                        seriesSOPField.setActToken(initialValue);
                    }
                    if (listener != null) {
                        seriesSOPField.addEventListener(listener);
                    }
                }
            } else if (fieldname.equals(MasterDataSeriesForm.FIELD_DS_AA_WO_FACTORY_DATA)) {
                if (ctrl.getEditControl().getControl() instanceof iPartsGuiSeriesAACheckComboBox) {
                    iPartsGuiSeriesAACheckComboBox seriesAACheckComboBox = (iPartsGuiSeriesAACheckComboBox)ctrl.getEditControl().getControl();
                    seriesAACheckComboBox.fillSeriesAAEnumValues(project, seriesId, TABLE_DA_SERIES, fieldname);
                    seriesAACheckComboBox.setActToken(initialValue);
                }
            }
        }
    }

    /**
     * Sammelt die editierten Werte von GuiControls für die Bearbeitung/Neuanlage von Baureihen
     *
     * @param project
     * @param attributes
     * @param editFields
     * @param editControls
     * @param id
     */
    public static void collectEditValuesForSeriesForm(EtkProject project, DBDataObjectAttributes attributes,
                                                      EtkEditFields editFields, EditControls editControls, IdWithType id) {
        int index = 0;
        for (EtkEditField field : editFields.getVisibleEditFields()) {
            if (!field.isEditierbar()) { // Nicht editierbare Felder ignorieren
                index++;
                continue;
            }
            if (field.getKey().getFieldName().equals(MasterDataSeriesForm.FIELD_DS_SOP)) {
                index++;
                continue;
            }
            DBDataObjectAttribute attrib = attributes.getField(field.getKey().getFieldName(), false);
            if (attrib == null) {
                if (!VirtualFieldsUtils.isVirtualField(field.getKey().getFieldName())) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR,
                                                       new RuntimeException("DBDataAttribute for field \"" + field.getKey().getFieldName()
                                                                            + "\" not found!"));
                }
            } else {
                EditControlFactory ctrl = editControls.getControlByFeldIndex(index).getEditControl();
                String value = null;
                if (ctrl != null) {
                    value = ctrl.getTextFromExtraControls();
                }
                if (value != null) {
                    attrib.setValueAsString(value, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                } else {
                    EditControl controlByFeldIndex = editControls.getControlByFeldIndex(index);
                    fillAttribByEditControlValue(project, controlByFeldIndex, field, attrib, TABLE_DA_SERIES, id);
                }
            }
            index++;
        }
    }
}
