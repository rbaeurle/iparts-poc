/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductFactoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sort.SortUtils;

import java.util.*;

/**
 * Textfield zur Auswahl und Darstellung der Relation Werke zu Produkt
 * Die Daten stehen in der Tabelle DA_PRODUCT_FACTORIES.
 */
public class iPartsGuiFactorySelectTextField extends AbstractProductRelatedButtonTextField {

    public static final String TYPE = "iPartsGuiFactorySelectTextField";

    private Collection<iPartsDataFactories> selectedFactories;
    private Collection<iPartsDataFactories> initialFactories;
    private int maxTextLength = 66;


    public iPartsGuiFactorySelectTextField(EtkProject project) {
        super(project);
        initialFactories = new DwList<>();
        selectedFactories = new DwList<>();
    }

    @Override
    public void init(AbstractJavaViewerForm parentForm, iPartsProductId productId) {
        // Alle Werke aus DA_FACTORIES laden
        final iPartsDataFactoriesList allFactories = new iPartsDataFactoriesList();
        allFactories.loadSortedByFactoryNumber(getProject());

        HashMap<String, iPartsDataFactories> allFactoriesMap = new HashMap<String, iPartsDataFactories>();
        for (iPartsDataFactories factory : allFactories) {
            allFactoriesMap.put(factory.getFactoryNumber(), factory);
        }

        if (productId != null) {
            setProductId(productId);
            // Alle Werke die dem Produkt zugeordnet wurden laden ...
            iPartsDataProductFactoryList factoryForProductList = iPartsDataProductFactoryList.loadDataProductFactoryListForProduct(getProject(), productId);
            // ... und die dazugehörigen Einträge aus DA_FACTORIES bestimmen
            selectedFactories = new DwList<iPartsDataFactories>();
            for (iPartsDataProductFactory dataProductFactory : factoryForProductList) {
                String factoryNumber = dataProductFactory.getAsId().getFactoryNumber();
                iPartsDataFactories dataFactories = allFactoriesMap.get(factoryNumber);
                if (dataFactories == null) {
                    // Für den unwahrscheinlichen Fall, dass in DA_PRODUCT_FACTORIES eine Werksnummer angegeben ist,
                    // die nicht in DA_FACTORIES enthalten ist
                    dataFactories = new iPartsDataFactories(getProject(), new iPartsFactoriesId());
                    dataFactories.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    EtkMultiSprache notFoundDesc = new EtkMultiSprache();
                    notFoundDesc.setText(getProject().getViewerLanguage(), "!!Werk nicht gefunden");
                    dataFactories.setFieldValue(iPartsConst.FIELD_DF_FACTORY_NO, factoryNumber, DBActionOrigin.FROM_EDIT);
                    dataFactories.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DF_DESC, notFoundDesc, DBActionOrigin.FROM_EDIT);
                }
                selectedFactories.add(dataFactories);
            }

            // Bereits zugeordnete Werke in das Textfield eintragen
            setText(factoriesToString(selectedFactories));
            initialFactories = selectedFactories;

            if (parentForm != null) {
                addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        Collection<iPartsDataFactories> factories = EditSelectFactoriesForm.showSelectionFactories(parentForm, productId, allFactories, selectedFactories);
                        if (factories != null) {
                            // Neu zugeordnete Werke ins das Textfield eintragen und zwischenspeichern
                            selectedFactories = factories;
                            setText(factoriesToString(selectedFactories));
                        }
                    }
                });
            } else {
                removeEventListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
            }
        }
    }

    private String factoriesToString(Collection<iPartsDataFactories> factories) {
        List<String> factoryNumbers = new DwList<String>(factories.size());
        for (iPartsDataFactories dataFactories : factories) {
            factoryNumbers.add(dataFactories.getFactoryNumber()); // Werksnummer ausgeben
        }
        SortUtils.sortList(factoryNumbers, true, true, true);
        return StrUtils.makeAbbreviation(StrUtils.stringListToString(factoryNumbers, ", "), maxTextLength);
    }

    public Set<String> getSelectedFactoryNumbers() {
        Set<String> factoryNumbers = new TreeSet<String>();
        for (iPartsDataFactories selectedFactory : selectedFactories) {
            factoryNumbers.add(selectedFactory.getFactoryNumber());
        }
        return factoryNumbers;
    }

    public Collection<iPartsDataFactories> getFactoriesDeleted() {
        List<iPartsDataFactories> result = new DwList<iPartsDataFactories>();
        for (iPartsDataFactories initialFactory : initialFactories) {
            if (!selectedFactories.contains(initialFactory)) {
                result.add(initialFactory);
            }
        }
        return result;
    }

    public Collection<iPartsDataFactories> getFactoriesNew() {
        List<iPartsDataFactories> result = new DwList<iPartsDataFactories>();
        for (iPartsDataFactories selectedFactory : selectedFactories) {
            if (!initialFactories.contains(selectedFactory)) {
                result.add(selectedFactory);
            }
        }
        return result;
    }

    @Override
    public boolean isModified() {
        if (initialFactories.size() != selectedFactories.size()) {
            return true;
        }
        if (!initialFactories.isEmpty()) {
            return !initialFactories.containsAll(selectedFactories);
        }
        return false;
    }

    public void saveData() {
        EtkProject project = getProject();
        iPartsProductId productId = getProductId();
        if ((productId != null) && (getProject() != null)) {
            iPartsDataProductFactoryList list = new iPartsDataProductFactoryList();
            for (iPartsDataFactories selectedFactory : selectedFactories) {
                iPartsDataProductFactory productFactory = new iPartsDataProductFactory(project, new iPartsProductFactoryId(productId.getProductNumber(), selectedFactory.getFactoryNumber()));
                if (!productFactory.existsInDB()) {
                    productFactory.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    productFactory.setFieldValueAsDateTime(iPartsConst.FIELD_DPF_EDAT, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
                    productFactory.setFieldValueAsDateTime(iPartsConst.FIELD_DPF_ADAT, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
                    list.add(productFactory, DBActionOrigin.FROM_EDIT);
                }
            }
            for (iPartsDataFactories deleteFactory : getFactoriesDeleted()) {
                iPartsDataProductFactory productFactory = new iPartsDataProductFactory(project, new iPartsProductFactoryId(productId.getProductNumber(), deleteFactory.getFactoryNumber()));
                list.delete(productFactory, true, DBActionOrigin.FROM_EDIT);
            }
            project.getDbLayer().startTransaction();
            try {
                list.saveToDB(project);
                project.getDbLayer().commit();
            } catch (RuntimeException e) {
                project.getDbLayer().rollback();
                Logger.getLogger().handleRuntimeException(e);
            }
        }
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }
}