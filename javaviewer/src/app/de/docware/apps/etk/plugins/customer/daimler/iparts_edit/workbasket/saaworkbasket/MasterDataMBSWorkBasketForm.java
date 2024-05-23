/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsEDSSaaCase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.AbstractGoToConstructionContainer;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MasterDataMBSWorkBasketForm extends MasterDataAbstractWorkBasketForm {


    private static final String TABLE_WORK_BASKET_MBS = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_MBS;
    private static final String FIELD_SAA_CASE = iPartsDataVirtualFieldsDefinition.WBM_SAA_CASE;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public MasterDataMBSWorkBasketForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, TABLE_WORK_BASKET_MBS, iPartsImportDataOrigin.SAP_MBS);
        nutzDokRemarkHelper = new WorkBasketNutzDokRemarkHelper(this, getProject(), iPartsWSWorkBasketItem.TYPE.SAA);
        searchHelper = new SearchWorkBasketHelperMBS(getProject(), getVirtualTableName(), this, nutzDokRemarkHelper);
        setSearchHelper(searchHelper);
        setDisplayResultFields(searchHelper.buildDisplayFields(), true);
    }

    public static void showMBSWorkBasket(AbstractJavaViewerForm owner) {
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        showMBSWorkBasket(owner.getConnector(), activeForm);
    }

    public static void showMBSWorkBasket(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        MasterDataMBSWorkBasketForm dlg = getNewMBSWorkBasketInstance(dataConnector, parentForm);
        dlg.showModal();
    }

    /**
     * Neue Instanz von MasterDataMBSWorkBasketForm erzeugen und Default-Sachen vorbesetzen
     *
     * @param dataConnector
     * @param parentForm
     * @return
     */
    public static MasterDataMBSWorkBasketForm getNewMBSWorkBasketInstance(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        MasterDataMBSWorkBasketForm dlg = new MasterDataMBSWorkBasketForm(dataConnector, parentForm);

        EtkDisplayFields searchFields = getSearchFields(dataConnector);

        dlg.setEditFields(null);
        dlg.setSearchFields(searchFields);
        dlg.setEditAllowed(false);
        dlg.setModifyAllowed(false);
        dlg.setNewAllowed(false);
        dlg.setDeleteAllowed(false);
        dlg.showToolbar(false);
        dlg.showSearchFields(true);
        dlg.setMaxResults(-1);
        dlg.setTitlePrefix("!!MBS-SAA-Arbeitsvorrat");
        dlg.setWindowName("MBSWorkBasketMasterData");
        return dlg;
    }

    /**
     * SearchFields noch ohne Konfiguration besetzen
     *
     * @param dataConnector
     * @return
     */
    private static EtkDisplayFields getSearchFields(AbstractJavaViewerFormIConnector dataConnector) {
        List<String> dbLanguages = dataConnector.getConfig().getDatabaseLanguages();
        EtkDisplayFields searchFields = new EtkDisplayFields();
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, false));
            EtkDisplayField searchField = createSearchField(dataConnector.getProject(), TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO, false, false);
            searchField.setText(new EtkMultiSprache("!!Baumuster", dbLanguages));
            searchFields.addFeld(searchField);
            searchField.setDefaultText(false);
            searchField = createSearchField(dataConnector.getProject(), TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SAA, false, false);
            searchField.setText(new EtkMultiSprache("!!SAA/BK-Nummer", dbLanguages));
            searchField.setDefaultText(false);
            searchFields.addFeld(searchField);
            EtkDisplayField field = createSearchField(dataConnector.getProject(), TABLE_WORK_BASKET_MBS, FIELD_SAA_CASE, false, false);
            VirtualFieldDefinition vField = iPartsDataVirtualFieldsDefinition.findField(TABLE_WORK_BASKET_MBS, FIELD_SAA_CASE);
            String labelText = "!!Geschäftsfall";
            if (vField != null) {
                labelText = vField.getDBDescription(dataConnector.getProject()).getDisplayName();
            }
            field.setDefaultText(false);
            field.setText(new EtkMultiSprache(labelText, new String[]{ TranslationHandler.getUiLanguage() }));
            searchFields.addFeld(field);

            searchFields.loadStandards(dataConnector.getConfig());
        }
        return searchFields;
    }

    /**
     * Callback für Goto-Construction falls gewollt mit Laden der TUs im Edit
     *
     * @param withOwnDialog
     */
    @Override
    protected void gotoConstruction(boolean withOwnDialog, boolean withLoadInEdit) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            GotoConstructionContainer gotoContainer = new GotoConstructionContainer(getProject(), attributes);
            gotoContainer.gotoMBSConstruction(withOwnDialog, withLoadInEdit);
        }
    }

    public static String getConGroupFromSnr(EtkProject project, String saaNumber) {
        String[] fields = new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR, FIELD_DSM_RELEASE_TO };
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_STRUCTURE_MBS, fields,
                                                                                           new String[]{ FIELD_DSM_SNR },
                                                                                           new String[]{ saaNumber },
                                                                                           ExtendedDataTypeLoadType.NONE,
                                                                                           false, true);
        attributesList.sort(true, new String[]{ FIELD_DSM_RELEASE_TO });
        // sollte SUB_SNR leer sein => Vorbesetzung mit saaNumber
        String subSnr = saaNumber;
        // unendlich wird ans Ende sortiert
        for (int lfdNr = attributesList.size() - 1; lfdNr >= 0; lfdNr--) {
            DBDataObjectAttributes attrib = attributesList.get(lfdNr);
            if (attrib.getFieldValue(FIELD_DSM_RELEASE_TO).isEmpty()) {
                if (!attrib.getFieldValue(FIELD_DSM_SUB_SNR).isEmpty()) {
                    subSnr = attrib.getFieldValue(FIELD_DSM_SUB_SNR);
                    break;
                }
            } else {
                break;
            }
        }
        return subSnr;
    }

    @Override
    protected boolean calcWhereFieldsAndValuesFromSearchFields(AbstractSearchWorkBasketHelper searchHelper, Set<String> modelNumbers) {
        EtkProject project = getProject();
        searchHelper.searchValuesAndFields.clear();
        searchSaaCase = null;
        searchHelper.searchProductNo = null;

        // Vorbereitung der Suchfelder. Alle Suchfelder bekommen ein * angehängt, außer Produktnummer. SAAs werden unformatiert.
        for (int lfdNr = 0; lfdNr < getSearchFields().size(); lfdNr++) {
            EditControl editCtrl = getEditControlFeldByIndex(lfdNr);
            if (editCtrl != null) {
                EditControlFactory ctrl = editCtrl.getEditControl();
                if (ctrl != null) {
                    String value = ctrl.getText().trim();
                    if (StrUtils.isValid(value)) {
                        // Länge von searchValue auf die maximale Feldlänge beschränken, um DB-Fehler zu vermeiden
                        value = project.getConfig().getDBDescription().cutValueIfLongerThanFieldLength(value,
                                                                                                       ctrl.getTableName(),
                                                                                                       ctrl.getFieldName());
                        if (ctrl.getFieldName().equals(getFieldSaaCase())) {
                            searchSaaCase = new HashSet<>();
                            searchSaaCase.add(iPartsEDSSaaCase.getFromDBValue(value));
                            continue;
                        }
                        if (ctrl.getFieldName().equals(searchHelper.getFieldSaa())) {
                            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
                            value = numberHelper.unformatSaaBkForEdit(project, value);

                            // Unterschiedliche Retail-SAA-Nummer vorhanden für die Konstruktions-SAA-Nummer?
                            String retailSAA = numberHelper.getDifferentRetailSAA(value);
                            if (retailSAA != null) {
                                value = retailSAA;
                            }
                        }
                        if (!ctrl.getFieldName().equals(iPartsConst.FIELD_DPM_PRODUCT_NO)) {
                            EtkFieldType fieldType = project.getFieldDescription(ctrl.getTableName(), ctrl.getFieldName()).getType();
                            EtkDisplayField searchField = getSearchFields().getFeld(lfdNr);
                            if (fieldType.isWildCardType() && !searchField.isSearchExact()) {
                                //eigenes WildCardSetting mit * am Ende
                                WildCardSettings wildCardSettings = new WildCardSettings();
                                wildCardSettings.addWildCardEnd();
                                value = wildCardSettings.makeWildCard(value);
                            }
                        }

                        if (ctrl.getFieldName().equals(iPartsConst.FIELD_DPM_PRODUCT_NO)) {
                            if (StrUtils.stringContainsWildcards(value)) {
                                MessageDialog.showWarning("!!Produktsuche nur ohne Wildcards erlaubt.");
                                return false;
                            }
                        }
                        if (!ctrl.getFieldName().equals(iPartsConst.FIELD_DPM_PRODUCT_NO)) {
                            // bei Produkten kein Upper, bei den anderen Felder schon
                            value = value.toUpperCase();
                            searchHelper.searchValuesAndFields.put(ctrl.getTableFieldName(), value);
                        } else {
                            searchHelper.searchProductNo = value;
                        }
                    }
                }
            }
        }
        return true;
    }

    // ===== Implementierung der abstrakten Getter aus der Basisklasse ====
    @Override
    protected String getVirtualTableName() {
        return TABLE_WORK_BASKET_MBS;
    }

    @Override
    protected boolean isSaaDisplayFieldName(String fieldName) {
        return fieldName.equals(getFieldSaa()) || fieldName.equals(FIELD_DS_SAA) || fieldName.equals(FIELD_DNS_SAA);
    }

    /**
     * Container für die gotoMBS-Sprünge (mit Erweiterung aus DAIMLER-11259)
     */
    private class GotoConstructionContainer extends AbstractGoToConstructionContainer {

        private final DBDataObjectAttributes attributes;

        public GotoConstructionContainer(EtkProject project, DBDataObjectAttributes attributes) {
            super(project, getModelNumber(attributes), getSaaBkNo(attributes), getConGroupFromSnr(project, getSaaBkNo(attributes)));
            this.attributes = attributes;
        }

        @Override
        protected boolean doGotoMBSConstruction(boolean withOwnDialog, boolean withLoadInEdit) {
            // KEM und Assembly für Markierung in der Stückliste merken
            boolean foundPath = findGoToPath(getConnector(), MasterDataMBSWorkBasketForm.this, withOwnDialog);
            if (withLoadInEdit) {
                loadAssembliesFromAttributeIdListInEdit(this.attributes, withOwnDialog);
            }
            return foundPath;
        }

    }
}
