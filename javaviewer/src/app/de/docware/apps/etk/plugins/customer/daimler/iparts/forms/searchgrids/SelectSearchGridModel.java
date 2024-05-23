/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;


/**
 * Suchformular für Suche nach Baumustern in der Tabelle {@link iPartsConst#TABLE_DA_MODEL} (iParts spezifisch)
 */
public class SelectSearchGridModel extends SimpleSelectSearchResultGrid {

    public static EtkDisplayFields createDisplayResultFields(EtkProject project) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_MODEL_NO, "!!Baumusternummer", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_SALES_TITLE, "!!Verkaufsbezeichnung", true, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_PRODUCT_GRP, "!!Produktgruppe", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_CODE, "!!baumusterbildende Code", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_AA, "!!Ausführungsart", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_MODEL_TYPE, "!!Baumusterart", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_MODEL_VISIBLE, "!!Baumuster anzeigen", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_SOURCE, "!!Quelle", false, false, false));
        return displayResultFields;
    }

    private boolean carAndVanInSession = iPartsRight.checkCarAndVanInSession();
    private boolean truckAndBusInSession = iPartsRight.checkTruckAndBusInSession();

    public SelectSearchGridModel(AbstractJavaViewerForm parentForm) {
        super(parentForm.getConnector(), parentForm, iPartsConst.TABLE_DA_MODEL, iPartsConst.FIELD_DM_MODEL_NO);
        setTitle("!!Baumuster auswählen");
        setAutoSelectSingleSearchResult(true);
        setDisplayResultFields(createDisplayResultFields(getProject()));
    }

    @Override
    protected boolean doValidAttributes(DBDataObjectAttributes attributes) {
        // Abkürzung falls beide Benutzer-Eigenschaften vorhanden sind
        if (carAndVanInSession && truckAndBusInSession) {
            return true;
        }
        String source = attributes.getFieldValue(iPartsConst.FIELD_DM_SOURCE);
        String modelNo = attributes.getFieldValue(iPartsConst.FIELD_DM_MODEL_NO);
        return iPartsFilterHelper.isASModelVisibleForUserInSession(modelNo, source,
                                                                   carAndVanInSession, truckAndBusInSession,
                                                                   getProject());
    }
}
