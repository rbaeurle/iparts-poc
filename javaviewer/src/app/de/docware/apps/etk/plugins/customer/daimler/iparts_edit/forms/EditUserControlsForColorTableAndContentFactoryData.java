/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableFactoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableToPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.AbstractVariantFactoryDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsVariantFactoryDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsVariantsToPartFactoryDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * UserControl zum Bearbeiten von Werkseinsatzdaten Farbtabelle zu Teil (DAIMLER-4861) und Farbtabelleninhalt (DAIMLER-1947)
 *
 * Regeln sind die gleichen wie in {@link EditUserControlsForFactoryData}
 */
public class EditUserControlsForColorTableAndContentFactoryData extends AbstractEditUserControlsForFactoryData {

    private static final String[] readOnlyFieldNames = new String[]{ FIELD_DCCF_TABLE_ID, FIELD_DCCF_POS, FIELD_DCCF_SDATA,
                                                                     FIELD_DCCF_SOURCE, FIELD_DCCF_STATUS };
    private static final String[] mustHaveValueFieldNames = new String[]{ FIELD_DCCF_FACTORY };
    private static String[] invisibleFieldNames = new String[]{};
    private static final String[] allowedEmptyPKFields = new String[]{};

    // Aufrufe für Werkseinsatzdaten zu Farbtabelle zu Teil

    public static iPartsDataColorTableFactory showCreateColorTableToPartFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                                    iPartsColorTableToPartId colorTableToPartId) {
        invisibleFieldNames = new String[]{};
        return showCreateColorTableToPartFactoryData(dataConnector, parentForm, colorTableToPartId, null);
    }

    public static iPartsDataColorTableFactory showCreateColorTableToPartFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                                    iPartsColorTableToPartId colorTableToPartId,
                                                                                    iPartsColorTableFactoryId externColorTableFactoryId) {
        invisibleFieldNames = new String[]{};
        return showEditColorTableFactoryData(dataConnector, parentForm, getPartToColorTableData(colorTableToPartId), externColorTableFactoryId);
    }

    // Aufrufe für Werkseinsatzdaten zum Farbtabelleninhalt
    public static iPartsDataColorTableFactory showCreateColorTableContentFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                                     iPartsColorTableContentId colorTableContentId) {
        invisibleFieldNames = new String[]{};
        return showCreateColorTableContentFactoryData(dataConnector, parentForm, colorTableContentId, null);
    }

    public static iPartsDataColorTableFactory showCreateColorTableContentFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                                     iPartsColorTableContentId colorTableContentId,
                                                                                     iPartsColorTableFactoryId externColorTableFactoryId) {
        invisibleFieldNames = new String[]{};
        return showEditColorTableFactoryData(dataConnector, parentForm, getColorTableContentData(colorTableContentId), externColorTableFactoryId);
    }

    public static iPartsDataColorTableFactory showCreateColorTableContentFactoryDataWithoutColorTableContentId(RelatedInfoBaseFormIConnector dataConnector,
                                                                                                               AbstractJavaViewerForm parentForm,
                                                                                                               iPartsDataColorTableFactory externColorTableFactoryData) {
        iPartsColorTableContentId dummyColorTableContentId = new iPartsColorTableContentId("123", "123", "123");
        invisibleFieldNames = new String[]{ FIELD_DCCF_TABLE_ID, FIELD_DCCF_POS, FIELD_DCCF_SDATA };
        return showEditColorTableFactoryData(dataConnector, parentForm, getColorTableContentData(dummyColorTableContentId), externColorTableFactoryData);
    }

    // Hilfsmethoden

    private static ExistingKeyValues getPartToColorTableData(iPartsColorTableToPartId colorTableToPartId) {
        return new ExistingKeyValues(colorTableToPartId, iPartsFactoryDataTypes.COLORTABLE_PART_AS,
                                     iPartsVariantsToPartFactoryDataForm.CONFIG_KEY_VARIANT_TABLE_FACTORY_DATA_AS,
                                     "!!Werkseinsatzdaten zu Farbtabelle zu Teil erstellen",
                                     "!!Werkseinsatzdaten zu Farbtabelle zu Teil bearbeiten");
    }

    private static ExistingKeyValues getColorTableContentData(iPartsColorTableContentId colorTableContentId) {
        return new ExistingKeyValues(colorTableContentId, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS,
                                     iPartsVariantFactoryDataForm.CONFIG_KEY_VARIANT_FACTORY_DATA_AS,
                                     "!!Werkseinsatzdaten zum Farbtabelleninhalt erstellen",
                                     "!!Werkseinsatzdaten zum Farbtabelleninhalt bearbeiten");
    }

    private static iPartsDataColorTableFactory showEditColorTableFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                             ExistingKeyValues existingKeyValues,
                                                                             iPartsColorTableFactoryId externColorTableFactoryId) {
        EtkProject project = dataConnector.getProject();
        iPartsDataColorTableFactory colorTableFactoryData = null;
        if (externColorTableFactoryId != null) {
            colorTableFactoryData = new iPartsDataColorTableFactory(project, externColorTableFactoryId);
            if (!colorTableFactoryData.existsInDB()) {
                colorTableFactoryData = null;
            }
        }
        return showEditColorTableFactoryData(dataConnector, parentForm, existingKeyValues, colorTableFactoryData);
    }

    private static iPartsDataColorTableFactory showEditColorTableFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                             ExistingKeyValues existingKeyValues,
                                                                             iPartsDataColorTableFactory colorTableFactoryData) {
        String tableName = TABLE_DA_COLORTABLE_FACTORY;
        EtkProject project = dataConnector.getProject();

        String text;
        boolean isNewForm = false;
        boolean useExistingData = false;
        if (colorTableFactoryData == null) {
            if (!existingKeyValues.isValid()) {
                return null;
            }
            iPartsColorTableFactoryId colorTableFactoryId = new iPartsColorTableFactoryId(existingKeyValues.getColorTableId(),
                                                                                          existingKeyValues.getPosition(), "", "", "",
                                                                                          existingKeyValues.getSdata());
            colorTableFactoryData = new iPartsDataColorTableFactory(project, colorTableFactoryId);
            colorTableFactoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            isNewForm = true;
            text = existingKeyValues.getCreateText();
        } else {
            useExistingData = colorTableFactoryData.getSource() == iPartsImportDataOrigin.IPARTS;
            text = existingKeyValues.getEditText();
        }
        iPartsEditUserControlsHelper.prepareForFactoryDataEdit(colorTableFactoryData, isNewForm,
                                                               existingKeyValues.getDataTypes(), FIELD_DCCF_ADAT,
                                                               FIELD_DCCF_DATA_ID, FIELD_DCCF_SOURCE, FIELD_DCCF_STATUS);
        EtkEditFields editFields = modifyEditFields(project, tableName, existingKeyValues.getConfigKey());
        EditUserControlForCreate eCtrl = new EditUserControlsForColorTableAndContentFactoryData(dataConnector, parentForm, tableName,
                                                                                                colorTableFactoryData.getAsId(),
                                                                                                colorTableFactoryData.getAttributes(), editFields,
                                                                                                isNewForm);
        eCtrl.setMainTitle(text);
        ModalResult modalResult = eCtrl.showModal();
        iPartsDataColorTableFactory result = null;
        if (modalResult == ModalResult.OK) {
            if (useExistingData) {
                // Weil das EditControl nur auf den Attributes arbeitet und wir die Beziehung zur alten ID nicht verlieren wollen,
                // muss hier die ID aus den bearbeiteten Attributes heraus synchronisiert werden.
                colorTableFactoryData.updateIdFromPrimaryKeys();
                result = colorTableFactoryData;
            } else {
                result = new iPartsDataColorTableFactory(project, colorTableFactoryData.getAsId());
                result.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
            }
        }
        return result;
    }

    private static EtkEditFields modifyEditFields(EtkProject project, String tableName, String configKey, String... extraReadOnlyFieldNames) {
        return modifyEditFields(project, configKey, tableName, mustHaveValueFieldNames,
                                allowedEmptyPKFields, invisibleFieldNames, readOnlyFieldNames, extraReadOnlyFieldNames);
    }

    protected EditUserControlsForColorTableAndContentFactoryData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                                                 IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                                                 boolean isNewForm) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields, isNewForm, "colorTableFactoryDataEdit", null);
    }


    @Override
    protected AbstractJavaViewerFormIConnector getConnectorForSpecialFields() {
        return getConnector().getOwnerConnector();
    }

    /**
     * Dokutyp über PartsListEntry des ParentForms erlangen
     * Für Farbtabelle zu Teil und Farbvariante zu Farbtabelle
     *
     * @return
     */
    @Override
    protected boolean partListEntryIsDialogDocuType() {
        if (getParentForm() instanceof AbstractVariantFactoryDataForm) {
            AbstractVariantFactoryDataForm parentForm = (AbstractVariantFactoryDataForm)getParentForm();
            if (parentForm != null) {
                if (EditModuleHelper.getDocumentationTypeFromPartListEntry(parentForm.getPartListEntry()).isPKWDocumentationType()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected String getFactoryFieldName() {
        return FIELD_DCCF_FACTORY;
    }

    @Override
    protected String getCodeToFieldName() {
        return FIELD_DCCF_STCB;
    }

    @Override
    protected String getCodeFromFieldName() {
        return FIELD_DCCF_STCA;
    }

    @Override
    protected String getPEMToDateFieldName() {
        return FIELD_DCCF_PEMTB;
    }

    @Override
    protected String getPEMFromDateFieldName() {
        return FIELD_DCCF_PEMTA;
    }

    @Override
    protected String getPEMToFieldName() {
        return FIELD_DCCF_PEMB;
    }

    @Override
    protected String getPEMFromFieldName() {
        return FIELD_DCCF_PEMA;
    }

    @Override
    protected String getFactoryTableName() {
        return TABLE_DA_COLORTABLE_FACTORY;
    }

    /**
     * Hilfsklasse zur Bestimmung der Quelldaten
     */
    private static class ExistingKeyValues {

        private String colorTableId;
        private String position;
        private String sdata;
        private String configKey;
        private iPartsFactoryDataTypes dataTypes;
        private String createText;
        private String editText;


        public ExistingKeyValues(iPartsColorTableToPartId colorTableToPartId, iPartsFactoryDataTypes dataType,
                                 String configKey, String createText, String editText) {
            if (colorTableToPartId != null) {
                setSourceValues(colorTableToPartId.getColorTableId(), colorTableToPartId.getPosition(),
                                colorTableToPartId.getSDATA(), dataType, configKey, createText, editText);
            }
        }

        public ExistingKeyValues(iPartsColorTableContentId colorTableContentId, iPartsFactoryDataTypes dataType,
                                 String configKey, String createText, String editText) {
            if (colorTableContentId != null) {
                setSourceValues(colorTableContentId.getColorTableId(), colorTableContentId.getPosition(),
                                colorTableContentId.getSDATA(), dataType, configKey, createText, editText);
            }
        }

        private void setSourceValues(String colorTableId, String position, String sdata, iPartsFactoryDataTypes dataType,
                                     String configKey, String createText, String editText) {
            this.colorTableId = colorTableId;
            this.position = position;
            this.sdata = sdata;
            this.createText = createText;
            this.editText = editText;
            this.dataTypes = dataType;
            this.configKey = configKey;
        }

        public String getColorTableId() {
            return colorTableId;
        }

        public String getPosition() {
            return position;
        }

        public String getSdata() {
            return sdata;
        }

        public String getConfigKey() {
            return configKey;
        }

        public iPartsFactoryDataTypes getDataTypes() {
            return dataTypes;
        }

        public String getCreateText() {
            return createText;
        }

        public String getEditText() {
            return editText;
        }

        public boolean isValid() {
            return StrUtils.isValid(colorTableId, position, sdata, configKey) && (dataTypes != null);
        }
    }
}