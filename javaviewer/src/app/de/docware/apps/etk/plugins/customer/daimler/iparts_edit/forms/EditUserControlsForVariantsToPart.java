/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableToPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoVariantsToPartDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * EditUserControl um Farbtabellen zu einem Teil zu editieren (obere Tabelle in der Related Info der Farbtabllen)
 */
public class EditUserControlsForVariantsToPart extends EditUserControlForCreate implements iPartsConst {

    // Schlüsselfelder, Quelle und die Zuordnung zm Teil sind gesperrt
    private static final String[] PRIMARY_KEY_FIELDS = { FIELD_DCTP_TABLE_ID, FIELD_DCTP_POS, FIELD_DCTP_SDATA };
    private static final String[] READ_ONLY_FIELDS_NAMES;
    private static final Set<String> DEFAULT_EDIT_FIELD_NAMES;

    static {
        READ_ONLY_FIELDS_NAMES = StrUtils.mergeArrays(PRIMARY_KEY_FIELDS, FIELD_DCTP_PART, FIELD_DCTP_SOURCE, FIELD_DCTP_POS_SOURCE);
        DEFAULT_EDIT_FIELD_NAMES = new HashSet<>();
        DEFAULT_EDIT_FIELD_NAMES.add(FIELD_DCTP_TABLE_ID);
        DEFAULT_EDIT_FIELD_NAMES.add(FIELD_DCTP_POS);
        DEFAULT_EDIT_FIELD_NAMES.add(FIELD_DCTP_SDATA);
        DEFAULT_EDIT_FIELD_NAMES.add(FIELD_DCTP_EVAL_PEM_FROM);
        DEFAULT_EDIT_FIELD_NAMES.add(FIELD_DCTP_EVAL_PEM_TO);
        DEFAULT_EDIT_FIELD_NAMES.add(FIELD_DCTP_STATUS);
    }

    public EditUserControlsForVariantsToPart(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             String tableName, IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
    }

    public static boolean editVariantsToPartData(AbstractJavaViewerFormIConnector dataConnector,
                                                 AbstractJavaViewerForm parentForm,
                                                 iPartsDataColorTableToPart colorTableToPart,
                                                 boolean isReadOnly) {
        EtkEditFields editFields = getEditFields(dataConnector.getProject());
        EditUserControlsForVariantsToPart eCtrl = new EditUserControlsForVariantsToPart(dataConnector, parentForm, TABLE_DA_COLORTABLE_PART,
                                                                                        colorTableToPart.getAsId(),
                                                                                        colorTableToPart.getAttributes(),
                                                                                        editFields);
        eCtrl.setReadOnly(isReadOnly);
        if (isReadOnly) {
            eCtrl.setMainTitle("!!Variantentabelle zu Teil anzeigen");
        } else {
            eCtrl.setMainTitle("!!Variantentabelle zu Teil editieren");
        }
        eCtrl.setTitle(TranslationHandler.translate("!!Variantentabelle \"%1\" zu Teil \"%2\"",
                                                    colorTableToPart.getAsId().getColorTableId(),
                                                    colorTableToPart.getFieldValue(FIELD_DCTP_PART)));

        ModalResult modalResult = eCtrl.showModal();
        if (modalResult == ModalResult.OK) {
            for (DBDataObjectAttribute attribute : eCtrl.getCurrentAttributes().values()) {
                if (attribute.isModified()) {
                    colorTableToPart.setFieldValue(attribute.getName(), attribute.getAsString(), DBActionOrigin.FROM_EDIT);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Liefert die Editfields für das Control. Wurden keine EditFields über die WB definiert, werden die PK Felder
     * im readOnly Modus angezeigt und nur die Felder "PEM ab/bis auswerten" und "Status" sind dann editierbar.
     *
     * @param project
     * @return
     */
    private static EtkEditFields getEditFields(EtkProject project) {
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(project.getConfig(), iPartsRelatedInfoVariantsToPartDataForm.CONFIG_KEY_VARIANTS_TO_PART_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            // Hier alle Felder für die Tabelle laden
            EtkEditFieldHelper.getEditFields(project, TABLE_DA_COLORTABLE_PART, editFields, false);
            // Wurden die EditFields via WB nicht vorgegeben, dann werden nur die PK sowie die "PEM ab/bis auswerten"
            // und Status Felder angezeigt. Die PK Felder sind nicht editierbar
            for (EtkEditField editField : editFields.getFields()) {
                editField.setVisible(DEFAULT_EDIT_FIELD_NAMES.contains(editField.getKey().getFieldName()));
            }
        }
        String[] readOnlyFields = Arrays.stream(READ_ONLY_FIELDS_NAMES).map(entry -> TableAndFieldName.make(TABLE_DA_COLORTABLE_PART, entry)).toArray(String[]::new);
        return modifyEditFields(editFields, null, null, null, null, readOnlyFields);
    }

    @Override
    protected void doEnableButtons(Event event) {
        enableOKButton(readOnly || checkForModified());
    }
}
