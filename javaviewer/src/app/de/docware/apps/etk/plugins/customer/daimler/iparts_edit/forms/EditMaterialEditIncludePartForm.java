/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsIncludePartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.util.StrUtils;

/**
 * Form zur Suche nach Material und Anlegen eines Mitlieferteil-Eintrags
 */
public class EditMaterialEditIncludePartForm extends EditMaterialEditForm {

    public static iPartsDataIncludePartList showEditMaterialEditIncludePartFormDirect(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                                      iPartsReplacement replacement) {
        EtkDataPartListEntry partListEntry = showEditMaterialEditIncludePartForm(dataConnector, parentForm);
        if (partListEntry != null) {
            EtkProject project = dataConnector.getProject();
            iPartsDataIncludePartList updatedIncludePartList = new iPartsDataIncludePartList();
            iPartsDataReplacePart dataReplacement = replacement.getAsDataReplacePart(project);
            if (dataReplacement == null) {
                return null;
            }
            iPartsDataIncludePart newIncludePart = iPartsIncludePartsHelper.convertPartListEntryToIncludePart(project,
                                                                                                              dataReplacement,
                                                                                                              partListEntry);
            updatedIncludePartList.add(newIncludePart, DBActionOrigin.FROM_EDIT);
            return updatedIncludePartList;
        }
        return null;
    }

    public static EtkDataPartListEntry showEditMaterialEditIncludePartForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        boolean pskMaterialsAllowed = false;
        if (dataConnector instanceof AssemblyListFormIConnector) {
            EtkDataAssembly assembly = ((AssemblyListFormIConnector)dataConnector).getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                pskMaterialsAllowed = ((iPartsDataAssembly)assembly).isPSKAssembly();
            }
        }
        EditMaterialEditIncludePartForm dlg = new EditMaterialEditIncludePartForm(dataConnector, parentForm, pskMaterialsAllowed);
        dlg.setTitle("!!Material für Mitlieferteile auswählen");
        if (ModalResult.OK == dlg.showModal()) {
            return dlg.getPartListEntry();
        }
        return null;
    }

    /**
     * Erzeugt eine Instanz von EditMaterialEditForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param pskMaterialsAllowed
     */
    public EditMaterialEditIncludePartForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           boolean pskMaterialsAllowed) {
        super(dataConnector, parentForm, null, pskMaterialsAllowed);
    }

    @Override
    protected void postCreateGui() {
        // damit der Übernahme-Button nicht angezeigt wird
        this.isEditAllowed = false;
        super.postCreateGui();
        // DoppelKlick in Tabelle ist wie OK-Button drücken
        this.setOnDblClickEvent(() -> okButtonClick(null));
    }

    @Override
    protected void okButtonClick(Event event) {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            AssemblyId targetAssemblyId = getConnector().getCurrentAssembly().getAsId();
            EtkDataPartListEntry partListEntry = buildPartListEntryFromMat(attributes, 0, "0",
                                                                           targetAssemblyId);
            // Menge setzen
            String menge = partListEntry.getFieldValue(EtkDbConst.FIELD_K_MENGE);
            if (!StrUtils.isValid(menge)) {
                partListEntry.setFieldValue(EtkDbConst.FIELD_K_MENGE, "1", DBActionOrigin.FROM_DB);
            }
            setPartListEntry(partListEntry);
            super.okButtonClick(event);
        }
    }

}
