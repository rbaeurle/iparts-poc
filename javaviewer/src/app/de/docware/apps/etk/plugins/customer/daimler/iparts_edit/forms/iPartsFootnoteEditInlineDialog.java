/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictSearchComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsFootNoteHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsFootnoteType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsStandardFootNotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsVirtualFootnoteHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditEqualizeFieldsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoSuperEditDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.TextRepresentation;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Formular zum Editieren von Fußnoten am Stücklisteneintrag
 */
public class iPartsFootnoteEditInlineDialog extends AbstractJavaViewerForm {

    private static final String PSEUDO_TEXT_KIND = TableAndFieldName.make("PSEUDO_TABLE", "TEXT_KIND");

    private iPartsRelatedEditFootNoteGrid grid;
    private iPartsDataPartListEntry partListEntry;
    private iPartsDataFootNoteList dataStandardFootNoteList;

    private OnChangeEvent onReloadGrid = null;
    private List<OnChangeEvent> onGridSelectionChangedEvents = null;

    private List<FootNoteWithType> originals;
    private List<FootNoteWithType> footnotes;
    private Set<AssemblyId> modifiedAssemblyIds;
    private iPartsDataFootNoteMatRefList modifiedMatRefs;

    private int searchBoxWidth = -1;
    private boolean readOnly;
    private boolean isEditForMaterial;

    /**
     * Erzeugt eine Instanz von iPartsFootnoteEditInlineDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsFootnoteEditInlineDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          EtkDataPartListEntry etkPartListEntry) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();

        isEditForMaterial = false;
        originals = new ArrayList<>();
        footnotes = new ArrayList<>();

        setPartListEntry(etkPartListEntry);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        grid = new iPartsRelatedEditFootNoteGrid(getConnector(), this);

        grid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelMain.addChild(grid.getGui());

        grid.setDisplayFields(getDisplayFields());
        setReadOnly(false);

        // Kein EventListenerFireOnce verwenden und Listener auch nicht deregistrieren, da mehrfach auf das Resize reagiert
        // werden muss
        grid.getGui().addEventListener(new EventListener(Event.ON_RESIZE_EVENT) {
            @Override
            public void fire(Event event) {
                // Größenanpassung des Grid's, da die ComboBoxen die max Stringbreite anfordern
                int width = event.getIntParameter(Event.EVENT_PARAMETER_NEWWIDTH);
                int gridWidth = width - 2; // 2 px für Ränder der Tabelle
                int colNo = 0;
                int searchBoxCol = -1;
                for (EtkDisplayField displayField : getDisplayFields().getFields()) {
                    if (displayField.isVisible()) {
                        if (!displayField.getKey().getFieldName().equals(iPartsConst.FIELD_DFNC_TEXT)) {
                            int columnWidth = grid.getTable().getColumnWidth(colNo);
                            columnWidth += 1; // 1 px für Rand zwischen Spalten
                            gridWidth -= columnWidth;
                        } else {
                            searchBoxCol = colNo;
                        }
                        colNo++;
                    }
                }
                if ((gridWidth > 0) && (searchBoxCol > -1)) {
                    gridWidth -= 33; // damit rechts auch noch platz für eine Scrollbar ist
                    // die letzte spalte auf die verfügbare Breite ziehen
                    grid.getTable().setColumnWidth(searchBoxCol, gridWidth);
                    searchBoxWidth = gridWidth;
                }
                reloadGrid(footnotes, true); // jetzt die echten Fußnoten ins grid zeichnen
            }
        });
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setPartListEntry(EtkDataPartListEntry partListEntry) {
        if (partListEntry instanceof iPartsDataPartListEntry) {
            this.partListEntry = (iPartsDataPartListEntry)partListEntry;

            // zuerst das Grid einmal mit einer leeren Farbfußnote zeichnen damit die Spaltenbreiten im Resize Header berechnet werden können
            List<FootNoteWithType> dummyFootnotes = new DwList<>();
            FootNoteWithType footNoteWithType = new FootNoteWithType(createEmptyFootNoteCatalogRef(true), FootNoteType.COLORTABLE_FOOTNOTE);
            dummyFootnotes.add(footNoteWithType);
            reloadGrid(dummyFootnotes, false);
        }
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public iPartsRelatedEditFootNoteGrid getGrid() {
        return grid;
    }

    public OnChangeEvent getOnReloadGrid() {
        return onReloadGrid;
    }

    public void setOnReloadGrid(OnChangeEvent onReloadGrid) {
        this.onReloadGrid = onReloadGrid;
    }

    public List<OnChangeEvent> getOnGridSelectionChangedEvents() {
        return onGridSelectionChangedEvents;
    }

    public void setOnGridSelectionChangedEvents(OnChangeEvent onGridSelectionChangedEvent) {
        if (this.onGridSelectionChangedEvents == null) {
            this.onGridSelectionChangedEvents = new ArrayList<>();
        }
        this.onGridSelectionChangedEvents.add(onGridSelectionChangedEvent);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        for (FootNoteType footNoteType : FootNoteType.values()) {
            footNoteType.setIsReadOnly(readOnly);
        }
        grid.getToolBar().setVisible(!readOnly);
        grid.getToolbarHelper().visibleMenu(EditToolbarButtonAlias.EDIT_NEW, grid.getContextMenu(), !readOnly);
        grid.getToolbarHelper().visibleMenu(EditToolbarButtonAlias.EDIT_WORK, grid.getContextMenu(), !readOnly);
        grid.getToolbarHelper().visibleMenu(EditToolbarButtonAlias.EDIT_DELETE, grid.getContextMenu(), !readOnly);
        grid.getToolbarHelper().visibleMenu(EditToolbarButtonAlias.EDIT_MOVE_UP, grid.getContextMenu(), !readOnly);
        grid.getToolbarHelper().visibleMenu(EditToolbarButtonAlias.EDIT_MOVE_DOWN, grid.getContextMenu(), !readOnly);
        grid.getToolbarHelper().visibleMenu(DataObjectGrid.CONTEXT_MENU_SEPARATOR, grid.getContextMenu(), !readOnly);
        this.readOnly = readOnly;
    }

    public boolean isEditForMaterial() {
        return isEditForMaterial;
    }

    public void setEditForMaterial(boolean editForMaterial) {
        isEditForMaterial = editForMaterial;
    }

    public List<iPartsFootNote> getAllFootNotes() {
        List<iPartsFootNote> allFootnotes = new DwList<>();
        for (FootNoteWithType footnote : footnotes) {
            List<iPartsFootNote> iPartsFootNoteList = convertToiPartsFootnote(footnote);
            if (!iPartsFootNoteList.isEmpty()) {
                allFootnotes.addAll(iPartsFootNoteList);
            }
        }
        return allFootnotes;
    }

    public List<iPartsFootNote> getAllSelectedFootNotes() {
        List<iPartsFootNote> allFootnotes = new DwList<>();
        if (getGrid().isSomethingSelected()) {
            int[] selectedIndices = getGrid().getTable().getSelectedRowIndices();
            for (int index : selectedIndices) {
                FootNoteWithType footnote = footnotes.get(index);
                List<iPartsFootNote> iPartsFootNoteList = convertToiPartsFootnote(footnote);
                if (!iPartsFootNoteList.isEmpty()) {
                    allFootnotes.addAll(iPartsFootNoteList);
                }
            }
        }
        return allFootnotes;
    }

    protected List<iPartsFootNote> convertToiPartsFootnote(FootNoteWithType footNoteWithType) {
        List<iPartsFootNote> iPartsFootNoteList = new DwList<>();
        iPartsFootNoteId id = footNoteWithType.getFNId();
        String name = footNoteWithType.getName();
        boolean isStandardFootnote = footNoteWithType.getStandardFootNote();
        // Unterscheidung
        // 1. Positions-/Farbtabellenfußnote
        // oder
        // 2. Material-/DIALOG-Fußnote
        if (footNoteWithType.hasPartlistEntryFootNotes() || footNoteWithType.hasColorFootNotes()) {
            List<EtkDataObject> footNoteObjectList = footNoteWithType.getAsDataObjectList(true);
            if (!footNoteObjectList.isEmpty()) {
                for (EtkDataObject dataObject : footNoteObjectList) {
                    boolean isMarked = false;
                    if (footNoteWithType.isColorTableFootNote()) {
                        isMarked = dataObject.getFieldValueAsBoolean(iPartsConst.FIELD_DFNK_FN_MARKED);
                        name = dataObject.getFieldValue(iPartsConst.FIELD_DFN_NAME);
                        id = new iPartsFootNoteId(dataObject.getFieldValue(iPartsConst.FIELD_DFN_ID));
                    }
                    List<String> footNoteTexts = null;
                    if (footNoteWithType.isCreatedFootNote) {
                        footNoteTexts = new DwList<>();
                        if (dataObject instanceof iPartsDataFootNoteCatalogueRef) {
                            DBDataObjectList<iPartsDataFootNoteContent> footNoteList = ((iPartsDataFootNoteCatalogueRef)dataObject).getFootNoteList();
                            String dbLanguage = getProject().getDBLanguage();
                            List<String> fallbackLanguages = getProject().getDataBaseFallbackLanguages();
                            for (iPartsDataFootNoteContent dataFootNoteContent : footNoteList) {
                                footNoteTexts.add(dataFootNoteContent.getText(dbLanguage, fallbackLanguages));
                            }
                        }
                    }
                    iPartsFootnoteType fnType = (footNoteWithType.isColorTableFootNote()) ? iPartsFootnoteType.COLOR_TABLEFOOTNOTE : iPartsFootnoteType.PARTLIST;
                    if ((footNoteWithType.type == FootNoteType.PART_FOOTNOTE_STANDARD) || (footNoteWithType.type == FootNoteType.PART_FOOTNOTE_NORMAL)) {
                        fnType = iPartsFootnoteType.PART;
                    }
                    iPartsFootNote helper = new iPartsFootNote(id, name, footNoteTexts, isStandardFootnote, fnType);
                    helper.setIsMarked(isMarked);
                    iPartsFootNoteList.add(helper);
                }
            }
        } else if ((footNoteWithType.hasPartFootnotes() && (footNoteWithType.isPartFootNote()))
                   || (footNoteWithType.hasDIALOGFootNotes() && (footNoteWithType.isConstructionFootNote()))) {
            iPartsFootnoteType fnType;
            switch (footNoteWithType.type) {
                case PART_FOOTNOTE:
                case PART_FOOTNOTE_NORMAL:
                case PART_FOOTNOTE_STANDARD:
                    fnType = iPartsFootnoteType.PART;
                    break;
                default:
                    fnType = iPartsFootnoteType.CONSTRUCTION_FOOTNOTE;
                    break;
            }
//            iPartsFootnoteType fnType = (footNoteWithType.type == FootNoteType.PART_FOOTNOTE) ? iPartsFootnoteType.PART : iPartsFootnoteType.CONSTRUCTION_FOOTNOTE;
            iPartsFootNote helper = new iPartsFootNote(id, name, null, isStandardFootnote, fnType);
            iPartsFootNoteList.add(helper);
        }
        return iPartsFootNoteList;
    }

    /**
     * Beendet das Editieren und legt die editierten Werte in den ursprünglichen Datenstrukturen ab.
     *
     * @return {@link EditUserControls.EditResult#STORED} falls das Ablegen der editierten Werte in die ursprünglichen Datenstrukturen ohne
     * Fehler durchgeführt werden konnte,
     * {@link EditUserControls.EditResult#UNMODIFIED} falls keine Daten verändert wurden und
     * {@link EditUserControls.EditResult#ERROR} falls es Fehler gab
     */
    public EditUserControls.EditResult stopAndStoreEdit() {
        if (!readOnly) {
            if (!saveToChangeset(true)) {
                return EditUserControls.EditResult.UNMODIFIED;
            } else {
                return EditUserControls.EditResult.STORED;
            }
        } else {
            return EditUserControls.EditResult.UNMODIFIED;
        }
    }

    /**
     * Alle durch Vererbung veränderte Assemblies; kann auch {@code null} sein.
     * Liefert erst nach dem Aufruf von {@link #getDataToSaveToChangeSet(boolean)} ein Ergebnis zurück.
     *
     * @return
     */
    public Set<AssemblyId> getModifiedAssemblyIds() {
        return modifiedAssemblyIds;
    }

    public iPartsDataFootNoteMatRefList getModifiedMatRefs() {
        return modifiedMatRefs;
    }

    private void updatePreview(boolean withSelect) {
        if (onReloadGrid != null) {
            onReloadGrid.onChange();
        }
        if (withSelect) {
            selectPreview();
        }
    }

    private void refreshSuperEdit() {
        if (parentForm instanceof RelatedInfoBaseForm) {
            iPartsRelatedInfoSuperEditDataForm.refreshSuperEdit((RelatedInfoBaseForm)parentForm);
        }
    }

    private void selectPreview() {
        if ((onGridSelectionChangedEvents != null) && !onGridSelectionChangedEvents.isEmpty()) {
            for (OnChangeEvent onGridSelectionChangeEvent : onGridSelectionChangedEvents) {
                onGridSelectionChangeEvent.onChange();
            }
        }
    }

    public void reloadFootNotes() {
        originals = new ArrayList<>();
        footnotes = new ArrayList<>();

        if (partListEntry != null) {
            EtkDataPart part = partListEntry.getPart();
            // Teilestamm Fußnoten laden
            if (part != null) {
                iPartsDataFootNoteMatRefList partsDataFootNoteMatRefs = iPartsDataFootNoteMatRefList.loadFootNotesForMatNumber(getProject(), part.getAsId(), true, false);
                iPartsDataFootNoteMatRefList sortedPartsDataFootNoteMatRefs = new iPartsDataFootNoteMatRefList();
                // sortieren nach 'echten' und in iParts erzeugten FNs
                for (iPartsDataFootNoteMatRef matRef : partsDataFootNoteMatRefs) {
                    if (!matRef.isiPartsSource()) {
                        sortedPartsDataFootNoteMatRefs.add(matRef, DBActionOrigin.FROM_DB);
                    }
                }
                for (iPartsDataFootNoteMatRef matRef : partsDataFootNoteMatRefs) {
                    if (matRef.isiPartsSource()) {
                        sortedPartsDataFootNoteMatRefs.add(matRef, DBActionOrigin.FROM_DB);
                    }
                }
                if (isEditForMaterial) {
                    for (iPartsDataFootNoteMatRef matRef : sortedPartsDataFootNoteMatRefs) {
                        matRef.getFootNoteList();
                        FootNoteType type = FootNoteType.PART_FOOTNOTE_NORMAL;
                        if (matRef.getFieldValueAsBoolean(iPartsConst.FIELD_DFN_STANDARD)) {
                            type = FootNoteType.PART_FOOTNOTE_STANDARD;
                        }
                        FootNoteWithType loadedFootnote = new FootNoteWithType(matRef, type);
                        originals.add(loadedFootnote);
                    }
                } else {
                    addToOriginals(sortedPartsDataFootNoteMatRefs, FootNoteType.PART_FOOTNOTE);
                }
            }

            if (!isEditForMaterial) {
                // DIALOG Fußnoten laden
                iPartsDataFootNotePosRefList dialogFootnotes = iPartsFootNoteHelper.getDIALOGFootnoteContentsForPartListEntryWithJoinedFields(getProject(), partListEntry);
                addToOriginals(dialogFootnotes, FootNoteType.CONSTRUCTION_FOOTNOTE);

                // Virtuelle Fußnoten laden
                iPartsDataFootNoteCatalogueRefList virtualFootNoteCatalogueRefs = iPartsVirtualFootnoteHelper.createVirtualFootNoteCatalogueRefListForEdit(partListEntry);
                addToOriginals(virtualFootNoteCatalogueRefs, FootNoteType.GENERATED_FOOTNOTE);

                // Stücklistenpositionsfußnoten laden
                FootNoteWithType loadedColorFootnote = null;
                iPartsDataFootNoteCatalogueRefList footNoteCatalogueRefs = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntryWithJoin(getProject(), partListEntry.getAsId());
                if (!footNoteCatalogueRefs.isEmpty()) {
                    for (iPartsDataFootNoteCatalogueRef footNoteCatalogueRef : footNoteCatalogueRefs) {
                        // Typ überprüfen
                        DBDataObjectList<iPartsDataFootNoteContent> footNoteContents = footNoteCatalogueRef.getFootNoteList();
                        iPartsFootnoteType type = iPartsFootnoteType.getFromDBValue(footNoteCatalogueRef.getFieldValue(iPartsConst.FIELD_DFN_TYPE));
                        if (type == iPartsFootnoteType.COLOR_TABLEFOOTNOTE) {
                            if (loadedColorFootnote == null) {
                                loadedColorFootnote = new FootNoteWithType(footNoteCatalogueRef, FootNoteType.COLORTABLE_FOOTNOTE);
                                loadedColorFootnote.setColorIds(footNoteCatalogueRef.getFieldValue(iPartsConst.FIELD_DFNK_COLORTABLEFOOTNOTE));
                                originals.add(loadedColorFootnote);
                            } else {
                                if (loadedColorFootnote.getColorIds().equals(footNoteCatalogueRef.getFieldValue(iPartsConst.FIELD_DFNK_COLORTABLEFOOTNOTE))) {
                                    loadedColorFootnote.getColorFootList().add(footNoteCatalogueRef);
                                } else {
                                    loadedColorFootnote = new FootNoteWithType(footNoteCatalogueRef, FootNoteType.COLORTABLE_FOOTNOTE);
                                    loadedColorFootnote.setColorIds(footNoteCatalogueRef.getFieldValue(iPartsConst.FIELD_DFNK_COLORTABLEFOOTNOTE));
                                    originals.add(loadedColorFootnote);
                                }
                            }
                        } else {
                            loadedColorFootnote = null;
                            // kann eine Normale, Standard oder Tabellenfußnote (mehrzeilig) sein
                            boolean standardFootnote = footNoteCatalogueRef.getFieldValueAsBoolean(iPartsConst.FIELD_DFN_STANDARD);
                            if (standardFootnote) {
                                FootNoteWithType loadedFootnote = new FootNoteWithType(footNoteCatalogueRef, FootNoteType.DEFAULT_FOOTNOTE);
                                originals.add(loadedFootnote);
                            } else {
                                boolean tableFootnote = footNoteContents.size() > 1;
                                FootNoteWithType loadedFootnote = new FootNoteWithType(footNoteCatalogueRef, FootNoteType.NORMAL_FOOTNOTE);
                                if (tableFootnote) {
                                    loadedFootnote.type = FootNoteType.TABLE_FOOTNOTE;
                                } else if (type == iPartsFootnoteType.FACTORY_DATA) {
                                    loadedFootnote.type = FootNoteType.FACTORYDATA_FOOTNOTE;
                                }
                                originals.add(loadedFootnote);
                            }
                        }
                    }
                }
            }
            // jetzt die originals zum bearbeiten kopieren
            footnotes = new ArrayList<>(originals);
            reloadGrid(footnotes, true);
            refreshSuperEdit();
        }
    }

    /**
     * Erzeugt für die übergebenen {@link AbstractFootnoteRef} Objekte einzelne {@link FootNoteWithType} Objekte und
     * legt sie in der <code>originals</code> Liste ab.
     *
     * @param dataObjectList
     * @param footNoteType
     */
    private void addToOriginals(EtkDataObjectList<? extends AbstractFootnoteRef> dataObjectList, FootNoteType footNoteType) {
        if (dataObjectList != null) {
            for (AbstractFootnoteRef footnoteRefObject : dataObjectList) {
                footnoteRefObject.getFootNoteList();
                FootNoteWithType loadedFootnote = new FootNoteWithType(footnoteRefObject, footNoteType);
                originals.add(loadedFootnote);
            }
        }
    }

    private void reloadGrid(List<FootNoteWithType> footnotes, boolean updatePreview) {
        grid.clearGrid();

        for (FootNoteWithType footnote : footnotes) {
            DBDataObjectList<iPartsDataFootNoteContent> footNoteContents;
            // Teilestamm oder DIALOG Fußnoten hinzufügen
            if (footnote.isPartFootNote() || (footnote.isConstructionFootNote())) {
                footNoteContents = footnote.getFootNoteList();
                if ((footNoteContents != null) && !footNoteContents.isEmpty()) {
                    for (iPartsDataFootNoteContent footNoteContent : footNoteContents) {
                        grid.addFootNoteToGrid(footnote, footNoteContent);
                        // Hat eine Fußnote mehr als eine Zeile, dann soll im Inline Editor nur die erste Zeile
                        // ausgegeben werden
                        if (footNoteContents.size() > 1) {
                            break;
                        }
                    }
                    continue;
                }
            }
            // Farbtabellen oder normale Fußnoten hinzufügen
            if (footnote.isColorTableFootNote()) {
                footNoteContents = footnote.getColorFootList().get(0).getFootNoteList();
                if ((footNoteContents != null) && !footNoteContents.isEmpty()) {
                    grid.addFootNoteToGrid(footnote, footnote.getColorFootList().get(0), footNoteContents.get(0));
                }
            } else {
                footNoteContents = footnote.getFootNoteList();
                if ((footNoteContents != null) && !footNoteContents.isEmpty()) {
                    grid.addFootNoteToGrid(footnote, footnote.footnoteRef, footNoteContents.get(0));
                } else {
                    iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(getProject(), null);
                    dataFootNoteContent.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
                    grid.addFootNoteToGrid(footnote, footnote.footnoteRef, dataFootNoteContent);
                }
            }
        }

        if (updatePreview) {
            updatePreview(false);
            formEnableButtons();
        }
    }

    protected void formEnableButtons() {
        if (grid.getToolbarHelper() != null) {
            boolean isDeleteEnabled;
            boolean isEditEnabled;
            boolean isMoveUpEnabled;
            boolean isMoveDownEnabled;

            if (grid.isSomethingSelected()) {
                isEditEnabled = true;
                GuiTable table = grid.getTable();
                if (grid.isSingleSelected()) {
                    GuiTableRow selectedRow = table.getSelectedRow();
                    int selectedIndex = table.getSelectedRowIndex();
                    if (selectedRow instanceof FootNoteRow) {
                        FootNoteType footNoteType = ((FootNoteRow)selectedRow).footNoteType;
                        isDeleteEnabled = ((FootNoteRow)selectedRow).isDeleteAllowed;  //footNoteType.isDeleteAllowed();
                        isMoveUpEnabled = footNoteType.isMoveAllowed() && (selectedIndex > 0);
                        if (isMoveUpEnabled) {
                            GuiTableRow prevSelectedRow = table.getRow(selectedIndex - 1);
                            if (prevSelectedRow instanceof FootNoteRow) {
                                isMoveUpEnabled = ((FootNoteRow)prevSelectedRow).footNoteType.isMoveAllowed();
                            }
                        }
                        isMoveDownEnabled = footNoteType.isMoveAllowed() && (selectedIndex < (table.getRowCount() - 1));
                        if (isMoveDownEnabled) {
                            GuiTableRow succSelectedRow = table.getRow(selectedIndex + 1);
                            if (succSelectedRow instanceof FootNoteRow) {
                                isMoveDownEnabled = ((FootNoteRow)succSelectedRow).footNoteType.isMoveAllowed();
                            }
                        }
                    } else {
                        isMoveUpEnabled = isMoveDownEnabled = isDeleteEnabled = false;
                    }
                } else {
                    // multiSelect
                    isMoveDownEnabled = isMoveUpEnabled = false;
                    isDeleteEnabled = false;
                    for (GuiTableRow row : table.getSelectedRows()) {
                        if (row instanceof FootNoteRow) {
                            FootNoteType footNoteType = ((FootNoteRow)row).footNoteType;
                            if (footNoteType.isDeleteAllowed()) {
                                isDeleteEnabled = true;
                                break;
                            }
                        }
                    }
                }
            } else {
                isEditEnabled = isMoveDownEnabled = isMoveUpEnabled = isDeleteEnabled = false;
            }

            grid.getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, grid.getContextMenu(), true);
            grid.getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, grid.getContextMenu(), isEditEnabled);
            grid.getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, grid.getContextMenu(), isDeleteEnabled);
            grid.getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MOVE_UP, grid.getContextMenu(), isMoveUpEnabled);
            grid.getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MOVE_DOWN, grid.getContextMenu(), isMoveDownEnabled);
        }
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isModified());
    }

    protected EtkDisplayFields getDisplayFields() {
        EtkDisplayFields defaultDisplayFields = createDefaultDisplayFields();
        defaultDisplayFields.loadStandards(getConfig());
        return defaultDisplayFields;
    }

    protected EtkDisplayFields createDefaultDisplayFields() {
        EtkDisplayFields defaultDisplayFields = new EtkDisplayFields();
        EtkMultiSprache multi;
        EtkDisplayField displayField;
        if (Constants.DEVELOPMENT) {
            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FN_CONTENT, iPartsConst.FIELD_DFNC_FNID, false, false);
            multi = new EtkMultiSprache();
            multi.setText(getProject().getViewerLanguage(), "FIELD_DFNC_FNID");
            displayField.setText(multi);
            displayField.setDefaultText(false);
            defaultDisplayFields.addFeld(displayField);

            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FN_CONTENT, iPartsConst.FIELD_DFNC_LINE_NO, false, false);
            multi = new EtkMultiSprache();
            multi.setText(getProject().getViewerLanguage(), "FIELD_DFNC_LINE_NO");
            displayField.setText(multi);
            displayField.setDefaultText(false);
            defaultDisplayFields.addFeld(displayField);

            displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FN_KATALOG_REF, iPartsConst.FIELD_DFNK_FN_SEQNO, false, false);
            multi = new EtkMultiSprache();
            multi.setText(getProject().getViewerLanguage(), "FIELD_DFNK_FN_SEQNO");
            displayField.setText(multi);
            displayField.setDefaultText(false);
            defaultDisplayFields.addFeld(displayField);
        }

        // Tabellennamen nicht mit angeben, da dieses Feld durch einen Join beim Laden in den Attributen der DA_FN_KATALOG_REF Tabelle enthalten ist
        displayField = new EtkDisplayField(iPartsConst.FIELD_DFN_NAME, true, false);
        multi = new EtkMultiSprache();
        multi.setText(getProject().getViewerLanguage(), "!!Nummer");
        displayField.setText(multi);
        displayField.setDefaultText(false);
        defaultDisplayFields.addFeld(displayField);

        displayField = new EtkDisplayField(PSEUDO_TEXT_KIND, true, false);
        multi = new EtkMultiSprache();
        multi.setText(getProject().getViewerLanguage(), "!!Textart");
        displayField.setText(multi);
        displayField.setDefaultText(false);
        defaultDisplayFields.addFeld(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_FN_CONTENT, iPartsConst.FIELD_DFNC_TEXT, true, false);
        multi = new EtkMultiSprache();
        multi.setText(getProject().getViewerLanguage(), "!!Text");
        displayField.setText(multi);
        displayField.setDefaultText(false);
        defaultDisplayFields.addFeld(displayField);

        return defaultDisplayFields;
    }

    protected void doMove(boolean moveUp) {
        if (grid.isSingleSelected()) {
            int selectedIndex = grid.getTable().getSelectedRowIndex();
            int futureIndex = selectedIndex;
            if (moveUp) {
                futureIndex--;
            } else {
                futureIndex++;
            }
            if ((futureIndex >= 0) && (futureIndex < grid.getTable().getRowCount())) {
                FootNoteWithType footNoteWithType = footnotes.remove(selectedIndex);
                footnotes.add(futureIndex, footNoteWithType);
                reloadGrid(footnotes, true);
                grid.getTable().setSelectedRow(futureIndex, true);
                refreshSuperEdit();
            }
        }
    }

    protected void doDelete() {
        if (grid.isSomethingSelected()) {
            int[] selectedIndices = grid.getTable().getSelectedRowIndices();
            for (int index = selectedIndices.length - 1; index >= 0; index--) {
                int rowIndex = selectedIndices[index];
                GuiTableRow selectedRow = grid.getTable().getRow(rowIndex);
                if (selectedRow instanceof FootNoteRow) {
                    FootNoteType footNoteType = ((FootNoteRow)selectedRow).footNoteType;
                    if (((FootNoteRow)selectedRow).isDeleteAllowed) {
                        footnotes.remove(rowIndex);
                    }
                }
            }
            reloadGrid(footnotes, true);
            refreshSuperEdit();
        }
    }

    protected void doCreateNewFootNote() {
        FootNoteType fnType = FootNoteType.NORMAL_FOOTNOTE;
        if (isEditForMaterial) {
            fnType = FootNoteType.PART_FOOTNOTE_STANDARD;
        }
        FootNoteWithType footNoteWithType = new FootNoteWithType(createEmptyFootNoteCatalogRef(false), fnType);
        footnotes.add(footNoteWithType);
        reloadGrid(footnotes, true);
        grid.getTable().setSelectedRow(grid.getTable().getRowCount() - 1, true);
        refreshSuperEdit();
    }

    protected void doView() {
        if (grid.isSomethingSelected()) {
            List<iPartsFootNote> collectedFootnotes = getAllSelectedFootNotes();
            iPartsFootnotePreviewDialog.showFootnotePreview(this, collectedFootnotes);
        }
    }

    protected void doChangeFootNoteType(final int rowNo, FootNoteType footNoteType) {
        FootNoteWithType footNoteWithType = new FootNoteWithType(createEmptyFootNoteCatalogRef(true), footNoteType);
        footnotes.set(rowNo, footNoteWithType);
        GuiTableRow row = grid.getTable().getRow(rowNo);
        if (row instanceof FootNoteRow) {
            FootNoteRow footNoteRow = (FootNoteRow)row;
            footNoteRow.footNoteType = footNoteType;
            footNoteRow.isDeleteAllowed = footNoteType.isDeleteAllowed();
            final String tableAndFieldName = TableAndFieldName.make(iPartsConst.TABLE_DA_FN_CONTENT, iPartsConst.FIELD_DFNC_TEXT);
            grid.replaceCombobox(rowNo, footNoteWithType.type, row, tableAndFieldName, "");
            grid.updateGridRow(rowNo, footNoteRow, footNoteWithType);
            grid.getTable().setSelectedRow(rowNo, true);
            updatePreview(true);
            refreshSuperEdit();
        }
    }

    private iPartsDataFootNoteCatalogueRef createEmptyFootNoteCatalogRef(boolean withContent) {
        iPartsFootNoteCatalogueRefId id = new iPartsFootNoteCatalogueRefId(partListEntry.getAsId(), "");
        iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef = new iPartsDataFootNoteCatalogueRef(getProject(), id);
        dataFootNoteCatalogueRef.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
        dataFootNoteCatalogueRef.getAttributes().addField(iPartsConst.FIELD_DFN_ID, "", DBActionOrigin.FROM_DB);
        if (withContent) {
            iPartsDataFootNoteContentList footNoteContents = new iPartsDataFootNoteContentList();
            footNoteContents.add(new iPartsDataFootNoteContent(getProject(), new iPartsFootNoteContentId("", "00001")), DBActionOrigin.FROM_EDIT);
            dataFootNoteCatalogueRef.setFootNoteList(footNoteContents);
        }
        return dataFootNoteCatalogueRef;
    }

    private boolean isModified() {
        // zuerst die leeren zeilen aus den footnotes entfernen und mit dieser liste arbeiten
        List<FootNoteWithType> footnotesCopy = new DwList<>();
        for (FootNoteWithType footnote : footnotes) {
            if (!footnote.isEmptyFootnote()) {
                footnotesCopy.add(footnote);
            }
        }

        if (originals.size() != footnotesCopy.size()) {
            return true;
        }
        int index = 0;
        for (FootNoteWithType originalFootNoteWithType : originals) {
            FootNoteWithType footNoteWithType = footnotesCopy.get(index);
            if (originalFootNoteWithType.type != footNoteWithType.type) {
                return true;
            }
            if (originalFootNoteWithType.hasPartFootnotes() && !footNoteWithType.hasPartFootnotes()) {
                return true;
            }
            if (originalFootNoteWithType.hasDIALOGFootNotes() && !footNoteWithType.hasDIALOGFootNotes()) {
                return true;
            }
            if (originalFootNoteWithType.hasPartlistEntryFootNotes() && !footNoteWithType.hasPartlistEntryFootNotes()) {
                return true;
            }
            if (originalFootNoteWithType.hasPartFootnotes() || originalFootNoteWithType.hasDIALOGFootNotes() || originalFootNoteWithType.hasPartlistEntryFootNotes()) {
                // MatRefFootNotes vergleichen
                DBDataObjectList<iPartsDataFootNoteContent> originalList = originalFootNoteWithType.getFootNoteList();
                DBDataObjectList<iPartsDataFootNoteContent> newList = footNoteWithType.getFootNoteList();
                if (originalList.size() != newList.size()) {
                    return true;
                }
                for (int i = 0; i < originalList.size(); i++) {
                    if (!originalList.get(i).getAsId().equals(newList.get(i).getAsId())) {
                        return true;
                    }
                }
            } else if (footNoteWithType.hasPartlistEntryFootNotes() || footNoteWithType.hasPartFootnotes() || footNoteWithType.hasDIALOGFootNotes()) {
                return true;
            }
            index++;
        }
        return false;
    }

    private void addRefToList(iPartsDataFootNoteCatalogueRefList list, iPartsDataFootNoteCatalogueRef ref, Set<String> processedRefs) {
        processedRefs.add(ref.getCacheKey());
        ref.removeForeignTablesAttributes();
        list.add(ref, DBActionOrigin.FROM_EDIT);
    }

    public GenericEtkDataObjectList getDataToSaveToChangeSet() {
        return getDataToSaveToChangeSet(true);
    }

    public GenericEtkDataObjectList getDataToSaveToChangeSet(boolean checkIfModified) {
        if (!checkIfModified || isModified()) {
            modifiedAssemblyIds = null;
            modifiedMatRefs = null;
            // durch die Bearbeitung kann eigentlich nur folgendes passieren:
            // es wird ein neuer Eintrag in DA_FN_CONTENT angelegt
            // es kann eine Verknüpfung in DA_KATALOG_REF verändert werden, neu angelegt werden oder gelöscht werden

            iPartsDataFootNoteMatRefList matRefs = new iPartsDataFootNoteMatRefList();
            iPartsDataFootNoteCatalogueRefList katalogRefs = new iPartsDataFootNoteCatalogueRefList();
            iPartsDataFootNoteContentList contentList = new iPartsDataFootNoteContentList();
            iPartsDataFootNoteList footNoteList = new iPartsDataFootNoteList();

            Set<String> processedFootnoteRefs = new HashSet<>();
            Set<iPartsFootNoteMatRefId> processedMatRefs = new HashSet<>();

            // laufende Nummer im Dataobject an die neue Reihenfolge anpassen
            int i = 1;
            String lfdNr = EtkDbsHelper.formatLfdNr(i);
            for (FootNoteWithType footnote : footnotes) {
                for (EtkDataObject dataObject : footnote.getAsDataObjectList(false)) {
                    if (dataObject.attributeExists(iPartsConst.FIELD_DFNK_FN_SEQNO)) {
                        dataObject.setFieldValue(iPartsConst.FIELD_DFNK_FN_SEQNO, lfdNr, DBActionOrigin.FROM_EDIT);
                        i++;
                        lfdNr = EtkDbsHelper.formatLfdNr(i);
                    }
                }
            }

            for (FootNoteWithType footnote : footnotes) {
                switch (footnote.type) {
                    case PART_FOOTNOTE:
                    case CONSTRUCTION_FOOTNOTE:
                        // hier darf sich nichts geändert haben
                        break;
                    case PART_FOOTNOTE_STANDARD:
                        if (!footnote.isEmptyFootnote()) {
                            if (footnote.hasPartFootnotes()) {
                                iPartsDataFootNoteMatRef matRef = footnote.getFootnoteMatRef();
                                if (matRef != null) {
                                    if (matRef.isiPartsSource()) {
                                        if (!matRef.existsInDB()) {
                                            matRef.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                            matRef.setSource(iPartsImportDataOrigin.IPARTS, DBActionOrigin.FROM_EDIT);
                                            matRefs.add(matRef, DBActionOrigin.FROM_EDIT);
                                        }
                                    }
                                    processedMatRefs.add(matRef.getAsId());
                                }
                            } else {
                                DBDataObjectList<iPartsDataFootNoteContent> footNoteContents = footnote.getFootNoteList();
                                if ((footNoteContents != null) && !footNoteContents.isEmpty()) {
                                    iPartsDataFootNoteContent footNoteContent = footNoteContents.get(0);
                                    iPartsFootNoteMatRefId matRefId = new iPartsFootNoteMatRefId(partListEntry.getPart().getAsId().getMatNr(),
                                                                                                 footNoteContent.getAsId().getFootNoteId());
                                    iPartsDataFootNoteMatRef matRef = new iPartsDataFootNoteMatRef(getProject(), matRefId);
                                    if (!matRef.existsInDB()) {
                                        matRef.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                        matRef.setSource(iPartsImportDataOrigin.IPARTS, DBActionOrigin.FROM_EDIT);
                                        matRefs.add(matRef, DBActionOrigin.FROM_EDIT);
                                    }
                                    processedMatRefs.add(matRef.getAsId());
                                }
                            }
                        }
                        break;
                    case GENERATED_FOOTNOTE:
                        if (footnote.hasPartlistEntryFootNotes()) {
                            iPartsDataFootNoteCatalogueRef catalogueRef = (iPartsDataFootNoteCatalogueRef)footnote.footnoteRef;
                            processedFootnoteRefs.add(catalogueRef.getCacheKey());
                        }
                        break;
                    case DEFAULT_FOOTNOTE:
                    case TABLE_FOOTNOTE:
                        // hier kann sich nur die katalog_ref ändern
                        if (footnote.hasPartlistEntryFootNotes()) {
                            addRefToList(katalogRefs, (iPartsDataFootNoteCatalogueRef)footnote.footnoteRef, processedFootnoteRefs);
                        }
                        break;
                    case COLORTABLE_FOOTNOTE:
                        // hier kann sich nur die katalog_ref ändern
                        if ((footnote.getColorFootList() != null) && !footnote.getColorFootList().isEmpty()) {
                            for (iPartsDataFootNoteCatalogueRef colorFootnoteRef : footnote.getColorFootList()) {
                                addRefToList(katalogRefs, colorFootnoteRef, processedFootnoteRefs);
                            }
                        }
                        break;
                    case NORMAL_FOOTNOTE:
                        // bei normalen Fußnoten kann content hinzukommen und sich die ref ändern
                        if (footnote.footnoteRef != null) {
                            if (!footnote.isEmptyFootnote() && footnote.hasPartlistEntryFootNotes()) {
                                iPartsDataFootNoteCatalogueRef catalogueRef = (iPartsDataFootNoteCatalogueRef)footnote.footnoteRef;
                                String fnId = catalogueRef.getFieldValue(iPartsConst.FIELD_DFN_ID);
                                iPartsDataFootNote dataFootNote = new iPartsDataFootNote(getProject(), new iPartsFootNoteId(fnId));
                                if (!dataFootNote.existsInDB()) {
                                    dataFootNote.setAttributes(catalogueRef.getAttributes(), DBActionOrigin.FROM_EDIT);
                                    dataFootNote.removeForeignTablesAttributes();
                                    footNoteList.add(dataFootNote, DBActionOrigin.FROM_EDIT);
                                }
                                addRefToList(katalogRefs, catalogueRef, processedFootnoteRefs);

                                for (iPartsDataFootNoteContent footNoteContent : catalogueRef.getFootNoteList()) {
                                    contentList.add(footNoteContent, DBActionOrigin.FROM_EDIT);
                                }
                            }
                        }
                        break;
                    case PART_FOOTNOTE_NORMAL:
                        if (footnote.footnoteRef != null) {
                            if (!footnote.isEmptyFootnote() && footnote.hasPartlistEntryFootNotes()) {
                                iPartsDataFootNoteCatalogueRef catalogueRef = (iPartsDataFootNoteCatalogueRef)footnote.footnoteRef;
                                String fnId = catalogueRef.getFieldValue(iPartsConst.FIELD_DFN_ID);
                                iPartsDataFootNote dataFootNote = new iPartsDataFootNote(getProject(), new iPartsFootNoteId(fnId));
                                if (!dataFootNote.existsInDB()) {
                                    dataFootNote.setAttributes(catalogueRef.getAttributes(), DBActionOrigin.FROM_EDIT);
                                    dataFootNote.removeForeignTablesAttributes();
                                    footNoteList.add(dataFootNote, DBActionOrigin.FROM_EDIT);
                                }
                                iPartsFootNoteMatRefId matRefId = new iPartsFootNoteMatRefId(partListEntry.getPart().getAsId().getMatNr(),
                                                                                             fnId);
                                iPartsDataFootNoteMatRef matRef = new iPartsDataFootNoteMatRef(getProject(), matRefId);
                                if (!matRef.existsInDB()) {
                                    matRef.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                    matRef.setSource(iPartsImportDataOrigin.IPARTS, DBActionOrigin.FROM_EDIT);
                                    matRefs.add(matRef, DBActionOrigin.FROM_EDIT);
                                }
                                processedMatRefs.add(matRef.getAsId());

                                for (iPartsDataFootNoteContent footNoteContent : catalogueRef.getFootNoteList()) {
                                    // MultiLang auf jeden Fall nachladen
                                    footNoteContent.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DFNC_TEXT);
                                    contentList.add(footNoteContent, DBActionOrigin.FROM_EDIT);
                                }
                            }
                        }
                        break;
                }
            }

            for (FootNoteWithType footnote : originals) {
                if (footnote.isColorTableFootNote()) {
                    if ((footnote.getColorFootList() != null) && !footnote.getColorFootList().isEmpty()) {
                        for (iPartsDataFootNoteCatalogueRef colorFootnoteRef : footnote.getColorFootList()) {
                            if (colorFootnoteRef != null) {
                                String key = colorFootnoteRef.getCacheKey();
                                if (!processedFootnoteRefs.contains(key)) {
                                    katalogRefs.delete(colorFootnoteRef, true, DBActionOrigin.FROM_EDIT);
                                }
                            }
                        }
                    }
                } else {
                    if (footnote.hasPartlistEntryFootNotes()) {
                        iPartsDataFootNoteCatalogueRef catalogueRef = ((iPartsDataFootNoteCatalogueRef)footnote.footnoteRef);
                        String key = catalogueRef.getCacheKey();
                        if (!processedFootnoteRefs.contains(key)) {
                            catalogueRef.removeForeignTablesAttributes();
                            katalogRefs.delete(catalogueRef, true, DBActionOrigin.FROM_EDIT);
                        }
                    } else if (isEditForMaterial && footnote.hasPartFootnotes()) {
                        iPartsDataFootNoteMatRef matRef = (iPartsDataFootNoteMatRef)footnote.footnoteRef;
                        if (!processedMatRefs.contains(matRef.getAsId())) {
                            matRefs.delete(matRef, true, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }

            // Eine GenericEtkDataObjectList als Sammel-DBDataObjectList verwenden, damit das ChangeSet innerhalb von
            // einer Transaktion verändert wird
            GenericEtkDataObjectList changeSetDataObjectList = new GenericEtkDataObjectList();
            changeSetDataObjectList.addAll(footNoteList, DBActionOrigin.FROM_EDIT);
            changeSetDataObjectList.addAll(matRefs, DBActionOrigin.FROM_EDIT);
            changeSetDataObjectList.addAll(contentList, DBActionOrigin.FROM_EDIT);
            changeSetDataObjectList.addAll(katalogRefs, DBActionOrigin.FROM_EDIT);
            if (checkIfModified) {
                modifiedAssemblyIds = EditEqualizeFieldsHelper.doEqualizeFootNoteEditor(getProject(), partListEntry,
                                                                                        katalogRefs, changeSetDataObjectList);
            }
            if (isEditForMaterial) {
                modifiedMatRefs = matRefs;
            }
            return changeSetDataObjectList;
        }
        return null;
    }

    public boolean saveToChangeset(boolean reloadFootNotes) {
        GenericEtkDataObjectList changeSetDataObjectList = getDataToSaveToChangeSet();
        if (changeSetDataObjectList != null) {
            addDataObjectListToActiveChangeSetForEdit(changeSetDataObjectList);
            reloadFootNotesWithPseudoTransAction(reloadFootNotes);

            return true;
        }
        return false;
    }

    public void reloadFootNotesWithPseudoTransAction(boolean reloadFootNotes) {
        startPseudoTransactionForActiveChangeSet(true);
        try {
            partListEntry.reloadFootNotes(); // Notwendig, damit die neuen Fußnoten an der Stückliste hängen
            if (reloadFootNotes) {
                reloadFootNotes();
            }
        } finally {
            stopPseudoTransactionForActiveChangeSet();
        }
    }

    private void onButtonOKClicked(Event event) {
        if (stopAndStoreEdit() == EditUserControls.EditResult.STORED) {
            mainWindow.setModalResult(ModalResult.OK);
        } else {
            mainWindow.setModalResult(ModalResult.CANCEL);
        }
        close();
    }

    public List<FootNoteWithType> getOriginalFootnotes() {
        return originals;
    }

    /**
     * Setzt und lädt die übergebenen Fußnoten
     *
     * @param originalFootnotes
     */
    public void setFootnotes(List<FootNoteWithType> originalFootnotes) {
        if (originalFootnotes != null) {
            this.originals = originalFootnotes;
            // jetzt die originals zum bearbeiten kopieren
            footnotes = new ArrayList<>(originals);
            reloadGrid(footnotes, true);
        }
    }


    protected class FootNoteWithType {

        // Entweder eine Referenz zur Stücklistenposition (iPartsDataFootNoteCatalogueRef), zum Teielstamm (iPartsDatatFootNoteMatRef)
        // oder zum DIALOG BCTE Schlüssel (iPartsDataFootNotePosRef)
        private AbstractFootnoteRef footnoteRef;
        // Für Farbtabellenfußnoten
        private List<iPartsDataFootNoteCatalogueRef> colorFootNoteRefList;
        private String colorIds;
        private boolean isCreatedFootNote;
        private FootNoteType type;

        FootNoteWithType(AbstractFootnoteRef footNoteRef, FootNoteType type) {
            if (type == FootNoteType.COLORTABLE_FOOTNOTE) {
                if (footNoteRef instanceof iPartsDataFootNoteCatalogueRef) {
                    this.colorFootNoteRefList = new DwList<>();
                    this.colorFootNoteRefList.add((iPartsDataFootNoteCatalogueRef)footNoteRef);
                }
            } else {
                this.footnoteRef = footNoteRef;
            }
            this.type = type;
        }

        public boolean isVirtual() {
            return (type == FootNoteType.GENERATED_FOOTNOTE) || (type == FootNoteType.CONSTRUCTION_FOOTNOTE);
        }

        public boolean isPartFootNote() {
            return (type == FootNoteType.PART_FOOTNOTE) || (type == FootNoteType.PART_FOOTNOTE_NORMAL) || (type == FootNoteType.PART_FOOTNOTE_STANDARD);
        }

        public boolean hasPartFootnotes() {
            return footnoteRef instanceof iPartsDataFootNoteMatRef;
        }

        public boolean isColorTableFootNote() {
            return type == FootNoteType.COLORTABLE_FOOTNOTE;
        }

        public boolean isConstructionFootNote() {
            return type == FootNoteType.CONSTRUCTION_FOOTNOTE;
        }


        public boolean hasDIALOGFootNotes() {
            return footnoteRef instanceof iPartsDataFootNotePosRef;
        }


        public boolean hasPartlistEntryFootNotes() {
            return footnoteRef instanceof iPartsDataFootNoteCatalogueRef;
        }

        public DBDataObjectList<iPartsDataFootNoteContent> getFootNoteList() {
            if (footnoteRef != null) {
                return footnoteRef.getFootNoteList();
            }
            return null;
        }

        public boolean hasColorFootNotes() {
            return colorFootNoteRefList != null;
        }

        public List<iPartsDataFootNoteCatalogueRef> getColorFootList() {
            return colorFootNoteRefList;
        }

        public String getColorIds() {
            return colorIds;
        }

        public void setColorIds(String colorIds) {
            this.colorIds = colorIds;
        }

        private EtkDataObject getAsDataObject() {
            if (hasPartFootnotes() || hasDIALOGFootNotes() || hasPartlistEntryFootNotes()) {
                return footnoteRef;
            } else if (hasColorFootNotes()) {
                return colorFootNoteRefList.get(0);
            }
            return null;
        }

        private String getStringValue(String fieldName) {
            String value = "";
            EtkDataObject dataObject = getAsDataObject();
            if (dataObject != null) {
                DBDataObjectAttribute attrib = dataObject.getAttribute(fieldName, false);
                if (attrib != null) {
                    value = attrib.getAsString();
                }
            }
            return value;
        }

        private boolean getBooleanValue(String fieldName) {
            boolean value = false;
            EtkDataObject dataObject = getAsDataObject();
            if (dataObject != null) {
                DBDataObjectAttribute attrib = dataObject.getAttribute(fieldName, false);
                if (attrib != null) {
                    value = attrib.getAsBoolean();
                }
            }
            return value;
        }

        public iPartsFootNoteId getFNId() {
            return new iPartsFootNoteId(getStringValue(iPartsConst.FIELD_DFN_ID));
        }

        public String getName() {
            return getStringValue(iPartsConst.FIELD_DFN_NAME);
        }

        public boolean getStandardFootNote() {
            return getBooleanValue(iPartsConst.FIELD_DFN_STANDARD);
        }

        public List<EtkDataObject> getAsDataObjectList(boolean considerSpecialFootnotes) {
            List<EtkDataObject> result = new DwList<>();
            if (considerSpecialFootnotes && (hasPartFootnotes() || hasDIALOGFootNotes())) {
                result.add(footnoteRef);
            } else if (hasPartlistEntryFootNotes()) {
                if (considerSpecialFootnotes || !isVirtual()) {
                    result.add(footnoteRef);
                }
            } else if (hasColorFootNotes()) {
                result.addAll(colorFootNoteRefList);
            }
            return result;
        }

        public boolean isEmptyFootnote() {
            return hasPartlistEntryFootNotes() && footnoteRef.getFieldValue(iPartsConst.FIELD_DFNK_FNID).isEmpty();
        }

        private iPartsDataFootNoteMatRef getFootnoteMatRef() {
            if (isEditForMaterial()) {
                if (isPartFootNote()) {
                    if (!isEmptyFootnote() && hasPartFootnotes()) {
                        EtkDataObject dataObject = getAsDataObject();
                        if ((dataObject != null) && (dataObject instanceof iPartsDataFootNoteMatRef)) {
                            return (iPartsDataFootNoteMatRef)dataObject;
                        }
                    }
                }
            }
            return null;
        }

        public boolean isEditAllowed() {
            if (type != null) {
                if (isEditForMaterial()) {
                    iPartsDataFootNoteMatRef matRef = getFootnoteMatRef();
                    if (matRef != null) {
                        return matRef.isiPartsSource();
                    }
                }
                return type.isEditAllowed();
            }
            return false;
        }

        public boolean isDeleteAllowed() {
            if (type != null) {
                if (isEditForMaterial()) {
                    iPartsDataFootNoteMatRef matRef = getFootnoteMatRef();
                    if (matRef != null) {
                        return matRef.isiPartsSource();
                    }
                }
                return type.isDeleteAllowed();
            }
            return false;
        }
    }


    protected enum FootNoteType {
        PART_FOOTNOTE("!!Stammfußnote", false, false, false, false, false),
        DEFAULT_FOOTNOTE("!!Standardfußnote (Pos)", true, true, true, true, false),
        NORMAL_FOOTNOTE("!!Fußnote (Pos)", true, true, true, true, false),
        TABLE_FOOTNOTE("!!Tabellenfußnote (Pos)", true, true, false, false, false),
        FACTORYDATA_FOOTNOTE("!!Fußnote Werksdaten (Pos)", true, true, false, false, false),
        COLORTABLE_FOOTNOTE("!!Farbtabellenfußnote (Pos)", true, true, false, false, false),
        GENERATED_FOOTNOTE("!!Automatisch erzeugt", false, false, false, false, false),
        CONSTRUCTION_FOOTNOTE("!!Konstruktionsfußnote", false, false, false, false, false),

        PART_FOOTNOTE_STANDARD("!!Stammfußnote (Standard)", false, true, true, true, true),
        PART_FOOTNOTE_NORMAL("!!Stammfußnote (Fußnote)", false, true, true, true, true);

        protected String description;
        protected boolean moveAllowed;
        protected boolean deleteAllowed;
        protected boolean editAllowed;
        protected boolean addToItem;
        protected boolean isMatEdit;
        protected boolean isReadOnly;

        FootNoteType(String description, boolean moveAllowed, boolean deleteAllowed, boolean editAllowed, boolean addToItem, boolean isMatEdit) {
            this.description = description;
            this.moveAllowed = moveAllowed;
            this.deleteAllowed = deleteAllowed;
            this.editAllowed = editAllowed;
            this.addToItem = addToItem;
            this.isMatEdit = isMatEdit;
            this.isReadOnly = false;
        }

        String getDescription() {
            return description;
        }

        public boolean isMoveAllowed() {
            return moveAllowed;
        }

        public boolean isDeleteAllowed() {
            return deleteAllowed;
        }

        public boolean isEditAllowed() {
            if (!isReadOnly) {
                return editAllowed;
            }
            return false;
        }

        public boolean isReadOnly() {
            return isReadOnly;
        }

        public void setIsReadOnly(boolean isReadOnly) {
            this.isReadOnly = isReadOnly;
        }
    }

    protected class FootNoteRow extends DataObjectGrid.GuiTableRowWithObjects {

        FootNoteType footNoteType;
        boolean isDeleteAllowed;

        private FootNoteRow(List<EtkDataObject> dataObjects) {
            super(dataObjects);
        }

        public void setFootnoteType(FootNoteWithType footNote) {
            this.footNoteType = footNote.type;
            isDeleteAllowed = footNote.isDeleteAllowed();
        }
    }

    public class iPartsRelatedEditFootNoteGrid extends DataObjectGrid {

        int defaultLabelHeight;
        private int currentRowNo;
        private FootNoteType currentfootNoteType;
        private Boolean currentfootNoteTypeIsEditAllowed;

        iPartsRelatedEditFootNoteGrid(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm) {
            super(connector, parentForm);
            if (EditControlFactory.useRComboBox()) {
                RComboBox<String> dummyCombobox = new RComboBox<>();
                defaultLabelHeight = dummyCombobox.getPreferredHeight();
            } else {
                GuiComboBox<String> dummyCombobox = new GuiComboBox<>();
                defaultLabelHeight = dummyCombobox.getPreferredHeight();
            }
        }


        /**
         * Callback für ComboBox Type
         *
         * @param event
         * @param rowNo
         * @param indexOfTextKind
         */
        private void onTypeChanged(Event event, int rowNo, int indexOfTextKind) {
            GuiTableRow row = getTable().getRow(rowNo);
            if (row instanceof FootNoteRow) {
                Object textKindChild = row.getChildForColumn(indexOfTextKind);
                Object userObject = null;
                if (textKindChild instanceof GuiPanel) {
                    GuiPanel parent = (GuiPanel)textKindChild;
                    if (!parent.getChildren().isEmpty()) {
                        AbstractGuiControl control = parent.getChildren().get(0);
                        if (control instanceof RComboBox) {
                            userObject = ((RComboBox)control).getSelectedUserObject();
                        } else if (control instanceof GuiComboBox) {
                            userObject = ((GuiComboBox)control).getSelectedUserObject();
                        }
                        if (userObject != null) {
                            doChangeFootNoteType(rowNo, (FootNoteType)userObject);
                        }
                    }
                }
            }
        }

        /**
         * Callback für ComboBox Text
         *
         * @param event
         * @param rowNo
         * @param indexOfTextContent
         */
        private void onSearchTextChanged(Event event, int rowNo, int indexOfTextContent) {
            GuiTableRow row = getTable().getRow(rowNo);
            if (row instanceof FootNoteRow) {
                FootNoteRow footNoteRow = (FootNoteRow)row;
                Object textKindChild = row.getChildForColumn(indexOfTextContent);
                Object userObject = null;
                DictSearchComboBox comboBox = null;
                if (textKindChild instanceof GuiPanel) {
                    GuiPanel parent = (GuiPanel)textKindChild;
                    if (!parent.getChildren().isEmpty()) {
                        AbstractGuiControl control = parent.getChildren().get(0);
                        if (control instanceof DictSearchComboBox) {
                            comboBox = (DictSearchComboBox)control;
                            userObject = comboBox.getSelectedItem();
                        } else if (control instanceof GuiComboBox) {
                            userObject = ((GuiComboBox)control).getSelectedUserObject();
                        } else if (control instanceof RComboBox) {
                            userObject = ((RComboBox)control).getSelectedUserObject();
                        }
                    }
                }
                if (userObject != null) {
                    iPartsDataFootNote dataFootNote = null;
                    iPartsDataFootNoteContentList contentList = null;
                    boolean isCreatedFootNote = false;
                    if (comboBox != null) {
                        String textId = (String)comboBox.getSelectedUserObject();
                        if (StrUtils.isValid(textId)) {
                            contentList = iPartsDataFootNoteContentList.loadFootNoteByDictId(getProject(), textId);
                            if (!contentList.isEmpty()) {
                                iPartsDataFootNoteContent footNoteContent = contentList.get(0);
                                iPartsFootNoteId id = new iPartsFootNoteId(footNoteContent.getAsId().getFootNoteId());
                                dataFootNote = new iPartsDataFootNote(getProject(), id);
                                if (!dataFootNote.loadFromDB(id)) {
                                    dataFootNote = null;
                                } else {
                                    contentList.clear(DBActionOrigin.FROM_DB);
                                    contentList = iPartsDataFootNoteContentList.loadFootNote(getProject(), dataFootNote.getAsId());
                                }
                            }
                            if (dataFootNote == null) {
                                EtkMultiSprache multi = getProject().getDbLayer().getLanguagesTextsByTextId(textId);
                                if (multi != null) {
                                    isCreatedFootNote = true;
                                    iPartsFootNoteContentId newFootNoteId = iPartsFootNoteContentId.createConvertedFootNoteId(textId);
                                    iPartsDataFootNoteContent footNoteContent = new iPartsDataFootNoteContent(getProject(), newFootNoteId);
                                    footNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                                    footNoteContent.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DFNC_TEXT, multi, DBActionOrigin.FROM_EDIT);
                                    iPartsFootNoteId id = new iPartsFootNoteId(newFootNoteId.getFootNoteId());
                                    dataFootNote = new iPartsDataFootNote(getProject(), id);
                                    dataFootNote.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                    contentList.add(footNoteContent, DBActionOrigin.FROM_DB);
                                }
                            }
                        }
                    } else {
                        switch (footNoteRow.footNoteType) {
                            case DEFAULT_FOOTNOTE:
                            case PART_FOOTNOTE_STANDARD:
                                if (userObject instanceof iPartsDataFootNote) {
                                    dataFootNote = (iPartsDataFootNote)userObject;
                                    contentList = iPartsDataFootNoteContentList.loadFootNote(getProject(), dataFootNote.getAsId());
                                }
                                break;
                        }
                    }
                    if (dataFootNote != null) {
                        FootNoteWithType oldFootNoteWithType = footnotes.get(rowNo);
                        iPartsFootNoteCatalogueRefId id = new iPartsFootNoteCatalogueRefId(partListEntry.getAsId(), dataFootNote.getAsId().getFootNoteId());
                        iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef = new iPartsDataFootNoteCatalogueRef(getProject(), id);
                        dataFootNoteCatalogueRef.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
                        copyAttribValues(oldFootNoteWithType.footnoteRef, dataFootNoteCatalogueRef,
                                         iPartsConst.FIELD_DFNK_FN_SEQNO, iPartsConst.FIELD_DFNK_FN_MARKED, iPartsConst.FIELD_DFNK_COLORTABLEFOOTNOTE);
                        addAttributesAndValues(dataFootNoteCatalogueRef, dataFootNote,
                                               iPartsConst.FIELD_DFN_ID, iPartsConst.FIELD_DFN_NAME,
                                               iPartsConst.FIELD_DFN_STANDARD, iPartsConst.FIELD_DFN_TYPE);

                        dataFootNoteCatalogueRef.setFootNoteList(contentList);
                        FootNoteWithType footNoteWithType = new FootNoteWithType(dataFootNoteCatalogueRef, footNoteRow.footNoteType);
                        footNoteWithType.isCreatedFootNote = isCreatedFootNote;
                        if (!contentList.isEmpty() && (contentList.size() > 1)) {
                            footNoteWithType.type = FootNoteType.TABLE_FOOTNOTE;
                        }
                        footnotes.set(rowNo, footNoteWithType);
                        updateGridRow(rowNo, footNoteRow, footNoteWithType);
                        grid.getTable().relayout();
                        updatePreview(true);
                        refreshSuperEdit();
                        formEnableButtons();
                    }
                }
            }
        }

        private void copyAttribValues(AbstractFootnoteRef source, iPartsDataFootNoteCatalogueRef dest, String... fieldNames) {
            for (String fieldName : fieldNames) {
                dest.setFieldValue(fieldName, source.getFieldValue(fieldName), DBActionOrigin.FROM_DB);
            }
        }

        private void addAttributesAndValues(iPartsDataFootNoteCatalogueRef dest, iPartsDataFootNote dataFootNote, String... fieldNames) {
            DBDataObjectAttributes destAttributes = dest.getAttributes();
            for (String fieldName : fieldNames) {
                destAttributes.addField(dataFootNote.getAttribute(fieldName), DBActionOrigin.FROM_DB);
            }
        }

        private void updateGridRow(int rowNo, FootNoteRow footNoteRow, FootNoteWithType footNoteWithType) {
            footNoteRow.dataObjects.clear();
            List<EtkDataObject> list = new DwList<>();
            list.add(footNoteWithType.footnoteRef);
            list.add(footNoteWithType.getFootNoteList().get(0));
            footNoteRow.dataObjects.addAll(list);
            int currentCol = 0;
            for (EtkDisplayField displayField : getDisplayFields().getFields()) {
                if (displayField.isVisible()) {
                    Object obj = footNoteRow.getChildForColumn(currentCol);
                    if ((obj instanceof GuiPanel) && !((GuiPanel)obj).getChildren().isEmpty()) {
                        AbstractGuiControl control = ((GuiPanel)obj).getChildren().get(0);
                        if (control instanceof GuiLabel) {
                            GuiLabel label = (GuiLabel)control;
                            String value = getDefaultStringValue(footNoteRow, displayField);
                            label.setText(value);
                        } else if (displayField.getKey().getName().equals(PSEUDO_TEXT_KIND)) {
                            if ((control instanceof GuiComboBox) || (control instanceof RComboBox)) {
                                control.switchOffEventListeners();
                                if (control instanceof GuiComboBox) {
                                    GuiComboBox guiComboBox = (GuiComboBox)control;
                                    if (!footNoteWithType.type.addToItem) {
                                        guiComboBox.addItem(footNoteWithType.type, TranslationHandler.translate(footNoteWithType.type.getDescription()));
                                    }
                                    guiComboBox.setSelectedUserObject(footNoteWithType.type);
                                    guiComboBox.setEnabled(footNoteWithType.isEditAllowed());
                                } else {
                                    RComboBox guiComboBox = (RComboBox)control;
                                    if (!footNoteWithType.type.addToItem) {
                                        guiComboBox.addItem(footNoteWithType.type, TranslationHandler.translate(footNoteWithType.type.getDescription()));
                                    }
                                    guiComboBox.setSelectedUserObject(footNoteWithType.type);
                                    guiComboBox.setEnabled(footNoteWithType.isEditAllowed());
                                }
                                control.switchOnEventListeners();
                            }
                        } else if (displayField.getKey().getFieldName().equals(iPartsConst.FIELD_DFNC_TEXT)) {
                            control.setEnabled(footNoteWithType.isEditAllowed());
                        }
                    }
                    currentCol++;
                }
            }
        }

        protected void replaceCombobox(final int rowNo, FootNoteType footNoteType, GuiTableRow row, String tableAndFieldName, String value) {
            final int indexOfField = getDisplayFields().getIndexOfFeld(tableAndFieldName, false);
            if (indexOfField > -1) {
                AbstractGuiControl replaceComboBox = createComboBoxByFootNoteType(footNoteType, tableAndFieldName, value);
                Object textChild = row.getChildForColumn(indexOfField);
                if ((textChild instanceof GuiPanel) && !((GuiPanel)textChild).getChildren().isEmpty()) {
                    GuiPanel parent = (GuiPanel)textChild;
                    AbstractGuiControl control = parent.getChildren().get(0);
                    control.removeFromParent();
                    replaceComboBox.setName(tableAndFieldName.toLowerCase());
                    replaceComboBox.setConstraints(getCellConstraints());
                    replaceComboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                        @Override
                        public void fire(Event event) {
                            onSearchTextChanged(event, rowNo, indexOfField);
                        }
                    });
                    parent.addChild(replaceComboBox);
                }
            }
        }

        void addFootNoteToGrid(FootNoteWithType footNote, EtkDataObject... dataObjects) {
            List<EtkDataObject> list = new DwList<>();
            list.addAll(Arrays.asList(dataObjects));

            currentRowNo = getTable().getRowCount();
            currentfootNoteType = footNote.type;
            currentfootNoteTypeIsEditAllowed = footNote.isEditAllowed();
            FootNoteRow row = createRowWithType(list, footNote, getTable().getRowCount());
            getTable().addRow(row);
            currentRowNo = -1;
            currentfootNoteType = null;
            currentfootNoteTypeIsEditAllowed = null;

            int pageSplitNumberOfEntriesPerPage = getTable().getPageSplitNumberOfEntriesPerPage();
            if ((pageSplitNumberOfEntriesPerPage > 0) && (getTable().getRowCount() > pageSplitNumberOfEntriesPerPage)) {
                getTable().setHtmlTablePageSplitMode(HtmlTablePageSplitMode.BUTTONS);
            }
        }

        @Override
        protected ToolbarButtonMenuHelper getToolbarHelper() {
            return super.getToolbarHelper();
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            super.onTableSelectionChanged(event);
            selectPreview();
            formEnableButtons();
        }

        @Override
        protected void createToolbarButtons(GuiToolbar toolbar) {
            ToolbarButtonMenuHelper.ToolbarMenuHolder menuHolder;
            menuHolder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doCreateNewFootNote();
                }
            });
            contextmenuHolder.addChild(menuHolder.menuItem);
            menuHolder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, "!!Anzeigen", getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doView();
                }
            });
            contextmenuHolder.addChild(menuHolder.menuItem);
            menuHolder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doDelete();
                }
            });
            contextmenuHolder.addChild(menuHolder.menuItem);
            menuHolder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MOVE_UP, "!!Eintrag nach oben verschieben", getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doMove(true);
                }
            });
            contextmenuHolder.addChild(menuHolder.menuItem);
            menuHolder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MOVE_DOWN, "!!Eintrag nach unten verschieben", getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doMove(false);
                }
            });
            contextmenuHolder.addChild(menuHolder.menuItem);
        }

        @Override
        protected void createContextMenuItems(GuiContextMenu contextMenu) {
            List<AbstractGuiControl> menuList = new DwList<AbstractGuiControl>(contextmenuHolder.getChildren());
            for (AbstractGuiControl control : menuList) {
                control.removeFromParent();
                contextMenu.addChild(control);
            }
        }

        FootNoteRow createRowWithType(List<EtkDataObject> dataObjects, FootNoteWithType footNote, final int rowNo) {
            FootNoteRow row = createRow(dataObjects);
            row.setFootnoteType(footNote);
            if (currentfootNoteType == null) {
                final int indexOfTextKind = getDisplayFields().getIndexOfFeld(PSEUDO_TEXT_KIND, false);
                if (indexOfTextKind > -1) {
                    Object textKindChild = row.getChildForColumn(indexOfTextKind);
                    if (EditControlFactory.useRComboBox()) {
                        if ((textKindChild instanceof GuiPanel) && !((GuiPanel)textKindChild).getChildren().isEmpty()) {
                            GuiPanel parent = ((GuiPanel)textKindChild);
                            RComboBox comboBox = (RComboBox)parent.getChildren().get(0);
                            if (!footNote.type.addToItem) {
                                comboBox.addItem(footNote.type, TranslationHandler.translate(footNote.type.getDescription()));
                            }
                            comboBox.setSelectedUserObject(footNote.type);
                            if (footNote.isEditAllowed()) {
                                comboBox.setEnabled(true);
                                comboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                                    @Override
                                    public void fire(Event event) {
                                        onTypeChanged(event, rowNo, indexOfTextKind);
                                    }
                                });
                                modifyFootNoteTextElement(footNote.type, rowNo, row);
                            }
                        }
                    } else {
                        if (textKindChild instanceof GuiComboBox) {
                            GuiComboBox comboBox = (GuiComboBox)textKindChild;
                            if (!footNote.type.addToItem) {
                                comboBox.addItem(footNote.type, TranslationHandler.translate(footNote.type.getDescription()));
                            }
                            comboBox.setSelectedUserObject(footNote.type);
                            if (footNote.isDeleteAllowed()) {
                                comboBox.setEnabled(true);
                                comboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                                    @Override
                                    public void fire(Event event) {
                                        onTypeChanged(event, rowNo, indexOfTextKind);
                                    }
                                });
                                modifyFootNoteTextElement(footNote.type, rowNo, row);
                            }
                        }
                    }
                }
            }
            return row;
        }

        private void modifyFootNoteTextElement(FootNoteType type, final int rowNo, FootNoteRow row) {
            Object textKindChild;
            String tableAndFieldName = TableAndFieldName.make(iPartsConst.TABLE_DA_FN_CONTENT, iPartsConst.FIELD_DFNC_TEXT);
            final int indexOfTextContent = getDisplayFields().getIndexOfFeld(tableAndFieldName, false);
            if (indexOfTextContent > -1) {
                textKindChild = row.getChildForColumn(indexOfTextContent);
                if ((textKindChild instanceof GuiPanel) && !((GuiPanel)textKindChild).getChildren().isEmpty()) {
                    GuiPanel parent = (GuiPanel)textKindChild;
                    AbstractGuiControl control = parent.getChildren().get(0);
                    String value = "";
                    if (control instanceof DictSearchComboBox) {
                        value = ((DictSearchComboBox)control).getSelectedItem();
                    } else if (control instanceof RComboBox) {
                        value = ((RComboBox)control).getSelectedItem();
                    } else if (control instanceof GuiComboBox) {
                        value = ((GuiComboBox)control).getSelectedItem();
                    }
                    grid.replaceCombobox(rowNo, type, row, tableAndFieldName, value);
                }
            }
        }

        @Override
        protected FootNoteRow createRow(List<EtkDataObject> dataObjects) {
            FootNoteRow row = new FootNoteRow(dataObjects);

            int rowNoIndex = 0;
            for (EtkDisplayField field : displayFields.getFields()) {
                if (field.isVisible()) {
                    if (field.getKey().getName().equals(PSEUDO_TEXT_KIND)) {
                        row.addChild(createTextKindCellComboBox(field.getKey().getName(), rowNoIndex), TextRepresentation.EMPTY_TEXT_REPRESENTATION);
                    } else if (field.getKey().getName().equals(TableAndFieldName.make(iPartsConst.TABLE_DA_FN_CONTENT, iPartsConst.FIELD_DFNC_TEXT))) {
                        String fieldName = field.getKey().getFieldName();
                        String tableName = field.getKey().getTableName();
                        EtkDataObject objectForTable = row.getObjectForTable(tableName);
                        String value = "";
                        if (objectForTable != null) {
                            String dbLanguage = getProject().getDBLanguage();
                            if ((currentfootNoteType != null) && (currentfootNoteType == FootNoteType.FACTORYDATA_FOOTNOTE)) {
                                value = ((iPartsDataFootNoteContent)objectForTable).getUnmodifiedText(dbLanguage,
                                                                                                      getProject().getDataBaseFallbackLanguages());
                                value = StrUtils.replaceNewlinesWithSpaces(value);
                            } else {
                                value = getVisObject().asString(tableName, fieldName, objectForTable.getAttributeForVisObject(fieldName),
                                                                dbLanguage, true);
                            }
                        }
                        row.addChild(createTextContentCellComboBox(field.getKey().getName(), value, rowNoIndex), TextRepresentation.EMPTY_TEXT_REPRESENTATION);
                    } else {
                        String value = getDefaultStringValue(row, field);
                        if (field.getKey().getFieldName().equals(iPartsConst.FIELD_DFN_NAME) && !StrUtils.isValid(value)) {
                            value = getDefaultStringValue(row, iPartsConst.TABLE_DA_FN_CONTENT, iPartsConst.FIELD_DFNC_FNID);
                        }
                        String finalValue = value;
                        row.addChild(createTextCell(value), () -> finalValue);
                    }
                    rowNoIndex++;
                }
            }

            return row;
        }

        private String getDefaultStringValue(FootNoteRow row, EtkDisplayField field) {
            String fieldName = field.getKey().getFieldName();
            String tableName = field.getKey().getTableName();
            return getDefaultStringValue(row, tableName, fieldName);
        }

        private String getDefaultStringValue(FootNoteRow row, String tableName, String fieldName) {
            EtkDataObject objectForTable = null;
            if (tableName.isEmpty()) {
                // sollte beim Display Field der Tabellen Namen nicht mit angegeben sein, dann wird gesucht
                // in welchem DataObject das entsprechende Feld enthalten ist
                for (EtkDataObject dataObject : row.dataObjects) {
                    if (dataObject.attributeExists(fieldName)) {
                        objectForTable = dataObject;
                        break;
                    }
                }
            } else {
                objectForTable = row.getObjectForTable(tableName);
            }

            String value = "";
            if (objectForTable != null) {
                value = getVisObject().asString(tableName, fieldName, objectForTable.getAttributeForVisObject(fieldName),
                                                getProject().getDBLanguage(), true);
            }
            return value;
        }

        private AbstractGuiControl createTextKindCellComboBox(String tableAndFieldName, final int rowNoIndex) {
            if (tableAndFieldName.contentEquals(PSEUDO_TEXT_KIND)) {
                if (EditControlFactory.useRComboBox()) {
                    RComboBox<FootNoteType> comboBox = new RComboBox<>();
                    comboBox.setFilterable(false);
                    for (FootNoteType footNoteType : FootNoteType.values()) {
                        boolean addToItem = false;
                        if (isEditForMaterial) {
                            if (footNoteType.isMatEdit) {
                                addToItem = footNoteType.addToItem;
                            }
                        } else {
                            if (!footNoteType.isMatEdit) {
                                addToItem = footNoteType.addToItem;
                            }
                        }
                        if (addToItem) {
                            comboBox.addItem(footNoteType, TranslationHandler.translate(footNoteType.getDescription()));
                        }
                    }
                    if (currentfootNoteType != null) {
                        if (!currentfootNoteType.addToItem) {
                            comboBox.addItem(currentfootNoteType, TranslationHandler.translate(currentfootNoteType.getDescription()));
                        }
                        comboBox.setSelectedUserObject(currentfootNoteType);
                        if (currentfootNoteTypeIsEditAllowed /*currentfootNoteType.isEditAllowed()*/) {
                            comboBox.setEnabled(true);
                            final int rowNo = currentRowNo;
                            comboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                                @Override
                                public void fire(Event event) {
                                    onTypeChanged(event, rowNo, rowNoIndex);
                                }
                            });
                        } else {
                            comboBox.setEnabled(false);
                        }
                    } else {
                        comboBox.setEnabled(false);
                    }
                    comboBox.setName(tableAndFieldName.toLowerCase());
                    GuiPanel panel = createCellPanel();
                    panel.setName(tableAndFieldName.toLowerCase());
                    comboBox.setConstraints(getCellConstraints());
                    panel.addChild(comboBox);
                    return panel;
                } else {
                    // Combobox befüllen und einhängen
                    GuiComboBox<FootNoteType> comboBox = new GuiComboBox<>();
                    for (FootNoteType footNoteType : FootNoteType.values()) {
                        boolean addToItem = false;
                        if (isEditForMaterial) {
                            if (footNoteType.isMatEdit) {
                                addToItem = footNoteType.addToItem;
                            }
                        } else {
                            if (!footNoteType.isMatEdit) {
                                addToItem = footNoteType.addToItem;
                            }
                        }
                        if (addToItem) {
                            comboBox.addItem(footNoteType, TranslationHandler.translate(footNoteType.getDescription()));
                        }
                    }
                    comboBox.setEnabled(false);
                    comboBox.setName(tableAndFieldName.toLowerCase());
                    GuiPanel panel = createCellPanel();
                    panel.setName(tableAndFieldName.toLowerCase());
                    comboBox.setConstraints(getCellConstraints());
                    panel.addChild(comboBox);
                    return panel;
                }
            }
            return createCellPanel();
        }

        private AbstractGuiControl createTextContentCellComboBox(String tableAndFieldName, String value, final int rowNoIndex) {
            if (tableAndFieldName.equals(TableAndFieldName.make(iPartsConst.TABLE_DA_FN_CONTENT, iPartsConst.FIELD_DFNC_TEXT))) {
                FootNoteType footNoteType = (currentfootNoteType == null) ? FootNoteType.PART_FOOTNOTE : currentfootNoteType;
                AbstractGuiControl control = createComboBoxByFootNoteType(footNoteType, tableAndFieldName, value);
                if (control != null) {
                    if (currentfootNoteType != null) {
                        if (control.isEnabled()) {
                            final int rowNo = currentRowNo;
                            control.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                                @Override
                                public void fire(Event event) {
                                    onSearchTextChanged(event, rowNo, rowNoIndex);
                                }
                            });
                        }
                    }
                    GuiPanel panel = createCellPanel();
                    panel.setName(tableAndFieldName.toLowerCase());
                    control.setConstraints(getCellConstraints());
                    panel.addChild(control);
                    return panel;
                }
            }
            return createTextCell(value);
        }

        private AbstractGuiControl createComboBoxByFootNoteType(FootNoteType type, String tableAndFieldName, String value) {
            AbstractGuiControl control;
            boolean isEditAllowed = (currentfootNoteTypeIsEditAllowed != null) ? currentfootNoteTypeIsEditAllowed : type.isEditAllowed();
            switch (type) {
                case DEFAULT_FOOTNOTE:
                case PART_FOOTNOTE_STANDARD:
                    // normale RGuiComboBox gefüllt enabled
                    control = createNormalComboBox(tableAndFieldName, value, isEditAllowed /*type.isEditAllowed()*/);
                    if (control instanceof RComboBox) {
                        RComboBox comboBox = (RComboBox)control;
                        comboBox.removeAllItems(); // Die Standardfußnote ohne Nummer wurde bereits hinzugefügt und muss jetzt entfernt werden
                        String language = getProject().getDBLanguage();
                        iPartsDataFootNote selectedDataFootNote = null;
                        iPartsStandardFootNotesCache standardFootnotesCache = iPartsStandardFootNotesCache.getInstance(getProject());
                        Collection<iPartsDataFootNote> allStandardFootNotes = standardFootnotesCache.getAllStandardFootNotesWithContent(getProject());
                        for (iPartsDataFootNote dataFootNote : allStandardFootNotes) {
                            String footNoteText = dataFootNote.getFieldValue(iPartsConst.FIELD_DFNC_TEXT, language, true);
                            if (footNoteText.equals(value)) {
                                selectedDataFootNote = dataFootNote;
                            }
                            comboBox.addItem(dataFootNote, dataFootNote.getFieldValue(iPartsConst.FIELD_DFN_ID) + " " + footNoteText);
                        }
                        comboBox.setSelectedUserObject(selectedDataFootNote);
                    }
                    break;
                case NORMAL_FOOTNOTE:
                case PART_FOOTNOTE_NORMAL:
                    // DictSearchComboBox enabled
                    DictSearchComboBox dictSearchComboBox = new DictSearchComboBox(getProject(), DictTextKindTypes.FOOTNOTE);
                    dictSearchComboBox.setFilterText(value, null, false);
                    dictSearchComboBox.setEnabled(isEditAllowed /*type.isEditAllowed()*/);
                    dictSearchComboBox.setName(tableAndFieldName.toLowerCase());
                    control = dictSearchComboBox;
                    break;
                default:
                    control = createNormalComboBox(tableAndFieldName, value, isEditAllowed /*type.isEditAllowed()*/);
                    break;
            }
            if (searchBoxWidth > 0) {
                control.setMaximumWidth(searchBoxWidth);
                control.setMinimumWidth(searchBoxWidth);
            }
            return control;
        }

        private AbstractGuiControl createNormalComboBox(String tableAndFieldName, String value, boolean enabled) {
            if (EditControlFactory.useRComboBox()) {
                RComboBox<String> comboBox = new RComboBox<>();
                comboBox.addItem(value);
                comboBox.setEnabled(enabled);
                comboBox.setName(tableAndFieldName.toLowerCase());
                return comboBox;
            } else {
                GuiComboBox<String> comboBox = new GuiComboBox<>();
                comboBox.addItem(value);
                comboBox.setEnabled(enabled);
                comboBox.setName(tableAndFieldName.toLowerCase());
                return comboBox;
            }
        }

        private GuiPanel createTextCell(String text) {
            if (text.isEmpty()) {
                text = " ";
            }
            GuiPanel panel = createCellPanel();
            GuiLabel label = new GuiLabel(text);
            label.setConstraints(getCellConstraints());
            panel.addChild(label);
            return panel;
        }

        private ConstraintsGridBag getCellConstraints() {
            return new ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, LayoutGridBag.ANCHOR_WEST, LayoutGridBag.FILL_HORIZONTAL, 0, 4, 0, 4);
        }

        private GuiPanel createCellPanel() {
            GuiPanel panel = new GuiPanel(new LayoutGridBag(false));
            panel.setMinimumHeight(defaultLabelHeight);
            panel.setBackgroundColor(Colors.clTransparent.getColor());
            return panel;
        }
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
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextmenuHolder = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextmenuHolder.setName("contextmenuHolder");
            contextmenuHolder.__internal_setGenerationDpi(96);
            contextmenuHolder.registerTranslationHandler(translationHandler);
            contextmenuHolder.setScaleForResolution(true);
            contextmenuHolder.setMinimumWidth(10);
            contextmenuHolder.setMinimumHeight(10);
            contextmenuHolder.setMenuName("contextmenuHolder");
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
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonOKClicked(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}