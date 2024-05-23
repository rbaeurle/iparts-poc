package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataPrimusIncludePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataPrimusIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataPrimusReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsIncludePartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.ArrayList;
import java.util.List;

public class iPartsPrimusReplacementsIncludePartsForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    private static final String CONFIG_KEY_PRIMUS_REPLACEMENTS_INCLUDE_PARTS = "Plugin/iPartsEdit/PrimusIncludeParts";
    private iPartsDataPrimusReplacePart primusReplacePart;
    private iPartsReplacement replacement;
    private List<EtkDataPartListEntry> filteredPartList;

    public static EtkDataPart createAndSetPartForReplacementChain(EtkProject project, String matNo) {
        EtkDataPart part = EtkDataObjectFactory.createDataPart(project, matNo, "");
        if (!part.existsInDB()) {
            part.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            part.setFieldValue(iPartsConst.FIELD_M_BESTNR, matNo, DBActionOrigin.FROM_DB);
            EtkMultiSprache multi = new EtkMultiSprache();
            multi.setText(project.getViewerLanguage(), TranslationHandler.translateForLanguage("!!<existiert nicht>", project.getViewerLanguage()));
            part.setFieldValueAsMultiLanguage(iPartsConst.FIELD_M_TEXTNR, multi, DBActionOrigin.FROM_DB);
        }
        return part;
    }

    protected iPartsPrimusReplacementsIncludePartsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsDataPrimusReplacePart primusReplacePart,
                                                       iPartsReplacement replacement, List<EtkDataPartListEntry> filteredPartList) {
        super(dataConnector, parentForm, CONFIG_KEY_PRIMUS_REPLACEMENTS_INCLUDE_PARTS, RELATED_INFO_PRIMUS_REPLACEMENT_INCLUDE_PARTS_TEXT,
              RELATED_INFO_PRIMUS_REPLACEMENT_INCLUDE_PARTS_TEXT, RELATED_INFO_PRIMUS_REPLACEMENT_INCLUDE_PARTS_TEXT);
        this.primusReplacePart = primusReplacePart;
        this.replacement = replacement;
        this.filteredPartList = filteredPartList;
        grid.setDisplayFields(getDisplayFields(CONFIG_KEY_PRIMUS_REPLACEMENTS_INCLUDE_PARTS));
        scaleFromParentForm(getWindow());
        dataToGrid();
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<>();
        addDisplayField(defaultDisplayFields, TABLE_DA_PRIMUS_INCLUDE_PART, FIELD_PIP_INCLUDE_PART_NO, false, false, true);
        addDisplayField(defaultDisplayFields, TABLE_MAT, FIELD_M_TEXTNR, true, false, false);
        addDisplayField(defaultDisplayFields, TABLE_DA_PRIMUS_INCLUDE_PART, FIELD_PIP_QUANTITY, false, false, false);
        return defaultDisplayFields;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        return null;
    }

    @Override
    public void dataToGrid() {

        String predecessorPartText = iPartsNumberHelper.formatPartNo(getProject(), primusReplacePart.getAsId().getPartNo());
        String successorPartText = iPartsNumberHelper.formatPartNo(getProject(), primusReplacePart.getFieldValue(FIELD_PRP_SUCCESSOR_PARTNO));

        setWindowTitle(iPartsConst.RELATED_INFO_INCLUDE_PARTS_TEXT, TranslationHandler.translate("!!Ersetzung von \"%1\" durch \"%2\"",
                                                                                                 predecessorPartText, successorPartText));
        grid.clearGrid();

        boolean hasKatalogFields = iPartsRelatedInfoPrimusReplacementChainForm.hasKatalogField(grid);
        iPartsDataPrimusIncludePartList includeParts = iPartsDataPrimusIncludePartList.loadIncludePartsForReplacement(getProject(), primusReplacePart.getAsId());
        for (iPartsDataPrimusIncludePart includePart : includeParts) {
            String includeMatNr = includePart.getAsId().getIncludePartNo();
            // Teil erzeugen, damit hinzukonfigurierte Felder aus der Materialtabelle geladen werden können
            EtkDataPart part = createAndSetPartForReplacementChain(getProject(), includeMatNr);
            if (hasKatalogFields && (replacement != null)) {
                EtkDataPartListEntry partListEntry = iPartsIncludePartsHelper.getPartListEntryForIncludePart(replacement, includePart, filteredPartList, FIELD_PIP_INCLUDE_PART_NO);
                grid.addObjectToGrid(includePart, part, partListEntry);
            } else {
                grid.addObjectToGrid(includePart, part);
            }
        }
    }

    @Override
    protected DataObjectFilterGrid createGrid() {
        DataObjectFilterGrid myGrid = new DataObjectFilterGrid(getConnector(), this) {

            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (fieldName.equals(FIELD_PIP_INCLUDE_PART_NO)) {
                    if (objectForTable != null) {
                        return iPartsNumberHelper.formatPartNo(getProject(), objectForTable.getFieldValue(fieldName));
                    }
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }
        };
        return myGrid;
    }

}
