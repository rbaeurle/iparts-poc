/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EnumComboBox;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelProperties;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPSKProductVariant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.TransmissionIdentKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UserControl für Product-Edit
 */
public class EditUserControlForProduct extends EditUserControlForCreate implements iPartsConst {

    private static final Set<String> ALLOWED_EMPTY_FIELDS = new HashSet<>(Arrays.asList(FIELD_DP_VALID_COUNTRIES,
                                                                                        FIELD_DP_INVALID_COUNTRIES,
                                                                                        iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES,
                                                                                        FIELD_DP_DISABLED_FILTERS));

    private Map<String, Object> specialFields;
    private boolean isNewForm;
    private GuiPanel panelForModelsForm;
    private EditConnectModelsForm modelsForm;
    private List<iPartsModelId> modelIds;

    /**
     * Entfernt alle PSK-Doku-Methoden aus der übergebenen {@link EnumRComboBox} mit Doku-Methoden-Enum.
     *
     * @param docuMethodComboBox
     */
    public static void removePSKDocuMethodsFromComboBox(EnumRComboBox docuMethodComboBox) {
        for (iPartsDocumentationType documentationType : iPartsDocumentationType.values()) {
            if (documentationType.isPSKDocumentationType()) {
                int pskDocuTypeIndex = docuMethodComboBox.getIndexByToken(documentationType.getDBValue());
                if (pskDocuTypeIndex != -1) {
                    docuMethodComboBox.removeItem(pskDocuTypeIndex);
                }
            }
        }
    }

    public EditUserControlForProduct(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                     IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                     boolean isNewForm) {
        super(dataConnector, parentForm, TABLE_DA_PRODUCT, id, attributes, externalEditFields);
        init(isNewForm);
    }

    /**
     * Initialisiert das Control
     *
     * @param isNewForm
     */
    private void init(boolean isNewForm) {
        this.isNewForm = isNewForm;
        // Das Textfeld für die Varianten verhält sich bei einer Neuanlage anders als bei einem existierenden Produkt
        AbstractGuiControl editVariantsControl = getEditGuiControlByFieldName(iPartsDataVirtualFieldsDefinition.DP_VARIANTS);
        if (editVariantsControl instanceof iPartsGuiProductVariantsSelectTextField) {
            ((iPartsGuiProductVariantsSelectTextField)editVariantsControl).setForProductCreation(isNewForm);
        }
    }

    public Set<String> getFactoryNumbers() {
        for (EditControl editControl : editControls) {
            if (editControl.getEditControl().getFieldName().equals(iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES)) {
                if (editControl.getEditControl().getControl() instanceof iPartsGuiFactorySelectTextField) {
                    return ((iPartsGuiFactorySelectTextField)editControl.getEditControl().getControl()).getSelectedFactoryNumbers();
                }
            }
        }

        return new TreeSet<>();
    }

    /**
     * Liefert ein Set mit allen IDs aller vorhandenen Produktvarianten
     *
     * @return
     */
    public Set<String> getProductVariantsValuesAsString() {
        if (iPartsRight.checkPSKInSession()) {
            EditControl editControl = getEditControlByTableAndFieldName(TABLE_DA_PRODUCT, iPartsDataVirtualFieldsDefinition.DP_VARIANTS);
            if (editControl != null) {
                AbstractGuiControl control = editControl.getEditControl().getControl();
                if (control instanceof iPartsGuiProductVariantsSelectTextField) {
                    Collection<iPartsDataPSKProductVariant> allVariantsFromControl = ((iPartsGuiProductVariantsSelectTextField)control).getSelectedVariants();
                    if ((allVariantsFromControl != null) && !allVariantsFromControl.isEmpty()) {
                        return allVariantsFromControl.stream().map(productVariant -> productVariant.getAsId().getVariantId()).collect(Collectors.toCollection(TreeSet::new));
                    }
                }
            }
        }
        return new TreeSet<>();
    }

    public List<iPartsModelId> getConnectedModelIds() {
        return modelsForm.getModelIds();
    }

    public void setConnectedModelIds(List<iPartsModelId> modelIds) {
        modelsForm.switchOffEventListeners();
        try {
            this.modelIds = modelIds;
            modelsForm.setModelIds(modelIds);
            if (modelIds != null) {
                modelsForm.setProductCharacteristics(getCurrentProductProperties());
            }
        } finally {
            modelsForm.switchOnEventListeners();
        }
    }

    public List<iPartsDataModel> getModifiedASDataModels() {
        return modelsForm.getModifiedASDataModels();
    }

    public List<iPartsDataModelProperties> getModifiedConstDataModels() {
        return modelsForm.getModifiedConstDataModels();
    }

    @Override
    public ModalResult showModal() {
        // damit sich bei Edit alles einpendelt
        doDocuMethodChanged(null);
        return super.showModal();
    }

    @Override
    protected void postCreateGui() {
        AbstractGuiControl panelGrid = getGui();
        removeChildFromPanelMain(panelGrid);
        AbstractConstraints panelGridConstraints = panelGrid.getConstraints();
        panelGrid.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));

        modelsForm = new EditConnectModelsForm(getConnector(), this, new iPartsProductId(id.getValue(1)));
        modelsForm.setPanelTitle("!!Verknüpfung");
        modelsForm.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doModelsChanged(event);
            }
        });
        panelForModelsForm = modelsForm.getGui();

        GuiPanel panelGridMaster = new GuiPanel();
        panelGridMaster.setLayout(new LayoutBorder());
        panelGridMaster.addChild(panelGrid);
        panelGridMaster.addChildBorderSouth(panelForModelsForm);

        panelGridMaster.setConstraints(panelGridConstraints);
        addChildToPanelMain(panelGridMaster);

        super.postCreateGui();
        setHeight(getPanelEditFields().getPreferredHeight() + panelForModelsForm.getPreferredHeight() + 120);

        // Callbacks für PSK-Flag, Doku-Methode und 'referenzierte Baureihe' setzen
        EditControl editControlForPSK = getEditControlByFieldName(FIELD_DP_PSK);
        if (editControlForPSK != null) {
            editControlForPSK.getAbstractGuiControl().addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    doPSKChanged(event);
                }
            });
        }

        EditControl editControlForReferencedSeries = getEditControlByFieldName(FIELD_DP_SERIES_REF);
        if (editControlForReferencedSeries != null) {
            editControlForReferencedSeries.getAbstractGuiControl().addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    doRefSeriesChanged(event);
                }
            });
        }

        EditControl editControlForDocuMethod = getEditControlByFieldName(FIELD_DP_DOCU_METHOD);
        if (editControlForDocuMethod != null) {
            editControlForDocuMethod.getAbstractGuiControl().addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    doDocuMethodChanged(event);
                }
            });
        }

        EditControl editControlForProductName = getEditControlByFieldName(FIELD_DP_PRODUCT_NO);
        if (editControlForProductName != null) {
            editControlForProductName.getAbstractGuiControl().addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    initProductRelevantControls();
                }
            });
        }

        EditControl editControlForASProductClasses = getEditControlByFieldName(FIELD_DP_ASPRODUCT_CLASSES);
        if (editControlForASProductClasses != null) {
            editControlForASProductClasses.getAbstractGuiControl().addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    // Hat sich die AS Produktklasse geändert, müssen auch die aktuellen Produkteigenschaften gesetzt werden
                    modelsForm.setProductCharacteristics(getCurrentProductProperties());
                }
            });
        }

        // Varianten sind ein MUSS Feld, wenn aktiv
        EditControl editControlVariants = getEditControlByFieldName(iPartsDataVirtualFieldsDefinition.DP_VARIANTS);
        if (editControlVariants != null) {
            editControlVariants.getLabel().setFontStyle(DWFontStyle.BOLD);
        }

        doPSKChanged(null);
        // Nach dem befüllen des Dialogs die aktuellen Produkteigenschaften setzen
        modelsForm.setProductCharacteristics(getCurrentProductProperties());
    }

    /**
     * Erzeugt aus den aktuellen Produktinforamtionen die dazugehörigen Produkteigenschaften
     *
     * @return
     */
    private MasterDataProductCharacteristics getCurrentProductProperties() {
        DBDataObjectAttribute attribute = getCurrentAttributeValue(FIELD_DP_ASPRODUCT_CLASSES);
        Set<String> asProductClasses = new LinkedHashSet<>(SetOfEnumDataType.parseSetofEnum(attribute.getAsString(),
                                                                                            false, false));
        boolean isCarAndVanProduct = asProductClasses.stream().anyMatch(iPartsProduct.AS_PRODUCT_CLASSES_CAR_AND_VAN::contains);
        boolean isTruckAndBusProduct = asProductClasses.stream().anyMatch(iPartsProduct.AS_PRODUCT_CLASSES_TRUCK_AND_BUS::contains);
        return new MasterDataProductCharacteristics(isCarAndVanProduct, isTruckAndBusProduct);
    }

    private String getProductNrFromControl() {
        EditControl editControlForProductName = getEditControlByFieldName(FIELD_DP_PRODUCT_NO);
        if (editControlForProductName != null) {
            return editControlForProductName.getText();
        }
        return "";
    }

    private void initProductRelevantControls() {
        String productNr = getProductNrFromControl();
        EditControl editControlForVariants = getEditControlByFieldName(iPartsDataVirtualFieldsDefinition.DP_VARIANTS);
        if (editControlForVariants != null) {
            AbstractGuiControl abstractGuiControl = editControlForVariants.getAbstractGuiControl();
            if (abstractGuiControl instanceof iPartsGuiProductVariantsSelectTextField) {
                ((iPartsGuiProductVariantsSelectTextField)abstractGuiControl).init(this, new iPartsProductId(productNr));
            }
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        modelsForm.setReadOnly(readOnly);
        if (specialFields != null) {
            EditPicturePreviewForm pictureSelect = (EditPicturePreviewForm)specialFields.get(FIELD_DP_PICTURE);
            if (pictureSelect != null) {
                pictureSelect.setReadOnly(readOnly);
            }
        }
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        // Bei EnumComboBoxen in Produkten 20 Einträge anzeigen
        AbstractGuiControl editControl = ctrl.getEditControl().getControl();
        if (editControl instanceof EnumComboBox) {
            ((EnumComboBox)editControl).setMaximumRowCount(20);
        }
        if (editControl instanceof EnumRComboBox) {
            ((EnumRComboBox)editControl).setMaximumRowCount(20);
        }

        if (specialFields == null) {
            specialFields = new LinkedHashMap<>();
            specialFields.put(FIELD_DP_PICTURE, null);
        }
        String fieldName = field.getKey().getFieldName();
        if (specialFields.containsKey(fieldName)) {
            Object obj = specialFields.get(fieldName);
            if (fieldName.equals(FIELD_DP_PICTURE)) {
                EditPicturePreviewForm pictureSelect;
                if (obj == null) {
                    pictureSelect = new EditPicturePreviewForm(getConnector(), this, true);
                    pictureSelect.setShowSelectButton(true);
                    specialFields.put(FIELD_DP_PICTURE, pictureSelect);
                } else {
                    pictureSelect = (EditPicturePreviewForm)obj;
                }
                AbstractGuiControl guiControl = pictureSelect.getGui();
                guiControl.setMinimumHeight(100);
                if (!J2EEHandler.isJ2EE()) {
                    guiControl.setMaximumWidth(editControl.getPreferredWidth());
                }
                guiControl.removeFromParent();
                ctrl.getEditControl().setControl(guiControl);
                pictureSelect.setPictureIds(initialValue);
            }
        } else {
            // Brand soll als Default "MB" haben und darf auch nicht leer sein, deshalb hier den leeren Eintrag entfernen
            // die Vorbesetzung erfolgt bei NEW im OnCreateAttributesEvent
            if (fieldName.equals(FIELD_DP_BRAND)) {
                if (editControl instanceof EnumComboBox) {
                    EnumComboBox enumComboBox = (EnumComboBox)editControl;
                    if (enumComboBox.getIndexOfItem("") > -1) {
                        enumComboBox.removeItem("");
                    }
                } else if (editControl instanceof EnumRComboBox) {
                    // Kopierter Part, der wieder entfernt werden kann, wenn GuiCombobox und RCombobox zusammengeführt wurden
                    EnumRComboBox enumComboBox = (EnumRComboBox)editControl;
                    if (enumComboBox.getIndexOfItem("") > -1) {
                        enumComboBox.removeItem("");
                    }
                }
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES) || fieldName.equals(iPartsDataVirtualFieldsDefinition.DP_VARIANTS)) {
                if (editControl instanceof AbstractProductRelatedButtonTextField) {
                    AbstractProductRelatedButtonTextField productRelatedButtonTextField = (AbstractProductRelatedButtonTextField)editControl;
                    productRelatedButtonTextField.init(this, getProductIdFromId());
                }
            } else if (fieldName.equals(iPartsConst.FIELD_DP_FINS)) {
                if (editControl instanceof iPartsGuiFinSelectTextField) {
                    iPartsGuiFinSelectTextField finField = (iPartsGuiFinSelectTextField)editControl;
                    finField.init(this, modelsForm);
                    finField.setProductId(getProductIdFromId());
                    if (initialDataArray != null) {
                        finField.setArray(initialDataArray);
                    }
                }
            }
        }
    }

    /**
     * Setzten der übergebenen (gültigen) AS Produktklassen
     *
     * @param editControl
     * @param validASProductClasses
     * @param isCarAndVanInSession
     */
    private void setValidProductClasses(AbstractGuiControl editControl, Set<String> validASProductClasses, boolean isCarAndVanInSession) {
        if (editControl instanceof EnumComboBox) {
            EnumComboBox enumComboBox = (EnumComboBox)editControl;
            // Selektion merken
            String[] selectedItems = enumComboBox.getSelectedItems();
            // Alle Einträge entfernen um sie eventuell neu zu laden (es können ja welche fehlen, wenn das PSK Flag hin und her geklickt wird)
            enumComboBox.removeAllItems();
            if (isCarAndVanInSession || iPartsRight.checkTruckAndBusInSession()) { // Wenn beides "false" -> Keine Eigenschaft vorhanden
                enumComboBox.setEnumTexte(getProject(), TABLE_DA_PRODUCT, FIELD_DP_ASPRODUCT_CLASSES, getProject().getDBLanguage(), true);
                for (Object userObject : new ArrayList<>(enumComboBox.getUserObjects())) { // getUserObjects() ist bei EnumComboBox die Original-Liste
                    if (!validASProductClasses.contains(userObject.toString())) {
                        enumComboBox.removeItemByUserObject(userObject);
                    }
                }
                // Selektion setzen
                enumComboBox.setSelectedItems(selectedItems);
            }
        } else if (editControl instanceof EnumRComboBox) {
            // Kopierter Part, der wieder entfernt werden kann, wenn GuiCombobox und RCombobox zusammengeführt wurden
            EnumRComboBox enumComboBox = (EnumRComboBox)editControl;
            // Selektion merken
            String[] selectedItems = enumComboBox.getSelectedItems();
            // Alle Einträge entfernen um sie eventuell neu zu laden (es können ja welche fehlen, wenn das PSK Flag hin und her geklickt wird)
            enumComboBox.removeAllItems();
            if (isCarAndVanInSession || iPartsRight.checkTruckAndBusInSession()) { // Wenn beides "false" -> Keine Eigenschaft vorhanden
                enumComboBox.setEnumTexte(getProject(), TABLE_DA_PRODUCT, FIELD_DP_ASPRODUCT_CLASSES, getProject().getDBLanguage(), true);
                for (String userObject : enumComboBox.getUserObjects()) { // getUserObjects() ist bei EnumRComboBox sowieso eine temporäre Liste
                    if (!validASProductClasses.contains(userObject)) {
                        enumComboBox.removeItemByUserObject(userObject);
                    }
                }
                // Selektion setzen
                enumComboBox.setSelectedItems(selectedItems);
            }
        }
    }

    /**
     * Liefert die gültigen AS Produktklassen in Abhängigkeit von PSK Eigenschaft, PKW/van und Truck/Bus Eigenschaften
     * und dem aktuellen Zustand des PSK Flags in den Stammdaten
     *
     * @param isCarAndVanInSession
     * @param isPSKActive
     * @return
     */
    private Set<String> getValidASProductClasses(boolean isCarAndVanInSession, boolean isPSKActive) {
        Set<String> validASProductClasses;
        if (iPartsRight.checkPSKInSession() && isPSKActive) {
            // Benutzer hat PSK Rechte -> Alle Produktklassen anzeigen
            validASProductClasses = new LinkedHashSet<>(iPartsProduct.AS_PRODUCT_CLASSES_CAR_AND_VAN);
            validASProductClasses.addAll(iPartsProduct.AS_PRODUCT_CLASSES_TRUCK_AND_BUS);
        } else {
            // Bei nicht-PSK entweder PKW/Van ODER Truck/Bus
            validASProductClasses = isCarAndVanInSession ? iPartsProduct.AS_PRODUCT_CLASSES_CAR_AND_VAN
                                                         : iPartsProduct.AS_PRODUCT_CLASSES_TRUCK_AND_BUS;
        }
        return validASProductClasses;
    }

    private iPartsProductId getProductIdFromId() {
        if ((this.id != null) && (this.id.getIdLength() > 0)) {
            return new iPartsProductId(this.id.getValue(1));
        }
        return null;
    }

    @Override
    protected void fillAttribByEditControlValue(int index, EtkEditField field, DBDataObjectAttribute attrib) {
        String fieldName = field.getKey().getFieldName();
        if (fieldName.equals(FIELD_DP_PICTURE)) {
            EditPicturePreviewForm pictureSelect = (EditPicturePreviewForm)specialFields.get(fieldName);
            attrib.setValueAsString(pictureSelect.getPictureIds(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        } else if (fieldName.equals(FIELD_DP_SERIES_REF)) {
            super.fillAttribByEditControlValue(index, field, attrib);
            String seriesRefNumber = attrib.getAsString();
            if (!seriesRefNumber.isEmpty()) {
                modelsForm.setPartialModelNumberWithWildCard(seriesRefNumber + "*");
            } else {
                modelsForm.setPartialModelNumberWithWildCard(null);
            }
        } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES) || fieldName.equals(iPartsDataVirtualFieldsDefinition.DP_VARIANTS)) {
            // Normalerweise würde hier der Inhalt des Textfield als Attribut eingetragen werden. Das ist aber bei
            // "Werke zu Produkt" und "Varianten zu Produkt" nicht erwünscht. Hier werden Änderungen im Textfield verwaltet.
            attrib.setValueAsNull(true, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        } else {
            super.fillAttribByEditControlValue(index, field, attrib);
        }
    }

    @Override
    public DBDataObjectAttributes getAttributes() {
        // Hier wird das Feld für "Werke zu Produkt" und "Varianten zu Produkt" wieder entfernt, weil es kein echtes Feld der Tabelle DA_PRODUCT
        // ist, und deshalb auch nicht beim Speichern mit verwendet werden darf.
        DBDataObjectAttributes attributes = super.getAttributes();
        removeAttributeIfExists(attributes, iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES);
        removeAttributeIfExists(attributes, iPartsDataVirtualFieldsDefinition.DP_VARIANTS);
        return attributes;
    }

    private void removeAttributeIfExists(DBDataObjectAttributes attributes, String fieldname) {
        attributes.remove(fieldname);
    }

    private void doPSKChanged(Event event) {
        if (!isReadOnly()) {
            // Falls das PSK-Attribut gar nicht vorhanden oder false ist, müssen alle PSK-Dokumethoden entfernt werden
            // und das Control für die Verwaltung von Varianten darf nicht sichtbar sein
            DBDataObjectAttribute attribute = getCurrentAttributeValue(FIELD_DP_PSK);
            boolean showPSKControls = (attribute != null) && attribute.getAsBoolean();
            handleDocuMethodAfterPSKChange(showPSKControls);
            handleASProductClassesAfterPSKChange(showPSKControls);
            // Das Varianten-zu-Produkt Control verschwinden lassen, wenn das Häkchen nicht gesetzt ist
            EditControl editControlVariants = getEditControlByFieldName(iPartsDataVirtualFieldsDefinition.DP_VARIANTS);
            if (editControlVariants != null) {
                boolean wasVisible = editControlVariants.getAbstractGuiControl().isVisible();
                editControlVariants.getAbstractGuiControl().setVisible(showPSKControls);
                editControlVariants.getLabel().setVisible(showPSKControls);
                initProductRelevantControls();
                // Ändert sich der Wert, muss hier die Größe angepasst werden
                if (wasVisible != showPSKControls) {
                    int newOverallSize = getPanelEditFields().getPreferredHeight() + panelForModelsForm.getPreferredHeight() + 120;
                    Dimension screenSize = FrameworkUtils.getScreenSize();
                    // Ist die neue Höhe (inkl. Puffer) kleiner als der Bildschirm, dann vergrößere nur das Panel mit den Edits
                    if (screenSize.getHeight() <= (newOverallSize + 40)) {
                        getPanelEditFields().setMinimumHeight(getPanelEditFields().getPreferredHeight());
                    } else {
                        setHeight(newOverallSize);
                    }
                }
            }
        }
    }

    /**
     * Setzt die gültigen AS Produktklassen in Abhängigkeit des PSK Flags in den Stammdaten
     *
     * @param isPSKActive
     */
    private void handleASProductClassesAfterPSKChange(boolean isPSKActive) {
        if (!iPartsRight.checkUserHasBothVehicleTypeRightsInSession()) {
            EditControl editControl = getEditControlByFieldName(FIELD_DP_ASPRODUCT_CLASSES);
            boolean isCarAndVanInSession = iPartsRight.checkCarAndVanInSession();
            Set<String> validASProductClasses = getValidASProductClasses(isCarAndVanInSession, isPSKActive);
            setValidProductClasses(editControl.getAbstractGuiControl(), validASProductClasses, isCarAndVanInSession);
        }
    }

    /**
     * Setzt die gültigen Dokumentationsmethoden in Abhängigkeit des PSK Flags ind en Stammdaten
     *
     * @param showPSKControls
     */
    private void handleDocuMethodAfterPSKChange(boolean showPSKControls) {
        EditControl editControlForDocuMethod = getEditControlByFieldName(FIELD_DP_DOCU_METHOD);
        if (editControlForDocuMethod != null) {
            AbstractGuiControl control = editControlForDocuMethod.getAbstractGuiControl();
            if (control instanceof EnumRComboBox) {
                EnumRComboBox docuMethodComboBox = (EnumRComboBox)control;
                docuMethodComboBox.setMaximumRowCount(20);

                if (!showPSKControls) { // PSK-Dokumethoden ausblenden
                    removePSKDocuMethodsFromComboBox(docuMethodComboBox);
                } else { // Alle Enum-Werte für die Dokumethoden inkl. PSK anzeigen
                    String oldToken = docuMethodComboBox.getActToken();
                    docuMethodComboBox.setEnumTexte(getProject(), TABLE_DA_PRODUCT, FIELD_DP_DOCU_METHOD, getProject().getDBLanguage(),
                                                    false);
                    docuMethodComboBox.setActToken(oldToken);
                }
            }
        }
    }

    /**
     * Liefert alle varianten zum Produkt, die verändert wurden (neue oder bearbeitete)
     *
     * @return
     */
    public Collection<iPartsDataPSKProductVariant> getModifiedProductVariants() {
        if (iPartsRight.checkPSKInSession()) {
            EditControl editControlVariants = getEditControlByFieldName(iPartsDataVirtualFieldsDefinition.DP_VARIANTS);
            if (editControlVariants.getAbstractGuiControl() instanceof iPartsGuiProductVariantsSelectTextField) {
                Collection<iPartsDataPSKProductVariant> result = ((iPartsGuiProductVariantsSelectTextField)editControlVariants.getAbstractGuiControl()).getModifiedVariantsObjects();
                if (isNewForm) {
                    String productNr = getProductNrFromControl();
                    if (StrUtils.isValid(productNr)) {
                        result.forEach(productVariant -> {
                            productVariant.setFieldValue(FIELD_DPPV_PRODUCT_NO, productNr, DBActionOrigin.FROM_EDIT);
                            productVariant.updateIdFromPrimaryKeys();
                            if (productVariant.isNew()) {
                                productVariant.updateOldId();
                            }
                        });
                    }
                }
                return result;
            }
        }
        return null;
    }

    private void doRefSeriesChanged(Event event) {
        DBDataObjectAttribute attrib = getCurrentAttributeValue(FIELD_DP_SERIES_REF);
        if (attrib == null) {
            return;
        }
        String seriesRefNumber = attrib.getAsString();
        if (!seriesRefNumber.isEmpty() && (seriesRefNumber.length() > 3)) {
            modelsForm.setPartialModelNumberWithWildCard(seriesRefNumber + "*");
        } else {
            modelsForm.setPartialModelNumberWithWildCard(null);
        }
    }

    private void doDocuMethodChanged(Event event) {
        if (!isReadOnly()) {
            iPartsDocumentationType docuType = getCurrentDocuType();
            modelsForm.setDocuType(docuType);
            switch (docuType) {
                case DIALOG:
                case DIALOG_IPARTS:
                    doRefSeriesChanged(event);
                    break;
            }

            // Lieferantennummer nur bei Truck anzeigen
            EditControl supplierNoControl = getEditControlByFieldName(FIELD_DP_SUPPLIER_NO);
            if (supplierNoControl != null) {
                supplierNoControl.setVisible(!docuType.isDIALOGDocumentationType() && (docuType != iPartsDocumentationType.UNKNOWN));
            }
            // Fahrzeugperspektive nur bei Car anzeigen
            EditControl carPerspectiveControl = getEditControlByFieldName(FIELD_DP_CAR_PERSPECTIVE);
            if (carPerspectiveControl != null) {
                boolean isVisible = docuType.isDIALOGDocumentationType() && (docuType != iPartsDocumentationType.UNKNOWN);
                boolean isEnabled = iPartsRight.CREATE_DELETE_CAR_PERSPECTIVE.checkRightInSession();
                carPerspectiveControl.setVisible(isVisible);
                carPerspectiveControl.getEditControl().getControl().setEnabled(isEnabled);
            }
        }
    }

    private iPartsDocumentationType getCurrentDocuType() {
        DBDataObjectAttribute attrib = getCurrentAttributeValue(FIELD_DP_DOCU_METHOD);
        if (attrib != null) {
            return iPartsDocumentationType.getFromDBValue(attrib.getAsString());
        }
        return iPartsDocumentationType.UNKNOWN;
    }

    private void doModelsChanged(Event event) {
        // DAIMLER-9768: Bei Doku-Methode DIALOG und Aggregatetyp GM/GA vom Produkt diesen Aggregatetyp an allen AS-Baumustern setzen
        DBDataObjectAttributes currentAttributes = getCurrentAttributes();
        if (currentAttributes != null) {
            // Doku-Methode vom Produkt bestimmen
            iPartsDocumentationType docuMethod;
            DBDataObjectAttribute docuMethodAttribute = currentAttributes.getField(FIELD_DP_DOCU_METHOD, false);
            if (docuMethodAttribute != null) {
                docuMethod = iPartsDocumentationType.getFromDBValue(docuMethodAttribute.getAsString());
            } else {
                docuMethod = iPartsProduct.getInstance(getProject(), modelsForm.getProductId()).getDocumentationType();
            }
            if (docuMethod.isPKWDocumentationType()) { // Muss DIALOG oder PSK_PKW sein
                // Aggregatetyp vom Produkt bestimmen
                String aggregateType;
                DBDataObjectAttribute aggregateTypeAttribute = currentAttributes.getField(FIELD_DP_AGGREGATE_TYPE, false);
                if (aggregateTypeAttribute != null) {
                    aggregateType = aggregateTypeAttribute.getAsString();
                } else {
                    aggregateType = iPartsProduct.getInstance(getProject(), modelsForm.getProductId()).getAggregateType();
                }

                // Bei GM/GA den Aggregatetyp an allen AS-Baumustern setzen
                if (Utils.objectEquals(aggregateType, TransmissionIdentKeys.TRANSMISSION_MECHANICAL) || Utils.objectEquals(aggregateType, TransmissionIdentKeys.TRANSMISSION_AUTOMATED)) {
                    for (iPartsDataModel asDataModel : modelsForm.getAllASDataModels()) {
                        asDataModel.setFieldValue(FIELD_DM_MODEL_TYPE, aggregateType, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
        // Hat sich der Auswahltyp geändert, müssen auch die aktuellen Produkteigenschaften gesetzt werden
        modelsForm.setProductCharacteristics(getCurrentProductProperties());
        doEnableButtons(event);
    }

    @Override
    protected void enableOKButton(boolean enabled) {
        boolean tempEnabled = enabled;
        if (tempEnabled) {
            // Ist es ein PSK Produkt, dann muss mind. eine Variante existieren
            DBDataObjectAttribute attribute = getCurrentAttributeValue(FIELD_DP_PSK);
            boolean showPSKControls = (attribute != null) && attribute.getAsBoolean();
            if (showPSKControls) {
                tempEnabled = !getProductVariantsValuesAsString().isEmpty();
            }
        }
        super.enableOKButton(tempEnabled);
    }

    @Override
    protected void doEnableButtons(Event event) {
        if (isNewForm) {
            super.doEnableButtons(event);
        } else {
            boolean enabled = checkForModified();
            enableOKButton(readOnly || enabled);
        }

        // "Nur gültig in Ländern" und "Nicht gültig in Ländern" schließen sich gegenseitig aus
        EditControl editControlForValidCountries = getEditControlByFieldName(FIELD_DP_VALID_COUNTRIES);
        EditControl editControlForInvalidCountries = getEditControlByFieldName(FIELD_DP_INVALID_COUNTRIES);
        // Sind beide EditControls vorhanden und Werte gesetzt, das jeweils andere EditControl disablen
        if ((editControlForValidCountries != null) && (editControlForInvalidCountries != null)) {
            if (isReadOnly()) {
                editControlForInvalidCountries.getEditControl().setReadOnly(true);
                editControlForValidCountries.getEditControl().setReadOnly(true);
            } else {
                EtkEditField invalidCountriesField = editFields.getFeldByName(TABLE_DA_PRODUCT, FIELD_DP_INVALID_COUNTRIES);
                editControlForInvalidCountries.getEditControl().setReadOnly(((invalidCountriesField != null) && !invalidCountriesField.isEditierbar())
                                                                            || (!editControlForValidCountries.getEditControl().getText().isEmpty()
                                                                                && editControlForInvalidCountries.getEditControl().getText().isEmpty()));
                EtkEditField validCountriesField = editFields.getFeldByName(TABLE_DA_PRODUCT, FIELD_DP_VALID_COUNTRIES);
                editControlForValidCountries.getEditControl().setReadOnly(((validCountriesField != null) && !validCountriesField.isEditierbar())
                                                                          || (!editControlForInvalidCountries.getEditControl().getText().isEmpty()
                                                                              && editControlForValidCountries.getEditControl().getText().isEmpty()));
            }
        }
    }

    private boolean areModelIdsModified() {
        boolean modified;
        List<iPartsModelId> selectedModelIds = getConnectedModelIds();
        if (!selectedModelIds.isEmpty()) {
            if (modelIds != null) {
                modified = modelIds.size() != selectedModelIds.size();
                if (!modified) {
                    modified = !modelIds.containsAll(selectedModelIds);
                }
            } else {
                modified = true;
            }
        } else {
            modified = ((modelIds != null) && !modelIds.isEmpty());
        }
        return modified;
    }

    @Override
    protected boolean isModified() {
        boolean isModified = super.isModified();
        if (!isModified) {
            isModified = areModelIdsModified();
        }
        if (!isModified) {
            for (EditControl editControl : editControls) {
                if (editControl.getEditControl().getFieldName().equals(iPartsDataVirtualFieldsDefinition.DP_VALID_FACTORIES) || editControl.getEditControl().getFieldName().equals(iPartsDataVirtualFieldsDefinition.DP_VARIANTS)) {
                    if (editControl.getEditControl().getControl() instanceof AbstractProductRelatedButtonTextField) {
                        isModified = ((AbstractProductRelatedButtonTextField)editControl.getEditControl().getControl()).isModified();
                        if (isModified) {
                            break;
                        }
                    }
                }
            }
        }
        if (!isModified) {
            isModified = !getModifiedASDataModels().isEmpty();
        }
        return isModified;
    }

    @Override
    protected boolean checkForModified() {
        boolean modified = super.checkForModified();
        if (!modified) {
            modified = isModified();
        }

        return modified;
    }

    /**
     * Hier besteht die Möglichkeit zu überprüfen, welche Felder ausgefüllt
     * sind bzw sein sollten
     * Dabei sollten hier NICHT die attributes benutzt werden
     *
     * @return
     */
    @Override
    protected boolean checkCompletionOfFormValues() {
        if (attributes != null) {
            int index = 0;
            List<String> warnings = new DwList<>();
            for (EtkEditField field : editFields.getVisibleEditFields()) {
                String fieldName = field.getKey().getFieldName();
                DBDataObjectAttribute clonedAttribute = getCurrentAttribByEditControlValue(index, field);
                if ((clonedAttribute != null) && clonedAttribute.isEmpty() && !ALLOWED_EMPTY_FIELDS.contains(fieldName)) {
                    warnings.add("  " + TranslationHandler.translate(editControls.getControlByFeldIndex(index).getLabel().getText()));
                }
                index++;
            }
            if (getConnectedModelIds().isEmpty()) {
                warnings.add("  " + TranslationHandler.translate("!!Verknüpfung Baumuster"));
            }

            if (warnings.size() > 0) {
                String msg = "!!Die folgenden Felder sind nicht besetzt:";
                if (warnings.size() == 1) {
                    msg = "!!Das folgende Feld ist nicht besetzt:";
                }
                warnings.add(0, TranslationHandler.translate(msg));
                warnings.add("");
                warnings.add(TranslationHandler.translate("!!Trotzdem abspeichern?"));
                if (MessageDialog.showYesNo(StrUtils.stringListToString(warnings, "\n")) == ModalResult.NO) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ist das Feld ein Mussfeld?
     * Defaultverhalten ist die Feldkonfiguration. Es kann aber sein dass ein Verhalten erzwungen werden muss und die
     * Feldkonfiguration außer Kraft gesetzt werden muss.
     * Alternative zur Änderung der Mussfeld-Eigenschaft der Feldkonfig. per Code. Hier müsste man genau wissen ob man auf
     * einem Klon arbeitet oder die zentrale Feldkonfig. ändert. Eine globale Feldkonfig. über diesen Weg zu verändern, ist m.E. unschön.
     *
     * @param field
     * @return
     */
    @Override
    protected boolean isMandatoryField(EtkEditField field) {
        if (field.getKey().getFieldName().equals(FIELD_DP_BRAND)) {
            return true;
        }
        return super.isMandatoryField(field);
    }
}