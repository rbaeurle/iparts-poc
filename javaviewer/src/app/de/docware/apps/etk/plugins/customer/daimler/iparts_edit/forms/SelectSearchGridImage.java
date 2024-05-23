/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.drawing.EtkImageSettings;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.events.OnNewEvent;
import de.docware.apps.etk.base.mechanic.imageview.forms.AssemblyImageForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataHotspot;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.drawing.ImageVariant;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsValidityScope;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.framework.modules.config.db.datatypes.DatatypeUtils;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.viewer.*;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.http.server.HttpServerException;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.j2ee.EC;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/*
 * Suchformular für Suche in Bildern
 */
public class SelectSearchGridImage extends AbstractJavaViewerForm implements iPartsConst {

    public static EtkDisplayFields createImageGridFields(EtkProject project) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_IMAGES, FIELD_I_TIFFNAME, "!!Modulnummer", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_IMAGES, FIELD_I_VER, "!!Modulversion", false, false, false));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_IMAGES, FIELD_I_BLATT, "!!Blattnummer", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_IMAGES, FIELD_I_IMAGES, "!!Zeichnungsnummer", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_IMAGES, FIELD_I_PVER, "!!Zeichnungsversion", false, false, false));
        return displayResultFields;
    }

    public static EtkDisplayFields createPoolGridFields(EtkProject project) {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_POOL, FIELD_P_IMAGES, "!!Zeichnungsnummer", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_POOL, FIELD_P_VER, "!!Zeichnungsversion", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_POOL, FIELD_P_SPRACH, "!!Sprache", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_POOL, FIELD_P_USAGE, "!!Verwendung", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_POOL, FIELD_P_IMGTYPE, "!!Zeichnungstyp", false, false, true));
        displayResultFields.addFeld(EtkDisplayFieldsHelper.createField(project, TABLE_POOL, FIELD_P_VALIDITY_SCOPE, "!!Gültigkeitsbereich", false, false, false));

        return displayResultFields;
    }

    private static class SelectSearchGridImagePool extends SimpleSelectSearchResultGrid {

        private final boolean isCarAndVan = iPartsRight.checkCarAndVanInSession();
        private final boolean isTruckAndBus = iPartsRight.checkTruckAndBusInSession();

        public SelectSearchGridImagePool(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm, TABLE_POOL, FIELD_P_IMAGES);
            setAutoSelectSingleSearchResult(true);
            setDisplayResultFields(createPoolGridFields(dataConnector.getProject()));
            // Sortierung
            LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
            sortFields.put(FIELD_P_IMAGES, false);
            sortFields.put(FIELD_P_VER, false);
            setSortFields(sortFields);
        }

        @Override
        protected String getVisualValueOfFieldValue(String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
            if (fieldName.equals(FIELD_P_USAGE) && searchTable.equals(TABLE_POOL)) {
                String result = getUITranslationHandler().getText(ImageVariant.usageToImageVariant(fieldValue.getAsString()).getDisplayText());
                return DatatypeUtils.addHtmlTags(EC.jhsnbsp(result), true);
            } else {
                return super.getVisualValueOfFieldValue(fieldName, fieldValue, isMultiLanguage);
            }
        }

        @Override
        protected EtkSqlCommonDbSelect buildQuery(String searchValue) {
            EtkSqlCommonDbSelect dbSelect = super.buildQuery(searchValue);
            // Bilder mit der Gültigkeitsbereich "UNUSED" oder leer sollen nicht angezeigt werden
            Condition condition = new Condition(TableAndFieldName.make(TABLE_POOL, FIELD_P_VALIDITY_SCOPE), Condition.OPERATOR_NOT_EQUALS,
                                                iPartsValidityScope.UNUSED.getScopeKey());
            dbSelect.getQuery().and(condition);
            condition = new Condition(TableAndFieldName.make(TABLE_POOL, FIELD_P_VALIDITY_SCOPE), Condition.OPERATOR_NOT_EQUALS, "");
            dbSelect.getQuery().and(condition);

            // Join zur PicOrder_Pictures Tabelle (Verknüpfung Bild zu Bildauftrag)
            Condition firstPicOrderPicJoinCond = new Condition(TableAndFieldName.make(TABLE_POOL, FIELD_P_IMAGES), Condition.OPERATOR_EQUALS,
                                                               new Fields(TableAndFieldName.make(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_PIC_ITEMID)));
            Condition secondPicOrderPicJoinCond = new Condition(TableAndFieldName.make(TABLE_POOL, FIELD_P_VER), Condition.OPERATOR_EQUALS,
                                                                new Fields(TableAndFieldName.make(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_PIC_ITEMREVID)));
            AbstractCondition joinCondition = firstPicOrderPicJoinCond.and(secondPicOrderPicJoinCond);
            dbSelect.getQuery().join(new LeftOuterJoin(TABLE_DA_PICORDER_PICTURES, joinCondition));

            // Join zur Picorder Tabelle -> enthält den Status des Bildauftrags
            joinCondition = new Condition(TableAndFieldName.make(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_GUID), "=",
                                          new Fields(TableAndFieldName.make(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_ORDER_GUID)));
            dbSelect.getQuery().join(new LeftOuterJoin(TABLE_DA_PICORDER, joinCondition));

            // Nur selektieren, wenn der der Bildauftrag abgeschlossen ist oder das Bild keine Referenz zu Bildaufträgen
            // hat (alle Bilder, die NICHT über den Bildauftrag in die DB gelangt sind)
            List<AbstractCondition> list = new ArrayList<>();
            list.add(Condition.isNull(TableAndFieldName.make(TABLE_DA_PICORDER_PICTURES, FIELD_DA_POP_PIC_ITEMREVID)));

            // Alle gültigen Endzustände für Bildaufträge berücksichtigen
            for (iPartsTransferStates validEndState : iPartsTransferStates.VALID_END_STATES) {
                list.add(new Condition(TableAndFieldName.make(TABLE_DA_PICORDER, FIELD_DA_PO_STATUS),
                                       Condition.OPERATOR_EQUALS, validEndState.getDBValue()));
            }

            ConditionList conditionList = new ConditionList(list, true);
            dbSelect.getQuery().and(conditionList);

            return dbSelect;
        }

        @Override
        protected boolean doValidAttributes(DBDataObjectAttributes attributes) {
            String scope = attributes.getFieldValue(FIELD_P_VALIDITY_SCOPE);
            return iPartsValidityScope.isScopeValid(scope, isCarAndVan, isTruckAndBus);
        }


        public void endSearch() { // Ist in der Superklasse nur protected
            super.endSearch();
        }
    }

    private SelectSearchGridImagePool searcher;
    private EtkImageSettings imageSettings;
    private GuiViewerImageInterface imgViewer;
    private EtkDataImage imageData;
    private GuiPanel panel;
    private AbstractGuiControl imageViewerCenterControl;
    private AssemblyId assemblyId;
    protected GuiButtonOnPanel buttonNew;
    protected OnNewEvent onNewEvent = null;


    public static DBDataObjectAttributesList searchImage(AbstractJavaViewerForm parentForm, AssemblyId assemblyId,
                                                         String title, boolean multiSelect, OnNewEvent onNewEvent) {
        AbstractJavaViewerFormIConnector dataConnector = parentForm.getConnector();
        SelectSearchGridImage dlg = new SelectSearchGridImage(dataConnector, parentForm);
        dlg.setMultiSelect(multiSelect);
        dlg.setAssemblyId(assemblyId);
        dlg.setOnNewEvent(onNewEvent);
        dlg.setTitle(title);
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getSelectedAttributesList();
        }
        return null;
    }


    /**
     * Erzeugt eine Instanz von SelectSearchGridImage.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public SelectSearchGridImage(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        mainWindow.setWidth((int)(screenSize.getWidth() * 0.8));
        mainWindow.setHeight((int)(screenSize.getHeight() * 0.8));
        mainWindow.mainSplitPane.setDividerPosition((int)(mainWindow.getWidth() * 0.4));
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, false);
        EditModuleFormConnector editConnector = new EditModuleFormConnector(getConnector());
        addOwnConnector(editConnector);

        // SearchGrid erzeugen und aufschnappen
        searcher = new SelectSearchGridImagePool(editConnector, this);
        searcher.setMaxResults(J2EEHandler.isJ2EE() ? iPartsConst.MAX_IMAGE_SEARCH_RESULTS_SIZE : -1);
        searcher.getGui().removeFromParent();
        ConstraintsBorder constraints = new ConstraintsBorder();
        constraints.setPosition(ConstraintsBorder.POSITION_CENTER);
        searcher.getGui().setConstraints(constraints);
        searcher.setMultiSelect(true);

        // Events beim SearchGrid setzen
        // Grid Selection changed
        searcher.setOnChangeEvent(this::doSelectionChanged);
        // Grid DoubleClick
        searcher.setOnDblClickEvent(() -> mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).doClick());
        // SearchGrid aufschnappen und sichtbar machen
        mainWindow.mainSplitPane_firstChild.addChild(searcher.getGui());

        // Picture Panel erzeugen und einhängen
        panel = new GuiPanel();
        panel.setBackgroundColor(Colors.clWhite.getColor());
        panel.setLayout(new LayoutBorder());
        constraints = new ConstraintsBorder();
        constraints.setPosition("center");
        panel.setConstraints(constraints);
        mainWindow.mainSplitPane_secondChild.addChild(panel);
        buttonNew = addCustomButton("!!Neu");
        buttonNew.setVisible(false);
        buttonNew.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
            public void fire(Event event) {
                buttonNewActionPerformed(event);
            }
        });

        searcher.requestFocusForSearchValue();
    }

    protected void doSelectionChanged() {
        DBDataObjectAttributesList selectedAttributeList = searcher.getSelectedAttributesList();
        boolean enableOK = (selectedAttributeList != null);
        if (enableOK) {
            if (selectedAttributeList.size() == 1) {
                DBDataObjectAttributes selectedAttributes = selectedAttributeList.get(0);
                updateImageWindow(selectedAttributes);
            } else {
                hideImageWindow();
            }
            enableOK = !selectedAttributeList.isEmpty();
        } else {
            hideImageWindow();
        }
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, enableOK);
    }

    public void setOnNewEvent(OnNewEvent onNewEvent) {
        this.onNewEvent = onNewEvent;
        if (onNewEvent != null) {
            buttonNew.setVisible(true);
            String buttonText = onNewEvent.getButtonText();
            if (buttonText != null) {
                buttonNew.setText(buttonText);
            }
        } else {
            buttonNew.setVisible(false);
        }
    }

    private void buttonNewActionPerformed(Event event) {
        if (onNewEvent != null) {
            searcher.endSearch();
            String newValue = onNewEvent.onNewEvent("");
            if (StrUtils.isEmpty(newValue)) {
                mainWindow.setModalResult(ModalResult.ABORT);
                mainWindow.setVisible(false);
            }
        }
    }

    /**
     * @param buttonText
     * @return
     */
    protected GuiButtonOnPanel addCustomButton(String buttonText) {
        return mainWindow.buttonPanel.addCustomButton(buttonText);
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

    private void updateImageWindow(DBDataObjectAttributes selectedAttributes) {
        // Normale Anzeige eines Bildes
        hideImageWindow();

        // Usage aus den Attributen des selektierten Pool-Datensatzes ermitteln und explizit die 2D Pixelgrafik anfordern
        // bei leerer Usage (SVGs haben SVG als Usage)
        String imgUsage = selectedAttributes.getFieldValue(FIELD_P_USAGE);
        if (imgUsage.isEmpty()) {
            imgUsage = EtkDataImage.IMAGE_USAGE_2D_FILLED;
        }

        int newImageIndex = -1;
        int imageIndex = 0;
        imgViewer = createImageViewer(imageIndex, selectedAttributes, imgUsage);
        AbstractGuiControl imageViewerGui;
        if (imgViewer != null) {
            imageViewerGui = imgViewer.getGui();
            if (imgViewer instanceof GuiViewerImage) {
                ((GuiViewerImage)imgViewer).adjustEditNotesMenuIdForPanWindow();
            }

            imageViewerGui.setConstraints(new ConstraintsBorder(LayoutBorder.POSITION_CENTER));
            imageViewerCenterControl = imageViewerGui;
            if (imageData != null) {
                EtkDataPool imageVariant = imageData.getBestImageVariant(getProject().getDBLanguage(), imgUsage);
                if (imageVariant != null) {
                    fillHotspots(getProject(), imgViewer, imageVariant);
                }
            }
            panel.addChild(imageViewerGui);

            newImageIndex = imageIndex /*getConnector().getImageIndex()*/;
        }

        panel.setVisible(newImageIndex >= 0);
    }

    private EtkDataImage loadImage(DBDataObjectAttributes selectedAttributes) {
        //Simulation für EtkDataImage
        String img = selectedAttributes.getField(FIELD_P_IMAGES).getAsString();
        String imgVer = selectedAttributes.getField(FIELD_P_VER).getAsString();
        String blatt = EtkDbsHelper.formatLfdNr(1);
        return EtkDataObjectFactory.createDataImage(getProject(), assemblyId, blatt, img, imgVer);
    }

    protected GuiViewerImageInterface createImageViewer(final int imageIndex, DBDataObjectAttributes selectedAttributes, String imgUsage) {
        if (imageIndex < 0) {
            return null;
        }
        imageData = loadImage(selectedAttributes);
        EtkDataPool variant = imageData.getBestImageVariant(getProject().getDBLanguage(), imgUsage);
        GuiViewerImageInterface imageViewer = null;
        if (variant != null) {
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
                    imageViewer.setData(imgData, imgType, GuiViewerImageInterface.MAX_NUMBER_OF_PIXELS_UNLIMITED, true);
                    imageViewer.display();
                    if (imageViewer instanceof AbstractImageViewer3D) {
                        ((AbstractImageViewer3D)imageViewer).assignSettings(getImageSettings().getImageCommonSettings(),
                                                                            getImageSettings().getImageHotspotSettings(),
                                                                            getImageSettings().getImageSecuritySettings());
                    }
                } else {
                    imageViewer = null;
                }
            }
        }
        return imageViewer;
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
            return GuiViewer.getImageViewerForFilename("dummy." + extension, imageIndex, false,
                                                       getProject().isDocumentDecryptionNecessary());
        } catch (HttpServerException e) {
            Logger.getLogger().handleRuntimeException(e);
            return null;
        }
    }

    /**
     * Befüllt den übergebenen <i>imageViewer</i> mit den Hotspots aus der Zeichnung <i>image</i>.
     *
     * @param project
     * @param imageViewer
     * @param imageVariant
     */
    public static void fillHotspots(EtkProject project, GuiViewerImageInterface imageViewer, EtkDataPool imageVariant) {
        EtkEbenenDaten ebene = project.getConfig().getPartsDescription().getEbene("");

        imageViewer.resetLinks();

        if (imageVariant != null) {
            for (EtkDataHotspot hotspot : imageVariant.getHotspots()) {
                Rectangle linkRect = new Rectangle(hotspot.getLeft(), hotspot.getTop(), hotspot.getRight() - hotspot.getLeft() + 1,
                                                   hotspot.getBottom() - hotspot.getTop() + 1);
                linkRect = imageViewer.modifyLinkRect(linkRect, hotspot.getKey(), hotspot.getKeyVer());
                GuiViewerLink link = AssemblyImageForm.createLinkWithDefaultColorsAndStyle(hotspot.getKey(), hotspot.getKeyVer(),
                                                                                           hotspot.getKey(), hotspot.getHotspotType(),
                                                                                           hotspot.getExtInfo(), linkRect,
                                                                                           ebene);

                imageViewer.addLink(link);
            }
        }
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
        if (searcher != null) {
            searcher.dispose();
        }
        super.dispose();
    }


    public String getSearchTable() {
        return searcher.getSearchTable();
    }

    public String getSearchColumn() {
        return searcher.getSearchColumn();
    }

    public EtkDisplayFields getDisplayResultFields() {
        return searcher.getDisplayResultFields();
    }

    public void setDisplayResultFields(EtkDisplayFields displayResultFields) {
        searcher.setDisplayResultFields(displayResultFields);
    }

    public boolean isMultiSelect() {
        return searcher.isMultiSelect();
    }

    public void setMultiSelect(boolean value) {
        searcher.setMultiSelect(value);
    }

    public DBDataObjectAttributes getSelectedAttributes() {
        return searcher.getSelectedAttributes();
    }

    public DBDataObjectAttributesList getSelectedAttributesList() {
        return searcher.getSelectedAttributesList();
    }

    public AssemblyId getAssemblyId() {
        return assemblyId;
    }

    public void setAssemblyId(AssemblyId assemblyId) {
        this.assemblyId = assemblyId;
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
            mainSplitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            mainSplitPane.setName("mainSplitPane");
            mainSplitPane.__internal_setGenerationDpi(96);
            mainSplitPane.registerTranslationHandler(translationHandler);
            mainSplitPane.setScaleForResolution(true);
            mainSplitPane.setMinimumWidth(10);
            mainSplitPane.setMinimumHeight(10);
            mainSplitPane.setDividerPosition(488);
            mainSplitPane.setDividerSize(10);
            mainSplitPane_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainSplitPane_firstChild.setName("mainSplitPane_firstChild");
            mainSplitPane_firstChild.__internal_setGenerationDpi(96);
            mainSplitPane_firstChild.registerTranslationHandler(translationHandler);
            mainSplitPane_firstChild.setScaleForResolution(true);
            mainSplitPane_firstChild.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutBorder mainSplitPane_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainSplitPane_firstChild.setLayout(mainSplitPane_firstChildLayout);
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