/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SelectSearchGridMaterial;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.forms.events.OnStartSearchEvent;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsSelectSearchGridMaterial;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.util.Utils;
import de.docware.util.sort.SortBetweenHelper;

/**
 * Form zur Suche nach Material und Anlegen eines Stücklisten-Eintrags
 */
public class EditMaterialEditForm extends AbstractJavaViewerForm {

    public interface OnPartListEntryValidEvent {

        public void onPartListEntryValidEvent(EtkDataPartListEntry partListEntry);
    }

    private SelectSearchGridMaterial searchMaterial;
    private EtkDataPartListEntry partListEntry = null;
    private boolean showBeforeInsert = true;
    protected boolean isEditAllowed;
    private iPartsEditPlugin editPlugin;
    private boolean pskMaterialsAllowed;
    protected OnPartListEntryValidEvent onPartListEntryValidEvent = null;

    /**
     * Erzeugt eine Instanz von EditMaterialEditForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditMaterialEditForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsEditPlugin editPlugin,
                                boolean pskMaterialsAllowed) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.editPlugin = editPlugin;
        this.isEditAllowed = getConnector().isAuthorOrderValid();
        this.pskMaterialsAllowed = pskMaterialsAllowed;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    protected void postCreateGui() {
        mainWindow.buttonpanelSelect.setButtonVisible(GuiButtonOnPanel.ButtonType.OK, false);
        mainWindow.buttonpanelSelect.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, false);
        mainWindow.buttonpanelSelect.setButtonVisible(GuiButtonOnPanel.ButtonType.APPLY, true);
        mainWindow.buttonpanelSelect.getButtonOnPanel(GuiButtonOnPanel.ButtonType.APPLY).setText("!!Stücklisteneintrag erzeugen");

        createSearchForm();
        enableApplyButton(false);
    }

    protected void createSearchForm() {
        searchMaterial = new iPartsSelectSearchGridMaterial(this, false, pskMaterialsAllowed);
        searchMaterial.setMultiSelect(false);
        searchMaterial.setOnChangeEvent(new OnChangeEvent() {
            @Override
            public void onChange() {
                DBDataObjectAttributes attributes = getSelectedAttributes();
                if (attributes != null) {
                    searchMaterial.doStopSearch();
                    enableApplyButton(true);
                }
            }
        });

        if (isEditAllowed) {
            searchMaterial.setOnDblClickEvent(new OnDblClickEvent() {
                @Override
                public void onDblClick() {
                    DBDataObjectAttributes attributes = getSelectedAttributes();
                    if (attributes != null) {
                        searchMaterial.doStopSearch();
                        enableApplyButton(true);
                        onButtonApplyAction(null);
                    }
                }
            });
        }

        searchMaterial.setOnStartSearchEvent(new OnStartSearchEvent() {
            @Override
            public void onStartSearch() {
                enableApplyButton(false);
            }
        });

        addToPanelGrid(searchMaterial.getGui());
    }

    protected void addToPanelGrid(AbstractGuiControl control) {
        mainWindow.panelGrid.addChildBorderCenter(control);
    }

    protected SelectSearchGridMaterial getSearchForm() {
        return searchMaterial;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        getSearchForm().requestFocusForSearchValue();
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public EtkDataPartListEntry getPartListEntry() {
        return partListEntry;
    }

    public void setPartListEntry(EtkDataPartListEntry partListEntry) {
        this.partListEntry = partListEntry;
    }

    public boolean isShowBeforeInsert() {
        return showBeforeInsert;
    }

    public void setShowBeforeInsert(boolean showBeforeInsert) {
        this.showBeforeInsert = showBeforeInsert;
    }

    public OnPartListEntryValidEvent getOnPartListEntryValidEvent() {
        return onPartListEntryValidEvent;
    }

    public void setOnPartListEntryValidEvent(OnPartListEntryValidEvent onPartListEntryValidEvent) {
        this.onPartListEntryValidEvent = onPartListEntryValidEvent;
    }

    protected void onButtonApplyAction(Event event) {
        if (!iPartsEditPlugin.startEditing()) { // Paralleles Bearbeiten verhindern
            return;
        }

        try {
            addPartListEntry();
        } finally {
            iPartsEditPlugin.stopEditing();
        }
    }

    private void addPartListEntry() {
        //Auswerten etc
        DBDataObjectAttributesList attributesList = getSelectedAttributesList();
        if (Utils.isValid(attributesList)) {
            boolean doAdd = true;
            DBDataObjectList<EtkDataPartListEntry> destPartList = getConnector().getCurrentAssembly().getPartListUnfiltered(null);

            // Startwert für die laufende Nummer und Sequenznummer auf Basis der höchsten existierenden laufenden
            // Nummer bzw. Sequenznummer bestimmen
            int destLfdNr = 0;
            String destSeqValue = "";
            AssemblyId targetAssemblyId = getConnector().getCurrentAssembly().getAsId();
            for (EtkDataPartListEntry destPartListEntry : destPartList) {
                destLfdNr = Math.max(destLfdNr, Integer.valueOf(destPartListEntry.getAsId().getKLfdnr()));
                // Maximum ermitteln
                String value = destPartListEntry.getFieldValue(EtkDbConst.FIELD_K_SEQNR);
                if (SortBetweenHelper.isGreater(value, destSeqValue)) {
                    destSeqValue = value;
                }
            }

            boolean showDialogBeforeInsert = showBeforeInsert && (attributesList.size() == 1);
            EtkDataPartListEntry lastAddedPLE = null;

            for (DBDataObjectAttributes attributes : attributesList) {
                // DAIMLER-9238: laufende Nummer hochzählen und reservieren
                destLfdNr = iPartsDataReservedPKList.getAndReserveNextKLfdNr(getProject(), targetAssemblyId, destLfdNr);
                // Nächsten Wert für die Sequenznummer
                destSeqValue = SortBetweenHelper.getNextSortValue(destSeqValue);

                EtkDataPartListEntry partListEntry = buildPartListEntryFromMat(attributes, destLfdNr, destSeqValue, targetAssemblyId);
                if (partListEntry != null) {
                    if (showDialogBeforeInsert) {
                        if (iPartsUserSettingsHelper.isMatrixEdit(getProject())) {
                            doAdd = EditMatrixUserPartListEntryControls.doShowPartListEntry(getConnector(), this, true, partListEntry);
                        } else {
                            doAdd = EditUserPartListEntryControls.doShowPartListEntry(getConnector(), this, true, partListEntry);
                        }
                    }

                    if (doAdd) {
                        destPartList.add(partListEntry, DBActionOrigin.FROM_EDIT);
                        lastAddedPLE = partListEntry;
                    }
                }
                if ((partListEntry == null) || !doAdd) {
                    // DAIMLER-9238: 'überflüssige' Reservierung aufheben
                    PartListEntryId helperId = new PartListEntryId(targetAssemblyId.getKVari(), targetAssemblyId.getKVer(),
                                                                   EtkDbsHelper.formatLfdNr(destLfdNr));
                    iPartsDataReservedPKList.deleteReservedPrimaryKey(getProject(), helperId);
                }
            }

            if (lastAddedPLE != null) {
                // Änderungen sollen sofort ins Changeset gespeichert werden
                getConnector().savePartListEntries(null, true);
                getConnector().getCurrentAssembly().clearCache(); // Der neue Stücklisteneintrag fehlt noch in allen (ungefilterten) Stücklisten im AssemblyCache

                if (onPartListEntryValidEvent != null) {
                    onPartListEntryValidEvent.onPartListEntryValidEvent(lastAddedPLE);
                }
            }
        }
    }

    protected EtkDataPartListEntry buildPartListEntryFromMat(DBDataObjectAttributes attributes, int destLfdNr, String destSeqValue,
                                                             AssemblyId targetAssemblyId) {
        DBDataObjectAttributes catalogAttributes = convertPartToPartListAttributes(attributes, destLfdNr, destSeqValue);
        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(),
                                                                                          new PartListEntryId(targetAssemblyId.getKVari(),
                                                                                                              targetAssemblyId.getKVer(),
                                                                                                              EtkDbsHelper.formatLfdNr(destLfdNr)));

        if (partListEntry != null) {
            partListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            partListEntry.getPart().loadFromDB(new iPartsPartId(attributes.getField(EtkDbConst.FIELD_M_MATNR).getAsString(),
                                                                attributes.getField(EtkDbConst.FIELD_M_VER).getAsString()));
            partListEntry.assignAttributesValues(getProject(), catalogAttributes, true, DBActionOrigin.FROM_EDIT);

            partListEntry.setAttributeValue(iPartsConst.FIELD_K_POS, iPartsConst.HOTSPOT_NOT_SET_INDICATOR, DBActionOrigin.FROM_EDIT);

            iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromPartListType(getConnector().getCurrentAssembly().getEbeneName());
            partListEntry.setAttributeValue(iPartsConst.FIELD_K_SOURCE_TYPE, sourceType.getDbValue(), DBActionOrigin.FROM_EDIT);

            // Neue Stücklisteneinträge können über diesen Weg nur für Nicht-DIALOG-Stücklisten erzeugt werden
            // -> K_SOURCE_GUID entsprechend setzen
            partListEntry.setAttributeValue(iPartsConst.FIELD_K_SOURCE_GUID, EditConstructionToRetailHelper.createNonDIALOGSourceGUID(partListEntry.getAsId()),
                                            DBActionOrigin.FROM_EDIT);

            // DAIMLER-5371 neu angelegte Teilepositionen mit ME != Stück ("01") haben die Menge "NB"
            EditModuleHelper.initMengeFromQuantunit(partListEntry.getPart(), partListEntry);

            // Virtuelle Felder mit leeren Werten hinzufügen
            for (EtkDisplayField displayField : getConnector().getCurrentAssembly().getEbene().getFields()) {
                String tableName = displayField.getKey().getTableName();
                String fieldName = displayField.getKey().getFieldName();
                if (VirtualFieldsUtils.isVirtualField(fieldName)) {
                    if (tableName.equals(iPartsConst.TABLE_MAT)) {
                        EtkDataPart part = partListEntry.getPart();
                        if (!part.attributeExists(fieldName)) {
                            part.getAttributes().addField(fieldName, "", true, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                        }
                    } else {
                        if (!partListEntry.attributeExists(fieldName)) {
                            partListEntry.getAttributes().addField(fieldName, "", true, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                        }
                    }
                }
            }
        }
        return partListEntry;
    }

    private DBDataObjectAttributes convertPartToPartListAttributes(DBDataObjectAttributes partAttributes, int newLfdNr, String destSeqNr) {
        AssemblyId assemblyId = getConnector().getCurrentAssembly().getAsId();
        DBDataObjectAttributes result = new DBDataObjectAttributes();
        result.addField(EtkDbConst.FIELD_K_VARI, assemblyId.getKVari(), DBActionOrigin.FROM_DB);
        result.addField(EtkDbConst.FIELD_K_VER, assemblyId.getKVer(), DBActionOrigin.FROM_DB);
        result.addField(EtkDbConst.FIELD_K_ART, EtkConfigConst.BAUTEILKENN, DBActionOrigin.FROM_DB);
        result.addField(EtkDbConst.FIELD_K_MATNR, partAttributes.getField(EtkDbConst.FIELD_M_MATNR).getAsString(), DBActionOrigin.FROM_DB);
        result.addField(EtkDbConst.FIELD_K_MVER, partAttributes.getField(EtkDbConst.FIELD_M_VER).getAsString(), DBActionOrigin.FROM_DB);
        result.addField(EtkDbConst.FIELD_K_LFDNR, EtkDbsHelper.formatLfdNr(newLfdNr), DBActionOrigin.FROM_DB);
        result.addField(EtkDbConst.FIELD_K_SEQNR, destSeqNr, DBActionOrigin.FROM_DB);
        result.addField(EtkDbConst.FIELD_K_EBENE, getConnector().getCurrentAssembly().getEbeneName(), DBActionOrigin.FROM_DB);

        return result;
    }

    protected void enableApplyButton(boolean enabled) {
        mainWindow.buttonpanelSelect.setButtonVisible(GuiButtonOnPanel.ButtonType.APPLY, isEditAllowed);
        mainWindow.buttonpanelSelect.setButtonEnabled(GuiButtonOnPanel.ButtonType.APPLY, isEditAllowed && enabled);
        enableButtons();
    }

    private void enableButtons() {
        boolean isEnabled = getSelectedAttributes() != null;
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isEnabled);
    }

    protected DBDataObjectAttributes getSelectedAttributes() {
        if (getSearchForm() != null) {
            return getSearchForm().getSelectedAttributes();
        }
        return null;
    }

    protected DBDataObjectAttributesList getSelectedAttributesList() {
        if (getSearchForm() != null) {
            return getSearchForm().getSelectedAttributesList();
        }
        return null;
    }

    protected void setOnDblClickEvent(OnDblClickEvent onDblClickEvent) {
        getSearchForm().setOnDblClickEvent(onDblClickEvent);
    }

    protected void okButtonClick(Event event) {
        mainWindow.setModalResult(ModalResult.OK);
        close();
    }

    public boolean isPskMaterialsAllowed() {
        return pskMaterialsAllowed;
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
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanelSelect;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelGrid;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
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
            buttonpanelSelect = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanelSelect.setName("buttonpanelSelect");
            buttonpanelSelect.__internal_setGenerationDpi(96);
            buttonpanelSelect.registerTranslationHandler(translationHandler);
            buttonpanelSelect.setScaleForResolution(true);
            buttonpanelSelect.setMinimumWidth(10);
            buttonpanelSelect.setMinimumHeight(10);
            buttonpanelSelect.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonApplyActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonApplyAction(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelSelectConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelSelectConstraints.setPosition("north");
            buttonpanelSelect.setConstraints(buttonpanelSelectConstraints);
            panelMain.addChild(buttonpanelSelect);
            panelGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelGrid.setName("panelGrid");
            panelGrid.__internal_setGenerationDpi(96);
            panelGrid.registerTranslationHandler(translationHandler);
            panelGrid.setScaleForResolution(true);
            panelGrid.setMinimumWidth(10);
            panelGrid.setMinimumHeight(10);
            panelGrid.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelGrid.setLayout(panelGridLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelGrid.setConstraints(panelGridConstraints);
            panelMain.addChild(panelGrid);
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
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    okButtonClick(event);
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