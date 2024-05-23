package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Erweiterung der {@link EnumRComboBox} für die AusfuehrungsArten von DIALOG-Produkten
 */
public class iPartsGuiAARComboBox extends EnumRComboBox {

    public static final String TYPE = "ipartsaarcombobox";

    public iPartsGuiAARComboBox() {
        super();
        setType(TYPE);
    }

    /**
     * Bei gesetztem {@link EtkProject} und {@link iPartsSeriesId} werden die in X4E gesetzten AusfuehrungsArten geladen
     * Sollte es keine geben, oder seriesId ist null, so werden die Standard Enums zu tableName und fieldName geladen
     *
     * @param project
     * @param seriesId
     * @param tableName
     * @param fieldName
     * @param enableSort
     */
    public void init(EtkProject project, iPartsSeriesId seriesId, String tableName, String fieldName, boolean enableSort) {
        if (project != null) {
            String language = project.getDBLanguage();
            setTooltip("");
            if ((seriesId != null) && (seriesId.isValidId())) {
                List<String> aaList = new DwList<>(iPartsDialogSeries.getInstance(project, seriesId).getValidAAForSeries(project));
                if (!aaList.isEmpty()) {
                    if (enableSort) {
                        Collections.sort(aaList, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o1.compareTo(o2);
                            }
                        });
                    }
                    removeAllItems();
                    EtkDbs etkDbs = project.getEtkDbs();
                    for (String aa : aaList) {
                        String enumKey = etkDbs.getEnum(TableAndFieldName.make(tableName, fieldName));
                        // Versuchen, eine Benennung für das Token zu ermitteln mit Fallback auf den Token selbst
                        String tokenValue = SetOfEnumDataType.getSetOfEnumToken(aa);
                        String enumText = etkDbs.getEnums().getEnumText(enumKey, tokenValue, language,
                                                                        tokenValue, project, true);
                        addItem(aa, enumText);
                        getTokens().add(aa);
                    }
                    return;
                }
            }
            setEnumTexte(project, tableName, fieldName, language, enableSort);
            setTooltip("!!keine X4E-Daten vorhanden.");
        }
    }
}
