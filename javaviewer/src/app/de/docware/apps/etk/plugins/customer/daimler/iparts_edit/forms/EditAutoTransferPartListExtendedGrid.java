package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.RowContentForAutoTransferToAS;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.List;

public class EditAutoTransferPartListExtendedGrid extends EditAutoTransferPartlistGrid {

    public static String FIELD_PSEUDO_NEW_TU = VirtualFieldsUtils.addVirtualFieldMask("DA_AS_TRANSFER-NEW_TU");

    /**
     * Erzeugt eine Instanz von EditAutoTransferPartlistGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param tableName
     */
    public EditAutoTransferPartListExtendedGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName) {
        super(dataConnector, parentForm, tableName);
    }

    @Override
    protected EtkDisplayFields createDisplayFields() {
        EtkDisplayFields displayFields = super.createDisplayFields();
        List<String> dbLanguages = getProject().getConfig().getDatabaseLanguages();
        EtkDisplayField displayField;
        // Neuer TU
        displayField = new EtkDisplayField(TABLE_PSEUDO, FIELD_PSEUDO_NEW_TU, false, false);
        displayField.setText(new EtkMultiSprache("!!Neuer TU", dbLanguages));
        displayField.setDefaultText(false);
        // Spaltenfilter explizit ausgeschaltet, da die virtuellen Felder nicht offiziell bekannt sind
        // und damit ein String-Spaltenfilter angelegt wird
        displayField.setColumnFilterEnabled(false);
        displayFields.addFeld(displayField);
        return displayFields;
    }

    @Override
    protected AbstractGuiControl getControlFromDisplayFields(String fieldName, int index, RowContentForAutoTransferToAS rowContent,
                                                             RowWithAttributesForAutoTransfer newRow,
                                                             final DBDataObjectAttributes attributes) {
        if (fieldName.equals(FIELD_PSEUDO_NEW_TU)) {
            String text = attributes.getFieldValue(fieldName);
            return createTextCell(text.equals("1") ? DesignImage.boolTrue.getImage() : null);
        }
        return super.getControlFromDisplayFields(fieldName, index, rowContent, newRow, attributes);
    }
}
