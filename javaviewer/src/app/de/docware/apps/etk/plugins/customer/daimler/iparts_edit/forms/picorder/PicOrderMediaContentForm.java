/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.drawing.EtkImageSettings;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.mechanic.imageview.model.UnsupportedImageViewerItem;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.drawing.*;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PoolEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataImageList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiPanelWithCheckbox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLAcceptMediaContainer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLContractor;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLCorrectMediaOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLReason;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaVariant;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditASPLMContractorForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SelectSearchGridImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiLengthLimitedTextArea;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.xml.iPartsEditXMLResponseSimulator;
import de.docware.framework.modules.config.db.datatypes.DatatypeUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.viewer.*;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.http.server.HttpServerException;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.j2ee.EC;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dialog zum Anzeigen von Bildern zu einem Bildauftrag
 */
public class PicOrderMediaContentForm extends AbstractJavaViewerForm implements iPartsConst {

    enum PictureDBActions {
        PICTURE_SAVE, PICTURE_DELETE, NOTHING
    }

    private static final String COL_CHECKBOX_TABLENAME = TABLE_DA_PICORDER_PICTURES;
    private static final String COL_CHECKBOX_FIELDNAME = FIELD_DA_POP_USED;

    public static boolean showASPLMMediaContents(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 AssemblyId assemblyId, iPartsDataPicOrder dataPicOrder) {
        int maxResults = J2EEHandler.isJ2EE() ? MAX_IMAGE_SEARCH_RESULTS_SIZE : -1;
        PicOrderMediaContentForm dlg = new PicOrderMediaContentForm(dataConnector, parentForm,
                                                                    assemblyId, dataPicOrder, maxResults, false);
        dlg.setEditAllowed(dataConnector.isAuthorOrderValid());
        dlg.setTitle(TranslationHandler.translate("!!Anzeige der AS-PLM Bilder (%1 %2)",
                                                  dataPicOrder.getOrderIdExtern(), dataPicOrder.getOrderRevisionExtern()));
        ModalResult result = dlg.showModal();
        if (dlg.isPictureSelectedForCorrection()) {
            return false;
        }
        return result == ModalResult.OK;

    }

    public static boolean showASPLMMediaContentsForChange(EditModuleFormIConnector dataConnector,
                                                          PicOrderMainForm parentForm,
                                                          AssemblyId assemblyId, iPartsDataPicOrder dataPicOrder) {

        int maxResults = J2EEHandler.isJ2EE() ? MAX_IMAGE_SEARCH_RESULTS_SIZE : -1;
        PicOrderMediaContentForm dlg = new PicOrderMediaContentForm(dataConnector, parentForm,
                                                                    assemblyId, dataPicOrder, maxResults, true);
        dlg.setEditAllowed(dataConnector.isAuthorOrderValid());
        dlg.setTitle("!!Auswahl der AS-PLM Bilder für Änderungsauftrag \"" + dataPicOrder.getOrderIdExtern() + "\"");
        if (dlg.showModal() == ModalResult.OK) {
            dlg.saveChangeInfo();
            dataPicOrder.addPictures(dlg.getSelectedPictures());
            return true;
        }
        return false;
    }

    private PictureDataObjectGrid objectGrid;
    private EtkImageSettings imageSettings;
    private GuiViewerImageInterface imgViewer;
    private GuiPanel panel;
    private AbstractGuiControl imageViewerCenterControl;
    private final int maxResults;
    private final iPartsDataPicOrder dataPicOrder;
    private GuiButton selectAllButton;
    private GuiButton deSelectAllButton;
    private EditASPLMContractorForm contractorForm;
    private List<iPartsDataPicOrderPicture> currSelectedPictures;
    private List<iPartsDataPicOrderPicture> currNotSelectedPictures;
    private List<List<EtkDataObject>> currSelectedPicturesWithPool;
    private iPartsGuiLengthLimitedTextArea reasonTextArea;

    private final AssemblyId assemblyId;
    private boolean onlyForChangeOrder;
    private boolean isEditAllowed = true;

    /**
     * Erzeugt eine Instanz von EditShowASPLMMediaContentPictureForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public PicOrderMediaContentForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                    AssemblyId assemblyId, iPartsDataPicOrder dataPicOrder, int maxResults, boolean forChangeOrder) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.assemblyId = assemblyId;
        this.dataPicOrder = dataPicOrder;
        this.maxResults = maxResults;
        setOnlyForChangeOrder(forChangeOrder);
        postCreateGui();
        EtkDisplayFields displayResultFields = createDisplayFields(dataConnector);
        setDisplayResultFields(displayResultFields);
        init();
    }

    private EtkDisplayFields createDisplayFields(AbstractJavaViewerFormIConnector dataConnector) {
        EtkDisplayFields displayResultFields = SelectSearchGridImage.createPoolGridFields(dataConnector.getProject());
        displayResultFields.getFeldByName(TABLE_POOL, FIELD_P_VER).setVisible(true);
        displayResultFields.getFeldByName(TABLE_POOL, FIELD_P_USAGE).setVisible(false); // wird intern benutzt und soll nicht angezeigt werden
        displayResultFields.getFeldByName(TABLE_POOL, FIELD_P_IMAGES).setColumnFilterEnabled(true);
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_VAR_TYPE, false, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_DESIGNER, false, false);
        displayField.setColumnFilterEnabled(true);
        displayResultFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_LAST_MODIFIED, false, false);
        displayResultFields.addFeld(displayField);
        displayResultFields.loadStandards(getConfig());
        boolean endStateOfChangeOrder = iPartsTransferStates.isEndState(dataPicOrder.getStatus()) && dataPicOrder.isChangeOrCopy(); // abgeschlossener Änderungsauftrag oder Kopierauftrag
        boolean notForChangeAndNotEndState = !iPartsTransferStates.isEndState(dataPicOrder.getStatus()) && !onlyForChangeOrder; // während und bevor Korrekturauftrag
        if (getConnector().isAuthorOrderValid() && (onlyForChangeOrder || endStateOfChangeOrder || notForChangeAndNotEndState)) {
            displayField = new EtkDisplayField(COL_CHECKBOX_TABLENAME, COL_CHECKBOX_FIELDNAME, false, false);
            displayField.loadStandards(getConfig());
            List<String> languages = getConfig().getViewerLanguages();
            EtkMultiSprache texte = new EtkMultiSprache();
            String text;
            if (onlyForChangeOrder) {
                text = "!!Ändern?";
            } else if (iPartsTransferStates.isEndState(dataPicOrder.getStatus())) {
                text = "!!Bild im Auftrag geändert";
            } else {
                text = "!!Korrigieren?";
            }
            for (String language : languages) {
                texte.setText(language, TranslationHandler.translateForLanguage(text, language));
            }
            displayField.setText(texte);
            displayResultFields.addFeld(displayField);
        }
        return displayResultFields;
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        setReasonTextArea();
        Dimension screenSize = FrameworkUtils.getScreenSize();
        // Größe auf mindestens 80% des Bildschirms/Browsers festlegen
        mainWindow.setMinimumHeight((int)(0.8 * screenSize.getHeight()));
        mainWindow.setMinimumWidth((int)(0.8 * screenSize.getWidth()));
        int minHeigthCorrectionPanel = (int)(0.4 * mainWindow.getHeight());
        mainWindow.panel_correction.setMinimumHeight(minHeigthCorrectionPanel);

        contractorForm = new EditASPLMContractorForm(getConnector(), this);
        AbstractGuiControl contractorGui = contractorForm.getContractorPanelToBeAdded();

        mainWindow.panel_contractor.addChildBorderCenter(contractorGui);

        contractorForm.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onContractorValueChange(event);
            }
        });
        contractorForm.setUserGroupLabelText("!!Gruppe", true);

        objectGrid = new PictureDataObjectGrid(getConnector(), this);
        objectGrid.setPageSplitNumberOfEntriesPerPage(maxResults);

        // ObjectGrid aufschnappen und sichtbar machen
        mainWindow.panel_picList.addChildBorderCenter(objectGrid.getGui());

        // Picture Panel erzeugen und einhängen
        panel = new GuiPanel();
        panel.setBackgroundColor(Colors.clWhite.getColor());
        panel.setLayout(new LayoutBorder());
        mainWindow.mainSplitPane_secondChild.addChildBorderCenter(panel);

        selectAllButton = mainWindow.buttonPanel.addCustomButton("!!Alle auswählen");
        selectAllButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doSelectAll(true, true);
            }
        });
        deSelectAllButton = mainWindow.buttonPanel.addCustomButton("!!Alle abwählen");
        deSelectAllButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doSelectAll(false, true);
            }
        });
        mainWindow.pack();
        doEnableButtons();
    }

    private void setReasonTextArea() {
        reasonTextArea = new iPartsGuiLengthLimitedTextArea();
        reasonTextArea.setName("textarea_reason");
        reasonTextArea.__internal_setGenerationDpi(96);
        reasonTextArea.registerTranslationHandler(getUITranslationHandler());
        reasonTextArea.setScaleForResolution(true);
        reasonTextArea.setMinimumWidth(200);
        reasonTextArea.setMinimumHeight(100);
        reasonTextArea.setScrollToVisible(true);
        reasonTextArea.addEventListener(new EventListener("onChangeEvent") {
            public void fire(Event event) {
                refreshView(event);
            }
        });
        ConstraintsBorder textarea_reasonConstraints = new ConstraintsBorder();
        reasonTextArea.setConstraints(textarea_reasonConstraints);
        mainWindow.scrollpane_reason.addChild(reasonTextArea);
    }

    /**
     * Setzt abhängig von der Auswahl des Benutzers den korrespondierenden Wert im DataObject
     */
    private void saveChangeInfo() {
        if (!getSelectedPictures().isEmpty()) {
            for (iPartsDataPicOrderPicture selectedPicture : getSelectedPictures()) {
                selectedPicture.setIsUsed(true);
            }
        }
        if (!getNotSelectedPictures().isEmpty()) {
            for (iPartsDataPicOrderPicture selectedPicture : getNotSelectedPictures()) {
                selectedPicture.setIsUsed(false);
            }
        }
    }

    private void refreshView(Event event) {
        doEnableButtons();
    }

    private void onContractorValueChange(Event event) {
        doEnableButtons();
    }

    private void doEnableButtons() {
        if (!iPartsTransferStates.isValidEndState(dataPicOrder.getStatus()) && isEditAllowed) {
            String okButtonText;
            // Korrekturen können nur bei einem normalen Auftrag angefordert werden
            if (onlyForChangeOrder) {
                okButtonText = "!!Zeichnungen auswählen";
            } else if (isPictureSelectedForCorrection()) {
                okButtonText = "!!Korrektur anfordern";
            } else {
                okButtonText = "!!Zeichnungen übernehmen";
            }
            getOKButton().setText(okButtonText);
            getOKButton().setEnabled(!isWaitingForCorrection());
            if (isPictureSelectedForCorrection() && !onlyForChangeOrder) {
                // Falls keine Gruppen gültig sind für den Benutzer, kann kein Korrekturauftrag versendet werden
                if (!contractorForm.isUserGroupComboBoxFilled() && contractorForm.isUserGroupMustField()) {
                    MessageDialog.showWarning("!!Keine für den Benutzer gültigen Gruppen gefunden. Es kann kein Korrekturauftrag gesendet werden!");
                } else {
                    mainWindow.panel_correction.setVisible(true);
                    mainWindow.calendar_dateDue.setDate(GregorianCalendar.getInstance());
                }
                checkIfCorrectionComplete();
            } else {
                mainWindow.panel_correction.setVisible(false);
                clearInput();
                if (!isWaitingForCorrection()) {
                    getOKButton().setEnabled(true);
                }
            }
            if ((iPartsTransferStates.canRequestCorrection(dataPicOrder.getStatus()) || onlyForChangeOrder) && !dataPicOrder.hasInvalidImageData()) {
                selectAllButton.setEnabled(true);
                deSelectAllButton.setEnabled(true);
            } else {
                selectAllButton.setEnabled(false);
                deSelectAllButton.setEnabled(false);
            }
        } else {
            getOKButton().setVisible(false);
            getCancelButton().setText("!!Schließen");
            selectAllButton.setVisible(false);
            deSelectAllButton.setVisible(false);
            mainWindow.panel_correction.setVisible(false);
        }
    }

    private void clearInput() {
        reasonTextArea.clear();
        contractorForm.clearComboboxes();
        mainWindow.calendar_dateDue.clearDate();
        mainWindow.calendar_dateDue.resetDateStringIfInvalid();
    }

    public void setEditAllowed(boolean isEdit) {
        boolean editAllowed = isEdit;
        if (dataPicOrder != null) {
            editAllowed &= dataPicOrder.isValid();
        }
        if (isEditAllowed != editAllowed) {
            isEditAllowed = editAllowed;
            doEnableButtons();
        }
    }

    private void checkIfCorrectionComplete() {
        boolean okButtonEnabled = !StrUtils.isEmpty(reasonTextArea.getText());
        okButtonEnabled &= contractorForm.isValid();
        getOKButton().setEnabled(okButtonEnabled);
    }

    private GuiButtonOnPanel getCancelButton() {
        return getButton(GuiButtonOnPanel.ButtonType.CANCEL);
    }

    private GuiButtonOnPanel getOKButton() {
        return getButton(GuiButtonOnPanel.ButtonType.OK);
    }

    private GuiButtonOnPanel getButton(GuiButtonOnPanel.ButtonType buttonType) {
        return mainWindow.buttonPanel.getButtonOnPanel(buttonType);
    }

    /**
     * (De-)Aktiviert die Checkboxen in jeder Zeile
     *
     * @param selected
     */
    private void doSelectAll(boolean selected, boolean checkBoxEnabled) {
        for (int i = 0; i < getTable().getRowCount(); i++) {
            GuiTableRow row = getTable().getRow(i);
            Object object = row.getChildForColumn(getTable().getColCount() - 1);
            if (object instanceof iPartsGuiPanelWithCheckbox) {
                iPartsGuiPanelWithCheckbox checkBox = ((iPartsGuiPanelWithCheckbox)object);
                checkBox.setSelected(selected);
                checkBox.setEnabled(checkBoxEnabled);
            }
        }
        doEnableButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public ModalResult showModal() {
        ModalResult result = mainWindow.showModal();
        close();
        return result;
    }

    @Override
    public void dispose() {
        if (contractorForm != null) {
            contractorForm.dispose();
        }
        super.dispose();
    }

    public EtkDisplayFields getDisplayResultFields() {
        return objectGrid.getDisplayFields();
    }

    public void setDisplayResultFields(EtkDisplayFields displayResultFields) {
        objectGrid.setDisplayFields(displayResultFields);
    }

    public void setMultiSelect(boolean value) {
        objectGrid.setMultiSelect(value);
    }

    public void setOnlyForChangeOrder(boolean onlyForChangeOrder) {
        this.onlyForChangeOrder = onlyForChangeOrder;
    }

    private void init() {
        iPartsDataPicOrderPicturesList picturesList;
        // Unterscheidung: Für Korrektur- und Änderungsaufträge (oder Kopien) dürfen nur die zu dem Auftrag gehörenden Zeichnungen
        // angezeigt werden. Bei einem abgeschlossenen Bildauftrag werden alle Zeichnungen angezeigt, die während der
        // Laufzeit eines Bildauftrags akzeptiert wurden.
        if (iPartsTransferStates.isEndState(dataPicOrder.getStatus())
            || iPartsTransferStates.isCreatedChangeOrCopyOrder(dataPicOrder.getStatus())) {
            picturesList = dataPicOrder.getPicturesWithPredecessors(true);
        } else {
            picturesList = dataPicOrder.getPictures();
        }
        // Da ein Änderungsauftrag initial alle Zeichnungen betrifft, wird überprüft, ob mind. eine ausgewählt wurde.
        // Falls nicht, wird bei allen ein Häkchen gesetzt
        boolean checkAllBoxes = true;
        if (iPartsTransferStates.isCreatedChangeOrCopyOrder(dataPicOrder.getStatus())) {
            for (iPartsDataPicOrderPicture dataPicOrderPicture : picturesList) {
                if (dataPicOrderPicture.isUsed()) {
                    checkAllBoxes = false;
                    break;
                }
            }
        } else {
            checkAllBoxes = false;
        }
        objectGrid.clearGrid();
        EtkDataPoolVariants variants = EtkDataObjectFactory.createDataPoolVariants();
        EtkDataPoolEntry poolEntry = EtkDataObjectFactory.createDataPoolEntry();
        poolEntry.init(getProject());
        for (iPartsDataPicOrderPicture dataPicOrderPicture : picturesList) {
            // Migrierte Bilder können nicht korrigiert werden -> Hier ausfiltern
            if (!dataPicOrderPicture.isASPLMPicture()) {
                continue;
            }

            // Nur bei einem abgeschlossenen Änderungsauftrag/Kopierauftrag den direkten Bezug zum Änderungsauftrag setzen (direkter
            // Bezug bedeutet, dass diese Zeichnungen innerhalb diesen Auftrags bearbeitet wurden)
            boolean isChangeOrderWithRefPictures = iPartsTransferStates.isEndState(dataPicOrder.getStatus())
                                                   && dataPicOrder.isChangeOrCopy()
                                                   && dataPicOrder.getPictures().containsId(dataPicOrderPicture.getAsId());
            if (checkAllBoxes || isChangeOrderWithRefPictures) {
                dataPicOrderPicture.setIsUsed(true);
            }
            PoolEntryId poolEntryId = new PoolEntryId(dataPicOrderPicture.getAsId().getPicItemId(), dataPicOrderPicture.getAsId().getPicItemRevId());
            if (poolEntry.loadFromDB(poolEntryId)) {
                variants.loadPoolVariants(getProject(), poolEntry, DBActionOrigin.FROM_EDIT);
                EtkDataPool bestVariant = variants.getBestImageVariant("", getPreferredImageUsage());
                if (bestVariant != null) {
                    objectGrid.addObjectToGrid(bestVariant, dataPicOrderPicture);
                }
            }
        }
        if (!picturesList.isEmpty()) {
            updateGrid();
        }
        objectGrid.showNoResultsLabel(picturesList.isEmpty());
        // Hat der Bildauftrag nur ungültige Bilder, dann darf nur eine Korrektur gesendet werden
        // -> Bild wird selektiert und er Benutzer kann die Selektion nicht entfernen
        if (dataPicOrder.hasInvalidImageData()) {
            reasonTextArea.clear();
            reasonTextArea.setText(TranslationHandler.translate("!!SVG enthält ungültige <clipPath> Elemente!"));
            doSelectAll(true, false);

        } else {
            doEnableButtons();
        }
    }

    private void updateGrid() {
        mainWindow.mainSplitPane.setDividerPosition(objectGrid.getOverallWidth());
        updateView();
    }

    protected void doSelectionChanged(Event event) {
        List<EtkDataObject> selectedList = objectGrid.getSelection();
        if (selectedList != null) {
            if (selectedList.size() >= 1) {
                for (EtkDataObject dataObject : selectedList) {
                    if (dataObject instanceof EtkDataPool) {
                        EtkDataPool pool = (EtkDataPool)dataObject;
                        updateImageWindow(pool);
                        break;
                    } else {
                        hideImageWindow();
                    }
                }
            } else {
                hideImageWindow();
            }
        } else {
            hideImageWindow();
        }
    }

    private void hideImageWindow() {
        // dispose() auf allen GuiViewerImageInterface-Instanzen aufrufen, um evtl. Ressourcen und EventHandler aufzuräumen
        if (imgViewer != null) {
            imgViewer.dispose();
            imgViewer = null;
        }

        // bisheriges imageViewerControl entfernen (kann ScrollPane oder GuiViewer sein)
        // Control darf erst ausgehängt werden, wenn die bislang angezeigten Viewer disposed wurden
        // Ansonsten können diese sich nicht mehr aufräumen (weil z.B. der GuiLogger nicht mehr erreichbar ist)
        if (imageViewerCenterControl != null) {
            imageViewerCenterControl.removeFromParent();
            imageViewerCenterControl = null;
        }
        panel.removeAllChildren();

    }

    private void updateImageWindow(EtkDataPool imageVariant) {
        // Normale Anzeige eines Bildes
        hideImageWindow();

        int newImageIndex = -1;
        int imageIndex = 0;
        if (imageIndex >= 0) {
            VarParam<EtkDataPool> displayedImageVariant = new VarParam<>(imageVariant);
            imgViewer = createImageViewer(imageIndex, displayedImageVariant);
            AbstractGuiControl imageViewerGui;
            if (imgViewer != null) {
                imageViewerGui = imgViewer.getGui();
                if (imgViewer instanceof GuiViewerImage) {
                    ((GuiViewerImage)imgViewer).adjustEditNotesMenuIdForPanWindow();
                }

                imageViewerGui.setConstraints(new ConstraintsBorder(LayoutBorder.POSITION_CENTER));
                imageViewerCenterControl = imageViewerGui;
                SelectSearchGridImage.fillHotspots(getProject(), imgViewer, displayedImageVariant.getValue());
                panel.addChild(imageViewerGui);
                newImageIndex = imageIndex;
            }
            panel.setVisible(newImageIndex >= 0);
        } else {
            //!! nur zum Testen
            AbstractGuiControl label = new UnsupportedImageViewerItem("!!Vorschaubild wurde angefordert").getGui();
            label.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
            panel.addChild(label);
            panel.setVisible(true);
        }
    }

    private void doSendMQMessage(Event event) {
        // Nicht Senden wenn es sich um die Auswahl für einen Änderungsauftrag handelt
        if (onlyForChangeOrder) {
            loadPicturesAndPool();
            close();
            return;
        }
        iPartsXMLMediaMessage mediaMessage;
        iPartsTransferStates oldPicOrderState = dataPicOrder.getStatus();
        iPartsTransferStates newPicOrderState;
        boolean sendingIsAllowed;
        PictureDBActions dbAction;
        // Unterscheidung Zeichnungen angenommen/abgelehnt
        if (!isPictureSelectedForCorrection()) {
            if (!isWaitingForCorrection()) {
                // Bild(-er) wurden angenommen, Vorschaubild erfragen
                newPicOrderState = iPartsTransferStates.CONFIRMATION_SEND;
                mediaMessage = getAcceptingMessage();
                sendingIsAllowed = mediaMessage != null;
            } else {
                close();
                return;
            }
            dbAction = PictureDBActions.PICTURE_SAVE;
        } else {
            mediaMessage = getRequestCorrectionMessage();
            if (mediaMessage == null) {
                return;
            }
            sendingIsAllowed = iPartsTransferStates.canRequestCorrection(dataPicOrder.getStatus());
            newPicOrderState = iPartsTransferStates.CORRECTION_MO_REQUESTED;
            dbAction = PictureDBActions.PICTURE_DELETE;
        }
        if (sendingIsAllowed) {
            int simAnswerDelay = iPartsPlugin.getSimAutoResponseDelayForSession(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY) * 1000;
            boolean simulateAnswer = (simAnswerDelay >= 0);
            try {
                // Der neue Status muss vor dem Senden in die DB geschrieben werden, da die Antwort vor handleDBOperations() kommen könnte
                dataPicOrder.setStatus(newPicOrderState, DBActionOrigin.FROM_EDIT);
                dataPicOrder.saveToDB();
                iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).sendXMLMessageWithMQ(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                                                     mediaMessage, simulateAnswer);

                boolean saveMessagesAsXML = iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_SIM_PIC_CONTENT_XML);
                if ((simulateAnswer || saveMessagesAsXML) && (mediaMessage.getRequest() != null)) {
                    iPartsXMLMediaMessage expectedResponseXmlMessage = null;
                    iPartsTransferNodeTypes operationType = mediaMessage.getRequestOperationType();
                    if (operationType == iPartsTransferNodeTypes.CORRECT_MEDIA_ORDER) {
                        expectedResponseXmlMessage = iPartsEditXMLResponseSimulator.createCorrectMediaOrderResponse(mediaMessage, dataPicOrder.getPictures());
                    } else if (operationType == iPartsTransferNodeTypes.ACCEPT_MEDIA_CONTAINER) {
                        expectedResponseXmlMessage = iPartsEditXMLResponseSimulator.createAcceptMediaContainerResponse(mediaMessage);
                    }
                    if (expectedResponseXmlMessage != null) {
                        iPartsEditXMLResponseSimulator.writeAndSendSimulatedMessageResponseFromXML(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                                   expectedResponseXmlMessage,
                                                                                                   saveMessagesAsXML,
                                                                                                   simAnswerDelay);
                    }
                }

                handlePictureDBActions(dbAction);
            } catch (Exception e) {
                // im Fehlerfall den ursprünglichen Status wiederherstellen, damit die MQ-Operation später erneut durchgeführt werden kann
                dataPicOrder.setStatus(oldPicOrderState, DBActionOrigin.FROM_EDIT);
                dataPicOrder.saveToDB();
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
                MessageDialog.showError(e.getMessage(), mainWindow.getTitle());
            }
        }
        close();
    }

    public boolean isWaitingForCorrection() {
        return iPartsTransferStates.isInCorrectionWorkflow(dataPicOrder.getStatus());
    }

    /**
     * Löscht oder Speichert die von AS-PLM erhaltenen Zeichnungen
     *
     * @param dbAction
     */
    private void handlePictureDBActions(PictureDBActions dbAction) {
        iPartsDataImageList list = null;
        getProject().getDbLayer().startTransaction();
        try {
            switch (dbAction) {
                case PICTURE_SAVE:
                    list = saveImages();
                    break;
                case PICTURE_DELETE:
                    deleteImages();
                    // Falls es ungültige Zeichnungen gab, hier den Kenner entfernen, da eine Korrektur abgeschickt wurde
                    if (dataPicOrder.hasInvalidImageData()) {
                        dataPicOrder.setFieldValueAsBoolean(FIELD_PO_INVALID_IMAGE_DATA, false, DBActionOrigin.FROM_EDIT);
                        dataPicOrder.saveToDB();
                    }
                    break;
            }
            getProject().getDbLayer().commit();
        } catch (Exception e) {
            getProject().getDbLayer().rollback();
            MessageDialog.showError(e.getMessage(), mainWindow.getTitle());
            return;
        }
        if (list != null) {
            // Nach dem Hinzufügen der neuen Zeichnungen den Gültigkeitsbereich der Zeichnungen aktualisieren
            iPartsDataImage.updateAndSaveValidityScopeForImages(list.getAsList(), getProject());
        }

    }

    /**
     * Löscht die von AS-PLM erhaltenen Zeichnungen
     */
    private void deleteImages() {
        for (List<EtkDataObject> dbObjectsList : getSelectedPicturesWithAllDataObjects()) {
            iPartsDataPicOrderPicture dataPicOrderPicture = null;
            EtkDataPool pool = null;
            for (EtkDataObject dbObject : dbObjectsList) {
                if (dbObject instanceof EtkDataPool) {
                    pool = (EtkDataPool)dbObject;
                } else if (dbObject instanceof iPartsDataPicOrderPicture) {
                    dataPicOrderPicture = (iPartsDataPicOrderPicture)dbObject;
                }
            }

            if (pool != null) {
                EtkDataPoolEntry poolEntry = EtkDataObjectFactory.createDataPoolEntry();
                poolEntry.init(getProject());
                poolEntry.setPKValues(pool.getAsId().getPImages(), pool.getAsId().getPVer(), DBActionOrigin.FROM_DB);
                poolEntry.deleteFromDB(true);

                EtkDataPoolVariants variants = EtkDataObjectFactory.createDataPoolVariants();
                variants.loadPoolVariantsForPool(getProject(), pool.getAsId(), DBActionOrigin.FROM_DB);
                variants.deleteFromDB(getProject(), true);
            }

            if (dataPicOrderPicture != null) {
                dataPicOrderPicture.deleteFromDB(true);
                dataPicOrder.getPictures().delete(dataPicOrderPicture, DBActionOrigin.FROM_EDIT);
                dataPicOrder.saveToDB();
            }
        }
    }

    /**
     * Speichert die Bildreferenzen zum Modul
     *
     * @return
     */
    private iPartsDataImageList saveImages() {
        iPartsDataImageList list = new iPartsDataImageList();
        if (dataPicOrder != null) {
            String code = dataPicOrder.getFieldValue(FIELD_DA_PO_CODES);
            String eventFromId = dataPicOrder.getFieldValue(FIELD_DA_PO_EVENT_FROM);
            String eventToId = dataPicOrder.getFieldValue(FIELD_DA_PO_EVENT_TO);
            boolean onlyFINVisible = dataPicOrder.getFieldValueAsBoolean(FIELD_PO_ONLY_FIN_VISIBLE);
            // Bei migrierten Bilder wird ein initialer Bildauftrag gefaket. Damit wir aber die Verknüpfung zum Originalbild
            // beibehalten hängt es an dieser Stelle schon an der Assembly. Daher müssen diese Bilder ausgefiltert werden,
            // sonst kommen sie mehrmals vor.
            boolean hasFakePredecessor = dataPicOrder.hasFakeOriginalPicOrder();
            Set<String> existingImages = new HashSet<>();
            if (hasFakePredecessor) {
                for (EtkDataImage image : getConnector().getCurrentAssembly().getUnfilteredImages()) {
                    String imageKey = makeImageKey(image.getImagePoolNo(), image.getImagePoolVer());
                    existingImages.add(imageKey);
                }
            }

            // Map mit der PV Nummer und Revision des neuen Bildes (von AS-PLM) auf die Bildreferenzen zur Bildnummer
            // in der DB. AS-PLM kann prinzipiell mehrere unterschiedliche PV Nummern pro Antwort schicken, daher die Map.
            // Standard ist aktuell: eine PV Nummer pro Antwort
            Map<PoolEntryId, List<EtkDataImage>> imagesWithOlderRev = new HashMap<>();
            for (iPartsDataPicOrderPicture imageDataObject : dataPicOrder.getPictures()) {
                if (hasFakePredecessor && !existingImages.isEmpty()) {
                    String imageKey = makeImageKey(imageDataObject.getAsId().getPicItemId(), imageDataObject.getAsId().getPicItemRevId());
                    if (existingImages.contains(imageKey)) {
                        continue;
                    }
                }
                // Zur Bildnummer alle Bildreferenzen aus der DB laden
                addExistingPicReferenceFromDBForPicItemId(imagesWithOlderRev, imageDataObject);
                // Das neue Bild an das Modul hängen und mit Gültigkeiten befüllen
                EtkDataImage image = addImageToAssembly(imageDataObject, code, eventFromId, eventToId, onlyFINVisible);
                list.add(image, DBActionOrigin.FROM_EDIT);
            }
            // Bildreferenzen anpassen sofern es der Benutzer möchte
            adjustPicturesInOtherModules(imagesWithOlderRev);

            if (isRevisionChangeSetActiveForEdit()) {
                addDataObjectToActiveChangeSetForEdit(getConnector().getCurrentAssembly());
            } else {
                // Changeset nicht aktiv, speichern in DB
                list.saveToDB(getProject());
            }
        }
        return list;
    }

    /**
     * Fügt das übergebene Bild dem Modul hinzu
     *
     * @param imageDataObject
     * @param code
     * @param eventFromId
     * @param eventToId
     * @param onlyFINVisible
     * @return
     */
    private EtkDataImage addImageToAssembly(iPartsDataPicOrderPicture imageDataObject, String code, String eventFromId,
                                            String eventToId, boolean onlyFINVisible) {
        String picItemId = imageDataObject.getAsId().getPicItemId();
        String picItemRevId = imageDataObject.getAsId().getPicItemRevId();
        List<EtkDataImage> sortedPictures = getConnector().getCurrentAssembly().getImages().stream()
                .filter(image -> image.getImagePoolNo().equals(picItemId))
                .sorted((current, next) -> current.getImagePoolVer().compareTo(next.getVer()))
                .collect(Collectors.toList());
        EtkDataImage image = getConnector().getCurrentAssembly().addImage(picItemId, picItemRevId,
                                                                          true, DBActionOrigin.FROM_EDIT);
        // PV Revision, Code, Ereignisse und der Wert für "Nur bei FIN ausgeben" werden immer gesetzt (Code, Ereignisse
        // und Boolean-Wert können im Bildauftrag angegeben werden). Code, Ereignisse und der Wert für "Nur mit FIN ausgeben"
        // des Vorgängers werden beim Erstellen des Änderungsauftrags vom Vorgänger an den Auftrag geschrieben. Daher
        // sind die Werte vom Auftrag (Methodenparameter) aktuell.
        image.setFieldValue(FIELD_I_PVER, picItemRevId, DBActionOrigin.FROM_EDIT);
        image.setFieldValue(FIELD_I_CODES, code, DBActionOrigin.FROM_EDIT);
        image.setFieldValue(FIELD_I_EVENT_FROM, eventFromId, DBActionOrigin.FROM_EDIT);
        image.setFieldValue(FIELD_I_EVENT_TO, eventToId, DBActionOrigin.FROM_EDIT);
        image.setFieldValueAsBoolean(FIELD_I_ONLY_FIN_VISIBLE, onlyFINVisible, DBActionOrigin.FROM_EDIT);
        // Falls am Modul Bilder hängen mit einer älteren Revision, müssen die Gültigkeiten übernommen werden
        if (!sortedPictures.isEmpty()) {
            // höchste Revision bestimmen
            EtkDataImage previousImage = sortedPictures.get(sortedPictures.size() - 1);
            // Alle Gültigkeiten der vorherigen Revision übernehmen.
            image.setFieldValueAsArray(FIELD_I_MODEL_VALIDITY, previousImage.getFieldValueAsArray(FIELD_I_MODEL_VALIDITY), DBActionOrigin.FROM_EDIT);
            image.setFieldValueAsArray(FIELD_I_SAA_CONSTKIT_VALIDITY, previousImage.getFieldValueAsArray(FIELD_I_SAA_CONSTKIT_VALIDITY), DBActionOrigin.FROM_EDIT);
            image.setFieldValueAsArray(FIELD_I_PSK_VARIANT_VALIDITY, previousImage.getFieldValueAsArray(FIELD_I_PSK_VARIANT_VALIDITY), DBActionOrigin.FROM_EDIT);
        }

        Calendar lastModified = imageDataObject.getFieldValueAsDateTime(FIELD_DA_POP_LAST_MODIFIED);
        if (lastModified != null) {
            image.setFieldValueAsDate(FIELD_I_IMAGEDATE, lastModified, DBActionOrigin.FROM_EDIT);
        }
        return image;
    }

    /**
     * Fügt der übergebenen <code>imagesWithOlderRev</code> Map alle Bildreferenzen hinzu, die die gleiche Bildnummer haben
     * und in der DB existieren.
     *
     * @param imagesWithOlderRev
     * @param newPicture
     */
    private void addExistingPicReferenceFromDBForPicItemId(Map<PoolEntryId, List<EtkDataImage>> imagesWithOlderRev, iPartsDataPicOrderPicture newPicture) {
        String picItemId = newPicture.getAsId().getPicItemId();
        String picItemRevId = newPicture.getAsId().getPicItemRevId();
        // Ohne ChangeSets laden, damit nur freigegebene Einträge angepast werden
        getProject().executeWithoutActiveChangeSets(() -> {
            EtkDataImageList images = new iPartsDataImageList();
            images.loadImagesForImageNumber(getProject(), picItemId);
            if (images.size() > 0) {
                String currentModule = getConnector().getCurrentAssembly().getAsId().getKVari();
                // Das aktuelle Modul ausfiltern, falls die alte Revision vorhanden ist
                List<EtkDataImage> otherModules = images.getAsList().stream().filter(image -> !image.getTiffName().equals(currentModule)).collect(Collectors.toList());
                if (!otherModules.isEmpty()) {
                    // Als Key die neue Bildnummer und die neue Revision verwenden
                    imagesWithOlderRev.put(new PoolEntryId(picItemId, picItemRevId), otherModules);
                }
            }
        }, false);
    }

    /**
     * Passt alle freigegebenen Bildreferenzen an, zu denen eine neue Revision geliefert wurde, sofern der Benutzer
     * es denn möchte.
     *
     * @param imagesWithOlderRev
     */
    private void adjustPicturesInOtherModules(Map<PoolEntryId, List<EtkDataImage>> imagesWithOlderRev) {
        if (!imagesWithOlderRev.isEmpty()) {
            GenericEtkDataObjectList<iPartsDataAssembly> assemblies = new GenericEtkDataObjectList<>();
            // AS-PLM kann prinzipiell mehrere unterschiedliche PV Nummern pro Antwort schicken, daher hier die
            // Unterscheidung zwischen einem und mehreren Bilder. Standard ist aktuell eine PV Nummer pro Antwort
            boolean hasSinglePicItem = imagesWithOlderRev.size() == 1;
            String picText = hasSinglePicItem ? "!!Zeichnung" : "!!Zeichnungen";
            String message = TranslationHandler.translate("!!In folgenden freigegebenen TUs wurden ältere Revisionen der %1 gefunden:",
                                                          TranslationHandler.translate(picText)) + "\n\n";
            StringBuilder builder = new StringBuilder(message);
            // Über alle Referenzen iterieren und den Text aufbauen sowie die Referenzen und Assemblies anpassen und ablegen
            imagesWithOlderRev.forEach((key, value) -> value.forEach(imageObject -> {
                String picItemId = imageObject.getFieldValue(FIELD_I_IMAGES);
                String picItemRevId = imageObject.getFieldValue(FIELD_I_PVER);
                String module = imageObject.getAsId().getITiffName();
                builder.append(TranslationHandler.translate("!!Zeichnung \"%1\", Revision \"%2\" in TU \"%3\"",
                                                            picItemId, picItemRevId, module));
                builder.append("\n");
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(imageObject.getAsId().getITiffName(),
                                                                                                                imageObject.getAsId().getIVer()));
                if ((assembly instanceof iPartsDataAssembly) && assembly.existsInDB()) {
                    // Fremde Assembly als geändert markieren
                    assembly.getAttributes().markAsModified();
                    Optional<EtkDataImage> imageFromAssembly = assembly.getUnfilteredImages().getAsList()
                            .stream()
                            .filter(image -> image.getAsId().equals(imageObject.getAsId()))
                            .findAny();
                    // Hier schon die neue Revision und das Assembly setzen, dann brauchen wir später nur die Liste komplett übernehmen und
                    // nicht nochmal durchlaufen. Möchte der Benutzer die Referenzen nicht anpassen, ist es dann auch egal.
                    imageFromAssembly.ifPresent(image -> {
                        image.setFieldValue(EtkDbConst.FIELD_I_PVER, key.getPEVer(), DBActionOrigin.FROM_EDIT);
                        assemblies.add((iPartsDataAssembly)assembly, DBActionOrigin.FROM_EDIT);
                    });
                }

            }));
            builder.append("\n");
            if (hasSinglePicItem) {
                // Standard-Fall
                builder.append(TranslationHandler.translate("!!Soll die neue Revision in alle freigegebenen TUs übernommen werden?"));
            } else {
                builder.append(TranslationHandler.translate("!!Sollen die neuen Revisionen in alle freigegebenen TUs übernommen werden?"));
            }

            if (MessageDialog.showYesNo(builder.toString(), "!!Übernahme Zeichnungen in andere TUs") == ModalResult.YES) {
                // Änderung im ChangeSet speichern
                if (isRevisionChangeSetActiveForEdit()) {
                    addDataObjectListToActiveChangeSetForEdit(assemblies);
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not store adjusted pictures" +
                                                                              " in change set. Edit change set not active");
                }
            }
        }
    }

    private String makeImageKey(String imagePoolNo, String imagePoolVer) {
        return imagePoolNo + "||" + imagePoolVer;
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    private EtkDataImage loadImage(EtkDataPool pool) {
        //Simulation für EtkDataImage
        String img = pool.getAsId().getPImages();
        String imgVer = pool.getAsId().getPVer();
        String blatt = EtkDbsHelper.formatLfdNr(1);
        return EtkDataObjectFactory.createDataImage(getProject(), assemblyId, blatt, img, imgVer);
    }

    protected GuiViewerImageInterface createImageViewer(final int imageIndex, VarParam<EtkDataPool> pool) {
        GuiViewerImageInterface imageViewer;

        if ((imageIndex < 0) /*|| (imageIndex >= getConnector().getCurrentAssembly().getImageCount())*/) {
            return null;
        }

        EtkDataImage imageData = loadImage(pool.getValue());
        String imgUsage = getPreferredImageUsage();
        EtkDataPool variant = imageData.getBestImageVariant(getProject().getDBLanguage(), imgUsage);

        if (variant != null) {
            pool.setValue(variant);
            String imgType = variant.getImageType();

            if (imgType != null) {
                imageViewer = getViewerByImageType(imgType, imageIndex);  // passenden Viewer für Bild bestimmen
                if (imageViewer == null) {
                    return null;
                }

                if (imageViewer instanceof GuiViewerImage) {
                    GuiViewerImage imageGuiViewer = (GuiViewerImage)imageViewer;
                    GuiViewerImageNavigation viewerImageNavigation = GuiViewerImageNavigation.create(false, imageIndex, 1);
                    imageGuiViewer.setGuiViewerImageNavigation(viewerImageNavigation);
                    imageGuiViewer.setShowUnmarkedHotspots(true);
                }

                byte[] imgData = variant.getImgBytes();
                if ((imgData != null) && (imgData.length > 0)) {
                    imageViewer.setData(imgData, imgType, GuiViewerImageInterface.MAX_NUMBER_OF_PIXELS_UNLIMITED /*maxNumberOfPixels*/, true);
                    imageViewer.display();
                    if (imageViewer instanceof AbstractImageViewer3D) {
                        ((AbstractImageViewer3D)imageViewer).assignSettings(getImageSettings().getImageCommonSettings(),
                                                                            getImageSettings().getImageHotspotSettings(),
                                                                            getImageSettings().getImageSecuritySettings());
                    }
                } else {
                    imageViewer = null;
                }
            } else {
                imageViewer = null;
            }
        } else {
            imageViewer = null;
        }
        return imageViewer;
    }

    private String getPreferredImageUsage() {
        // Interne Prio bzgl. SVG und 2D abhängig vom Connector; 3D bei Bedarf hinzufügen
        return getConnector().isPreferSVGImages() ? EtkDataImage.IMAGE_USAGE_SVG : EtkDataImage.IMAGE_USAGE_2D_FILLED;
    }

    /**
     * Imagesetting ermitteln, falls noch nicht geladen, tue das
     */
    private EtkImageSettings getImageSettings() {
        if (imageSettings == null) {
            imageSettings = new EtkImageSettings();
            imageSettings.load(getConfig());
        }
        return imageSettings;
    }

    private GuiViewerImageInterface getViewerByImageType(String extension, final int imageIndex) {
        try {
            final GuiViewerImageInterface imageViewer = GuiViewer.getImageViewerForFilename("dummy." + extension, imageIndex, false,
                                                                                            getProject().isDocumentDecryptionNecessary());

            if (imageViewer != null) {
                imageViewer.addEventListener(new GuiViewerImageEvents() {
                    @Override
                    public void OnLoaded() {
                    }

                    @Override
                    public boolean OnLinkClick(List<GuiViewerLink> links, int imageIndex, boolean imageIs3D, int button) {
                        return false;
                    }

                    @Override
                    public boolean OnLinkDblClick(GuiViewerLink link, int imageIndex, boolean imageIs3D, int button) {
                        return false;
                    }

                    @Override
                    public String OnLinkHintTextNeeded(GuiViewerLink link, String hintText) {
                        return hintText;
                    }

                    @Override
                    public void OnZoomed(double zoomFactor) {
                    }

                    @Override
                    public void OnScrollbarVisibilityChanged(boolean horizontalScrollbarVisible, boolean verticalScrollbarVisible) {

                    }
                });
            }

            return imageViewer;
        } catch (HttpServerException e) {
            Logger.getLogger().throwRuntimeException(e);
            return null;
        }
    }

    public GuiTable getTable() {
        return objectGrid.getTable();
    }

    public boolean isPictureSelectedForCorrection() {
        return !getSelectedPictures().isEmpty();
    }

    public List<iPartsDataPicOrderPicture> getSelectedPictures() {
        if (currSelectedPictures != null) {
            return currSelectedPictures;
        }
        loadPicturesAndPool();
        return currSelectedPictures;
    }

    public List<iPartsDataPicOrderPicture> getNotSelectedPictures() {
        if (currNotSelectedPictures != null) {
            return currNotSelectedPictures;
        }
        loadPicturesAndPool();
        return currNotSelectedPictures;
    }

    public List<List<EtkDataObject>> getSelectedPicturesWithAllDataObjects() {
        if (currSelectedPicturesWithPool != null) {
            return currSelectedPicturesWithPool;
        }
        loadPicturesAndPool();
        return currSelectedPicturesWithPool;
    }

    private void loadPicturesAndPool() {
        int rowCount = getTable().getRowCount();
        List<List<EtkDataObject>> picturesWithPool = new ArrayList<>();
        List<iPartsDataPicOrderPicture> pictures = new ArrayList<>();
        List<iPartsDataPicOrderPicture> notSelectedPictures = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            GuiTableRow row = getTable().getRow(i);
            Object object = row.getChildForColumn(row.size() - 1);
            if (object instanceof iPartsGuiPanelWithCheckbox) {
                DataObjectGrid.GuiTableRowWithObjects rowWithAttributes = (DataObjectGrid.GuiTableRowWithObjects)row;
                boolean isSelected = ((iPartsGuiPanelWithCheckbox)object).isSelected();
                if (isSelected) {
                    picturesWithPool.add(rowWithAttributes.dataObjects);
                }
                for (EtkDataObject dataObject : rowWithAttributes.dataObjects) {
                    if (dataObject.getAsId().getType().equals(iPartsPicOrderPictureId.TYPE)) {
                        iPartsDataPicOrderPicture picOrderPicture = (iPartsDataPicOrderPicture)dataObject;
                        if (isSelected) {
                            pictures.add(picOrderPicture);
                        } else {
                            notSelectedPictures.add(picOrderPicture);
                        }
                    }
                }

            }

        }
        currSelectedPicturesWithPool = picturesWithPool;
        currSelectedPictures = pictures;
        currNotSelectedPictures = notSelectedPictures;
    }

    /**
     * Erstellt die "Accept" Event Nachricht, die an AS-PLM geschickt wird
     *
     * @return
     */
    public iPartsXMLMediaMessage getAcceptingMessage() {
        iPartsXMLAcceptMediaContainer acceptMediaContainer = new iPartsXMLAcceptMediaContainer(dataPicOrder.getOrderIdExtern(), dataPicOrder.getOrderRevisionExtern());
        return iPartsPicOrderEditHelper.createMessageFromOperation(acceptMediaContainer, dataPicOrder.getAsId().getOrderGuid(), null);
    }

    /**
     * Erstellt die Korrektur-Anfrage, die an AS-PLM geschickt wird
     *
     * @return
     */
    public iPartsXMLMediaMessage getRequestCorrectionMessage() {
        iPartsXMLCorrectMediaOrder como = new iPartsXMLCorrectMediaOrder(dataPicOrder.getOrderIdExtern(), dataPicOrder.getOrderRevisionExtern());
        como.setReason(new iPartsXMLReason(reasonTextArea.getText()));
        iPartsXMLContractor contractor = createASPLMContractor();
        if (contractor == null) {
            return null;
        }
        como.setContractor(contractor);
        como.setDateDue(mainWindow.calendar_dateDue.getDate());
        if (!allPicturesSelected()) {
            for (List<EtkDataObject> dataObjectList : getSelectedPicturesWithAllDataObjects()) {
                iPartsDataPicOrderPicture dataPicOrderPicture = null;
                EtkDataPool pool = null;
                for (EtkDataObject object : dataObjectList) {
                    if (object instanceof iPartsDataPicOrderPicture) {
                        dataPicOrderPicture = (iPartsDataPicOrderPicture)object;
                    } else if (object instanceof EtkDataPool) {
                        pool = (EtkDataPool)object;
                    }
                }
                if ((dataPicOrderPicture == null) || (pool == null)) {
                    continue;
                }
                iPartsXMLMediaVariant variant = dataPicOrderPicture.getAsMediaVariant(pool.getImgLanguage());
                como.addMediaVariant(variant);
            }
        }
        return iPartsPicOrderEditHelper.createMessageFromOperation(como, dataPicOrder.getAsId().getOrderGuid(), null);
    }


    /**
     * Erstellt ein {@link iPartsXMLContractor} Objekt und aktualisiert im dataPicOrder Objekt den Bearbeiter (Group und/oder user)
     *
     * @return
     */
    private iPartsXMLContractor createASPLMContractor() {
        // Setze zuerst die neuen Werte
        String groupGuid = contractorForm.getSelectedGroupGuid();
        if (groupGuid == null) {
            // Kann eigentlich nicht sein, da der Button sonst nicht aktiv wäre
            MessageDialog.showError(TranslationHandler.translate("!!Auftragnehmer Gruppe darf nicht leer sein!"), mainWindow.getTitle());
            return null;
        }
        dataPicOrder.setAttributeValue(FIELD_DA_PO_JOB_GROUP, groupGuid, DBActionOrigin.FROM_EDIT);
        String userGuid = contractorForm.getSelectedUserGuid();
        // Ist die userGuid leer, muss der User am Bildauftrag entfernt werden (Es wurde nur eine Gruppe gesetzt)
        if (userGuid == null) {
            userGuid = "";
        }
        dataPicOrder.setAttributeValue(FIELD_DA_PO_JOB_USER, userGuid, DBActionOrigin.FROM_EDIT);
        // Contractor mit neuen Werten zusammenbauen lassen und zurückgeben
        return dataPicOrder.getASPLMContractor();

    }

    private boolean allPicturesSelected() {
        return getSelectedPictures().size() == dataPicOrder.getPictures().size();
    }

    private class PictureDataObjectGrid extends DataObjectFilterGrid {

        public PictureDataObjectGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            doSelectionChanged(event);
        }

        protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
            String value = super.getVisualValueOfField(tableName, fieldName, objectForTable);
            if ((objectForTable != null) && fieldName.equals(FIELD_P_USAGE) && tableName.equals(TABLE_POOL)) {
                value = getUITranslationHandler().getText(ImageVariant.usageToImageVariant(objectForTable.getFieldValue(fieldName, getProject().getDBLanguage(), false)).getDisplayText());
                value = DatatypeUtils.addHtmlTags(EC.jhsnbsp(value), true);
            }
            return value;
        }

        @Override
        protected GuiTableRowWithObjects createRow(List<EtkDataObject> dataObjects) {
            GuiTableRowWithObjects row = new GuiTableRowWithObjects(dataObjects);

            for (EtkDisplayField field : displayFields.getFields()) {
                if (field.isVisible()) {
                    String fieldName = field.getKey().getFieldName();
                    String tableName = field.getKey().getTableName();
                    // Welches Object ist für diese Tabelle zuständig?
                    EtkDataObject objectForTable = row.getObjectForTable(tableName);
                    if (tableName.equals(COL_CHECKBOX_TABLENAME) && fieldName.equals(COL_CHECKBOX_FIELDNAME)) {
                        OnChangeEvent onChangeEvent = null;
                        boolean isEnabled = iPartsTransferStates.canRequestCorrection(dataPicOrder.getStatus()) || onlyForChangeOrder;
                        if (isEnabled) {
                            onChangeEvent = () -> {
                                currSelectedPictures = null;
                                currSelectedPicturesWithPool = null;
                                currNotSelectedPictures = null;
                                doEnableButtons();
                            };
                        }
                        iPartsGuiPanelWithCheckbox checkbox = new iPartsGuiPanelWithCheckbox(objectForTable.getFieldValueAsBoolean(FIELD_DA_POP_USED),
                                                                                             onChangeEvent);
                        checkbox.setEnabled(isEnabled);
                        row.addChild(checkbox, checkbox::getTextRepresentation);
                    } else {
                        String value;
                        value = getVisualValueOfField(tableName, fieldName, objectForTable);
                        GuiLabel label = new GuiLabel(value);
                        row.addChild(label);
                    }
                }
            }
            return row;
        }
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
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
        private de.docware.framework.modules.gui.controls.GuiSplitPane mainSplitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainSplitPane_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_picList;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_correction;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_reason;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_reason;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpane_reason;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_dateDue;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.calendar.GuiCalendar calendar_dateDue;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_contractor;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mainSplitPane_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(1200);
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            mainSplitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            mainSplitPane.setName("mainSplitPane");
            mainSplitPane.__internal_setGenerationDpi(96);
            mainSplitPane.registerTranslationHandler(translationHandler);
            mainSplitPane.setScaleForResolution(true);
            mainSplitPane.setMinimumWidth(10);
            mainSplitPane.setMinimumHeight(10);
            mainSplitPane.setDividerPosition(491);
            mainSplitPane.setDividerSize(10);
            mainSplitPane_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainSplitPane_firstChild.setName("mainSplitPane_firstChild");
            mainSplitPane_firstChild.__internal_setGenerationDpi(96);
            mainSplitPane_firstChild.registerTranslationHandler(translationHandler);
            mainSplitPane_firstChild.setScaleForResolution(true);
            mainSplitPane_firstChild.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutGridBag mainSplitPane_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mainSplitPane_firstChild.setLayout(mainSplitPane_firstChildLayout);
            panel_picList = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_picList.setName("panel_picList");
            panel_picList.__internal_setGenerationDpi(96);
            panel_picList.registerTranslationHandler(translationHandler);
            panel_picList.setScaleForResolution(true);
            panel_picList.setMinimumWidth(10);
            panel_picList.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_picListLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_picList.setLayout(panel_picListLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_picListConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            panel_picList.setConstraints(panel_picListConstraints);
            mainSplitPane_firstChild.addChild(panel_picList);
            panel_correction = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_correction.setName("panel_correction");
            panel_correction.__internal_setGenerationDpi(96);
            panel_correction.registerTranslationHandler(translationHandler);
            panel_correction.setScaleForResolution(true);
            panel_correction.setMinimumWidth(10);
            panel_correction.setMinimumHeight(10);
            panel_correction.setTitle("!!Korrekturanfrage - bitte ausfüllen");
            de.docware.framework.modules.gui.layout.LayoutGridBag panel_correctionLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panel_correction.setLayout(panel_correctionLayout);
            panel_reason = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_reason.setName("panel_reason");
            panel_reason.__internal_setGenerationDpi(96);
            panel_reason.registerTranslationHandler(translationHandler);
            panel_reason.setScaleForResolution(true);
            panel_reason.setMinimumWidth(10);
            panel_reason.setMinimumHeight(10);
            panel_reason.setTitle("!!Informationen zur Korrektur");
            de.docware.framework.modules.gui.layout.LayoutGridBag panel_reasonLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panel_reasonLayout.setCentered(false);
            panel_reason.setLayout(panel_reasonLayout);
            label_reason = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_reason.setName("label_reason");
            label_reason.__internal_setGenerationDpi(96);
            label_reason.registerTranslationHandler(translationHandler);
            label_reason.setScaleForResolution(true);
            label_reason.setMinimumWidth(10);
            label_reason.setMinimumHeight(10);
            label_reason.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            label_reason.setText("!!Grund:");
            label_reason.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_reasonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "ne", "n", 4, 8, 4, 4);
            label_reason.setConstraints(label_reasonConstraints);
            panel_reason.addChild(label_reason);
            scrollpane_reason = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpane_reason.setName("scrollpane_reason");
            scrollpane_reason.__internal_setGenerationDpi(96);
            scrollpane_reason.registerTranslationHandler(translationHandler);
            scrollpane_reason.setScaleForResolution(true);
            scrollpane_reason.setMinimumWidth(10);
            scrollpane_reason.setMinimumHeight(10);
            scrollpane_reason.setBorderWidth(1);
            scrollpane_reason.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag scrollpane_reasonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 100.0, "c", "b", 4, 4, 4, 8);
            scrollpane_reason.setConstraints(scrollpane_reasonConstraints);
            panel_reason.addChild(scrollpane_reason);
            label_dateDue = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_dateDue.setName("label_dateDue");
            label_dateDue.__internal_setGenerationDpi(96);
            label_dateDue.registerTranslationHandler(translationHandler);
            label_dateDue.setScaleForResolution(true);
            label_dateDue.setMinimumWidth(10);
            label_dateDue.setMinimumHeight(10);
            label_dateDue.setText("!!Gew. Fertigstellungsdatum:");
            label_dateDue.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_dateDueConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 8, 4);
            label_dateDue.setConstraints(label_dateDueConstraints);
            panel_reason.addChild(label_dateDue);
            calendar_dateDue = new de.docware.framework.modules.gui.controls.calendar.GuiCalendar();
            calendar_dateDue.setName("calendar_dateDue");
            calendar_dateDue.__internal_setGenerationDpi(96);
            calendar_dateDue.registerTranslationHandler(translationHandler);
            calendar_dateDue.setScaleForResolution(true);
            calendar_dateDue.setMinimumWidth(10);
            calendar_dateDue.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag calendar_dateDueConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "w", "n", 4, 4, 8, 8);
            calendar_dateDue.setConstraints(calendar_dateDueConstraints);
            panel_reason.addChild(calendar_dateDue);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_reasonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 100.0, "c", "b", 8, 8, 4, 8);
            panel_reason.setConstraints(panel_reasonConstraints);
            panel_correction.addChild(panel_reason);
            panel_contractor = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_contractor.setName("panel_contractor");
            panel_contractor.__internal_setGenerationDpi(96);
            panel_contractor.registerTranslationHandler(translationHandler);
            panel_contractor.setScaleForResolution(true);
            panel_contractor.setMinimumWidth(10);
            panel_contractor.setMinimumHeight(20);
            panel_contractor.setPaddingTop(4);
            panel_contractor.setPaddingLeft(8);
            panel_contractor.setPaddingRight(8);
            panel_contractor.setPaddingBottom(8);
            panel_contractor.setTitle("!!Bearbeiter");
            de.docware.framework.modules.gui.layout.LayoutBorder panel_contractorLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_contractor.setLayout(panel_contractorLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_contractorConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 0.0, "c", "b", 4, 8, 4, 8);
            panel_contractor.setConstraints(panel_contractorConstraints);
            panel_correction.addChild(panel_contractor);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_correctionConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 100.0, 0.0, "c", "b", 0, 0, 0, 0);
            panel_correction.setConstraints(panel_correctionConstraints);
            mainSplitPane_firstChild.addChild(panel_correction);
            mainSplitPane.addChild(mainSplitPane_firstChild);
            mainSplitPane_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainSplitPane_secondChild.setName("mainSplitPane_secondChild");
            mainSplitPane_secondChild.__internal_setGenerationDpi(96);
            mainSplitPane_secondChild.registerTranslationHandler(translationHandler);
            mainSplitPane_secondChild.setScaleForResolution(true);
            mainSplitPane_secondChild.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutBorder mainSplitPane_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainSplitPane_secondChild.setLayout(mainSplitPane_secondChildLayout);
            mainSplitPane.addChild(mainSplitPane_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainSplitPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainSplitPane.setConstraints(mainSplitPaneConstraints);
            panelMain.addChild(mainSplitPane);
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
                    doSendMQMessage(event);
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