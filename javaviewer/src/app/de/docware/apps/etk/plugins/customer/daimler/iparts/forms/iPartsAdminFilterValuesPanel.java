/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.forms.events.OnChangeSearchValueEvent;
import de.docware.apps.etk.base.forms.events.OnEnableButtonsEvent;
import de.docware.apps.etk.base.forms.events.OnValidateAttributesEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TwoGridValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.calendar.GuiCalendar;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class iPartsAdminFilterValuesPanel extends AbstractJavaViewerForm {

    private final static String PSK_VARIANTS_SELECT_TOOLTIP_TEXT = "!!Kein gültiges PSK-Produkt vorhanden";

    private iPartsModel currentModel;
    private RComboBox<DatacardIdentOrderElem> comboboxAggIdent;
    private iPartsGuiAggIdentTextField textFieldAggIdent;
    private GuiLabel labelAdditionalAggregateSNR;
    private GuiTextField textFieldAdditionalAggregateSNR;
    private GuiButtonTextField buttontextfieldModels;
    private iPartsGuiDIALOGTypesButtonTextField saaButtonTextField;
    private EnumRComboBox enumSteering;
    private EnumRComboBox enumGearBox;
    private GuiLabel labelSpringFields;
    private GuiButtonTextField textfieldPartForSpring;
    private EnumRComboBox enumPGroup;
    private iPartsGuiDIALOGTypesButtonTextField codeButtonTextField;
    private iPartsGuiEventSelectComboBox comboboxEvent;
    private RComboBox<String> comboboxFactory;
    private GuiLabel labelTechnicalApprovalDate;
    private GuiLabel labelDateOfTechnicalState;
    private GuiCalendar calendarTechnicalApprovalDate;
    private GuiCalendar calendarDateOfTechnicalState;
    private GuiLabel labelAggModels;
    private GuiLabel labelPSKVariants;
    private iPartsGuiDIALOGTypesButtonTextField aggregateModelsButtonTextField;
    private iPartsGuiPSKVariantsSelectTextField pskVariantsSelectTextField;
    private GuiLabel labelCountry;
    private iPartsGuiCountrySelectionBox countryCombobox;
    private GuiLabel labelSpecification;
    private GuiTextField specificationValue;
    private GuiLabel labelAutoProdSelect;
    private GuiLabel labelAutoProdSelectValues;
    private boolean isAutoSelectedProductsPSK = false;
    private GuiLabel labelExchangeAggregate;
    private GuiLabel labelExchangeAggregateValue;
    private GuiLabel labelEldasAggregateInDIALOGVehicleValue;
    private GuiButton buttonLoad;
    private GuiPanel panelVIN;
    private iPartsGuiAggIdentTextField textFieldVINIdent;
    private Color originalModelBackgroundColor = null;
    private boolean isViewing; // true wenn Anzeige Datenkarte; false wenn Anzeige Filterdialog
    private boolean hasChanged;
    private boolean ignoreAggIdentChangeEvent;
    private AbstractDataCard loadedDataCard;
    private SpringFields springFields;
    private OnEnableButtonsEvent onEnableButtonsEvent;
    private String lastLoadedDatacardNumber; // Für die Anzeige im Dialog
    private OnChangeEvent currentLoadEvent;

    /**
     * Erzeugt eine Instanz von iPartsAdminFilterValuesPanel.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsAdminFilterValuesPanel(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        this(dataConnector, parentForm, null, null, null);
    }

    public iPartsAdminFilterValuesPanel(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                        OnChangeEvent onLoadVehicleDataCardEvent, OnChangeEvent onLoadAggregateDataCardEvent,
                                        OnEnableButtonsEvent onEnableButtonsEvent) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.ignoreAggIdentChangeEvent = false;
        this.onEnableButtonsEvent = onEnableButtonsEvent;
        this.isViewing = false;
        this.lastLoadedDatacardNumber = "";
        postCreateGui(onLoadVehicleDataCardEvent, onLoadAggregateDataCardEvent);
        this.setLoadedDataCard(null);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(final OnChangeEvent onLoadVehicleDataCardEvent, final OnChangeEvent onLoadAggregateDataCardEvent) {
        int y = 0;
        // FIN
        comboboxAggIdent = new RComboBox<DatacardIdentOrderElem>();
        comboboxAggIdent.setConstraints(createLabelConstraints(y));
        comboboxAggIdent.setName("comboboxAggIdent");
        panelMain.panelFilterValues.addChild(comboboxAggIdent);
        fillAggIdentCombobox();
        comboboxAggIdent.setMinimumWidth(170);
        currentLoadEvent = onLoadVehicleDataCardEvent;
        comboboxAggIdent.addEventListener(new EventListener(de.docware.framework.modules.gui.event.Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                DatacardIdentOrderElem identOrderElem = comboboxAggIdent.getSelectedUserObject();
                if (identOrderElem != null) {
                    textFieldAggIdent.setIdent(identOrderElem);
                    identOrderElem.getAggregateIdent().fillFactoryCombobox(comboboxFactory);

                    if (!ignoreAggIdentChangeEvent) {
                        switch (identOrderElem.getType()) {
                            case VIN:
                            case FIN:
                                // Werte werden in beiden Fällen aus der Datenkarte geladen
                                clearDialog(true);
                                if (isLoadedDataCardValid() && isVehicleDatacardLoaded()) {
                                    initDialog((VehicleDataCard)getLoadedDataCard());
                                }
                                break;
                            case UNKNOWN:
                                identOrderElem.getAggregateIdent().fillFactoryCombobox(comboboxFactory);
                                break;
                            default:
                                break;
                        }
                    }
                    switch (identOrderElem.getType()) {
                        case FIN:
                        case VIN:
                            currentLoadEvent = onLoadVehicleDataCardEvent;
                            if (!ignoreAggIdentChangeEvent) {
                                if (!StrUtils.isEmpty(lastLoadedDatacardNumber)) {
                                    updateInfoText(getLoadedDataCard());
                                } else {
                                    updateInfoText(fillDataCard(new VehicleDataCard(true)));
                                }
                            }
                            if (buttonLoad != null) {
                                buttonLoad.setEnabled(true);
                            }
                            comboboxAggIdent.setEnabled(!isViewing());
                            break;
                        case UNKNOWN:
                            currentLoadEvent = onLoadVehicleDataCardEvent;
                            if (!ignoreAggIdentChangeEvent) {
                                updateInfoText(getLoadedDataCard());
                            }
                            if (buttonLoad != null) {
                                buttonLoad.setEnabled(false);
                            }
                            break;
                        default:
                            currentLoadEvent = onLoadAggregateDataCardEvent;
                            if (!ignoreAggIdentChangeEvent) {
                                updateInfoText(fillDataCard(new AggregateDataCard(true)));
                            }
                            if (buttonLoad != null) {
                                buttonLoad.setEnabled(!identOrderElem.getAggregateIdent().isEmpty());
                            }
                            comboboxAggIdent.setEnabled(false);
                            break;
                    }
                }
            }
        });

        GuiPanel panelIdent = new GuiPanel();
        panelIdent.setName("panelIdent");
        LayoutGridBag panelIdentLayout = new LayoutGridBag();
        panelIdent.setLayout(panelIdentLayout);

        textFieldAggIdent = new iPartsGuiAggIdentTextField(true);
        textFieldAggIdent.setName("textFieldAggIdent");
        ConstraintsGridBag constraints;
        constraints = createFieldConstraints(0, 1);
        constraints.setGridx(0);
        constraints.setWeightx(100.0);
        textFieldAggIdent.setConstraints(constraints);
        panelIdent.addChild(textFieldAggIdent);
        textFieldAggIdent.addEventListener(new EventListener(de.docware.framework.modules.gui.event.Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                // dauernde Updates bei setValue verhindern
                OnEnableButtonsEvent oldOnEnableButtonsEvent = onEnableButtonsEvent;
                onEnableButtonsEvent = null;
                try {
                    if (!ignoreAggIdentChangeEvent) {
                        checkAggIdentData(event);
                    }
                    ignoreAggIdentChangeEvent = false;
                } finally {
                    // nach der Aktion einmal direkt aufrufen
                    onEnableButtonsEvent = oldOnEnableButtonsEvent;
                    doOnEnableChangeEvent();
                }
            }
        });

        if (onLoadVehicleDataCardEvent != null) {
            buttonLoad = new GuiButton();
            buttonLoad.setName("buttonLoad");
            buttonLoad.__internal_setGenerationDpi(96);
            buttonLoad.registerTranslationHandler(getUITranslationHandler());
            buttonLoad.setScaleForResolution(true);
            buttonLoad.setMinimumWidth(100);
            buttonLoad.setMaximumHeight(21);
            buttonLoad.setMnemonicEnabled(true);
            buttonLoad.setText("!!Datenkarte laden...");
            //buttonLoad.setModalResult(ModalResult.NONE);
            buttonLoad.addEventListener(new EventListener(de.docware.framework.modules.gui.event.Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    if (currentLoadEvent != null) {
                        currentLoadEvent.onChange();
                    }
                }
            });
            constraints = createFieldConstraints(0, 1);
            constraints.setGridx(1);
            buttonLoad.setConstraints(constraints);
            panelIdent.addChild(buttonLoad);
        } else {
            panelVIN = new GuiPanel();
            constraints = createFieldConstraints(0, 1);
            constraints.setGridx(1);
            constraints.setInsetsLeft(0);
            constraints.setInsetsRight(0);
            panelVIN.setConstraints(constraints);
            panelVIN.setLayout(new LayoutGridBag());
            GuiLabel label = createLabel(0, "VIN", "!!VIN");
            constraints = (ConstraintsGridBag)label.getConstraints();
            constraints.setInsetsLeft(0);
            constraints.setInsetsTop(0);
            constraints.setInsetsBottom(0);
            label.setConstraints(constraints);
            panelVIN.addChild(label);
            textFieldVINIdent = new iPartsGuiAggIdentTextField(false);
            textFieldVINIdent.setEditable(false);
            constraints = createFieldConstraints(0, 1);
            constraints.setWeightx(100.0);
            constraints.setInsetsTop(0);
            constraints.setInsetsBottom(0);
            textFieldVINIdent.setConstraints(constraints);
            textFieldVINIdent.setName("textFieldVINIdent");
            textFieldVINIdent.setIdent(comboboxAggIdent.getUserObject(DatacardIdentOrderTypes.VIN.ordinal()));
            panelVIN.addChild(textFieldVINIdent);
            panelIdent.addChild(panelVIN);
        }

        ConstraintsGridBag panelIdentContraints = createFieldConstraints(y, 2);
        panelIdentContraints.setInsets(0, 0, 0, 0);
        panelIdent.setConstraints(panelIdentContraints);
        panelMain.panelFilterValues.addChild(panelIdent);

        y++;

        // Model
        GuiLabel label = createLabel(y, "Model", "!!Baumuster");
        panelMain.panelFilterValues.addChild(label);
        buttontextfieldModels = new GuiButtonTextField();
        buttontextfieldModels.setButtonVisible(true);
        buttontextfieldModels.setButtonText("...");

        constraints = createFieldConstraints(y, 2);
        constraints.setWeightx(100.0);
        buttontextfieldModels.setConstraints(constraints);
        buttontextfieldModels.setName("buttontextfieldModels");
        panelMain.panelFilterValues.addChild(buttontextfieldModels);
        buttontextfieldModels.addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                onModelSearchEvent(event);
            }
        });
        buttontextfieldModels.addEventListener(new EventListener(de.docware.framework.modules.gui.event.Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                // dauernde Updates bei setValue verhindern
                OnEnableButtonsEvent oldOnEnableButtonsEvent = onEnableButtonsEvent;
                onEnableButtonsEvent = null;
                try {
                    if (!getModelNo().isEmpty()) {
                        resetIdent(false);
                    }
                    handleDirectTranfer();
                    checkModelData();
                    if (isVehicleDatacardLoaded()) {
                        updateInfoText(fillDataCard(new VehicleDataCard(true)));
                    } else {
                        updateInfoText(fillDataCard(new AggregateDataCard(true)));
                    }
                } finally {
                    updateAutoProductSelectValues(false);
                    // nach der Aktion einmal direkt aufrufen
                    onEnableButtonsEvent = oldOnEnableButtonsEvent;
                    doOnEnableChangeEvent();
                    // Ereignis zurücksetzen, da für die neu ausgewählte Baureihe andere Ereignise gültig sind.
                    setEvent(null);
                }
            }
        });
        y++;

        labelAdditionalAggregateSNR = createLabel(y, "AggregateSNR", "!!Aggregate-Sachnummer");
        panelMain.panelFilterValues.addChild(labelAdditionalAggregateSNR);
        textFieldAdditionalAggregateSNR = new GuiTextField();
        textFieldAdditionalAggregateSNR.setConstraints(createFieldConstraints(y, 2));
        textFieldAdditionalAggregateSNR.setEditable(false);
        panelMain.panelFilterValues.addChild(textFieldAdditionalAggregateSNR);
        y++;

        EventListener enableChangeEventListener = new EventListener(de.docware.framework.modules.gui.event.Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doOnEnableChangeEvent();
            }
        };

        label = createLabel(y, "SAAs", "!!SAAs/BKs");
        panelMain.panelFilterValues.addChild(label);
        /* ButtonTextField für SAAs */
        saaButtonTextField = new iPartsGuiDIALOGTypesButtonTextField(parentForm, iPartsFilterGridForm.DIALOG_TYPES.SAA);
        saaButtonTextField.setEditable(false);
        saaButtonTextField.setConstraints(createFieldConstraints(y, 2));
        saaButtonTextField.setName("saaButtonTextField");
        panelMain.panelFilterValues.addChild(saaButtonTextField);
        saaButtonTextField.addEventListener(enableChangeEventListener);
        y++;

        int maxWidthForComboBoxes = 250;

        label = createLabel(y, "Steering", "!!Lenkung");
        panelMain.panelFilterValues.addChild(label);
        /* EnumComboBox für Steering */
        enumSteering = new EnumRComboBox();
        enumSteering.setMaximumWidth(maxWidthForComboBoxes);
        enumSteering.setEnumTexte(getProject(), EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_STEERING, getProject().getViewerLanguage(), false);
        enumSteering.setConstraints(createFieldConstraints(y, 1));
        enumSteering.setName("enumSteering");
        enumSteering.addEventListener(enableChangeEventListener);
        panelMain.panelFilterValues.addChild(enumSteering);
        y++;

        label = createLabel(y, "GearBox", "!!Getriebeart");
        panelMain.panelFilterValues.addChild(label);
        /* EnumCombox für GerBox */
        enumGearBox = new EnumRComboBox();
        enumGearBox.setMaximumWidth(maxWidthForComboBoxes);
        enumGearBox.setEnumTexte(getProject(), EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_GEARBOX_TYPE, getProject().getViewerLanguage(), false);
        enumGearBox.setConstraints(createFieldConstraints(y, 1));
        enumGearBox.setName("enumGearBox");
        enumGearBox.addEventListener(enableChangeEventListener);
        panelMain.panelFilterValues.addChild(enumGearBox);
        y++;

        labelSpringFields = createLabel(y, "PartForSpring", "!!Teilenummern für Feder-Filter");
        panelMain.panelFilterValues.addChild(labelSpringFields);
        textfieldPartForSpring = new GuiButtonTextField();
        textfieldPartForSpring.setButtonVisible(true);
        textfieldPartForSpring.setButtonText("...");
        constraints = createFieldConstraints(y, 2);
        textfieldPartForSpring.setConstraints(constraints);
        textfieldPartForSpring.setName("textfieldPartForSpring");
        textfieldPartForSpring.setEditable(false);
        panelMain.panelFilterValues.addChild(textfieldPartForSpring);
        //
        textfieldPartForSpring.addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                onSpringSearchEvent(event);
            }
        });

        textfieldPartForSpring.addEventListener(enableChangeEventListener);
        y++;

        label = createLabel(y, "PGroup", "!!Produktgruppe");
        panelMain.panelFilterValues.addChild(label);
        /* EnumComBox für ProductGroup */
        enumPGroup = new EnumRComboBox();
        enumPGroup.setMaximumWidth(maxWidthForComboBoxes);
        enumPGroup.setEnumTexte(getProject(), EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_PRODUCT_GRP, getProject().getViewerLanguage(), true);
        enumPGroup.setConstraints(createFieldConstraints(y, 1));
        enumPGroup.setName("enumPGroup");
        panelMain.panelFilterValues.addChild(enumPGroup);
        enumPGroup.addEventListener(new EventListener(de.docware.framework.modules.gui.event.Event.ON_CHANGE_EVENT) {
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                doOnEnableChangeEvent();
                codeButtonTextField.setProductGroupEnumValue(getProductGroup());
            }
        });
        y++;

        GuiLabel labelCode = createLabel(y, "Code", "!!Code");
        panelMain.panelFilterValues.addChild(labelCode);

        /* ButtonTextField für Code */
        codeButtonTextField = new iPartsGuiDIALOGTypesButtonTextField(parentForm, iPartsFilterGridForm.DIALOG_TYPES.CODE);
        codeButtonTextField.setEditable(false);
        constraints = createFieldConstraints(y, 2);
        codeButtonTextField.setConstraints(constraints);
        codeButtonTextField.setName("codeButtonTextField");
        panelMain.panelFilterValues.addChild(codeButtonTextField);
        codeButtonTextField.addEventListener(new EventListener(de.docware.framework.modules.gui.event.Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                doOnEnableChangeEvent();
                updateAutoProductSelectValues(true);
            }
        });
        y++;

        label = createLabel(y, "Event", "!!Ereignis");
        panelMain.panelFilterValues.addChild(label);
        comboboxEvent = new iPartsGuiEventSelectComboBox();
        comboboxEvent.setMaximumWidth(maxWidthForComboBoxes);
        constraints = createFieldConstraints(y, 1);
        comboboxEvent.setConstraints(constraints);
        comboboxEvent.setName("comboboxEvent");
        comboboxEvent.addEventListener(enableChangeEventListener);
        panelMain.panelFilterValues.addChild(comboboxEvent);
        y++;

        label = createLabel(y, "Factory", "!!Werk");
        panelMain.panelFilterValues.addChild(label);
        comboboxFactory = new RComboBox<>();
        comboboxFactory.setMaximumWidth(maxWidthForComboBoxes);
        constraints = createFieldConstraints(y, 1);
        comboboxFactory.setConstraints(constraints);
        comboboxFactory.setName("comboboxFactory");
        comboboxFactory.addEventListener(enableChangeEventListener);
        panelMain.panelFilterValues.addChild(comboboxFactory);
        y++;

        labelTechnicalApprovalDate = createLabel(y, "TechnicalApprovalDate", "!!Schlussabnahmedatum");
        panelMain.panelFilterValues.addChild(labelTechnicalApprovalDate);
        calendarTechnicalApprovalDate = new GuiCalendar();
        calendarTechnicalApprovalDate.setMaximumWidth(maxWidthForComboBoxes);
        calendarTechnicalApprovalDate.setName("calendarTechnicalApprovalDate");
        calendarTechnicalApprovalDate.__internal_setGenerationDpi(96);
        calendarTechnicalApprovalDate.registerTranslationHandler(getUITranslationHandler());
        constraints = createFieldConstraints(y, 1);
        calendarTechnicalApprovalDate.setConstraints(constraints);
        panelMain.panelFilterValues.addChild(calendarTechnicalApprovalDate);
        calendarTechnicalApprovalDate.clearDate();
        calendarTechnicalApprovalDate.addEventListener(enableChangeEventListener);
        y++;

        labelDateOfTechnicalState = createLabel(y, "DateOfTechnicalState", "!!Termin Technischer Zustand");
        panelMain.panelFilterValues.addChild(labelDateOfTechnicalState);
        calendarDateOfTechnicalState = new GuiCalendar();
        calendarDateOfTechnicalState.setMaximumWidth(maxWidthForComboBoxes);
        calendarDateOfTechnicalState.setName("calendarDateOfTechnicalState");
        calendarDateOfTechnicalState.__internal_setGenerationDpi(96);
        calendarDateOfTechnicalState.registerTranslationHandler(getUITranslationHandler());
        constraints = createFieldConstraints(y, 1);
        calendarDateOfTechnicalState.setConstraints(constraints);
        panelMain.panelFilterValues.addChild(calendarDateOfTechnicalState);
        calendarDateOfTechnicalState.clearDate();
        calendarDateOfTechnicalState.addEventListener(enableChangeEventListener);
        y++;

        labelAggModels = createLabel(y, "AggregateModels", "!!Aggregatebaumuster");
        panelMain.panelFilterValues.addChild(labelAggModels);
        /* ButtonTextField für AggregateModels */
        aggregateModelsButtonTextField = new iPartsGuiDIALOGTypesButtonTextField(parentForm, iPartsFilterGridForm.DIALOG_TYPES.AGG_MODELS);
        aggregateModelsButtonTextField.setEditable(false);
        aggregateModelsButtonTextField.setConstraints(createFieldConstraints(y, 2));
        aggregateModelsButtonTextField.setName("aggregateModelsButtonTextField");
        panelMain.panelFilterValues.addChild(aggregateModelsButtonTextField);
        aggregateModelsButtonTextField.addEventListener(enableChangeEventListener);
        y++;

        if (iPartsRight.checkPSKInSession()) {
            labelPSKVariants = createLabel(y, "PSKVariants", "!!PSK-Variantengültigkeit");
            panelMain.panelFilterValues.addChild(labelPSKVariants);
            /* ButtonTextField für PSK Variantengültigkeit */
            pskVariantsSelectTextField = new iPartsGuiPSKVariantsSelectTextField(getProject());
            pskVariantsSelectTextField.setEditable(false);
            pskVariantsSelectTextField.setButtonTooltip(PSK_VARIANTS_SELECT_TOOLTIP_TEXT);
            pskVariantsSelectTextField.setConstraints(createFieldConstraints(y, 2));
            pskVariantsSelectTextField.setName("pskVariantsSelectTextField");
            panelMain.panelFilterValues.addChild(pskVariantsSelectTextField);
            pskVariantsSelectTextField.addEventListener(enableChangeEventListener);
            pskVariantsSelectTextField.init(parentForm);
            y++;
        }

        labelCountry = createLabel(y, "country", "!!Ländergültigkeit");
        panelMain.panelFilterValues.addChild(labelCountry);
        countryCombobox = new iPartsGuiCountrySelectionBox(getProject(), "");
        countryCombobox.setConstraints(createFieldConstraints(y, 2));
        countryCombobox.setName("countryCombobox");
        panelMain.panelFilterValues.addChild(countryCombobox);
        countryCombobox.addEventListener(enableChangeEventListener);
        y++;

        labelSpecification = createLabel(y, "specification", "!!Spezifikationen");
        panelMain.panelFilterValues.addChild(labelSpecification);
        specificationValue = new GuiTextField();
        specificationValue.setConstraints(createFieldConstraints(y, 2));
        specificationValue.setName("specificationValueLabel");
        specificationValue.setEditable(false);
        panelMain.panelFilterValues.addChild(specificationValue);
        y++;

        labelAutoProdSelect = createLabel(y, "AutoProdSelect", "!!Gültige Produkte");
        constraints = (ConstraintsGridBag)labelAutoProdSelect.getConstraints();
        constraints.setInsetsTop(16);
        constraints.setInsetsBottom(16);
        labelAutoProdSelect.setConstraints(constraints);
        panelMain.panelFilterValues.addChild(labelAutoProdSelect);
        labelAutoProdSelectValues = createLabel(y, "AutoProdSelectValues", "");
        labelAutoProdSelectValues.setHorizontalAlignment(GuiLabel.HorizontalAlignment.LEFT);
        constraints = createFieldConstraints(y, 2);
        constraints.setInsetsTop(16);
        constraints.setInsetsBottom(16);
        labelAutoProdSelectValues.setConstraints(constraints);
        panelMain.panelFilterValues.addChild(labelAutoProdSelectValues);
        y++;

        labelExchangeAggregate = createLabel(y, "ExchangeAggregate", "");
        constraints = (ConstraintsGridBag)labelExchangeAggregate.getConstraints();
        labelExchangeAggregate.setConstraints(constraints);
        panelMain.panelFilterValues.addChild(labelExchangeAggregate);
        labelExchangeAggregateValue = createLabel(y, "ExchangeAggregateValue", "!!Endnummernfilter ist deaktiviert");
        labelExchangeAggregateValue.setHorizontalAlignment(GuiLabel.HorizontalAlignment.LEFT);
        constraints = createFieldConstraints(y, 2);
        labelExchangeAggregateValue.setConstraints(constraints);
        panelMain.panelFilterValues.addChild(labelExchangeAggregateValue);
        labelExchangeAggregate.setVisible(false);
        labelExchangeAggregateValue.setVisible(false);
        y++;

        labelEldasAggregateInDIALOGVehicleValue = createLabel(y, "EldasAggregateInDIALOGVehicleValue", "!!Reduzierte Filterung (ELDAS-Aggregat in DIALOG-Fahrzeug)");
        labelEldasAggregateInDIALOGVehicleValue.setHorizontalAlignment(GuiLabel.HorizontalAlignment.LEFT);
        constraints = createFieldConstraints(y, 2);
        labelEldasAggregateInDIALOGVehicleValue.setConstraints(constraints);
        panelMain.panelFilterValues.addChild(labelEldasAggregateInDIALOGVehicleValue);
        labelEldasAggregateInDIALOGVehicleValue.setVisible(false);

        label = createLabel(y + 5, "", "");
        constraints = new ConstraintsGridBag(1, y + 5, 1, 1, 0.0, 100.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE,
                                             0, 0, 0, 0);
        label.setConstraints(constraints);
        panelMain.panelFilterValues.addChild(label);

        setIgnoreAggIdentChangeEvent(true);
        comboboxAggIdent.setSelectedIndex(DatacardIdentOrderTypes.FIN.ordinal());
        setIgnoreAggIdentChangeEvent(false);
    }

    @Override
    public AbstractGuiControl getGui() {
        return panelMain;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public boolean isViewing() {
        return isViewing;
    }

    public void setViewing(boolean viewing) {
        if (isViewing != viewing) {
            textFieldAggIdent.setEditable(!viewing);
            comboboxAggIdent.setEnabled(!viewing);
            buttontextfieldModels.setEditable(!viewing);
            saaButtonTextField.setEditable(!viewing);
            codeButtonTextField.setEditable(!viewing);
            enumSteering.setEnabled(!viewing);
            enumGearBox.setEnabled(!viewing);
            textfieldPartForSpring.setEditable(!viewing);
            enumPGroup.setEnabled(!viewing);
            comboboxEvent.setEnabled(!viewing);
            comboboxFactory.setEnabled(!viewing);
            calendarTechnicalApprovalDate.setEditable(!viewing);
            calendarDateOfTechnicalState.setEditable(!viewing);
            aggregateModelsButtonTextField.setEditable(!viewing);
            if (pskVariantsSelectTextField != null) {
                pskVariantsSelectTextField.setVisible(!viewing);
            }
            if (labelPSKVariants != null) {
                labelPSKVariants.setVisible(!viewing);
            }

            saaButtonTextField.setViewing(viewing);
            codeButtonTextField.setViewing(viewing);
            aggregateModelsButtonTextField.setViewing(viewing);
            countryCombobox.setEnabled(!viewing);
        }
        isViewing = viewing;
        // ContextMenu immer anzeigen
        textFieldAggIdent.setContextMenu(true, parentForm);
        if (textFieldVINIdent != null) {
            textFieldVINIdent.setContextMenu(true, parentForm);
        }
    }

    private void setPanelVINVisible(boolean visible) {
        if ((panelVIN != null) && (visible != panelVIN.isVisible())) {
            panelVIN.setVisible(visible);
        }
    }

    public VinId getVinId() {
        DatacardIdentOrderElem identOrderElem = getSelectedOrderElem();
        if ((identOrderElem != null) && (identOrderElem.getType() == DatacardIdentOrderTypes.VIN)) {
            String vinNo = textFieldAggIdent.getIdentOrderElem().getAggregateIdent().getIdent();
            return new VinId(vinNo);
        } else {
            return new VinId();
        }
    }

    public FinId getFinIdFromIdentOrderElement() {
        DatacardIdentOrderElem identOrderElem = getSelectedOrderElem();
        if ((identOrderElem != null) && (identOrderElem.getType() == DatacardIdentOrderTypes.FIN)) {
            DatacardIdentOrderElem aggIdentOrderElem = textFieldAggIdent.getIdentOrderElem();
            if (aggIdentOrderElem != null) {
                String finNo = aggIdentOrderElem.getAggregateIdent().getIdent();
                return new FinId(finNo);
            }
        }

        return new FinId();
    }

    public FinId getFinIdFromLoadedDataCard() {
        if (isVehicleDatacardLoaded()) {
            VehicleDataCard dataCard = (VehicleDataCard)getLoadedDataCard();
            if (dataCard != null) {
                return dataCard.getFinId();
            }
        }
        return new FinId();
    }

    public void setFinId(FinId finId, boolean checkFinData) {
        setFinAsString(finId.getFIN(), checkFinData);
    }

    public void setFinAsString(String fin, boolean checkFinData) {
        setIdentValue(fin);
        if (checkFinData) {
            checkFINData(null);
        }
    }

    /**
     * Setzt für die Anzeige im Dialog die letzte geladene FIN bzw. VIN
     *
     * @param lastFinId
     */
    public void setLastLoadedDatacardNumber(FinId lastFinId) {
        if (!isViewing()) {
            String text;
            if ((lastFinId != null)) {
                String finContent = lastFinId.getFIN();
                VinId possibleVin = new VinId(finContent); // Falls die FIN nicht gültig ist, wird versucht daraus eine VIN zu machen
                if (lastFinId.isValidId()) {
                    text = TranslationHandler.translate("!!Zuletzt geladene FIN: %1", lastFinId.getFIN()) + " ";
                    lastLoadedDatacardNumber = lastFinId.getFIN();
                } else if (possibleVin.isValidId()) {
                    text = TranslationHandler.translate("!!Zuletzt geladene VIN: %1", possibleVin.getVIN()) + " ";
                    lastLoadedDatacardNumber = possibleVin.getVIN();
                } else {
                    text = "";
                    lastLoadedDatacardNumber = "";
                }
            } else {
                text = "";
                lastLoadedDatacardNumber = "";
            }
            panelMain.labelLastFin.setText(text);
        }
    }

    private void checkFINData(Event event) {
        checkFINData(event, false);
    }

    /**
     * Analysiert die eingegebene FIN und befüllt die verknüpften Eingabefelder. Über <i>finFromDataCard</i> kann
     * vorgebenen werden, dass die FIN der geladenen Datenkarte analysiert werden soll (und nicht der Inhalt im Eingabefenster).
     *
     * @param event
     */
    private void checkFINData(Event event, boolean finFromDataCard) {
        FinId finId = null;
        if (finFromDataCard && isLoadedDataCardValid() && getLoadedDataCard().isVehicleDataCard()) {
            finId = getFinIdFromLoadedDataCard();
        }
        if (finId == null) {
            finId = getFinIdFromIdentOrderElement();
        }
        if (event != null) { // den Fall gibt es nicht
            // an der FIN wurde editiert => falls von realer Datenkarte besetzt: BM gehen vor
            handleDirectTranfer();
        }
        if (finId.isModelNumberValid()) {
            setModelNo(finId.getFullModelNumber(), true);
        } else {
            setModelNo("", true);
        }
        // Lenkung nur setzen, wenn es eine Links- oder Rechtslenkung ist
        if (finId.isSteeringValid(true)) {
            setSteeringValue(finId.getLeftOrRightSteeringAsEnumKey());
        } else {
            setSteeringValue("");
        }
        setFactoryValueFromFin(finId);
        calendarTechnicalApprovalDate.clearDate();
        calendarDateOfTechnicalState.clearDate();
    }

    /**
     * Bestimmt das Werk basierend auf dem WMI und dem Werkskennbuchstaben der FIN
     *
     * @param finId
     */

    private void setFactoryValueFromFin(FinId finId) {
        String factoryNumber = null;
        if (finId.isWMIValid() && finId.isFactorySignValid()) {
            factoryNumber = iPartsFactoryModel.getInstance(getProject()).getFactoryNumberForWMI(finId.getWorldManufacturerIdentifier(),
                                                                                                finId.getFactorySign(),
                                                                                                new iPartsModelId(finId.getFullModelNumber()), finId.getSteering());
        }
        setFactoryValue(factoryNumber);
    }

    /**
     * Liefert den {@link de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.AggregateIdent} vom Textfeld für die FIN bzw. den Aggregate-Ident zurück.
     *
     * @return
     */
    public AggregateIdent getAggregateIdent() {
        if (textFieldAggIdent.getIdentOrderElem() != null) {
            return textFieldAggIdent.getIdentOrderElem().getAggregateIdent();
        } else {
            return new FinId_Agg();
        }
    }

    public void setIdentValue(String value) {
        textFieldAggIdent.switchOffEventListeners();
        textFieldAggIdent.setValue(value);
        textFieldAggIdent.switchOnEventListeners();
    }

    public DatacardIdentOrderTypes getAggregateType() {
        if (textFieldAggIdent.getIdentOrderElem() != null) {
            return textFieldAggIdent.getIdentOrderElem().getType();
        } else {
            return DatacardIdentOrderTypes.UNKNOWN;
        }
    }

    /*===== Getter and Setter ======*/
    public String getModelNo() {
        return buttontextfieldModels.getText().toUpperCase().trim();
    }

    public void setModelNo(String modelNo, boolean checkModelData) {
        currentModel = iPartsModel.getInstance(getProject(), new iPartsModelId(modelNo));
        buttontextfieldModels.switchOffEventListeners();
        buttontextfieldModels.setText(modelNo);
        setModelBackgroundColor(modelNo);
        buttontextfieldModels.switchOnEventListeners();
        if (checkModelData) {
            checkModelData();
        }
        doOnEnableChangeEvent();
    }

    public boolean checkModelNo() {
        return (currentModel != null) && currentModel.existsInDB();
    }

    public TwoGridValues getDatacardSaaNumbers() {
        return saaButtonTextField.getTwoGridValues();
    }

    public void setDatacardSaaNumbers(TwoGridValues datacardSaaNumbers) {
        saaButtonTextField.setTwoGridValues(datacardSaaNumbers);
    }

    public TwoGridValues getCodes() {
        return codeButtonTextField.getTwoGridValues();
    }

    public void setCodes(TwoGridValues codes) {
        codeButtonTextField.setTwoGridValues(codes);
    }

    public String getProductGroup() {
        return enumPGroup.getActToken();
    }

    public void setProductGroup(String productGroup) {
        enumPGroup.setActToken(productGroup);
        codeButtonTextField.setProductGroupEnumValue(productGroup);
    }

    public String getAutoProdSelectValues() {
        return labelAutoProdSelectValues.getText();
    }

    public TwoGridValues getAggModelsNumbers() {
        return aggregateModelsButtonTextField.getTwoGridValues();
    }

    public void setAggModelsNumbers(TwoGridValues newAggModels) {
        aggregateModelsButtonTextField.setTwoGridValues(newAggModels);
    }

    /**
     * Gewählte PSK-Varianten vom SelectTextField holen
     *
     * @return
     */
    public Set<String> getPSKVariants() {
        if (pskVariantsSelectTextField != null) {
            return new HashSet<>(pskVariantsSelectTextField.getArray().getArrayAsStringList());
        } else {
            return new HashSet<>();
        }
    }

    public String getSteeringValue() {
        return enumSteering.getActToken();
    }

    public void setSteeringValue(String steeringValue) {
        enumSteering.setActToken(steeringValue);
    }

    public String getGearboxValue() {
        return enumGearBox.getActToken();
    }

    public void setGearboxValue(String gearboxValue) {
        enumGearBox.setActToken(gearboxValue);
    }

    public String getTechnicalApprovalDateValue() {
        return calendarTechnicalApprovalDate.getDateAsRawString();
    }

    public void setTechnicalApprovalDateValue(String date) {
        if (StrUtils.isEmpty(date)) {
            calendarTechnicalApprovalDate.clearDate();
        } else {
            calendarTechnicalApprovalDate.setDate(date);
        }
    }

    public String getDateOfTechnicalStateValue() {
        return calendarDateOfTechnicalState.getDateAsRawString();
    }

    public void setDateOfTechnicalStateValue(String date) {
        if (StrUtils.isEmpty(date)) {
            calendarDateOfTechnicalState.clearDate();
        } else {
            calendarDateOfTechnicalState.setDate(date);
        }
    }

    /**
     * Liefert den ausgewählten Event-Wert aus dem Filter-Dialog
     *
     * @return
     */
    public iPartsEvent getEvent() {
        return comboboxEvent.getSelectedEvent();
    }

    public void setEvent(iPartsEvent event) {
        comboboxEvent.setSelectedEvent(event);
    }

    public String getFactoryValue() {
        return comboboxFactory.getSelectedUserObject();
    }

    public void setFactoryValue(String factoryValue) {
        if (StrUtils.isEmpty(factoryValue)) {
            comboboxFactory.setSelectedItem("");
        } else {
            if (!comboboxFactory.setSelectedUserObject(factoryValue) && isAggregateDatacardLoaded() && ((AggregateDataCard)getLoadedDataCard()).isUseVehicleFactoryNumber()) {
                // Falls das Werk von der Fahrzeug-Datenkarte kommt, muss dieses in der Regel erst zur ComboBox hinzugefügt werden
                comboboxFactory.addItem(factoryValue, factoryValue + " (" + TranslationHandler.translate("!!Fahrzeug-Datenkarte") + ")");
                comboboxFactory.setSelectedUserObject(factoryValue);
            }
        }
    }

    public SpringFields getSpringFields() {
        if (textfieldPartForSpring.isEnabled()) {
            return springFields;
        } else {
            return new SpringFields();
        }
    }

    public void setSpringFilter(java.util.List<String> springLegFront, java.util.List<String> springRear, java.util.List<String> springShimRear) {
        StringBuilder str = new StringBuilder();
        if (!springLegFront.isEmpty()) {
            str.append("Z-FB v:");
            str.append(" ");
            java.util.List<String> helpList = new DwList<String>();
            for (String springNo : springLegFront) {
                helpList.add(iPartsNumberHelper.formatPartNo(getProject(), springNo));
            }
            str.append(StrUtils.stringListToString(helpList, ", "));
            str.append(";");
        }
        if (!springRear.isEmpty()) {
            if (!str.toString().isEmpty()) {
                str.append(" ");
            }
            str.append("F h:");
            str.append(" ");
            java.util.List<String> helpList = new DwList<String>();
            for (String springNo : springRear) {
                helpList.add(iPartsNumberHelper.formatPartNo(getProject(), springNo));
            }
            str.append(StrUtils.stringListToString(helpList, ", "));
            str.append(";");
        }
        if (!springShimRear.isEmpty()) {
            if (!str.toString().isEmpty()) {
                str.append(" ");
            }
            str.append("Fb h:");
            str.append(" ");
            java.util.List<String> helpList = new DwList<String>();
            for (String springNo : springShimRear) {
                helpList.add(iPartsNumberHelper.formatPartNo(getProject(), springNo));
            }
            str.append(StrUtils.stringListToString(helpList, ", "));
        }
        textfieldPartForSpring.setText(str.toString());
    }

    public void setAdditionalAggregateSNR(String additionalAggregateSNR) {
        this.textFieldAdditionalAggregateSNR.setText(additionalAggregateSNR);
    }

    public void setSpecValidity(Set<String> specValidities) {
        if (!Utils.isValid(specValidities)) {
            specificationValue.setText("");
        } else {
            specificationValue.setText(StrUtils.stringListToString(specValidities, ", "));
        }
    }

    public Set<String> getSpecValidity() {
        String text = specificationValue.getText().trim();
        if (StrUtils.isValid(text)) {
            List<String> stringList = StrUtils.toStringList(text, ",", false, true);
            Set<String> result = new TreeSet<>(stringList);
            return result;
        }
        return new HashSet<>();
    }

    public String getCountry() {
        return countryCombobox.getSelectedCountryCode();
    }

    public void setCountry(String country) {
        if (country == null) {
            countryCombobox.setSelectedUserObject("");
        } else {
            countryCombobox.setSelectedUserObject(country);
        }
    }

    /*===== Getter and Setter End ======*/

    public void setSpringFilter(SpringFields springFields) {
        setSpringFilter(springFields.activeSpringLegFront, springFields.activeSpringRear, springFields.activeSpringShimRear);
    }

    public void setIgnoreAggIdentChangeEvent(boolean ignoreChangeEvents) {
        ignoreAggIdentChangeEvent = ignoreChangeEvents;
    }

    public boolean isLoadedDataCardValid() {
        return getLoadedDataCard() != null;
    }

    public boolean isVehicleDatacardLoaded() {
        if (isLoadedDataCardValid()) {
            return getLoadedDataCard().isVehicleDataCard();
        }
        return false;
    }

    public boolean isAggregateDatacardLoaded() {
        if (isLoadedDataCardValid()) {
            return getLoadedDataCard().isAggregateDataCard();
        }
        return false;
    }

    public AbstractDataCard getLoadedDataCard() {
        return loadedDataCard;
    }

    private void setLoadedDataCard(AbstractDataCard dataCard) {
        loadedDataCard = dataCard;
        if (isVehicleDatacardLoaded()) {
            springFields = new SpringFields((VehicleDataCard)getLoadedDataCard());
        } else {
            springFields = new SpringFields();
        }
        setSpringFilter(springFields);
        updateInfoText(loadedDataCard);
    }

    public void clearDialog(boolean withDeleteIdentValue) {
        clearDialog(withDeleteIdentValue, false);
    }

    public void clearDialog(boolean withDeleteIdentValue, boolean resetDataCard) {
        ignoreAggIdentChangeEvent = true;
//        setSelectedIndexByAggIdentCombo(DatacardIdentOrderTypes.FIN);
        hasChanged = false;
        if (withDeleteIdentValue) {
            setIdentValue("");
        }

        DatacardIdentOrderTypes type = getSelectedOrderElem().getType();

        // Nur FIN und VIN berücksichtigen.
        DatacardIdentOrderTypes searchType = null;

        if (type.equals(DatacardIdentOrderTypes.FIN)) {
            searchType = DatacardIdentOrderTypes.VIN;
        } else if (type.equals(DatacardIdentOrderTypes.VIN)) {
            searchType = DatacardIdentOrderTypes.FIN;
        }

        if (searchType != null) {
            // Über den Inhalt der ComboBox iterieren und das passende FIN oder VIN-Element herausholen
            for (DatacardIdentOrderElem elem : comboboxAggIdent.getUserObjects()) {
                if (elem.getType() == searchType) {
                    textFieldAggIdent.switchOffEventListeners();
                    textFieldAggIdent.setValue("");
                    textFieldAggIdent.switchOnEventListeners();
                }
            }

            // Der RESET-Button löscht über den zweiten Parameter die Datenkarte.
            if (resetDataCard) {
                loadedDataCard = null;
            }
        }

        setDirectTransfer(true);
        setModelNo("", true);
        setDatacardSaaNumbers(new TwoGridValues());
        setSteeringValue("");
        setGearboxValue("");
        springFields = new SpringFields();
        setSpringFilter(springFields);
        setProductGroup("");
        setCodes(new TwoGridValues());
        setFactoryValue("");
        setTechnicalApprovalDateValue("");
        setDateOfTechnicalStateValue("");
        setLabelTechnicalApprovalDateEnhanced(false);
        setCountry("");
        setSpecValidity(null);
        setAggModelsNumbers(new TwoGridValues());
        ignoreAggIdentChangeEvent = false;
        updateAutoProductSelectValues(true);
        updateInfoText(null);
        setAdditionalAggregateSNR("");

        // Button "Datenkarte laden..." muss bei Aggregaten disabled werden, wenn der Ident leer ist
        if (buttonLoad != null) {
            DatacardIdentOrderElem identOrderElem = getSelectedOrderElem();
            buttonLoad.setEnabled((identOrderElem != null) && ((identOrderElem.getType() == DatacardIdentOrderTypes.FIN)
                                                               || (identOrderElem.getType() == DatacardIdentOrderTypes.VIN)));
        }
    }

    /**
     * Aktualisiert abhängig von der eingegebenen FIN und den eingegebenen Code die Anzeige der gültigen Produkte mittels
     * Auto-Product-Select.
     *
     * @param setPSKVariantsFromDataCard
     */
    public void updateAutoProductSelectValues(boolean setPSKVariantsFromDataCard) {
        FinId finId;
        // Falls VIN ausgewählt wurde und zu der VIN eine FIN existiert -> Nutze die FIN für den APS
        if ((getSelectedOrderElem().getType() == DatacardIdentOrderTypes.VIN) && isVehicleDatacardLoaded()) {
            finId = getFinIdFromLoadedDataCard();
        } else {
            finId = getFinIdFromIdentOrderElement();
        }


        if (StrUtils.isValid(getModelNo())) { // Das Baumuster ist notwendig für die Bestimmung der Baureihe
            iPartsModel model = iPartsModel.getInstance(getProject(), new iPartsModelId(getModelNo()));
            if (model.getSeriesId().isValidId()) { // Baureihe vorhanden?
                iPartsDialogSeries series = iPartsDialogSeries.getInstance(getProject(), model.getSeriesId());

                // Ereignisse zur Baureihe zur Combobox hinzufügen
                if (series.isEventTriggered()) {
                    iPartsEvent selectedEvent = getEvent();
                    comboboxEvent.init(getProject(), series.getSeriesId(), (selectedEvent != null) ? selectedEvent.getEventId() : null);
                } else {
                    comboboxEvent.init(getProject(), series.getSeriesId(), null);
                }
            } else {
                comboboxEvent.init(getProject(), null, null);
            }
        } else {
            comboboxEvent.init(getProject(), null, null);
        }

        Set<iPartsProductId> pskProductIds = new TreeSet<>();
        TreeSet<String> productNumbers = getProductNumbers(getModelNo(), getCodes(), finId, pskProductIds, getProject());

        String productsString = StrUtils.stringListToString(productNumbers, ", ");
        labelAutoProdSelectValues.setText(productsString);

        // Falls das PSK Variantengültigkeit Textfeld vorhanden ist und es PSK Produkte gibt, dann
        // das Feld enablen und Produkte setzen, sowie eine schon vorhandene Selektion
        if (pskVariantsSelectTextField != null) {
            boolean isAutoSelectedProductsPSKLocal = false;
            if (!pskProductIds.isEmpty()) {
                pskVariantsSelectTextField.setEditable(true);
                pskVariantsSelectTextField.setButtonTooltip("");
                // Nur bei dem erneuten initialisieren des Filterdialogs darf die Selektion gesetzt werden
                // Sonst kommt der Aufruf durch das Ändern von anderen Filterwerten bzgl. FIN/Ident/Baumuster -> Selektion muss leer sein
                if (setPSKVariantsFromDataCard) {
                    AbstractDataCard dataCard = getLoadedDataCard();
                    if (dataCard != null) {
                        Set<String> selectedPSKVariantsSet = dataCard.getPskVariants();
                        if (selectedPSKVariantsSet == null) {
                            selectedPSKVariantsSet = new HashSet<>();
                        }
                        pskVariantsSelectTextField.addDataArrayFromSelection(selectedPSKVariantsSet);
                    }
                } else {
                    pskVariantsSelectTextField.addDataArrayFromSelection(new HashSet<>());
                }
                pskVariantsSelectTextField.setProductIds(pskProductIds);
                isAutoSelectedProductsPSKLocal = true;
            } else {
                // Falls keine PSK-Produkte vorhanden sind, darf das Feld nicht editierbar sein
                // Schon gesetzte Selektionen müssen im Dialog rausgenommen werden
                pskVariantsSelectTextField.setEditable(false);
                pskVariantsSelectTextField.setButtonTooltip(PSK_VARIANTS_SELECT_TOOLTIP_TEXT);
                pskVariantsSelectTextField.addDataArrayFromSelection(new HashSet<>());
            }
            isAutoSelectedProductsPSK = isAutoSelectedProductsPSKLocal;
        }

        AggregateIdent aggIdent = getAggregateIdent();
        labelExchangeAggregate.setVisible(aggIdent.isExchangeAggregate());
        labelExchangeAggregateValue.setVisible(aggIdent.isExchangeAggregate());
        if (aggIdent.isExchangeAggregate()) {
            switch (aggIdent.getAggType()) {
                case ENGINE:
                    labelExchangeAggregate.setText(DCAggregateTypes.EXCHANGE_ENGINE);
                    break;
                case TRANSMISSION:
                    labelExchangeAggregate.setText(DCAggregateTypes.EXCHANGE_TRANSMISSION);
                    break;
                default:
                    labelExchangeAggregate.setText("");
                    break;
            }
        }

        labelEldasAggregateInDIALOGVehicleValue.setVisible((loadedDataCard != null) && (loadedDataCard instanceof AggregateDataCard)
                                                           && ((AggregateDataCard)loadedDataCard).isEldasAggregateInDIALOGVehicle(getProject()));
    }

    /**
     * APS-Produkte zusammenstellen und PSK Produkte in Set speichern
     *
     * @param modelNo
     * @param codes
     * @param finId
     * @param pskProductIds
     * @param project
     * @return
     */
    public static TreeSet<String> getProductNumbers(String modelNo, TwoGridValues codes, FinId finId, Set<iPartsProductId> pskProductIds,
                                                    EtkProject project) {
        List<iPartsProduct> productsForModel = iPartsFilterHelper.getAutoSelectProducts(modelNo, finId, codes, project);
        // APS-Produkte alphabetisch sortiert zusammenstellen
        TreeSet<String> productNumbers = new TreeSet<>();
        for (iPartsProduct product : productsForModel) {
            iPartsProductId productId = product.getAsId();
            if (product.isPSK()) {
                // PSK-Produkte nicht zeigen, falls Autor keine PSK Eigenschaft hat
                if (!iPartsRight.checkPSKInSession()) {
                    continue;
                }
                pskProductIds.add(productId);
            } else if (!iPartsRight.checkUserHasBothVehicleTypeRightsInSession() // Hat der Benutzer beide Eigenschaften, brauchen wir die Produkte nicht zu filtern
                       && !iPartsFilterHelper.isProductVisibleForUserInSession(product)) { // Produkte, die zu den Eigenschaften des Benutzers nicht passen, müssen ausgefiltert werden (außer PSK)
                continue;
            }
            String productNumber = productId.getProductNumber();
            if (!product.isRetailRelevantFromDB()) {
                productNumber = "<" + productNumber + ">";
            }
            productNumbers.add(productNumber);
        }
        return productNumbers;
    }

    /**
     * Sichtbarkeit von Feldern steuern, die nur für Fahrzeugdatenkarte anzuzeigen sind.
     *
     * @param visible
     */
    private void setVehicleOnlyFieldsVisible(boolean visible) {
        labelAggModels.setVisible(visible);
        aggregateModelsButtonTextField.setVisible(visible);
        labelSpringFields.setVisible(visible);
        textfieldPartForSpring.setVisible(visible);
        labelDateOfTechnicalState.setVisible(visible);
        calendarDateOfTechnicalState.setVisible(visible);
        labelSpecification.setVisible(visible);
        specificationValue.setVisible(visible);
        labelCountry.setVisible(visible);
        countryCombobox.setVisible(visible);

        // Felder die nur für Aggregate Datenkarten sichtbar sind
        labelAdditionalAggregateSNR.setVisible(!visible);
        textFieldAdditionalAggregateSNR.setVisible(!visible);
    }

    /**
     * Baumusterdaten prüfen und Dialogfelder aus Baumuster befüllen
     */
    private void checkModelData() {
        String modelNo = getModelNo();
        if (!modelNo.isEmpty()) {
            if (!currentModel.getModelId().getModelNumber().equalsIgnoreCase(modelNo)) {
                currentModel = iPartsModel.getInstance(getProject(), new iPartsModelId(modelNo));
            }
        } else {
            currentModel = iPartsModel.getInstance(getProject(), new iPartsModelId(""));
        }

        // Wenn das Baumuster existiert, setze die Produktgruppe des Aggregatetyp des Baumusters
        if (checkModelNo()) {
            setProductGroup(currentModel.getProductGroup());

        } else {
            setProductGroup("");
            //setGearboxValue("");
        }
        String gearboxValue = "";
        if (isVehicleDatacardLoaded() && getLoadedDataCard().isDataCardLoaded()) { // notwendig für die Getriebeart in Fahrzeug-Datenkarten (übernommen von einer Getriebe-Datenkarte)
            gearboxValue = getLoadedDataCard().getGearboxValue();
        } else if (DCAggregateTypes.getDCAggregateTypeByAggregateType(currentModel.getAggregateType()) == DCAggregateTypes.TRANSMISSION) {
            // Die Getriebeart soll primär über das Produkt vom Baumuster ermittelt werden
            gearboxValue = iPartsModel.getAggregateTypeFromFirstProduct(getProject(), currentModel.getModelId().getModelNumber());
        }
        setGearboxValue(gearboxValue); // andere Aggregatearten als Getriebe würden hier rausfliegen, weil es keine Getriebe-Enums sind

        // Kleinbuchstaben zu Großbuchstaben umstellen und Leerzeichen/Sonderzeichen verhindern
        String actText = buttontextfieldModels.getText();
        String convertedText = controlText(actText);
        if (!convertedText.equals(actText)) {
            buttontextfieldModels.switchOffEventListeners();
            final String setText = convertedText;
            Session.invokeThreadSafeInSessionWithChildThread(() -> {
                buttontextfieldModels.setText(setText);
                buttontextfieldModels.switchOnEventListeners();
            });
        }

        setModelBackgroundColor(modelNo);
        saaButtonTextField.setCurrentModel(currentModel);
        codeButtonTextField.setCurrentModel(currentModel);
        aggregateModelsButtonTextField.setCurrentModel(currentModel);
        setSpecValidity(null);
    }

    /**
     * Nur Großschreibung erlauben beim Model
     * Rausgezogen aus iPartsGuiAlphaNumTextField
     *
     * @param text
     * @return
     */
    private String controlText(String text) {
        if (StrUtils.isValid(text)) {
            StringBuilder str = new StringBuilder();
            for (int lfdNr = 0; lfdNr < text.length(); lfdNr++) {
                char ch = text.charAt(lfdNr);
                if (Character.isLetterOrDigit(ch)) {
                    // .isLetter(ch) lässt Umlaute etc durch, deswegen nochmal gezielt abgefragt
                    if ((ch >= '0') && (ch <= '9')) {
                        str.append(ch);
                    } else if ((ch >= 'A') && (ch <= 'Z')) {
                        str.append(ch);
                    } else if ((ch >= 'a') && (ch <= 'z')) {
                        str.append(Character.toUpperCase(ch));
                    }
                }
            }
            return str.toString();
        }
        return text;
    }

    private void checkAggIdentData(de.docware.framework.modules.gui.event.Event event) {
        if (!isViewing()) {
            DatacardIdentOrderElem identOrderElem = getSelectedOrderElem();
            if (identOrderElem != null) {
                if ((identOrderElem.getType() == DatacardIdentOrderTypes.FIN) || (identOrderElem.getType() == DatacardIdentOrderTypes.VIN)) {
                    if (identOrderElem.getType() == DatacardIdentOrderTypes.FIN) {
                        textfieldPartForSpring.setEnabled(true);
                        setSpringFilter(getSpringFields());
                        if (buttonLoad != null) {
                            buttonLoad.setEnabled(true);
                        }

                        if (!isLoadedDataCardValid()) {
                            checkFINData(null);
                        } else {
                            if (isVehicleDatacardLoaded()) {
                                if (getFinIdFromIdentOrderElement().isValidId() || getFinIdFromIdentOrderElement().isModelNumberValid()) {
                                    checkFINData(null);
                                }
                                setModelNo(getModelNo(), true);
                            } else {
                                clearDialog(false);
                                setFinAsString(identOrderElem.getAggregateIdent().getIdent(), true);
                            }
                        }
                        updateInfoText(fillDataCard(new VehicleDataCard(true)));
                        updateAutoProductSelectValues(false); // Setze alle gültigen Produkte
                    } else {
                        // für VIN alles zurücksetzen
                        setDirectTransfer(false);
                        setModelNo("", true);
                        setProductGroup("");
                        setSteeringValue("");
                        setFactoryValue("");
                        setLabelTechnicalApprovalDateEnhanced(false);
                        textfieldPartForSpring.setEnabled(false);
                        if (buttonLoad != null) {
                            buttonLoad.setEnabled(true);
                        }
                    }
                } else {
                    // Aggregate
                    if (identOrderElem.getAggregateIdent().getIdent().isEmpty()) {
                        String modelNo = getModelNo();
                        if (!modelNo.isEmpty()) {
                            if (iPartsModel.isVehicleModel(modelNo)) {
                                modelNo = "";
                            }
                        }
                        setModelNo(modelNo, true);
                    } else {
                        if (identOrderElem.getAggregateIdent().isModelNumberValid()) {
                            String modelNo = identOrderElem.getAggregateIdent().getModelNumber();
                            if (!modelNo.isEmpty() && !iPartsModel.isAggregateModel(modelNo)) {
                                modelNo = iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + modelNo;
                            }
                            setModelNo(modelNo, true);
                        } else {
                            setModelNo("", true);
                        }
                    }
                    setSteeringValue("");

                    // Werksnummer aus dem Werkskennbuchstaben über die WMI-Mapping-Tabelle bestimmen
                    String factorySign = identOrderElem.getAggregateIdent().getFactorySign();
                    String factoryNumber = "";
                    if (StrUtils.isValid(factorySign)) {
                        factoryNumber = iPartsFactoryModel.getInstance(getProject()).getFactoryNumberForAggregate(identOrderElem.getAggregateIdent().getAggregateTypeByModel(),
                                                                                                                  factorySign, new iPartsModelId(getModelNo()));
                    }
                    setFactoryValue(factoryNumber);

                    textfieldPartForSpring.setText("");
                    textfieldPartForSpring.setEnabled(false);
                    if (buttonLoad != null) {
                        buttonLoad.setEnabled(!identOrderElem.getAggregateIdent().isEmpty() && identOrderElem.isVIScallAllowed());
                    }
                    updateInfoText(fillDataCard(new AggregateDataCard(true)));
                    updateAutoProductSelectValues(false); // Setze alle gültigen Produkte
                    //DAIMLER-3432 Einkommentieren falls abfrage nur mit gültigen Idents möglich sein soll
//                    buttonLoad.setEnabled(identOrderElem.getAggregateIdent().isValidId());

//??                    calendarDate.clearDate();
                    switch (identOrderElem.getType()) {
                        case ENGINE_NEW:
                        case ENGINE_OLD:
                            EngineIdent engineIdent = (EngineIdent)identOrderElem.getAggregateIdent();
                            setSteeringValue(engineIdent.getSteeringEnumKey());
                            setGearboxValue(engineIdent.getTransmissionEnumKey());
                            break;
                        case TRANSMISSION:
                        case TRANSMISSION_AUTOMATED:
                        case TRANSMISSION_MECHANICAL:
                        case TRANSFER_CASE:
                            TransmissionIdent transmissionIdent = (TransmissionIdent)identOrderElem.getAggregateIdent();
                            break;
                        case AXLE_REAR_NEW:
                        case AXLE_REAR_OLD:
                        case AXLE_FRONT_NEW:
                        case AXLE_FRONT_OLD:
                            AxleIdent axleIdent = (AxleIdent)identOrderElem.getAggregateIdent();
                            break;
                        case CAB:
                            CabIdent cabIdent = (CabIdent)identOrderElem.getAggregateIdent();
                            setSteeringValue(cabIdent.getSteeringKey());
                            break;
                        case AFTER_TREATMENT:
                            ATSIdent atsIdent = (ATSIdent)identOrderElem.getAggregateIdent();
                            break;
                        case STEERING:
                            SteeringIdent steeringIdent = (SteeringIdent)identOrderElem.getAggregateIdent();
                            break;
                        case PLATFORM:
                            PlatformIdent platformIdent = (PlatformIdent)identOrderElem.getAggregateIdent();
                            break;
                        case ELECTRO_ENGINE:
                            ElectroEngineIdent electroEngineIdent = (ElectroEngineIdent)identOrderElem.getAggregateIdent();
                            break;
                        case HIGH_VOLTAGE_BATTERY:
                            HighVoltageBatIdent highVoltageBatIdent = (HighVoltageBatIdent)identOrderElem.getAggregateIdent();
                            break;
                    }

                }
            }

        }
    }

    /**
     * Dialog ohne Datenkarte erzeugen
     * typisch: bei Laden Datenkarte
     */
    public void initDialog() {
        hasChanged = false;
        setLoadedDataCard(null);
        checkModelNo();
        setDirectTransfer(true);
        changeFinDisplay(true);
        setProductGroup("");
        setModelNo("", true);
        setSteeringValue("");
        setFactoryValue("");
        setLabelTechnicalApprovalDateEnhanced(false);
        setTechnicalApprovalDateValue("");
        setDateOfTechnicalStateValue("");
        setCountry("");
        setSpecValidity(null);
        updateAutoProductSelectValues(true);

        // Sichtbarkeit für Felder, die nur für Fahrzeug-Datenkarten angezeigt werden sollen, setzen
        setVehicleOnlyFieldsVisible(true);
    }

    /**
     * Dialog mit Inhalt der Datenkarte vorbelegen
     * - Übernahme aus Datenkartenansicht in Filterdialog
     * - Öffnen Filterdialog (Datenkarte ist die virtuelle aus dem Filter)
     *
     * @param dataCard
     */
    public void initDialog(AbstractDataCard dataCard) {
        if (dataCard != null) {
            // dauernde Updates bei setValue verhindern
            OnEnableButtonsEvent oldOnEnableButtonsEvent = onEnableButtonsEvent;
            onEnableButtonsEvent = null;
            try {
                if (dataCard.isVehicleDataCard()) {
                    initDialog((VehicleDataCard)dataCard);
                } else {
                    initDialog((AggregateDataCard)dataCard);
                }
            } finally {
                // nach der Aktion einmal direkt aufrufen
                onEnableButtonsEvent = oldOnEnableButtonsEvent;
                doOnEnableChangeEvent();
                updateAutoProductSelectValues(true);
            }
        } else {
            initDialog();
        }
    }

    /**
     * Dialog mit Inhalt der Datenkarte befüllen
     * - Übernahme aus Datenkartenansicht in Filterdialog
     * - Öffnen Filterdialog (Datenkarte ist die virtuelle aus dem Filter)
     * <p>
     * Bei der Übernahme aus dem Datenkartendialog hängen alle Daten an der Datenkarte.
     * Bei der Übernahme aus dem Filter kennen wir nur noch die filterrelevanten Werte, wissen aber nicht mehr ob sie aus
     * den Originalwerten einer Datenkarte oder einer Benutzereingabe entstanden sind. Also müssen wir hier die getFilterXXX() Getter
     * verwenden.
     * Da die getFilterXXX() Methoden beide Fälle abdecken, sind sie hier zu verwenden.
     *
     * @param vehicleDataCard
     */
    protected void initDialog(VehicleDataCard vehicleDataCard) {
        hasChanged = false;
        if (vehicleDataCard != null) {
            setDirectTransfer(false);
            setLoadedDataCard(vehicleDataCard);
            setIgnoreAggIdentChangeEvent(true);
            changeFinDisplay(true);
            setLabelTechnicalApprovalDateEnhanced(false);

            // vermutlich kann dieser ganze Block stark zusammengefasst werden
            if (vehicleDataCard.isVirtualDataCard()) {
                // aus Filter geladen
                setInitIdentValue(vehicleDataCard);
                String modelNo = vehicleDataCard.getModelNo();
                if (!modelNo.isEmpty() && !iPartsModel.isVehicleModel(modelNo) && !iPartsModel.isAggregateModel(modelNo)) {
                    modelNo = iPartsConst.MODEL_NUMBER_PREFIX_CAR + modelNo;
                }
                setModelNo(modelNo, true);   // wird eigentlich schon von setFinAsString() aufgerufen
            } else {
                if (vehicleDataCard.isDataCardLoaded()) {
                    setInitIdentValue(vehicleDataCard);
                    setModelNo(vehicleDataCard.getModelNo(), true);  // wird eigentlich schon von setFinAsString() aufgerufen
                } else {
                    if (vehicleDataCard.getFinId().isValidId()) {
                        setFinAsString(vehicleDataCard.getFinId().getFIN(), true);
                    } else {
                        setModelNo(vehicleDataCard.getModelNo(), true);
                    }
                }
            }
            if (textFieldVINIdent != null) {
                if (!StrUtils.isEmpty(vehicleDataCard.getVin())) {
                    setPanelVINVisible(true);
                    textFieldVINIdent.getIdentOrderElem().getAggregateIdent().setIdent(vehicleDataCard.getVin());
                    textFieldVINIdent.setValue(vehicleDataCard.getVin());
                } else {
                    setPanelVINVisible(false);
                }
            }
            setCodes(vehicleDataCard.getCodes());
            setEvent(vehicleDataCard.getEvent());
            setDatacardSaaNumbers(vehicleDataCard.getSaas());
            setAggModelsNumbers(vehicleDataCard.getAggregateModelNumbers());
            setTechnicalApprovalDateValue(vehicleDataCard.getTechnicalApprovalDate());
            setDateOfTechnicalStateValue(vehicleDataCard.getDateOfTechnicalState());
            setSpecValidity(vehicleDataCard.getSpecValidities(getProject()));
            setCountry(vehicleDataCard.getCountry());

            /*
             * Jedenfalls hat eine aus einer geladenen Datenkarte erzeugte virtuelle Datenkarte f.d. Filter beim Wiedereinlesen
             * aus dem Filter auch den Zustand isLoaded (obwohl sie m.E. nicht wissen müsste, wie sie entstanden ist).
             * Deshalb wurde dieser Block nicht ausgeführt. Ich habe daher die ODER-Bed. f.d. virt. Datenkarte ergänzt.
             *
             * Hier werden die gespeicherten Werte aus der virtuellen Datenkarte wieder hergestellt.
             */
            if (!vehicleDataCard.isDataCardLoaded() || vehicleDataCard.isVirtualDataCard()) {
                setSteeringValue(vehicleDataCard.getSteeringValue());
                setGearboxValue(vehicleDataCard.getGearboxValue());
                setProductGroup(vehicleDataCard.getProductGroup());
                setFactoryValue(vehicleDataCard.getFactoryNumber(getProject()));
            }
            updateAutoProductSelectValues(true);
            setIgnoreAggIdentChangeEvent(false);

            // Sichtbarkeit für Felder, die nur für Fahrzeug-Datenkarten angezeigt werden sollen, setzen
            setVehicleOnlyFieldsVisible(true);
        } else {
            initDialog();
        }
    }

    /**
     * Setzt den Inhalt des FIN/VIN Feldes mit zusätzlicher Überprüfung der FIN Daten
     *
     * @param vehicleDataCard
     */
    private void setInitIdentValue(VehicleDataCard vehicleDataCard) {
        if (vehicleDataCard.getFinId().isValidId() && (getSelectedOrderElem().getType() == DatacardIdentOrderTypes.FIN)) {
            // Die FIN ist gültig und es wurde "FIN" in der Combobox ausgewählt -> setze die FIN und überprüfe die
            // abhängigen Werte
            setFinAsString(vehicleDataCard.getFinId().getFIN(), true);
        } else if (getSelectedOrderElem().getType() == DatacardIdentOrderTypes.VIN) {
            // Es wurde "VIN" in der Combobox ausgewählt
            if (vehicleDataCard.getVinId().isValidId()) {
                // Wenn eine gültige VIN existiert, setze die VIN
                setIdentValue(vehicleDataCard.getVinId().getVIN());
            }
            if (vehicleDataCard.getFinId().isValidId()) {
                // Wenn zu der VIN eine gültige FIN existiert, setze die Werte abhängig von der FIN
                checkFINData(null, true);
            }
        } else {
            resetIdent(false);
        }

    }

    /**
     * Siehe Kommentar bei gleicher Methode für Fahrzeugdatenkarte
     */
    protected void initDialog(AggregateDataCard aggregateDataCard) {
        hasChanged = false;
        setDirectTransfer(false);
        setLoadedDataCard(aggregateDataCard);
        setIgnoreAggIdentChangeEvent(true);
        changeFinDisplay(false);

        String modelNo = aggregateDataCard.getModelNo();
        if (!modelNo.isEmpty() && !iPartsModel.isAggregateModel(modelNo)) {
            modelNo = iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + modelNo;
        }
        setModelNo(modelNo, true);
        setEvent(aggregateDataCard.getEvent());
        setCodes(aggregateDataCard.getCodes());
        setDatacardSaaNumbers(aggregateDataCard.getSaas());
        setTechnicalApprovalDateValue(aggregateDataCard.getTechnicalApprovalDate());

        // todo siehe Kommentar an gleicher Stelle in Methode für VehicleDatacard
        if (aggregateDataCard.isDataCardLoaded() || aggregateDataCard.isVirtualDataCard()) {
            setLabelTechnicalApprovalDateEnhanced(aggregateDataCard.isApprovalDateEnriched());
        } else {
            setLabelTechnicalApprovalDateEnhanced(false);
        }
        setSteeringValue(aggregateDataCard.getSteeringValue());
        setGearboxValue(aggregateDataCard.getGearboxValue());
        setProductGroup(aggregateDataCard.getProductGroup());
        String factoryNumber = aggregateDataCard.getFactoryNumber(getProject());
        setFactoryValue(factoryNumber);
        setIgnoreAggIdentChangeEvent(false);

        // Sichtbarkeit für Felder, die nur für Fahrzeug-Datenkarten angezeigt werden sollen, setzen
        setVehicleOnlyFieldsVisible(false);
        if (aggregateDataCard.isDataCardLoaded()) {
            setAdditionalAggregateSNR(aggregateDataCard.getObjectNo());
        } else {
            labelAdditionalAggregateSNR.setVisible(false);
            textFieldAdditionalAggregateSNR.setVisible(false);
        }
    }

    /**
     * Erzeugt eine virtuelle Datenkarte aus der übergebenen Datenkarte und dem Dialoginhalt.
     *
     * @param dataCard
     * @return
     */
    public AbstractDataCard fillDataCard(AbstractDataCard dataCard) {
        if (dataCard == null) {
            dataCard = new VehicleDataCard(true);
        }
        DatacardIdentOrderElem identOrderElem = getSelectedOrderElem();
        if (dataCard.isVehicleDataCard()) {
            if ((identOrderElem.getType() == DatacardIdentOrderTypes.FIN) || (identOrderElem.getType() == DatacardIdentOrderTypes.VIN)) {
                return fillDataCard((VehicleDataCard)dataCard);
            } else {
                return fillDataCard(new AggregateDataCard());
            }
        } else {
            if ((identOrderElem.getType() != DatacardIdentOrderTypes.FIN) && (identOrderElem.getType() != DatacardIdentOrderTypes.VIN)) {
                return fillDataCard((AggregateDataCard)dataCard);
            } else {
                return fillDataCard(new VehicleDataCard());
            }
        }
    }

    public AbstractGuiControl getFocusComponent() {
        return textFieldAggIdent;
    }

    /**
     * Erzeugung einer virtuellen Fahrzeugdatenkarte
     * - für den Filter (Schließen des Filterdialogs mit OK)
     * <p>
     * Die alte Datenkarte wird im Prinzip komplett mit den Werten aus dem Filterdialog überschrieben, allerdings wird
     * die alte Datenkarte dazu verwendet mit {@see AbstractDataCard#isModified} auf Veränderung zu prüfen und dann
     * ein Event auszulösen.
     * <p>
     * Das müsste m.E. genauso funktionieren, wenn ich null übergeben würde da sich die virt. Datenkarte nur aus dem Dialogzustand ergeben müsste
     *
     * @param dataCard die im Filter gespeicherte virtuelle Datenkarte
     * @return
     */
    private VehicleDataCard fillDataCard(VehicleDataCard dataCard) {
        if (dataCard == null) {
            dataCard = new VehicleDataCard(true);
        }
        VehicleDataCard resultDataCard = dataCard.cloneMeAsVirtual();
        DatacardIdentOrderElem identOrderElem = getSelectedOrderElem();
        if (identOrderElem.getType() == DatacardIdentOrderTypes.VIN) {
            resultDataCard.setVin(getVinId().getVIN());
        } else {
            resultDataCard.setFinId(getFinIdFromIdentOrderElement(), getProject());
        }
        resultDataCard.setModelNo(getModelNo(), getProject());
        resultDataCard.setEvent(getEvent());
        resultDataCard.setCodes(getCodes());
        resultDataCard.setSaas(getDatacardSaaNumbers());
        resultDataCard.setAggregateModelNumbers(getAggModelsNumbers());

        resultDataCard.setPskVariants(getPSKVariants());
        SpringFields localSpringFields = getSpringFields();
        localSpringFields.setSpringFields(resultDataCard);

        resultDataCard.setSteeringValue(getSteeringValue());
        resultDataCard.setGearboxValue(getGearboxValue());
        resultDataCard.setProductGroup(getProductGroup());
        resultDataCard.setFactoryNumber(getFactoryValue());
        resultDataCard.setTechnicalApprovalDate(getTechnicalApprovalDateValue());
        resultDataCard.setDateOfTechnicalState(getDateOfTechnicalStateValue());
        resultDataCard.setCountry(getCountry());
        if (isVehicleDatacardLoaded()) {
            VehicleDataCard loadedDataCard = (VehicleDataCard)getLoadedDataCard();
            loadedDataCard.copyAggregateDataCards(resultDataCard);
            resultDataCard.setDataCardFlagsByDataCard(loadedDataCard);
            resultDataCard.setDataCardFlagsByDataCard(null);
        } else {
            resultDataCard.setDataCardFlagsByDataCard(null);
        }
        hasChanged = resultDataCard.isModified(dataCard, false);
        return resultDataCard;
    }

    /**
     * Erzeugung einer virtuellen Aggregatedatenkarte
     * - für den Filter (Schließen des Filterdialogs mit OK)
     * <p>
     * Die alte Datenkarte wird im Prinzip komplett mit den Werten aus dem Filterdialog überschrieben, allerdings wird
     * die alte Datenkarte dazu verwendet mit {@see AbstractDataCard#isModified} auf Veränderung zu prüfen und dann
     * ein Event auszulösen.
     * <p>
     * Das müsste m.E. genauso funktionieren, wenn ich null übergeben würde da sich die virt. Datenkarte nur aus dem Dialogzustand ergeben müsste
     *
     * @param dataCard die im Filter gespeicherte virtuelle Datenkarte
     * @return
     */
    private AggregateDataCard fillDataCard(AggregateDataCard dataCard) {
        if (dataCard == null) {
            dataCard = new AggregateDataCard(true);
        }
        AggregateDataCard resultDataCard = (AggregateDataCard)dataCard.cloneMe();
        if (isAggregateDatacardLoaded()) {
            AggregateDataCard aggregateDataCard = (AggregateDataCard)getLoadedDataCard();
            if (aggregateDataCard.getAggregateType() == DCAggregateTypes.UNKNOWN) {
                setAggregateTypeBySelectedItem(resultDataCard);
            } else {
                resultDataCard.setAggregateBasicType(aggregateDataCard.getAggregateType(), aggregateDataCard.getAggregateTypeOf());
            }
            resultDataCard.setObjectNo(aggregateDataCard.getObjectNo());
            resultDataCard.setObjectNoVariant(aggregateDataCard.getObjectNoVariant());
            resultDataCard.setDataCardFlagsByDataCard(aggregateDataCard);
            resultDataCard.setParentDatacard(aggregateDataCard.getParentDatacard());
        } else {
            setAggregateTypeBySelectedItem(resultDataCard);
            resultDataCard.setDataCardFlagsByDataCard(null);
        }
        resultDataCard.setAggregateIdent(textFieldAggIdent.getIdentOrderElem().getAggregateIdent().getIdent());
        resultDataCard.setModelNo(getModelNo(), getProject());
        resultDataCard.setEvent(getEvent());
        resultDataCard.setCodes(getCodes());
        resultDataCard.setSaas(getDatacardSaaNumbers());
        resultDataCard.setSteeringValue(getSteeringValue());
        resultDataCard.setGearboxValue(getGearboxValue());
        resultDataCard.setProductGroup(getProductGroup());
        resultDataCard.setDataCardFlagsByDataCard(null);
        resultDataCard.setPskVariants(getPSKVariants());

        // Werksnummer über die WMI-Mapping-Tabelle aus dem Werkskennbuchstaben ermitteln (war vor DAIMLER-6138 so)
//        String factorySign = getFactoryValue();
//        String factoryNumber = iPartsFactoryModel.getInstance(getProject()).getFactoryNumberForWMI("", factorySign, new iPartsModelId(getModelNo()),
//                                                                                                   getAggregateType().getDbValue());

        // Nach DAIMLER-6138 enthält die ComboBox mit den Werken nun direkt die factoryNumber als UserObject
        String factoryNumber = getFactoryValue();
        resultDataCard.setFactoryNumber(factoryNumber);

        resultDataCard.setTechnicalApprovalDate(getTechnicalApprovalDateValue());

        resultDataCard.setOldIdentSpecification(textFieldAggIdent.getIdentOrderElem().isOldIdent());

        hasChanged = resultDataCard.isModified(dataCard, false);
        return resultDataCard;
    }

    private void fillAggIdentCombobox() {
        DatacardIdentOrderMap identOrderMap = new DatacardIdentOrderMap(getProject());
        for (DatacardIdentOrderElem identOrderElem : identOrderMap.getDatacardIdentOrderElemMap().values()) {
            comboboxAggIdent.addItem(identOrderElem, identOrderElem.getDescription());
        }
        comboboxAggIdent.setMinimumWidth(comboboxAggIdent.getPreferredWidth());
        comboboxAggIdent.setSelectedIndex(-1); // damit beim initalen setzen der Selektion ein onChange Event kommt
    }

    private void fillAggIdentCombobox(DatacardIdentOrderTypes type) {
        comboboxAggIdent.switchOffEventListeners();
        comboboxAggIdent.removeAllItems();
        DatacardIdentOrderMap identOrderMap = new DatacardIdentOrderMap(getProject());
        for (DatacardIdentOrderElem identOrderElem : identOrderMap.getDatacardIdentOrderElemMap().values()) {
            if (identOrderElem.getType().getDCAggregateType() == type.getDCAggregateType()) {
                comboboxAggIdent.addItem(identOrderElem, identOrderElem.getDescription());
            }
        }
        comboboxAggIdent.setSelectedIndex(-1); // damit beim initalen setzen der Selektion ein onChange Event kommt
        comboboxAggIdent.switchOnEventListeners();
    }

    private DatacardIdentOrderElem getSelectedOrderElem() {
        return comboboxAggIdent.getSelectedUserObject();
    }

    private void setSelectedIndexByAggIdentCombo(DatacardIdentOrderTypes type) {
        fillAggIdentCombobox(type);
        if (!isViewing()) {
            comboboxAggIdent.setEnabled((type == DatacardIdentOrderTypes.FIN) || (type == DatacardIdentOrderTypes.VIN));
        }
        DatacardIdentOrderElem selectedIdentOrderElem = comboboxAggIdent.getSelectedUserObject();
        if ((selectedIdentOrderElem == null) || (selectedIdentOrderElem.getType() != type)) {
            for (DatacardIdentOrderElem identOrderElem : comboboxAggIdent.getUserObjects()) {
                if (identOrderElem.getType() == type) {
                    comboboxAggIdent.setSelectedUserObject(identOrderElem);
                    break;
                }
            }
        }
    }

    private void resetIdent(boolean withClear) {
        textFieldAggIdent.switchOffEventListeners();
        textFieldAggIdent.clear();
        textFieldAggIdent.switchOnEventListeners();
        setFactoryValue(null);
        setSteeringValue("");
        setGearboxValue("");
        calendarTechnicalApprovalDate.clearDate();
        calendarDateOfTechnicalState.clearDate();
        if (withClear) {
            springFields = new SpringFields();
        }
        setSpringFilter(getSpringFields());
        handleDirectTranfer();
    }

    private void changeFinDisplay(boolean isVehicle) {
        if (isVehicle) {
            setIdentValue("");
            if (!DatacardIdentOrderTypes.isVehicleType(getSelectedOrderElem().getType())) {
                // Kommen wir aus einer anderen Datenkarte, dann wird explizit "FIN" gesetzt
                setSelectedIndexByAggIdentCombo(DatacardIdentOrderTypes.FIN);
            } else {
                // Wenn es schon ein Fahrzeug ist, dann setze den Wert der ausgewählt wurde (FIN / VIN)
                setSelectedIndexByAggIdentCombo(getSelectedOrderElem().getType());
            }
        } else {
            if (isLoadedDataCardValid()) {
                AggregateDataCard dataCard = (AggregateDataCard)getLoadedDataCard();
                DatacardIdentOrderTypes identOrderType = DatacardIdentOrderTypes.UNKNOWN;
                switch (dataCard.getAggregateType()) {
                    case ENGINE:
                        identOrderType = DatacardIdentOrderTypes.ENGINE_NEW;
                        if (dataCard.isOldIdentSpecification()) {
                            identOrderType = DatacardIdentOrderTypes.ENGINE_OLD;
                        }
                        DatacardIdentOrderElem identOrderElem = new DatacardIdentOrderElem(getProject(), identOrderType);
                        identOrderElem.getAggregateIdent().setIdent(dataCard.getAggregateIdent());
                        break;
                    case TRANSMISSION:
                        identOrderType = DatacardIdentOrderTypes.TRANSMISSION;
                        iPartsModel currentModel = iPartsModel.getInstance(getProject(), new iPartsModelId(dataCard.getModelNo()));
                        if ((currentModel != null) && currentModel.existsInDB()) {
                            if (currentModel.getAggregateType().equals(DatacardIdentOrderTypes.TRANSMISSION_AUTOMATED.getDbValue())) {
                                identOrderType = DatacardIdentOrderTypes.TRANSMISSION_AUTOMATED;
                            } else if (currentModel.getAggregateType().equals(DatacardIdentOrderTypes.TRANSMISSION_MECHANICAL.getDbValue())) {
                                identOrderType = DatacardIdentOrderTypes.TRANSMISSION_MECHANICAL;
                            }
                        }
                        break;
                    case TRANSFER_CASE:
                        identOrderType = DatacardIdentOrderTypes.TRANSMISSION;
                        break;
                    case AXLE:
                        if (DCAggregateTypeOf.isFront(dataCard.getAggregateTypeOf())) {
                            identOrderType = DatacardIdentOrderTypes.AXLE_FRONT_NEW;
                            identOrderElem = new DatacardIdentOrderElem(getProject(), identOrderType);
                            identOrderElem.getAggregateIdent().setIdent(dataCard.getAggregateIdent());
                            if (!identOrderElem.getAggregateIdent().isEmpty() && !identOrderElem.getAggregateIdent().isValidId()) {
                                identOrderType = DatacardIdentOrderTypes.AXLE_FRONT_OLD;
                            }
                        } else if (DCAggregateTypeOf.isRear(dataCard.getAggregateTypeOf())) {
                            identOrderType = DatacardIdentOrderTypes.AXLE_REAR_NEW;
                            identOrderElem = new DatacardIdentOrderElem(getProject(), identOrderType);
                            identOrderElem.getAggregateIdent().setIdent(dataCard.getAggregateIdent());
                            if (!identOrderElem.getAggregateIdent().isEmpty() && !identOrderElem.getAggregateIdent().isValidId()) {
                                identOrderType = DatacardIdentOrderTypes.AXLE_REAR_OLD;
                            }
                        } else if (dataCard.getAggregateTypeOf() == DCAggregateTypeOf.NONE) {
                            identOrderType = DatacardIdentOrderTypes.AXLE_FRONT_NEW;
                            identOrderElem = new DatacardIdentOrderElem(getProject(), identOrderType);
                            identOrderElem.getAggregateIdent().setIdent(dataCard.getAggregateIdent());
                            if (!identOrderElem.getAggregateIdent().isEmpty() && !identOrderElem.getAggregateIdent().isValidId()) {
                                identOrderType = DatacardIdentOrderTypes.AXLE_FRONT_OLD;
                            }
                        }
                        break;
                    case CAB:
                        identOrderType = DatacardIdentOrderTypes.CAB;
                        break;
                    case AFTER_TREATMENT_SYSTEM:
                        identOrderType = DatacardIdentOrderTypes.AFTER_TREATMENT;
                        break;
                    case ELECTRO_ENGINE:
                        identOrderType = DatacardIdentOrderTypes.ELECTRO_ENGINE;
                        break;
                    case FUEL_CELL:
                        identOrderType = DatacardIdentOrderTypes.FUEL_CELL;
                        break;
                    case HIGH_VOLTAGE_BATTERY:
                        identOrderType = DatacardIdentOrderTypes.HIGH_VOLTAGE_BATTERY;
                        break;
                    case STEERING:
                        identOrderType = DatacardIdentOrderTypes.STEERING;
                        break;
                    case EXHAUST_SYSTEM:
                        //identOrderType = DatacardIdentOrderTypes.TRANSMISSION;
                        break;
                    case PLATFORM:
                        identOrderType = DatacardIdentOrderTypes.PLATFORM;
                        break;
                }
                if (identOrderType == DatacardIdentOrderTypes.UNKNOWN) {
                    identOrderType = DatacardIdentOrderTypes.FIN;
                }
                setIdentValue(dataCard.getAggregateIdent());
                setSelectedIndexByAggIdentCombo(identOrderType);
            } else {
                setIdentValue("");
                setSelectedIndexByAggIdentCombo(DatacardIdentOrderTypes.FIN);
            }
        }
        if (isViewing) {
            labelAggModels.setVisible(isVehicle);
            aggregateModelsButtonTextField.setVisible(isVehicle);
        }
    }

    /**
     * Sucht und setzt das ausgewählte Baumuster
     *
     * @param event
     */
    private void onModelSearchEvent(Event event) {
        final Set<String> foundModelNumbers = new HashSet<>();
        OnChangeSearchValueEvent changeSearchValueCallback = new OnChangeSearchValueEvent() {
            @Override
            public String onChangeSearchValueEvent(String startValue) {
                // Set mit den bereits gefundenen Baumuster-Nummern bei jedem neuen Suchwert leeren
                foundModelNumbers.clear();
                return startValue;
            }
        };

        OnValidateAttributesEvent validationCallback = new OnValidateAttributesEvent() {
            @Override
            public boolean isValid(DBDataObjectAttributes attributes) {
                String currentModelNumber = attributes.getFieldValue(iPartsConst.FIELD_DM_MODEL_NO);
                return foundModelNumbers.add(currentModelNumber);
            }
        };

        EtkDataObjectList.JoinData joinData = new EtkDataObjectList.JoinData(iPartsConst.TABLE_DA_PRODUCT_MODELS,
                                                                             new String[]{ iPartsConst.FIELD_DM_MODEL_NO },
                                                                             new String[]{ iPartsConst.FIELD_DPM_MODEL_NO },
                                                                             false, false);

        SelectSearchGridModel selectSearchGridModel = new SelectSearchGridModel(parentForm);
        selectSearchGridModel.setTitle("!!Baumuster mit Produkt-Verknüpfung");
        selectSearchGridModel.setOnChangeSearchValueEvent(changeSearchValueCallback);
        selectSearchGridModel.setOnValidateAttributesEvent(validationCallback);
        selectSearchGridModel.setJoinData(joinData);
        String modelNo = selectSearchGridModel.showGridSelectionDialog(getModelNo());
        // Im View-Modus nichts übernehmen
        if (isViewing) {
            return;
        }
        // Wenn die Baumusternummer leer ist oder das gleiche Baumuster ausgewählt wurde, dass schon im Feld stand -> nicht setzen
        if (StrUtils.isEmpty(modelNo)) {
            return;
        }

        if ((currentModel != null) && currentModel.getModelId().getModelNumber().equals(modelNo)) {
            return;
        }

        // dauernde Updates verhindern
        OnEnableButtonsEvent oldOnEnableButtonsEvent = onEnableButtonsEvent;
        onEnableButtonsEvent = null;
        try {
            resetIdent(false);
            setDirectTransfer(true);
            setModelNo(modelNo, true);
            setDirectTransfer(false);
            if (isVehicleDatacardLoaded()) {
                updateInfoText(fillDataCard(new VehicleDataCard(true)));
            } else {
                updateInfoText(fillDataCard(new AggregateDataCard(true)));
            }
        } finally {
            // nach der Aktion einmal direkt aufrufen
            onEnableButtonsEvent = oldOnEnableButtonsEvent;
            doOnEnableChangeEvent();
            updateAutoProductSelectValues(false);
            // Ereignis zurücksetzen, da für die neu ausgewählte Baureihe andere Ereignise gültig sind.
            setEvent(null);
        }
    }

    /**
     * Anzeige Federbeine, Federn und Beilagen zur Datenkarte
     *
     * @param event
     */
    private void onSpringSearchEvent(de.docware.framework.modules.gui.event.Event event) {
        String titleAddition = "";
        if (isVehicleDatacardLoaded()) {
            titleAddition = getFinIdFromLoadedDataCard().getFIN();
        }
        if (iPartsSpringDialog.showSpringDialog(getConnector(), parentForm, titleAddition, getSpringFields(), isViewing)) {
            setSpringFilter(getSpringFields());
        }
    }

    private void updateButtonText(AbstractDataCard datacard) {
        String buttonLoadText = "!!Datenkarte laden...";
        if (!isViewing()) {
            if (datacard != null) {
                if (datacard.isVirtualDataCard()) {
                    buttonLoadText = "!!Datenkarte laden...";
                } else if (datacard.isDataCardLoaded()) {
                    if (!datacard.isModelLoaded()) {
                        buttonLoadText = "!!Datenkarte auswählen...";
                    }
                }
            }
        }
        if (buttonLoad != null) {
            buttonLoad.setText(buttonLoadText);
        }
    }

    /**
     * Setzt die Hintergrundfarbe für das Baumustertextfeld in Abhängigkeit vom Baumusterinput
     *
     * @param modelNo
     */
    private void setModelBackgroundColor(String modelNo) {
        boolean isModelNoValid = StrUtils.isEmpty(modelNo);
        if (!isModelNoValid) {
            isModelNoValid = iPartsModel.isModelNumberValid(modelNo) && checkModelNo();
        }
        toggleTextField(buttontextfieldModels, isModelNoValid);
    }

    private void toggleTextField(GuiButtonTextField btnTextField, boolean isValid) {
        if (!isValid) {
            if (originalModelBackgroundColor == null) {
                originalModelBackgroundColor = btnTextField.getTextfieldBackgroundColor();
                if (Colors.clDefault.getColor().equals(originalModelBackgroundColor)) {
                    originalModelBackgroundColor = Colors.clDesignTextFieldEnabledBackground.getColor();
                }
            }
            btnTextField.setTextfieldBackgroundColor(Colors.clDesignErrorBackground.getColor());
        } else {
            // die Hintergrundfarbe des Textfelds wiederherstellen
            if (originalModelBackgroundColor != null) {
                btnTextField.setTextfieldBackgroundColor(originalModelBackgroundColor);
                originalModelBackgroundColor = null;
            }
        }
    }

    private void updateSourceText(AbstractDataCard datacard) {
        String text = "";
        if (!isViewing()) {
            if (datacard != null) {
                if (datacard.isModelLoaded()) {
                    // Model-Loaded Flag = true
                    if (datacard.isDataCardLoaded()) {
                        // Datenkarte via WS geladen
                        if (datacard.isAggregateDataCard()) {
                            text = TranslationHandler.translate("!!Aus %1-Datenkarte (%2) geladen",
                                                                TranslationHandler.translate(iPartsDataCardDialog.AGGREGATE_NAME),
                                                                iPartsDataCardDialog.buildAggregateItemString(getProject(), ((AggregateDataCard)datacard)));
                        } else {
                            VehicleDataCard vehicleDataCard = (VehicleDataCard)datacard;
                            String datacardNumber = "";
                            // Abhängig von der Anzeige wird die FIN bzw. VIN gesetzt
                            if (vehicleDataCard.getFinId().isValidId()) {
                                datacardNumber = vehicleDataCard.getFinId().getFIN();
                            } else if (vehicleDataCard.getVinId().isValidId()) {
                                datacardNumber = vehicleDataCard.getVinId().getVIN();
                            }
                            text = TranslationHandler.translate("!!Aus %1-Datenkarte (%2) geladen",
                                                                TranslationHandler.translate(iPartsDataCardDialog.VEHICLE_NAME),
                                                                datacardNumber);
                        }
                    } else {
                        // Datenkarte 'zu Fuß' eingegeben (mit isModelLoaded())
                        if (datacard.isAggregateDataCard()) {
                            // Aggregate-Datenkarte
                            // Ist der Ident befüllt?
                            if (((AggregateDataCard)datacard).getAggregateIdent().isEmpty()) {
                                text = TranslationHandler.translate("!!Datenkarte aus %1-Baumuster befüllt",
                                                                    iPartsModel.isAggregateModel(datacard.getModelNo()) ?
                                                                    TranslationHandler.translate(iPartsDataCardDialog.AGGREGATE_NAME) :
                                                                    TranslationHandler.translate(iPartsDataCardDialog.VEHICLE_NAME));
                            } else {
                                text = TranslationHandler.translate("!!Datenkarte aus Ident befüllt");
                            }
                        } else {
                            // Fahrzeug Datenkarte
                            // Ist der Ident befüllt?
                            if (((VehicleDataCard)datacard).getFinId().isValidId()) {
                                text = TranslationHandler.translate("!!Datenkarte aus FIN befüllt");
                            } else {
                                text = TranslationHandler.translate("!!Datenkarte aus %1-Baumuster befüllt",
                                                                    iPartsModel.isAggregateModel(datacard.getModelNo()) ?
                                                                    TranslationHandler.translate(iPartsDataCardDialog.AGGREGATE_NAME) :
                                                                    TranslationHandler.translate(iPartsDataCardDialog.VEHICLE_NAME));
                            }
                        }
                    }
                } else {
                    // Model-Loaded Flag = false
                    if (datacard.isDataCardLoaded()) {
                        // Datenkarte via WS geladen
                        if (datacard.isAggregateDataCard()) {
                            text = TranslationHandler.translate("!!Aus %1-Datenkarte (%2) geladen",
                                                                TranslationHandler.translate(iPartsDataCardDialog.AGGREGATE_NAME),
                                                                iPartsDataCardDialog.buildAggregateItemString(getProject(), ((AggregateDataCard)datacard)));
                        } else {
                            text = TranslationHandler.translate("!!Aus %1-Datenkarte (%2) geladen",
                                                                TranslationHandler.translate(iPartsDataCardDialog.VEHICLE_NAME),
                                                                ((VehicleDataCard)datacard).getFinId().getFIN());
                        }
                    } else {
                        text = TranslationHandler.translate("!!Virtuelle Datenkarte");
                    }
                }
            } else {
                text = TranslationHandler.translate("!!Virtuelle Datenkarte");
            }
        }
        panelMain.labelInfo.setText(text);
    }

    private void updateInfoText(AbstractDataCard datacard) {
        updateSourceText(datacard);
        updateButtonText(datacard);
    }

    private void setDirectTransfer(boolean value) {
        saaButtonTextField.setDirectTransfer(value);
        codeButtonTextField.setDirectTransfer(value);
        aggregateModelsButtonTextField.setDirectTransfer(value);
    }

    private void handleDirectTranfer() {
        if (!isViewing()) {
            if (isLoadedDataCardValid() && getLoadedDataCard().isDataCardLoaded()) {
                setDirectTransfer(false);
            } else {
                setDirectTransfer(true);
            }
        }
    }

    private void setLabelTechnicalApprovalDateEnhanced(boolean value) {
        if (value) {
            labelTechnicalApprovalDate.setFontStyle(DWFontStyle.BOLD);
        } else {
            labelTechnicalApprovalDate.setFontStyle(DWFontStyle.PLAIN);
        }
    }

    /**
     * Aggregatetyp in Datenkarte setzen aufgrund der im Dialog eingestellten Aggregateart
     *
     * @param resultDataCard
     */
    private void setAggregateTypeBySelectedItem(AggregateDataCard resultDataCard) {
        DatacardIdentOrderElem identOrderElem = getSelectedOrderElem();
        switch (identOrderElem.getType()) {
            case ENGINE_NEW:
            case ENGINE_OLD:
                resultDataCard.setAggregateBasicType(DCAggregateTypes.ENGINE, DCAggregateTypeOf.NONE);
                break;
            case TRANSMISSION:
            case TRANSMISSION_AUTOMATED:
            case TRANSMISSION_MECHANICAL:
                resultDataCard.setAggregateBasicType(DCAggregateTypes.TRANSMISSION, DCAggregateTypeOf.NONE);
                break;
            case TRANSFER_CASE:
                resultDataCard.setAggregateBasicType(DCAggregateTypes.TRANSFER_CASE, DCAggregateTypeOf.NONE);
                break;
            case AXLE_REAR_NEW:
            case AXLE_REAR_OLD:
                DCAggregateTypeOf aggregateTypeOf = resultDataCard.getAggregateTypeOf();
                if (aggregateTypeOf == DCAggregateTypeOf.UNKNOWN) {
                    aggregateTypeOf = DCAggregateTypeOf.NONE;
                }
                resultDataCard.setAggregateBasicType(DCAggregateTypes.AXLE, aggregateTypeOf);
                break;
            case AXLE_FRONT_NEW:
            case AXLE_FRONT_OLD:
                aggregateTypeOf = resultDataCard.getAggregateTypeOf();
                if (aggregateTypeOf == DCAggregateTypeOf.UNKNOWN) {
                    aggregateTypeOf = DCAggregateTypeOf.NONE;
                }
                resultDataCard.setAggregateBasicType(DCAggregateTypes.AXLE, aggregateTypeOf);
                break;
            case CAB:
                resultDataCard.setAggregateBasicType(DCAggregateTypes.CAB, DCAggregateTypeOf.NONE);
                break;
            case AFTER_TREATMENT:
                resultDataCard.setAggregateBasicType(DCAggregateTypes.AFTER_TREATMENT_SYSTEM, DCAggregateTypeOf.NONE);
                break;
            case STEERING:
                resultDataCard.setAggregateBasicType(DCAggregateTypes.STEERING, DCAggregateTypeOf.NONE);
                break;
            case PLATFORM:
                resultDataCard.setAggregateBasicType(DCAggregateTypes.PLATFORM, DCAggregateTypeOf.NONE);
                break;
        }
    }

    private void doOnEnableChangeEvent() {
        if (onEnableButtonsEvent != null) {
            onEnableButtonsEvent.doEnableButtons();
        }
    }

    private GuiLabel createLabel(int y, String labelName, String labelText) {
        GuiLabel label = new GuiLabel();
        label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
        label.setName("label_" + labelName);
        label.__internal_setGenerationDpi(96);
        label.registerTranslationHandler(getUITranslationHandler());
        label.setScaleForResolution(true);
        label.setMinimumWidth(10);
        label.setMinimumHeight(10);
        label.setText(labelText);
        label.setConstraints(createLabelConstraints(y));
        return label;
    }

    private ConstraintsGridBag createLabelConstraints(int y) {
        return new ConstraintsGridBag(0, y, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                      4, 8, 4, 4);
    }

    private ConstraintsGridBag createFieldConstraints(int y, int gridwidth) {
        return new ConstraintsGridBag(1, y, gridwidth, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                                      4, 4, 4, 8);
    }

    public boolean isAutoSelectedProductsPSK() {
        return isAutoSelectedProductsPSK;
    }

    public static class SpringFields {

        protected java.util.List<String> activeSpringLegFront; // VPD: 00064: ZB-Federbein vorn
        protected java.util.List<String> activeSpringRear; // VPD: 00065: Feder hinten
        protected java.util.List<String> activeSpringShimRear; // VPD: 00066: Federbeilage hinten

        public SpringFields() {
            activeSpringLegFront = new DwList<>();
            activeSpringRear = new DwList<>();
            activeSpringShimRear = new DwList<>();
        }

        public SpringFields(VehicleDataCard vehicleDataCard) {
            activeSpringLegFront = vehicleDataCard.getActiveSpringLegFront();
            activeSpringRear = vehicleDataCard.getActiveSpringRear();
            activeSpringShimRear = vehicleDataCard.getActiveSpringShimRear();
        }

        public void setSpringFields(VehicleDataCard vehicleDataCard) {
            vehicleDataCard.setActiveSpringLegFront(activeSpringLegFront);
            vehicleDataCard.setActiveSpringRear(activeSpringRear);
            vehicleDataCard.setActiveSpringShimRear(activeSpringShimRear);
        }

        public void setActiveSpringLegFront(java.util.List<String> activeSpringLegFront) {
            this.activeSpringLegFront = activeSpringLegFront;
        }

        public void setActiveSpringRear(java.util.List<String> activeSpringRear) {
            this.activeSpringRear = activeSpringRear;
        }

        public void setActiveSpringShimRear(java.util.List<String> activeSpringShimRear) {
            this.activeSpringShimRear = activeSpringShimRear;
        }

        public boolean isValid() {
            return !activeSpringLegFront.isEmpty() || !activeSpringRear.isEmpty() || !activeSpringShimRear.isEmpty();
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        panelMain = new PanelMainClass(translationHandler);
        panelMain.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected PanelMainClass panelMain;

    private class PanelMainClass extends de.docware.framework.modules.gui.controls.GuiPanel {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelFilterValues;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelInfo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelInfo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelLastFin;

        private PanelMainClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(panelMainLayout);
            panelFilterValues = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelFilterValues.setName("panelFilterValues");
            panelFilterValues.__internal_setGenerationDpi(96);
            panelFilterValues.registerTranslationHandler(translationHandler);
            panelFilterValues.setScaleForResolution(true);
            panelFilterValues.setMinimumWidth(10);
            panelFilterValues.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelFilterValuesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelFilterValues.setLayout(panelFilterValuesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelFilterValuesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelFilterValues.setConstraints(panelFilterValuesConstraints);
            this.addChild(panelFilterValues);
            panelInfo = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelInfo.setName("panelInfo");
            panelInfo.__internal_setGenerationDpi(96);
            panelInfo.registerTranslationHandler(translationHandler);
            panelInfo.setScaleForResolution(true);
            panelInfo.setMinimumWidth(10);
            panelInfo.setMinimumHeight(10);
            panelInfo.setPaddingTop(4);
            panelInfo.setPaddingLeft(8);
            panelInfo.setPaddingRight(8);
            panelInfo.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelInfoLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelInfo.setLayout(panelInfoLayout);
            labelInfo = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelInfo.setName("labelInfo");
            labelInfo.__internal_setGenerationDpi(96);
            labelInfo.registerTranslationHandler(translationHandler);
            labelInfo.setScaleForResolution(true);
            labelInfo.setMinimumWidth(10);
            labelInfo.setMinimumHeight(10);
            labelInfo.setText("!!Test");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelInfoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelInfo.setConstraints(labelInfoConstraints);
            panelInfo.addChild(labelInfo);
            labelLastFin = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelLastFin.setName("labelLastFin");
            labelLastFin.__internal_setGenerationDpi(96);
            labelLastFin.registerTranslationHandler(translationHandler);
            labelLastFin.setScaleForResolution(true);
            labelLastFin.setMinimumWidth(10);
            labelLastFin.setMinimumHeight(10);
            labelLastFin.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelLastFinConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelLastFinConstraints.setPosition("east");
            labelLastFin.setConstraints(labelLastFinConstraints);
            panelInfo.addChild(labelLastFin);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelInfoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelInfoConstraints.setPosition("south");
            panelInfo.setConstraints(panelInfoConstraints);
            this.addChild(panelInfo);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
//</editor-fold>
}