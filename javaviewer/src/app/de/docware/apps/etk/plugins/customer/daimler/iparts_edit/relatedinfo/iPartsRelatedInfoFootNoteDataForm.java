/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListCellContentFromPlugin;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.PartsListIconOrder;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.RelatedInfoSingleEditHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiFootNoteViewerPanel;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class iPartsRelatedInfoFootNoteDataForm extends AbstractRelatedInfoPartlistDataForm {

    public static final String IPARTS_MENU_ITEM_SHOW_FOOT_NOTE_DATA = "iPartsMenuItemShowFootNoteData";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.SA_TU,
                                                                                   iPartsModuleTypes.WorkshopMaterial,
                                                                                   iPartsModuleTypes.DialogRetail,
                                                                                   iPartsModuleTypes.EDSRetail,
                                                                                   iPartsModuleTypes.PSK_PKW,
                                                                                   iPartsModuleTypes.PSK_TRUCK,
                                                                                   iPartsModuleTypes.Dialog_SM_Construction);


    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_FOOT_NOTE_DATA, "!!Fußnoten anzeigen",
                                EditDefaultImages.edit_btn_footNotes.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_FOOT_NOTE_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste gültig für den gewünschten Modultyp?
        boolean menuItemVisible = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            menuItemVisible = relatedInfoIsVisible(connector.getSelectedPartListEntries().get(0));
        }

        // Im Edit nur Menüeinträge zur Bearbeitung anzeigen
        if (menuItemVisible && isEditContext(connector, true)) {
            menuItemVisible = false;
        }

        // Separator und Menüeintrag aktualisieren
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_FOOT_NOTE_DATA, menuItemVisible);
    }


    private boolean withAlternateViewing = true;
    private iPartsGuiFootNoteViewerPanel viewerPanel;

    public iPartsRelatedInfoFootNoteDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        return null;
    }


    public static boolean relatedInfoIsVisible(EtkDataPartListEntry entry) {
        return relatedInfoIsVisible(entry, false);
    }

    private static boolean relatedInfoIsVisible(EtkDataPartListEntry entry, boolean isEditMode) {
        if (entry != null) {
            EtkDataAssembly assembly = entry.getOwnerAssembly();
            if (AbstractRelatedInfoPartlistDataForm.relatedInfoIsVisible(assembly, VALID_MODULE_TYPES)) {
                if (entry instanceof iPartsDataPartListEntry) {
                    Collection<iPartsFootNote> footNotes;
                    if (isEditMode) {
                        footNotes = ((iPartsDataPartListEntry)entry).getFootNotes();
                    } else {
                        footNotes = ((iPartsDataPartListEntry)entry).getFootNotesForRetail();
                    }
                    return (footNotes != null) && !footNotes.isEmpty();
                }
            }
        }
        return false;
    }

    public static boolean relatedInfoIsVisible(AssemblyListFormIConnector connector) {
        if (connector.getSelectedPartListEntries().size() == 1) {
            return relatedInfoIsVisible(connector.getSelectedPartListEntries().get(0));
        }
        return false;
    }


    public static AssemblyListCellContentFromPlugin getRelatedInfoIcon(EtkDataPartListEntry entry, boolean isEditMode) {
        if (relatedInfoIsVisible(entry, isEditMode)) {
            String pathName = iPartsConst.CONFIG_KEY_RELATED_INFO_FOOT_NOTE_DATA;
            if (isEditMode) {
                pathName = RelatedInfoSingleEditHelper.getActiveRelatedInfo(entry.getEtkProject(), iPartsConst.CONFIG_KEY_RELATED_INFO_FOOT_NOTE_DATA);
            }

            AssemblyListCellContentFromPlugin iconInfo = new AssemblyListCellContentFromPlugin(pathName,
                                                                                               EditDefaultImages.edit_btn_footNotes.getImage());
            iconInfo.setHint(TranslationHandler.translate("!!Fußnoten"));
            iconInfo.setCursor(DWCursor.Hand);
            iconInfo.setIconType(PartsListIconOrder.PartsListIconType.itPlugin);
            return iconInfo;
        }
        return null;
    }


    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        if (withAlternateViewing) {
            viewerPanel = new iPartsGuiFootNoteViewerPanel();
            viewerPanel.setConstraints(mainWindow.scrollPaneFootNotes.getConstraints());
            mainWindow.scrollPaneFootNotes.removeFromParent();
            mainWindow.panelMain.addChild(viewerPanel);
        }
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    /**
     * Holt sich die Haupt-Gui-Komponente
     *
     * @return Die Hautp-Gui-Komponente (form oder window)
     */
    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }


    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().getActiveRelatedSubForm() == this) {
            if (withAlternateViewing) {
                Collection<iPartsFootNote> footNotes = null;
                if (getConnector().getRelatedInfoData().isPartListEntryId()) {
                    EtkDataPartListEntry entry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
                    if (entry instanceof iPartsDataPartListEntry) {
                        footNotes = ((iPartsDataPartListEntry)entry).getFootNotesForRetail();
                    }
                }
                viewerPanel.setFootNotes(getProject(), footNotes);
            } else {
                StringBuilder s = new StringBuilder();
                if (getConnector().getRelatedInfoData().isPartListEntryId()) {
                    EtkDataPartListEntry entry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
                    if (entry instanceof iPartsDataPartListEntry) {
                        Collection<iPartsFootNote> footNotes = ((iPartsDataPartListEntry)entry).getFootNotesForRetail();
                        if (footNotes != null) {
                            for (iPartsFootNote footNote : footNotes) {
                                String footNoteName = footNote.getFootNoteName(getProject());
                                for (String footNoteText : footNote.getFootNoteTexts(getProject())) {
                                    List<String> lines = StrUtils.toStringList(footNoteText, "\n", true);

                                    // Falls der Fußnotentext mehrere Zeilen enthält, vor jede Zeile die Fußnotennummer
                                    for (String line : lines) {
                                        if (s.length() > 0) {
                                            s.append('\n');
                                        }
                                        if (!StrUtils.isEmpty(footNoteName)) {
                                            s.append(footNoteName);
                                            s.append(" ");
                                        }
                                        s.append(line);
                                    }
                                }
                            }
                        }
                    }
                }
                mainWindow.textareaFootNotes.setText(s.toString());
            }
        }
    }

    private void closeWindow(Event event) {
        close();
        mainWindow.setVisible(false);
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
        private de.docware.framework.modules.gui.controls.GuiLabel labelFootNotes;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollPaneFootNotes;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaFootNotes;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setTitle("!!Fußnoten");
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
            panelMain.setPaddingTop(4);
            panelMain.setPaddingLeft(8);
            panelMain.setPaddingRight(8);
            panelMain.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            labelFootNotes = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelFootNotes.setName("labelFootNotes");
            labelFootNotes.__internal_setGenerationDpi(96);
            labelFootNotes.registerTranslationHandler(translationHandler);
            labelFootNotes.setScaleForResolution(true);
            labelFootNotes.setMinimumWidth(10);
            labelFootNotes.setMinimumHeight(10);
            labelFootNotes.setPaddingBottom(4);
            labelFootNotes.setText("!!Fußnoten:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelFootNotesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelFootNotesConstraints.setPosition("north");
            labelFootNotes.setConstraints(labelFootNotesConstraints);
            panelMain.addChild(labelFootNotes);
            scrollPaneFootNotes = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollPaneFootNotes.setName("scrollPaneFootNotes");
            scrollPaneFootNotes.__internal_setGenerationDpi(96);
            scrollPaneFootNotes.registerTranslationHandler(translationHandler);
            scrollPaneFootNotes.setScaleForResolution(true);
            scrollPaneFootNotes.setMinimumWidth(0);
            scrollPaneFootNotes.setMinimumHeight(0);
            scrollPaneFootNotes.setBorderWidth(1);
            scrollPaneFootNotes.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textareaFootNotes = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaFootNotes.setName("textareaFootNotes");
            textareaFootNotes.__internal_setGenerationDpi(96);
            textareaFootNotes.registerTranslationHandler(translationHandler);
            textareaFootNotes.setScaleForResolution(true);
            textareaFootNotes.setMinimumWidth(0);
            textareaFootNotes.setMinimumHeight(0);
            textareaFootNotes.setFontName("Monospaced");
            textareaFootNotes.setFontSize(13);
            textareaFootNotes.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaFootNotesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaFootNotes.setConstraints(textareaFootNotesConstraints);
            scrollPaneFootNotes.addChild(textareaFootNotes);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollPaneFootNotesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollPaneFootNotes.setConstraints(scrollPaneFootNotesConstraints);
            panelMain.addChild(scrollPaneFootNotes);
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