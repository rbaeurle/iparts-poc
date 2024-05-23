/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.session.Session;

import java.util.HashMap;
import java.util.Map;


/**
 * Suchformular für Suche nach Coden in der Tabelle {@link iPartsConst#TABLE_DA_CODE} (iParts spezifisch)
 */
public class SelectSearchGridCode extends SimpleSelectSearchResultGrid {

    private final String productGroup;
    private final String compareDate;

    public SelectSearchGridCode(AbstractJavaViewerForm parentForm, String productGroup, String compareDate) {
        super(parentForm.getConnector(), parentForm, iPartsConst.TABLE_DA_CODE, iPartsConst.FIELD_DC_CODE_ID);
        setTitle("!!Code auswählen");
        setAutoSelectSingleSearchResult(true);
        setDisplayResultFields(EtkDisplayFieldsHelper.createDefaultDisplayResultFields(getProject(), getSearchTable()));
        this.productGroup = productGroup;
        this.compareDate = compareDate;
    }

    /**
     * Es sollen analog zur Code-Matrix Proval Code mit geladen werden und auch bevorzugt werden bei der Anzeige
     *
     * @param searchValue CodeID mit wildcard
     * @return wurde die explizite Suche verwendet
     */
    @Override
    protected boolean executeExplicitSearch(String searchValue) {
        String seriesNo = getFilterValues()[0];
        iPartsDataCodeList list = iPartsDataCodeList.loadDataProvalSpecial(getProject(), getProject().getDBLanguage(),
                                                                           searchValue, seriesNo, true);
        // Die Baureihe kann bei den Codes auch manchmal fehlen, da der DIALOG Code-Importer die Baureihe nicht
        // kennt -> prophylaktisch schonmal laden
        iPartsDataCodeList listWithEmptySeriesNo = iPartsDataCodeList.loadDataProvalSpecial(getProject(), getProject().getDBLanguage(),
                                                                                            searchValue, "", true);
        Map<String, iPartsDataCodeList> codeIdToCodeDataMap = new HashMap<>();
        list.forEach(codeData -> {
            iPartsDataCodeList codeList = codeIdToCodeDataMap.computeIfAbsent(codeData.getAsId().getCodeId(), k -> new iPartsDataCodeList());
            codeList.add(codeData, DBActionOrigin.FROM_DB);
        });

        // Falls es zu einer CodeId nur Daten gab, während der Suche mit leerer Baureihe, müssen diese noch in
        // die Map aufgenommen werden
        listWithEmptySeriesNo.forEach(codeData -> {
            String codeId = codeData.getAsId().getCodeId();
            iPartsDataCodeList codeList = codeIdToCodeDataMap.get(codeId);
            if (codeList == null) {
                codeList = new iPartsDataCodeList();
                codeList.add(codeData, DBActionOrigin.FROM_DB);
                codeIdToCodeDataMap.put(codeId, codeList);
            } else {
                // Es gibt schon Daten zu dieser CodeId in der Map,
                // aber die Baureihennummer ist auch schon leer -> hinzufügen
                boolean hasEmptySeriesNo = codeList.get(0).getAsId().getSeriesNo().isEmpty();
                if (hasEmptySeriesNo) {
                    codeList.add(codeData, DBActionOrigin.FROM_DB);
                }
            }
        });

        int processedRecords = 0;
        for (iPartsDataCodeList codesGrouped : codeIdToCodeDataMap.values()) {
            if (checkMaxResultsExceeded(processedRecords)) {
                break;
            }
            iPartsDataCode result = iPartsDataCodeList.calculateFittingDateTimeCode(codesGrouped, compareDate, productGroup, true);
            if (result != null) {
                processedRecords += addFoundAttributes(result.getAttributes());
            }
        }
        Session.invokeThreadSafeInSession(() -> getTable().sortRowsAccordingToColumn(0, true));
        return true;
    }
}
