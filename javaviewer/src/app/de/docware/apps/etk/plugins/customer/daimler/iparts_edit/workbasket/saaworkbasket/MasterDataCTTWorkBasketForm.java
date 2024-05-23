package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;

public class MasterDataCTTWorkBasketForm extends MasterDataAbstractWorkBasketForm {

    private static final String TABLE_WORK_BASKET_CTT = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_CTT;
    private static final String FIELD_SAA_CASE = iPartsDataVirtualFieldsDefinition.WBC_SAA_CASE;

    /**
     * Neue Instanz von MasterDataEDSWorkBasketForm erzeugen und Default-Sachen vorbesetzen
     *
     * @param dataConnector
     * @param parentForm
     * @return
     */
    public static MasterDataCTTWorkBasketForm getNewCTTWorkBasketInstance(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        MasterDataCTTWorkBasketForm dlg = new MasterDataCTTWorkBasketForm(dataConnector, parentForm);

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
        dlg.setTitlePrefix("!!CTT-SAA-Arbeitsvorrat");
        dlg.setWindowName("CTTWorkBasketMasterData");
        return dlg;
    }

    /**
     * SearchFields noch ohne Konfiguration besetzen
     *
     * @param dataConnector
     * @return
     */
    private static EtkDisplayFields getSearchFields(AbstractJavaViewerFormIConnector dataConnector) {
        EtkDisplayFields searchFields = new EtkDisplayFields();
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO, false, false));
            searchFields.addFeld(createSearchField(dataConnector.getProject(), TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SAA, false, false));
            EtkDisplayField field = createSearchField(dataConnector.getProject(), TABLE_WORK_BASKET_CTT, FIELD_SAA_CASE, false, false);
            VirtualFieldDefinition vField = iPartsDataVirtualFieldsDefinition.findField(TABLE_WORK_BASKET_CTT, FIELD_SAA_CASE);
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
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public MasterDataCTTWorkBasketForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, "", iPartsImportDataOrigin.SAP_CTT);
        nutzDokRemarkHelper = new WorkBasketNutzDokRemarkHelper(this, getProject(), iPartsWSWorkBasketItem.TYPE.SAA);
        searchHelper = new SearchWorkBasketHelperCTT(getProject(), getVirtualTableName(), this, nutzDokRemarkHelper);
        setSearchHelper(searchHelper);
        setDisplayResultFields(searchHelper.buildDisplayFields(), true);
    }

    @Override
    protected void gotoConstruction(boolean withOwnDialog, boolean withLoadInEdit) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            // Springe zu CTT
            iPartsGotoHelper.gotoCTTConstruction(getConnector(), this, true, getModelNumber(attributes),
                                                 getSaaBkNo(attributes), null);
        }
    }

    @Override
    protected String getVirtualTableName() {
        return TABLE_WORK_BASKET_CTT;
    }

    @Override
    protected boolean isSaaDisplayFieldName(String fieldName) {
        return fieldName.equals(getFieldSaa()) || fieldName.equals(FIELD_DS_SAA) || fieldName.equals(FIELD_DNS_SAA);
    }
}
