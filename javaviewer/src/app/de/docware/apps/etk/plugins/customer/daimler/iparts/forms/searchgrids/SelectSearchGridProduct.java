/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.sql.SQLStringConvert;


/**
 * Suchformular für Suche nach Produkten in der Tabelle {@link iPartsConst#TABLE_DA_PRODUCT} (iParts spezifisch)
 */
public class SelectSearchGridProduct extends SimpleSelectSearchResultGrid {

    public static EtkDisplayFields createDisplayResultFields(EtkProject project) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_PRODUCT, iPartsConst.FIELD_DP_PRODUCT_NO, "!!Produktnummer", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_PRODUCT, iPartsConst.FIELD_DP_STRUCTURING_TYPE, "!!Strukturtyp", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_PRODUCT, iPartsConst.FIELD_DP_TITLE, "!!Produktbezeichnung", true, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_PRODUCT, iPartsConst.FIELD_DP_PICTURE, "!!Produktbild", false, false, true));
        return displayResultFields;
    }

    private boolean isPSKAllowed = iPartsRight.checkPSKInSession();

    public SelectSearchGridProduct(AbstractJavaViewerForm parentForm) {
        super(parentForm.getConnector(), parentForm, iPartsConst.TABLE_DA_PRODUCT, iPartsConst.FIELD_DP_PRODUCT_NO);
        setTitle("!!Produkt auswählen");
        setAutoSelectSingleSearchResult(true);
        setDisplayResultFields(createDisplayResultFields(getProject()));
    }

    @Override
    protected EtkSqlCommonDbSelect buildQuery(String searchValue) {
        EtkSqlCommonDbSelect sqlSelect = super.buildQuery(searchValue);
        if (!isPSKAllowed) {
            // PSK-Produkte nicht auflisten
            sqlSelect.addSelectField(iPartsConst.TABLE_DA_PRODUCT, new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT, iPartsConst.FIELD_DP_PSK,
                                                                                       false, false),
                                     SQLStringConvert.booleanToPPString(Boolean.FALSE), false, false);
        }
        return sqlSelect;
    }

    @Override
    protected boolean doValidAttributes(DBDataObjectAttributes attributes) {
        String productNo = attributes.getFieldValue(iPartsConst.FIELD_DP_PRODUCT_NO);
        iPartsProduct product = iPartsProduct.getInstance(getProject(), new iPartsProductId(productNo));
        // Falls Produkt ein PSK Produkt ist und der User PSK-Rechte hat => anzeigen
        if (product.isPSK()) {
            return isPSKAllowed;
        }
        return iPartsFilterHelper.isProductVisibleForUserInSession(product);
    }

}
