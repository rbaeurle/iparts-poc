/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkResponsiveDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleDoubleListSelectForm;
import de.docware.apps.etk.base.mechanic.listview.forms.PartListEntryUserObjectForTableRow;
import de.docware.apps.etk.base.misc.EtkTableHelper;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditAssemblyListForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsEditBaseValidationForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.RunTimeLogger;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsEditAssemblyListFormConnectorWithFilterSettings;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.*;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.Utils;
import de.docware.util.sql.TableAndFieldName;

import java.util.ArrayList;
import java.util.List;

/**
 * Basisform zur Anzeige von Validierungen auf Stücklisten-Ebene.
 */
public abstract class iPartsEditAssemblyListValidationForm extends iPartsEditBaseValidationForm {

    public static final String IPARTS_MENU_ITEM_OPEN_EDIT_RETAIL_WINDOW = "iPartsMenuItemOpenEditRetailWindow";
    public static final String IPARTS_MENU_TEXT_OPEN_EDIT_RETAIL_WINDOW = "!!Gehe zur Teileposition";

    protected iPartsAssemblyId loadedAssemblyId;
    protected iPartsOmittedParts omittedParts;

    private AbstractJavaViewerForm relatedFormForNonModalShow;
    private ValidationTimers runTimeLoggers;

    public iPartsEditAssemblyListValidationForm(AbstractJavaViewerFormIConnector dataConnector,
                                                AbstractJavaViewerForm parentForm,
                                                boolean removeAdditionalInfoPanel,
                                                boolean minimizeAdditionalInfoPanel) {
        super(dataConnector, parentForm, removeAdditionalInfoPanel, minimizeAdditionalInfoPanel, TABLE_KATALOG);
        omittedParts = iPartsOmittedParts.getInstance(getProject());
    }

    protected boolean logPerformanceMessages() {
        return runTimeLoggers != null;
    }

    @Override
    protected AbstractJavaViewerForm createValidationContent(final iPartsFilterSwitchboard filterForModelEvaluationSwitchboard) {
        iPartsEditAssemblyListFormConnectorWithFilterSettings assemblyListFormConnectorUnfiltered =
                new iPartsEditAssemblyListFormConnectorWithFilterSettings(getConnector()) {

                    private List<EtkDataPartListEntry> currentPartListEntries;
                    private List<EtkDisplayField> oldDesktopDisplayFields;

                    @Override
                    public List<EtkDataPartListEntry> getCurrentPartListEntries() {
                        if (currentPartListEntries != null) { // Cache für die Stücklisteneinträge
                            return currentPartListEntries;
                        }
                        final iPartsDataAssembly assembly = (iPartsDataAssembly)getCurrentAssembly();
                        String kVari = assembly.getAsId().getKVari();
                        RunTimeLogger runTimeLogger = null;
                        if (logPerformanceMessages()) {
                            runTimeLogger = new RunTimeLogger(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK, true);
                            Logger.log(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK, LogType.DEBUG, "Starting quality check for TU \"" + kVari + "\"");
                        }

                        // Durch updateValidationContent() ist die Stückliste nach dem Ändern der selectedModels immer "sauber" ohne
                        // virtuelle Baumusterauswertungs-Felder
                        final List<EtkDataPartListEntry> filteredPartList = getUnfilteredPartListEntries();

                        if (getMessageLog() != null) {
                            getMessageLog().clear();
                            resetMessageLog(filteredPartList.size() + getModelsForEvaluation().size());
                            getMessageLog().setAutoClose(true);
                            if (!iPartsEditAssemblyListValidationForm.this.getMainWindow().isShownModal()) {
                                // Beim Updaten der Anzeige im nicht modalen Fenster, muss das Abbrechen unterbunden werden,
                                // da sonst halbgare Daten zur Anzeige kommen würden.
                                getMessageLog().disableButtons(true);
                            }
                            getMessageLog().showModal(getRootWindow(), new FrameworkRunnable() {
                                @Override
                                public void run(FrameworkThread thread) {
                                    try {
                                        if (EtkDataObjectFactory.createDataAssembly(getProject(), assembly.getAsId()).existsInDB()) {
                                            doValidation(filteredPartList, assembly);
                                        } else {
                                            MessageDialog dlg = new MessageDialog("!!Die zu prüfende Stückliste existiert nicht mehr.", "!!Fehler",
                                                                                  null, MessageDialogIcon.ERROR.getImage(),
                                                                                  new MessageDialogButtons[]{ MessageDialogButtons.OK }, false);
                                            dlg.showModal(getRootWindow());
                                            iPartsEditAssemblyListValidationForm.this.close();
                                        }
                                    } catch (Exception e) {
                                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                                        MessageDialog dlg = new MessageDialog("!!Während der Qualitätsprüfung ist ein Fehler aufgetreten.", "!!Fehler",
                                                                              null, MessageDialogIcon.ERROR.getImage(),
                                                                              new MessageDialogButtons[]{ MessageDialogButtons.OK }, false);
                                        dlg.showModal(getRootWindow());
                                    }
                                }
                            });
                        } else {
                            doValidation(filteredPartList, assembly);
                        }

                        currentPartListEntries = filteredPartList;
                        if (logPerformanceMessages()) {
                            Logger.log(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK, LogType.DEBUG, ("Finished check for TU \"" + kVari + "\" in " + runTimeLogger.getDurationString()));
                        }
                        return filteredPartList;
                    }

                    @Override
                    public void setCurrentAssembly(EtkDataAssembly value) {
                        if (value != getCurrentAssembly()) {
                            currentPartListEntries = null; // Cache für die Stücklisteneinträge zurücksetzen
                        }
                        super.setCurrentAssembly(value);
                    }

                    @Override
                    public void dataChanged(AbstractJavaViewerForm sender) {
                        if (validationContent instanceof IPartsRelatedInfoAssemblyListForm) {
                            IPartsRelatedInfoAssemblyListForm assemblyListForm = (IPartsRelatedInfoAssemblyListForm)validationContent;
                            if (assemblyListForm.isDisposed()) {
                                return;
                            }
                            EtkDataAssembly currentAssembly = assemblyListForm.getConnector().getCurrentAssembly();
                            if ((currentAssembly == null) || !currentAssembly.getAsId().isValidId()) {
                                // Dieses ValidationForm wurde gar nicht initialisiert (weil unnötig)
                                return;
                            }

                            EtkResponsiveDisplayFields displayFields = new EtkResponsiveDisplayFields(getProject(), assemblyListForm.getDisplayFieldsForTableHelper(),
                                                                                                      assemblyListForm.getEbeneNameForSessionSave(),
                                                                                                      assemblyListForm.getExtraConfigKeyForDisplayFields());
                            List<EtkDisplayField> desktopDisplayFields = displayFields.getDesktopDisplayList();
                            if (!Utils.objectEquals(desktopDisplayFields, oldDesktopDisplayFields)) {
                                super.dataChanged(sender);
                                oldDesktopDisplayFields = new ArrayList<>(desktopDisplayFields);
                            }
                        }

                        // Nicht reagieren
                    }
                };

        assemblyListFormConnectorUnfiltered.setFilterActive(false);

        IPartsRelatedInfoAssemblyListForm form = new IPartsRelatedInfoAssemblyListForm(assemblyListFormConnectorUnfiltered);
        form.setName("validateModuleForm");
        form.addOwnConnector(assemblyListFormConnectorUnfiltered);
        return form;
    }

    private void doValidation(List<EtkDataPartListEntry> filteredPartList, iPartsDataAssembly assembly) {
        beforeValidatePartList(filteredPartList, assembly);

        if (isValidationForPartListPossible(assembly)) {
            doValidationForModels(filteredPartList, assembly);
        } else { // Keine Prüfungen für Stückliste und Farben möglich
            Session.invokeThreadSafeInSession(() -> {
                getGui().removeAllChildren();
                GuiLabel noValidationPossibleLabel = new GuiLabel(translate("!!Für den Dokumentationstyp \"%1\" gibt es aktuell keine Überprüfungen.",
                                                                            assembly.getDocumentationType().getExportValue()));
                noValidationPossibleLabel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                noValidationPossibleLabel.setPadding(8);
                getGui().addChild(noValidationPossibleLabel);
            });
        }

        afterValidatePartList(filteredPartList, assembly);
    }

    protected void doValidationForModels(List<EtkDataPartListEntry> filteredPartList, iPartsDataAssembly assembly) {
        if (logPerformanceMessages()) {
            runTimeLoggers.setStartTime(TIMER_TYPES.TIMER_MODEL_FILTER_CHECK);
        }
        fireMessage("!!Qualitätsprüfung für Stückliste");
        // Für alle Baumuster aus selectedModels eine virtuelle Baumuster-Datenkarte erzeugen, damit filtern und
        // das Filterergebnis über den Filtergrund an das virtuelle Feld für die Baumusterauswertung übertragen
        EtkProject project = getProject();
        for (String selectedModel : getModelsForEvaluation()) {
            // Baumuster-Datenkarte erzeugen und im Filter setzen
            AbstractDataCard dataCard = filterForModelEvaluation.setDataCardByModel(selectedModel, project);
            filterForInvisibleEntries.setCurrentDataCard(dataCard, project);

            validatePartList(filteredPartList, assembly, selectedModel);

            // Zum Schluss den Filtergrund wieder löschen (lieber in einer eigenen Schleife, da Aufrufe von checkFilter()
            // durchaus auch den Filtergrund von anderen Stücklisteneinträgen setzen können
            for (EtkDataPartListEntry partListEntry : filteredPartList) {
                filterForModelEvaluation.clearFilterReasonForDataObject(partListEntry, true);
                filterForInvisibleEntries.clearFilterReasonForDataObject(partListEntry, true);
            }

            // Gefilterte Werkseinsatzdaten und darin v.a. auch das für die Filterung verwendete Baumuster zurücksetzen
            if (assembly != null) {
                assembly.clearAllFactoryDataForRetailForPartList();
            }
        }
        if (logPerformanceMessages()) {
            runTimeLoggers.stopTimeAndStore(TIMER_TYPES.TIMER_MODEL_FILTER_CHECK);
        }
    }

    protected void beforeValidatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly) {
        if (logPerformanceMessages()) {
            runTimeLoggers = new ValidationTimers(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK);
            runTimeLoggers.setkVari(assembly.getAsId().getKVari());
        }
    }

    protected abstract void validatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly, String selectedModel);

    protected void afterValidatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly) {
        if (logPerformanceMessages()) {
            runTimeLoggers.logRunTimes();
        }
    }

    protected boolean isPartListEntryInvisibleAndValid(EtkDataPartListEntry partListEntry) {
        if (omittedParts == null) {
            omittedParts = iPartsOmittedParts.getInstance(getProject());
        }
        boolean isInvisible = omittedParts.isOmittedPart(partListEntry) || partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT)
                              || partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_ONLY_MODEL_FILTER);
        return isInvisible && filterForInvisibleEntries.checkFilter(partListEntry);
    }

    public List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<>();
        EtkDisplayField displayField = new EtkDisplayField(EtkDbConst.TABLE_KATALOG, EtkDbConst.FIELD_K_POS, false, false);
        displayField.setDefaultWidth(false);
        displayField.setWidth(7);
        displayField.setColumnFilterEnabled(true);
        displayField.setInFlyerAnzeigen(true);
        displayField.setInRelatedInfoAnzeigen(true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(EtkDbConst.TABLE_MAT, EtkDbConst.FIELD_M_BESTNR, false, false);
        displayField.setDefaultWidth(false);
        displayField.setWidth(15);
        displayField.setColumnFilterEnabled(true);
        displayField.setInFlyerAnzeigen(true);
        displayField.setInRelatedInfoAnzeigen(true);
        displayField.setText(new EtkMultiSprache("!!Teilenummer", getProject().getConfig().getViewerLanguages()));
        displayField.setDefaultText(false);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(EtkDbConst.TABLE_MAT, EtkDbConst.FIELD_M_TEXTNR, true, false);
        displayField.setDefaultWidth(false);
        displayField.setWidth(100);
        displayField.setGrowColumn(true);
        displayField.setInFlyerAnzeigen(true);
        displayField.setInRelatedInfoAnzeigen(true);
        defaultDisplayFields.add(displayField);
        return defaultDisplayFields;
    }

    public void doValidation() {
        // Nur die Validierung durchführen, aber in updateData() nicht die GUI aufbauen, da ansonsten intern über Session.invokeThreadSafe()
        // synchronisiert wird und dadurch keine parallele Bearbeitung in mehreren Threads möglich wäre
        updateData(null, false, false);
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        updateData(sender, forceUpdateAll, true);
    }

    private void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll, boolean updateValidationGUI) {
        super.updateData(sender, forceUpdateAll);
        EtkDataAssembly currentAssembly = EditModuleHelper.getAssemblyFromConnector(getConnector());
        if ((currentAssembly != null) && (!currentAssembly.getAsId().equals(loadedAssemblyId) || forceUpdateAll)) {
            if (currentAssembly instanceof iPartsDataAssembly) {
                loadedAssemblyId = ((iPartsDataAssembly)currentAssembly).getAsId();
                updateValidationContent(true, forceUpdateAll, updateValidationGUI);
            }
        }
    }

    @Override
    public void updateValidationContent(boolean getSelectedModelsFromSession, boolean forceReloadAssembly, boolean updateValidationGUI) {
        super.updateValidationContent(getSelectedModelsFromSession, forceReloadAssembly, updateValidationGUI);
        if ((validationContent instanceof IPartsRelatedInfoAssemblyListForm) && (loadedAssemblyId != null)) {
            EtkDataAssembly loadedAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), loadedAssemblyId);
            loadedAssembly = loadedAssembly.getLastHiddenSingleSubAssemblyOrThis(null);
            IPartsRelatedInfoAssemblyListForm validationContentForm = (IPartsRelatedInfoAssemblyListForm)validationContent;
            if (forceReloadAssembly) {
                validationContentForm.setCurrentAssembly(null);
            }
            validationContentForm.setCurrentAssembly(loadedAssembly);
            validationContentForm.getConnector().getCurrentPartListEntries(); // Führt effektiv die Validierung durch
            if (updateValidationGUI) {
                updateValidationGUI(false);
            }
        }
    }

    public void updateValidationGUI(boolean forceUpdateAll) {
        if ((validationContent instanceof IPartsRelatedInfoAssemblyListForm) && (loadedAssemblyId != null)) {
            ((IPartsRelatedInfoAssemblyListForm)validationContent).getConnector().updateAllViews(this, forceUpdateAll);
        }
    }

    @Override
    public iPartsProductId getProductIdForModelStorage() {
        if (loadedAssemblyId != null) {
            // validationContent.getCurrentAssembly() ist hier leider noch nicht verfügbar
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), loadedAssemblyId);
            assembly = assembly.getLastHiddenSingleSubAssemblyOrThis(null);
            if (assembly instanceof iPartsDataAssembly) {
                return ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
            }
        }
        return null;
    }

    /**
     * Liefert die für die Validierung geladene Stückliste zurück.
     *
     * @return
     */
    public EtkDataAssembly getCurrentAssembly() {
        if ((validationContent instanceof IPartsRelatedInfoAssemblyListForm) && (loadedAssemblyId != null)) {
            return ((IPartsRelatedInfoAssemblyListForm)validationContent).getConnector().getCurrentAssembly();
        } else {
            return null;
        }
    }

    protected void setSelectedIndex(int index, boolean scrollTo) {
        if (validationContent instanceof IPartsRelatedInfoAssemblyListForm) {
            if (((IPartsRelatedInfoAssemblyListForm)validationContent).getPartListTable() != null) {
                ((IPartsRelatedInfoAssemblyListForm)validationContent).getPartListTable().setSelectedRow(index, scrollTo);
            }
        }
    }

    protected int getSelectedIndex() {
        if (validationContent instanceof IPartsRelatedInfoAssemblyListForm) {
            if (((IPartsRelatedInfoAssemblyListForm)validationContent).getPartListTable() != null) {
                return ((IPartsRelatedInfoAssemblyListForm)validationContent).getPartListTable().getSelectedRowIndex();
            }
        }
        return -1;
    }

    protected String getAdditionalInfoText(EtkDataPartListEntry partListEntry, String model) {
        return "";
    }

    protected void handleCellClick(EtkDataPartListEntry partListEntry, int colIndex) {
        String model = getModelInColumn(colIndex);
        String additionalInfoText = getAdditionalInfoText(partListEntry, model);
        setAdditionalInfoText(additionalInfoText);
    }

    protected boolean isValidationForColorsPossible(iPartsDataAssembly assembly) {
        return !isSimplifiedQualityCheck() && assembly.getDocumentationType().isPKWDocumentationType();
    }

    protected boolean isValidationForPartListPossible(iPartsDataAssembly assembly) {
        return !isSimplifiedQualityCheck() && (assembly.getDocumentationType().isPKWDocumentationType() || assembly.getDocumentationType().isTruckDocumentationType());
    }

    /**
     * Falls die Qualitätsprüfung in einem nicht-modalen Fenster geöffnet wird, merken wir uns hier einen evtl. vorhandenen
     * Zwischendialog (Freigabevorprüfung) der dann ggf. vor einem Sprung in die Stückliste geschlossen werden soll
     *
     * @param relatedForm
     */
    public void setRelatedFormForNonModalShow(AbstractJavaViewerForm relatedForm) {
        this.relatedFormForNonModalShow = relatedForm;
    }

    /**
     * Zuvor gemerkten Dialog schließen
     */
    public void closeRelatedFormForNonModalShow() {
        if (relatedFormForNonModalShow != null) {
            relatedFormForNonModalShow.close();
            // Dialog muss nur einmal geschlossen werden
            relatedFormForNonModalShow = null;
        }
    }

    protected void handleCellClick(TableRowInterface row, int colIndex) {
        if (row != null) {
            Object additionalData = row.getAdditionalData();
            if (additionalData instanceof PartListEntryUserObjectForTableRow) {
                handleCellClick(((PartListEntryUserObjectForTableRow)additionalData).getPartListEntry(), colIndex);
            }
        }
    }


    protected class IPartsRelatedInfoAssemblyListForm extends EditAssemblyListForm.EditAssemblyListView {

        private GuiTable oldPartListTable;
        private EventListener partListTableEventListener;
        private GuiMenuItem goToMenuItem;

        public IPartsRelatedInfoAssemblyListForm(EditModuleFormIConnector assemblyListFormConnectorUnfiltered) {
            super(assemblyListFormConnectorUnfiltered, iPartsEditAssemblyListValidationForm.this);
            setTestHotspotsActive(false);
            hideEmptyPlaceHolder = true;
            setDelayedUpdates(false); // Validierte Stücklisten immer sofort anzeigen
        }

        @Override
        public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
            // Vor der Initialisierung über setCurrentAssembly() kein super.updateData() aufrufen, weil es ansonsten zu diversen
            // NPEs kommen würde
            if (getConnector().getCurrentAssembly().getAsId().isValidId()) {
                super.updateData(sender, forceUpdateAll);
            }
        }

        @Override
        protected boolean useRTable() {
            return false;
        }

        @Override
        public GuiTable getPartListTable() {
            TableInterface partListTable = super.getPartListTable();
            if (partListTable != null) {
                if (!(partListTable.getTableGui() instanceof GuiTable)) {
                    throw new WrongTableClassException(this.getClass().getName());
                }
                return (GuiTable)partListTable.getTableGui();
            }
            return null;
        }

        @Override
        public void loadCurrentAssembly() {
            super.loadCurrentAssembly();

            GuiTable partListTable = getPartListTable();
            if ((partListTable != null) && (partListTable != oldPartListTable)) {
                // Mehrfache Registrierung des EventListeners und Hinzufügen des Menüpunkts vermeiden
                if ((partListTableEventListener != null) && (oldPartListTable != null)) {
                    oldPartListTable.removeEventListener(partListTableEventListener);
                }
                oldPartListTable = partListTable;
                partListTableEventListener = new EventListener(Event.MOUSE_RELEASED_EVENT,
                                                               EventListenerOptions.buildEnumSet(true, true)) {
                    @Override
                    public void fire(Event event) {
                        if (event.getIntParameter(Event.EVENT_PARAMETER_MOUSE_BUTTON) <= 1) { // nur bei Linksklick
                            AbstractGuiControl control = (AbstractGuiControl)event.getParameter(Event.EVENT_PARAMETER_ACTUAL_ACTIVATING_CONTROL);
                            if (control != null) {
                                GuiTableRow row = control.getParentTableRow();
                                if (row != null) {
                                    int colIndex = row.getPositionInRowForControl(control);
                                    handleCellClick(row, colIndex);
                                }
                            }
                        }
                    }
                };
                partListTable.addEventListener(partListTableEventListener);

                // Kontextmenü Eintrag für den Sprung in die Edit Stückliste (nur einmalig zum ContextMenu hinzufügen!)
                if (goToMenuItem == null) {
                    goToMenuItem = new GuiMenuItem();
                    goToMenuItem.setUserObject(IPARTS_MENU_ITEM_OPEN_EDIT_RETAIL_WINDOW);
                    goToMenuItem.setName(IPARTS_MENU_ITEM_OPEN_EDIT_RETAIL_WINDOW);
                    goToMenuItem.setText(IPARTS_MENU_TEXT_OPEN_EDIT_RETAIL_WINDOW);
                    goToMenuItem.setIcon(DefaultImages.module.getImage());
                    EventListener listener = new EventListener(Event.MENU_ITEM_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
                        @Override
                        public void fire(Event event) {
                            List<EtkDataPartListEntry> selectedList = getConnector().getSelectedPartListEntries();
                            if ((selectedList != null) && (selectedList.size() == 1)) {
                                closeRelatedFormForNonModalShow();
                                EtkDataPartListEntry selectedPartListEntry = selectedList.get(0);
                                iPartsGotoHelper.loadAndGotoEditRetail(getConnector(), getParentForm(), selectedPartListEntry.getAsId());
                            }
                        }
                    };
                    goToMenuItem.addEventListener(listener);
                    partListTable.getContextMenu().addChild(goToMenuItem);
                }
            }
        }

        @Override
        protected boolean isEditAllowed() {
            return false;
        }

        @Override
        protected GuiButtonPanel getGuiButtonPanel() {
            if (parentForm instanceof iPartsEditAssemblyListValidationOverlappingEntriesForm) {
                return ((iPartsEditAssemblyListValidationOverlappingEntriesForm)parentForm).getButtonPanel();
            } else {
                return null;
            }
        }

        @Override
        protected void enableButtons() {
            // Zum Stücklisteneintrag springen nur bei singleSelect
            if (goToMenuItem != null) {
                List<EtkDataPartListEntry> selectedList = getConnector().getSelectedPartListEntries();
                boolean isSingleSelect = (selectedList != null) && (selectedList.size() == 1);
                goToMenuItem.setVisible(isSingleSelect);
            }

        }

        @Override
        protected EtkTableHelper createTableHelper() {
            List<EtkDisplayField> iPartsEditDisplayFields = new ArrayList<>();

            // Normale Felder aus der Konfiguration hinzufügen
            String configKey = getDisplayFieldConfigKey(getConnector().getCurrentAssembly().getEbeneName());
            if ((configKey != null) && !configKey.isEmpty()) {
                iPartsEditDisplayFields.addAll(iPartsEditAssemblyListValidationForm.this.getDisplayFields(configKey).getFields());
            } else {
                // Unbekannter Stücklistentyp -> nur Standardfelder hinzufügen durch Verwendung von null als Konfigurationsschlüssel
                iPartsEditDisplayFields.addAll(iPartsEditAssemblyListValidationForm.this.getDisplayFields(null).getFields());
            }
            EtkResponsiveDisplayFields localDisplayFields = new EtkResponsiveDisplayFields(getProject(), iPartsEditDisplayFields,
                                                                                           getEbeneNameForSessionSave(),
                                                                                           getExtraConfigKeyForDisplayFields());

            firstAdditionalFieldIndex = localDisplayFields.getDesktopDisplayList().size() + 1;
            if (additionalDisplayFields != null) {
                localDisplayFields.addDesktopDisplayList(additionalDisplayFields);
            }

            EtkTableHelper tableHelper = new EtkTableHelper(localDisplayFields);
            return tableHelper;
        }

        @Override
        public List<EtkDisplayField> getDisplayFieldsForTableHelper() {
            return super.getDisplayFieldsForTableHelper();
        }

        @Override
        public String getEbeneNameForSessionSave() {
            return TableAndFieldName.make(getCurrentAssembly().getEbeneName(), SimpleDoubleListSelectForm.PARTLIST_SOURCE_MODEL_EVALUATION);
        }

        @Override
        public String getExtraConfigKeyForDisplayFields() {
            return getDisplayFieldConfigKey(getCurrentAssembly().getEbeneName());
        }

        public void setCurrentAssembly(EtkDataAssembly assembly) {
            getConnector().setCurrentAssembly(assembly);
        }

        @Override
        protected void partsListEntryDblClick(EtkDataPartListEntry partListEntry) {
        }
    }
}