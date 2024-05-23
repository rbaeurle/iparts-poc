/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataIncludeConstMat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataIncludeConstMatList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstMat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Anzeige der Mitlieferteile zu einer Ersetzung in der Konstruktion
 */
public class iPartsReplacementsIncludePartsConstForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    private static final String CONFIG_KEY_INCLUDE_CONST_MAT = "Plugin/iPartsEdit/IncludeConstMat";

    private iPartsDataReplaceConstMat replacement;

    /**
     * Erzeugt einen neuen Dialog für die Anzeige der Mitlieferteile zu einer Ersetzung
     *
     * @param dataConnector
     * @param parentForm
     */
    public iPartsReplacementsIncludePartsConstForm(RelatedInfoFormConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   iPartsDataReplaceConstMat replacement) {
        super(dataConnector, parentForm, null, CONFIG_KEY_INCLUDE_CONST_MAT, "");
        grid.setDisplayFields(getDisplayFields(CONFIG_KEY_INCLUDE_CONST_MAT));
        this.replacement = replacement;
        scaleFromParentForm(getWindow());
        dataToGrid();
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        addDisplayField(defaultDisplayFields, TABLE_DA_INCLUDE_CONST_MAT, FIELD_DICM_INCLUDE_PART_NO, false, false, true);
        addDisplayField(defaultDisplayFields, TABLE_MAT, FIELD_M_CONST_DESC, true, false, false);
        addDisplayField(defaultDisplayFields, TABLE_DA_INCLUDE_CONST_MAT, FIELD_DICM_INCLUDE_PART_QUANTITY, false, false, false);
        return defaultDisplayFields;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        return null;
    }

    @Override
    public void dataToGrid() {
        String dbLanguage = getProject().getDBLanguage();

        String predecessorPartText = replacement.getDisplayValue(iPartsConst.FIELD_DRCM_PRE_PART_NO, dbLanguage);
        String successorPartText = replacement.getDisplayValue(iPartsConst.FIELD_DRCM_PART_NO, dbLanguage);

        setWindowTitle(iPartsConst.RELATED_INFO_INCLUDE_PARTS_TEXT, TranslationHandler.translate("!!Ersetzung von '%1' durch '%2'",
                                                                                                 predecessorPartText, successorPartText));
        grid.clearGrid();

        iPartsDataIncludeConstMatList includeConstMats = iPartsDataIncludeConstMatList.loadIncludeList(getProject(), replacement.getAsId());
        for (iPartsDataIncludeConstMat includeConstMat : includeConstMats) {
            String includeMatNr = includeConstMat.getFieldValue(FIELD_DICM_INCLUDE_PART_NO);
            // Teil erzeugen, damit hinzukonfigurierte Felder aus der Materialtabelle geladen werden können
            EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), includeMatNr, "");
            part.setFieldValue(FIELD_M_BESTNR, includeMatNr, DBActionOrigin.FROM_DB);
            grid.addObjectToGrid(includeConstMat, part);
        }
    }
}