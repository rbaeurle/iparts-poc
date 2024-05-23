/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsFactoryDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PEMDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoFactoryDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.misc.id.IdWithType;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * UserControl zum Bearbeiten von Werkseinsatzdaten (DAIMLER-4827)
 * <p>
 * Regeln:
 * Werksnummer muss aus den zum Produkt hinterlegten Werksnummern ausgewählt werden (DA_PRODUCT_FACTORY)
 * Als PEM muss eine echte PEM-Nummer oder ein "Dummy" manuell eingegeben werden
 * PEM Termin kann echter Wert oder leer sein
 * PEM Termin ab <= PEM Termin bis
 * Steuercode sind optional
 */
public class EditUserControlsForFactoryData extends AbstractEditUserControlsForFactoryData {

    private static final String[] readOnlyFieldNames = new String[]{ FIELD_DFD_ADAT, FIELD_DFD_SEQ_NO, FIELD_DFD_SOURCE, FIELD_DFD_STATUS };
    private static final String[] mustHaveValueFieldNames = new String[]{};
    private static final String[] invisibleFieldNames = new String[]{};
    private static final String[] allowedEmptyPKFields = new String[]{ FIELD_DFD_SPKZ, FIELD_DFD_SEQ_NO };

    public static iPartsDataFactoryData showCreateFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                              EtkDataPartListEntry partListEntry, iPartsDataFactoryDataList factoryDataList,
                                                              boolean isReadOnly) {
        return showEditFactoryData(dataConnector, parentForm, partListEntry, (iPartsFactoryDataId)null, factoryDataList, isReadOnly);
    }

    public static iPartsDataFactoryData showEditFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                            EtkDataPartListEntry partListEntry, iPartsFactoryDataId externFactoryDataId, iPartsDataFactoryDataList factoryDataList,
                                                            boolean isReadOnly) {
        iPartsDataFactoryData factoryData = null;
        if (externFactoryDataId != null) {
            factoryData = new iPartsDataFactoryData(dataConnector.getProject(), externFactoryDataId);
            if (!factoryData.existsInDB()) {
                factoryData = null;
            }
        }
        return showEditFactoryData(dataConnector, parentForm, partListEntry, factoryData, factoryDataList, isReadOnly);
    }

    public static iPartsDataFactoryData showEditFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                            EtkDataPartListEntry partListEntry, iPartsDataFactoryData factoryData, iPartsDataFactoryDataList factoryDataList,
                                                            boolean isReadOnly) {

        String tableName = TABLE_DA_FACTORY_DATA;
        EtkProject project = dataConnector.getProject();
        boolean isReadOnlyTemp = isReadOnly;
        // Ist der Edit aktiv und die Position zu der die Werksdaten gehören gesperrt, dann dürfen die Werksdaten nicht
        // bearbeitet werden
        if (!isReadOnlyTemp) {
            isReadOnlyTemp = iPartsLockEntryHelper.isLockedWithDBCheck(partListEntry);
        }
        String text;
        boolean isNewForm = false;
        boolean useExistingData = false;
        if (factoryData == null) {
            iPartsDataAssembly ownerAssembly = (iPartsDataAssembly)partListEntry.getOwnerAssembly();
            // DIALOG
            if (ownerAssembly.getDocumentationType().isPKWDocumentationType()) {
                iPartsDialogBCTEPrimaryKey dialogBCTEPrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
                if (dialogBCTEPrimaryKey != null) {
                    String dialogGUID = dialogBCTEPrimaryKey.createDialogGUID();
                    iPartsFactoryDataId factoryDataId = new iPartsFactoryDataId(dialogGUID, "");
                    factoryData = new iPartsDataFactoryData(project, factoryDataId);
                    factoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    factoryData.setFieldsFromBCTEKEYAndPartListEntry(partListEntry);
                } else {
                    // obwohl DokuTyp == DIALOG, kein BCTE-Key hinterlegt
                    // => tu so, als ob es Truck wäre
                    iPartsFactoryDataId factoryDataId = iPartsDataFactoryData.getFactoryDataIDForNonDIALOGFromPartListEntry(partListEntry, factoryDataList);
                    factoryData = new iPartsDataFactoryData(project, factoryDataId);
                    factoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
            } else {
                // ELDAS
                iPartsFactoryDataId factoryDataId = iPartsDataFactoryData.getFactoryDataIDForNonDIALOGFromPartListEntry(partListEntry, factoryDataList);
                factoryData = new iPartsDataFactoryData(project, factoryDataId);
                factoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }

            isNewForm = true;
            text = "!!Werkseinsatzdaten erstellen";
        } else {
            useExistingData = factoryData.getSource() == iPartsImportDataOrigin.IPARTS;
            text = "!!Werkseinsatzdaten bearbeiten";
        }

        Optional<PEMDataHelper.PEMDataOrigin> pemDataOrigin = Optional.empty();
        iPartsDataAssembly ownerAssembly = (iPartsDataAssembly)partListEntry.getOwnerAssembly();
        // Eingrenzung nur bei Truck relevant
        if (ownerAssembly.getDocumentationType().isTruckDocumentationType()) {
            if (ownerAssembly.isSAAssembly()) {
                // Bei einer freien SA werden alle Produktverwendungen geprüft. Ist sie in keinem Produkt verortet, können
                // keine Werksdaten angezeigt werden. Falls mind. ein Produkt existiert, wird bei freien SAs die SA selber
                // zum Erzeugen der IZV PEMs verwendet.
                pemDataOrigin = retrievePemDataOriginForSa(project, ownerAssembly);
            } else {
                // Bei ELDAS wird geprüft, ob das Produkt zur Stückliste existiert. Falls ja, wird dieses Produkt zum
                // Erzeugen von neuen PEMs verwendet.
                pemDataOrigin = retrievePemDataOriginForELDAS(ownerAssembly);
            }
            if (!pemDataOrigin.isPresent()) {
                isReadOnlyTemp = true;
            }
        }

        if (isReadOnlyTemp) {
            text = "!!Werkseinsatzdaten anzeigen";
        }

        Set<String> fieldNamesToModifyAfterEdit = new HashSet<>();
        if (ownerAssembly.getDocumentationType().isPKWDocumentationType()) {
            iPartsEditUserControlsHelper.prepareForFactoryDataEdit(factoryData, isNewForm, iPartsFactoryDataTypes.FACTORY_DATA_AS,
                                                                   FIELD_DFD_ADAT, FIELD_DFD_DATA_ID, FIELD_DFD_SOURCE, FIELD_DFD_STATUS,
                                                                   fieldNamesToModifyAfterEdit);
        } else {
            iPartsEditUserControlsHelper.prepareForFactoryDataEdit(factoryData, isNewForm, null,
                                                                   FIELD_DFD_ADAT, null, FIELD_DFD_SOURCE, FIELD_DFD_STATUS,
                                                                   fieldNamesToModifyAfterEdit);
        }
        EtkEditFields editFields = modifyEditFields(project, tableName);
        EditUserControlsForFactoryData eCtrl = new EditUserControlsForFactoryData(dataConnector, parentForm, tableName,
                                                                                  factoryData.getAsId(),
                                                                                  factoryData.getAttributes(),
                                                                                  editFields, isNewForm,
                                                                                  pemDataOrigin.orElse(null));
        eCtrl.setMainTitle(text);
        eCtrl.setReadOnly(isReadOnlyTemp);
        setTitleWithEvalPEM(eCtrl, partListEntry);
        ModalResult modalResult = eCtrl.showModal();
        iPartsDataFactoryData result = null;
        if (modalResult == ModalResult.OK) {
            if (useExistingData) {
                // Weil das EditControl nur auf den Attributes arbeitet und wir die Beziehung zur alten ID nicht verlieren wollen,
                // muss hier die ID aus den bearbeiteten Attributes heraus synchronisiert werden.
                if (fieldNamesToModifyAfterEdit.contains(FIELD_DFD_ADAT)) {
                    setModifyFlag(factoryData, FIELD_DFD_ADAT);
                }
                factoryData.updateIdFromPrimaryKeys();
                result = factoryData;
            } else {
                result = new iPartsDataFactoryData(project, factoryData.getAsId());
                result.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
                for (String fieldName : fieldNamesToModifyAfterEdit) {
                    setModifyFlag(result, fieldName);
                }
            }
        }
        return result;
    }

    /**
     * Erzeugt ein {@link PEMDataHelper.PEMDataOrigin} für Positionen in einer ELDAS Stückliste
     *
     * @param ownerAssembly
     * @return
     */
    private static Optional<PEMDataHelper.PEMDataOrigin> retrievePemDataOriginForELDAS(iPartsDataAssembly ownerAssembly) {
        iPartsProductId productId = ownerAssembly.getProductIdFromModuleUsage();
        if (productId == null) {
            // Falls bei einem ELDAS-Modul kein Produkt bestimmt werden konnte, dann soll keine Bearbeitung möglich sein.
            MessageDialog.showError(TranslationHandler.translate("!!Fehler bei der Bestimmung des Produkts für TU \"%1\". Werkseinsatzdaten-Bearbeitung ist nicht möglich.",
                                                                 ownerAssembly.getAsId().getKVari()));
            return Optional.empty();
        } else {
            return Optional.of(new PEMDataHelper.PEMDataOrigin(productId.getProductNumber()));
        }
    }

    /**
     * Erzeugt ein {@link PEMDataHelper.PEMDataOrigin} für Positionen in einer freien-SA Stückliste
     *
     * @param project
     * @param ownerAssembly
     * @return
     */
    private static Optional<PEMDataHelper.PEMDataOrigin> retrievePemDataOriginForSa(EtkProject project, iPartsDataAssembly ownerAssembly) {
        iPartsDataProductSAsList productSAsList = iPartsDataProductSAsList.loadProductSasForSaAssembly(project, ownerAssembly);
        if (productSAsList.isEmpty()) {
            // Falls bei einem frei SA-Modul kein Produkt bestimmt werden konnte, dann soll keine Bearbeitung möglich sein.
            MessageDialog.showError(TranslationHandler.translate("!!Fehler bei der Bestimmung eines Produkts für SA-TU \"%1\". Werkseinsatzdaten-Bearbeitung ist nicht möglich.",
                                                                 ownerAssembly.getAsId().getKVari()));
            return Optional.empty();
        } else {
            // Da alle Produkte zu einer SA geladen wurden, kann hier der erste Treffer zum bestimmen der SA herangezogen werden
            return Optional.of(new PEMDataHelper.PEMDataOrigin(productSAsList.get(0).getAsId().getSaNumber()));
        }
    }

    private static void setModifyFlag(EtkDataObject dataObject, String fieldName) {
        DBDataObjectAttribute attrib = dataObject.getAttribute(fieldName, false);
        if ((attrib != null) && !attrib.isModified()) {
            ModiDBDataObjectAttribute modifiedAttrib = new ModiDBDataObjectAttribute(attrib);
            attrib.assign(modifiedAttrib);
        }
    }

    private static class ModiDBDataObjectAttribute extends DBDataObjectAttribute {

        public ModiDBDataObjectAttribute(DBDataObjectAttribute source) {
            super(source);
            setModified(true);
        }
    }

    private static void setTitleWithEvalPEM(EditUserControlsForFactoryData eCtrl, EtkDataPartListEntry partListEntry) {
        boolean evalPemFrom = false;
        boolean evalPemTo = false;
        if (partListEntry instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
            evalPemFrom = iPartsPartListEntry.isPEMFromRelevant();
            evalPemTo = iPartsPartListEntry.isPEMToRelevant();
        }

        String yes = TranslationHandler.translate("!!Ja");
        String no = TranslationHandler.translate("!!Nein");
        String evalPemFromStr = evalPemFrom ? yes : no;
        String evalPemToStr = evalPemTo ? yes : no;

        eCtrl.setTitle(TranslationHandler.translate("!!(Vererbte) Auswertung PEM ab: %1%2(Vererbte) Auswertung PEM bis: %3", evalPemFromStr, "\n",
                                                    evalPemToStr));
    }

    public static iPartsDialogBCTEPrimaryKey generateDialogBCTEPrimaryKey(EtkDataPartListEntry entry) {
        String seriesNo = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO);
        String hm = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_HM);
        String m = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_M);
        String sm = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SM);
        String posE = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE);
        String posV = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV);
        String ww = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_WW);
        String et = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_ETKZ);
        String aa = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_AA);
        String sData = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SDATA);
        return new iPartsDialogBCTEPrimaryKey(seriesNo, hm, m, sm, posE, posV, ww, et, aa, sData);
    }

    /**
     * Überschreiben damit in diesem Dialog leere Primärschlüsselfelder zugelassen werden
     *
     * @return
     */
    @Override
    protected boolean checkValues() {
        return true;
    }

    @Override
    protected AbstractJavaViewerFormIConnector getConnectorForSpecialFields() {
        return getConnector();
    }

    /**
     * Dokutyp über PartsListEntry des ParentForms erlangen
     *
     * @return
     */
    @Override
    protected boolean partListEntryIsDialogDocuType() {
        if (getParentForm() instanceof iPartsRelatedInfoFactoryDataForm) {
            iPartsRelatedInfoFactoryDataForm parentForm = (iPartsRelatedInfoFactoryDataForm)getParentForm();
            if (parentForm != null) {
                if (EditModuleHelper.getDocumentationTypeFromPartListEntry(parentForm.getConnector().getRelatedInfoData().getAsPartListEntry(parentForm.getProject())).isPKWDocumentationType()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static EtkEditFields modifyEditFields(EtkProject project, String tableName, String... extraReadOnlyFieldNames) {
        return modifyEditFields(project, iPartsRelatedInfoFactoryDataForm.CONFIG_KEY_PRODUCTION_DATA_AS, tableName, mustHaveValueFieldNames,
                                allowedEmptyPKFields, invisibleFieldNames, readOnlyFieldNames, extraReadOnlyFieldNames);
    }


    protected EditUserControlsForFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                             IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                             boolean isNewForm, PEMDataHelper.PEMDataOrigin pemDataOrigin) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields, isNewForm, "factoryDataEdit", pemDataOrigin);
    }

    @Override
    protected String getFactoryFieldName() {
        return FIELD_DFD_FACTORY;
    }

    @Override
    protected String getFactoryTableName() {
        return TABLE_DA_FACTORY_DATA;
    }

    @Override
    protected String getCodeToFieldName() {
        return FIELD_DFD_STCB;
    }

    @Override
    protected String getCodeFromFieldName() {
        return FIELD_DFD_STCA;
    }

    @Override
    protected String getPEMToDateFieldName() {
        return FIELD_DFD_PEMTB;
    }

    @Override
    protected String getPEMFromDateFieldName() {
        return FIELD_DFD_PEMTA;
    }

    @Override
    protected String getPEMToFieldName() {
        return FIELD_DFD_PEMB;
    }

    @Override
    protected String getPEMFromFieldName() {
        return FIELD_DFD_PEMA;
    }
}