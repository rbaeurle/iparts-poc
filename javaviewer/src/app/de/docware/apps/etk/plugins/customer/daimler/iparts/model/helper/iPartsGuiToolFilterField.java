/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.FilterChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiFinModelTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.IdentToDataCardHelper;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.toolbar.AbstractGuiToolComponent;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.Keys;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.guiapps.guidesigner.controls.GUIDesignerProperty;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Panel/iPartsGuiFinModelTextField Komponente, die in die GuiToolbar eingehängt werden kann
 */
public class iPartsGuiToolFilterField extends AbstractGuiToolComponent {

    public static final String TYPE = "toolFilterField";
    public static final String EVENT_TOOL_FILTERFIELD_VALUE_ENTERED = Event.CUSTOM_EVENT_TYPE_PREFIX + "toolfilterfield_value_entered";

    private static final String TOOLTIP_TEXT = "!!FIN/VIN/Baumuster eingeben";
    private static final String PLACEHOLDER_TEXT = "!!FIN/VIN/Baumuster";

    private final AbstractJavaViewerFormIConnector dataConnector;
    private final EtkProject project;
    private iPartsGuiFinModelTextField finModelTextField;
    private boolean isEditable;
    private String lastText;

    public iPartsGuiToolFilterField(AbstractJavaViewerFormIConnector connector) {
        super();
        this.type = TYPE; // Überlagern des tatsächlichen Typs
        this.dataConnector = connector;
        this.project = connector.getProject();
        setMinimumHeightMobile(DEFAULT_MINIMUM_SIZE_MOBILE);
        setPaddingTop(2);
        setPaddingBottom(2);
        reinitComponents();
    }

    public EtkProject getProject() {
        return project;
    }

    @Override
    protected void reinitComponents() {
        if (toolbar == null) {
            return;
        }

        if (finModelTextField == null) {
            finModelTextField = new iPartsGuiFinModelTextField(true);
            finModelTextField.setConstraints(new ConstraintsBorder(LayoutBorder.POSITION_CENTER));
            finModelTextField.setEditable(isEditable);
            setLayout(null); // Hier wird BorderLayout gesetzt
            finModelTextField.setMinimumWidth(119);
            finModelTextField.initForVinFallback(getProject());
            lastText = null;

            Runnable changeFilterRunnable = () -> {
                String ident = finModelTextField.getModelNo();
                if (ident.isEmpty()) {
                    if (iPartsFilter.get().isFilterActive()) {
                        iPartsFilter.get().disableAllFilters();
                        getProject().fireProjectEvent(new FilterChangedEvent());
                    }
                } else {
                    IdentToDataCardHelper.activateFilterForIdentWithDataCard(ident.trim(), dataConnector);
                }
            };

            EventListener gainFocusListener = new EventListener(Event.ON_FOCUS_GAINED_EVENT) {
                @Override
                public void fire(Event event) {
                    lastText = finModelTextField.getModelNo();
                }
            };

            EventListener lostFocusListener = new EventListener(Event.ON_FOCUS_LOST_EVENT) {
                @Override
                public void fire(Event event) {
                    if (!Utils.objectEquals(lastText, finModelTextField.getModelNo())) {
                        changeFilterRunnable.run();
                    }
                }
            };

            setBackgroundColor(Colors.clTransparent.getColor());
            final GuiPanel finalThis = this;
            finModelTextField.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                public void fire(Event event) {
                    fireEvent(EventCreator.createOnChangeEvent(finalThis, finalThis.getUniqueId()));
                }
            });
            finModelTextField.setEventHotkeys(Keys.KEY_ENTER);
            finModelTextField.addEventListener(new EventListener(Event.HOTKEY_EVENT) {
                @Override
                public void fire(Event event) {
                    changeFilterRunnable.run();
                }
            });
            finModelTextField.addEventListener(gainFocusListener);
            finModelTextField.addEventListener(lostFocusListener);
            setToolTipAndPlaceHolder();
            addChild(finModelTextField);
            setTextAndTooltip();
        }
    }

    /**
     * Je nachdem, ob der Filter aktiv ist, die zugehörige FIN/VIN oder Baumuster in TextField eintragen und Tooltip bestimmen
     */
    private void setTextAndTooltip() {
        iPartsFilter filter = iPartsFilter.get();
        if (filter.isFilterActive()) {
            AbstractDataCard dataCard = filter.getCurrentDataCard();
            boolean isLoaded = dataCard.isDataCardLoaded();
            if (dataCard.isVehicleDataCard()) {
                VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                // FIN
                String fin = null;
                FinId finId = vehicleDataCard.getFinId();
                if ((finId != null) && finId.isValidId()) {
                    fin = finId.getFIN();
                }
                if (StrUtils.isValid(fin)) {
                    finModelTextField.setFinId(finId);
                    if (isLoaded) {
                        setFinModelToolTip("!!Filter aktiv mit FIN %1, Datenkarte geladen", fin);
                    } else {
                        setFinModelToolTip("!!Filter aktiv mit FIN %1", fin);
                    }
                    return;
                }

                // VIN
                String vin = vehicleDataCard.getVin();
                if (StrUtils.isValid(vin) && !Utils.objectEquals(vin, fin)) {
                    finModelTextField.setVin(vin);
                    if (isLoaded) {
                        setFinModelToolTip("!!Filter aktiv mit VIN %1, Datenkarte geladen", vin);
                    } else {
                        setFinModelToolTip("!!Filter aktiv mit VIN %1", vin);
                    }
                    return;
                }
            } else if (dataCard.isAggregateDataCard()) {
                // Bei einer Agg-Datenkarte Filter aktiv und Feld leer lassen. Über den Tooltip weiß der Autor, dass
                // eine Agg-Datenkarte zum Filtern benutzt wird. Der Ident wird absichtlich nicht angegeben, weil man hier
                // erzwingen möchte, dass der autor den Dialog öffnet
                finModelTextField.setText("");
                if (isLoaded) {
                    setFinModelToolTip("!!Filter aktiv mit geladener Aggregate-Datenkarte");
                } else {
                    setFinModelToolTip("!!Filter aktiv mit Aggregate-Datenkarte");
                }

                // Bei einem vorhandenen Aggregate-Ident auch nicht das Baumuster anzeigen (weil das nur halbrichtig wäre)
                if (StrUtils.isValid(((AggregateDataCard)dataCard).getAggregateIdent())) {
                    return;
                }
            }

            // Baumuster
            String modelNo = dataCard.getModelNo();
            if (StrUtils.isValid(modelNo)) {
                finModelTextField.setModelNo(modelNo);
                setFinModelToolTip("!!Filter aktiv mit Baumuster %1", modelNo);
                return;
            }

        } else {
            setToolTipAndPlaceHolder();
        }
    }

    private void setToolTipAndPlaceHolder() {
        finModelTextField.setTooltip(TOOLTIP_TEXT);
        finModelTextField.setPlaceHolderText(PLACEHOLDER_TEXT);
    }

    private void setFinModelToolTip(String key, String... placeHolderTexts) {
        finModelTextField.setTooltip(TranslationHandler.translate(key, placeHolderTexts));
    }

    @Override
    public boolean isOfType(String type) {
        if (super.isOfType(type)) {
            return true;
        }
        if (type.equals(AbstractGuiToolComponent.TYPE)) {
            return true;
        }
        return false;
    }

    /**
     * Liefere eine Liste der Event-Arten, die für diesen Komponententyp registrierbar sind.
     */
    @Override
    public java.util.List<String> getEventTypes() {
        return new ArrayList<>(Arrays.asList(Event.ON_CHANGE_EVENT));
    }

    @Override
    protected void cloneProperties(AbstractGuiControl control) {
        if (control.isOfType(TYPE)) {
//            GuiToolCombobox toolCombobox = (GuiToolCombobox)control;
//            toolCombobox.combobox = (GuiComboBox)combobox.cloneMe();
//            toolCombobox.maximumRowCount = maximumRowCount;
        }
    }

    @GUIDesignerProperty
    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
        if (finModelTextField != null) {
            finModelTextField.setEditable(isEditable);
        }
    }
}
