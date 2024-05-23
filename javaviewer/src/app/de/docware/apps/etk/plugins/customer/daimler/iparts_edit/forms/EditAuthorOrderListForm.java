/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsVirtualUserGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog zur Darstellung einer Liste von Autoren-Aufträgen
 */
public class EditAuthorOrderListForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    private iPartsDataAuthorOrderList dataAuthorOrderList;

    /**
     * Zeigt eine Liste aller nicht-freigegebenen Autoren-Aufträge, in denen das übergebene Modul gerade bearbeitet wird.
     *
     * @param dataConnector
     * @param parentForm
     * @param assemblyId
     */
    public static void showAuthorOrderListForModuleInEdit(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                          AssemblyId assemblyId) {
        iPartsDataAuthorOrderList dataAuthorOrderList = EditModuleHelper.getActiveAuthorOrderListForModule(assemblyId, dataConnector.getProject());
        EditAuthorOrderListForm dlg = new EditAuthorOrderListForm(dataConnector, parentForm, dataAuthorOrderList,
                                                                  TranslationHandler.translate("!!Nicht freigegebene Autoren-Aufträge, in denen der TU \"%1\" bearbeitet wird",
                                                                                               assemblyId.getKVari()));
        dlg.showModal();
    }

    public EditAuthorOrderListForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                   iPartsDataAuthorOrderList dataAuthorOrderList, String subTitle) {
        super(dataConnector, parentForm, "", "", "!!Autoren-Aufträge", subTitle);
        getWindow().setSize(1000, 500);
        this.dataAuthorOrderList = dataAuthorOrderList;
        dataToGrid();
        grid.sortTableAfterColumn(0, true);
        getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE).requestFocus();
    }

    @Override
    protected DataObjectFilterGrid createGrid() {
        return new EditDataObjectFilterGrid(getConnector(), this) {
            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (fieldName.equals(FIELD_DAO_CREATOR_GRP_ID) || fieldName.equals(FIELD_DAO_CURRENT_GRP_ID)) {
                    String virtualUserGroupId = objectForTable.getFieldValue(fieldName);
                    return iPartsVirtualUserGroup.getVirtualUserGroupName(virtualUserGroupId);
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }
        };
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> displayFields = new ArrayList<>();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_NAME, false, false);
        displayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CREATOR_GRP_ID, false, false);
        displayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CREATION_USER_ID, false, false);
        displayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CURRENT_GRP_ID, false, false);
        displayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CURRENT_USER_ID, false, false);
        displayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_STATUS, false, false);
        displayFields.add(displayField);
        return displayFields;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        return dataAuthorOrderList;
    }
}