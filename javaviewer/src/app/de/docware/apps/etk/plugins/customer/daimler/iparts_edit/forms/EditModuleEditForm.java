package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SelectSearchGridMaterial;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.forms.events.OnStartSearchEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.misc.Constants;

public class EditModuleEditForm extends EditMaterialEditForm {

    private iPartsSearchFilterGridModule searchModule;

    /**
     * Erzeugt eine Instanz von EditMaterialEditForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param editPlugin
     */
    public EditModuleEditForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsEditPlugin editPlugin,
                              iPartsProductId productId) {
        super(dataConnector, parentForm, editPlugin, false);
        createSearchFormAfter(productId);
    }

    public void doStartSearch() {
        searchModule.doStartSearch();
    }

    @Override
    protected void createSearchForm() {

    }

    protected void createSearchFormAfter(iPartsProductId productId) {
        searchModule = new iPartsSearchFilterGridModule(this, productId);
        searchModule.setMultiSelect(Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST);
        searchModule.setOnChangeEvent(new OnChangeEvent() {
            @Override
            public void onChange() {
                DBDataObjectAttributes attributes = searchModule.getSelectedAttributes();
                if (attributes != null) {
                    searchModule.doStopSearch();
                    enableApplyButton(true);
                }
            }
        });

        if (isEditAllowed) {
            searchModule.setOnDblClickEvent(new OnDblClickEvent() {
                @Override
                public void onDblClick() {
                    DBDataObjectAttributes attributes = searchModule.getSelectedAttributes();
                    if (attributes != null) {
                        searchModule.doStopSearch();
                        enableApplyButton(true);
                        onButtonApplyAction(null);
                    }
                }
            });
        }

        searchModule.setOnStartSearchEvent(new OnStartSearchEvent() {
            @Override
            public void onStartSearch() {
                enableApplyButton(false);
            }
        });

        addToPanelGrid(searchModule.getGui());

    }

    @Override
    protected EtkDataPartListEntry buildPartListEntryFromMat(DBDataObjectAttributes attributes, int destLfdNr, String destSeqValue,
                                                             AssemblyId targetAssemblyId) {
        EtkDataPartListEntry partListEntry = super.buildPartListEntryFromMat(attributes, destLfdNr, destSeqValue, targetAssemblyId);
        if (partListEntry != null) {
            partListEntry.setFieldValue(DBConst.FIELD_K_SACH, partListEntry.getPart().getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);
            partListEntry.setFieldValue(DBConst.FIELD_K_SVER, partListEntry.getPart().getAsId().getMVer(), DBActionOrigin.FROM_EDIT);
        }
        return partListEntry;
    }

    @Override
    protected DBDataObjectAttributes getSelectedAttributes() {
        if (searchModule != null) {
            return searchModule.getSelectedAttributes();
        }
        return null;
    }

    @Override
    protected DBDataObjectAttributesList getSelectedAttributesList() {
        if (searchModule != null) {
            return searchModule.getSelectedAttributesList();
        }
        return null;
    }

    @Override
    protected SelectSearchGridMaterial getSearchForm() {
        return null;
    }

}
