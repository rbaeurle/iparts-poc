/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.framework.modules.db.DBDataObjectAttributes;

/**
 * Suchformular für Suche nach SAs in der Tabelle {@link iPartsConst#TABLE_DA_SA} (iParts spezifisch)
 */
public class SelectSearchGridSA extends SimpleSelectSearchResultGrid implements iPartsConst {

    private final boolean isPSKAllowed = iPartsRight.checkPSKInSession();
    private final boolean hasBothCarAndTruckRights = iPartsRight.checkUserHasBothVehicleTypeRightsInSession();
    private final boolean hasNeitherCarNorTruckRights = !iPartsRight.checkCarAndVanInSession() && !iPartsRight.checkTruckAndBusInSession();
    private iPartsNumberHelper numberHelper = new iPartsNumberHelper();

    public SelectSearchGridSA(AbstractJavaViewerForm parentForm) {
        super(parentForm.getConnector(), parentForm, TABLE_DA_SA, FIELD_DS_SA);
        setTitle("!!Freie SA auswählen");
        setAutoSelectSingleSearchResult(true);
        setDisplayResultFields(EtkDisplayFieldsHelper.createDefaultDisplayResultFields(getProject(), getSearchTable()));
        setJoinData();
    }

    @Override
    protected String getSearchValue() {
        return numberHelper.unformatSaaBkForEdit(getProject(), super.getSearchValue());
    }

    /**
     * Fügt den Join auf die Tabelle DA_SA_MODULES hinzu, damit nur SAs angezeigt werden, die auch wirklich eine
     * Stückliste besitzen.
     */
    public void setJoinData() {
        EtkDataObjectList.JoinData joinData = new EtkDataObjectList.JoinData(TABLE_DA_SA_MODULES,
                                                                             new String[]{ FIELD_DS_SA },
                                                                             new String[]{ FIELD_DSM_SA_NO },
                                                                             false, false);
        setJoinData(joinData);
    }

    @Override
    protected boolean doValidAttributes(DBDataObjectAttributes attributes) {
        iPartsSAId saId = new iPartsSAId(attributes.getFieldValue(FIELD_DS_SA));
        iPartsSA sa = iPartsSA.getInstance(getProject(), saId);
        // Ist die freie SA nur in PSK Produkten verortet, dann darf der Benutzer sie nur sehen, wenn er auch die PSK
        // Eigenschaft hat. Hat er sie nicht, darf die freie SA nicht angezeigt werden.
        if (sa.isOnlyInPSKProducts(getProject())) {
            return isPSKAllowed;
        }
        // Hat er keine PKW und Truck Eigenschaften, darf die freie SA nicht angezeigt werden
        if (hasNeitherCarNorTruckRights) {
            return false;
        }
        // Hat er beide Eigenschaften, wird die freie SA sofort angezeigt
        if (hasBothCarAndTruckRights) {
            return true;
        }
        // Abhängig von der Benutzer-Eigenschaft die freie SA anzeigen
        return iPartsFilterHelper.isSAVisibleForUserInSession(sa, getProject());
    }
}
