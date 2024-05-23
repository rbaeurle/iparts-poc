/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.iPartsDataCardRetrievalHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.IdentToDataCardHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Abgewandelte EditSelectDataObjectsForm für die Eingabe von FINs
 * Farbliche Hinterlegung im Grid, falls eine FIN nicht zum Produkt passt oder die Datenkarte nicht bestimmt werden kann
 * Im Grid werden die FINs via iPartsDataProduct/iPartsProductId verwaltet (spart die Dummy-Anlage von iPartsDataFIN)
 */
public class EditSelectFINForm extends EditSelectDataObjectsForm {

    public static Collection<String> showSelectionFins(AbstractJavaViewerForm parentForm, iPartsProductId productId,
                                                       Set<String> validModelNumbers, Collection<String> currentFinOrVinList,
                                                       boolean isEditable) {
        EditSelectFINForm dlg = new EditSelectFINForm(parentForm.getConnector(), parentForm, currentFinOrVinList, productId,
                                                      validModelNumbers, true);
        dlg.setEditAllowed(isEditable);
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getCompleteSelectedFinList();
        }
        return null;
    }

    private GuiButton buttonAddValue;
    private iPartsGuiFinVinTextField textFieldFinVin;
    private Set<String> validModelNumbers;
    private Collection<String> finList;

    /**
     * Erzeugt eine Instanz von EditSelectDataObjectsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public EditSelectFINForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                             Collection<String> currentFinOrVinList, iPartsProductId productId, Set<String> validModelNumbers,
                             boolean checkAtOK) {
        super(dataConnector, parentForm, iPartsConst.TABLE_DA_PRODUCT, "", "");
        this.validModelNumbers = validModelNumbers;
        this.finList = currentFinOrVinList;
        showOnlySelectedElements();
        addInputArea();
        setMoveEntriesVisible(true);
        setTitle(TranslationHandler.translate("!!Bearbeiten von FINs zum Produkt \"%1\"", productId.getProductNumber()));
        setSelectedEntriesTitle("!!Ausgewählte Elemente");
        setNoDoubles(true);

        addCurrentFinOrVinList(currentFinOrVinList);
        doEnableOKButton();
        if (checkAtOK) {
            getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                public void fire(Event event) {
                    if (checkAllAtOK()) {
                        getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).removeEventListener(this);
                        getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).doClick();
                    }
                }
            });
        }
    }

    /**
     * Überprüfung der bereits vorhandenen FINs. Relevant, falls am Produkt die Baumuster geändert wurden
     *
     * @return
     */
    private boolean checkAllAtOK() {
        boolean result = true;
        List<String> checkFinList = getCompleteSelectedFinList();
        if (finList != null) {
            Iterator<String> iterator = checkFinList.iterator();
            while (iterator.hasNext()) {
                String fin = iterator.next();
                // die neu hinzugekommenen FINs brauchen nicht mehr überprüft werden
                if (!finList.contains(fin)) {
                    iterator.remove();
                }
            }
        }
        if (!checkFinList.isEmpty()) {
            // jetzt alle Fins entfernen, deren Datenkarte im Cache ist
            Iterator<String> iterator = checkFinList.iterator();
            while (iterator.hasNext()) {
                String fin = iterator.next();
                // Ist die Datenkarte im Cache, dann braucht sie nicht mehr überprüft werden
                if (iPartsDataCardRetrievalHelper.getCachedVehicleDataCard(getProject(), new FinId(fin)) != null) {
                    iterator.remove();
                }
            }
        }
        if (!checkFinList.isEmpty()) {
            result = checkTableForValidFins(true, false, checkFinList);
        }

        return result;
    }

    /**
     * Die gesammelten FINs als Stringliste liefern
     *
     * @return
     */
    protected List<String> getCompleteSelectedFinList() {
        List<String> selectedFins = new DwList<>();
        for (IdWithType id : getCompleteSelectedIdList()) {
            if (id.getType().equals(iPartsProductId.TYPE)) {
                selectedFins.add(id.getValue(1));
            }
        }
        return selectedFins;
    }

    /**
     * Eine Liste von FIN/VINs zum Grid hinzufügen
     *
     * @param currentFinOrVinList
     */
    private void addCurrentFinOrVinList(Collection<String> currentFinOrVinList) {
        EtkProject project = getProject();
        List<EtkDataObject> selectedList = new DwList<>();
        for (String finOrVin : currentFinOrVinList) {
            iPartsProductId id = new iPartsProductId(finOrVin);
            iPartsDataProduct product = new iPartsDataProduct(project, id);
            selectedList.add(product);
        }
        fillSelectedEntries(selectedList);
        checkTableForValidFins(false, false, null);
    }

    /**
     * Alle FINs (oder die FINs aus checkOnlyFinsList) im Grid überprüfen
     * Überprüft wird, ob das BM der FIN/VIN im Produkt definiert ist und (wahlweise) ob es zur FIN/VIN eine Datenkarte existiert
     *
     * @param withDataCardCheck
     * @param withWarning
     * @param checkOnlyFinsList
     * @return
     */
    private boolean checkTableForValidFins(boolean withDataCardCheck, boolean withWarning, List<String> checkOnlyFinsList) {
        boolean result = true;
        GuiTable table = selectedEntriesGrid.getTable();
        for (GuiTableRow row : table.getRows()) {
            if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                EtkDataObject dataObject = ((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(TABLE_DA_PRODUCT);
                if (dataObject != null) {
                    if (dataObject.getAsId().getType().equals(iPartsProductId.TYPE)) {
                        String fin = dataObject.getAsId().getValue(1);
                        boolean doCheck = true;
                        if (Utils.isValid(checkOnlyFinsList)) {
                            doCheck = checkOnlyFinsList.contains(fin);
                        }
                        if (doCheck) {
                            StringBuilder warnings = new StringBuilder();
                            if (!checkPossibleFinOrVin(fin, withDataCardCheck, withWarning, warnings)) {
                                row.setBackgroundColor(Colors.clDesignErrorBackground.getColor());
                                String msg = warnings.toString();
                                if (!msg.isEmpty()) {
                                    row.setTooltip(new GuiLabel(msg));
                                }
                                result = false;
                            }
                        }
                    }
                }
            }
        }

        if (!result) {
            // Workaround für das Setzen vom Tooltip, was bei GuiTable eigentlich nicht vorgesehen ist, sobald die Zeilen
            // schon angezeigt werden
            AbstractGuiControl tableParent = table.getParent();
            table.removeFromParent();
            tableParent.addChild(table);
        }

        return result;
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(500, 400);
    }

    @Override
    protected void createToolbarButtons() {
        ToolbarButtonMenuHelper.ToolbarMenuHolder holder;

        holder = toolbarHelperSelected.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.EDIT_DELETE, "!!Löschen",
                                                                     getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doRemove(event);
                }
            });
        getContextMenuSelectedEntries().addChild(holder.menuItem);
        super.createToolbarButtons();

        setToolbarButtonVisible(iPartsToolbarButtonAlias.IMG_RIGHT, false);
        setToolbarButtonVisible(iPartsToolbarButtonAlias.IMG_LEFT, false);
        setToolbarButtonVisible(iPartsToolbarButtonAlias.IMG_RIGHT_ALL, false);
        setToolbarButtonVisible(iPartsToolbarButtonAlias.IMG_LEFT_ALL, false);
    }

    @Override
    protected EtkDisplayFields buildDefaultDisplayFields(boolean forSelectedEntries) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(searchTable);
        EtkDisplayField displayField = createDisplayField(tableDef, iPartsConst.FIELD_DP_PRODUCT_NO);
        displayField.setText(new EtkMultiSprache("!!Hinterlegte FINs", new String[]{ TranslationHandler.getUiLanguage() }));
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    @Override
    protected void doEnableButtons() {
        super.doEnableButtons();
        int selectedEntriesSelectionCount = selectedEntriesGrid.getTable().getSelectedRows().size();
        toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.EDIT_DELETE, getContextMenuSelectedEntries(),
                                                         selectedEntriesSelectionCount > 0);
        if (isEditAllowed) {
            int itemCount = selectedEntriesGrid.getTable().getRowCount();
            if ((itemCount > 1) && (selectedEntriesSelectionCount == 1)) {
                int selectedIndex = selectedEntriesGrid.getTable().getSelectedRowIndex();
                toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_UP, getContextMenuSelectedEntries(),
                                                                 selectedIndex > 0);
                toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_DOWN, getContextMenuSelectedEntries(),
                                                                 (selectedIndex + 1) < itemCount);
            } else {
                toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_UP, getContextMenuSelectedEntries(), false);
                toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_DOWN, getContextMenuSelectedEntries(), false);
            }
        }
    }

    /**
     * Für en/disable des OK-Buttons
     *
     * @return
     */
    @Override
    protected boolean areEntriesChanged() {
        // OK-Button soll immer enabled sein, damit die FIN-Prüfungen bzgl. VIS bei Klick auf OK auch bei unveränderter FIN-Liste funktionieren
        return true;
    }

    /**
     * Fügt den Bereich für die manuelle Eingabe hinzu
     */
    private void addInputArea() {
        // Panel für das FIN-Suchfeld erzeugen
        GuiPanel panelInput = new GuiPanel();
        panelInput.setName("panelInput");
        panelInput.__internal_setGenerationDpi(96);
        panelInput.registerTranslationHandler(getUITranslationHandler());
        panelInput.setScaleForResolution(true);
        panelInput.setMinimumWidth(10);
        panelInput.setMinimumHeight(10);
        LayoutGridBag panelInputLayout = new LayoutGridBag();
        panelInput.setLayout(panelInputLayout);

        GuiLabel labelAddFIN = new GuiLabel();
        labelAddFIN.setName("labelAddFIN");
        labelAddFIN.__internal_setGenerationDpi(96);
        labelAddFIN.registerTranslationHandler(getUITranslationHandler());
        labelAddFIN.setScaleForResolution(true);
        labelAddFIN.setMinimumWidth(10);
        labelAddFIN.setMinimumHeight(10);
        labelAddFIN.setText("!!Manuelle Eingabe:");
        ConstraintsGridBag gridbagConstraints = new ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "e", "n", 8, 8, 8, 4);
        labelAddFIN.setConstraints(gridbagConstraints);
        panelInput.addChild(labelAddFIN);

        buttonAddValue = new GuiButton();
        buttonAddValue.setName("buttonAddValue");
        buttonAddValue.__internal_setGenerationDpi(96);
        buttonAddValue.registerTranslationHandler(getUITranslationHandler());
        buttonAddValue.setScaleForResolution(true);
        buttonAddValue.setMinimumWidth(100);
        buttonAddValue.setMinimumHeight(10);
        buttonAddValue.setMnemonicEnabled(true);
        buttonAddValue.setText("!!Hinzufügen");
        buttonAddValue.setModalResult(ModalResult.NONE);
        buttonAddValue.setEnabled(false);
        buttonAddValue.setDefaultButton(true);
        buttonAddValue.addEventListener(new de.docware.framework.modules.gui.event.EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                addAdditionalFIN(event);
            }
        });
        gridbagConstraints = new ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "w", "n", 8, 4, 8, 8);
        buttonAddValue.setConstraints(gridbagConstraints);
        panelInput.addChild(buttonAddValue);

        textFieldFinVin = new iPartsGuiFinVinTextField(true);
        textFieldFinVin.setName("textFieldFinVin");
        textFieldFinVin.__internal_setGenerationDpi(96);
        textFieldFinVin.registerTranslationHandler(getUITranslationHandler());
        textFieldFinVin.setScaleForResolution(true);
        textFieldFinVin.setMinimumWidth(200);
        textFieldFinVin.setMinimumHeight(10);
        textFieldFinVin.initForVinFallback(getProject());
        textFieldFinVin.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                checkFinVinData();
            }
        });
        gridbagConstraints = new ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "w", "n", 8, 4, 8, 4);
        textFieldFinVin.setConstraints(gridbagConstraints);
        panelInput.addChild(textFieldFinVin);

        ConstraintsBorder panelInputConstraints = new ConstraintsBorder();
        panelInputConstraints.setPosition(ConstraintsBorder.POSITION_SOUTH);
        panelInput.setConstraints(panelInputConstraints);
        getPanelForFurtherElements().addChild(panelInput);
    }

    private void addAdditionalFIN(Event event) {
        // überprüfen, ob BM von FIN in Produkt + loadDataCard() funktioniert
        iPartsProductId id = null;
        if (textFieldFinVin.isFinValid()) {
            id = new iPartsProductId(textFieldFinVin.getFinId().getFIN());
        } else if (textFieldFinVin.isVinValid()) {
            id = new iPartsProductId(textFieldFinVin.getVinId().getVIN());
        }
        if (id != null) {
            // überprüfen, ob BM von FIN in Produkt + loadDataCard() funktioniert
            if (checkPossibleFinOrVin(id.getProductNumber())) {
                iPartsDataProduct product = new iPartsDataProduct(getProject(), id);
                List<EtkDataObject> selectedList = new DwList<>(1);
                selectedList.add(product);
                doAddEntries(selectedList);
                textFieldFinVin.setFin("");
            }
        }
    }

    private boolean checkPossibleFinOrVin(String identNo) {
        return checkPossibleFinOrVin(identNo, true, true, null);
    }

    private boolean checkPossibleFinOrVin(String identNo, boolean withDataCardCheck, boolean withWarning, StringBuilder warnings) {
        if (StrUtils.isValid(identNo)) {
            if (textFieldFinVin.checkFinAgainstModelNos(identNo, validModelNumbers)) {
                if (withDataCardCheck) {
                    // überprüfen ob loadDataCard() funktioniert
                    VehicleDataCard dataCard = IdentToDataCardHelper.loadVehicleDataCard(identNo, getProject());
                    if (dataCard != null) {
                        if (dataCard.isDataCardLoaded()) {
                            return true;
                        } else {
                            addWarningMsg(withWarning, warnings, "!!Datenkarte für FIN/VIN \"%1\" ist nicht vorhanden.", identNo);
                        }
                    }
                } else {
                    return true;
                }
            } else {
                // Fehlermeldung
                addWarningMsg(withWarning, warnings, "!!Baumuster der FIN/VIN \"%1\" ist nicht im Produkt enthalten.", identNo);
            }
        }
        return false;
    }

    private void addWarningMsg(boolean withWarning, StringBuilder warnings, String key, String... placeHolderTexts) {
        String msg = TranslationHandler.translate(key, placeHolderTexts);
        if (withWarning) {
            // Fehlermeldung
            MessageDialog.showWarning(msg);
        } else if (warnings != null) {
            if (warnings.length() > 0) {
                warnings.append("\n");
            }
            warnings.append(msg);
        }
    }

    private void checkFinVinData() {
        buttonAddValue.setEnabled(isEditAllowed() && textFieldFinVin.isFinOrVinValid());
    }
}
