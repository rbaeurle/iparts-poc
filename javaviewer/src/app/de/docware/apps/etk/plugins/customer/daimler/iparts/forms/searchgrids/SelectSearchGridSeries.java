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


/**
 * Suchformular für Suche nach Baureihen in der Tabelle {@link iPartsConst#TABLE_DA_SERIES} (iParts spezifisch)
 */
public class SelectSearchGridSeries extends SimpleSelectSearchResultGrid {

    public static EtkDisplayFields createDisplayResultFields(EtkProject project) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_SERIES, iPartsConst.FIELD_DS_SERIES_NO, "!!Baureihennummer", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_SERIES, iPartsConst.FIELD_DS_TYPE, "!!Baureihentyp", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, iPartsConst.TABLE_DA_SERIES, iPartsConst.FIELD_DS_NAME, "!!Baureihenbezeichnung", true, false, true));
        return displayResultFields;
    }

    public SelectSearchGridSeries(AbstractJavaViewerForm parentForm) {
        super(parentForm.getConnector(), parentForm, iPartsConst.TABLE_DA_SERIES, iPartsConst.FIELD_DS_SERIES_NO);
        setTitle("!!Baureihe auswählen");
        setAutoSelectSingleSearchResult(true);
        setDisplayResultFields(createDisplayResultFields(getProject()));
    }

}
