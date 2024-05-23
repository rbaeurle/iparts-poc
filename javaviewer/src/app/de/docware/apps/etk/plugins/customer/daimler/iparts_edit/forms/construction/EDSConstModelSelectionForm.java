/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.construction;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.framework.modules.db.DBDataSetCancelable;
import de.docware.framework.modules.db.DBDatabaseDomain;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.util.CanceledException;
import de.docware.util.sql.terms.Condition;

import java.util.Set;

/**
 * Konkrete Klasse für die Auwahl von EDS/BCS Konstruktionsbaumuster
 */
public class EDSConstModelSelectionForm extends AbstractConstModelSelectionForm {

    public static final String CONFIG_KEY_SELECT_EDS_MODEL_DATA = iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS;

    protected EDSConstModelSelectionForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                         iPartsDataAssembly dataAssembly) {
        super(dataConnector, parentForm, dataAssembly, CONFIG_KEY_SELECT_EDS_MODEL_DATA, CONFIG_KEY_SELECT_EDS_MODEL_DATA);
    }

    @Override
    protected String getTitle() {
        return "!!EDS Konstruktions-Baumuster";
    }

    @Override
    protected String getSubTitle() {
        return "!!Auswahl EDS Baumuster";
    }

    @Override
    protected Set<String> getSelectedModelSet(boolean isAggregate) {
        return SessionKeyHelper.getEdsSelectedModelSet(isAggregate);
    }

    @Override
    protected boolean isValidModel(iPartsDataModel modelData) {
        // Check, ob es Daten zum BM in der Struktur gibt
        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
        String modelField = structureHelper.getModelNumberField();
        DBSQLQuery query = getProject().getEtkDbs().getDBForDomain(DBDatabaseDomain.MAIN).getNewQuery();
        query.selectDistinct(modelField).from(structureHelper.getStructureTableName());
        query.where(new Condition(modelField, Condition.OPERATOR_EQUALS, modelData.getAsId().getModelNumber()));
        query.limit(1);
        try (DBDataSetCancelable dataSet = query.executeQueryCancelable()) { // Query ausführen
            if ((dataSet != null) && dataSet.next()) {
                return true;
            }
        } catch (CanceledException ignored) {
        }
        return false;
    }

    @Override
    protected void setConstructionModelSetToFilter(Set<String> filterValues) {
        // Abhängig vom Typ (Aggregat oder Fahrzeug) Baumuster für den Filter setzen
        SessionKeyHelper.setEdsConstructionModelSetToFilter(filterValues, isAggregateForm());
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            iPartsUserSettingsHelper.setSelectedEDSConstModels(getProject(), SessionKeyHelper.getSelectedEDSModelMap());
        }
    }

}
