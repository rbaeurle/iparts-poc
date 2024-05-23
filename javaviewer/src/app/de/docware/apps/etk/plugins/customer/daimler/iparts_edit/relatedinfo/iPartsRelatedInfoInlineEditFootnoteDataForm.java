/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsFootnoteEditInlineDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiFootNoteViewerPanel;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.util.Utils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class iPartsRelatedInfoInlineEditFootnoteDataForm extends AbstractRelatedInfoEditDataForm implements iPartsSuperEditRelatedInfoInterface {

    private iPartsFootnoteEditInlineDialog editDialog;
    private iPartsGuiFootNoteViewerPanel previewPanel;
    private iPartsDataPartListEntry partListEntry;
    private boolean suppressReloadData;

    public iPartsRelatedInfoInlineEditFootnoteDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                       IEtkRelatedInfo relatedInfo, boolean isEditContext) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui(isEditContext);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(boolean isEditContext) {
        editDialog = new iPartsFootnoteEditInlineDialog(getConnector(), this, getConnector().getRelatedInfoData().getAsPartListEntry(getProject()));
        editDialog.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelEdit.addChild(editDialog.getGui());
        editDialog.setReadOnly(!isEditContext);
        OnChangeEvent onReloadGrid = new OnChangeEvent() {
            @Override
            public void onChange() {
                updatePreview();
            }
        };
        OnChangeEvent onGridSelectionChanged = new OnChangeEvent() {
            @Override
            public void onChange() {
                selectPreview();
            }
        };
        editDialog.setOnReloadGrid(onReloadGrid);
        editDialog.setOnGridSelectionChangedEvents(onGridSelectionChanged);

        previewPanel = new iPartsGuiFootNoteViewerPanel();
        previewPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelPreview.addChild(previewPanel);
    }

    @Override
    public void dispose() {
        if (editDialog != null) {
            editDialog.dispose();
        }
        super.dispose();
    }

    public void setPreviewDividerPosition(int dividerPosition) {
        mainWindow.splitPane.setDividerPosition(dividerPosition);
    }

    public boolean isEditForMaterial() {
        return editDialog.isEditForMaterial();
    }

    public void setEditForMaterial(boolean editForMaterial) {
        editDialog.setEditForMaterial(editForMaterial);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    @Override
    public boolean hasElementsToShow() {
        return !editDialog.getAllFootNotes().isEmpty();
    }

    private void reloadData() {
        if (suppressReloadData) {
            return;
        }

        editDialog.reloadFootNotes();
    }

    @Override
    public void dataChanged() {
        super.dataChanged();

        if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
            iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)getConnector().getEditContext();
            if (editContext.isUpdateFootNotes() || editContext.isUpdateMatFootNotes()) {
                reloadData();
            }
        }
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        EtkDataPartListEntry currentPartListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject()).cloneMe(getProject());
        if ((currentPartListEntry instanceof iPartsDataPartListEntry) && (forceUpdateAll || ((getConnector().getActiveRelatedSubForm() == this)
                                                                                             && ((partListEntry == null)
                                                                                                 || !Utils.objectEquals(currentPartListEntry.getAsId(), partListEntry.getAsId()))))) {
            if ((partListEntry != null) && Utils.objectEquals(currentPartListEntry.getAsId(), partListEntry.getAsId())) {
                if (!suppressReloadData) { // StackOverflow durch rekursiven Aufruf von hideRelatedInfoCalled() vermeiden
                    // Aktuelle Änderungen sichern, da reloadData() die Fußnoten neu lädt und alle bisherigen Änderungen
                    // dadurch verloren gehen würden
                    suppressReloadData = true; // doppeltes reloadData() vermeiden
                    try {
                        hideRelatedInfoCalled();
                    } finally {
                        suppressReloadData = false;
                    }
                }
            } else {
                partListEntry = (iPartsDataPartListEntry)currentPartListEntry;
                editDialog.setPartListEntry(partListEntry);
            }
            reloadData();
        }
    }

    public void setPreviewPanelVisible(boolean visible) {
        mainWindow.panelPreview.setVisible(visible);
        mainWindow.splitPane.setDividerSize(visible ? 5 : 0);
    }

    private void updatePreview() {
        if (previewPanel != null) {
            Collection<iPartsFootNote> allFootnotes = editDialog.getAllFootNotes();
            allFootnotes = removeDuplicatePartFootnote(allFootnotes);
            previewPanel.setFootNotes(getProject(), allFootnotes);
        }
    }

    private Collection<iPartsFootNote> removeDuplicatePartFootnote(Collection<iPartsFootNote> allFootnotes) {
        Map<String, iPartsFootNote> partFootnotes = new LinkedHashMap<>();
        Map<String, iPartsFootNote> dialogFootnotes = new LinkedHashMap<>();
        Map<String, iPartsFootNote> normalFootnotes = new LinkedHashMap<>();
        for (iPartsFootNote footnote : allFootnotes) {
            if (footnote.isPartFootnote()) {
                partFootnotes.put(footnote.getFootNoteId().getFootNoteId(), footnote);
            } else if (footnote.isDIALOGFootnote()) {
                dialogFootnotes.put(footnote.getFootNoteId().getFootNoteId(), footnote);
            } else {
                normalFootnotes.put(footnote.getFootNoteId().getFootNoteId(), footnote);
            }
        }
        addIfNotExist(partFootnotes, dialogFootnotes);
        addIfNotExist(partFootnotes, normalFootnotes);
        return partFootnotes.values();
    }

    private void addIfNotExist(Map<String, iPartsFootNote> targetMap, Map<String, iPartsFootNote> sourceMap) {
        if ((targetMap == null) || (sourceMap == null) || sourceMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, iPartsFootNote> sourceEntry : sourceMap.entrySet()) {
            if (!targetMap.containsKey(sourceEntry.getKey())) {
                targetMap.put(sourceEntry.getKey(), sourceEntry.getValue());
            }
        }
    }

    private void selectPreview() {
        if (previewPanel != null) {
            List<iPartsFootNote> allFootnotes = editDialog.getAllSelectedFootNotes();
            previewPanel.setSelection(allFootnotes);
        }
    }

    private void save() {
        if (editDialog.stopAndStoreEdit() == EditUserControls.EditResult.STORED) {
            setModifiedByEdit(true);
            if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
                iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)getConnector().getEditContext();
                editContext.setFireDataChangedEvent(true);
                editContext.setUpdateFootNotes(true);

                // Fußnoten wurden geändert -> Stücklisteneintrag ebenfalls als geändert markieren
                EtkDataPartListEntry originalPLE = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
                if (originalPLE != null) {
                    editContext.addSaveEditRunnable(() -> originalPLE.markPartListEntryInChangeSetAsChanged());
                }

                // RelatedInfo-Daten updaten
                startPseudoTransactionForActiveChangeSet(true);
                try {
                    getConnector().dataChanged(null);
                } finally {
                    stopPseudoTransactionForActiveChangeSet();
                }

                // Module mit vererbten Daten merken, damit deren Caches beim Bestätigen der RelatedEdit gelöscht und
                // die Module im Edit neu geladen werden können
                editContext.addModifiedAssemblyIds(editDialog.getModifiedAssemblyIds());
            } else {
                // DataChangedEvent ist hier notwendig, damit eine evtl. geöffnete AS-Stückliste im Katalog auch aktualisiert wird
                getProject().fireProjectEvent(new DataChangedEvent(null), true);
            }
        }
    }

    public iPartsFootnoteEditInlineDialog getFootnoteEditor() {
        return editDialog;
    }

    public void onButtonOkClick(Event event) {
        save();
        close();
    }

    public void onButtonCancelClick(Event event) {
        close();
    }

    @Override
    protected boolean hideRelatedInfoCalled() {
        if (!getConnector().isEditContext()) {
            return true;
        }

        save();
        return true;
    }

    @Override
    public int calculateOptimalHeight() {
        int optimalHeight = getFootnoteEditor().getGrid().getToolBar().getPreferredHeight() + 6 + getFootnoteEditor().getGrid().getTable().getPreferredHeight();
        mainWindow.splitPane.setResizeWeight(0.0d);
        mainWindow.splitPane.setDividerPosition(optimalHeight);
        optimalHeight += previewPanel.getPreferredHeightForMainPanel() + 14;
        return optimalHeight;
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextmenuHolder;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelEdit;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPreview;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextmenuHolder = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextmenuHolder.setName("contextmenuHolder");
            contextmenuHolder.__internal_setGenerationDpi(96);
            contextmenuHolder.registerTranslationHandler(translationHandler);
            contextmenuHolder.setScaleForResolution(true);
            contextmenuHolder.setMinimumWidth(10);
            contextmenuHolder.setMinimumHeight(10);
            contextmenuHolder.setMenuName("contextmenu");
            contextmenuHolder.setParentControl(this);
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
            splitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitPane.setName("splitPane");
            splitPane.__internal_setGenerationDpi(96);
            splitPane.registerTranslationHandler(translationHandler);
            splitPane.setScaleForResolution(true);
            splitPane.setMinimumWidth(10);
            splitPane.setMinimumHeight(10);
            splitPane.setHorizontal(false);
            splitPane.setDividerPosition(391);
            panelEdit = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelEdit.setName("panelEdit");
            panelEdit.__internal_setGenerationDpi(96);
            panelEdit.registerTranslationHandler(translationHandler);
            panelEdit.setScaleForResolution(true);
            panelEdit.setMinimumWidth(0);
            panelEdit.setMinimumHeight(0);
            panelEdit.setPaddingTop(4);
            panelEdit.setPaddingLeft(8);
            panelEdit.setPaddingRight(8);
            panelEdit.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelEditLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelEdit.setLayout(panelEditLayout);
            splitPane.addChild(panelEdit);
            panelPreview = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPreview.setName("panelPreview");
            panelPreview.__internal_setGenerationDpi(96);
            panelPreview.registerTranslationHandler(translationHandler);
            panelPreview.setScaleForResolution(true);
            panelPreview.setMinimumWidth(0);
            panelPreview.setMinimumHeight(0);
            panelPreview.setPaddingTop(4);
            panelPreview.setPaddingLeft(8);
            panelPreview.setPaddingRight(8);
            panelPreview.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelPreviewLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelPreview.setLayout(panelPreviewLayout);
            splitPane.addChild(panelPreview);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitPane.setConstraints(splitPaneConstraints);
            panelMain.addChild(splitPane);
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
                    onButtonOkClick(event);
                }
            });
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCancelActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonCancelClick(event);
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