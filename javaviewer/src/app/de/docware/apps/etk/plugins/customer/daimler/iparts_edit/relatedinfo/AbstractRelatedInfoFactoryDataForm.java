/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ResponseDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseDataWithSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.AbstractTwoDataObjectGridsEditForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.DataObjectFilterGridWithStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.HTMLUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * Abstraktes Formular für die Werkseinsatzdaten innerhalb der RelatedInfo inkl. Rückmeldedaten.
 */
public abstract class AbstractRelatedInfoFactoryDataForm extends AbstractTwoDataObjectGridsEditForm implements iPartsSuperEditRelatedInfoInterface {

    public static final String IPARTS_MENU_ITEM_SHOW_RESPONSE_DATA = "iPartsMenuItemShowResponseData";
    protected static final String LINK_SEPARATOR_NAME = "linkSeparator";

    public static GuiMenuItem createResponseDataMenuItem(final ResponseDataCallback callback, final AbstractRelatedInfoFactoryDataForm parentForm) {
        GuiMenuItem menuItem = new GuiMenuItem();
        menuItem.setUserObject(IPARTS_MENU_ITEM_SHOW_RESPONSE_DATA);
        menuItem.setName(IPARTS_MENU_ITEM_SHOW_RESPONSE_DATA);
        menuItem.setText(TranslationHandler.translate(iPartsConst.RELATED_INFO_RESPONSE_DATA_TEXT));
        menuItem.setIcon(EditDefaultImages.edit_history.getImage());
        menuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                ResponseDataQuery responseDataQuery = callback.getResponseDataQuery();
                iPartsResponseDataForm responseDataForm = new iPartsResponseDataForm(parentForm, responseDataQuery);
                responseDataForm.showModal();
                parentForm.dataChanged();
            }
        });

        return menuItem;
    }

    // Map von PEM auf Rückmeldedaten inkl. Ausreißer
    // für Retailfilter: Daten vom Stücklisteneintrag bzw. Variantentabelle
    // Ungefiltert: Daten aus der Datenbank
    protected Map<String, List<iPartsResponseDataWithSpikes>> responseDataForPemRetailFilter;
    protected Map<String, List<iPartsResponseDataWithSpikes>> responseDataForPemUnfiltered;

    // falls der Dialog modal verwendet wird, müssen beide auf denselben Stücklisteneintrag verweisen
    protected PartListEntryId loadedPartListEntryId;
    protected EtkDataPartListEntry partListEntry;
    protected boolean isRetailPartList; // AS- oder Konstruktionsstückliste

    protected AbstractRelatedInfoFactoryDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 IEtkRelatedInfo relatedInfo, String configKeyTop, String dataObjectGridTopTitle,
                                                 String configKeyBottom, String dataObjectGridBottomTitle, boolean scaleFromParent,
                                                 boolean enableEditMode) {
        super(dataConnector, parentForm, relatedInfo, configKeyTop, dataObjectGridTopTitle, configKeyBottom, dataObjectGridBottomTitle, enableEditMode);
        init(scaleFromParent);
    }

    protected AbstractRelatedInfoFactoryDataForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 String configKeyTop, String dataObjectGridTopTitle, String configKeyBottom,
                                                 String dataObjectGridBottomTitle, boolean scaleFromParent, boolean enableEditMode) {
        super(dataConnector, parentForm, configKeyTop, dataObjectGridTopTitle, configKeyBottom, dataObjectGridBottomTitle, enableEditMode);
        init(scaleFromParent);

    }

    private void init(boolean scaleFromParent) {
        if (scaleFromParent) {
            scaleFromParentForm(getWindow());
        }
        responseDataForPemRetailFilter = new HashMap<>();
        responseDataForPemUnfiltered = new HashMap<>();

        EventListener calculateOptimalHeightEventListener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                calculateOptimalHeight();
            }
        };
        getCheckboxRetailFilter().addEventListener(calculateOptimalHeightEventListener);
        getCheckboxShowHistory().addEventListener(calculateOptimalHeightEventListener);
    }

    /**
     * Überschreiben damit wir das Kontextmenü erweitern können
     *
     * @param top
     * @return
     */
    @Override
    protected DataObjectGrid createGrid(final boolean top) {
        EtkDisplayFields displayFields = getDisplayFields(top);

        // Titel der PEM ab/bis Spalten bestimmen
        EtkDisplayField field = displayFields.getFeldByName(getFactoryDataTableName(), getPemFromFieldName());
        final String pemFromTitle;
        if (field != null) {
            pemFromTitle = field.getText().getText(getProject().getViewerLanguage());
        } else {
            pemFromTitle = "!!PEM ab";
        }

        field = displayFields.getFeldByName(getFactoryDataTableName(), getPemToFieldName());
        final String pemToTitle;
        if (field != null) {
            pemToTitle = field.getText().getText(getProject().getViewerLanguage());
        } else {
            pemToTitle = "!!PEM bis";
        }

        final VarParam<GuiMenuItem> responseDataMenuItemPemA = new VarParam<GuiMenuItem>(null);
        final VarParam<GuiMenuItem> responseDataMenuItemPemB = new VarParam<GuiMenuItem>(null);

        // DataObjectGrids mit integriertem Status Kontextmenü verwenden
        final DataObjectFilterGridWithStatus dataGrid = new DataObjectFilterGridWithStatus(getConnector(), this, getTableName(), getStatusFieldName()) {

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                super.createContextMenuItems(contextMenu);

                // Popup-Menüeintrag für Anzeige Rückmeldedaten zu PEM Ab
                responseDataMenuItemPemA.setValue(AbstractRelatedInfoFactoryDataForm.createResponseDataMenuItem(new AbstractRelatedInfoFactoryDataForm.ResponseDataCallback() {
                                                                                                                    @Override
                                                                                                                    public AbstractRelatedInfoFactoryDataForm.ResponseDataQuery getResponseDataQuery() {
                                                                                                                        List<EtkDataObject> selection = getSelection();
                                                                                                                        if (selection != null) {
                                                                                                                            EtkDataObject dataModel = selection.get(0);

                                                                                                                            Map<String, List<iPartsResponseDataWithSpikes>> responseDataMap = getResponseDataMap(isRetailFilter());
                                                                                                                            List<iPartsResponseDataWithSpikes> responseData = null;
                                                                                                                            if (!StrUtils.isEmpty(getPemFromFieldName())) {
                                                                                                                                String pem = dataModel.getFieldValue(getPemFromFieldName());
                                                                                                                                responseData = responseDataMap.get(pem);
                                                                                                                            }
                                                                                                                            return new AbstractRelatedInfoFactoryDataForm.ResponseDataQuery(dataModel, getFactoryFieldName(),
                                                                                                                                                                                            getSeriesNoFieldName(),
                                                                                                                                                                                            getAAFieldName(),
                                                                                                                                                                                            getPemFromFieldName(),
                                                                                                                                                                                            getPartListEntry(),
                                                                                                                                                                                            isRetailFilter(), isEditable(),
                                                                                                                                                                                            isELDASPartList(),
                                                                                                                                                                                            responseData, true);
                                                                                                                        } else {
                                                                                                                            return null;
                                                                                                                        }
                                                                                                                    }
                                                                                                                }

                        , AbstractRelatedInfoFactoryDataForm.this));
                contextMenu.addChild(responseDataMenuItemPemA.getValue());

                // Popup-Menüeintrag für Anzeige Rückmeldedaten zu PEM Bis
                responseDataMenuItemPemB.setValue(AbstractRelatedInfoFactoryDataForm.createResponseDataMenuItem(new AbstractRelatedInfoFactoryDataForm.ResponseDataCallback() {
                                                                                                                    @Override
                                                                                                                    public AbstractRelatedInfoFactoryDataForm.ResponseDataQuery getResponseDataQuery() {
                                                                                                                        List<EtkDataObject> selection = getSelection();
                                                                                                                        if (selection != null) {
                                                                                                                            EtkDataObject dataModel = selection.get(0);

                                                                                                                            Map<String, List<iPartsResponseDataWithSpikes>> responseDataMap = getResponseDataMap(isRetailFilter());
                                                                                                                            List<iPartsResponseDataWithSpikes> responseData = null;
                                                                                                                            if (!StrUtils.isEmpty(getPemToFieldName())) {
                                                                                                                                String pem = dataModel.getFieldValue(getPemToFieldName());
                                                                                                                                responseData = responseDataMap.get(pem);
                                                                                                                            }

                                                                                                                            return new AbstractRelatedInfoFactoryDataForm.ResponseDataQuery(dataModel, getFactoryFieldName(),
                                                                                                                                                                                            getSeriesNoFieldName(),
                                                                                                                                                                                            getAAFieldName(),
                                                                                                                                                                                            getPemToFieldName(),
                                                                                                                                                                                            getPartListEntry(),
                                                                                                                                                                                            isRetailFilter(), isEditable(),
                                                                                                                                                                                            isELDASPartList(),
                                                                                                                                                                                            responseData, false);
                                                                                                                        } else {
                                                                                                                            return null;
                                                                                                                        }
                                                                                                                    }
                                                                                                                }

                        , AbstractRelatedInfoFactoryDataForm.this));
                contextMenu.addChild(responseDataMenuItemPemB.getValue());
            }

            @Override
            protected void statusChanged() {
                statusChangedForGrid(top);
            }

            @Override
            protected iPartsDataReleaseState getStatusFromSelection(List<List<EtkDataObject>> selection) {
                iPartsDataReleaseState resultStatus = super.getStatusFromSelection(selection);
                if (resultStatus != null) {
                    // zusätzlich müssen die Werksdaten unterschiedliche Werke haben
                    Set<String> collectedFactories = new TreeSet<>();
                    for (List<EtkDataObject> etkDataObjects : selection) {
                        for (EtkDataObject etkDataObject : etkDataObjects) {
                            if (etkDataObject.getTableName().equals(getFactoryDataTableName())) {
                                String fieldValue = etkDataObject.getFieldValue(getFactoryFieldName());
                                if (!collectedFactories.add(fieldValue)) {
                                    return null;
                                }
                            }
                        }
                    }
                }
                return resultStatus;
            }
        };

        // Menüeinträge nach Selektion anpassen
        dataGrid.getTable().addEventListener(new EventListener(Event.TABLE_SELECTION_EVENT) {
            @Override
            public void fire(Event event) {
                List<EtkDataObject> selection = dataGrid.getSelection();
                if (selection != null) {
                    EtkDataObject dataModel = selection.get(0);

                    // Menüpunkte für Rückmeldedaten von PEM ab/bis mit PEM-Nummer versehen und Enabled-Zustand anpassen
                    if (responseDataMenuItemPemA.getValue() != null) {
                        String pemFromValue = dataModel.getFieldValue(getPemFromFieldName());
                        responseDataMenuItemPemA.getValue().setText(TranslationHandler.translate(iPartsConst.RELATED_INFO_RESPONSE_DATA_TEXT)
                                                                    + " " + TranslationHandler.translate(pemFromTitle) + " ("
                                                                    + pemFromValue + ")");
                        boolean responseDataAvailableForPEMA = ResponseDataHelper.isResponseDataAvailableForPEM(pemFromValue, getResponseDataMap(isRetailFilter()));
                        responseDataMenuItemPemA.getValue().setEnabled((editMode && !pemFromValue.isEmpty() && !isRetailFilter())
                                                                       || responseDataAvailableForPEMA);
                    }
                    if (responseDataMenuItemPemB.getValue() != null) {
                        String pemToValue = dataModel.getFieldValue(getPemToFieldName());
                        responseDataMenuItemPemB.getValue().setText(TranslationHandler.translate(iPartsConst.RELATED_INFO_RESPONSE_DATA_TEXT)
                                                                    + " " + TranslationHandler.translate(pemToTitle) + " ("
                                                                    + pemToValue + ")");
                        boolean responseDataAvailableForPEMB = ResponseDataHelper.isResponseDataAvailableForPEM(pemToValue, getResponseDataMap(isRetailFilter()));
                        responseDataMenuItemPemB.getValue().setEnabled((editMode && !pemToValue.isEmpty() && !isRetailFilter())
                                                                       || responseDataAvailableForPEMB);
                    }
                }
            }
        });
        return dataGrid;
    }

    protected Map<String, List<iPartsResponseDataWithSpikes>> getResponseDataMap(boolean isRetailfilter) {
        if (isRetailfilter) {
            return responseDataForPemRetailFilter;
        } else {
            return responseDataForPemUnfiltered;
        }
    }

    public void hideEditResponseData() {
        for (AbstractGuiControl item : gridTop.getContextMenu().getChildren()) {
            if ((item.getUserObject() != null) && (item.getUserObject() instanceof String)) {
                if ((item.getUserObject().equals(IPARTS_MENU_ITEM_SHOW_RESPONSE_DATA)) || (item.getUserObject().equals("menuSeparatorT"))) {
                    item.setVisible(false);
                }
            }
        }
    }

    private void setPartListEntryFromConnector() {
        setPartListEntry(getConnector().getRelatedInfoData().getAsPartListEntry(getProject()));
    }

    protected void setPartListEntry(EtkDataPartListEntry partListEntry) {
        this.partListEntry = partListEntry;
        if (this.partListEntry != null) {
            iPartsModuleTypes moduleType = iPartsModuleTypes.getType(this.partListEntry.getOwnerAssembly().getEbeneName());
            // der Stücklistentyp sollte sich auch im non-modalen Fall nicht ändern. Er muss aber hier bestimmt werden weil er vom Stüli-Eintrag abhängt
            isRetailPartList = !moduleType.isConstructionRelevant();
        }
    }

    @Override
    public void dataChanged() {
        super.dataChanged();

        PartListEntryId partListEntryId = getConnector().getRelatedInfoData().getAsPartListEntryId();
        if ((partListEntryId != null) && partListEntryId.isValidId()) {
            loadedPartListEntryId = partListEntryId;
            // für einen evtl. künftigen non-modalen Aufruf fragen wir den Stücklisteneintrag hier ab statt bei Formularerzeugung
            setPartListEntryFromConnector();
        }
    }

    protected void createCopyPasteToolbarbuttons() {
        if (editMode || !isRetailPartList) {
            initToolbarHelperTop();
        }

        if (editMode) {
            // im EditMode einen zusätzlichen Separator einfügen um Copy/ Paste von den anderen Buttons zu trennen
            toolbarHelperTop.addSeparator("copyAndPasteSeparator");
        }

        if (editMode || !isRetailPartList) {
            toolbarHelperTop.addToolbarButton(EditToolbarButtonAlias.EDIT_COPY_FACTORY_DATA, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    copyFactoryData(true);
                }
            });
            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_COPY_FACTORY_DATA, true);
        }

        if (editMode) {
            // Paste Button für Werkseinsatzdaten nur im TU bearbeiten
            toolbarHelperTop.addToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_FACTORY_DATA, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    pasteFactoryData();
                }
            });
            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_FACTORY_DATA, false);
        }
    }

    protected boolean copyFactoryData(boolean withUpdate) {
        MessageDialog.show("Not implemented");
        return false;
    }

    protected void pasteFactoryData() {
        MessageDialog.show("Not implemented");
    }

    protected void createLinkUnlinkToolbarbuttons() {
        if (editMode) {
            toolbarHelperTop.addSeparator(LINK_SEPARATOR_NAME);
            toolbarHelperTop.addToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    pasteAndLinkFactoryData();
                }
            });

            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_PASTE_AND_LINK_FD, false);
            toolbarHelperTop.addToolbarButton(EditToolbarButtonAlias.EDIT_UNLINK_FD, new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    unlinkFactoryData();
                }
            });

            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_UNLINK_FD, false);

            // Geht zur Konstruktionsstückliste des gekoppelten Stücklisteneintrags
            toolbarHelperTop.addToolbarButton(EditToolbarButtonAlias.EDIT_GO_TO_BCTE, new EventListener(Event.ACTION_PERFORMED_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
                @Override
                public void fire(Event event) {
                    gotoLinkedConstructionEntry();
                }
            });

            toolbarHelperTop.enableToolbarButton(EditToolbarButtonAlias.EDIT_GO_TO_BCTE, false);
        }
    }

    protected void pasteAndLinkFactoryData() {
        MessageDialog.show("Not implemented");
    }

    protected void unlinkFactoryData() {
        MessageDialog.show("Not implemented");
    }

    protected void gotoLinkedConstructionEntry() {
        MessageDialog.show("Not implemented");
    }

    protected void initToolbarHelperTop() {
        if (toolbarHelperTop == null) {
            toolbarHelperTop = new EditToolbarButtonMenuHelper(getConnector(), gridTop.getToolBar());
        }
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();
        setPartListEntryFromConnector();
        if (!editMode && !isRetailPartList) {
            initToolbarHelperTop();
        }
    }

    @Override
    protected String getTableName() {
        return getFactoryDataTableName();
    }

    public EtkDataPartListEntry getPartListEntry() {
        return partListEntry;
    }

    protected abstract String getFactoryDataTableName();

    protected abstract String getFactoryFieldName();

    protected abstract String getPemFromFieldName();

    protected abstract String getPemToFieldName();

    protected abstract String getSeriesNoFieldName();

    protected abstract String getAAFieldName();

    protected abstract boolean isEditable();

    protected abstract boolean isELDASPartList();

    /**
     * Wird aufgerufen, wenn sich der Status für mindestens einen Eintrag im oberen bzw. unteren Grid geändert hat, damit
     * diese Änderungen gespeichert werden können.
     */
    protected abstract void statusChangedForGrid(boolean top);

    @Override
    protected boolean isContextMenuEntryForNewObjectVisible(boolean top) {
        return top; // Kein "Neu" im unteren Grid
    }

    @Override
    protected boolean isContextMenuEntryForDeleteObjectVisible(boolean top) {
        return top; // Kein "Löschen" im unteren Grid
    }

    @Override
    public int calculateOptimalHeight() {
        setAutoDividerPosition(false);
        int scrollBarWidth = FrameworkUtils.getSystemScrollbarWidth(false);
        int titleAndBorderHeight = HTMLUtils.getTextDimension(getTopPanel().getFont(), "Wg").getHeight() + 10;
        DataObjectGrid grid = getGrid(true);
        int optimalHeight = grid.getToolBar().getPreferredHeight() + 6 + grid.getTable().getPreferredHeight()
                            + scrollBarWidth + titleAndBorderHeight + 10 + ((grid.getTable().getRowCount() == 0) ? titleAndBorderHeight : 0);
        setSplitPaneDividerRatio(0.0d);
        setSplitPaneDividerPosition(optimalHeight);
        if (!isRetailFilterSet()) {
            grid = getGrid(false);
            optimalHeight += grid.getToolBar().getPreferredHeight() + 6 + grid.getTable().getPreferredHeight()
                             + scrollBarWidth + titleAndBorderHeight + ((grid.getTable().getRowCount() == 0) ? titleAndBorderHeight : 0)
                             + (getCheckboxRetailFilter().isVisible() || getCheckboxShowHistory().isVisible() ? getCheckboxRetailFilter().getPreferredHeight() + 16 : 0);
        }
        return optimalHeight;
    }

    /**
     * Callback zur Bestimmung der Daten für die Anzeige der Response-Daten.
     */
    public interface ResponseDataCallback {

        ResponseDataQuery getResponseDataQuery();
    }

    /**
     * Daten die aus dem Dialog Werkseinsatzdaten an den Dialog Rückmeldedaten übertragen werden sollen.
     */
    public static class ResponseDataQuery {

        private String factory = "";
        private String seriesNo = "";
        private String aa = "";
        private String pem = "";
        private boolean retailFilter;
        private EtkDataPartListEntry partlistEntry;
        // DAIMLER-4870 TODO parameter kann wieder ausgebaut werden wenn auch Rückmeldedaten von Varianten editierbar werden sollen
        private boolean isEditable;
        private boolean isPemFrom;
        protected boolean isEldasPartlist;

        private List<iPartsResponseDataWithSpikes> responseDataList;

        public ResponseDataQuery(EtkDataObject dataModel, String factoryFieldName, String seriesNoFieldName,
                                 String aaFieldName, String pemFieldName, EtkDataPartListEntry partlistEntry,
                                 boolean retailFilter, boolean isEditable, boolean isEldasPartlist,
                                 List<iPartsResponseDataWithSpikes> responseData, boolean isPemFrom) {
            if (dataModel != null) {
                if (!StrUtils.isEmpty(factoryFieldName)) {
                    factory = dataModel.getFieldValue(factoryFieldName);
                }
                if (!StrUtils.isEmpty(seriesNoFieldName)) {
                    seriesNo = dataModel.getFieldValue(seriesNoFieldName);
                }
                if (!StrUtils.isEmpty(aaFieldName)) {
                    aa = dataModel.getFieldValue(aaFieldName);
                }
                if (!StrUtils.isEmpty(pemFieldName)) {
                    pem = dataModel.getFieldValue(pemFieldName);
                }
            }
            this.retailFilter = retailFilter;
            this.partlistEntry = partlistEntry;
            this.isEditable = isEditable;
            this.responseDataList = responseData;
            this.isPemFrom = isPemFrom;
            this.isEldasPartlist = isEldasPartlist;
        }

        public ResponseDataQuery(String pem, String factory, String seriesNo, String aa, EtkDataPartListEntry partlistEntry,
                                 boolean retailFilter, boolean isEditable, List<iPartsResponseDataWithSpikes> responseData, boolean isPemFrom) {
            this.pem = pem;
            this.factory = factory;
            this.seriesNo = seriesNo;
            this.aa = aa;
            this.partlistEntry = partlistEntry;
            this.retailFilter = retailFilter;
            this.isEditable = isEditable;
            this.responseDataList = responseData;
            this.isPemFrom = isPemFrom;
            this.isEldasPartlist = false; // Ausreißer gibts bei ELDAS nicht
        }

        public String getPem() {
            return pem;
        }

        public String getFactory() {
            return factory;
        }

        public boolean isRetailFilter() {
            return retailFilter;
        }

        public EtkDataPartListEntry getPartlistEntry() {
            return partlistEntry;
        }

        public String getSeriesNo() {
            return seriesNo;
        }

        public String getAa() {
            return aa;
        }

        public boolean isEditable() {
            return this.isEditable;
        }

        public List<iPartsResponseDataWithSpikes> getResponseDataList() {
            return responseDataList;
        }

        public boolean isPemFrom() {
            return isPemFrom;
        }

        public boolean isEldasPartlist() {
            return isEldasPartlist;
        }
    }
}