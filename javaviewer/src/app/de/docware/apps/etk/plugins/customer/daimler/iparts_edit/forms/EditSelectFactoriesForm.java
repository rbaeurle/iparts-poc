package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactoriesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditSelectDataObjectsForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sort.SortStringCache;

import java.util.*;

/**
 * Formular zu Auswahl von Werken zum Produkt
 */
public class EditSelectFactoriesForm extends EditSelectDataObjectsForm {


    public static Collection<iPartsDataFactories> showSelectionFactories(AbstractJavaViewerForm parentForm,
                                                                         iPartsProductId productId,
                                                                         iPartsDataFactoriesList allFactories,
                                                                         Collection<iPartsDataFactories> factoriesForProduct) {
        EditSelectFactoriesForm dlg = new EditSelectFactoriesForm(parentForm.getConnector(), parentForm, allFactories, factoriesForProduct);
        dlg.setTitle(TranslationHandler.translate("!!Werke zu Produkt \"%1\"", productId.getProductNumber()));

        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getSelectedFactories();
        }
        return null;
    }

    /**
     * Erzeugt eine Instanz von EditSelectDataObjectsForm.
     *
     * @param dataConnector
     * @param parentForm
     */
    public EditSelectFactoriesForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                   iPartsDataFactoriesList allFactories,
                                   Collection<iPartsDataFactories> factoriesForProduct) {
        super(dataConnector, parentForm, iPartsConst.TABLE_DA_FACTORIES,
              iPartsEditConfigConst.iPARTS_EDIT_CONFIG_PRODUCT_FACTORIES_KEY,
              iPartsEditConfigConst.iPARTS_EDIT_CONFIG_PRODUCT_FACTORIES_KEY);

        setAvailableEntriesTitle("!!Verfügbare Werke:");
        setSelectedEntriesTitle("!!Ausgewählte Werke:");

        setWithDeleteEntry(true);
        setMoveEntriesVisible(false);

        List<EtkDataObject> allFactoriesList = new ArrayList<EtkDataObject>(allFactories.getAsList());
        fillAvailableEntries(allFactoriesList);

        List<EtkDataObject> factoriesForProductList = new ArrayList<EtkDataObject>(factoriesForProduct);
        doAddEntries(factoriesForProductList);
    }


    @Override
    protected EtkDisplayFields buildDefaultDisplayFields(boolean forSelectedEntries) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(searchTable);
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DF_LETTER_CODE));
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DF_FACTORY_NO));
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DF_DESC));
        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    public List<iPartsDataFactories> getSelectedFactories() {
        List<iPartsDataFactories> factoriesList = new DwList<iPartsDataFactories>();
        List<EtkDataObject> list = getCompleteSelectedList();
        if (list != null) {
            for (EtkDataObject dataObject : list) {
                if (dataObject instanceof iPartsDataFactories) {
                    factoriesList.add((iPartsDataFactories)dataObject);
                }
            }
        }
        return factoriesList;
    }

    /**
     * Werke nach der Werksnummer sortieren.
     *
     * @param selectedObjects
     */
    @Override
    protected void sortSelectedObjects(List<EtkDataObject> selectedObjects) {
        SortStringCache sortStringCache = new SortStringCache();
        Comparator<EtkDataObject> sortComparator = (o1, o2) -> {
            if ((o1 instanceof iPartsDataFactories) && (o2 instanceof iPartsDataFactories)) {
                return sortStringCache.getSortString(((iPartsDataFactories)o1).getFactoryNumber(), false)
                        .compareTo(sortStringCache.getSortString(((iPartsDataFactories)o2).getFactoryNumber(), false));
            }
            return 0;
        };
        Collections.sort(selectedObjects, sortComparator);
    }
}
