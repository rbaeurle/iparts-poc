/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkResponsiveDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.SimpleDoubleListSelectForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListForm;
import de.docware.apps.etk.base.misc.EtkTableHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.AbstractRelatedInfoMainForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Related Info Dialog für die Anzeige vom Grund für die Ausfilterung von Stücklisteneinträgen
 */
public class iPartsRelatedInfoFilterReasonDataForm extends AbstractRelatedInfoPartlistDataForm implements iPartsConst {

    private iPartsFilter filterWithFilterReason = new iPartsFilter();
    private AssemblyListForm assemblyListForm;
    private AssemblyId loadedAssemblyId;
    private List<EtkDataPartListEntry> filteredPartListEntries;

    /**
     * Erzeugt einen neuen Dialog für die Anzeige der Stückliste mit 3 neuen Spalten, die den Grund der Filterung enthalten
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsRelatedInfoFilterReasonDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    private void postCreateGui() {
        filterWithFilterReason.setWithFilterReason(true);

        // Connector mit ungefilterter Stückliste und Aufruf von filterWithFilterReason sowie Anzeige der Related Info
        // in einem neuen modalen Dialog
        iPartsAssemblyListSelectFormConnectorWithFilterSettings assemblyListFormConnectorUnfiltered = new iPartsAssemblyListSelectFormConnectorWithFilterSettings(getConnector()) {
            @Override
            public List<EtkDataPartListEntry> getCurrentPartListEntries() {
                if (filteredPartListEntries == null) {
                    List<EtkDataPartListEntry> unfilteresPartListEntries = getUnfilteredPartListEntries();
                    AssemblyId assemblyId = getCurrentAssembly().getAsId();
                    boolean isStructureParentNode = iPartsVirtualNode.isVirtualId(assemblyId) && iPartsVirtualNode.isStructureNode(iPartsVirtualNode.parseVirtualIds(assemblyId));
                    boolean productStructureWithAggregatesForSession = isStructureParentNode && iPartsProduct.isProductStructureWithAggregatesForSession();
                    filteredPartListEntries = new DwList<>(unfilteresPartListEntries.size());
                    for (EtkDataPartListEntry partListEntry : unfilteresPartListEntries) {
                        // Falls der Vater-Knoten ein Strukturknoten ist, dann überprüfen, ob es sich um einen Produktknoten
                        // handelt und dieser zur aktuellen Einstellung bzgl. der Anzeige von Aggregate-Produkten in Fahrzeug-Produkten
                        // passt -> falls nicht, dann diesen Stücklisteneintrag auch nicht in der ungefilterten Stücklisrte anzeigen
                        if (isStructureParentNode) {
                            List<iPartsVirtualNode> pleVirtualNodesPath = iPartsVirtualNode.parseVirtualIds(partListEntry.getDestinationAssemblyId());
                            if (!filterWithFilterReason.checkProductStructureWithAggregatesForVirtualNodePath(pleVirtualNodesPath,
                                                                                                              productStructureWithAggregatesForSession)) {
                                continue;
                            }
                        }

                        boolean addPartListEntry = true;
                        if (!filterWithFilterReason.checkFilter(partListEntry)) {
                            // Ausgefilterte PSK-Produkte auch in der ungefilterten Stückliste nicht anzeigen
                            if (partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTER_NAME).equals(iPartsFilterSwitchboard.FilterTypes.PSK.getDBValue())) {
                                addPartListEntry = false;
                            }
                        }
                        if (addPartListEntry) {
                            filteredPartListEntries.add(partListEntry);
                        }
                    }

                    // Nachfilterung der gesamten Stückliste
                    filteredPartListEntries = filterWithFilterReason.postFilterForFilteredPartList(filteredPartListEntries);
                }
                return filteredPartListEntries;
            }

            @Override
            public void showRelatedInfo(RelatedInfoFormConnector relatedConnector) {
                iPartsRelatedInfoFormConnectorWithFilterSettings connector = new iPartsRelatedInfoFormConnectorWithFilterSettings(relatedConnector);
                connector.setRelatedInfoData(relatedConnector.getRelatedInfoData());
                connector.setActiveRelatedSubForm(relatedConnector.getActiveRelatedSubForm());
                connector.setFilterActive(false);

                AbstractRelatedInfoMainForm instanceRelatedInfo = getFormFactory().createRelatedInfoMainForm(connector, assemblyListForm);
                instanceRelatedInfo.addOwnConnector(connector);

                instanceRelatedInfo.setRelatedInfoData(connector.getRelatedInfoData());
                instanceRelatedInfo.showModal();
            }
        };

        assemblyListFormConnectorUnfiltered.setFilterActive(false);

        // Anzeige der Stückliste mit den virtuellen Feldern für den Grund der Ausfilterung
        assemblyListForm = new IPartsRelatedInfoAssemblyListForm(assemblyListFormConnectorUnfiltered);
        assemblyListForm.setName("filterReasonForm");

        assemblyListForm.setTestHotspotsActive(false);

        // Stückliste anstatt des Platzhalters anzeigen
        mainWindow.panelAssemblyListPlaceholder.removeFromParent();
        assemblyListForm.getGui().setConstraints(mainWindow.panelAssemblyListPlaceholder.getConstraints());
        mainWindow.panelMain.addChild(assemblyListForm.getGui());
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        return null;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        AssemblyId currentAssemblyId = getConnector().getRelatedInfoData().getSachAssemblyId();
        if (((getConnector().getActiveRelatedSubForm() == this) || (getConnector().getActiveRelatedSubForm() == parentForm))
            && !Utils.objectEquals(currentAssemblyId, loadedAssemblyId)) {
            loadedAssemblyId = currentAssemblyId;
            updateAssemblyList();
        }
    }

    /**
     * Gibt die übergebene Liste aus Produkten als kommaseparierten String zurück
     *
     * @param products
     * @return
     */
    private String getTranslatedProductsStr(List<iPartsProduct> products) {
        if (products.size() < 1) {
            return "";
        } else {
            String productListStr = "";
            for (iPartsProduct product : products) {
                if (!productListStr.isEmpty()) {
                    productListStr += ", ";
                }
                productListStr += product.getAsId().getProductNumber();
            }
            return TranslationHandler.translate("!!Über %1 (%2) deaktivierte Filter:",
                                                (products.size() == 1) ? TranslationHandler.translate("!!Produkt")
                                                                       : TranslationHandler.translate("!!Produkte"),
                                                productListStr);
        }
    }

    private void updateAssemblyList() {
        iPartsPlugin.updateFilterButton(getConnector(), mainWindow.toolButtonFilter);

        if (loadedAssemblyId == null) {
            // Die Relatedinfo wurde für diesen Typ total falsch aufgerufen
            return;
        }

        // Aktuelle Datenkarte und ausgewählte Filter setzen
        filterWithFilterReason.setCurrentDataCard(iPartsFilter.get().getCurrentDataCard(), getProject());
        filterWithFilterReason.setSwitchboardState(iPartsFilter.get().getSwitchboardState());

        // Falls Unterbaugruppen ausgeblendet werden, müssen deren Stückliste angezeigt werden
        EtkProject project = getProject();
        EtkDataAssembly loadedAssembly = EtkDataObjectFactory.createDataAssembly(project, loadedAssemblyId);
        loadedAssembly = loadedAssembly.getLastHiddenSingleSubAssemblyOrThis(null);

        assemblyListForm.getConnector().setCurrentAssembly(loadedAssembly);
        filteredPartListEntries = null;
        assemblyListForm.getConnector().updateAllViews(this, false);

        // Label für die Filterdaten befüllen
        if (StrUtils.isValid(filterWithFilterReason.getCurrentDataCard().getModelNo()) && (loadedAssembly instanceof iPartsDataAssembly)) {
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)loadedAssembly;
            List<AbstractDataCard> dataCards = filterWithFilterReason.getRelevantDatacardsForAssembly(iPartsAssembly);
            if (!dataCards.isEmpty()) {
                iPartsFilter filter = iPartsFilter.get();
                iPartsDocumentationType documentationType = iPartsAssembly.getDocumentationType();
                String labelText;
                String labelDeactivatedFilterTypesText = "";
                if (dataCards.size() == 1) {
                    labelText = TranslationHandler.translate("!!Datenkarte für Filterung:") + " ";
                } else {
                    labelText = TranslationHandler.translate("!!Datenkarten für Filterung:") + " ";
                }
                boolean firstDataCard = true;
                boolean isEldasAggregateInDIALOGVehicle = false;
                for (AbstractDataCard dataCard : dataCards) {
                    if (!firstDataCard) {
                        labelText += "; ";
                    } else {
                        firstDataCard = false;
                    }
                    String dataCardText;
                    if (dataCard.isVehicleDataCard()) {
                        VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                        dataCardText = TranslationHandler.translate("!!Fahrzeug") + " ";
                        if (vehicleDataCard.getFinId().isValidId()) {
                            dataCardText += "FIN " + vehicleDataCard.getFinId().getFIN();
                            dataCardText += " " + TranslationHandler.translate("!!Baumuster") + " " + vehicleDataCard.getModelNo();
                        } else {
                            dataCardText += TranslationHandler.translate("!!Baumuster") + " " + vehicleDataCard.getModelNo();
                        }
                    } else {
                        AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCard;
                        dataCardText = TranslationHandler.translate(aggregateDataCard.getAggregateType().getDescription()) + " ";
                        String aggregateTypeOf = aggregateDataCard.getAggregateTypeOf().getDescription();
                        if (!aggregateTypeOf.isEmpty()) {
                            dataCardText += TranslationHandler.translate(aggregateTypeOf) + " ";
                        }
                        if (StrUtils.isValid(aggregateDataCard.getAggregateIdent())) {
                            dataCardText += TranslationHandler.translate("!!Ident") + " " + aggregateDataCard.getAggregateIdent();
                            dataCardText += " " + TranslationHandler.translate("!!Baumuster") + " " + aggregateDataCard.getModelNo();
                        } else {
                            dataCardText += TranslationHandler.translate("!!Baumuster") + " " + aggregateDataCard.getModelNo();
                        }
                    }
                    labelText += dataCardText;

                    // Deaktivierte (theoretisch aber im Filter aktive) Filtertypen für die Datenkarte bestimmen und als Text setzen
                    String deactivatedFilterTypesForDataCardText = "";
                    List<iPartsFilterSwitchboard.FilterTypes> deactivatedFilterTypes = filter.getDeactivatedFilterTypesForDataCard(dataCard, documentationType, project);
                    for (iPartsFilterSwitchboard.FilterTypes deactivatedFilterType : deactivatedFilterTypes) {
                        if (filter.getSwitchboardState().isFilterActivated(deactivatedFilterType)) {
                            if (deactivatedFilterTypesForDataCardText.isEmpty()) {
                                if (dataCards.size() == 1) {
                                    deactivatedFilterTypesForDataCardText = TranslationHandler.translate("!!Deaktivierte Filter:");
                                } else {
                                    deactivatedFilterTypesForDataCardText = TranslationHandler.translate("!!Deaktivierte Filter für %1:", dataCardText);
                                }
                                deactivatedFilterTypesForDataCardText += ' ';
                            } else {
                                deactivatedFilterTypesForDataCardText += ", ";
                            }
                            deactivatedFilterTypesForDataCardText += deactivatedFilterType.getDescription(project);
                        }
                    }

                    // DAIMLER-6774, jetzt können Filter über das Produkt AB-geschaltet werden.
                    // Diese Filter sind nicht aktivierbar, sie sind in der Oberfläche komplett deaktiviert.
                    String deactivatedFilterTypesForProductsText = "";
                    List<iPartsProduct> products = filterWithFilterReason.getProductListForDataCard(project, dataCard, true);
                    String translatedProductsStr = getTranslatedProductsStr(products);
                    Set<iPartsFilterSwitchboard.FilterTypes> disabledProductFiltersList = iPartsFilterHelper.getDisabledFilters(products);
                    for (iPartsFilterSwitchboard.FilterTypes deactivatedFilterType : disabledProductFiltersList) {
                        if (deactivatedFilterTypesForProductsText.isEmpty()) {
                            if (dataCards.size() == 1) {
                                deactivatedFilterTypesForProductsText = translatedProductsStr;
                            } else {
                                deactivatedFilterTypesForProductsText = TranslationHandler.translate("!!%1 für %2:", translatedProductsStr, dataCardText);
                            }
                            deactivatedFilterTypesForProductsText += ' ';
                        } else {
                            deactivatedFilterTypesForProductsText += ", ";
                        }
                        deactivatedFilterTypesForProductsText += deactivatedFilterType.getDescription(project);
                    }

                    // Das Label mit den Texten bestücken:
                    if (!deactivatedFilterTypesForDataCardText.isEmpty()) {
                        labelDeactivatedFilterTypesText += '\n' + deactivatedFilterTypesForDataCardText;
                    }
                    if (!deactivatedFilterTypesForProductsText.isEmpty()) {
                        labelDeactivatedFilterTypesText += '\n' + deactivatedFilterTypesForProductsText;
                    }

                    if (!isEldasAggregateInDIALOGVehicle && (dataCard instanceof AggregateDataCard)) {
                        isEldasAggregateInDIALOGVehicle = ((AggregateDataCard)dataCard).isEldasAggregateInDIALOGVehicle(project);
                    }
                }

                // DAIMLER-7200: Baumusterfilterung bei ELDAS-Aggregaten wenn Fahrzeugbaureihen DIALOG und es eine ELDAS-Stückliste ist
                if (isEldasAggregateInDIALOGVehicle && iPartsAssembly.getDocumentationType().isTruckDocumentationType()) {
                    if (!labelDeactivatedFilterTypesText.isEmpty()) {
                        labelDeactivatedFilterTypesText += '\n';
                    }
                    labelDeactivatedFilterTypesText += TranslationHandler.translate("!!Reduzierte Filterung für ELDAS-Aggregat in DIALOG-Fahrzeug:");
                    boolean firstFilter = true;
                    for (iPartsFilterSwitchboard.FilterTypes filterType : iPartsFilter.VALID_ELDAS_AGGREGATE_IN_DIALOG_VEHICLE_FILTER_TYPES) {
                        if (filterType != iPartsFilterSwitchboard.FilterTypes.AGG_MODELS) { // Aggregate-Filter hier nicht auflisten
                            if (firstFilter) {
                                labelDeactivatedFilterTypesText += " ";
                                firstFilter = false;
                            } else {
                                labelDeactivatedFilterTypesText += ", ";
                            }
                            labelDeactivatedFilterTypesText += filterType.getDescription(project);
                        }
                    }
                }

                mainWindow.labelDataCardOrModel.setText(labelText + labelDeactivatedFilterTypesText);
            } else {
                mainWindow.labelDataCardOrModel.setText("!!Keine passende Datenkarte für die Filterung dieser Stückliste gefunden.");
            }
        } else {
            mainWindow.labelDataCardOrModel.setText("");
        }
    }

    private void closeWindow(Event event) {
        close();
        mainWindow.setVisible(false);
    }

    private void showFilter(Event event) {
        if (iPartsPlugin.showiPartsFilterDialog(getConnector())) {
            updateAssemblyList();
        }
    }


    private class IPartsRelatedInfoAssemblyListForm extends AssemblyListForm {

        public IPartsRelatedInfoAssemblyListForm(iPartsAssemblyListSelectFormConnectorWithFilterSettings assemblyListFormConnectorUnfiltered) {
            super(assemblyListFormConnectorUnfiltered, iPartsRelatedInfoFilterReasonDataForm.this);
            hideEmptyPlaceHolder = true;
        }

        @Override
        protected EtkTableHelper createTableHelper() {
            List<EtkDisplayField> iPartsFilterDisplayFields = new ArrayList<>();

            // Virtuelle Felder für den Grund der Ausfilterung von Stücklisteneinträgen als erste Felder hinzufügen
            addVirtualFilterReasonField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED, iPartsFilterDisplayFields, 10);
            addVirtualFilterReasonField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTER_NAME, iPartsFilterDisplayFields, 20);
            EtkDisplayField filterReasonDescriptionField = addVirtualFilterReasonField(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_DESCRIPTION,
                                                                                       iPartsFilterDisplayFields, 50);
            filterReasonDescriptionField.setGrowColumn(true);

            // Normale Felder vom Stücklistentyp hinzufügen
            EtkTableHelper originalTableHelper = super.createTableHelper();
            iPartsFilterDisplayFields.addAll(originalTableHelper.getDesktopDisplayList());

            EtkResponsiveDisplayFields localDisplayList = new EtkResponsiveDisplayFields(getProject(), iPartsFilterDisplayFields,
                                                                                         originalTableHelper.getFixedColumnsCount());

            return new EtkTableHelper(localDisplayList);
        }

        private EtkDisplayField addVirtualFilterReasonField(String virtualFieldName, List<EtkDisplayField> localDisplayList,
                                                            int width) {
            EtkDisplayField displayField = new EtkDisplayField(TABLE_KATALOG, virtualFieldName, false, false);
            displayField.loadStandards(getProject().getConfig());
            displayField.setDefaultWidth(false);
            displayField.setWidth(width);
            displayField.setColumnFilterEnabled(true);
            displayField.setInFlyerAnzeigen(true);
            displayField.setInRelatedInfoAnzeigen(true);
            localDisplayList.add(displayField);
            return displayField;
        }

        @Override
        protected String getEbeneNameForSessionSave() {
            return TableAndFieldName.make(getCurrentAssembly().getEbeneName(), SimpleDoubleListSelectForm.PARTLIST_SOURCE_FILTER);
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolButton toolButtonFilter;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel FQU63830461;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDataCardOrModel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelAssemblyListPlaceholder;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setTitle("!!Ungefilterte Stückliste");
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            panelTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTop.setName("panelTop");
            panelTop.__internal_setGenerationDpi(96);
            panelTop.registerTranslationHandler(translationHandler);
            panelTop.setScaleForResolution(true);
            panelTop.setMinimumWidth(10);
            panelTop.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelTopLayout.setCentered(false);
            panelTop.setLayout(panelTopLayout);
            toolbar = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbar.setName("toolbar");
            toolbar.__internal_setGenerationDpi(96);
            toolbar.registerTranslationHandler(translationHandler);
            toolbar.setScaleForResolution(true);
            toolbar.setMinimumWidth(10);
            toolbar.setMinimumHeight(10);
            toolbar.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarSmallBackground"));
            toolbar.setButtonLayout(de.docware.framework.modules.gui.controls.toolbar.ToolButtonLayout.IMAGE_WEST);
            toolbar.setButtonStyle(de.docware.framework.modules.gui.controls.toolbar.ToolButtonStyle.SMALL);
            toolButtonFilter = new de.docware.framework.modules.gui.controls.toolbar.GuiToolButton();
            toolButtonFilter.setName("toolButtonFilter");
            toolButtonFilter.__internal_setGenerationDpi(96);
            toolButtonFilter.registerTranslationHandler(translationHandler);
            toolButtonFilter.setScaleForResolution(true);
            toolButtonFilter.setMinimumWidth(10);
            toolButtonFilter.setMinimumHeight(10);
            toolButtonFilter.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            FQU63830461 = new de.docware.framework.modules.gui.controls.GuiLabel();
            FQU63830461.setName("FQU63830461");
            FQU63830461.__internal_setGenerationDpi(96);
            FQU63830461.registerTranslationHandler(translationHandler);
            FQU63830461.setScaleForResolution(true);
            FQU63830461.setText("!!Filter");
            toolButtonFilter.setTooltip(FQU63830461);
            toolButtonFilter.setGlyph(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignFilter"));
            toolButtonFilter.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    showFilter(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag toolButtonFilterConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 100.0, "w", "v", 2, 2, 2, 2);
            toolButtonFilter.setConstraints(toolButtonFilterConstraints);
            toolbar.addChild(toolButtonFilter);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag toolbarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "n", "n", 4, 0, 0, 0);
            toolbar.setConstraints(toolbarConstraints);
            panelTop.addChild(toolbar);
            labelDataCardOrModel = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDataCardOrModel.setName("labelDataCardOrModel");
            labelDataCardOrModel.__internal_setGenerationDpi(96);
            labelDataCardOrModel.registerTranslationHandler(translationHandler);
            labelDataCardOrModel.setScaleForResolution(true);
            labelDataCardOrModel.setMinimumWidth(10);
            labelDataCardOrModel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelDataCardOrModelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 1, 8, 0, 4);
            labelDataCardOrModel.setConstraints(labelDataCardOrModelConstraints);
            panelTop.addChild(labelDataCardOrModel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelTopConstraints.setPosition("north");
            panelTop.setConstraints(panelTopConstraints);
            panelMain.addChild(panelTop);
            panelAssemblyListPlaceholder = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelAssemblyListPlaceholder.setName("panelAssemblyListPlaceholder");
            panelAssemblyListPlaceholder.__internal_setGenerationDpi(96);
            panelAssemblyListPlaceholder.registerTranslationHandler(translationHandler);
            panelAssemblyListPlaceholder.setScaleForResolution(true);
            panelAssemblyListPlaceholder.setMinimumWidth(10);
            panelAssemblyListPlaceholder.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelAssemblyListPlaceholderLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelAssemblyListPlaceholder.setLayout(panelAssemblyListPlaceholderLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelAssemblyListPlaceholderConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelAssemblyListPlaceholder.setConstraints(panelAssemblyListPlaceholderConstraints);
            panelMain.addChild(panelAssemblyListPlaceholder);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCloseActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    closeWindow(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}