/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.images.iPartsDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControlsForColorTableAndContentFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.CopyAndPasteData;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;
import java.util.Map;

/**
 * Formular für die Werkseinsatzdaten für einen Farbtabelleninhalt.
 */
public class iPartsVariantFactoryDataForm extends AbstractVariantFactoryDataForm implements iPartsConst {

    public static final String CONFIG_KEY_VARIANT_FACTORY_DATA = "Plugin/iPartsEdit/VariantFactoryData";
    public static final String CONFIG_KEY_VARIANT_FACTORY_DATA_AS = "Plugin/iPartsEdit/VariantFactoryDataAfterSales";

    private iPartsColorTableContentId currentColorTableContentId;
    private Map<Integer, iPartsColorTableContentId> allVariants;
    private int currentIndex;
    private GuiButtonOnPanel previousButton;
    private GuiButtonOnPanel nextButton;
    private GuiLabel variantsLabel;

    public iPartsVariantFactoryDataForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                        EtkDataPartListEntry partListEntry, int selectedIndex, Map<Integer, iPartsColorTableContentId> allVariants,
                                        iPartsSeriesId filterSeriesId, boolean enableEditMode, boolean retailFilter) {
        super(dataConnector, parentForm, CONFIG_KEY_VARIANT_FACTORY_DATA_AS + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              CONFIG_KEY_VARIANT_FACTORY_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              partListEntry, filterSeriesId, enableEditMode, retailFilter);
        this.currentIndex = selectedIndex;
        this.allVariants = allVariants;
        addBrowseButtons();
        setReadOnly(isRetailFilter() || !(editMode && isEditContext(dataConnector, true)));
        createCopyPasteToolbarbuttons();
        setCurrentContentObject();
    }

    /**
     * Lädt die Werksdaten zum aktuellen {@link iPartsDataColorTableContent} Objekt und zeigt sie an
     */
    private void setCurrentContentObject() {
        iPartsColorTableContentId contentId = allVariants.get(currentIndex);
        Session.invokeThreadSafeInSession(() -> {
            if (contentId != null) {
                String factoryDataText = TranslationHandler.translate("!!Werkseinsatzdaten zu %1", contentId.getColorTableId());
                iPartsDataColorTableContent colorTableContent = new iPartsDataColorTableContent(getProject(), contentId);
                String varText = colorTableContent.getDisplayValue(FIELD_DCTC_COLOR_VAR, getProject().getDBLanguage());
                String posText = colorTableContent.getDisplayValue(FIELD_DCTC_POS, getProject().getDBLanguage());
                String dateFromText = colorTableContent.getDisplayValue(FIELD_DCTC_SDATA, getProject().getDBLanguage());
                setWindowTitle(factoryDataText, factoryDataText + ' ' + TranslationHandler.translate("!!für Variante \"%1\", Position \"%2\" und Datum ab \"%3\"", varText, posText, dateFromText));
                currentColorTableContentId = contentId;
                variantsLabel.setText(TranslationHandler.translate("!!%1 von %2", String.valueOf(currentIndex + 1), String.valueOf(allVariants.size())));
                getWindow().repaint();
            }
            enableBrowseButtons();
            enableButtonsAndMenu();
            dataToGrid();
        });
    }

    /**
     * Zeigt die Daten zur nächsten Variante an
     */
    private void showNextVariant() {
        if (currentIndex < (allVariants.size() - 1)) {
            currentIndex++;
        }
        setCurrentContentObject();
    }

    /**
     * Zeigt die Daten zur vorherigen Variante an
     */
    private void showPreviousVariant() {
        if (currentIndex > 0) {
            currentIndex--;
        }
        setCurrentContentObject();
    }

    /**
     * Aktiviert bzw. Deaktiviert die Buttons zum Navigieren durch alle Varianten
     */
    private void enableBrowseButtons() {
        if (allVariants.isEmpty()) {
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            return;
        }
        previousButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < (allVariants.size() - 1));

    }

    /**
     * Fügt die Buttons zum Navigieren durch alle Varianten hinzu
     */
    private void addBrowseButtons() {
        previousButton = getButtonPanel().addCustomButton("!!Vorheriger Eintrag");
        previousButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                showPreviousVariant();
            }
        });
        previousButton.setIcon(iPartsDefaultImages.edit_btn_up.getImage());

        nextButton = getButtonPanel().addCustomButton("!!Nächster Eintrag");
        nextButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                showNextVariant();
            }
        });
        nextButton.setIcon(iPartsDefaultImages.edit_btn_down.getImage());

        // Das Label soll direkt neben die Buttons
        AbstractConstraints constraints = nextButton.getConstraints();
        AbstractGuiControl parent = nextButton.getParent();
        if ((constraints instanceof ConstraintsGridBag) && (parent instanceof GuiPanel)) {
            ConstraintsGridBag constraintsGB = (ConstraintsGridBag)constraints;
            variantsLabel = new GuiLabel();
            ((GuiPanel)parent).addChildGridBag(variantsLabel, constraintsGB.getGridx() + 1, constraintsGB.getGridy(),
                                               1, 1, 0, 0,
                                               ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                               8, 8, 4, 8);
        }
    }

    public int getSelectedIndex() {
        return currentIndex;
    }

    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
        iPartsDataColorTableFactoryList factoryDataList;
        if (top) {
            if (isRetailFilter()) { // Anstatt Werkseinsatzdaten After-Sales die Daten verdichtet aus den Datenstrukturen vom Stücklisteneintrag anzeigen
                iPartsDataColorTableFactoryList colorContentFactoryDataListForRetail = ColorTableHelper.getColorContentFactoryDataListForRetail(getPartListEntry(), currentColorTableContentId,
                                                                                                                                                getFilterSeriesId(), getProject(), responseDataForPemRetailFilter);
                addDataObjectListToGrid(top, colorContentFactoryDataListForRetail);
                return;
            } else {
                factoryDataList = iPartsDataColorTableFactoryList.loadColorTableFactoryListForColorTableContentIdForAS(getProject(),
                                                                                                                       currentColorTableContentId);
                // ungefilterte Daten zum Kopieren merken
                listOfUnfilteredFactoryData.addAll(factoryDataList.getAsList());
            }
        } else {
            // Werkseinsatzdaten Konstruktion
            factoryDataList = iPartsDataColorTableFactoryList.loadColorTableFactoryListForColorTableContentId(getProject(),
                                                                                                              currentColorTableContentId);
            listOfUnfilteredFactoryData.addAll(factoryDataList.getAsList());
        }
        // Werksdaten filtern
        filterFactoryData(factoryDataList);
        addVirtualFieldsForResponseData(factoryDataList);
        iPartsDataColorTableFactoryList result = new iPartsDataColorTableFactoryList();
        result.addAll(factoryDataList, DBActionOrigin.FROM_DB);
        addDataObjectListToGrid(top, result);
        enableButtonsAndMenu();
    }

    @Override
    protected void doNew(boolean top) {
        iPartsDataColorTableFactory result = EditUserControlsForColorTableAndContentFactoryData.showCreateColorTableContentFactoryData(
                getConnector(), this, currentColorTableContentId);
        if (result != null) {
            // handelt eventuelle Statusänderungen ab inkl. dem speichern des aktuellen Eintrags im Changeset
            statusChangedForDataObject(result, top);
        }
    }

    @Override
    protected void doEdit(boolean top) {
        iPartsDataColorTableFactory selectedColorTableFactory = getColorTableFactoryFromSelection(top);
        if (selectedColorTableFactory != null) {
            iPartsDataColorTableFactory result = EditUserControlsForColorTableAndContentFactoryData.showCreateColorTableContentFactoryData(
                    getConnector(), this, currentColorTableContentId,
                    selectedColorTableFactory.getAsId());
            if (result != null) {
                // bei Statusänderungen diese Abhandeln und den aktuellen Eintrag im Changeset speichern
                if (selectedColorTableFactory.getReleaseState() != result.getReleaseState()) {
                    statusChangedForDataObject(result, top);
                } else {
                    saveDataObjectWithUpdate(result);
                }
            }
        }
    }

    @Override
    protected boolean isEditable() {
        return editMode && !isReadOnly;
    }

    @Override
    public List<iPartsDataDIALOGChange> getDataDIALOGChanges(EtkDataObject dataObject) {
        List<iPartsDataDIALOGChange> result = new DwList<>();
        if (isRelatedInfoEditContext() && (dataObject instanceof iPartsDataColorTableFactory)) {
            iPartsDataColorTableFactory factoryData = (iPartsDataColorTableFactory)dataObject;
            iPartsColorTableDataId colorTableDataId = new iPartsColorTableDataId(factoryData.getAsId().getTableId());
            iPartsDataColorTableData colorTableData = new iPartsDataColorTableData(getProject(), colorTableDataId);
            iPartsDataColorTableToPartList colorTableToPartList = iPartsDataColorTableToPartList.loadColorTableToPartListForColortableId(getProject(), colorTableDataId);
            for (iPartsDataColorTableToPart colorTableToPart : colorTableToPartList) {
                // Für jedes Teil muss ein Eintrag in DA_DIALOG_CHANGES angelegt werden, sodass auch registriert wird, wenn derselbe
                // Datensatz von einem anderen Stücklisteneintrag mit einem anderen Teil aus geändert wurde.
                iPartsDialogChangesId dialogChangesId = new iPartsDialogChangesId(iPartsDataDIALOGChange.ChangeType.COLORTABLE_FACTORY_DATA,
                                                                                  factoryData.getAsId(), colorTableData.getValidSeries(),
                                                                                  "", colorTableToPart.getPartNumber(), "");
                result.add(new iPartsDataDIALOGChange(getProject(), dialogChangesId));
            }
        }
        return result;
    }

    @Override
    protected void pasteFactoryData() {
        iPartsDataColorTableFactoryList results = CopyAndPasteData.pasteFactoryDataOfColortableContent(getProject(), currentColorTableContentId);
        if (results != null) {
            // bei Statusänderungen diese Abhandeln und den aktuellen Eintrag im Changeset speichern
            // Werkseinsatzdaten werden als AS-Werkseinsatzdaten eingefügt
            statusChangedForDataObjects(results, true);
        }
    }
}
