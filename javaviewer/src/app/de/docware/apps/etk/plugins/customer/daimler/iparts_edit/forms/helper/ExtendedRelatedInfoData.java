package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoData;

import java.util.HashSet;
import java.util.Set;

/**
 * Hilfsklasse, um beim Öffnen einer RelatedInfo mit Grid eine Selection vornehmen zu können
 * Beispiel: iPartsRelatedInfoWireHarnessDataForm.handleExternalSelection() und
 * iPartsGotoHelper.startRelatedInfo()
 */
public class ExtendedRelatedInfoData extends EtkRelatedInfoData {

    private String selectTableAndFieldName;
    private Set<String> selectValues;

    public ExtendedRelatedInfoData() {
        this.selectTableAndFieldName = "";
        this.selectValues = new HashSet<>();
    }

    public void setSelectTableAndFieldName(String tableAndFieldName) {
        selectTableAndFieldName = tableAndFieldName;
    }

    public String getSelectTableAndFieldName() {
        return selectTableAndFieldName;
    }

    public Set<String> getSelectValues() {
        return selectValues;
    }

    public void addSelectValues(Set<String> values) {
        selectValues.addAll(values);
    }

    public void addSelectValue(String value) {
        selectValues.add(value);
    }
}
