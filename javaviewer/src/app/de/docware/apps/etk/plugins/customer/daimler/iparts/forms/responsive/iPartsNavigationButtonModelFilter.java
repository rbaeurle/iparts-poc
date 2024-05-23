/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.responsive;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.events.FilterChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.IdentToDataCardHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.framework.modules.gui.dialogs.inputdialog.InputDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.base.AbstractItem;
import de.docware.framework.modules.gui.responsive.base.RButtonImages;
import de.docware.framework.modules.gui.responsive.base.actionitem.ActionItem;
import de.docware.framework.modules.gui.responsive.base.actionitem.ActionItemEvents;
import de.docware.framework.modules.gui.responsive.components.navigationmenu.RNavigationButtonModel;
import de.docware.framework.modules.gui.responsive.components.navigationmenu.RNavigationMenuHierarchyGroup;
import de.docware.util.StrUtils;
import de.docware.util.Utils;


/**
 * Navigationbutton-Eintrag für den iParts-Filter.
 */
public class iPartsNavigationButtonModelFilter extends RNavigationButtonModel {

    public iPartsNavigationButtonModelFilter(AbstractJavaViewerForm parentForm) {
        setActionItem(createActionItem(parentForm.getConnector()));
        setHierarchyGroup(RNavigationMenuHierarchyGroup.AFTER_FIRST);
    }

    private ActionItem createActionItem(final AbstractJavaViewerFormIConnector dataConnector) {
        final RButtonImages buttonImagesUnChecked = new RButtonImages(DefaultImages.t2filter.getImage(), DefaultImages.t2filterHover.getImage(),
                                                                      DefaultImages.t2filterHover.getImage(), DefaultImages.t2filter.getImage());
        final RButtonImages buttonImagesChecked = new RButtonImages(DefaultImages.t2filterActive.getImage(), DefaultImages.t2filterActiveHover.getImage(),
                                                                    DefaultImages.t2filterActiveHover.getImage(), DefaultImages.t2filterActive.getImage());
        ActionItemEvents filterActionItemEvents = new ActionItemEvents(null) {

            private String lastIdentValue = "";

            @Override
            public void click(AbstractItem mainItem, Event event) {
                showIdentInputDialog(lastIdentValue);
            }

            private void showIdentInputDialog(String identValue) {
                String ident = InputDialog.show("!!Filter ändern", "!!FIN, VIN oder Baumuster eingeben", identValue, false);
                if (ident != null) {
                    if (ident.isEmpty()) {
                        lastIdentValue = ident;
                        iPartsFilter.get().disableAllFilters();
                        dataConnector.getProject().fireProjectEvent(new FilterChangedEvent());
                    } else {
                        AbstractDataCard dataCard = IdentToDataCardHelper.activateFilterForIdentWithDataCard(ident.trim(), dataConnector);
                        if (dataCard != null) {
                            lastIdentValue = ident;
                        } else {
                            showIdentInputDialog(ident); // Eingabe-Dialog erneut anzeigen mit dem letzten Ident bei einem Fehler
                        }
                    }
                }
            }

            @Override
            public String getText() {
                return TranslationHandler.translate("!!Filter");
            }

            @Override
            public String getTooltip() {
                iPartsFilter filter = iPartsFilter.get();
                if (filter.isFilterActive()) {
                    String tooltip = TranslationHandler.translate("!!Filter aktiv");
                    AbstractDataCard dataCard = filter.getCurrentDataCard();
                    if (dataCard.isVehicleDataCard()) {
                        VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;

                        // FIN
                        String fin = null;
                        FinId finId = vehicleDataCard.getFinId();
                        if ((finId != null) && finId.isValidId()) {
                            fin = finId.getFIN();
                        }
                        if (StrUtils.isValid(fin)) {
                            tooltip += "\nFIN: " + fin;
                        }

                        // VIN
                        String vin = vehicleDataCard.getVin();
                        if (StrUtils.isValid(vin) && !Utils.objectEquals(vin, fin)) {
                            tooltip += "\nVIN: " + vin;
                        }
                    }

                    // Baumuster
                    if (StrUtils.isValid(dataCard.getModelNo())) {
                        tooltip += "\n" + TranslationHandler.translate("!!Baumuster") + ": " + dataCard.getModelNo();
                    }

                    // Datenkarte geladen?
                    if (dataCard.isDataCardLoaded()) {
                        tooltip += "\n" + TranslationHandler.translate("!!Datenkarte geladen");
                    }

                    return tooltip;
                } else {
                    return TranslationHandler.translate("!!Filter");
                }
            }

            @Override
            public boolean isVisible() {
                return true;
            }

            @Override
            public RButtonImages getImages() {
                if (iPartsFilter.get().isFilterActive()) {
                    return buttonImagesChecked;
                } else {
                    return buttonImagesUnChecked;
                }
            }
        };

        return new ActionItem("iPartsFilterActionItem", filterActionItemEvents);
    }
}
