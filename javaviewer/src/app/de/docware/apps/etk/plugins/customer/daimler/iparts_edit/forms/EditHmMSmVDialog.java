/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditFormComboboxHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/*/
 * Dialog zur Eingabe eines HmMSm-Knotens (vertikale Ausrichtung)
 */
public class EditHmMSmVDialog extends AbstractJavaViewerForm {

    public static HmMSmId showHmMSmVDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                           HmMSmId startHmMSmId, String labelHM, String labelM, String labelSM) {
        EditHmMSmVDialog hmMSmDlg = new EditHmMSmVDialog(dataConnector, parentForm, null);
        hmMSmDlg.init(new iPartsSeriesId(startHmMSmId.getSeries()));
        hmMSmDlg.setStartHmMSmId(startHmMSmId);
        hmMSmDlg.initLabels(labelHM, labelM, labelSM, true);
        if (hmMSmDlg.showModal() == ModalResult.OK) {
            return hmMSmDlg.getHMMSMId();
        }
        return null;
    }

    // Defaultwerte
    private String startLanguageDefaultValue = Language.DE.getCode();

    // Spezifische Eigenschaften der Komponente
    private String startLanguage = startLanguageDefaultValue;

    private boolean isReadOnly = false;
    private OnVerifyEvent onVerifyEvent = null;
    private EventListener doVerifyEvent;

    // TextFelder für ReadOnly Betrieb
    private GuiTextField textfield_HM = null;
    private GuiTextField textfield_M = null;
    private GuiTextField textfield_SM = null;

    protected EventListeners eventListeners;

    private HmMSm hmMSm = null;
    private HmMSmId startHmMSmId = null;
    private iPartsSeriesId seriesId;
    private Map<String, Map<String, Set<String>>> positivHmMSmMap;
    private Set<String> doneHM;

    private RComboBox<HmMSmNode> rCombobox_HM;
    private RComboBox<HmMSmNode> rCombobox_M;
    private RComboBox<HmMSmNode> rCombobox_SM;

    private boolean loadPositivListWithDialogSeries = true;

    public interface OnVerifyEvent {

        public boolean verify(EditHmMSmVDialog dialog);
    }

    /**
     * Erzeugt eine Instanz von EditHmMSmVDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditHmMSmVDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsSeriesId seriesId) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.seriesId = seriesId;
        this.eventListeners = new EventListeners();

//        this.hmMSm = HmMSm.getInstance(getProject(), seriesId);
        this.doVerifyEvent = new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                GuiButtonOnPanel okButton = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
                if (!onVerifyEvent.verify(EditHmMSmVDialog.this)) {
                    okButton.setEnabled(false);
                } else {
                    okButton.setModalResult(ModalResult.OK);
                    okButton.removeEventListener(this);
                    okButton.doClick();
                }
            }
        };

        postCreateGui();
        mainWindow.pack();
    }


    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        setTitle("!!DIALOG-Konstruktions Verortung festlegen");

        rCombobox_HM = RComboBox.replaceGuiComboBox(mainWindow.combobox_HM);
        rCombobox_HM.requestFocus();
        rCombobox_HM.addEventListener(new EventListener(Event.COMBOBOX_CLOSED_EVENT) {
            @Override
            public void fire(Event event) {
                // Beim Schließen mit Auswahl eines KGs automatisch die TU-ComboBox fokussieren
                boolean comboBoxClosedByControl = event.getBooleanParameter(Event.EVENT_PARAMETER_COMBOBOX_CLOSED_BY_CONTROL);
                if (!comboBoxClosedByControl) { // DropDown-Popup wurde nicht durch das Control selbst geschlossen -> es wurde z.B. TAB gedrückt
                    if (rCombobox_HM.getSelectedIndex() != -1) {
                        rCombobox_M.requestFocus();
                    }
                }
            }
        });

        rCombobox_M = RComboBox.replaceGuiComboBox(mainWindow.combobox_M);
        rCombobox_M.addEventListener(new EventListener(Event.COMBOBOX_CLOSED_EVENT) {
            @Override
            public void fire(Event event) {
                // Beim Schließen mit Auswahl eines TUs automatisch den OK-Button fokussieren
                boolean comboBoxClosedByControl = event.getBooleanParameter(Event.EVENT_PARAMETER_COMBOBOX_CLOSED_BY_CONTROL);
                if (!comboBoxClosedByControl) { // DropDown-Popup wurde nicht durch das Control selbst geschlossen -> es wurde z.B. TAB gedrückt
                    if (rCombobox_M.getSelectedIndex() != -1) {
                        rCombobox_SM.requestFocus();
                    }
                }
            }
        });

        rCombobox_SM = RComboBox.replaceGuiComboBox(mainWindow.combobox_SM);
        rCombobox_SM.addEventListener(new EventListener(Event.COMBOBOX_CLOSED_EVENT) {
            @Override
            public void fire(Event event) {
                // Beim Schließen mit Auswahl eines TUs automatisch den OK-Button fokussieren
                boolean comboBoxClosedByControl = event.getBooleanParameter(Event.EVENT_PARAMETER_COMBOBOX_CLOSED_BY_CONTROL);
                if (!comboBoxClosedByControl) { // DropDown-Popup wurde nicht durch das Control selbst geschlossen -> es wurde z.B. TAB gedrückt
                    if (rCombobox_SM.getSelectedIndex() != -1) {
                        GuiButtonOnPanel buttonOnPanel = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
                        if (buttonOnPanel != null) {
                            buttonOnPanel.requestFocus();
                        }
                    }
                }
            }
        });

        // zusätzlichen Button verstecken
        setButtonHmMSmChangeListener(null);
    }


    private void initLabels(String labelHM, String labelM, String labelSM, boolean setBold) {
        if (!StrUtils.isEmpty(labelHM)) {
            mainWindow.label_HM.setText(labelHM);
        }
        if (!StrUtils.isEmpty(labelHM)) {
            mainWindow.label_M.setText(labelM);
        }
        if (!StrUtils.isEmpty(labelSM)) {
            mainWindow.label_SM.setText(labelSM);
        }
        if (setBold) {
            mainWindow.label_HM.setFontStyle(DWFontStyle.BOLD);
            mainWindow.label_M.setFontStyle(DWFontStyle.BOLD);
            mainWindow.label_SM.setFontStyle(DWFontStyle.BOLD);
        }
    }

    public void init(iPartsSeriesId seriesId) {
        if ((seriesId != null) && seriesId.isValidId()) {
            this.seriesId = seriesId;
            // Aktive ChangeSets berücksichtigen
            startPseudoTransactionForActiveChangeSet(true);
            try {
                hmMSm = HmMSm.getInstance(getProject(), seriesId);
            } finally {
                stopPseudoTransactionForActiveChangeSet();
            }
            Collection<HmMSmNode> hmNodes = hmMSm.getHMNodeList();
            // besetzen der HG-Combobox + Vorbereitung G-/TU-Combobox
            fillComboBoxItems(rCombobox_HM, hmNodes, null);
            EditFormComboboxHelper.clearComboBox(rCombobox_M);
            EditFormComboboxHelper.clearComboBox(rCombobox_SM);
            positivHmMSmMap = new HashMap<>();
            doneHM = new TreeSet<>();
            setStartHmMSmId(startHmMSmId);

//            boolean hasTemplateValues = false;
//            mainWindow.label_Info.setVisible(hasTemplateValues);
        }
        enableButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setOnVerifyEvent(OnVerifyEvent onVerifyEvent) {
        this.onVerifyEvent = onVerifyEvent;
        GuiButtonOnPanel okButton = mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
        if (onVerifyEvent != null) {
            okButton.addEventListener(doVerifyEvent);
            okButton.setModalResult(ModalResult.NONE);
        } else {
            okButton.removeEventListener(doVerifyEvent);
            okButton.setModalResult(ModalResult.OK);
        }
    }

    public void setStartHmMSmId(HmMSmId startHmMSmId) {
        if (startHmMSmId != null) {
            if (isInit()) {
                HmMSmNode node = hmMSm.getHmNode(startHmMSmId.getHm());
                setSelectedIndexByNode(rCombobox_HM, node);
                node = hmMSm.getMNode(startHmMSmId.getHm(), startHmMSmId.getM());
                setSelectedIndexByNode(rCombobox_M, node);
                node = hmMSm.getSmNode(startHmMSmId.getHm(), startHmMSmId.getM(), startHmMSmId.getSm());
                setSelectedIndexByNode(rCombobox_SM, node);
                this.startHmMSmId = null;
            } else {
                this.startHmMSmId = startHmMSmId;
            }
        }
    }

    /**
     * Überprüfungsfunktion, ob EinPasPanel initialisiert ist
     *
     * @return
     */
    public boolean isInit() {
        return (hmMSm != null);
    }

    public HmMSmId getHMMSMId() {
        return new HmMSmId(seriesId.getSeriesNumber(), getSelectedHMNumber(), getSelectedMNumber(), getSelectedSMNumber());
    }

    /**
     * Test, ob alle EinPas Felder besetzt sind
     *
     * @return
     */
    public boolean isValid() {
        if (isInit()) {
            return (getSelectedHMNumber() != null) && (getSelectedMNumber() != null) && (getSelectedSMNumber() != null);
        }
        return false;
    }

    /**
     * zusätzliche Überschrift auf dem EinPAS-Panel
     *
     * @param title
     */
    public void setPanelTitle(String title) {
        mainWindow.panelMain.setTitle(title);
    }

    public void setPanelTitleFontStyle(DWFontStyle style) {
        // der title-Fontstyle kann i.A. nicht geändertwerden => ändere Fontstyle der Labels
        mainWindow.label_HM.setFontStyle(style);
        mainWindow.label_M.setFontStyle(style);
        mainWindow.label_SM.setFontStyle(style);
    }

    public String getStartLanguage() {
        return startLanguage;
    }

    public void setStartLanguage(String startLanguage) {
        this.startLanguage = startLanguage;
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(iPartsSeriesId seriesId) {
        if (this.seriesId != null) {
            if (seriesId != null) {
                if (!this.seriesId.equals(seriesId)) {
                    this.seriesId = seriesId;
                    init(seriesId);
                }
            } else {
                this.seriesId = seriesId;
                hmMSm = null;
            }
        } else {
            this.seriesId = seriesId;
            init(seriesId);
        }
    }

    /**
     * ReadOnly setzen
     * Die Comboboxen werden durch TextFields (editable=false + spezielle Background Color) ersetzt
     *
     * @param value
     */
    public void setReadOnly(boolean value) {
        if (value != isReadOnly) {
            if (value) {
                boolean setNames = (textfield_HM == null);
                textfield_HM = EditFormComboboxHelper.replaceComboBoxByTextField(rCombobox_HM, textfield_HM, mainWindow.panelMain);
                textfield_M = EditFormComboboxHelper.replaceComboBoxByTextField(rCombobox_M, textfield_M, mainWindow.panelMain);
                textfield_SM = EditFormComboboxHelper.replaceComboBoxByTextField(rCombobox_SM, textfield_SM, mainWindow.panelMain);
                if (setNames) {
                    textfield_HM.setName("textfield_HM");
                    textfield_M.setName("textfield_M");
                    textfield_SM.setName("textfield_SM");
                }
            } else {
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfield_HM, rCombobox_HM, mainWindow.panelMain);
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfield_M, rCombobox_M, mainWindow.panelMain);
                EditFormComboboxHelper.replaceTextFieldByComboBox(textfield_SM, rCombobox_SM, mainWindow.panelMain);
            }
            isReadOnly = value;
        }
    }

    /**
     * Eventlistener für den zusätzlichen Button setzen (damit wird der Button automatisch sichtbar)
     * oder Löschen (damit wird der Button automatisch unsichtbar)
     *
     * @param eventListener
     */
    public void setButtonHmMSmChangeListener(EventListener eventListener) {
        if (eventListener != null) {
            mainWindow.button_HmMSm_Change.addEventListener(eventListener);
            mainWindow.button_HmMSm_Change.setVisible(true);
        } else {
            mainWindow.button_HmMSm_Change.removeEventListeners(Event.ACTION_PERFORMED_EVENT);
            mainWindow.button_HmMSm_Change.setVisible(false);
        }
    }


    /**
     * liefert die selektierte HM-Nummer
     *
     * @return
     */
    private String getSelectedHMNumber() {
        return getSelectedNumber(rCombobox_HM);
    }

    /**
     * liefert die selektierte M-Nummer
     *
     * @return
     */
    private String getSelectedMNumber() {
        return getSelectedNumber(rCombobox_M);
    }

    /**
     * liefert die selektierte SM-Nummer
     *
     * @return
     */
    private String getSelectedSMNumber() {
        return getSelectedNumber(rCombobox_SM);
    }

    /**
     * liefert die Selektierte EinPAS Nummer einer Combobox
     *
     * @param combobox
     * @return
     */
    private String getSelectedNumber(RComboBox<HmMSmNode> combobox) {
        HmMSmNode selectedNode = getSelectedUserObject(combobox);
        if (selectedNode != null) {
            return selectedNode.getNumber();
        }
        return null;
    }

    /**
     * liefert das selektierte Userobject einer Combobox
     *
     * @param combobox
     * @return
     */
    private HmMSmNode getSelectedUserObject(RComboBox<HmMSmNode> combobox) {
        return combobox.getSelectedUserObject();
    }

    private void setSelectedIndexByNode(RComboBox<HmMSmNode> combobox, HmMSmNode node) {
        if (node != null) {
            int index = -1;
            for (int lfdNr = 0; lfdNr < combobox.getItemCount(); lfdNr++) {
                HmMSmNode actNode = combobox.getUserObject(lfdNr);
                if (actNode.getNumber().equals(node.getNumber())) {
                    index = lfdNr;
                    break;
                }
            }
            if (index != -1) {
                combobox.setSelectedIndex(index);
            }
        }
    }

//    private void clearComboBox(RComboBox<HmMSmNode> combobox) {
//        combobox.removeAllItems();
//        combobox.setEnabled(false);
//    }

    /**
     * Füllen der Items einer Combobox
     *
     * @param combobox
     * @param nodes
     */
    private void fillComboBoxItems(RComboBox<HmMSmNode> combobox, Collection<HmMSmNode> nodes, Set<String> positivValues) {
        combobox.switchOffEventListeners();
        combobox.removeAllItems();
        if (nodes != null) {
            Iterator<HmMSmNode> iter = nodes.iterator();
            while (iter.hasNext()) {
                HmMSmNode node = iter.next();
                combobox.addItem(node, buildHmMSmComboText(node, startLanguage, positivValues));
            }
            combobox.setEnabled(true);
        } else {
            combobox.setEnabled(false);
        }
        combobox.setSelectedIndex(-1);
        combobox.switchOnEventListeners();
    }

    /**
     * Callback für HG-Combobox
     * (=> besetzen der G-Combobox)
     *
     * @param event
     */
    private void onChangeHM(Event event) {
        String numberHMNode = getSelectedHMNumber();
        if (numberHMNode != null) {
            if (!doneHM.contains(numberHMNode)) {
                buildPositivList(getProject(), numberHMNode, null, null);
                if (positivHmMSmMap.get(numberHMNode) == null) {
                    rCombobox_HM.switchOffEventListeners();
                    HmMSmNode selectedNode = getSelectedUserObject(rCombobox_HM);
                    String text = buildHmMSmComboText(selectedNode, startLanguage, new TreeSet<>());
                    int index = rCombobox_HM.getSelectedIndex();
                    rCombobox_HM.addItem(selectedNode, text, null, index, false);
                    rCombobox_HM.switchOnEventListeners();
                }
            }
            Collection<HmMSmNode> mNodes = hmMSm.getMNodeList(numberHMNode);
            Map<String, Set<String>> hmMap = positivHmMSmMap.get(numberHMNode);
            if (hmMap != null) {
                fillComboBoxItems(rCombobox_M, mNodes, hmMap.keySet());
            } else {
                EditFormComboboxHelper.clearComboBox(rCombobox_M);
            }
            EditFormComboboxHelper.clearComboBox(rCombobox_SM);
            fireOnChangeEvent(event);
        }
    }

    /**
     * Callback für G-Combobox
     * (=> besetzen der TU-Combobox)
     *
     * @param event
     */
    private void onChangeM(Event event) {
        String numberHMNode = getSelectedHMNumber();
        String numberMNode = getSelectedMNumber();
        if ((numberHMNode != null) && (numberMNode != null)) {
            Collection<HmMSmNode> smNodes = hmMSm.getSMNodeList(numberHMNode, numberMNode);
            Set<String> mPositiveList = null;
            Map<String, Set<String>> hmMap = positivHmMSmMap.get(numberHMNode);
            if (hmMap != null) {
                mPositiveList = hmMap.get(numberMNode);
                if (mPositiveList == null) {
                    mPositiveList = new TreeSet<>();
                }
            } else {
                mPositiveList = new TreeSet<>();
            }
            fillComboBoxItems(rCombobox_SM, smNodes, mPositiveList);
            fireOnChangeEvent(event);
        }
    }

    /**
     * Callback für TU-Combobox
     * (i.A. not used)
     *
     * @param event
     */
    private void onChangeSM(Event event) {
        fireOnChangeEvent(event);
    }

    private void fireOnChangeEvent(Event event) {
        for (EventListener listener : eventListeners.getListeners(Event.ON_CHANGE_EVENT)) {
            listener.fire(event);
        }
        enableButtons();
    }

    private void enableButtons() {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isValid());
    }

    private void buildPositivList(EtkProject project, String hm, String m, String sm) {
        final Set<HmMSmId> result = new TreeSet<>();
        if (!loadPositivListWithDialogSeries) {
            iPartsDataDialogDataList list = new iPartsDataDialogDataList();
            list.clear(DBActionOrigin.FROM_DB);
            list.setSearchWithoutActiveChangeSets(true);

            EtkDisplayFields selectFields = new EtkDisplayFields();
            selectFields.addFeld(SimpleMasterDataSearchResultGrid.createSearchField(project, iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_HM, false, false));
            selectFields.addFeld(SimpleMasterDataSearchResultGrid.createSearchField(project, iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_M, false, false));
            selectFields.addFeld(SimpleMasterDataSearchResultGrid.createSearchField(project, iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_SM, false, false));
            String[] whereTableAndFields = new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_SERIES_NO) };
            String[] whereValues = new String[]{ seriesId.getSeriesNumber() };
            if (StrUtils.isValid(hm)) {
                whereTableAndFields = mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_HM) });
                whereValues = mergeArrays(whereValues, new String[]{ hm });
            }
            if (StrUtils.isValid(m)) {
                whereTableAndFields = mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_M) });
                whereValues = mergeArrays(whereValues, new String[]{ m });
            }
            if (StrUtils.isValid(sm)) {
                whereTableAndFields = mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_SM) });
                whereValues = mergeArrays(whereValues, new String[]{ sm });
            }
            final VarParam<Integer> counter = new VarParam<>(0);
            list.searchSortAndFillWithJoin(project, null, selectFields, whereTableAndFields, whereValues,
                                           false, null, false,
                                           new EtkDataObjectList.FoundAttributesCallback() {
                                               @Override
                                               public boolean foundAttributes(DBDataObjectAttributes attributes) {
                                                   HmMSmId hmMSmId = new HmMSmId(seriesId.getSeriesNumber(),
                                                                                 attributes.getFieldValue(iPartsConst.FIELD_DD_HM),
                                                                                 attributes.getFieldValue(iPartsConst.FIELD_DD_M),
                                                                                 attributes.getFieldValue(iPartsConst.FIELD_DD_SM));
                                                   result.add(hmMSmId);
                                                   counter.setValue(counter.getValue() + 1);
                                                   return false;
                                               }
                                           });
        } else {
            result.addAll(iPartsDialogSeries.getInstance(project, seriesId).getSubModuleIds(project));
        }
        if (!result.isEmpty()) {
            for (HmMSmId hmMSmId : result) {
                String currentHM = hmMSmId.getHm();
                if (StrUtils.isValid(currentHM)) {
                    Map<String, Set<String>> hmMap = positivHmMSmMap.get(currentHM);
                    if (hmMap == null) {
                        hmMap = new HashMap<>();
                        positivHmMSmMap.put(currentHM, hmMap);
                        doneHM.add(currentHM);
                    }
                    String currentM = hmMSmId.getM();
                    if (StrUtils.isValid(currentM)) {
                        Set<String> smList = hmMap.get(currentM);
                        if (smList == null) {
                            smList = new TreeSet<>();
                            hmMap.put(currentM, smList);
                        }
                        if (hmMSmId.isSmNode()) {
                            smList.add(hmMSmId.getSm());
                        }
                    }
                }
            }
        }
    }

    private String[] mergeArrays(String[] array1, String[] array2) {
        if (array1 != null) {
            return StrUtils.mergeArrays(array1, array2);
        }
        return array2;
    }


    /**
     * hier wird der Text für die Comboboxen erzeugt
     *
     * @param node
     * @param language
     * @return
     */
    static public String buildHmMSmComboText(HmMSmNode node, String language, Set<String> positivValues) {
        if (node != null) {
            String suffix = "";
            if (positivValues != null) {
                if (!positivValues.contains(node.getNumber())) {
                    suffix = " *";
                }
            }
            return node.getNumber() + " - " + node.getTitle().getText(language) + suffix;
        }
        return "";
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
        private de.docware.framework.modules.gui.controls.GuiLabel label_HM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> combobox_HM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton button_HmMSm_Change;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_M;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> combobox_M;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_SM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> combobox_SM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_Info;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

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
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            label_HM = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_HM.setName("label_HM");
            label_HM.__internal_setGenerationDpi(96);
            label_HM.registerTranslationHandler(translationHandler);
            label_HM.setScaleForResolution(true);
            label_HM.setMinimumWidth(10);
            label_HM.setMinimumHeight(10);
            label_HM.setText("!!Hauptmodul");
            label_HM.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_HMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 8, 4, 4);
            label_HM.setConstraints(label_HMConstraints);
            panelMain.addChild(label_HM);
            combobox_HM = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            combobox_HM.setName("combobox_HM");
            combobox_HM.__internal_setGenerationDpi(96);
            combobox_HM.registerTranslationHandler(translationHandler);
            combobox_HM.setScaleForResolution(true);
            combobox_HM.setMinimumWidth(10);
            combobox_HM.setMinimumHeight(10);
            combobox_HM.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeHM(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_HMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 8, 4, 4, 4);
            combobox_HM.setConstraints(combobox_HMConstraints);
            panelMain.addChild(combobox_HM);
            button_HmMSm_Change = new de.docware.framework.modules.gui.controls.GuiButton();
            button_HmMSm_Change.setName("button_HmMSm_Change");
            button_HmMSm_Change.__internal_setGenerationDpi(96);
            button_HmMSm_Change.registerTranslationHandler(translationHandler);
            button_HmMSm_Change.setScaleForResolution(true);
            button_HmMSm_Change.setMinimumWidth(5);
            button_HmMSm_Change.setMinimumHeight(10);
            button_HmMSm_Change.setMaximumWidth(30);
            button_HmMSm_Change.setMaximumHeight(20);
            button_HmMSm_Change.setMnemonicEnabled(true);
            button_HmMSm_Change.setText("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag button_HmMSm_ChangeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 0, 4, 6);
            button_HmMSm_Change.setConstraints(button_HmMSm_ChangeConstraints);
            panelMain.addChild(button_HmMSm_Change);
            label_M = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_M.setName("label_M");
            label_M.__internal_setGenerationDpi(96);
            label_M.registerTranslationHandler(translationHandler);
            label_M.setScaleForResolution(true);
            label_M.setMinimumWidth(10);
            label_M.setMinimumHeight(10);
            label_M.setText("!!Modul");
            label_M.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_MConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            label_M.setConstraints(label_MConstraints);
            panelMain.addChild(label_M);
            combobox_M = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            combobox_M.setName("combobox_M");
            combobox_M.__internal_setGenerationDpi(96);
            combobox_M.registerTranslationHandler(translationHandler);
            combobox_M.setScaleForResolution(true);
            combobox_M.setMinimumWidth(10);
            combobox_M.setMinimumHeight(10);
            combobox_M.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeM(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_MConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 4);
            combobox_M.setConstraints(combobox_MConstraints);
            panelMain.addChild(combobox_M);
            label_SM = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_SM.setName("label_SM");
            label_SM.__internal_setGenerationDpi(96);
            label_SM.registerTranslationHandler(translationHandler);
            label_SM.setScaleForResolution(true);
            label_SM.setMinimumWidth(10);
            label_SM.setMinimumHeight(10);
            label_SM.setText("!!Submodul");
            label_SM.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_SMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            label_SM.setConstraints(label_SMConstraints);
            panelMain.addChild(label_SM);
            combobox_SM = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            combobox_SM.setName("combobox_SM");
            combobox_SM.__internal_setGenerationDpi(96);
            combobox_SM.registerTranslationHandler(translationHandler);
            combobox_SM.setScaleForResolution(true);
            combobox_SM.setMinimumWidth(10);
            combobox_SM.setMinimumHeight(10);
            combobox_SM.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeSM(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag combobox_SMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 4);
            combobox_SM.setConstraints(combobox_SMConstraints);
            panelMain.addChild(combobox_SM);
            label_0 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_0.setName("label_0");
            label_0.__internal_setGenerationDpi(96);
            label_0.registerTranslationHandler(translationHandler);
            label_0.setScaleForResolution(true);
            label_0.setMinimumWidth(10);
            label_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 100.0, "c", "n", 0, 0, 0, 0);
            label_0.setConstraints(label_0Constraints);
            panelMain.addChild(label_0);
            label_Info = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_Info.setName("label_Info");
            label_Info.__internal_setGenerationDpi(96);
            label_Info.registerTranslationHandler(translationHandler);
            label_Info.setScaleForResolution(true);
            label_Info.setMinimumWidth(10);
            label_Info.setMinimumHeight(10);
            label_Info.setText("!!* Standard-Benennung (bisher keine Verwendung in Konstruktions-Baureihe)");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_InfoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 0.0, 0.0, "w", "n", 4, 4, 4, 4);
            label_Info.setConstraints(label_InfoConstraints);
            panelMain.addChild(label_Info);
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