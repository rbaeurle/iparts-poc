/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditSelectDataObjectsForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Formular zur Baumusterauswahl.
 */
public class EditSelectModelsForm extends EditSelectDataObjectsForm {

    public static final int MINIMUM_MODEL_NUMBER_LENGTH_FOR_SEARCH = 4;

    private final GuiPanel modelNumberSearchPanel;
    private final GuiLabel modelNumberSearchLabel;
    private final GuiTextField modelNumberSearchTextField;
    private iPartsProductId productId;
    protected Collection<String> modelList;
    private boolean carAndVanForFilter = iPartsRight.checkCarAndVanInSession();
    private boolean truckAndBusForFilter = iPartsRight.checkTruckAndBusInSession();

    /**
     * Zeigt den Baumusterauswahldialog mit den übergebenen Daten, wobei <i>productId</i> und <i>partialModelNumberWithWildCard</i>
     * auch kombiniert werden können.
     *
     * @param parentForm
     * @param productId                      {@link iPartsProductId} für eine Liste der verfügbaren Baumuster zu diesem Produkt
     * @param partialModelNumberWithWildCard (Partielle) Baumusternummer, die auch Wildcards enthalten kann, für die
     *                                       Suche/Vorfilterung der verfügbaren Baumuster
     * @param modelNumberSearchFieldVisible  Flag, ob das Suchfeld für die Baumusternummer angezeigt werden soll
     * @param modelList                      Liste der bisher ausgewählten Baumuster
     * @return Liste der ausgewählten Baumuster
     */
    public static Collection<String> showSelectionModels(AbstractJavaViewerForm parentForm, iPartsProductId productId,
                                                         String partialModelNumberWithWildCard, boolean modelNumberSearchFieldVisible,
                                                         Collection<String> modelList) {
        return showSelectionModels(parentForm, productId, partialModelNumberWithWildCard, modelNumberSearchFieldVisible,
                                   modelList, null);
    }

    /**
     * Zeigt den Baumusterauswahldialog mit den übergebenen Daten, wobei <i>productId</i> und <i>partialModelNumberWithWildCard</i>
     * auch kombiniert werden können.
     * Ist <i>productCharacteristics</i> gesetzt, so wird die Focus-Einschränkung nur für dieses Produkt vorgenommen.
     *
     * @param parentForm
     * @param productId
     * @param partialModelNumberWithWildCard
     * @param modelNumberSearchFieldVisible
     * @param modelList
     * @param masterDataProductCharacteristics
     * @return
     */
    public static Collection<String> showSelectionModels(AbstractJavaViewerForm parentForm, iPartsProductId productId,
                                                         String partialModelNumberWithWildCard, boolean modelNumberSearchFieldVisible,
                                                         Collection<String> modelList,
                                                         MasterDataProductCharacteristics masterDataProductCharacteristics) {
        EditSelectModelsForm dlg = new EditSelectModelsForm(parentForm.getConnector(), parentForm, modelList);
        dlg.setProductId(productId);
        if (masterDataProductCharacteristics != null) {
            dlg.setRightsFromProduct(masterDataProductCharacteristics);
        }
        dlg.setModelNumberSearchFieldVisible(modelNumberSearchFieldVisible);
        dlg.setModelNumberSearchValue(partialModelNumberWithWildCard, true); // befüllt die verfügbaren Baumuster

        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getSelectedModelsList();
        }
        return null;
    }

    /**
     * Erzeugt eine Instanz von EditSelectModelsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditSelectModelsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, Collection<String> modelList) {
        this(dataConnector, parentForm, iPartsConst.TABLE_DA_MODEL, modelList,
             iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
             iPartsEditConfigConst.iPARTS_EDIT_MASTER_MODEL_AFTER_SALES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
    }

    public EditSelectModelsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                String tableName, Collection<String> modelList,
                                String configKeyForAvailableEntriesDisplayFields, String configKeyForSelectedEntriesDisplayFields) {
        super(dataConnector, parentForm, tableName, configKeyForAvailableEntriesDisplayFields, configKeyForSelectedEntriesDisplayFields);
        setName("SelectModelsForm");
        setAvailableEntriesTitle("!!Verfügbare Baumuster:");
        setSelectedEntriesTitle("!!Ausgewählte Baumuster:");
        availableEntriesGrid.setNoResultsLabelText("!!Keine Baumuster");
        selectedEntriesGrid.setNoResultsLabelText("!!Keine Baumuster");
        setWithDeleteEntry(true);
        if (modelList == null) {
            modelList = new DwList<>();
        }
        this.modelList = modelList;
        fillSelectedModelsList(modelList);
        setMoveEntriesVisible(false);

        // Panel für das Baumusternummer-Suchfeld erzeugen
        modelNumberSearchPanel = new GuiPanel(new LayoutGridBag());

        modelNumberSearchLabel = new GuiLabel("!!Baumustersuche");
        modelNumberSearchLabel.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                                     ConstraintsGridBag.FILL_NONE, 4, 4, 4, 8));
        modelNumberSearchPanel.addChild(modelNumberSearchLabel);

        modelNumberSearchTextField = new GuiTextField();
        modelNumberSearchTextField.setName("modelNumberSearchTextField");
        modelNumberSearchTextField.setMinimumWidth(100);
        modelNumberSearchTextField.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 1, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                                         ConstraintsGridBag.FILL_VERTICAL, 4, 0, 4, 4));
        modelNumberSearchPanel.addChild(modelNumberSearchTextField);

        modelNumberSearchTextField.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                searchAndFillAvailableModels();
            }
        });

        modelNumberSearchPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_NORTH));
        getPanelForFurtherElements().addChild(modelNumberSearchPanel);

        modelNumberSearchTextField.requestFocus();
    }

    protected String getModelNumberSearchText() {
        return modelNumberSearchTextField.getText().trim();
    }

    protected void setModelNumberSearchTextEditable(boolean editable) {
        modelNumberSearchTextField.setEditable(editable);
    }

    protected void setModelNumberSearchLabelText(String text) {
        modelNumberSearchLabel.setText(text);
    }

    public void fillAvailableModelsList(iPartsProductId productId, String partialModelNumberWithWildCard) {
        setProductId(productId);
        if ((productId != null) && productId.isValidId()) {
            Set<String> productModels = iPartsProduct.getInstance(getProject(), productId).getModelNumbers(getProject());
            boolean checkModelNumberMatch = StrUtils.isValid(partialModelNumberWithWildCard);
            List<String> modelList = new DwList<>();
            for (String modelNumber : productModels) {
                if (!checkModelNumberMatch || StrUtils.matchesSqlLike(partialModelNumberWithWildCard, modelNumber, false)) {
                    modelList.add(modelNumber);
                }
            }
            fillAvailableModelsList(modelList);
        } else {
            fillAvailableModelsList(partialModelNumberWithWildCard.toUpperCase());
        }
    }

    public void fillAvailableModelsList(String partialModelNumberWithWildCard) {
        setProductId(null);
        if (StrUtils.isValid(partialModelNumberWithWildCard)) {
            iPartsDataModelList dataModelList = iPartsDataModelList.loadDataModelListForModelNumberWithWildCards(getProject(),
                                                                                                                 partialModelNumberWithWildCard);
            List<String> modelList = new DwList<>();
            for (iPartsDataModel dataModel : dataModelList) {
                String source = dataModel.getFieldValue(FIELD_DM_SOURCE);
                String modelNo = dataModel.getAsId().getModelNumber();
                if (iPartsFilterHelper.isASModelVisibleForUserInSession(modelNo, source, carAndVanForFilter,
                                                                        truckAndBusForFilter, getProject())) {
                    modelList.add(dataModel.getAsId().getModelNumber());
                }
            }
            fillAvailableModelsList(modelList);
        } else {
            fillAvailableModelsList(new DwList<>());
        }
    }

    public void fillAvailableModelsList(Collection<String> modelList) {
        List<EtkDataObject> availableList = new DwList<>();
        for (String model : modelList) {
            iPartsModelId modelId = new iPartsModelId(model);
            iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
            availableList.add(dataModel);
        }
        fillAvailableEntries(availableList);

        // Bei leerer Liste und vorhandenem Suchtext für die Baumuster das Label für keine Baumuster anzeigen
        if (availableList.isEmpty() && !getModelNumberSearchText().isEmpty()) {
            availableEntriesGrid.showNoResultsLabel(true);
        }

        doEnableOKButton();
    }

    public void fillSelectedModelsList(Collection<String> modelList) {
        List<EtkDataObject> selectedList = new DwList<>();
        for (String model : modelList) {
            iPartsModelId modelId = new iPartsModelId(model);
            iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
            selectedList.add(dataModel);
        }
        initSelectedEntries(selectedList);
        doEnableOKButton();
    }

    public List<String> getSelectedModelsList() {
        List<String> modelList = new DwList<>();
        List<EtkDataObject> list = getCompleteSelectedList();
        if (list != null) {
            for (EtkDataObject dataObject : list) {
                if (dataObject instanceof iPartsDataModel) {
                    modelList.add(dataObject.getAsId().getValue(1));
                }
            }
        }
        return modelList;
    }

    @Override
    protected boolean areEntriesChanged() {
        if (modelList != null) {
            Collection<String> selectedModelsList = getSelectedModelsList();
            if (modelList.size() != selectedModelsList.size()) {
                return true;
            } else {
                return !modelList.containsAll(selectedModelsList);
            }
        }
        return true;
    }

    @Override
    protected EtkDisplayFields buildDefaultDisplayFields(boolean forSelectedEntries) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(searchTable);
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DM_MODEL_NO));
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DM_SERIES_NO));
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DM_SALES_TITLE));
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DM_DEVELOPMENT_TITLE));
        displayFields.addFeld(createDisplayField(tableDef, iPartsConst.FIELD_DM_CODE));
        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    /**
     * Setzt die {@link iPartsProductId} für eine Liste der verfügbaren Baumuster zu diesem Produkt.
     *
     * @param productId
     */
    protected void setProductId(iPartsProductId productId) {
        this.productId = productId;
        if (productId != null) {
            setTitle(TranslationHandler.translate("!!Baumuster auswählen (Alle Baumuster des Produkts \"%1\")",
                                                  productId.getProductNumber()));
        } else {
            setTitle("!!Baumuster auswählen");
        }
    }

    protected void setRightsFromProduct(MasterDataProductCharacteristics masterDataProductCharacteristics) {
        // Sind die Eigenschaften eines darüber liegenden Produkts vorhanden, soll das BM in Abhängigkeit
        // davon angezeigt werden und nicht in Abhängigkeit des Benutzers:
        // Ist das Produkt ein PKW Produkt, dann darf das BM nur angezeigt werden, wenn es eine passende
        // Quelle besitzt oder in Produkten vorkommt, die ebenfalls den Typ "PKW" haben. Truck BM werden
        // analog dazu gefiltert. BMn, die nur in PSK Produkten vorkommen, dürfen nur von PSK Benutzer
        // gesehen werden.
        // Zur Sicherheit werden die Eigenschaften des Benutzers und die aktuellen Eigenschaften des Produkts UND-verknüpft
        this.carAndVanForFilter &= masterDataProductCharacteristics.isCarAndVanProduct();
        this.truckAndBusForFilter &= masterDataProductCharacteristics.isTruckAndBusProduct();
    }

    /**
     * Flag, ob das Suchfeld für die Baumusternummer angezeigt werden soll.
     *
     * @param visible
     */
    public void setModelNumberSearchFieldVisible(boolean visible) {
        modelNumberSearchPanel.setVisible(visible);
    }

    /**
     * Setzt die (partielle) Baumusternummer, die auch Wildcards enthalten kann, für die Suche/Vorfilterung der verfügbaren
     * Baumuster.
     *
     * @param modelNumberSearchValue
     * @param startSearch
     */
    public void setModelNumberSearchValue(String modelNumberSearchValue, boolean startSearch) {
        modelNumberSearchTextField.switchOffEventListeners();
        try {
            if (StrUtils.isValid(modelNumberSearchValue)) {
                modelNumberSearchTextField.setText(modelNumberSearchValue);
            } else {
                modelNumberSearchTextField.setText("");
            }
        } finally {
            modelNumberSearchTextField.switchOnEventListeners();
        }

        if (startSearch) {
            searchAndFillAvailableModels();
        }
    }

    private void searchAndFillAvailableModels() {
        // Ohne Thread und Abwarten wie bei generischen Suchen sofort die Baumuster bestimmen, da es maximal 1000
        // Baumuster sein können bei mindestens 4 Stellen bzw. nur die Baumuster eines Produkts
        String modelNumberSearchValue = getModelNumberSearchText();
        String valueWithoutWildcards = StrUtils.removeCharsFromString(modelNumberSearchValue, new char[]{ '*', '?' });
        if (((productId != null) && productId.isValidId()) || (valueWithoutWildcards.length() >= MINIMUM_MODEL_NUMBER_LENGTH_FOR_SEARCH)) {
            availableEntriesGrid.setNoResultsLabelText("!!Keine Baumuster");
            availableEntriesGrid.showNoResultsLabel(false);
            WildCardSettings wildCardSettings = new WildCardSettings();
            wildCardSettings.addNoAutoWildCard();
            wildCardSettings.addWildCardEnd();
            modelNumberSearchValue = wildCardSettings.makeWildCard(modelNumberSearchValue);
            fillAvailableModelsList(productId, modelNumberSearchValue);
        } else { // Zu wenig Stellen für die Suche
            if (modelNumberSearchPanel.isVisible()) {
                availableEntriesGrid.setNoResultsLabelText("!!Minimale Anzahl von Zeichen für Suche nicht erreicht");
                availableEntriesGrid.clearGrid();
                availableEntriesGrid.showNoResultsLabel(!modelNumberSearchValue.isEmpty());
            }
        }
    }
}
