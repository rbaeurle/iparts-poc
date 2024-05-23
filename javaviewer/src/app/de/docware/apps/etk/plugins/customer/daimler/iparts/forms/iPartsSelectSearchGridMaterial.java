/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SelectSearchGridMaterial;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.util.sql.SQLStringConvert;


/**
 * Suchformular für iParts-spezifischbe Suche in Material (mit New und Edit)
 */
public class iPartsSelectSearchGridMaterial extends SelectSearchGridMaterial {

    public iPartsSelectSearchGridMaterial(AbstractJavaViewerForm parentForm, boolean createMaterialAllowed, boolean pskMaterialsAllowed) {
        super(parentForm, createMaterialAllowed);
        if (!pskMaterialsAllowed || !iPartsRight.checkPSKInSession()) {
            setFilterFieldNames(new String[]{ iPartsConst.FIELD_M_PSK_MATERIAL });
            setFilterValues(new String[]{ SQLStringConvert.booleanToPPString(false) });
        }
        setAutoSelectSingleSearchResult(true);
    }
}
